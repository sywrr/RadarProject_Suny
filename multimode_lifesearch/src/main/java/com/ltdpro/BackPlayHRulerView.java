package com.ltdpro;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnDoubleTapListener;

public class BackPlayHRulerView extends View {
	
	private String TAG = "BackPlayHRulerView";
	private float mXDownPos,mYDownPos;   //触控点点下时记录的坐标
	private float mNowMoveXPos,mNowMoveYPos;   //当前点的坐标位置
	private int mLeftspace,mRightspace,mTopspace,mBottomspace;
	private int mTotalScans=0;
	private double mDistancePerScans=0.;   //相邻两道数据间的距离间隔(厘米)
	
	Paint mWidePaint;          //主画笔用的paint;
	Paint mBackposPaint;
	Paint mTextPaint;
	Context mContext;
	private GestureDetector detector;
	private HRulerGestureListener mListener;
	
	//
	private int mLongscaleLength = 10;    //长刻度
	private int mShortscaleLength = 6;    //短刻度
	
	//
	private int mScansPerScale = 50;    //每个刻度表示的道数
	
	//显示类型
	private int SHOW_SCANS=1;
	private int SHOW_DISTANCE=2;
	private int mShowType=SHOW_SCANS;
	
	//
	private int mZoomX=1;   //压缩倍数
	
	//
	private int mPixsPerScan=1;
	
