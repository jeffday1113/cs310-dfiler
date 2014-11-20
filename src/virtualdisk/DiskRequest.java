package virtualdisk;

import common.Constants.DiskOperationType;

import dblockcache.DBuffer;

public class DiskRequest {
	
	private DBuffer myBuf;
	private DiskOperationType myOp;
	
	public DiskRequest(DBuffer buf, DiskOperationType operation){
		myBuf=buf;
		myOp=operation;
	}
	
	public DBuffer getDBuffer(){
		return myBuf;
	}
	
	public DiskOperationType getOpType(){
		return myOp;
	}

}
