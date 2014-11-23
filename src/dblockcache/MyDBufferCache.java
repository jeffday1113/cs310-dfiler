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
	private List<DBuffer> dirtyList;
	private Map<Integer, DBuffer> mapOfDBuffs;

	public MyDBufferCache(int cacheSize) {
		super(cacheSize);
		// TODO Auto-generated constructor stub
		mapOfDBuffs = new HashMap<Integer, DBuffer>();
		dirtyList = new ArrayList<DBuffer>();
	}

	@Override
	public DBuffer getBlock(int blockID) {
		//synchronizing on mapOfDBuffs to prevent conflicts
		//check whether it contains it, if it does then return it. 
		DBuffer d;
		if((d = mapOfDBuffs.get(blockID))!=null){
			//marks as busy then gives up control 
			//busy call should probably be made from DFS
			d.setBusy(true);;
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
			return d;
			//returns a block which hasn't performed I/O, DFS tells it I/O...i think
		}
	}
	
	private DBuffer findLRUAndRemove(){
		//TODO:
		return null;
	}

	@Override
	public void releaseBlock(DBuffer buf) {
		//mark block as not held
		// TODO Auto-generated method stub
		buf.setBusy(false);
		
	}

	@Override
	public synchronized void sync() {
		// TODO Auto-generated method stub
		for(DBuffer d : dirtyList){
			try {
				disk.startRequest(d, Constants.DiskOperationType.WRITE);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void giveDisk(MyVirtualDisk d){
		disk = d;
	}

}
