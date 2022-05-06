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
 * ʵ��      "���Ʋɼ�"  �Ļط�
 */
public class backPlayDIBView extends View {
	
	private String TAG="BackPlayDIBView";       //����ı�ʶ
	public Activity mParentActivity;
	///�ط��ļ���ر���
	private String mFilename;
	private FileInputStream mBackplayFile=null;      //
	private long mHadReadLength = 0;                 //�Ѿ���ȡ���״����ݳ��ȣ��ֽڣ�
	private long mBackfileLength = 0;                //�ط������ļ��ĳ���(�ֽ�):�����ļ�ͷ
	public FileHeader mBackplayFileHeader = new FileHeader();   //�ط������ļ�ͷ
	private long mHeadReadPos;        //ͷλ��
	public  long mTailReadPos;        //βλ��
	public int  mZoomX=1;            //����ϵ��
	
	///�ط�λͼ
	private int mDIBHeight = 512;
	private int mDIBWidth  = 1000;  //1280;  //2016.6.10
	private int mBegShowDIBScan = 0;        //��ǰ����Ļ����ʾ����ʼ����
	private int mHadCreateDIBWidth = 0;     //�Ѿ����ɵ�λͼ���
	private int[] mDIBPixels = new int[mDIBHeight*mDIBWidth];
	
	////�״�����ʵʱ��ȡ��ʱ��
	private Handler mBackplayHandler = new Handler();
	private long mBackplayDelayTime = 100L;
	private boolean  mIsBackplaying = false;    //���ڻطű�־
	private boolean  mBackplayPause = false;    //��ͣ�ط�
	private int  mBackplaySpeed=5;              //�ط��ٶ�
	
	private final int BACKPLAY_NO=0;
	private final int BACKPLAY_FORWARD_DIR=1;      //��ǰ����
	private final int BACKPLAY_BACK_DIR=2;         //������
	private int  mBackplayDir = BACKPLAY_NO;
	//
	private boolean mIsStopBackplayTimer=false;
	
	//
	private GestureDetector detector;
	private myGestureListener gListener;
	
	//����һЩ����ͣģʽ�´�������ı���
	private float mSingleDownPosX;    //������x����
	private float mSingleDownPosY;    //������y����
	private float mSingleNowPosX;     //�����ƶ�ʱx����
	private float mSingleNowPosY;     //�����ƶ�ʱy����
	
	//����ѡ���������
	private boolean mIsSelectRect=false;    //
	Rect mSelectRect;   //ѡ�������Χ
	private double mZoomPlus = 1.0;
	
	//�Ƿ����ڱ궨����
	private boolean mIsDistanceMark=false;
	private boolean mFirstPointsExist=false;  //��һ����ʼ���Ƿ��Ѿ�����
	private boolean mSecondPointsExist = false;
	private float mFirstMarkDownPosX,mFirstMarkDownPosY;
	private float mSecondMarkDownPosX,mSecondMarkDownPosY;

	public long mTotalScans=0;   //�ܹ��طŵĵ���
	
	//��������
	private int BACKGROUND_DEFAULTSCANS=300;
	private double[] mBackgroundDatas = new double[8192];
	private int mBackgroundScans = 300;
	private double mHadRcvBackgroundScans = 0;
	private boolean mIsBackgroundOk = false;   //�������ݾ���
	public  boolean mIsRemoveBackground = false;  //�Ƿ���б�������
	private short[] mFirstBackgroundDatas = new short[8192];
	
	//��ʾ����
	private int DIB_BITMAP=1;
	private int WIGGLE_BITMAP=2;
	private int mDIBType = DIB_BITMAP;//WIGGLE_BITMAP;//
		
	//�ֶ��������ò���
	private int mNowAdjustScan=0;    //��ǰ�ֶ���������ĵ���
	public boolean mIsAdjustManuPlus = false;    //�Ƿ��ֶ���������
	private short[]  mManuplusScanDatas = new short[8192];      //���ڵ��������ݵ�ԭʼ����
	private double[] mManuplus = new double[8192];              //����ֵ
	private Paint mWavePaint = new Paint();      //�������εĻ���
	private Paint mHardplusNormalPointPaint = new Paint();      //һ��Ӳ�������Ļ���
	private Paint mHardplusSelectPointPaint = new Paint();      //ѡ��Ӳ�������Ļ���
	private Paint mHardplusLinePaint = new Paint();             //Ӳ���������ߵĻ���
	private Paint mHardplusRangePaint = new Paint();
	private int   mSelHardplusIndex=-1;                          //ѡ�е�Ӳ�������
	private int   mManuplusRangeWidth = 200;
	private float[]  mManuPlusPointsVal = new float[9];         //�ֶ��������ֵ
	
