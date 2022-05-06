package com.Concurrent;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class CLHLock {
    private static class CLHNode {
        volatile boolean mIsLocked = true;
    }

    volatile CLHNode mTail;

    static final ThreadLocal<CLHNode> mLocal = new ThreadLocal<>();

    static final AtomicReferenceFieldUpdater<CLHLock, CLHNode> mUpdater =
            AtomicReferenceFieldUpdater.newUpdater(CLHLock.class, CLHNode.class, "mTail");

    public void lock() {
        CLHNode node = new CLHNode();
        mLocal.set(node);

        CLHNode preNode = mUpdater.getAndSet(this, node);
        if (preNode != null) {
            while (preNode.mIsLocked) {}
        }
    }

    public void unlock() {
        CLHNode node = mLocal.get();
        if (node != null && !mUpdater.compareAndSet(this, node, null)) {
            node.mIsLocked = false;
        }
    }
}
