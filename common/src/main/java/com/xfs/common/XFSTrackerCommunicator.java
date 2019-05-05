package com.xfs.common;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Implements operations for communication between peer and tracking server.
 * Defines communication protocol between peer and tracking server.
 *    -Register (peer -> tracking server) message format:
 *         Register::machID::port::fileList
 *       where machID - unique identifier of peer
 *           port - port on which peer listens for connections from other peers
 *           fileList - list of files to be shared. Each file name is separated
 *                      by a "::" character.
 *    -UpdateList (peer -> tracking server) message format:
 *          UpdateList::machID::fileList
 *        where machID and fileList are defined the same as in Register
 *    -Find (peer -> tracking server) message format:
 *          Find::filename
 *        where filename - name of file
 */
public class XFSTrackerCommunicator implements TrackingServerCommunicator {

    /**
     * Register the client with the tracking server at boot time. Send the
     * end point information and the list of files.
     */
    public void register(SocketWrapper sock, String[] fileList, int machID,
			 int peerPort) throws IOException {
	try {
	    String shareList = fileList[0];
	    for(int i = 1; i < fileList.length; i++) {
		shareList += "::" + fileList[i];
	    }
	    
	    String msg = "Register" + "::" + machID + "::" + peerPort + "::" +
		shareList;   
	    sock.send(msg);
	    sock.close();
	    
	} catch(IOException e) {
	    throw e;
	}
    }
    
    /**
     * Send tracking server an updated list of local files.
     */
    public void updateList(SocketWrapper sock, String[] fileList, int machID)
	throws IOException {
	try {
	    String shareList = fileList[0];
	    for(int i = 1; i < fileList.length; ++i) {
		shareList += "::" + fileList[i];
	    }

	    sock.send("UpdateList::" + machID + "::" + shareList);
	    sock.close();
	} catch(IOException e) {
	    throw e;
	}
    }
    
    /**
     * Ask tracking server for a list of peers that can provide a certain file
     * @return list of peers with file
     */
    public Peer[] find(SocketWrapper sock, String filename) throws IOException {
	Peer[] peers = null;
	try {
	    sock.send("Find::" + filename);
	    String data = sock.receive();
	    if(data != null) {
		    peers = parsePeerList(data);
	    }
	    sock.close();
	} catch(IOException e) {
	    throw e;
	}
	    return peers;
    }

    /**
     * Interpret a message from a peer according to the communication protocol
     * defined.
     * @return A ServerAction object that defines an action and other fields
     * associated with that action.
     */
    public ServerAction processRequest(SocketWrapper sock) throws IOException {
	String data = sock.receive();
	String[] input = data.split("::");
	ServerAction action;
	String[] fileList;
	String clientID;
	try {
	    switch(input[0]) {
	    case "Register":
		action = new ServerAction(ServerAction.REGISTER);
		fileList = Arrays.copyOfRange(input, 3, input.length);
		action.setFileList(fileList);
		
		clientID = input[1];
		action.setClientID(clientID);
		
		int peerPort = Integer.parseInt(input[2]);
		action.setPeerPort(peerPort);
		return action;
	    case "Find":
		action = new ServerAction(ServerAction.FIND);
		String filename = input[1];
		action.setFilename(filename);
		
		return action;
	    case "UpdateList":
		action = new ServerAction(ServerAction.UPDATE_LIST);
		fileList = Arrays.copyOfRange(input, 2, input.length);
		action.setFileList(fileList);
		clientID = input[1];
		action.setClientID(clientID);
		return action;
	    default:
		throw new IOException();
	    }
	} catch(IOException e) {
	    throw e;
	}
    }

    /**
     * Send a list of peers to a peer.
     */
    public void sendPeerList(SocketWrapper sock, Peer[] peerList) {
	if(peerList == null) {
	    sock.send(null);
	} else {
	    String message = peerList[0].getInfo();
	    for(int i = 1; i < peerList.length; i++) {
		message += "::" + peerList[i].getInfo();
	    }
	    sock.send(message);
	}
    }

    /**
     * Parse the list of peers sent by the tracking server into a coherent list
     * @param data - raw string of peer list
     * @return - peer list
     */
    private Peer[] parsePeerList(String data) {
	if(data.equals("")) {
	    return null;
	}
	String[] parts = data.split("::");
	Peer[] peers = new Peer[parts.length/3];
	for(int i = 0; i < parts.length-1; i+=3) {
	    peers[i/3] = new Peer(parts[i],
				  Integer.parseInt(parts[i+1]), parts[i+2]);
	}
	return peers;
    }

    /**
     * Blocking call. Block until connection is re-established with server.
     * @param ipaddr ip address of machine you want to connect to
     * @param sockPort listening port of machine you want to connect to
     * @return socket
     */
    /*
    private synchronized Socket reconnect(String ipaddr, int sockPort) {
	Socket cSock = null;
	try {
	    cSock = getSocket(ipaddr, sockPort);
	    //log.info("Trackng server is down. Trying to re-connect...");
	    while(cSock == null) {
		cSock = getSocket(ipaddr, sockPort);
	    }
	    //log.info("Tracking server is back up.");
	} catch(IOException e) {
	}
	return cSock;
    }
    */
}
