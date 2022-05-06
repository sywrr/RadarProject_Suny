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
 * ���̿���
 *
 * @author huangss
 */
public class MultiModeLifeSearchActivity extends Activity implements UncaughtExceptionHandler {
    // ��ͼ����
    private TextView tv, textview_systemtime;
    public realTimeDIBView mRealTimeDIBView; // ʵʱ��ʾ��
    public scanView mScanView;
    public backPlayDIBView mbackPlayDIBView; // �ط���ʾ��
    public BackplayScanView mbackplayScanView;
    private LeftFragment fragLeft; // ��ȡfragement��ʵ��
    private RightFragment fragRight; // ��ȡrightfragment��ʵ��
    private MyApplication mApp; // ȫ�ֲ���
    private String TAG = "IDSC2600MainActivity"; // ����tag
    private String KTAG = "KTAG"; // ������Ӧ�ı�־
    private String SERIALPORT = "/dev/ttySAC3"; // ���ڶ˿ں�
    // ������
    private ExpandableListView elv;
    private GPSDevice gpsport = new GPSDevice(); // GPS�˿�
    // �ط�״̬��־
    private boolean mIsBackplaying = false; // ���ڻطű�־
    private boolean mBackplayPause = false; // ��ͣ�ط�
    private boolean mBackplayFocusView = false; // �ط�ʱ�����λ�ã����ط���Ϊfalse���Ҳ൥����Ϊtrue
    private String mBackplayFileName; // ���ڻطŵ������ļ�
    private final int BACKPLAY_FORWARD_DIR = 1; // ����ǰ
    private final int BACKPLAY_BACK_DIR = 2; // �����
    private int mBackplayDir = BACKPLAY_FORWARD_DIR; // �طŵķ���
    public boolean mIsTempstopBackplay = false; // �Ƿ���ͣ�ط�
    private FileInputStream mBackplayFile = null;
    private FileHeader mBackplayFileHeader = new FileHeader();
    private int mResponFlag = 0; // ��Ӧ��־�����ֻطŻ���ɾ����1�ǻطţ�2��ɾ��
    final private int BACKPLAY = 1;
    final private int DELETE = 2;

    private int REALTIME_THREADMSG_READDATAS = 1; // ��ȡ�״�������Ϣ
    private int REALTIME_THREADMSG_SDCARDINFS = 2; // ��ȡ�洢�ռ�ʣ�࣬ͬʱ�����˵�ص���
    private int REALTIME_THREADMSG_OVERSPEED = 3; // ���ٱ���
    private int REALTIME_THREADMSG_POWER = 4; // ��ȡ��ص���
    private int REALTIME_THREADMSG_GPS = 5; // ��ȡGPS������Ƶ��20HZ
    private int SYSTEM_TIME = 6; // ÿ�����ϵͳʱ��һ��
    private int MAXFULLBATTERYTIME = 4800; // �������ʱ��
    private int MINFULLBATTERYTIME = 0; // ��С����ʱ��
    private int WARNBATTERYLEVEL = 10; // ��ʾ�ĵ����ٷֱ�
    private static final String FileName = "setFactoryParams"; // �ļ�����
    private static final String PackageName = "com.example.setsteptime";
    private static final String KEY = "stepelapsed";
    private static int MODE = Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE;
    private Bitmap mBitmap;
    // ��־�ļ�
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

        requestWindowFeature(Window.FEATURE_NO_TITLE); // ���ر�����
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                  WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
                             WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        getWindow().setAttributes(params);
        super.setContentView(R.layout.activity_main); // Ĭ�ϲ��ֹ�����

        mApp = (MyApplication) this.getApplicationContext();
        mApp.mRadarDevice.setContext(this);
        mApp.mRadarDevice.newPositiveData();
        mApp.mRadarDevice.mMainActivity = this;

        if (mApp.mPowerDevice.openPowerDevice() < 0)
            DebugUtil.i(TAG, "powerDevice<0");
        else
            DebugUtil.i(TAG, "powerDevice>=0");

        // �򿪵�Դ
        // devicePowerUp();

        System.loadLibrary("Detect");

        // //ʵʱ������ͼ
        mRealTimeDIBView = (realTimeDIBView) findViewById(R.id.layoutRealView);
        mRealTimeDIBView.setZOrderOnTop(true); // 0531

        mScanView = (scanView) findViewById(R.id.viewSinglewave);
        mScanView.setZOrderOnTop(true);

        // mScanView = new scanView(this);
        // //�طŴ�����ͼ
        mbackPlayDIBView = (backPlayDIBView) findViewById(R.id.layoutBackPlayView);
        mbackPlayDIBView.mParentActivity = this;
        mbackplayScanView = (BackplayScanView) findViewById(R.id.backplayScanview);

        // //��¼���ֱ��
        // ʵʱ�ɼ�ʱ�ı��
        mApp.mTimewndRuler = findViewById(R.id.viewVTWRuler);
        mApp.mDeepRuler = findViewById(R.id.viewDDRuler);
        mApp.mHorRuler = findViewById(R.id.viewHRuler);
        // 2016.6.10 �ط�����ʱ�ı��
        mApp.mBTimewndRuler = findViewById(R.id.viewBVTWRuler);
        ((BackPlayVRulerView) (mApp.mBTimewndRuler)).setShowTimewndType();
        mApp.mBDeepRuler = findViewById(R.id.viewBDDRuler);
        ((BackPlayVRulerView) (mApp.mBDeepRuler)).setShowDeepType();
        mApp.mBHorRuler = findViewById(R.id.viewBHRuler);
        ((BackPlayHRulerView) (mApp.mBHorRuler)).mApp = mApp;
        mApp.mMainActivity = this;

        // ϵͳʱ��
        textview_systemtime = (TextView) findViewById(R.id.textview_systemtime);

        // ����ˮƽ��ߵ����ҿհ׿��
        HRulerView hRView = (HRulerView) mApp.mHorRuler;
        hRView.mApp = mApp;

        // �õ���Ļ�Ŀ�Ⱥ͸߶�
        int sW, sH;
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        sW = dm.widthPixels;
        sH = dm.heightPixels;
        mApp.setScreenRange(sW, sH);
        // ��ñ���ͼƬ1019
        /*
         * WallpaperManager manager = WallpaperManager.getInstance(this);
         * BitmapDrawable drw = (BitmapDrawable)manager.getDrawable();
         * this.mBitmap = drw.getBitmap();
         */

        filePathString = mApp.mRadarDevice.getStoragePath();
        // ���ûط�
        setSelectplaybackPopwindow();
        // ���ûطŲ���
        setBackPlayPopupWindow();
        // ���ó��ٱ�������
        setOverSpeedPopupWindow();
        // ������·�߳�
        createNetwork();
        // ����ʵʱ�߳�hss0427
        createRealtimeThread();
        // ����ʱ�����hss427
        createStateThread();
        // ����GPS�˿�
        // openGPSDevice();
        backupBackground();
        // ���fragmentʵ��
        fragLeft = new LeftFragment();

        startRadar();

        fragRight = new RightFragment();

        // ������ȡһ�ε�ص���
        changePowerInfs();

        // ��ȡGPS������Ϣ
        // GPSDevice gpsDevice = new GPSDevice();
        // gpsDevice.openSerialPort("/dev/ttySAC0",115200,'N',8, 1, 0);//��������

