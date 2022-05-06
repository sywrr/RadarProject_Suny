package com.ltd.lifesearch_xa;

import java.text.DecimalFormat;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.ViewGroup.MarginLayoutParams;

public class realTimeDIBView extends View
{
	private String TAG="realTimeView";
	private GestureDetector detector;
	private final static int MARGIN = 2;
	//��ʾ����
	private int DIB_BITMAP=1;
	private int WIGGLE_BITMAP=2;
	private int mDIBType = DIB_BITMAP;
	
	//���λͼ�Ĵ洢��
	public int mDIBHeight = 512;
	private int mDIBWidth = 600;     //800;
	private int[] mDIBPixels = new int[mDIBHeight*mDIBWidth];
	
	//�����߷�Χ
	private int mTimerangeRulerWidth = 80;
	private int mDeeprangeRulerWidth = 80;
	private int mSingleScanWidth = 150;
	private boolean mIsShowTimerangeRuler = true;
	private boolean mIsShowDeeprangeRuler = true;
	private boolean mIsShowSingleScanWave = true;
	private int mScaleNum = 10;
	private int mLongscaleLength = 18;
	private int mShortscaleLength = 10;
	
	////
	Paint mRulerWidePaint;          //�������õ�paint;
	Paint mSingleWavePaint;          
	Paint mRulerTextPaint;    
	
	////��㴥�ش���
	float[] mDownPointsX = new float[3];
	float[] mDownPointsY = new float[3];
	float[] mNowPointsX  = new float[3];
	float[] mNowPointsY  = new float[3];
	public int  mXZoom=1;      //x�����ϵ�����ϵ��
	public int  mYZoom=1;      //y�����ϵ�����ϵ��
	//ˮƽ�������
	public float  mHorRulerXPos = 0;
	public float  mHorRulerYPos = 0;
	
	//��ֱ�������
//	public float  mVerRulerXPos = 640;
//	public float  mVerRulerYPos = 0;
//	public int    mVerRulerLeftSpace = 2;
//	public int    mVerRulerWidth = 60;
	
	//��������
	private float mHorizontalZoom = 1;
	private float mVerticalZoom = 1;
	
	//
	private Paint mBackposPaint;
	//
	private Bitmap mBitmap;
	
	//
	private Activity mParentActivity;
	
	//
	private VelocityTracker mVelocityTracker01;
	private colorPalette mColorPal = new colorPalette();
	
	private Context mContext = null;
	
	//���캯��
	public realTimeDIBView(Context context)
	{
		super(context);
		initData(context);
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
            
    }   
    public void initData(Context context)
    {
    	mContext = context;
		
		//
		int i,j;
		for(i=0;i<mDIBHeight;i++)
			for(j=0;j<mDIBWidth;j++)
				mDIBPixels[i*mDIBWidth+j]=0;
		
		//
		mBackposPaint = new Paint();
    	mBackposPaint.setColor(Color.WHITE);
    	mBackposPaint.setStrokeWidth(2);
    	    	
    	//
    	mRulerWidePaint = new Paint();
    	mRulerWidePaint.setColor(Color.BLACK);
    	mRulerWidePaint.setStrokeWidth(3);
    	//
    	mSingleWavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    	mSingleWavePaint.setColor(Color.BLACK);
    	mSingleWavePaint.setStrokeWidth(1);
    	//
    	mRulerTextPaint = new Paint();
    	mRulerTextPaint.setColor(Color.BLACK);
    	mRulerTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
    }
    //
    public void setParentActivity(Activity parent)
    {
    	mParentActivity = parent;
    }
	
	public boolean isShowDIB()
	{
		return mDIBType == DIB_BITMAP;
	}
	public boolean isShowWiggle()
	{
		return mDIBType == WIGGLE_BITMAP;
	}	
	
	//��y������ѹ��ͼ��
	public void zoomInY()
	{
		Log.i(TAG,"zoomInY");
	}
	//��y�����ϷŴ�ͼ��
	public void zoomOutY()
	{
		Log.i(TAG,"zoomOutY");
	}
	//�������κ���
	@Override
	protected void onDraw(Canvas canvas)
	{
		//����ʱ�����
		drawTimerangeRuler(canvas);
		
		//������ȱ��
		drawDeeprangeRuler(canvas);
		
		//������������
		drawSingleScan(canvas);
		
		//����dibλͼ
		drawDIB(canvas);
	}

