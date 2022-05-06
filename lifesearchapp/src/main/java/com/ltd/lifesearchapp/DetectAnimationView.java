package com.ltd.lifesearchapp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Utils.AbstractLogger;
import Utils.Logcat;

public class DetectAnimationView extends View {

    private final Context mContext;

    public DetectAnimationView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public DetectAnimationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public DetectAnimationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    // 探测界面标题文字画笔
    private final Paint mTitleTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // 探测界面标题画笔
    private final Paint mTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // 边框画笔
    private final Paint mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // 探测结果距离文字画笔
    private final Paint mDetectResultPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // 探测范围文字画笔
    private final Paint mDetectTxtPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // 探测时长文字画笔
    private final Paint mDetectTimePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private volatile int detectDistance = -1;

    private void init() {
        mTitleTextPaint.setColor(Color.rgb(9, 216, 41));
        Typeface fontTypeface = Typeface.create("宋体", Typeface.BOLD);
        mTitleTextPaint.setTypeface(fontTypeface);
        mTitleTextPaint.setTextAlign(Paint.Align.CENTER);
        mTitleTextPaint.setTextSize(20);
        mTitlePaint.setColor(Color.rgb(88, 90, 87));
        mBorderPaint.setColor(Color.BLACK);
        mBorderPaint.setStrokeWidth(mBorderWidth);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mDetectResultPaint.setColor(Color.RED);
        mDetectResultPaint.setTypeface(fontTypeface);
        mDetectResultPaint.setTextSize(20);
        mDetectResultPaint.setTextAlign(Paint.Align.CENTER);
        mDetectTxtPaint.setTypeface(fontTypeface);
        mDetectTxtPaint.setTextSize(30);
        mDetectTxtPaint.setTextAlign(Paint.Align.LEFT);
        mDetectTxtPaint.setColor(Color.BLACK);
        mDetectTimePaint.setTypeface(fontTypeface);
        mDetectTimePaint.setColor(Color.rgb(9, 216, 41));
        mDetectTimePaint.setTextAlign(Paint.Align.LEFT);
        mDetectTimePaint.setTextSize(30);

        Resources res = mContext.getResources();
        mBitmapBreathTarget = BitmapFactory.decodeResource(res, R.drawable.breath_target);
        mBitmapMidBreathTarget = BitmapFactory.decodeResource(res, R.drawable.middle_breath);
        mBitmapMoveTarget = BitmapFactory.decodeResource(res, R.drawable.move_target);
        mBitmapMidMoveTarget = BitmapFactory.decodeResource(res, R.drawable.middle_move);

        initViewSize();
    }

    // 初始化界面的尺寸，以及后续绘图需要用到的坐标信息
    // 使用view.post，如果更新界面大小不成功就继续post
    private void initViewSize() {
        post(new Runnable() {
            @Override
            public void run() {
                if ((mWidth = getWidth()) > 0 && (mHeight = getHeight()) > 0) {
                    mLogger.debug("width: " + mWidth + ", height: " + mHeight);
                    mViewRect.left = Math.round(mBorderWidth);
                    mViewRect.top = Math.round(mBorderWidth);
                    mViewRect.right = mWidth - mViewRect.left;
                    mViewRect.bottom = mHeight - mViewRect.top;
                    int innerWidth = mViewRect.right - mViewRect.left;
                    float animationStartX = mViewRect.left + (innerWidth - mTitleWidth) / 2;
                    float animationStartY = mViewRect.top + 5 + mTitleHeight * 2 - 8f;
                    PointF pointF = new PointF(animationStartX, animationStartY);
                    mAnimationArea[0] = pointF;
                    mAnimationArea[1] = new PointF(animationStartX + mTitleWidth, pointF.y);
                    mAnimationArea[2] = new PointF(mViewRect.left + 50f, mViewRect.bottom - 100f);
                    mAnimationArea[3] = new PointF(mViewRect.right - 50f, mViewRect.bottom - 100f);
                } else {
                    initViewSize();
                }
            }
        });
    }

    private final float mBorderWidth = 2f;

    private final float mTitleWidth = 500f;

    private final float mTitleHeight = 30f;

    private final Rect mViewRect = new Rect();

