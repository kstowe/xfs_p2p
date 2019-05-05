package com.xfs.client;

import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;
import com.xfs.common.TrackingServerCommunicator;
import com.xfs.common.XFSTrackerCommunicator;

public class Client {
    public static void main(String[] args) throws IOException {

	if(args.length != 5) {
	    System.out.println("Usage: java Client server_ip server_port " +
			       "port_to_listen_for_clients clientID corruption_probability");
	    System.exit(1);
	}

	int peerPort = Integer.parseInt(args[2]);
	int serverPort = Integer.parseInt(args[1]);
	int machID = Integer.parseInt(args[3]);
	Downloader downloader = new FileDownloader(Double.parseDouble(args[4]));
	TrackingServerCommunicator tracker = new XFSTrackerCommunicator();
	PeerSelector peerSelector = new RandomPeerSelector();
	DirectoryManager directoryManager = new XFSDirectoryManager();
	String shareDirectory = "./share/" + machID;

	XFSPeer xfsPeer = new XFSPeer(machID, peerSelector, downloader,
				      tracker, directoryManager, shareDirectory);
	
	ClientImpl client = new ClientImpl(args[0], serverPort, peerPort, machID, xfsPeer);

	xfsPeer.startPeerHandler(peerPort);

	client.run();
    }
}