        /*
         * SerialPort srlport = new SerialPort(); DebugUtil.i(TAG,
         * "����GPS�������ݣ�"); srlport.openSerialPort("/dev/ttySAC0",115200,'N',8, 1,
         * 0);//��������
         */

        // ������ָʾ�ƶ���
        // mApp.mPowerDevice.PowerLightOn();
        // mApp.mPowerDevice.WorkLightOn();
        // mApp.mPowerDevice.BatLightOn();

        // DisplayMetrics dmc = new DisplayMetrics();
        // getWindowManager().getDefaultDisplay().getMetrics(dmc);
        // String strResolution = "Resolution:"+dmc.widthPixels
        // +"*"+dmc.heightPixels;
        // showToastMsg(strResolution);

        // ������־�ļ�
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
        // ����״ﴦ�ڹ���״̬
        if (mApp.mRadarDevice.isRunningMode()) {
            DebugUtil.i(TAG, "radarStart : device has start!");
            return true;
        }
        // װ������
        // loadDriver();

        // ����ѡ�������Ƶ�ʣ�װ��ָ���Ĳ����ļ�
        String fileName;
        fileName = mApp.mRadarDevice.getInnerStoragePath() +
                   mApp.mRadarDevice.mParamsFilefolderPath;
        fileName += radarDevice.g_antenFrqStr[frqIndex] + ".par";
        // Toast.makeText(this, fileName, Toast.LENGTH_SHORT).show();

        /**
         * ����Ƶ�����ļ����ò���
         */
        mApp.mRadarDevice.setAntenFrq(frqIndex);
        if (!mApp.mRadarDevice.onlyLoadParamsFromeFile(fileName)) {
            // DebugUtil.i(TAG,"!!!!!~~~~Now setAntenFrq:"+frqIndex);
            // Toast.makeText(this, "~~~~Now AntenFrq:"+frqIndex,
            // Toast.LENGTH_SHORT).show();

            // ��������Ƶ���������������Ĳ���������������
            mApp.mRadarDevice.changeParamsFromeAntenfrq(frqIndex);
        } else {
        }

        if (!mApp.mRadarDevice.loadSystemSetFile()) {
            System.out.println("����ϵͳ���ò���ʧ��");
        } else {
            System.out.println("����ϵͳ���ò����ɹ�");
        }

        /**
         * ��Ĭ�ϲ�����ļ����ò���ǲ���
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
        // ���²���������ʾֵ
        int nowSel = mApp.mRadarDevice.getAntenFrqSel();
        // DebugUtil.i(TAG,"!!!!!Now AntenFrq:"+nowSel);
        // Toast.makeText(this, "!!!!!~~~~Now AntenFrq:"+nowSel,
        // Toast.LENGTH_SHORT).show();
        mApp.mRadarDevice.refreshFileHeader();
        // ���ݵ�ǰ���״�������ò����б�����ݣ�����
        // changeParamsListFromeRadar();
        // hss2016.6.6
        // �����б�
//        mApp.mTimewndRuler.invalidate();
//        mApp.mDeepRuler.invalidate();
//        ((HRulerView) mApp.mHorRuler).setShowscanMode();

        // mParamsListAdapter.notifyDataSetChanged();

        /*
         * //װ�ز����У�������ļ� fileName =
         * mApp.mRadarDevice.mSDCardPath+mApp.mRadarDevice
         * .mParamsFilefolderPath; fileName +=
         * mApp.mRadarDevice.mWhellcheckFilename;
         * if(!mApp.mRadarDevice.loadWhellcheckParams(fileName)) {
         *
         * }
         */
        // �����״�
        ret = mApp.mRadarDevice.start();
        if (ret == LeftFragment.RADARDEVICE_ERROR_NO) {
            // mRealtimeDIBView.initDIB();
            // mRealtimeDIBView.invalidate();
//            mRealthreadReadingDatas = true; // ���ñ�־,��ʼ��ȡ����
            bRet = true;

            // //��������������ʾ�ŵ���activity
            // scanView view = (scanView)findViewById(R.id.viewSinglewave);
            // view.invalidate();

            String name = mApp.mRadarDevice.getParamsPath() + "defSetParams.par";
            // loadSetParamsFile(name); �����ļ�

            // BB80?20170419hss
            // mApp.mRadarDevice.setHandleMode();

            // iv_state.setBackgroundResource(R.drawable.greenpoint);
        } else {
            Log.d("debug_radar", "open radar failed");
            if (ret == LeftFragment.RADARDEVICE_ERROR_OPEN) {
//                showToastMsg("�״��豸�򿪴���!");
                Log.d("debug", "�״��豸�򿪴���");
            }
            if (ret == LeftFragment.RADARDEVICE_ERROR_STARTCOMMAND) {
//                showToastMsg("���Ϳ����������!");
                Log.d("debug", "���Ϳ����������");
            }
            // UnloadDriver();
        }

