package Connection;

import com.ltd.lifesearchapp.Global;

import Utils.AbstractLogger;

import java.io.IOException;

public class ReceiveTransfer extends Transfer {
    public ReceiveTransfer(Connector connector, AbstractLogger logger) {
        super(connector, logger);
    }

    protected Packet receivePacket() throws IOException {
        if (!mConnector.isConnected())
            return null;
        // create new packet
        Packet pack = new Packet();
        pack.setPacketFlag(mPacketFlag);
        long st = System.nanoTime();
        long costTime;
        // receive packet and send ack
        if (!pack.receivePacket(mConnector.getInputStream()) || !mAckPacket.sendPacket(
                mConnector.getOutputStream())) {
            mConnector.closeConnection();
            costTime = (System.nanoTime() - st) / 1000000;
            AbstractLogger.Error("receive packet failed, cost time: " + costTime + "ms", mLogger);
            return null;
        }
        if (pack.getPacketType() == Global.PACKET_FILE_HEAD)
            mLogger.debug("receive file head length: " + pack.getPacketLength());
        return pack;
    }

    protected void doCommunication() {
        super.doCommunication();
        try {
            // if receive packet successfully and not equals to heart beat packet
            // add to packet queue
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
