package com.xfs.client;

import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class XFSDirectoryManager implements DirectoryManager {
    /**
     * Read directory for the list of files that the peer currently has
     */
    @Override
    public ArrayList<String> getFileNames(String dirpath) {
	return getFileNames(new File(dirpath));
    }

    /**
     * Read directory for the list of files that the peer currently has.
     * Recursively search the directory supplied as an argument to find
     * files.
     * @return List of files at the specified directory.
     */
    public ArrayList<String> getFileNames(File shareFolder) {
	File[] files = shareFolder.listFiles();
	ArrayList<String> shareList = new ArrayList<String>();
	for(File fileEntry : shareFolder.listFiles()) {
	    if(fileEntry.isDirectory()) {
		shareList.addAll(getFileNames(fileEntry));
	    } else {
		String name = fileEntry.getName();
		shareList.add(name);
	    }
	}
	return shareList;
    }

    /**
     * Print the byte array to a specified filepath. The name of the file
     * must be included as part of the filepath.
     */
    @Override
    public void printToFile(String filepath, byte[] fileBytes)
	throws IOException {
	try {
	    BufferedOutputStream toFile =
		new BufferedOutputStream(new FileOutputStream(filepath));
	    if(fileBytes.length >= 0) {
		toFile.write(fileBytes, 0, fileBytes.length-1);
	    }
	    toFile.flush();
	    toFile.close();
	} catch(IOException e) {
	    new File(filepath).delete();
	    throw e;
	}
    }
}
