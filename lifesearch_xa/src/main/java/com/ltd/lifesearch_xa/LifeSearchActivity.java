package com.ltd.lifesearch_xa;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;



public class LifeSearchActivity extends AppCompatActivity implements OnClickListener {
    public WiFiNetDevice mWifiDevice;
    public RadarDevice mRadarDevice;
    private LifeSearchApplication mApp;
    private final static String TAG = "LifeSearchActivity";
    private final static int NET_CHECK_TIMERID = 1;
    private final static int DETECT_TIMERID = 2;

    private Button mBeginDetectBtn = null;
    private Button mStopDetectBtn = null;
    private Button mBrowseResultBtn = null;
    private TextView mRadarStatusText = null;
    public TextView mDetectResultText = null;
    private AnimationView mAnimationView = null;
    public boolean mCanBeep = false;
    private int mDetectNumber = 0;
    private int mNowTargetIndex = 0;
    public int mTargetNumber = 0;
    public RESULT[] mTargetInfo = new RESULT[100];
    public boolean mControlMode = false;
    private short mSetTargetPos = 0; // 设定距离
    private static int mOffDistance = 36;
    private boolean mCanFlash = false;
    public int mSigPosOff = 0;
    private boolean mHasBegDetect = true;
    public int mBeepNumber = 0;
    static int mFirstRangeID = 0;
    public long mUseTime = 0;
    public int mDistanceOff = 0;
    private FileOutputStream mOutputStream = null;
    private int mHasWriteToFileDetectRsu = 0;
    private boolean mIsStart = false;
    private boolean mAlertFlag = false;
    private String mDetectTimeString = null;
    public String mResultFileName = null;
    private boolean mExitThread = false;

    public DevResult mDetectResult = new DevResult();
    public BLUERCV mBlueRcvBuf = new BLUERCV();

    private boolean mDetectMode = true;
    public realTimeDIBView mDibView = null;
    private View mDetectView = null;
    private MyTimer mTimer = null;
    private boolean mLastBeginFlag = false; // 进入最后一段的标志
    private boolean mDetectEnd = false; // 一次探测过程真正结束标志
//	// 添加读写权限

    private final int REQUEST_EXTERNAL_STORAGE = 1;
    private String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    //动态获取权限
    public void getPermission() {
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        // setContentView(R.layout.view_detect/* new AnimationView(this) */);

        LayoutInflater inflater = LayoutInflater.from(this);
        mDetectView = inflater.inflate(R.layout.view_detect, null);
        setContentView(mDetectView);
        // mLayout.addView(detectview);

        mWifiDevice = new WiFiNetDevice(this);
        mRadarDevice = new RadarDevice();
        mRadarDevice.setContext(this);

        for (int i = 0; i < 100; i++) {
            mTargetInfo[i] = new RESULT();
        }

        mBeginDetectBtn = (Button) findViewById(R.id.button_begindetect);
        mBrowseResultBtn = (Button) findViewById(R.id.button_sendresult);
        mAnimationView = (AnimationView) findViewById(R.id.view_detect);
        mRadarStatusText = (TextView) findViewById(R.id.edit_radarstatus);
        mDetectResultText = (TextView) findViewById(R.id.edit_radarresult);

        mBeginDetectBtn.setOnClickListener(this);
        mBrowseResultBtn.setOnClickListener(this);
        mRadarStatusText.setTextColor(Color.BLUE);
        mDetectResultText.setTextColor(Color.BLUE);

        // mBeginDetectBtn.setEnabled(false);
        // mSendResultBtn.setEnabled(false);

        mApp = (LifeSearchApplication) getApplicationContext();
        mWifiDevice.connectToHost();
        // mWifiDevice.setAttachActivity(this);
        // mWifiDevice.createSvrSocket();
        mDibView = new realTimeDIBView(this);

        mExitThread = false;
        mTimer = new MyTimer(this, mTimerHandler);
        mTimer.setTimer(NET_CHECK_TIMERID, 1000);

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels; // 屏幕宽度（像素）
        int height = metric.heightPixels; // 屏幕高度（像素）
        float density = metric.density; // 屏幕密度（0.75 / 1.0 / 1.5）
        int densityDpi = metric.densityDpi; // 屏幕密度DPI（120 / 160 / 240）

        // showToast(String.valueOf(width) + "---" + String.valueOf(height) +
        // "---" + String.valueOf(densityDpi));

        // Intent intent = new Intent();
        // intent.setClass(this, DebugActivity.class);
        // startActivity(intent);
        // finish();

        // //测试用例
        // mDetectResult.mTargetNum = 1;
        // mDetectResult.mResult[0].mExistBreath=1;
        // mDetectResult.mResult[0].mBreathPos=700;
        // //
        // mDetectResult.mMiddleTargetNum=1;
        // mDetectResult.mResult[10].mExistBreath = 1;
        // mDetectResult.mResult[10].mBreathPos = 1200;
        // //
        // mTargetNumber = 4;
        // for(int i=0; i<mTargetNumber; i++)
        // {
        // mTargetInfo[i].mExistBreath = 1;
        // mTargetInfo[i].mBreathPos = (short)(500 + 100 * i);
        // }
        // mCanBeep = true;

        getStoragePath(this, "EXT");
        getPermission();
    }

    public void showToast(CharSequence msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        mWifiDevice.stopThreads();
        mWifiDevice.closesvrRcvSockets();
        mWifiDevice.closeSvrSndSockets();

        mTimer.killTimer(NET_CHECK_TIMERID);
        mTimer.killAllTimers();

        mExitThread = true;
        super.onDestroy();
        finish();
        // System.exit(0);
    }

