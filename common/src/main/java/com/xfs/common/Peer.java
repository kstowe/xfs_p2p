package com.xfs.common;

/**
 * Representation of a peer in the network.
 */
public class Peer {
    /**
     * Port and ip address at which the peer listens for other peers
     */
    private final int port;
    private final String ip;

    /**
     * Unique identifier of the peer
     */
    private final String id;

    /**
     * Network latency of peer
     */
    private int latency;

    /**
     * Current load at peer
     */
    private int load;  // ms?
    
    public Peer(String ID, int port, String ip) {
	this.id = ID;
	this.port = port;
	this.ip = ip;
    }

    public int getPort() {
	return this.port;
    }

    public String getID() {
	return this.id;
    }

    public String getAddress() {
	return this.ip;
    }

    public int getLatency() {
	return this.latency;
    }

    public int getLoad() {
	return this.load;
    }

    public String getInfo() {
	return id + "::" + port + "::" + ip;
    }

    public void setLatency(int latency) {
	this.latency = latency;
    }

    public void setLoad(int load) {
	this.load = load;
    }

    public String toString() {
	return port + ":" + ip + ":" + id;
    }

    @Override
    public boolean equals(Object object) {
	if(object == null) {
	    return false;
	} else if(!(object instanceof Peer)) {
	    return false;
	} else {
	    Peer peer = (Peer)object;
	    return this.id.equals(peer.getID()) && this.port == peer.getPort() &&
		this.ip.equals(peer.getAddress());
	}
    }
}
