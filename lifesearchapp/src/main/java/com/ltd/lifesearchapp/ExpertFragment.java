package com.ltd.lifesearchapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;
//import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import Connection.Packet;
import Connection.Receiver;
import Utils.AbstractLogger;
import Utils.Logcat;
import Utils.FileLogger;
import Utils.Utils;

public class ExpertFragment extends Fragment implements Releasable {

    View mView;

    private Context mContext;

    private RadarDataView mRadarDataView = null;

    private RadarRulerView mRadarRulerView = null;

    private AbstractLogger mLogger = new Logcat("ExpertFragment", true);

    private final static AbstractLogger logger = new FileLogger("ExpertFragment", true);

    static {
        ((FileLogger) logger).open(Environment.getExternalStorageDirectory() + "/log.txt");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    // 上传器，负责在特定时刻项雷达主机发送上传数据的命令
    private final class Uploader {
        private volatile boolean mUploaded = false;

        public final void start() {
            if (!mUploaded) {
                Packet pack = new Packet(Global.PACKET_COMMAND, 2);
                pack.setPacketFlag(0xAAAABBBB);
                pack.putShort(Global.RADAR_COMMAND_UPLOAD);
                ((MainActivity) mContext).getNetwork().putCommandPacket(pack);
                mUploaded = true;
            }
        }

        public final void stop() {
            if (mUploaded) {
                Packet pack = new Packet(Global.PACKET_COMMAND, 2);
                pack.setPacketFlag(0xAAAABBBB);
                pack.putShort(Global.RADAR_COMMAND_STOP_UPLOAD);
                ((MainActivity) mContext).getNetwork().putCommandPacket(pack);
                mUploaded = false;
            }
        }
    }

    private boolean setBool(AtomicBoolean bool, boolean boolValue) {
        while (bool.get() != boolValue) {
            if (bool.compareAndSet(!boolValue, boolValue))
                return true;
        }
        return false;
    }

    private final Uploader mUploader = new Uploader();

    public void startUpload() {
        mUploader.start();
    }

    public void stopUpload() {
        mUploader.stop();
    }

    public void stopSurfaceView() {
        long st = System.nanoTime();
        Thread stopRulerViewThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mRadarRulerView.stopDraw();
            }
        });

        Thread stopDataViewThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mRadarDataView.stopDraw();
            }
        });

        stopRulerViewThread.start();
        stopDataViewThread.start();

        Utils.joinThreadUninterruptibly(stopRulerViewThread);
        Utils.joinThreadUninterruptibly(stopDataViewThread);

        mLogger.debug("stop surface view cost time: " + (System.nanoTime() - st) / 1000000);
    }

    public void startSurfaceView() {
        mRadarRulerView.startDraw();
        mRadarDataView.startDraw();
    }

    private void lockSurfaceView() {
        mRadarRulerView.lockView();
        mRadarDataView.lockView();
    }

    private void unlockSurfaceView() {
        mRadarRulerView.unlockView();
        mRadarDataView.unlockView();
    }

    private void clearSurfaceView() {
        mRadarRulerView.clearView();
        mRadarDataView.clearView();
    }

    private volatile boolean mIsHidden = true;

    private final _RadarDataPool mRadarDataPool = new _RadarDataPool();

    private final RadarDataQueue mQueue = new RadarDataQueue(mRadarDataPool);

    protected final class PlayManager {

        // 开始显示实时数据
        public final static int BEG_REALTIME = 0xaa;

        // 开始回放雷达数据文件
        public final static int BEG_PLAY_BACK = 0xbb;

        // 结束播放，此时不显示任何图像
        public final static int END_PLAY = 0xcc;

        // 信号队列，如果队列为空，会一直阻塞知道队列中有信号
        private final _LinkedBlockingQueue<Integer> mSignalQueue = new _LinkedBlockingQueue<>();

        private final Queue<String> mPlaybackPathQueue = new ConcurrentLinkedQueue<>();

        // 当前处理的信号
        private volatile int mCurrentSignal = END_PLAY;

        // 提交的最新信号
        private volatile int mLatestSignal = END_PLAY;

        private volatile int mShowMode = 0;

        private static final int showRealtimeData = 0;

        private static final int showPlayback = 1;

        private static final int hidden = 2;

        private void setMode(int mode) { mShowMode = mode; }

        private final AbstractLogger mLogger = new Logcat("PlayManager", true);

        private Integer getSignal() {
            Integer signal = mSignalQueue.poll();
            if (signal != null)
                mCurrentSignal = signal;
            return signal;
        }

        private void post(int signal) {
            if (signal != BEG_REALTIME && signal != BEG_PLAY_BACK && signal != END_PLAY)
                throw new IllegalArgumentException("invalid posted value");
            if (mLatestSignal != signal || mLatestSignal == BEG_PLAY_BACK) {
                mSignalQueue.add(signal);
                mLatestSignal = signal;
            }
        }

        private boolean isPlayingBack() {
            int queueSize = mSignalQueue.size();
            if (queueSize == 1) {
                Integer signal = mSignalQueue.peek();
                if (signal == null)
                    throw new IllegalStateException("peek() return null but size() is 1");
                return signal == BEG_PLAY_BACK;
            }
            if (queueSize == 0)
                return mCurrentSignal == BEG_PLAY_BACK;
            return mSignalQueue.remove() == BEG_PLAY_BACK;
        }

        public final void postStopPlay() {
            post(END_PLAY);
        }

        public final void postPlayback(String playbackFilePath) {
            if (setBool(mProcessingPlayback, true)) {
                mPlaybackPathQueue.offer(playbackFilePath);
                post(BEG_PLAY_BACK);
            } else {
                Toast.makeText(mContext, "正在处理回放请求", Toast.LENGTH_SHORT).show();
            }
        }

        public final void postPlayRealtimeData() {
            if (mCurrentSignal != BEG_REALTIME && setBool(mProcessingRealtime, true)) {
                post(BEG_REALTIME);
            } else {
                Toast.makeText(mContext, "操作频繁，已被拒绝", Toast.LENGTH_SHORT).show();
            }
        }

        private int readFromFileStream(byte[] buf, int offset, int length, FileInputStream fis)
                throws IOException {
            int totalRead = 0;
            int readLen;
            while (totalRead < length) {
                readLen = fis.read(buf, totalRead + offset, length - totalRead);
                if (readLen == -1)
                    return -1;
                totalRead += readLen;
            }
            return totalRead;
        }

        private void readFileHead(FileInputStream fis) throws IOException {
            readFromFileStream(mFileHead, 0, 1024, fis);
            mFileHeader.read(mFileHead);
        }

        // 将指定文件中的雷达数据添加到队列中
        private void pushRadarDataFromFile(FileInputStream fis, long fileLength)
                throws IOException {
            int readLen, totalRead = 0;
            readFileHead(fis);
            int scanLength = mFileHeader.rh_nsamp;
            mLogger.debug("read file head, scanLength is " + scanLength);
            long checkFileLengthValue = (fileLength - 1024) % (scanLength * 2);
            mLogger.debug("check value: " + checkFileLengthValue);
            if (checkFileLengthValue != 0)
                throw new IllegalArgumentException("file length is not times of scanLength");
            while (isPlayingBack() && !mPlayStop.get() && totalRead < fileLength - 1024 &&
                   isDataSurfaceViewShowing()) {
                readLen = Math.min(scanLength * 2, (int) (fileLength - totalRead));
                readLen = readFromFileStream(mOneRadarData, 0, readLen, fis);
                if (readLen < 0)
                    throw new IOException("error occurred when read radar data from file");
                totalRead += readLen;
                if (!mQueue.push(mOneRadarData, 0, scanLength * 2, scanLength))
                    continue;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignore) { }
            }
        }

        private void doPlaybackDataCollection() {
            FileInputStream fis = null;
            String playbackFilePath = mPlaybackPathQueue.poll();
            if (playbackFilePath == null)
                throw new IllegalStateException("play back file path is not selected");
            try {
                File playbackFile = new File(playbackFilePath);
                fis = new FileInputStream(playbackFile);
                pushRadarDataFromFile(fis, playbackFile.length());
            } catch (IOException e) {
                mLogger.errorStackTrace(e);
            } finally {
                mLogger.debug("playback radar file finished");
                if (Thread.interrupted())
                    mLogger.debug("clear interrupt flag");
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        mLogger.errorStackTrace(e);
                    }
                }
            }
        }

        private void refreshPlay() {
            lockSurfaceView();
            mQueue.clearAndLock();
            clearSurfaceView();
            unlockSurfaceView();
            mQueue.unlock();
        }

        private void handleSignal(int signal) {
            switch (signal) {
                case BEG_PLAY_BACK:
                    mLogger.debug("begin playback mode");
                    setMode(showPlayback);
                    stopUpload();
                    refreshPlay();
                    doPlaybackDataCollection();
                    break;
                case BEG_REALTIME:
                    mLogger.debug("begin realtime mode");
                    setMode(showRealtimeData);
                    refreshPlay();
                    startUpload();
                    break;
                case END_PLAY:
                    mLogger.debug("no play mode");
                    setMode(hidden);
                    stopUpload();
                    refreshPlay();
                    break;
            }
        }

        private void doShowModeSwitch() {
            Integer signal;
            while (!mPlayStop.get()) {
                signal = getSignal();
                if (signal == null) {
                    mLogger.error("interrupt signal queue");
                    return;
                }
                while (mRadarDataView.destroyed() && !mPlayStop.get()) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ignore) { }
                }
                if (!mPlayStop.get()) {
                    setBool(mProcessingRealtime, false);
                    setBool(mProcessingPlayback, false);
                    handleSignal(signal);
                }
            }
        }

        private Thread mPlayThread;

        public final boolean isShowingRealtimeData() { return mShowMode == showRealtimeData; }

        public final boolean isShowingPlayback() { return mShowMode == showPlayback; }

        private final AtomicBoolean mProcessingRealtime = new AtomicBoolean(false);

        private final AtomicBoolean mProcessingPlayback = new AtomicBoolean(false);

        private final AtomicBoolean mPlayStop = new AtomicBoolean(false);

        private void initPlay() {
            mPlayThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    doShowModeSwitch();
                }
            });
            mPlayThread.start();
        }

        private void stopPlay() {
            mPlayStop.set(true);
            mPlayThread.interrupt();
            while (mPlayThread.isAlive()) {
                try {
                    mPlayThread.join();
                } catch (InterruptedException ignore) {}
            }
        }

    }

    public final PlayManager mPlayManager = new PlayManager();

    private boolean isDataSurfaceViewShowing() {
        return !mIsHidden && !mRadarDataView.locked() && !mRadarDataView.destroyed();
    }

    private final FileHeader mFileHeader = new FileHeader();

    private final byte[] mFileHead = new byte[1024];

    private final byte[] mOneRadarData = new byte[16384];

    public final boolean isInHiddenStatus() { return mIsHidden; }

    @Override
    public void onHiddenChanged(boolean hidden) {
        mIsHidden = hidden;
        super.onHiddenChanged(hidden);
        mLogger.debug("hidden: " + hidden);
        if (hidden) {
            mPlayManager.post(PlayManager.END_PLAY);
        } else {
            mPlayManager.post(PlayManager.BEG_REALTIME);
        }
    }

    @Override
    public void release() {
        ((FileLogger) logger).close();
        stopSurfaceView();
        if (mWriteThread != null)
            mWriteThread.stopWrite();
        mPlayManager.stopPlay();
    }

    public class RadarDataCallback extends MyCallback {

        public RadarDataCallback(AbstractLogger logger, String packetName) {
            super(logger, packetName);
        }

        @Override
        public void invoke(Packet pack) {
            super.invoke(pack);
            mLogger.debug("receive radar data packet: " + pack.getPacketLength());
//            logger.debug("receive radar data packet: " + pack.getPacketLength());
            if (isDataSurfaceViewShowing() && mPlayManager.isShowingRealtimeData()) {
                mQueue.push(pack);
                mWriteThread.postWrite(pack);
            }
        }
    }

    public class FileHeadCallback extends MyCallback {

        public FileHeadCallback(AbstractLogger logger, String packetName) {
            super(logger, packetName);
        }

        @Override
        public void invoke(Packet pack) {
            super.invoke(pack);
            if (isDataSurfaceViewShowing() && mPlayManager.isShowingRealtimeData())
                mWriteThread.postWrite(pack);
        }
    }

    public class UploadEndCallback extends MyCallback {

        public UploadEndCallback(AbstractLogger logger, String packetName) {
            super(logger, packetName);
        }

        @Override
        public void invoke(Packet pack) {
            super.invoke(pack);
            mLogger.debug("receive upload end packet");
            mWriteThread.postWrite(pack);
            mLogger.debug("free: " + mRadarDataPool.getFreeSize());
        }
    }

    private void registerCallbacks() {
        Receiver receiver = ((MainActivity) mContext).getNetwork().mReceiverGroup.getReceiver(
                "radar_data");
        if (receiver != null) {
            RadarDataCallback radarDataCallback = new RadarDataCallback(mLogger, "radar_data");
            receiver.addCallback(Global.PACKET_DATA, radarDataCallback);
            FileHeadCallback fileHeadCallback = new FileHeadCallback(mLogger, "radar_file_head");
            receiver.addCallback(Global.PACKET_FILE_HEAD, fileHeadCallback);
            UploadEndCallback uploadEndCallback = new UploadEndCallback(mLogger, "upload_finish");
            receiver.addCallback(Global.PACKET_ACK, uploadEndCallback);
            mWriteThread = new RadarFileWriteThread(mLteFiles);
            mWriteThread.start();
        }
    }

    private final String mLteFiles = Environment.getExternalStorageDirectory().getPath() +
                                     "/LteFiles";

    private RadarFileWriteThread mWriteThread = null;

    private void onPlaybackClick() {
        ((MainActivity) mContext).setRequestCode(200);
        stopUpload();
        Intent intent = new Intent(mContext, SelectFileActivity.class);
        intent.putExtra("path", mLteFiles);
        intent.putExtra("category", "RadarData");
        intent.putExtra("requestCode", 200);
        startActivityForResult(intent, 200);
    }

    private void onRealtimeDataClick() {
        mPlayManager.postPlayRealtimeData();
    }

    private void initView() {
        mRadarDataView = mView.findViewById(R.id.radar_data_view);
        mRadarRulerView = mView.findViewById(R.id.radar_ruler_view);
        mRadarDataView.setRulerView(mRadarRulerView);
        mView.findViewById(R.id.playback_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLogger.debug("点击回放文件按钮");
                onPlaybackClick();
            }
        });

        mView.findViewById(R.id.realtime_data).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLogger.debug("点击实时数据按钮");
                onRealtimeDataClick();
            }
        });

        mRadarDataView.setRadarDataQueue(mQueue);
        mRadarDataView.setRadarDataPool(mRadarDataPool);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.expert_view, container, false);
        initView();
        registerCallbacks();
        mPlayManager.initPlay();
        return mView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mLogger.debug("expert view destroyed");
        mRadarDataView.getHolder().removeCallback(mRadarDataView);
        mRadarRulerView.getHolder().removeCallback(mRadarRulerView);
    }
}
