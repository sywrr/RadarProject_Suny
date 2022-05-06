package com.ltd.lifesearchapp;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Connection.Packet;

public final class RadarDataQueue {

    private final static int[] scanLengthArray = new int[]{256, 512, 1024, 2048, 4096, 8192};

    private final Lock mDataLock = new ReentrantLock(true);

    private final AtomicBoolean mLocked = new AtomicBoolean(false);

    private final _RadarDataPool mPool;

    public RadarDataQueue(_RadarDataPool radarDataPool) { mPool = radarDataPool; }

    private final List<_RadarData> mRadarDataList = new LinkedList<>();

    private void checkParams(int length, int scanLength) {
        if (length % scanLength != 0)
            throw new IllegalArgumentException("length must be times of scanLength");
        for (int scanLengthVal : scanLengthArray) {
            if (scanLength == scanLengthVal)
                return;
        }
        throw new IllegalArgumentException("invalid scanLength: " + scanLength);
    }

    private boolean setLocked(boolean lock) {
        while (mLocked.get() != lock) {
            if (mLocked.compareAndSet(!lock, lock))
                return true;
        }
        return false;
    }

    private boolean uncheckedPush(byte[] data, int offset, int length, int scanLength) {
        mDataLock.lock();
        try {
            if (!mLocked.get()) {
                _RadarData radarData = mPool.allocRadarData(length);
                radarData.setScanLength(scanLength);
                radarData.setData(data, offset, length);
                mRadarDataList.add(radarData);
                return true;
            }
            return false;
        } finally {
            mDataLock.unlock();
        }
    }

    public boolean push(byte[] data, int offset, int length, int scanLength) {
        if (data == null || length < 0 || offset < 0 || offset + length > data.length)
            throw new IllegalArgumentException("data is null or invalid range");
        if (scanLength > length / 2)
            throw new IllegalArgumentException("no one scan data");
        checkParams(length, scanLength);
        return uncheckedPush(data, offset, length, scanLength);
    }

    public boolean push(Packet pack) {
        byte[] data = pack.data();
        int length = pack.getPacketLength();
        if (data == null || length < 0 || length > data.length)
            throw new IllegalArgumentException("data is null or invalid range");
        short scanLength = (short) (data[0] | (data[1] << 8));
        if (scanLength + 1 > length / 2)
            throw new IllegalArgumentException("illegal data range: " + 0 + ", " + length);
        checkParams(length - 2, scanLength);
        return uncheckedPush(data, 2, length - 2, scanLength);
    }

    public _RadarData pop() {
        mDataLock.lock();
        try {
            if (!mRadarDataList.isEmpty())
                return mRadarDataList.remove(0);
            return null;
        } finally {
            mDataLock.unlock();
        }
    }

    public void clearAndLock() {
        mDataLock.lock();
        try {
            if (setLocked(true)) {
                while (!mRadarDataList.isEmpty()) {
                    mPool.recycle(mRadarDataList.remove(0));
                }
            }
        } finally {
            mDataLock.unlock();
        }
    }

    public void unlock() {
        setLocked(false);
    }
}