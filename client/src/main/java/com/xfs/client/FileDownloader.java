package com.xfs.client;

import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;
import com.xfs.common.SocketWrapper;
import com.xfs.common.PeerAction;

/**
 * Implementation of operations for peer to peer communication. Defines the 
 * communication protocol between the peers. For this downloader, peers specify
 * commands to other peers by sending messages in plain text. Files are sent as
 * raw bytes. The possible commands are as follows:
 *    download -> Download::filename
 *      where "filename" is replaced by the name of a file
 *    get load -> GetLoad
 *     
 */
public class FileDownloader implements Downloader {
    /**
     * Probability at which a download should be corrupted. Used for testing 
     * purposes.
     */
    private double threshold = 0;

    public FileDownloader(double threshold) {
	this.threshold = threshold*100;
    }

    /**
     * Download a file from another peer. Verify the checksum upon download
     * to check for corruption.
     * @param filename Name of file
     * @return File as an array of bytes
     * @throws ChecksumMismatchException Thrown when checksum of downloaded
     * file does not match the checksum of the actual file.
     */
    public byte[] download(SocketWrapper sock, String filename)
	throws ChecksumMismatchException, IOException {
	byte[] fileBytes = null;
	try {
	    sock.send("Download::" + filename);

	    byte[] byteArray = sock.receiveFile();
	    if(byteArray.length < 20) {
		System.out.println(byteArray.length);
		throw new IOException();
	    }

	    boolean checksumMatch = verifyChecksum(byteArray);
	    if(!checksumMatch) {
		throw new ChecksumMismatchException();
	    }
 
	    fileBytes = Arrays.copyOfRange(byteArray, 20, byteArray.length);
	    
	} catch(IOException e) {
	    throw e;
	}
	return fileBytes;
    }

    /**
     * Send a file to another peer. The format for sending a file is a byte
     * array with 20-byte checksum of the file at the front, followed by the 
     * actual file after the first 20 bytes.
     */
    public int sendDownload(SocketWrapper sock, byte[] fileBytes) throws IOException {
	try {
	    byte[] checksumBytes = SHA1(fileBytes);
	    byte[] message = new byte[checksumBytes.length + fileBytes.length];
	    for(int i = 0; i < checksumBytes.length; i++) {
		message[i] = checksumBytes[i];
	    }
	    for(int i = 0; i < fileBytes.length; i++) {
		message[i + checksumBytes.length] = fileBytes[i];
	    }

	    //corruptFile(byteArray);

	    sock.sendFile(message);
	} catch(IOException e) {
	    throw e;
	}
	return 1;
    }

    /**
     * Ask another peer for its current load. The communication protocol for
     * this downloader only requires that the peer send the word "GetLoad" to
     * specify a get load operation at another peer. The expected format of the 
     * response is simply an integer.
     * @return Current load at the peer or -1 on failure
     */
    public int getLoad(SocketWrapper sock) {
	int load = 0;
	try {
	    sock.send("GetLoad");
	    load = Integer.parseInt(sock.receive());
	} catch(NumberFormatException e) {
	    return -1;
	} catch(IOException e) {
	    return -1;
	}
	return load;
    }

    /**
     * Send the current number of downloads to an asking peer. The format for the message
     * is simply the number in plain text.
     */
    public int sendLoad(SocketWrapper sock, int concurrentDownloads) throws IOException {
	sock.send("" + concurrentDownloads);
	return 0;
    }

    /**
     * Process a message from another peer according to the communication
     * protocol defined by this downloader.
     * @return A PeerAction object that defines to the caller what action to
     * take.
     */
    public PeerAction processRequest(SocketWrapper sock) throws IOException {
	try {
	    String data = sock.receive();
	    String[] input = data.split("::");
	    //int delay = (latencyList.get(input[1])).intValue();
		
	    switch(input[0]) {
	    case "GetLoad":
		return new PeerAction(PeerAction.SEND_LOAD);
	    case "Download":
		return new PeerAction(PeerAction.SEND_DOWNLOAD, input[1]);
	    default:
		throw new IOException();
	    }
	} catch(IOException e) {
	    throw e;
	}
    }

    /**
     * Utility for changing a byte of a file with a probability according to the
     * threshold field.
     */
    private void corruptFile(byte[] fileBytes) {
	int place;
	Random rand = new Random();
	int chance = rand.nextInt(100);
	if(chance <= this.threshold) {
	    place = rand.nextInt(fileBytes.length);
	    fileBytes[place] = (byte)'\uffff';
	}
    }

    /**
     * Delay to emulate a wide-area network.
     */
    private void emulateDelay() {
	try {
	    //Thread.sleep(this.delay);
	} catch(Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * Wrapper for sending a message to include a delay.
     */
    private void send(PrintWriter outStream, String msg) {
	emulateDelay();
	outStream.println(msg);
    }

    /**
     * Wrapper for sending a message to include a delay.
     */
    private void send(OutputStream outStream, byte[] byteArray) {
	emulateDelay();
	try {
	    outStream.write(byteArray, 0, byteArray.length);
	    outStream.flush();
	} catch(IOException e) {
	    e.printStackTrace();
	}
    }

    /**
     * Verify that the checksum of a file matches. The parameter of this method
     * must contain a 20-byte checksum at the front of the array. The rest of 
     * the array must contain the file. Compute the checksum of the file and
     * compare it to the 20-bytes at the front of the file.
     */
    private static boolean verifyChecksum(byte[] bytes) {
	StringBuffer sb = new StringBuffer();
	for(int i = 0; i < 20; i++) {
	    sb.append(Integer.toHexString((bytes[i] & 0xFF) |
					  0x100).substring(1,3));
	}
	String realChecksum = sb.toString();
	System.out.println(realChecksum);
	
	byte[] fileBytes = Arrays.copyOfRange(bytes, 20, bytes.length);
	byte[] checksumBytes = SHA1(fileBytes);
	sb = new StringBuffer();
	for(int i = 0; i < 20; i++) {
	    sb.append(Integer.toHexString((checksumBytes[i] & 0xFF) |
					  0x100).substring(1,3));
	}
	String computedChecksum = sb.toString();
	System.out.println(computedChecksum);
	
	return realChecksum.equals(computedChecksum);
    }

    /**
     * Compute SHA1 hash on list of raw bytes. Return sha1 hash as an array of
     * bytes.
     */
    private static byte[] SHA1(byte[] inputText) {
	byte arr[] = null;

	try{
	    MessageDigest m = MessageDigest.getInstance("SHA-1");
	    
	    arr = m.digest(inputText);
	} catch (Exception e){
	}
		
	StringBuffer sb = new StringBuffer();  
	for (int i = 0; i < arr.length; ++i) {  
	    sb.append(Integer.toHexString((arr[i] & 0xFF) | 0x100).substring(1,3));  
	}  
	return arr;
    }
}
