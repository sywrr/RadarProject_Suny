package com.ltd.lifesearchapp.Detect;

public class DetectResult {
    public final static short RESULT_INTER = 0x01;

    public final static short RESULT_BREATH = 0x10;

    public final static short RESULT_FINAL = 0x02;

    public final static short RESULT_MOVE = 0x20;

    private final static short[] types = new short[]{RESULT_FINAL | RESULT_MOVE,
                                                     RESULT_FINAL | RESULT_BREATH,
                                                     RESULT_INTER | RESULT_MOVE,
                                                     RESULT_INTER | RESULT_BREATH};

    private short resultType = 0;

    private short existTarget = 0;

    private short targetPos = -1;

    public DetectResult() {}

    public DetectResult(short type) {
        checkResultType(type);
        resultType = type;
    }

    public DetectResult(short type, short targetPos) {
        checkResultType(type);
        resultType = type;
        setTargetPos(targetPos);
    }

    private void checkResultType(int resultType) {
        for (short type : types) {
            if (resultType == type)
                return;
        }
        throw new IllegalArgumentException("invalid result type");
    }

    public boolean isBreath() { return (resultType & RESULT_BREATH) == RESULT_BREATH; }

    public boolean isMove() { return (resultType & RESULT_MOVE) == RESULT_MOVE; }

    public boolean isInterResult() { return (resultType & RESULT_INTER) == RESULT_INTER; }

    public boolean isFinalResult() { return (resultType & RESULT_FINAL) == RESULT_FINAL; }

    public void setFinalType() {
        if (isInterResult())
            throw new IllegalStateException("result is already set to inter type");
        resultType |= RESULT_FINAL;
    }

    public void setInterType() {
        if (isFinalResult())
            throw new IllegalStateException("result is already set to final type");
        resultType |= RESULT_INTER;
    }

    public void setMoveType() {
        if (isBreath())
            throw new IllegalStateException("result is already set to breath type");
        resultType |= RESULT_MOVE;
    }

    public void setBreathType() {
        if (isMove())
            throw new IllegalStateException("result already set to move type");
        resultType |= RESULT_BREATH;
    }

    public short getResultType() {
        return resultType;
    }

    public void setTargetPos(short pos) {
        if (targetPos != -1)
            throw new IllegalStateException("result target pos is already set");
        if (pos < 0)
            throw new IllegalArgumentException("target pos < 0");
        targetPos = pos;
        existTarget = 1;
    }

    public short getTargetPos() {
        return targetPos;
    }

    public boolean existTarget() {
        return existTarget == 1;
    }

    public void reset() {
        resultType = 0;
        existTarget = 0;
        targetPos = -1;
    }

    public void copy(DetectResult detectResult) {
        resultType = detectResult.resultType;
        existTarget = detectResult.existTarget;
        targetPos = detectResult.targetPos;
    }

}
