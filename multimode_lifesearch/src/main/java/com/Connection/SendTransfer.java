package com.Connection;

import com.Utils.AbstractLogger;

import java.io.IOException;
import java.net.SocketException;

import com.Utils.Utils;

public class SendTransfer extends Transfer {
    public SendTransfer(Connector connector, AbstractLogger logger) {
        super(connector, logger);
        mTmpPacket = new Packet();
    }

    @Override
    public void initTransfer(int packetFlag, Packet heartbeatPacket, Packet ackPacket) {
        super.initTransfer(packetFlag, heartbeatPacket, ackPacket);
        mTmpPacket.setPacketFlag(mPacketFlag);
    }

    @Override
    public void initTransfer(int packetFlag, Packet heartbeatPacket, Packet ackPacket,
                             int connectTimeout, int timeout, int reconnectInterval)
            throws SocketException {
        super.initTransfer(packetFlag, heartbeatPacket, ackPacket, connectTimeout, timeout,
                           reconnectInterval);
        mTmpPacket.setPacketFlag(mPacketFlag);
    }

    protected int mHeartbeatInterval = -1;

    public void setHeartbeatInterval(int interval) {
        mSpainLock.lock();
        if (mStop) {
            mHeartbeatInterval = interval;
        } else {
            mSpainLock.unlock();
            throw new RuntimeException("heartbeat interval can only set when transfer is stopped");
        }
        mSpainLock.unlock();
    }

    protected Thread mHeartbeatThread = null;
    protected volatile boolean mHeartbeatThreadStop = true;

    protected void putHeartbeatPacket() {
        long st = System.nanoTime();
        if (mPacketQueue.isEmpty() && mFailedPacket == null)
            mPacketQueue.add(mHeartbeatPacket.copy());
        long et = System.nanoTime();
        if ((et - st) / 1000000 < mHeartbeatInterval) {
            long sleepTime = mHeartbeatInterval * 1000000 - (et - st);
            long millions = (sleepTime) / 1000000;
            int nanos = (int) ((sleepTime) % 1000000);
            try {
                Thread.sleep(millions, nanos);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void startHeartbeatThread() {
        if (mHeartbeatInterval == -1)
            throw new RuntimeException("set heartbeat interval before start transfer");
        mHeartbeatThreadStop = false;
        mHeartbeatThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mHeartbeatThreadStop) {
                    putHeartbeatPacket();
                }
                AbstractLogger.Debug("heartbeat thread finished", mLogger);
            }
        });
        mHeartbeatThread.start();
    }

    private void stopHeartbeatThread() {
        mHeartbeatThreadStop = true;
        mHeartbeatThread.interrupt();
        Utils.joinThreadUninterruptibly(mHeartbeatThread);
    }

    protected Packet mTmpPacket;

    protected boolean sendPacket(Packet pack) throws IOException {
        if (pack == null) {
            return true;
        }
        if (!mConnector.isConnected())
            return false;
        long st = System.nanoTime();
        long costTime;
        if (!pack.sendPacket(mConnector.getOutputStream()) || !mTmpPacket.receiveAsPacket(
                mAckPacket, mConnector.getInputStream())) {
            mConnector.closeConnection();
            costTime = (System.nanoTime() - st) / 1000000;
            AbstractLogger.Error("send packet failed, cost time: " + costTime + "ms", mLogger);
            return false;
        }
//        costTime = (System.nanoTime() - st) / 1000000;
//        AbstractLogger.Debug("send packet success, cost time: " + costTime + "ms", mLogger);
//        mLogger.debug("send successfully");
        return true;
    }

    protected void doStart() {
        super.doStart();
        mFailedPacket = null;
        startHeartbeatThread();
    }

    protected void doStop() {
        super.doStop();
        stopHeartbeatThread();
    }

    protected Packet mFailedPacket;

    protected void doCommunication() {
        long st = System.nanoTime();
        super.doCommunication();
        Packet pack = null;
        boolean success = false;
        try {
            if (mFailedPacket == null) {
                pack = mPacketQueue.poll();
                if (pack == null) {
                    Thread.sleep(10);
                    return;
                }
            } else {
                pack = mFailedPacket;
            }
            if ((success = sendPacket(pack))) {
                mFailedPacket = null;
            } else {
                mFailedPacket = pack;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (!success)
                mFailedPacket = pack;
        }
    }

    public void put(Packet pack) {
        mPacketQueue.add(pack);
    }
}