	//���ñ�߷�Χ
	public void setRulerRect()
	{
		int w,h;
		w = this.getWidth();
		h = this.getHeight();
//		mVerRulerXPos = w/2;
//		mVerRulerYPos = 0;
//		mVerRulerWidth = 60;
	}
	//�õ���ֱ��ߵ���ʾ��Χ
	public Rect getTimeRulerRect(Canvas canvas)
	{
		Rect r = new Rect(0, 0, 0, 0);
		r.left = 0;
		r.top = MARGIN;
		r.right = r.left + mTimerangeRulerWidth;
		r.bottom = this.getHeight() - MARGIN;
		//
		return r;
	}
	
	public Rect getDeepRulerRect(Canvas canvas)
	{
		Rect r = new Rect(0, 0, 0, 0);
		r.left = getWidth() - mDeeprangeRulerWidth - mSingleScanWidth;
		r.top = MARGIN;
		r.right = r.left + mDeeprangeRulerWidth;
		r.bottom = this.getHeight() - MARGIN;
		//
		return r;
	}
	
	public Rect getSingleScanRect(Canvas canvas)
	{
		Rect r = new Rect(0, 0, 0, 0);
		r.left = getWidth() - mSingleScanWidth;
		r.top = MARGIN;
		r.right = r.left + mDeeprangeRulerWidth;
		r.bottom = this.getHeight() - MARGIN;
		//
		return r;
	}
	       