    public Handler mTimerHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);

            switch (msg.what) {
                case NET_CHECK_TIMERID:
                    mWifiDevice.addNetcheckTransfer();
                    updateNetStatus();
                    // if (mWifiDevice.isHadConnect())
                    // {
                    // mBeginDetectBtn.setEnabled(true);
                    // // mSendResultBtn.setEnabled(true);
                    // }
                    // else
                    // {
                    // mBeginDetectBtn.setEnabled(false);
                    // // mSendResultBtn.setEnabled(false);
                    // }

                    break;

                case DETECT_TIMERID:
                    if (mRadarDevice.isDeting()) {
                        mUseTime++;

                        // ///超过90秒没有收到结果
                        if (mUseTime >= 180) {
                            if (!mAlertFlag) {
                                showAlertDialog();
                                mAlertFlag = true;
                            }
                        }
                    }

                    mAnimationView.setBitmapIndex(getBitmapIndex());
                    mAnimationView.invalidate();
                    // Log.e(TAG, "Bitmap Index = " +
                    // String.valueOf(getBitmapIndex()));
                    break;

                default:
                    break;
            }
        }

    };
    private boolean mWifiConnected = false;

    private void updateNetStatus() {
        if (mWifiDevice.isHadConnect()) {
            if (!mWifiConnected) {
                showToast("Connected to radar equipment!");
                mWifiConnected = true;
                mAnimationView.invalidate();
            }

            return;
        } else {
            if (mWifiConnected) {
                showToast("The network connection with the radar device is disconnected, please check the network connection!");
                mWifiConnected = false;
                mAnimationView.invalidate();
            }
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v.equals(mBeginDetectBtn)) {

            // mAnimationView.setBitmapIndex(1);
            if (!mIsStart) {
                if (!mWifiDevice.isHadConnect()) {
                    showToast("Not connected to radar device, please check network connection status！");
                    return;
                }
                if (mRadarDevice.isDeting()) {
                    stopDetect();
                }

                beginDetect();

                // mTargetNumber = 4;
                // for(int i=0;i<mTargetNumber;i++)
                // {
                // mTargetInfo[i].mExistBreath = 1;
                // mTargetInfo[i].mBreathPos = (short)(500 + 100 * i);
                // }
            } else {
                stopDetect();
            }

            mIsStart = !mIsStart;
        } else if (v.equals(mStopDetectBtn)) {

        } else if (v.equals(mBrowseResultBtn)) {
            if (mIsStart || mRadarDevice.isDeting()) {
                showToast("Please stop probing first！");
                return;
            }
            // showPhoneNumberDialog(this);

            Intent intent = new Intent(this, ResultExplorerActivity.class);
           // intent.setClass(LifeSearchActivity.this, ResultExplorerActivity.class);

            startActivity(intent);
        }
    }

    public void shotView(View view) {
        Bitmap bitmap;

        // view.setDrawingCacheEnabled(true);
        // view.buildDrawingCache();
        // bitmap = view.getDrawingCache();

        bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);
        view.draw(canvas);

        if (bitmap != null) {
            try {
                FileOutputStream out = new FileOutputStream(mResultFileName + ".png");
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    public int onDevDataMsg(DevData devData) {
        int i;

        // 根据设置设定探测结果
        setDetectingResult(devData);

        short rangeD = 0, rangeU = 0;
        short[] range = mRadarDevice.getDetectingRange();
        rangeD = range[0];
        rangeU = range[1];

        switch (devData.mType) {
            case Global.DEVICE_ALL_INFS:
                // 设置设备状态
                // Log.e(TAG, "Receive one Begin DEVICE_ALL_INFS!");
                switch (devData.mStatus.mType) {
                    // 正在探测
                    case Global.DEVICE_DETECTING:
                        // 设置雷达设备状态
                        mRadarDevice.setDetingRange(devData.mStatus.mRangeUp, devData.mStatus.mRangeDown);
                        mRadarDevice.setStatus(Global.RADAR_STATUS_DETECTING);

                        // Log.e(TAG, "Now Receive DEVICE_DETECTING_INFO!");

                        //
                        int start = (devData.mStatus.mRangeUp - mSigPosOff * 15) / 100;
                        int end = (devData.mStatus.mRangeDown - mSigPosOff * 15) / 100;
                        String text = "Probing" + String.valueOf(start) + "m--" + String.valueOf(end) + "m...";
                        mRadarStatusText.setText(text);
                        break;
                    // 探测结束
                    case Global.DEVICE_DETECTEND:
                        mRadarDevice.setStatus(Global.RADAR_STATUS_READY);
//                        Log.e(TAG, "Now Receive DEVICE_DETECTEND_INFO!");
                        mRadarStatusText.setText("The detection is over, the device is ready");
                        stopDetect();
                        mIsStart = !mIsStart;
                        mDetectEnd = true;
                        break;
                    // 设备就绪
                    case Global.DEVICE_READY:
                        if (!mDetectEnd) {
                            // 防止收不到探测结束
                            if (mLastBeginFlag) {
                                stopDetect();
                                mIsStart = !mIsStart;
                                mRadarStatusText.setText("The detection is over, the device is ready");
                                mDetectEnd = true;
                                return 1;
                            }
                            short oldMode;
                            oldMode = mRadarDevice.getStatus();
                            mRadarDevice.setStatus(Global.RADAR_STATUS_READY);
                            // Log.e(TAG, "Now Receive READY_INFO!");
                            mRadarStatusText.setText("The device is ready");
                        } else {
                            mRadarStatusText.setText("The detection is over, the device is ready");
                        }

                        break;
                    // 正在保存数据
                    case Global.DEVICE_SAVING:
                        mRadarDevice.setStatus(Global.RADAR_STATUS_SAVING);
                        // Log.e(TAG, "Now saving....!");
                        break;
                    // 正在连续保存数据
                    case Global.DEVICE_CONTINUE_SAVING:
                        mRadarDevice.setStatus(Global.RADAR_STATUS_CONSAVING);
                        // Log.e(TAG, "Now ContinueSaving...!");
                        break;
                }
                // ////检测结果的合法性
                checkResult(devData);
                // 读取探测结果
                mDetectResult.mTargetNum = devData.mResult.mTargetNum; // 目标数
                // Log.e(TAG, "target number = " + mDetectResult.mTargetNum);

                for (i = 0; i < 10; i++) {
                    // ////////////
                    mDetectResult.mResult[i].mBreathPos = devData.mResult.mResult[i].mBreathPos;
                    mDetectResult.mResult[i].mExistBreath = devData.mResult.mResult[i].mExistBreath;

                    // /////////////
                    mDetectResult.mResult[i].mExistMove = devData.mResult.mResult[i].mExistMove;
                    mDetectResult.mResult[i].mMovePos = devData.mResult.mResult[i].mMovePos;
                }
                // 中间结果
                mDetectResult.mMiddleTargetNum = devData.mResult.mMiddleTargetNum;
                // Log.e(TAG, "middle target num = " +
                // devData.mResult.mMiddleTargetNum);
                for (i = 0; i < 10; i++) {
                    // /////////
                    mDetectResult.mResult[i + 10].mBreathPos = devData.mResult.mResult[i + 10].mBreathPos;
                    mDetectResult.mResult[i + 10].mExistBreath = devData.mResult.mResult[i + 10].mExistBreath;

                    if (devData.mResult.mResult[i + 10].mExistBreath > 0) {
                        // Log.e(TAG, "middle target exit breath!");
                    }

                    // //////////
                    mDetectResult.mResult[i + 10].mExistMove = devData.mResult.mResult[i + 10].mExistMove;
                    mDetectResult.mResult[i + 10].mMovePos = devData.mResult.mResult[i + 10].mMovePos;
                }
                // /////保存结果到记录文件
            {
                // //如果一段探测结束
                if (devData.mStatus.mType == Global.DEVICE_RESULT) {
                    // 使用时间设为0
                    mUseTime = 0;
                    // 重新开启蜂鸣器
                    mCanBeep = true;
                    // 累计次数加1
                    mDetectNumber++;
                    // Log.e(TAG, "one detect end, target number = " +
                    // mDetectResult.mTargetNum);
                }

                // //设置全局结果记录
                int index;
                index = mNowTargetIndex;
                // Log.e(TAG, "mNowTargetIndex = " + String.valueOf(index));
                for (i = 0; i < mDetectResult.mTargetNum; i++) {
                    if (index >= 100)
                        index = 0;
                    // //这样下一次值就被冲掉了
                    // mTargetInfo[index] = mDetectResult.mResult[i];

                    mTargetInfo[index].mExistBreath = mDetectResult.mResult[i].mExistBreath;
                    mTargetInfo[index].mBreathPos = mDetectResult.mResult[i].mBreathPos;
                    mTargetInfo[index].mExistMove = mDetectResult.mResult[i].mExistMove;
                    mTargetInfo[index].mMovePos = mDetectResult.mResult[i].mMovePos;
                    index++;
                }

                mNowTargetIndex = index;
                mTargetNumber += mDetectResult.mTargetNum;
                if (mTargetNumber >= 100)
                    mTargetNumber = 100;

                // 如果没有探测结果
                if (mDetectResult.mTargetNum == 0) {
                    break;
                } else {
                    mAnimationView.invalidate();
                }
                // ///保存探测结果
                saveResultToFile();
            }
            break;
            // 设备探测结果
            case Global.DEVICE_DETECT_RESULT:
                // ////检测结果的合法性
                checkResult(devData);
                mDetectResult.mTargetNum = devData.mResult.mTargetNum;

                for (i = 0; i < devData.mResult.mTargetNum; i++) {
                    // ///////////////
                    mDetectResult.mResult[i].mBreathPos = devData.mResult.mResult[i].mBreathPos;
                    mDetectResult.mResult[i].mExistBreath = devData.mResult.mResult[i].mExistBreath;

                    // ///////////////
                    mDetectResult.mResult[i].mExistMove = devData.mResult.mResult[i].mExistMove;
                    mDetectResult.mResult[i].mMovePos = devData.mResult.mResult[i].mMovePos;

                    mCanBeep = true;
                }
                mDetectResult.mMiddleTargetNum = devData.mResult.mMiddleTargetNum;
                for (i = 0; i < devData.mResult.mMiddleTargetNum; i++) {
                    // /////////
                    mDetectResult.mResult[i + 10].mBreathPos = devData.mResult.mResult[i + 10].mBreathPos;
                    mDetectResult.mResult[i + 10].mExistBreath = devData.mResult.mResult[i + 10].mExistBreath;

                    // ////////
                    mDetectResult.mResult[i + 10].mExistMove = devData.mResult.mResult[i + 10].mExistMove;
                    mDetectResult.mResult[i + 10].mMovePos = devData.mResult.mResult[i + 10].mMovePos;
                }
                break;
            // 设备状态
            case Global.DEVICE_STATUS:
                switch (devData.mStatus.mType) {
                    case Global.DEVICE_DETECTING:
                        // 设置雷达设备状态
                        mRadarDevice.setDetingRange(devData.mStatus.mRangeUp, devData.mStatus.mRangeDown);
                        mRadarDevice.setStatus(Global.RADAR_STATUS_DETECTING);
                        break;
                    case Global.DEVICE_DETECTEND:
                        mRadarDevice.setStatus(Global.RADAR_STATUS_READY);
                        break;
                    case Global.DEVICE_READY:
                        mRadarDevice.setStatus(Global.RADAR_STATUS_READY);
                        break;
                }

                break;
        }

        return 1;
    }

    // //根据设置设定探测结果
    private void setDetectingResult(DevData devData) {
        if (!mControlMode) {
            return;
        }

        short rangeUp, rangeDown;
        rangeUp = devData.mStatus.mRangeUp;
        rangeDown = devData.mStatus.mRangeDown;

        // ///如果是空扫
        if (mBlueRcvBuf.mTargetType == Global.BLUE_TARGET_SCAN) {
            for (int i = 0; i < 10; i++) {
                devData.mResult.mResult[i].mBreathPos = 0;
                devData.mResult.mResult[i].mExistBreath = 0;
                devData.mResult.mResult[i].mExistMove = 0;
                devData.mResult.mResult[i].mMovePos = 0;
                //
                devData.mResult.mResult[10 + i].mExistBreath = 0;
                devData.mResult.mResult[10 + i].mExistMove = 0;
            }
            devData.mResult.mMiddleTargetNum = 0;
            devData.mResult.mResult[10].mExistBreath = 0;
            devData.mResult.mResult[10].mBreathPos = 0;
            //
            return;
        }
        // //

        Log.e(TAG, "Enter setDetectingResult " + mSetTargetPos + "!");
        int add;
        if (mSetTargetPos != 0) {
            // //初识化为没有探测到目标
            for (int i = 0; i < 10; i++) {
                devData.mResult.mResult[i].mBreathPos = 0;
                devData.mResult.mResult[i].mExistBreath = 0;
                devData.mResult.mResult[i].mExistMove = 0;
                devData.mResult.mResult[i].mMovePos = 0;
                //
                devData.mResult.mResult[10 + i].mExistBreath = 0;
                devData.mResult.mResult[10 + i].mExistMove = 0;
            }
            devData.mResult.mMiddleTargetNum = 0;
            devData.mResult.mResult[10].mExistBreath = 0;
            devData.mResult.mResult[10].mBreathPos = 0;

            // //接收到中间结果，开始中间结果的闪烁
            if (devData.mStatus.mType == Global.DEVICE_INTER_RESULT || mCanFlash) {
                Log.e(TAG, "Control mode Inter Result!");
                //
                mCanFlash = true;
                //
                if (mSetTargetPos >= rangeUp && mSetTargetPos <= rangeDown) {
                    for (int i = 0; i < 10; i++) {
                        devData.mResult.mResult[i].mBreathPos = 0;
                        devData.mResult.mResult[i].mExistBreath = 0;
                        devData.mResult.mResult[i].mExistMove = 0;
                        devData.mResult.mResult[i].mMovePos = 0;
                        //
                        devData.mResult.mResult[10 + i].mExistBreath = 0;
                        devData.mResult.mResult[10 + i].mExistMove = 0;
                    }
                    devData.mResult.mMiddleTargetNum = 1;
                    devData.mResult.mResult[10].mExistBreath = 1;
                    if (devData.mStatus.mType == Global.DEVICE_INTER_RESULT) {
                        Random rdm = new Random();
                        add = rdm.nextInt();
                        mOffDistance = add % 50;
                    }
                    devData.mResult.mResult[10].mBreathPos = (short) (mSetTargetPos + mOffDistance);

                }
            }

            // ///设置最终结果
            // 接收到一段探测结束消息 && 在探测范围内
            if ((devData.mStatus.mType == Global.DEVICE_RESULT) || (devData.mResult.mTargetNum != 0)) {
                Log.e(TAG, "Control mode Result!");
                if ((mSetTargetPos >= rangeUp) && (mSetTargetPos <= rangeDown)) {
                    //
                    for (int i = 0; i < 10; i++) {
                        devData.mResult.mResult[i].mBreathPos = 0;
                        devData.mResult.mResult[i].mExistBreath = 0;
                        devData.mResult.mResult[i].mExistMove = 0;
                        devData.mResult.mResult[i].mMovePos = 0;
                    }
                    // //
                    devData.mResult.mTargetNum = 1;
                    devData.mResult.mResult[0].mExistBreath = 1;
                    Random rdm = new Random();
                    add = rdm.nextInt();
                    mOffDistance = add % 50;
                    devData.mResult.mResult[0].mBreathPos = (short) (mSetTargetPos + mOffDistance);
                    devData.mResult.mMiddleTargetNum = 0;

                    // //如果是单目标模式，停止探测
                    if (mRadarDevice.isSingleTargetMode()) {
                        // //停止探测
                        stopDetect();
                    }
                    mCanFlash = false;
                    return;
                }
            }
        }
    }

    private void checkResult(DevData devData) {
        short rangeD = 0, rangeU = 0;
        short[] range = mRadarDevice.getDetectingRange();
        rangeD = range[0];
        rangeU = range[1];
        rangeU = (short) (rangeD + 300);
        short distance;

        for (int i = 0; i < 20; i++) {
            if (devData.mResult.mResult[i].mExistBreath != 0) {
                distance = devData.mResult.mResult[i].mBreathPos;

                if (distance < rangeD) {
                    distance = rangeD;
                } else {
                    if (distance > rangeU)
                        distance = rangeU;
                }

                // distance -= 140;
                devData.mResult.mResult[i].mBreathPos = distance;
            }

            //
            if (devData.mResult.mResult[i].mExistMove != 0) {
                distance = devData.mResult.mResult[i].mMovePos;

                if (distance < rangeD) {
                    distance = rangeD;
                } else {
                    if (distance > rangeU)
                        distance = rangeU;
                }

                // distance -= 140;
                devData.mResult.mResult[i].mMovePos = distance;
            }
        }
    }

    private void beginDetect() {
        resetDetectResult(); // 初识化为没有发现目标
        mWifiDevice.clearSendPacket();
        // 设置一次探测范围
        byte[] comBuf = new byte[4];
        comBuf[0] = Global.RADAR_COMMAND_TIMEWND & 0xFF;
        comBuf[1] = (Global.RADAR_COMMAND_TIMEWND >> 8) & 0xFF;
        comBuf[2] = 20;
        comBuf[3] = 0;
        mWifiDevice.sendCommand(comBuf, 4);

        comBuf[0] = Global.RADAR_COMMAND_SAMPLEN & 0xFF;
        comBuf[1] = (Global.RADAR_COMMAND_SAMPLEN >> 8) & 0xFF;
        comBuf[2] = 2048 & 0xFF;
        comBuf[3] = (2048 >> 8) & 0xFF;
        mWifiDevice.sendCommand(comBuf, 4);

        comBuf[0] = Global.RADAR_COMMAND_SCANSPEED & 0xFF;
        comBuf[1] = (Global.RADAR_COMMAND_SCANSPEED >> 8) & 0xFF;
        comBuf[2] = 64;
        comBuf[3] = 0;
        mWifiDevice.sendCommand(comBuf, 4);

        // 设置探测范围
        comBuf[0] = Global.RADAR_COMMAND_DETECTRANGE & 0xFF;
        comBuf[1] = (Global.RADAR_COMMAND_DETECTRANGE >> 8) & 0xFF;
        comBuf[2] = (byte) ((Global.DEFAULT_DETECTRANGE / 100) & 0xFF);
        comBuf[3] = (byte) (((Global.DEFAULT_DETECTRANGE / 100) >> 8) & 0xFF);
        mWifiDevice.sendCommand(comBuf, 4);

        // 设置起始探测位置
        short sigPos = (short) (mSigPosOff * 15 + mRadarDevice.mNowBegPos);
        comBuf[0] = Global.RADAR_COMMAND_DETECTBEGPOS & 0xFF;
        comBuf[1] = (Global.RADAR_COMMAND_DETECTBEGPOS >> 8) & 0xFF;
        comBuf[2] = (byte) (sigPos & 0xFF);
        comBuf[3] = (byte) ((sigPos >> 8) & 0xFF);
        mWifiDevice.sendCommand(comBuf, 4);

        // 生成一个命令传输
        comBuf[0] = Global.RADAR_COMMAND_BEGDETECT & 0xFF;
        comBuf[1] = (Global.RADAR_COMMAND_BEGDETECT >> 8) & 0xFF;
        mWifiDevice.sendCommand(comBuf, 2);

        //
        mDetectResult.mTargetNum = 0; // 初识化为没有发现目标
        mRadarDevice.setDetingRange((short) 0, (short) 0);
        mHasBegDetect = false;

        // ///////////////生成探测结果文件

        String folderPath;
        folderPath = getExternalFilesDir(null) + "/" + Global.RESULT_DIRECTORY_NAME;
//		Log.d("folderPath", "folderPath"+folderPath);
        File lteDir = new File(folderPath);
        if (!lteDir.exists()) {
            lteDir.mkdirs();
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        long localTime = System.currentTimeMillis();
        Date timeSpan = new Date(localTime);
        mDetectTimeString = format.format(timeSpan);
        mResultFileName = folderPath + "/" + mDetectTimeString;

        // showToast(fileName);
        File file = new File(mResultFileName + ".txt");
        try {
            file.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            showToast(e.getMessage());
            e.printStackTrace();
        }
        try {
            mOutputStream = new FileOutputStream(mResultFileName + ".txt");
            if (mOutputStream == null) {
                showToast("mOutputStream = null");
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            showToast(e.getMessage());
            e.printStackTrace();
        }

        mCanBeep = true;
        mBeepNumber = 0;
        mUseTime = 0;
        mDetectNumber = 0;
        mHasWriteToFileDetectRsu = 0;
        // ////////////////////

        // /清除最终结果
        mTargetNumber = 0;
        mNowTargetIndex = 0;
        mCanFlash = false;

        mRadarDevice.setStatus(Global.RADAR_COMMAND_BEGDETECT);

        mTimer.setTimer(DETECT_TIMERID, 500);

        mDetectResultText.setText("");
        // mBrowseResultBtn.setEnabled(false);
        mAnimationView.startAnimation();
        mBeginDetectBtn.setBackgroundResource(R.drawable.x_stopdetect);

        mLastBeginFlag = false;
        mDetectEnd = false;
    }

    private void stopDetect() {
        // 生成一个命令传输
        byte[] command = new byte[2];
        command[0] = Global.RADAR_COMMAND_ENDDETECT & 0xFF;
        command[1] = (Global.RADAR_COMMAND_ENDDETECT >> 8) & 0xFF;
        mWifiDevice.sendCommand(command, 2);

        //
        mRadarDevice.setStatus(Global.RADAR_STATUS_SNDINGENDDETECT);
        mUseTime = 0;
        mDetectNumber = 0;
        mCanFlash = false;

        //
        mBlueRcvBuf.mTargetPos = 0;
        mControlMode = false;
        mSetTargetPos = 0;

        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (mTimer.isTimerOn(DETECT_TIMERID)) {
            mTimer.killTimer(DETECT_TIMERID);
        }

        mAnimationView.stopAnimation();
        // mAnimationView.invalidate();
        // mBrowseResultBtn.setEnabled(true);
        shotView(mAnimationView);
        // mAnimationView.setShotFlag();
        mBeginDetectBtn.setBackgroundResource(R.drawable.x_begindetect);

        mLastBeginFlag = false;
    }

    private void resetDetectResult() {
        mDetectResult.mTargetNum = 0;
        mDetectResult.mMiddleTargetNum = 0;

        for (int i = 0; i < 20; i++) {
            mDetectResult.mResult[i].mBreathPos = 0;
            mDetectResult.mResult[i].mExistBreath = 0;

            mDetectResult.mResult[i].mExistMove = 0;
            mDetectResult.mResult[i].mMovePos = 0;
        }
    }

    private int getBitmapIndex() {
        int index;
        short rangeD = 0, rangeU = 0;
        short[] range = mRadarDevice.getDetectingRange();
        rangeD = range[0];
        rangeU = range[1];
        rangeD -= (short) (mSigPosOff * 15);
        rangeU -= (short) (mSigPosOff * 15);

        index = rangeD / 300 + 1;

        // Log.e(TAG, String.valueOf(rangeD) + " --- " +
        // String.valueOf(rangeU));
        if (index > Global.DEFAULT_DETECTRANGE / 300) {
            index = Global.DEFAULT_DETECTRANGE / 300;
        }

        if (index == Global.DEFAULT_DETECTRANGE / 300) {
            mLastBeginFlag = true;
        }

        return index;
    }

    public void setRadarStatusText(String text) {
        mRadarStatusText.setText(text);
        Log.e(TAG, text);
    }

    private void showPhoneNumberDialog(Context context) {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View textEntryView = inflater.inflate(R.layout.view_phone_number, null);
        final EditText numberInput = (EditText) textEntryView.findViewById(R.id.edit_phonenum);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle("请输入电话号码");
        builder.setView(textEntryView);
        builder.setPositiveButton("发送", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String phoneNum = numberInput.getText().toString();

                if (mResultFileName != null) {
                    try {

                        File resultFile = new File(mResultFileName + ".txt");

                        FileInputStream in = new FileInputStream(resultFile);
                        int size = in.available();
                        byte[] buf = new byte[size];
                        in.read(buf);
                        in.close();

                        String msg = mDetectTimeString + "探测结果：\n" + new String(buf);
                        showToast(msg);
                        // sendMsg(msg, phoneNum);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    showToast("结果文件不存在！");
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        builder.show();
    }

    public void showAlertDialog() {
        Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("System error");
        builder.setIcon(R.drawable.ic_launcher);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                System.exit(0);
            }
        });
        builder.setMessage("The system encounters strong interference, please restart the program and radar!");
        builder.show();
    }

    private void sendMsg(String msg, String phoneNum) {
        SmsManager smsManager = SmsManager.getDefault();
        PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent(), 0);

        // 短信字数大于70，自动分条
        List<String> ms = smsManager.divideMessage(msg);

        for (String str : ms) {
            // 短信发送
            smsManager.sendTextMessage(phoneNum, null, str, sentIntent, null);
        }
    }

    private void saveResultToFile() {
        int distance = 0;
        if (mOutputStream != null) {
            // ///写入探测结果: 次数(4位) 探测结果(厘米4位) 回车和换行
            int i;
            String txt;
            // mDetectResult.mTargetNum = 10;
            for (i = 0; i < mDetectResult.mTargetNum; i++) {
                if ((mDetectResult.mResult[i].mExistBreath == 0) && (mDetectResult.mResult[i].mExistMove == 0))
                    continue;
                if (mDetectResult.mResult[i].mExistBreath != 0)
                    distance = mDetectResult.mResult[i].mBreathPos;
                if (mDetectResult.mResult[i].mExistMove != 0)
                    distance = mDetectResult.mResult[i].mMovePos;
                //
                mHasWriteToFileDetectRsu++;
                // distance = mHasWriteToFileDetectRsu;

                txt = String.valueOf(mHasWriteToFileDetectRsu);
                String[] ss = {"0000", "000", "00", "0", ""};
                txt = ss[txt.length()] + txt + " ";
                // //写入探测次数
                int len = txt.length();
                byte[] buf = txt.getBytes();
                try {
                    mOutputStream.write(buf, 0, len);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // ///写入探测结果
                if (distance <= 0) {
                    txt = "0000";
                } else {
                    txt = String.valueOf(distance);
                    txt = ss[txt.length()] + txt;
                }
                len = txt.length();
                buf = txt.getBytes();
                try {
                    mOutputStream.write(buf, 0, len);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // 写入回车和换行
                buf[0] = '\n';
                try {
                    mOutputStream.write(buf, 0, 1);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendBeginPos(short pos) {
        byte[] command = new byte[4];
        command[0] = Global.RADAR_COMMAND_DETECTBEGPOS & 0xFF;
        command[1] = (Global.RADAR_COMMAND_DETECTBEGPOS >> 8) & 0xFF;
        command[2] = (byte) (pos & 0xFF);
        command[3] = (byte) ((pos >> 8) & 0xFF);
        mWifiDevice.sendCommand(command, 4);
        mRadarDevice.setBegPos(pos);
    }

    void onSingletagetMode() {
        // TODO: Add your command handler code here
        byte[] command = new byte[4];
        command[0] = Global.RADAR_COMMAND_DETECTMODE & 0xFF;
        command[1] = (Global.RADAR_COMMAND_DETECTMODE >> 8) & 0xFF;
        command[2] = (byte) (Global.SINGLE_DETECTMODE & 0xFF);
        command[3] = (byte) ((Global.SINGLE_DETECTMODE >> 8) & 0xFF);
        mWifiDevice.sendCommand(command, 4);
        mRadarDevice.setDetectMode(Global.SINGLE_DETECTMODE);
    }

    void onMultitargetMode() {
        // TODO: Add your command handler code here
        byte[] command = new byte[4];
        command[0] = Global.RADAR_COMMAND_DETECTMODE & 0xFF;
        command[1] = (Global.RADAR_COMMAND_DETECTMODE >> 8) & 0xFF;
        command[2] = (byte) (Global.MANY_DETECTMODE & 0xFF);
        command[3] = (byte) ((Global.MANY_DETECTMODE >> 8) & 0xFF);
        mWifiDevice.sendCommand(command, 4);
        mRadarDevice.setDetectMode(Global.MANY_DETECTMODE);
    }

    private void beginTransWave() {
        // 生成一个命令传输
        byte[] comBuf = new byte[4];
        comBuf[0] = Global.RADAR_COMMAND_SHOWWAVE & 0xFF;
        comBuf[1] = (Global.RADAR_COMMAND_SHOWWAVE >> 8) & 0xFF;
        comBuf[2] = 1;
        comBuf[3] = 0;
        mWifiDevice.sendCommand(comBuf, 4);

        // if (mTimer.isTimerOn(SHOWWAVE_TIMERID))
        // {
        // mTimer.killTimer(SHOWWAVE_TIMERID);
        // }
        // mTimer.setTimer(SHOWWAVE_TIMERID, 100);
        // mRadarDevice.beginSave();
    }

    private void stopTransWave() {
        // if (mTimer.isTimerOn(SHOWWAVE_TIMERID))
        // {
        // mTimer.killTimer(SHOWWAVE_TIMERID);
        // }
        // mRadarDevice.stopSave();

        byte[] comBuf = new byte[4];
        comBuf[0] = Global.RADAR_COMMAND_SHOWWAVE & 0xFF;
        comBuf[1] = (Global.RADAR_COMMAND_SHOWWAVE >> 8) & 0xFF;
        comBuf[2] = 0;
        comBuf[3] = 0;
        mWifiDevice.sendCommand(comBuf, 4);
    }

    private void sendSigPos(short sigpos) {
        byte[] comBuf = new byte[4];
        comBuf[0] = Global.RADAR_COMMAND_SIGNALPOS & 0xFF;
        comBuf[1] = (Global.RADAR_COMMAND_SIGNALPOS >> 8) & 0xFF;
        comBuf[2] = (byte) (sigpos & 0xFF);
        comBuf[3] = (byte) ((sigpos >> 8) & 0xFF);
        ;
        mWifiDevice.sendCommand(comBuf, 4);
    }

    private void showSigPosDialog(Context context) {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View textEntryView = inflater.inflate(R.layout.view_sigpos, null);
        final EditText numberInput = (EditText) textEntryView.findViewById(R.id.edit_sigpos);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle("请输入信号位置");
        builder.setView(textEntryView);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String text = numberInput.getText().toString();
                sendSigPos((short) Integer.parseInt(text));
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.operation_menu, menu);
        return true;
        // return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        // if (!mWifiDevice.isHadConnect())
        // {
        // showToast("未连接到雷达设备，请检查网络连接状态！");
        // return false;
        // }
        if (mIsStart || mRadarDevice.isDeting()) {
            showToast("Please stop probing first！");
            return false;
        }
        mDetectEnd = false;
        switch (item.getItemId()) {
            case R.id.beginpos0:
                sendBeginPos((short) 0);
                break;
            case R.id.beginpos3:
                sendBeginPos((short) 300);
                break;
            case R.id.beginpos6:
                sendBeginPos((short) 600);
                break;
            case R.id.beginpos9:
                sendBeginPos((short) 900);
                break;
            case R.id.beginpos12:
                sendBeginPos((short) 1200);
                break;
            case R.id.beginpos15:
                sendBeginPos((short) 1500);
                break;
            case R.id.beginpos18:
                sendBeginPos((short) 1800);
                break;
            case R.id.beginpos21:
                sendBeginPos((short) 2100);
                break;
            case R.id.beginpos24:
                sendBeginPos((short) 2400);
                break;
            case R.id.beginpos27:
                sendBeginPos((short) 2700);
                break;
            case R.id.beginpos30:
                sendBeginPos((short) 3000);
                break;
            case R.id.singletarget:
                onSingletagetMode();
                item.setChecked(true);
                break;
            case R.id.multitarget:
                onMultitargetMode();
                item.setChecked(true);
                break;
            case R.id.detectmode:
                stopTransWave();
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                setContentView(mDetectView);
                item.setChecked(true);
                mDetectMode = !mDetectMode;
                break;
            case R.id.promode:
                // sendSigPos((short)-90);
                beginTransWave();
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                setContentView(mDibView);
                // sendBeginPos((short) 0);
                mDibView.initDIB();
                item.setChecked(true);
                mDetectMode = !mDetectMode;
                break;
            // case R.id.sigpos:
            // showSigPosDialog(this);
            // break;

            default:
                break;
        }
        return true;
        // return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        MenuItem menuItem = menu.findItem(R.id.multitarget);

        if (mRadarDevice.isMultiTargetMode()) {
            menuItem.setChecked(true);
        } else {
            menuItem.setChecked(false);
        }

        menuItem = menu.findItem(R.id.singletarget);

        if (mRadarDevice.isSingleTargetMode()) {
            menuItem.setChecked(true);
        } else {
            menuItem.setChecked(false);
        }

        menuItem = menu.findItem(R.id.detectmode);
        if (mDetectMode) {
            menuItem.setChecked(true);
        } else {
            menuItem.setChecked(false);
        }

        menuItem = menu.findItem(R.id.promode);
        if (mDetectMode) {
            menuItem.setChecked(false);
        } else {
            menuItem.setChecked(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        // TODO Auto-generated method stub
        return super.onMenuOpened(featureId, menu);
    }

    public Handler mNetHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case Global.MESSAGE_SND_COMMAND:
                    String text = (String) msg.obj;
                    setRadarStatusText(text);
                    break;
                case Global.MESSAGE_RCV_DEVICE_DATA:
                    Log.e(TAG, "recv one device data!");
                    DevData devData = (DevData) msg.obj;
                    onDevDataMsg(devData);
                    break;
                case Global.MESSAGE_RCV_WAVE:
                    if (isDetectMode()) {
                        mRadarDevice.initBuf();
                        break;
                    }
                    byte[] waveBuf = (byte[]) msg.obj;
                    mRadarDevice.recvDatas(waveBuf, msg.arg1);

                    // Log.e(TAG, "Rcv wave data!");
                    break;

                default:
                    break;
            }
        }
    };

    private boolean isDetectMode() {
        return mDetectMode ? true : false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        // return super.onKeyDown(keyCode, event);
        Log.e(TAG, "onKeyDown keycode = " + keyCode);

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // TODO Auto-generated method stub
        // int action = event.getAction();
        // int keyCode = event.getKeyCode();
        //
        // // if (keyCode == KeyEvent.KEYCODE_S)
        // // {
        // // if (action == KeyEvent.ACTION_UP)
        // // {
        // // long actionUpTime = event.getEventTime();
        // // long actionDownTime = event.getDownTime();
        // // long remainTime = actionUpTime - actionDownTime;
        // // if (remainTime < 3000)
        // // {
        // //
        // // }
        // // else
        // // {
        // // }
        // // }
        // // }
        // if (keyCode == KeyEvent.KEYCODE_BACK && action ==
        // KeyEvent.ACTION_DOWN)
        // {
        // return true;
        // }
        //
        // if (keyCode == KeyEvent.KEYCODE_BACK && action == KeyEvent.ACTION_UP)
        // {
        // if (mRadarDevice.isDeting() || mIsStart)
        // {
        // return true;
        // }
        // else
        // {
        // return super.dispatchKeyEvent(event);
        // }
        //
        // }

        return super.dispatchKeyEvent(event);
    }

    public static String getStoragePath(Context mContext, String keyword) {
        String targetpath = "";
        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");

            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");

            Method getPath = storageVolumeClazz.getMethod("getPath");

            Object result = getVolumeList.invoke(mStorageManager);

            final int length = Array.getLength(result);

            Method getUserLabel = storageVolumeClazz.getMethod("getUserLabel");

            for (int i = 0; i < length; i++) {

                Object storageVolumeElement = Array.get(result, i);

                String userLabel = (String) getUserLabel.invoke(storageVolumeElement);

                String path = (String) getPath.invoke(storageVolumeElement);

                if (userLabel.contains(keyword)) {
                    targetpath = path;
                }

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return targetpath;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        // return super.onKeyDown(keyCode, event);
        boolean exit = false;
        Log.e(TAG, "onKeyUp keycode = " + keyCode);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(this).setTitle("Tips").setMessage("confirm to exit the program？").setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                }
            }).setPositiveButton("Quit", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    finish();
                }
            }).show();

            return true;
        }

        if (mRadarDevice.isDeting()) {
            // ////如果正在探测
            // 得到探测范围
            short rangeU, rangeD;

            short[] range = mRadarDevice.getDetectingRange();
            rangeD = range[0];
            rangeU = range[1];

            short distance = mBlueRcvBuf.mTargetPos;
            if (distance < rangeD || distance > rangeU) {
                distance = rangeD;
            }

            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                distance += 100;
                if (distance > rangeU)
                    distance = rangeU;
                mBlueRcvBuf.mTargetPos = distance;
                mBlueRcvBuf.mTargetType = Global.BLUE_TARGET_BREATH;

                return true;
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                distance -= 100;
                if (distance < rangeD)
                    distance = rangeD;
                mBlueRcvBuf.mTargetPos = distance;
                mBlueRcvBuf.mTargetType = Global.BLUE_TARGET_BREATH;
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                Log.e(TAG, "Enter control mode!");
                mControlMode = true;
                mBlueRcvBuf.mTargetPos = distance;
                mSetTargetPos = mBlueRcvBuf.mTargetPos;
                mCanFlash = false;

                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                Log.e(TAG, "Exit control mode!");
                mBlueRcvBuf.mTargetPos = 0;
                mControlMode = false;
                mSetTargetPos = 0;
                mCanFlash = false;

                return true;
            } else {
                return super.onKeyUp(keyCode, event);
            }
        } else {
            return super.onKeyUp(keyCode, event);
        }

        // if(pMsg->wParam == KEY_ENTER)
        // {
        // if(g_blueRcvBuf.m_targetType != BLUE_TARGET_SCAN)
        // {
        // distance = 100;
        // g_blueRcvBuf.m_targetPos = distance;
        // g_blueRcvBuf.m_targetType = BLUE_TARGET_SCAN; //空扫
        // theApp.m_controlMode = true;
        // theApp.m_setTargetPos = g_blueRcvBuf.m_targetPos;
        // }
        // else
        // {
        // g_blueRcvBuf.m_targetType = BLUE_TARGET_BREATH;
        // g_blueRcvBuf.m_targetPos = 0;
        // theApp.m_controlMode = false;
        // theApp.m_setTargetPos = 0;
        // }
        // //
        // return 1;
        // }

    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        Log.e(TAG, "onKeyLongPress keycode = " + keyCode);

        return super.onKeyLongPress(keyCode, event);
    }
}
