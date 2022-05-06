package com.ltd.lifesearch_xa;

import java.io.FileOutputStream;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class AnimationView extends View
{

	private static final String TAG = "AnimationView";
	private static final int LEFTSPACE = 4;
	private static final int RIGHTSPACE = 4;
	private static final int TOPSPACE = 4;
	private static final int BOTTOMSPACE = 4;
	private static final int TITLEHEIGHT = 50;
	private static final int TOPOFFSET = 220;
	private static final int BOTTOMOFFSET = 30;

	private AnimationThread mAnimationThread;
	private Paint outline_paint = new Paint();
	private int mBitmapIndex = 0;
	private boolean mBackFlag = true;
	private boolean mIsStart = false;
	private int mTimeCnt = 0;
	private long mStartTime = 0;
	private Context mContext = null;
	private static boolean mShowInterDetect = false; // 显示中间结果

	private boolean mShotFlag = false;

	private Bitmap[] mBitmapsArray = new Bitmap[12];
	private Bitmap mBitmapTarget;
	private Bitmap mBitmapTargetMove;
	private Bitmap mBitmapMidTarget;
	private Bitmap mBitmapConnect;
	private Bitmap mBitmapDisConnect;

	// 网格相关
	private short mGridNum = Global.DEFAULT_DETECTRANGE / 300;
	private Paint[] mPaints;
	private float mDegree = 52;
	private float mRadiusInter = 49;
	private PointF[] mPointFs = new PointF[4];
	private PointF[][] mGridScalePos;
	private PointF[] mGridResultPos;
	
	//记录上一个最终目标的距离
	private String mStrMoveDis = "";
	private String mStrBreathDis = "";
	
	private Ringtone mRingtone;
	private Rect mWifiRect = new Rect();

	public AnimationView(Context context)
	{
		super(context);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public AnimationView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public AnimationView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context);
	}

	private void init(Context context)
	{
		Resources res = context.getResources();

		mBitmapsArray[0] = BitmapFactory.decodeResource(res, R.drawable.pic0);
		mBitmapsArray[1] = BitmapFactory.decodeResource(res, R.drawable.pic1);
		mBitmapsArray[2] = BitmapFactory.decodeResource(res, R.drawable.pic2);
		mBitmapsArray[3] = BitmapFactory.decodeResource(res, R.drawable.pic3);
		mBitmapsArray[4] = BitmapFactory.decodeResource(res, R.drawable.pic4);
		mBitmapsArray[5] = BitmapFactory.decodeResource(res, R.drawable.pic5);
		mBitmapsArray[6] = BitmapFactory.decodeResource(res, R.drawable.pic6);
		mBitmapsArray[7] = BitmapFactory.decodeResource(res, R.drawable.pic7);
		mBitmapsArray[8] = BitmapFactory.decodeResource(res, R.drawable.pic8);
		mBitmapsArray[9] = BitmapFactory.decodeResource(res, R.drawable.pic9);
		mBitmapsArray[10] = BitmapFactory.decodeResource(res, R.drawable.pic10);
		mBitmapsArray[11] = BitmapFactory.decodeResource(res, R.drawable.pic11);

		mBitmapTarget = BitmapFactory.decodeResource(res, R.drawable.target);
//		mBitmapTargetMove = BitmapFactory.decodeResource(res, R.drawable.move);
		mBitmapMidTarget = BitmapFactory.decodeResource(res, R.drawable.icointer);

		mBitmapConnect = BitmapFactory.decodeResource(res, R.drawable.connect);
		mBitmapDisConnect = BitmapFactory.decodeResource(res,
				R.drawable.disconnect);

		outline_paint.setColor(Color.BLACK);
		outline_paint.setStrokeWidth(1.5f);
		mContext = context;
		
		for (int i = 0; i < mPointFs.length; i++)
		{
			mPointFs[i] = new PointF();
		}

		mPaints = new Paint[4];

		mPaints[0] = new Paint();
		mPaints[0].setAntiAlias(true);
		// mPaints[0].setColor(0x88FF0000);
		mPaints[0].setColor(0x88E8E8E7);

		mPaints[1] = new Paint(mPaints[0]);
		mPaints[1].setColor(Color.rgb(88, 90, 87));

		mPaints[2] = new Paint(mPaints[0]);
		mPaints[2].setStyle(Paint.Style.STROKE);
		mPaints[2].setStrokeWidth(3.5f);
		mPaints[2].setColor(Color.rgb(177, 179, 181));
//		mPaints[2].setColor(getResources().getColor(R.color.blue));

		mPaints[3] = new Paint();
		mPaints[3].setColor(Color.rgb(90, 221, 69));
		mPaints[3].setStrokeWidth(1);

		mGridScalePos = new PointF[2][];
		mGridResultPos = new PointF[mGridNum];
		for (int i = 0; i < 2; i++)
		{
			mGridScalePos[i] = new PointF[mGridNum];
			for (int j = 0; j < mGridNum; j++)
			{
				mGridScalePos[i][j] = new PointF();
				mGridResultPos[j] = new PointF();
			}
		}
		
		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		mRingtone = RingtoneManager.getRingtone(mContext, alert);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		// TODO Auto-generated method stub
		super.onDraw(canvas);

		if (Global.GRIDVIEW)
		{
			//左上
			mPointFs[0].x = TOPOFFSET;
			mPointFs[0].y = TOPSPACE + TITLEHEIGHT;
			//右上
			mPointFs[1].x = getWidth() - TOPOFFSET;
			mPointFs[1].y = TOPSPACE + TITLEHEIGHT;
			//左下
			mPointFs[2].x = BOTTOMOFFSET;
			mPointFs[2].y = getHeight() - 20;
			//右下
			mPointFs[3].x = getWidth() - BOTTOMOFFSET;
			mPointFs[3].y = getHeight() - 20;
			
			mGridNum = Global.DEFAULT_DETECTRANGE / 300;
			mRadiusInter = (mPointFs[3].y - mPointFs[0].y) / mGridNum;	
			mDegree = (float)(Math.atan(mRadiusInter * mGridNum / (mPointFs[0].x - mPointFs[2].x)));
			drawGrid(canvas);
		}
		else
		{
			drawBackBMP(canvas);
		}

		drawDetectResult(canvas);
//		if (!Global.GRIDVIEW)
//		{
			drawIcon(canvas);
//		}

		if (mShotFlag)
		{

			mShotFlag = false;
		}
	}

	private void drawBackBMP(Canvas canvas)
	{
		Bitmap bmp = mBitmapsArray[0];

		int bmpHeight = bmp.getHeight();
		int bmpWidth = bmp.getWidth();
		int viewHeight = getHeight();
		int viewWidth = getWidth();

		// ((LifeSearchActivity)mContext).showToast(String.valueOf(bmp.getHeight()));
		float coeffH; // 底图画到视图时的缩放系数
		coeffH = (float) viewHeight / bmpHeight;
		float coeffW = (float) viewWidth / bmpWidth;

		boolean isDetecting = ((LifeSearchActivity) mContext).mRadarDevice
				.isDeting();
		if (!isDetecting || !mIsStart)
		{
			bmp = mBitmapsArray[0];
		}
		else
		{
			if (mBackFlag)
			{
				bmp = mBitmapsArray[0];
			}
			else
			{
				bmp = mBitmapsArray[mBitmapIndex];
			}

		}

		Rect srcR, dstR;
		srcR = new Rect(0, 0, 0, 0);
		dstR = new Rect(0, 0, 0, 0);

		srcR.left = 0;
		srcR.top = 0;
		srcR.right = bmp.getWidth();
		srcR.bottom = bmp.getHeight();

		dstR.left = 0;
		dstR.top = 0;
		dstR.right = getWidth();
		dstR.bottom = getHeight();

		canvas.drawBitmap(bmp, srcR, dstR, null);

		if (isDetecting)
		{
			mBackFlag = !mBackFlag;
		}

		// Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		// p.setStyle(Paint.Style.STROKE);
		// p.setStrokeWidth(2);
		// RectF oval1 = new RectF(150, 20, 280, 80);
		// canvas.drawArc(oval1, 0, 180, false, p);

		if (((LifeSearchActivity) mContext).mRadarDevice.isDeting() && mIsStart)
		{
			String str = "Time:"	+ ((LifeSearchActivity) mContext).mUseTime / 2	+ "S";

			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setTextSize(48);
			paint.setColor(Color.GREEN);

			Typeface typeface = Typeface.create("宋体", Typeface.DEFAULT.BOLD);
			paint.setTypeface(typeface);

			int xPos1 = 4;
			int yPos1 = (int) (10 * coeffH);
			canvas.drawText(str, xPos1, yPos1, paint);
		}

		String scaleTxt;
		for (int i = 0; i < 11; i++)
		{
			scaleTxt = String.valueOf((i + 1) * 3) + "米";

			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setTextSize(40);
			paint.setColor(getResources().getColor(R.color.blue));

			Typeface typeface = Typeface.create("宋体", Typeface.DEFAULT.BOLD);
			paint.setTypeface(typeface);

			int xPos1 = (int) ((Global.mScalePos[i][0]) * coeffW) - 6;
			int yPos1 = (int) (Global.mScalePos[i][1] * coeffH) + 3;
			canvas.drawText(scaleTxt, xPos1, yPos1, paint);
		}
	}
	
	private void drawOneResult(Canvas canvas, Bitmap targetBMP, short distance, Paint paint)
	{
		// //根据bmp背景图计算缩放系数
		int topPix = Global.DETECTRANGE_BEGINPIX; // 36;
		short range = ((LifeSearchActivity) mContext).mRadarDevice
				.getDetectRange();
		Bitmap bmp = mBitmapsArray[0];

		int bmpHeight = bmp.getHeight();
		int bmpWidth = bmp.getWidth();
		int viewHeight = getHeight();
		int viewWidth = getWidth();
		
		float xPos1, yPos1; // 呼吸和体动的图标显示坐标
		String disStr = "";
		float coeffH; // 底图画到视图时的缩放系数
		coeffH = (float) viewHeight / bmpHeight;

		float coeffW = (float) viewWidth / bmpWidth;

		//
		int xOrg;
		
		if (Global.GRIDVIEW)
		{
			xOrg = viewWidth / 2;
		}
		else 
		{
			xOrg = (int) (bmpWidth / 2 * coeffW);
		}
		
		int yOrg = (int) (topPix * coeffH); // 36:为图上从探测范围开始的位图坐标，0~36是位图的天线部分

		float bmpTargetWidth = targetBMP.getWidth() / 0.4f;
		float bmpTargetHeight = targetBMP.getHeight() / 0.4f;
		// 确定目标在屏幕上的坐标
		xPos1 = xOrg - bmpTargetWidth / 2; // x坐标

		int index1;
		float lDis;
		// 确定本扫描段的显示位置
		index1 = distance / 300;
		if (index1 >= 11)
			index1 = 10;
		if (Global.GRIDVIEW)
		{
			lDis = mGridResultPos[index1].y - mGridResultPos[index1].x; // 扫描段在位图中的坐标高度
			yPos1 = mGridResultPos[index1].x + (distance - index1 * 300) * lDis / 300;
			yPos1 -= bmpTargetHeight / 2;
		}
		else
		{
			lDis = Global.mResultPos[index1][1] - Global.mResultPos[index1][0]; // 扫描段在位图中的坐标高度
			yPos1 = Global.mResultPos[index1][0] + (distance - index1 * 300) * lDis / 300;
			yPos1 = (int) (yPos1 * coeffH);
			yPos1 -= bmpTargetHeight / 2;
		}

		Rect srcR, dstR;
		srcR = new Rect(0, 0, 0, 0);
		dstR = new Rect(0, 0, 0, 0);

		srcR.left = 0;
		srcR.top = 0;
		srcR.right = targetBMP.getWidth();
		srcR.bottom = targetBMP.getHeight();

		dstR.left = (int)xPos1;
		dstR.top = (int)yPos1;
		dstR.right = (int)(dstR.left + bmpTargetWidth);
		dstR.bottom = (int)(dstR.top + bmpTargetHeight);
		// 粘贴目标位图
		canvas.drawBitmap(targetBMP, srcR, dstR, null);

		String msg = String.valueOf(xPos1) + "---" + String.valueOf(yPos1);
		// Log.e(TAG, msg);

		// /显示距离信息
		disStr = distance + "cm";

		xPos1 += bmpTargetWidth;
		yPos1 += bmpTargetHeight / 2 + 9;
		canvas.drawText(disStr, xPos1, yPos1, paint);
	}

	private int drawDetectResult(Canvas canvas)
	{

		RadarDevice radarDevice = ((LifeSearchActivity) mContext).mRadarDevice;
		DevResult detectResult = ((LifeSearchActivity) mContext).mDetectResult;
		
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setTextSize(20);
		paint.setColor(Color.RED);

		Typeface typeface = Typeface.create("宋体", Typeface.DEFAULT.BOLD);
		paint.setTypeface(typeface);
		
		short distance = 0;	
		int i;
		
		// ////画出保存的结果
		int targetNumber = ((LifeSearchActivity) mContext).mTargetNumber;
		// Log.e(TAG, "mTargetNumber = " + String.valueOf(targetNumber));
		RESULT[] targetInfo = ((LifeSearchActivity) mContext).mTargetInfo;
		
		for (i = 0; i < targetNumber; i++)
		{
			if ((targetInfo[i].mExistBreath == 0)
					&& (targetInfo[i].mExistMove == 0))
			{
				continue;
			}
			//
			if (targetInfo[i].mExistBreath != 0)
			{
				distance = targetInfo[i].mBreathPos;
				drawOneResult(canvas, mBitmapTarget, distance, paint);
			}
			if (targetInfo[i].mExistMove != 0)
			{
				distance = targetInfo[i].mMovePos;
				drawOneResult(canvas, mBitmapTargetMove, distance, paint);
			}
		}
		 ////////画出正在探测的结果
		 boolean existBreathTarget = false;
		 boolean existMoveTarget = false;
		 short disMove = 0;
		 String disStr;
		 if(radarDevice.isBackShowing())
		 {
//			 distance = radarDevice.mBackShowTargetPos;
//			 existTarget = radarDevice.mExistBackShowTarget;
		 }
		 else
		 {
			 int distanceOff = ((LifeSearchActivity)mContext).mDistanceOff;
			 for(i=0; i<detectResult.mTargetNum; i++)
			 {
				 if((detectResult.mResult[i].mExistBreath == 0) && (detectResult.mResult[i].mExistMove == 0))
				 {
					 continue;
				 }
				 if(detectResult.mResult[i].mExistBreath != 0)
				 {
					 distance = detectResult.mResult[i].mBreathPos;
//					 Log.e(TAG, "result breath dis = " + distance);
					 existBreathTarget = true;
				 }
				 if(detectResult.mResult[i].mExistMove != 0)
				 {
					 disMove = detectResult.mResult[i].mMovePos;
					 existMoveTarget = true;
					 Log.e(TAG, "result move dis = " + distance);
				 }
				 distance += distanceOff;
				 //写入探测结果文件
//				 disStr = String.valueOf(distance + distanceOff) + "厘米";
			 }
		 }
		 if(existBreathTarget)
		 {
			 drawOneResult(canvas, mBitmapTarget, distance, paint);
			
			 /////显示距离信息
			 disStr = String.valueOf(distance) + "cm";
			 ((LifeSearchActivity)mContext).mDetectResultText.setText(disStr);
			 mStrBreathDis = disStr;
			 if(((LifeSearchActivity)mContext).mCanBeep)
			 {
				 if (((LifeSearchActivity)mContext).mBeepNumber == 0)
				 {
					 	mRingtone.play();
				 }
			 }
		 }
		 
		 if (mRingtone.isPlaying())
		 {
			//蜂鸣
			((LifeSearchActivity)mContext).mBeepNumber++;
			if(((LifeSearchActivity)mContext).mBeepNumber >= 12)
			{
				mRingtone.stop();
				((LifeSearchActivity)mContext).mBeepNumber = 0;
				((LifeSearchActivity)mContext).mCanBeep = false;
				Log.e(TAG, "stop beep!");
			}
		 }
		 
		 if(existMoveTarget)
		 {
			 drawOneResult(canvas, mBitmapTargetMove, disMove, paint);
			
			 /////显示距离信息
			 disStr = String.valueOf(disMove);
//			 ((LifeSearchActivity)mContext).mMoveResultText.setText(disStr);
			 mStrMoveDis = disStr;
			 if(((LifeSearchActivity)mContext).mCanBeep)
			 {
				 //蜂鸣
				 ((LifeSearchActivity)mContext).mBeepNumber++;
				 if(((LifeSearchActivity)mContext).mBeepNumber >= 5)
				 {
					 ((LifeSearchActivity)mContext).mBeepNumber = 0;
					 ((LifeSearchActivity)mContext).mCanBeep = false;
				 }
			 }
		 }
		 
//		 else
//		 {
//			 if(radarDevice.isBackShowing())
//				 disStr = "未发现目标";
//		 }
		 ////

		// ////显示中间结果
		 paint.setColor(Color.RED);
		if (detectResult.mMiddleTargetNum != 0)
		{
			for (int j = 0; j < 1/*detectResult.mMiddleTargetNum*/; j++)
			{
				if ((detectResult.mResult[10 + j].mExistBreath == 0) && (detectResult.mResult[10 + j].mExistMove == 0))
				{
					continue;
				}
				if (detectResult.mResult[10 + j].mExistBreath != 0)
				{
					distance = detectResult.mResult[10 + j].mBreathPos;
					if (distance > 0)
					{
						existBreathTarget = true;
						break;
					}
				}
				if (detectResult.mResult[10 + j].mExistMove != 0)
				{
					disMove = detectResult.mResult[10 + j].mMovePos;
					if (disMove > 0)
					{
						existMoveTarget = true;
						break;
					}
				}
			}
			
	
			if(existBreathTarget && mShowInterDetect)
			{
				drawOneResult(canvas, mBitmapMidTarget, distance, paint);
				
				/////显示距离信息
				disStr = String.valueOf(distance) + "cm";
				((LifeSearchActivity)mContext).mDetectResultText.setText(disStr);
			}
			 
			if(existMoveTarget && mShowInterDetect)
			{
				drawOneResult(canvas, mBitmapMidTarget, disMove, paint);
				
				/////显示距离信息
				disStr = String.valueOf(disMove);
//				((LifeSearchActivity)mContext).mMoveResultText.setText(disStr);
			}
			
			//中间结果的编辑框也闪烁
			if (existBreathTarget || existMoveTarget)
			{
				if (!mShowInterDetect)
				{
//					((LifeSearchActivity)mContext).mDetectResultText.setText("");
//					((LifeSearchActivity)mContext).mMoveResultText.setText("");
//					((LifeSearchActivity)mContext).showToast("--" + existBreathTarget + "--" + existMoveTarget);
				}
				
				mShowInterDetect = !mShowInterDetect;
			}
			else 
			{
//				((LifeSearchActivity)mContext).mDetectResultText.setText(mStrBreathDis);
//				((LifeSearchActivity)mContext).mMoveResultText.setText(mStrMoveDis);
			}
		}
		else 
		{
//			((LifeSearchActivity)mContext).mDetectResultText.setText(mStrBreathDis);
//			((LifeSearchActivity)mContext).mMoveResultText.setText(mStrMoveDis);
		}

		return 0;
	}

	private void drawIcon(Canvas canvas)
	{
		Bitmap bmp;
		if (((LifeSearchActivity) mContext).mWifiDevice.isHadConnect())
		{
			bmp = mBitmapConnect;
		}
		else
		{

			bmp = mBitmapDisConnect;
		}

		int xPos1 = getWidth() - 4 * bmp.getWidth();
		int yPos1 = 20;

		Rect srcR, dstR;
		srcR = new Rect(0, 0, 0, 0);
		dstR = new Rect(0, 0, 0, 0);

		srcR.left = 0;
		srcR.top = 0;
		srcR.right = bmp.getWidth();
		srcR.bottom = bmp.getHeight();

		dstR.left = xPos1;
		dstR.top = yPos1;
		dstR.right = dstR.left + 3 * bmp.getWidth();
		dstR.bottom = dstR.top + 3 * bmp.getHeight();
		
		mWifiRect = dstR;

		canvas.drawBitmap(bmp, srcR, dstR, null);
	}

	class AnimationThread extends Thread
	{

		public AnimationThread(AnimationView animationView)
		{
			// TODO Auto-generated constructor stub
		}

		@Override
		public void run()
		{
			// TODO Auto-generated method stub
			while (mIsStart)
			{
				try
				{
					Thread.sleep(2000);
					Message msg = new Message();
					mAnimationThreadHandler.sendMessage(msg);
					// Log.e(TAG, "Thread run!");
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

	}

	public Handler mAnimationThreadHandler = new Handler()
	{

		@Override
		public void handleMessage(Message msg)
		{
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			// mTimeCnt++;
			//
			// if (mTimeCnt > 25) {
			// mTimeCnt = 0;
			// mBitmapIndex++;
			//
			// if (mBitmapIndex > 8) {
			// mBitmapIndex = 1;
			// }

			// setBitmapIndex(mBitmapIndex);
			// }

			// invalidate();
		}

	};

	public void startAnimation()
	{
		// mAnimationThread = new AnimationThread(this);
		// mAnimationThread.start();
		mIsStart = true;
		mBackFlag = true;
		mTimeCnt = 0;
		mBitmapIndex = 1;
		mStrBreathDis = "";
		mStrMoveDis = "";
		
		if (mRingtone.isPlaying())
		{
			mRingtone.stop();
		}
	}

	public void stopAnimation()
	{
		mIsStart = false;
		invalidate();
		
		if (mRingtone.isPlaying())
		{
			mRingtone.stop();
		}
		
		// if (mAnimationThread != null) {
		// while (mAnimationThread.isAlive()) {
		// }
		// mAnimationThread = null;
		// }
	}

	public void setBitmapIndex(int index)
	{
		mBitmapIndex = index;
	}

	private void shotView(String fileName)
	{
		Bitmap bitmap;

		setDrawingCacheEnabled(true);
		buildDrawingCache();
		bitmap = getDrawingCache();

		// bitmap = Bitmap.createBitmap(getWidth(), getHeight(),
		// Config.ARGB_8888);
		// Canvas canvas = new Canvas();
		// canvas.setBitmap(bitmap);
		// draw(canvas);

		if (bitmap != null)
		{
			try
			{
				FileOutputStream out = new FileOutputStream(fileName + ".png");
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
		}
	}

	public void setShotFlag()
	{
		mShotFlag = true;
	}

	private void drawGrid(Canvas canvas)
	{
		float xOrg1, yOrg1, xOrg2, yOrg2, width, height;

		width = getWidth();
		height = getHeight();
	
		canvas.drawColor(0xE8E8E7);
		
		RectF rect = new RectF();
		rect.left = mPointFs[0].x - 2;
		rect.top = TOPSPACE + 2;
		rect.right = mPointFs[1].x + 2;
		rect.bottom = mPointFs[1].y;
		canvas.drawRoundRect(rect, 10, 10, mPaints[1]);
		rect.top += 15;
		canvas.drawRect(rect, mPaints[1]);
		
		rect.top -= 15;
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setTextSize(30);
		paint.setColor(Color.rgb(9, 216, 41));

		Typeface typeface = Typeface.create("宋体", Typeface.DEFAULT.BOLD);
		paint.setTypeface(typeface);
		paint.setTextAlign(Align.CENTER);
		canvas.drawText("LTD-90B", rect.left + rect.width() / 2, rect.top + rect.height() / 2 + 12, paint);

		// draw outline
		canvas.drawLine(LEFTSPACE, TOPSPACE, width - RIGHTSPACE, TOPSPACE,
				outline_paint); // top
		canvas.drawLine(width - RIGHTSPACE, TOPSPACE, width - RIGHTSPACE,
				height - BOTTOMSPACE, outline_paint); // right
		canvas.drawLine(LEFTSPACE, height - BOTTOMSPACE, width - RIGHTSPACE,
				height - BOTTOMSPACE, outline_paint); // bottom
		canvas.drawLine(LEFTSPACE, TOPSPACE, LEFTSPACE, height - BOTTOMSPACE,
				outline_paint); // left
		
		xOrg1 = mPointFs[0].x;
		yOrg1 = mPointFs[0].y;
		xOrg2 = mPointFs[1].x;
		yOrg2 = mPointFs[1].y;

//		RectF[] rectF = new RectF[mGridNum];
		int i = 0;
		for (i = 0; i < mGridNum; i++)
		{
			float halfRectW = mRadiusInter * (i + 1);
//			rectF[i] = new RectF(xOrg - halfRectW, yOrg - halfRectW, xOrg + halfRectW, yOrg + halfRectW);
			mGridScalePos[0][i].x = (float) (xOrg1 - halfRectW * Math.tan(Math.PI / 2 - mDegree));
			mGridScalePos[0][i].y = (float) (halfRectW + yOrg1);

			mGridScalePos[1][i].x = (float) (xOrg2 + halfRectW * Math.tan(Math.PI / 2 - mDegree));
			mGridScalePos[1][i].y = (float) (halfRectW + yOrg2);

			//x为网格上面弧线的坐标，y为下面弧线的坐标
			mGridResultPos[i].x = mRadiusInter * i + yOrg1;
			mGridResultPos[i].y = halfRectW + yOrg1;
		}
		for (i = 0; i < mGridNum; i++)
		{
//			canvas.drawArc(rectF[i], (180 - mDegree) / 2, mDegree, false, mPaints[2]);
			canvas.drawLine(mGridScalePos[0][i].x, mGridScalePos[0][i].y, mGridScalePos[1][i].x, mGridScalePos[1][i].y, mPaints[2]);
		}
		canvas.drawLine(xOrg1, yOrg1, xOrg2, yOrg2, mPaints[2]);

		float startX, startY, stopX, stopY;
		stopX = mGridScalePos[0][mGridNum - 1].x;
		stopY = mGridScalePos[0][mGridNum - 1].y;
		canvas.drawLine(mPointFs[0].x, mPointFs[0].y, stopX, stopY, mPaints[2]); // left
		stopX = mGridScalePos[1][mGridNum - 1].x;
		stopY = mGridScalePos[1][mGridNum - 1].y;
		canvas.drawLine(mPointFs[1].x, mPointFs[1].y, stopX, stopY, mPaints[2]); // right

		String scaleTxt;
		for (i = 0; i < mGridNum; i++)
		{
			scaleTxt = String.valueOf((i + 1) * 3);

			paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setTextSize(20);
//			paint.setColor(0xFF6EB7F8);
			paint.setColor(Color.rgb(0, 0, 255));

			typeface = Typeface.create("宋体", Typeface.DEFAULT.BOLD);
			// paint.setTextAlign(Align.CENTER);
			paint.setTypeface(typeface);
			float textWidth = paint.measureText(scaleTxt);

			float xPos1 = (int) (mGridScalePos[0][i].x) - textWidth - 1;
			float yPos1 = (int) (mGridScalePos[0][i].y) + 3;
			canvas.drawText(scaleTxt, xPos1, yPos1, paint);
			xPos1 = (int) (mGridScalePos[1][i].x) + 2;
			yPos1 = (int) (mGridScalePos[1][i].y) + 3;
			canvas.drawText(scaleTxt, xPos1, yPos1, paint);
		}
		
		//闪烁
		boolean isDetecting = ((LifeSearchActivity) mContext).mRadarDevice.isDeting();
		
		Log.d("debug_flags", "isdetecting: " + isDetecting + ", isstart: " + mIsStart);
		
		if (isDetecting && mIsStart)
		{
			if (!mBackFlag)
			{
//				RectF flashRectF = new RectF();
//				flashRectF.left = xOrg - (mRadiusInter * (mBitmapIndex)  + mRadiusInter / 2);
//				flashRectF.top = yOrg - (mRadiusInter * (mBitmapIndex) + mRadiusInter / 2);
//				flashRectF.right = xOrg + (mRadiusInter * (mBitmapIndex) + mRadiusInter / 2);
//				flashRectF.bottom = yOrg + (mRadiusInter * (mBitmapIndex) + mRadiusInter / 2);
//				
//				canvas.drawArc(flashRectF, (180 - mDegree) / 2, mDegree, false,	mPaints[3]);
				
				if (mBitmapIndex > mGridNum + 1)
				{
					mBitmapIndex = mGridNum + 1;
				}
				for (int j = 0; j < mRadiusInter - 2; j++)
				{
					startX = (float)(mGridScalePos[0][mBitmapIndex - 1].x + (j) * Math.tan(Math.PI / 2 - mDegree)) + 2;
					startY = mGridScalePos[0][mBitmapIndex - 1].y - j - 2;
					stopX = (float)(mGridScalePos[1][mBitmapIndex - 1].x - (j) * Math.tan(Math.PI / 2 - mDegree)) - 2;
					stopY = mGridScalePos[1][mBitmapIndex - 1].y - j - 2;
					canvas.drawLine(startX, startY, stopX, stopY, mPaints[3]);
				}
			}
		}

		if (isDetecting)
		{
			mBackFlag = !mBackFlag;
		}
		
		if (((LifeSearchActivity) mContext).mRadarDevice.isDeting() && mIsStart)
		{
			String str = "Time:"	+ ((LifeSearchActivity) mContext).mUseTime / 2	+ "S";

			paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setTextSize(25);
			paint.setColor(Color.rgb(9, 216, 41));
			
			if(((LifeSearchActivity)mContext).mControlMode && (((LifeSearchActivity)mContext).mBlueRcvBuf.mTargetType == Global.BLUE_TARGET_SCAN))
			{
				paint.setColor(Color.rgb(0, 0, 255));
			}

			typeface = Typeface.create("宋体", Typeface.DEFAULT.BOLD);
			paint.setTypeface(typeface);

			int xPos1 = 30;
			int yPos1 = 50;
			canvas.drawText(str, xPos1, yPos1, paint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		// TODO Auto-generated method stub
		float x = event.getX();
		float y = event.getY();
		int action = event.getAction();
		
		if (action == MotionEvent.ACTION_DOWN)
		{
			if (x > mWifiRect.left && x < mWifiRect.right && y > mWifiRect.top && y < mWifiRect.bottom)
			{
				Intent intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
				((LifeSearchActivity)mContext).startActivityForResult(intent, 0);
			}
		}
		return true;
//		return super.onTouchEvent(event);
	}
}
