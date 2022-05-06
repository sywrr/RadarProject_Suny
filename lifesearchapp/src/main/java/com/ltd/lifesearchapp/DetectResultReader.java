package com.ltd.lifesearchapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import Utils.AbstractLogger;
import Utils.Logcat;

public class DetectResultReader {

    private final class DetectResultBuffer {

        private int mLength = 0;

        private int mIndex = 0;

        private final byte[] mByteData = new byte[30];

        private final int[] mOneResult = new int[5];

        private boolean readByteFromFile(int writeOffset) throws IOException {
            return mReadIs.read(mByteData, writeOffset, 1) == 1;
        }

        // 将探测结果文件中的一次探测所有结果读取到buffer当中
        // 每一次探测都以;作为分隔符
        public final boolean write() {
            mIndex = mLength = 0;
            mOneResult[0] = mOneResult[1] = mOneResult[2] = -1;
            int offset = 0;
            boolean readFinished = true;
            boolean readLoop = true;
            while (offset < mByteData.length && readLoop) {
                readFinished = false;
                try {
                    if (!readByteFromFile(offset) ||
                        (mByteData[offset] == (byte) (';') && (readFinished = true)))
                        break;
                    ++offset;
                    readFinished = true;
                } catch (IOException e) {
                    mLogger.errorStackTrace(e);
                } finally {
                    readLoop = readFinished;
                }
            }
            if (readFinished) {
                int length = Math.min(offset + 1, mByteData.length);
                readFinished = mByteData[length - 1] == (byte) (';') && length > 9 &&
                               (length - 9) % 6 == 0;
                if (readFinished)
                    mLength = length;
            }
            return readFinished;
        }

        // 解析buffer当中的数据并一次解析一个探测结果
        public final boolean next() {
            if (mLength == 0)
                throw new IllegalStateException("detect result buffer is not init");
            if (mIndex == mLength - 1) {
                mOneResult[0] = mOneResult[1] = mOneResult[2] = -1;
                mLength = mIndex = 0;
                return false;
            }
            if (mIndex == 0) {
                mOneResult[0] = toInt(mByteData, 0);
                mOneResult[1] = toShort(mByteData, 4);
                mOneResult[2] = toShort(mByteData, 6);
                mIndex += 8;
            }
            mOneResult[3] = toShort(mByteData, mIndex);
            mOneResult[4] = toShort(mByteData, mIndex + 4);
            mIndex += 6;
            return true;
        }

        public final boolean hasNext() {
            return mLength > 0 && mIndex < mLength - 1;
        }

    }

    private final DetectResultBuffer mBuffer = new DetectResultBuffer();

    private long mReadIndex = 0;

    private long mFileLength = 0;

    private FileInputStream mReadIs = null;

    private final AbstractLogger mLogger = new Logcat("DetectResultReader", true);

    private int toInt(byte[] buf, int offset) {
        int val = 0;
        for (int i = 0; i < 4; i++) {
            val |= ((buf[i + offset] & 0xff) << (8 * i));
        }
        return val;
    }

    private short toShort(byte[] buf, int offset) {
        short val = 0;
        val |= (buf[offset] & 0xff);
        val |= ((buf[offset + 1] & 0xff) << 8);
        return val;
    }

    private void closeFile() {
        if (mReadIs != null) {
            try {
                mReadIs.close();
            } catch (IOException e) {
                mLogger.errorStackTrace(e);
            } finally {
                mReadIs = null;
                mReadIndex = mFileLength = 0;
            }
        }
    }

    // 设置探测结果文件路径
    public final void setPath(String path) {
        closeFile();
        try {
            File file = new File(path);
            mReadIs = new FileInputStream(file);
            mFileLength = file.length();
        } catch (IOException e) {
            mLogger.errorStackTrace(e);
        }
    }

    // 从探测结果文件中读取结果，如果读取到文件末尾，则返回false，否则返回true
    public final boolean read() {
        if ((mReadIndex > 0 && mReadIndex == mFileLength) ||
            ((mBuffer.mLength == 0 || !mBuffer.hasNext()) && !mBuffer.write())) {
            mLogger.debug("read finished, close file");
            closeFile();
            return false;
        }
        mReadIndex += mBuffer.mLength;
        return mBuffer.next();
    }

    public final int getScans() { return mBuffer.mOneResult[0]; }

    public final int getTargetPos() { return mBuffer.mOneResult[4]; }

    public final int getType() { return mBuffer.mOneResult[3]; }

    public final short getDetectStart() { return (short) mBuffer.mOneResult[1]; }

    public final short getDetectEnd() { return (short) mBuffer.mOneResult[2]; }

    public final boolean isReadFinished() { return mReadIndex == mFileLength; }
}
