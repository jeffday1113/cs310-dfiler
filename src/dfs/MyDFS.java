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
		
		//I don't think there will be anything in the myFileIDMap at this point
		//Build the dfiles
		  for (int id : myFileIDMap.keySet()) {
	            DFile d = myFileIDMap.get(id);
	            // Check that the size of each DFile is a legal value
	            if (d.getSize() > common.Constants.MAX_FILE_SIZE
	                    * Constants.BLOCK_SIZE)
					try {
						throw new Exception();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            // Check that the block maps of all DFiles have a valid block
	            // number for every block in the DFile
	            for (int i = 0; i < d.getBlocks().size(); i++) {
	                if (d.getPhysicalBlockNumber(i) > common.Constants.NUM_OF_BLOCKS)
						try {
							throw new Exception();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	            }
	        }
		 // Initialize the free and allocated blocks on the virtual disk
		  for (DFile file : myFileIDMap.values()) {
	            for (int j = 0; j < Constants.BLOCK_SIZE; j++) {
	                int blockid = file.getPhysicalBlockNumber(j);
	                if (blockid == -1) break;
	                // Check that no data block is listed for more than one DFile
	                if (blockid != 0) {
	                    if (myAllocatedBlocks.contains(blockid)) { //invalid allocation }
	                    myAllocatedBlocks.add(blockid);
	                }
	            }
	        }
		  }
      for (int i = Constants.MAX_DFILES + 1; i < common.Constants.NUM_OF_BLOCKS; i++) {
          if (!myAllocatedBlocks.contains(i)) {
              myFreeBlocks.add(i);
          }
      }
	}

	@Override
	public synchronized DFileID createDFile() {
		// TODO Auto-generated method stub
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
		byte[] newINode = makeByteArrayInodeFromDFile(fil);
		
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
		// TODO Auto-generated method stub
		//logic to pull each individual block i dont feel like writing it now
		 DFile file = myFileIDMap.get(dFID.getDFileID());
		 
	        if (file == null) return -1;
	        int numblocks = file.getBlocks().size();
	        int offset = startOffset;
	        int done = count;

	        for (int i = 0; i < numblocks; i++) {
	            DBuffer d = dBuffCache.getBlock(file.getPhysicalBlockNumber(i));
	            if (!d.checkValid()) {
	                d.startFetch();
	                d.waitValid();
	            }

	            int read = d.read(buffer, offset, done);
	            done -= read;
	            offset += read;
	        }
	       
	        return count;
	}
	@Override
	public int write(DFileID dFID, byte[] buffer, int startOffset, int count) {
		// TODO Auto-generated method stub
		//same as above but make sure to have a check to see if need to add more blocks to file
	    DFile file = myFileIDMap.get(dFID.getDFileID());
        if (file == null) return -1;
//free and add blocks
		 int numblocks = file.getBlocks().size();
	        int offset = startOffset;
	        int done = count;

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
