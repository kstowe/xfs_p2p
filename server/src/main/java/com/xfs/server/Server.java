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
 * Driver class of server. Prints log information to console.
 */
public class Server {
    public static void main(String[] args) throws Exception {
	if(args.length < 1) {
	    System.out.println("Usage: java Server port");
	    System.exit(1);
	}

	int port = Integer.parseInt(args[0]);
	TrackingServerCommunicator communicator = new XFSTrackerCommunicator();
	ServerImpl srv = new ServerImpl(communicator);
	ServerSocket sSock = null;

	System.out.println("Listening for clients on port " + port);

	try {
	    sSock = new ServerSocket(port);
	} catch (IOException e) {
	    System.out.println("Error: cannot open socket");
	    System.exit(1);
	}

	System.out.println("Server is listening...");

	while(true) {
	    srv.new ClientQuery(sSock.accept()).start();
	}
    }
}

