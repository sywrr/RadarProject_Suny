package com.ltd.lifesearchapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;

import Utils.Logcat;

public class RadarRulerView extends RadarSurfaceView {

    public RadarRulerView(Context context) {
        super(context);
        initPaint();
    }

    public RadarRulerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public RadarRulerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    @Override
    protected void initView(Context context) {
        super.initView(context);
        mLogger = new Logcat("RadarRulerView", true);
    }

    private void initPaint() {
        mLargeScalePaint = new Paint();
        mSmallScalePaint = new Paint();
        mLargeScalePaint.setColor(Color.BLACK);
        mLargeScalePaint.setStrokeWidth(2);
        mSmallScalePaint.setColor(Color.BLACK);
        mSmallScalePaint.setStrokeWidth(1);
    }

    @Override
    protected void doRun() {
        super.doRun();
        doDraw(null);
    }

    public void setScans(int beginScans, int endScans) {
        mBeginScans = beginScans;
        mEndScans = endScans;
    }

    private Paint mLargeScalePaint;
    private Paint mSmallScalePaint;

    private void drawScaleAndText(Canvas canvas, int x, int y, int scaleHeight, int scans,
                                  Paint paint) {
        int startY = y - scaleHeight;
        canvas.drawLine(x, startY, x, y, paint);
        String text = String.valueOf(scans);
        Rect rect = new Rect();
        paint.setTextSize(15);
        Typeface typeface = Typeface.create("ËÎÌו", Typeface.BOLD);
        paint.setTypeface(typeface);
        paint.getTextBounds(text, 0, text.length(), rect);
        float textX = x - (float) rect.width() / 2;
        float textY = y + (float) rect.height() / 2 + 10;
        canvas.drawText(text, textX, textY, paint);
    }

    private void drawLargeScaleAndText(Canvas canvas, int x, int y, int scans) {
        drawScaleAndText(canvas, x, y, 20, scans, mLargeScalePaint);
    }

    private void drawSmallScaleAndText(Canvas canvas, int x, int y, int scans) {
        drawScaleAndText(canvas, x, y, 10, scans, mSmallScalePaint);
    }

    @Override
    protected void doStartDraw() {
        super.doStartDraw();
    }

    private int getNextScaleScans(int scans) {
        if (scans == 0)
            return 0;
        return ((scans - 1) / mScansPerScale + 1) * mScansPerScale;
    }

    @Override
    protected void doStop() {
        super.doStop();
        mDrawThread.interrupt();
    }

    @Override
    public void clearView() {
        super.clearView();
        mLockDrawer.draw(null);
        mLogger.debug("clear view draw");
    }

    @Override
    protected void onPostLockView() {
        super.onPostLockView();
    }

    private void drawRuler(Canvas canvas) {
        int y = getRelativeY(mHeight - 30);
        Point endPoint = new Point(getRelativeX(10) + getMaxScans() * mPixesPerScans, y);
        int totalScans = mEndScans - mBeginScans;
        int startX = endPoint.x - (totalScans - 1) * mPixesPerScans - 1;
        int x;
        int startScans = getNextScaleScans(mBeginScans);
        int scans;
        boolean large;
        for (int i = 0; i < mPixesPerScans; ++i) {
            canvas.drawColor(Color.WHITE);
            canvas.drawLine(startX, y, endPoint.x, y, mLargeScalePaint);
            scans = startScans;
            large = (scans % (2 * mScansPerScale) == 0);
            while (scans <= mEndScans) {
                x = startX + (scans - mBeginScans) * mPixesPerScans;
                if (large) {
                    drawLargeScaleAndText(canvas, x, y, scans);
                } else {
                    drawSmallScaleAndText(canvas, x, y, scans);
                }
                scans += mScansPerScale;
                large = !large;
            }
            --startX;
        }
    }

    @Override
    protected void drawOnCanvas(Canvas canvas) {
        super.drawOnCanvas(canvas);
        drawRuler(canvas);
    }

    @Override
    protected void doDraw(Rect rect) {
        long st = System.nanoTime();
        super.doDraw(rect);
        if (mRunning && !mDestroyed) {
            long costTime = System.nanoTime() - st;
            DrawSleep(costTime);
        }
    }
}
