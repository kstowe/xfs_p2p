package com.xfs.client;

import com.xfs.common.Peer;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.security.*;
import com.xfs.common.TrackingServerCommunicator;
import com.xfs.common.SocketWrapper;
import com.xfs.common.XFSSocket;
import com.xfs.common.PeerAction;

public class XFSPeer {
    /**
     * Unique identifier of local machine
     */
    private int machID;

    /**
     * Port on which this peer listens for other peers
     */
    private int peerPort;

    /**
     * Number of downloads this peer is currently servicing
     */
    private int concurrentDownloads;

    /**
     * Endpoint info of tracking server
     */
    private Host trackingServer;

    /**
     * Network latency to all other peers in the system
     */
    private HashMap<String, Integer>latencyList;

    /**
     * Provides algorithm for peer selection
     */
    private PeerSelector peerSelector;

    /**
     * Interface for communicating with other peers
     */
    private Downloader downloader;

    /**
     * Interface for communicating with tracking server
     */
    private TrackingServerCommunicator tracker;

    /**
     * Log file
     */
    private Logger log;

    /**
     * Path to the directory that the peer wants to share.
     */
    private String shareDirectory;

    /**
     * Object for handling interaction with filesytem.
     */
    private DirectoryManager directoryManager;
    
    public XFSPeer(int machID, PeerSelector peerSelector, Downloader downloader,
		   TrackingServerCommunicator tracker, DirectoryManager dm, String shareDirectory) {
	this.machID = machID;
	this.peerSelector = peerSelector;
	this.downloader = downloader;
	this.tracker = tracker;
	this.concurrentDownloads = 0;
	this.log = setUpLogFile();
	this.directoryManager = dm;
	this.shareDirectory = shareDirectory + "/";
    }

    public void setTrackingServer(Host host) {
	this.trackingServer = new Host(host);
    }

    public void setLatencyList(Map<String, Integer> latencyList) {
	this.latencyList = new HashMap<String, Integer>(latencyList);
    }

    public void startPeerHandler(int peerPort) {
	new HandlePeers(peerPort).start();
    }

    /**
     * Create log file in share/machID/
     */
    private Logger setUpLogFile() {
	Logger logger = null;
	FileHandler handler;
	try {
	    handler = new FileHandler("./share/" + this.machID + "/log.txt", true);
	    logger = Logger.getLogger("log");
	    logger.addHandler(handler);
	    handler.setFormatter(new SimpleFormatter());
	    logger.info("Peer is up.");
	} catch(Exception e) {
	    e.printStackTrace();
	}
	return logger;
    }

    /**
     * Reister with tracking server. Send the tracking server the peers unique
     * machine id, the port on which it will listen for other peers, and the
     * list of files that it can supply.
     * @param filepath Path to the folder the peer wants to share.
     */
    public void register(String filepath, int machID, int peerPort) {
	String[] shareArray = null;
	ArrayList<String> shareList = this.directoryManager.getFileNames(filepath);
	shareArray = new String[shareList.size()];
	shareList.toArray(shareArray);
	try {
	    SocketWrapper sock = new XFSSocket(trackingServer.getIP(),
					       trackingServer.getPort());
	    tracker.register(sock, shareArray, machID, peerPort);
	} catch(IOException e) {
	    log.info("Failed to register.");
	}
    }

    /**
     * Download a file.
     * @param filepath Path to location where downloaded file will be written
     * @param filename Name of file to be downloaded
     */
    public int download(String filepath, String filename) {
	Peer[] peers = null;
	try {
	    SocketWrapper sock = new XFSSocket(trackingServer.getIP(),
					       trackingServer.getPort());
	    peers = tracker.find(sock, filename);
	} catch(IOException e) {
	    log.info(e.getMessage());
	}
	if(peers == null) {
	    log.info("No peers with file " + filename +
		     " are currently available.");
	    return -1;
	}

	addLatencies(peers);
	addLoads(peers);

	final long startTime = System.currentTimeMillis();
		    
	byte[] fileBytes = null;
	int success = -1;
	while(true) {
	    Peer bestPeer = peerSelector.choosePeer(peers);
	    if(bestPeer == null) {
		log.info("No peers with file " + filename +
			 " are currently available.");
		return -1;
	    }
	    else if(bestPeer.getID().equals(""+this.machID)) {
		log.info("Download failed: File is local.");
		break;
	    }
	    log.info("Downloading " + filename + " from peer " + bestPeer.getID());
	    addDownload();
	    while(true) {
		try {
		    SocketWrapper sock = new
			XFSSocket(bestPeer.getAddress(), bestPeer.getPort());
		    fileBytes = downloader.download(sock, filename);
		    sock.close();
		    success = 1;
		    break;
		} catch(ChecksumMismatchException e) {
		    log.info("Download failed: Checksum mismatch. Retrying...");
		} catch(IOException e) {
		    log.info("Download failed: Peer " + bestPeer.getID() +
			     " is down. Selecting new peer...");
		    bestPeer.setLoad(-1);
		    break;
		}
	    }

	    if(success == 1) {
		filepath += "/";
		try {
		    directoryManager.printToFile(filepath + filename, fileBytes);
		    break;
		} catch(IOException e) {
		    log.info("Failed to print to " + filepath);
		    return -1;
		}
	    }
	}

	final long endTime = System.currentTimeMillis();
	log.info("Total time: " + (endTime - startTime));

	ArrayList<String> shareList = this.directoryManager.getFileNames(filepath);
	String[] fileList = new String[shareList.size()];
	shareList.toArray(fileList);

	try {
	    SocketWrapper sock = new XFSSocket(trackingServer.getIP(),
					       trackingServer.getPort());
	    tracker.updateList(sock, fileList, machID);
	} catch(IOException e) {
	    log.info("Failed to send tracking server updated file list.");
	}
	return success;
    }


