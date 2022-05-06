package com.ltdpro;

import java.util.concurrent.atomic.AtomicInteger;

public class RadarData {
    private final short[] mData;

    private int mScanLength;

    private int mSignalPos;

    private final AtomicInteger mUseCount = new AtomicInteger(0);

    private int mSize;

    public RadarData() {
        mData = new short[8192];
        mSize = 0;
    }

    private void increaseUseCount(int delta) {
        int oldValue;
        do {
            oldValue = mUseCount.get();
            if (delta > 0 && delta > Integer.MAX_VALUE - oldValue)
                throw new IllegalStateException("use count overflow");
            if (delta < 0 && oldValue + delta < 0)
                throw new IllegalStateException("can not decrease use count");
        } while (!mUseCount.compareAndSet(oldValue, oldValue + delta));
    }

    public synchronized final void setInUse(int delta) {
        increaseUseCount(delta);
    }

    public synchronized final boolean isInUse() { return mUseCount.get() > 0; }

    public void set(short[] buf, int offset, int length) {
        if (mScanLength != length)
            throw new IllegalArgumentException("radar data can only contains one scan data");
        if (buf == null)
            throw new NullPointerException("buf is null");
        if (offset < 0 || length < 0 || offset + length > buf.length)
            throw new IllegalArgumentException("invalid buf range: " + offset + ", " + length);
        System.arraycopy(buf, offset, mData, 0, length);
        mSize = length;
    }

    public final short[] data() { return mData; }

    public final int size() { return mSize; }

    public final void setScanLength(int scanLength) { mScanLength = scanLength; }

    public final void clear() { mSize = 0; }

}
