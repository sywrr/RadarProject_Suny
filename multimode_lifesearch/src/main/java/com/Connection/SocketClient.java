package com.Connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

public class SocketClient implements Connector {
    private Socket mSocket;
    protected InputStream mInputStream;
    protected OutputStream mOutputStream;
    protected String mHostIp;
    protected int mHostPort;
    protected volatile int mConnectTimeout;
    protected volatile boolean mIsConnected;
    protected volatile boolean mInputShutdown;
    protected volatile boolean mOutputShutdown;

    public SocketClient(String ip, int port) {
        mHostIp = ip;
        mHostPort = port;
        mIsConnected = false;
        mInputShutdown = true;
        mOutputShutdown = true;
    }

    public void setTimeout(int timeout) throws SocketException {
        if (!isConnected())
            throw new SocketException("can not set timeout because connector is not connected");
        if (timeout > 0)
            mSocket.setSoTimeout(timeout);
    }

    protected void checkState() throws SocketException {
        if (mHostPort == -1 || mHostIp == null) {
            throw new SocketException("connector is already closed");
        }
    }

    protected boolean inputAvailable() {
        return mInputStream != null && !mInputShutdown;
    }

    protected boolean outputAvailable() {
        return mOutputStream != null && !mOutputShutdown;
    }

    public boolean isInputShutdown() {
        return mSocket != null && inputAvailable();
    }

    public boolean isOutputShutdown() {
        return mSocket != null && outputAvailable();
    }

    public boolean isConnected() {
        return mIsConnected;
    }

    public void shutdownInput() throws IOException {
        checkState();
        if (mSocket != null && !mInputShutdown) {
            try {
                mSocket.shutdownInput();
            } finally {
                mInputShutdown = true;
            }
        }
    }

    public void shutdownOutput() throws IOException {
        checkState();
        if (mSocket != null && !mOutputShutdown) {
            try {
                mOutputStream.flush();
                mSocket.shutdownOutput();
            } finally {
                mOutputShutdown = true;
            }
        }
    }

    @Override
    public boolean isClosed() {
        return mHostIp == null || mHostPort == -1;
    }

    @Override
    public boolean isConnectionClosed() {
        return mSocket == null;
    }

    @Override
    public void setLongConnection(boolean on) throws SocketException {
        checkState();
        if (mSocket == null)
            throw new SocketException("connection is already closed");
        mSocket.setKeepAlive(on);
    }

    public void closeConnection() throws IOException {
        checkState();
        if (mSocket != null) {
            try {
                mSocket.close();
            } finally {
                mSocket = null;
                mInputShutdown = true;
                mInputStream = null;
                mOutputShutdown = true;
                mOutputStream = null;
                mIsConnected = false;
            }
        }
    }

    public void close() throws IOException {
        closeConnection();
        mHostIp = null;
        mHostPort = -1;
        mConnectTimeout = -1;
    }

    @Override
    public void reconnect() throws IOException {
        if (mSocket != null)
            closeConnection();
        connect(mConnectTimeout);
    }

    public void connect(int timeout) throws IOException {
        checkState();
        if (mSocket == null) {
            mSocket = new Socket();
        }
        mConnectTimeout = timeout;
        if (timeout < 0)
            mSocket.connect(new InetSocketAddress(mHostIp, mHostPort));
        else
            mSocket.connect(new InetSocketAddress(mHostIp, mHostPort), timeout);
        mIsConnected = true;
    }

    public void connect() throws IOException {
        connect(mConnectTimeout);
    }

    @Override
    public int getTimeout() throws SocketException {
        if (!isConnected())
            throw new SocketException("connector is not connected");
        return mSocket.getSoTimeout();
    }

    @Override
    public void setConnectTimeout(int timeout) throws SocketException {
        checkState();
        mConnectTimeout = timeout;
    }

    @Override
    public int getConnectTimeout() throws SocketException {
        checkState();
        return mConnectTimeout;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (mSocket == null) {
            throw new SocketException("connector is not connected");
        }
        if (mInputStream == null) {
            mInputStream = mSocket.getInputStream();
            mInputShutdown = false;
        }
        return mInputStream;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (mSocket == null) {
            throw new SocketException("connector is closed");
        }
        if (mOutputStream == null) {
            mOutputStream = mSocket.getOutputStream();
            mOutputShutdown = false;
        }
        return mOutputStream;
    }
}