    private final PointF[] mAnimationArea = new PointF[4];

    private final AbstractLogger mLogger = new Logcat("DetectAnimationView", true);

    private long mStartTime;

    private volatile double mAnimateTime = 0;

    // 是否已经开始动画绘制
    private final AtomicBoolean mStarted = new AtomicBoolean(false);

    private volatile int mWidth = 0, mHeight = 0;

    private int mScanTimes = 0;

    private void drawLine(Canvas canvas, PointF startPointF, PointF endPointF, Paint paint) {
        canvas.drawLine(startPointF.x, startPointF.y, endPointF.x, endPointF.y, paint);
    }

    // 更新接口，用于内部静态类handler修改外部类私有数据
    // 这样设计的好处在于，可以让内部静态类持有该接口实例，修改外部类数据
    // 同时也不需要让外部类暴露实际修改数据的接口
    private class UpdateInterface {

        public long getStartTime() { return mStartTime; }

        public void resetStartTime() {
            mAnimateTime = 0;
            mStartTime = System.nanoTime();
        }

        public boolean isStarted() { return mStarted.get(); }

        public void setStarted(boolean flag) { mStarted.set(flag); }

        public void resetScanTimes() { mScanTimes = 0; }

    }

    private final UpdateInterface mUpdateInterface = new UpdateInterface();

    // 刷新界面的handler
    private static class UpdateHandler extends Handler {

        public final static int START_ANIMATION = 0xaa;

        public final static int STOP_ANIMATION = 0xbb;

        private final View mView;

