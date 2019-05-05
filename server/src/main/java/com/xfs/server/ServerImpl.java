package com.xfs.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

import com.xfs.common.ServerAction;
import com.xfs.common.Peer;
import com.xfs.common.TrackingServerCommunicator;
import com.xfs.common.XFSTrackerCommunicator;
import com.xfs.common.SocketWrapper;
import com.xfs.common.XFSSocket;

/**
 * Tracking server monitors which peers are active and which files they can 
 * provide. Tracking server does not maintain state persistently. If the
 * tracking server goes down, its knowledge of the state of the peer-to-peer
 * network is lost and must be re-populated by peer connections.
 */
public class ServerImpl {
    /**
     * Maps filename to list of clients that have it
     */
    HashMap<String, ArrayList<String>> filemap = new HashMap<>();
    
    /**
     * Maps client IDs to port/ip info
     */
    HashMap<String, Peer> clientMap = new HashMap<>();
    
    /**
     * Maps filename to checksum
     */
    HashMap<String, String> checksumMap = new HashMap<>();

    /**
     * Interface for interacting with peers
     */
    TrackingServerCommunicator communicator;

    public ServerImpl(TrackingServerCommunicator communicator) {
	this.communicator = communicator;
    }

    /**
     * Thread for servicing a connection from a peer.
     */
    class ClientQuery extends Thread {
	SocketWrapper sock;
	ClientQuery(Socket sock) {
	    try {
		this.sock = new XFSSocket(sock);
	    } catch(IOException e) {
	    }
	}

	@Override
	public void run() {
	    try {
		ServerAction action = communicator.processRequest(sock);
		switch(action.getAction()) {
		case ServerAction.REGISTER:
		    System.out.println("Register: Client " + action.getClientID());
		    String peerIP = (sock.getInetAddress()).getHostAddress();
		    registerPeer(action.getClientID(), action.getPeerPort(),
				 peerIP, action.getFileList());
		    break;
		case ServerAction.FIND:
		    System.out.println("Find:" + action.getFilename());
		    findPeers(sock, action.getFilename());
		    break;
		case ServerAction.UPDATE_LIST:
		    System.out.println("UpdateList: Client " + action.getClientID());
		    updatePeerList(sock, action.getClientID(), action.getFileList());
		    break;
		default:
		}
	    } catch(IOException e) {
	    }
	}

	/**
	 * Update the list of file that a given peer can provide.
	 */
	public void updatePeerList(SocketWrapper sock, String clientID,
				   String[] fileList) {
	    parseFileList(clientID, fileList);
	}

	/**
	 * Register a peer by storing its id in the clientMap and its files in
	 * the fileMap.
	 */
	public void registerPeer(String clientID, int peerPort, String peerIP,
	    String[] fileList) {
	    // Register a peer's endpoint information and files
	    System.out.println("Receiving updates from Peer " + clientID);
	    if(!clientMap.containsKey(clientID)) {
		clientMap.put(clientID, new Peer(clientID, peerPort, peerIP));
		System.out.print("Peer connections: ");
		print(clientMap);
	    }
	    parseFileList(clientID, fileList);
	}

	/**
	 * Read fileMap to find which peers have a particular file.
	 */
	public void findPeers(SocketWrapper sock, String filename) {
	    String msg, checksum;
	    
	    if(!filemap.containsKey(filename)) {
		communicator.sendPeerList(sock, null);
	    } else {
		ArrayList<String> peerList = filemap.get(filename);
		Peer[] peerListArray = new Peer[peerList.size()];
		for(int i = 0; i < peerListArray.length; i++) {
		    peerListArray[i] = clientMap.get(peerList.get(i));
		}
		communicator.sendPeerList(sock, peerListArray);
	    }
	}
    }

    /**
     * Add a clientID-fileList mapping to the fileMap
     */
    public void parseFileList(String clientID, String[] fileList) {
	for(int i = 0; i < fileList.length; i++) {
	    if(!filemap.containsKey(fileList[i])) {
		filemap.put(fileList[i], new ArrayList<String>());
	    }
	    ArrayList<String> peerList = filemap.get(fileList[i]);
	    if(!peerList.contains(clientID)) {
		peerList.add(clientID);
	    }
	}
    }

    /**
     * Print out map of connected peers
     */
    private static void print(Map<String, Peer> map) {
	if (map.isEmpty()) {
	    System.out.println("Map is empty");
	} else {
	    System.out.println(map);
	}
    }

    /**
     * Print out file table
     */
    private static void print2(Map<String, ArrayList<String>> map) {
	if (map.isEmpty()) {
	    System.out.println("Map is empty");
	} else {
	    System.out.println(map);
	}
    }

    /**
     * Transform a list of peers that have a particular into a String
     * format that can be sent over the network.
     */
    private String prepareList(ArrayList<String> peerList) {
	String info = "";
	for(int i = 0; i < peerList.size(); i++) {
	    info += (clientMap.get(peerList.get(i))).getInfo() + "::";
	}
	return info;
    }
}
