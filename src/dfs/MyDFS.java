package dfs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import virtualdisk.MyVirtualDisk;
import common.Constants;
import common.DFileID;
import dblockcache.DBuffer;
import dblockcache.MyDBufferCache;

public class MyDFS extends DFS{
	
	private SortedSet<Integer> myAllocatedBlocks;
	private SortedSet<Integer> myFreeBlocks;
	private Map<Integer, DFile> myFileIDMap;
	private MyDBufferCache dBuffCache;
	
	MyDFS(String volName, boolean format) {
		super(volName, format);
		myAllocatedBlocks=new TreeSet<Integer>();
		myFreeBlocks=new TreeSet<Integer>();
		myFileIDMap = new HashMap<Integer, DFile>();
	}

	MyDFS(boolean format) {
		//super(format);
		this(Constants.vdiskName, format);
	}

	MyDFS() {
		this(Constants.vdiskName, false);
	}
	
	
	@Override
	public void init() throws FileNotFoundException, IOException {
		dBuffCache = new MyDBufferCache(Constants.NUM_OF_CACHE_BLOCKS);
		MyVirtualDisk disk = new MyVirtualDisk(_volName, _format);
		dBuffCache.giveDisk(disk);
		
		DBuffer block0 = dBuffCache.getBlock(0);
		block0.startFetch();
		block0.waitValid();
		//check for info in block0 to make sure disk is chill

		for(int i = 1; i < Constants.MAX_DFILES; i++){
			DBuffer inode = dBuffCache.getBlock(i);
			inode.startFetch();
			inode.waitValid();
			DFile fil = makeDFileFromInode(inode);
			if(fil != null){
				myFileIDMap.put(i, fil);
			}
		}
		
		for(DFile fil:myFileIDMap.values()){
			for(Integer i:fil.getBlocks()){
				myAllocatedBlocks.add(i);
			}
		}
		
		for(int i = Constants.MAX_DFILES + 1; i < Constants.NUM_OF_BLOCKS; i++){
			if(!myAllocatedBlocks.contains(i)){
				myFreeBlocks.add(i);
			}
		}

	}

	@Override
	public synchronized DFileID createDFile() {
		int newIDNum=1;
		while (myFileIDMap.containsKey(newIDNum)){
			newIDNum++;
		}
		DFileID newFileID = new DFileID(newIDNum);
		//right now these keep track of all blocks - should only keep track of non-inodes
		//make sure to initialize these in init
		//should really change the structure of this
		myAllocatedBlocks.add(myFreeBlocks.first());
		myFreeBlocks.remove(myFreeBlocks.first());
		List<Integer> newBlockList = new ArrayList<Integer>();
		//does this work? not sure how sortedsets work...should do differently
		newBlockList.add(myAllocatedBlocks.first());
		DFile newFile = new DFile(newFileID, newBlockList);
		myFileIDMap.put(newIDNum, newFile);
		
		writeINode(newFile);
		
		return newFileID;
	}
	
	private void writeINode(DFile fil){
		//need to request block associated with file - needs more arguments
		//then writes to that block, then pushes block
		byte[] newINode = makeInodeFromDFile(fil);
		
		//the file needs to know its inode location - we can make it its ID if
		//the id's are given in the right manner
		
		DBuffer db = dBuffCache.getBlock(fil.getID().getDFileID());
		db.write(newINode, 0, Constants.BLOCK_SIZE);
		db.startPush();
	}

	@Override
	public synchronized void destroyDFile(DFileID dFID) {
		//Get the file associated with the fileID and the blocks
		DFile fileToDestroy = myFileIDMap.get(dFID.getDFileID());
		List<Integer> fileBlocks = fileToDestroy.getBlocks();
		for (Integer i : fileBlocks){
			myAllocatedBlocks.remove(i);
			myFreeBlocks.add(i);
		}
		
		myFileIDMap.remove(dFID.getDFileID());
		//prevents initialization errors
		writeINode(fileToDestroy);
		
	}

