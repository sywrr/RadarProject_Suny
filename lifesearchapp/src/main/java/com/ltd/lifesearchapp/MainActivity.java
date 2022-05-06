package com.ltd.lifesearchapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.ltd.lifesearchapp.Detect.DetectResult;
import com.ltd.lifesearchapp.Detect.DetectResultHandler;
import com.ltd.lifesearchapp.Detect.MBMReader;
import com.ltd.lifesearchapp.Detect.RadarDetect;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import Utils.AbstractLogger;
import Utils.Logcat;


public class MainActivity extends AppCompatActivity implements Thread.UncaughtExceptionHandler {

    static {
        System.loadLibrary("DetectRadarData");
    }
    public RadarDevice mRadarDevice;
    private LifeSearchApplication mApp;
    private final static String TAG = "MainActivity";

    private final static String defaultParams = "";

    private final int sampleLen = 2048;
    private DetectFragment mFragment = null;
    public String mLifeSearchLog ;
    OutputStream fos = null;
    private int number = 0;
    private Context mContext = null;
    public void setFileName(String fileName){
        this.mLifeSearchLog= fileName;
    }
    public String getFileName(){
        return mLifeSearchLog;
    }
    public static String getSDCardPath(){
        String sd_root = Environment.getExternalStorageDirectory().getAbsolutePath()+"/lifeSearchLteFiles";
        File dir = new File(sd_root);
        if(!dir.exists()){
            System.err.println("�ļ���δ����");
            dir.mkdirs();
        }
        if(dir.exists()){
            System.err.println("�ļ����Ѵ���");
        }
        System.err.println("root:"+sd_root);
        return  sd_root;
    }
    public static String creatNewFiles(){
        String file_root = Environment.getExternalStorageDirectory().getAbsolutePath()+"/lifeSearchLteFiles/test.txt";
        File file = new File(file_root);
        if(!file.exists()){
            System.err.println("�ļ�δ����");
            try {
                file.createNewFile();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        if(file.exists()){
            System.err.println("�ļ��Ѵ���");
        }
        System.err.println("root:"+file_root);
        return  file_root;
    }

    //�����µ������ļ���
    public String createNewLog() {
        return createNewLog_ByIndex();
    }
    //���������������ļ���
    private int mNowFileindex = 0;          //�ļ�����
    private String mLTEFilefolderPath="/LteFileMissLog";
    private  String mResultPath = "/DetectResults";
    RandomAccessFile bos =null;
    RandomAccessFile bos1 =null;
    public String createNewLog_ByIndex() {
        String fileName;
        int index = 1;
        do {
            fileName = "/ltefile" + index + ".txt";
            fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + mLTEFilefolderPath +
                    fileName;
            File file = new File(fileName);
            if (!file.exists())
                break;
            index++;
        } while (true);
        //
        mNowFileindex = index;

        return fileName;
    }
    //��̬��ȡȨ��
    private void getPermission(){
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }
    public final DataCollection dataCollection = new DataCollection("192.168.0.216", "192.168.0.10",8, 2,new DataCollection.DataHandler() {
        @Override
        public void handleReceiveError(String err) {
            System.err.println("receive error: " + err);
        }

        @Override
        public void handleReceiveComplete(byte[] b, int offset, int prevIdx, int curIdx)  {
            if (curIdx == prevIdx) {
                // handle first receive data
//                System.err.println("���յ�һ������");
                number = 1;
//                int ret = Test.beginSaveData(dataCollection.getFileName(), 0);
                radarDetect.pushData(b, offset, sampleLen, sampleLen);
            } else if (curIdx - prevIdx == 1) {
                // handle receive one scan data
                int recNum = curIdx - prevIdx;
                number =number+recNum;
//                int ret = Test.beginSaveData(dataCollection.getFileName(), 0);
//                System.out.println("���յ�����"+number);
                radarDetect.pushData(b, offset, sampleLen, sampleLen);
            } else {
                // handle data missing
                int nDataMiss = curIdx - prevIdx;
//              System.err.println("���յ����ݲ�����" + curIdx + ", " + prevIdx );
                String missLog = "����λ�ã�" + prevIdx + "\n" + "��ʧ������"+ (curIdx - prevIdx )+"\n" ;
            }
        }
    });

    public final DetectResultHandler body_detectResultHandler =new DetectResultHandler() {
        @Override
        public void handle(List<DetectResult> detectResultList) {
            // handle detect result
            if(detectResultList.isEmpty()){

//               System.err.println("û�н��");
            }else{
                DetectResult dr = detectResultList.get(0);
                dr.getResultType();
                dr.getTargetPos();
                //                detectResult.getResultType();
//                System.err.println("handle�嶯̽����:"+   dr.getResultType()+ "���룺"+dr.getTargetPos() );
//                dr.reset();
            }
        }
    };
    public final DetectResultHandler breath_detectResultHandler =new DetectResultHandler() {
        @Override
        public void handle(List<DetectResult> detectResultList) {
            // handle detect result
            if(detectResultList.isEmpty()){
//                System.err.println("û�н��");
            }else{
                DetectResult dr = detectResultList.get(0);
                dr.getResultType();
                dr.getTargetPos();
                long time = System.currentTimeMillis();
                //ʱ���ȡ
                SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss ");
                Date date = new Date(System.currentTimeMillis());
                String curTime = formatter.format(date);
                String fileName = dataCollection.getFileName();

//                System.out.println(formatter.format(date));
                String str = dr.getResultType()==1 ? "�ļ�����"+fileName+ " " +"ʱ�䣺"+curTime+" " +"�м���������"
                        + dr.getTargetPos() + "\n"
                        : "�ļ�����"+fileName+ " " +"ʱ�䣺"+curTime+" " +"���պ��������"
                        + dr.getTargetPos() +"\n";
                if(dr.getResultType()==1){

                    System.err.println("test�м�������"+dr.getTargetPos());
                }
                else
                    System.err.println("test���պ������"+dr.getTargetPos());
//                dr.reset();
                try {
                    bos = new RandomAccessFile(getFileName(), "rw");
                    long fileLength = bos.length();
                    bos.seek(fileLength);
                    bos.write(str.getBytes());
                }catch (IOException e){
                    e.printStackTrace();
                }
                finally {
                    if(bos!=null){
                        try {
                            bos.close();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    };
    final HomeReceiver mHomeReceiver = new HomeReceiver();

    final IntentFilter mIntentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
    private NetworkDevice mNetwork;
    private boolean mReceiversStarted = false;

    private AbstractLogger mLogger = new Logcat("MainActivity", true);

    public final RadarDetect radarDetect = new RadarDetect();

    private void setLandScape() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable e) {
        // TODO Auto-generated method stub
        mLogger.error(e);
    }

    public NetworkDevice getNetwork() {
        return mNetwork;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLogger.debug(" on start");
        if (!mReceiversStarted) {
//            mNetwork.mReceiverGroup.startAllReceivers();
            mReceiversStarted = true;
        }
        ExpertFragment expertFragment = requireExpertFragment();
        if (expertFragment != null)
            expertFragment.startSurfaceView();
    }

    RadioGroup mMenu = null;

    private RadarFragment mRadarFragment = null;

    private Fragment initChildFragments(Class<?> fragmentClass, String fragmentTag, boolean hide) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);
        if (fragment == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            try {
                fragment = (Fragment) fragmentClass.newInstance();
                transaction.add(R.id.main_fragment, fragment, fragmentTag);
                if (hide) {
                    transaction.hide(fragment);
                } else {
                    transaction.show(fragment);
                }
                transaction.commit();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (java.lang.InstantiationException e) {
                e.printStackTrace();
            }
        } else {
            mLogger.error("fragment with tag " + fragmentTag + " is already added");
        }
        return fragment;
    }

    private void initView() {
        mRadarFragment = (RadarFragment) initChildFragments(RadarFragment.class, "radar_tag",
                false);
    }

    private void initMenu() {
        mMenu = findViewById(R.id.menu);
        ((RadioButton) mMenu.findViewById(R.id.radar)).setTextColor(
                getResources().getColor(R.color.orange));
        mMenu.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.hide(mRadarFragment);
                ((RadioButton) mMenu.findViewById(R.id.radar)).setTextColor(Color.BLACK);
                ((RadioButton) mMenu.findViewById(R.id.micro_vibration)).setTextColor(Color.BLACK);
                ((RadioButton) mMenu.findViewById(R.id.video)).setTextColor(Color.BLACK);
                ((RadioButton) mMenu.findViewById(R.id.etc)).setTextColor(Color.BLACK);
                RadioButton button = mMenu.findViewById(checkedId);
                button.setTextColor(getResources().getColor(R.color.orange));
                switch (checkedId) {
                    case R.id.radar:
                        mLogger.debug("click radar");
                        transaction.show(mRadarFragment);
                        break;
                    case R.id.micro_vibration:
                        mLogger.debug("click ΢��");
                        break;
                    case R.id.video:
                        mLogger.debug("click video");
                        break;
                    case R.id.etc:
                        mLogger.debug("click etc");
                        break;
                    default:
                        mLogger.debug("unknown tab");
                        break;
                }
                transaction.commit();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(this);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setLandScape();
        setContentView(R.layout.activity_main);

        initView();
        initMenu();

        mLogger.debug("init view finished");

        createNetwork();
        mRadarDevice = new RadarDevice();
        mRadarDevice.setContext(this);

        mApp = (LifeSearchApplication) getApplicationContext();

        registerReceiver(mHomeReceiver, mIntentFilter);
        //��ȡȨ��
        getPermission();

        radarDetect.setHandlers(body_detectResultHandler,breath_detectResultHandler);
    }

    public void createNetwork() {
        mNetwork = new NetworkDevice(5000);
    }

    public void showToast(CharSequence msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void releaseResource() {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof Releasable)
                ((Releasable) fragment).release();
        }
//        mNetwork.stopNetTransfers();
//        mNetwork.closeConnectors();
    }

    public Fragment requireFragment(String tag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment == null)
            throw new NullPointerException("fragment with tag " + tag + " is null");
        return fragment;
    }

    public final SettingsFragment requireSettingsFragment() {
        return (SettingsFragment) requireChildFragment(requireFragment("radar_tag"),
                "settings_tag");
    }

    private static Fragment requireChildFragment(Fragment fragment, String tag) {
        if (fragment == null)
            throw new IllegalArgumentException(
                    "passing getExpertFragment fragment can not be null");
        Fragment childFragment = fragment.getChildFragmentManager().findFragmentByTag(tag);
        if (childFragment == null)
            throw new NullPointerException("child fragment with tag " + tag + " is null");
        return childFragment;
    }

    public ExpertFragment requireExpertFragment() {
        return (ExpertFragment) requireChildFragment(requireFragment("radar_tag"), "expert_tag");
    }

    public DetectFragment requireDetectFragment() {
        return (DetectFragment) requireChildFragment(requireFragment("radar_tag"), "detect_tag");
    }

    public DetectResultPlaybackFragment requirePlaybackFragment() {
        return (DetectResultPlaybackFragment) requireChildFragment(requireFragment("radar_tag"),
                "detect_result_playback");
    }

    private volatile int mRequestCode;

    public final void setRequestCode(int requestCode) {
        mRequestCode = requestCode;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mLogger.debug("requestCode: " + requestCode + ", resultCode: " + resultCode);
        mLogger.debug("data: " + data);
        switch (resultCode) {
            case 201:
                if (data != null) {
                    String playbackFilePath = data.getStringExtra("result");
                    mLogger.debug("playback: " + playbackFilePath);
                    ExpertFragment expertFragment = requireExpertFragment();
                    expertFragment.mPlayManager.postPlayback(playbackFilePath);
                }
                break;
            case 203:
                if (data != null) {
                    String detectResultFilePath = data.getStringExtra("result");
                    mLogger.debug("detect result: " + detectResultFilePath);
                    requirePlaybackFragment().showDetectResult(detectResultFilePath);
                }
                break;
        }
        if (data == null) {
            switch (mRequestCode) {
                case 200:
                    requireExpertFragment().mPlayManager.postStopPlay();
                    mLogger.debug("û��ѡ��طŵ��ļ�");
                    break;
                case 202:
                    mLogger.debug("û��ѡ��̽�����ļ�");
                    break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLogger.debug("on pause");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mLogger.debug("on restart");
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unregisterReceiver(mHomeReceiver);
        mLogger.debug("onDestroy");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        mLogger.error("onKeyDown keycode = " + keyCode);

        return super.onKeyDown(keyCode, event);
    }

    public void exit() {
        DetectFragment detectFragment = requireDetectFragment();
        if (detectFragment.checkDetectState()) {
            releaseResource();
            finish();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        mLogger.error("onKeyUp keycode = " + keyCode);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(this).setTitle("��ܰ��ʾ").setMessage("ȷ���˳�����").setNegativeButton(
                    "ȡ��", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    }).setPositiveButton("�˳�", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    exit();
                }
            }).show();

            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        mLogger.error("onKeyLongPress keycode = " + keyCode);

        return super.onKeyLongPress(keyCode, event);
    }

    //sunyan 2021.11.3 add test begin
    private void loop() {
        boolean loop = true;
        for (; loop; ) {
//            System.out.println("create: " + Test.createInstance(0, "192.168.0.156", "192.168.0.10",12));
            for (; ; ) {
                boolean complete = false;
                try {
                    boolean state = Test.runningStatus();
                    complete = true;
                    if (state) {
                        System.err.println("running state true");
                        loop = false;
                        break;
                    } else {
                        System.err.println("running state false");
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                } finally {
                    if (!complete) {
                        Test.releaseInstance();
                        System.err.println("release");
                    }
                }
            }
        }
        Test.start();
//        int num = Test.saveSetting("{\"samplingPoints\":8192,\"scanSpeed\":64}    ",true);
//        System.err.println("���ò�����" + num);
        byte[] data = new byte[512 * 4 * 500];
        boolean initial = true;
        int prevIndex = -1;
        long st = System.currentTimeMillis();
        int sampleLen = 512;
        try {
            while (true) {
                Pair<Integer, Integer> p = Test.receivedData(data);
                boolean state = Test.runningStatus();
                System.err.println("�豸״̬��" + state);
                int step = p.first;
//                System.err.println("step: " + step);
                if (p.second > 0) {
                    System.err.println("�������ݵ�����" + p.second);
                    ByteBuffer buffer = ByteBuffer.wrap(data);
                    buffer.order(ByteOrder.LITTLE_ENDIAN);
                    int offset = 0;
                    while (offset < p.second) {
                        int pos = buffer.position();
                        int index = buffer.getInt(pos + 8);
                        if (!initial) {
                            if (index - prevIndex != 1) {
                                System.err.println("�������������ݣ�" + index + ", " + prevIndex);
                            }
                        } else {
                            initial = false;
                        }
                        prevIndex = index;
                        buffer.position(pos + sampleLen * 4);
                        ++offset;
                    }
                }
//                if ((System.currentTimeMillis() - st) >= 10000) {
//                    st = System.currentTimeMillis();
//                    sampleLen = sampleLen == 512 ? 256 : sampleLen == 256 ? 128 : 512;
//                    System.err.println("�޸Ĳ�������: " + sampleLen);
//                    JSONObject jsonObject = new JSONObject();
//                    try {
//                        jsonObject.put("samplingPoints", sampleLen);
//                        Test.saveSetting(jsonObject.toString(), true);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void test2700Interface() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                loop();
            }
        }).start();
    }
    //sunyan 2021.11.3 add test end
}
