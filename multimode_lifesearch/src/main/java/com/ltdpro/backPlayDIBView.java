package com.ltdpro;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ltd.multimode_lifesearch.R;
import com.ltd.multimodelifesearch.activity.MultiModeLifeSearchActivity;

import java.io.File;
import java.io.FileInputStream;

/*
 * 实现      "类似采集"  的回放
 */
public class backPlayDIBView extends View {
	
	private String TAG="BackPlayDIBView";       //该类的标识
	public Activity mParentActivity;
	///回放文件相关变量
	private String mFilename;
	private FileInputStream mBackplayFile=null;      //
	private long mHadReadLength = 0;                 //已经读取的雷达数据长度（字节）
	private long mBackfileLength = 0;                //回放数据文件的长度(字节):包括文件头
	public FileHeader mBackplayFileHeader = new FileHeader();   //回放数据文件头
	private long mHeadReadPos;        //头位置
	public  long mTailReadPos;        //尾位置
	public int  mZoomX=1;            //缩放系数
	
	///回放位图
	private int mDIBHeight = 512;
	private int mDIBWidth  = 1000;  //1280;  //2016.6.10
	private int mBegShowDIBScan = 0;        //当前在屏幕上显示的起始道号
	private int mHadCreateDIBWidth = 0;     //已经生成的位图宽度
	private int[] mDIBPixels = new int[mDIBHeight*mDIBWidth];
	
	////雷达数据实时读取定时器
	private Handler mBackplayHandler = new Handler();
	private long mBackplayDelayTime = 100L;
	private boolean  mIsBackplaying = false;    //正在回放标志
	private boolean  mBackplayPause = false;    //暂停回放
	private int  mBackplaySpeed=5;              //回放速度
	
	private final int BACKPLAY_NO=0;
	private final int BACKPLAY_FORWARD_DIR=1;      //向前滚动
	private final int BACKPLAY_BACK_DIR=2;         //向后滚动
	private int  mBackplayDir = BACKPLAY_NO;
	//
	private boolean mIsStopBackplayTimer=false;
	
	//
	private GestureDetector detector;
	private myGestureListener gListener;
	
	//定义一些在暂停模式下触点操作的变量
	private float mSingleDownPosX;    //单点点击x坐标
	private float mSingleDownPosY;    //单点点击y坐标
	private float mSingleNowPosX;     //单点移动时x坐标
	private float mSingleNowPosY;     //单点移动时y坐标
	
	//定义选择区域操作
	private boolean mIsSelectRect=false;    //
	Rect mSelectRect;   //选择的区域范围
	private double mZoomPlus = 1.0;
	
	//是否正在标定距离
	private boolean mIsDistanceMark=false;
	private boolean mFirstPointsExist=false;  //第一个起始点是否已经存在
	private boolean mSecondPointsExist = false;
	private float mFirstMarkDownPosX,mFirstMarkDownPosY;
	private float mSecondMarkDownPosX,mSecondMarkDownPosY;

	public long mTotalScans=0;   //总共回放的道数
	
	//背景数据
	private int BACKGROUND_DEFAULTSCANS=300;
	private double[] mBackgroundDatas = new double[8192];
	private int mBackgroundScans = 300;
	private double mHadRcvBackgroundScans = 0;
	private boolean mIsBackgroundOk = false;   //背景数据就绪
	public  boolean mIsRemoveBackground = false;  //是否进行背景消除
	private short[] mFirstBackgroundDatas = new short[8192];
	
	//显示类型
	private int DIB_BITMAP=1;
	private int WIGGLE_BITMAP=2;
	private int mDIBType = DIB_BITMAP;//WIGGLE_BITMAP;//
		
	//手动增益设置参数
	private int mNowAdjustScan=0;    //当前手动调节增益的道号
	public boolean mIsAdjustManuPlus = false;    //是否手动调节增益
	private short[]  mManuplusScanDatas = new short[8192];      //正在调整的数据道原始数据
	private double[] mManuplus = new double[8192];              //增益值
	private Paint mWavePaint = new Paint();      //单道波形的画笔
	private Paint mHardplusNormalPointPaint = new Paint();      //一般硬件增益点的画笔
	private Paint mHardplusSelectPointPaint = new Paint();      //选中硬件增益点的画笔
	private Paint mHardplusLinePaint = new Paint();             //硬件增益曲线的画笔
	private Paint mHardplusRangePaint = new Paint();
	private int   mSelHardplusIndex=-1;                          //选中的硬件增益点
	private int   mManuplusRangeWidth = 200;
	private float[]  mManuPlusPointsVal = new float[9];         //手动增益点数值
	
