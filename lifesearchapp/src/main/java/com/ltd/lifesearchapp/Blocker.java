package com.ltd.lifesearchapp;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class Blocker {
    private final Lock lock;

    private final Condition cond;

    private int val = 0;

    public Blocker(Lock lock) {
        this.lock = lock;
        cond = lock.newCondition();
    }

    public final void block() {
        lock.lock();
        try {
            if (val - 1 < 0) {
                val -= 1;
                cond.awaitUninterruptibly();
            } else if (val == 1) {
                val = 0;
            } else if (val < 0) {
                throw new IllegalStateException("block too much times");
            }
        } finally {
            lock.unlock();
        }
    }

    private void unblock(boolean all) {
        lock.lock();
        try {
            if (val < 0) {
                if (all) {
                    cond.signalAll();
                    val = 0;
                } else {
                    val += 1;
                    cond.signal();
                }
            } else if (val == 0) {
                val = 1;
            }
        } finally {
            lock.unlock();
        }
    }

    public final void unblock() { unblock(false); }

    public final void unblockAll() { unblock(true); }
}
