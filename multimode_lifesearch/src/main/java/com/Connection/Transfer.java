package com.Connection;

import com.Concurrent.CLHLock;
import com.Utils.AbstractLogger;
import com.Utils.Utils;

import java.io.IOException;
import java.net.SocketException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class Transfer {

    protected AbstractLogger mLogger;
    protected Queue<Packet> mPacketQueue;
    protected Connector mConnector;

    protected Packet mHeartbeatPacket = null;
    protected Packet mAckPacket = null;

    protected volatile int mReconnectInterval = -1;

    protected volatile boolean mStop = true;

    protected volatile int mPacketFlag;

    public Transfer(Connector connector, AbstractLogger logger) {
        if (connector == null)
            throw new RuntimeException("connector and queue can not be null");
        mConnector = connector;
        mPacketQueue = new ConcurrentLinkedQueue<>();
//        mPacketQueue = new LinkedBlockingQueue<>();
        mLogger = logger;
        mThread = null;
    }

    private void setPacketFlag(int packetFlag) {
        mPacketFlag = packetFlag;
    }

    public void initTransfer(int packetFlag, Packet heartbeatPacket, Packet ackPacket) {
        mSpainLock.lock();
        if (mStop) {
            mHeartbeatPacket = heartbeatPacket;
            mAckPacket = ackPacket;
            setPacketFlag(packetFlag);
        } else {
            mSpainLock.unlock();
            throw new RuntimeException("can not set connection params before close transfer");
        }
        mSpainLock.unlock();
    }

    public void initTransfer(int packetFlag, Packet heartbeatPacket, Packet ackPacket,
                             int connectTimeout, int timeout, int reconnectInterval)
            throws SocketException {
        mSpainLock.lock();
        try {
            if (mStop) {
                setPacketFlag(packetFlag);
                mHeartbeatPacket = heartbeatPacket;
                mAckPacket = ackPacket;
                mTimeout = timeout;
                mReconnectInterval = reconnectInterval;
                mConnector.setConnectTimeout(connectTimeout);
            } else {
                throw new RuntimeException("can not set connection params before close transfer");
            }
        } finally {
            mSpainLock.unlock();
        }
    }

    private void checkInitState() {
        if (mHeartbeatPacket == null || mAckPacket == null)
            throw new RuntimeException("transfer is not initialized");
    }

    protected volatile int mTimeout = -1;

    protected volatile Thread mThread;

    protected void doCommunication() {}

    protected void doThreadStop() {
        if (!mConnector.isConnectionClosed()) {
            try {
                mConnector.shutdownInput();
                mConnector.shutdownOutput();
                mConnector.closeConnection();
            } catch (IOException e) {
                AbstractLogger.Except(e, mLogger);
            }
        }
    }

    private void firstConnect() {
        if (!mConnector.isConnected()) {
            try {
                mConnector.connect();
            } catch (IOException e) {
                AbstractLogger.Except(e, mLogger);
                return;
            }
        }
        if (!setConnectorTimeout()) {
            try {
                mConnector.closeConnection();
            } catch (IOException e) {
                AbstractLogger.Except(e, mLogger);
            }
        }
    }

    private boolean setConnectorTimeout() {
        try {
            mConnector.setTimeout(mTimeout);
            return true;
        } catch (SocketException e) {
            AbstractLogger.Except(e, mLogger);
        }
        return false;
    }

    protected void doStop() {}

    protected void doStart() {
        if (mAckPacket == null)
            throw new RuntimeException("ack packet can not be null");
    }

    protected void doReconnect() {
        try {
            if (mReconnectInterval > 0) {
                Thread.sleep(mReconnectInterval);
            }
            mConnector.reconnect();
            if (!setConnectorTimeout()) {
                mConnector.closeConnection();
            }
        } catch (InterruptedException | IOException e) {
            AbstractLogger.Except(e, mLogger);
        }
    }

    private void doRun() {
        firstConnect();
        while (!mStop) {
            if (!mConnector.isConnected()) {
                doReconnect();
            } else {
                doCommunication();
            }
        }
        doThreadStop();
    }

    public boolean isRunning() {
        return !mStop;
    }

    public int size() {
        return mPacketQueue.size();
    }

    public boolean isEmpty() {
        return mPacketQueue.isEmpty();
    }

    protected CLHLock mSpainLock = new CLHLock();

    public void startTransfer() {
        mSpainLock.lock();
        if (mThread != null && !mStop) {
            mSpainLock.unlock();
            Utils.joinThreadUninterruptibly(mThread);
            mSpainLock.lock();
        }
        try {
            if (mStop) {
                checkInitState();
                doStart();
                mStop = false;
                mThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        doRun();
                    }
                });
                mThread.start();
            }
        } finally {
            mSpainLock.unlock();
        }
    }

    public void postStop() {
        mSpainLock.lock();
        if (!mStop) {
            mStop = true;
            mThread.interrupt();
            doStop();
        }
        mSpainLock.unlock();
    }

    public void clearTransfer() {
        mPacketQueue.clear();
    }

    public boolean isConnected() {
        return mConnector.isConnected();
    }
}