	public BackPlayHRulerView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initData(context);
	}
	/**
     * Constructor
     */
    public BackPlayHRulerView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            initData(context);
     }

    /**
     * Constructor
     */
    public BackPlayHRulerView(Context context, AttributeSet attrs) {
            super(context, attrs);
            initData(context);
     }  
    //
    public void setTouchDistance(double distance)
    {
    	mDistancePerScans = distance;
    }
    
    public void initData(Context context)
    {
    	mListener= new HRulerGestureListener();
		detector = new GestureDetector(context, mListener);
		
     	mWidePaint = new Paint();
    	mWidePaint.setColor(Color.BLACK);
    	mWidePaint.setStrokeWidth(2);
    	//
    	mBackposPaint = new Paint();
    	mBackposPaint.setColor(Color.WHITE);
    	mBackposPaint.setStrokeWidth(2);
    	//
    	mTextPaint = new Paint();
    	mTextPaint.setColor(Color.BLACK);
    	mTextPaint.setStrokeWidth(3);
    	mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
    	
    	//
    	mLeftspace = 2;
    	mRightspace = 2;
    	mTopspace = 2;
    	mBottomspace = 2;
    	
    	mContext = context;
    	mListener.setAttachView(this);

    	mDistancePerScans=0.;
     }
    public void setZoomX(int zoomX)
    {
    	mZoomX = zoomX;
    }
    public void setNowScans(int scans)
    {
    	mTotalScans = scans;
    }
    
    @Override
	protected void onDraw(Canvas canvas)
	{
//		DebugUtil.i(TAG,"onDraw");
		if(mShowType == SHOW_SCANS)
		{
			drawScansRuler(canvas);
		}
		if(mShowType == SHOW_DISTANCE)
		{
			drawDistanceRuler(canvas);
		}
		
	}
    //
    public void setShowscanMode()
    {
    	 mShowType = SHOW_SCANS;
		 invalidate();
    }
    
    public void setShowdistanceMode()
    {
     	 mShowType = SHOW_DISTANCE;
	     invalidate();
    }
    ////画出数据道标尺
    public void drawScansRuler(Canvas canvas)
    {
    	mLeftspace = mApp.mBTimewndRuler.getWidth();
    	mRightspace = mApp.mBDeepRuler.getWidth();
    	//画出主线
    	int xOrg,yOrg,xPos,yPos;
    	xOrg = 0 + mLeftspace;
    	yOrg = getHeight()-mBottomspace;
    	xPos = getWidth()-mRightspace;
    	yPos = yOrg;
    	mWidePaint.setColor(Color.BLACK);
    	canvas.drawLine(xOrg, yOrg, xPos, yPos, mWidePaint);
    			
    	//画出刻度线
    	long hasRcvScans;
    	MyApplication app;
    	app = (MyApplication)mContext.getApplicationContext();
    	hasRcvScans = mTotalScans;
    	int srcWidth;
    	srcWidth = this.getWidth() - mLeftspace - mRightspace; //app.getScreenWidth();
    			
    	long endScan;      //结束的整刻度道数
    	long addEndScan;   //不足一个刻度的道数
    	long scaleNum;     //刻度数
    	long begScan;      //起始刻度值
    	long addBegScan;   //
    	int begXPos,begYPos,nowXPos,nowYPos;
    	double pixsPerScan = 1.0;   //每道数据占用的像素点数(dib图为1；堆积图为8)
    	pixsPerScan = mPixsPerScan;
    	int pixsPerScale;   //相邻刻度间的像素点差
    	//计算结束点的整刻度道数和非整刻度道数
    	scaleNum = hasRcvScans/mScansPerScale;      
    	endScan  = scaleNum*mScansPerScale;          //结束点的整刻度道数
    	addEndScan  = hasRcvScans%mScansPerScale;
    	//确定起始道号(结束道号  - 1屏能够容纳的道数)
    	begScan = (long) (hasRcvScans - srcWidth*mZoomX/pixsPerScan);
    	if(begScan<=0)
    	{
    		begXPos = (int) (Math.abs(begScan) / mZoomX * pixsPerScan);
    		begScan = 0;
    	}
    	else
    		//已经接收的道数大于了一屏宽度
    	{
    		long oldBegScan = begScan;
    		if((begScan%mScansPerScale)!=0)
    			begScan = (begScan/mScansPerScale+1)*mScansPerScale;
    		begXPos = (int) ((begScan-oldBegScan)/mZoomX*pixsPerScan);
    	}
    	///
    	begXPos += mLeftspace;
    	///
    	int i;
    	String text;
    	Rect txtRect = new Rect();
    	int scaleWidth;       //相邻两个刻度间相差的像素点数
    	scaleWidth = (int) (mScansPerScale*2*pixsPerScan/mZoomX);
    	int showScaleWidth = scaleWidth;   //用来显示刻度值的像素宽度
    	int begScaleNum = (int) (scaleNum - (srcWidth*mZoomX/pixsPerScan/mScansPerScale));
    	mWidePaint.setColor(Color.BLACK);
    	int endXPos = this.getWidth() - mRightspace;
    	for(i=0;i<=scaleNum-begScaleNum;i++)
    	{
    		nowXPos = (int) (begXPos + i*mScansPerScale*pixsPerScan/mZoomX);
    		nowYPos = yOrg;
    		//
    		if(nowXPos>endXPos)
    			break;
    		if(i%2==0)
    		{
    			nowYPos = yOrg - 2 - mLongscaleLength;
    		}
    		else
    		{
    			nowYPos = yOrg - 2 - mShortscaleLength;
    		}
    		mWidePaint.setColor(Color.BLACK);
    		canvas.drawLine(nowXPos, yOrg, nowXPos, nowYPos, mWidePaint);
    		//
    		if(i%2==0)
    		{
	    		text = ""+(int)(begScan+mScansPerScale*i);
	    		if(i==0)
	    			text = text + "道";
	    		//调整是否刻度值过大，导致相邻刻度值显示重叠
	    		mTextPaint.getTextBounds(text, 0, text.length(), txtRect);
	    		if(txtRect.width()<showScaleWidth)
	    		{
	    			canvas.drawText(text, nowXPos-txtRect.width()/2, nowYPos, mTextPaint);
	    			showScaleWidth = scaleWidth;
//	    			mWidePaint.setColor(Color.RED);
	        		canvas.drawLine(nowXPos, yOrg, nowXPos, nowYPos, mWidePaint);
	    		}
	    		else
	    		{
	    			showScaleWidth += scaleWidth;
	    		}
    		}
    	}
    }
    ////设置相邻两道数据的采集距离
    public void setDistancePerScans(double distance)
    {
    	mDistancePerScans = distance;
    }
    ////画出距离标尺
    /*
     * 方法：首先根据采集的数据道数，计算出距离值；
     *     计算每个刻度对应的距离值；
     */
    public MyApplication mApp = null;
    public void drawDistanceRuler(Canvas canvas)
    {
    	mLeftspace = mApp.mBTimewndRuler.getWidth();
    	mRightspace = mApp.mBDeepRuler.getWidth();
    	//画出主线
    	int xOrg,yOrg,xPos,yPos;
    	xOrg = 0 + mLeftspace;
    	yOrg = getHeight()-mBottomspace;
    	xPos = getWidth()-mRightspace;
    	yPos = yOrg;
    	mWidePaint.setColor(Color.BLACK);
    	canvas.drawLine(xOrg, yOrg, xPos, yPos, mWidePaint);
    			
    	//
    	double pixsPerScan = 1.0;
    	pixsPerScan = mPixsPerScan;
    	long hasRcvScans;
    	MyApplication app;
    	app = (MyApplication)mContext.getApplicationContext();
    	hasRcvScans = mTotalScans;      //已经接收到的道数
    	int srcWidth;
    	srcWidth = this.getWidth() - mLeftspace - mRightspace; //app.getScreenWidth();
    	
    	//相隔两道的距离值(cm)
    	double distancePerScan;   
    	distancePerScan = mDistancePerScans;
    	DebugUtil.i(TAG,"Now distancePerScan:="+distancePerScan);
    	if(distancePerScan == 0)
    		return;
    	
    	/////计算每个刻度对应的距离(默认50道) 
    	double scansPerScale;
    	double distancePerScale;   
    	scansPerScale = mScansPerScale;
    	distancePerScale = distancePerScan*scansPerScale;
    	//刻度距离值以10cm为单位
    	if(((int)distancePerScale)%10 != 0)
    	{
    		distancePerScale = ((int)(distancePerScale/10.)+1)*10;
    		scansPerScale = (distancePerScale/distancePerScan);
    	}
    	DebugUtil.i(TAG,"Now distancePerScale:="+distancePerScale);
    	
    	////计算起始和终止位置的道数
    	int begXPos;    //距离标尺起始位置(第一个整刻度位置)
    	long endScan,begScan;
    	endScan = hasRcvScans;   //结束道号
    	begScan = (long) (endScan - srcWidth/pixsPerScan*mZoomX);   //起始道号
    	////计算起始和终止距离值
    	double begDistance,endDistance;    
    	begDistance = begScan*distancePerScan;
    	endDistance = endScan*distancePerScan;
//    	DebugUtil.i(TAG,"BegDistance:="+begDistance+";endDistance:="+endDistance);
    	//计算第一个刻度对应的距离值
    	double firstScaleDistance=0;
    	if(begDistance<=0)
    	{
    		begXPos = (int) (Math.abs(begDistance) / distancePerScan / mZoomX * pixsPerScan);
    		firstScaleDistance = 0;
    	}
    	else
    		//已经接收的道数大于了一屏宽度
    	{
    		//调整第一个整距离刻度的坐标值
    		if((int)begDistance%(int)distancePerScale !=0 )
    		{
    			firstScaleDistance = ((int)begDistance/(int)distancePerScale+1)*distancePerScale;
    		}
    		else
    		{
    			firstScaleDistance = begDistance;
    		}
    		//
    		begScan = (long)((firstScaleDistance-begDistance)/distancePerScan);
    		begXPos = (int) (begScan/mZoomX*pixsPerScan);
    	}
    	///
    	begXPos += mLeftspace;
    	//
    	int i;
    	String text;
    	double nowDistance;
    	int nowScans;
    	int nowXPos,nowYPos;
    	int begYPos = yOrg;
    	//
    	Rect txtRect = new Rect();
    	int scaleWidth;       //相邻两个刻度间相差的像素点数
    	scaleWidth = (int) (distancePerScale/distancePerScan*2*pixsPerScan/mZoomX);
    	int showScaleWidth = scaleWidth;   //用来显示刻度值的像素宽度
    	int endXPos = this.getWidth() - mRightspace;
    	
    	for(i=0;true;i++)
    	{
    		nowXPos = (int) (begXPos + i*scansPerScale*pixsPerScan/mZoomX);
    		if(nowXPos<0)
    			continue;
    		if(nowXPos>endXPos)
    			break;
    		nowDistance = firstScaleDistance+i*distancePerScale;
    		if(nowDistance>endDistance)
    			break;
    		nowYPos = yOrg;
    		if(i%2==0)
    		{
    			nowYPos = yOrg - 2 - mLongscaleLength;
    		}
    		else
    		{
    			nowYPos = yOrg - 2 - mShortscaleLength;
    		}
    		mWidePaint.setColor(Color.BLACK);
    		canvas.drawLine(nowXPos, yOrg, nowXPos, nowYPos, mWidePaint);
    		//
    		int intVal;
    		if(i%2==0)
    		{
    			intVal = (int) nowDistance;
    			if(intVal>=100)
    			{
    				float fVal = (float) (nowDistance/100.);
    				intVal = (int) (fVal*100);
    				fVal = (float)((intVal)/100.);
    				text = ""+fVal;
    				if(i==0)
    					text = text + "m";
    			}
    			else
    			{
		    		text = ""+(int)(nowDistance);
		    		if(i==0)
		    			text = text + "cm";
    			}
    			mTextPaint.getTextBounds(text, 0, text.length(), txtRect);
    			if(txtRect.width()<showScaleWidth)
	    		{
	    			canvas.drawText(text, nowXPos-txtRect.width()/2, nowYPos, mTextPaint);
	    			showScaleWidth = scaleWidth;
//	    			mWidePaint.setColor(Color.RED);
	        		canvas.drawLine(nowXPos, yOrg, nowXPos, nowYPos, mWidePaint);
	    		}
	    		else
	    		{
	    			showScaleWidth += scaleWidth;
	    		}
    		}
    	}
    }
    //自定义GestureListener类
  	public class HRulerGestureListener implements GestureDetector.OnGestureListener,
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
  			hide();
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
  		     if(mShowType == SHOW_SCANS)
  		    	 mShowType = SHOW_DISTANCE;
  		     else
  		    	 mShowType = SHOW_SCANS;
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
  	//
  	public void setPixsPerScan(int pixs)
  	{
  		mPixsPerScan = pixs;
  	}
  	///
  	public void hide()
  	{
//  	Animation animation = AnimationUtils.loadAnimation(this.getContext(), R.drawable.alphadisappear);
//	    this.startAnimation(animation);
  		this.setVisibility(View.INVISIBLE);
  	}
    @Override
   	public boolean onTouchEvent(MotionEvent event)
   	{
    	//手势识别
	    if(detector.onTouchEvent(event))
		{
			return true;
		}
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
               	if(Math.abs(yPos) >= 0 && Math.abs(yPos) <= this.getHeight())
               	{
               		yPos = 0;
               	}
               	moveToXPos = left;// + (mNowMoveXPos - mXDownPos);
               	moveToYPos = top + (mNowMoveYPos-mYDownPos);
     		    break;
            case MotionEvent.ACTION_MOVE:
               	mNowMoveYPos = yPos;
               	mNowMoveXPos = xPos;
               	if(Math.abs(yPos) >= 0 && Math.abs(yPos) <= this.getHeight())
               	{
               		yPos = 0;
               	}
               	moveToXPos = left;// + (mNowMoveXPos - mXDownPos);
               	moveToYPos = top + (mNowMoveYPos-mYDownPos);
   		        break;
   		}
   	    this.invalidate();
   	    return true;
   	}
    ////2016.6.10
    public void setLeftSpace(int space)
    {
    	this.mLeftspace = space;
    }
    public void setRightSpace(int space)
    {
    	this.mRightspace = space;
    }
}
