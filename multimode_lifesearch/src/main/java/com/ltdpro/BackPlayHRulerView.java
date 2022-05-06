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
	private float mXDownPos,mYDownPos;   //���ص����ʱ��¼������
	private float mNowMoveXPos,mNowMoveYPos;   //��ǰ�������λ��
	private int mLeftspace,mRightspace,mTopspace,mBottomspace;
	private int mTotalScans=0;
	private double mDistancePerScans=0.;   //�����������ݼ�ľ�����(����)
	
	Paint mWidePaint;          //�������õ�paint;
	Paint mBackposPaint;
	Paint mTextPaint;
	Context mContext;
	private GestureDetector detector;
	private HRulerGestureListener mListener;
	
	//
	private int mLongscaleLength = 10;    //���̶�
	private int mShortscaleLength = 6;    //�̶̿�
	
	//
	private int mScansPerScale = 50;    //ÿ���̶ȱ�ʾ�ĵ���
	
	//��ʾ����
	private int SHOW_SCANS=1;
	private int SHOW_DISTANCE=2;
	private int mShowType=SHOW_SCANS;
	
	//
	private int mZoomX=1;   //ѹ������
	
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
    ////�������ݵ����
    public void drawScansRuler(Canvas canvas)
    {
    	mLeftspace = mApp.mBTimewndRuler.getWidth();
    	mRightspace = mApp.mBDeepRuler.getWidth();
    	//��������
    	int xOrg,yOrg,xPos,yPos;
    	xOrg = 0 + mLeftspace;
    	yOrg = getHeight()-mBottomspace;
    	xPos = getWidth()-mRightspace;
    	yPos = yOrg;
    	mWidePaint.setColor(Color.BLACK);
    	canvas.drawLine(xOrg, yOrg, xPos, yPos, mWidePaint);
    			
    	//�����̶���
    	long hasRcvScans;
    	MyApplication app;
    	app = (MyApplication)mContext.getApplicationContext();
    	hasRcvScans = mTotalScans;
    	int srcWidth;
    	srcWidth = this.getWidth() - mLeftspace - mRightspace; //app.getScreenWidth();
    			
    	long endScan;      //���������̶ȵ���
    	long addEndScan;   //����һ���̶ȵĵ���
    	long scaleNum;     //�̶���
    	long begScan;      //��ʼ�̶�ֵ
    	long addBegScan;   //
    	int begXPos,begYPos,nowXPos,nowYPos;
    	double pixsPerScan = 1.0;   //ÿ������ռ�õ����ص���(dibͼΪ1���ѻ�ͼΪ8)
    	pixsPerScan = mPixsPerScan;
    	int pixsPerScale;   //���ڿ̶ȼ�����ص��
    	//�������������̶ȵ����ͷ����̶ȵ���
    	scaleNum = hasRcvScans/mScansPerScale;      
    	endScan  = scaleNum*mScansPerScale;          //����������̶ȵ���
    	addEndScan  = hasRcvScans%mScansPerScale;
    	//ȷ����ʼ����(��������  - 1���ܹ����ɵĵ���)
    	begScan = (long) (hasRcvScans - srcWidth*mZoomX/pixsPerScan);
    	if(begScan<=0)
    	{
    		begXPos = (int) (Math.abs(begScan) / mZoomX * pixsPerScan);
    		begScan = 0;
    	}
    	else
    		//�Ѿ����յĵ���������һ�����
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
    	int scaleWidth;       //���������̶ȼ��������ص���
    	scaleWidth = (int) (mScansPerScale*2*pixsPerScan/mZoomX);
    	int showScaleWidth = scaleWidth;   //������ʾ�̶�ֵ�����ؿ��
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
	    			text = text + "��";
	    		//�����Ƿ�̶�ֵ���󣬵������ڿ̶�ֵ��ʾ�ص�
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
    ////���������������ݵĲɼ�����
    public void setDistancePerScans(double distance)
    {
    	mDistancePerScans = distance;
    }
    ////����������
    /*
     * ���������ȸ��ݲɼ������ݵ��������������ֵ��
     *     ����ÿ���̶ȶ�Ӧ�ľ���ֵ��
     */
    public MyApplication mApp = null;
    public void drawDistanceRuler(Canvas canvas)
    {
    	mLeftspace = mApp.mBTimewndRuler.getWidth();
    	mRightspace = mApp.mBDeepRuler.getWidth();
    	//��������
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
    	hasRcvScans = mTotalScans;      //�Ѿ����յ��ĵ���
    	int srcWidth;
    	srcWidth = this.getWidth() - mLeftspace - mRightspace; //app.getScreenWidth();
    	
    	//��������ľ���ֵ(cm)
    	double distancePerScan;   
    	distancePerScan = mDistancePerScans;
    	DebugUtil.i(TAG,"Now distancePerScan:="+distancePerScan);
    	if(distancePerScan == 0)
    		return;
    	
    	/////����ÿ���̶ȶ�Ӧ�ľ���(Ĭ��50��) 
    	double scansPerScale;
    	double distancePerScale;   
    	scansPerScale = mScansPerScale;
    	distancePerScale = distancePerScan*scansPerScale;
    	//�̶Ⱦ���ֵ��10cmΪ��λ
    	if(((int)distancePerScale)%10 != 0)
    	{
    		distancePerScale = ((int)(distancePerScale/10.)+1)*10;
    		scansPerScale = (distancePerScale/distancePerScan);
    	}
    	DebugUtil.i(TAG,"Now distancePerScale:="+distancePerScale);
    	
    	////������ʼ����ֹλ�õĵ���
    	int begXPos;    //��������ʼλ��(��һ�����̶�λ��)
    	long endScan,begScan;
    	endScan = hasRcvScans;   //��������
    	begScan = (long) (endScan - srcWidth/pixsPerScan*mZoomX);   //��ʼ����
    	////������ʼ����ֹ����ֵ
    	double begDistance,endDistance;    
    	begDistance = begScan*distancePerScan;
    	endDistance = endScan*distancePerScan;
//    	DebugUtil.i(TAG,"BegDistance:="+begDistance+";endDistance:="+endDistance);
    	//�����һ���̶ȶ�Ӧ�ľ���ֵ
    	double firstScaleDistance=0;
    	if(begDistance<=0)
    	{
    		begXPos = (int) (Math.abs(begDistance) / distancePerScan / mZoomX * pixsPerScan);
    		firstScaleDistance = 0;
    	}
    	else
    		//�Ѿ����յĵ���������һ�����
    	{
    		//������һ��������̶ȵ�����ֵ
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
    	int scaleWidth;       //���������̶ȼ��������ص���
    	scaleWidth = (int) (distancePerScale/distancePerScan*2*pixsPerScan/mZoomX);
    	int showScaleWidth = scaleWidth;   //������ʾ�̶�ֵ�����ؿ��
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
    //�Զ���GestureListener��
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
  		     DebugUtil.i("TAG", "onDoubleTap---->����˫���¼�"); 
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
  		      DebugUtil.i("TAG", "onDoubleTapEvent----->˫���¼��е��¼���Ӧ");  
  		      
  		      return true;  
  	    }  
  		@Override
  		public boolean onSingleTapConfirmed(MotionEvent e)
  		{  
  		    // TODO Auto-generated method stub  
  			DebugUtil.i("TAG", "onSingleTapConfirmed----->˫���¼�������ɵ����¼�");  
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
    	//����ʶ��
	    if(detector.onTouchEvent(event))
		{
			return true;
		}
       	//�õ�����λ��
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
