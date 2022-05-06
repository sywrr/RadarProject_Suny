package com.ltd.lifesearchapp;

public class _DetectResult {

	public final static short RESULT_INTER = 0x01;
	public final static short RESULT_BREATH = 0x10;
	public final static short RESULT_FINAL = 0x02;
	public final static short RESULT_MOVE = 0x20;

	private final static short[] types = new short[]{RESULT_FINAL | RESULT_MOVE,
													 RESULT_FINAL | RESULT_BREATH,
													 RESULT_INTER | RESULT_MOVE,
													 RESULT_INTER | RESULT_BREATH};

	private short mType = -1;
	private short mExistTarget = 0;
	private short mTargetPos = -1;

	public _DetectResult() {}

	public _DetectResult(short type) {
		checkResultType(type);
		mType = type;
	}

	public _DetectResult(short type, short targetPos) {
		checkResultType(type);
		mType = type;
		setTargetPos(targetPos);
	}

	private void checkResultType(int resultType) {
		for (short type : types) {
			if (resultType == type)
				return;
		}
		throw new IllegalArgumentException("invalid result type");
	}

	public boolean isBreath() { return (mType & RESULT_BREATH) == RESULT_BREATH; }

	public boolean isMove() { return (mType & RESULT_MOVE) == RESULT_MOVE; }

	public boolean isInterResult() { return (mType & RESULT_INTER) == RESULT_INTER; }

	public boolean isFinalResult() { return (mType & RESULT_FINAL) == RESULT_FINAL; }

	public void setFinalType() {
		if (isInterResult())
			throw new IllegalStateException("result is already set to inter type");
		mType |= RESULT_FINAL;
	}

	public void setInterType() {
		if (isFinalResult())
			throw new IllegalStateException("result is already set to final type");
		mType |= RESULT_INTER;
	}

	public void setMoveType() {
		if (isBreath())
			throw new IllegalStateException("result is already set to breath type");
		mType |= RESULT_MOVE;
	}

	public void setBreathType() {
		if (isMove())
			throw new IllegalStateException("result already set to move type");
		mType |= RESULT_BREATH;
	}

	public short getResultType() { return mType; }

	public void setTargetPos(short pos) {
		if (mTargetPos != -1)
			throw new IllegalStateException("result breath_target pos is already set");
		mTargetPos = pos;
		mExistTarget = 1;
	}

	public short getTargetPos() { return mTargetPos; }

	public boolean existTarget() { return mExistTarget == 1; }

	public void reset() {
		mType = -1;
		mExistTarget = 0;
		mTargetPos = -1;
	}

	public void copy(_DetectResult detectResult) {
		mType = detectResult.mType;
		mExistTarget = detectResult.mExistTarget;
		mTargetPos = detectResult.mTargetPos;
	}

}
