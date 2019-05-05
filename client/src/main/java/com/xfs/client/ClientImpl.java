package com.xfs.client;

import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;

/**
 * Command-line user interface for interacting with the peer-to-peer system. 
 * Provides a user menu asking the user for a command. The user can then enter
 * the name of the file to download.
 */
public class ClientImpl {
    /**
     * Port on which peer waits for other peers
     */
    private int peerPort;

    /**
     * IP address and port at which tracking server is located
     */
    private int port;
    private String ip;

    /**
     * Unique identifier of machine
     */
    private int machID;

    /**
     * List mapping each peer by their id to the latency from the local machine
     */
    private HashMap<String, Integer> latencyList;

    /**
     * System peer object
     */
    private XFSPeer xfsPeer;

    public ClientImpl(String ip, int port, int peerPort,
		      int machID, XFSPeer xfsPeer) {
	this.ip = ip;
	this.port = port;
	this.peerPort = peerPort;
	this.machID = machID;
	this.xfsPeer = xfsPeer;
    }

    /**
     * Run the peer by registering with the tracking server and then presenting
     * the user menu to the user and wait for input.
     */
    public void run() {
	latencyList = getLatenciesFromFile();
	xfsPeer.setLatencyList(latencyList);
	xfsPeer.setTrackingServer(new Host(ip, port));

	while(true) {
	    xfsPeer.register("./share/" + machID, this.machID,
			     peerPort);

	    Scanner scan = new Scanner(System.in);
	    String data = "";
	    String msg;

	    printMainScreen();

	    int command = -1;
	    boolean valid = false;
	    while(!valid) {
		String input = scan.nextLine();
		try {
		    command = Integer.parseInt(input);
		    valid = true;
		} catch(NumberFormatException e) {
		    System.out.print("Please enter a valid command (1-2): ");
		}
	    }
	    switch(command) {
	    case 1:
		System.out.println("Enter file name:");
		String filename = scan.nextLine();
		int result = xfsPeer.download("./share/" + machID, filename);
		break;
	    case 2:
		System.exit(0);
	    default:
		System.out.println("Please enter a valid command (1-5): ");
	    }
	}
    }

    public void printMainScreen() {
	System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
	System.out.println("Filesystem Operations");
	System.out.println("    1: Download");
	System.out.println("    2: Exit");
	System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
	System.out.print("Enter command number (1-2): ");
    }

    /**
     * Read the network latencies from the configuration file 'latency.txt'.
     * Latencies will be stored in a map, mapping peer id's to latencies
     * @return hash map of peers to latencies
     */
    public HashMap<String, Integer> getLatenciesFromFile() {
	Scanner readFile = null;
	String line;
	String[] connection;
	HashMap<String, Integer> latencyList = new HashMap<String, Integer>();
	try {
	    File latencyFile = new File("latency.txt");
	    if(!latencyFile.exists()) {
		throw new FileNotFoundException();
	    } else {
		readFile = new Scanner(new File("latency.txt"));
	    }
	} catch(FileNotFoundException e) {
	    System.out.println("Error: latency.txt file missing.");
	    System.exit(1);
	}

	// Latency to local machine is 0
	latencyList.put(""+this.machID, new Integer(Integer.MAX_VALUE));
	
	while(readFile.hasNextLine()) {
	    line = readFile.nextLine();
	    connection = line.split(":");
	    if(connection[0].equals(this.machID)) {
		latencyList.put(connection[1], new Integer(connection[2]));
	    } else if(connection[1].equals(this.machID)) {
		latencyList.put(connection[0], new Integer(connection[2]));
	    }
	}
	return latencyList;
    }
}
