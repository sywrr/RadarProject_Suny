package Connection;

import Utils.AbstractLogger;
import Utils.Utils;

import java.io.IOException;
import java.net.SocketException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * transfer for generate connection
 */
public abstract class Transfer {
    protected AbstractLogger mLogger;
    // queue for store packet
    protected Queue<Packet> mPacketQueue;

    // connector for establish connection and io operation
    protected Connector mConnector;

    // heart beat packet
    protected Packet mHeartbeatPacket = null;

    // ack packet
    protected Packet mAckPacket = null;

    // interval for reconnect
    protected volatile int mReconnectInterval = -1;

    // if transfer is stopped
    protected volatile boolean mStop = true;

    // packet flag for identify packet
    protected volatile int mPacketFlag;

    public Transfer(Connector connector, AbstractLogger logger) {
        if (connector == null)
            throw new RuntimeException("connector and queue can not be null");
        mConnector = connector;
        mPacketQueue = new LinkedBlockingQueue<>();
        mLogger = logger;
        mThread = null;
    }

    private void setPacketFlag(int packetFlag) {
        mPacketFlag = packetFlag;
    }

    // initialize transfer, invoked before startTransfer
    public void initTransfer(int packetFlag, Packet heartbeatPacket, Packet ackPacket) {
        mTransferLock.lock();
        if (mStop) {
            mHeartbeatPacket = heartbeatPacket;
            mAckPacket = ackPacket;
            setPacketFlag(packetFlag);
        } else {
            mTransferLock.unlock();
            throw new RuntimeException("can not set connection params before stop transfer");
        }
        mTransferLock.unlock();
    }

    // initialize transfer, invoked before startTransfer
    public void initTransfer(
            int packetFlag, Packet heartbeatPacket, Packet ackPacket, int connectTimeout,
            int timeout, int reconnectInterval)
            throws SocketException {
        mTransferLock.lock();
        try {
            if (mStop) {
                setPacketFlag(packetFlag);
                mHeartbeatPacket = heartbeatPacket;
                mAckPacket = ackPacket;
                mTimeout = timeout;
                mReconnectInterval = reconnectInterval;
                mConnector.setConnectTimeout(connectTimeout);
            } else {
                throw new RuntimeException("can not set connection params before stop transfer");
            }
        } finally {
            mTransferLock.unlock();
        }
    }

    private void checkInitState() {
        if (mHeartbeatPacket == null || mAckPacket == null)
            throw new RuntimeException("transfer is not initialized");
    }

    protected volatile int mTimeout = -1;

    protected volatile Thread mThread;

    // implemented by derived for communication
    protected void doCommunication() {}

    // invoked on work thread terminated
    protected void doThreadStop() {
        if (!mConnector.isConnectionClosed() && !mConnector.isClosed()) {
            try {
                mConnector.shutdownInput();
                mConnector.shutdownOutput();
                mConnector.closeConnection();
            } catch (IOException e) {
                AbstractLogger.Except(e, mLogger);
            }
        }
    }

    // initial connect
    private void firstConnect() {
        // if not connected, connect
        if (!mConnector.isConnected()) {
            try {
                mConnector.connect();
            } catch (IOException e) {
                AbstractLogger.Except(e, mLogger);
                return;
            }
        }
        // set connector io timeout
        if (!setConnectorTimeout()) {
            try {
                mConnector.closeConnection();
            } catch (IOException e) {
                AbstractLogger.Except(e, mLogger);
            }
        }
    }

    // set connector io timeout
    private boolean setConnectorTimeout() {
        try {
            mConnector.setTimeout(mTimeout);
            return true;
        } catch (SocketException e) {
            AbstractLogger.Except(e, mLogger);
        }
        return false;
    }

    // invoked on stopTransfer called
    protected void doStop() {}

    // invoked on startTransfer called
    protected void doStart() {
        if (mAckPacket == null)
            throw new RuntimeException("ack packet can not be null");
    }

    // reconnect, close current connection and establish new
    protected void doReconnect() {
        try {
            // sleep to resolve bad performance caused by busy connect requests
            if (mReconnectInterval > 0) {
                Thread.sleep(mReconnectInterval);
            }
            // make reconnect
            mConnector.reconnect();
            // set connector io timeout
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
            // if current state is connected, communicate
            // otherwise, reconnect
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

    protected final Lock mTransferLock = new ReentrantLock(true);

    public void startTransfer() {
        mTransferLock.lock();
        // wait util last transfer is shutdown
        if (mThread != null && !mStop) {
            mTransferLock.unlock();
            Utils.joinThreadUninterruptibly(mThread);
            mTransferLock.lock();
        }
        try {
            if (mStop) {
                checkInitState();
                doStart();
                // start new thread for transfer
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
            mTransferLock.unlock();
        }
    }

    // post stop signal and call doStop
    public void postStop() {
        mTransferLock.lock();
        if (!mStop) {
            mStop = true;
            mThread.interrupt();
            doStop();
        }
        mTransferLock.unlock();
    }

    // clear all packets placed in transfer
    public void clearTransfer() {
        mTransferLock.lock();
        if (mStop) {
            mPacketQueue.clear();
        }
        mTransferLock.unlock();
        throw new RuntimeException("can not clear transfer unless transfer is stopped");
    }

    public boolean isConnected() {
        return mConnector.isConnected();
    }
}
