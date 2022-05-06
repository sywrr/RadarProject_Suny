package com.ltd.lifesearchapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import java.util.Arrays;

import Utils.Logcat;

// 雷达数据视图
public class RadarDataView extends RadarSurfaceView {

    private int mScanLength = 8192;

    private RadarRulerView mRulerView;

    public void setRulerView(RadarRulerView rulerView) { mRulerView = rulerView; }

    private int[] mDibPixes = null;

    private static final int oneScanPixes = 819;

    public RadarDataView(Context context) {
        super(context);
        initPaint();
    }

    public RadarDataView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public RadarDataView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    private Paint mPaint;

    @Override
    protected void initView(Context context) {
        super.initView(context);
        mLogger = new Logcat("RadarDataView", true);
        mLogger.debug("init radar data view");
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setStrokeWidth(2);
        mPaint.setTextSize(15);
        mPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mPaint.setColor(Color.BLACK);
    }

    private void initDib() {
        if (mDibPixes != null && mEndScans - mBeginScans > 0)
            Arrays.fill(mDibPixes, 0);
        mDibBitmap = null;
    }

    // 在第一次画图之前调用
    @Override
    protected void doStartDraw() {
        super.doStartDraw();
        mLogger.debug("doStartDraw");
        mDibRect.left = getRelativeX(10);
        mDibRect.top = getRelativeY(0);
        mDibRect.right = getRelativeX(mDibWidth - 10);
        mDibRect.bottom = getRelativeY(819);
        mFirstDraw = true;
        if (mDibPixes == null || oneScanPixes * getMaxScans() != mDibPixes.length) {
            mDibPixes = new int[oneScanPixes * getMaxScans()];
        } else {
            initDib();
        }
    }

    private static class RulerParam {
        public float mPosX;
        public float mPosY;
        public int mLength;
        public float mStartValue;
        public float mEndValue;
        public float mInterval;
        public String mHeadText;
        public boolean mLeftFlag;
        public boolean mVerticalFlag;
        public int mScaleLength;

        public void setPos(float x, float y) {
            mPosX = x;
            mPosY = y;
        }

        public void setLength(int length) {
            mLength = length;
        }

        public void setScaleValues(float start, float end, float interval) {
            mStartValue = start;
            mEndValue = end;
            mInterval = interval;
        }

        public void setHeadText(String text) {
            mHeadText = text;
        }

        public void setLeftFlag(boolean leftFlag) {
            mLeftFlag = leftFlag;
        }

        public void setVertical(boolean isVertical) {
            mVerticalFlag = isVertical;
        }

        public void setScaleLength(int scaleLength) {
            mScaleLength = scaleLength;
        }
    }

    // 绘制伪彩图的深度和时窗标尺
    private void drawVerticalRuler(Canvas canvas, RulerParam param, Paint paint) {
        canvas.drawLine(param.mPosX, param.mPosY, param.mPosX, param.mPosY - param.mLength, paint);
        float startY = param.mPosY - param.mLength;
        int length = param.mScaleLength;
        String text;
        float intervalHeight = param.mLength /
                               ((param.mEndValue - param.mStartValue) / param.mInterval);
        Rect rect = new Rect();
        float y = startY;
        float textX, textY;
        for (float i = param.mStartValue; i <= param.mEndValue; i += param.mInterval) {
            text = String.valueOf(i);
            if (i == param.mStartValue) {
                text += " (" + param.mHeadText + ")";
            }
            paint.getTextBounds(text, 0, text.length(), rect);
            if (!param.mLeftFlag) {
                canvas.drawLine(param.mPosX, y, param.mPosX + length, y, paint);
                textX = param.mPosX + 10 + length;
            } else {
                canvas.drawLine(param.mPosX, y, param.mPosX - length, y, paint);
                textX = param.mPosX - rect.width() - 10 - length;
            }
            textY = y + (float) rect.height() / 2;
            canvas.drawText(text, textX, textY, paint);
            y += intervalHeight;
        }
    }

    private RulerParam getTimeWindowRulerParam(float x, float y, int height) {
        mLogger.debug("x: " + x + ", y: " + y);
        RulerParam param = new RulerParam();
        param.setPos(x, y);
        param.setHeadText("ns");
        param.setLeftFlag(true);
        param.setScaleValues(0, 80, 8);
        param.setVertical(true);
        param.setScaleLength(15);
        param.setLength(height);
        return param;
    }

    private RulerParam getDeepRulerParam(float x, float y, int height) {
        mLogger.debug("x: " + x + ", y: " + y);
        RulerParam param = new RulerParam();
        param.setPos(x, y);
        param.setHeadText("m");
        param.setLeftFlag(false);
        param.setScaleValues(0, 12, 1);
        param.setVertical(true);
        param.setScaleLength(15);
        param.setLength(height);
        return param;
    }

    private final class RulerDrawer extends Drawer {

        volatile boolean mDrawFinished = false;

        @Override
        protected void onDraw(Canvas canvas) {
            drawRulers(canvas);
            mDrawFinished = true;
        }

        @Override
        protected boolean checkDraw() {
            return checkViewStatus();
        }
    }

    private final RulerDrawer mRulerDrawer = new RulerDrawer();

    private boolean firstDraw() {
        mRulerDrawer.mDrawFinished = false;
        mRulerDrawer.draw(null);
        return mRulerDrawer.mDrawFinished;
    }

    private void drawRulers(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        int startX = getRelativeX(0);
        int totalHeight = mDibRect.bottom - mDibRect.top;
        int startY = getRelativeY(totalHeight);
        int dibWidth = mDibWidth;
        drawVerticalRuler(canvas, getTimeWindowRulerParam(startX, startY, totalHeight), mPaint);
        drawVerticalRuler(canvas, getDeepRulerParam(startX + dibWidth, startY, totalHeight),
                          mPaint);
    }

