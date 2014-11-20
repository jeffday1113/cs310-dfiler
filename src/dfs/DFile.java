package dfs;

import java.util.List;

import common.DFileID;

public class DFile {
	
	private DFileID myFileID;
	private List<Integer> myBlocks;
	
	public DFile (DFileID fileID, List<Integer> blocks){
		myFileID=fileID;
		myBlocks=blocks;
	}

}
