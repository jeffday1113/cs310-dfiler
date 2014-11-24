package dblockcache;

import java.io.IOException;

import virtualdisk.MyVirtualDisk;
import common.Constants;

public class MyDBuffer extends DBuffer{
	
	private boolean isBusy;
	private boolean isClean;
	private boolean isValid;
	private MyVirtualDisk disk;
	private int blockLocation;
	private byte[] dBuffer;
	
	public MyDBuffer(MyVirtualDisk d, int loc){
		isBusy = false;
		isClean = false;
		isValid = false;
		disk = d;
		blockLocation = loc;
		dBuffer = new byte[Constants.BLOCK_SIZE];
	}
	
	@Override
	public void startFetch() {
		isBusy = true;
		try {
			disk.startRequest(this, Constants.DiskOperationType.READ);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		isClean = true;
		isValid = true;
	}

	@Override
	public synchronized void startPush() {
		if(isClean){
			return;
		}
		isBusy = true;
		try {
			disk.startRequest(this, Constants.DiskOperationType.WRITE);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		isClean = true;
		notifyAll();
	}

	@Override
	public boolean checkValid() {
		return isValid;
	}

	@Override
	public synchronized boolean waitValid() {
		while(!isValid){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public boolean checkClean() {
		return isClean;
	}

	@Override
	public synchronized boolean waitClean() {
		while(!isClean){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public boolean isBusy() {
		return isBusy;
	}
	
	@Override
	public synchronized void setBusy(boolean b){
		isBusy = b;
	}

	@Override
	public synchronized int read(byte[] buffer, int startOffset, int count) {
		waitValid();
		if(count > Constants.BLOCK_SIZE){
			count = Constants.BLOCK_SIZE;
		}
		for(int i = startOffset, j = 0; j < count; j++, i++){
			buffer[i] = dBuffer[j];
		}
		notifyAll();
		return count;
	}

	@Override
	public synchronized int write(byte[] buffer, int startOffset, int count) {
		if(count > Constants.BLOCK_SIZE){
			count = Constants.BLOCK_SIZE;
		}
		for(int i = startOffset, j = 0; j < count; j++, i++){
			dBuffer[j] = buffer[i];
		}
		isClean = false;
		notifyAll();
		return count;
	}

	@Override
	public synchronized void ioComplete() {
		isBusy = false;
		isClean = true;
		notifyAll();
	}

	@Override
	public int getBlockID() {
		return blockLocation;
	}

	@Override
	public byte[] getBuffer() {
		return dBuffer;
	}

}
