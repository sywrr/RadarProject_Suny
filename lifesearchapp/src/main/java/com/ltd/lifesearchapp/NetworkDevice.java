package com.ltd.lifesearchapp;

import org.json.JSONObject;

import java.net.SocketException;

import Connection.Connector;
import Connection.Packet;
import Connection.PacketCallback;
import Connection.Receiver;
import Connection.ReceiveTransfer;
import Connection.ReceiverGroup;
import Connection.SendTransfer;

import Connection.SocketServer;
import Connection.Transfer;
import Utils.AbstractLogger;
import Utils.Logcat;
import Utils.Utils;

public class NetworkDevice {

    private ReceiveTransfer mDetectTransfer;
    private SendTransfer mCommTransfer;
    private ReceiveTransfer mDataTransfer;

    private AbstractLogger mLogger = new Logcat("NetworkDevice", true);

    private Connector mCommandServer = null;
    private Connector mDetectServer = null;
    private Connector mDataServer = null;

    public ReceiverGroup mReceiverGroup = new ReceiverGroup();

    private int mPort;

    private void startCommTransfer() {
        mCommandServer = new SocketServer(mPort);
        mCommTransfer = new SendTransfer(mCommandServer, new Logcat("CommTransfer", true));
        Packet heartbeatPacket = Global.newPacket(0xAAAABBBB, Global.PACKET_NETWORK, 0);
        Packet ackPacket = Global.newPacket(0xAAAABBBB, Global.PACKET_ACK, 0);
        try {
            mCommTransfer.initTransfer(0xAAAABBBB, heartbeatPacket, ackPacket, -1, 5000, 0);
            mCommTransfer.setHeartbeatInterval(1000);
            mCommTransfer.startTransfer();
        } catch (SocketException e) {
            AbstractLogger.Except(e, mLogger);
        }
    }

    private void startDetectTransfer() {
        mDetectServer = new SocketServer(mPort + 2);
        mDetectTransfer = new ReceiveTransfer(mDetectServer,
                                              new Logcat("DetectTransfer", true));
        Packet heartbeatPacket = Global.newPacket(0xAAAABBBB, Global.PACKET_NETWORK, 0);
        Packet ackPacket = Global.newPacket(0xAAAABBBB, Global.PACKET_ACK, 0);
        try {
            mDetectTransfer.initTransfer(0xAAAABBBB, heartbeatPacket, ackPacket, -1, 5000, 0);
            mDetectTransfer.startTransfer();
        } catch (SocketException e) {
            AbstractLogger.Except(e, mLogger);
        }
    }

    private void startDataTransfer() {
        mDataServer = new SocketServer(mPort + 1);
        mDataTransfer = new ReceiveTransfer(mDataServer,
                                            new Logcat("DataTransfer", true));
        Packet heartbeatPacket = Global.newPacket(0xAAAABBBB, Global.PACKET_NETWORK, 0);
        Packet ackPacket = Global.newPacket(0xAAAABBBB, Global.PACKET_ACK, 0);
        try {
            mDataTransfer.initTransfer(0xAAAABBBB, heartbeatPacket, ackPacket, -1, 5000, 0);
            mDataTransfer.startTransfer();
        } catch (SocketException e) {
            AbstractLogger.Except(e, mLogger);
        }
    }

    public void stopNetTransfers() {
        mDetectTransfer.postStop();
        mCommTransfer.postStop();
        mDataTransfer.postStop();
        AbstractLogger.Debug("receiver is stopped", mLogger);
        mReceiverGroup.unregisterAllReceivers();
        AbstractLogger.Debug("stop transfers finished", mLogger);
    }

    public void closeConnectors() {
        try {
            if (mDetectServer != null)
                mDetectServer.close();
            if (mCommandServer != null)
                mCommandServer.close();
            if (mDataServer != null)
                mDataServer.close();
        } catch (Exception e) {
            AbstractLogger.Except(e, mLogger);
        }
        AbstractLogger.Debug("all server connector is closed", mLogger);
    }

    public boolean isConnected() {
        return mCommTransfer.isConnected() && mDetectTransfer.isConnected() &&
               (mDataTransfer == null || mDataTransfer.isConnected());
    }

    public void putCommandPacket(Packet pack) {
        mCommTransfer.put(pack);
    }

    public NetworkDevice(int port) {
        mPort = port;
        startCommTransfer();
        startDetectTransfer();
        startDataTransfer();
        Receiver detectReceiver = new Receiver();
        detectReceiver.setReceiveTransfer(mDetectTransfer);
        mReceiverGroup.registerReceiver("detect", detectReceiver);
        Receiver dataReceiver = new Receiver();
        dataReceiver.setReceiveTransfer(mDataTransfer);
        mReceiverGroup.registerReceiver("radar_data", dataReceiver);
    }

    public AbstractLogger getLogger() {
        return mLogger;
    }
}
