package com.xfs.client;

import com.xfs.common.PeerAction;
import com.xfs.common.Peer;
import com.xfs.common.SocketWrapper;
import com.xfs.common.XFSSocket;
import java.io.IOException;
import org.junit.Test;
import org.junit.Before;
import org.junit.Ignore;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestFileDownloader {
    public Downloader downloader;

    @Before
    public void setup() {
	downloader = new FileDownloader(0.25);
    }

    @Test
    public void testSendDownloadSendsFileWithChecksum() throws IOException {
	String testString = "This is a test string";
	String testStringChecksum = "e2f67c772368acdeee6a2242c535c6cc28d8e0ed";
	byte[] stringBytes = testString.getBytes();
	byte[] checksumBytes = convertChecksumStringToBytes(testStringChecksum);

	byte[] expectedMessageBytes = concatArrays(checksumBytes, stringBytes);

	SocketWrapper sock = mock(XFSSocket.class);
	downloader.sendDownload(sock, testString.getBytes());
	
	verify(sock).sendFile(expectedMessageBytes);
    }

    @Test
    public void testSendLoadSendsCorrectMessage() throws IOException {
	SocketWrapper sock = mock(XFSSocket.class);
	int numDownloads = 5;
	downloader.sendLoad(sock, numDownloads);

	String expectedMessage = "" + 5;
	verify(sock).send(expectedMessage);
    }

    @Test
    public void testDownloadSendsCorrectCommandAndFilename()
	throws IOException, ChecksumMismatchException {
	String testString = "This is a test string";
	String testStringChecksum = "e2f67c772368acdeee6a2242c535c6cc28d8e0ed";
	byte[] stringBytes = testString.getBytes();
	byte[] checksumBytes = convertChecksumStringToBytes(testStringChecksum);

	byte[] messageBytes = concatArrays(checksumBytes, stringBytes);
	
	SocketWrapper sock = mock(XFSSocket.class);
	String filename = "file1.txt";

	when(sock.receiveFile()).thenReturn(messageBytes);

	downloader.download(sock, filename);

	String expectedMessage = "Download::file1.txt";
	verify(sock).send(expectedMessage);
    }

    @Test
    public void testDownloadReturnsCorrectFile()
	throws IOException, ChecksumMismatchException {
	String testString = "This is a test string";
	String testStringChecksum = "e2f67c772368acdeee6a2242c535c6cc28d8e0ed";
	byte[] stringBytes = testString.getBytes();
	byte[] checksumBytes = convertChecksumStringToBytes(testStringChecksum);

	byte[] messageBytes = concatArrays(checksumBytes, stringBytes);
	
	SocketWrapper sock = mock(XFSSocket.class);
	String filename = "file1.txt";

	when(sock.receiveFile()).thenReturn(messageBytes);

	byte[] downloadedBytes = downloader.download(sock, filename);

	byte[] expectedBytes = stringBytes;
	assertArrayEquals(downloadedBytes, expectedBytes);
    }

    @Test(expected=IOException.class)
    public void downloadThrowsIOExceptionIfFileTooShort()
	throws IOException, ChecksumMismatchException {
	String testString = "This is a test string";
	String testStringChecksum = "e2f67c772368acdeee6a2242c535c6cc28d8e0ed";
	byte[] stringBytes = testString.getBytes();
	byte[] checksumBytes = convertChecksumStringToBytes(testStringChecksum);

	byte[] messageBytes = concatArrays(checksumBytes, stringBytes);

	SocketWrapper sock = mock(XFSSocket.class);
	String filename = "file1.txt";

	when(sock.receiveFile()).thenReturn(new byte[]{new Integer(5).byteValue()});

	downloader.download(sock, filename);
    }

    @Test(expected=ChecksumMismatchException.class)
    public void downloadThrowsChecksumMistmatchExceptionWhenFileCorrupted()
	throws IOException, ChecksumMismatchException {
	String corruptedTestString = "This% is a te#st st@!ri$ng";
	String testStringChecksum = "e2f67c772368acdeee6a2242c535c6cc28d8e0ed";
	byte[] corruptedStringBytes = corruptedTestString.getBytes();
	byte[] checksumBytes = convertChecksumStringToBytes(testStringChecksum);

	byte[] messageBytes = concatArrays(checksumBytes, corruptedStringBytes);
	
	SocketWrapper sock = mock(XFSSocket.class);
	String filename = "file1.txt";

	when(sock.receiveFile()).thenReturn(messageBytes);

	byte[] downloadedBytes = downloader.download(sock, filename);
    }

    @Test
    public void processRequestReturnsCorrectCommandForGetLoad()
	throws IOException {
	SocketWrapper sock = mock(XFSSocket.class);
	PeerAction expectedPeerAction = new PeerAction(PeerAction.SEND_LOAD);	
	when(sock.receive()).thenReturn("GetLoad");
	
	PeerAction actualPeerAction = downloader.processRequest(sock);
	
	assertEquals(expectedPeerAction, actualPeerAction);
    }

    @Test
    public void processRequestReturnsCorrectCommandForDownload()
	throws IOException {
	SocketWrapper sock = mock(XFSSocket.class);
	String filename = "file1.txt";
	PeerAction expectedPeerAction = new
	    PeerAction(PeerAction.SEND_DOWNLOAD, filename);	
	when(sock.receive()).thenReturn("Download::file1.txt");
	
	PeerAction actualPeerAction = downloader.processRequest(sock);
	
	assertEquals(expectedPeerAction, actualPeerAction);
    }

    @Test
    public void getLoadReturnsCorrectLoad() throws IOException {
	SocketWrapper sock = mock(XFSSocket.class);
	int expectedLoad = 5;
	when(sock.receive()).thenReturn("" + 5);

	int actualLoad = downloader.getLoad(sock);

	assertEquals(expectedLoad, actualLoad);
    }

    @Test
    public void getLoadReturnsNegativeWhenInputIsBad() throws IOException {
	SocketWrapper sock = mock(XFSSocket.class);
	int expectedLoad = -1;
	when(sock.receive()).thenReturn("k");

	int actualLoad = downloader.getLoad(sock);

	assertEquals(expectedLoad, actualLoad);
    }

    @Test
    public void getLoadReturnsNegativeWhenPeerIsDown() throws IOException {
	SocketWrapper sock = mock(XFSSocket.class);
	int expectedLoad = -1;
	when(sock.receive()).thenThrow(IOException.class);

	int actualLoad = downloader.getLoad(sock);

	assertEquals(expectedLoad, actualLoad);
    }


    public byte[] concatArrays(byte[] arr1, byte[] arr2) {
	byte[] concatArr = new byte[arr1.length + arr2.length];
	for(int i = 0; i < arr1.length; i++) {
	    concatArr[i] = arr1[i];
	}
	for(int i = 0; i < arr2.length; i++) {
	    concatArr[i + arr1.length] = arr2[i];
	}
	return concatArr;
    }

    public byte[] convertChecksumStringToBytes(String checksumString) {
	byte[] checksumBytes = new byte[checksumString.length()/2];
	for(int i = 0; i < checksumString.length(); i+=2) {
	    checksumBytes[i/2] = Integer.decode("0x" + checksumString.charAt(i) +
						checksumString.charAt(i+1)).byteValue();
	}
	return checksumBytes;
    }
}
