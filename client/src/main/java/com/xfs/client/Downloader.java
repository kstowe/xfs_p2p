package com.xfs.client;

import com.xfs.common.SocketWrapper;
import com.xfs.common.PeerAction;
import java.io.IOException;

/**
 * Operations for interacting with another peer in the system. Each operation
 * receives a socket as a parameter, connecting to another peer.
 */
public interface Downloader {
    /**
     * Download a file.
     * @throws ChecksumMismatchException Thrown when the checksum of the
     * downloaded file does not match the checksum of the actual file.
     */
    public byte[] download(SocketWrapper sock, String filename)
	throws ChecksumMismatchException, IOException;

    /**
     * Send a file to another peer.
     */
    public int sendDownload(SocketWrapper sock, byte[] fileBytes)
	throws IOException;

    /**
     * Get the load at another peer.
     */
    public int getLoad(SocketWrapper sock);

    /**
     * Send the current load to another peer.
     */
    public int sendLoad(SocketWrapper sock, int concurrentDownloads)
	throws IOException;

    /**
     * Interpret a message from another peer.
     * @return A PeerAction object containing an action constant and information
     * pertaining to that action.
     */
    public PeerAction processRequest(SocketWrapper sock) throws IOException;
}
