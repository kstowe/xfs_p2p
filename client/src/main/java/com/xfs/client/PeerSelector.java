package com.xfs.client;

import com.xfs.common.Peer;

/**
 * Algorithm for selecting a peer to download from.
 */
public interface PeerSelector {
    /**
     * Return a peer or null if none can be selected.
     */
    public Peer choosePeer(Peer[] peers);
}
