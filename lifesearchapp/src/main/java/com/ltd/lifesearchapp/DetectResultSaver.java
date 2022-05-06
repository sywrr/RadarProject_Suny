package com.ltd.lifesearchapp;

import android.os.Environment;

import java.io.IOException;

import Connection.Packet;
import Utils.AbstractLogger;
import Utils.Logcat;

public class DetectResultSaver {
    private final byte[] mSaveBytes = new byte[30];

    private final AbstractLogger mLogger = new Logcat("SaverManager", true);

    private DetectResultWriter mDetectResultWriter = null;

    private volatile boolean mAllowWrite = false;

    public DetectResultSaver(String path) {
        try {
            long singleLimit = 1024L * 1024 * 1024;
            mDetectResultWriter = new DetectResultWriter(path, 3 * singleLimit, singleLimit);
        } catch (IOException e) {
            mLogger.errorStackTrace(e);
        }
    }

    public final void startSaveResult() {
        if (mDetectResultWriter != null && !mAllowWrite)
            mAllowWrite = true;
    }

    public final void saveResult(Packet pack) {
        if (mAllowWrite) {
            boolean writeFinished = false;
            try {
                int packetLength = pack.getPacketLength();
                System.arraycopy(pack.data(), 0, mSaveBytes, 0, packetLength);
                mSaveBytes[packetLength] = (byte) (';');
                mDetectResultWriter.write(mSaveBytes, 0, packetLength + 1);
                writeFinished = true;
                mLogger.debug("save one detect result successfully");
            } catch (IOException e) {
                mLogger.errorStackTrace(e);
            } finally {
                if (!writeFinished)
                    mAllowWrite = false;
            }
        }
    }

    public final void finishSaveResult() {
        if (mDetectResultWriter != null) {
            try {
                mDetectResultWriter.finishWrite();
            } catch (IOException e) {
                mLogger.errorStackTrace(e);
            } finally {
                mAllowWrite = false;
                System.out.println("finish save detect result");
            }
        }
    }
}