	//构造函数
	public backPlayDIBView(Context context)
	{
	    super(context);
	    InitData(context);
	}
	/**
     * Constructor
     */
    public backPlayDIBView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            InitData(context);
    }

    /**
     * Constructor
     */
    public backPlayDIBView(Context context, AttributeSet attrs) {
            super(context, attrs);
            InitData(context);
    }   
    
    //
    public void setManuAdjustScanDatas()
    {
    	//计算当前调整例子波形位置
    	long rPos;
    	rPos = (mTailReadPos+mHeadReadPos)/2;
    	int scanLen;
    	scanLen = mBackplayFileHeader.rh_nsamp*2;
    	int scans;
    	scans = (int) ((rPos-1024)/scanLen);
    	mNowAdjustScan = scans;
    	
    	//读取一道数据
    	long offset;
    	int retLen;
    	byte[] buf = new byte[scanLen*2];
    	offset = 1024+scanLen*scans;
    	try{
    		mBackplayFile = new FileInputStream(mFilename);
    		//移动文件读取指针到指定位置
    		mBackplayFile.skip(offset);
    		retLen=mBackplayFile.read(buf, 0, scanLen);
    	}
    	catch(Exception e)
    	{
    		return;
    	}
    	//
		int i,j;
		short temVal;
		short temVal1;
		int newLen = retLen/2;
		for(i=0;i<newLen;i++)
		{
			temVal = 0;
			temVal = buf[i*2];
			temVal &= 0xff;
			temVal1=0;
			temVal1 = buf[i*2+1];
			temVal1=(short)(temVal1<<8);
			temVal1 &= 0xff00;
			temVal = (short)(temVal + temVal1);
			mManuplusScanDatas[i] = temVal;
		}
    	//
		try{
			mBackplayFile.close();
		}
		catch(Exception e)
		{
			DebugUtil.e(TAG, "readDatas fail!");
		}
		
		//
		BackplayScanView backView=(BackplayScanView)mParentActivity.findViewById(R.id.backplayScanview);
		backView.setWaveDatas(mManuplusScanDatas,scanLen/2);
		backView.invalidate();
    }
    //
    public void setActivity(Activity activity)
    {
    	mParentActivity = activity;
    }
    //
	public void setManuHardplus(float[] plus)
	{
		int i;
		for(i=0;i<9;i++)
		{
			mManuPlusPointsVal[i] = plus[i];
		}
		//计算波形的放大倍数
		//计算倍数
		int scanLen = mBackplayFileHeader.rh_nsamp;
		double[] zoomBase = new double[9];
		
		//计算各点的斜率
		int j;
		double temVal;
		double oldZoom;
		//
		MyApplication app;
    	app = (MyApplication)getContext().getApplicationContext();
		
		float[] mOldHardPlus = mBackplayFileHeader.getHardplus();
		for(j=0;j<9;j++)
		{
			if(app.mRadarDevice.mIsUseSoftPlus)
				temVal = mManuPlusPointsVal[j]/20.;
			else
				temVal = (mManuPlusPointsVal[j]-mOldHardPlus[j])/20.;
			temVal = Math.pow(10, temVal);
			zoomBase[j]=temVal;
			//
			DebugUtil.i(TAG, "new hardplus:=" + mManuPlusPointsVal[j] + "zoomBase[" + (j) + "]=" + zoomBase[j]);
		}
		//利用线性插值算法计算放大倍数
		double scanLenPer;
		int index;
		scanLenPer=scanLen/8.;
		for(i=0;i<8;i++)
		{
			//计算当前段的放大斜率
			double zoom1;
			zoom1=(zoomBase[i+1]-zoomBase[i])/scanLenPer;
			for(j=0;j<(int)scanLenPer;j++)
			{
				index = (int)(j+i*scanLenPer);
				mManuplus[index] = zoomBase[i]+zoom1*j;
			}
		}
	}
	
    //显示手动增益调整窗口
	public void showManuplusAdjustRange()
    {
    	//计算当前调整例子波形位置
    	long rPos;
    	rPos = (mTailReadPos+mHeadReadPos)/2;
    	int scanLen;
    	scanLen = mBackplayFileHeader.rh_nsamp*2;
    	int scans;
    	scans = (int) ((rPos-1024)/scanLen);
    	mNowAdjustScan = scans;
    	
    	//读取一道数据
    	long offset;
    	int retLen;
    	byte[] buf = new byte[scanLen*2];
    	offset = 1024+scanLen*scans;
    	try{
    		mBackplayFile = new FileInputStream(mFilename);
    		//移动文件读取指针到指定位置
    		mBackplayFile.skip(offset);
    		retLen=mBackplayFile.read(buf, 0, scanLen);
    		for(int i = 0 ;i < 10;i++)
    		{
    			DebugUtil.i(TAG, "buf[" + i + "]=" + buf[i]);
    		}
    	}
    	catch(Exception e)
    	{
    		return;
    	}
    	//
		int i,j;
		short temVal;
		short temVal1;
		int newLen = retLen/2;
		for(i=0;i<newLen;i++)
		{
			temVal = 0;
			temVal = buf[i*2];
			temVal &= 0xff;
			temVal1=0;
			temVal1 = buf[i*2+1];
			temVal1=(short)(temVal1<<8);
			temVal1 &= 0xff00;
			temVal = (short)(temVal + temVal1);
			mManuplusScanDatas[i] = temVal;
		}
    	//
		try{
			mBackplayFile.close();
		}
		catch(Exception e)
		{
			DebugUtil.e(TAG, "readDatas fail!");
		}
		
		//
		BackplayScanView backView=(BackplayScanView)mParentActivity.findViewById(R.id.backplayScanview);
		for(int t = 0;t<10;t++)
		{
			DebugUtil.i(TAG, "mManuplusScanDatas[" + t + "]=" + mManuplusScanDatas[t]);
		}
		backView.setWaveDatas(mManuplusScanDatas,scanLen/2);
		backView.setHardplus(mBackplayFileHeader.getHardplus());
		backView.initManuplusZooms();
		backView.calZoomplus();
		backView.invalidate();
		
		//
//		Button button;
//		button = (Button)mParentActivity.findViewById(R.id.buttonBackplayHardplusDel1);
//		button.setVisibility(View.VISIBLE);
//		button = (Button)mParentActivity.findViewById(R.id.buttonBackplayHardplusAdd1);
//		button.setVisibility(View.VISIBLE);
    	//
    	mIsAdjustManuPlus = true;
    	invalidate();
    }
    
    //隐藏手动增益调节窗口
    public void hideManuplusAdjustRange()
    {
    	mIsAdjustManuPlus = false;
    	int i;
//    	for(i=0;i<8192;i++)
//    		mManuplus[i]=1;
    	
    	//
		reloadCurrentDIB(mZoomX);
		
    	//
    	invalidate();
    }
    
    public boolean isManuPlus()
    {
    	return mIsAdjustManuPlus;
    }
    public boolean isRemback()
    {
    	return mIsRemoveBackground;
    }
    public boolean isMarkDistance()
    {
    	return mIsDistanceMark;
    }
    //根据当前x坐标位置，得到对应的道号
    public int getScanindexFromeXPos(float posX)
    {
    	int scanIndex = 0;
    	int scanLen;
    	scanLen = mBackplayFileHeader.rh_nsamp*2;
    	MyApplication app;
    	app = (MyApplication)getContext().getApplicationContext();
    	int srcWidth;
    	srcWidth = app.getScreenWidth();
    	//计算终止道号
    	int tailScanIndex;
    	tailScanIndex = (int) ((mTailReadPos-1024)/scanLen);
    	
    	//
    	double pixsDiff = srcWidth-posX;
    	double pixsPerScan = 1;
    	if(isShowWiggle())
    		pixsPerScan = app.getWigglePixsPerScan();
    	pixsPerScan = pixsPerScan/mZoomX;
    	
    	//
    	scanIndex = (int) (tailScanIndex - pixsDiff/pixsPerScan);
    	DebugUtil.i(TAG, "Now distanceMark getScanFromePosX:=" + scanIndex);
    	//
    	return scanIndex;
    }
    
    
    /////自定义GestureListener类
    /*
     * 除了一些特殊处理外，大部分处理反悔了false，以便让视窗的继续onTouchEvent处理这些消息
     */
  	public class myGestureListener implements GestureDetector.OnGestureListener, OnDoubleTapListener
  	{
  		@Override
  		public boolean onDown(MotionEvent e) {
  			DebugUtil.i(TAG, "onDown");
  			//进行距离和深度的信息提示
  			if(mIsDistanceMark)
  			{
  				if(!mFirstPointsExist)
  				{
  					mFirstPointsExist = true;
  					mSecondMarkDownPosX = mFirstMarkDownPosX = e.getX();
  					mSecondMarkDownPosY = mFirstMarkDownPosY = e.getY();
  					DebugUtil.i(TAG, "First downX:=" + mFirstMarkDownPosX + ";downY:=" + mFirstMarkDownPosY);
  					//显示距离标定点位置
  					showMarkPoints();
  				}
  				else
  				{
  					mSecondPointsExist = true;
  					mSecondMarkDownPosX = e.getX();
  					mSecondMarkDownPosY = e.getY();
  					DebugUtil.i(TAG, "Second downX:=" + mSecondMarkDownPosX + ";downY:=" + mSecondMarkDownPosY);
  					//显示距离标定点位置
  					showMarkPoints();
  					////显示距离信息
  					showMarkDistanceMsg();
  				}
  				//
  				return true;
  			}
  			
  			return false;
//  			return true;
  		}
  		
  		@Override
  		public boolean onSingleTapUp(MotionEvent e) {
  			DebugUtil.i(TAG, "onSingleTapUp");
  			return false;
//  			return true;
  		}
  		
  		@Override
  		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                float distanceY)
  		{
  			DebugUtil.i(TAG, "onScroll");
  			if(mIsDistanceMark)
  			{
  				return true;
  			}
  			return false;
//  			return true;
  		}
  		
  		@Override
  		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY)
  		{
  			DebugUtil.i(TAG, "onFling");
  			return false;
//  			return false;
  		}
  		
  		@Override
  		public void onLongPress(MotionEvent e)
  		{
  			DebugUtil.i(TAG, "onLongPress");
  			//更改图像的显示类型
//			changeDIBType();
  		}
  		
  		@Override
  		public void onShowPress(MotionEvent e)
  		{
  			DebugUtil.i(TAG, "onShowPress");
  		}
  		
  		@Override
  		public boolean onDoubleTap(MotionEvent e)
  		{  
  			 // TODO Auto-generated method stub  
  		     DebugUtil.i(TAG, "onDoubleTap---->处理双击事件");
  		     restoreZoomX();
   		     return true;
  		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent arg0) {
			// TODO Auto-generated method stub
			return false;
		}
  	}
  	
  	public void changeDIBType()
	{
		MyApplication app;
		app = (MyApplication)this.getContext().getApplicationContext();
		BackPlayHRulerView rulerView;
//		rulerView = (BackPlayHRulerView)(mParentActivity.findViewById(R.id.backplay_HRuler));
		if(mDIBType == DIB_BITMAP)
		{
			mDIBType = WIGGLE_BITMAP;
			Toast.makeText(this.getContext(), "进入堆积图显示模式", Toast.LENGTH_SHORT).show();
			//设置水平标尺参数
//			rulerView.setPixsPerScan(app.getWigglePixsPerScan());
		}
		else
		{
			mDIBType = DIB_BITMAP;
			Toast.makeText(this.getContext(), "进入彩色图显示模式", Toast.LENGTH_SHORT).show();
			//设置水平标尺参数
//			rulerView.setPixsPerScan(1);
		}
	}
  	
  	//更改调色板
  	public void changeColorPal()
  	{
  		if(!isBackPlaying())
  			return;
  		//
  		reloadCurrentDIB(mZoomX);
  	}
  	public void changeToDIBMode()
  	{
  		MyApplication app;
		app = (MyApplication)this.getContext().getApplicationContext();
		BackPlayHRulerView rulerView;
//		rulerView = (BackPlayHRulerView)(mParentActivity.findViewById(R.id.backplay_HRuler));
		mDIBType = DIB_BITMAP;
		Toast.makeText(this.getContext(), "进入彩色图显示模式", Toast.LENGTH_SHORT).show();
		//设置水平标尺参数
//		rulerView.setPixsPerScan(1);
		
		//
		reloadCurrentDIB(mZoomX);
  	}
  	public void changeToWiggleMode()
  	{
  		MyApplication app;
		app = (MyApplication)this.getContext().getApplicationContext();
		BackPlayHRulerView rulerView;
//		rulerView = (BackPlayHRulerView)(mParentActivity.findViewById(R.id.backplay_HRuler));
		mDIBType = WIGGLE_BITMAP;
		Toast.makeText(this.getContext(), "进入堆积图显示模式", Toast.LENGTH_SHORT).show();
		//设置水平标尺参数
//		rulerView.setPixsPerScan(app.getWigglePixsPerScan());
		//
		reloadCurrentDIB(mZoomX);
	}
  	public boolean isShowDIB()
	{
		return mDIBType == DIB_BITMAP;
	}
	public boolean isShowWiggle()
	{
		return mDIBType == WIGGLE_BITMAP;
	}
    //
    public int getBackFileTotalScans()
    {
    	int scanLen;
    	scanLen = mBackplayFileHeader.rh_nsamp;
    	return (int)((mBackfileLength-1024)/2/scanLen);
    }
    
    //
    public int getHadReadScans()
    {
    	return mHadCreateDIBWidth;
    }
    
    public void InitData(Context context)
	{
		//
		int i,j;
		for(i=0;i<mDIBHeight;i++)
			for(j=0;j<mDIBWidth;j++)
				mDIBPixels[i*mDIBWidth+j]=0;
		//
		gListener=new myGestureListener();
		detector = new GestureDetector(context, gListener);
		//
		
		//设置各个画笔的内容
		mWavePaint.setColor(Color.GREEN);
		mWavePaint.setStrokeWidth(2);
		//	
		mHardplusNormalPointPaint.setColor(Color.GREEN);
		mHardplusNormalPointPaint.setStrokeWidth(1);
		//
		mHardplusSelectPointPaint.setColor(Color.RED);
		mHardplusSelectPointPaint.setStrokeWidth(1);
		//
		mHardplusLinePaint.setColor(Color.GREEN);
		mHardplusLinePaint.setStrokeWidth(2);
		
		//
		mHardplusRangePaint.setColor(Color.GREEN);
		mHardplusRangePaint.setStrokeWidth(1);
		
		//
		for(i=0;i<9;i++)
			mManuPlusPointsVal[i] = 0;
	}
    public void stopBackplayTimer()
    {
    	mIsStopBackplayTimer = true;
    }
    public void addBackSpeed()
    {
    	mBackplaySpeed += 5;
    }
    public void delBackSpeed()
    {
    	if(mBackplaySpeed>5)
    		mBackplaySpeed -= 5;
    }
    //开始进行区域选择
    public void beginSelectRect()
    {
    	mIsSelectRect = true;
    }
    //取消区域选择
    public void cancelSelectRect()
    {
    	mIsSelectRect = false;
    }
    //开始距离标定
    public void beginDistanceMark()
    {
    	mIsDistanceMark = true;
    	mFirstPointsExist = false;
    	mSecondPointsExist = false;
    }
    //取消距离标定
    public void cancelDistanceMark()
    {
    	mIsDistanceMark = false;
    	mFirstPointsExist = false;
    	mSecondPointsExist = false;
    	//
    	//显示提示信息
//    	TextView txtView;
//    	txtView = (TextView)mParentActivity.findViewById(R.id.textview_backplay_picmsg);
//    	txtView.setVisibility(View.INVISIBLE);
    	
    	//
    	invalidate();
    }
	//画出波形函数
	@Override
	protected void onDraw(Canvas canvas)
	{
		//画出dib位图
		drawDIB(canvas);
		
		if(mIsDistanceMark)
			//画出距离标注点位置
			drawDistanceMarks(canvas);
		
		//画出手动增益调节区域
//		if(mIsAdjustManuPlus)
//			drawManuplusAdjust(canvas);
	}
	//画出手动增益调节区域
	public void drawManuplusAdjust(Canvas canvas)
	{
		MyApplication app;
		app = (MyApplication)(getContext().getApplicationContext());
		////计算当前调整波形的显示位置
		int scans;
		int scanLen;
		scanLen = mBackplayFileHeader.rh_nsamp*2;
		scans = (int) ((mTailReadPos-mHeadReadPos)/scanLen);
		double pixsCoeff;
		pixsCoeff = getWidth()/scans;
		int orgX,orgY;
		orgX = (int) (pixsCoeff*mNowAdjustScan);
		orgY = 0;
		
		////画出调节边框
		float left,right,top,bottom;
		bottom = getHeight();
		top = 0;
		left = orgX - mManuplusRangeWidth/2;
		right = left+mManuplusRangeWidth;
		if(left<0)
		{
			left = 0;
			right = mManuplusRangeWidth;
		}
		canvas.drawLine(left, top, right, top, mHardplusRangePaint);
		canvas.drawLine(right, top, right, bottom, mHardplusRangePaint);
		canvas.drawLine(right, bottom, left, bottom, mHardplusRangePaint);
		canvas.drawLine(left, bottom, left, top, mHardplusRangePaint);
		
		////画出增益曲线
		float xPos,yPos;
		float xOrg,yOrg;
		float perW;
		float perH;
		float befPosX,befPosY;		
		//
		perH = getHeight();
		perH = perH/8;
		xOrg = befPosX = left;
		yOrg = befPosY = 0;
		float hardPlusRange = app.mRadarDevice.getHardplusRange();
		perW = mManuplusRangeWidth/hardPlusRange;
		
		//
		float[] vals=mBackplayFileHeader.getHardplus();
		
		int i;
		float val;
		for(i=0;i<9;i++)
		{
			val=vals[i];
			xPos=xOrg+(val-(-10))*perW;
			yPos=yOrg+i*perH;
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
				canvas.drawCircle(xPos, yPos, 10, mHardplusNormalPointPaint);
			}
			else
			{
				canvas.drawCircle(xPos, yPos, 10, mHardplusSelectPointPaint);
			}
		}
		
		////画出波形
		int mScanLen;
		mScanLen = scanLen/2;
		perH = getHeight();
		perH = perH/mScanLen;
		xOrg = befPosX = left + mManuplusRangeWidth/2;
		yOrg = befPosY = 0;
		perW = 65534/mManuplusRangeWidth;
		for(i=0;i<mScanLen;i++)
    	{
			xPos = xOrg + mManuplusScanDatas[i]/perW;
			yPos = yOrg + i*perH;
			canvas.drawLine(befPosX, befPosY, xPos, yPos, mWavePaint);
			//
			befPosX = xPos;
			befPosY = yPos;
    	}
	}
	
	//画出距离标定位置
	public void drawDistanceMarks(Canvas canvas)
	{
		if(!mIsDistanceMark)
			return;
		if(!mFirstPointsExist)
			return;
		Paint mPaint = new Paint();
		Paint mPaint1 = new Paint();
		mPaint1.setColor(Color.GREEN);
		mPaint1.setStrokeWidth(2);
		mPaint.setColor(Color.RED);
		mPaint.setStrokeWidth(2);
		
		//
		float cx,cy;
		if(mFirstPointsExist)
		{
			cx = mFirstMarkDownPosX;
			cy = mFirstMarkDownPosY;
			canvas.drawCircle(cx, cy, 8, mPaint1);
		}
		if(mSecondPointsExist)
		{
			cx = mSecondMarkDownPosX;
			cy = mSecondMarkDownPosY;
			canvas.drawCircle(cx, cy, 8, mPaint);
		}
	}
	//
	public void drawDIB(Canvas canvas)
	{
		Bitmap bitmap = Bitmap.createBitmap(mDIBPixels, mDIBWidth, mDIBHeight, Bitmap.Config.ARGB_8888);
		Rect srcR,dstR;
		srcR = new Rect(0, 0, 0, 0);
		dstR = new Rect(0, 0, 0, 0);
		
		//得到位图显示范围
		dstR = getDibShowRect();
		
		//
		srcR.right=mDIBWidth;
		srcR.left = mDIBWidth-dstR.width();
		if(srcR.left<0)
			srcR.left=0;
		srcR.top=0;
		srcR.bottom=mDIBHeight;
		int scanLen;
		scanLen = mBackplayFileHeader.rh_nsamp;
		if(scanLen<mDIBHeight)
			srcR.bottom = scanLen;
		
		//
		canvas.drawBitmap(bitmap,srcR,dstR,null);
	}
	//得到dib图显示使用的矩形范围
	public Rect getDibShowRect()
	{
		Rect r = new Rect(0, 0, 0, 0);
		int width,height;
		width = getWidth();
		height = getHeight();
		r.right = width;
		r.bottom = height;
		return r;
	}
	
	//开始回放雷达数据文件
	public boolean beginBackplay(String fileName)
	{
		//
		mFilename = fileName;
		//检测回放文件是否存在
		File file = new File(mFilename);
		if(!file.exists())
			return false;	
		//得到文件长
		mBackfileLength = file.length();
	
		//读取文件头
		try{
			mBackplayFile = new FileInputStream(mFilename);
		}
		catch (Exception e)
		{
			DebugUtil.e(TAG, "begBackplay error!");
			return false;
		}
		mBackplayFileHeader.load(mBackplayFile);
		if(mBackplayFileHeader.rh_range==0)
		{
			mBackplayFileHeader.rh_range=40;
		}
		if(mBackplayFileHeader.rh_epsr==0)
		{
			mBackplayFileHeader.rh_epsr=1;
		}
		
		//关闭文件
		try{
			mBackplayFile.close();
		}
		catch(Exception e)
		{
			DebugUtil.e(TAG, "close backfile error!");
		}
		
		mHadReadLength = 1024;
		mHadCreateDIBWidth = 0;
		mIsBackplaying = true;
		mBackplayPause=false;
		mHeadReadPos = 1024;
		mTailReadPos = 1024;
		mHadCreateDIBWidth=0;
		mBackplayDir = BACKPLAY_FORWARD_DIR;
		//
		mTotalScans = (mBackfileLength-1024)/mBackplayFileHeader.rh_nsamp/2;
		//设置背景消除参数
		createBackGround();
		
		//启动定时器
		mBackplayHandler.postDelayed(mBackplayUpdateTimeTask,mBackplayDelayTime);
		mIsStopBackplayTimer=false;
		
		//
		MyApplication app;
    	app = (MyApplication)getContext().getApplicationContext();
		if(app.mRadarDevice.mIsUseSoftPlus)
		{
			////计算增益值
			this.setManuHardplus(mBackplayFileHeader.rh_rgainf);
		}
		
		////设置时间标尺 深度标尺 2016.6.10
		((BackPlayVRulerView)(app.mBTimewndRuler)).setTimeWindow(mBackplayFileHeader.getTimeWindow());
		((BackPlayVRulerView)(app.mBDeepRuler)).setDeep(mBackplayFileHeader.getDeep());
		//设置水平标尺
		BackPlayHRulerView HRView;
		HRView = (BackPlayHRulerView)(app.mBHorRuler);
		double touchDistance = mBackplayFileHeader.getDistancePerScans();
		if(mBackplayFileHeader.isWhellMode() && touchDistance==0)
			touchDistance = 1.;
		HRView.setTouchDistance(touchDistance);
		if(mBackplayFileHeader.isWhellMode())
		{
			HRView.setShowdistanceMode();
		}
		else
		{
			HRView.setShowscanMode();
		}
		//更新MainActivity的mBackplayFileHeader变量内容
		app.mMainActivity.setBackplayFileHeader(mBackplayFileHeader);
		//
		return true; 
	}
	
	public void createBackGround()
	{
		mIsBackgroundOk = false;
		mHadRcvBackgroundScans = 0;
		for(int i=0;i<8192;i++)
			mBackgroundDatas[i] = 0.;
		mIsBackgroundOk=true;
		
		//
		mBackgroundScans = BACKGROUND_DEFAULTSCANS;
		if(mTotalScans<mBackgroundScans)
			mBackgroundScans = (int) mTotalScans;
		
		//
		int needRLen;
		int scanLen;
		scanLen = mBackplayFileHeader.rh_nsamp;
		needRLen = mBackgroundScans*mBackplayFileHeader.rh_nsamp*2;
		byte[] buf = new byte[needRLen];
		//跳到指定位置
		try{
			mBackplayFile = new FileInputStream(mFilename);
			//移动文件读取指针到指定位置
			mBackplayFile.skip(1024);
			needRLen=mBackplayFile.read(buf, 0, needRLen);
		}
		catch(Exception e)
		{
			DebugUtil.e(TAG, "createBackGround:read() fail!");
			return;
		}
		short[] temShort = new short[needRLen/2];
		short temVal,temVal1;
		int i;
		for(i=0;i<needRLen/2;i++)
		{
			temVal = 0;
			temVal = buf[i*2];
			temVal &= 0xff;
			temVal1=0;
			temVal1 = buf[i*2+1];
			temVal1=(short)(temVal1<<8);
			temVal1 &= 0xff00;
			temVal = (short)(temVal + temVal1);
			temShort[i] = temVal;
		}
		int scans;
		int j;
		scans = needRLen/2/scanLen;
		mHadRcvBackgroundScans = scans;
		for(i=0;i<scans;i++)
		{
			for(j=0;j<scanLen;j++)
			{
				mBackgroundDatas[j] += temShort[i*scanLen+j];
			}
		}
		for(j=0;j<scanLen;j++)
		{
			mFirstBackgroundDatas[j] = temShort[j];
		}
		mIsBackgroundOk=true;
		//
		
		//
		try{
			mBackplayFile.close();
		}
		catch(Exception e)
		{
			DebugUtil.e(TAG, "createBackGround:close() fail!");
			return;
		}		
	}
	
	////因为触摸点坐标变化，引起位图移动
	public void scrollDibForPointsmove()
	{
		//如果不处于暂停模式，不进行该项操作
		if(!isBackplayPause())
			return;
		//
		int xOff;
		xOff=(int)(mSingleNowPosX-mSingleDownPosX);
		if(xOff==0)
			return;
		////
		MyApplication app;
		int pixsPerScan = 1;
		if(isShowWiggle())
		{
			app = (MyApplication)this.getContext().getApplicationContext();
			pixsPerScan = app.getWigglePixsPerScan();
		}
		//读取指定道数数据
		int scanLen;
		scanLen = mBackplayFileHeader.rh_nsamp;
		//确定要滑动的道数
		int srcScans = Math.abs(xOff) / pixsPerScan;
		if(srcScans<5)//mBackplaySpeed)
			srcScans = 5;//mBackplaySpeed;
		if(srcScans>mDIBWidth/pixsPerScan)
			srcScans = mDIBWidth/pixsPerScan-1;
		int addScan = 1;
		if(isShowWiggle())
		{
			srcScans += addScan;
		}
		int needRLength = scanLen*srcScans*2*mZoomX;   //需要读取的数据长度
		byte[] buf = new byte[needRLength];
		int readLen;
		int scans;
		int moveScans;
		
		//xOff>0时向后滚动视图
		if(xOff>0)
		{
			readLen = readDatas_BackDir(buf,needRLength);
			scans = readLen/mBackplayFileHeader.rh_nsamp/2;
			scans = scans/mZoomX;
			moveScans = scans;
			//判断是否只是滚动图像
			if(scans == 0)
			{
				if(mTailReadPos>1024)
				{
					scans = (int)(mTailReadPos-1024)/mBackplayFileHeader.rh_nsamp/2;
					scans = scans/mZoomX;
					if(scans>xOff)
						scans = xOff;
				}
				mTailReadPos -= scans*mZoomX*mBackplayFileHeader.rh_nsamp*2;
			}

			if(isShowWiggle() && moveScans>0)
			{

				moveScans = scans-addScan;
				
			}
			else
			{
				moveScans = scans;
			}
			//
			if(isShowDIB())
			{
				ScrollDIBBack(scans);
				//将读取的数据转换成dib位图
				changeDatasToDIB(buf,readLen,0);
			}
			if(isShowWiggle())
			{
				DebugUtil.i(TAG, "Now scroll wiggle:=" + scans + ";pixsPerScan:=" + pixsPerScan);
				ScrollDIBBack(moveScans*pixsPerScan);
				clearDIBFromeBegin(moveScans*pixsPerScan);
				//将读入的数据转换成堆积图
				changeDatasToWiggle(buf,readLen,0);
			}
		}
		else
		{
			readLen = readDatas_ForwardDir(buf,needRLength);
			scans = readLen/scanLen/2;
			scans = scans/mZoomX;
			DebugUtil.i(TAG, "This readForwardDir:=" + readLen + "bytes");
			//
			if(isShowDIB())
			{
				ScrollDIBForward(scans);
				//将读取的数据转换成dib位图
				changeDatasToDIB(buf,readLen,mDIBWidth-scans);
			}
			if(isShowWiggle())
			{
				DebugUtil.i(TAG, "Now scroll wiggle:=" + scans + ";pixsPerScan:=" + pixsPerScan);
				if(scans>0)
				{
					moveScans = scans-1;
					ScrollDIBForward(moveScans*pixsPerScan);
					clearDIBFromeTail(moveScans*pixsPerScan);
					//将读入的数据转换成堆积图
					changeDatasToWiggle(buf,readLen,mDIBWidth-scans*pixsPerScan);
				}
			}
			
			DebugUtil.i(TAG, "Now move forward:=" + scans);
		}
		//如果正在进行手动增益调节，此时重设调节波形
		if(mIsAdjustManuPlus)
		{
			setManuAdjustScanDatas();
		}
		DebugUtil.i(TAG, "Now scroll:=" + xOff);
		
		//设置水平标尺道数范围
//		LTDMainActivity ltdActivity;
		int totalScans;
		totalScans = (int) ((mTailReadPos-1024)/mBackplayFileHeader.rh_nsamp/2);
//		ltdActivity = (LTDMainActivity)mParentActivity;
//		ltdActivity.setBackplayHRulerScans(totalScans);
		
		//
		invalidate();
	}
	
	//位图后退时，从当前文件头部读取指定长度数据
	public int readDatasBackDir(byte[] buf,int needLen)
	{
		int retLen=0;
		//计算文件读取指针的位置
		long offset=0;
		int scanLen;
		scanLen = mBackplayFileHeader.rh_nsamp*2;
		offset = mHeadReadPos;
		offset = offset-needLen;
		if(isShowWiggle())
			offset = offset+1*scanLen*mZoomX;
		if(offset<1024)
		{
			needLen = (int)(mHeadReadPos-1024);
			if(isShowWiggle())
				needLen += 1*scanLen*mZoomX;
			offset = 1024;
		}
				
		//
		try{
			mBackplayFile = new FileInputStream(mFilename);
			//移动文件读取指针到指定位置
			mBackplayFile.skip(offset);
			retLen=mBackplayFile.read(buf, 0, needLen);
		}
		catch(Exception e)
		{
			return 0;
		}
		
		///已经到达头部的处理放在后面
		if(retLen == 0)
		{
			
		}
		///还没有到达文件头
		if(retLen>0)
		{
			int delLen = retLen;
			if(isShowWiggle())
				delLen -= scanLen*1*mZoomX;
			mHeadReadPos=mHeadReadPos-delLen;
			mTailReadPos -= delLen;
//			mHeadReadPos=mHeadReadPos-retLen;
//			mTailReadPos -= retLen;
		}
		if(mTailReadPos<1024)
			mTailReadPos = 1024;
		//
		return retLen;
	}
	//从当前的文件尾部读入指定长度数据
	public int readDatasForwardDir(byte[] buf,int needLen)
	{
		int retLen=0;
		//计算文件读取指针的位置
		long offset=0;
		int scanLen;
		scanLen = mBackplayFileHeader.rh_nsamp*2;
		//如果正在向前回放
		offset = mTailReadPos;

		//
		try{
			mBackplayFile = new FileInputStream(mFilename);
			//移动文件读取指针到指定位置
			mBackplayFile.skip(offset);
			retLen=mBackplayFile.read(buf, 0, needLen);
		}
		catch(Exception e)
		{
			return 0;
		}
		
		//
		int pixsPerScan;
		MyApplication app;
		app = (MyApplication)this.getContext().getApplicationContext();
		pixsPerScan = 1;
		if(isShowWiggle())
		{
			pixsPerScan = app.getWigglePixsPerScan();
		}
		//修改记录变量
		mTailReadPos = mTailReadPos + retLen;
		if(mTailReadPos >= 1024+mDIBWidth*mZoomX*scanLen/pixsPerScan)
			mHeadReadPos = mTailReadPos - mDIBWidth*mZoomX*scanLen/pixsPerScan;

		//
		return retLen;
	}
	////向前滚动位图
	public void ScrollDIBForward(int scrScans)
	{
		if(scrScans <= 0)
			return;
		for(int i=0;i<mDIBHeight;i++)
		{	
			for(int j=scrScans;j<mDIBWidth;j++)
			{
				mDIBPixels[i*mDIBWidth+(j-scrScans)] = mDIBPixels[i*mDIBWidth+j];
				mDIBPixels[i*mDIBWidth+j] = 0;
			}
		}
	}
	////向后滚动位图
	public void ScrollDIBBack(int scrScans)
	{
		if(scrScans <= 0)
			return;
		for(int i=0;i<mDIBHeight;i++)
		{	
			for(int j=mDIBWidth-scrScans-1;j>=0;j--)
			{
				mDIBPixels[i*mDIBWidth+(j+scrScans)] = mDIBPixels[i*mDIBWidth+j];
			}
		}
		for(int i=0;i<mDIBHeight;i++)
		{	
			for(int j=0;j<scrScans;j++)
			{
				mDIBPixels[i*mDIBWidth+j] = 0;
			}
		}
	}
	//
	public int NO_MARK = 0;   //没有标记
	public int BIG_MARK_FLAG = 1;
	public int SMALL_MARK_FLAG =2;
	public int BIG_MARK_VAL = 0x4000;    //大标(正标)
	public int SMALL_MARK_VAL = 0x8000;  //小标(负标)
	public int mMarkType = NO_MARK;      //标记类型
	////将数据转换成 "堆积" 图
	public void changeDatasToWiggle(byte[] buf,int bufLen,int begScanIndex)
	{
		//将数据转换成short型
		short[] temShort = new short[bufLen/2];
		int i,j;
		short temVal0;
		short temVal1;
		int newLen = bufLen/2;
		for(i=0;i<newLen;i++)
		{
			temVal0 = 0;
			temVal0 = buf[i*2];
			temVal0 &= 0xff;
			//
			temVal1=0;
			temVal1 = buf[i*2+1];
			temVal1=(short)(temVal1<<8);
			temVal1 &= 0xff00;
			//
			temVal0 = (short)(temVal0 + temVal1);
			temShort[i] = temVal0;
		}
		
		//得到道长和每道数据占用的像素点数
		MyApplication app;
		app = (MyApplication)(getContext().getApplicationContext());
		int mScanLen;
		mScanLen = mBackplayFileHeader.rh_nsamp;
		int pixsPerScan;
		pixsPerScan = app.getWigglePixsPerScan();
		
		//计算读入的总道数
		int scans;
		scans = bufLen/2;
		scans = scans/mScanLen;
		//增加水平方向上的缩放
		scans = scans/mZoomX;
				
		//转换位图
		int colNumber;   //调色板颜色数
		int[][] mColPal;
		int palIndex;
		palIndex = app.mColorPal.getColpalIndex();
		colNumber = app.mColorPal.getColorNumber();
		mColPal = app.mColorPal.getColors();
		
		//背景色(负向峰值颜色)
		int backCol = 0;
		backCol = app.mColorPal.getBackColor();
		int fillCol = 0;
		fillCol = app.mColorPal.getFillColor();
		
		//
		double dataVal;
		int dataInter;
		double perCol;
		long maxVal;
		long mixVal;
		int  wndWidth=pixsPerScan*2;
		maxVal = app.mRadarDevice.getMaxValue();
		mixVal = app.mRadarDevice.getMixValue();
		perCol = (maxVal-mixVal)/(wndWidth*2);
		dataInter = mScanLen/mDIBHeight;
		if(dataInter<1)
			dataInter = 1;
		//确定填充起始位置
		int fillBaseIndex = begScanIndex;
		short flagVal=0;
		double temVal;
		short[] scanDatas = new short[mScanLen];
		int guard1,guard2;
		int fillIndex;
		double temDoubleVal;
		int hadRScans;
		hadRScans = (int)(mTailReadPos-1024)/mBackplayFileHeader.rh_nsamp/2;
		for(i=0;i<scans;i++)
		{
			mMarkType = NO_MARK;
			//进行缩放处理
			flagVal = 0;
			for(j=0;j<mScanLen;j++)
			{
				temVal = 0;
				scanDatas[j] = 0;
				for(int m=0;m<mZoomX;m++)
				{
					temVal += temShort[(i*mZoomX+m)*mScanLen + j];
					flagVal |= (temShort[(i*mZoomX+m)*mScanLen + 1]);
				}
				scanDatas[j] = (short) (temVal/mZoomX);
			}
			//如果正在做手动增益
			if(mIsAdjustManuPlus || app.mRadarDevice.mIsUseSoftPlus)
			{
				for(j=2;j<mScanLen;j++)
				{
					temDoubleVal = scanDatas[j]*mManuplus[j];
					if(temDoubleVal > 0x7fff)
						temDoubleVal = 0x7fff;
					if(temDoubleVal < -0x7fff)
						temDoubleVal = -0x7fff;
					scanDatas[j] = (short)temDoubleVal;
				}
			}
			//标记判断
			if((flagVal & BIG_MARK_VAL)!=0)
				mMarkType = BIG_MARK_FLAG;
			if((flagVal & SMALL_MARK_VAL)!=0)
				mMarkType = SMALL_MARK_FLAG;
			//
			for(j=0;j<mDIBHeight;j++)
			{
				int temFillIndex = (fillBaseIndex + i*pixsPerScan)+(j*mDIBWidth);//+pixsPerScan/2;
				
				guard1 = temFillIndex-wndWidth/2;
				guard2 = guard1+wndWidth;
				if(guard1 < j*mDIBWidth)
					guard1 = j*mDIBWidth;
				if(guard2 > (j+1)*mDIBWidth)
					guard2 = (j+1)*mDIBWidth;
				/*
				guard1 = 0+j*mDIBWidth;
				guard2 = 0+(j+1)*mDIBWidth;
				*/
				if(j*dataInter<mScanLen)
				{
					//标记处理
					dataVal = scanDatas[j*dataInter];
					if(mIsRemoveBackground)
					{
						double temVal3=dataVal;
						temVal3 -= mBackgroundDatas[j*dataInter]/mHadRcvBackgroundScans;
						if(temVal3<-0x7fff)
							temVal3=-0x7fff;
						if(temVal3>0x7fff)
							temVal3=0x7fff;
						dataVal = (short) temVal3;
//						dataVal -= mBackgroundDatas[j*dataInter]/mHadRcvBackgroundScans;
					}
					if(mMarkType == BIG_MARK_FLAG)
						dataVal = maxVal;
					if(mMarkType == SMALL_MARK_FLAG)
					{
						if(j*dataInter<=mScanLen/2)
							dataVal = maxVal;
					}
					dataVal = dataVal*mZoomPlus;
					if(dataVal > maxVal)
						dataVal = maxVal;
					if(dataVal<mixVal)
						dataVal = mixVal;
					///计算要填充的计数
					int fillNumber;
					fillNumber = (int) (dataVal/perCol);
					if(fillNumber<=0)
					{
						fillIndex = temFillIndex+fillNumber;
						if(fillIndex<guard1 && 
						   (fillBaseIndex+i) == 0)
							continue;
						if(fillIndex<guard1)
							fillIndex = guard1;
						//
						mDIBPixels[fillIndex] = fillCol;
					}
					else
					{
						for(int kk=0;kk<fillNumber;kk++)
						{
							fillIndex = temFillIndex+kk;
							if(fillIndex>=guard2)
								fillIndex = guard2-1;
							//
							mDIBPixels[fillIndex] = fillCol; 
						}
					}
				}
			}
			//更换背景
			if(mHadRcvBackgroundScans<hadRScans)
			{
				for(int m=0;m<mScanLen;m++)
				{
					mBackgroundDatas[m] += scanDatas[m];
					mFirstBackgroundDatas[m] = scanDatas[m];
				}
				mHadRcvBackgroundScans++;
			}
		}
	}
	
	////生成位图
	/*
	 * buf:生成位图的数据;
	 * bufLen:数据长度
	 * begScanIndex:位图填充起始位置
	 * 
	 */
	public void changeDatasToDIB(byte[] buf,int bufLen,int begScanIndex)
	{
		///转换数据格式?
		/*
		 *有没有一种方法直接操作short型数据，而不需要这样转换 
		 */
		short[] temShort = new short[bufLen/2];
		int i,j;
		short temVal;
		short temVal1;
		int newLen = bufLen/2;
		for(i=0;i<newLen;i++)
		{
			temVal = 0;
			temVal = buf[i*2];
			temVal &= 0xff;
			//
			temVal1=0;
			temVal1 = buf[i*2+1];
			temVal1=(short)(temVal1<<8);
			temVal1 &= 0xff00;
			//
			temVal = (short)(temVal + temVal1);
			temShort[i] = temVal;
		}
		
		//
		MyApplication app;
		app = (MyApplication)(getContext().getApplicationContext());
		int mScanLen;
		mScanLen = mBackplayFileHeader.rh_nsamp;
		
		//计算要生成的位图像素点数
		int scans;
		scans = bufLen/2;
		scans = scans/mScanLen;
		scans = scans/mZoomX;
								
		//转换位图
		int colNumber;   //调色板颜色数
		int[][] mColPal;
		colNumber = app.mColorPal.getColorNumber();
		mColPal = app.mColorPal.getColors();
		
		short dataVal;
		int dataInter;
		double perCol;
		perCol = 0xefff/colNumber;
		dataInter = mScanLen/mDIBHeight;
		if(dataInter<1)
			dataInter = 1;
		int  fillColIndex;
		//确定填充位置
		int fillBaseIndex;
		fillBaseIndex = begScanIndex;
		int fillIndex;
		int col;
		int k;
		double temDoubleVal;
		short[] scanDatas = new short[mScanLen];
		for(i=0;i<mScanLen;i++)
			scanDatas[i] = 0;
		short flagVal;
		int m;
		int hadRScans;
		hadRScans = (int)(mTailReadPos-1024)/mBackplayFileHeader.rh_nsamp/2;
		for(i=0;i<scans;i++)
		{
			if(fillBaseIndex+i>=mDIBWidth)
				break;
			////
			mMarkType = NO_MARK;
			flagVal = 0;
			//根据缩放系数，将多道数据转换成一道数据
			for(m=0;m<mScanLen;m++)
			{
				scanDatas[m] = 0;
				temDoubleVal = 0;
				for(int mm=0;mm<mZoomX;mm++)
				{
					temDoubleVal +=  temShort[(i*mZoomX+mm)*mScanLen+m];
					flagVal |= (temShort[(i*mZoomX+mm)*mScanLen + 1]);
				}
				scanDatas[m]  = (short) (temDoubleVal/mZoomX);
			}
			
			//更换背景
			if(mHadRcvBackgroundScans<hadRScans)
			{
				for(m=0;m<mScanLen;m++)
				{
	//				mBackgroundDatas[m] -= mFirstBackgroundDatas[m];
					mBackgroundDatas[m] += scanDatas[m];
					mFirstBackgroundDatas[m] = scanDatas[m];
					/*
					if(m == 93 && mHadRcvBackgroundScans==710)
					{
						DebugUtil.i(TAG,"scanDatas:="+scanDatas[m]+";backDatas:="+mBackgroundDatas[m]+";");
					}
					*/
				}
				mHadRcvBackgroundScans++;
			}
			//如果正在做手动增益
			if(mIsAdjustManuPlus || app.mRadarDevice.mIsUseSoftPlus)
			{
				for(m=2;m<mScanLen;m++)
				{
					temDoubleVal = scanDatas[m]*mManuplus[m];
					if(temDoubleVal > 0x7fff)
						temDoubleVal = 0x7fff;
					if(temDoubleVal < -0x7fff)
						temDoubleVal = -0x7fff;
					scanDatas[m] = (short)temDoubleVal;
				}
			}
			//标记判断
			if((flagVal & BIG_MARK_VAL)!=0)
				mMarkType = BIG_MARK_FLAG;
			if((flagVal & SMALL_MARK_VAL)!=0)
				mMarkType = SMALL_MARK_FLAG;
			//
			for(j=0;j<mDIBHeight;j++)
			{
				if(j*dataInter>=mScanLen)
					break;
				dataVal = scanDatas[j*dataInter];
				short temVal2 = dataVal;
				//背景消除
				if(mIsRemoveBackground)
				{
					double temVal3=dataVal;
					temVal3 -= mBackgroundDatas[j*dataInter]/mHadRcvBackgroundScans;
					if(temVal3<-0x7fff)
						temVal3=-0x7fff;
					if(temVal3>0x7fff)
						temVal3=0x7fff;
					dataVal = (short) temVal3;
					/*
					if(temVal2>0&&dataVal<0&&i==710)
					{
						DebugUtil.i(TAG,"scanDatas:="+temVal2+";backDatas:="+mBackgroundDatas[j*dataInter]/mHadRcvBackgroundScans+";");
						DebugUtil.i(TAG,"index:="+j*dataInter);
					}
					*/
				}
				if(dataVal>0x7fff)
					dataVal = 0x7fff;
				if(dataVal<-0x7fff)
					dataVal = -0x7fff;
				if(mMarkType == BIG_MARK_FLAG)
					dataVal = 0x7fff;
				if(mMarkType == SMALL_MARK_FLAG)
				{
					if(j*dataInter<=mScanLen/2)
						dataVal = 0x7fff;
				}
				temDoubleVal = dataVal*mZoomPlus;
				if(temDoubleVal > 0x7fff)
					temDoubleVal = 0x7fff;
				if(temDoubleVal < -0x7fff)
					temDoubleVal = -0x7fff;
				dataVal = (short)temDoubleVal;
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
				mDIBPixels[fillIndex] = col;  //
			}
		}
	}
	
	////
	public boolean endBackplay()
	{
		mIsBackplaying=false;
		mBackplayPause=false;
		mBackplayDir = BACKPLAY_NO;
		//初始化dib
		int i,j;
		for(i=0;i<mDIBHeight;i++)
			for(j=0;j<mDIBWidth;j++)
				mDIBPixels[i*mDIBWidth+j]=0;
		//
		try{
			mBackplayFile.close();
			mBackplayFile=null;
		}
		catch (Exception e)
		{
			return false;
		}
		return true;
	}
	////
	public boolean isBackPlaying()
	{
		return mBackplayDir != BACKPLAY_NO;
	}
	////是否暂停回放:暂停|已经回放到尾|已经回放到头
	public boolean isBackplayPause()
	{
//		DebugUtil.i(TAG,"now backplaypause:="+mBackplayPause);
		//检测是否处于回放模式
		if(!isBackPlaying())
			return false;
		//暂停回放
		if(mBackplayPause)
			return true;

		//向前回放并且已经到达数据尾端
		if(isBackPlayForwardDir())
		{
			if(mBackfileLength == mTailReadPos)
				return true;
		}
		
		//向后回放并且已经到达数据首端
		if(isBackPlayBackDir())
		{
			if(mHeadReadPos == 1024 && mTailReadPos == 1024)
				return true;
		}
		
		//
		return false;
	}
	////
	public void setBackplayDirForward()
	{
		mBackplayDir = BACKPLAY_FORWARD_DIR;
	}
	////
	public boolean isBackPlayForwardDir()
	{
		return mBackplayDir == BACKPLAY_FORWARD_DIR;
	}
	////
	public void setBackplayDirBack()
	{
		mBackplayDir = BACKPLAY_BACK_DIR;
	}
	////
	public boolean isBackPlayBackDir()
	{
		return mBackplayDir == BACKPLAY_BACK_DIR;
	}
	////是否停止回放
	public boolean isBackplayEnd()
	{
		return !mIsBackplaying; 
	}
	////设置暂停状态
	public void setBackplayPauseStatus(boolean isPause)
	{
		mBackplayPause = isPause;
	}
	//得到要回放的文件总的数据长度
	public long getTotalBackplayBytes()
	{
		File file = new File(mFilename);
		if(!file.exists())
			return 0;
		return file.length();
	}
	
	/**
	 * 设置背景消除标志
	 * @param flag
	 */
	public void setRemoveBackground(boolean flag)
	{
		mIsRemoveBackground = flag;
		if(isBackplayPause())
		{
			reloadCurrentDIB(mZoomX);
			invalidate();
		}
	}
	
	//得到已经读取的数据道数
	public int getNowHasReadScans()
	{
		int scanLen;
		scanLen = mBackplayFileHeader.rh_nsamp;
		return (int)(mHadReadLength/scanLen/2);
	}
	//得到已经生成的dib图宽度
	public int getHadCreateDIBWidth()
	{
		return mHadCreateDIBWidth;
	}
	//得到已经读取得数据长度
	public int getHadReadDatasLength()
	{
		return (int)mHadReadLength;
	}
	//设置回放速度
	public void setBackplaySpeed(int speed)
	{
		mBackplaySpeed = speed;
	}
	///向后滚动时读取数据
	public int readDatas_BackDir(byte[] buf,int needLen)
	{
		int retLen=0;
		//计算文件读取指针的位置
		long offset=0;
		int scanLen;
		scanLen = mBackplayFileHeader.rh_nsamp*2;
		
		////设置offset
		int addScan = 1;
		//如果已经回放到头位置
		if(mHeadReadPos <=1024 )
		{
			needLen = 0;
			offset = 1024;
		}
		else
		{
			offset = mHeadReadPos;
			offset = offset-needLen;
			if(isShowWiggle())
				offset = offset+scanLen*addScan*mZoomX;
			if(offset<=1024)
			{
				needLen = (int)(mHeadReadPos-1024);
				offset = 1024;
				if(needLen>0)
				{
					if(isShowWiggle())
						needLen += addScan*scanLen*mZoomX;
				}
			}
		}
		
		//跳到指定位置并读取数据
		try{
			mBackplayFile = new FileInputStream(mFilename);
			//移动文件读取指针到指定位置
			mBackplayFile.skip(offset);
			retLen=mBackplayFile.read(buf, 0, needLen);
		}
		catch(Exception e)
		{
			return 0;
		}
		
		//
		int pixsPerScan;
		MyApplication app;
		app = (MyApplication)this.getContext().getApplicationContext();
		pixsPerScan = 1;
		if(isShowWiggle())
		{
			pixsPerScan = app.getWigglePixsPerScan();
		}
		
		///已经到达头部的处理放在后面
		if(retLen == 0)
		{
				
		}
		///还没有到达文件头
		if(retLen>0)
		{
			int delLen = retLen;
			if(isShowWiggle())
				delLen -= scanLen*addScan*mZoomX;
			mHeadReadPos=mHeadReadPos-delLen;
			mTailReadPos -= delLen;
		}
		if(mHeadReadPos<1024)
			mHeadReadPos = 1024;
		if(mTailReadPos<1024)
			mTailReadPos = 1024;

		//
		try{
			mBackplayFile.close();
		}
		catch(Exception e)
		{
			DebugUtil.e(TAG, "readDatas fail!");
		}
		//
		return retLen;
	}
	///
	public int readDatas_ForwardDir(byte[] buf,int needLen)
	{
		int retLen=0;
		//计算文件读取指针的位置
		long offset=0;
		int scanLen;
		scanLen = mBackplayFileHeader.rh_nsamp*2;
		///addScan:为了显示堆积波形特殊处理所需变量
		int addScan;
		addScan = 1;
		////设置offset
		offset = mTailReadPos;
		File temFile = new File(mFilename);
		if(offset >= temFile.length())
			return 0;
		if(isShowWiggle())
		{
			offset = offset-scanLen*addScan*mZoomX;
		}
		if(offset<1024)
			offset = 1024;
		
		//跳到指定位置
		try{
			mBackplayFile = new FileInputStream(mFilename);
			//移动文件读取指针到指定位置
			mBackplayFile.skip(offset);
			retLen=mBackplayFile.read(buf, 0, needLen);
		}
		catch(Exception e)
		{
			return 0;
		}
//		DebugUtil.i(TAG,"Skip:="+offset+";readLength:="+retLen);
		int pixsPerScan;
		MyApplication app;
		app = (MyApplication)this.getContext().getApplicationContext();
		pixsPerScan = 1;
		if(isShowWiggle())
		{
			pixsPerScan = app.getWigglePixsPerScan();
		}
		
		//设置尾指针
		mTailReadPos = mTailReadPos + retLen;          //尾指针
		if(isShowWiggle())
			mTailReadPos = mTailReadPos - scanLen*addScan*mZoomX;
		//设置头指针
		if(mTailReadPos >= 1024+mDIBWidth*mZoomX*scanLen/pixsPerScan)    
			mHeadReadPos = mTailReadPos - mDIBWidth*scanLen*mZoomX/pixsPerScan;
		
		//
		try{
			mBackplayFile.close();
		}
		catch(Exception e)
		{
			DebugUtil.e(TAG, "readDatas fail!");
		}
		//
		return retLen;
	}
	///读取雷达数据
	public int readDatas(byte[] buf,int needLen)
	{
		int retLen=0;
		//计算文件读取指针的位置
		long offset=0;
		int scanLen;
		scanLen = mBackplayFileHeader.rh_nsamp*2;
		
		////设置offset
		//如果正在正向回放
		if(isBackPlayForwardDir())
		{
			offset = mTailReadPos;
			if(isShowWiggle())
			{
				offset = offset-scanLen*1*mZoomX;
			}
			if(offset<1024)
				offset = 1024;
		}
		
		//如果正在反向回放
		if(isBackPlayBackDir())
		{
			if(mHeadReadPos <=1024)
			{
				needLen = 0;
				offset = 1024;
			}
			else
			{
				offset = mHeadReadPos;
				offset = offset-needLen;
				if(isShowWiggle())
					offset = offset+scanLen*1*mZoomX;
				if(offset<=1024)
				{
					needLen = (int)(mHeadReadPos-1024);
					offset = 1024;
					if(needLen>0)
					{
						if(isShowWiggle())
							needLen += 1*scanLen*mZoomX;
					}
				}
			}
		}
		
		//跳到指定位置
		try{
			mBackplayFile = new FileInputStream(mFilename);
			//移动文件读取指针到指定位置
			mBackplayFile.skip(offset);
			retLen=mBackplayFile.read(buf, 0, needLen);
		}
		catch(Exception e)
		{
			return 0;
		}
		
		int pixsPerScan;
		MyApplication app;
		app = (MyApplication)this.getContext().getApplicationContext();
		pixsPerScan = 1;
		if(isShowWiggle())
		{
			pixsPerScan = app.getWigglePixsPerScan();
		}
		
		//修改记录变量
		if(isBackPlayForwardDir())
		{
			//设置尾指针
			mTailReadPos = mTailReadPos + retLen;          //尾指针
			if(isShowWiggle())
				mTailReadPos = mTailReadPos - scanLen*1*mZoomX;
			//设置头指针
			if(mTailReadPos >= 1024+mDIBWidth*mZoomX*scanLen/pixsPerScan)    
				mHeadReadPos = mTailReadPos - mDIBWidth*scanLen*mZoomX/pixsPerScan;
		}
		if(isBackPlayBackDir())
		{
			///已经到达头部的处理放在后面
			if(retLen == 0)
			{
				
			}
			///还没有到达文件头
			if(retLen>0)
			{
				int delLen = retLen;
				if(isShowWiggle())
					delLen -= scanLen*1*mZoomX;
				mHeadReadPos=mHeadReadPos-delLen;
				mTailReadPos -= delLen;
			}
			if(mTailReadPos<1024)
				mTailReadPos = 1024;
		}
		//
		try{
			mBackplayFile.close();
		}
		catch(Exception e)
		{
			DebugUtil.e(TAG, "readDatas fail!");
		}
		//
		return retLen;
	}
	///数据更新定时器任务
    private Runnable mBackplayUpdateTimeTask = new Runnable(){
		@Override
		public void run(){
			////
			if(mIsStopBackplayTimer)
				return;
			////读取数据并生成位图显示
			if(isBackplayEnd())
				return;
			////判断是否处在暂停模式
			if(!isBackplayPause())
			{	
				//正向滚动
				if(isBackPlayForwardDir())
				{
					backPlayForward();
				}
				//反向滚动
				if(isBackPlayBackDir())
				{
					backPlayBack();
				}
				//如果正在进行手动增益调节，此时重设调节波形
				if(mIsAdjustManuPlus)
				{
					setManuAdjustScanDatas();
				}
				//
				invalidate();
			}
			
			//设置道数
			int totalScans;
			totalScans = (int) ((mTailReadPos-1024)/mBackplayFileHeader.rh_nsamp/2);
			((MultiModeLifeSearchActivity)mParentActivity).setBackplayScans(totalScans);
			((MultiModeLifeSearchActivity)mParentActivity).setBackplayHRulerScans(totalScans);
			//设置水平标尺 2016.6.10
			
			//
			mBackplayHandler.postDelayed(this, mBackplayDelayTime);
		}
	};
	////正向滚动
	public void backPlayForward()
	{
		//按照回放速度读取指定道数数据
		int scanLen;
		scanLen = mBackplayFileHeader.rh_nsamp;
		int readScans;
		readScans = mBackplaySpeed;
		int addScans = 1;
		//显示堆积图，此时调整要读取的道数:多读一道数据，用来重新生成滚动后的第一道数据位图，防止屏幕滚动后的不正常.
		if(isShowWiggle())
			readScans = mBackplaySpeed+addScans;
		int needRLength = scanLen*readScans*2*mZoomX;   //需要读取的数据长度
		byte[] buf = new byte[needRLength];
		int readLen;
		readLen = readDatas_ForwardDir(buf,needRLength);
//		readLen = readDatas(buf,needRLength);
				
		boolean isShow = this.isShown();
		DebugUtil.i(TAG, "isShow:=" + isShow);
		////
		MyApplication app;
		int pixsPerScan = 1;
		if(isShowWiggle())
		{
			app = (MyApplication)this.getContext().getApplicationContext();
			pixsPerScan = app.getWigglePixsPerScan();
		}
		////计算位图滚动的道数
		int scans;
		scans = readLen/mBackplayFileHeader.rh_nsamp/2;
		scans = scans/mZoomX;
		scans = scans*pixsPerScan;
		
		////滚动位图
		DebugUtil.i(TAG, "forward scroll :=" + scans);
		int srcScans = scans;
		if(isShowWiggle())
			srcScans = srcScans-addScans*pixsPerScan;
		ScrollDIBForward(srcScans);
		
		if(isShowDIB())
		{
			//将读入的数据转换成dib图
			for(int i = 0;i<10;i++)
			{
				DebugUtil.i(TAG, "buf[" + i + "]=" + buf[i]);
			}
			
			changeDatasToDIB(buf,readLen,mDIBWidth-scans);
			DebugUtil.i(TAG, "changeDatasToDIB");
		}
		if(isShowWiggle())
		{
			//将空出的部分填零
			clearDIBFromeTail(srcScans);
			//将读入的数据转换成堆积图
			changeDatasToWiggle(buf,readLen,mDIBWidth-scans);
		}
	}
	
	////反向滚动
	public void backPlayBack()
	{
		//按照回放速度读取指定道数数据
		int scanLen;
		scanLen = mBackplayFileHeader.rh_nsamp;
		int needScans;
		needScans = mBackplaySpeed;
		//堆积图下，调整读取的道数:增加一道数据，是指用来重新生成当前显示的第一道图像的数据.
		if(isShowWiggle())
			needScans = needScans+1;
		int needRLength = scanLen*needScans*2*mZoomX;   //需要读取的数据长度
		byte[] buf = new byte[needRLength];
		int readLen;
		readLen = readDatas_BackDir(buf,needRLength);
//		readLen = readDatas(buf,needRLength);
		
		////
		MyApplication app;
		int pixsPerScan = 1;
		if(isShowWiggle())
		{
			app = (MyApplication)this.getContext().getApplicationContext();
			pixsPerScan = app.getWigglePixsPerScan();
		}
		////计算位图滚动的道数
		int scans;
		scans = readLen/mBackplayFileHeader.rh_nsamp/2;
		scans = scans/mZoomX;
		
		//
		int srcScans = scans;
		//判断是否只是滚动图像,即:已经到了文件头部，只做滚动就可以了
		if(scans == 0)
		{
			if(mTailReadPos>1024)
			{
				scans = (int)(mTailReadPos-1024)/mBackplayFileHeader.rh_nsamp/2;
				scans /= mZoomX;
				if(scans>mBackplaySpeed)
					scans = mBackplaySpeed;
			}
			mTailReadPos -= scans*mZoomX*mBackplayFileHeader.rh_nsamp*2;
		}
		
		//
		if(isShowWiggle() && srcScans>0)
			srcScans = scans-1;
		else
			srcScans = scans;
		ScrollDIBBack(srcScans*pixsPerScan);

		if(isShowDIB())
		{
			//将读取的数据转换成dib位图
			changeDatasToDIB(buf,readLen,0);
		}
		if(isShowWiggle())
		{
			//将空出的部分清零
			clearDIBFromeBegin(srcScans*pixsPerScan);
			//将读入的数据转换成堆积图
			changeDatasToWiggle(buf,readLen,0);
		}
		
		DebugUtil.i(TAG, "back scroll :=" + scans);
	}
	public void clearDIBFromeBegin(int scans)
	{
		MyApplication app;
		app = (MyApplication)getContext().getApplicationContext();
		int backCol;
		backCol = app.mColorPal.getBackColor();
		//清空滚动区域
		int fillIndex;
		for(int m=0;m<scans;m++)
		{
			for(int j=0;j<mDIBHeight;j++)
			{
				fillIndex = m+(j*mDIBWidth);
				mDIBPixels[fillIndex] = backCol;
			}
		}
	}
	public void clearDIBFromeTail(int scans)
	{
		MyApplication app;
		app = (MyApplication)getContext().getApplicationContext();
		int backCol;
		backCol = app.mColorPal.getBackColor();
		
		//清空滚动区域
		int fillIndex;
		int baseIndex = mDIBWidth - scans;
		for(int m=baseIndex;m<mDIBWidth;m++)
		{
			for(int j=0;j<mDIBHeight;j++)
			{
				fillIndex = m+(j*mDIBWidth);
				mDIBPixels[fillIndex] = backCol;
			}
		}
	}
	/////锁屏状态下的多点处理
	float mDownPosX1,mDownPosX2;
	float mDownPosY1,mDownPosY2;
	float mNowDownPosX1,mNowDownPosX2;
	float mNowDownPosY1,mNowDownPosY2;
	boolean isManageManyPoints=false;
	public boolean manageManyPoints(MotionEvent event)
	{
		//得到触屏的手指数，并根据数目进行相应处理
		int num;
		num=event.getPointerCount();
		String msg= "已经点下:" + num + "个触点";
		DebugUtil.i(TAG, msg);
		
		//如果大于3，那么此时要进行回放方式切换操作，
		//     返回false,将处理操作交给父窗口LTDMainActivity,判断是否要进行回放模式的切换
		if(num>=3)
		{
			return false;
		}
		//如果=2，那么进行缩放|测距处理
		if(num == 2)
		{
			int p1=event.findPointerIndex(0);
			int p2=event.findPointerIndex(1);
			float nowPosX1,nowPosX2,nowPosY1,nowPosY2;
			switch(event.getAction())
			{
			case MotionEvent.ACTION_POINTER_1_DOWN:
				try{
					mDownPosX1=mNowDownPosX1=event.getX(p1);
					mDownPosY1=mNowDownPosY1=event.getY(p1);
					//
					DebugUtil.i(TAG, "Point1_Down x:=" + mDownPosX1 + ";  y:=" + mDownPosY1);
				}
				catch(IllegalArgumentException e){
					DebugUtil.e(TAG, "Manage ACTION_POINTER_1_DOWN Fail!");
				}
				break;
			case MotionEvent.ACTION_POINTER_1_UP:
			case MotionEvent.ACTION_POINTER_2_UP:
				//缩放处理
				float oldXOff,nowXOff;
				oldXOff = mDownPosX1-mDownPosX2;
				nowXOff = mNowDownPosX1-mNowDownPosX2;
				if(Math.abs(nowXOff) >= Math.abs(oldXOff) + 300)
				{
					zoomOutX();
				}
				if(Math.abs(nowXOff) <= Math.abs(oldXOff) - 300)
				{
					zoomInX();
				}
				break;
			case MotionEvent.ACTION_POINTER_2_DOWN:
				try{
					mDownPosX1=mNowDownPosX1=event.getX(p1);
					mDownPosY1=mNowDownPosY1=event.getY(p1);
					mDownPosX2=mNowDownPosX2=event.getX(p2);
					mDownPosY2=mNowDownPosY2=event.getY(p2);
					isManageManyPoints=true;
					//
					DebugUtil.i(TAG, "Point2_1_Down x:=" + mDownPosX2 + ";  Y:=" + mDownPosY2);
					DebugUtil.i(TAG, "Point2_2_Down x:=" + mDownPosX2 + ";  Y:=" + mDownPosY2);
				}
				catch(IllegalArgumentException e){
					DebugUtil.e(TAG, "Manage ACTION_POINTER_2_DOWN Fail!");
				}
				break;
			case MotionEvent.ACTION_MOVE:
				try{
					mNowDownPosX1=nowPosX1=event.getX(p1);
					mNowDownPosY1=nowPosY1=event.getY(p1);
					mNowDownPosX2=nowPosX2=event.getX(p2);
					mNowDownPosY2=nowPosY2=event.getY(p2);
					DebugUtil.i(TAG, "Point1 xDistance:=" + (nowPosX1 - mDownPosX1) + ";  yDistance:=" + (nowPosY1 - mDownPosY1));
					DebugUtil.i(TAG, "Point2 xDistance:=" + (nowPosX2 - mDownPosX2) + ";  yDistance:=" + (nowPosY2 - mDownPosY2));
				}
				catch(IllegalArgumentException e) {
			         // TODO Auto-generated catch block 
			         e.printStackTrace(); 
			     } 
				break;
			}
			return true;
		}
		//
		return true;
	}
	
	//缩小
	public void zoomInX()
	{
		int oldZoomX = mZoomX;
		mZoomX = mZoomX+1;
		//重新装载当前页的数据
		reloadCurrentDIB(oldZoomX);
		
		//设置x轴缩放系数
//		BackPlayHRulerView HRuler;
//		HRuler = (BackPlayHRulerView)mParentActivity.findViewById(R.id.backplay_HRuler);
//		HRuler.setZoomX(mZoomX);
		
		//
		String text;
//		text = "x轴方向缩放系数:="+mZoomX;
//		Toast.makeText(this.getContext(), text, Toast.LENGTH_SHORT).show();
	}
	public void zoomOutX()
	{
		int oldZoomX;
		oldZoomX = mZoomX;
		if(mZoomX>1)
		{
			mZoomX = mZoomX-1;
			//重新装载当前页的数据
			reloadCurrentDIB(oldZoomX);
		}
		
		//设置x轴缩放系数
//		BackPlayHRulerView HRuler;
//		HRuler = (BackPlayHRulerView)mParentActivity.findViewById(R.id.backplay_HRuler);
//		HRuler.setZoomX(mZoomX);
		
		//
		String text;
//		text = "x轴方向缩放系数:="+mZoomX;
//		Toast.makeText(this.getContext(), text, Toast.LENGTH_SHORT).show();
	}
	//
	public boolean mFillfull = false;
	public void fillFullScreen()
	{
		if(mBackplayDir == BACKPLAY_NO)
			return;
		//
		int width = this.getWidth();
		int zoomx;
		int oldZoomX = mZoomX;
		zoomx = (int) (mTotalScans/width);
		if(zoomx>10)
			zoomx = 10;
		if(zoomx<1)
			zoomx = 1;
		mZoomX = zoomx;
		//设置x轴缩放系数
		BackPlayHRulerView HRuler;
//		HRuler = (BackPlayHRulerView)mParentActivity.findViewById(R.id.backplay_HRuler);
//		HRuler.setZoomX(mZoomX);
//		HRuler.invalidate();
		//重新装载当前页的数据
		reloadCurrentDIB(oldZoomX);
		
		//
		mFillfull = true;
	}
	//
	public void restoreZoomX()
	{
		int oldZoomX;
		oldZoomX = mZoomX;
		mZoomX = 1;
		String text;
		text = "x轴方向缩放系数:"+mZoomX;
//		Toast.makeText(this.getContext(), text, Toast.LENGTH_SHORT).show();
		
		//设置x轴缩放系数
		BackPlayHRulerView HRuler;
//		HRuler = (BackPlayHRulerView)mParentActivity.findViewById(R.id.backplay_HRuler);
//		HRuler.setZoomX(mZoomX);
		//
		reloadCurrentDIB(oldZoomX);
		
		//
		mFillfull = false;
	}
	//重新装载当前页
	/*
	 * 当缩放系数改变时，此时要对当前位图重新装载，并生成位图显示；
	 * 生成范围以当前显示位图的中间道为准.
	 * oldZoomX:老的缩放系数
	 */
	public void reloadCurrentDIB(int oldZoomX)
	{
		if(!mIsBackplaying)
			return;
		//
		MyApplication app;
		app = (MyApplication)this.getContext().getApplicationContext();
		////得到数据道长
		int scanLen;
		scanLen = mBackplayFileHeader.rh_nsamp*2;
		////得到当前位图中间位置对应的道号
		long centerPos;
		centerPos = mHeadReadPos + (mTailReadPos-mHeadReadPos)/2;
		int centerScan;
		centerScan = (int) ((centerPos-1024)/scanLen);
		DebugUtil.i(TAG, "Now reloadCurrentDIB CenterScan:=" + centerScan);
		
		////根据新的缩放系数，计算当前位图对应的起始和终止位置
		int pixsPerScan=1;
		if(isShowWiggle())
		{
			pixsPerScan = app.getWigglePixsPerScan();
		}
		//得到总共可以显示的道数
		int totalScans;
		totalScans = app.getScreenWidth()*mZoomX/pixsPerScan;
		/*
		//计算起始道号和终止道号
		int begScan=0,endScan=0;
		int tem1;
		begScan = centerScan-totalScans/2;
		tem1 = begScan;
		if(begScan<0)
		{
			begScan = 0;
		}
		endScan = begScan+totalScans;
		if(endScan>mTotalScans)
			endScan = (int) mTotalScans;
		//
		DebugUtil.i(TAG,"Now begScan:="+begScan+";endScan:="+endScan);
		*/
		int begScan = 0;
		int endScan = 0;
		//当前结束道
		int nowEndScan;
		nowEndScan = (int) ((mTailReadPos-1024)/scanLen);
		//确定开始道和结束道
		if(nowEndScan<=totalScans)
		{
			begScan = 0;
			endScan = nowEndScan;
		}
		else
		{
			begScan = centerScan-totalScans/2;
			endScan = begScan+totalScans;
		}
		if(endScan>mTotalScans)
		{
			endScan = (int) mTotalScans;
			begScan = endScan - totalScans;
		}
		if(begScan<0)
			begScan = 0;
		/////重新生成位图
		//设置位图默认色
		initDIB();
		//读取数据
		long offset;
		offset = 1024+begScan*scanLen;
		//跳到指定位置
		int needRLen;
		needRLen = (endScan-begScan)*scanLen;
		byte[] buf = new byte[needRLen];
		try{
			mBackplayFile = new FileInputStream(mFilename);
			//移动文件读取指针到指定位置
			mBackplayFile.skip(offset);
			needRLen=mBackplayFile.read(buf, 0, needRLen);
		}
		catch(Exception e)
		{
			DebugUtil.i(TAG, "reloadCurrentDIB() readDatas Fail!");
			return;
		}
		
		//设置记录变量
		mHeadReadPos = 1024+begScan*scanLen;
		mTailReadPos = mHeadReadPos + needRLen; //1024+endScan*scanLen;
		
		//
		endScan = (int) ((mTailReadPos-1024)/scanLen);
		int endPixs;
		if(isShowWiggle())
			pixsPerScan = app.getWigglePixsPerScan();
		endPixs = (endScan-begScan)/mZoomX*pixsPerScan;
		int scanIndex=0;
		if(endPixs<mDIBWidth)
			scanIndex = mDIBWidth-endPixs;
		//生成位图
		if(isShowDIB())
			changeDatasToDIB(buf,needRLen,scanIndex);
		if(isShowWiggle())
			changeDatasToWiggle(buf,needRLen,scanIndex);
		//
		this.invalidate();
	}
	
	//初始化位图
	public void initDIB()
	{
		MyApplication app;
		app = (MyApplication)this.getContext().getApplicationContext();
		int backCol = app.mColorPal.getBackColor();
		int i,j;
		if(isShowWiggle())
		{
			for(i=0;i<mDIBHeight;i++)
				for(j=0;j<mDIBWidth;j++)
					mDIBPixels[i*mDIBWidth+j]=backCol;
		}
		else
		{
			for(i=0;i<mDIBHeight;i++)
				for(j=0;j<mDIBWidth;j++)
					mDIBPixels[i*mDIBWidth+j]=0x00000000;
		}
	}
	
	//增益放大
	public boolean  mZoomout = false;
	public void zoomOutPlus()
	{
		mZoomPlus += 0.1;
		int zoom=(int) (mZoomPlus*10);
		mZoomPlus = zoom/10.;
		
		String text;
		text = "幅值放大倍数:"+mZoomPlus;
//		Toast.makeText(this.getContext(), text, 300).show();
		
		//
		reloadCurrentDIB(mZoomX);
		mZoomout = true;
	}
	
	//判断是否做了缩放操作
	public boolean isZoom()
	{
		return mZoomin || mZoomout;
	}
	//增益缩小
	public boolean mZoomin = false;
	public void zoomInPlus()
	{
		mZoomPlus -= 0.1;
		if(mZoomPlus < 0.1)
			mZoomPlus = 0.1;
		int zoom=(int) (mZoomPlus*10);
		mZoomPlus = zoom/10.;
		
		String text;
		text = "幅值缩小倍数:"+mZoomPlus;
//		Toast.makeText(this.getContext(), text, 300).show();
		
		//
		reloadCurrentDIB(mZoomX);
		mZoomin = true;
	}
	
	public double getZoomPlus()
	{
		return mZoomPlus;
	}
	//增益恢复
	public void zoomRestorePlus()
	{
		mZoomPlus = 1.0;
		int zoom=(int) (mZoomPlus*10);
		mZoomPlus = zoom/10.;
		/*
		String text;
		text = "幅值放大倍数:"+mZoomPlus;
		Toast.makeText(this.getContext(), text, 300).show();
		*/
		//
		reloadCurrentDIB(mZoomX);
		mZoomout = false;
		mZoomin = false;
	}
	
	//
	public void showMarkPoints()
	{
		this.invalidate();
	}
	
	//显示标定距离信息
	public void showMarkDistanceMsg()
	{
		int begScan,endScan;
		begScan = getScanindexFromeXPos(mFirstMarkDownPosX);
		endScan = getScanindexFromeXPos(mSecondMarkDownPosX);
		double distancePerScan;      //相邻两道的距离(cm);
		distancePerScan = mBackplayFileHeader.getDistancePerScans();
		double distance;
		distance = distancePerScan*(endScan-begScan);
		distance = ((int)(distance*100))/100.;
		
		//显示提示信息
		TextView txtView;
//		txtView = (TextView)mParentActivity.findViewById(R.id.textview_backplay_picmsg);
//		txtView.setVisibility(View.VISIBLE);
		String msgTxt = "水平距离:" + distance + "厘米;";
		
		//
		MyApplication app;
		app = (MyApplication)this.getContext().getApplicationContext();
		
		//
		int scanLen;
		scanLen = mBackplayFileHeader.rh_nsamp;
		double timeWnd;
		timeWnd = mBackplayFileHeader.getTimeWindow();
		double coeffY;
		coeffY = timeWnd/this.getHeight();
		double tem;   //坐标差
		tem = mSecondMarkDownPosY - mFirstMarkDownPosY;
		tem = tem*coeffY;
		int temTime;
		temTime = (int) (tem*10);
		tem = temTime/10.;
		
		//计算深度
		double deep;
		deep = mBackplayFileHeader.getDeep();
		double coeffDeep;
		coeffDeep = deep/getHeight();
		deep = (mSecondMarkDownPosY-mFirstMarkDownPosY)*coeffDeep;
		int temDeep;
		temDeep = (int) (deep*1000);
		deep = temDeep/1000.;
		
		msgTxt += "垂直距离:"+tem+"ns"+" "+deep+"米";
		//
//		txtView.setText(msgTxt);
	}
	//
	public int getBackplaySpeed()
	{
		return mBackplaySpeed;
	}
}

