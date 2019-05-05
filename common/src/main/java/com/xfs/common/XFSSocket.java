package com.xfs.common;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.net.InetAddress;

/**
 * Provide interface for sending and receiving messages and files.
 */
public class XFSSocket implements SocketWrapper {
    private Socket sock;
    private PrintWriter out;
    private BufferedReader in;

    public XFSSocket() {
    }

    public XFSSocket(Socket sock) throws IOException {
	this.sock = sock;
	out = new PrintWriter(sock.getOutputStream());
	in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
    }
    
    public XFSSocket(String ip, int port ) throws IOException {
	sock = new Socket(ip, port);
	out = new PrintWriter(sock.getOutputStream());
	in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
    }
    
    public void connect(String ip, int port ) throws IOException {
	sock = new Socket(ip, port);
	out = new PrintWriter(sock.getOutputStream());
	in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
    }

    public void send(String data) {
	out.println(data);
	out.flush();
    }

    public void sendFile(byte[] data) throws IOException {
	OutputStream outStream = this.sock.getOutputStream();
	outStream.write(data);
	outStream.flush();
    }

    public String receive() throws IOException {
	return in.readLine();
    }

    /**
     * Receive an array of bytes. The max size of a file that can be received is
     * 100MB. Any larger file will be cut down to 100MB. 
     */
    public byte[] receiveFile() throws IOException {
	byte[] byteArray = new byte[100000000]; //max size 100MB
	InputStream in = this.sock.getInputStream();
	int bytesRead = in.read(byteArray, 0, byteArray.length);
	System.out.println("First read: " + bytesRead);
	int totalBytesRead = bytesRead;
	do {
	    bytesRead = in.read(byteArray, totalBytesRead, byteArray.length-totalBytesRead);
	    if(bytesRead >= 0) {
		totalBytesRead += bytesRead;
	    }
	} while(bytesRead > -1);
	
	in.close();
	System.out.println("Read " + totalBytesRead);
	return Arrays.copyOfRange(byteArray, 0, totalBytesRead);
    }

    public void close() throws IOException {
	sock.close();
	in.close();
	out.close();
    }

    public InetAddress getInetAddress() {
	return sock.getInetAddress();
    }
}
