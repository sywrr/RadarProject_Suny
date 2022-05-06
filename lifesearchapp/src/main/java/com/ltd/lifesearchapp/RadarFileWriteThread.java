package com.ltd.lifesearchapp;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import Connection.Packet;
import Utils.Logcat;

public class RadarFileWriteThread extends Thread {

    public RadarFileWriteThread(String path) {
        super();
        mDir = path;
    }

    private final static Packet finishPacket;

    static {
        finishPacket = new Packet(Global.PACKET_ACK, 2);
        finishPacket.setPacketFlag(0xAAAABBBB);
        finishPacket.putShort((short) 0);
    }

    private final String mDir;

    private RadarFileWriter mWriter;

    private volatile boolean mStop = false;

    private volatile boolean mWriting = true;

    private final Logcat mLogger = new Logcat("RadarFileWriteThread", true);

    private final ConcurrentLinkedQueue<Packet> mQueue = new ConcurrentLinkedQueue<>();

    private void doWrite(Packet pack) {
        pack.seek(0);
        boolean writeFinished = false;
        try {
            if (pack.getPacketType() == Global.PACKET_FILE_HEAD) {
                if (mWriter.isWriting())
                    throw new IllegalStateException(
                            "got file head but last save is not finished???");
                mWriter.write(pack.data(), 0, pack.getPacketLength());
                mWriting = true;
                mLogger.debug("write file head successfully");
            } else if (pack.getPacketType() == Global.PACKET_DATA) {
                mWriter.write(pack.data(), 2, pack.getPacketLength() - 2);
                mLogger.debug("write file data successfully");
            }
            writeFinished = true;
        } catch (IOException e) {
            mLogger.error(e);
        } finally {
            if (!writeFinished)
                mWriting = false;
        }
    }

    private void init() {
        mStop = true;
        mLogger.debug("path: " + mDir);
        File file = new File(mDir);
        if (!file.exists() && !file.mkdirs()) {
            mLogger.error("mkdirs failed");
            return;
        }
        mLogger.debug("mkdirs successfully");
        mWriter = null;
        try {
            long totalLimit = 1024L * 1024 * 1024 * 3;
            long singleLimit = 1024L * 1024 * 1024;
            mWriter = new RadarFileWriter(mDir, totalLimit, singleLimit);
            mLogger.debug("init writer finished");
            mStop = false;
        } catch (IOException e) {
            mLogger.error(e);
        }
    }

    private void finishSave() {
        try {
            mWriter.finishWrite();
        } catch (IOException e) {
            mLogger.error(e);
        } finally {
            mWriting = false;
        }
    }

    private volatile boolean mOneWriteFinished = true;

    @Override
    public final void run() {
        init();
        Packet pack;
        while (!mStop || !mOneWriteFinished) {
            pack = mQueue.poll();
            if (pack != null && pack.equals(finishPacket)) {
                mLogger.debug("finish packet received");
                finishSave();
                mOneWriteFinished = true;
            } else if (pack == null ||
                       (pack.getPacketType() != Global.PACKET_FILE_HEAD && !mWriting)) {
                if (pack == null && mStop) {
                    mLogger.debug("stop write thread without receive finish packet");
                    break;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignore) {}
            } else {
                mOneWriteFinished = false;
                doWrite(pack);
            }
        }
    }

    public final synchronized void stopWrite() {
        if (!mStop) {
            mStop = true;
            if (isAlive()) {
                interrupt();
                while (isAlive()) {
                    try {
                        join();
                    } catch (InterruptedException ignore) {}
                }
            }
            finishSave();
        }
        mLogger.debug("radar file write thread exit");
    }

    public final void postWrite(Packet pack) {
        mQueue.offer(pack);
    }
}
