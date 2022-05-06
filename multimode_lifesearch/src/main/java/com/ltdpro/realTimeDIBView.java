package com.ltdpro;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.SurfaceHolder.Callback;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.VelocityTracker;
import android.widget.Toast;

public class realTimeDIBView extends SurfaceView implements Callback, Runnable {
    private String TAG = "realTimeView";
    private GestureDetector detector;
    private myGestureListener gListener;
    private int radarNum = 0;

    // ��ʾ����
    private int DIB_BITMAP = 1;
    private int WIGGLE_BITMAP = 2;
    private int mDIBType = DIB_BITMAP;

    // ���λͼ�Ĵ洢��
    public int mDIBHeight = 512; // 256;//512��Ϊ256hss //2016.6.10
    private int mDIBWidth = 1000;// 800; //hss1280��800; //2016.6.10
    private int[] mDIBPixels = new int[mDIBHeight * mDIBWidth];

    // �����߷�Χ
    private int mTimerangeRulerWidth = 100;
    private int mDeeprangeRulerWidth = 100;
    private boolean mIsShowTimerangeRuler = false;
    private boolean mIsShowDeeprangeRuler = false;

    // ��������
    private float mHorizontalZoom = 1;
    private float mVerticalZoom = 1;
    private Paint mBackposPaint;
    private Bitmap mBitmap;
    private Activity mParentActivity;
    private VelocityTracker mVelocityTracker01;

    // ���Ķ��߳�
    SurfaceHolder mSurfaceHolder = null;
    private boolean mLoop = false;// ѭ������

    // ���캯��
    public realTimeDIBView(Context context) {
        super(context);
        initData(context);
        DebugUtil.i(TAG, "enter realTime(Context context)");
        mSurfaceHolder = this.getHolder();
        mSurfaceHolder.addCallback(this);
        this.setFocusable(false);
        mLoop = true;
    }

