package com.ltdpro;

public class DetectResultParser {

    private short[] mResultArray;

    public void setOriginalResult(short[] result) {
        if (result == null)
            throw new NullPointerException();
        mResultArray = result;
    }

    public boolean isMultiMode() { return mResultArray[1] == 6; }

    public boolean isMoveResult() { return mResultArray[0] == (short) 0xDDAA; }

    public boolean isBreathResult() { return mResultArray[0] == (short) 0xDDBB; }

    private void setMoveResult(DetectResult detectResult, int index) {
        if (index > 1 && BaseDetect.getMultiMode() == 0)
            throw new IllegalArgumentException("not multi detect mode");
        if (detectResult.isBreath())
            throw new IllegalArgumentException("can not set move data on breath result");
        detectResult.setMoveType();
        if (index == 1) {
            if (mResultArray[2] == 1) {
                detectResult.setTargetPos(mResultArray[3]);
            }
        } else if (index == 2) {
            if (mResultArray[6] == 1) {
                detectResult.setTargetPos(mResultArray[7]);
            }
        }
    }

    private void setBreathResult(DetectResult detectResult, int index) {
        if (index != 1)
            throw new IllegalArgumentException("can only set first breath result");
        if (detectResult.isMove())
            throw new IllegalArgumentException("can not set breath data on move result");
        detectResult.setBreathType();
        if (mResultArray[4] == 1) {
            detectResult.setTargetPos(mResultArray[5]);
        }
    }

    public void setResult(DetectResult detectResult, int index) {
        if (isBreathResult()) {
            setBreathResult(detectResult, index);
        } else {
            setMoveResult(detectResult, index);
        }
    }

    public int moveResults() {
        int nResults = 0;
        if (mResultArray[2] == 1)
            ++nResults;
        if (mResultArray[1] == 6 && mResultArray[6] == 1)
            ++nResults;
        return nResults;
    }

    public int breathResults() {
        return mResultArray[4];
    }

}