        return bRet;
    }

    private void radarRealStart() {
        mApp.mRadarDevice.mIsUseSoftPlus = false;
        // ��ʱ�ϵ�
        // �ϵ���ȡ����
        // ��ȡ����оƬ�Ĵ�����Ϣ

        try {
            devicePowerUp();
        } catch (Exception e) {
            DebugUtil.i(TAG, "��ʱ�ϵ�sleep run fail_sleep!");
            Log.d("debug_radar", "�豸�ϵ����");
        }

        boolean bRet = radarStart(8);
        if (!bRet) {
//            showToastMsg("�����״����!");
            Log.d("debug_start_radar", "�����״����");
        } else {
            Log.d("debug_radar", "�����״�ɹ�");
            mApp.mRadarDevice.continueShow();
            /*
             * tv_antenna = (TextView)view1.findViewById(R.id.id_antenna);
             * tv_antenna.setText(freqStr);
             */
            // ����������0315
            Log.d("debug_radar", "before work light on");
            if (mApp.mPowerDevice.WorkLightOn()) {
                Log.d("debug_radar", "work light on success");
            } else {
                Log.d("debug_radar", "work light on fail");
                DebugUtil.i(TAG, "�����ƿ���ʧ�ܣ�");
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
     * �豸�ϵ�
     */
    private void devicePowerUp() {
        mApp.mPowerDevice.AntennaPowerUp();// �����ϵ�
        mApp.mPowerDevice.DisplayPowerUp();// ��ѹ�ø�
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
     * �ָ�״̬
     */
    public void backupBackground() {
        Context c = null;
        // ���ز���ʱ��
        // ��ȡ����
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
                DebugUtil.i(TAG, " û�ҵ�KEY��IDSC2600/sharedpref��");
                edit.putFloat(KEY, 4.4f);
                boolean ret_edit = edit.commit();
                if (!ret_edit) {
                    Toast.makeText(this, "commitʧ�ܣ�", Toast.LENGTH_SHORT).show();
                } else
                    ;
            }
        } else
            ;
        // ����Ĭ��״̬������ַ�ת״̬�ͱ����״̬
        mApp.getLightState();
        mApp.getTurnWheelState();
        mApp.getFileDscend();

        // ���ݱ����״̬���򿪻��߹رձ����
        if (mApp.getPowerLightState()) {
            mApp.mPowerDevice.PowerLightOn();
        } else {
            mApp.mPowerDevice.PowerLightOff();
        }
    }

    Thread mRealThread;

    /**
     * ����ʵʱ��ȡ���߳�
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

    // ����״̬�����߳�
    public void createStateThread() {
        createTimeThread();
        createBatteryThread();
    }

    // ϵͳʱ����ʾ
    private void createTimeThread() {
        DebugUtil.i(TAG, "createSystemTimeThread");
        TimeThread timeThread = new TimeThread();
        Thread thread = new Thread(timeThread);
        thread.start();
    }

    // ������ʾ
    private void createBatteryThread() {
        BatteryThread batThread = new BatteryThread();
        Thread threadbat = new Thread(batThread);
        threadbat.start();
    }

    // ��GPS�˿ڣ��Ӵ��ڽ�������
    public void openGPSDevice() {
        gpsport.setContext(this);
        gpsport.openSerialPort("/dev/ttySAC1", 115200, 'N', 8, 1, 0);// ��������
    }

    // �ر�GPS�˿ڶ�ȡ
    public void closeGPSDevice() {
        gpsport.closeSerialPort();
    }

    // �رչ���������zhzhw������
    public void closeBar(Context context) {
        try {
            // ��Ҫroot Ȩ��
            Build.VERSION_CODES vc = new Build.VERSION_CODES();
            Build.VERSION vr = new Build.VERSION();
            String ProcID = "79";

            if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH) {
                ProcID = "42"; // ICS AND NEWER
            }

            // ��Ҫroot Ȩ��
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

    // ���ü��غ���
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

    // ����ȫ����ʾ
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

    // toast����
    public void showToastMsg(String txt) {
        Toast.makeText(this, txt, Toast.LENGTH_SHORT).show();
    }

    // ʵʱ�߳�
    private int mrLen = 0;

    class RealtimeRunnable implements Runnable {
        Context mContext; // ����ͼ

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
            // û��Ҫ��ֹͣ�̵߳������
            while (!mApp.isRealThreadStop()) {
                scans = 0;
                sleepTime = 200;
                // Log.d("debug_real_time", "second value: " +
                // mApp.isRealThreadReadingDatas());
                // //���Ҫ���ȡ�״�����
                st = System.nanoTime();
                if (mApp.isRealThreadReadingDatas()) {
                    sleepTime = mApp.getRealthreadSleepTime();
                    try {
                        // ��ȡ����
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
                            // ����λͼ
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

                            // �����ź�
                            Message msg = new Message();
                            msg.arg1 = REALTIME_THREADMSG_READDATAS;
                            mRealtimeThreadHandler.sendMessage(msg); // hss0425

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        DebugUtil.i("TimeActivity", "RealtimeThread run fail_1!");
                    }
                }
                // //�����߳�
                else {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (Exception e) {
                        DebugUtil.i("TimeActivity", "RealtimeThread run fail_sleep!");
                    }
                }

                // ��ʱ���µ�
                changeInfsDelayNumber++;
                if (changeInfsDelayNumber >= 15)// hss 20180509��ʱˢ��
                {
                    changeInfsDelayNumber = 0;
                    updateMemory();// ���´洢
                    // //�洢��ʣ��ռ�
                    try {

                        Message msg = new Message();
                        msg.obj = activity;
                        msg.arg1 = REALTIME_THREADMSG_SDCARDINFS;
                        mRealtimeThreadHandler.sendMessage(msg);

                        // ����ʹ��jni�ص��洢�ռ�
                        // StorageManager mStorageManager = (StorageManager)
                        // mContext.getSystemService(Context.STORAGE_SERVICE);
                        // StorageVolume[] storageVolumes =
                        // mStorageManager.getVolumeList();
                    } catch (Exception e) {
                        DebugUtil.i(TAG, "RealtimeThread run fail_5!");
                    }
                }

                // ����Ƿ���
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

    // ϵͳʱ��,ÿ�����һ��
    class TimeThread extends Thread {
        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = SYSTEM_TIME; // ��Ϣ��һ������ֵ��
                    mStateHandler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } while (true);
        }
    }

    /**
     * @author huangss ��ص�����ȡ
     */
    class BatteryThread extends Thread {
        @Override
        public void run() {
            do {
                try {
                    Message msg = new Message();
                    msg.what = REALTIME_THREADMSG_POWER; // ��Ϣ��һ������ֵ��
                    mStateHandler.sendMessage(msg);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }

    /**
     * �����͵��ź�
     */
    public Handler mRealtimeThreadHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // DebugUtil.i("TimeActivity",
            // "mRealtimeThreadHandler handleMessage");
            // og.d("TimeActivity",
            // "mRealtimeThreadHandler handleMessage:"+msg.arg1);
            /**
             * �����ȡ�״�������Ϣ
             */
            if (msg.arg1 == REALTIME_THREADMSG_READDATAS) {

                // 0427hss
                if (mrLen > 0) {
                    // �ȸ������listviewhss0714
                    // LeftFragment.mRadarParamAdapter.notifyDataSetChanged();

                    // ����λ��ɫͼ,�ƶ���������ʱ�򲻸���hss0425
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
                // �����Ѿ��ɼ��ĵ�����Ϣ
                setHasReceiveScansInf();
            }

            // /����sd��������Ϣ
            if (msg.arg1 == REALTIME_THREADMSG_SDCARDINFS) {
                // Log.d("TimeActivity","start sd");
                long start1 = System.currentTimeMillis();

                changeMemoryInfs();

                long end1 = System.currentTimeMillis();

                // Log.d("TimeActivity","end sd" + (end1-start1));
            }
            // ��������Ϣ
            if (msg.arg1 == REALTIME_THREADMSG_OVERSPEED) {
                if (msg.arg2 == 1)
                    beginOverspeedAlarm();// ���ͳ��ٱ����ź�
                else if (msg.arg2 == 0)
                    stopOverspeedAlarm();// �������ٱ����ź�
            }
            // ����GPS���ݱ���

            // Log.d("TimeActivity",
            // "end  mRealtimeThreadHandler handleMessage:"+msg.arg1);

        }

        /*
         * ����ѡ��Բɼ������ݻ��ѻ�������α��ͼ
         */
        private void drawDIB(int rLen) {
            // DebugUtil.i("KTAG", "Main isDraw!");
            long start1 = System.currentTimeMillis();

            // mRealTimeDIBView.invalidate(); //0522
            long end1 = System.currentTimeMillis();
            DebugUtil.i(TAG,
                        "2 after mRealTimeDIBView.invalidate()" + String.valueOf(end1 - start1));
            // ���µ�������
            start1 = System.currentTimeMillis();
            // mScanView.invalidate(); 0522

            end1 = System.currentTimeMillis();
            DebugUtil.i(TAG, "3 after mScanView.invalidate()" + String.valueOf(end1 - start1));
            // ����ˮƽ���
            start1 = System.currentTimeMillis();
            mApp.mHorRuler.invalidate();
            end1 = System.currentTimeMillis();
            DebugUtil.i(TAG, "4 after mHorRuler.invalidate()" + String.valueOf(end1 - start1));
        }
    };

    /**
     * ����ʱ��
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

        // �л���
        if (keyCode == KeyEvent.KEYCODE_TAB) {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                isTabPressed = false;
                DebugUtil.i(KTAG, "Main onKeyUp tab!");
                // //2016.6.10 : ���ڻط�ʱ�����ɻص��ɼ�ģʽ
                if (mIsBackplaying) {
                    DebugUtil.i(KTAG, "Main isPlayBackMode =" + mApp.mRadarDevice.getNowMode());
                    // ��������ѡ��Ի���
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
        // ��¼F3����״̬
        else if (keyCode == KeyEvent.KEYCODE_F3) {
            // ����
            // saveBitmapForSdCard();

            if (event.getAction() == KeyEvent.ACTION_UP) {
                // ����
                // saveBitmapForSdCard();

                // ���ڱ��� ��ͣ����|��������
                if (mApp.mRadarDevice.isSavingMode()) {
                    if (mApp.mRadarDevice.isTemstopSaveMode()) {
                        mApp.mRadarDevice.continueSave();
                    } else {
                        mApp.mRadarDevice.tempStopSave();
                    }
                    return true;
                }
                // ���ڻط�״̬
                if (this.mbackPlayDIBView.isBackPlaying()) {
                    boolean isTemStop = mbackPlayDIBView.isBackplayPause();
                    isTemStop = !isTemStop;
                    mbackPlayDIBView.setBackplayPauseStatus(isTemStop);
                    mIsTempstopBackplay = isTemStop;
                    return true;
                }
                // ƽ��״̬
                if (!mApp.mRadarDevice.isSetting_Command()) {
                    // /û�б��棬��ͣ��ʾ | ������ʾ
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
        // �����
        else if (keyCode == KeyEvent.KEYCODE_F2) {
            Log.d("debug", "���±����");
            if (event.getAction() == KeyEvent.ACTION_UP) {
                if (mApp.mRadarDevice.isSavingMode()) {
                    stopSave();
                    normalShowRealtimeView();
                    mApp.mListView.requestFocus();
                    try {
                        mLog.print(TAG, "�������!");
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    Log.d("debug", "û�д��ڱ���״̬");
                    if (!mApp.mRadarDevice.isRunningMode()) {
                        Log.d("debug", "�״�δ����");
                        Toast.makeText(this, "�״�δ������", Toast.LENGTH_SHORT).show();
                    } else if (mApp.mRadarDevice.isSetting_AllHardPlus_Command() ||
                               mApp.mRadarDevice.isSetting_Scanspeed_Command() ||
                               mApp.mRadarDevice.isSetting_StepHardPlus_Command()) {
                        Log.d("debug", "is setting");
                    } else {
                        if (beginSave())// �������ɹ������ɼ��������
                        {
                            maxShowRealtimeView();
                            try {
                                mLog.print(TAG, "��ʼ����!");
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                mLog.print(TAG, "�������棬���Ǳ���ʧ��");
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }// �������ʧ�ܣ���������
                    }
                }
            }
        }
        // ��С��
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
                // �������ò��������桢ȫ���ɼ�������·������˵�
                if (!mApp.mRadarDevice.isSetting_AllHardPlus_Command() &&
                    !mApp.mRadarDevice.isSavingMode() &&
                    !mApp.mRadarDevice.isSetting_StepHardPlus_Command()) {
                    DebugUtil.i("diance", "2.�����ò���etc");
                    fragLeft.onKeyDown(keyCode, event, this);
                    return false;
                } else if (mApp.mRadarDevice.isSavingMode()) {
                    mApp.mRadarDevice.smallMark();
                } else {
                    DebugUtil.i("diance", "3.������");
                }
            }
        }
        // ����
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
        // �ط�
        else if (keyCode == KeyEvent.KEYCODE_F4) {
            DebugUtil.i(TAG, "OnKeyUp:F4 back");
            mResponFlag = this.BACKPLAY;

            // �رչ�����
            /**
             * 1.ֹͣ�״�����ļ�ѡ�� 2.�����ļ���ȫ����ʾ�������ڻطŵ�״̬��־ 3.������������
             */
            if (this.isMaxShowBackPlayView()) {
                // ������״̬��ԭ
                resetPlayBack();
                this.normalShowBackPlayView();
                mApp.mListView.requestFocus();
            } else {
                if (mApp.mRadarDevice.isSavingMode())
                    ;// ����ʱ���ط�
                    // 0619����ǵ�����״̬���ط�
                    // else if(mApp.mRadarDevice.isDianCeMode() );
                else {
                    // ����ֹͣ�������е��״�
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
                    // ����ѡ������ļ�
                    // getStorageFolderPath();
                    this.showSelectplaybackPopwindow();
                }// ��������Ӧ�ط�
            }
        }
        // ɾ����
        else if (keyCode == KeyEvent.KEYCODE_DEL) {
            // //2016.6.10 �ֲ����ģʽ�£������Ƿ�����������
            if (mApp.mRadarDevice.isRunningMode() && mApp.mRadarDevice.isWhellMode() &&
                mApp.mRadarDevice.isBackOrientMode()) {
                discardDataDialog();
                // mApp.mRadarDevice.discardBackDatas();
            } else
                // //
                if (!isMaxShowBackPlayView()) {
                    if (mApp.mRadarDevice.isSavingMode())
                        ;// ����ʱ���ط�
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
     * ��Ļ��ͼ���浽�ڴ濨
     * <p>
     * //     * @param bitName
     * //     * @param mBitmap
     */
    public void saveBitmapForSdCard() {
        // ����file����
        getWindow().getDecorView().setDrawingCacheEnabled(true);
        Bitmap bmp = getWindow().getDecorView().getDrawingCache();
        long time = System.currentTimeMillis();
        File f = new File("/mnt/sdcard/" + bmp + "_" + time + ".png");
        try {
            // ����
            f.createNewFile();
        } catch (IOException e) {

        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // ԭ�ⲻ���ı������ڴ濨��
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
        showToastMsg("�������浽sdcard��");
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
            mLog.print(TAG, "�˳��ɼ������");
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
                    ;// || mApp.isCustomSetting());//����ʱ���˳�
                else if (mIsBackplaying)// �Ƿ��ڻط�
                {
                    // ������״̬��ԭ
                    resetPlayBack();
                    this.normalShowBackPlayView();
                    mApp.mListView.requestFocus();
                    // fragLeft.onKeyDown(keyCode,event,this); //����left������
                } else if (isMaxShowRealtimeView())// �Ƿ���ȫ����ʾ
                {
                    normalShowRealtimeView();
                    mApp.mListView.requestFocus();
                } else {
                    if (mApp.getLeftFragmentTab() == 0 && isTabPressed) {
                        // �����Ի���ѯ��
                        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("�Ƿ��˳�Ӧ�ã�").setMessage("�Ƿ��˳��ɼ������").setPositiveButton("ȷ��",
                                                                                              new DialogInterface.OnClickListener() {
                                                                                                  @Override
                                                                                                  public void onClick(
                                                                                                          DialogInterface dialog,
                                                                                                          int whichButton) {
                                                                                                      performExitApp();
                                                                                                  }
                                                                                              })
                               .setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
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
        // //�����ֲ������ͼ
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
     * ����������ʾʵʱ�ɼ�����
     */
    public void normalShowRealtimeView() {
        DebugUtil.i(TAG, "enter normalShowRealtimeView!");
        RelativeLayout lFrag;
        LinearLayout.LayoutParams params;

        // ��ʾ��� ��������
        lFrag = (RelativeLayout) findViewById(R.id.left_fragment);
        params = (android.widget.LinearLayout.LayoutParams) lFrag.getLayoutParams();
        params.weight = (float) 0.25;
        lFrag.setLayoutParams(params);
        lFrag.setVisibility(View.VISIBLE);

        // ���ػط�ģʽ �������� ��ͼ
        this.mbackplayScanView.setVisibility(View.INVISIBLE);

        // ��ʾ ʵʱ�ɼ� �������� ��ͼ
        mScanView.setVisibility(View.VISIBLE);

        // �����Ҳ���ʾ����
        lFrag = (RelativeLayout) findViewById(R.id.right_fragment);
        params = (android.widget.LinearLayout.LayoutParams) lFrag.getLayoutParams();
        params.weight = (float) 0.25;
        lFrag.setLayoutParams(params);
        lFrag.setVisibility(View.VISIBLE);

        LinearLayout mFrag;
        LinearLayout.LayoutParams layoutParam;

        // ��ʾ ʵʱ�ɼ� ʱ�����
        mFrag = (LinearLayout) findViewById(R.id.layout_VTWRuler);
        mFrag.setVisibility(View.VISIBLE); // 2016.6.10
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.1;
        mFrag.setLayoutParams(layoutParam);

        // ���� �ط�ģʽ ʱ����� 2016.6.10
        mFrag = (LinearLayout) findViewById(R.id.layout_BVTWRuler);
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.0;
        mFrag.setLayoutParams(layoutParam);
        mFrag.setVisibility(View.INVISIBLE);

        // ��ʾ ʵʱ�ɼ� ��ȱ��
        mFrag = (LinearLayout) findViewById(R.id.layout_VDDRuler);
        mFrag.setVisibility(View.VISIBLE); // 2016.6.10
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.1;
        mFrag.setLayoutParams(layoutParam);
        // ���� �ط�ģʽ ��ȱ�� 2016.6.10
        mFrag = (LinearLayout) findViewById(R.id.layout_BVDDRuler);
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.0;
        mFrag.setLayoutParams(layoutParam);
        mFrag.setVisibility(View.INVISIBLE);

        // //2016.6.10 ��ʾʵʱ�ɼ� ˮƽ��ߣ����� �ط�ģʽ ˮƽ���
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

        // ����ʵʱ�ɼ����طŲ��ɼ�
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
     * ���ȫ����ʾʵʱ�ɼ���ͼ
     */
    public void maxShowRealtimeView() {
        RelativeLayout lFrag;
        LinearLayout.LayoutParams params;
        // ������� ��������
        lFrag = (RelativeLayout) findViewById(R.id.left_fragment);
        params = (android.widget.LinearLayout.LayoutParams) lFrag.getLayoutParams();
        params.width = 0;
        params.weight = 0;
        lFrag.setLayoutParams(params);
        lFrag.setVisibility(View.INVISIBLE);

        // �����Ҳ� �������� ����
        lFrag = (RelativeLayout) findViewById(R.id.right_fragment);
        params = (android.widget.LinearLayout.LayoutParams) lFrag.getLayoutParams();
        params.width = 0;
        params.weight = 0;
        lFrag.setVisibility(View.INVISIBLE);

        LinearLayout mFrag;
        LinearLayout.LayoutParams layoutParam;
        // //��ʾ ʵʱ�ɼ� ģʽ�� ʱ�����
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

        // ��ʾ ʵʱ�ɼ� ģʽ�� ��ȱ��
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

        // //2016.6.10 ��ʾʵʱ�ɼ� ˮƽ��ߣ����� �ط�ģʽ ˮƽ���
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
     * ���ûطŵ���������öԻ���
     */
    private View mSetPlayBackView;
    private PopupWindow mSetPlayBackWindow;
    private Button bt_pbbackg = null;// ȥ����
    private Button bt_pbzoom = null;// �������
    private Button bt_pbxzoom = null;// ���ŵ���
    private boolean bl_pbbackg = false, bl_pbzoom = false, bl_pbxzoom = false;// �Ƿ���в���

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
     * ��ʾ�ط����ô���
     */
    public void showBackPlayPopupWindow() {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        getWindow().setAttributes(params);

        mSetPlayBackWindow.setBackgroundDrawable(new BitmapDrawable());
        mSetPlayBackWindow.showAtLocation(mSetPlayBackView, Gravity.CENTER | Gravity.BOTTOM, 0, 0);
    }

    /**
     * �ط�����ȥ�����������������
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
                        // ȥ����
                        mbackPlayDIBView.setRemoveBackground(true);
                    } else {
                        bt_pbbackg.setTextColor(Color.BLACK);
                        mbackPlayDIBView.setRemoveBackground(false);
                    }
                    break;
                case R.id.bt_pbzoom:
                    bl_pbzoom = !bl_pbzoom;
                    if (bl_pbzoom) {
                        // �������Ŵ������¼�
                        bt_pbzoom.setTextColor(Color.RED);
                    } else {
                        // �ָ�����
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
                        // �ָ�����
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
     * �طŲ�������İ�������
     */
    public Button.OnKeyListener mSettingPlayback_OnKeyHandler = new Button.OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            // TODO Auto-generated method stub
            int id = v.getId();
            // �����ϼ���������Ӧ��ֵ
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (id) {
                        case R.id.bt_pbzoom:
                            // DebugUtil.toast(IDSC2600MainActivity.this,
                            // "bt_pbzoom");
                            if (bl_pbzoom)// �����������
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
                            if (bl_pbzoom)// �����������
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
     * �����û�ԭ��ʼ��״̬
     *
     * @return
     */
    public boolean resetPlayBack() {
        // ��ȥ������
        bt_pbbackg.setTextColor(Color.BLACK);
        mbackPlayDIBView.setRemoveBackground(false);
        // �ָ�����
        if (mbackPlayDIBView.isZoom())
            mbackPlayDIBView.zoomRestorePlus();
        else
            ;
        bt_pbzoom.setTextColor(Color.BLACK);
        // �ָ�����
        mbackPlayDIBView.mZoomX = 1;
        setBackplayHRulerZoomx(1);
        bt_pbxzoom.setTextColor(Color.BLACK);

        bl_pbbackg = false;
        bl_pbzoom = false;
        bl_pbxzoom = false;
        return true;
    }

    // //��ʼ����
    public boolean beginSave() {
        // ������ֲ�ģʽ�������˶�λ����
        if (mApp.mRadarDevice.isWhellMode()) {
            mApp.mRadarDevice.setBackFillPos(0);
            mApp.mRadarDevice.endBackOrient1();
        } else
            ;

        // ���ɱ����ļ�
        if (judgeExistSpace()) {
            if (!mApp.mRadarDevice.createNewDatasFile()) {
                Log.d("debug", "���ɱ����ļ�����");
                Toast.makeText(this, "���ɱ����ļ�����_1!", Toast.LENGTH_SHORT).show();
                return false;
            } else
                ;
        } else {
            Log.d("debug", "judge failed");
            return false;
        }

        // ��ʼ�����ļ�
        if (!mApp.mRadarDevice.beginSave()) {
            Log.d("debug", "begin save return false");
            Toast.makeText(this, "�������ݴ���_2!", Toast.LENGTH_SHORT).show();
            return false;
        }
        Toast.makeText(this, "��ʼ��������!", Toast.LENGTH_SHORT).show();
//        mApp.mRadarDevice.setSaveDetectResult(true);
        Log.d("debug", "��ʼ����");
        mApp.mRadarDetect.setSave(true);
        mApp.mRadarDetect.setStoragePath(mApp.mRadarDevice.getStoragePath());
        mApp.mRadarDetect.startRadarDetect();
        Toast.makeText(this, "����̽���߳�!", Toast.LENGTH_SHORT).show();

        // ��ʾ�����ļ���
        String txt, subString;
        subString = mApp.mRadarDevice.getSaveFilename();
        int sub = subString.lastIndexOf("/");
        subString = subString.substring(sub, subString.length());
        txt = "����:" + subString + "!";
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

    // �ж��Ƿ��и������Լ��洢�ռ�Ĵ�С
    public boolean judgeExistSpace() {
        // ������ڴ�����
        long size[] = mApp.getSDCardMemory();
        String path = mApp.mRadarDevice.getStoragePath();
        Log.d("debug", "path: " + path);
        DebugUtil.i(TAG, "judgeExistSpace=" + path);

        // ������ô洢�ռ�Ϊ0���ж��Ƿ����
        if (size[1] == 0) {
            path += "/test.txt";
            File file = new File(path);

            if (file.mkdir()) {
                if (file.delete())
                    ;
                else
                    ;
                // ������ʾ�洢�ռ��С
                DebugUtil.infoDialog(this, "�洢�ռ䲻��", "���ô洢�ռ�" + size[1] + "");
                return true;
            } else {
                // ��ʾ������
                Log.d("debug", "δ�ҵ�����");
                DebugUtil.infoDialog(this, "�洢�ռ䲻����", "δ�ҵ�������");
            }

            return false;
        }
        // ��ʾ�洢�ռ��С
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
                DebugUtil.toast(this, "���ô洢�ռ��С/�ܴ洢��С:" + txt);

                return true;
            } else {
                // ��ʾ������
                DebugUtil.infoDialog(this, "�洢�ռ䲻����", "δ�ҵ�������");
            }
            return true;
        }
    }

    // //ֹͣ����
    public void stopSave() {
        Toast.makeText(this, "�������ݽ���!", Toast.LENGTH_LONG).show();
        TextView txtView;
        txtView = (TextView) findViewById(R.id.textview_savefilename);
        txtView.setVisibility(View.INVISIBLE);
        // txtView.setWidth(0);
        mApp.mRadarDevice.stopSave();
        mApp.mRadarDetect.stopRadarDetect();
        Toast.makeText(this, "��ֹͣ�����״�����!", Toast.LENGTH_LONG).show();
    }

    // �����Ѿ��طŵĵ���
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
            txt = "���߾���:" + distance + "m";
            txtView.setText(txt);
        } else {
            txt = "�ѻطŵ���:" + scans + "��";
            // ����ǵ��ģʽ
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

            txt = "���߾���:" + distance + "m";
            txtView.setText(txt);
        } else {
            scans = mApp.mRadarDevice.getHadRcvScans();
            txt = "�Ѳɵ���:" + scans + "��";
            // ����ǵ��ģʽ20160614
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

    // //ֹͣ�״�
    public boolean radarStop() {
        MyApplication theApp;
        theApp = (MyApplication) getApplicationContext();

        // ֹͣ��ȡ����
        mApp.setRealThreadReadingDatas(false);
        theApp.mRadarDevice.stop();

        // 2016.6.10
        fragLeft.setRadarState(false);

        // ����Ĭ�ϲ���
        String fileName;
        int sel;
        sel = theApp.mRadarDevice.getAntenFrqSel();
        fileName = theApp.mRadarDevice.INNERSTORAGE + theApp.mRadarDevice.mParamsFilefolderPath;
        fileName += radarDevice.g_antenFrqStr[sel] + ".par";
        // ��������ļ�
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
        // Ѱ��sd���е��״�����ļ�
        String path;
        path = app.mRadarDevice.getParamsPath();
        final List<String> dataList = new ArrayList<String>();
        if (!app.getParamFilenamesFromeSD(dataList, path) || dataList.size() == 0) {
            showToastMsg("�����ڲ����ļ�!");
            return;
        }
        // �õ��ļ���
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
        new AlertDialog.Builder(this).setTitle("ѡ���״����").setSingleChoiceItems(fileNames, Sel,
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
                "ȷ��", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mApp.mMainActivity.loadRadarParamsFromeFile(mParamSelFileName);
                    }
                }).setNegativeButton("ȡ��", new android.content.DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
    }

    // ���ز���
    public void loadRadarParamsFromeFile(String pathName) {
        DebugUtil.i(TAG, "loadRadarParamsFromeFile,pathName=" + pathName);

        mApp.mRadarDevice.loadParamsFile(pathName);
        mApp.mRadarDevice.refreshFileHeader();

        RadarParamExpandableListAdapter adapter = (RadarParamExpandableListAdapter) mApp.mListView
                .getExpandableListAdapter();
        adapter.notifyDataSetChanged();

        // ���±��
        mApp.mTimewndRuler.invalidate();
        mApp.mDeepRuler.invalidate();
    }

    /**
     * ���ûطŵ�״̬
     */
    public boolean mIsMaxshowBackPlayView = false;

    public boolean isMaxShowBackPlayView() {
        return mIsMaxshowBackPlayView;
    }

    /**
     * ���õ������ļ�����
     */
    private View backPlayFilePathView;
    private PopupWindow mBackPlayFilePopupWindow;
    private ListView listView;
    private Button btn_up;// ������һ��
    private CheckBox cbx_fileOrder;// �ļ�˳���б�
    private TextView file_path;
    private FileAdapter fileAdapter;
    private String filePathString = null; // ѡ��طŵ��ļ�·��
    // ���ô洢λ��ѡ��Ի���
    private View playbackSelectView;
    private PopupWindow mplaybackSelectPopWindow;
    private RadioGroup radioGroupplayback;

    /**
     * ���ûطŵ����Ķ��壬�����õ����طŵļ���
     */
    public void setSelectplaybackPopwindow() {
        playbackSelectView = LayoutInflater.from(this).inflate(
                R.layout.layout_playbackselect_popwindow, null);
        radioGroupplayback = (RadioGroup) playbackSelectView.findViewById(R.id.radioGroupPlayback);

        // Ĭ��ѡ���һ��radioButton
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
        // ȡ����������ʧ����
        mplaybackSelectPopWindow.setFocusable(true);
    }

    // �洢λ��ѡ����Ӧ
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

    // �����洢ѡ�񴰿�
    public void showSelectplaybackPopwindow() {
        // ���ݵ�ǰ��ѡ������radioѡ��
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
     * �طź�ɾ��ʹ�õ�·��ѡ������
     */
    public void showBackPlayFilePath() {
        DebugUtil.i(TAG, "enter showBackPlayFilePath!");

        backPlayFilePathView = LayoutInflater.from(this).inflate(R.layout.playback_sdcardlist,
                                                                 null);
        // �ҵ���Ӧ�Ŀؼ�
        listView = (ListView) backPlayFilePathView.findViewById(R.id.listView1);
        btn_up = (Button) backPlayFilePathView.findViewById(R.id.button_up);
        cbx_fileOrder = (CheckBox) backPlayFilePathView.findViewById(R.id.check_fileOrder);
        cbx_fileOrder.setChecked(mApp.getisDecendOrder());
        cbx_fileOrder.setOnCheckedChangeListener(fileOrderChangeListener);

        // �½�һ������������������ListView����ʾ�ļ��б�
        fileAdapter = new FileAdapter(this);
        // ��������������������
        listView.setAdapter(fileAdapter);
        DebugUtil.i(TAG, "listview.setOnkeylistener!");

        // ѡ��ʱ���ļ�����·����ã�����Ӧɾ������ɾ������
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
                // ����һ��������
                FileAdapter fileNext = (FileAdapter) listView.getAdapter();
                File f = fileNext.list.get(arg2);
                // ��ȡ�ļ�·�����ļ�����
                if (f.isDirectory()) {
                    // ��ȡsd����Ӧ�Ĵ洢Ŀ¼
                    // mApp.mRadarDevice.getSDCardPath();
                    fileAdapter.scanFiles(filePathString + mApp.mRadarDevice.mLTEFilefolderPath);
                } else {
                    fileNameString = f.getName();
                    if (fileNameString.endsWith(".lte") || fileNameString.endsWith(".LTE")) {
                        fileNameString = f.getAbsolutePath();
                        DebugUtil.i(TAG, "fileNameString=" + fileNameString);

                        // �Ƕ�ά�ļ�ʱ�ж�Ŀǰ���ڻطŻ���ɾ��״̬
                        if (mResponFlag == BACKPLAY) {
                            // �ж��ļ���С
                            long length = f.length();
                            int sizeG = (int) (length / 1024 / 1024 / 1024);
                            int max = 4;

                            if (length <= 1024 || sizeG >= max) {
                                DebugUtil.infoDialog(mcontext, "����", "�ļ���С����");
                            } else {
                                mApp.setBackplayFilePath(fileNameString);
                                maxShowBackPlayView();
                                beginBackplayFile(fileNameString);
                            }
                            mBackPlayFilePopupWindow.dismiss();
                        }// �ط�ģʽ��
                        else if (mResponFlag == DELETE) {
                            DebugUtil.i(TAG, "Flag = delete,fileNameString=" + fileNameString);

                            new AlertDialog.Builder(MultiModeLifeSearchActivity.this).setTitle(
                                    "ȷ��ɾ��?").setNegativeButton("ȷ��",
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
                                                                           builder.setTitle("ɾ���ɹ�")
                                                                                  .setMessage(
                                                                                          "ɾ���ɹ���")
                                                                                  .setNegativeButton(
                                                                                          "ȷ��",
                                                                                          new DialogInterface.OnClickListener() {
                                                                                              @Override
                                                                                              public void onClick(
                                                                                                      DialogInterface dialog,
                                                                                                      int whichButton) {
                                                                                                  fileAdapter
                                                                                                          = new FileAdapter(
                                                                                                          MultiModeLifeSearchActivity.this);
                                                                                                  // ��������������������
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
                                                                           builder.setTitle("ɾ��ʧ��")
                                                                                  .setMessage(
                                                                                          "ɾ��ʧ�ܣ�")
                                                                                  .setNegativeButton(
                                                                                          "ȷ��",
                                                                                          new DialogInterface.OnClickListener() {
                                                                                              @Override
                                                                                              public void onClick(
                                                                                                      DialogInterface dialog,
                                                                                                      int whichButton) {
                                                                                              }
                                                                                          }).show();
                                                                       }
                                                                   }
                                                               }).setPositiveButton("ȡ��",
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

                        }// ɾ��ģʽ��
                    } else if (fileNameString.endsWith(".txt")) {
                        openFile(f);
                    } else {
                        Toast.makeText(MultiModeLifeSearchActivity.this, "�����״�ɼ��ļ�",
                                       Toast.LENGTH_SHORT).show();
                        Toast.makeText(MultiModeLifeSearchActivity.this, fileNameString,
                                       Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // ��ʾĬ�ϵ��ļ�Ŀ¼
        fileAdapter.scanFiles(filePathString + mApp.mRadarDevice.mLTEFilefolderPath);

        // ��Ӧ�����ϡ��İ�ť
        btn_up.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // ����������
                showSelectplaybackPopwindow();
                mBackPlayFilePopupWindow.dismiss();
            }
        });

        mBackPlayFilePopupWindow = new PopupWindow(backPlayFilePathView,
                                                   android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                                                   android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        // ȡ����������ʧ����
        mBackPlayFilePopupWindow.setFocusable(true);

        int xPos = backPlayFilePathView.getHeight();
        int yPos = backPlayFilePathView.getWidth();

        this.mBackPlayFilePopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mBackPlayFilePopupWindow.setWidth(256);// Ĭ�Ͽ��

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
                    // ��������������������
                    listView.setAdapter(fileAdapter);
                    fileAdapter.scanFiles(filePathString + mApp.mRadarDevice.mLTEFilefolderPath);
                    // ��¼״̬
                    break;
            }
        }
    };

    // �б���Ӧ����
    private boolean onceDialog = false;
    public View.OnKeyListener mListViewOnKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            DebugUtil.i(TAG, "0.mListViewOnKeyListener =" + keyCode);

            // ����ɾ���Լ�û������󻯻ط�
            if (keyCode == KeyEvent.KEYCODE_DEL && !isMaxShowBackPlayView()) {
                DebugUtil.i(TAG, "1.keyCode_del!,fileNameString=" + fileNameString);
                if (!onceDialog) {
                    onceDialog = true;
                    new AlertDialog.Builder(MultiModeLifeSearchActivity.this).setTitle("ȷ��ɾ��?")
                                                                             .setNegativeButton(
                                                                                     "ȷ��",
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
                                                                                                         "ɾ���ɹ�")
                                                                                                        .setMessage(
                                                                                                                "ɾ���ɹ���")
                                                                                                        .setNegativeButton(
                                                                                                                "ȷ��",
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
                                                                                                                        // ��������������������
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
                                                                                                         "ɾ��ʧ��")
                                                                                                        .setMessage(
                                                                                                                "ɾ��ʧ�ܣ�")
                                                                                                        .setNegativeButton(
                                                                                                                "ȷ��",
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
                                                                                     "ȡ��",
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

    // ɾ��ָ��·�����ļ�
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

    // ���ļ�
    private void openFile(File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // ����intent��Action����
        intent.setAction(Intent.ACTION_VIEW);
        // ����intent��data��Type���ԡ�
        intent.setDataAndType(Uri.fromFile(file), "text/plain");
        // ��ת
        startActivity(intent);
    }

    // �õ��ļ�·��
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
     * ����������ʾ�طŽ���
     */
    public void normalShowBackPlayView() {
        stopBackplayFile();
        normalShowRealtimeView();
        mIsMaxshowBackPlayView = false;
    }

    /**
     * �����ʾ�طŽ���
     */
    public void maxShowBackPlayView() {
        mIsMaxshowBackPlayView = true;
        mIsMaxshowRealtimeView = false; // 2016.6.10

        RelativeLayout lFrag;
        LinearLayout.LayoutParams params;

        // �������Ŀ�����
        lFrag = (RelativeLayout) findViewById(R.id.left_fragment);
        params = (android.widget.LinearLayout.LayoutParams) lFrag.getLayoutParams();
        params.width = 0;
        params.weight = 0;
        lFrag.setLayoutParams(params);
        lFrag.setVisibility(View.INVISIBLE);

        // ��ʾ�ط� �������� ����
        this.mbackplayScanView.setVisibility(View.VISIBLE);
        // ����ʵʱ�ɼ� �������� ����
        mScanView.setVisibility(View.INVISIBLE);

        lFrag = (RelativeLayout) findViewById(R.id.right_fragment);
        params = (android.widget.LinearLayout.LayoutParams) lFrag.getLayoutParams();
        params.width = 0;
        params.weight = 0;
        lFrag.setVisibility(View.VISIBLE);
        lFrag.setLayoutParams(params);

        LinearLayout mFrag;
        LinearLayout.LayoutParams layoutParam;

        // //2016.6.10 ����ʵʱ�ɼ� ʱ����ߣ���ʾ�ط�ģʽ ʱ�����
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

        // //2016.6.10 ����ʵʱ�ɼ� ��ȱ�ߣ���ʾ�ط�ģʽ ��ȱ��
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

        // //2016.6.10 ����ʵʱ�ɼ� ˮƽ��ߣ���ʾ �ط�ģʽ ˮƽ���
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

        // //��ʾ�ط���ͼ
        mFrag = (LinearLayout) findViewById(R.id.layout_backplayview);
        mFrag.setVisibility(View.VISIBLE); // 2016.6.10
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = (float) 0.9;
        mFrag.setLayoutParams(layoutParam);

        // ���� ʵʱ�ɼ� ��ͼ
        mFrag = (LinearLayout) findViewById(R.id.layout_realtimeview);
        layoutParam = (LinearLayout.LayoutParams) mFrag.getLayoutParams();
        layoutParam.weight = 0;
        layoutParam.width = 0;
        mFrag.setLayoutParams(layoutParam);
        mFrag.setVisibility(View.INVISIBLE);
    }

    /**
     * ��ʼ�ط�
     */
    public boolean beginBackplayFile(String fileName) {
        boolean ret = true;
        DebugUtil.i(TAG, String.valueOf(mIsBackplaying));

        if (mApp.mRadarDevice.setPlayBackMode())
            ;
        else
            DebugUtil.i(TAG, "setPlayBackMode error!");

        // ������ڻط����ݣ���ֹͣ�ط�
        if (mIsBackplaying) {
            ret = stopBackplayFile();
        }
        if (!ret) {
            return ret;
        }

        // ���ûط��ļ���
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
            // ���ûط��ٶ�
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
     * ֹͣ�ط�
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
     * ��ȡ��ص���
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

            // 0316ֻ�����ڱ���10�׵�����ȡ��
//            double tempLevel = (leftLevel - 15) * 100 / 85;
//            leftLevel = (int) tempLevel;

            // С������Ϊ0
            if (leftLevel < 0) {
                leftLevel = 0;
            } else
                ;

            // ����100����ʾ
            if (leftLevel > 100) {
                return;
            } else
                ;

            txtViewPower.setText("����:" + leftLevel + "%");

            System.err.println("������" + leftLevel);

            // ���޵�ص��͵�����ص�״̬�ı�
            if (leftLevel <= WARNBATTERYLEVEL) {
                txtViewPower.setTextColor(Color.RED);
                if (!lowPowerInfo) {
                    lowPowerInfo = true;
                    // ��˸��ʾ��������
                    DebugUtil.infoDialog(this, "��������", "�������㣬�뾡�챣�����ݣ�");
                    // �����Ʊ�Ϊ����
                    mApp.mPowerDevice.BatLightOn();
                } else
                    ;
            } else {
                // �رյ�����
                mApp.mPowerDevice.BatLightOff();
                txtViewPower.setTextColor(Color.GREEN);
                // �ж�֮ǰ�ǲ��ǵ͵�����������ǣ��رյ�����
                if (lowPowerInfo) {
                    lowPowerInfo = false;// ȡ���͵�����־
                } else
                    ;
            }
        } else {
            System.err.println("�޵������");
            // DebugUtil.i(TAG,"������ȡ�쳣!");
            txtViewPower.setText("����:�޵������!");
            // +"leftLevel="+String.valueOf(leftBattery)
            // +"fullBattery="+String.valueOf(fullBattery));
            txtViewPower.setTextColor(Color.RED);
            // ��˸��ʾ��������
        }
    }

    /**
     * �õ��洢��������Ϣ
     */
    private TextView txtViewSpace;
    /**
     * 20170315�洢�ռ�ÿ���Ӷ�ȡһ�Σ�С��100Mʱ����
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
         * //0315�洢�ռ�С��100M���� if( left < 100 && mApp.mRadarDevice.isSavingMode()
         * ) { this.stopSave(); normalShowRealtimeView();
         * mApp.mListView.requestFocus(); stopSpark();
         * txtViewSpace.setTextColor(Color.RED); DebugUtil.infoDialog(this,
         * "�������","�ռ䲻�㱣�������"); } else { if( left <= 500 ) { // DebugUtil.i(TAG,
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
            txtChangeMemory = "(SD��)ʣ��ռ�:";
        } else if (mApp.mRadarDevice.isSelectUSB()) {
            txtChangeMemory = "(USB��)ʣ��ռ�:";
        } else if (mApp.mRadarDevice.isSelectMemory()) {
            txtChangeMemory = "(�ڴ�)ʣ��ռ�:";
        }
        infsMemory = mApp.getSDCardMemory();
    }

    // ��������ʱ��˸
    private Timer bat_timer = null;
    private TimerTask bat_task = null;
    private boolean bat_runTask = false;

    public void sparkBattery() {
        DebugUtil.i(TAG, "spark battery!");
    }

    // �ռ䲻�㱨��
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

    // ���ٱ���
    public void beginOverspeedAlarm() {
        TextView txtView;
        txtView = (TextView) findViewById(R.id.textview_overspeed);
        txtView.setVisibility(View.VISIBLE);
        String txt;
        txt = "�ѳ���!";
        txtView.setText(txt);
        showOverSpeedPopupWindow();
    }

    // ֹͣ���ٱ���
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
        // ������ʾ�Ի���
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("������������");
        builder.setTitle("ȷ����������������?").setMessage("ȷ������").setNegativeButton("��",
                                                                            new DialogInterface.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(
                                                                                        DialogInterface dialog,
                                                                                        int whichButton) {
                                                                                    mApp.mRadarDevice
                                                                                            .discardBackDatas();
                                                                                }
                                                                            }).setPositiveButton(
                "��", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                }).show();
    }

    // ���ٵ���
    private View overSpeedView;
    private PopupWindow mOverSpeedPopWindow;

    public boolean setOverSpeedPopupWindow() {
        overSpeedView = View.inflate(this, R.layout.overspeedalarm_popwindow, null);
        mOverSpeedPopWindow = new PopupWindow(overSpeedView, 100,
                                              android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        mOverSpeedPopWindow.setFocusable(false);

        return true;
    }

    // ������ʾ
    public void showOverSpeedPopupWindow() {
        DebugUtil.i(TAG, "showOverSpeedPopupWindow!");
        mOverSpeedPopWindow.setBackgroundDrawable(new BitmapDrawable());
        mOverSpeedPopWindow.showAtLocation(overSpeedView, Gravity.CENTER | Gravity.CENTER, 0, 0);
    }

    /**
     * С������λ������
     */
    public double setDigits(double inputNum, int digitNum) {
        double tempNum = inputNum;
        BigDecimal b = new BigDecimal(tempNum);
        tempNum = b.setScale(digitNum, BigDecimal.ROUND_HALF_DOWN).doubleValue();// ����ȡ��
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
