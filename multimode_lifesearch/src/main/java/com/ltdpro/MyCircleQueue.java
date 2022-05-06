package com.ltdpro;

public class MyCircleQueue {

	private Object[] mObjs;
	private int mStart, mFinish;
	private int mCapacity;

	public MyCircleQueue(int capacity) {
		mCapacity = capacity;
		mStart = mFinish = 0;
		if (capacity > 0)
			mObjs = new Object[capacity + 1];
		else
			mObjs = null;
	}

	public void put(Object obj) {
		if (mObjs == null)
			return;
		int finish = (mFinish + 1) % (mCapacity + 1);
		mObjs[mFinish] = obj;
		mFinish = finish;
		if (mFinish == mStart)
			mStart = (mStart + 1) % (mCapacity + 1);
	}

	public Object get() {
		if (mObjs == null || mStart == mFinish)
			return null;
		Object obj = mObjs[mStart];
		mStart = (mStart + 1) % (mCapacity + 1);
		return obj;
	}

	public int size() {
		return mFinish >= mStart ? mFinish - mStart : (mCapacity + 1 - mStart) + mFinish;
	}

	public boolean isEmpty() {
		return mStart == mFinish;
	}

	public boolean isFull() {
		return (mFinish + 1) % (mCapacity + 1) == mStart;
	}
}
