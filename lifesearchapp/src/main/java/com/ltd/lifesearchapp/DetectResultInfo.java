package com.ltd.lifesearchapp;

public class DetectResultInfo {

    private short mType;

    private int mScans;

    private short mTargetPos;

    private short mDetectStart;

    private short mDetectEnd;

    public DetectResultInfo(short type, int scans, short targetPos, short detectStart,
                            short detectEnd) {
        mTargetPos = targetPos;
        mType = type;
        mScans = scans;
        mDetectStart = detectStart;
        mDetectEnd = detectEnd;
    }

    public final short getType() { return mType; }

    public final int getScans() { return mScans; }

    public final short getTargetPos() { return mTargetPos; }

    public final short getDetectStart() { return mDetectStart; }

    public final short getDetectEnd() { return mDetectEnd; }

}
