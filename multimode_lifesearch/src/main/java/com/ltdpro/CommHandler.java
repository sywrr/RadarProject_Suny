package com.ltdpro;

import android.content.Context;

import com.Connection.Packet;
import com.Connection.ReceiveTransfer;
import com.Connection.SendTransfer;
import com.Utils.AbstractLogger;
import com.Utils.Logcat;

public class CommHandler {
    private final Context mContext;

    private SendTransfer mSendTransfer;

    private ReceiveTransfer mReceiveTransfer;

    private volatile boolean mStop;

    private AbstractLogger mLogger = new Logcat("CommHandler", true);

    private radarDevice getRadarDevice() {
        return ((MyApplication) (mContext.getApplicationContext())).mRadarDevice;
    }

    private RadarDetect getRadarDetect() {
        return ((MyApplication) (mContext.getApplicationContext())).mRadarDetect;
    }

    public CommHandler(Context context, SendTransfer sendTransfer,
                       ReceiveTransfer receiveTransfer) {
        mContext = context;
        mSendTransfer = sendTransfer;
        mReceiveTransfer = receiveTransfer;
        getRadarDetect().setTransfer(mSendTransfer);
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                doRun();
            }
        });
        mThread.start();
    }

    private void setTimeWindow(int timeWindow) {
        mLogger.debug("设置时窗: " + timeWindow);
        getRadarDevice().setTimeWindow(timeWindow, true);
        short scanSpeed = (short) (16 * (80 / timeWindow));
        short scanLength = (short) (8192 / (80 / timeWindow));
        if (timeWindow == 20)
            scanSpeed = 32;
        if (scanSpeed > getRadarDevice().getScanSpeed()) {
            getRadarDevice().directSetScanLength(scanLength);
            getRadarDevice().directSetScanSpeed(scanSpeed);
        } else {
            getRadarDevice().directSetScanSpeed(scanSpeed);
            getRadarDevice().directSetScanLength(scanLength);
        }
    }

    private void setRadarDetectParams(short detectMode, short detectStart, short detectEnd,
                                      short autoDetect, int timeWindow) {
        mLogger.debug(detectMode == 0 ? "单目标模式" : "多目标模式");
        mLogger.debug("detect range: " + detectStart + ", " + detectEnd);
        mLogger.debug(autoDetect == 1 ? "自动搜寻" : "手动搜寻");
        mLogger.debug("时窗: " + timeWindow);
        BaseDetect.setMultiMode(detectMode);
        getRadarDetect().setDetectRange(detectStart, detectEnd, autoDetect == (short) 1);
        int signalPos = detectStart * 20 / 3 + 9;
        mLogger.debug("设置信号位置: " + signalPos);
        getRadarDevice().setSignalpos(signalPos);
        setTimeWindow(timeWindow);
    }

    private void handleCommPacket(Packet pack) {
        if (!pack.canGetShort())
            return;
        switch (pack.getShort()) {
            case Global.RADAR_COMMAND_BEGDETECT:
                Packet responseStartPacket = new Packet(Global.PACKET_START_DETECT_RESPONSE, 0);
                responseStartPacket.setPacketFlag(0xAAAABBBB);
                mSendTransfer.put(responseStartPacket);
                mLogger.debug("start detect");
                getRadarDetect().setSend(true);
                getRadarDetect().setStoragePath(getRadarDevice().getStoragePath());
                setRadarDetectParams(pack.getShort(), pack.getShort(), pack.getShort(),
                                     pack.getShort(), pack.getShort() == 3 ? 20 : 80);
                getRadarDetect().startRadarDetect();
                break;
            case Global.RADAR_COMMAND_ENDDETECT:
                mLogger.debug("stop detect");
                getRadarDetect().stopRadarDetect();
                getRadarDetect().resetManager();
                mSendTransfer.clearTransfer();
                break;
            case Global.RADAR_COMMAND_UPLOAD:
                getRadarDevice().refreshFileHeader();
                getRadarDevice().setUpload(true);
                mLogger.debug("开始上传");
                break;
            case Global.RADAR_COMMAND_STOP_UPLOAD:
                getRadarDevice().setUpload(false);
                mLogger.debug("停止上传");
                break;
            case Global.RADAR_COMMAND_SIGNALPOS:
                int signalPos = pack.getShort();
                getRadarDevice().setSignalpos(signalPos);
                mLogger.debug("设置信号位置: " + signalPos);
                break;
            case Global.RADAR_COMMAND_SET_SINGLE_MODE:
                mLogger.debug("设置为单目标模式");
                BaseDetect.setMultiMode(0);
                break;
            case Global.RADAR_COMMAND_SET_MULTI_MODE:
                mLogger.debug("设置为多目标模式");
                BaseDetect.setMultiMode(1);
                break;
            case Global.RADAR_COMMAND_SET_TIME_WINDOW:
                setTimeWindow(pack.getShort());
                break;
        }
    }

    private void doRun() {
        Packet pack;
        while (!mStop) {
            pack = mReceiveTransfer.get();
            if (pack == null)
                continue;
            switch (pack.getPacketType()) {
                case Global.PACKET_ACK:
                    mLogger.debug("receive ack packet");
                    break;
                case Global.PACKET_COMMAND:
                    mLogger.debug("receive command packet");
                    handleCommPacket(pack);
                    break;
                case Global.PACKET_DATA:
                    mLogger.debug("receive data packet");
                    break;
                case Global.PACKET_DETECT_RESULT:
                    mLogger.debug("receive detect result packet");
                    break;
                case Global.PACKET_NETWORK:
                    mLogger.debug("receive net packet");
                    break;
            }
        }
    }

    private Thread mThread;

    public void stopHandler() {
        mStop = true;
        mThread.interrupt();
        while (mThread.isAlive()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                AbstractLogger.Except(e, mLogger);
            }
        }
    }
}
