package dblockcache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import common.Constants;
import virtualdisk.MyVirtualDisk;

public class MyDBufferCache extends DBufferCache{
	
	private MyVirtualDisk disk;
	private List<DBuffer> lruList;
	private Map<Integer, DBuffer> mapOfDBuffs;

	public MyDBufferCache(int cacheSize) {
		super(cacheSize);
		mapOfDBuffs = new HashMap<Integer, DBuffer>();
		lruList = new LinkedList<DBuffer>();
	}

	@Override
	public DBuffer getBlock(int blockID) {
		//synchronizing on mapOfDBuffs to prevent conflicts
		//check whether it contains it, if it does then return it. 
		DBuffer d;
		if((d = mapOfDBuffs.get(blockID))!=null){
			//marks as busy then gives up control 
			//busy call should probably be made from DFS
			d.setBusy(true);
			promote(d);
			return d;
		}
		//if not, check if full, if it is find LRU, then request it from the vdf and wait, then return	
		else{
			if(mapOfDBuffs.values().size() == _cacheSize){
				findLRUAndRemove();
			}
			d = new MyDBuffer(disk, blockID);
			d.setBusy(true);
			mapOfDBuffs.put(blockID, d);
			promote(d);
			return d;
			//returns a block which hasn't performed I/O, DFS tells it I/O...i think
		}
	}
	
	private void promote(DBuffer d){
		int pos;
		if((pos = lruList.indexOf(d)) != -1){
			lruList.remove(pos);
			lruList.add(d);
		}
		else{
			lruList.add(d);
		}
	}
	
	private void findLRUAndRemove(){
		int i = 0;
		while(lruList.get(i).isBusy()){
			i++;
		}
		DBuffer d = lruList.remove(i);
		mapOfDBuffs.remove(d);
	}

	@Override
	public void releaseBlock(DBuffer buf) {
		//mark block as not held
		buf.setBusy(false);
		
	}

	
	@Override
	public synchronized void sync() {
		for(DBuffer d : mapOfDBuffs.values()){
			if(!d.checkClean()){
				try {
					disk.startRequest(d, Constants.DiskOperationType.WRITE);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		}
	}
	
	public void giveDisk(MyVirtualDisk d){
		disk = d;
	}

}
