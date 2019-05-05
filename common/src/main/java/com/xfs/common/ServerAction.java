package com.xfs.common;

/**
 * Data structure to define the actions that the tracking server may take.
 * The operations defined are: register, find, and update list. The fields
 * of this class correspond to information needed for each action.
 */
public class ServerAction {
    public static final int REGISTER = 0,
	FIND = 1,
	UPDATE_LIST = 2;

    private int action;
    private int peerPort;
    private String clientID;
    private String[] fileList;
    private String filename;

    public ServerAction(int action) {
	this.action = action;
    }

    public String getFilename() {
	return this.filename;
    }

    public int getAction() {
	return this.action;
    }

    public int getPeerPort() {
	return this.peerPort;
    }

    public String getClientID() {
	return this.clientID;
    }

    public String[] getFileList() {
	return this.fileList;
    }

    public void setPeerPort(int peerPort) {
	this.peerPort = peerPort;
    }

    public void setClientID(String clientID) {
	this.clientID = clientID;
    }

    public void setFileList(String[] fileList) {
	this.fileList = fileList;
    }

    public void setFilename(String filename) {
	this.filename = filename;
    }
}
