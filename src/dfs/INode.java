package dfs;
import java.nio.ByteBuffer;

import common.Constants;

public class INode {
	
	public byte[] makeByteArrayInodeFromDFile(DFile d){
		byte[] ret = new byte[ Constants.INODE_SIZE ];
		byte[] sizeBytes = ByteBuffer.allocate(4).putInt(d.size()).array();
		byte[] idBytes = ByteBuffer.allocate(4).putInt(d.id().getDFileID()).array();
		for(int i = 0; i < 4; i++){
			ret[i] = sizeBytes[i];
			ret[i+4] = idBytes[i];
		}
		int pos = 8;
		for(Integer i:d.getBlocks()){
			byte[] mapBytes = ByteBuffer.allocate(4).putInt(i).array();
			for(int j = 0; j < 4; j++){
				ret[j + pos] = mapBytes[j];
			}
			pos = pos+4;
		}
		return ret;
	}

}
