package com.example.simpleplayer;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileFlattener {
	private static List<String> allFiles = new ArrayList<String>();
	
	public void flattenFolder(String path, int level){
		File origin = new File(path);
		File[] files = origin.listFiles();
		
		if (files == null) return; 
		
		for (File file : files) {
			if (file.isDirectory() && !file.isHidden() && file.canRead() && level > 0) flattenFolder(path + "/" + file.getName(), level-1);
			else if (file.isFile() && file.canRead() && file.getName().endsWith("mp3")) allFiles.add(file.getAbsolutePath());
	    }
	}
	
	public List<String> getFlattenedFiles(){
		return allFiles;
	}
}