    private final Rect mDibRect = new Rect();

    private boolean mFirstDraw;

    @Override
    protected void doRun() {
        super.doRun();
        if (mFirstDraw) {
            mFirstDraw = !firstDraw();
        } else {
            copyDibRect();
            doDraw(mDirtyRect);
        }
    }

    private void scrollRadarData(int scans) {
        mLogger.debug("scroll scans: " + scans);
        int maxScans = getMaxScans();
        for (int i = 0; i < oneScanPixes; i++) {
            System.arraycopy(mDibPixes, i * maxScans + scans, mDibPixes, i * maxScans,
                             maxScans - scans);
        }
        mEndScans += scans;
        if (mEndScans - mBeginScans > maxScans)
            mBeginScans = mEndScans - maxScans;
        mRulerView.setScans(mBeginScans, mEndScans);
    }

    private colorPalette mColorPal = new colorPalette();

    @Override
    protected void doStop() {
        super.doStop();
        mDibBitmap = null;
        ((MainActivity) mContext).requireExpertFragment().stopUpload();
    }

    private void clearDib() {
        initDib();
        mBeginScans = mEndScans = 0;
        mRulerView.setScans(mBeginScans, mEndScans);
    }

    // 将雷达数据转换成伪彩图
    private void changeDataToDib(short[] buf, int length) {
        int colorNum = mColorPal.getColorNumber();
        int[][] colors = mColorPal.getColors();
        int dataInterval = Math.max(mScanLength / oneScanPixes, 1);
        int perCol = 0xefff / colorNum;
        int dataVal;
        int fillColIndex;
        int scans = length / mScanLength;
        int maxScans = getMaxScans();
        int fillBaseIndex = maxScans - scans;
        int fillIndex;
        for (int i = 0; i < scans; i++) {
            for (int j = 0; j < oneScanPixes; j++) {
                if (j * dataInterval >= mScanLength) {
                    break;
                }
                dataVal = buf[j * dataInterval + i * mScanLength];
                fillColIndex = dataVal / perCol;
                fillColIndex += colorNum / 2;
                if (fillColIndex < 0)
                    fillColIndex = 0;
                else if (fillColIndex >= colorNum)
                    fillColIndex = colorNum - 1;
                int a = 255 << 24;
                int r = colors[fillColIndex][0];
                r <<= 16;
                int g = colors[fillColIndex][1];
                g <<= 8;
                int b = colors[fillColIndex][2];
                int color = a | r | g | b;
                fillIndex = fillBaseIndex + i + j * maxScans;
                mDibPixes[fillIndex] = color;
            }
        }
    }

    // 实际生成的伪彩图位图对象
    // 如果不为空，则直接显示该位图
    private Bitmap mDibBitmap = null;

    private final Rect mDirtyRect = new Rect();

    private void copyDibRect() {
        mDirtyRect.left = mDibRect.left;
        mDirtyRect.top = mDibRect.top;
        mDirtyRect.right = mDibRect.right;
        mDirtyRect.bottom = mDibRect.bottom;
    }

    private void drawDib(Canvas canvas) {
        if (mDibBitmap == null) {
            mDibBitmap = Bitmap.createBitmap(mDibPixes, getMaxScans(), oneScanPixes,
                                             Bitmap.Config.ARGB_8888);
        }
        Rect srcR = new Rect(0, 0, getMaxScans(), Math.min(oneScanPixes, mScanLength));
        canvas.drawBitmap(mDibBitmap, srcR, mDibRect, null);
    }

    @Override
    protected void onSurfaceCreated(SurfaceHolder holder) {
        super.onSurfaceCreated(holder);
        mFirstDraw = true;
        copyDibRect();
        mLockDrawer.draw(mDirtyRect);
        mLogger.debug("on created draw");
        clearDib();
    }

    @Override
    protected void onSurfaceDestroyed(SurfaceHolder holder) {
        super.onSurfaceDestroyed(holder);
//        copyDibRect();
//        mLockDrawer.draw(mDirtyRect);
//        mLogger.debug("on destroyed draw");
    }

    @Override
    protected void onPostLockView() {
        super.onPostLockView();
    }

    @Override
    protected void onUnlockView() {
        super.onUnlockView();
    }

    public void clearView() {
        copyDibRect();
        mLockDrawer.draw(mDirtyRect);
        mLogger.debug("clear view draw");
        clearDib();
    }

    // 从队列中不断获取雷达数据，并转换成伪彩图
    // 绘制完成后会讲雷达数据所用内存进行回收
    @Override
    protected void drawOnCanvas(Canvas canvas) {
        super.drawOnCanvas(canvas);
        canvas.drawColor(Color.WHITE);
        _RadarData radarData = mRadarDataQueue.pop();
        if (radarData != null) {
            if (mScanLength != radarData.scanLength()) {
                mLogger.debug("scan length changed: " + radarData.scanLength());
                mScanLength = radarData.scanLength();
                clearDib();
            }
            mLogger.debug("size: " + radarData.size() + ", scanLength: " + mScanLength);
            mDibBitmap = null;
            scrollRadarData(radarData.size() / mScanLength);
            changeDataToDib(radarData.data(), radarData.size());
            mRadarDataPool.recycle(radarData);
            mRadarDataPool.shrink(10, 5);
        }
        drawDib(canvas);
    }

    private RadarDataQueue mRadarDataQueue;

    private _RadarDataPool mRadarDataPool;

    public final void setRadarDataQueue(RadarDataQueue radarDataQueue) {
        mRadarDataQueue = radarDataQueue;
    }

    public final void setRadarDataPool(_RadarDataPool radarDataPool) {
        mRadarDataPool = radarDataPool;
    }
}
