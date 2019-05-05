package com.xfs.common;

/**
 * Data structure representing a possible action a peer may take.
 * The two actions defined are: send load and send download.
 */
public class PeerAction {
    public static final int SEND_LOAD = 0,
	                    SEND_DOWNLOAD = 1;
    
    private int action;
    private String filename;

    public PeerAction(int action) {
	this(action, "");
    }

    public PeerAction(int action, String filename) {
	this.action = action;
	this.filename = filename;
    }

    public int getAction() {
	return this.action;
    }

    public String getFilename() {
	return this.filename;
    }

    @Override
    public boolean equals(Object object) {
	if(object == null) {
	    return false;
	} else if(!(object instanceof PeerAction)) {
	    return false;
	} else {
	    PeerAction peerAction = (PeerAction)object;
	    return this.action == peerAction.action &&
		this.filename.equals(peerAction.filename);
	}
    }
}
