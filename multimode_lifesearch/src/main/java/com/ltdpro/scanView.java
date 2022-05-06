package com.ltdpro;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

/*
 * 单道波形显示视图
 */
public class scanView extends SurfaceView implements Callback, Runnable
{
	private String TAG="scanView";
	
	private Paint mTextPaint = new Paint();      //字体paint
	private Paint mWavePaint = new Paint();      //单道波形的画笔
	private Paint mHardplusNormalPointPaint = new Paint();      //一般硬件增益点的画笔
	private Paint mHardplusSelectPointPaint = new Paint();      //选中硬件增益点的画笔
	private Paint mHardplusLinePaint = new Paint();             //硬件增益曲线的画笔
	private Paint mGridPaint = new Paint();                     //画网格需要的画笔
	private int   mSelHardplusIndex	= 1;                          //选中的硬件增益点

	private short[] mScanDatas=new short[8192];
	private float[] mOldHardPlus={0,0,0,0,0,0,0,0,0};
	
	//更改多线程
	SurfaceHolder mSurfaceHolder = null;
	private boolean mLoop = false;//循环控制

	public void initDatas(Context context)
	{
		//设置各个画笔的内容
		mWavePaint.setColor(Color.BLACK);
		mWavePaint.setStrokeWidth(2);
			
		mHardplusNormalPointPaint.setColor(Color.GREEN);
		mHardplusNormalPointPaint.setStrokeWidth(1);
		
		mHardplusSelectPointPaint.setColor(Color.RED);
		mHardplusSelectPointPaint.setStrokeWidth(1);
		//
		mHardplusLinePaint.setColor(Color.GREEN);
		mHardplusLinePaint.setStrokeWidth(2);
		//
		mGridPaint.setColor(Color.BLACK);
		mGridPaint.setStrokeWidth((float) 0.5);
		mGridPaint.setAntiAlias(true);
		mGridPaint.setStyle(Paint.Style.STROKE);
		
		//显示参数值的paint;
		mTextPaint.setColor(Color.GREEN);
		mTextPaint.setStrokeWidth(2);
		mTextPaint.setTextSize(20);
	}
	
	//构造函数
	public scanView(Context context, AttributeSet attrs)
	{		
		super(context,attrs);
		initDatas(context);
		DebugUtil.i(TAG, "enter scanView(Context context)");
		mSurfaceHolder = this.getHolder();
		mSurfaceHolder.addCallback(this);
		this.setFocusable(false);
		mLoop = true;
	}
     
    //绘图循环
    @Override
	public void run() 
    {
		while( mLoop )
		{
			try
			{
				Thread.sleep(200);
				
			}catch(Exception e)
			{
				DebugUtil.e(TAG, "run thread sleep error!");
			}
			
			synchronized(mSurfaceHolder)
			{	
				Draw();		
			}
		}		
	}

