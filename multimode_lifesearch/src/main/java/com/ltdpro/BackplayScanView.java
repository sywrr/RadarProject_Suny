package com.ltdpro;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class BackplayScanView extends SurfaceView implements SurfaceHolder.Callback{
	private String TAG="BackplayScanView";
	private GestureDetector detector;
	private myGestureListener gListener;
	private SurfaceHolder mSurfaceHolder;
	
	//
	private Paint mTextPaint = new Paint();      //����paint
	private Paint mWavePaint = new Paint();      //�������εĻ���
	private Paint mHardplusNormalPointPaint = new Paint();      //һ��Ӳ�������Ļ���
	private Paint mHardplusSelectPointPaint = new Paint();      //ѡ��Ӳ�������Ļ���
	private Paint mHardplusLinePaint = new Paint();             //Ӳ���������ߵĻ���
	private Paint mGridPaint = new Paint();                     //��������Ҫ�Ļ���
	private int   mSelHardplusIndex=-1;                          //ѡ�е�Ӳ�������
	
	//���崥�����еĲ���
	private final int   ADJUST_NO=0;                        //�����κβ���
	private final int   ADJUST_ONEPOINT_HARDPLUS=1;         //���ڵ�������
	private final int   ADJUST_ALLPOINT_HARDPLUS=2;         //�������е�����
	private final int   ADJUST_SIGNALPOS=3;                 //�����ź�λ��
	private final int   ADJUST_TIMEWINDOW=4;                //����ʱ��
	private int   mWhichOperater=ADJUST_NO;                 //����������ֲ���
	private float mYHadScrollDistance=0;                    //y�������Ѿ������ľ���
	
	////��㴥������(���ڵ���ʱ��)
	//��¼���㰴��ʱ������λ��
	private float  mDownPosX1,mDownPosX2;   //������ĺ�����
	private float  mDownPosY1,mDownPosY2;   //�������������
	//��¼��ǰ�����ƶ�����λ��
	private float  mNowDownPosX1,mNowDownPosX2;
	private float  mNowDownPosY1,mNowDownPosY2;
	//��¼����̧��ʱ������λ��
	private float  mUpPosX1,mUpPosX2;
	private float  mUpPosY1,mUpPosY2;
	
	//�����ڴ������ڲ����Ĺ�������ʾ����Ϣ����
	private final int   SHOW_ADJUST_NO=0;
	private final int   SHOW_ADJUST_TIMEWINDOW=1;
	private final int   SHOW_ADJUST_SIGNALPOS=2;
	private final int   SHOW_ADJUST_ONEHARDPLUS=3;
	private final int   SHOW_ADJUST_ALLHARDPLUS=4;
	private int   mShowAdjustParamType=SHOW_ADJUST_NO;
	
	//
	private float mDownPosX,mDownPosY;
	private short[] mScanDatas=new short[8192];
	private int mScanLen=512;
	public double mZoomParentPlus;   //�Ŵ���
	
	//���㴥��ʱ
	private float mTouchDownXPos;    //�����㰴��ʱ��¼������x
	private float mTouchDownYPos;    //�����㰴��ʱ��¼������y
	private float mNowTouchXPos;     //��ǰ��λ��x
	private float mNowTouchYPos;
	
	//
	private float[] mOldHardPlus={0,0,0,0,0,0,0,0,0};
	private float[] mNowHardPlus={0,0,0,0,0,0,0,0,0};
	private double[]  mZoomplus= new double[8192];
	
	//
	private View mBackplayDIBView;    //
	//
	public void setBackplayDIBView(View view)
	{
		mBackplayDIBView = view;
	}
	//�Զ���GestureListener��
	public class myGestureListener implements GestureDetector.OnGestureListener
	{
		@Override
		public boolean onDown(MotionEvent e) {
			DebugUtil.i(TAG,"onDown");
			mWhichOperater = ADJUST_NO;
			manageGestureDown(e);
			//
			return true;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			DebugUtil.i(TAG,"onSingleTapUp");
//			mWhichOperater=ADJUST_NO;
			return true;
		}
			
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                float distanceY)
		{
			DebugUtil.i(TAG,"onScroll");
//			manageGestureScroll(distanceX,distanceY);
			return true;
		}
			
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY)
		{
			DebugUtil.i(TAG,"onFling");
			return false;
//			return true;
		}
			
		@Override
		public void onLongPress(MotionEvent e)
		{
			DebugUtil.i(TAG,"onLongPress");
		}
			
		@Override
		public void onShowPress(MotionEvent e)
		{
			DebugUtil.i(TAG,"onShowPress");
		}
	}
		
	public void initDatas(Context context)
	{
		mSurfaceHolder = this.getHolder();
		mSurfaceHolder.addCallback(this);
		//
		gListener=new myGestureListener();
		detector = new GestureDetector(context, gListener);
		
		//���ø������ʵ�����
		mWavePaint.setColor(Color.BLACK);
		mWavePaint.setStrokeWidth(2);
		
		mGridPaint.setColor(Color.BLACK);
		mGridPaint.setStrokeWidth((float) 0.5);
		mGridPaint.setAntiAlias(true);
		mGridPaint.setStyle(Paint.Style.STROKE);
		//	
		mHardplusNormalPointPaint.setColor(Color.GREEN);
		mHardplusNormalPointPaint.setStrokeWidth(1);
		//
		mHardplusSelectPointPaint.setColor(Color.RED);
		mHardplusSelectPointPaint.setStrokeWidth(1);
		//
		mHardplusLinePaint.setColor(Color.GREEN);
		mHardplusLinePaint.setStrokeWidth(2);
		
		//��ʾ����ֵ��paint;
		mTextPaint.setColor(Color.GREEN);
		mTextPaint.setStrokeWidth(2);
		mTextPaint.setTextSize(20);
		
		//
		int i;
		for(i=0;i<8192;i++)
			mZoomplus[i] = 1;
		
		//
		mZoomParentPlus = 1.;
	}
	//
	public void initManuplusZooms()
	{
		int i;
		for(i=0;i<8192;i++)
			mZoomplus[i] = 1;
	}
	//���캯��
	public BackplayScanView(Context context)
	{
		super(context);
		initDatas(context);
		
	}
	//
	public void setWaveDatas(short[] waveDatas,int scanLen)
	{
		int i;
		for(i=0;i<scanLen;i++)
			mScanDatas[i] = waveDatas[i];
		//
		mScanLen = scanLen;
	}
	//
	public void setHardplus(float[] plus)
	{
		int i;
		for(i=0;i<9;i++)
		{
			mOldHardPlus[i] = 
					mNowHardPlus[i] = plus[i];
		}
	}
	public void calZoomplus()
	{
		//���㱶��
		int scanLen = mScanLen;
		double[] zoomBase = new double[9];
		
		//��������б��
		int j;
		double temVal;
		double oldZoom;
		MyApplication app;
    	app = (MyApplication)getContext().getApplicationContext();
		for(j=0;j<9;j++)
		{
			if(app.mRadarDevice.mIsUseSoftPlus)
				temVal = mNowHardPlus[j]/20.;
			else
				temVal = (mNowHardPlus[j]-mOldHardPlus[j])/20.;
			temVal = Math.pow(10, temVal);
			zoomBase[j]=temVal;
		}
		//�������Բ�ֵ�㷨����Ŵ���
		int i;
		double scanLenPer;
		int index;
		scanLenPer=scanLen/8.;
		for(i=0;i<8;i++)
		{
			//���㵱ǰ�εķŴ�б��
			double zoom1;
			zoom1=(zoomBase[i+1]-zoomBase[i])/scanLenPer;
			for(j=0;j<(int)scanLenPer;j++)
			{
				index = (int)(j+i*scanLenPer);
				mZoomplus[index] = zoomBase[i]+zoom1*j;
			}
		}
	}
	/**
     * Constructor
     */
    public BackplayScanView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            initDatas(context);
     }

    /**
     * Constructor
     */
    public BackplayScanView(Context context, AttributeSet attrs) {
            super(context, attrs);
            initDatas(context);
    }   
    
	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		
	}
	//�ж��Ƿ������ڵ���ʱ��
	public boolean isAdjustingTimewindow(float xDistance,float yDistance)
	{
		//y����������x������룬����x�������С
		if(Math.abs(yDistance) > Math.abs(xDistance) &&
           Math.abs(xDistance) <= 5)
		{
			return true;
		}
		//
		return false;
	}
	//�ж��Ƿ����ڵ����ź�λ��
	public boolean isAdjustingSignalpos(float distanceX,float distanceY)
	{
		//y����������x������룬����x�������С
		if(Math.abs(distanceY) > Math.abs(distanceX) &&
           Math.abs(distanceX) <= 15 &&
           ((mWhichOperater == ADJUST_NO)||(mWhichOperater == ADJUST_SIGNALPOS)))
		{
			return true;
		}
		//
		return false;
	}
	//�ж��Ƿ����ڵ���������������
	public boolean isAdjustingAllHardplus(float distanceX,float distanceY)
	{
		//x����������y������룬����y�������С
		if(Math.abs(distanceX) > Math.abs(distanceY) &&
           Math.abs(distanceY) <= 15 &&
           ((mWhichOperater == ADJUST_NO)||(mWhichOperater == ADJUST_ALLPOINT_HARDPLUS))
				)
		{
			return true;
		}
		//
		return false;
	}
	//�õ������Ӳ����������
	public int hitWhichHardplus(MotionEvent e)
	{
		MyApplication app;
		app = (MyApplication)this.getContext().getApplicationContext();
		//
		int i;
		float xPos,yPos;
		float xPosHardplus,yPosHardplus;
		xPos=e.getX();
		yPos=e.getY();
		float range = app.mRadarDevice.getHardplusRange();
		float mixVal = app.mRadarDevice.getMixHardplus();
		float maxVal = app.mRadarDevice.getMaxHardplus();
			
		//
		float perH = (float)getHeight()/(float)8;
		float perW = getWidth()/range;
		
		//
		float[] vals=mNowHardPlus;
		float left,right,top,bottom;
		int ret = -1;
		for(i=0;i<9;i++)
		{
			xPosHardplus=(vals[i]-mixVal)*perW;
			yPosHardplus=i*perH;
			left=xPosHardplus-hardPlusPointR*2;
			right=xPosHardplus+hardPlusPointR*2;
			top=yPosHardplus-hardPlusPointR*2;
			bottom=yPosHardplus+hardPlusPointR*2;
			if(xPos>=left && xPos<=right && yPos>=top && yPos<=bottom)
			{
				ret = i;
				break;
			}
		}
		//
		return ret;
	}
	//����"���"����
	public boolean manageGestureDown(MotionEvent e)
	{
		//��¼�µ�ǰ�Ĵ�����λ��
		mTouchDownXPos = e.getX();
		mTouchDownYPos = e.getY();
		mNowTouchXPos = mTouchDownXPos;
		mNowTouchYPos = mTouchDownYPos;
			
		//�ж��Ƿ������������ߵ���
		int whichHardplus;
		whichHardplus = hitWhichHardplus(e);
		mSelHardplusIndex=whichHardplus;
		mYHadScrollDistance = 0;
		if(whichHardplus == -1)
		{
			mWhichOperater=ADJUST_NO;
			mShowAdjustParamType=SHOW_ADJUST_NO;
			invalidate();
			return true;
		}
		//
		mWhichOperater=ADJUST_ONEPOINT_HARDPLUS;
		invalidate();
		  
		//
		return true;
	}
	
	public boolean manageGestureScroll(float distanceX,float distanceY)
	{
		//��¼��ǰ����
		mNowTouchXPos += distanceX;
		mNowTouchYPos += distanceY;
		
		//������������
		float[] vals;
		float xPerWidth;
		MyApplication app;
		app = (MyApplication)this.getContext().getApplicationContext();
		float range = app.mRadarDevice.getHardplusRange();
		xPerWidth=getWidth()/range;
		float maxHardPlus,mixHardPlus;
		maxHardPlus = app.mRadarDevice.getMaxHardplus();
		mixHardPlus = app.mRadarDevice.getMixHardplus();
		if(mWhichOperater == ADJUST_ONEPOINT_HARDPLUS)
		{
			DebugUtil.i(TAG,"Adjusting one hardpluspoint");
			float val=mNowHardPlus[mSelHardplusIndex];
			val += (-distanceX)/xPerWidth;
			if(val>maxHardPlus)
				val=maxHardPlus;
			if(val<mixHardPlus)
				val=mixHardPlus;
			mNowHardPlus[mSelHardplusIndex] = val;
			//
			mShowAdjustParamType=SHOW_ADJUST_ONEHARDPLUS;
			mWhichOperater = ADJUST_ONEPOINT_HARDPLUS;
			invalidate();
			return true;
		}
			
		//����������������
//		if(mWhichOperater == ADJUST_NO )
		{
			//���������
			if(isAdjustingAllHardplus(distanceX,distanceY))
			{
				int i;
				float val;
				float addVal=(-distanceX)/xPerWidth;
				for(i=0;i<9;i++)
				{
					val=mNowHardPlus[i];
					val += addVal;
					if(val>maxHardPlus)
						val=maxHardPlus;
					if(val<mixHardPlus)
						val=mixHardPlus;
					mNowHardPlus[i] = val;
				}
				DebugUtil.i(TAG,"Adjusting all HardplusPoints");
				//
				invalidate();
				mWhichOperater = ADJUST_ALLPOINT_HARDPLUS;
				mShowAdjustParamType=SHOW_ADJUST_ALLHARDPLUS;
				return true;
			}
		}
		//
		return true;
	}

	//
	public void drawWave(Canvas canvas)
	{
		float xOrg,yOrg;
		float xPos,yPos;
		float perW;
		float perH;
		float befPosX,befPosY;
		
		//
		perH = getHeight();
		perH = perH/mScanLen;
		xOrg = befPosX = getWidth()/2;
		yOrg = befPosY = 0;
		perW = 65534/getWidth();
		for(int i=0;i<mScanLen;i++)
    	{
			xPos = (float) (xOrg + mScanDatas[i]/perW*mZoomplus[i]);
			yPos = yOrg + i*perH;
			canvas.drawLine(befPosX, befPosY, xPos, yPos, mWavePaint);
			//
			befPosX = xPos;
			befPosY = yPos;
    	}
		
		MyApplication app;
		app = (MyApplication)this.getContext().getApplicationContext();
		//�������ڵ�������ʱ�Ĳ���
		if(mShowAdjustParamType == SHOW_ADJUST_ONEHARDPLUS ||
		   mShowAdjustParamType == SHOW_ADJUST_ALLHARDPLUS)
		{
			//���㱶��
			int scanLen = mScanLen;
			double[] zoomVals = new double[scanLen];
			double[] zoomBase = new double[9];
			
			//��������б��
			
			int j;
			double temVal;
			double oldZoom;
			for(j=0;j<9;j++)
			{
				if(app.mRadarDevice.mIsUseSoftPlus)
					temVal = mNowHardPlus[j]/20.;
				else
					temVal =(mNowHardPlus[j]-mOldHardPlus[j])/20.;
				temVal = Math.pow(10, temVal);
				zoomBase[j]=temVal;
				//
				DebugUtil.i(TAG,"new hardplus:="+mNowHardPlus[j]+"zoomBase["+(j)+"]="+zoomBase[j]);
			}
			//�������Բ�ֵ�㷨����Ŵ���
			int i;
			double scanLenPer;
			int index;
			scanLenPer=scanLen/8.;
			for(i=0;i<8;i++)
			{
				//���㵱ǰ�εķŴ�б��
				double zoom1;
				zoom1=(zoomBase[i+1]-zoomBase[i])/scanLenPer;
				for(j=0;j<(int)scanLenPer;j++)
				{
					index = (int)(j+i*scanLenPer);
					zoomVals[index] = zoomBase[i]+zoom1*j;
				}
			}
			
			//�����µķŴ������㲢��������
			perH = getHeight();
			perH = perH/mScanLen;
			xOrg = befPosX = getWidth()/2;
			yOrg = befPosY = 0;
			perW = 65534/getWidth();
			double temValD;
			
			for(i=0;i<mScanLen;i++)
	    	{
				temValD = mScanDatas[i];
				temValD *= zoomVals[i];
				temValD *= mZoomParentPlus;
				xPos = xOrg +(float)(mScanDatas[i]/perW*zoomVals[i]);
				yPos = yOrg + i*perH;
				canvas.drawLine(befPosX, befPosY, xPos, yPos, mHardplusSelectPointPaint);
				//
				befPosX = xPos;
				befPosY = yPos;
	    	}
		}
	}
	private int hardPlusPointR=16;
	//����Ӳ����������
	public void drawHardplus(Canvas canvas)
	{
		float xOrg,yOrg;
		float xPos,yPos;
		float perW;
		float perH;
		float befPosX,befPosY;
		MyApplication app;
		app=(MyApplication)(getContext().getApplicationContext());
		float range = app.mRadarDevice.getHardplusRange();
		float mixVal = app.mRadarDevice.getMixHardplus();
		float maxVal = app.mRadarDevice.getMaxHardplus();
		
		//
		perH = getHeight();
		perH = perH/8;
		xOrg = befPosX = 0;
		yOrg = befPosY = 0;
		perW = getWidth()/range;
		
		//
		int i;
		float val;
		for(i=0;i<9;i++)
		{
			val=mNowHardPlus[i];
			xPos=xOrg+(val-mixVal)*perW;
			yPos=yOrg+i*perH;
			if(i==0)
				yPos += hardPlusPointR;
			if(i==8)
				yPos -= hardPlusPointR;
			if(i==0)
			{
				befPosX=xPos;
				befPosY=yPos;
			}
			else
			{
				canvas.drawLine(befPosX, befPosY, xPos, yPos, mHardplusLinePaint);
				befPosX=xPos;
				befPosY=yPos;
			}
			if(i!=mSelHardplusIndex)
			{
				canvas.drawCircle(xPos, yPos, hardPlusPointR, mHardplusNormalPointPaint);
			}
			else
			{
				canvas.drawCircle(xPos, yPos, hardPlusPointR, mHardplusSelectPointPaint);
			}
			
			//������ڵ�����������
			if(mShowAdjustParamType == SHOW_ADJUST_ALLHARDPLUS)
			{
				int intVal;
				intVal = (int)val;
				String text= "" + intVal + "dB";
				canvas.drawText(text, xPos+hardPlusPointR+2, yPos+hardPlusPointR, mTextPaint);//mHardplusNormalPointPaint);
			}
		}
	}
	//��ʾ���ڵ��ڵĲ���
	public void drawAdjustParam(Canvas canvas)
	{
		float xPos,yPos;
		xPos=this.getWidth()-100;
		yPos=50;
		
		//
		String text="";
		int val;
		switch(mShowAdjustParamType)
		{
		case SHOW_ADJUST_ONEHARDPLUS:
			val=(int)(mNowHardPlus[mSelHardplusIndex]);
			text="����:"+val+"dB";
			break;
		}
		canvas.drawText(text, xPos-10, yPos, mTextPaint);
	}
	
	//�������κ���
	@Override
	protected void onDraw(Canvas canvas)
	{
		DebugUtil.i(TAG,"onDraw");
		drawGrid(canvas);
		drawHardplus(canvas);
		drawWave(canvas);
		drawAdjustParam(canvas);
	}
	
	////////////////////////////////////////////////////////////////
	public void drawGrid(Canvas canvas)
	{
		float xOrg,yOrg;
		float xPos,yPos;
		float perW;
		float perH;
		float befPosX,befPosY;
		PathEffect effects = new DashPathEffect(new float[]{5, 5, 5, 5}, 1);
		//
		perH = getHeight();
		perH = (float) (perH/8.);
		xOrg = 0;
		yOrg = 0;
		perW = (float) (getWidth()/14.);
		
		//
		mGridPaint.setPathEffect(effects);
		Path path = new Path();
		int i;
		for(i=0;i<=8;i++)
		{
			xOrg = 0;
			yOrg = i*perH;
			xPos = getWidth();
			yPos = yOrg;
			path.moveTo(xOrg, yOrg);
			path.lineTo(xPos, yPos);
			canvas.drawPath(path, mGridPaint);
//			canvas.drawLine(xOrg, yOrg, xPos, yPos, mGridPaint);
		}
		for(i=0;i<=14;i++)
		{
			xOrg = i*perW;
			yOrg = 0;
			xPos = xOrg;
			yPos = getHeight();
			path.moveTo(xOrg, yOrg);
			path.lineTo(xPos, yPos);
			canvas.drawPath(path, mGridPaint);
//			canvas.drawLine(xOrg, yOrg, xPos, yPos, mGridPaint);
		}
	}
	
	public void setTimewindFromeManagePoints()
	{
		//���ݵ�ǰλ�ü��㵱ǰʱ��ֵ
		float oldYDistance;
		float nowYDistance;
		oldYDistance = Math.abs(mDownPosY2 - mDownPosY1);
		nowYDistance = Math.abs(mNowDownPosY2 - mNowDownPosY1);
		//�õ���ʱ����������ʱ��
		MyApplication app;
		app=(MyApplication)(getContext().getApplicationContext());
		if(app.mRadarDevice.isSavingMode()||
			!app.mRadarDevice.isRunningMode())
			return;
		int timeWnd;
		timeWnd=app.mRadarDevice.getTimeWindow();
		float perWnd;
		perWnd = ((float)timeWnd)/((float)this.getHeight());
		
		timeWnd += (oldYDistance-nowYDistance)*perWnd;
		timeWnd = app.mRadarDevice.checkTimewnd(timeWnd);
		DebugUtil.i(TAG,"Now Timewnd:="+timeWnd+" ns");
		
		//
		app.mRadarDevice.setTimeWindow(timeWnd,true);
		
		//����ʱ����ʾֵ
//		IDSC2600MainActivity activity;
//		activity = (LTDMainActivity)getContext();
//		activity.setRealtimeParamsListAdapterText(timeWnd,activity.COMMAND_TIMEWINDOW_ID);
//		activity.setTimewindowWheelValue(timeWnd);
	}
	//��㴥�ش���
	public boolean manageManyPoints(MotionEvent event)
	{
		int count;
		count = event.getPointerCount();
		DebugUtil.i(TAG,"Now PointsNumber:="+count);
		
		//
		int p1=event.findPointerIndex(0);
		int p2=event.findPointerIndex(1);
		float nowPosX1,nowPosX2,nowPosY1,nowPosY2;
		switch(event.getAction())
		{
		case MotionEvent.ACTION_POINTER_1_DOWN:
			mDownPosX1=event.getX(p1);
			mDownPosY1=event.getY(p1);
			DebugUtil.i(TAG,"Point1_Down x:="+mDownPosX1+";  y:="+mDownPosY1);
			break;
		case MotionEvent.ACTION_POINTER_1_UP:
			nowPosX1=event.getX(p1);
			nowPosY1=event.getY(p1);
			//������ڵ���ʱ����־
			mWhichOperater=ADJUST_NO;
			mShowAdjustParamType=SHOW_ADJUST_NO;
			//����ʱ��ֵ
			setTimewindFromeManagePoints();
			DebugUtil.i(TAG,"Point1_Up x:="+nowPosX1+"; Y:="+nowPosY1);
			break;
		case MotionEvent.ACTION_POINTER_2_DOWN:
			mDownPosX1=event.getX(p1);
			mDownPosY1=event.getY(p1);
			mDownPosX2=event.getX(p2);
			mDownPosY2=event.getY(p2);
			//�������ڵ���ʱ����־
			mWhichOperater=ADJUST_TIMEWINDOW;
			mShowAdjustParamType=SHOW_ADJUST_TIMEWINDOW;
			DebugUtil.i(TAG,"Point2_1_Down x:="+mDownPosX2+";  Y:="+mDownPosY2);
			DebugUtil.i(TAG,"Point2_2_Down x:="+mDownPosX2+";  Y:="+mDownPosY2);
			break;
		case MotionEvent.ACTION_POINTER_2_UP:
			nowPosX2=event.getX(p1);
			nowPosY2=event.getY(p1);
			//������ڵ���ʱ����־
			mWhichOperater=ADJUST_NO;
			mShowAdjustParamType=SHOW_ADJUST_NO;
			DebugUtil.i(TAG,"Point2_Up x:="+nowPosX2+"; Y:="+nowPosY2);
			//����ʱ��ֵ
			setTimewindFromeManagePoints();
			break;
		case MotionEvent.ACTION_MOVE:
			mNowDownPosX1=nowPosX1=event.getX(p1);
			mNowDownPosY1=nowPosY1=event.getY(p1);
			mNowDownPosX2=nowPosX2=event.getX(p2);
			mNowDownPosY2=nowPosY2=event.getY(p2);
			DebugUtil.i(TAG,"Point1 xDistance:="+(nowPosX1-mDownPosX1)+";  yDistance:="+(nowPosY1-mDownPosY1));
			DebugUtil.i(TAG,"Point2 xDistance:="+(nowPosX2-mDownPosX2)+";  yDistance:="+(nowPosY2-mDownPosY2));
			break;
		}
		
		//
		invalidate();
		return true;
	}
	
	//�����¼�
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
//	    DebugUtil.i(TAG,"****onTouchEvent****");
		/*
	    //��㴥��
	    if(event.getPointerCount()>=2)
	    {
	    	manageManyPoints(event);
	    	return true;
	    }
	    */
	    //����ʶ��
	    if(detector.onTouchEvent(event))
		{
			return true;
		}
	    ////
	    switch(event.getAction())
	    {
		    case MotionEvent.ACTION_UP:
		    case MotionEvent.ACTION_CANCEL:
		    {
		    	DebugUtil.i(TAG,"onTouchEvent:ACTION_UP");
		    	manageTouchUp();
		    	mWhichOperater = ADJUST_NO;
		    	mShowAdjustParamType = SHOW_ADJUST_NO;
		    	//
		    	invalidate();
		    	break;
		    }
		    
	    }
	    return true;
	}
	//
	public void manageTouchUp()
	{
		backPlayDIBView dibView;
		dibView = (backPlayDIBView)mBackplayDIBView;
		//������������
		float[] vals;
		vals=mNowHardPlus;
		if(mWhichOperater == ADJUST_ONEPOINT_HARDPLUS)
		{
			float val=vals[mSelHardplusIndex];
			if(val>130)
				val=130;
			if(val<-10)
				val=-10;
			vals[mSelHardplusIndex] = val;
			dibView.setManuHardplus(vals);
			//
			mShowAdjustParamType=SHOW_ADJUST_ONEHARDPLUS;
			dibView.reloadCurrentDIB(dibView.mZoomX);
			//�������汶��
			calZoomplus();
			invalidate();
		}				
		//����������������
//		if(mWhichOperater == ADJUST_NO)
		{
			//���������
			if(mWhichOperater == ADJUST_ALLPOINT_HARDPLUS)
			{
				dibView.setManuHardplus(vals);
				dibView.reloadCurrentDIB(dibView.mZoomX);
				//�������汶��
				calZoomplus();
				invalidate();
			}
		}
		//
		mWhichOperater = ADJUST_NO;
	}
	
	//��������ֵ
	public void addHardPlus(float plus)
	{
		backPlayDIBView dibView;
		dibView = (backPlayDIBView)mBackplayDIBView;
		//������������
		float[] vals;
		vals=mNowHardPlus;
		if(mWhichOperater == ADJUST_ONEPOINT_HARDPLUS)
		{
			float val=vals[mSelHardplusIndex];
			val += plus;
			if(val>130)
				val=130;
			if(val<-10)
				val=-10;
			vals[mSelHardplusIndex] = val;
			dibView.setManuHardplus(vals);
			//
			mShowAdjustParamType=SHOW_ADJUST_ONEHARDPLUS;
			dibView.reloadCurrentDIB(dibView.mZoomX);
			//�������汶��
			calZoomplus();
			invalidate();
		}				
		//����������������
		else
		{
			//���������
//			if(mWhichOperater == ADJUST_ALLPOINT_HARDPLUS)
			{
				mShowAdjustParamType=SHOW_ADJUST_ALLHARDPLUS;
				for(int i=0;i<9;i++)
				{
					float val=vals[i];
					val += plus;
					if(val>130)
						val=130;
					if(val<-10)
						val=-10;
					vals[i] = val;
				}
				dibView.setManuHardplus(vals);
				dibView.reloadCurrentDIB(dibView.mZoomX);
				//�������汶��
				calZoomplus();
				invalidate();
			}
		}
	}
	//
	private View.OnTouchListener handlerTouchEvent = new View.OnTouchListener()
	{	
		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			// TODO Auto-generated method stub
			DebugUtil.i(TAG,"onTouch");
			if(detector.onTouchEvent(arg1))
			{
				return true;
			}
			/*
			else
			{
				return super.onTouchEvent(arg1);
			}
			*/
			return true;
		}
	};
	
	//
	public void redrawSignalposVal()
	{
		Canvas lCanvas = mSurfaceHolder.lockCanvas();
		drawAdjustParam(lCanvas);
		mSurfaceHolder.unlockCanvasAndPost(lCanvas);
	}
	public void redrawHardplusVal()
	{
		Canvas lCanvas = mSurfaceHolder.lockCanvas();
		drawAdjustParam(lCanvas);
		mSurfaceHolder.unlockCanvasAndPost(lCanvas);
	}
	public void setZoomPlus(double zoom)
	{
		mZoomParentPlus = zoom;
	}
}
