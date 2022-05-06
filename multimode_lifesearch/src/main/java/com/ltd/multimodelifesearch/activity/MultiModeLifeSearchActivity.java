package com.ltd.multimodelifesearch.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Connection.Packet;
import com.ltd.multimode_lifesearch.R;
import com.ltd.multimodelifesearch.adapter.FileAdapter;
import com.ltd.multimodelifesearch.adapter.RadarParamExpandableListAdapter;
import com.ltd.multimodelifesearch.ui.LeftFragment;
import com.ltd.multimodelifesearch.ui.RightFragment;
import com.ltdpro.BackPlayHRulerView;
import com.ltdpro.BackPlayVRulerView;
import com.ltdpro.BackplayScanView;
import com.ltdpro.BatteryAttribute;
import com.ltdpro.DebugUtil;
import com.ltdpro.FileHeader;
import com.ltdpro.GPSDevice;
import com.ltdpro.Global;
import com.ltdpro.HRulerView;
import com.ltdpro.LogWriter;
import com.ltdpro.MyApplication;
import com.ltdpro.NetworkDevice;
import com.ltdpro.backPlayDIBView;
import com.ltdpro.radarDevice;
import com.ltdpro.realTimeDIBView;
import com.ltdpro.scanView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.System.exit;

/**
 * 键盘控制
 *
 * @author huangss
 */
public class MultiModeLifeSearchActivity extends Activity implements UncaughtExceptionHandler {
    // 视图变量
    private TextView tv, textview_systemtime;
    public realTimeDIBView mRealTimeDIBView; // 实时显示类
    public scanView mScanView;
    public backPlayDIBView mbackPlayDIBView; // 回放显示类
    public BackplayScanView mbackplayScanView;
    private LeftFragment fragLeft; // 获取fragement的实例
    private RightFragment fragRight; // 获取rightfragment的实例
    private MyApplication mApp; // 全局参数
    private String TAG = "IDSC2600MainActivity"; // 设置tag
    private String KTAG = "KTAG"; // 按键响应的标志
    private String SERIALPORT = "/dev/ttySAC3"; // 串口端口号
    // 适配器
    private ExpandableListView elv;
    private GPSDevice gpsport = new GPSDevice(); // GPS端口
    // 回放状态标志
    private boolean mIsBackplaying = false; // 正在回放标志
    private boolean mBackplayPause = false; // 暂停回放
    private boolean mBackplayFocusView = false; // 回放时焦点的位置，左侧回放区为false，右侧单道区为true
    private String mBackplayFileName; // 正在回放的数据文件
    private final int BACKPLAY_FORWARD_DIR = 1; // 方向前
    private final int BACKPLAY_BACK_DIR = 2; // 方向后
    private int mBackplayDir = BACKPLAY_FORWARD_DIR; // 回放的方向
    public boolean mIsTempstopBackplay = false; // 是否暂停回放
    private FileInputStream mBackplayFile = null;
    private FileHeader mBackplayFileHeader = new FileHeader();
    private int mResponFlag = 0; // 响应标志，区分回放还是删除，1是回放，2是删除
    final private int BACKPLAY = 1;
    final private int DELETE = 2;

    private int REALTIME_THREADMSG_READDATAS = 1; // 读取雷达数据消息
    private int REALTIME_THREADMSG_SDCARDINFS = 2; // 读取存储空间剩余，同时处理了电池电量
    private int REALTIME_THREADMSG_OVERSPEED = 3; // 超速报警
    private int REALTIME_THREADMSG_POWER = 4; // 读取电池电量
    private int REALTIME_THREADMSG_GPS = 5; // 读取GPS的数据频率20HZ
    private int SYSTEM_TIME = 6; // 每秒更新系统时间一次
    private int MAXFULLBATTERYTIME = 4800; // 最大满电时间
    private int MINFULLBATTERYTIME = 0; // 最小满电时间
    private int WARNBATTERYLEVEL = 10; // 提示的电量百分比
    private static final String FileName = "setFactoryParams"; // 文件名称
    private static final String PackageName = "com.example.setsteptime";
    private static final String KEY = "stepelapsed";
    private static int MODE = Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE;
    private Bitmap mBitmap;
    // 日志文件
    private LogWriter mLog = null;

    //    private Network mNetwork = null;
    private NetworkDevice mNetwork = null;

    public NetworkDevice getNetwork() { return mNetwork; }

