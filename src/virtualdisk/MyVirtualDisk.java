package virtualdisk;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import common.Constants;
import common.Constants.DiskOperationType;
import dblockcache.DBuffer;

public class MyVirtualDisk extends VirtualDisk{

	private Queue<DiskRequest> myRequestQueue;

	public MyVirtualDisk() throws FileNotFoundException, IOException {
		super();
		// TODO Auto-generated constructor stub
		myRequestQueue = new LinkedList<DiskRequest>();
	}

	@Override
	public void startRequest(DBuffer buf, DiskOperationType operation)
			throws IllegalArgumentException, IOException {
		// TODO Auto-generated method stub
		synchronized(myRequestQueue){
			myRequestQueue.add(new DiskRequest(buf, operation));
			myRequestQueue.notifyAll();
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		DiskRequest currentRequest;
		synchronized(myRequestQueue){
			while (!myRequestQueue.isEmpty()){
				currentRequest = myRequestQueue.poll();
				if (currentRequest!=null){
					try{
						if (currentRequest.getOpType()==Constants.DiskOperationType.READ)
							readBlock(currentRequest.getDBuffer());
						else
							writeBlock(currentRequest.getDBuffer());
					}
					catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					finally{
						currentRequest.getDBuffer().ioComplete();
					}
				}
			}
			try {
				myRequestQueue.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
