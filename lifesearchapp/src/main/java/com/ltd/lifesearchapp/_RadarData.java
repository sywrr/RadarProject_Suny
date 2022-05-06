package com.ltd.lifesearchapp;

public final class _RadarData {

    private native static void _memory_copy(byte[] byteData, int srcIndex, short[] shortData,
                                            int destIndex, int length);

    private final short[] mData;

    private int mLength, mSize;
    private int mScanLength;

    public _RadarData(int length) {
        mLength = length;
        mSize = 0;
        mData = new short[mLength];
    }

    public final void setData(byte[] data, int offset, int length) {
        length /= 2;
        if (length > mLength)
            throw new IllegalArgumentException("data overflow: " + length);
        _memory_copy(data, offset, mData, 0, length);
        mSize = length;
    }

    public final int length() { return mLength; }

    public final int size() { return mSize; }

    public final short[] data() { return mData; }

    public final int scanLength() { return mScanLength; }

    public final void clear() { mSize = 0; }

    public final void setScanLength(int scanLength) { mScanLength = scanLength; }
}