    // private Lock mLock = new ReentrantLock();

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        // TODO Auto-generated method stub
        Log.e(TAG, Log.getStackTraceString(ex));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE); // 隐藏标题栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                  WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
                             WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        getWindow().setAttributes(params);
        super.setContentView(R.layout.activity_main); // 默认布局管理器

        mApp = (MyApplication) this.getApplicationContext();
        mApp.mRadarDevice.setContext(this);
        mApp.mRadarDevice.newPositiveData();
        mApp.mRadarDevice.mMainActivity = this;

        if (mApp.mPowerDevice.openPowerDevice() < 0)
            DebugUtil.i(TAG, "powerDevice<0");
        else
            DebugUtil.i(TAG, "powerDevice>=0");

        // 打开电源
        // devicePowerUp();

        System.loadLibrary("Detect");

        // //实时处理视图
        mRealTimeDIBView = (realTimeDIBView) findViewById(R.id.layoutRealView);
        mRealTimeDIBView.setZOrderOnTop(true); // 0531

        mScanView = (scanView) findViewById(R.id.viewSinglewave);
        mScanView.setZOrderOnTop(true);

        // mScanView = new scanView(this);
        // //回放处理视图
        mbackPlayDIBView = (backPlayDIBView) findViewById(R.id.layoutBackPlayView);
        mbackPlayDIBView.mParentActivity = this;
        mbackplayScanView = (BackplayScanView) findViewById(R.id.backplayScanview);

        // //记录各种标尺
        // 实时采集时的标尺
        mApp.mTimewndRuler = findViewById(R.id.viewVTWRuler);
        mApp.mDeepRuler = findViewById(R.id.viewDDRuler);
        mApp.mHorRuler = findViewById(R.id.viewHRuler);
        // 2016.6.10 回放数据时的标尺
        mApp.mBTimewndRuler = findViewById(R.id.viewBVTWRuler);
        ((BackPlayVRulerView) (mApp.mBTimewndRuler)).setShowTimewndType();
        mApp.mBDeepRuler = findViewById(R.id.viewBDDRuler);
        ((BackPlayVRulerView) (mApp.mBDeepRuler)).setShowDeepType();
        mApp.mBHorRuler = findViewById(R.id.viewBHRuler);
        ((BackPlayHRulerView) (mApp.mBHorRuler)).mApp = mApp;
        mApp.mMainActivity = this;

        // 系统时间
        textview_systemtime = (TextView) findViewById(R.id.textview_systemtime);

        // 设置水平标尺的左右空白宽度
        HRulerView hRView = (HRulerView) mApp.mHorRuler;
        hRView.mApp = mApp;

        // 得到屏幕的宽度和高度
        int sW, sH;
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        sW = dm.widthPixels;
        sH = dm.heightPixels;
        mApp.setScreenRange(sW, sH);
        // 获得背景图片1019
        /*
         * WallpaperManager manager = WallpaperManager.getInstance(this);
         * BitmapDrawable drw = (BitmapDrawable)manager.getDrawable();
         * this.mBitmap = drw.getBitmap();
         */

        filePathString = mApp.mRadarDevice.getStoragePath();
        // 设置回放
        setSelectplaybackPopwindow();
        // 设置回放参数
        setBackPlayPopupWindow();
        // 设置超速报警弹窗
        setOverSpeedPopupWindow();
        // 开启网路线程
        createNetwork();
        // 开启实时线程hss0427
        createRealtimeThread();
        // 开启时间电量hss427
        createStateThread();
        // 开启GPS端口
        // openGPSDevice();
        backupBackground();
        // 获得fragment实例
        fragLeft = new LeftFragment();

        startRadar();

        fragRight = new RightFragment();

        // 开机读取一次电池电量
        changePowerInfs();

        // 读取GPS串口信息
        // GPSDevice gpsDevice = new GPSDevice();
        // gpsDevice.openSerialPort("/dev/ttySAC0",115200,'N',8, 1, 0);//开启串口

        /*
         * SerialPort srlport = new SerialPort(); DebugUtil.i(TAG,
         * "开启GPS串口数据！"); srlport.openSerialPort("/dev/ttySAC0",115200,'N',8, 1,
         * 0);//开启串口
         */

        // 将三个指示灯都打开
        // mApp.mPowerDevice.PowerLightOn();
        // mApp.mPowerDevice.WorkLightOn();
        // mApp.mPowerDevice.BatLightOn();

        // DisplayMetrics dmc = new DisplayMetrics();
        // getWindowManager().getDefaultDisplay().getMetrics(dmc);
        // String strResolution = "Resolution:"+dmc.widthPixels
        // +"*"+dmc.heightPixels;
        // showToastMsg(strResolution);

        // 生成日志文件
        try {
            mLog = LogWriter.open();
            mLog.beginLogcat();
            mLog.beginDmesg();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private boolean radarStart(int frqIndex) {
        int ret;
        boolean bRet = false;
        // 如果雷达处于工作状态
        if (mApp.mRadarDevice.isRunningMode()) {
            DebugUtil.i(TAG, "radarStart : device has start!");
            return true;
        }
        // 装载驱动
        // loadDriver();

        // 根据选择的天线频率，装载指定的参数文件
        String fileName;
        fileName = mApp.mRadarDevice.getInnerStoragePath() +
                   mApp.mRadarDevice.mParamsFilefolderPath;
        fileName += radarDevice.g_antenFrqStr[frqIndex] + ".par";
        // Toast.makeText(this, fileName, Toast.LENGTH_SHORT).show();

        /**
         * 由主频参数文件设置参数
         */
        mApp.mRadarDevice.setAntenFrq(frqIndex);
        if (!mApp.mRadarDevice.onlyLoadParamsFromeFile(fileName)) {
            // DebugUtil.i(TAG,"!!!!!~~~~Now setAntenFrq:"+frqIndex);
            // Toast.makeText(this, "~~~~Now AntenFrq:"+frqIndex,
            // Toast.LENGTH_SHORT).show();

            // 根据天线频率索引，仅仅更改参数，不发送命令
            mApp.mRadarDevice.changeParamsFromeAntenfrq(frqIndex);
        } else {
        }

        if (!mApp.mRadarDevice.loadSystemSetFile()) {
            System.out.println("加载系统设置参数失败");
        } else {
            System.out.println("加载系统设置参数成功");
        }

        /**
         * 由默认测距轮文件设置测距仪参数
         */
        if (!mApp.mRadarDevice.loadDefaultWhellcheckParams()) {
            DebugUtil.i(TAG, "loadDefaultWheelCheckParams fail!");
            mApp.mRadarDevice.changeWheelPropertyFromAnteFrq(frqIndex);
        } else {
            DebugUtil.i(TAG, "loadDefaultWheelCheckParams!Success!");
        }

        DebugUtil.i(TAG, "radarstart loaddefaultcheck,extendNumber=" +
                         mApp.mRadarDevice.getWheelExtendNumber());
        DebugUtil.i("radarDevice", "3.leftgetWheeltype" + mApp.mRadarDevice.getWheeltypeSel());
        // 更新参数设置显示值
        int nowSel = mApp.mRadarDevice.getAntenFrqSel();
        // DebugUtil.i(TAG,"!!!!!Now AntenFrq:"+nowSel);
        // Toast.makeText(this, "!!!!!~~~~Now AntenFrq:"+nowSel,
        // Toast.LENGTH_SHORT).show();
        mApp.mRadarDevice.refreshFileHeader();
        // 根据当前的雷达参数设置参数列表框内容？？？
        // changeParamsListFromeRadar();
        // hss2016.6.6
        // 更新列表
//        mApp.mTimewndRuler.invalidate();
//        mApp.mDeepRuler.invalidate();
//        ((HRulerView) mApp.mHorRuler).setShowscanMode();

        // mParamsListAdapter.notifyDataSetChanged();

        /*
         * //装载测距轮校正参数文件 fileName =
         * mApp.mRadarDevice.mSDCardPath+mApp.mRadarDevice
         * .mParamsFilefolderPath; fileName +=
         * mApp.mRadarDevice.mWhellcheckFilename;
         * if(!mApp.mRadarDevice.loadWhellcheckParams(fileName)) {
         *
         * }
         */
        // 开启雷达
        ret = mApp.mRadarDevice.start();
        if (ret == LeftFragment.RADARDEVICE_ERROR_NO) {
            // mRealtimeDIBView.initDIB();
            // mRealtimeDIBView.invalidate();
//            mRealthreadReadingDatas = true; // 设置标志,开始读取数据
            bRet = true;

            // //更新增益曲线显示放到主activity
            // scanView view = (scanView)findViewById(R.id.viewSinglewave);
            // view.invalidate();

            String name = mApp.mRadarDevice.getParamsPath() + "defSetParams.par";
            // loadSetParamsFile(name); 参数文件

            // BB80?20170419hss
            // mApp.mRadarDevice.setHandleMode();

            // iv_state.setBackgroundResource(R.drawable.greenpoint);
        } else {
            Log.d("debug_radar", "open radar failed");
            if (ret == LeftFragment.RADARDEVICE_ERROR_OPEN) {
//                showToastMsg("雷达设备打开错误!");
                Log.d("debug", "雷达设备打开错误");
            }
            if (ret == LeftFragment.RADARDEVICE_ERROR_STARTCOMMAND) {
//                showToastMsg("发送开启命令错误!");
                Log.d("debug", "发送开启命令错误");
            }
            // UnloadDriver();
        }

        return bRet;
    }

    private void radarRealStart() {
        mApp.mRadarDevice.mIsUseSoftPlus = false;
        // 分时上电
        // 上电后读取串口
        // 读取天线芯片的串口信息

        try {
            devicePowerUp();
        } catch (Exception e) {
            DebugUtil.i(TAG, "分时上电sleep run fail_sleep!");
            Log.d("debug_radar", "设备上电错误");
        }

        boolean bRet = radarStart(8);
        if (!bRet) {
//            showToastMsg("开启雷达错误!");
            Log.d("debug_start_radar", "开启雷达错误");
        } else {
            Log.d("debug_radar", "开启雷达成功");
            mApp.mRadarDevice.continueShow();
            /*
             * tv_antenna = (TextView)view1.findViewById(R.id.id_antenna);
             * tv_antenna.setText(freqStr);
             */
            // 开启工作灯0315
            Log.d("debug_radar", "before work light on");
            if (mApp.mPowerDevice.WorkLightOn()) {
                Log.d("debug_radar", "work light on success");
            } else {
                Log.d("debug_radar", "work light on fail");
                DebugUtil.i(TAG, "工作灯开启失败！");
            }
        }
    }

    private void startRadar() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mApp.mRadarDevice.setAntenFrq(8);
                radarRealStart();
                mApp.setRealThreadStop(false);
                mApp.setRealThreadReadingDatas(true);
                mApp.mRadarDevice.setHardplus(new float[]{5, 5, 5, 5, 5, 5, 5, 5, 5});
            }
        }).start();
    }

    /*
     * 设备上电
     */
    private void devicePowerUp() {
        mApp.mPowerDevice.AntennaPowerUp();// 天线上电
        mApp.mPowerDevice.DisplayPowerUp();// 高压置高
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mApp.mPowerDevice.StepPowerUp();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mApp.mPowerDevice.DSPPowerUp();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 恢复状态
     */
    public void backupBackground() {
        Context c = null;
        // 加载步进时间
        // 读取设置
        try {
            c = MultiModeLifeSearchActivity.this.createPackageContext(PackageName,
                                                                      Context.CONTEXT_IGNORE_SECURITY);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        if (c != null) {
            SharedPreferences share = c.getSharedPreferences(FileName, MODE);
            SharedPreferences.Editor edit = share.edit();
            boolean bComitted = edit.commit();
            if (!bComitted) {
                return;
//                throw new RuntimeException("(AndroidApplication) Unable to save new string.");
            } else
                ;

            if (share.contains(KEY)) {
                mApp.mRadarDevice.setDelayTime(share.getFloat(KEY, 4.4f));
            } else {
                DebugUtil.i(TAG, " 没找到KEY！IDSC2600/sharedpref！");
                edit.putFloat(KEY, 4.4f);
                boolean ret_edit = edit.commit();
                if (!ret_edit) {
                    Toast.makeText(this, "commit失败！", Toast.LENGTH_SHORT).show();
                } else
                    ;
            }
        } else
            ;
        // 加载默认状态，测距轮反转状态和背光灯状态
        mApp.getLightState();
        mApp.getTurnWheelState();
        mApp.getFileDscend();

        // 根据背光灯状态，打开或者关闭背光灯
        if (mApp.getPowerLightState()) {
            mApp.mPowerDevice.PowerLightOn();
        } else {
            mApp.mPowerDevice.PowerLightOff();
        }
    }

    Thread mRealThread;

    /**
     * 创建实时读取的线程
     */
    public void createRealtimeThread() {
        DebugUtil.i("MainActivity", "createRealtimeThread");
        RealtimeRunnable realRunnable = new RealtimeRunnable(this);
        mRealThread = new Thread(realRunnable);
        mRealThread.start();
    }

    public void createNetwork() {
        //        mNetwork = new Network("192.168.254.100", 5000);
        mNetwork = new NetworkDevice("192.168.43.10", 5000, this);
        //        mNetwork.setRadarDevice(mApp.mRadarDevice);
        //        mNetwork.setDetect(mApp.mRadarDevice.getDetect());
    }

    // 创建状态更新线程
    public void createStateThread() {
        createTimeThread();
        createBatteryThread();
    }

    // 系统时间显示
    private void createTimeThread() {
        DebugUtil.i(TAG, "createSystemTimeThread");
        TimeThread timeThread = new TimeThread();
        Thread thread = new Thread(timeThread);
        thread.start();
    }

    // 电量显示
    private void createBatteryThread() {
        BatteryThread batThread = new BatteryThread();
        Thread threadbat = new Thread(batThread);
        threadbat.start();
    }

    // 打开GPS端口，从串口接收数据
    public void openGPSDevice() {
        gpsport.setContext(this);
        gpsport.openSerialPort("/dev/ttySAC1", 115200, 'N', 8, 1, 0);// 开启串口
    }

    // 关闭GPS端口读取
    public void closeGPSDevice() {
        gpsport.closeSerialPort();
    }

    // 关闭工具条，从zhzhw处拿来
    public void closeBar(Context context) {
        try {
            // 需要root 权限
            Build.VERSION_CODES vc = new Build.VERSION_CODES();
            Build.VERSION vr = new Build.VERSION();
            String ProcID = "79";

            if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH) {
                ProcID = "42"; // ICS AND NEWER
            }

            // 需要root 权限
            Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c",
                                                                  "service call activity " +
                                                                  ProcID +
                                                                  " s16 com.android.systemui\n" +
                                                                  "exit\n"}); // WAS
            // 79
            proc.waitFor();
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
        WallpaperManager manager = WallpaperManager.getInstance(this);

        /*
         * try { manager.setBitmap(mBitmap); } catch (IOException e) { // TODO
         * Auto-generated catch block e.printStackTrace(); }
         */

    }

    // 设置加载横屏
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else
            ;
        closeBar(this);
    }

    // 设置全屏显示
    private void full(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    // toast弹出
    public void showToastMsg(String txt) {
        Toast.makeText(this, txt, Toast.LENGTH_SHORT).show();
    }

    // 实时线程
    private int mrLen = 0;

    class RealtimeRunnable implements Runnable {
        Context mContext; // 父视图

        public RealtimeRunnable(Context context) {
            mContext = context;
        }

        @Override
        public void run() {
            DebugUtil.i("TimeActivity", "RealtimeThread");
            long sleepTime;
            int changeInfsDelayNumber = 0;

            MultiModeLifeSearchActivity activity;
            activity = (MultiModeLifeSearchActivity) mContext;
            // Log.d("debug_real_time", "first value: " +
            // mApp.isRealThreadStop());

            long st, et;
            int scans;
            // 没有要求停止线程的情况下
            while (!mApp.isRealThreadStop()) {
                scans = 0;
                sleepTime = 200;
                // Log.d("debug_real_time", "second value: " +
                // mApp.isRealThreadReadingDatas());
                // //如果要求读取雷达数据
                st = System.nanoTime();
                if (mApp.isRealThreadReadingDatas()) {
                    sleepTime = mApp.getRealthreadSleepTime();
                    try {
                        // 读取数据
                        mrLen = 0;
                        long start1 = System.currentTimeMillis();
                        mrLen = mApp.radarReadMaxWaveDatas();
                        long endread = System.currentTimeMillis();
                        // DebugUtil.i(TAG, "0 after radarReadMaxWaveDatas"
                        // + String.valueOf(endread-start1));
                        scans = mrLen / 2 / mApp.mRadarDevice.getScanLength();

                        if (mApp.mRadarDevice.isTempstopShow()) {
                            DebugUtil.i(TAG, "is Temp stopShow!");
                        } else {
                            // DebugUtil.i(TAG, "is not Temp stopShow!");
                            // 计算位图
                            // start1 = System.currentTimeMillis();
                            // if (mApp.mRadarDevice.isDIBShow())
//                             mRealTimeDIBView.changeDatasToDIB(mApp.GetRadarDatasBuf(),
//                             mrLen);
                            // if (mApp.mRadarDevice.isWiggleShow())
                            // mRealTimeDIBView.changeDatasToWiggle(mApp.GetRadarDatasBuf(),
                            // mrLen);
                            // long end1 = System.currentTimeMillis();
                            // DebugUtil.i(TAG,
                            // "1 after mRealTimeDIBView.changeDatasToDIB"
                            // + String.valueOf(end1-start1));
//                            start1 = System.currentTimeMillis();

                            // 发送信号
                            Message msg = new Message();
                            msg.arg1 = REALTIME_THREADMSG_READDATAS;
                            mRealtimeThreadHandler.sendMessage(msg); // hss0425

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        DebugUtil.i("TimeActivity", "RealtimeThread run fail_1!");
                    }
                }
                // //休眠线程
                else {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (Exception e) {
                        DebugUtil.i("TimeActivity", "RealtimeThread run fail_sleep!");
                    }
                }

                // 定时更新的
                changeInfsDelayNumber++;
                if (changeInfsDelayNumber >= 15)// hss 20180509定时刷新
                {
                    changeInfsDelayNumber = 0;
                    updateMemory();// 更新存储
                    // //存储卡剩余空间
                    try {

                        Message msg = new Message();
                        msg.obj = activity;
                        msg.arg1 = REALTIME_THREADMSG_SDCARDINFS;
                        mRealtimeThreadHandler.sendMessage(msg);

                        // 尝试使用jni回调存储空间
                        // StorageManager mStorageManager = (StorageManager)
                        // mContext.getSystemService(Context.STORAGE_SERVICE);
                        // StorageVolume[] storageVolumes =
                        // mStorageManager.getVolumeList();
                    } catch (Exception e) {
                        DebugUtil.i(TAG, "RealtimeThread run fail_5!");
                    }
                }

                // 检测是否超速
                if (mApp.mRadarDevice.isOverSpeed()) {
                    DebugUtil.i(TAG, "Whell speed has over!");
                    try {
                        Message msg = new Message();
                        msg.obj = activity;
                        msg.arg1 = REALTIME_THREADMSG_OVERSPEED;
                        msg.arg2 = 1;
                        mRealtimeThreadHandler.sendMessage(msg);
                    } catch (Exception e) {
                        DebugUtil.i(TAG, "RealtimeThread run fail_6!");
                    }
                } else {
                    try {
                        Message msg = new Message();
                        msg.obj = activity;
                        msg.arg1 = REALTIME_THREADMSG_OVERSPEED;
                        msg.arg2 = 0;
                        mRealtimeThreadHandler.sendMessage(msg);
                    } catch (Exception e) {
                        DebugUtil.i(TAG, "RealtimeThread run fail_7!");
                    }
                }
                et = System.nanoTime();
                long costTime = (et - st) / 1000000;
                if (costTime < 20L) {
                    try {
                        Thread.sleep(20L - costTime);
                    } catch (Exception e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                }
            }
        }
    }

    // 系统时间,每秒更新一次
    class TimeThread extends Thread {
        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = SYSTEM_TIME; // 消息（一个整型值）
                    mStateHandler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } while (true);
        }
    }

    /**
     * @author huangss 电池电量读取
     */
    class BatteryThread extends Thread {
        @Override
        public void run() {
            do {
                try {
                    Message msg = new Message();
                    msg.what = REALTIME_THREADMSG_POWER; // 消息（一个整型值）
                    mStateHandler.sendMessage(msg);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }

    /**
     * 处理发送的信号
     */
    public Handler mRealtimeThreadHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // DebugUtil.i("TimeActivity",
            // "mRealtimeThreadHandler handleMessage");
            // og.d("TimeActivity",
            // "mRealtimeThreadHandler handleMessage:"+msg.arg1);
            /**
             * 处理读取雷达数据消息
             */
            if (msg.arg1 == REALTIME_THREADMSG_READDATAS) {

                // 0427hss
                if (mrLen > 0) {
                    // 先更新左侧listviewhss0714
                    // LeftFragment.mRadarParamAdapter.notifyDataSetChanged();

                    // 更新位彩色图,移动左侧参数的时候不更新hss0425
                    // if (mApp.isDraw()) {
                    // // 0427hss
                    // drawDIB(mrLen);
                    // } else
                    // ;
                } else {
                    if (mApp.mRadarDevice.isBackOrientMode())
                        ;
                        // mRealTimeDIBView.invalidate();
                    else
                        ; // 0522
                }
                // 设置已经采集的道数信息
                setHasReceiveScansInf();
            }

            // /处理sd卡容量信息
            if (msg.arg1 == REALTIME_THREADMSG_SDCARDINFS) {
                // Log.d("TimeActivity","start sd");
                long start1 = System.currentTimeMillis();

                changeMemoryInfs();

                long end1 = System.currentTimeMillis();

                // Log.d("TimeActivity","end sd" + (end1-start1));
            }
            // 处理超速信息
            if (msg.arg1 == REALTIME_THREADMSG_OVERSPEED) {
                if (msg.arg2 == 1)
                    beginOverspeedAlarm();// 发送超速报警信号
                else if (msg.arg2 == 0)
                    stopOverspeedAlarm();// 结束超速报警信号
            }
            // 处理GPS数据保存

            // Log.d("TimeActivity",
            // "end  mRealtimeThreadHandler handleMessage:"+msg.arg1);

        }

        /*
         * 根据选择对采集的数据画堆积波或者伪彩图
         */
        private void drawDIB(int rLen) {
            // DebugUtil.i("KTAG", "Main isDraw!");
            long start1 = System.currentTimeMillis();

            // mRealTimeDIBView.invalidate(); //0522
            long end1 = System.currentTimeMillis();
            DebugUtil.i(TAG,
                        "2 after mRealTimeDIBView.invalidate()" + String.valueOf(end1 - start1));
            // 更新单道波形
            start1 = System.currentTimeMillis();
            // mScanView.invalidate(); 0522

            end1 = System.currentTimeMillis();
            DebugUtil.i(TAG, "3 after mScanView.invalidate()" + String.valueOf(end1 - start1));
            // 更新水平标尺
            start1 = System.currentTimeMillis();
            mApp.mHorRuler.invalidate();
            end1 = System.currentTimeMillis();
            DebugUtil.i(TAG, "4 after mHorRuler.invalidate()" + String.valueOf(end1 - start1));
        }
    };

    /**
     * 处理时间
     */
    private Handler mStateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == SYSTEM_TIME) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                String time = df.format(new Date());
                textview_systemtime.setText(time);
            }

            if (msg.what == REALTIME_THREADMSG_POWER) {
                changePowerInfs();
                mLog.writeDmesg();
            }
        }
    };

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        DebugUtil.i(KTAG, "Main onKeyUp,keyCode:=" + keyCode);

        // 切换键
        if (keyCode == KeyEvent.KEYCODE_TAB) {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                isTabPressed = false;
                DebugUtil.i(KTAG, "Main onKeyUp tab!");
                // //2016.6.10 : 正在回放时，不可回到采集模式
                if (mIsBackplaying) {
                    DebugUtil.i(KTAG, "Main isPlayBackMode =" + mApp.mRadarDevice.getNowMode());
                    // 弹出功能选择对话框
                    showBackPlayPopupWindow();
                    // return false;
                } else if (!mApp.mRadarDevice.isSavingMode()) {
                    if (isMaxShowRealtimeView()) {
                        normalShowRealtimeView();
                        mApp.mListView.requestFocus();
                    } else {
                        maxShowRealtimeView();
                    }
                }
            }
        }
        // 记录F3键的状态
        else if (keyCode == KeyEvent.KEYCODE_F3) {
            // 截屏
            // saveBitmapForSdCard();

            if (event.getAction() == KeyEvent.ACTION_UP) {
                // 截屏
                // saveBitmapForSdCard();

                // 正在保存 暂停保存|继续保存
                if (mApp.mRadarDevice.isSavingMode()) {
                    if (mApp.mRadarDevice.isTemstopSaveMode()) {
                        mApp.mRadarDevice.continueSave();
                    } else {
                        mApp.mRadarDevice.tempStopSave();
                    }
                    return true;
                }
                // 正在回放状态
                if (this.mbackPlayDIBView.isBackPlaying()) {
                    boolean isTemStop = mbackPlayDIBView.isBackplayPause();
                    isTemStop = !isTemStop;
                    mbackPlayDIBView.setBackplayPauseStatus(isTemStop);
                    mIsTempstopBackplay = isTemStop;
                    return true;
                }
                // 平常状态
                if (!mApp.mRadarDevice.isSetting_Command()) {
                    // /没有保存，暂停显示 | 继续显示
                    if (mApp.mRadarDevice.isTempstopShow()) {
                        DebugUtil.i(TAG, "isTempstopShow to continueShow!");
                        mApp.mRadarDevice.continueShow();
                    } else {
                        mApp.mRadarDevice.tempStopShow();
                    }
                    return true;
                }
            }
            return false;
        }
        // 保存键
        else if (keyCode == KeyEvent.KEYCODE_F2) {
            Log.d("debug", "按下保存键");
            if (event.getAction() == KeyEvent.ACTION_UP) {
                if (mApp.mRadarDevice.isSavingMode()) {
                    stopSave();
                    normalShowRealtimeView();
                    mApp.mListView.requestFocus();
                    try {
                        mLog.print(TAG, "保存完毕!");
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    Log.d("debug", "没有处于保存状态");
                    if (!mApp.mRadarDevice.isRunningMode()) {
                        Log.d("debug", "雷达未开启");
                        Toast.makeText(this, "雷达未开启！", Toast.LENGTH_SHORT).show();
                    } else if (mApp.mRadarDevice.isSetting_AllHardPlus_Command() ||
                               mApp.mRadarDevice.isSetting_Scanspeed_Command() ||
                               mApp.mRadarDevice.isSetting_StepHardPlus_Command()) {
                        Log.d("debug", "is setting");
                    } else {
                        if (beginSave())// 如果保存成功，将采集界面最大化
                        {
                            maxShowRealtimeView();
                            try {
                                mLog.print(TAG, "开始保存!");
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                mLog.print(TAG, "开启保存，但是保存失败");
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }// 如果保存失败，不做处理
                    }
                }
            }
        }
        // 打小标
        else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            DebugUtil.i("Left", "onKeyUp");

            if (this.isMaxShowBackPlayView()) {
                if (this.mbackPlayDIBView.isBackPlaying()) {
                    this.mbackPlayDIBView.setBackplayDirForward();
                } else
                    ;
            } else if (mApp.mRadarDevice.isDianCeMode() && mApp.mRadarDevice.isRunningMode()) {
                DebugUtil.i("diance", "0.onceDianCe");
                mApp.mRadarDevice.onceDianCe();
            } else {
                DebugUtil.i("diance", "1.else");
                // 不在设置参数、保存、全屏采集的情况下返回主菜单
                if (!mApp.mRadarDevice.isSetting_AllHardPlus_Command() &&
                    !mApp.mRadarDevice.isSavingMode() &&
                    !mApp.mRadarDevice.isSetting_StepHardPlus_Command()) {
                    DebugUtil.i("diance", "2.不设置参数etc");
                    fragLeft.onKeyDown(keyCode, event, this);
                    return false;
                } else if (mApp.mRadarDevice.isSavingMode()) {
                    mApp.mRadarDevice.smallMark();
                } else {
                    DebugUtil.i("diance", "3.其它！");
                }
            }
        }
        // 打大标
        else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (this.isMaxShowBackPlayView()) {
                if (this.mbackPlayDIBView.isBackPlaying()) {
                    this.mbackPlayDIBView.setBackplayDirBack();
                } else
                    ;
            } else {
                mApp.mRadarDevice.bigMark();
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (this.isMaxShowBackPlayView()) {
                int speed;
                speed = this.mbackPlayDIBView.getBackplaySpeed();
                speed += 10;
                this.mbackPlayDIBView.setBackplaySpeed(speed);
            } else
                ;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (this.isMaxShowBackPlayView()) {
                DebugUtil.i(TAG, "maxbackplayview!");

                DebugUtil.i(TAG, "setPlayBackView no show!");
                int speed;
                speed = this.mbackPlayDIBView.getBackplaySpeed();
                speed -= 10;
                if (speed <= 1)
                    speed = 1;
                this.mbackPlayDIBView.setBackplaySpeed(speed);

            } else
                ;
        }
        // 回放
        else if (keyCode == KeyEvent.KEYCODE_F4) {
            DebugUtil.i(TAG, "OnKeyUp:F4 back");
            mResponFlag = this.BACKPLAY;

            // 关闭工作灯
            /**
             * 1.停止雷达，弹出文件选择 2.加载文件，全局显示，设置在回放的状态标志 3.焦点在增益上
             */
            if (this.isMaxShowBackPlayView()) {
                // 将设置状态还原
                resetPlayBack();
                this.normalShowBackPlayView();
                mApp.mListView.requestFocus();
            } else {
                if (mApp.mRadarDevice.isSavingMode())
                    ;// 保存时不回放
                    // 0619如果是点测调节状态不回放
                    // else if(mApp.mRadarDevice.isDianCeMode() );
                else {
                    // 首先停止正在运行的雷达
                    if (mApp.mRadarDevice.isRunningMode()) {
                        radarStop();
                        if (mApp.getLeftFragmentTab() == 0) {
                            View view1 = LeftFragment.mexpListView.getChildAt(0);
                            ImageView iv_state = (ImageView) view1.findViewById(R.id.imgv_state);
                            iv_state.setBackgroundResource(R.drawable.redpoint);
                        } else
                            ;
                    } else
                        ;
                    // 弹窗选择加载文件
                    // getStorageFolderPath();
                    this.showSelectplaybackPopwindow();
                }// 不保存响应回放
            }
        }
        // 删除键
        else if (keyCode == KeyEvent.KEYCODE_DEL) {
            // //2016.6.10 轮测回退模式下，提问是否丢弃回退数据
            if (mApp.mRadarDevice.isRunningMode() && mApp.mRadarDevice.isWhellMode() &&
                mApp.mRadarDevice.isBackOrientMode()) {
                discardDataDialog();
                // mApp.mRadarDevice.discardBackDatas();
            } else
                // //
                if (!isMaxShowBackPlayView()) {
                    if (mApp.mRadarDevice.isSavingMode())
                        ;// 保存时不回放
                    else {
                        DebugUtil.i(TAG, "OnKeyUp:Delete");
                        this.mResponFlag = DELETE;
                        showBackPlayFilePath();
                    }
                } else {
                    mBackplayFocusView = !mBackplayFocusView;
                    if (mBackplayFocusView) {
                        // fragRight.onKeyUp(keyCode,event,this);
                        // return false;
                    } else
                        ;
                }
        }
        // TODO Auto-generated method stub
        return super.onKeyUp(keyCode, event);
    }

    /**
     * 屏幕截图保存到内存卡
     * <p>
     * //     * @param bitName
     * //     * @param mBitmap
     */
    public void saveBitmapForSdCard() {
        // 创建file对象
        getWindow().getDecorView().setDrawingCacheEnabled(true);
        Bitmap bmp = getWindow().getDecorView().getDrawingCache();
        long time = System.currentTimeMillis();
        File f = new File("/mnt/sdcard/" + bmp + "_" + time + ".png");
        try {
            // 创建
            f.createNewFile();
        } catch (IOException e) {

        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 原封不动的保存在内存卡上
        bmp.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        showToastMsg("截屏保存到sdcard！");
    }

    public boolean isTabPressed = false;

    void exitActivity() {
        Log.d(TAG, "exit activity");
        mNetwork.stopNetTransfers();
        mNetwork.waitTransfersAllStop();
        mApp.mRadarDetect.close();
        mApp.mRadarDevice.saveSystemSetFile();
        MultiModeLifeSearchActivity.this.finish();
        exit(0);
    }

    private void performExitApp() {
        mApp.mRadarDevice.stop();
        mApp.setRealThreadStop(true);
        mApp.mPowerDevice.closePowerDevice();
        mApp.setRunFirstHardplusThread(false);
        try {
            mLog.print(TAG, "退出采集软件！");
            mLog.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        MultiModeLifeSearchActivity.this.exitActivity();
        // System.exit(0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        DebugUtil.i(KTAG, "Main onKeyDown,keyCode:=" + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_TAB:
                isTabPressed = true;
                return false;
            case KeyEvent.KEYCODE_BACK:
                if (mApp.mRadarDevice.isSavingMode())
                    ;// || mApp.isCustomSetting());//保存时不退出
                else if (mIsBackplaying)// 是否在回放
                {
                    // 将设置状态还原
                    resetPlayBack();
                    this.normalShowBackPlayView();
                    mApp.mListView.requestFocus();
                    // fragLeft.onKeyDown(keyCode,event,this); //调用left处理返回
                } else if (isMaxShowRealtimeView())// 是否在全屏显示
                {
                    normalShowRealtimeView();
                    mApp.mListView.requestFocus();
                } else {
                    if (mApp.getLeftFragmentTab() == 0 && isTabPressed) {
                        // 弹出对话框询问
                        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("是否退出应用？").setMessage("是否退出采集软件？").setPositiveButton("确定",
                                                                                              new DialogInterface.OnClickListener() {
                                                                                                  @Override
                                                                                                  public void onClick(
                                                                                                          DialogInterface dialog,
                                                                                                          int whichButton) {
                                                                                                      performExitApp();
                                                                                                  }
                                                                                              })
                               .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                   @Override
                                   public void onClick(DialogInterface dialog, int which) {
                                       // TODO Auto-generated method stub
                                   }
                               }).show();
                    }
                    fragLeft.onKeyDown(keyCode, event, this);
                }
                return false;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        // TODO Auto-generated method stub
        DebugUtil.i(TAG, "onKeyMultiple!");

        return super.onKeyMultiple(keyCode, repeatCount, event);
    }

    private View wheelExtendView;
    private PopupWindow mWheelExtendWindow;
    private int mWheelTypeNumber = 10;
    private int[] mWheeltypeRadiosID = new int[mWheelTypeNumber];

    public void setWheelExtendWindow() {
        wheelExtendView = View.inflate(this, R.layout.layout_whellparams, null);
        // //生成轮测控制视图
        int wheelExtendNumber = 0;
        wheelExtendNumber = mApp.getWheelExtendNumber();
        mWheelExtendWindow = new PopupWindow(wheelExtendView, 280,// LayoutParams.WRAP_CONTENT,
                                             android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        mWheelExtendWindow.setFocusable(true);
    }

    public CheckBox.OnCheckedChangeListener mCheckboxOnCheckedChangeListener
            = new CheckBox.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
            // TODO Auto-generated method stub
            int id;
            id = arg0.getId();
            MyApplication app;
            app = mApp;
            //
            switch (id) {
                case R.id.checkbox_turnwhell:
                    app.mRadarDevice.turnWhell(arg1);
                    break;
            }
        }
    };
    // //
    public EditText.OnKeyListener mEditOnKeyListener = new EditText.OnKeyListener() {
        @Override
        public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
            // TODO Auto-generated method stub
            // mMainActivity.showToastMsg(""+arg2.getKeyCode());
            return false;
        }

    };

    // ///////////////////////////
    public boolean mIsMaxshowRealtimeView = false;

    public boolean isMaxShowRealtimeView() {
        return mIsMaxshowRealtimeView;
    }

    /**
     * 正常比例显示实时采集界面
     */
    public void normalShowRealtimeView() {
        DebugUtil.i(TAG, "enter normalShowRealtimeView!");
        RelativeLayout lFrag;
        LinearLayout.LayoutParams params;

        // 显示左侧 控制区域
        lFrag = (RelativeLayout) findViewById(R.id.left_fragment);
        params = (android.widget.LinearLayout.LayoutParams) lFrag.getLayoutParams();
        params.weight = (float) 0.25;
        lFrag.setLayoutParams(params);
        lFrag.setVisibility(View.VISIBLE);

        // 隐藏回放模式 单道波形 视图
        this.mbackplayScanView.setVisibility(View.INVISIBLE);

        // 显示 实时采集 单道波形 视图
        mScanView.setVisibility(View.VISIBLE);

        // 设置右侧显示区域
        lFrag = (RelativeLayout) findViewById(R.id.right_fragment);
        params = (android.widget.LinearLayout.LayoutParams) lFrag.getLayoutParams();
        params.weight = (float) 0.25;
        lFrag.setLayoutParams(params);
        lFrag.setVisibility(View.VISIBLE);

        LinearLayout mFrag;
        LinearLayout.LayoutParams layoutParam;

        // 显示 实时采集 时窗标尺
        mFrag = (LinearLayout) findViewById(R.id.layout_VTWRuler);
        mFrag.setVisibility(View.VISIBLE); // 2016.6.10
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.1;
        mFrag.setLayoutParams(layoutParam);

        // 隐藏 回放模式 时窗标尺 2016.6.10
        mFrag = (LinearLayout) findViewById(R.id.layout_BVTWRuler);
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.0;
        mFrag.setLayoutParams(layoutParam);
        mFrag.setVisibility(View.INVISIBLE);

        // 显示 实时采集 深度标尺
        mFrag = (LinearLayout) findViewById(R.id.layout_VDDRuler);
        mFrag.setVisibility(View.VISIBLE); // 2016.6.10
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.1;
        mFrag.setLayoutParams(layoutParam);
        // 隐藏 回放模式 深度标尺 2016.6.10
        mFrag = (LinearLayout) findViewById(R.id.layout_BVDDRuler);
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.0;
        mFrag.setLayoutParams(layoutParam);
        mFrag.setVisibility(View.INVISIBLE);

        // //2016.6.10 显示实时采集 水平标尺，隐藏 回放模式 水平标尺
        mFrag = (LinearLayout) findViewById(R.id.layout_hRuler);
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.05;
        mFrag.setLayoutParams(layoutParam);
        mFrag.setVisibility(View.VISIBLE);
        //
        mFrag = (LinearLayout) findViewById(R.id.layout_BhRuler);
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.0;
        mFrag.setLayoutParams(layoutParam);
        mFrag.setVisibility(View.INVISIBLE);

        // 设置实时可见，回放不可见
        mFrag = (LinearLayout) findViewById(R.id.layout_realtimeview);
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.8;
        mFrag.setLayoutParams(layoutParam);
        mFrag.setVisibility(View.VISIBLE);

        mFrag = (LinearLayout) findViewById(R.id.layout_backplayview);
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = 0;
        layoutParam.width = 0;
        mFrag.setLayoutParams(layoutParam);
        mFrag.setVisibility(View.INVISIBLE);

        mFrag = (LinearLayout) findViewById(R.id.middle_fragment);
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.width = 0;// LayoutParams.FILL_PARENT;
        layoutParam.height = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParam.weight = (float) 0.5;
        mFrag.setLayoutParams(layoutParam);
        mIsMaxshowRealtimeView = false;
    }

    /**
     * 最大化全屏显示实时采集视图
     */
    public void maxShowRealtimeView() {
        RelativeLayout lFrag;
        LinearLayout.LayoutParams params;
        // 隐藏左边 控制区域
        lFrag = (RelativeLayout) findViewById(R.id.left_fragment);
        params = (android.widget.LinearLayout.LayoutParams) lFrag.getLayoutParams();
        params.width = 0;
        params.weight = 0;
        lFrag.setLayoutParams(params);
        lFrag.setVisibility(View.INVISIBLE);

        // 隐藏右侧 单道波形 区域
        lFrag = (RelativeLayout) findViewById(R.id.right_fragment);
        params = (android.widget.LinearLayout.LayoutParams) lFrag.getLayoutParams();
        params.width = 0;
        params.weight = 0;
        lFrag.setVisibility(View.INVISIBLE);

        LinearLayout mFrag;
        LinearLayout.LayoutParams layoutParam;
        // //显示 实时采集 模式下 时窗标尺
        mFrag = (LinearLayout) findViewById(R.id.layout_VTWRuler);
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.05;
        mFrag.setLayoutParams(layoutParam);
        // 2016.6.10
        mFrag.setVisibility(View.VISIBLE);
        mFrag = (LinearLayout) findViewById(R.id.layout_BVTWRuler);
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.0;
        mFrag.setLayoutParams(layoutParam);
        mFrag.setVisibility(View.INVISIBLE);

        // 显示 实时采集 模式下 深度标尺
        mFrag = (LinearLayout) findViewById(R.id.layout_VDDRuler);
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.05;
        mFrag.setLayoutParams(layoutParam);
        // 2016.6.10
        mFrag.setVisibility(View.VISIBLE);
        mFrag = (LinearLayout) findViewById(R.id.layout_BVDDRuler);
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.0;
        mFrag.setLayoutParams(layoutParam);
        mFrag.setVisibility(View.INVISIBLE);

        // //2016.6.10 显示实时采集 水平标尺，隐藏 回放模式 水平标尺
        mFrag = (LinearLayout) findViewById(R.id.layout_hRuler);
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.05;
        mFrag.setLayoutParams(layoutParam);
        mFrag.setVisibility(View.VISIBLE);

        mFrag = (LinearLayout) findViewById(R.id.layout_BhRuler);
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.0;
        mFrag.setLayoutParams(layoutParam);
        mFrag.setVisibility(View.INVISIBLE);

        mFrag = (LinearLayout) findViewById(R.id.layout_realtimeview);
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.9;
        mFrag.setLayoutParams(layoutParam);
        mFrag = (LinearLayout) findViewById(R.id.middle_fragment);
        mFrag.setVisibility(View.VISIBLE);

        mFrag = (LinearLayout) findViewById(R.id.layout_backplayview);
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = 0;
        layoutParam.width = 0;
        mFrag.setLayoutParams(layoutParam);
        mFrag.setVisibility(View.INVISIBLE);

        this.mbackplayScanView.setVisibility(View.INVISIBLE);
        mScanView.setVisibility(View.VISIBLE);

        mIsMaxshowRealtimeView = true;
    }

    /**
     * 设置回放弹框参数设置对话框
     */
    private View mSetPlayBackView;
    private PopupWindow mSetPlayBackWindow;
    private Button bt_pbbackg = null;// 去背景
    private Button bt_pbzoom = null;// 增益调节
    private Button bt_pbxzoom = null;// 缩放调节
    private boolean bl_pbbackg = false, bl_pbzoom = false, bl_pbxzoom = false;// 是否进行操作

    public void setBackPlayPopupWindow() {
        // View dv = getWindow().getDecorView();
        // dv.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        // |View.SYSTEM_UI_FLAG_FULLSCREEN);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        getWindow().setAttributes(params);

        mSetPlayBackView = View.inflate(this, R.layout.playback_set, null);

        bt_pbbackg = (Button) mSetPlayBackView.findViewById(R.id.bt_pbbackg);
        bt_pbzoom = (Button) mSetPlayBackView.findViewById(R.id.bt_pbzoom);
        bt_pbxzoom = (Button) mSetPlayBackView.findViewById(R.id.bt_pbxzoom);

        bt_pbbackg.setOnClickListener(mSettingPlayback_OnClickHandler);
        bt_pbzoom.setOnClickListener(mSettingPlayback_OnClickHandler);
        bt_pbxzoom.setOnClickListener(mSettingPlayback_OnClickHandler);

        bt_pbzoom.setOnKeyListener(mSettingPlayback_OnKeyHandler);
        bt_pbxzoom.setOnKeyListener(mSettingPlayback_OnKeyHandler);

        mSetPlayBackWindow = new PopupWindow(mSetPlayBackView, 280,
                                             android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        mSetPlayBackWindow.setFocusable(true);
    }

    /**
     * 显示回放设置窗口
     */
    public void showBackPlayPopupWindow() {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        getWindow().setAttributes(params);

        mSetPlayBackWindow.setBackgroundDrawable(new BitmapDrawable());
        mSetPlayBackWindow.showAtLocation(mSetPlayBackView, Gravity.CENTER | Gravity.BOTTOM, 0, 0);
    }

    /**
     * 回放设置去背景、调增益和缩放
     */
    public View.OnClickListener mSettingPlayback_OnClickHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.bt_pbbackg:
                    bl_pbbackg = !bl_pbbackg;
                    if (bl_pbbackg) {
                        bt_pbbackg.setTextColor(Color.RED);
                        // 去背景
                        mbackPlayDIBView.setRemoveBackground(true);
                    } else {
                        bt_pbbackg.setTextColor(Color.BLACK);
                        mbackPlayDIBView.setRemoveBackground(false);
                    }
                    break;
                case R.id.bt_pbzoom:
                    bl_pbzoom = !bl_pbzoom;
                    if (bl_pbzoom) {
                        // 进行缩放处理上下键
                        bt_pbzoom.setTextColor(Color.RED);
                    } else {
                        // 恢复增益
                        if (mbackPlayDIBView.isZoom())
                            mbackPlayDIBView.zoomRestorePlus();
                        else
                            ;
                        bt_pbzoom.setTextColor(Color.BLACK);
                    }
                    break;
                case R.id.bt_pbxzoom:
                    bl_pbxzoom = !bl_pbxzoom;
                    if (bl_pbxzoom)
                        bt_pbxzoom.setTextColor(Color.RED);
                    else {
                        // 恢复缩放
                        mbackPlayDIBView.mZoomX = 1;
                        setBackplayHRulerZoomx(1);
                        bt_pbxzoom.setTextColor(Color.BLACK);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 回放参数处理的按键监听
     */
    public Button.OnKeyListener mSettingPlayback_OnKeyHandler = new Button.OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            // TODO Auto-generated method stub
            int id = v.getId();
            // 处理上键，增加相应的值
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (id) {
                        case R.id.bt_pbzoom:
                            // DebugUtil.toast(IDSC2600MainActivity.this,
                            // "bt_pbzoom");
                            if (bl_pbzoom)// 如果在做处理
                            {
                                mbackPlayDIBView.zoomOutPlus();
                            } else
                                ;
                            break;
                        case R.id.bt_pbxzoom:
                            if (bl_pbxzoom) {
                                mbackPlayDIBView.zoomInX();
                                setBackplayHRulerZoomx(mbackPlayDIBView.mZoomX);
                            } else
                                ;
                            break;
                        default:
                            break;
                    }
                } else
                    ;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (id) {
                        case R.id.bt_pbzoom:
                            if (bl_pbzoom)// 如果在做处理
                            {
                                mbackPlayDIBView.zoomInPlus();
                            } else
                                ;
                            break;
                        case R.id.bt_pbxzoom:
                            if (bl_pbxzoom) {
                                mbackPlayDIBView.zoomOutX();
                                setBackplayHRulerZoomx(mbackPlayDIBView.mZoomX);
                            } else
                                ;
                            break;
                        default:
                            break;
                    }
                } else
                    ;
            } else
                ;
            return false;
        }
    };

    /**
     * 将设置还原初始的状态
     *
     * @return
     */
    public boolean resetPlayBack() {
        // 不去除背景
        bt_pbbackg.setTextColor(Color.BLACK);
        mbackPlayDIBView.setRemoveBackground(false);
        // 恢复增益
        if (mbackPlayDIBView.isZoom())
            mbackPlayDIBView.zoomRestorePlus();
        else
            ;
        bt_pbzoom.setTextColor(Color.BLACK);
        // 恢复缩放
        mbackPlayDIBView.mZoomX = 1;
        setBackplayHRulerZoomx(1);
        bt_pbxzoom.setTextColor(Color.BLACK);

        bl_pbbackg = false;
        bl_pbzoom = false;
        bl_pbxzoom = false;
        return true;
    }

    // //开始保存
    public boolean beginSave() {
        // 如果是轮测模式，将回退定位清零
        if (mApp.mRadarDevice.isWhellMode()) {
            mApp.mRadarDevice.setBackFillPos(0);
            mApp.mRadarDevice.endBackOrient1();
        } else
            ;

        // 生成保存文件
        if (judgeExistSpace()) {
            if (!mApp.mRadarDevice.createNewDatasFile()) {
                Log.d("debug", "生成保存文件错误");
                Toast.makeText(this, "生成保存文件错误_1!", Toast.LENGTH_SHORT).show();
                return false;
            } else
                ;
        } else {
            Log.d("debug", "judge failed");
            return false;
        }

        // 开始保存文件
        if (!mApp.mRadarDevice.beginSave()) {
            Log.d("debug", "begin save return false");
            Toast.makeText(this, "保存数据错误_2!", Toast.LENGTH_SHORT).show();
            return false;
        }
        Toast.makeText(this, "开始保存数据!", Toast.LENGTH_SHORT).show();
//        mApp.mRadarDevice.setSaveDetectResult(true);
        Log.d("debug", "开始保存");
        mApp.mRadarDetect.setSave(true);
        mApp.mRadarDetect.setStoragePath(mApp.mRadarDevice.getStoragePath());
        mApp.mRadarDetect.startRadarDetect();
        Toast.makeText(this, "启动探测线程!", Toast.LENGTH_SHORT).show();

        // 显示保存文件名
        String txt, subString;
        subString = mApp.mRadarDevice.getSaveFilename();
        int sub = subString.lastIndexOf("/");
        subString = subString.substring(sub, subString.length());
        txt = "保存:" + subString + "!";
        TextView txtView;
        txtView = (TextView) findViewById(R.id.textview_savefilename);
        txtView.setVisibility(View.VISIBLE);
        // txtView.setWidth(100);
        int index = txt.lastIndexOf("/");
        txt = txt.substring(index + 1);
        txtView.setText(txt);

        // mRealTimeDIBView.initDIB();
        // mRealTimeDIBView.invalidate();// 0522
        return true;
    }

    // 判断是否有该外设以及存储空间的大小
    public boolean judgeExistSpace() {
        // 如果存在此外设
        long size[] = mApp.getSDCardMemory();
        String path = mApp.mRadarDevice.getStoragePath();
        Log.d("debug", "path: " + path);
        DebugUtil.i(TAG, "judgeExistSpace=" + path);

        // 如果可用存储空间为0，判断是否存在
        if (size[1] == 0) {
            path += "/test.txt";
            File file = new File(path);

            if (file.mkdir()) {
                if (file.delete())
                    ;
                else
                    ;
                // 存在提示存储空间大小
                DebugUtil.infoDialog(this, "存储空间不足", "可用存储空间" + size[1] + "");
                return true;
            } else {
                // 提示不存在
                Log.d("debug", "未找到外设");
                DebugUtil.infoDialog(this, "存储空间不存在", "未找到该外设");
            }

            return false;
        }
        // 提示存储空间大小
        else {
            path += "/test.txt";
            File file = new File(path);

            if (file.mkdir()) {
                if (file.delete())
                    ;
                else
                    return false;

                String txt = null;
                if (size[1] > 1024 * 1024 * 1024)
                    txt = size[1] / 1024 / 1024 / 1024 + "G/" + size[0] / 1024 / 1024 / 1024 + "G";
                else
                    txt = size[1] / 1024 / 1024 + "M/" + size[0] / 1024 / 1024 + "M";
                DebugUtil.toast(this, "可用存储空间大小/总存储大小:" + txt);

                return true;
            } else {
                // 提示不存在
                DebugUtil.infoDialog(this, "存储空间不存在", "未找到该外设");
            }
            return true;
        }
    }

    // //停止保存
    public void stopSave() {
        Toast.makeText(this, "保存数据结束!", Toast.LENGTH_LONG).show();
        TextView txtView;
        txtView = (TextView) findViewById(R.id.textview_savefilename);
        txtView.setVisibility(View.INVISIBLE);
        // txtView.setWidth(0);
        mApp.mRadarDevice.stopSave();
        mApp.mRadarDetect.stopRadarDetect();
        Toast.makeText(this, "已停止分析雷达数据!", Toast.LENGTH_LONG).show();
    }

    // 设置已经回放的道数
    public void setBackplayScans(int scans) {
        TextView txtView;
        String txt;
        txtView = (TextView) findViewById(R.id.textview_rcvscans);
        txtView.setVisibility(View.VISIBLE);
        if (mBackplayFileHeader.isWhellMode()) {
            double distance;
            distance = scans * mBackplayFileHeader.getDistancePerScans();
            distance = distance / 100.;
            distance = ((int) (distance * 1000)) / 1000.;
            txt = "已走距离:" + distance + "m";
            txtView.setText(txt);
        } else {
            txt = "已回放道数:" + scans + "道";
            // 如果是点测模式
            if (mBackplayFileHeader.isDianCeMode()) {
                float distance = 0;
                distance = (int) ((scans - 1) * mBackplayFileHeader.getDianCeDistancePerScans());
                distance = (float) (distance / 100.);
                txt = txt + "(" + distance + "m)";
            }
            txtView.setText(txt);
        }
    }

    public void setHasReceiveScansInf() {
        TextView txtView;
        String txt;
        long scans;
        txtView = (TextView) findViewById(R.id.textview_rcvscans);
        txtView.setVisibility(View.VISIBLE);
        if (mApp.mRadarDevice.isWhellMode()) {
            double distance;
            scans = mApp.mRadarDevice.getHadRcvScans();
            scans = scans - mApp.mRadarDevice.mFillposCursor;
            distance = scans * mApp.mRadarDevice.getTouchDistance();
            distance = distance / 100.;
            distance = ((int) (distance * 1000)) / 1000.;

            txt = "已走距离:" + distance + "m";
            txtView.setText(txt);
        } else {
            scans = mApp.mRadarDevice.getHadRcvScans();
            txt = "已采道数:" + scans + "道";
            // 如果是点测模式20160614
            if (mApp.mRadarDevice.isDianCeMode()) {
                // float distance=0;
                // distance = (int)
                // ((scans-1)*mApp.mRadarDevice.mDianceDistance);
                // distance = (float) (distance/100.);
                // txt = txt +"(" + distance +"m)";
            }
            txtView.setText(txt);
        }
    }

    // //停止雷达
    public boolean radarStop() {
        MyApplication theApp;
        theApp = (MyApplication) getApplicationContext();

        // 停止读取数据
        mApp.setRealThreadReadingDatas(false);
        theApp.mRadarDevice.stop();

        // 2016.6.10
        fragLeft.setRadarState(false);

        // 保存默认参数
        String fileName;
        int sel;
        sel = theApp.mRadarDevice.getAntenFrqSel();
        fileName = theApp.mRadarDevice.INNERSTORAGE + theApp.mRadarDevice.mParamsFilefolderPath;
        fileName += radarDevice.g_antenFrqStr[sel] + ".par";
        // 保存参数文件
        // theApp.mRadarDevice.saveParamsFile(fileName);
        // theApp.mRadarDevice.saveSystemSetFile();
        // theApp.mRadarDevice.saveDefaultCheckParamsFile();

        return true;
    }

    public int mParamsFileSel = 0;
    public String mParamSelFileName;

    public void loadParams() {
        MyApplication app;
        app = (MyApplication) getApplicationContext();
        // 寻找sd卡中的雷达参数文件
        String path;
        path = app.mRadarDevice.getParamsPath();
        final List<String> dataList = new ArrayList<String>();
        if (!app.getParamFilenamesFromeSD(dataList, path) || dataList.size() == 0) {
            showToastMsg("不存在参数文件!");
            return;
        }
        // 得到文件名
        int i;
        int size;
        size = dataList.size();
        String[] fileNames = new String[size];
        for (i = 0; i < size; i++) {
            fileNames[i] = dataList.get(i);
            int index;
            index = fileNames[i].lastIndexOf('/');
            fileNames[i] = fileNames[i].substring(index + 1);
        }
        int Sel = 0;
        mParamsFileSel = 0;
        mParamSelFileName = dataList.get(mParamsFileSel);
        new AlertDialog.Builder(this).setTitle("选择雷达参数").setSingleChoiceItems(fileNames, Sel,
                                                                              new android.content.DialogInterface.OnClickListener() {
                                                                                  @Override
                                                                                  public void onClick(
                                                                                          DialogInterface dialog,
                                                                                          int which) {
                                                                                      mParamsFileSel
                                                                                              =
                                                                                              which;
                                                                                      mParamSelFileName
                                                                                              =
                                                                                                                                                                                            dataList
                                                                                              .get(which);
                                                                                  }
                                                                              }).setPositiveButton(
                "确定", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mApp.mMainActivity.loadRadarParamsFromeFile(mParamSelFileName);
                    }
                }).setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
    }

    // 加载参数
    public void loadRadarParamsFromeFile(String pathName) {
        DebugUtil.i(TAG, "loadRadarParamsFromeFile,pathName=" + pathName);

        mApp.mRadarDevice.loadParamsFile(pathName);
        mApp.mRadarDevice.refreshFileHeader();

        RadarParamExpandableListAdapter adapter = (RadarParamExpandableListAdapter) mApp.mListView
                .getExpandableListAdapter();
        adapter.notifyDataSetChanged();

        // 更新标尺
        mApp.mTimewndRuler.invalidate();
        mApp.mDeepRuler.invalidate();
    }

    /**
     * 设置回放的状态
     */
    public boolean mIsMaxshowBackPlayView = false;

    public boolean isMaxShowBackPlayView() {
        return mIsMaxshowBackPlayView;
    }

    /**
     * 设置弹窗的文件内容
     */
    private View backPlayFilePathView;
    private PopupWindow mBackPlayFilePopupWindow;
    private ListView listView;
    private Button btn_up;// 返回上一级
    private CheckBox cbx_fileOrder;// 文件顺序列表
    private TextView file_path;
    private FileAdapter fileAdapter;
    private String filePathString = null; // 选择回放的文件路径
    // 设置存储位置选择对话框
    private View playbackSelectView;
    private PopupWindow mplaybackSelectPopWindow;
    private RadioGroup radioGroupplayback;

    /**
     * 设置回放弹窗的定义，并设置单道回放的加载
     */
    public void setSelectplaybackPopwindow() {
        playbackSelectView = LayoutInflater.from(this).inflate(
                R.layout.layout_playbackselect_popwindow, null);
        radioGroupplayback = (RadioGroup) playbackSelectView.findViewById(R.id.radioGroupPlayback);

        // 默认选择第一个radioButton
        int sel = mApp.mRadarDevice.getSelectStorageIndex();
        RadioButton radioButton = null;
        radioButton = (RadioButton) radioGroupplayback.getChildAt(sel);
        radioGroupplayback.check(radioButton.getId());

        int i;
        for (i = 0; i < radioGroupplayback.getChildCount(); i++) {
            radioButton = (RadioButton) radioGroupplayback.getChildAt(i);
            radioButton.setOnClickListener(mPlaybackSelect_OnClickHandler);
        }

        this.mbackPlayDIBView.setActivity(this);
        this.mbackplayScanView.setBackplayDIBView(mbackPlayDIBView);

        mplaybackSelectPopWindow = new PopupWindow(playbackSelectView,
                                                   android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                                                   android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        // 取消外面点击消失功能
        mplaybackSelectPopWindow.setFocusable(true);
    }

    // 存储位置选择响应
    public View.OnClickListener mPlaybackSelect_OnClickHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            int arg1 = v.getId();
            int selectedID = 1;
            switch (arg1) {
                case R.id.playbackInner:
                    selectedID = mApp.mRadarDevice.INNER_INDEX;
                    break;
                case R.id.playbackSdcard:
                    selectedID = mApp.mRadarDevice.SDCARD_INDEX;
                    break;
                case R.id.playbackUSB:
                    selectedID = mApp.mRadarDevice.USB_INDEX;
                    break;
            }
            filePathString = mApp.mRadarDevice.getStoragePathByIndex(selectedID);
            showBackPlayFilePath();
            mplaybackSelectPopWindow.dismiss();
        }
    };

    // 弹出存储选择窗口
    public void showSelectplaybackPopwindow() {
        // 根据当前的选择，设置radio选择
        RadioButton radio;
        int sel = mApp.mRadarDevice.getSelectStorageIndex();
        RadioButton radioButton = null;
        radioButton = (RadioButton) radioGroupplayback.getChildAt(sel);
        radioGroupplayback.check(radioButton.getId());

        int id = radioGroupplayback.getCheckedRadioButtonId();
        radio = (RadioButton) playbackSelectView.findViewById(id);
        radio.requestFocus();

        mplaybackSelectPopWindow.setBackgroundDrawable(new BitmapDrawable());
        mplaybackSelectPopWindow.showAtLocation(playbackSelectView, Gravity.CENTER | Gravity.CENTER,
                                                0, 0);
        mplaybackSelectPopWindow.update();
    }

    private String fileNameString = null;

    /**
     * 回放和删除使用的路径选择处理函数
     */
    public void showBackPlayFilePath() {
        DebugUtil.i(TAG, "enter showBackPlayFilePath!");

        backPlayFilePathView = LayoutInflater.from(this).inflate(R.layout.playback_sdcardlist,
                                                                 null);
        // 找到相应的控件
        listView = (ListView) backPlayFilePathView.findViewById(R.id.listView1);
        btn_up = (Button) backPlayFilePathView.findViewById(R.id.button_up);
        cbx_fileOrder = (CheckBox) backPlayFilePathView.findViewById(R.id.check_fileOrder);
        cbx_fileOrder.setChecked(mApp.getisDecendOrder());
        cbx_fileOrder.setOnCheckedChangeListener(fileOrderChangeListener);

        // 新建一个适配器。用来适配ListView，显示文件列表
        fileAdapter = new FileAdapter(this);
        // 设置适配器，绑定适配器
        listView.setAdapter(fileAdapter);
        DebugUtil.i(TAG, "listview.setOnkeylistener!");

        // 选择时将文件绝对路径获得，以响应删除键的删除操作
        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                FileAdapter fileNext = (FileAdapter) listView.getAdapter();
                File f = fileNext.list.get(position);
                fileNameString = f.getAbsolutePath();
                DebugUtil.i(TAG, "listView getSelectedItem=" + fileNameString);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
        final Context mcontext = this;
        listView.setOnKeyListener(mListViewOnKeyListener);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // 声明一个适配器
                FileAdapter fileNext = (FileAdapter) listView.getAdapter();
                File f = fileNext.list.get(arg2);
                // 获取文件路径和文件名称
                if (f.isDirectory()) {
                    // 获取sd卡对应的存储目录
                    // mApp.mRadarDevice.getSDCardPath();
                    fileAdapter.scanFiles(filePathString + mApp.mRadarDevice.mLTEFilefolderPath);
                } else {
                    fileNameString = f.getName();
                    if (fileNameString.endsWith(".lte") || fileNameString.endsWith(".LTE")) {
                        fileNameString = f.getAbsolutePath();
                        DebugUtil.i(TAG, "fileNameString=" + fileNameString);

                        // 是二维文件时判断目前处于回放还是删除状态
                        if (mResponFlag == BACKPLAY) {
                            // 判断文件大小
                            long length = f.length();
                            int sizeG = (int) (length / 1024 / 1024 / 1024);
                            int max = 4;

                            if (length <= 1024 || sizeG >= max) {
                                DebugUtil.infoDialog(mcontext, "错误", "文件大小错误！");
                            } else {
                                mApp.setBackplayFilePath(fileNameString);
                                maxShowBackPlayView();
                                beginBackplayFile(fileNameString);
                            }
                            mBackPlayFilePopupWindow.dismiss();
                        }// 回放模式下
                        else if (mResponFlag == DELETE) {
                            DebugUtil.i(TAG, "Flag = delete,fileNameString=" + fileNameString);

                            new AlertDialog.Builder(MultiModeLifeSearchActivity.this).setTitle(
                                    "确定删除?").setNegativeButton("确定",
                                                               new DialogInterface.OnClickListener() {
                                                                   @Override
                                                                   public void onClick(
                                                                           DialogInterface dialog,
                                                                           int whichButton) {
                                                                       if (deleteFilePath(
                                                                               fileNameString)) {
                                                                           android.app.AlertDialog.Builder
                                                                                   builder
                                                                                   =
                                                                                    new AlertDialog.Builder(
                                                                                   MultiModeLifeSearchActivity.this);
                                                                           builder.setTitle("删除成功")
                                                                                  .setMessage(
                                                                                          "删除成功！")
                                                                                  .setNegativeButton(
                                                                                          "确定",
                                                                                          new DialogInterface.OnClickListener() {
                                                                                              @Override
                                                                                              public void onClick(
                                                                                                      DialogInterface dialog,
                                                                                                      int whichButton) {
                                                                                                  fileAdapter
                                                                                                          = new FileAdapter(
                                                                                                          MultiModeLifeSearchActivity.this);
                                                                                                  // 设置适配器，绑定适配器
                                                                                                  listView.setAdapter(
                                                                                                          fileAdapter);
                                                                                                  fileAdapter
                                                                                                          .scanFiles(
                                                                                                                  filePathString +
                                                                                                                  mApp.mRadarDevice.mLTEFilefolderPath);
                                                                                              }
                                                                                          }).show();
                                                                       } else {
                                                                           android.app.AlertDialog.Builder
                                                                                   builder
                                                                                   =
                                                                                    new AlertDialog.Builder(
                                                                                   MultiModeLifeSearchActivity.this);
                                                                           builder.setTitle("删除失败")
                                                                                  .setMessage(
                                                                                          "删除失败！")
                                                                                  .setNegativeButton(
                                                                                          "确定",
                                                                                          new DialogInterface.OnClickListener() {
                                                                                              @Override
                                                                                              public void onClick(
                                                                                                      DialogInterface dialog,
                                                                                                      int whichButton) {
                                                                                              }
                                                                                          }).show();
                                                                       }
                                                                   }
                                                               }).setPositiveButton("取消",
                                                                                    new DialogInterface.OnClickListener() {
                                                                                        @Override
                                                                                        public void onClick(
                                                                                                DialogInterface dialog,
                                                                                                int whichButton) {

                                                                                        }
                                                                                    }).show();
                            // fileAdapter.scanFiles( filePathString +
                            // mApp.mRadarDevice.mLTEFilefolderPath );
                            DebugUtil.i(TAG, "after Notify");

                        }// 删除模式下
                    } else if (fileNameString.endsWith(".txt")) {
                        openFile(f);
                    } else {
                        Toast.makeText(MultiModeLifeSearchActivity.this, "不是雷达采集文件",
                                       Toast.LENGTH_SHORT).show();
                        Toast.makeText(MultiModeLifeSearchActivity.this, fileNameString,
                                       Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // 显示默认的文件目录
        fileAdapter.scanFiles(filePathString + mApp.mRadarDevice.mLTEFilefolderPath);

        // 响应“向上”的按钮
        btn_up.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // 设置适配器
                showSelectplaybackPopwindow();
                mBackPlayFilePopupWindow.dismiss();
            }
        });

        mBackPlayFilePopupWindow = new PopupWindow(backPlayFilePathView,
                                                   android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                                                   android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        // 取消外面点击消失功能
        mBackPlayFilePopupWindow.setFocusable(true);

        int xPos = backPlayFilePathView.getHeight();
        int yPos = backPlayFilePathView.getWidth();

        this.mBackPlayFilePopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mBackPlayFilePopupWindow.setWidth(256);// 默认宽度

        mBackPlayFilePopupWindow.showAtLocation(backPlayFilePathView,
                                                Gravity.CENTER | Gravity.CENTER, 0, 0);
        mBackPlayFilePopupWindow.update();
    }

    public CheckBox.OnCheckedChangeListener fileOrderChangeListener
            = new CheckBox.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
            // TODO Auto-generated method stub
            int id;
            id = arg0.getId();
            MyApplication app;
            app = mApp;

            switch (id) {
                case R.id.check_fileOrder:
                    mApp.setIsDecendOrder(!mApp.getisDecendOrder());
                    mApp.rememberFileDscend();
                    fileAdapter = new FileAdapter(MultiModeLifeSearchActivity.this);
                    // 设置适配器，绑定适配器
                    listView.setAdapter(fileAdapter);
                    fileAdapter.scanFiles(filePathString + mApp.mRadarDevice.mLTEFilefolderPath);
                    // 记录状态
                    break;
            }
        }
    };

    // 列表响应处理
    private boolean onceDialog = false;
    public View.OnKeyListener mListViewOnKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            DebugUtil.i(TAG, "0.mListViewOnKeyListener =" + keyCode);

            // 按下删除以及没有在最大化回放
            if (keyCode == KeyEvent.KEYCODE_DEL && !isMaxShowBackPlayView()) {
                DebugUtil.i(TAG, "1.keyCode_del!,fileNameString=" + fileNameString);
                if (!onceDialog) {
                    onceDialog = true;
                    new AlertDialog.Builder(MultiModeLifeSearchActivity.this).setTitle("确定删除?")
                                                                             .setNegativeButton(
                                                                                     "确定",
                                                                                     new DialogInterface.OnClickListener() {
                                                                                         @Override
                                                                                         public void onClick(
                                                                                                 DialogInterface dialog,
                                                                                                 int whichButton) {
                                                                                             if (deleteFilePath(
                                                                                                     fileNameString)) {
                                                                                                 android.app.AlertDialog.Builder
                                                                                                         builder
                                                                                                         = new AlertDialog.Builder(
                                                                                                         MultiModeLifeSearchActivity.this);
                                                                                                 builder.setTitle(
                                                                                                         "删除成功")
                                                                                                        .setMessage(
                                                                                                                "删除成功！")
                                                                                                        .setNegativeButton(
                                                                                                                "确定",
                                                                                                                new DialogInterface.OnClickListener() {
                                                                                                                    @Override
                                                                                                                    public void onClick(
                                                                                                                            DialogInterface dialog,
                                                                                                                            int whichButton) {
                                                                                                                        onceDialog
                                                                                                                                = false;
                                                                                                                        fileAdapter
                                                                                                                                = new FileAdapter(
                                                                                                                                MultiModeLifeSearchActivity.this);
                                                                                                                        // 设置适配器，绑定适配器
                                                                                                                        listView.setAdapter(
                                                                                                                                fileAdapter);
                                                                                                                        fileAdapter
                                                                                                                                .scanFiles(
                                                                                                                                        filePathString +
                                                                                                                                        mApp.mRadarDevice.mLTEFilefolderPath);
                                                                                                                    }
                                                                                                                })
                                                                                                        .show();
                                                                                             } else {
                                                                                                 android.app.AlertDialog.Builder
                                                                                                         builder
                                                                                                         = new AlertDialog.Builder(
                                                                                                         MultiModeLifeSearchActivity.this);
                                                                                                 builder.setTitle(
                                                                                                         "删除失败")
                                                                                                        .setMessage(
                                                                                                                "删除失败！")
                                                                                                        .setNegativeButton(
                                                                                                                "确定",
                                                                                                                new DialogInterface.OnClickListener() {
                                                                                                                    @Override
                                                                                                                    public void onClick(
                                                                                                                            DialogInterface dialog,
                                                                                                                            int whichButton) {
                                                                                                                        onceDialog
                                                                                                                                = false;
                                                                                                                    }
                                                                                                                })
                                                                                                        .show();
                                                                                             }
                                                                                         }
                                                                                     })
                                                                             .setPositiveButton(
                                                                                     "取消",
                                                                                     new DialogInterface.OnClickListener() {
                                                                                         @Override
                                                                                         public void onClick(
                                                                                                 DialogInterface dialog,
                                                                                                 int whichButton) {
                                                                                             onceDialog
                                                                                                     = false;
                                                                                         }
                                                                                     }).show();
                } else
                    ;
            } else
                ;
            return false;
        }
    };

    // 删除指定路径的文件
    public boolean deleteFilePath(String fileNamePath) {
        DebugUtil.i(TAG, "deleteFilePath=" + fileNamePath);
        File f = new File(fileNamePath);
        if (f.exists()) {
            if (f.delete())
                return true;
            else
                return false;
        } else {
            return false;
        }
    }

    // 打开文件
    private void openFile(File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // 设置intent的Action属性
        intent.setAction(Intent.ACTION_VIEW);
        // 设置intent的data和Type属性。
        intent.setDataAndType(Uri.fromFile(file), "text/plain");
        // 跳转
        startActivity(intent);
    }

    // 得到文件路径
    private String getFilePath(String fileNewPathString) {
        int len = fileNewPathString.length();
        int index = fileNewPathString.lastIndexOf("/");
        index = fileNewPathString.lastIndexOf("/");
        if (index <= 0)
            fileNewPathString = "/";
        else {
            fileNewPathString = fileNewPathString.substring(0, index);
            if ((len - 1) == index) {
                fileNewPathString = getFilePath(fileNewPathString);
            } else
                ;
        }
        return fileNewPathString;
    }

    /**
     * 正常比例显示回放界面
     */
    public void normalShowBackPlayView() {
        stopBackplayFile();
        normalShowRealtimeView();
        mIsMaxshowBackPlayView = false;
    }

    /**
     * 最大化显示回放界面
     */
    public void maxShowBackPlayView() {
        mIsMaxshowBackPlayView = true;
        mIsMaxshowRealtimeView = false; // 2016.6.10

        RelativeLayout lFrag;
        LinearLayout.LayoutParams params;

        // 隐藏左侧的控制区
        lFrag = (RelativeLayout) findViewById(R.id.left_fragment);
        params = (android.widget.LinearLayout.LayoutParams) lFrag.getLayoutParams();
        params.width = 0;
        params.weight = 0;
        lFrag.setLayoutParams(params);
        lFrag.setVisibility(View.INVISIBLE);

        // 显示回放 单道波形 窗口
        this.mbackplayScanView.setVisibility(View.VISIBLE);
        // 隐藏实时采集 单道波形 窗口
        mScanView.setVisibility(View.INVISIBLE);

        lFrag = (RelativeLayout) findViewById(R.id.right_fragment);
        params = (android.widget.LinearLayout.LayoutParams) lFrag.getLayoutParams();
        params.width = 0;
        params.weight = 0;
        lFrag.setVisibility(View.VISIBLE);
        lFrag.setLayoutParams(params);

        LinearLayout mFrag;
        LinearLayout.LayoutParams layoutParam;

        // //2016.6.10 隐藏实时采集 时窗标尺，显示回放模式 时窗标尺
        /*
         * mFrag = (LinearLayout)findViewById(R.id.layout_VTWRuler); layoutParam
         * = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
         * layoutParam.weight = (float) 0.05;
         * mFrag.setLayoutParams(layoutParam);
         */
        //
        mFrag = (LinearLayout) findViewById(R.id.layout_VTWRuler);
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.0;
        mFrag.setLayoutParams(layoutParam);
        mFrag.setVisibility(View.INVISIBLE);
        //
        mFrag = (LinearLayout) findViewById(R.id.layout_BVTWRuler);
        mFrag.setVisibility(View.VISIBLE);
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.05;
        mFrag.setLayoutParams(layoutParam);

        // //2016.6.10 隐藏实时采集 深度标尺，显示回放模式 深度标尺
        /*
         * mFrag = (LinearLayout)findViewById(R.id.layout_VDDRuler); layoutParam
         * = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
         * layoutParam.weight = (float) 0.05;
         * mFrag.setLayoutParams(layoutParam);
         */
        //
        mFrag = (LinearLayout) findViewById(R.id.layout_VDDRuler);
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.0;
        mFrag.setLayoutParams(layoutParam);
        mFrag.setVisibility(View.INVISIBLE);
        //
        mFrag = (LinearLayout) findViewById(R.id.layout_BVDDRuler);
        mFrag.setVisibility(View.VISIBLE);
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.05;
        mFrag.setLayoutParams(layoutParam);

        // //2016.6.10 隐藏实时采集 水平标尺，显示 回放模式 水平标尺
        mFrag = (LinearLayout) findViewById(R.id.layout_hRuler);
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.0;
        mFrag.setLayoutParams(layoutParam);
        mFrag.setVisibility(View.INVISIBLE);
        //
        mFrag = (LinearLayout) findViewById(R.id.layout_BhRuler);
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.05;
        mFrag.setLayoutParams(layoutParam);
        mFrag.setVisibility(View.VISIBLE);

        // //显示回放视图
        mFrag = (LinearLayout) findViewById(R.id.layout_backplayview);
        mFrag.setVisibility(View.VISIBLE); // 2016.6.10
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.9;
        mFrag.setLayoutParams(layoutParam);

        // 隐藏 实时采集 视图
        mFrag = (LinearLayout) findViewById(R.id.layout_realtimeview);
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = 0;
        layoutParam.width = 0;
        mFrag.setLayoutParams(layoutParam);
        mFrag.setVisibility(View.INVISIBLE);
    }

    /**
     * 开始回放
     */
    public boolean beginBackplayFile(String fileName) {
        boolean ret = true;
        DebugUtil.i(TAG, String.valueOf(mIsBackplaying));

        if (mApp.mRadarDevice.setPlayBackMode())
            ;
        else
            DebugUtil.i(TAG, "setPlayBackMode error!");

        // 如果正在回放数据，先停止回放
        if (mIsBackplaying) {
            ret = stopBackplayFile();
        }
        if (!ret) {
            return ret;
        }

        // 设置回放文件名
        mBackplayFileName = fileName;
        TextView txtView;
        txtView = (TextView) findViewById(R.id.textview_savefilename);
        String file = fileName;
        int index = file.lastIndexOf("/");
        file = file.substring(index + 1);
        txtView.setText(file);

        txtView.setVisibility(View.VISIBLE);
        // txtView.setWidth(100);

        ret = mbackPlayDIBView.beginBackplay(fileName);
        // mbackPlayDIBView.showManuplusAdjustRange();

        if (ret) {
            // 设置回放速度
            mbackPlayDIBView.setBackplaySpeed(50);
            mbackPlayDIBView.setBackplayDirForward();
        } else
            ;

        mIsBackplaying = ret;
        mIsTempstopBackplay = false;
        mBackplayDir = BACKPLAY_FORWARD_DIR;

        return ret;
    }

    /**
     * 停止回放
     */
    public boolean stopBackplayFile() {
        boolean ret = true;
        mIsBackplaying = false;
        ret = this.mbackPlayDIBView.endBackplay();

        TextView txtView;
        txtView = (TextView) findViewById(R.id.textview_savefilename);
        txtView.setVisibility(View.INVISIBLE);
        // txtView.setWidth(0);

        return ret;
    }

    public void setStatusMsg(String txt) {
        TextView txtView;
        txtView = (TextView) findViewById(R.id.textview_savefilename);
        txtView.setVisibility(View.VISIBLE);
        int index = txt.lastIndexOf("/");
        txt = txt.substring(index + 1);
        txtView.setText(txt);
        // txtView.setWidth(100);
    }

    /**
     * 获取电池电量
     */
    private TextView txtViewPower;
    private boolean lowPowerInfo = false;

    private void sendBatteryInfo(short level) {
        System.err.println("send battery info");
        Packet pack = Global.newPacket(0xAAAABBBB, Global.PACKET_BATTERY, 2);
        pack.putShort(level);
        mNetwork.putDataPacket(pack);
    }

    public void changePowerInfs() {
        int leftLevel = 0;
        BatteryAttribute attr = new BatteryAttribute();
        DebugUtil.i(TAG, "Before read battery!");
        mApp.mPowerDevice.getBatteryAttribute(attr);
        DebugUtil.i(TAG, "After read battery");

        short fullBattery = 0;
        short leftBattery = 0;
        fullBattery = attr.mAveTimeToEmpty;
        leftBattery = attr.mRunTimeToEmpty;
        txtViewPower = (TextView) findViewById(R.id.textview_power);

        if (fullBattery >= MINFULLBATTERYTIME && fullBattery <= MAXFULLBATTERYTIME &&
            leftBattery >= 0 && leftBattery <= fullBattery) {
            leftLevel = ((leftBattery * 100 / fullBattery));

            System.out.println("original: " + leftLevel);

            sendBatteryInfo((short) leftLevel);

            // 0316只是用在本次10套电量读取中
//            double tempLevel = (leftLevel - 15) * 100 / 85;
//            leftLevel = (int) tempLevel;

            // 小于零设为0
            if (leftLevel < 0) {
                leftLevel = 0;
            } else
                ;

            // 大于100不显示
            if (leftLevel > 100) {
                return;
            } else
                ;

            txtViewPower.setText("电量:" + leftLevel + "%");

            System.err.println("电量：" + leftLevel);

            // 从无电池到低电量电池的状态改变
            if (leftLevel <= WARNBATTERYLEVEL) {
                txtViewPower.setTextColor(Color.RED);
                if (!lowPowerInfo) {
                    lowPowerInfo = true;
                    // 闪烁提示保存数据
                    DebugUtil.infoDialog(this, "电量不足", "电量不足，请尽快保存数据！");
                    // 电量灯变为常亮
                    mApp.mPowerDevice.BatLightOn();
                } else
                    ;
            } else {
                // 关闭电量灯
                mApp.mPowerDevice.BatLightOff();
                txtViewPower.setTextColor(Color.GREEN);
                // 判断之前是不是低电量，如果曾是，关闭电量灯
                if (lowPowerInfo) {
                    lowPowerInfo = false;// 取消低电量标志
                } else
                    ;
            }
        } else {
            System.err.println("无电池数据");
            // DebugUtil.i(TAG,"电量读取异常!");
            txtViewPower.setText("电量:无电池数据!");
            // +"leftLevel="+String.valueOf(leftBattery)
            // +"fullBattery="+String.valueOf(fullBattery));
            txtViewPower.setTextColor(Color.RED);
            // 闪烁提示保存数据
        }
    }

    /**
     * 得到存储卡容量信息
     */
    private TextView txtViewSpace;
    /**
     * 20170315存储空间每分钟读取一次，小于100M时报警
     */
    long[] infsMemory = null;
    String txtChangeMemory = "";

    public void changeMemoryInfs() {
        txtViewSpace = (TextView) findViewById(R.id.textview_space);

        if (infsMemory[1] > 1024 * 1024 * 1024) {
            txtChangeMemory = txtChangeMemory + infsMemory[1] / 1024 / 1024 / 1024 + "G/" +
                              infsMemory[0] / 1024 / 1024 / 1024 + "G";
            long left = infsMemory[1] / 1024 / 1024 / 1024;
            // DebugUtil.i(TAG, "left>500,storage="+left);
            // stopSpark();
            txtViewSpace.setTextColor(Color.BLACK);
        }

        /*
         * else { txtChangeMemory +=
         * infsMemory[1]/1024/1024+"M/"+infsMemory[0]/1024/1024+"M"; long left =
         * infsMemory[1]/1024/1024;
         *
         * //0315存储空间小于100M报警 if( left < 100 && mApp.mRadarDevice.isSavingMode()
         * ) { this.stopSave(); normalShowRealtimeView();
         * mApp.mListView.requestFocus(); stopSpark();
         * txtViewSpace.setTextColor(Color.RED); DebugUtil.infoDialog(this,
         * "保存结束","空间不足保存结束！"); } else { if( left <= 500 ) { // DebugUtil.i(TAG,
         * "left<=500,storage="+left); if( mApp.mRadarDevice.isSavingMode() ) {
         * // DebugUtil.i(TAG,"isSavingMode!"); runTask = true; sparkStorage();
         * timer.schedule(task,1,1000); } else { stopSpark();
         * txtViewSpace.setTextColor(Color.RED); } } else { // DebugUtil.i(TAG,
         * "left>500,storage="+left); //stopSpark();
         * txtViewSpace.setTextColor(Color.BLACK); } } }
         */
        // DebugUtil.i(TAG, "timer="+(timer==null)+"task="+(task==null));
        txtViewSpace.setText(txtChangeMemory);

    }

    private void updateMemory() {
        if (mApp.mRadarDevice.isSelectSDCard()) {
            txtChangeMemory = "(SD卡)剩余空间:";
        } else if (mApp.mRadarDevice.isSelectUSB()) {
            txtChangeMemory = "(USB盘)剩余空间:";
        } else if (mApp.mRadarDevice.isSelectMemory()) {
            txtChangeMemory = "(内存)剩余空间:";
        }
        infsMemory = mApp.getSDCardMemory();
    }

    // 电量不足时闪烁
    private Timer bat_timer = null;
    private TimerTask bat_task = null;
    private boolean bat_runTask = false;

    public void sparkBattery() {
        DebugUtil.i(TAG, "spark battery!");
    }

    // 空间不足报警
    private int clo = 0;
    private Timer timer = null;
    private TimerTask task = null;
    private boolean runTask = false;

    public void sparkStorage() {
        DebugUtil.i(TAG, "sparkStorage!");
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (runTask) {
                            DebugUtil.i(TAG, "sparkStorage run!");
                            if ((clo % 10 == 0) | (clo % 10 == 1) | (clo % 10 == 2) |
                                (clo % 10 == 4) | (clo % 10 == 5) | (clo % 10 == 6) |
                                (clo % 10 == 8) | (clo % 10 == 9)) {
                                clo++;
                                txtViewSpace.setTextColor(Color.RED);
                            } else if ((clo % 10 == 3) | (clo % 10 == 7)) {
                                clo++;
                                txtViewSpace.setTextColor(Color.TRANSPARENT);
                            }
                        } else
                            ;
                    }
                });
            }
        };
    }

    public void stopSpark() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        runTask = false;
    }

    // 超速报警
    public void beginOverspeedAlarm() {
        TextView txtView;
        txtView = (TextView) findViewById(R.id.textview_overspeed);
        txtView.setVisibility(View.VISIBLE);
        String txt;
        txt = "已超速!";
        txtView.setText(txt);
        showOverSpeedPopupWindow();
    }

    // 停止超速报警
    public void stopOverspeedAlarm() {
        TextView txtView;
        txtView = (TextView) findViewById(R.id.textview_overspeed);
        txtView.setVisibility(View.INVISIBLE);
        String txt;
        txt = "";
        txtView.setText(txt);
        mOverSpeedPopWindow.dismiss();
    }

    // /2016.6.10
    public void setBackplayHRulerScans(int scans) {
        BackPlayHRulerView view;
        view = (BackPlayHRulerView) (mApp.mBHorRuler);
        view.setNowScans(scans);
        view.invalidate();
    }

    public void setBackplayHRulerZoomx(int zoomx) {
        BackPlayHRulerView view;
        view = (BackPlayHRulerView) (mApp.mBHorRuler);
        view.setZoomX(zoomx);
        view.invalidate();
    }

    // /2016.6.10
    public void setBackplayFileHeader(FileHeader fileHeader) {
        mBackplayFileHeader.copyFrome(fileHeader);
    }

    // /2016.6.10
    public void discardDataDialog() {
        // 弹出提示对话框
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("丢弃回退数据");
        builder.setTitle("确定丢弃回退数据吗?").setMessage("确定丢弃").setNegativeButton("是",
                                                                            new DialogInterface.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(
                                                                                        DialogInterface dialog,
                                                                                        int whichButton) {
                                                                                    mApp.mRadarDevice
                                                                                            .discardBackDatas();
                                                                                }
                                                                            }).setPositiveButton(
                "否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                }).show();
    }

    // 超速弹框
    private View overSpeedView;
    private PopupWindow mOverSpeedPopWindow;

    public boolean setOverSpeedPopupWindow() {
        overSpeedView = View.inflate(this, R.layout.overspeedalarm_popwindow, null);
        mOverSpeedPopWindow = new PopupWindow(overSpeedView, 100,
                                              android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        mOverSpeedPopWindow.setFocusable(false);

        return true;
    }

    // 超速显示
    public void showOverSpeedPopupWindow() {
        DebugUtil.i(TAG, "showOverSpeedPopupWindow!");
        mOverSpeedPopWindow.setBackgroundDrawable(new BitmapDrawable());
        mOverSpeedPopWindow.showAtLocation(overSpeedView, Gravity.CENTER | Gravity.CENTER, 0, 0);
    }

    /**
     * 小数保留位数计算
     */
    public double setDigits(double inputNum, int digitNum) {
        double tempNum = inputNum;
        BigDecimal b = new BigDecimal(tempNum);
        tempNum = b.setScale(digitNum, BigDecimal.ROUND_HALF_DOWN).doubleValue();// 向下取整
        return tempNum;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
    }

}
