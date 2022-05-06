package com.ltdpro;

public class MyLinkedQueue {

	protected class ListNode {
		Object mObj;
		ListNode mNext;

		public ListNode() {
			mObj = null;
			mNext = null;
		}
	}

	protected ListNode mHeadNode, mTailNode;
	protected int mSize;

	public MyLinkedQueue() {
		mHeadNode = null;
		mTailNode = null;
		mSize = 0;
	}

	public int getSize() {
		return mSize;
	}

	public void popObject() {
		if (mHeadNode != null) {
			mHeadNode = mHeadNode.mNext;
			if (mHeadNode == null)
				mTailNode = null;
			--mSize;
		}
	}

	public void putObject(Object obj) {
		ListNode newNode = new ListNode();
		newNode.mObj = obj;
		if (mHeadNode == null) {
			mHeadNode = newNode;
			mTailNode = newNode;
		} else {
			mTailNode.mNext = newNode;
			mTailNode = newNode;
		}
		++mSize;
	}

	public Object getObject() {
		if (mHeadNode == null)
			return null;
		return mHeadNode.mObj;
	}
}
