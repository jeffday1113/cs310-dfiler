package dfs;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import common.Constants;
import common.DFileID;

public class MyDFS extends DFS{
	
	private Set<Integer> myAllocatedBlocks;
	private Set<Integer> myFreeBlocks;
	
	MyDFS(String volName, boolean format) {
		super(volName, format);
		myAllocatedBlocks=new HashSet<Integer>();
		myFreeBlocks=new HashSet<Integer>();
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
		return null;
	}

	@Override
	public void destroyDFile(DFileID dFID) {
		// TODO Auto-generated method stub
		
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
