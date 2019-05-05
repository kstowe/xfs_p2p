package com.xfs.client;

import java.util.Random;
import com.xfs.common.Peer;

/**
 * Select a peer from a list at random.
 */
public class RandomPeerSelector implements PeerSelector {
    @Override
    public Peer choosePeer(Peer[] peers) {
	if(peers.length <= 0) {
	    return null;
	}
	Random rand = new Random();
	int peerIndex = rand.nextInt(peers.length);
	return peers[peerIndex];
    }
}
