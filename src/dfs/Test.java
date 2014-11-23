package dfs;

import java.io.FileNotFoundException;
import java.io.IOException;

import common.DFileID;

public class Test {

	public static void main(String arg[]){
		MyDFS dfs = new MyDFS();
		try {
			dfs.init();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DFileID j = dfs.createDFile();
	}
	
}