        public UpdateHandler(View view) {
            super();
            mView = view;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.obj != null) {
                if (!(msg.obj instanceof UpdateInterface))
                    throw new IllegalArgumentException("invalid update handler message");
                UpdateInterface updateInterface = (UpdateInterface) msg.obj;
                switch (msg.what) {
                    case START_ANIMATION:
                        updateInterface.setStarted(true);
                        updateInterface.resetStartTime();
                        updateInterface.resetScanTimes();
                        break;
                    case STOP_ANIMATION:
                        updateInterface.setStarted(false);
                        break;
                }
            }
            mView.invalidate();
        }
    }

    private final UpdateHandler mUpdateHandler = new UpdateHandler(this);

    private final Lock mUpdateLock = new ReentrantLock(true);

    private final AtomicInteger mLastMsg = new AtomicInteger(UpdateHandler.STOP_ANIMATION);

    // 向更新handler发送延时信息来刷新界面
    private void update(long delay) {
        int what = mStart.get() ? UpdateHandler.START_ANIMATION : UpdateHandler.STOP_ANIMATION;
        Message msg = new Message();
        if (mLastMsg.getAndSet(what) != what) {
            msg.what = what;
            msg.obj = mUpdateInterface;
            if (what == UpdateHandler.STOP_ANIMATION) {
                mUpdateHandler.removeMessages(-1);
            }
        } else {
            msg.what = -1;
            msg.obj = null;
        }
        mUpdateHandler.sendMessageDelayed(msg, delay);
    }

    // 绘制界面网格，也就是界面背景
    private boolean drawGrid(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        if (mWidth > 0 && mHeight > 0) {
            canvas.drawRect(mBorderWidth / 2, mBorderWidth / 2, mWidth - mBorderWidth / 2,
                            mHeight - mBorderWidth / 2, mBorderPaint);
            int innerWidth = mViewRect.right - mViewRect.left;
            float titleStartX = mViewRect.left + (innerWidth - mTitleWidth) / 2;
            float titleStartY = mViewRect.top + 5;
            RectF rectF = new RectF(titleStartX - 0.5f, titleStartY,
                                    titleStartX + mTitleWidth - 0.5f, titleStartY + mTitleHeight);
            canvas.drawRoundRect(rectF, 10f, 10f, mTitlePaint);
            canvas.drawRect(titleStartX, titleStartY + mTitleHeight - 10f,
                            titleStartX + mTitleWidth, titleStartY + mTitleHeight * 2 - 10f,
                            mTitlePaint);
            String txt = "SER-10";
            float txtStartX = titleStartX + mTitleWidth / 2;
            float txtStartY = titleStartY + mTitleHeight - 5f;
            Rect txtRect = new Rect();
            mTitleTextPaint.getTextBounds(txt, 0, txt.length(), txtRect);
            canvas.drawText(txt, txtStartX, txtStartY + (txtRect.bottom - txtRect.top) / 2f - 2f,
                            mTitleTextPaint);
            drawLine(canvas, mAnimationArea[0], mAnimationArea[1], mBorderPaint);
            drawLine(canvas, mAnimationArea[0], mAnimationArea[2], mBorderPaint);
            drawLine(canvas, mAnimationArea[2], mAnimationArea[3], mBorderPaint);
            drawLine(canvas, mAnimationArea[1], mAnimationArea[3], mBorderPaint);
            return true;
        }
        mLogger.debug("draw grid failed");
        return false;
    }

    private PointF copyPointF(PointF pointF) {
        PointF copyPointF = new PointF();
        copyPointF.x = pointF.x;
        copyPointF.y = pointF.y;
        return copyPointF;
    }

    // 绘制探测界面中央动画区域的闪烁效果
    // 该闪烁特效每隔0.5秒闪烁一次
    private void drawAnimationArea(Canvas canvas) {
        if (((long) mAnimateTime / 500) % 2 == 0) {
            PointF startPointF = copyPointF(mAnimationArea[0]);
            PointF endPointF = copyPointF(mAnimationArea[1]);
            float heightDelta = mAnimationArea[2].y - mAnimationArea[0].y - mBorderWidth;
            float widthDelta = mAnimationArea[0].x - mAnimationArea[2].x - mBorderWidth;
            float scale = widthDelta / heightDelta;
            Paint paint = new Paint();
            paint.setColor(Color.rgb(200, 200, 200));
            paint.setStrokeWidth(1);
            int drawLines = Math.round(heightDelta);
            if (drawLines > heightDelta)
                --drawLines;
            for (int i = 0; i < drawLines; i++) {
                ++startPointF.y;
                startPointF.x -= scale;
                ++endPointF.y;
                endPointF.x += scale;
                canvas.drawLine(startPointF.x + mBorderWidth / 2, startPointF.y,
                                endPointF.x - mBorderWidth / 2, endPointF.y, paint);
            }
        }
    }

    // 绘制底部探测范围
    private void drawDetectRange(Canvas canvas) {
        float detectRangeStartX = mAnimationArea[2].x + 30;
        float detectRangeStartY = (mAnimationArea[2].y + mViewRect.bottom) / 2;
        _DetectParams detectParams = mDetectParamsVariable.get();
        String detectRangeTxt = String.format(Locale.ENGLISH, "正在探测: %d - %d米",
                                              detectParams.mDetectStart, detectParams.mDetectEnd);
        Rect detectRangeTxtRect = new Rect();
        mDetectTxtPaint.getTextBounds(detectRangeTxt, 0, detectRangeTxt.length(),
                                      detectRangeTxtRect);
        int detectRangeTxtHeight = detectRangeTxtRect.bottom - detectRangeTxtRect.top;
        canvas.drawText(detectRangeTxt, detectRangeStartX,
                        detectRangeStartY + detectRangeTxtHeight / 2f, mDetectTxtPaint);
    }

    private void drawTxtDetectResult(Canvas canvas) {
        String txtResult = "探测结果：";
        if (detectDistance != -1) {
            txtResult += detectDistance + "cm";
        }
        float detectTxtStartY = (mAnimationArea[2].y + mViewRect.bottom) / 2;
        float detectTxtStartX = mAnimationArea[3].x - 250;
        Rect detectDisRect = new Rect();
        mDetectTxtPaint.getTextBounds(txtResult, 0, txtResult.length(), detectDisRect);
        int detectTxtHeight = detectDisRect.bottom - detectDisRect.top;
        canvas.drawText(txtResult, detectTxtStartX, detectTxtStartY + detectTxtHeight / 2f,
                        mDetectTxtPaint);
    }

    // 绘制当前总探测时长
    private void drawDetectTime(Canvas canvas) {
        long now;
        double lastTime = ((double) ((now = System.nanoTime()) - mStartTime)) / 1000000;
        mStartTime = now;
        mAnimateTime += lastTime;
        String detectTimeTxt = String.format(Locale.ENGLISH, "探测时间: %d秒",
                                             (int) (mAnimateTime / 1000));
        Rect detectTimeTxtRect = new Rect();
        mDetectTimePaint.getTextBounds(detectTimeTxt, 0, detectTimeTxt.length(), detectTimeTxtRect);
        int detectTimeTxtHeight = detectTimeTxtRect.bottom - detectTimeTxtRect.top;
        int detectTimeTxtWidth = detectTimeTxtRect.right - detectTimeTxtRect.left;
        float detectTimeStartX = (2 * mViewRect.left + mAnimationArea[0].x - detectTimeTxtWidth) /
                                 3;
        float detectTimeStartY = mViewRect.top + 50;
        canvas.drawText(detectTimeTxt, detectTimeStartX,
                        detectTimeStartY + detectTimeTxtHeight / 2f, mDetectTimePaint);
    }

    public void resetDetectTime() {
        mUpdateInterface.resetStartTime();
    }

    private void animationDraw(Canvas canvas) {
        drawDetectTime(canvas);
        drawAnimationArea(canvas);
        drawDetectResults(canvas);
        drawDetectRange(canvas);
        drawTxtDetectResult(canvas);
    }

    // 每次绘制完成后向更新handler发送延时信息方便下一次更新画面
    @Override
    protected final void onDraw(Canvas canvas) {
        if (!drawGrid(canvas)) {
            invalidate();
        } else {
            if (mStarted.get())
                animationDraw(canvas);
            mUpdateLock.lock();
            try {
                if (mStarted.get())
                    update(50);
            } finally {
                mUpdateLock.unlock();
            }
        }
    }

    // 是否开始动画绘制
    private final AtomicBoolean mStart = new AtomicBoolean(false);

    // 开始动画
    public final void startAnimation() {
        mUpdateLock.lock();
        try {
            if (!mStart.get()) {
                mStart.set(true);
                onStartAnimation();
                update(0);
            }
        } finally {
            mUpdateLock.unlock();
        }
    }

    // 结束动画
    public final void stopAnimation() {
        mUpdateLock.lock();
        try {
            if (mStart.get()) {
                mStart.set(false);
                update(0);
                onStopAnimation();
            }
        } finally {
            mUpdateLock.unlock();
        }
    }

    private void onStartAnimation() {
        mDetectResults.setLocked(false);
    }

    private void onStopAnimation() {
        mDetectResults.clearResults(true);
        mLastResultBuffer.init();
    }

    public final DetectResults mDetectResults = new DetectResults(2, 1);

    private final DetectParamsVariable mDetectParamsVariable = new DetectParamsVariable();

    public final void setDetectParams(int detectStart, int detectEnd, int detectInterval) {
        mDetectParamsVariable.set(detectStart, detectEnd, detectInterval);
    }

    public final void setDetectRangeParams(int detectStart, int detectEnd) {
        mDetectParamsVariable.set(detectStart, detectEnd);
    }

    // 上一次探测结果的缓冲区
    // 用于在一定时间内缓存上一次探测结果
    // 由于界面刷新频率较快，如果不缓存探测结果，可能会导致界面上的探测结果一闪而过
    private final class LastResultsBuffer {

        private final long mTimeLimit;

        private final _DetectResult[] mLastResults;

        private final long[] mTimeStamp;

        public LastResultsBuffer(long timeLimit) {
            mTimeLimit = timeLimit;
            mLastResults = new _DetectResult[mMaxBreathTargets + mMaxMoveTargets];
            Arrays.fill(mLastResults, null);
            mTimeStamp = new long[mLastResults.length];
        }

        public void setLastResult(int index, _DetectResult detectResult) {
            int realIndex = index + (detectResult.isMove() ? 0 : mMaxMoveTargets);
            mLastResults[realIndex] = detectResult;
            mTimeStamp[realIndex] = System.nanoTime();
        }

        public _DetectResult getLastResult(boolean isMove, int index) {
            int realIndex = index + (isMove ? 0 : mMaxMoveTargets);
            _DetectResult detectResult = mLastResults[realIndex];
            if (detectResult != null) {
                long now = System.nanoTime();
                long duration = (now - mTimeStamp[realIndex]) / 1000000L;
                if (duration >= mTimeLimit) {
                    detectResult = null;
                    mLastResults[realIndex] = null;
                    mTimeStamp[realIndex] = now;
                }
            }
            return detectResult;
        }

        public void init() {
            Arrays.fill(mLastResults, null);
        }

    }

    private final LastResultsBuffer mLastResultBuffer = new LastResultsBuffer(500L);

    private Bitmap mBitmapBreathTarget = null;

    private Bitmap mBitmapMidBreathTarget = null;

    private Bitmap mBitmapMoveTarget = null;

    private Bitmap mBitmapMidMoveTarget = null;

    private final int mMaxMoveTargets = 2;

    private final int mMaxBreathTargets = 1;

    private void drawOneResult(Canvas canvas, _DetectResult detectResult) {
        Bitmap targetBMP;
        if (detectResult.isFinalResult()) {
            targetBMP = detectResult.isMove() ? mBitmapMoveTarget : mBitmapBreathTarget;
        } else {
            targetBMP = detectResult.isMove() ? mBitmapMidMoveTarget : mBitmapMidBreathTarget;
        }
        int nSample = detectResult.getTargetPos();
        _DetectParams detectParams = mDetectParamsVariable.get();
        int detectStart = detectParams.mDetectStart * 100;
        int maxDetectDistance = detectParams.mDetectInterval * 100 + detectStart;
        int scanLength = 8192 / (12 / detectParams.mDetectInterval);//2048
        int distance = detectStart + (nSample * detectParams.mDetectInterval * 100 / scanLength);
        int distanceCheck = ((MainActivity) mContext).requireSettingsFragment().getDistanceCheck();
        distance = Math.min(distance + distanceCheck, maxDetectDistance); // 修正探测距离60cm
        detectDistance = distance;
        final boolean isMoveTarget = targetBMP == mBitmapMidMoveTarget ||
                                     targetBMP == mBitmapMoveTarget;
        int bitmapWidth = targetBMP.getWidth();
        int bitmapHeight = targetBMP.getHeight();
        String disTxt = distance + "cm";
        Rect disTxtRect = new Rect();
        mDetectResultPaint.getTextBounds(disTxt, 0, disTxt.length(), disTxtRect);
        int disTxtHeight = disTxtRect.bottom - disTxtRect.top;
        int disTxtWidth = disTxtRect.right - disTxtRect.left;
        float offset = Math.max((disTxtWidth - bitmapWidth) / 2f, 0f) + 30;
        float startX, startY;
        if (isMoveTarget) {
            startX = (mViewRect.right + mViewRect.left) / 2f - bitmapWidth;
            startX -= offset;
        } else {
            startX = (mViewRect.right + mViewRect.left) / 2f;
            startX += offset;
        }
        float animationHeight = mAnimationArea[2].y - mAnimationArea[0].y;
        startY = mAnimationArea[0].y + nSample * animationHeight / scanLength - bitmapHeight / 2f;
        canvas.drawBitmap(targetBMP, startX, startY, null);
        canvas.drawText(disTxt, startX + bitmapWidth / 2f, startY + bitmapHeight + disTxtHeight,
                        mDetectResultPaint);
    }

    private void drawDetectResults(Canvas canvas, boolean isMove) {
        _DetectResult detectResult;
        int index = 0;
        int maxTargets = isMove ? mMaxMoveTargets : mMaxBreathTargets;
        while (index < maxTargets && (detectResult = mDetectResults.getResult(isMove, index)) !=
                                     null) {
            mLastResultBuffer.setLastResult(index, detectResult);
            drawOneResult(canvas, detectResult);
            ++index;
        }
        // 当没有新的探测结果并且探测结果缓存时间有效时，绘制上一次探测结果
        // 防止由于刷新频率过高导致的探测结果一闪而过看不清的问题
        while (index < maxTargets) {
            detectResult = mLastResultBuffer.getLastResult(isMove, index);
            if (detectResult != null)
                drawOneResult(canvas, detectResult);
            ++index;
        }
    }

    private void drawDetectResults(Canvas canvas) {
        drawDetectResults(canvas, true);
        drawDetectResults(canvas, false);
    }
}
