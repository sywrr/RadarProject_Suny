package com.ltdpro;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class Blocker {
    private final Lock mLock;
    private final Condition mCond;
    private final AtomicInteger mBlockValue;
    private final AtomicBoolean mBlocking;

    public Blocker(Lock lock) {
        mLock = lock;
        mCond = lock.newCondition();
        mBlockValue = new AtomicInteger(0);
        mBlocking = new AtomicBoolean(false);
    }

    public final void block() {
        mLock.lock();
        try {
            if (mBlockValue.get() == 0) {
                mBlocking.set(true);
                mCond.awaitUninterruptibly();
            }
        } finally {
            mBlockValue.set(0);
            mLock.unlock();
        }
    }

    private void unblock(boolean all) {
        mLock.lock();
        int blockValue = mBlockValue.get();
        try {
            if (blockValue == 0 && mBlocking.get()) {
                if (all)
                    mCond.signalAll();
                else
                    mCond.signal();
            } else {
                mBlockValue.set(blockValue == Integer.MAX_VALUE ? 1 : blockValue + 1);
            }
        } finally {
            mBlocking.set(false);
            mLock.unlock();
        }
    }

    public final void unblock() { unblock(false); }

    public final void unblockAll() { unblock(true); }
}
