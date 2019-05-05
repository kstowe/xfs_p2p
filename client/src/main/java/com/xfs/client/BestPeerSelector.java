package com.xfs.client;

import com.xfs.common.Peer;

/**
 * Peer selector optimized to choose the peer with the best proportion of load
 * and latency. The proportion was found experimentally.
 */
public class BestPeerSelector implements PeerSelector {
    /**
     * Algorithm for peer selection that weights the load by 12/13 and the
     * latency by 1/13.
     */
    @Override
    public Peer choosePeer(Peer[] peers) {
	int[] peerScores = computeScores(peers);
	int minIndex = chooseMin(peerScores);
	return peers[minIndex];
    }

    /**
     * Compute a score for each peer based on latency and load
     * @return list of the score for each peer
     */
    private static int[] computeScores(Peer[] peers) {
	int[] peerScores = new int[peers.length];
	for(int i = 0; i < peers.length; i++) {
	    if(peers[i] == null || peers[i].getLoad() == -1 ||
	       peers[i].getLatency() == -1) {
		peerScores[i] = Integer.MAX_VALUE;
	    } else {
		peerScores[i] = peers[i].getLoad()*12 + peers[i].getLatency();
	    }
	}
	return peerScores;
    }

    /**
     * Find the index with the best (lowest) score
     */
    private static int chooseMin(int[] peerScores) {
	int minScore = Integer.MAX_VALUE;
	int indexOfMin = 0;
	for(int i = 0; i < peerScores.length; i++) {
	    if(peerScores[i] < minScore) {
		minScore = peerScores[i];
		indexOfMin = i;
	    }
	}
	return indexOfMin;
    }
}
