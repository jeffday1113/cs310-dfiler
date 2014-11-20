package dfs;

import java.util.List;

import common.DFileID;

public class DFile {
	
	private DFileID myFileID;
	private List<Integer> myBlocks;
	private int myNumReaders;
	private int myNumWriters;
	
	public DFile (DFileID fileID, List<Integer> blocks){
		myFileID=fileID;
		myBlocks=blocks;
		myNumReaders=0;
		myNumWriters=0;
	}

}
