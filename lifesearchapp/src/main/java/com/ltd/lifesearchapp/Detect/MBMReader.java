package com.ltd.lifesearchapp.Detect;

public class MBMReader implements DetectResultReader {
    private short[] resultArray;
    public void read(short[] result) {
        if (result == null)
            throw new NullPointerException();
        resultArray = result;
    }

    public boolean isMultiMode() { return resultArray[1] == 6; }

    public boolean isMoveResult() { return resultArray[0] == (short) 0xDDAA; }

    public boolean isBreathResult() { return resultArray[0] == (short) 0xDDBB; }

    public DetectResult getResult(boolean isMove, int idx) {
        if (idx < 1 || idx > 2)
            throw new IllegalArgumentException("invalid result index");
        if (idx > 1 && DetectUtil.getMultiMode() == 0)
            throw new IllegalArgumentException("not multi detect mode");
        DetectResult dr = new DetectResult();
        if (idx == 1) {
            if (isMove && resultArray[2] == 1) {
                dr.setMoveType();
                dr.setTargetPos(resultArray[3]);
//                System.err.println("Ìå¶¯Ì½²â½á¹û£º"+ dr.getResultType()+" "+"¾àÀë:"+dr.getTargetPos());
            } else if (!isMove && resultArray[4] == 1) {
                dr.setBreathType();
                dr.setTargetPos(resultArray[5]);
                System.err.println("ºôÎüÌ½²â½á¹û£º"+ dr.getResultType()+" "+"¾àÀë:"+dr.getTargetPos());
            }
        } else {
            if (!isMove)
                throw new IllegalArgumentException("max breath result is 1");
            if (resultArray.length >= 8 && resultArray[6] == 1) {
                dr.setTargetPos(resultArray[7]);
            }
        }
        return dr;
    }

    public int moveResults() {
        int nResults = 0;
        if (resultArray[2] == 1)
            ++nResults;
        if (resultArray[1] == 6 && resultArray[6] == 1)
            ++nResults;
        return nResults;
    }

    public int breathResults() {
        return resultArray[4];
    }
}
