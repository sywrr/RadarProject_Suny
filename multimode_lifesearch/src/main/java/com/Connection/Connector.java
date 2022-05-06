package com.Connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

public interface Connector {
    void connect(int timeout) throws IOException;

    void connect() throws IOException;

    void closeConnection() throws IOException;

    void close() throws IOException;

    void reconnect() throws IOException;

    InputStream getInputStream() throws IOException;

    OutputStream getOutputStream() throws IOException;

    boolean isConnected();

    boolean isInputShutdown();

    boolean isOutputShutdown();

    void setTimeout(int timeout) throws SocketException;

    int getTimeout() throws SocketException;

    void setConnectTimeout(int timeout) throws SocketException;

    int getConnectTimeout() throws SocketException;

    void shutdownInput() throws IOException;

    void shutdownOutput() throws IOException;

    boolean isClosed();

    boolean isConnectionClosed();

    void setLongConnection(boolean on) throws SocketException;
}