	@Override
	public int read(DFileID dFID, byte[] buffer, int startOffset, int count) {
		//logic to pull each individual block i dont feel like writing it now
		 DFile file = myFileIDMap.get(dFID.getDFileID());
		 
	        if (file == null) return -1;
	        file.lockRead();
	        int numblocks = file.getBlocks().size();
	        int offset = startOffset;
	        int done = count;

	        for (int i = 0; i < numblocks; i++) {
	        	if(done > 0){
	        		DBuffer d = dBuffCache.getBlock(file.getPhysicalBlockNumber(i));
	        		if (!d.checkValid()) {
	        			d.startFetch();
	        			d.waitValid();
	        		}


	        		int read = d.read(buffer, offset, done);
	        		done -= read;
	        		offset += read;
	        	}
	        }
	       file.releaseRead();
	        return count;
	}
	
	@Override
	public int write(DFileID dFID, byte[] buffer, int startOffset, int count) {
		//same as above but make sure to have a check to see if need to add more blocks to file
		DFile file = myFileIDMap.get(dFID.getDFileID());
		if (file == null) return -1;
		//free and add blocks
		
		file.lockWrite();
		int numblocks = file.getBlocks().size();
		int moreBlocks = (int) Math.ceil((double) count / Constants.BLOCK_SIZE) - numblocks;
		int offset = startOffset;
		int done = count;
		if(moreBlocks > 0){
			for(int i = 0; i < moreBlocks; i++){
				file.addBlock(myFreeBlocks.first());
				myAllocatedBlocks.add(myFreeBlocks.first());
				myFreeBlocks.remove(myFreeBlocks.first());
			}
		}
		for (int i = 0; i < numblocks; i++) {
			DBuffer d = dBuffCache.getBlock(file.getPhysicalBlockNumber(i));
			if (!d.checkValid()) {
				d.startFetch();
				d.waitValid();
			}

			int write = d.write(buffer, offset, done);
			done -= write;
			offset += write;
		}
		writeINode(file);
		file.releaseWrite();
		return count;
	}

	@Override
	public int sizeDFile(DFileID dFID) {
		// TODO Auto-generated method stub
		//this should really be a method of DFile..
		DFile file = myFileIDMap.get(dFID.getDFileID());
		return file.getSize();
	}

	@Override
	public List<DFileID> listAllDFiles() {
		List<DFileID> allFilesList = new ArrayList<DFileID>();
		for (Integer i : myFileIDMap.keySet()){
			allFilesList.add(new DFileID(i));
		}
		return allFilesList;
	}

	@Override
	public synchronized void sync() {
		// TODO Auto-generated method stub
		dBuffCache.sync();
	}
	
	private byte[] makeInodeFromDFile(DFile d){
		byte[] ret = new byte[Constants.INODE_SIZE];
		byte[] sizeBytes = ByteBuffer.allocate(4).putInt(d.getSize()).array();
		byte[] idBytes = ByteBuffer.allocate(4).putInt(d.getID().getDFileID()).array();
		for(int i = 0; i < 4; i++){
			ret[i] = sizeBytes[i];
			ret[i+4] = idBytes[i];
		}
		int pos = 8;
		for(Integer i:d.getBlocks()){
			byte[] mapBytes = ByteBuffer.allocate(4).putInt(i).array();
			for(int j = 0; j < 4; j++){
				ret[j + pos] = mapBytes[j];
			}
			pos = pos+4;
		}
		return ret;
	}
	

	
	private DFile makeDFileFromInode(DBuffer inode){
		byte[] ret = new byte[Constants.INODE_SIZE];
		inode.read(ret, 0, ret.length);
		ByteBuffer bb = ByteBuffer.wrap(ret);
		int size = bb.getInt(0);
		if(size == 0){
			return null;
		}
		int eyedee = bb.getInt(1);
		DFileID dfid = new DFileID(eyedee);
		List<Integer> blockList = new ArrayList<Integer>();
		for(int i = 2; i < Constants.MAX_FILE_SIZE + 2; i++){
			if(bb.getInt(i) == 0){
				break;
			}
			blockList.add(bb.getInt(i));
		}
		DFile dfil = new DFile(dfid, blockList);
		return dfil;
	}

}
