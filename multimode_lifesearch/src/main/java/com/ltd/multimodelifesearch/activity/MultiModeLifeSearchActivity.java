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
 * ????????
 *
 * @author huangss
 */
public class MultiModeLifeSearchActivity extends Activity implements UncaughtExceptionHandler {
    // ????????
    private TextView tv, textview_systemtime;
    public realTimeDIBView mRealTimeDIBView; // ??????????
    public scanView mScanView;
    public backPlayDIBView mbackPlayDIBView; // ??????????
    public BackplayScanView mbackplayScanView;
    private LeftFragment fragLeft; // ????fragement??????
    private RightFragment fragRight; // ????rightfragment??????
    private MyApplication mApp; // ????????
    private String TAG = "IDSC2600MainActivity"; // ????tag
    private String KTAG = "KTAG"; // ??????????????
    private String SERIALPORT = "/dev/ttySAC3"; // ??????????
    // ??????
    private ExpandableListView elv;
    private GPSDevice gpsport = new GPSDevice(); // GPS????
    // ????????????
    private boolean mIsBackplaying = false; // ????????????
    private boolean mBackplayPause = false; // ????????
    private boolean mBackplayFocusView = false; // ??????????????????????????????false??????????????true
    private String mBackplayFileName; // ??????????????????
    private final int BACKPLAY_FORWARD_DIR = 1; // ??????
    private final int BACKPLAY_BACK_DIR = 2; // ??????
    private int mBackplayDir = BACKPLAY_FORWARD_DIR; // ??????????
    public boolean mIsTempstopBackplay = false; // ????????????
    private FileInputStream mBackplayFile = null;
    private FileHeader mBackplayFileHeader = new FileHeader();
    private int mResponFlag = 0; // ????????????????????????????1????????2??????
    final private int BACKPLAY = 1;
    final private int DELETE = 2;

    private int REALTIME_THREADMSG_READDATAS = 1; // ????????????????
    private int REALTIME_THREADMSG_SDCARDINFS = 2; // ????????????????????????????????????
    private int REALTIME_THREADMSG_OVERSPEED = 3; // ????????
    private int REALTIME_THREADMSG_POWER = 4; // ????????????
    private int REALTIME_THREADMSG_GPS = 5; // ????GPS??????????20HZ
    private int SYSTEM_TIME = 6; // ????????????????????
    private int MAXFULLBATTERYTIME = 4800; // ????????????
    private int MINFULLBATTERYTIME = 0; // ????????????
    private int WARNBATTERYLEVEL = 10; // ????????????????
    private static final String FileName = "setFactoryParams"; // ????????
    private static final String PackageName = "com.example.setsteptime";
    private static final String KEY = "stepelapsed";
    private static int MODE = Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE;
    private Bitmap mBitmap;
    // ????????
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

        requestWindowFeature(Window.FEATURE_NO_TITLE); // ??????????
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                  WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
                             WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        getWindow().setAttributes(params);
        super.setContentView(R.layout.activity_main); // ??????????????

        mApp = (MyApplication) this.getApplicationContext();
        mApp.mRadarDevice.setContext(this);
        mApp.mRadarDevice.newPositiveData();
        mApp.mRadarDevice.mMainActivity = this;

        if (mApp.mPowerDevice.openPowerDevice() < 0)
            DebugUtil.i(TAG, "powerDevice<0");
        else
            DebugUtil.i(TAG, "powerDevice>=0");

        // ????????
        // devicePowerUp();

        System.loadLibrary("Detect");

        // //????????????
        mRealTimeDIBView = (realTimeDIBView) findViewById(R.id.layoutRealView);
        mRealTimeDIBView.setZOrderOnTop(true); // 0531

        mScanView = (scanView) findViewById(R.id.viewSinglewave);
        mScanView.setZOrderOnTop(true);

        // mScanView = new scanView(this);
        // //????????????
        mbackPlayDIBView = (backPlayDIBView) findViewById(R.id.layoutBackPlayView);
        mbackPlayDIBView.mParentActivity = this;
        mbackplayScanView = (BackplayScanView) findViewById(R.id.backplayScanview);

        // //????????????
        // ????????????????
        mApp.mTimewndRuler = findViewById(R.id.viewVTWRuler);
        mApp.mDeepRuler = findViewById(R.id.viewDDRuler);
        mApp.mHorRuler = findViewById(R.id.viewHRuler);
        // 2016.6.10 ????????????????
        mApp.mBTimewndRuler = findViewById(R.id.viewBVTWRuler);
        ((BackPlayVRulerView) (mApp.mBTimewndRuler)).setShowTimewndType();
        mApp.mBDeepRuler = findViewById(R.id.viewBDDRuler);
        ((BackPlayVRulerView) (mApp.mBDeepRuler)).setShowDeepType();
        mApp.mBHorRuler = findViewById(R.id.viewBHRuler);
        ((BackPlayHRulerView) (mApp.mBHorRuler)).mApp = mApp;
        mApp.mMainActivity = this;

        // ????????
        textview_systemtime = (TextView) findViewById(R.id.textview_systemtime);

        // ??????????????????????????
        HRulerView hRView = (HRulerView) mApp.mHorRuler;
        hRView.mApp = mApp;

        // ????????????????????
        int sW, sH;
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        sW = dm.widthPixels;
        sH = dm.heightPixels;
        mApp.setScreenRange(sW, sH);
        // ????????????1019
        /*
         * WallpaperManager manager = WallpaperManager.getInstance(this);
         * BitmapDrawable drw = (BitmapDrawable)manager.getDrawable();
         * this.mBitmap = drw.getBitmap();
         */

        filePathString = mApp.mRadarDevice.getStoragePath();
        // ????????
        setSelectplaybackPopwindow();
        // ????????????
        setBackPlayPopupWindow();
        // ????????????????
        setOverSpeedPopupWindow();
        // ????????????
        createNetwork();
        // ????????????hss0427
        createRealtimeThread();
        // ????????????hss427
        createStateThread();
        // ????GPS????
        // openGPSDevice();
        backupBackground();
        // ????fragment????
        fragLeft = new LeftFragment();

        startRadar();

        fragRight = new RightFragment();

        // ????????????????????
        changePowerInfs();

        // ????GPS????????
        // GPSDevice gpsDevice = new GPSDevice();
        // gpsDevice.openSerialPort("/dev/ttySAC0",115200,'N',8, 1, 0);//????????

