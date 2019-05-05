package com.xfs.common;

import java.io.*;
import java.util.Arrays;
import org.junit.Test;
import org.junit.Before;
import org.junit.Ignore;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestXFSTrackerCommunicator {
    TrackingServerCommunicator tracker;
    SocketWrapper sock;

    @Before public void setUp() {
	tracker = new XFSTrackerCommunicator();
	sock = mock(XFSSocket.class);
    }
    
    @Test public void checkInputOfRegisterTest() throws IOException {	
	String[] fileList = new String[]{"file1.pdf","file2.pdf"};
	int machID = 13;
	int peerPort = 10001;

	tracker.register(sock, fileList, machID, peerPort);

	String expectedMessage = "Register::13::10001::file1.pdf::file2.pdf";
	verify(sock).send(expectedMessage);
    }

    @Test public void checkInputOfUpdateListTest() throws IOException {
	String[] fileList = new String[]{"file1.pdf","file2.pdf"};
	int machID = 13;

	tracker.updateList(sock, fileList, machID);

	String expectedMessage = "UpdateList::13::file1.pdf::file2.pdf";
	verify(sock).send(expectedMessage);
    }

    @Test public void checkInputOfFindTest() throws IOException {
	String filename = "file1.pdf";
	//when(sock.send(contains("file1.pdf"))).thenReturn("");
	when(sock.receive()).thenReturn("");
	
	tracker.find(sock, filename);

	String expectedMessage = "Find::file1.pdf";
	verify(sock).send(expectedMessage);
    }

    @Test public void findReturnsNullWhenNobodyHasFile() throws IOException {
	String filename = "file1.pdf";
	//when(sock.send(contains("file1.pdf")));
	when(sock.receive()).thenReturn("");
	
	Peer[] peers = tracker.find(sock, filename);

	assertNull(peers);
    }

    @Test public void findCorrectlyParsesPeerListTest() throws IOException {
	Peer[] peers = new Peer[]{new Peer("67", 10005, "1.0.6.78"),
				  new Peer("123", 10004, "1.0.0.1")};
	//when(tracker.get(contains("file1.pdf"))).thenReturn(new Peer[]);
	String filename = "file1.txt";
	String peerListString = "67::10005::1.0.6.78::123::10004::1.0.0.1";
	when(sock.receive()).thenReturn(peerListString);
	
	Peer[] foundPeers = tracker.find(sock, filename);

	assertArrayEquals(peers, foundPeers);
    }
}
