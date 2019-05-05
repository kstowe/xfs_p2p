package com.xfs.client;

/**
 * Data structure to represent a network endpoint.
 */
public class Host {
    private String ip;
    private int port;

    public Host(String ip, int port) {
	this.ip = ip;
	this.port = port;
    }

    public Host(Host host) {
	this.ip = host.getIP();
	this.port = host.getPort();
    }

    public String getIP() {
	return this.ip;
    }

    public int getPort() {
	return this.port;
    }
}