    /**
     * Utility for find: Determine the current latency for each peer in a list
     * of peers.
     */
    private void addLatencies(Peer[] peers) {
	for(int i = 0; i < peers.length; i++) {
	    Integer latency = this.latencyList.get(peers[i].getID());
	    if(latency == null) {
		peers[i].setLatency(-1);
	    } else {
		peers[i].setLatency(latency.intValue());
	    }
	}
    }

    /**
     * Utility for find: Determine the current load for each peer in a list of
     * peers. 
     */
    private void addLoads(Peer[] peers) {
	for(int i = 0; i < peers.length; i++) {
	    if(!peers[i].getID().equals(this.machID)) {
		int load;
		try {
		    SocketWrapper sock = new XFSSocket(peers[i].getAddress(),
						       peers[i].getPort());
		    load = downloader.getLoad(sock);
		    sock.close();
		} catch(IOException e) {
		    load = -1;
		}
		peers[i].setLoad(load);
	    }
	}
    }

    /**
     * 
     */
    private byte[] prepareMessage(String filepath) {
	File fileToSend = new File(filepath);
	byte[] fileBytes = new byte[(int)fileToSend.length()];

	try {
	    BufferedInputStream fileStream =new BufferedInputStream(
		new FileInputStream(fileToSend));
	    fileStream.read(fileBytes, 0, fileBytes.length);
	    fileStream.close();
	} catch(IOException e) {
	}
	return fileBytes;
    }


    /**
     * Increment the load at the client
     */
    public synchronized void addDownload() {
	this.concurrentDownloads++;
    }

    /**
     * Decrement the load at the client
     */
    public synchronized void removeDownload() {
	this.concurrentDownloads--;
    }
    
/**************************************THREADS*********************************/

    /**
     * Thread for listening for connections from other peers. When a connection
     * arrives, hand the connection off to a PeerQuery thread to service it.
     */
    class HandlePeers extends Thread {
	private int peerPort;
	
	HandlePeers(int peerPort) {
	    this.peerPort = peerPort;
	}
	
	@Override public void run() {
	    ServerSocket sSock = null;
	    try {
		sSock = new ServerSocket(this.peerPort);
	    } catch(IOException e) {
		System.out.println("Error: cannot open socket");
		System.exit(1);
	    }
	    while(true) {
		try {
		    new PeerQuery(sSock.accept()).start();
		} catch(IOException e) {
		}
	    }
	}
    }

    /**
     * Thread for handling a connection from another peer.
     */
    class PeerQuery extends Thread {
	int delay = 0;
	SocketWrapper sock = null;
	PeerQuery(Socket sock) {
	    try {
		this.sock = new XFSSocket(sock);
	    } catch(IOException e) {
		log.info("Failed to create xfs socket.");
	    }
	}
	@Override public void run() {
	    PeerAction action;
	    try {
		action = downloader.processRequest(this.sock);
	    } catch(IOException e) {
		log.info("Request cannot be processed: " + e.getMessage());
		try {
		    this.sock.close();
		} catch(IOException e2) {
		}
		return;
	    }
	    
	    switch(action.getAction()) {
	    case PeerAction.SEND_LOAD:
		log.info("Sending load: " + concurrentDownloads);
		try {
		    downloader.sendLoad(this.sock, concurrentDownloads);
		    this.sock.close();
		} catch(IOException e) {
		}
		break;
	    case PeerAction.SEND_DOWNLOAD:
		addDownload();
		try {
		    byte[] fileBytes = prepareMessage(shareDirectory+action.getFilename());
		    downloader.sendDownload(this.sock, fileBytes);
		    this.sock.close();
		} catch(IOException e) {
		}
		removeDownload();
		log.info(action.getFilename() + " successfully sent");
		break;
	    default:
	    }
	}
    }
}
