package com.ltdpro;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnDoubleTapListener;
import android.widget.FrameLayout;

public class BackPlayVRulerView extends View {
	
	private String TAG="BackPlayVRulerView";
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
	//
	private int mTimeWindow=40;
	private double mDeep=3.0;
	
	////
	Paint mWidePaint;          //主画笔用的paint;
	Paint mThinPaint;
	Paint mTextPaint;
	//
	private GestureDetector detector;
	private VRulerGestureListener mListener;
	View mContext;
	
	public BackPlayVRulerView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initData(context);
	}
	/**
     * Constructor
     */
    public BackPlayVRulerView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            initData(context);
     }
    /**
     * Constructor
     */
    public BackPlayVRulerView(Context context, AttributeSet attrs) {
            super(context, attrs);
            initData(context);
     }  
    public void setTimeWindow(int time)
    {
    	mTimeWindow = time;
    }
    public void setDeep(double deep)
    {
    	mDeep = deep;
    }
    public void initData(Context context)
    {
    	mListener=new VRulerGestureListener();
		detector = new GestureDetector(context, mListener);
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
    	mListener.setAttachView(this);
    	mRulertype = DEEP_RULER;
    	
    }
  //自定义GestureListener类
  	public class VRulerGestureListener implements GestureDetector.OnGestureListener,
          OnDoubleTapListener
  	{
  		private View mView;
   		@Override
  		public boolean onDown(MotionEvent e) {
  			DebugUtil.i(TAG,"onDown");
   			//
  			return false;
 // 			return true;
  		}

  		@Override
  		public boolean onSingleTapUp(MotionEvent e) {
  			DebugUtil.i(TAG,"onSingleTapUp");
   			return true;
  		}
  		
  		@Override
  		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                float distanceY)
  		{
   			DebugUtil.i(TAG,"onScroll,xOff:="+distanceX+"yOff:="+distanceY);
//    		return true;
   			return false;
  		}
  		
  		@Override
  		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY)
  		{
  			DebugUtil.i(TAG,"onFling");
 // 			return true;
  			return false;
  		}
  		
  		@Override
  		public void onLongPress(MotionEvent e)
  		{
  			DebugUtil.i(TAG,"onLongPress");
//  			hide();
  		}
  		
  		@Override
  		public void onShowPress(MotionEvent e)
  		{
  			DebugUtil.i(TAG,"onShowPress");
  		}
  		
  		@Override
  		public boolean onDoubleTap(MotionEvent e)
  		{  
  			 // TODO Auto-generated method stub  
  		     DebugUtil.i("TAG", "onDoubleTap---->处理双击事件"); 
  		     if(mRulertype == TIME_RULER)
  		    	 mRulertype = DEEP_RULER;
  		     else
  		    	 mRulertype = TIME_RULER;
  		     invalidate();
  		     return true;
  		}
  		@Override
  		public boolean onDoubleTapEvent(MotionEvent e)
  		{  
  		     // TODO Auto-generated method stub  
  		      DebugUtil.i("TAG", "onDoubleTapEvent----->双击事件中的事件响应");  
  		      
  		      return true;  
  	    }  
  		@Override
  		public boolean onSingleTapConfirmed(MotionEvent e)
  		{  
  		    // TODO Auto-generated method stub  
  			DebugUtil.i("TAG", "onSingleTapConfirmed----->双击事件过长变成单击事件");  
  			return true;  
  		}  

  		//
  		public void setAttachView(View view)
  		{
  			mView = view;
   		}
  	}
  	///
  	public void hide()
  	{
  //		Animation animation = AnimationUtils.loadAnimation(this.getContext(), R.drawable.alphadisappear);
  //	    this.startAnimation(animation);
  		this.setVisibility(View.INVISIBLE);
  	}
  	@Override
	protected void onDraw(Canvas canvas)
	{
		DebugUtil.i(TAG,"onDraw");
		
		if(mRulertype == TIME_RULER)
		{
			drawTimeRuler(canvas);
		}
		else
		{
			drawDeepRuler(canvas);
		}
	}
  	//2016.6.10
  	public void setShowTimewndType()
  	{
  		mRulertype = TIME_RULER;
  	}
  	public void setShowDeepType()
  	{
  		mRulertype = DEEP_RULER;
  	}
  	//
    public void drawTimeRuler(Canvas canvas)
    {
    	//画出中心轴
    	float xPos,yPos;
    	float xOrgPos,yOrgPos;
    	int width = this.getWidth();
      	int height = this.getHeight();
      	
      	//
    	xOrgPos = width - mWidePaint.getStrokeWidth();//mWidePaint.getStrokeWidth()+mLeftspace;
    	yOrgPos = 0;
    	xPos = xOrgPos;
    	yPos = yOrgPos+getHeight();
    	mWidePaint.setColor(Color.BLACK);
    	canvas.drawLine(xOrgPos, yOrgPos, xPos, yPos, mWidePaint);
    	
    	//得到时窗值
    	MyApplication app;
    	app=(MyApplication)(getContext().getApplicationContext());
    	int timeWnd = mTimeWindow;
    	
    	////计算每个刻度的刻度值
    	double valPerScale;
    	valPerScale = this.getHeight()/1.;
    	valPerScale = timeWnd/valPerScale;
    	//1:
    	double perScaleVal;    //每个刻度的值
    	double perScalePix;    //每个刻度占用的像素点
    	boolean  hasAdd=false;   //是否有不足的刻度
    	//2:
    	int perTime = 1;
    	while(perTime<(timeWnd/m_scaleNum))
    	{
    		perTime += 1;
    	} 	
    	perScalePix=this.getHeight()/1.;
    	perScaleVal=perTime/1.;
    	int scaleNum=(int) (timeWnd/perScaleVal);
    	if(scaleNum*perScaleVal<timeWnd)
    		hasAdd=true;
    	perScalePix=perScalePix*perScaleVal/(timeWnd);
    	//3:
    	mTextPaint.setTextAlign(Paint.Align.RIGHT);
    	int i;
    	float xPos1,xPos2,yPos1,yPos2;
    	String text;
    	for(i=0;i<=scaleNum;i++)
    	{
    		xPos1 = xOrgPos;
    		mWidePaint.setColor(Color.BLACK);
    		if(i%2==0)
      		{
      			xPos2 = xOrgPos - 2 - mLongscaleLength;// - mRightspace;
      		}
      		else
      		{
      			xPos2 = xOrgPos - 2 - mShortscaleLength;// - mRightspace;
      		}
    		/*
    		if(i%2==0)
    		{
    			xPos2 = xOrgPos + 2 + mLongscaleLength;
//    			mWidePaint.setColor(Color.RED);
    		}
    		else
    		{
    			xPos2 = xOrgPos + 2 + mShortscaleLength;
    		}
    		*/
    		yPos1 = (float) (yOrgPos + mTopBegin + mNowTopSpace + i*perScalePix);
    		yPos2 = yPos1;
    		canvas.drawLine(xPos1, yPos1, xPos2, yPos2, mWidePaint);
    		//
    		text = ""+(int)(perScaleVal*i);
    		if(i==0)
    			text = text + "(ns)";
    		xPos2 = xOrgPos -2 - mLongscaleLength; //xOrgPos + 2 + mLongscaleLength;
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
   			xPos2 = xOrgPos - 2 - mLongscaleLength;//xOrgPos + 2 + mLongscaleLength;
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
    	mWidePaint.setColor(Color.BLACK);
    	canvas.drawLine(xOrgPos, yOrgPos, xPos, yPos, mWidePaint);
    	//得到时窗值
    	MyApplication app;
    	app=(MyApplication)(getContext().getApplicationContext());
    	double deep = mDeep; //app.mRadarDevice.getDeep();
    	
    	////计算每个刻度的刻度值
    	double valPerScale;
    	valPerScale = this.getHeight()/1.;
    	valPerScale = deep/valPerScale;
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
    	perScalePix=this.getHeight()/1.;
    	perScaleVal=perDeep/1.;
    	int scaleNum=(int) (deep/perScaleVal);
    	if(scaleNum*perScaleVal<deep)
    		hasAdd=true;
    	perScalePix=perScalePix*perScaleVal/(deep);
    	//3:
    	mTextPaint.setTextAlign(Paint.Align.LEFT);
    	int i;
    	float xPos1,xPos2,yPos1,yPos2;
    	String text;
    	for(i=0;i<=scaleNum;i++)
    	{
    		xPos1 = xOrgPos;
    		mWidePaint.setColor(Color.BLACK);
    		if(i%2==0)
    		{
    			xPos2 = xOrgPos + 2 + mLongscaleLength;
//    			mWidePaint.setColor(Color.RED);
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
    @Override
	public boolean onTouchEvent(MotionEvent event)
	{
    	//手势识别
	    if(detector.onTouchEvent(event))
		{
			return true;
		}
    	
    	//得到屏幕的宽度
    	MyApplication app;
    	app=(MyApplication)(getContext().getApplicationContext());
    	int sreenWidth = app.getScreenWidth();
    	
    	//得到坐标位置
	    float xPos = event.getX();
	    float yPos = event.getY();
	    float moveToXPos,moveToYPos;
	    int left,right,top,bottom;
	    left = this.getLeft();
	    right = this.getRight();
	    top = this.getTop();
	    bottom = this.getBottom();
	    
	    switch (event.getAction())
		{
	    	case MotionEvent.ACTION_DOWN:
	    		mXDownPos = xPos;
	    		mYDownPos = yPos;
	    		mNowMoveXPos = xPos;
	    		mNowMoveYPos = yPos;
		        break;
		    case MotionEvent.ACTION_UP:
		    case MotionEvent.ACTION_CANCEL:
		    	mNowMoveYPos = yPos;
            	mNowMoveXPos = xPos;
            	xPos = mNowMoveXPos-mXDownPos;
		    	if(Math.abs(xPos) >= 0 && Math.abs(xPos) <= 15)//this.getWidth())
		    	{
		    		xPos = 0;
		    	}
            	if(xPos<0)   //向左移
		    	{
		    		moveToXPos = (left - Math.abs(xPos));
		    	}
		    	else    //向右移
		    	{
		    		moveToXPos = left+xPos;
		    	}
            	moveToYPos = top;// + (mNowMoveYPos-mYDownPos);
		    	mTopBegin += (int) (yPos-mYDownPos);
		    	mNowTopSpace = 0;
		    	//
		    	mXDownPos = xPos;
		    	mYDownPos = yPos;
		    	if(xPos !=0 )
            		MoveTo(moveToXPos,moveToYPos);
		    	DebugUtil.i(TAG,"**ACTION_UP mTopBegin:="+mTopBegin);
 		        break;
            case MotionEvent.ACTION_MOVE:
            	mNowMoveYPos = yPos;
            	mNowMoveXPos = xPos;
            	xPos = mNowMoveXPos-mXDownPos;
            	if(Math.abs(xPos) >= 0 && Math.abs(xPos) <= 15)//this.getWidth())
            	{
            		xPos = 0;
            	}
            	if(xPos<0)   //向左移
		    	{
		    		moveToXPos = (left - Math.abs(xPos));
		    	}
		    	else         //向右移
		    	{
		    		moveToXPos = left+xPos;
		    	}
            	moveToYPos = top;// + (mNowMoveYPos-mYDownPos);
            	mNowTopSpace = (int) (yPos-mYDownPos);
            	if(xPos !=0 )
            		MoveTo(moveToXPos,moveToYPos);
 		    	//
 		    	DebugUtil.i(TAG,"**ACTION_MOVE mTopBegin:="+mTopBegin+";mNowTopSpace:="+mNowTopSpace);
 		        break;
		}
	    this.invalidate();
	    return true;
	}
    public void MoveTo(float xOff,float yOff)
    {
    	MyApplication app;
    	app=(MyApplication)(getContext().getApplicationContext());
    	int sreenWidth = app.getScreenWidth();
    	
    	int left,right,top,bottom;
	    left = (int) xOff;
	    top =  (int) yOff;
	    right = left+this.getWidth();
	    bottom = top+this.getHeight();
	    /*
	    this.setLeft(left);
	    this.setRight(right);
	    this.setTop(top);
	    this.setBottom(bottom);
	    */
	    FrameLayout.LayoutParams params= (FrameLayout.LayoutParams) this.getLayoutParams();
	    if(left<0)
	    	left = 0;
	    params.leftMargin = left;
	    this.setLayoutParams(params);
	    //
	    /*
	    DebugUtil.i(TAG,"LeftMargin:="+params.leftMargin+";;left:="+left);
	    DebugUtil.i(TAG,"RightMargin:="+params.rightMargin+";;right:="+right);
	    DebugUtil.i(TAG,"TopMargin:="+params.topMargin+";;top:="+top);
	    DebugUtil.i(TAG,"BottomMargin:="+params.bottomMargin+";;bottom:="+bottom);
	    */
    }
}