	//���캯��
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
    	//���㵱ǰ�������Ӳ���λ��
    	long rPos;
    	rPos = (mTailReadPos+mHeadReadPos)/2;
    	int scanLen;
    	scanLen = mBackplayFileHeader.rh_nsamp*2;
    	int scans;
    	scans = (int) ((rPos-1024)/scanLen);
    	mNowAdjustScan = scans;
    	
    	//��ȡһ������
    	long offset;
    	int retLen;
    	byte[] buf = new byte[scanLen*2];
    	offset = 1024+scanLen*scans;
    	try{
    		mBackplayFile = new FileInputStream(mFilename);
    		//�ƶ��ļ���ȡָ�뵽ָ��λ��
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
		//���㲨�εķŴ���
		//���㱶��
		int scanLen = mBackplayFileHeader.rh_nsamp;
		double[] zoomBase = new double[9];
		
		//��������б��
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
		//�������Բ�ֵ�㷨����Ŵ���
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
				mManuplus[index] = zoomBase[i]+zoom1*j;
			}
		}
	}
	
    //��ʾ�ֶ������������
	public void showManuplusAdjustRange()
    {
    	//���㵱ǰ�������Ӳ���λ��
    	long rPos;
    	rPos = (mTailReadPos+mHeadReadPos)/2;
    	int scanLen;
    	scanLen = mBackplayFileHeader.rh_nsamp*2;
    	int scans;
    	scans = (int) ((rPos-1024)/scanLen);
    	mNowAdjustScan = scans;
    	
    	//��ȡһ������
    	long offset;
    	int retLen;
    	byte[] buf = new byte[scanLen*2];
    	offset = 1024+scanLen*scans;
    	try{
    		mBackplayFile = new FileInputStream(mFilename);
    		//�ƶ��ļ���ȡָ�뵽ָ��λ��
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
    
    //�����ֶ�������ڴ���
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
    //���ݵ�ǰx����λ�ã��õ���Ӧ�ĵ���
    public int getScanindexFromeXPos(float posX)
    {
    	int scanIndex = 0;
    	int scanLen;
    	scanLen = mBackplayFileHeader.rh_nsamp*2;
    	MyApplication app;
    	app = (MyApplication)getContext().getApplicationContext();
    	int srcWidth;
    	srcWidth = app.getScreenWidth();
    	//������ֹ����
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
    
    
    /////�Զ���GestureListener��
    /*
     * ����һЩ���⴦���⣬�󲿷ִ�������false���Ա����Ӵ��ļ���onTouchEvent������Щ��Ϣ
     */
  	public class myGestureListener implements GestureDetector.OnGestureListener, OnDoubleTapListener
  	{
  		@Override
  		public boolean onDown(MotionEvent e) {
  			DebugUtil.i(TAG, "onDown");
  			//���о������ȵ���Ϣ��ʾ
  			if(mIsDistanceMark)
  			{
  				if(!mFirstPointsExist)
  				{
  					mFirstPointsExist = true;
  					mSecondMarkDownPosX = mFirstMarkDownPosX = e.getX();
  					mSecondMarkDownPosY = mFirstMarkDownPosY = e.getY();
  					DebugUtil.i(TAG, "First downX:=" + mFirstMarkDownPosX + ";downY:=" + mFirstMarkDownPosY);
  					//��ʾ����궨��λ��
  					showMarkPoints();
  				}
  				else
  				{
  					mSecondPointsExist = true;
  					mSecondMarkDownPosX = e.getX();
  					mSecondMarkDownPosY = e.getY();
  					DebugUtil.i(TAG, "Second downX:=" + mSecondMarkDownPosX + ";downY:=" + mSecondMarkDownPosY);
  					//��ʾ����궨��λ��
  					showMarkPoints();
  					////��ʾ������Ϣ
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
  			//����ͼ�����ʾ����
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
  		     DebugUtil.i(TAG, "onDoubleTap---->����˫���¼�");
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
			Toast.makeText(this.getContext(), "����ѻ�ͼ��ʾģʽ", Toast.LENGTH_SHORT).show();
			//����ˮƽ��߲���
//			rulerView.setPixsPerScan(app.getWigglePixsPerScan());
		}
		else
		{
			mDIBType = DIB_BITMAP;
			Toast.makeText(this.getContext(), "�����ɫͼ��ʾģʽ", Toast.LENGTH_SHORT).show();
			//����ˮƽ��߲���
//			rulerView.setPixsPerScan(1);
		}
	}
  	
  	//���ĵ�ɫ��
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
		Toast.makeText(this.getContext(), "�����ɫͼ��ʾģʽ", Toast.LENGTH_SHORT).show();
		//����ˮƽ��߲���
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
		Toast.makeText(this.getContext(), "����ѻ�ͼ��ʾģʽ", Toast.LENGTH_SHORT).show();
		//����ˮƽ��߲���
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
		
		//���ø������ʵ�����
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
    //��ʼ��������ѡ��
    public void beginSelectRect()
    {
    	mIsSelectRect = true;
    }
    //ȡ������ѡ��
    public void cancelSelectRect()
    {
    	mIsSelectRect = false;
    }
    //��ʼ����궨
    public void beginDistanceMark()
    {
    	mIsDistanceMark = true;
    	mFirstPointsExist = false;
    	mSecondPointsExist = false;
    }
    //ȡ������궨
    public void cancelDistanceMark()
    {
    	mIsDistanceMark = false;
    	mFirstPointsExist = false;
    	mSecondPointsExist = false;
    	//
    	//��ʾ��ʾ��Ϣ
//    	TextView txtView;
//    	txtView = (TextView)mParentActivity.findViewById(R.id.textview_backplay_picmsg);
//    	txtView.setVisibility(View.INVISIBLE);
    	
    	//
    	invalidate();
    }
	//�������κ���
	@Override
	protected void onDraw(Canvas canvas)
	{
		//����dibλͼ
		drawDIB(canvas);
		
		if(mIsDistanceMark)
			//���������ע��λ��
			drawDistanceMarks(canvas);
		
		//�����ֶ������������
//		if(mIsAdjustManuPlus)
//			drawManuplusAdjust(canvas);
	}
	//�����ֶ������������
	public void drawManuplusAdjust(Canvas canvas)
	{
		MyApplication app;
		app = (MyApplication)(getContext().getApplicationContext());
		////���㵱ǰ�������ε���ʾλ��
		int scans;
		int scanLen;
		scanLen = mBackplayFileHeader.rh_nsamp*2;
		scans = (int) ((mTailReadPos-mHeadReadPos)/scanLen);
		double pixsCoeff;
		pixsCoeff = getWidth()/scans;
		int orgX,orgY;
		orgX = (int) (pixsCoeff*mNowAdjustScan);
		orgY = 0;
		
		////�������ڱ߿�
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
		
		////������������
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
		
		////��������
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
	
	//��������궨λ��
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
		
		//�õ�λͼ��ʾ��Χ
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
	//�õ�dibͼ��ʾʹ�õľ��η�Χ
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
	
	//��ʼ�ط��״������ļ�
	public boolean beginBackplay(String fileName)
	{
		//
		mFilename = fileName;
		//���ط��ļ��Ƿ����
		File file = new File(mFilename);
		if(!file.exists())
			return false;	
		//�õ��ļ���
		mBackfileLength = file.length();
	
		//��ȡ�ļ�ͷ
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
		
		//�ر��ļ�
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
		//���ñ�����������
		createBackGround();
		
		//������ʱ��
		mBackplayHandler.postDelayed(mBackplayUpdateTimeTask,mBackplayDelayTime);
		mIsStopBackplayTimer=false;
		
		//
		MyApplication app;
    	app = (MyApplication)getContext().getApplicationContext();
		if(app.mRadarDevice.mIsUseSoftPlus)
		{
			////��������ֵ
			this.setManuHardplus(mBackplayFileHeader.rh_rgainf);
		}
		
		////����ʱ���� ��ȱ�� 2016.6.10
		((BackPlayVRulerView)(app.mBTimewndRuler)).setTimeWindow(mBackplayFileHeader.getTimeWindow());
		((BackPlayVRulerView)(app.mBDeepRuler)).setDeep(mBackplayFileHeader.getDeep());
		//����ˮƽ���
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
		//����MainActivity��mBackplayFileHeader��������
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
		//����ָ��λ��
		try{
			mBackplayFile = new FileInputStream(mFilename);
			//�ƶ��ļ���ȡָ�뵽ָ��λ��
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
	
	////��Ϊ����������仯������λͼ�ƶ�
	public void scrollDibForPointsmove()
	{
		//�����������ͣģʽ�������и������
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
		//��ȡָ����������
		int scanLen;
		scanLen = mBackplayFileHeader.rh_nsamp;
		//ȷ��Ҫ�����ĵ���
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
		int needRLength = scanLen*srcScans*2*mZoomX;   //��Ҫ��ȡ�����ݳ���
		byte[] buf = new byte[needRLength];
		int readLen;
		int scans;
		int moveScans;
		
		//xOff>0ʱ��������ͼ
		if(xOff>0)
		{
			readLen = readDatas_BackDir(buf,needRLength);
			scans = readLen/mBackplayFileHeader.rh_nsamp/2;
			scans = scans/mZoomX;
			moveScans = scans;
			//�ж��Ƿ�ֻ�ǹ���ͼ��
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
				//����ȡ������ת����dibλͼ
				changeDatasToDIB(buf,readLen,0);
			}
			if(isShowWiggle())
			{
				DebugUtil.i(TAG, "Now scroll wiggle:=" + scans + ";pixsPerScan:=" + pixsPerScan);
				ScrollDIBBack(moveScans*pixsPerScan);
				clearDIBFromeBegin(moveScans*pixsPerScan);
				//�����������ת���ɶѻ�ͼ
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
				//����ȡ������ת����dibλͼ
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
					//�����������ת���ɶѻ�ͼ
					changeDatasToWiggle(buf,readLen,mDIBWidth-scans*pixsPerScan);
				}
			}
			
			DebugUtil.i(TAG, "Now move forward:=" + scans);
		}
		//������ڽ����ֶ�������ڣ���ʱ������ڲ���
		if(mIsAdjustManuPlus)
		{
			setManuAdjustScanDatas();
		}
		DebugUtil.i(TAG, "Now scroll:=" + xOff);
		
		//����ˮƽ��ߵ�����Χ
//		LTDMainActivity ltdActivity;
		int totalScans;
		totalScans = (int) ((mTailReadPos-1024)/mBackplayFileHeader.rh_nsamp/2);
//		ltdActivity = (LTDMainActivity)mParentActivity;
//		ltdActivity.setBackplayHRulerScans(totalScans);
		
		//
		invalidate();
	}
	
	//λͼ����ʱ���ӵ�ǰ�ļ�ͷ����ȡָ����������
	public int readDatasBackDir(byte[] buf,int needLen)
	{
		int retLen=0;
		//�����ļ���ȡָ���λ��
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
			//�ƶ��ļ���ȡָ�뵽ָ��λ��
			mBackplayFile.skip(offset);
			retLen=mBackplayFile.read(buf, 0, needLen);
		}
		catch(Exception e)
		{
			return 0;
		}
		
		///�Ѿ�����ͷ���Ĵ�����ں���
		if(retLen == 0)
		{
			
		}
		///��û�е����ļ�ͷ
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
	//�ӵ�ǰ���ļ�β������ָ����������
	public int readDatasForwardDir(byte[] buf,int needLen)
	{
		int retLen=0;
		//�����ļ���ȡָ���λ��
		long offset=0;
		int scanLen;
		scanLen = mBackplayFileHeader.rh_nsamp*2;
		//���������ǰ�ط�
		offset = mTailReadPos;

		//
		try{
			mBackplayFile = new FileInputStream(mFilename);
			//�ƶ��ļ���ȡָ�뵽ָ��λ��
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
		//�޸ļ�¼����
		mTailReadPos = mTailReadPos + retLen;
		if(mTailReadPos >= 1024+mDIBWidth*mZoomX*scanLen/pixsPerScan)
			mHeadReadPos = mTailReadPos - mDIBWidth*mZoomX*scanLen/pixsPerScan;

		//
		return retLen;
	}
	////��ǰ����λͼ
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
	////������λͼ
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
	public int NO_MARK = 0;   //û�б��
	public int BIG_MARK_FLAG = 1;
	public int SMALL_MARK_FLAG =2;
	public int BIG_MARK_VAL = 0x4000;    //���(����)
	public int SMALL_MARK_VAL = 0x8000;  //С��(����)
	public int mMarkType = NO_MARK;      //�������
	////������ת���� "�ѻ�" ͼ
	public void changeDatasToWiggle(byte[] buf,int bufLen,int begScanIndex)
	{
		//������ת����short��
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
		
		//�õ�������ÿ������ռ�õ����ص���
		MyApplication app;
		app = (MyApplication)(getContext().getApplicationContext());
		int mScanLen;
		mScanLen = mBackplayFileHeader.rh_nsamp;
		int pixsPerScan;
		pixsPerScan = app.getWigglePixsPerScan();
		
		//���������ܵ���
		int scans;
		scans = bufLen/2;
		scans = scans/mScanLen;
		//����ˮƽ�����ϵ�����
		scans = scans/mZoomX;
				
		//ת��λͼ
		int colNumber;   //��ɫ����ɫ��
		int[][] mColPal;
		int palIndex;
		palIndex = app.mColorPal.getColpalIndex();
		colNumber = app.mColorPal.getColorNumber();
		mColPal = app.mColorPal.getColors();
		
		//����ɫ(�����ֵ��ɫ)
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
		//ȷ�������ʼλ��
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
			//�������Ŵ���
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
			//����������ֶ�����
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
			//����ж�
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
					//��Ǵ���
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
					///����Ҫ���ļ���
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
			//��������
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
	
	////����λͼ
	/*
	 * buf:����λͼ������;
	 * bufLen:���ݳ���
	 * begScanIndex:λͼ�����ʼλ��
	 * 
	 */
	public void changeDatasToDIB(byte[] buf,int bufLen,int begScanIndex)
	{
		///ת�����ݸ�ʽ?
		/*
		 *��û��һ�ַ���ֱ�Ӳ���short�����ݣ�������Ҫ����ת�� 
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
		
		//����Ҫ���ɵ�λͼ���ص���
		int scans;
		scans = bufLen/2;
		scans = scans/mScanLen;
		scans = scans/mZoomX;
								
		//ת��λͼ
		int colNumber;   //��ɫ����ɫ��
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
		//ȷ�����λ��
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
			//��������ϵ�������������ת����һ������
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
			
			//��������
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
			//����������ֶ�����
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
			//����ж�
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
				//��������
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
		//��ʼ��dib
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
	////�Ƿ���ͣ�ط�:��ͣ|�Ѿ��طŵ�β|�Ѿ��طŵ�ͷ
	public boolean isBackplayPause()
	{
//		DebugUtil.i(TAG,"now backplaypause:="+mBackplayPause);
		//����Ƿ��ڻط�ģʽ
		if(!isBackPlaying())
			return false;
		//��ͣ�ط�
		if(mBackplayPause)
			return true;

		//��ǰ�طŲ����Ѿ���������β��
		if(isBackPlayForwardDir())
		{
			if(mBackfileLength == mTailReadPos)
				return true;
		}
		
		//���طŲ����Ѿ����������׶�
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
	////�Ƿ�ֹͣ�ط�
	public boolean isBackplayEnd()
	{
		return !mIsBackplaying; 
	}
	////������ͣ״̬
	public void setBackplayPauseStatus(boolean isPause)
	{
		mBackplayPause = isPause;
	}
	//�õ�Ҫ�طŵ��ļ��ܵ����ݳ���
	public long getTotalBackplayBytes()
	{
		File file = new File(mFilename);
		if(!file.exists())
			return 0;
		return file.length();
	}
	
	/**
	 * ���ñ���������־
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
	
	//�õ��Ѿ���ȡ�����ݵ���
	public int getNowHasReadScans()
	{
		int scanLen;
		scanLen = mBackplayFileHeader.rh_nsamp;
		return (int)(mHadReadLength/scanLen/2);
	}
	//�õ��Ѿ����ɵ�dibͼ���
	public int getHadCreateDIBWidth()
	{
		return mHadCreateDIBWidth;
	}
	//�õ��Ѿ���ȡ�����ݳ���
	public int getHadReadDatasLength()
	{
		return (int)mHadReadLength;
	}
	//���ûط��ٶ�
	public void setBackplaySpeed(int speed)
	{
		mBackplaySpeed = speed;
	}
	///������ʱ��ȡ����
	public int readDatas_BackDir(byte[] buf,int needLen)
	{
		int retLen=0;
		//�����ļ���ȡָ���λ��
		long offset=0;
		int scanLen;
		scanLen = mBackplayFileHeader.rh_nsamp*2;
		
		////����offset
		int addScan = 1;
		//����Ѿ��طŵ�ͷλ��
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
		
		//����ָ��λ�ò���ȡ����
		try{
			mBackplayFile = new FileInputStream(mFilename);
			//�ƶ��ļ���ȡָ�뵽ָ��λ��
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
		
		///�Ѿ�����ͷ���Ĵ�����ں���
		if(retLen == 0)
		{
				
		}
		///��û�е����ļ�ͷ
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
		//�����ļ���ȡָ���λ��
		long offset=0;
		int scanLen;
		scanLen = mBackplayFileHeader.rh_nsamp*2;
		///addScan:Ϊ����ʾ�ѻ��������⴦���������
		int addScan;
		addScan = 1;
		////����offset
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
		
		//����ָ��λ��
		try{
			mBackplayFile = new FileInputStream(mFilename);
			//�ƶ��ļ���ȡָ�뵽ָ��λ��
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
		
		//����βָ��
		mTailReadPos = mTailReadPos + retLen;          //βָ��
		if(isShowWiggle())
			mTailReadPos = mTailReadPos - scanLen*addScan*mZoomX;
		//����ͷָ��
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
	///��ȡ�״�����
	public int readDatas(byte[] buf,int needLen)
	{
		int retLen=0;
		//�����ļ���ȡָ���λ��
		long offset=0;
		int scanLen;
		scanLen = mBackplayFileHeader.rh_nsamp*2;
		
		////����offset
		//�����������ط�
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
		
		//������ڷ���ط�
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
		
		//����ָ��λ��
		try{
			mBackplayFile = new FileInputStream(mFilename);
			//�ƶ��ļ���ȡָ�뵽ָ��λ��
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
		
		//�޸ļ�¼����
		if(isBackPlayForwardDir())
		{
			//����βָ��
			mTailReadPos = mTailReadPos + retLen;          //βָ��
			if(isShowWiggle())
				mTailReadPos = mTailReadPos - scanLen*1*mZoomX;
			//����ͷָ��
			if(mTailReadPos >= 1024+mDIBWidth*mZoomX*scanLen/pixsPerScan)    
				mHeadReadPos = mTailReadPos - mDIBWidth*scanLen*mZoomX/pixsPerScan;
		}
		if(isBackPlayBackDir())
		{
			///�Ѿ�����ͷ���Ĵ�����ں���
			if(retLen == 0)
			{
				
			}
			///��û�е����ļ�ͷ
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
	///���ݸ��¶�ʱ������
    private Runnable mBackplayUpdateTimeTask = new Runnable(){
		@Override
		public void run(){
			////
			if(mIsStopBackplayTimer)
				return;
			////��ȡ���ݲ�����λͼ��ʾ
			if(isBackplayEnd())
				return;
			////�ж��Ƿ�����ͣģʽ
			if(!isBackplayPause())
			{	
				//�������
				if(isBackPlayForwardDir())
				{
					backPlayForward();
				}
				//�������
				if(isBackPlayBackDir())
				{
					backPlayBack();
				}
				//������ڽ����ֶ�������ڣ���ʱ������ڲ���
				if(mIsAdjustManuPlus)
				{
					setManuAdjustScanDatas();
				}
				//
				invalidate();
			}
			
			//���õ���
			int totalScans;
			totalScans = (int) ((mTailReadPos-1024)/mBackplayFileHeader.rh_nsamp/2);
			((MultiModeLifeSearchActivity)mParentActivity).setBackplayScans(totalScans);
			((MultiModeLifeSearchActivity)mParentActivity).setBackplayHRulerScans(totalScans);
			//����ˮƽ��� 2016.6.10
			
			//
			mBackplayHandler.postDelayed(this, mBackplayDelayTime);
		}
	};
	////�������
	public void backPlayForward()
	{
		//���ջط��ٶȶ�ȡָ����������
		int scanLen;
		scanLen = mBackplayFileHeader.rh_nsamp;
		int readScans;
		readScans = mBackplaySpeed;
		int addScans = 1;
		//��ʾ�ѻ�ͼ����ʱ����Ҫ��ȡ�ĵ���:���һ�����ݣ������������ɹ�����ĵ�һ������λͼ����ֹ��Ļ������Ĳ�����.
		if(isShowWiggle())
			readScans = mBackplaySpeed+addScans;
		int needRLength = scanLen*readScans*2*mZoomX;   //��Ҫ��ȡ�����ݳ���
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
		////����λͼ�����ĵ���
		int scans;
		scans = readLen/mBackplayFileHeader.rh_nsamp/2;
		scans = scans/mZoomX;
		scans = scans*pixsPerScan;
		
		////����λͼ
		DebugUtil.i(TAG, "forward scroll :=" + scans);
		int srcScans = scans;
		if(isShowWiggle())
			srcScans = srcScans-addScans*pixsPerScan;
		ScrollDIBForward(srcScans);
		
		if(isShowDIB())
		{
			//�����������ת����dibͼ
			for(int i = 0;i<10;i++)
			{
				DebugUtil.i(TAG, "buf[" + i + "]=" + buf[i]);
			}
			
			changeDatasToDIB(buf,readLen,mDIBWidth-scans);
			DebugUtil.i(TAG, "changeDatasToDIB");
		}
		if(isShowWiggle())
		{
			//���ճ��Ĳ�������
			clearDIBFromeTail(srcScans);
			//�����������ת���ɶѻ�ͼ
			changeDatasToWiggle(buf,readLen,mDIBWidth-scans);
		}
	}
	
	////�������
	public void backPlayBack()
	{
		//���ջط��ٶȶ�ȡָ����������
		int scanLen;
		scanLen = mBackplayFileHeader.rh_nsamp;
		int needScans;
		needScans = mBackplaySpeed;
		//�ѻ�ͼ�£�������ȡ�ĵ���:����һ�����ݣ���ָ�����������ɵ�ǰ��ʾ�ĵ�һ��ͼ�������.
		if(isShowWiggle())
			needScans = needScans+1;
		int needRLength = scanLen*needScans*2*mZoomX;   //��Ҫ��ȡ�����ݳ���
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
		////����λͼ�����ĵ���
		int scans;
		scans = readLen/mBackplayFileHeader.rh_nsamp/2;
		scans = scans/mZoomX;
		
		//
		int srcScans = scans;
		//�ж��Ƿ�ֻ�ǹ���ͼ��,��:�Ѿ������ļ�ͷ����ֻ�������Ϳ�����
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
			//����ȡ������ת����dibλͼ
			changeDatasToDIB(buf,readLen,0);
		}
		if(isShowWiggle())
		{
			//���ճ��Ĳ�������
			clearDIBFromeBegin(srcScans*pixsPerScan);
			//�����������ת���ɶѻ�ͼ
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
		//��չ�������
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
		
		//��չ�������
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
	/////����״̬�µĶ�㴦��
	float mDownPosX1,mDownPosX2;
	float mDownPosY1,mDownPosY2;
	float mNowDownPosX1,mNowDownPosX2;
	float mNowDownPosY1,mNowDownPosY2;
	boolean isManageManyPoints=false;
	public boolean manageManyPoints(MotionEvent event)
	{
		//�õ���������ָ������������Ŀ������Ӧ����
		int num;
		num=event.getPointerCount();
		String msg= "�Ѿ�����:" + num + "������";
		DebugUtil.i(TAG, msg);
		
		//�������3����ô��ʱҪ���лطŷ�ʽ�л�������
		//     ����false,�������������������LTDMainActivity,�ж��Ƿ�Ҫ���лط�ģʽ���л�
		if(num>=3)
		{
			return false;
		}
		//���=2����ô��������|��ദ��
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
				//���Ŵ���
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
	
	//��С
	public void zoomInX()
	{
		int oldZoomX = mZoomX;
		mZoomX = mZoomX+1;
		//����װ�ص�ǰҳ������
		reloadCurrentDIB(oldZoomX);
		
		//����x������ϵ��
//		BackPlayHRulerView HRuler;
//		HRuler = (BackPlayHRulerView)mParentActivity.findViewById(R.id.backplay_HRuler);
//		HRuler.setZoomX(mZoomX);
		
		//
		String text;
//		text = "x�᷽������ϵ��:="+mZoomX;
//		Toast.makeText(this.getContext(), text, Toast.LENGTH_SHORT).show();
	}
	public void zoomOutX()
	{
		int oldZoomX;
		oldZoomX = mZoomX;
		if(mZoomX>1)
		{
			mZoomX = mZoomX-1;
			//����װ�ص�ǰҳ������
			reloadCurrentDIB(oldZoomX);
		}
		
		//����x������ϵ��
//		BackPlayHRulerView HRuler;
//		HRuler = (BackPlayHRulerView)mParentActivity.findViewById(R.id.backplay_HRuler);
//		HRuler.setZoomX(mZoomX);
		
		//
		String text;
//		text = "x�᷽������ϵ��:="+mZoomX;
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
		//����x������ϵ��
		BackPlayHRulerView HRuler;
//		HRuler = (BackPlayHRulerView)mParentActivity.findViewById(R.id.backplay_HRuler);
//		HRuler.setZoomX(mZoomX);
//		HRuler.invalidate();
		//����װ�ص�ǰҳ������
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
		text = "x�᷽������ϵ��:"+mZoomX;
//		Toast.makeText(this.getContext(), text, Toast.LENGTH_SHORT).show();
		
		//����x������ϵ��
		BackPlayHRulerView HRuler;
//		HRuler = (BackPlayHRulerView)mParentActivity.findViewById(R.id.backplay_HRuler);
//		HRuler.setZoomX(mZoomX);
		//
		reloadCurrentDIB(oldZoomX);
		
		//
		mFillfull = false;
	}
	//����װ�ص�ǰҳ
	/*
	 * ������ϵ���ı�ʱ����ʱҪ�Ե�ǰλͼ����װ�أ�������λͼ��ʾ��
	 * ���ɷ�Χ�Ե�ǰ��ʾλͼ���м��Ϊ׼.
	 * oldZoomX:�ϵ�����ϵ��
	 */
	public void reloadCurrentDIB(int oldZoomX)
	{
		if(!mIsBackplaying)
			return;
		//
		MyApplication app;
		app = (MyApplication)this.getContext().getApplicationContext();
		////�õ����ݵ���
		int scanLen;
		scanLen = mBackplayFileHeader.rh_nsamp*2;
		////�õ���ǰλͼ�м�λ�ö�Ӧ�ĵ���
		long centerPos;
		centerPos = mHeadReadPos + (mTailReadPos-mHeadReadPos)/2;
		int centerScan;
		centerScan = (int) ((centerPos-1024)/scanLen);
		DebugUtil.i(TAG, "Now reloadCurrentDIB CenterScan:=" + centerScan);
		
		////�����µ�����ϵ�������㵱ǰλͼ��Ӧ����ʼ����ֹλ��
		int pixsPerScan=1;
		if(isShowWiggle())
		{
			pixsPerScan = app.getWigglePixsPerScan();
		}
		//�õ��ܹ�������ʾ�ĵ���
		int totalScans;
		totalScans = app.getScreenWidth()*mZoomX/pixsPerScan;
		/*
		//������ʼ���ź���ֹ����
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
		//��ǰ������
		int nowEndScan;
		nowEndScan = (int) ((mTailReadPos-1024)/scanLen);
		//ȷ����ʼ���ͽ�����
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
		/////��������λͼ
		//����λͼĬ��ɫ
		initDIB();
		//��ȡ����
		long offset;
		offset = 1024+begScan*scanLen;
		//����ָ��λ��
		int needRLen;
		needRLen = (endScan-begScan)*scanLen;
		byte[] buf = new byte[needRLen];
		try{
			mBackplayFile = new FileInputStream(mFilename);
			//�ƶ��ļ���ȡָ�뵽ָ��λ��
			mBackplayFile.skip(offset);
			needRLen=mBackplayFile.read(buf, 0, needRLen);
		}
		catch(Exception e)
		{
			DebugUtil.i(TAG, "reloadCurrentDIB() readDatas Fail!");
			return;
		}
		
		//���ü�¼����
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
		//����λͼ
		if(isShowDIB())
			changeDatasToDIB(buf,needRLen,scanIndex);
		if(isShowWiggle())
			changeDatasToWiggle(buf,needRLen,scanIndex);
		//
		this.invalidate();
	}
	
	//��ʼ��λͼ
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
	
	//����Ŵ�
	public boolean  mZoomout = false;
	public void zoomOutPlus()
	{
		mZoomPlus += 0.1;
		int zoom=(int) (mZoomPlus*10);
		mZoomPlus = zoom/10.;
		
		String text;
		text = "��ֵ�Ŵ���:"+mZoomPlus;
//		Toast.makeText(this.getContext(), text, 300).show();
		
		//
		reloadCurrentDIB(mZoomX);
		mZoomout = true;
	}
	
	//�ж��Ƿ��������Ų���
	public boolean isZoom()
	{
		return mZoomin || mZoomout;
	}
	//������С
	public boolean mZoomin = false;
	public void zoomInPlus()
	{
		mZoomPlus -= 0.1;
		if(mZoomPlus < 0.1)
			mZoomPlus = 0.1;
		int zoom=(int) (mZoomPlus*10);
		mZoomPlus = zoom/10.;
		
		String text;
		text = "��ֵ��С����:"+mZoomPlus;
//		Toast.makeText(this.getContext(), text, 300).show();
		
		//
		reloadCurrentDIB(mZoomX);
		mZoomin = true;
	}
	
	public double getZoomPlus()
	{
		return mZoomPlus;
	}
	//����ָ�
	public void zoomRestorePlus()
	{
		mZoomPlus = 1.0;
		int zoom=(int) (mZoomPlus*10);
		mZoomPlus = zoom/10.;
		/*
		String text;
		text = "��ֵ�Ŵ���:"+mZoomPlus;
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
	
	//��ʾ�궨������Ϣ
	public void showMarkDistanceMsg()
	{
		int begScan,endScan;
		begScan = getScanindexFromeXPos(mFirstMarkDownPosX);
		endScan = getScanindexFromeXPos(mSecondMarkDownPosX);
		double distancePerScan;      //���������ľ���(cm);
		distancePerScan = mBackplayFileHeader.getDistancePerScans();
		double distance;
		distance = distancePerScan*(endScan-begScan);
		distance = ((int)(distance*100))/100.;
		
		//��ʾ��ʾ��Ϣ
		TextView txtView;
//		txtView = (TextView)mParentActivity.findViewById(R.id.textview_backplay_picmsg);
//		txtView.setVisibility(View.VISIBLE);
		String msgTxt = "ˮƽ����:" + distance + "����;";
		
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
		double tem;   //�����
		tem = mSecondMarkDownPosY - mFirstMarkDownPosY;
		tem = tem*coeffY;
		int temTime;
		temTime = (int) (tem*10);
		tem = temTime/10.;
		
		//�������
		double deep;
		deep = mBackplayFileHeader.getDeep();
		double coeffDeep;
		coeffDeep = deep/getHeight();
		deep = (mSecondMarkDownPosY-mFirstMarkDownPosY)*coeffDeep;
		int temDeep;
		temDeep = (int) (deep*1000);
		deep = temDeep/1000.;
		
		msgTxt += "��ֱ����:"+tem+"ns"+" "+deep+"��";
		//
//		txtView.setText(msgTxt);
	}
	//
	public int getBackplaySpeed()
	{
		return mBackplaySpeed;
	}
}

