package com.Connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class SocketServer implements Connector {

    protected ServerSocket mServer = null;
    protected Socket mProxy = null;
    protected InputStream mInputStream = null;
    protected OutputStream mOutputStream = null;
    protected volatile boolean mInputShutdown = true;
    protected volatile boolean mOutputShutdown = true;
    protected volatile boolean mIsConnected = false;
    protected int mPort;
    protected int mConnectTimeout = -1;

    public SocketServer(int port) {
        mPort = port;
    }

    public void setConnectTimeout(int timeout) throws SocketException {
        checkState();
        mConnectTimeout = timeout;
    }

    public int getConnectTimeout() throws SocketException {
        checkState();
        return mConnectTimeout;
    }

    protected void closeServer() throws IOException {
        if (mServer != null) {
            try {
                mServer.close();
            } finally {
                mServer = null;
                mPort = -1;
                mConnectTimeout = -1;
            }
        }
    }

    protected void checkState() throws SocketException {
        if (mPort == -1) {
            throw new SocketException("connector is already closed");
        }
    }

    protected boolean inputAvailable() {
        return mInputStream != null && !mInputShutdown;
    }

    protected boolean outputAvailable() {
        return mOutputStream != null && !mOutputShutdown;
    }

    public boolean isConnected() {
        return mIsConnected;
    }

    @Override
    public void setTimeout(int timeout) throws SocketException {
        if (!isConnected())
            throw new SocketException(
                    "can not set timeout because connector is no longer " + "connected");
        if (timeout > 0)
            mProxy.setSoTimeout(timeout);
    }

    @Override
    public int getTimeout() throws SocketException {
        if (!isConnected())
            throw new SocketException(
                    "can not get timeout because connector is no longer " + "connected");
        return mProxy.getSoTimeout();
    }

    public boolean isInputShutdown() {
        return mProxy != null && inputAvailable();
    }

    public boolean isOutputShutdown() {
        return mProxy != null && outputAvailable();
    }

    protected void closeProxy() throws IOException {
        if (mProxy != null) {
            try {
                mProxy.close();
            } finally {
                mProxy = null;
                mInputShutdown = true;
                mInputStream = null;
                mOutputStream = null;
                mOutputShutdown = true;
                mIsConnected = false;
            }
        }
    }

    public void shutdownInput() throws IOException {
        checkState();
        if (mProxy != null && !mInputShutdown) {
            try {
                mProxy.shutdownInput();
            } finally {
                mInputShutdown = true;
            }
        }
    }

    public void shutdownOutput() throws IOException {
        checkState();
        if (mProxy != null && !mOutputShutdown) {
            try {
                mOutputStream.flush();
                mProxy.shutdownOutput();
            } finally {
                mOutputShutdown = true;
            }
        }
    }

    @Override
    public boolean isClosed() {
        return mPort == -1;
    }

    @Override
    public boolean isConnectionClosed() {
        return mProxy == null;
    }

    @Override
    public void setLongConnection(boolean on) throws SocketException {
        checkState();
        if (mProxy == null)
            throw new SocketException("connection is already closed");
        mProxy.setKeepAlive(on);
    }

    @Override
    public void connect(int timeout) throws IOException {
        checkState();
        if (mProxy != null)
            throw new SocketException("connector is already connected");
        boolean needSetConnectTimeout = false;
        if (mServer == null) {
            mServer = new ServerSocket(mPort);
            needSetConnectTimeout = true;
        } else if (mConnectTimeout != timeout) {
            needSetConnectTimeout = true;
        }
        if (needSetConnectTimeout) {
            mConnectTimeout = timeout;
            if (timeout > 0)
                mServer.setSoTimeout(timeout);
        }
        mProxy = mServer.accept();
        mIsConnected = true;
    }

    @Override
    public void connect() throws IOException {
        connect(mConnectTimeout);
    }

    public void closeConnection() throws IOException {
        checkState();
        closeProxy();
    }

    public void close() throws IOException {
        try {
            closeConnection();
        } finally {
            closeServer();
        }
    }

    @Override
    public void reconnect() throws IOException {
        if (mProxy != null)
            closeConnection();
        connect(mConnectTimeout);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (mProxy == null) {
            throw new SocketException("connector is not connected");
        }
        if (mInputStream == null) {
            mInputStream = mProxy.getInputStream();
            mInputShutdown = false;
        }
        return mInputStream;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (mProxy == null) {
            throw new SocketException("connector is not connected");
        }
        if (mOutputStream == null) {
            mOutputStream = mProxy.getOutputStream();
            mOutputShutdown = false;
        }
        return mOutputStream;
    }
}
