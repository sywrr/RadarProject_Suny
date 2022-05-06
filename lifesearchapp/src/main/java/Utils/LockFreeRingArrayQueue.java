package Utils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class LockFreeRingArrayQueue<T> {

    private final AtomicReferenceArray<T> mAtomicReferenceArray;

    private final int mQueueCapacity, mBits;

    private void checkQueueSize(int size) {
        boolean valid = (size & (size - 1)) == 0;
        if (!valid)
            throw new IllegalArgumentException("size must be a power of 2");
    }

    private final AtomicInteger mHead = new AtomicInteger(0);
    private final AtomicInteger mTail = new AtomicInteger(0);

    @SuppressWarnings("unchecked")
    public LockFreeRingArrayQueue(int size) {
        if (size <= 0)
            throw new IllegalArgumentException("queue size can not be negative");
        checkQueueSize(size);
        mAtomicReferenceArray = new AtomicReferenceArray<>((T[]) new Object[size]);
        mQueueCapacity = size;
        mBits = size - 1;
    }

    public boolean offer(T val) {
        if (val == null)
            throw new IllegalArgumentException("val can not be null");
        int tail;
        while ((tail = mTail.get()) - mHead.get() < mQueueCapacity) {
            if (mAtomicReferenceArray.compareAndSet(tail & mBits, null, val)) {
                mTail.compareAndSet(tail, tail + 1);
                return true;
            }
            while (mAtomicReferenceArray.get(tail & mBits) != null) {
                int oldTail;
                while ((oldTail = mTail.get()) - (tail + 1) < 0) {
                    if (mTail.compareAndSet(oldTail, tail + 1))
                        break;
                }
                if ((tail = mTail.get()) - mHead.get() >= mQueueCapacity)
                    return false;
            }
        }
        return false;
    }

    public void add(T val) {
        if (!offer(val))
            throw new IllegalArgumentException("queue is over flow");
    }

    public T peek() {
        int head = mHead.get();
        T result = null;
        while (mTail.get() - head > 0) {
            result = mAtomicReferenceArray.get(head & mBits);
            if (result != null)
                break;
            ++head;
        }
        return result;
    }

    public T element() {
        T result = peek();
        if (result == null)
            throw new IllegalStateException("queue is empty");
        return result;
    }

    public T poll() {
        T result = null;
        int head;
        while (mTail.get() - (head = mHead.get()) > 0) {
            result = mAtomicReferenceArray.get(head & mBits);
            if (result != null) {
                if (mAtomicReferenceArray.compareAndSet(head & mBits, result, null)) {
                    mHead.compareAndSet(head, head + 1);
                    break;
                }
                continue;
            }
            while (result == null) {
                int oldHead;
                while ((oldHead = mHead.get()) - (head + 1) < 0) {
                    if (mHead.compareAndSet(oldHead, head + 1))
                        break;
                }
                if (mTail.get() - (head = mHead.get()) < 0)
                    throw new IllegalStateException("size < 0, error");
                if (mTail.get() - (head = mHead.get()) == 0)
                    return null;
                result = mAtomicReferenceArray.get(head & mBits);
            }
        }
        return result;
    }

    public T remove() {
        T result = poll();
        if (result == null)
            throw new IllegalStateException("queue is empty");
        return result;
    }

    public boolean contains(Object val) {
        T result;
        int start = mHead.get();
        while (mTail.get() - start > 0) {
            result = mAtomicReferenceArray.get(start & mBits);
            if (result != null && result.equals(val))
                return true;
            ++start;
        }
        return false;
    }

    public int size() {
        return mTail.get() - mHead.get();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

}