        /*
         * SerialPort srlport = new SerialPort(); DebugUtil.i(TAG,
         * "????GPS??????????"); srlport.openSerialPort("/dev/ttySAC0",115200,'N',8, 1,
         * 0);//????????
         */

        // ??????????????????
        // mApp.mPowerDevice.PowerLightOn();
        // mApp.mPowerDevice.WorkLightOn();
        // mApp.mPowerDevice.BatLightOn();

        // DisplayMetrics dmc = new DisplayMetrics();
        // getWindowManager().getDefaultDisplay().getMetrics(dmc);
        // String strResolution = "Resolution:"+dmc.widthPixels
        // +"*"+dmc.heightPixels;
        // showToastMsg(strResolution);

        // ????????????
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
        // ????????????????????
        if (mApp.mRadarDevice.isRunningMode()) {
            DebugUtil.i(TAG, "radarStart : device has start!");
            return true;
        }
        // ????????
        // loadDriver();

        // ??????????????????????????????????????
        String fileName;
        fileName = mApp.mRadarDevice.getInnerStoragePath() +
                   mApp.mRadarDevice.mParamsFilefolderPath;
        fileName += radarDevice.g_antenFrqStr[frqIndex] + ".par";
        // Toast.makeText(this, fileName, Toast.LENGTH_SHORT).show();

        /**
         * ??????????????????????
         */
        mApp.mRadarDevice.setAntenFrq(frqIndex);
        if (!mApp.mRadarDevice.onlyLoadParamsFromeFile(fileName)) {
            // DebugUtil.i(TAG,"!!!!!~~~~Now setAntenFrq:"+frqIndex);
            // Toast.makeText(this, "~~~~Now AntenFrq:"+frqIndex,
            // Toast.LENGTH_SHORT).show();

            // ??????????????????????????????????????????
            mApp.mRadarDevice.changeParamsFromeAntenfrq(frqIndex);
        } else {
        }

        if (!mApp.mRadarDevice.loadSystemSetFile()) {
            System.out.println("????????????????????");
        } else {
            System.out.println("????????????????????");
        }

        /**
         * ??????????????????????????????
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
        // ??????????????????
        int nowSel = mApp.mRadarDevice.getAntenFrqSel();
        // DebugUtil.i(TAG,"!!!!!Now AntenFrq:"+nowSel);
        // Toast.makeText(this, "!!!!!~~~~Now AntenFrq:"+nowSel,
        // Toast.LENGTH_SHORT).show();
        mApp.mRadarDevice.refreshFileHeader();
        // ??????????????????????????????????????????
        // changeParamsListFromeRadar();
        // hss2016.6.6
        // ????????
//        mApp.mTimewndRuler.invalidate();
//        mApp.mDeepRuler.invalidate();
//        ((HRulerView) mApp.mHorRuler).setShowscanMode();

        // mParamsListAdapter.notifyDataSetChanged();

        /*
         * //?????????????????????? fileName =
         * mApp.mRadarDevice.mSDCardPath+mApp.mRadarDevice
         * .mParamsFilefolderPath; fileName +=
         * mApp.mRadarDevice.mWhellcheckFilename;
         * if(!mApp.mRadarDevice.loadWhellcheckParams(fileName)) {
         *
         * }
         */
        // ????????
        ret = mApp.mRadarDevice.start();
        if (ret == LeftFragment.RADARDEVICE_ERROR_NO) {
            // mRealtimeDIBView.initDIB();
            // mRealtimeDIBView.invalidate();
//            mRealthreadReadingDatas = true; // ????????,????????????
            bRet = true;

            // //??????????????????????activity
            // scanView view = (scanView)findViewById(R.id.viewSinglewave);
            // view.invalidate();

            String name = mApp.mRadarDevice.getParamsPath() + "defSetParams.par";
            // loadSetParamsFile(name); ????????

            // BB80?20170419hss
            // mApp.mRadarDevice.setHandleMode();

            // iv_state.setBackgroundResource(R.drawable.greenpoint);
        } else {
            Log.d("debug_radar", "open radar failed");
            if (ret == LeftFragment.RADARDEVICE_ERROR_OPEN) {
//                showToastMsg("????????????????!");
                Log.d("debug", "????????????????");
            }
            if (ret == LeftFragment.RADARDEVICE_ERROR_STARTCOMMAND) {
//                showToastMsg("????????????????!");
                Log.d("debug", "????????????????");
            }
            // UnloadDriver();
        }

