package com.xfs.common;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestPeer {
    @Test public void testPeerEqualsMethod() {
	Peer peer1 = new Peer("34", 10004, "1.1.13.13");
	Peer peer2 = new Peer("34", 10004, "1.1.13.13");

	assertEquals(peer1, peer2);
    }
}
