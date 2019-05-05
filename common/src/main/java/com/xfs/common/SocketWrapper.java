package com.xfs.common;

import java.io.IOException;
import java.net.InetAddress;

public interface SocketWrapper {
    public void connect(String ip, int port) throws IOException;
    public void send(String data);
    public void sendFile(byte[] data) throws IOException;
    public String receive() throws IOException;
    public byte[] receiveFile() throws IOException;
    public void close() throws IOException;
    public InetAddress getInetAddress();
}
