package com.Connection;

import com.Utils.AbstractLogger;

import java.io.IOException;

public class ReceiveTransfer extends Transfer {
    public ReceiveTransfer(Connector connector, AbstractLogger logger) {
        super(connector, logger);
    }

    protected Packet receivePacket() throws IOException {
        if (!mConnector.isConnected())
            return null;
        Packet pack = new Packet();
        pack.setPacketFlag(mPacketFlag);
        long st = System.nanoTime();
        long costTime;
        if (!pack.receivePacket(mConnector.getInputStream()) || !mAckPacket.sendPacket(
                mConnector.getOutputStream())) {
            mConnector.closeConnection();
            costTime = (System.nanoTime() - st) / 1000000;
            AbstractLogger.Error("receive packet failed, cost time: " + costTime + "ms", mLogger);
            return null;
        }
//        costTime = (System.nanoTime() - st) / 1000000;
//        AbstractLogger.Debug("receive packet success, cost time: " + costTime + "ms", mLogger);
        return pack;
    }

    protected void doCommunication() {
        super.doCommunication();
        try {
            Packet pack = receivePacket();
            if (pack != null && (mHeartbeatPacket == null || !pack.equals(mHeartbeatPacket)))
                mPacketQueue.add(pack);
        } catch (Exception e) {
            AbstractLogger.Except(e, mLogger);
        }
    }

    public Packet get() {
        return mPacketQueue.poll();
    }
}
