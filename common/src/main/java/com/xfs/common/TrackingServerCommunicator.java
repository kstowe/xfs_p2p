package com.xfs.common;

import java.io.IOException;

/**
 * Operations for communication between peer and tracking server.
 */
public interface TrackingServerCommunicator {
    /**
     * Register with the tracking server.
     */
    public void register(SocketWrapper sock, String[] fileList, int machID,
			 int peerPort) throws IOException;

    /**
     * Send updated list to tracking server.
     */
    public void updateList(SocketWrapper sock, String[] fileList, int machID)
	throws IOException;

    /**
     * Request a list of peers that can provide a certain file.
     */
    public Peer[] find(SocketWrapper sock, String filename) throws IOException;

    /**
     * Interpret a message from a peer.
     * @return A ServerAction object that defines what action the server should
     * take, along with other information related to that action.
     */
    public ServerAction processRequest(SocketWrapper sock) throws IOException;

    /**
     * Send a list of peers to a peer.
     */
    public void sendPeerList(SocketWrapper sock, Peer[] peerList);
}
