package dfs;

import java.util.List;

import common.DFileID;


public class DFile {
	
	private DFileID myFileID;
	private List<Integer> myBlocks;
	private int myNumReaders;
	private int myNumWriters;
	private int myNumWaitingWriters;
	private int size;
	
	
	public DFile (DFileID fileID, List<Integer> blocks){
		myFileID=fileID;
		myBlocks=blocks;
		myNumReaders=0;
		myNumWriters=0;
		size = blocks.size();
	}
	
	public void addBlock(int blockid){
		myBlocks.add(blockid);
	}
	
	public int getSize(){
		return size;
	}
	
	public DFileID getID(){
		return myFileID;
	}
	
	public List<Integer> getBlocks(){
		return myBlocks;
	}
	public int getPhysicalBlockNumber (int block) {
        if (block < 0 || block >= myBlocks.size()) return -1;
        return myBlocks.get(block);
    }
	
	public synchronized void lockRead(){
		while(myNumWriters > 0 || myNumWaitingWriters > 0){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		myNumReaders++;
	}
	
	public synchronized void lockWrite(){
		myNumWaitingWriters++;
		while(myNumReaders > 0 || myNumWriters > 0){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		myNumWriters++;
		myNumWaitingWriters--;
	}
	
	public synchronized void releaseRead(){
		myNumReaders--;
		notifyAll();
	}
	
	public synchronized void releaseWrite(){
		myNumWriters--;
		notifyAll();
	}
}
