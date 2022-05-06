package com.ltd.lifesearchapp;

import android.util.Log;

import Connection.Packet;
import Connection.PacketCallback;
import Utils.AbstractLogger;

public class MyCallback implements PacketCallback {

    protected AbstractLogger mLogger;

    protected String mPacketName;

    public MyCallback(AbstractLogger logger, String packetName) {
        mLogger = logger;
        mPacketName = packetName;
    }

    @Override
    public void invoke(Packet pack) {
//        AbstractLogger.Info("handle " + mPacketName + " packet", mLogger);
    }
}