	//����ʱ�����
	public void drawTimerangeRuler(Canvas canvas)
	{
		
		Rect rRuler;
		rRuler = getTimeRulerRect(canvas);
		//////����������
	    float xPos,yPos;
	    float xOrgPos,yOrgPos;
	    //��ʼx,y����
	    xOrgPos = rRuler.right - 3;
	    yOrgPos = rRuler.top;
	    //��ֹx,y����
	    xPos = xOrgPos;
	    yPos = rRuler.bottom;
	    canvas.drawLine(xOrgPos, yOrgPos, xPos, yPos, mRulerWidePaint);
	    
	    ////�õ�ʱ��ֵ
	    int timeWnd = ((LifeSearchActivity)mContext).mRadarDevice.getTimeWnd();
	    int scanLen = ((LifeSearchActivity)mContext).mRadarDevice.getScanLen();
//	    int dibH = app.mRealDibView.mDIBHeight;
	    ////����ÿ���̶ȵĿ̶�ֵ
	    //1:
	    double perScaleVal;    //ÿ���̶ȵ�ֵ
	    double perScalePix;    //ÿ���̶�ռ�õ����ص�
	    boolean  hasAdd = false;   //�Ƿ��в���Ŀ̶�
	    //2:������Ͽ̶�Ҫ�����С�����̶�ֵ
	    int perTime = 1;
	    while(perTime < (timeWnd / mScaleNum))
	    {
	    	perTime += 1;
	    }
	    int height = this.getHeight() - 2 * MARGIN;
	    perScalePix = height / 1.;
	    perScaleVal = perTime / 1.;
	    int scaleNum = (int) (timeWnd / perScaleVal);
	    if(scaleNum * perScaleVal < timeWnd)
	    	hasAdd = true;
	    perScalePix = perScalePix * perScaleVal / ((float)timeWnd);
	    //3:
	    int i;
	    float xPos1, xPos2, yPos1, yPos2;
	    String text;
	    float textWidth;
	    for(i=0; i<=scaleNum; i++)
	    {
	    	xPos1 = xOrgPos;
	    	if(i % 2 == 0)
	    	{
	    		xPos2 = xOrgPos + 2 - mLongscaleLength;
	    	}
	    	else
	    	{
	    		xPos2 = xOrgPos + 2 - mShortscaleLength;
	    	}
	    	yPos1 = (float) (yOrgPos + i * perScalePix);
	    	yPos2 = yPos1;
	    	canvas.drawLine(xPos1, yPos1, xPos2, yPos2, mRulerWidePaint);
	    	
	    	if (i == 0)
			{
	    		yPos2 += 3;
			}
	    	if (i == scaleNum && !hasAdd)
			{
	    		yPos2 -= 3;
			}
	    	
	    	//
	    	if (i % 2 == 0)
			{
	    		mRulerTextPaint.setTextSize(12);
	    		text = "" + (int)(perScaleVal * i);
		    	textWidth = mRulerTextPaint.measureText(text);
		    	xPos2 = xOrgPos - 2 - mLongscaleLength - textWidth;
		    	
		    	canvas.drawText(text, xPos2, yPos2 + 4, mRulerTextPaint);
			}
	    	
	    }
	    
	    if (hasAdd)
		{
	    	text = String.valueOf(timeWnd);
	    	textWidth = mRulerTextPaint.measureText(text);
	    	xPos1 = xOrgPos;
			yPos1 = rRuler.bottom;
			xPos2 = xOrgPos + 2 - mLongscaleLength;
			yPos2 = yPos1;
			
			canvas.drawLine(xPos1, yPos1, xPos2, yPos2, mRulerWidePaint);
			xPos2 = xOrgPos - 2 - mLongscaleLength - textWidth;
			canvas.drawText(text, xPos2, yPos2, mRulerTextPaint);
		}
	    text = "time ruler(ns)";
	    mRulerTextPaint.setTextSize(20);
	    textWidth = mRulerTextPaint.measureText(text);
	    float centerX, centerY;  //������������
	    centerX = mTimerangeRulerWidth / 4 + 4;
	    centerY = height / 2;

	    canvas.translate(centerX, centerY);
	    canvas.rotate(90);
	    canvas.drawText(text, 0 - textWidth / 2, 20 / 2, mRulerTextPaint);
	    canvas.rotate(-90);
	    canvas.translate(-centerX, -centerY);
	}
	//������ȱ��
	public void drawDeeprangeRuler(Canvas canvas)
	{
		Rect rRuler;
		rRuler = getDeepRulerRect(canvas);
		//////����������
	    float xPos,yPos;
	    float xOrgPos,yOrgPos;
	    //��ʼx,y����
	    xOrgPos = rRuler.left + 3;
	    yOrgPos = rRuler.top;
	    //��ֹx,y����
	    xPos = xOrgPos;
	    yPos = rRuler.bottom;
	    canvas.drawLine(xOrgPos, yOrgPos, xPos, yPos, mRulerWidePaint);
	    ////����ÿ���̶ȵĿ̶�ֵ
		//1:
		float perScaleVal;    //ÿ���̶ȵ�ֵ
		float perScalePix;    //ÿ���̶�ռ�õ����ص�
		boolean  hasAdd = false;   //�Ƿ��в���Ŀ̶�
		float deepR;
		int timeWnd;

		timeWnd = ((LifeSearchActivity)mContext).mRadarDevice.getTimeWnd();
	 	deepR = Global.DEFAULT_SCAN_RANGE;//(float)(timeWnd / 2 * 30 / Math.sqrt(10));
	 	short begpos = ((LifeSearchActivity)mContext).mRadarDevice.getBegPos();
		//2:
		float perDeep = 0.1f;
		while(perDeep < (deepR / mScaleNum))
		{
			perDeep += 0.1;
		}
		int height = this.getHeight() - 2 * MARGIN;
		perScalePix = height / 1.f;
		perScaleVal = perDeep/1.f;
		int scaleNum = (int)(deepR / perScaleVal);
		if(scaleNum * perScaleVal < deepR)
			hasAdd = true;
		perScalePix = perScalePix * perScaleVal / ((float)deepR);
		
		int i;
	    float xPos1, xPos2, yPos1, yPos2;
	    String text;
	    float textWidth;
	    for(i=0; i<=scaleNum; i++)
	    {
	    	xPos1 = xOrgPos;
	    	if(i % 2 == 0)
	    	{
	    		xPos2 = xOrgPos + 2 + mLongscaleLength;
	    	}
	    	else
	    	{
	    		xPos2 = xOrgPos + 2 + mShortscaleLength;
	    	}
	    	yPos1 = (float) (yOrgPos + i * perScalePix);
	    	yPos2 = yPos1;
	    	canvas.drawLine(xPos1, yPos1, xPos2, yPos2, mRulerWidePaint);
	    	
	    	if (i == 0)
			{
	    		yPos2 += 3;
			}
	    	if (i == scaleNum && !hasAdd)
			{
	    		yPos2 -= 3;
			}
	    	//
	    	if (i % 2 == 0)
			{
	    		mRulerTextPaint.setTextSize(12);
	    		
	    		DecimalFormat df = new DecimalFormat("0");
	    		
	    		text = df.format(perScaleVal * i + begpos);
		    	textWidth = mRulerTextPaint.measureText(text);
		    	xPos2 = xOrgPos + 4 + mLongscaleLength;
		    	canvas.drawText(text, xPos2, yPos2 + 4, mRulerTextPaint);
			}
	    	
	    }
	    
	    ////��ʾ���һ���̶�ֵ
		xPos1 = xOrgPos;
		yPos1 = rRuler.bottom;
		xPos2 = xOrgPos + 2 + mLongscaleLength;
		yPos2 = yPos1;
		
		canvas.drawLine(xPos1, yPos1, xPos2, yPos2, mRulerWidePaint);
		text = String.valueOf((int)deepR + begpos);
		canvas.drawText(text, xPos2 + 2, yPos2, mRulerTextPaint);

	    text = "depth ruler(cm)";
	    mRulerTextPaint.setTextSize(20);
	    textWidth = mRulerTextPaint.measureText(text);
	    float centerX, centerY;  //������������
	    centerX = xOrgPos + mDeeprangeRulerWidth * 3 / 4;
	    centerY = height / 2;
	    
	    canvas.translate(centerX, centerY);
	    canvas.rotate(90);
	    canvas.drawText(text, 0 - textWidth / 2, 0, mRulerTextPaint);
	    canvas.rotate(-90);
	    canvas.translate(-centerX, -centerY);
	}
	