        return bRet;
    }

    private void radarRealStart() {
        mApp.mRadarDevice.mIsUseSoftPlus = false;
        // ????????
        // ??????????????
        // ??????????????????????

        try {
            devicePowerUp();
        } catch (Exception e) {
            DebugUtil.i(TAG, "????????sleep run fail_sleep!");
            Log.d("debug_radar", "????????????");
        }

        boolean bRet = radarStart(8);
        if (!bRet) {
//            showToastMsg("????????????!");
            Log.d("debug_start_radar", "????????????");
        } else {
            Log.d("debug_radar", "????????????");
            mApp.mRadarDevice.continueShow();
            /*
             * tv_antenna = (TextView)view1.findViewById(R.id.id_antenna);
             * tv_antenna.setText(freqStr);
             */
            // ??????????0315
            Log.d("debug_radar", "before work light on");
            if (mApp.mPowerDevice.WorkLightOn()) {
                Log.d("debug_radar", "work light on success");
            } else {
                Log.d("debug_radar", "work light on fail");
                DebugUtil.i(TAG, "????????????????");
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
     * ????????
     */
    private void devicePowerUp() {
        mApp.mPowerDevice.AntennaPowerUp();// ????????
        mApp.mPowerDevice.DisplayPowerUp();// ????????
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
     * ????????
     */
    public void backupBackground() {
        Context c = null;
        // ????????????
        // ????????
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
                DebugUtil.i(TAG, " ??????KEY??IDSC2600/sharedpref??");
                edit.putFloat(KEY, 4.4f);
                boolean ret_edit = edit.commit();
                if (!ret_edit) {
                    Toast.makeText(this, "commit??????", Toast.LENGTH_SHORT).show();
                } else
                    ;
            }
        } else
            ;
        // ????????????????????????????????????????
        mApp.getLightState();
        mApp.getTurnWheelState();
        mApp.getFileDscend();

        // ??????????????????????????????????
        if (mApp.getPowerLightState()) {
            mApp.mPowerDevice.PowerLightOn();
        } else {
            mApp.mPowerDevice.PowerLightOff();
        }
    }

    Thread mRealThread;

    /**
     * ??????????????????
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

    // ????????????????
    public void createStateThread() {
        createTimeThread();
        createBatteryThread();
    }

    // ????????????
    private void createTimeThread() {
        DebugUtil.i(TAG, "createSystemTimeThread");
        TimeThread timeThread = new TimeThread();
        Thread thread = new Thread(timeThread);
        thread.start();
    }

    // ????????
    private void createBatteryThread() {
        BatteryThread batThread = new BatteryThread();
        Thread threadbat = new Thread(batThread);
        threadbat.start();
    }

    // ????GPS????????????????????
    public void openGPSDevice() {
        gpsport.setContext(this);
        gpsport.openSerialPort("/dev/ttySAC1", 115200, 'N', 8, 1, 0);// ????????
    }

    // ????GPS????????
    public void closeGPSDevice() {
        gpsport.closeSerialPort();
    }

    // ??????????????zhzhw??????
    public void closeBar(Context context) {
        try {
            // ????root ????
            Build.VERSION_CODES vc = new Build.VERSION_CODES();
            Build.VERSION vr = new Build.VERSION();
            String ProcID = "79";

            if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH) {
                ProcID = "42"; // ICS AND NEWER
            }

            // ????root ????
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

    // ????????????
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

    // ????????????
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

    // toast????
    public void showToastMsg(String txt) {
        Toast.makeText(this, txt, Toast.LENGTH_SHORT).show();
    }

    // ????????
    private int mrLen = 0;

    class RealtimeRunnable implements Runnable {
        Context mContext; // ??????

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
            // ????????????????????????
            while (!mApp.isRealThreadStop()) {
                scans = 0;
                sleepTime = 200;
                // Log.d("debug_real_time", "second value: " +
                // mApp.isRealThreadReadingDatas());
                // //????????????????????
                st = System.nanoTime();
                if (mApp.isRealThreadReadingDatas()) {
                    sleepTime = mApp.getRealthreadSleepTime();
                    try {
                        // ????????
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
                            // ????????
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

                            // ????????
                            Message msg = new Message();
                            msg.arg1 = REALTIME_THREADMSG_READDATAS;
                            mRealtimeThreadHandler.sendMessage(msg); // hss0425

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        DebugUtil.i("TimeActivity", "RealtimeThread run fail_1!");
                    }
                }
                // //????????
                else {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (Exception e) {
                        DebugUtil.i("TimeActivity", "RealtimeThread run fail_sleep!");
                    }
                }

                // ??????????
                changeInfsDelayNumber++;
                if (changeInfsDelayNumber >= 15)// hss 20180509????????
                {
                    changeInfsDelayNumber = 0;
                    updateMemory();// ????????
                    // //??????????????
                    try {

                        Message msg = new Message();
                        msg.obj = activity;
                        msg.arg1 = REALTIME_THREADMSG_SDCARDINFS;
                        mRealtimeThreadHandler.sendMessage(msg);

                        // ????????jni????????????
                        // StorageManager mStorageManager = (StorageManager)
                        // mContext.getSystemService(Context.STORAGE_SERVICE);
                        // StorageVolume[] storageVolumes =
                        // mStorageManager.getVolumeList();
                    } catch (Exception e) {
                        DebugUtil.i(TAG, "RealtimeThread run fail_5!");
                    }
                }

                // ????????????
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

    // ????????,????????????
    class TimeThread extends Thread {
        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = SYSTEM_TIME; // ??????????????????
                    mStateHandler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } while (true);
        }
    }

    /**
     * @author huangss ????????????
     */
    class BatteryThread extends Thread {
        @Override
        public void run() {
            do {
                try {
                    Message msg = new Message();
                    msg.what = REALTIME_THREADMSG_POWER; // ??????????????????
                    mStateHandler.sendMessage(msg);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }

    /**
     * ??????????????
     */
    public Handler mRealtimeThreadHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // DebugUtil.i("TimeActivity",
            // "mRealtimeThreadHandler handleMessage");
            // og.d("TimeActivity",
            // "mRealtimeThreadHandler handleMessage:"+msg.arg1);
            /**
             * ????????????????????
             */
            if (msg.arg1 == REALTIME_THREADMSG_READDATAS) {

                // 0427hss
                if (mrLen > 0) {
                    // ??????????listviewhss0714
                    // LeftFragment.mRadarParamAdapter.notifyDataSetChanged();

                    // ????????????,????????????????????????hss0425
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
                // ??????????????????????
                setHasReceiveScansInf();
            }

            // /????sd??????????
            if (msg.arg1 == REALTIME_THREADMSG_SDCARDINFS) {
                // Log.d("TimeActivity","start sd");
                long start1 = System.currentTimeMillis();

                changeMemoryInfs();

                long end1 = System.currentTimeMillis();

                // Log.d("TimeActivity","end sd" + (end1-start1));
            }
            // ????????????
            if (msg.arg1 == REALTIME_THREADMSG_OVERSPEED) {
                if (msg.arg2 == 1)
                    beginOverspeedAlarm();// ????????????????
                else if (msg.arg2 == 0)
                    stopOverspeedAlarm();// ????????????????
            }
            // ????GPS????????

            // Log.d("TimeActivity",
            // "end  mRealtimeThreadHandler handleMessage:"+msg.arg1);

        }

        /*
         * ??????????????????????????????????????
         */
        private void drawDIB(int rLen) {
            // DebugUtil.i("KTAG", "Main isDraw!");
            long start1 = System.currentTimeMillis();

            // mRealTimeDIBView.invalidate(); //0522
            long end1 = System.currentTimeMillis();
            DebugUtil.i(TAG,
                        "2 after mRealTimeDIBView.invalidate()" + String.valueOf(end1 - start1));
            // ????????????
            start1 = System.currentTimeMillis();
            // mScanView.invalidate(); 0522

            end1 = System.currentTimeMillis();
            DebugUtil.i(TAG, "3 after mScanView.invalidate()" + String.valueOf(end1 - start1));
            // ????????????
            start1 = System.currentTimeMillis();
            mApp.mHorRuler.invalidate();
            end1 = System.currentTimeMillis();
            DebugUtil.i(TAG, "4 after mHorRuler.invalidate()" + String.valueOf(end1 - start1));
        }
    };

    /**
     * ????????
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

        // ??????
        if (keyCode == KeyEvent.KEYCODE_TAB) {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                isTabPressed = false;
                DebugUtil.i(KTAG, "Main onKeyUp tab!");
                // //2016.6.10 : ????????????????????????????
                if (mIsBackplaying) {
                    DebugUtil.i(KTAG, "Main isPlayBackMode =" + mApp.mRadarDevice.getNowMode());
                    // ??????????????????
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
        // ????F3????????
        else if (keyCode == KeyEvent.KEYCODE_F3) {
            // ????
            // saveBitmapForSdCard();

            if (event.getAction() == KeyEvent.ACTION_UP) {
                // ????
                // saveBitmapForSdCard();

                // ???????? ????????|????????
                if (mApp.mRadarDevice.isSavingMode()) {
                    if (mApp.mRadarDevice.isTemstopSaveMode()) {
                        mApp.mRadarDevice.continueSave();
                    } else {
                        mApp.mRadarDevice.tempStopSave();
                    }
                    return true;
                }
                // ????????????
                if (this.mbackPlayDIBView.isBackPlaying()) {
                    boolean isTemStop = mbackPlayDIBView.isBackplayPause();
                    isTemStop = !isTemStop;
                    mbackPlayDIBView.setBackplayPauseStatus(isTemStop);
                    mIsTempstopBackplay = isTemStop;
                    return true;
                }
                // ????????
                if (!mApp.mRadarDevice.isSetting_Command()) {
                    // /?????????????????? | ????????
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
        // ??????
        else if (keyCode == KeyEvent.KEYCODE_F2) {
            Log.d("debug", "??????????");
            if (event.getAction() == KeyEvent.ACTION_UP) {
                if (mApp.mRadarDevice.isSavingMode()) {
                    stopSave();
                    normalShowRealtimeView();
                    mApp.mListView.requestFocus();
                    try {
                        mLog.print(TAG, "????????!");
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    Log.d("debug", "????????????????");
                    if (!mApp.mRadarDevice.isRunningMode()) {
                        Log.d("debug", "??????????");
                        Toast.makeText(this, "????????????", Toast.LENGTH_SHORT).show();
                    } else if (mApp.mRadarDevice.isSetting_AllHardPlus_Command() ||
                               mApp.mRadarDevice.isSetting_Scanspeed_Command() ||
                               mApp.mRadarDevice.isSetting_StepHardPlus_Command()) {
                        Log.d("debug", "is setting");
                    } else {
                        if (beginSave())// ??????????????????????????????
                        {
                            maxShowRealtimeView();
                            try {
                                mLog.print(TAG, "????????!");
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                mLog.print(TAG, "??????????????????????");
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }// ??????????????????????
                    }
                }
            }
        }
        // ??????
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
                // ??????????????????????????????????????????????
                if (!mApp.mRadarDevice.isSetting_AllHardPlus_Command() &&
                    !mApp.mRadarDevice.isSavingMode() &&
                    !mApp.mRadarDevice.isSetting_StepHardPlus_Command()) {
                    DebugUtil.i("diance", "2.??????????etc");
                    fragLeft.onKeyDown(keyCode, event, this);
                    return false;
                } else if (mApp.mRadarDevice.isSavingMode()) {
                    mApp.mRadarDevice.smallMark();
                } else {
                    DebugUtil.i("diance", "3.??????");
                }
            }
        }
        // ??????
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
        // ????
        else if (keyCode == KeyEvent.KEYCODE_F4) {
            DebugUtil.i(TAG, "OnKeyUp:F4 back");
            mResponFlag = this.BACKPLAY;

            // ??????????
            /**
             * 1.?????????????????????? 2.???????????????????????????????????????? 3.????????????
             */
            if (this.isMaxShowBackPlayView()) {
                // ??????????????
                resetPlayBack();
                this.normalShowBackPlayView();
                mApp.mListView.requestFocus();
            } else {
                if (mApp.mRadarDevice.isSavingMode())
                    ;// ????????????
                    // 0619????????????????????????
                    // else if(mApp.mRadarDevice.isDianCeMode() );
                else {
                    // ??????????????????????
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
                    // ????????????????
                    // getStorageFolderPath();
                    this.showSelectplaybackPopwindow();
                }// ??????????????
            }
        }
        // ??????
        else if (keyCode == KeyEvent.KEYCODE_DEL) {
            // //2016.6.10 ????????????????????????????????????
            if (mApp.mRadarDevice.isRunningMode() && mApp.mRadarDevice.isWhellMode() &&
                mApp.mRadarDevice.isBackOrientMode()) {
                discardDataDialog();
                // mApp.mRadarDevice.discardBackDatas();
            } else
                // //
                if (!isMaxShowBackPlayView()) {
                    if (mApp.mRadarDevice.isSavingMode())
                        ;// ????????????
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
     * ????????????????????
     * <p>
     * //     * @param bitName
     * //     * @param mBitmap
     */
    public void saveBitmapForSdCard() {
        // ????file????
        getWindow().getDecorView().setDrawingCacheEnabled(true);
        Bitmap bmp = getWindow().getDecorView().getDrawingCache();
        long time = System.currentTimeMillis();
        File f = new File("/mnt/sdcard/" + bmp + "_" + time + ".png");
        try {
            // ????
            f.createNewFile();
        } catch (IOException e) {

        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // ????????????????????????
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
        showToastMsg("??????????sdcard??");
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
            mLog.print(TAG, "??????????????");
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
                    ;// || mApp.isCustomSetting());//????????????
                else if (mIsBackplaying)// ??????????
                {
                    // ??????????????
                    resetPlayBack();
                    this.normalShowBackPlayView();
                    mApp.mListView.requestFocus();
                    // fragLeft.onKeyDown(keyCode,event,this); //????left????????
                } else if (isMaxShowRealtimeView())// ??????????????
                {
                    normalShowRealtimeView();
                    mApp.mListView.requestFocus();
                } else {
                    if (mApp.getLeftFragmentTab() == 0 && isTabPressed) {
                        // ??????????????
                        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("??????????????").setMessage("??????????????????").setPositiveButton("????",
                                                                                              new DialogInterface.OnClickListener() {
                                                                                                  @Override
                                                                                                  public void onClick(
                                                                                                          DialogInterface dialog,
                                                                                                          int whichButton) {
                                                                                                      performExitApp();
                                                                                                  }
                                                                                              })
                               .setNegativeButton("????", new DialogInterface.OnClickListener() {
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
        // //????????????????
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
     * ????????????????????????
     */
    public void normalShowRealtimeView() {
        DebugUtil.i(TAG, "enter normalShowRealtimeView!");
        RelativeLayout lFrag;
        LinearLayout.LayoutParams params;

        // ???????? ????????
        lFrag = (RelativeLayout) findViewById(R.id.left_fragment);
        params = (android.widget.LinearLayout.LayoutParams) lFrag.getLayoutParams();
        params.weight = (float) 0.25;
        lFrag.setLayoutParams(params);
        lFrag.setVisibility(View.VISIBLE);

        // ???????????? ???????? ????
        this.mbackplayScanView.setVisibility(View.INVISIBLE);

        // ???? ???????? ???????? ????
        mScanView.setVisibility(View.VISIBLE);

        // ????????????????
        lFrag = (RelativeLayout) findViewById(R.id.right_fragment);
        params = (android.widget.LinearLayout.LayoutParams) lFrag.getLayoutParams();
        params.weight = (float) 0.25;
        lFrag.setLayoutParams(params);
        lFrag.setVisibility(View.VISIBLE);

        LinearLayout mFrag;
        LinearLayout.LayoutParams layoutParam;

        // ???? ???????? ????????
        mFrag = (LinearLayout) findViewById(R.id.layout_VTWRuler);
        mFrag.setVisibility(View.VISIBLE); // 2016.6.10
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.1;
        mFrag.setLayoutParams(layoutParam);

        // ???? ???????? ???????? 2016.6.10
        mFrag = (LinearLayout) findViewById(R.id.layout_BVTWRuler);
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.0;
        mFrag.setLayoutParams(layoutParam);
        mFrag.setVisibility(View.INVISIBLE);

        // ???? ???????? ????????
        mFrag = (LinearLayout) findViewById(R.id.layout_VDDRuler);
        mFrag.setVisibility(View.VISIBLE); // 2016.6.10
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.1;
        mFrag.setLayoutParams(layoutParam);
        // ???? ???????? ???????? 2016.6.10
        mFrag = (LinearLayout) findViewById(R.id.layout_BVDDRuler);
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.0;
        mFrag.setLayoutParams(layoutParam);
        mFrag.setVisibility(View.INVISIBLE);

        // //2016.6.10 ???????????? ?????????????? ???????? ????????
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

        // ????????????????????????
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
     * ??????????????????????????
     */
    public void maxShowRealtimeView() {
        RelativeLayout lFrag;
        LinearLayout.LayoutParams params;
        // ???????? ????????
        lFrag = (RelativeLayout) findViewById(R.id.left_fragment);
        params = (android.widget.LinearLayout.LayoutParams) lFrag.getLayoutParams();
        params.width = 0;
        params.weight = 0;
        lFrag.setLayoutParams(params);
        lFrag.setVisibility(View.INVISIBLE);

        // ???????? ???????? ????
        lFrag = (RelativeLayout) findViewById(R.id.right_fragment);
        params = (android.widget.LinearLayout.LayoutParams) lFrag.getLayoutParams();
        params.width = 0;
        params.weight = 0;
        lFrag.setVisibility(View.INVISIBLE);

        LinearLayout mFrag;
        LinearLayout.LayoutParams layoutParam;
        // //???? ???????? ?????? ????????
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

        // ???? ???????? ?????? ????????
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

        // //2016.6.10 ???????????? ?????????????? ???????? ????????
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
     * ??????????????????????????
     */
    private View mSetPlayBackView;
    private PopupWindow mSetPlayBackWindow;
    private Button bt_pbbackg = null;// ??????
    private Button bt_pbzoom = null;// ????????
    private Button bt_pbxzoom = null;// ????????
    private boolean bl_pbbackg = false, bl_pbzoom = false, bl_pbxzoom = false;// ????????????

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
     * ????????????????
     */
    public void showBackPlayPopupWindow() {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        getWindow().setAttributes(params);

        mSetPlayBackWindow.setBackgroundDrawable(new BitmapDrawable());
        mSetPlayBackWindow.showAtLocation(mSetPlayBackView, Gravity.CENTER | Gravity.BOTTOM, 0, 0);
    }

    /**
     * ????????????????????????????
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
                        // ??????
                        mbackPlayDIBView.setRemoveBackground(true);
                    } else {
                        bt_pbbackg.setTextColor(Color.BLACK);
                        mbackPlayDIBView.setRemoveBackground(false);
                    }
                    break;
                case R.id.bt_pbzoom:
                    bl_pbzoom = !bl_pbzoom;
                    if (bl_pbzoom) {
                        // ??????????????????
                        bt_pbzoom.setTextColor(Color.RED);
                    } else {
                        // ????????
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
                        // ????????
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
     * ??????????????????????
     */
    public Button.OnKeyListener mSettingPlayback_OnKeyHandler = new Button.OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            // TODO Auto-generated method stub
            int id = v.getId();
            // ??????????????????????
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (id) {
                        case R.id.bt_pbzoom:
                            // DebugUtil.toast(IDSC2600MainActivity.this,
                            // "bt_pbzoom");
                            if (bl_pbzoom)// ????????????
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
                            if (bl_pbzoom)// ????????????
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
     * ????????????????????
     *
     * @return
     */
    public boolean resetPlayBack() {
        // ??????????
        bt_pbbackg.setTextColor(Color.BLACK);
        mbackPlayDIBView.setRemoveBackground(false);
        // ????????
        if (mbackPlayDIBView.isZoom())
            mbackPlayDIBView.zoomRestorePlus();
        else
            ;
        bt_pbzoom.setTextColor(Color.BLACK);
        // ????????
        mbackPlayDIBView.mZoomX = 1;
        setBackplayHRulerZoomx(1);
        bt_pbxzoom.setTextColor(Color.BLACK);

        bl_pbbackg = false;
        bl_pbzoom = false;
        bl_pbxzoom = false;
        return true;
    }

    // //????????
    public boolean beginSave() {
        // ??????????????????????????????
        if (mApp.mRadarDevice.isWhellMode()) {
            mApp.mRadarDevice.setBackFillPos(0);
            mApp.mRadarDevice.endBackOrient1();
        } else
            ;

        // ????????????
        if (judgeExistSpace()) {
            if (!mApp.mRadarDevice.createNewDatasFile()) {
                Log.d("debug", "????????????????");
                Toast.makeText(this, "????????????????_1!", Toast.LENGTH_SHORT).show();
                return false;
            } else
                ;
        } else {
            Log.d("debug", "judge failed");
            return false;
        }

        // ????????????
        if (!mApp.mRadarDevice.beginSave()) {
            Log.d("debug", "begin save return false");
            Toast.makeText(this, "????????????_2!", Toast.LENGTH_SHORT).show();
            return false;
        }
        Toast.makeText(this, "????????????!", Toast.LENGTH_SHORT).show();
//        mApp.mRadarDevice.setSaveDetectResult(true);
        Log.d("debug", "????????");
        mApp.mRadarDetect.setSave(true);
        mApp.mRadarDetect.setStoragePath(mApp.mRadarDevice.getStoragePath());
        mApp.mRadarDetect.startRadarDetect();
        Toast.makeText(this, "????????????!", Toast.LENGTH_SHORT).show();

        // ??????????????
        String txt, subString;
        subString = mApp.mRadarDevice.getSaveFilename();
        int sub = subString.lastIndexOf("/");
        subString = subString.substring(sub, subString.length());
        txt = "????:" + subString + "!";
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

    // ??????????????????????????????????
    public boolean judgeExistSpace() {
        // ??????????????
        long size[] = mApp.getSDCardMemory();
        String path = mApp.mRadarDevice.getStoragePath();
        Log.d("debug", "path: " + path);
        DebugUtil.i(TAG, "judgeExistSpace=" + path);

        // ??????????????????0??????????????
        if (size[1] == 0) {
            path += "/test.txt";
            File file = new File(path);

            if (file.mkdir()) {
                if (file.delete())
                    ;
                else
                    ;
                // ????????????????????
                DebugUtil.infoDialog(this, "????????????", "????????????" + size[1] + "");
                return true;
            } else {
                // ??????????
                Log.d("debug", "??????????");
                DebugUtil.infoDialog(this, "??????????????", "????????????");
            }

            return false;
        }
        // ????????????????
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
                DebugUtil.toast(this, "????????????????/??????????:" + txt);

                return true;
            } else {
                // ??????????
                DebugUtil.infoDialog(this, "??????????????", "????????????");
            }
            return true;
        }
    }

    // //????????
    public void stopSave() {
        Toast.makeText(this, "????????????!", Toast.LENGTH_LONG).show();
        TextView txtView;
        txtView = (TextView) findViewById(R.id.textview_savefilename);
        txtView.setVisibility(View.INVISIBLE);
        // txtView.setWidth(0);
        mApp.mRadarDevice.stopSave();
        mApp.mRadarDetect.stopRadarDetect();
        Toast.makeText(this, "??????????????????!", Toast.LENGTH_LONG).show();
    }

    // ??????????????????
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
            txt = "????????:" + distance + "m";
            txtView.setText(txt);
        } else {
            txt = "??????????:" + scans + "??";
            // ??????????????
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

            txt = "????????:" + distance + "m";
            txtView.setText(txt);
        } else {
            scans = mApp.mRadarDevice.getHadRcvScans();
            txt = "????????:" + scans + "??";
            // ??????????????20160614
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

    // //????????
    public boolean radarStop() {
        MyApplication theApp;
        theApp = (MyApplication) getApplicationContext();

        // ????????????
        mApp.setRealThreadReadingDatas(false);
        theApp.mRadarDevice.stop();

        // 2016.6.10
        fragLeft.setRadarState(false);

        // ????????????
        String fileName;
        int sel;
        sel = theApp.mRadarDevice.getAntenFrqSel();
        fileName = theApp.mRadarDevice.INNERSTORAGE + theApp.mRadarDevice.mParamsFilefolderPath;
        fileName += radarDevice.g_antenFrqStr[sel] + ".par";
        // ????????????
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
        // ????sd??????????????????
        String path;
        path = app.mRadarDevice.getParamsPath();
        final List<String> dataList = new ArrayList<String>();
        if (!app.getParamFilenamesFromeSD(dataList, path) || dataList.size() == 0) {
            showToastMsg("??????????????!");
            return;
        }
        // ??????????
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
        new AlertDialog.Builder(this).setTitle("????????????").setSingleChoiceItems(fileNames, Sel,
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
                "????", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mApp.mMainActivity.loadRadarParamsFromeFile(mParamSelFileName);
                    }
                }).setNegativeButton("????", new android.content.DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
    }

    // ????????
    public void loadRadarParamsFromeFile(String pathName) {
        DebugUtil.i(TAG, "loadRadarParamsFromeFile,pathName=" + pathName);

        mApp.mRadarDevice.loadParamsFile(pathName);
        mApp.mRadarDevice.refreshFileHeader();

        RadarParamExpandableListAdapter adapter = (RadarParamExpandableListAdapter) mApp.mListView
                .getExpandableListAdapter();
        adapter.notifyDataSetChanged();

        // ????????
        mApp.mTimewndRuler.invalidate();
        mApp.mDeepRuler.invalidate();
    }

    /**
     * ??????????????
     */
    public boolean mIsMaxshowBackPlayView = false;

    public boolean isMaxShowBackPlayView() {
        return mIsMaxshowBackPlayView;
    }

    /**
     * ??????????????????
     */
    private View backPlayFilePathView;
    private PopupWindow mBackPlayFilePopupWindow;
    private ListView listView;
    private Button btn_up;// ??????????
    private CheckBox cbx_fileOrder;// ????????????
    private TextView file_path;
    private FileAdapter fileAdapter;
    private String filePathString = null; // ??????????????????
    // ??????????????????????
    private View playbackSelectView;
    private PopupWindow mplaybackSelectPopWindow;
    private RadioGroup radioGroupplayback;

    /**
     * ????????????????????????????????????????
     */
    public void setSelectplaybackPopwindow() {
        playbackSelectView = LayoutInflater.from(this).inflate(
                R.layout.layout_playbackselect_popwindow, null);
        radioGroupplayback = (RadioGroup) playbackSelectView.findViewById(R.id.radioGroupPlayback);

        // ??????????????radioButton
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
        // ????????????????????
        mplaybackSelectPopWindow.setFocusable(true);
    }

    // ????????????????
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

    // ????????????????
    public void showSelectplaybackPopwindow() {
        // ????????????????????radio????
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
     * ????????????????????????????????
     */
    public void showBackPlayFilePath() {
        DebugUtil.i(TAG, "enter showBackPlayFilePath!");

        backPlayFilePathView = LayoutInflater.from(this).inflate(R.layout.playback_sdcardlist,
                                                                 null);
        // ??????????????
        listView = (ListView) backPlayFilePathView.findViewById(R.id.listView1);
        btn_up = (Button) backPlayFilePathView.findViewById(R.id.button_up);
        cbx_fileOrder = (CheckBox) backPlayFilePathView.findViewById(R.id.check_fileOrder);
        cbx_fileOrder.setChecked(mApp.getisDecendOrder());
        cbx_fileOrder.setOnCheckedChangeListener(fileOrderChangeListener);

        // ????????????????????????ListView??????????????
        fileAdapter = new FileAdapter(this);
        // ??????????????????????
        listView.setAdapter(fileAdapter);
        DebugUtil.i(TAG, "listview.setOnkeylistener!");

        // ????????????????????????????????????????????????
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
                // ??????????????
                FileAdapter fileNext = (FileAdapter) listView.getAdapter();
                File f = fileNext.list.get(arg2);
                // ??????????????????????
                if (f.isDirectory()) {
                    // ????sd????????????????
                    // mApp.mRadarDevice.getSDCardPath();
                    fileAdapter.scanFiles(filePathString + mApp.mRadarDevice.mLTEFilefolderPath);
                } else {
                    fileNameString = f.getName();
                    if (fileNameString.endsWith(".lte") || fileNameString.endsWith(".LTE")) {
                        fileNameString = f.getAbsolutePath();
                        DebugUtil.i(TAG, "fileNameString=" + fileNameString);

                        // ????????????????????????????????????????
                        if (mResponFlag == BACKPLAY) {
                            // ????????????
                            long length = f.length();
                            int sizeG = (int) (length / 1024 / 1024 / 1024);
                            int max = 4;

                            if (length <= 1024 || sizeG >= max) {
                                DebugUtil.infoDialog(mcontext, "????", "??????????????");
                            } else {
                                mApp.setBackplayFilePath(fileNameString);
                                maxShowBackPlayView();
                                beginBackplayFile(fileNameString);
                            }
                            mBackPlayFilePopupWindow.dismiss();
                        }// ??????????
                        else if (mResponFlag == DELETE) {
                            DebugUtil.i(TAG, "Flag = delete,fileNameString=" + fileNameString);

                            new AlertDialog.Builder(MultiModeLifeSearchActivity.this).setTitle(
                                    "?????????").setNegativeButton("????",
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
                                                                           builder.setTitle("????????")
                                                                                  .setMessage(
                                                                                          "??????????")
                                                                                  .setNegativeButton(
                                                                                          "????",
                                                                                          new DialogInterface.OnClickListener() {
                                                                                              @Override
                                                                                              public void onClick(
                                                                                                      DialogInterface dialog,
                                                                                                      int whichButton) {
                                                                                                  fileAdapter
                                                                                                          = new FileAdapter(
                                                                                                          MultiModeLifeSearchActivity.this);
                                                                                                  // ??????????????????????
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
                                                                           builder.setTitle("????????")
                                                                                  .setMessage(
                                                                                          "??????????")
                                                                                  .setNegativeButton(
                                                                                          "????",
                                                                                          new DialogInterface.OnClickListener() {
                                                                                              @Override
                                                                                              public void onClick(
                                                                                                      DialogInterface dialog,
                                                                                                      int whichButton) {
                                                                                              }
                                                                                          }).show();
                                                                       }
                                                                   }
                                                               }).setPositiveButton("????",
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

                        }// ??????????
                    } else if (fileNameString.endsWith(".txt")) {
                        openFile(f);
                    } else {
                        Toast.makeText(MultiModeLifeSearchActivity.this, "????????????????",
                                       Toast.LENGTH_SHORT).show();
                        Toast.makeText(MultiModeLifeSearchActivity.this, fileNameString,
                                       Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // ??????????????????
        fileAdapter.scanFiles(filePathString + mApp.mRadarDevice.mLTEFilefolderPath);

        // ??????????????????
        btn_up.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // ??????????
                showSelectplaybackPopwindow();
                mBackPlayFilePopupWindow.dismiss();
            }
        });

        mBackPlayFilePopupWindow = new PopupWindow(backPlayFilePathView,
                                                   android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                                                   android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        // ????????????????????
        mBackPlayFilePopupWindow.setFocusable(true);

        int xPos = backPlayFilePathView.getHeight();
        int yPos = backPlayFilePathView.getWidth();

        this.mBackPlayFilePopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mBackPlayFilePopupWindow.setWidth(256);// ????????

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
                    // ??????????????????????
                    listView.setAdapter(fileAdapter);
                    fileAdapter.scanFiles(filePathString + mApp.mRadarDevice.mLTEFilefolderPath);
                    // ????????
                    break;
            }
        }
    };

    // ????????????
    private boolean onceDialog = false;
    public View.OnKeyListener mListViewOnKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            DebugUtil.i(TAG, "0.mListViewOnKeyListener =" + keyCode);

            // ????????????????????????????
            if (keyCode == KeyEvent.KEYCODE_DEL && !isMaxShowBackPlayView()) {
                DebugUtil.i(TAG, "1.keyCode_del!,fileNameString=" + fileNameString);
                if (!onceDialog) {
                    onceDialog = true;
                    new AlertDialog.Builder(MultiModeLifeSearchActivity.this).setTitle("?????????")
                                                                             .setNegativeButton(
                                                                                     "????",
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
                                                                                                         "????????")
                                                                                                        .setMessage(
                                                                                                                "??????????")
                                                                                                        .setNegativeButton(
                                                                                                                "????",
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
                                                                                                                        // ??????????????????????
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
                                                                                                         "????????")
                                                                                                        .setMessage(
                                                                                                                "??????????")
                                                                                                        .setNegativeButton(
                                                                                                                "????",
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
                                                                                     "????",
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

    // ??????????????????
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

    // ????????
    private void openFile(File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // ????intent??Action????
        intent.setAction(Intent.ACTION_VIEW);
        // ????intent??data??Type??????
        intent.setDataAndType(Uri.fromFile(file), "text/plain");
        // ????
        startActivity(intent);
    }

    // ????????????
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
     * ????????????????????
     */
    public void normalShowBackPlayView() {
        stopBackplayFile();
        normalShowRealtimeView();
        mIsMaxshowBackPlayView = false;
    }

    /**
     * ??????????????????
     */
    public void maxShowBackPlayView() {
        mIsMaxshowBackPlayView = true;
        mIsMaxshowRealtimeView = false; // 2016.6.10

        RelativeLayout lFrag;
        LinearLayout.LayoutParams params;

        // ????????????????
        lFrag = (RelativeLayout) findViewById(R.id.left_fragment);
        params = (android.widget.LinearLayout.LayoutParams) lFrag.getLayoutParams();
        params.width = 0;
        params.weight = 0;
        lFrag.setLayoutParams(params);
        lFrag.setVisibility(View.INVISIBLE);

        // ???????? ???????? ????
        this.mbackplayScanView.setVisibility(View.VISIBLE);
        // ???????????? ???????? ????
        mScanView.setVisibility(View.INVISIBLE);

        lFrag = (RelativeLayout) findViewById(R.id.right_fragment);
        params = (android.widget.LinearLayout.LayoutParams) lFrag.getLayoutParams();
        params.width = 0;
        params.weight = 0;
        lFrag.setVisibility(View.VISIBLE);
        lFrag.setLayoutParams(params);

        LinearLayout mFrag;
        LinearLayout.LayoutParams layoutParam;

        // //2016.6.10 ???????????? ?????????????????????? ????????
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

        // //2016.6.10 ???????????? ?????????????????????? ????????
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

        // //2016.6.10 ???????????? ?????????????? ???????? ????????
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

        // //????????????
        mFrag = (LinearLayout) findViewById(R.id.layout_backplayview);
        mFrag.setVisibility(View.VISIBLE); // 2016.6.10
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.9;
        mFrag.setLayoutParams(layoutParam);

        // ???? ???????? ????
        mFrag = (LinearLayout) findViewById(R.id.layout_realtimeview);
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = 0;
        layoutParam.width = 0;
        mFrag.setLayoutParams(layoutParam);
        mFrag.setVisibility(View.INVISIBLE);
    }

    /**
     * ????????
     */
    public boolean beginBackplayFile(String fileName) {
        boolean ret = true;
        DebugUtil.i(TAG, String.valueOf(mIsBackplaying));

        if (mApp.mRadarDevice.setPlayBackMode())
            ;
        else
            DebugUtil.i(TAG, "setPlayBackMode error!");

        // ????????????????????????????
        if (mIsBackplaying) {
            ret = stopBackplayFile();
        }
        if (!ret) {
            return ret;
        }

        // ??????????????
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
            // ????????????
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
     * ????????
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
     * ????????????
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

            // 0316????????????10????????????
//            double tempLevel = (leftLevel - 15) * 100 / 85;
//            leftLevel = (int) tempLevel;

            // ??????????0
            if (leftLevel < 0) {
                leftLevel = 0;
            } else
                ;

            // ????100??????
            if (leftLevel > 100) {
                return;
            } else
                ;

            txtViewPower.setText("????:" + leftLevel + "%");

            System.err.println("??????" + leftLevel);

            // ??????????????????????????????
            if (leftLevel <= WARNBATTERYLEVEL) {
                txtViewPower.setTextColor(Color.RED);
                if (!lowPowerInfo) {
                    lowPowerInfo = true;
                    // ????????????????
                    DebugUtil.infoDialog(this, "????????", "??????????????????????????");
                    // ??????????????
                    mApp.mPowerDevice.BatLightOn();
                } else
                    ;
            } else {
                // ??????????
                mApp.mPowerDevice.BatLightOff();
                txtViewPower.setTextColor(Color.GREEN);
                // ??????????????????????????????????????????
                if (lowPowerInfo) {
                    lowPowerInfo = false;// ??????????????
                } else
                    ;
            }
        } else {
            System.err.println("??????????");
            // DebugUtil.i(TAG,"????????????!");
            txtViewPower.setText("????:??????????!");
            // +"leftLevel="+String.valueOf(leftBattery)
            // +"fullBattery="+String.valueOf(fullBattery));
            txtViewPower.setTextColor(Color.RED);
            // ????????????????
        }
    }

    /**
     * ??????????????????
     */
    private TextView txtViewSpace;
    /**
     * 20170315????????????????????????????100M??????
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
         * //0315????????????100M???? if( left < 100 && mApp.mRadarDevice.isSavingMode()
         * ) { this.stopSave(); normalShowRealtimeView();
         * mApp.mListView.requestFocus(); stopSpark();
         * txtViewSpace.setTextColor(Color.RED); DebugUtil.infoDialog(this,
         * "????????","??????????????????"); } else { if( left <= 500 ) { // DebugUtil.i(TAG,
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
            txtChangeMemory = "(SD??)????????:";
        } else if (mApp.mRadarDevice.isSelectUSB()) {
            txtChangeMemory = "(USB??)????????:";
        } else if (mApp.mRadarDevice.isSelectMemory()) {
            txtChangeMemory = "(????)????????:";
        }
        infsMemory = mApp.getSDCardMemory();
    }

    // ??????????????
    private Timer bat_timer = null;
    private TimerTask bat_task = null;
    private boolean bat_runTask = false;

    public void sparkBattery() {
        DebugUtil.i(TAG, "spark battery!");
    }

    // ????????????
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

    // ????????
    public void beginOverspeedAlarm() {
        TextView txtView;
        txtView = (TextView) findViewById(R.id.textview_overspeed);
        txtView.setVisibility(View.VISIBLE);
        String txt;
        txt = "??????!";
        txtView.setText(txt);
        showOverSpeedPopupWindow();
    }

    // ????????????
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
        // ??????????????
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("????????????");
        builder.setTitle("???????????????????").setMessage("????????").setNegativeButton("??",
                                                                            new DialogInterface.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(
                                                                                        DialogInterface dialog,
                                                                                        int whichButton) {
                                                                                    mApp.mRadarDevice
                                                                                            .discardBackDatas();
                                                                                }
                                                                            }).setPositiveButton(
                "??", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                }).show();
    }

    // ????????
    private View overSpeedView;
    private PopupWindow mOverSpeedPopWindow;

    public boolean setOverSpeedPopupWindow() {
        overSpeedView = View.inflate(this, R.layout.overspeedalarm_popwindow, null);
        mOverSpeedPopWindow = new PopupWindow(overSpeedView, 100,
                                              android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        mOverSpeedPopWindow.setFocusable(false);

        return true;
    }

    // ????????
    public void showOverSpeedPopupWindow() {
        DebugUtil.i(TAG, "showOverSpeedPopupWindow!");
        mOverSpeedPopWindow.setBackgroundDrawable(new BitmapDrawable());
        mOverSpeedPopWindow.showAtLocation(overSpeedView, Gravity.CENTER | Gravity.CENTER, 0, 0);
    }

    /**
     * ????????????????
     */
    public double setDigits(double inputNum, int digitNum) {
        double tempNum = inputNum;
        BigDecimal b = new BigDecimal(tempNum);
        tempNum = b.setScale(digitNum, BigDecimal.ROUND_HALF_DOWN).doubleValue();// ????????
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
