package com.xfs.client;

import java.util.ArrayList;
import java.io.IOException;

/**
 * Operations for interacting with the filesystem.
 */
public interface DirectoryManager {
    /**
     * Return a list of files on the local machine at the specified directory
     * @param dirpath Path to directory on local machine
     * @return List of files in the specified directory
     */
    public ArrayList<String> getFileNames(String dirpath);

    /**
     * Print a list of bytes to a file.
     * @param filepath Location and name of file to print to. Note that the name
     * of the file must be included at the end of the path.
     */
    public void printToFile(String filepath, byte[] fileBytes) throws IOException;
}
