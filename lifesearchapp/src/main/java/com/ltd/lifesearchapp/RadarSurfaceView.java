package com.ltd.lifesearchapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Utils.AbstractLogger;
import Utils.Utils;

public class RadarSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    protected Context mContext;

    protected SurfaceHolder mHolder = null;

    protected Thread mDrawThread;

    protected AbstractLogger mLogger;

    public RadarSurfaceView(Context context) {
        super(context);
        initView(context);
    }

    public RadarSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public RadarSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    protected final static class Waiter {
        private final AtomicInteger mWaitValue = new AtomicInteger(0);

        private final Condition mCond;

        public Waiter(Lock lock) {
            mCond = lock.newCondition();
        }

        private void increase() {
            int oldValue;
            do {
                oldValue = mWaitValue.get();
                if (oldValue == Integer.MAX_VALUE)
                    throw new IllegalStateException("wait count overflow");
            } while (!mWaitValue.compareAndSet(oldValue, oldValue + 1));
        }

        private boolean decrease() {
            int oldValue;
            do {
                oldValue = mWaitValue.get();
                if (oldValue == 0)
                    return false;
            } while (!mWaitValue.compareAndSet(oldValue, oldValue - 1));
            return true;
        }

        private boolean zero() {
            int oldValue;
            do {
                oldValue = mWaitValue.get();
                if (oldValue == 0)
                    return false;
            } while (!mWaitValue.compareAndSet(oldValue, 0));
            return true;
        }

        public final void await_() {
            increase();
            mCond.awaitUninterruptibly();
        }

        public final void signal_() {
            if (decrease())
                mCond.signal();
        }

        public final void signalAll_() {
            if (zero())
                mCond.signalAll();
        }
    }

    protected final Lock mDrawLock = new ReentrantLock(true);

    protected final Waiter mDestroyWaiter = new Waiter(mDrawLock);

    protected final Waiter mLockViewWaiter = new Waiter(mDrawLock);

    protected volatile boolean mDestroyed = true;

    protected volatile boolean mLocked = false;

    public final boolean locked() { return mLocked; }

    public final boolean destroyed() { return mDestroyed; }

    protected final boolean checkViewStatus() {
        mDrawLock.lock();
        try {
            while (mRunning && (mDestroyed || mLocked)) {
                if (mDestroyed) {
                    mDestroyWaiter.await_();
                } else if (mLocked) {
                    mLockViewWaiter.await_();
                }
            }
            return mRunning;
        } finally {
            mDrawLock.unlock();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mDrawLock.lock();
        try {
            onSurfaceCreated(holder);
            if (mDestroyed) {
                mDestroyed = false;
                mDestroyWaiter.signal_();
            }
            AbstractLogger.Debug("surface created", mLogger);
        } finally {
            mDrawLock.unlock();
        }
    }

    protected void onSurfaceCreated(SurfaceHolder holder) {}

    protected void onSurfaceDestroyed(SurfaceHolder holder) {}

    protected volatile boolean mRunning = false;

    protected int mLeftPadding = 0;

    protected int mRightPadding = 0;

    protected int mTopPadding = 0;

    protected int mBottomPadding = 0;

    protected volatile int mWidth;

    protected volatile int mHeight;

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mLogger.debug("surface changed");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mDrawLock.lock();
        try {
            onSurfaceDestroyed(holder);
            mDestroyed = true;
            mLogger.debug("call surface destroyed");
        } finally {
            mDrawLock.unlock();
        }
    }

    public void clearView() { }

    protected final Drawer mLockDrawer = new Drawer() {
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.WHITE);
        }

        @Override
        protected boolean checkDraw() {
            return true;
        }
    };

    public final void lockView() {
        mDrawLock.lock();
        try {
            if (!mLocked) {
                onPostLockView();
                mLocked = true;
                mLogger.debug("set locked true");
            }
        } finally {
            mDrawLock.unlock();
        }
    }

    public final void unlockView() {
        mDrawLock.lock();
        try {
            if (mLocked) {
                onUnlockView();
                mLocked = false;
                mLogger.debug("set locked false");
                mLockViewWaiter.signal_();
            }
        } finally {
            mDrawLock.unlock();
        }
    }

    public final boolean isHidden() {
        return ((MainActivity) mContext).requireExpertFragment().isInHiddenStatus();
    }

    protected void onPostLockView() {}

    protected void onUnlockView() {}

    protected void initView(Context context) {
        mContext = context;
        mHolder = this.getHolder();
        mHolder.addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setKeepScreenOn(true);
        mWidth = -1;
        mHeight = -1;
        mDrawThread = null;
        mRunning = false;
    }

    protected int mDibWidth;

    protected int mPixesPerScans = 1;
    protected int mScansPerScale = 50;

    protected int mBeginScans = 0;
    protected int mEndScans = 0;

    protected void doStartDraw() {
        mLeftPadding = 80;
        mRightPadding = 80;
        mTopPadding = 10;
        mBottomPadding = 0;
        mDibWidth = mWidth - mLeftPadding - mRightPadding;
        mBeginScans = mEndScans = 0;
        mLogger.debug("width: " + mWidth);
    }

    protected int getMaxScans() {
        return (getRelativeX(mDibWidth - 10) - getRelativeX(10)) / mPixesPerScans;
    }

    public void stopDraw() {
        Thread drawThread = null;
        mDrawLock.lock();
        try {
            if (mRunning) {
                long st = System.nanoTime();
                mRunning = false;
                doStop();
                drawThread = mDrawThread;
                mDrawThread = null;
                long et = System.nanoTime();
                AbstractLogger.Debug("stop draw thread cost time: " + (et - st) / 1000000, mLogger);
            }
        } finally {
            mDrawLock.unlock();
        }
        Utils.joinThreadUninterruptibly(drawThread);
        mLogger.debug("join finished");
    }

    public void startDraw() {
        mDrawLock.lock();
        try {
            if (!mRunning) {
                mDrawThread = new Thread(this);
                mRunning = true;
                mDrawThread.start();
            }
        } finally {
            mDrawLock.unlock();
        }
    }

    @Override
    public void run() {
        while ((mWidth = getWidth()) <= 0 || (mHeight = getHeight()) <= 0) {
            if (!mRunning) {
                mLogger.debug("stop expert view thread");
                return;
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignore) { }
        }
        if (mRunning) {
            doStartDraw();
            while (mRunning) { doRun(); }
        }
        if (mLogger != null)
            mLogger.debug(mLogger.getTag() + " draw thread is already exit");
    }

    protected void doStop() {
        mDrawThread.interrupt();
        mDestroyWaiter.signal_();
        mLockViewWaiter.signal_();
    }

    protected void doRun() { }

    protected abstract class Drawer {

        protected abstract void onDraw(Canvas canvas);

        protected abstract boolean checkDraw();

        public final void draw(Rect rect) {
            Canvas canvas = null;
            mDrawLock.lock();
            try {
                if (!checkDraw())
                    return;
                try {
                    if (rect != null) {
                        canvas = mHolder.lockCanvas(rect);
                    } else {
                        canvas = mHolder.lockCanvas();
                    }
                    if (canvas != null)
                        onDraw(canvas);
                } catch (Exception e) {
                    AbstractLogger.ExceptStackTrace(e, mLogger);
                } finally {
                    if (canvas != null)
                        mHolder.unlockCanvasAndPost(canvas);
                }
            } finally {
                mDrawLock.unlock();
            }
        }

    }

    protected final Drawer mDrawer = new Drawer() {
        @Override
        protected void onDraw(Canvas canvas) {
            drawOnCanvas(canvas);
        }

        @Override
        protected boolean checkDraw() {
            return checkViewStatus();
        }
    };

    protected void doDraw(Rect rect) {
        mDrawer.draw(rect);
    }

    protected void drawOnCanvas(Canvas canvas) {}

    protected final void DrawSleep(long costTime) {
        long avgTime = 16666666;
        if (costTime < 0) {
            mLogger.error("cost time: " + ((double) costTime / 1000000) + " ms, avgTime: " +
                          ((double) avgTime / 1000000) + " ms");
            throw new RuntimeException("time calculate error");
        }
        if (avgTime > costTime) {
            long sleepTime = avgTime - costTime;
            try {
                Thread.sleep(sleepTime / 1000000, (int) (sleepTime % 1000000));
            } catch (InterruptedException e) {
                mLogger.debug("interrupt ruler view wait");
            }
        }
    }

    protected Rect getDrawableRect() {
        Rect rect = new Rect();
        rect.left = mLeftPadding;
        rect.right = mWidth - mRightPadding;
        rect.top = mTopPadding;
        rect.bottom = mHeight - mBottomPadding;
        return rect;
    }

    protected int getRelativeX(boolean flag, int xOffset) {
        if (flag) {
            if (xOffset < 0) {
                return Math.max(mWidth - mRightPadding + xOffset, mLeftPadding);
            }
            return Math.min(mLeftPadding + xOffset, mWidth - mRightPadding);
        } else {
            if (xOffset < 0) {
                return Math.min(mLeftPadding - xOffset, mWidth - mRightPadding);
            }
            return Math.max(mWidth - mRightPadding - xOffset, mLeftPadding);
        }
    }

    protected int getRelativeY(boolean flag, int yOffset) {
        if (flag) {
            if (yOffset < 0) {
                return Math.max(mHeight - mBottomPadding + yOffset, mTopPadding);
            }
            return Math.min(mTopPadding + yOffset, mHeight - mBottomPadding);
        } else {
            if (yOffset < 0) {
                return Math.min(mTopPadding - yOffset, mHeight - mBottomPadding);
            }
            return Math.max(mHeight - mBottomPadding - yOffset, mTopPadding);
        }
    }

    protected int getRelativeX(int xOffset) {
        return getRelativeX(true, xOffset);
    }

    protected int getRelativeY(int yOffset) {
        return getRelativeY(true, yOffset);
    }
}
