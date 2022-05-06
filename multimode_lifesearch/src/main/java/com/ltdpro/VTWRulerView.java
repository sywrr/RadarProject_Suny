package com.ltdpro;
/*
 * 垂直标尺视图(时窗|深度)
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;

import android.view.GestureDetector;
import android.view.View;

public class VTWRulerView extends View {
	private String TAG="VRulerView";
	private String mTimeTitle="时窗(ns)";
	private String mDeepTitle="深度(m)";
	private float mXDownPos,mYDownPos;   //触控点点下时记录的坐标
	private float mNowMoveXPos,mNowMoveYPos;   //当前点的坐标位置
	private int mLeftspace=2;
	private int mRightspace=2;
	private int TIME_RULER = 1;
	private int DEEP_RULER = 2;
	private int mRulertype=TIME_RULER;   //标尺类型
	private int m_scaleNum = 20;         //共分为20个刻度
	private int mTopBegin = 0;     //起始刻度位置
	private int mNowTopSpace = 0;
	private int mLongscaleLength = 10;
	private int mShortscaleLength = 6;
	
	////
	Paint mWidePaint;          //主画笔用的paint;
	Paint mThinPaint;
	Paint mTextPaint;
	//
	private GestureDetector detector;
//	private VRulerGestureListener mListener;
	View mContext;
	
	//
	private int mChannel=0;   //对应的通道索引号
	
	public VTWRulerView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initData(context);
	}
	/**
     * Constructor
     */
    public VTWRulerView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            initData(context);
     }

    /**
     * Constructor
     */
    public VTWRulerView(Context context, AttributeSet attrs) {
            super(context, attrs);
            initData(context);
     }  
    
    public void initData(Context context)
    {
//    	mListener=new VRulerGestureListener();
//		detector = new GestureDetector(context,mListener);
		//
    	mWidePaint = new Paint();
    	mWidePaint.setColor(Color.BLACK);
    	mWidePaint.setStrokeWidth(3);
    	//
    	mThinPaint = new Paint();
    	mThinPaint.setColor(Color.BLACK);
    	mThinPaint.setStrokeWidth(1);
    	//
    	mTextPaint = new Paint();
    	mTextPaint.setColor(Color.BLACK);
    	mTextPaint.setStrokeWidth(3);
    	mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
    	mTextPaint.setTextAlign(Paint.Align.RIGHT);
    	
    	//
//    	mListener.setAttachView(this);
    	mRulertype = DEEP_RULER;    	
    }
    
    public void setChannel(int channel)
    {
    	mChannel = channel;
    }
    /*
    @Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder,int format,int width,int height)
	{
		
	}
	*/
    @Override
	protected void onDraw(Canvas canvas)
	{
//		DebugUtil.i(TAG,"onDraw");
		
//		if(mRulertype == TIME_RULER)
//		{
			drawTimeRuler(canvas);
//		}

//		drawDeepRuler(canvas);
	}
    
    public void drawTimeRuler(Canvas canvas)
    {
    	//////画出中心轴
      	float xPos,yPos;
      	float xOrgPos,yOrgPos;
      	int width = this.getWidth();
      	int height = this.getHeight();
      	
      	//起始x,y坐标
      	xOrgPos = width - mWidePaint.getStrokeWidth();// + mLeftspace;
      	yOrgPos = 0;
      	//终止x,y坐标
      	xPos = xOrgPos;
      	yPos = yOrgPos+getHeight();
      	canvas.drawLine(xOrgPos, yOrgPos, xPos, yPos, mWidePaint);
      	
      	////得到时窗值
      	MyApplication app;
      	app=(MyApplication)(getContext().getApplicationContext());
      	int timeWnd = app.mRadarDevice.getTimeWindow();
      	int scanLen = app.mRadarDevice.getScanLength();
      	int dibH = 512;//app.mRealDibView.mDIBHeight;
      	
      	////计算每个刻度的刻度值
      	//1:
      	double perScaleVal;    //每个刻度的值
      	double perScalePix;    //每个刻度占用的像素点
      	boolean  hasAdd=false;   //是否有不足的刻度
      	//2:计算符合刻度要求的最小的整刻度值
      	int perTime = 1;
      	while(perTime<(timeWnd/m_scaleNum))
      	{
      		perTime += 1;
      	}
       	perScalePix=height/1.;
      	perScaleVal=perTime/1.;
      	int scaleNum=(int) (timeWnd/perScaleVal);
      	if(scaleNum*perScaleVal<timeWnd)
      		hasAdd=true;
      	perScalePix=perScalePix*perScaleVal/(timeWnd);
      	//3:
      	int i;
      	float xPos1,xPos2,yPos1,yPos2;
      	String text;
      	for(i=0;i<=scaleNum;i++)
      	{
      		xPos1 = xOrgPos;
      		if(i%2==0)
      		{
      			xPos2 = xOrgPos - 2 - mLongscaleLength;// - mRightspace;
      		}
      		else
      		{
      			xPos2 = xOrgPos - 2 - mShortscaleLength;// - mRightspace;
      		}
      		yPos1 = (float) (yOrgPos + mTopBegin + mNowTopSpace + i*perScalePix);
      		yPos2 = yPos1;
      		canvas.drawLine(xPos1, yPos1, xPos2, yPos2, mWidePaint);
      		//
      		text = ""+(int)(perScaleVal*i);
      		if(i==0)
      			text = text + "(ns)";
      		xPos2 = xOrgPos -2 - mLongscaleLength; //+ 2 + mLongscaleLength;
      		if(i==0)
      			yPos2 = yPos2+10;
      		if(i!=0)
      			yPos2 = yPos2+4;
      		if(i == scaleNum && !hasAdd)
      			yPos2 = yPos2-4;
      		canvas.drawText(text, xPos2, yPos2, mTextPaint);
      	}
      	if(hasAdd)
      	{
      		xPos1 = xOrgPos;
     		xPos2 = xOrgPos - 2 - mLongscaleLength;
     		yPos1 = (float) (yOrgPos + mTopBegin + mNowTopSpace + i*perScalePix);
     		if(yPos1>getHeight())
     			yPos1 = yOrgPos+getHeight();
      		yPos2 = yPos1;
      		canvas.drawLine(xPos1, yPos1, xPos2, yPos2, mWidePaint);
      		//
      		text = ""+timeWnd;
       		xPos2 = xOrgPos - 2 - mLongscaleLength;
     			yPos2 = yPos2-4;
      		canvas.drawText(text, xPos2, yPos2, mTextPaint);
      	}
    }
    public void drawDeepRuler(Canvas canvas)
    {
    	//画出中心轴
    	float xPos,yPos;
    	float xOrgPos,yOrgPos;
    	xOrgPos = mWidePaint.getStrokeWidth();
    	yOrgPos = 0;
    	xPos = xOrgPos;
    	yPos = yOrgPos+getHeight();
    	canvas.drawLine(xOrgPos, yOrgPos, xPos, yPos, mWidePaint);
    	
    	//得到时窗值
    	MyApplication app;
    	app=(MyApplication)(getContext().getApplicationContext());
    	
    	double deep = app.mRadarDevice.getDeep();
    	int scanLen = app.mRadarDevice.getScanLength();
    	int dibH = 512;// app.mRealDibView.mDIBHeight;
    	
    	////计算每个刻度的刻度值
    	//1:
    	double perScaleVal;    //每个刻度的值
    	double perScalePix;    //每个刻度占用的像素点
    	boolean  hasAdd=false;   //是否有不足的刻度
    	//2:
    	double perDeep = 0.1;
    	while(perDeep<(deep/m_scaleNum))
    	{
    		perDeep += 0.1;
    	}
    	int height = this.getHeight();
    	perScalePix=height/1.;
    	perScaleVal=perDeep/1.;
    	int scaleNum=(int) (deep/perScaleVal);
    	if(scaleNum*perScaleVal<deep)
    		hasAdd=true;
    	perScalePix=perScalePix*perScaleVal/(deep);
    	//3:
    	int i;
    	float xPos1,xPos2,yPos1,yPos2;
    	String text;
    	for(i=0;i<=scaleNum;i++)
    	{
    		xPos1 = xOrgPos;
    		if(i%2==0)
    		{
    			xPos2 = xOrgPos + 2 + mLongscaleLength;
    		}
    		else
    		{
    			xPos2 = xOrgPos + 2 + mShortscaleLength;
    		}
    		yPos1 = (float) (yOrgPos + mTopBegin + mNowTopSpace + i*perScalePix);
    		yPos2 = yPos1;
    		canvas.drawLine(xPos1, yPos1, xPos2, yPos2, mWidePaint);
    		//
    		text = ""+((int)((perScaleVal*i)*10))/10.;
    		if(i==0)
    			text = text + "(m)";
    		xPos2 = xOrgPos + 2 + mLongscaleLength;
    		if(i==0)
    			yPos2 = yPos2+10;
    		if(i!=0)
    			yPos2 = yPos2+4;
    		if(i == scaleNum && !hasAdd)
    			yPos2 = yPos2-4;
    		canvas.drawText(text, xPos2, yPos2, mTextPaint);
    	}
    	if(hasAdd)
    	{
    		xPos1 = xOrgPos;
   			xPos2 = xOrgPos + 2 + mLongscaleLength;
   			yPos1 = (float) (yOrgPos + mTopBegin + mNowTopSpace + i*perScalePix);
   			if(yPos1>getHeight())
   				yPos1 = yOrgPos+getHeight();
    		yPos2 = yPos1;
    		canvas.drawLine(xPos1, yPos1, xPos2, yPos2, mWidePaint);
    		//
    		text = ""+deep;
     		xPos2 = xOrgPos + 2 + mLongscaleLength;
   			yPos2 = yPos2-4;
    		canvas.drawText(text, xPos2, yPos2, mTextPaint);
    	}
    }
    	
  	//隐藏
	public void hide()
	{
		this.setVisibility(View.INVISIBLE);
	}
}
