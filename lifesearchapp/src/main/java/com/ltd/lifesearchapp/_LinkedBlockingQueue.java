package com.ltd.lifesearchapp;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class _LinkedBlockingQueue<T> {

    public interface Predication {
        boolean predicate();
    }

    private static final class Node<T> {
        T mValue;

        Node<T> mNext;

        public Node(T value) {
            mValue = value;
            mNext = null;
        }
    }

    private Node<T> mHead, mTail;

    private final AtomicInteger mCount = new AtomicInteger(0);

    private final Lock mNodeLock = new ReentrantLock(true);

    private final Condition mEmptyCond = mNodeLock.newCondition();

    private void changeCount(int delta) {
        if (delta == 0)
            return;
        int oldCount;
        do {
            oldCount = mCount.get();
            if (delta > 0 && delta > Integer.MAX_VALUE - oldCount)
                throw new IllegalArgumentException("queue count is overflow");
            if (delta < 0 && oldCount < -delta)
                throw new IllegalArgumentException("can not decrease queue count of " + delta);
        } while (!mCount.compareAndSet(oldCount, oldCount + delta));
    }

    public _LinkedBlockingQueue() {
        mHead = mTail = null;
    }

    public final boolean offer(T e) {
        if (e == null)
            throw new NullPointerException();
        mNodeLock.lock();
        boolean empty = false;
        try {
            Node<T> newNode = new Node<>(e);
            if (mHead == null) {
                mHead = newNode;
                empty = true;
            } else {
                mTail.mNext = newNode;
            }
            mTail = newNode;
            changeCount(1);
            if (empty)
                mEmptyCond.signal();
            return true;
        } finally {
            mNodeLock.unlock();
        }
    }

    public final void add(T e) {
        offer(e);
    }

    public final T peek() {
        mNodeLock.lock();
        try {
            while (mHead == null) {
                try {
                    mEmptyCond.await();
                } catch (InterruptedException ignore) {
                    return null;
                }
            }
            return mHead.mValue;
        } finally {
            mNodeLock.unlock();
        }
    }

    public final T element() {
        T result = peek();
        if (result == null)
            throw new IllegalStateException("queue is empty");
        return result;
    }

    public final T poll() {
        mNodeLock.lock();
        try {
            while (mHead == null) {
                try {
                    mEmptyCond.await();
                } catch (InterruptedException ignore) {
                    return null;
                }
            }
            T result = mHead.mValue;
            mHead = mHead.mNext;
            if (mHead == null)
                mTail = null;
            changeCount(-1);
            return result;
        } finally {
            mNodeLock.unlock();
        }
    }

    public final T remove() {
        T result = poll();
        if (result == null)
            throw new IllegalStateException("queue is empty");
        return result;
    }

    public final boolean isEmpty() {
        mNodeLock.lock();
        try {
            return mHead == null;
        } finally {
            mNodeLock.unlock();
        }
    }

    public final int size() { return mCount.get(); }

    public final T back() {
        mNodeLock.lock();
        try {
            if (mHead == null)
                return null;
            return mTail.mValue;
        } finally {
            mNodeLock.unlock();
        }
    }

}
