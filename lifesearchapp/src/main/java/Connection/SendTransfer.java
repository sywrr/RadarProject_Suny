package Connection;

import Utils.AbstractLogger;
import Utils.Utils;

import java.io.IOException;
import java.net.SocketException;

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

    // set heart beat packet interval
    // heart beat packet is send only when packet queue is empty
    public void setHeartbeatInterval(int interval) {
        mTransferLock.lock();
        if (mStop) {
            mHeartbeatInterval = interval;
        } else {
            mTransferLock.unlock();
            throw new RuntimeException("heartbeat interval can only set when transfer is stopped");
        }
        mTransferLock.unlock();
    }

    protected Thread mHeartbeatThread = null;

    protected volatile boolean mHeartbeatThreadStop = true;

    // put heart beat packet and sleep interval
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

    // start heart beat thread to add heart beat packet to packet queue
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

    // stop heart beat thread
    private void stopHeartbeatThread() {
        mHeartbeatThreadStop = true;
        mHeartbeatThread.interrupt();
        Utils.joinThreadUninterruptibly(mHeartbeatThread);
    }

    // temp packet for receive and compare to ack
    protected Packet mTmpPacket;

    protected boolean sendPacket(Packet pack) throws IOException {
        if (pack == null) {
            return true;
        }
        if (!mConnector.isConnected())
            return false;
        long st = System.nanoTime();
        long costTime;
        // send pack and receive ack packet
        if (!pack.sendPacket(mConnector.getOutputStream()) || !mTmpPacket.receiveAsPacket(
                mAckPacket, mConnector.getInputStream())) {
            // send failed, close connection for next reconnect
            mConnector.closeConnection();
            costTime = (System.nanoTime() - st) / 1000000;
            AbstractLogger.Error("send packet failed, cost time: " + costTime + "ms", mLogger);
            return false;
        }
//        costTime = (System.nanoTime() - st) / 1000000;
//        AbstractLogger.Debug("send packet success, cost time: " + costTime + "ms", mLogger);
        return true;
    }

    protected void doStart() {
        super.doStart();
        mFailedPacket = null;
        // when startTransfer invoked, start heart beat thread
        startHeartbeatThread();
    }

    protected void doStop() {
        super.doStop();
        // when stopTransfer invoked, stop heart beat thread
        stopHeartbeatThread();
    }

    // packet send failed
    protected Packet mFailedPacket;

    protected void doCommunication() {
        super.doCommunication();
        Packet pack = null;
        try {
            // if has packet send failed, send again
            // otherwise, get packet from packet queue
            if (mFailedPacket == null) {
                while (pack == null && !mStop)
                    pack = mPacketQueue.poll();
            } else {
                pack = mFailedPacket;
            }
            // if send failed, set failed packet
            // otherwise, set failed packet null
            if (sendPacket(pack)) {
                mFailedPacket = null;
            } else {
                mFailedPacket = pack;
            }
        } catch (Exception e) {
            AbstractLogger.Except(e, mLogger);
            mFailedPacket = pack;
        }
    }

    // put packet to packet queue for send
    public void put(Packet pack) {
        mPacketQueue.add(pack);
    }
}
