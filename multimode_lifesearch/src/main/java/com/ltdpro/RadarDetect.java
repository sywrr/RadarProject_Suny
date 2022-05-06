package com.ltdpro;

import android.util.Log;

import com.Connection.Packet;
import com.Connection.SendTransfer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class RadarDetect extends BaseDetect {

    static {
        System.loadLibrary("Detect");
        setMultiMode(1);
    }

    private final AtomicBoolean mReceiveData = new AtomicBoolean(false);

    private static boolean setBoolean(AtomicBoolean bool, boolean boolValue) {
        boolean oldValue;
        while ((oldValue = bool.get()) != boolValue) {
            if (bool.compareAndSet(oldValue, boolValue))
                return true;
        }
        return false;
    }

    private interface DetectImpl {
        long preDetect(short[] ad_frame_data);

        long detect(short[] result_data);

        void init();
    }

    private static class BodyImpl implements DetectImpl {
        @Override
        public long preDetect(short[] ad_frame_data) {
            long st = System.nanoTime();
            BaseDetect.detect_body_pre(ad_frame_data);
            return (System.nanoTime() - st) / 1000000;
        }

        public long detect(short[] result_data) {
            long st = System.nanoTime();
            BaseDetect.detect_body(result_data);
            return (System.nanoTime() - st) / 1000000;
        }

        public void init() {
            BaseDetect.init_body();
        }
    }

    private static class BreathImpl implements DetectImpl {
        @Override
        public long preDetect(short[] ad_frame_data) {
            long st = System.nanoTime();
            BaseDetect.detect_breath_pre(ad_frame_data);
            return (System.nanoTime() - st) / 1000000;
        }

        public long detect(short[] result_data) {
            long st = System.nanoTime();
            BaseDetect.detect_breath(result_data, 1);
            return (System.nanoTime() - st) / 1000000;
        }

        public void init() {
            BaseDetect.init_breath();
        }
    }

    private SendTransfer mTransfer = null;

    public synchronized void setTransfer(SendTransfer transfer) {
        if (mTransfer != null)
            throw new IllegalStateException("transfer is already set");
        mTransfer = transfer;
    }

    private final class DetectResultFileManager {

        private int mCount;

        private String mOutputDirPath;

        private boolean filterFolder(String folderName) {
            return folderName.startsWith("detect_result_");
        }

        private int folderIndex(String folderName) {
            return Integer.parseInt(folderName.substring(folderName.lastIndexOf('_') + 1));
        }

        private void init() {
            String rootDir = mStoragePath + "/detect_results";
            Log.d("RadarDetect", "rootDir: " + rootDir);
            File rootFile = new File(rootDir);
            if (!rootFile.exists()) {
                if (!rootFile.mkdir()) {
                    mCount = -1;
                    Log.d("RadarDetect", "mkdir failed");
                    return;
                }
            }
            File[] subFiles = rootFile.listFiles();
            if (subFiles == null || subFiles.length == 0) {
                mCount = 0;
                return;
            }
            int maxIndex = 0;
            for (File subFile : subFiles) {
                if (subFile.isDirectory() && filterFolder(subFile.getName())) {
                    maxIndex = Math.max(maxIndex, folderIndex(subFile.getName()));
                }
            }
            mCount = maxIndex * 2;
        }

        private void ensureInitSuccess() {
            boolean firstInit = true;
            while (mCount == -1) {
                if (!firstInit) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ignored) { }
                }
                Log.d("RadarDetect", "init failed, reInit");
                init();
                firstInit = false;
            }
        }

        public synchronized String createDetectResultFolder() {
            ensureInitSuccess();
            if (mCount % 2 == 0) {
                String newFolderPath = mStoragePath + "/detect_results/detect_result_" +
                                       (mCount / 2 + 1);
                File newFolder = new File(newFolderPath);
                mOutputDirPath = newFolder.exists() || newFolder.mkdir() ? newFolderPath : null;
                if (mOutputDirPath == null) {
                    Log.d("RadarDetect", "mkdir failed in create folder");
                }
            }
            if (mOutputDirPath != null)
                ++mCount;
            return mOutputDirPath;
        }
    }

    private final DetectResultFileManager mFileManager;

    private String mStoragePath;

    private volatile int mScanSpeed;

    private volatile int mAntennaType;

    private volatile int mWindow;

    private volatile int mScanLength;

    private volatile int mSignalPos;

    private void initRadarParams() {
        mScanSpeed = mRadarDevice.getScanSpeed();
        mAntennaType = mRadarDevice.getAntenFrq();
        mWindow = mRadarDevice.getTimeWindow();
        mScanLength = mRadarDevice.getScanLength();
        mSignalPos = mRadarDevice.getSignalpos();
    }

    public void setData(short[] buf, int offset, int length, int scanLength) {
        if (mReceiveData.get()) {
            RadarData radarData;
            while (length >= scanLength) {
                radarData = mRadarDataPool.alloc();
                radarData.setScanLength(scanLength);
                radarData.set(buf, offset, scanLength);
                offset += scanLength;
                length -= scanLength;
                mBodyThread.putData(radarData);
                mBreathThread.putData(radarData);
            }
        }
    }

    private void checkDetectStarted() {
        if (mBodyThread.isDetectStarted() || mBreathThread.isDetectStarted()) {
            throw new IllegalStateException("radar detect is already started");
        }
    }

    public void setStoragePath(String path) {
        if (path == null)
            throw new IllegalArgumentException("path is null");
        if (mStoragePath == null || !mStoragePath.equals(path)) {
            checkDetectStarted();
            mStoragePath = path;
        }
    }

    public void setSend(boolean send) {
        checkDetectStarted();
        mBodyThread.setSendResult(send);
        mBreathThread.setSendResult(send);
    }

    public void setSave(boolean save) {
        checkDetectStarted();
        mBodyThread.setWriteResult(save);
        mBreathThread.setWriteResult(save);
    }

    private DetectResultManager mDetectResultManager = new DetectResultManager(
            new DetectResultManager.HandlerConfigure(2, 5, 3),
            new DetectResultManager.HandlerConfigure(1, 5, 3));

    public void resetManager() { mDetectResultManager.resetManager(); }

    private class BaseDetectThread extends Thread {

        private final short[] mDetectResult = new short[20];

        private PrintStream mWriteStream;

        private final DetectImpl mDetectImpl;

        private final AtomicBoolean mDetectLocked = new AtomicBoolean(true);

        private final AtomicBoolean mStopThread = new AtomicBoolean(false);

        private final AtomicBoolean mStart = new AtomicBoolean(false);

        private final AtomicBoolean mStoppingDetect = new AtomicBoolean(false);

        private final AtomicBoolean mSendResult = new AtomicBoolean(false);

        private final AtomicBoolean mWriteResult = new AtomicBoolean(false);

        private final ConcurrentLinkedQueue<RadarData> mQueue = new ConcurrentLinkedQueue<>();

        private final Lock mDetectLock = new ReentrantLock(true);

        private final Blocker mStopBlocker = new Blocker(mDetectLock);

        private final Blocker mStartBlocker = new Blocker(mDetectLock);

        private final AtomicLong mOnceDetectTime = new AtomicLong(0L);

        private final String TAG;

        private int mScans;

        private final AtomicInteger mDetectStart = new AtomicInteger(-1);

        private final AtomicInteger mDetectEnd = new AtomicInteger(-1);

        protected final void debug(String msg) { Log.d(TAG, msg); }

        protected final void error(String msg) { Log.e(TAG, msg); }

        protected final void error(Throwable e) { Log.e(TAG, "except: " + e.getMessage()); }

        protected final void errorInDetail(Throwable e) { Log.e(TAG, Log.getStackTraceString(e)); }

        protected BaseDetectThread(String tag, DetectImpl detectImpl) {
            TAG = tag;
            mDetectImpl = detectImpl;
        }

        public final void setWriteResult(boolean writeResult) {
            setBoolean(mWriteResult, writeResult);
        }

        public final void setSendResult(boolean sendResult) {
            setBoolean(mSendResult, sendResult);
        }

        public final boolean isSaving() { return mWriteResult.get(); }

        public final boolean isDetectStarted() { return mStart.get(); }

        private void init() { mDetectImpl.init(); }

        private long detect(RadarData radarData) {
            return mDetectImpl.preDetect(radarData.data()) + mDetectImpl.detect(mDetectResult);
        }

        public final void putData(RadarData radarData) {
            if (!mDetectLocked.get()) {
                radarData.setInUse(1);
                mQueue.offer(radarData);
            }
        }

        private final class Writer {

            private final class IntegerStack {

                private final char[] mCharList = new char[100];

                private int mSize = 0;

                public void build(int val) {
                    boolean negative = val < 0;
                    if (negative)
                        val = -val;
                    char c;
                    do {
                        c = (char) ('0' + val % 10);
                        mCharList[mSize++] = c;
                        val /= 10;
                    } while (val > 0);
                    if (negative)
                        mCharList[mSize++] = '-';
                }

                public char pop() {
                    if (mSize == 0)
                        throw new RuntimeException("stack is empty");
                    return mCharList[--mSize];
                }

                public boolean isEmpty() { return mSize == 0; }

            }

            private final IntegerStack mStack = new IntegerStack();

            private final StringBuilder mWriteBuffer = new StringBuilder(1024);

            public Writer writeChar(char c) {
                if (mWriteStream != null) {
                    if (mWriteBuffer.capacity() == mWriteBuffer.length()) {
                        mWriteStream.print(mWriteBuffer.toString());
                        mWriteBuffer.delete(0, mWriteBuffer.length());
                    }
                    mWriteBuffer.append(c);
                }
                return this;
            }

            public Writer writeInt(int val) {
                if (mWriteStream != null) {
                    mStack.build(val);
                    while (!mStack.isEmpty())
                        writeChar(mStack.pop());
                }
                return this;
            }

            public void flush() {
                if (mWriteStream != null) {
                    if (mWriteBuffer.length() > 0) {
                        mWriteStream.print(mWriteBuffer.toString());
                        mWriteBuffer.delete(0, mWriteBuffer.length());
                    }
                }
            }
        }

        private final Writer mWriter = new Writer();

        private void writeToFile() {
            if (mWriteStream != null) {
                debug("write " + mScans + "th scans");
                mWriter.writeInt(mScans).writeChar(' ');
                int len = BaseDetect.getMultiMode() == 1 ? 8 : 6;
                for (int i = 0; i < len; i++) {
                    if (i == 0)
                        mWriter.writeInt(mDetectResult[i]);
                    else
                        mWriter.writeChar(' ').writeInt(mDetectResult[i]);
                }
                mWriter.writeChar('\n');
            } else {
                error("stream is null");
            }
        }

        protected void handleDetectResultPacket(Packet pack) { }

        private void pushResult(short[] result) {
            Packet pack = mDetectResultManager.process(result, mScans, mDetectStart.get(),
                                                       mDetectEnd.get());
            if (pack != null) {
                debug("got detect result");
                mTransfer.put(pack);
                handleDetectResultPacket(pack);
            }
        }

        private void handleResult() {
            if (mSendResult.get())
                pushResult(mDetectResult);
            if (mWriteResult.get())
                writeToFile();
        }

        private void finishWrite() {
            if (mWriteStream != null) {
                mWriter.flush();
                mWriteStream.close();
                mWriteStream = null;
            }
        }

        protected void onOnceDetectLoopFinished() {}

        private void doDetectLoop() {
            RadarData radarData;
            long st;
            boolean loop = true;
            while (!mStopThread.get() && loop) {
                st = System.nanoTime();
                radarData = mQueue.poll();
                if (radarData != null) {
//                    debug("alloc blocks: " + getAllocBlocks() + ", " +
//                          mRadarDataPool.mFreeQueue.size());
                    ++mScans;
                    long time = detect(radarData);
                    radarData.setInUse(-1);
                    if (!radarData.isInUse())
                        mRadarDataPool.dealloc(radarData);
//                    debug("detect " + mScans + "th data cost " + time + " ms");
                    handleResult();
                } else if (mStoppingDetect.get()) {
                    loop = false;
                    doStopDetect();
                    mStopBlocker.unblockAll();
                } else {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ignore) { }
                }
                mOnceDetectTime.set(mOnceDetectTime.get() + (System.nanoTime() - st) / 1000000L);
                onOnceDetectLoopFinished();
            }
        }

        private void doStopDetect() {
            finishWrite();
            mSendResult.set(false);
            mWriteResult.set(false);
            mStoppingDetect.set(false);
            mDetectLocked.set(true);
            mStart.set(false);
        }

        protected final void resetDetectRange() {
            mDetectStart.set(-1);
            mDetectEnd.set(-1);
        }

        private void checkStop() {
            if (mStopThread.get())
                throw new IllegalStateException("radar detect thread is shutdown");
        }

        @Override
        public final void run() {
            while (!mStopThread.get()) {
                mStartBlocker.block();
                doDetectLoop();
            }
            debug("radar detect thread exit");
        }

        protected final void stopDetect() {
            checkStop();
            if (mStart.get()) {
                mStoppingDetect.set(true);
                mStopBlocker.block();
            }
        }

        protected final void initDetectRange() {
            int detectStart;
            if ((detectStart = mDetectStart.get()) != -1 && !mAutoDetect)
                return;
            int detectInterval = mOneMaxDetectInterval * mWindow / mMaxTimeWindow;
            if (detectStart == -1) {
                mDetectStart.set(mDetectRangeStart);
            } else if (mAutoDetect) {
                mDetectStart.set(detectInterval + detectStart);
            }
            mDetectEnd.set(mDetectStart.get() + detectInterval);
        }

        protected final int getDetectStart() { return mDetectStart.get(); }

        protected final int getDetectEnd() { return mDetectEnd.get(); }

        protected void startDetect() {
            checkStop();
            if (mStart.get())
                throw new IllegalStateException("detect is not stopped");
            initRadarParams();
            initDetectRange();
            BaseDetect.changeParams(mScanSpeed, mAntennaType, mWindow, mScanLength);
            debug("扫速: " + mScanSpeed);
            debug("天线主频: " + mAntennaType);
            debug("时窗: " + mWindow);
            debug("采样点数: " + mScanLength);
            debug("信号位置: " + mSignalPos);
            init();
            mScans = 0;
            mDetectLocked.set(false);
            mOnceDetectTime.set(0);
            mStart.set(true);
            mStartBlocker.unblock();
        }

        protected final void shutdown() {
            stopDetect();
            mStopThread.set(true);
            mStartBlocker.unblock();
            while (isAlive()) {
                try {
                    join();
                } catch (InterruptedException ignore) {}
            }
        }

        protected final void createStream(String fileName) {
            if (mWriteStream == null) {
                try {
                    String folderPath = mFileManager.createDetectResultFolder();
                    if (folderPath == null)
                        throw new IllegalStateException("folder path is null");
                    String filePath = folderPath + "/" + fileName;
                    debug("file path: " + filePath);
                    mWriteStream = new PrintStream(new FileOutputStream(filePath));
                } catch (IOException e) {
                    errorInDetail(e);
                }
            }
        }

        protected final long getOnceDetectTime() { return mOnceDetectTime.get(); }
    }

    private final class BodyThread extends BaseDetectThread {

        public BodyThread() {
            super("BodyDetect", new BodyImpl());
        }

        @Override
        public void startDetect() {
            if (isSaving())
                createStream("body.txt");
            super.startDetect();
        }
    }

    private final class BreathThread extends BaseDetectThread {

        private long mRestartInterval;

        private final long mNormalRestartInterval;

        public BreathThread(long restartInterval) {
            super("BreathDetect", new BreathImpl());
            mRestartInterval = restartInterval;
            mNormalRestartInterval = restartInterval;
        }

        @Override
        public void startDetect() {
            if (isSaving())
                createStream("breath.txt");
            super.startDetect();
        }

        private boolean startNextDetect() {
            if (isDetectStarted())
                throw new IllegalStateException("breath detect is not stopped");
            if (mWindow != 20)
                throw new IllegalStateException("只有时窗为20ns时才可以进行连续呼吸探测");
            mRestartInterval = mNormalRestartInterval;
            debug("reset interval to 70");
            int signalPos = mSignalPos + mWindow;
            int maxSignalPos = mMaxDetectDistance * mMaxTimeWindow / mOneMaxDetectInterval;
            if (signalPos >= maxSignalPos)
                return false;
            mRadarDevice.setSignalpos(signalPos);
            setSend(true);
            startDetect();
            debug("信号位置：" + signalPos + ", " + mSignalPos);
            debug("时窗：" + mRadarDevice.getTimeWindow());
            setBoolean(mReceiveData, true);
            Packet pack = new Packet(Global.DETECT_RANGE_REPORT, 4);
            pack.putShort((short) getDetectStart());
            pack.putShort((short) getDetectEnd());
            pack.setPacketFlag(0xAAAABBBB);
            mTransfer.put(pack);
            return true;
        }

        private Thread mRestartThread = null;

        private final AtomicBoolean mRestarting = new AtomicBoolean(false);

        private void restartNextDetect() {
            if (!setBoolean(mRestarting, true))
                return;
            if (mRestartThread != null) {
                while (mRestartThread.isAlive()) {
                    try {
                        mRestartThread.join();
                    } catch (InterruptedException ignore) {}
                }
            }
            mRestartThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    debug("restart next detect");
                    if (setBoolean(mReceiveData, false)) {
                        stopDetect();
                        resetManager();
                        mTransfer.clearTransfer();
                    }
                    debug("this breath detect is stopped");
                    if (!startNextDetect()) {
                        debug("detect finished");
                        Packet pack = new Packet(Global.DETECT_FINISHED, 0);
                        pack.setPacketFlag(0xAAAABBBB);
                        mTransfer.put(pack);
                    } else {
                        debug("start next detect");
                    }
                    setBoolean(mRestarting, false);
                }
            });
            mRestartThread.start();
        }

        @Override
        protected void onOnceDetectLoopFinished() {
            super.onOnceDetectLoopFinished();
            long onceDetectTime = getOnceDetectTime();
            if (onceDetectTime >= mRestartInterval && mWindow == 20) {
                restartNextDetect();
            }
        }

        @Override
        protected void handleDetectResultPacket(Packet pack) {
            super.handleDetectResultPacket(pack);
            if (pack.getPacketLength() >= 10) {
                short detectResultType = pack.getShort(8);
                if ((detectResultType & DetectResult.RESULT_FINAL) != 0 &&
                    (detectResultType & DetectResult.RESULT_BREATH) != 0) {
                    debug("got a final breath packet");
                    if (mWindow == 20)
                        restartNextDetect();
                } else if ((detectResultType & DetectResult.RESULT_BREATH) != 0 &&
                           (detectResultType & DetectResult.RESULT_INTER) != 0) {
                    debug("got a inter breath packet");
                    if (mWindow == 20 && mRestartInterval == 70000L) {
                        mRestartInterval *= 2;
                        debug("set interval to 140");
                    }
                }
            }
        }
    }

    private final static class RadarDataPool {

        private final ConcurrentLinkedQueue<RadarData> mFreeQueue = new ConcurrentLinkedQueue<>();

        private final AtomicInteger mAllocBlocks = new AtomicInteger(0);

        private void increaseAllocBlocks(int delta) {
            int oldValue;
            do {
                oldValue = mAllocBlocks.get();
                if (delta > 0 && oldValue > Integer.MAX_VALUE - delta)
                    throw new OutOfMemoryError("can not alloc any blocks");
            } while (!mAllocBlocks.compareAndSet(oldValue, oldValue + delta));
        }

        public final RadarData alloc() {
            RadarData radarData = mFreeQueue.poll();
            if (radarData == null) {
                increaseAllocBlocks(1);
                return new RadarData();
            }
            return radarData;
        }

        public final void dealloc(RadarData radarData) {
            radarData.clear();
            mFreeQueue.offer(radarData);
        }

        public final void clear() {
            while (!mFreeQueue.isEmpty()) {
                if (mFreeQueue.poll() != null)
                    increaseAllocBlocks(-1);
            }
        }

    }

    private volatile int mDetectRangeStart;

    private volatile int mDetectRangeEnd;

    private volatile boolean mAutoDetect;

    private final int mOneMaxDetectInterval;

    private final int mMaxDetectDistance;

    private final int mMaxTimeWindow;

    private final radarDevice mRadarDevice;

    public RadarDetect(radarDevice radar, int startSignalPos, int maxTimeWindow,
                       int oneMaxDetectInterval, int maxDetectDistance) {
        mRadarDevice = radar;
        mFileManager = new DetectResultFileManager();
        mMaxTimeWindow = maxTimeWindow;
        mOneMaxDetectInterval = oneMaxDetectInterval;
        mRadarDevice.setSignalpos(startSignalPos);
        mMaxDetectDistance = maxDetectDistance;
        startDetectThreads();
    }

    private boolean mStarted = false;

    private void checkRadarDetectStarted() {
        if (!mStarted)
            throw new IllegalStateException("radar detect is not started");
    }

    public synchronized void setDetectRange(int detectRangeStart, int detectRangeEnd,
                                            boolean autoDetect) {
        if (mBodyThread.isDetectStarted() || mBreathThread.isDetectStarted())
            throw new IllegalStateException("can not set detect range when detect is started");
        mDetectRangeStart = detectRangeStart;
        mDetectRangeEnd = detectRangeEnd;
        mAutoDetect = autoDetect;
    }

    public synchronized void startRadarDetect() {
        stopRadarDetect();
        mBreathThread.startDetect();
        if (mWindow == 80) {
            mBodyThread.startDetect();
        }
        setBoolean(mReceiveData, true);
    }

    public synchronized void stopRadarDetect() {
        checkRadarDetectStarted();
        setBoolean(mReceiveData, false);
        if (mBodyThread.isDetectStarted()) {
            mBodyThread.stopDetect();
        }
        mBodyThread.resetDetectRange();
        if (mBreathThread.isDetectStarted()) {
            mBreathThread.stopDetect();
        }
        mBreathThread.resetDetectRange();
    }

    private synchronized void startDetectThreads() {
        if (mStarted)
            throw new IllegalStateException("detect threads is already started");
        mBodyThread = new BodyThread();
        mBreathThread = new BreathThread(70000L);
        mBodyThread.start();
        mBreathThread.start();
        mStarted = true;
    }

    private void stopAndWait(BaseDetectThread baseDetectThread) {
        if (baseDetectThread != null) {
            baseDetectThread.shutdown();
        }
    }

    public synchronized void close() {
        if (mBodyThread != null && mBreathThread != null) {
            stopAndWait(mBodyThread);
            stopAndWait(mBreathThread);
            mRadarDataPool.clear();
            mBodyThread = null;
            mBreathThread = null;
        }
    }

    public final int getAllocBlocks() { return mRadarDataPool.mAllocBlocks.get(); }

    private final RadarDataPool mRadarDataPool = new RadarDataPool();

    private BodyThread mBodyThread = null;

    private BreathThread mBreathThread = null;

}
