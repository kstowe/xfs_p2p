package com.xfs.client;

import com.xfs.common.Peer;
import org.junit.Test;
import org.junit.Before;
import org.junit.Ignore;
import static org.junit.Assert.*;

public class TestBestPeerSelector {
    Peer[] peers;
    PeerSelector selector;
    
    @Before public void setUp() {
	peers = new Peer[3];
	selector = new BestPeerSelector();
    }

    @Test public void selectBestPeerFromList() {
	peers[0] = makePeer(1, 3);
	peers[1] = makePeer(2, 4);
	peers[2] = makePeer(3, 4);
	
	Peer bestPeer = selector.choosePeer(peers);

	assertEquals(bestPeer, peers[0]);
    }

    public Peer makePeer(int load, int latency) {
	Peer peer = new Peer("15", 11523, "1.1.2.3");
	peer.setLoad(load);
	peer.setLatency(latency);
	return peer;
    }
    
    @Test public void selectFromEmptyList() {
	Peer bestPeer = selector.choosePeer(peers);

	assertNull(bestPeer);
    }
}
