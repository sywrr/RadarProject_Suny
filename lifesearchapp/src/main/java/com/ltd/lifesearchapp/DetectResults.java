package com.ltd.lifesearchapp;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Connection.Packet;

public class DetectResults {

    // 是否锁定探测结果
    // 如果锁定，会导致无法添加，也无法获取探测结果
    private final AtomicBoolean mLocked = new AtomicBoolean(true);

    private final Lock mResultLock = new ReentrantLock(true);

    public final void setLocked(boolean isLocked) {
        mLocked.set(isLocked);
    }

    private static class ResultBuffer {

        private final _DetectResult[] mResults;

        private int mHead, mTail;

        public ResultBuffer(int size) {
            mResults = new _DetectResult[size];
            mHead = mTail = 0;
        }

        public int size() { return mTail - mHead; }

        public boolean isEmpty() { return size() == 0; }

        public _DetectResult remove() {
            if (isEmpty())
                return null;
            return mResults[(mHead++) % mResults.length];
        }

        public _DetectResult get() {
            if (isEmpty())
                return null;
            return mResults[mHead % mResults.length];
        }

        public void add(_DetectResult detectResult) {
            if (size() == mResults.length)
                ++mHead;
            mResults[(mTail++) % mResults.length] = detectResult;
        }

        public void clear() {
            mHead = mTail = 0;
        }

    }

    private final ResultBuffer[] mMoveBuffers, mBreathBuffers;

    public DetectResults(int moveTargets, int breathTargets) {
        if (moveTargets <= 0 || breathTargets <= 0)
            throw new IllegalArgumentException("params can not be negative");
        mMoveBuffers = new ResultBuffer[moveTargets];
        for (int i = 0; i < moveTargets; i++) {
            mMoveBuffers[i] = new ResultBuffer(8);
        }
        mBreathBuffers = new ResultBuffer[breathTargets];
        for (int j = 0; j < breathTargets; j++) {
            mBreathBuffers[j] = new ResultBuffer(8);
        }
    }

    // 如果是体动或者最终呼吸探测结果，则直接添加
    // 否则必是中间呼吸结果，只有在当前显示的呼吸结果不是最终探测结果是才会添加
    // 最终呼吸结果只要出现除非有下一个最终呼吸否则会一直显示在界面上
    public final void addResult(int index, _DetectResult detectResult) {
        if (!mLocked.get()) {
            boolean isMove = detectResult.isMove();
            if ((detectResult.isBreath() && isMove) || (detectResult.isMove() && !isMove))
                throw new IllegalArgumentException("invalid result type");
            ResultBuffer buffer = isMove ? mMoveBuffers[index] : mBreathBuffers[index];
            mResultLock.lock();
            try {
                if (mLocked.get())
                    return;
                if (isMove || detectResult.isFinalResult()) {
                    buffer.clear();
                    buffer.add(detectResult);
                } else {
                    _DetectResult curBreathResult = buffer.get();
                    if (curBreathResult == null || !curBreathResult.isFinalResult()) {
                        buffer.add(detectResult);
                    }
                }
            } finally {
                mResultLock.unlock();
            }
        }
    }

    public final void clearResults(boolean isLocked) {
        if (!mLocked.get()) {
            mResultLock.lock();
            try {
                if (mLocked.get())
                    return;
                mLocked.set(isLocked);
                for (ResultBuffer resultBuffer : mMoveBuffers) {
                    resultBuffer.clear();
                }
                for (ResultBuffer resultBuffer : mBreathBuffers) {
                    resultBuffer.clear();
                }
            } finally {
                mResultLock.unlock();
            }
        }
    }

    public final void clearResults(boolean isLocked, boolean isMove) {
        if (!mLocked.get()) {
            mResultLock.lock();
            try {
                if (mLocked.get())
                    return;
                mLocked.set(isLocked);
                for (ResultBuffer resultBuffer : (isMove ? mMoveBuffers : mBreathBuffers)) {
                    resultBuffer.clear();
                }
            } finally {
                mResultLock.unlock();
            }
        }
    }

    public final void clearResults() { clearResults(false); }

    private _DetectResult getMoveResult(int index) {
        return mMoveBuffers[index].remove();
    }

    private _DetectResult getBreathResult(int index) {
        _DetectResult detectResult = mBreathBuffers[index].get();
        if (detectResult != null && !detectResult.isFinalResult())
            mBreathBuffers[index].remove();
        return detectResult;
    }

    public final _DetectResult getResult(boolean isMove, int index) {
        if (!mLocked.get()) {
            mResultLock.lock();
            try {
                if (mLocked.get())
                    return null;
                if (isMove)
                    return getMoveResult(index);
                return getBreathResult(index);
            } finally {
                mResultLock.unlock();
            }
        }
        return null;
    }

    public final void addResults(Packet pack) {
        if (!mLocked.get()) {
            int length = pack.getPacketLength();
            if ((length - 8) % 6 != 0)
                throw new IllegalArgumentException("packet length invalid");
            pack.seek(8);
            _DetectResult detectResult;
            int moveIndex = 0, breathIndex = 0;
            for (int i = 0; i < (length - 8) / 6 && !mLocked.get(); i++) {
                short type = pack.getShort();
                pack.getShort();
                short targetPos = pack.getShort();
                detectResult = new _DetectResult(type, targetPos);
                int index = detectResult.isMove() ? moveIndex++ : breathIndex++;
                addResult(index, detectResult);
            }
        }
    }

}
