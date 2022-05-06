package com.ltdpro;

import com.Connection.Packet;

public class Result {

	private short mExistMove;
	private short mMovePos;
	private short mExistBreath;
	private short mBreathPos;
	private short mType;

	public final static short RESULT_INTER = 0x01;
	public final static short RESULT_BREATH = 0x10;
	public final static short RESULT_FINAL = 0x02;
	public final static short RESULT_MOVE = 0x20;

	public Result() {
		mExistMove = 0;
		mMovePos = 0;
		mExistBreath = 0;
		mBreathPos = 0;
	}

	public boolean isExistMove() {
		return mExistMove == 1;
	}

	public short getMovePos() {
		return mMovePos;
	}

	public boolean isExistBreath() {
		return mExistBreath == 1;
	}

	public short getBreathPos() {
		return mBreathPos;
	}

	public short getType() {
		return mType;
	}

	public void setType(short type) {
		mType = type;
	}

	public boolean isExistTarget() {
		return mExistMove == 1 || mExistBreath == 1;
	}

	public void setResult(short[] result_data) {
		if (result_data != null) {
			mExistMove = result_data[2];
			mMovePos = result_data[3];
			mExistBreath = result_data[4];
			mBreathPos = result_data[5];
		}
	}

	public void copyResult(Result r) {
		mType = r.mType;
		mExistMove = r.mExistMove;
		mMovePos = r.mMovePos;
		mExistBreath = r.mExistBreath;
		mBreathPos = r.mBreathPos;
	}

	public void resetResult() {
		mType = 0;
		mExistMove = 0;
		mMovePos = 0;
		mExistBreath = 0;
		mBreathPos = 0;
	}

	public Packet getResultPacket() {
		Packet pack = new Packet(Global.PACKET_DETECT_RESULT, 10);
		pack.setPacketFlag(0xAAAABBBB);
		pack.putShort(mType);
		pack.putShort(mExistMove);
		pack.putShort(mMovePos);
		pack.putShort(mExistBreath);
		pack.putShort(mBreathPos);
		return pack;
	}

	public boolean isInterResult() {
		return mType == 0x11 || mType == 0x21;
	}

	public boolean isFinalResult() {
		return mType == 0x12 || mType == 0x22;
	}

	public boolean isMoveType() {
		return mType == 0x21 || mType == 0x22;
	}

	public boolean isBreathType() {
		return mType == 0x11 || mType == 0x12;
	}

	public boolean isFinalBreathType() {
		return mType == 0x12;
	}
}