    //创建画图线程
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO 开启绘图线程
//		DebugUtil.i(TAG, "enter surfaceCreated!");
//		Canvas c = holder.lockCanvas();
//		c.drawColor(0xe1e1e1); 
		new Thread(this).start();
		mLoop = true;		
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
		// TODO Auto-generated method stub
		//		DebugUtil.i(TAG,"surfaceChanged!");		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		mLoop = false;
		//		DebugUtil.i(TAG,"surfaceDestroyed!");
	}

	//画波形
	public void drawWave(Canvas canvas)
	{
		//	DebugUtil.i(TAG, "enter drawWave!");
		int mScanLen;
		
		float xOrg,yOrg;
		float xPos,yPos;
		float perW;
		float perH;
		float befPosX,befPosY;

		float hRuler = 0;
		MyApplication app;
		app=(MyApplication)(getContext().getApplicationContext());
		mScanLen =app.mRadarDevice.getScanLength();
		mScanDatas = app.mRadarDevice.getRecentScanDatas();
		perH = getHeight()-hRuler;
		perH = perH/mScanLen;
		xOrg = befPosX = getWidth()/2;
		yOrg = befPosY = hRuler;
		if(getWidth() <= 0)
			perW = 0;
		else
			perW = 65534/getWidth();
		
		mScanDatas[0] = 0;
		mScanDatas[1] = 0;
		double temVal1;
		for(int i=0;i<mScanLen;i++)
    	{
			temVal1 = mScanDatas[i];
			if(temVal1>0x7fff)
				temVal1 = 0x7fff;
			if(temVal1<-0x7fff)
				temVal1 = -0x7fff;
			xPos = (float) (xOrg + temVal1/perW);
			yPos = yOrg + i*perH;
			canvas.drawLine(befPosX, befPosY, xPos, yPos, mWavePaint);
			
			befPosX = xPos;
			befPosY = yPos;
    	}
	}
	
	//画出硬件增益曲线
	private int hardPlusPointR=8;
	public void drawHardplus(Canvas canvas)
	{
//		DebugUtil.i(TAG, "drawHardplus!");
		MyApplication app;
		float xOrg,yOrg;
		float xPos,yPos;
		float perW;
		float perH;
		float befPosX,befPosY;

		app=(MyApplication)(getContext().getApplicationContext());
		float range = app.mRadarDevice.getHardplusRange();
		float mixVal,maxVal;
		mixVal = app.mRadarDevice.getMixHardplus();
		maxVal = app.mRadarDevice.getMaxHardplus();
		
		float hRuler = 0;
		perH = getHeight()-hRuler;
		perH = perH/8;
		xOrg = befPosX = 0;
		yOrg = befPosY = hRuler;
		perW = getWidth()/range;
		
		//
		float[] vals=app.mRadarDevice.getHardplus();
		
		int i;
		float val;
		String txt;
		for(i=0;i<9;i++)
		{
//			DebugUtil.i(TAG,"vals["+i+"]="+vals[i]);
			val=vals[i];
			xPos=xOrg+(val-(mixVal))*perW;
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
			//显示dB值
			txt = ""+((int)val)+"dB";
			canvas.drawText(txt, xPos+hardPlusPointR, yPos+hardPlusPointR, mTextPaint);
		}
	}
	
	//画出波形函数
	public void Draw()
	{
//		DebugUtil.i(TAG,"ScanView onDraw");
		Canvas canvas = mSurfaceHolder.lockCanvas();
			
		//long start = System.currentTimeMillis();

		if ( mSurfaceHolder == null || canvas == null )
		{
			return;
		}
		else;
		
		canvas.drawColor(0xffffffff);//设置背景色，在xml中设置也可
		drawGrid(canvas);
		drawHardplus(canvas);
		
		//判断是否暂停
		MyApplication app;
		app=(MyApplication)(getContext().getApplicationContext());
		
		drawWave(canvas);	
		
		//long end = System.currentTimeMillis();		
		mSurfaceHolder.unlockCanvasAndPost(canvas);	
		//		DebugUtil.i(TAG, "3.scanView time="+String.valueOf(end - start));
	}
	
	
	public int getNowSelectHardplusPoint()
	{
		return mSelHardplusIndex;
	}
	
	//画网格
	public void drawGrid(Canvas canvas)
	{
//		DebugUtil.i(TAG, "enter drawGrid!");
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
	
	//处理上键
	public void manageKeyUp()
	{
		mSelHardplusIndex -= 1;
		if( mSelHardplusIndex < 0 )
			mSelHardplusIndex = 0;
		else;
		//
//		invalidate();
	}
	
	//处理下键
	public void manageKeyDown()
	{
		mSelHardplusIndex += 1;
		if(mSelHardplusIndex>=8)
			mSelHardplusIndex = 8;
		//
//		invalidate();
	}

	public void manageKeyLeft(int add,boolean isSendCommand)
	{
		MyApplication app;
		app=(MyApplication)(getContext().getApplicationContext());
		float[] vals=app.mRadarDevice.getHardplus();
		float val=vals[mSelHardplusIndex];
		//
		val = val-add;
		//
		int mixVal = app.mRadarDevice.getMixHardplus();
		if(val<mixVal)
			val = mixVal;
		vals[mSelHardplusIndex] = val;
		//
		if(isSendCommand)
			app.mRadarDevice.setHardplus(vals);
		else
			app.mRadarDevice.setHardplusValusOnly(vals);
		//invalidate();
		//
		DebugUtil.i(TAG,"manageKeyRight,hardPluse:="+val);
	}
	
	public void manageKeyRight(int add,boolean isSendCommand)
	{
		MyApplication app;
		app=(MyApplication)(getContext().getApplicationContext());
		float[] vals=app.mRadarDevice.getHardplus();
		float val=vals[mSelHardplusIndex];
		//
		val = val+add;
		int maxVal = app.mRadarDevice.getMaxHardplus();
		if(val>maxVal)
			val = maxVal;
		vals[mSelHardplusIndex] = val;
		
		if(isSendCommand)
			app.mRadarDevice.setHardplus(vals);
		else
			app.mRadarDevice.setHardplusValusOnly(vals);
//		invalidate();
		DebugUtil.i(TAG,"manageKeyRight,hardPluse:="+val);
	}
}