	private void drawSingleScan(Canvas canvas)
	{
		// TODO Auto-generated method stub
		Rect rSingleScan;
		rSingleScan = getSingleScanRect(canvas);
		//////����������
	    float xPos,yPos;
	    float xOrgPos,yOrgPos;
	    int scanLen = ((LifeSearchActivity)mContext).mRadarDevice.getScanLen();
	    //��ʼx,y����
	    xOrgPos = rSingleScan.left + mSingleScanWidth / 2;
	    yOrgPos = rSingleScan.top;
	    //��ֹx,y����
	    xPos = xOrgPos;
	    yPos = rSingleScan.bottom;
	    canvas.drawLine(xOrgPos, yOrgPos, xPos, yPos, mRulerWidePaint);
	    
	    float xCoeff;
		float yCoeff;
		xCoeff = rSingleScan.width();
		yCoeff = rSingleScan.height();

		xCoeff = xCoeff / (Global.MAXVAL - Global.MINVAL);
		yCoeff = yCoeff / scanLen;

		float xStart, yStart, xStop, yStop;
		short[] dataBuf = ((LifeSearchActivity)mContext).mRadarDevice.getRecentScanDatas();
		
		for(int i=0; i < (scanLen - 1); i++) 
		{
			short val = dataBuf[i];
			xStart = xOrgPos + xCoeff * (val);
			yStart = yOrgPos + yCoeff * i;
			val = dataBuf[i + 1];
			xStop = xOrgPos + xCoeff * val;
			yStop = yOrgPos + yCoeff * (i + 1);
			canvas.drawLine(xStart, yStart, xStop, yStop, mSingleWavePaint);
        }
//		Log.e(TAG, String.valueOf(dataBuf[0]));
	}
	//
	public void setHorRulerPos(float xPos,float yPos)
	{
		mHorRulerXPos = xPos;
		mHorRulerYPos = yPos;
	}
	//
	public void setVerRulerPos(float xPos,float yPos)
	{
//		mVerRulerXPos = xPos;
//		mVerRulerYPos = yPos;
	}
	//
	public void drawDIB(Canvas canvas)
	{
		Bitmap bitmap = Bitmap.createBitmap(mDIBPixels,mDIBWidth,mDIBHeight,Bitmap.Config.ARGB_8888);
		Rect srcR,dstR;
		srcR = new Rect(0,0,0,0);
		dstR = new Rect(0,0,0,0);
		
		//
		dstR = getDibShowRect();
		
		//
		srcR.right=mDIBWidth;
		srcR.left = mDIBWidth-dstR.width();
		if(srcR.left<0)
			srcR.left=0;
		srcR.top=0;
		srcR.bottom=mDIBHeight;
		
		//
		int scanL;
		scanL = ((LifeSearchActivity)mContext).mRadarDevice.getScanLen();
		if(scanL<mDIBHeight)
			srcR.bottom = scanL;
		
		//
		canvas.drawBitmap(bitmap,srcR,dstR,null);
	}
	//�õ�dibͼ��ʾʹ�õľ��η�Χ
	public Rect getDibShowRect()
	{
		Rect r = new Rect(0,0,0,0);
		int width,height;
		width = getWidth();
		height = getHeight();
		r.top = MARGIN;
		r.right = width;
		r.bottom = height - MARGIN;
//		HRulerView hRView = (HRulerView)(((LTDMainActivity)mParentActivity).findViewById(R.id.layoutHRuler));
//		if(hRView.getVisibility() == View.VISIBLE)
//		{
//			r.top = 50;
//		}
//		else
//		{
//			r.top = 0;
//		}
		
		if (mIsShowSingleScanWave)
		{
			r.right -= mSingleScanWidth;
		}
		if(mIsShowTimerangeRuler)
		{
			r.right -= mDeeprangeRulerWidth;
		}
		if(mIsShowDeeprangeRuler)
		{
			r.left += mTimerangeRulerWidth;
		}
		
		return r;
	}
	//
	long mBefDownTime=0;
	//����λͼ
	public void ScrollDIB(int scans)
	{
		for(int i=0;i<mDIBHeight;i++)
		{	
			for(int j=scans;j<mDIBWidth;j++)
			{
				mDIBPixels[i*mDIBWidth+(j-scans)] = mDIBPixels[i*mDIBWidth+j];
			}
		}
	}
	//
	public void initDIB()
	{
		for(int i=0;i<mDIBHeight;i++)
		{	
			for(int j=0;j<mDIBWidth;j++)
			{
				mDIBPixels[i*mDIBWidth+j] = 0x0;
			}
		}
	}
	//
	public int NO_MARK = 0;   //û�б��
	public int BIG_MARK_FLAG = 1;
	public int SMALL_MARK_FLAG =2;
	public int BIG_MARK_VAL = 0x4000;    //���(����)
	public int SMALL_MARK_VAL = 0x8000;  //С��(����)
	public int mMarkType = NO_MARK;      //�������
	////������ת����dibλͼ
	public void changeDatasToDIB(short[] buf,int length)
	{
		int mScanLen;
		mScanLen = ((LifeSearchActivity)mContext).mRadarDevice.getScanLen();
		
		//���������ܵ���
		int scans;
		scans = length/2;
		scans = scans/mScanLen;
		//����ˮƽ�����ϵ�����
		scans = scans/mXZoom;
				
		//����λͼ����
		int srollScans;
		int fillPosOff;
		fillPosOff = 0;
		srollScans = scans - fillPosOff;
		if(srollScans<=0)
			srollScans = 0;
		ScrollDIB(srollScans);
				
		//ת��λͼ
		int colNumber;   //��ɫ����ɫ��
		int[][] mColPal;
		int palIndex;
		palIndex = mColorPal.getColpalIndex();
		colNumber = mColorPal.getColorNumber();
		mColPal = mColorPal.getColors();
		
		short dataVal;
		int dataInter;
		double perCol;
		perCol = 0xefff/colNumber;
		dataInter = mScanLen/mDIBHeight;
		if(dataInter<1)
			dataInter = 1;
		int  fillColIndex;
		//ȷ�����λ��
		int fillBaseIndex;
		if(fillPosOff>=scans)
		{
			fillBaseIndex = mDIBWidth - fillPosOff;
		}
		else
		{
			fillBaseIndex = mDIBWidth - scans;
		}
		int fillIndex;
		int col;
		short flagVal;
		double temVal;
		int j;
		short[] scanDatas = new short[mScanLen];
		for(int i=0;i<scans;i++)
		{
			mMarkType = NO_MARK;
			//�������Ŵ���
			flagVal = 0;
			for(j=0;j<mScanLen;j++)
			{
				temVal = 0;
				scanDatas[j] = 0;
				for(int m=0;m<mXZoom;m++)
				{
					temVal += buf[(i*mXZoom+m)*mScanLen + j];
					flagVal |= (short) (buf[(i*mXZoom+m)*mScanLen + 1]);
				}
				scanDatas[j] = (short) (temVal/mXZoom);
			}
			//����ж�
//			if((flagVal & BIG_MARK_VAL)!=0)
//				mMarkType = BIG_MARK_FLAG;
//			if((flagVal & SMALL_MARK_VAL)!=0)
//				mMarkType = SMALL_MARK_FLAG;
			//
			for(j=0;j<mDIBHeight;j++)
			{
				if(j*dataInter>=mScanLen)
				{
					fillIndex = (fillBaseIndex + i)+(j*mDIBWidth);
					mDIBPixels[fillIndex] = 0; 
				}
				else
				{
					//��Ǵ���
					dataVal = scanDatas[j*dataInter];
					if(j==0)
						dataVal = 0;
					if(mMarkType == BIG_MARK_FLAG)
						dataVal = 0x7fff;
					if(mMarkType == SMALL_MARK_FLAG)
					{
						if(j*dataInter<=mScanLen/2)
							dataVal = 0x7fff;
					}
					fillColIndex = (int)(dataVal/perCol);
					fillColIndex += colNumber/2;
					//
					if(fillColIndex<0)
						fillColIndex=0;
					if(fillColIndex>=colNumber)
						fillColIndex = colNumber-1; 
					
					col = 0;
					int r,g,b,a;
					a = 255<<24;
					r = mColPal[fillColIndex][0];
					r = r<<16;
						
					g = mColPal[fillColIndex][1];
					g = g<<8;
					b = mColPal[fillColIndex][2];
					col = a|r|g|b;
					//
					fillIndex = (fillBaseIndex + i)+(j*mDIBWidth);
					mDIBPixels[fillIndex] = col; 
				}
			}
		}
		//
	}
}
