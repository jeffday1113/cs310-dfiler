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
		// TODO Auto-generated method stub
		dBuffCache = new MyDBufferCache(Constants.NUM_OF_CACHE_BLOCKS);
		MyVirtualDisk disk = new MyVirtualDisk(_volName, _format);
		dBuffCache.giveDisk(disk);
		//need to check disk inodes and see if files exist!!!
	}

	@Override
	public DFileID createDFile() {
		// TODO Auto-generated method stub
		int newIDNum=1;
		while (myFileIDMap.containsKey(newIDNum))
			newIDNum++;
		DFileID newFileID = new DFileID(newIDNum);
		myAllocatedBlocks.add(myFreeBlocks.first());
		myFreeBlocks.remove(myFreeBlocks.first());
		List<Integer> newBlockList = new ArrayList<Integer>();
		newBlockList.add(myAllocatedBlocks.first());
		DFile newFile = new DFile(newFileID, newBlockList);
		myFileIDMap.put(newIDNum, newFile);
		
		writeINode(newFile);
		
		return newFileID;
	}
	
	private void writeINode(DFile fil){
		//need to request block associated with file - needs more arguments
		//then writes to that block, then pushes block
		byte[] newINode = makeByteArrayInodeFromDFile(fil);
		
		//the file needs to know its inode location - we can make it its ID if
		//the id's are given in the right manner
		
		DBuffer db = dBuffCache.getBlock(fil.getID().getDFileID());
		db.write(newINode, 0, Constants.BLOCK_SIZE);
		db.startPush();
	}

	@Override
	public void destroyDFile(DFileID dFID) {
		//Get the file associated with the fileID and the blocks
		DFile fileToDestroy = myFileIDMap.get(dFID.getDFileID());
		List<Integer> fileBlocks = fileToDestroy.getBlocks();
		for (Integer i : fileBlocks){
			myAllocatedBlocks.remove(i);
			myFreeBlocks.add(i);
		}
		
		myFileIDMap.remove(dFID.getDFileID());
		
	}

	@Override
	public int read(DFileID dFID, byte[] buffer, int startOffset, int count) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int write(DFileID dFID, byte[] buffer, int startOffset, int count) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sizeDFile(DFileID dFID) {
		// TODO Auto-generated method stub
		DFile file = myFileIDMap.get(dFID.getDFileID());
		return file.getSize();
	}

	@Override
	public List<DFileID> listAllDFiles() {
		List<DFileID> allFilesList = new ArrayList<DFileID>();
		for (Integer i : myFileIDMap.keySet()){
			allFilesList.add(myFileIDMap.get(i).getID());
		}
		return allFilesList;
	}

	@Override
	public void sync() {
		// TODO Auto-generated method stub
		
	}
	
	private byte[] makeByteArrayInodeFromDFile(DFile d){
		byte[] ret = new byte[ Constants.INODE_SIZE ];
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

}
