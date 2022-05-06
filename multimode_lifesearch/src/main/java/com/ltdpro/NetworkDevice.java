package com.ltdpro;

import android.content.Context;

import com.Connection.Connector;
import com.Connection.Packet;
import com.Connection.ReceiveTransfer;
import com.Connection.SendTransfer;
import com.Connection.SocketClient;
import com.Utils.AbstractLogger;
import com.Utils.Logcat;

import java.net.SocketException;

public class NetworkDevice {

    private ReceiveTransfer mCommTransfer;
    private Uploader.UploadTransfer mDataTransfer;
    private SendTransfer mDetectTransfer;

    private final Uploader mUploader = new Uploader();

    private AbstractLogger mLogger = new Logcat("NetworkDevice", true);

    private String mHostIp;
    private int mHostPort;

    private CommHandler mHandler;

    private Connector mCommandClient, mDetectClient, mDataClient;

    private void startCommTransfer(Context context) {
        AbstractLogger logger = new Logcat("CommTransfer", true);
        mCommandClient = new SocketClient(mHostIp, mHostPort);
        mCommTransfer = new ReceiveTransfer(mCommandClient, logger);
        Packet heartbeatPacket = Global.newPacket(0xAAAABBBB, Global.PACKET_NETWORK, 0);
        Packet ackPacket = Global.newPacket(0xAAAABBBB, Global.PACKET_ACK, 0);
        try {
            mCommTransfer.initTransfer(0xAAAABBBB, heartbeatPacket, ackPacket, 5000, 5000, 500);
        } catch (SocketException e) {
            mLogger.error(e);
        }
        mCommTransfer.startTransfer();
        mHandler = new CommHandler(context, mDetectTransfer, mCommTransfer);
    }

    private void startDetectTransfer() {
        AbstractLogger logger = new Logcat("DetectTransfer", true);
        mDetectClient = new SocketClient(mHostIp, mHostPort + 2);
        mDetectTransfer = new SendTransfer(mDetectClient, logger);
        Packet heartbeatPacket = Global.newPacket(0xAAAABBBB, Global.PACKET_NETWORK, 0);
        Packet ackPacket = Global.newPacket(0xAAAABBBB, Global.PACKET_ACK, 0);
        try {
            mDetectTransfer.initTransfer(0xAAAABBBB, heartbeatPacket, ackPacket, 5000, 5000, 500);
        } catch (SocketException e) {
            mLogger.error(e);
        }
        mDetectTransfer.setHeartbeatInterval(1000);
        mDetectTransfer.startTransfer();
    }

    private void startDataTransfer() {
        AbstractLogger logger = new Logcat("DataTransfer", true);
        mDataClient = new SocketClient(mHostIp, mHostPort + 1);
        mDataTransfer = mUploader.new UploadTransfer(mDataClient, logger);
        mUploader.setUploadTransfer(mDataTransfer);
        Packet heartbeatPacket = Global.newPacket(0xAAAABBBB, Global.PACKET_NETWORK, 0);
        Packet ackPacket = Global.newPacket(0xAAAABBBB, Global.PACKET_ACK, 0);
        try {
            mDataTransfer.initTransfer(0xAAAABBBB, heartbeatPacket, ackPacket, 5000, 5000, 500);
        } catch (SocketException e) {
            mLogger.error(e);
        }
        mDataTransfer.setHeartbeatInterval(1000);
        mDataTransfer.startTransfer();
    }

    public NetworkDevice(String ip, int port, Context context) {
        mHostIp = ip;
        mHostPort = port;
        startDetectTransfer();
        startCommTransfer(context);
        startDataTransfer();
    }

    public void stopNetTransfers() {
        mDetectTransfer.postStop();
        mCommTransfer.postStop();
        mDataTransfer.postStop();
        mHandler.stopHandler();
        AbstractLogger.Debug("all transfers and handler stopped", mLogger);
    }

    public void waitTransfersAllStop() {
        try {
            mCommandClient.close();
        } catch (Exception e) {
            AbstractLogger.Except(e, mLogger);
        }
        try {
            mDetectClient.close();
        } catch (Exception e) {
            AbstractLogger.Except(e, mLogger);
        }
        try {
            mDataClient.close();
        } catch (Exception e) {
            AbstractLogger.Except(e, mLogger);
        }
        AbstractLogger.Debug("all client connector is closed", mLogger);
    }

    public boolean isConnected() {
        return mDetectTransfer.isConnected() && mCommTransfer.isConnected() &&
               mDataTransfer.isConnected();
    }

    public void putDataPacket(Packet pack) {
        mDataTransfer.put(pack);
    }

    public Uploader getUploader() { return mUploader; }
}
