package dfs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import common.Constants;
import common.DFileID;

public class MyDFS extends DFS{
	
	private SortedSet<Integer> myAllocatedBlocks;
	private SortedSet<Integer> myFreeBlocks;
	private Map<Integer, DFile> myFileIDMap;
	
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
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DFileID createDFile() {
		// TODO Auto-generated method stub
		int newIDNum=0;
		while (myFileIDMap.containsKey(newIDNum))
			newIDNum++;
		DFileID newFileID = new DFileID(newIDNum);
		myAllocatedBlocks.add(myFreeBlocks.first());
		myFreeBlocks.remove(myFreeBlocks.first());
		List<Integer> newBlockList = new ArrayList<Integer>();
		newBlockList.add(myAllocatedBlocks.first());
		DFile newFile = new DFile(newFileID, newBlockList);
		myFileIDMap.put(newIDNum, newFile);
		
		
		byte[] newINode = INode.makeByteArrayInodeFromDFile(newFile);
		
		writeINode(newINode);
		
		return newFileID;
	}
	
	private void writeINode(byte[] inode){
		
		
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
		return 0;
	}

	@Override
	public List<DFileID> listAllDFiles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sync() {
		// TODO Auto-generated method stub
		
	}

}