    // ������ͼ�߳�
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO ������ͼ�߳�
        DebugUtil.i(TAG, "enter surfaceCreated!");
        // Canvas c = holder.lockCanvas();
        // c.drawColor(0xe1e1e1);
        new Thread(this).start();
        mLoop = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub
        // DebugUtil.i(TAG,"surfaceChanged!");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        mLoop = false;
        // DebugUtil.i(TAG,"surfaceDestroyed!");
    }

    public void setRadarNum(int inputRadarNum) {
        this.radarNum = inputRadarNum;
    }

    /**
     * Constructor
     */
    public realTimeDIBView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initData(context);
    }

    /**
     * Constructor
     */
    public realTimeDIBView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData(context);
        DebugUtil.i(TAG, "enter realTime(Context context)");
        mSurfaceHolder = this.getHolder();
        mSurfaceHolder.addCallback(this);
        this.setFocusable(false);
        mLoop = true;
    }

    public void initData(Context context) {
        gListener = new myGestureListener();
        detector = new GestureDetector(context, gListener);
        gListener.setContext(context);

        int i, j;
        for (i = 0; i < mDIBHeight; i++)
            for (j = 0; j < mDIBWidth; j++)
                mDIBPixels[i * mDIBWidth + j] = 0;

        mBackposPaint = new Paint();
        mBackposPaint.setColor(Color.WHITE);
        mBackposPaint.setStrokeWidth(2);

        mRulerWidePaint = new Paint();
        mRulerWidePaint.setColor(Color.BLACK);
        mRulerWidePaint.setStrokeWidth(3);

        mRulerThinPaint = new Paint();
        mRulerThinPaint.setColor(Color.BLACK);
        mRulerThinPaint.setStrokeWidth(1);

        mRulerTextPaint = new Paint();
        mRulerTextPaint.setColor(Color.WHITE);
        mRulerTextPaint.setStrokeWidth(3);
        mRulerTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    public void setParentActivity(Activity parent) {
        mParentActivity = parent;
    }

    // �Զ���GestureListener��
    public class myGestureListener
            implements GestureDetector.OnGestureListener, OnDoubleTapListener {
        Context mContext;

        @Override
        public boolean onDown(MotionEvent e) {
            // DebugUtil.i(TAG,"onDown");
            // �ø����ڼ�¼�µ���λ��
            // LTDMainActivity activity;
            // activity = (LTDMainActivity)mParentActivity;
            // activity.markDownPos(e);
            return true;
            // return false;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            DebugUtil.i(TAG, "onSingleTapUp");
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            DebugUtil.i(TAG, "onScroll");
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            DebugUtil.i(TAG, "onFling");
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            DebugUtil.i(TAG, "onLongPress");
            // ��ͼ���
            // ((LTDMainActivity)mParentActivity).maxRealtimeView();
        }

        @Override
        public void onShowPress(MotionEvent e) {
            DebugUtil.i(TAG, "onShowPress");
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // TODO Auto-generated method stub
            DebugUtil.i(TAG, "onDoubleTap---->����˫���¼�");
            // //���ݵ��λ�ã����б�Ǵ���
            int yPos;
            yPos = (int) e.getY();
            MyApplication app = (MyApplication) mContext.getApplicationContext();
            int height = app.getScreenHeight();
            if (yPos > height / 2) {
                // //��С��
                app.mRadarDevice.smallMark();
            }
            if (yPos < height / 2) {
                // //����
                app.mRadarDevice.bigMark();
            }
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            // TODO Auto-generated method stub
            DebugUtil.i(TAG, "onDoubleTapEvent----->˫���¼��е��¼���Ӧ");
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // TODO Auto-generated method stub
            DebugUtil.i(TAG, "onSingleTapConfirmed----->˫���¼�������ɵ����¼�");
            return true;
        }

        //
        public void setContext(Context context) {
            mContext = context;
        }
    }

    public void changeDIBType() {
        MyApplication app;
        app = (MyApplication) this.getContext().getApplicationContext();
        // HRulerView rulerView;
        // rulerView =
        // (HRulerView)(mParentActivity.findViewById(R.id.layoutHRuler));
        if (mDIBType == DIB_BITMAP) {
            mDIBType = WIGGLE_BITMAP;
            Toast.makeText(this.getContext(), "����ѻ�ͼ��ʾģʽ", Toast.LENGTH_SHORT).show();
            // ����ˮƽ��߲���
            // rulerView.setPixsPerScan(app.getWigglePixsPerScan());
        } else {
            mDIBType = DIB_BITMAP;
            Toast.makeText(this.getContext(), "�����ɫͼ��ʾģʽ", Toast.LENGTH_SHORT).show();
            // ����ˮƽ��߲���
            // rulerView.setPixsPerScan(1);
        }
    }

    public void setShowType_WIGGLE() {
        mDIBType = WIGGLE_BITMAP;
    }

    public void setShowType_DIB() {
        mDIBType = DIB_BITMAP;
    }

    public boolean isShowDIB() {
        return mDIBType == DIB_BITMAP;
    }

    public boolean isShowWiggle() {
        return mDIBType == WIGGLE_BITMAP;
    }

    // //��㴥�ش���
    float[] mDownPointsX = new float[3];
    float[] mDownPointsY = new float[3];
    float[] mNowPointsX = new float[3];
    float[] mNowPointsY = new float[3];
    public int mXZoom = 1; // x�����ϵ�����ϵ��
    public int mYZoom = 1; // y�����ϵ�����ϵ��
    // ˮƽ�������
    public float mHorRulerXPos = 0;
    public float mHorRulerYPos = 0;

    // ��ֱ�������
    public float mVerRulerXPos = 640;
    public float mVerRulerYPos = 0;
    public int mVerRulerLeftSpace = 2;
    public int mVerRulerWidth = 60;

    // ��x������ѹ��ͼ��
    public void zoomInX() {
        DebugUtil.i(TAG, "zoomInX");
        mXZoom = mXZoom + 1;
        String txt;
        txt = "x�������ű���:=" + mXZoom;
        //
        // HRulerView hRuler;
        // hRuler = (HRulerView)mParentActivity.findViewById(R.id.layoutHRuler);
        // hRuler.setZoomX(mXZoom);
        Toast.makeText(this.getContext(), txt, Toast.LENGTH_SHORT).show();
    }

    // ��x�����ϷŴ�ͼ��
    public void zoomOutX() {
        DebugUtil.i(TAG, "zoomOutX");
        if (mXZoom > 1)
            mXZoom = mXZoom - 1;
        //
        // HRulerView hRuler;
        // hRuler = (HRulerView)mParentActivity.findViewById(R.id.layoutHRuler);
        // hRuler.setZoomX(mXZoom);
        //
        String txt;
        txt = "x�������ű���:=" + mXZoom;
        Toast.makeText(this.getContext(), txt, Toast.LENGTH_SHORT).show();
    }

    // ��y������ѹ��ͼ��
    public void zoomInY() {
        DebugUtil.i(TAG, "zoomInY");
    }

    // ��y�����ϷŴ�ͼ��
    public void zoomOutY() {
        DebugUtil.i(TAG, "zoomOutY");
    }

    // �������κ���
    /*
     * @Override protected void onDraw(Canvas canvas) { //��ʱ���� long startDrawDIB
     * = System.currentTimeMillis(); //����dibλͼ drawDIB(canvas); long endDrawDIB
     * = System.currentTimeMillis() - startDrawDIB;
     * //DebugUtil.i("IDSC2600MainActivity", "2.drawDIB time="+
     * String.valueOf(endDrawDIB));
     *
     * //���������� drawBackpos(canvas); }
     */

    // ����λͼ
    public void draw() {
        DebugUtil.i(TAG, "ScanView onDraw");
        Canvas canvas = mSurfaceHolder.lockCanvas();

        // long start = System.currentTimeMillis();

        if (mSurfaceHolder == null || canvas == null) {
            return;
        } else
            ;

        // ��ʱ����
        long startDrawDIB = System.currentTimeMillis();

        // ����dibλͼ
        drawDIB(canvas);

        long endDrawDIB = System.currentTimeMillis() - startDrawDIB;
        DebugUtil.i("IDSC2600MainActivity", "2.drawDIB time=" + String.valueOf(endDrawDIB));

        // ����������
        drawBackpos(canvas);

        // long end = System.currentTimeMillis();
        mSurfaceHolder.unlockCanvasAndPost(canvas);
        // DebugUtil.i(TAG, "3.scanView time="+String.valueOf(end - start));
    }

    // ��������
    public void drawBackpos(Canvas canvas) {
        // DebugUtil.i(TAG,"drawbackpos");
        MyApplication app;
        app = (MyApplication) this.getContext().getApplicationContext();
        /*
         * if(!app.mRadarDevice.isBackOrientMode()) return;
         */
        //
        int pixsPerScan = 1;
        int backPos = 0;
        backPos = app.mRadarDevice.getFillposCursor();

        if (backPos == 0) {
            // DebugUtil.i(TAG,"backPos:= 0");
            return;
        } else {
            DebugUtil.i(TAG, "backPos:=" + backPos);
        }

        int srcW = 0;
        srcW = this.getWidth();
        if (isShowWiggle()) {
            pixsPerScan = app.getWigglePixsPerScan();
        }
        int xPos;
        int yPos;
        int xOrgPos, yOrgPos;
        xOrgPos = srcW - backPos * pixsPerScan / mXZoom;
        // DebugUtil.i(TAG, "xOrgPos="+xOrgPos);
        yOrgPos = 0;
        xPos = xOrgPos;
        yPos = this.getHeight();

        int colNumber; // ��ɫ����ɫ��
        int[][] mColPal;
        int palIndex;
        palIndex = app.mColorPal.getColpalIndex();
        colNumber = app.mColorPal.getColorNumber();
        mColPal = app.mColorPal.getColors();
        int col = 0;
        int r, g, b, a;
        a = 255 << 24;
        r = mColPal[0][0];
        r = r << 16;
        g = mColPal[0][1];
        g = g << 8;
        b = mColPal[0][2];
        col = a | r | g | b;

        mBackposPaint.setColor(col);
        canvas.drawLine(xOrgPos, yOrgPos, xPos, yPos, mBackposPaint);
    }

    public int getWid() {
        return this.getWidth();
    }

    // ���ñ�߷�Χ
    public void setRulerRect() {
        int w, h;
        w = this.getWidth();
        h = this.getHeight();
        mVerRulerXPos = w / 2;
        mVerRulerYPos = 0;
        mVerRulerWidth = 60;
    }

    // �õ���ֱ��ߵ���ʾ��Χ
    public Rect getVerRulerRect(Canvas canvas) {
        Rect r = new Rect(0, 0, 0, 0);
        r.left = (int) mVerRulerXPos;
        r.right = r.left + mVerRulerWidth;
        r.top = (int) mVerRulerYPos;
        r.bottom = this.getHeight();

        //
        return r;
    }

    // //
    Paint mRulerWidePaint; // �������õ�paint;
    Paint mRulerThinPaint;
    Paint mRulerTextPaint;

    //
    public void setHorRulerPos(float xPos, float yPos) {
        mHorRulerXPos = xPos;
        mHorRulerYPos = yPos;
    }

    //
    public void setVerRulerPos(float xPos, float yPos) {
        mVerRulerXPos = xPos;
        mVerRulerYPos = yPos;
    }

    // ��ͼ
    public void drawDIB(Canvas canvas) {
        Bitmap bitmap = Bitmap.createBitmap(mDIBPixels, mDIBWidth, mDIBHeight,
                                            Bitmap.Config.ARGB_8888);
        Rect srcR, dstR;
        srcR = new Rect(0, 0, 0, 0);
        dstR = new Rect(0, 0, 0, 0);
        dstR = getDibShowRect();

        srcR.right = mDIBWidth;
        srcR.left = mDIBWidth - dstR.width();
        if (srcR.left < 0)
            srcR.left = 0;
        srcR.top = 0;
        srcR.bottom = mDIBHeight;

        MyApplication app;
        app = (MyApplication) (getContext().getApplicationContext());
        int scanL;
        scanL = app.mRadarDevice.getScanLength();
        if (scanL < mDIBHeight)
            srcR.bottom = scanL;

        canvas.drawBitmap(bitmap, srcR, dstR, null);

        bitmap.recycle();
        bitmap = null;

        // DebugUtil.i(TAG,
        // "srcR:="+srcR.left+";"+srcR.right+";"+srcR.top+";"+srcR.bottom);
        // DebugUtil.i(TAG,"dstR:="+dstR.left+";"+dstR.right+";"+dstR.top+";"+dstR.bottom);
    }

    // �õ�dibͼ��ʾʹ�õľ��η�Χ
    public Rect getDibShowRect() {
        Rect r = new Rect(0, 0, 0, 0);
        int width, height;
        width = getWidth();
        height = getHeight();
        r.right = width;
        r.bottom = height;
        return r;
    }

    long mBefDownTime = 0;

    // public MyViewGroup mViewGroup = null;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        DebugUtil.i(TAG, "****onTouchEvent****");

        // ��㴥��:��ʱ�����㣬������ɺ������´�����¼�
        if (event.getPointerCount() > 1) {
            DebugUtil.i(TAG, "****manageManyPoints***");
            return true;
        }

        // ��⴦��:������ڵ�⣬���д���󣬲������´��ݸô����¼�
        MyApplication app;
        app = (MyApplication) getContext().getApplicationContext();
        boolean isLock;
        isLock = app.isScreenLock();
        boolean isDianceMode;
        isDianceMode = app.mRadarDevice.isDianCeMode();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isDianceMode && isLock) {
                app.mRadarDevice.onceDianCe();
                return true;
            }
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (isDianceMode) {
                return true;
            }
        }

        // ����Ƿ���һ��˫���¼�������ǣ���ʱ������ͼ
        if (!isLock) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                long time = System.currentTimeMillis();
                // DebugUtil.i(TAG,"Now Time:="+time);
                if (time - mBefDownTime <= 1000) {
                    float xPos;
                    xPos = event.getX();
                    int w;
                    w = this.getWidth();
                    if (xPos <= w && xPos >= w - 200) {
                        // if(!((LTDMainActivity)mParentActivity).isLockOperater())
                        // mViewGroup.snapToScreen(2);
                    }
                    if (xPos >= 0 && xPos <= 100) {
                        // if(!((LTDMainActivity)mParentActivity).isLockOperater())
                        // mViewGroup.snapToScreen(0);
                    }
                    return true;
                }
                mBefDownTime = time;
            }
        }

        // ���û����������ʱ����false,�����¼�������ȥ,�ù�����ͼ(MyViewGroup)��ͼȥ������
        if (!isLock)
            return false;

        // ��������Ļ������½��н�һ������
        if (detector.onTouchEvent(event)) {
            return true;
        }

        // ///��������
        if (mVelocityTracker01 == null) {
            DebugUtil.e(TAG, "mVelocity=null");
            // ʵ��flinging�¼���ͨ��obtain()ȡ���µ�trackingʵ��
            mVelocityTracker01 = VelocityTracker.obtain();
        }
        // ��User���ص�MotionEvent����Tracker
        mVelocityTracker01.addMovement(event);
        // /�ж�User��onTouchEvent��Ļ�����¼�
        int mFlingVelocity = 10;
        int scrW = app.getScreenWidth();
        switch (event.getAction()) {
            // ��ָ̧��
            case MotionEvent.ACTION_UP:
                DebugUtil.e(TAG, "onTouchEvent:ACTION_UP");
                // ����ָ�뿪��Ļ�¼�����
                // ��¼��mVelocityTracker01�ļ�¼����ȡ��X�Ử���ٶ�
                VelocityTracker velocityTracker = mVelocityTracker01;
                velocityTracker.computeCurrentVelocity(mFlingVelocity);
                float velocityX = velocityTracker.getXVelocity();
                DebugUtil.e(TAG, "VelocityX:=" + velocityX);
                // DebugUtil.e(TAG,"xPos:="+event.getX());
                // ���ݻ����ٶȣ������Ƿ�������ж�
                if (Math.abs(velocityX) >= mFlingVelocity) {
                    {
                        Toast.makeText(mParentActivity, "���Ƚ��н�������!", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                if (mVelocityTracker01 != null) {
                    mVelocityTracker01.recycle();
                    mVelocityTracker01 = null;
                }
                break;
        }
        // ���û�д�����ʱ����false����ϵͳ��������
        return false;
    }

    // ����λͼ
    public void ScrollDIB(int scans) {
        if (scans > 0) {
            for (int i = 0; i < mDIBHeight; i++) {
                System.arraycopy(mDIBPixels, i * mDIBWidth + scans, mDIBPixels, i * mDIBWidth,
                                 (i + 1) * mDIBWidth);
            }
        }
    }

    public void initDIB() {
        // DebugUtil.i("realTimeDIBView", "initDIB");
        for (int i = 0; i < mDIBHeight; i++) {
            for (int j = 0; j < mDIBWidth; j++) {
                mDIBPixels[i * mDIBWidth + j] = 0x0;
            }
        }
    }

    public int NO_MARK = 0; // û�б��
    public int BIG_MARK_FLAG = 1;
    public int SMALL_MARK_FLAG = 2;
    public int BIG_MARK_VAL = 0x4000; // ���(����)
    public int SMALL_MARK_VAL = 0x8000; // С��(����)
    public int mMarkType = NO_MARK; // �������

    // //������ת���� "�ѻ�" ͼ
    public void changeDatasToWiggle(short[] buf, int length) {
        MyApplication app;
        app = (MyApplication) (getContext().getApplicationContext());
        int mScanLen;
        mScanLen = app.mRadarDevice.getScanLength();
        int pixsPerScan;
        pixsPerScan = app.getWigglePixsPerScan();

        // ���������ܵ���
        int scans;
        scans = length / 2;
        scans = scans / mScanLen;
        // ����ˮƽ�����ϵ�����
        scans = scans / mXZoom;

        // ����λͼ����
        int canSrollScans = mDIBWidth / pixsPerScan;
        if (scans > canSrollScans)
            scans = canSrollScans;
        //
        int srollScans;
        int fillPosOff;
        int j;
        int fillIndex;
        fillPosOff = app.mRadarDevice.getFillposCursor();
        srollScans = scans - fillPosOff;
        if (srollScans <= 0)
            srollScans = 0;
        srollScans = srollScans * pixsPerScan;
        ScrollDIB(srollScans);
        // ��չ�������
        int baseIndex = mDIBWidth - srollScans;
        // DebugUtil.i(TAG,"baseIndex:="+baseIndex+"srollScans:="+srollScans+";scans:="+scans);
        for (int m = baseIndex; m < mDIBWidth; m++) {
            for (j = 0; j < mDIBHeight; j++) {
                // DebugUtil.i(TAG,"baseIndex:="+baseIndex+";j="+j+";m="+m);
                // DebugUtil.i(TAG,"fillIndex:="+(m+(j*mDIBWidth)));
                fillIndex = m + (j * mDIBWidth);
                mDIBPixels[fillIndex] = 0;
            }
        }
        // ת��λͼ
        int colNumber; // ��ɫ����ɫ��
        int[][] mColPal;
        int palIndex;
        palIndex = app.mColorPal.getColpalIndex();
        colNumber = app.mColorPal.getColorNumber();
        mColPal = app.mColorPal.getColors();

        int backCol = 0;
        int r, g, b, a;
        a = 255 << 24;
        r = 0;// mColPal[0][0];
        r = r << 16;

        g = 0;// mColPal[0][1];
        g = g << 8;
        b = 0;// mColPal[0][2];
        backCol = a | r | g | b;

        int fillCol = 0;
        a = 255 << 24;
        r = 0;// mColPal[colNumber-1][0];
        r = r << 16;

        g = 0;// mColPal[colNumber-1][1];
        g = g << 8;
        b = 0;// mColPal[colNumber-1][2];
        fillCol = a | r | g | b;

        //
        short dataVal;
        int dataInter;
        double perCol;
        perCol = 0xefff / pixsPerScan / 2;
        dataInter = mScanLen / mDIBHeight;
        if (dataInter < 1)
            dataInter = 1;
        int fillColIndex;
        int fillBaseIndex;
        // ȷ�������ʼλ��
        try {
            if (fillPosOff >= scans) {
                fillBaseIndex = mDIBWidth - fillPosOff * pixsPerScan;
            } else {
                fillBaseIndex = mDIBWidth - scans * pixsPerScan;
            }
            if (fillBaseIndex < 0) {
                Exception fillBaseIndexExcept = new Exception("fillBaseIndexΪ����=" + fillBaseIndex);
                throw fillBaseIndexExcept;
            }

            int col;
            short flagVal;
            double temVal;
            short[] scanDatas = new short[mScanLen];
            int guard1, guard2;
            for (int i = 0; i < scans; i++) {
                mMarkType = NO_MARK;
                // �������Ŵ���
                flagVal = 0;
                for (j = 0; j < mScanLen; j++) {
                    temVal = 0;
                    scanDatas[j] = 0;
                    for (int m = 0; m < mXZoom; m++) {
                        temVal += buf[(i * mXZoom + m) * mScanLen + j];
                        flagVal |= (buf[(i * mXZoom + m) * mScanLen + 1]);
                    }
                    scanDatas[j] = (short) (temVal / mXZoom);
                }
                // ����ж�
                if ((flagVal & BIG_MARK_VAL) != 0)
                    mMarkType = BIG_MARK_FLAG;
                else if ((flagVal & SMALL_MARK_VAL) != 0)
                    mMarkType = SMALL_MARK_FLAG;
                //
                for (j = 0; j < mDIBHeight; j++) {
                    //
                    guard1 = 0 + j * mDIBWidth;
                    guard2 = guard1 + mDIBWidth;
                    //
                    int temFillIndex = (fillBaseIndex + i * pixsPerScan) + (j * mDIBWidth);
                    if (j * dataInter < mScanLen) {
                        // ��Ǵ���
                        dataVal = scanDatas[j * dataInter];
                        if (j == 0)
                            dataVal = 0;
                        if (mMarkType == BIG_MARK_FLAG)
                            dataVal = 0x7fff;
                        if (mMarkType == SMALL_MARK_FLAG) {
                            if (j * dataInter <= mScanLen / 2)
                                dataVal = 0x7fff;
                        }
                        // /����Ҫ���ļ���
                        int fillNumber;
                        fillNumber = (int) (dataVal * 2 / perCol);
                        if (fillNumber <= 0) {
                            fillIndex = temFillIndex + fillNumber;
                            if (fillIndex < guard1)
                                fillIndex = guard1;
                            //
                            mDIBPixels[fillIndex] = backCol;
                        } else {
                            for (int kk = 0; kk < fillNumber; kk++) {
                                fillIndex = temFillIndex + kk;
                                if (fillIndex >= guard2)
                                    fillIndex = guard2 - 1;
                                //
                                mDIBPixels[fillIndex] = fillCol;
                            }
                        }
                    }
                }
            }

            app.mRadarDevice.delFillposCursor(scans);
        } catch (Exception e) {
            DebugUtil.e(TAG, e.getMessage());
        }
    }

    public int FLAG_INDEX = 1;

    // //������ת����dibλͼ,ͼ�񲻶�1103
    public void changeDatasToDIB(short[] buf, int length) {
        // DebugUtil.i(TAG, "changeDatasToDIB!");
        long startDIB = System.currentTimeMillis();// ��ǰʱ���Ӧ�ĺ���

        MyApplication app;
        app = (MyApplication) (getContext().getApplicationContext());

        int mScanLen;
        mScanLen = app.mRadarDevice.getScanLength();

        // ���
        if (length > mDIBWidth * mScanLen * 2) {
            length = mDIBWidth * mScanLen * 2;
        } else
            ;

        // ���������ܵ���
        int scans;
        scans = length / 2 / mScanLen;

        // DebugUtil.i(TAG, "changeDatasToDIB scans="+String.valueOf(scans));
        // for(int i=0;i<10;i++)
        // DebugUtil.i("realTimeDIB","buf[" + i + "]=" +
        // String.valueOf(buf[i]));
        // ����ˮƽ�����ϵ�����

        // ����λͼ����
        int srollScans = 0;
        int fillPosOff = 0;

        fillPosOff = app.mRadarDevice.getFillposCursor();
        // DebugUtil.i(TAG, "changetodibview fillposcursor=" + fillPosOff);

        srollScans = scans - fillPosOff;
        if (srollScans <= 0)
            srollScans = 0;

        ScrollDIB(srollScans);// hss

        // ת��λͼ
        int colNumber; // ��ɫ����ɫ��
        int[][] mColPal;
        int palIndex;
        palIndex = app.mColorPal.getColpalIndex();
        colNumber = app.mColorPal.getColorNumber();
        mColPal = app.mColorPal.getColors();

        short dataVal;
        int dataInter;
        double perCol;
        perCol = 0xefff / colNumber;
        dataInter = mScanLen / mDIBHeight;
        if (dataInter < 1)
            dataInter = 1;
        int fillColIndex;

        // ȷ�����λ��
        int fillBaseIndex;
        if (fillPosOff >= scans) {
            fillBaseIndex = mDIBWidth - fillPosOff;
        } else {
            fillBaseIndex = mDIBWidth - scans;
        }
        // DebugUtil.i(TAG,"fillBaseIndex:="+fillBaseIndex);

        int fillIndex;
        int col;
        short flagVal;
        double temVal;
        int j;
        short[] scanDatas = new short[mScanLen];
        // DebugUtil.i(TAG,"scans:="+scans);

        long end1 = System.currentTimeMillis();// ��ǰʱ���Ӧ�ĺ���
        // DebugUtil.i(TAG, "1.end1="+String.valueOf(end1-startDIB));

        for (int i = 0; i < scans; i++) {
            // DebugUtil.i(TAG,"i:="+i);
            mMarkType = NO_MARK;
            // �������Ŵ���
            flagVal = 0;
            for (j = 0; j < mScanLen; j++) {
                temVal = 0;
                scanDatas[j] = 0;
                for (int m = 0; m < mXZoom; m++) {
                    temVal += buf[(i * mXZoom + m) * mScanLen + j];
                    flagVal |= (buf[(i * mXZoom + m) * mScanLen + 1]);
                }
                scanDatas[j] = (short) (temVal / mXZoom);
                // if( j == mScanLen - 2 );
                // DebugUtil.i("hi","hi");
            }

            flagVal = scanDatas[FLAG_INDEX];
            // ����ж�hss20141230
            if ((flagVal & BIG_MARK_VAL) != 0)
                mMarkType = BIG_MARK_FLAG;
            if ((flagVal & SMALL_MARK_VAL) != 0)
                mMarkType = SMALL_MARK_FLAG;

            for (j = 0; j < mDIBHeight; j++) {
                if (j * dataInter >= mScanLen) {
                    fillIndex = (fillBaseIndex + i) + (j * mDIBWidth);
                    // DebugUtil.i(TAG,"fillIndex:="+fillIndex);
                    mDIBPixels[fillIndex] = 0;
                } else {
                    // ��Ǵ���
                    dataVal = scanDatas[j * dataInter];
                    if (j == 0)
                        dataVal = 0;

                    if (mMarkType == BIG_MARK_FLAG)
                        dataVal = 0x7fff;
                    if (mMarkType == SMALL_MARK_FLAG) {
                        if (j * dataInter <= mScanLen / 2)
                            dataVal = 0x7fff;
                    }

                    fillColIndex = (int) (dataVal / perCol);
                    fillColIndex += colNumber / 2;

                    if (fillColIndex < 0)
                        fillColIndex = 0;
                    if (fillColIndex >= colNumber)
                        fillColIndex = colNumber - 1;

                    col = 0;
                    int r, g, b, a;
                    a = 255 << 24;
                    r = mColPal[fillColIndex][0];
                    r = r << 16;

                    g = mColPal[fillColIndex][1];
                    g = g << 8;
                    b = mColPal[fillColIndex][2];
                    col = a | r | g | b;
                    //
                    fillIndex = (fillBaseIndex + i) + (j * mDIBWidth);
                    // D ebugUtil.i(TAG,"fillIndex_1:="+fillIndex);

                    mDIBPixels[fillIndex] = col;
                    // DebugUtil.i(TAG, "mDIBPixels["+fillIndex+"]="+col);
                }
            }
        }
        end1 = System.currentTimeMillis();// ��ǰʱ���Ӧ�ĺ���
        // DebugUtil.i(TAG, "2.end="+String.valueOf(end1-startDIB));
        app.mRadarDevice.delFillposCursor(scans);
        long endChangeDIB = System.currentTimeMillis() - startDIB;
        draw();
        // DebugUtil.i(TAG,"get Scans="+String.valueOf(scans));
        // DebugUtil.i(TAG,
        // "changeDIB use time ="+String.valueOf(endChangeDIB));
    }

    // ��ʼ��ͼ
    @Override
    public void run() {
        while (mLoop) {
            DebugUtil.i(TAG, "enter realTimeDIBView!");
            try {
                Thread.sleep(100);

            } catch (Exception e) {
                DebugUtil.e(TAG, "run thread sleep error!");
            }

            synchronized (mSurfaceHolder) {
                // draw();
            }
        }
    }
}
