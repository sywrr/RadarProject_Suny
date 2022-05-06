package com.ltd.lifesearchapp;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import com.ltd.lifesearchapp.Detect.DetectParams;
import com.ltd.lifesearchapp.Detect.DetectRange;
import com.ltd.lifesearchapp.Detect.DetectResultHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import Connection.Packet;
import Connection.Receiver;
import Utils.AbstractLogger;
import Utils.Logcat;

public class DetectFragment extends Fragment implements Releasable {

    private View mView = null;

    private Context mContext;

    private ImageView mPadBatteryView = null;

    private ImageView mSignalView = null;

    private ImageView mConnectStateView = null;

    private ImageView mDeviceBatteryView = null;

    private ImageButton mDetectButton = null;

    private ImageButton mAutoDetectButton = null;

    private ImageButton mCheckButton = null;

    private ImageButton mWifiButton = null;

    private ImageButton mExitButton = null;

    private TextView mDetectRangeView = null;

    private TextView mFileNameText = null;

    private volatile boolean mCheckOff = true;

    private DetectAnimationView mDetectAnimationView = null;

    private AbstractLogger mLogger = new Logcat("DetectFragment", true);

    @Override
    public void release() {
        long st = System.nanoTime();
        mLogger.debug("stop update thread");
        stopThread();
        mLogger.debug("unregister receiver");
        try {
            mContext.unregisterReceiver(mWifiReceiver);
            mContext.unregisterReceiver(mBatteryReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mStateHandler.setContext(null);
        mStateHandler.removeCallbacksAndMessages(null);
        mLogger.debug("release cost time: " + (System.nanoTime() - st) / 1000000 + " ms");
    }

    private class StateHandler extends Handler {

        public final static short UPDATE_DETECT_RANGE = 0xaa;

        public final static short SHOW_CONNECTION_STATUS = 0xbb;

        public final static short PERFORM_STOP_DETECT = 0xcc;

        private Context mContext;

        public void setContext(Context context) {
            mContext = context;
        }

        private DetectFragment mFragment = null;

        @Override
        public void handleMessage(Message msg) {
            if (mContext != null) {
                switch (msg.what) {
                    case SHOW_CONNECTION_STATUS:
                        ((MainActivity) mContext).showToast((CharSequence) msg.obj);
                        break;
                    case UPDATE_DETECT_RANGE:
                        System.out.println("update detect range");
                        if (mFragment == null)
                            mFragment = DetectFragment.this;
                        mFragment.mDetectRangeView.setText((String) (msg.obj));
                        break;
                    case PERFORM_STOP_DETECT:
                        System.out.println("stop detect and update UI");
                        if (mFragment == null)
                            mFragment = DetectFragment.this;
                        mFragment.mDetectButton.performClick();
                        break;
                }
            }
        }
    }

    private final StateHandler mStateHandler = new StateHandler();

    private BroadcastReceiver mWifiReceiver;
    private BroadcastReceiver mBatteryReceiver;

    private WifiManager mWifiManager;

    private final int[] mBatteryIdArray = new int[]{R.drawable.sys_battery_0,
            R.drawable.sys_battery_1,
            R.drawable.sys_battery_2,
            R.drawable.sys_battery_3,
            R.drawable.sys_battery_4,
            R.drawable.sys_battery_5,
            R.drawable.sys_battery_6,
            R.drawable.sys_battery_7,
            R.drawable.sys_battery_8,
            R.drawable.sys_battery_9};

    private final int[] mSignalIdArray = new int[]{R.drawable.stat_sys_signal_0,
            R.drawable.stat_sys_signal_1,
            R.drawable.stat_sys_signal_2,
            R.drawable.stat_sys_signal_3,
            R.drawable.stat_sys_signal_4,
            R.drawable.stat_sys_signal_5};

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.detect_view, container, false);
        mTxtHandler = new MyHandler(mView);
        mDetectAnimationView = mView.findViewById(R.id.detect_animation_view);
        initView();
        initButton();
        mStateHandler.setContext(mContext);
        Context appContext = mContext.getApplicationContext();
        mWifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        registerWifiReceiver();
        registerBatteryReceiver();
        registerCallbacks();
        startThread();
        return mView;
    }

    private static class MyHandler extends Handler {

        TextView mTxtView;

        public MyHandler(View view) {
            super();
            mTxtView = view.findViewById(R.id.disk_text);
        }

        @Override
        public void handleMessage(Message msg) {
            mTxtView.setText((CharSequence) msg.obj);
        }
    }

    private MyHandler mTxtHandler;

    private void registerCallbacks() {
        NetworkDevice network = ((MainActivity) mContext).getNetwork();
        Receiver detectReceiver = network.mReceiverGroup.getReceiver("detect");
        if (detectReceiver != null) {
            DetectResultCallback detectResultCallback = new DetectResultCallback(mLogger,
                    "detect_result");
            detectReceiver.addCallback(Global.PACKET_DETECT_RESULT, detectResultCallback);
            StartDetectCallback startDetectCallback = new StartDetectCallback(mLogger,
                    "start_detect_response");
            detectReceiver.addCallback(Global.PACKET_START_DETECT_RESPONSE, startDetectCallback);
            DetectRangeCallback detectRangeCallback = new DetectRangeCallback(mLogger,
                    "detect_range_report");
            detectReceiver.addCallback(Global.DETECT_RANGE_REPORT, detectRangeCallback);
            DetectFinishedCallback detectFinishedCallback = new DetectFinishedCallback(mLogger,
                    "detect_finished");
            detectReceiver.addCallback(Global.DETECT_FINISHED, detectFinishedCallback);
            NoBreathResultCallback noBreathResultCallback = new NoBreathResultCallback(mLogger,
                    "no_breath_result");
            detectReceiver.addCallback(Global.DETECT_NO_BREATH_RESULT, noBreathResultCallback);
        }

        Receiver dataReceiver = network.mReceiverGroup.getReceiver("radar_data");
        if (dataReceiver != null) {
            BatteryCallback batteryCallback = new BatteryCallback(mLogger, "battery_info");
            dataReceiver.addCallback(Global.PACKET_BATTERY, batteryCallback);
        }
    }

    private int getWifiLevel(int numOfLevel) {
        WifiInfo info = mWifiManager.getConnectionInfo();
        mLogger.debug("rssi: " + info.getRssi());
        return WifiManager.calculateSignalLevel(info.getRssi(), numOfLevel);
    }

    private volatile boolean mRadarConnectState = false;

    private void updateWifiState() {
        int level = getWifiLevel(6);
        mLogger.debug("wifi level: " + level);
        mSignalView.setImageResource(mSignalIdArray[level]);
    }

    private void updateConnectState() {
//        boolean state = getRadarConnectState();
//        boolean state = Test.runningStatus();
        MainActivity activity = (MainActivity) mContext;
        boolean state = activity.dataCollection.getState();
        if (state == mRadarConnectState)
            return;
        mRadarConnectState = state;
        Message msg = new Message();
        msg.what = StateHandler.SHOW_CONNECTION_STATUS;
        if (!state) {
            msg.obj = "与雷达设备失去连接，请检查网络状况";
            mConnectStateView.setImageResource(R.drawable.radar_state_off);
//            mFileNameText.setText("连接已断开");

        } else {
            msg.obj = "成功连接到雷达设备";
            mConnectStateView.setImageResource(R.drawable.radar_state_on);
//            System.err.println("文件名："+activity.dataCollection.getFileName());
//            mFileNameText.setText(activity.dataCollection.getFileName());
        }
        mStateHandler.sendMessage(msg);
    }

    private void updatePadBattery(int leftBattery, int totalBattery) {
        int index = leftBattery * 10 / totalBattery;
        if (index == 10)
            index = 9;
        mPadBatteryView.setImageResource(mBatteryIdArray[index]);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void updateDisk() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File file = Environment.getExternalStorageDirectory();
            StatFs statFs = new StatFs(file.getAbsolutePath());
            long blockSize = statFs.getBlockSizeLong();
            long blockCount = statFs.getBlockCountLong();
            long availableBlocks = statFs.getAvailableBlocksLong();
            long gb = 1024 * 1024 * 1024;
            double availableMemory = (double) (blockSize * availableBlocks) / gb;
            double totalMemory = (double) (blockSize * blockCount) / gb;
            DecimalFormat formatter = new DecimalFormat("0.00");
            String txt = (String.format(mView.getResources().getString(R.string.detect_disk_text),
                    formatter.format(availableMemory),
                    formatter.format(totalMemory)));
            Message msg = new Message();
            msg.obj = txt;
            mTxtHandler.sendMessage(msg);
        }
    }

    private boolean getRadarConnectState() {
        return ((MainActivity) mContext).getNetwork().isConnected();
    }

    private void registerWifiReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mWifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action == null)
                    return;
                if (action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
                    updateWifiState();
                } else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                    if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED ||
                            mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLING) {
                        mSignalView.setImageResource(R.drawable.stat_sys_signal_null);
                    }
                }
            }
        };
        mContext.registerReceiver(mWifiReceiver, filter);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void registerBatteryReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mBatteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action != null && action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                    int leftBattery = intent.getIntExtra("level", 0);
                    int totalBattery = intent.getIntExtra("scale", 0);
                    mLogger.debug("battery: " + leftBattery * 100 / totalBattery);
                    updatePadBattery(leftBattery, totalBattery);
                }
            }
        };
        mContext.registerReceiver(mBatteryReceiver, filter);
    }

    private void initView() {
        mSignalView = mView.findViewById(R.id.signal);
        mSignalView.setImageResource(R.drawable.stat_sys_signal_0);
        mConnectStateView = mView.findViewById(R.id.radar_state);
        mConnectStateView.setImageResource(R.drawable.radar_state_off);
        mPadBatteryView = mView.findViewById(R.id.battery);
        mPadBatteryView.setImageResource(R.drawable.sys_battery_0);
        mDeviceBatteryView = mView.findViewById(R.id.device_battery);
        mDeviceBatteryView.setImageResource(R.drawable.sys_battery_0);
    }

    private final AtomicBoolean mStartingDetect = new AtomicBoolean(false);

    private final AtomicBoolean mDetecting = new AtomicBoolean(false);

    private final ReentrantLock mDetectLock = new ReentrantLock(true);

    public final boolean isDetecting() {
        return mDetecting.get();
    }

    private static boolean setBool(AtomicBoolean bool, boolean boolValue) {
        while (bool.get() != boolValue) {
            if (bool.compareAndSet(!boolValue, boolValue))
                return true;
        }
        return false;
    }

    // 接收到雷达主机开始探测响应后调用
    private void finishStartDetect() {
        mDetectLock.lock();
        try {
            if (setBool(mStartingDetect, false)) {
                if (setBool(mDetecting, true)) {
                    mDetectAnimationView.startAnimation();
                    mLogger.debug("开始探测");
                }
            }
        } finally {
            mDetectLock.unlock();
        }
    }

    // 下发开始探测指令和雷达探测参数
    private void startDetect() {
        MainActivity activity = (MainActivity) mContext;
        SettingsFragment settingsFragment = activity.requireSettingsFragment();
        int[] detectRange = settingsFragment.getDetectRange();
        NetworkDevice network = activity.getNetwork();
        Packet pack = new Packet(Global.PACKET_COMMAND, 12);
        pack.setPacketFlag(0xAAAABBBB);
        pack.putShort(Global.RADAR_COMMAND_BEGDETECT);
        pack.putShort((short) (settingsFragment.isSingleMode() ? 0 : 1));
        pack.putShort((short) detectRange[0]);
        pack.putShort((short) detectRange[1]);
        pack.putShort((short) (mAutoDetect ? 1 : 0));
        pack.putShort((short) settingsFragment.getDetectInterval());
        network.putCommandPacket(pack);
        mDetectResultSaver.startSaveResult();
        mDetectAnimationView.setDetectParams(detectRange[0], detectRange[1],
                settingsFragment.getDetectInterval());
    }

    // 下发停止探测指令
    private void stopDetect() {
        MainActivity activity = (MainActivity) mContext;
        Packet pack = new Packet(Global.PACKET_COMMAND, 2);
        pack.setPacketFlag(0xAAAABBBB);
        pack.putShort(Global.RADAR_COMMAND_ENDDETECT);
        activity.getNetwork().putCommandPacket(pack);
        mDetectResultSaver.finishSaveResult();
    }

    private boolean stopDetectAndUpdateUI() {
        mDetectLock.lock();
        try {
            if (!mDetecting.get())
                throw new IllegalStateException();
            if (setBool(mDetecting, false)) {
                stopDetect();
                mDetectAnimationView.stopAnimation();
                mLogger.debug("结束探测");
                return true;
            }
            return false;
        } finally {
            mDetectLock.unlock();
        }
    }

    private volatile boolean mAutoDetect = true;

    private void multiModeDetectOnClick() {
        mDetectLock.lock();
        try {
            CharSequence text = null;
            if (mStartingDetect.get()) {
                Toast.makeText(mContext, "正在开始探测，等待雷达主机响应", Toast.LENGTH_SHORT).show();
                return;
            }
            if (mDetecting.get()) {
                if (stopDetectAndUpdateUI())
                    text = "开始探测";
            } else {
                if (!getRadarConnectState()) {
                    text = "未连接到雷达设备，请先检查网络状态";
                    Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (setBool(mStartingDetect, true)) {
                    text = "停止探测";
                    startDetect();
                }
            }
            if (text != null)
                ((TextView) mView.findViewById(R.id.detect_button_text)).setText(text);
        } finally {
            mDetectLock.unlock();
        }
    }

    private volatile boolean stopSimulation = false;

    Thread simulateThread = null;

    private void new90BDetectButtonOnClick() {
        mDetectLock.lock();
        try {
            MainActivity activity = (MainActivity) mContext;
            String txt;
            if (!mDetecting.get()) {
                txt = "停止探测";
                SettingsFragment settingsFragment = activity.requireSettingsFragment();
                int[] detectRange = settingsFragment.getDetectRange();
                DetectRange range = new DetectRange();
                int rs = detectRange[0], re = detectRange[1];
                int interval = settingsFragment.getDetectInterval();
                range.set(rs, re, interval);
                DetectParams.Value v = new DetectParams.Value();
                v.signalPos = 12 + (rs * 20) / 3;
                v.antennaType = 400;
                if (interval == 3) {
                    v.scanSpeed = 32;
                    v.sampleLen = 2048;
                    v.window = 20;
                } else {
                    v.scanSpeed = 16;
                    v.sampleLen = 8192;
                    v.window = 80;
                }
                DetectParams params = new DetectParams();
                params.set(v);
                activity.radarDetect.startDetect(params, range);
                mDetecting.set(true);
                stopSimulation = false;
                simulateThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        byte[] buf = new byte[v.sampleLen << 1];
                        while (!stopSimulation) {
                            activity.radarDetect.pushData(buf, 0, buf.length, v.sampleLen);
                            long interval = 1000 / (v.scanSpeed);
                            try {
                                Thread.sleep(interval);
                            } catch (InterruptedException ignore) {
                            }
                        }
                    }
                });
                simulateThread.start();
            } else {
                txt = "开始探测";
                activity.radarDetect.stopDetect();
                mDetecting.set(false);
                stopSimulation = true;
                simulateThread.interrupt();
                try {
                    simulateThread.join();
                } catch (InterruptedException ignore) {
                }
            }
            ((TextView) mView.findViewById(R.id.detect_button_text)).setText(txt);
        } finally {
            mDetectLock.unlock();
        }
    }

    private void initButton() {
        mDetectButton = mView.findViewById(R.id.detect_button);
        mDetectButton.setImageResource(R.drawable.system_start);
        ((TextView) mView.findViewById(R.id.detect_button_text)).setText("开始探测");
        mDetectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                new90BDetectButtonOnClick();
//                multiModeDetectOnClick();
//                newLifeSearchDetectButtonOnClick();
                testLifeSearchDetectButtonOnClick();
            }
        });
        mFileNameText = (TextView) mView.findViewById(R.id.fileName_text);
        mAutoDetectButton = mView.findViewById(R.id.auto_detect_button);
        mAutoDetectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAutoDetect) {
                    ((TextView) mView.findViewById(R.id.auto_detect_txt))
                            .setTextColor(getResources().getColor(R.color.black));
                    mAutoDetect = false;
                } else {
                    ((TextView) mView.findViewById(R.id.auto_detect_txt))
                            .setTextColor(getResources().getColor(R.color.orange));
                    mAutoDetect = true;
                }
            }
        });
        ((TextView) mView.findViewById(R.id.auto_detect_txt))
                .setTextColor(getResources().getColor(R.color.orange));

        mCheckButton = mView.findViewById(R.id.check_button);
        mCheckButton.setImageResource(R.drawable.check_off);
        mCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCheckOff) {
                    mCheckButton.setImageResource(R.drawable.check_on);
                    mCheckOff = false;
                } else {
                    mCheckButton.setImageResource(R.drawable.check_off);
                    mCheckOff = true;
                }
            }
        });

        mWifiButton = mView.findViewById(R.id.wifi_button);
        mWifiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });

        mExitButton = mView.findViewById(R.id.exit_button);
        mExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) mContext).exit();
            }
        });
    }

    public boolean checkDetectState() {
        if (mDetecting.get()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("退出程序");
            builder.setMessage("请先停止探测再退出程序!");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //
                }
            });
            builder.show();
            return false;
        }
        return true;
    }

    private void startThread() {
        mStop = false;
        mUpdateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                doRun();
            }
        });
        mUpdateThread.start();
    }

    private Thread mUpdateThread = null;

    private volatile boolean mStop = true;

    private void stopThread() {
        if (!mStop) {
            mStop = true;
            mUpdateThread.interrupt();
        }
    }

    private void doRun() {
        updateWifiState();
//        MainActivity activity = (MainActivity) mContext;
        while (!mStop) {
            //更新连接状态
            updateConnectState();
//            mFileNameText.setText(activity.dataCollection.getmFraFileName());
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                updateDisk();
//            } else {
//                mLogger.error("build version is too old");
//                mStop = true;
//                break;
//            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                mLogger.debug("interrupt sleep");
            }
        }
    }

    private final static String detectResultFiles = Environment.getExternalStorageDirectory() +
            "/DetectResults";

    public static String getDetectResultPath() {
        return detectResultFiles;
    }

    // 探测结果保存对象，用户缓存探测结果
    private final DetectResultSaver mDetectResultSaver = new DetectResultSaver(detectResultFiles);

    // 探测结果回调，在收到探测结果时调用
    public class DetectResultCallback extends MyCallback {

        public DetectResultCallback(AbstractLogger logger, String packetName) {
            super(logger, packetName);
        }

        @Override
        public void invoke(Packet pack) {
            super.invoke(pack);
            mLogger.debug("receive detect result");
            mDetectAnimationView.mDetectResults.addResults(pack);
            mDetectResultSaver.saveResult(pack);
        }
    }

    // 雷达主机开始探测响应回调
    public class StartDetectCallback extends MyCallback {
        public StartDetectCallback(AbstractLogger logger, String packetName) {
            super(logger, packetName);
        }

        @Override
        public void invoke(Packet pack) {
            super.invoke(pack);
            mLogger.debug("receive start detect response");
            finishStartDetect();
            mDetectAnimationView.resetDetectTime();
        }
    }

    // 探测范围回调，在雷达主机上传当前探测范围是被调用
    public class DetectRangeCallback extends MyCallback {
        public DetectRangeCallback(AbstractLogger logger, String packetName) {
            super(logger, packetName);
        }

        @Override
        public void invoke(Packet pack) {
            super.invoke(pack);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException ignore) {
                    }
                    mDetectAnimationView.mDetectResults.clearResults(false);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ignore) {
                    }
                    mLogger.debug("receive detect range report");
                    int detectStart = pack.getShort();
                    int detectEnd = pack.getShort();
                    mLogger.debug("start: " + detectStart + ", end: " + detectEnd);
                    mDetectAnimationView.setDetectRangeParams(detectStart, detectEnd);
                }
            }).start();
        }
    }

    //sunyan 2021.11.3 add begin
    private void newLifeSearchDetectButtonOnClick() {
        mDetectLock.lock();
        try {
            MainActivity activity = (MainActivity) mContext;
            String txt;
            if (!mDetecting.get()) {
                txt = "停止探测";
//                SettingsFragment settingsFragment = activity.requireSettingsFragment();
                int[] detectRange = new int[]{0, 3};
                DetectRange range = new DetectRange();
                //初始范围
                int rs = detectRange[0], re = detectRange[1];
//                int interval = settingsFragment.getDetectInterval();
                int interval = 3;
                range.set(rs, re, interval);
                DetectParams.Value v = new DetectParams.Value();
                //sunyan 2021.11.8 update  begin
//                v.signalPos = 12 + (rs * 20) / 3;
                v.signalPos = 22 + (rs * 20) / 3;
                //sunyan 2021.11.8 update end
                v.antennaType = 400;
                if (interval == 3) {
                    v.scanSpeed = 128;
                    v.sampleLen = 2048;
                    v.window = 20;
                } else {
                    v.scanSpeed = 16;
                    v.sampleLen = 8192;
                    v.window = 80;
                }
                DetectParams params = new DetectParams();
                params.set(v);
                //给算法发送参数
                //sunyan 2021.11.4 add begin
                //activity.dataCollection.setFileName(createNewFileName());
                activity.setFileName(activity.createNewLog());
                //发送下位机参数
                activity.dataCollection.setParams("{\"samplingPoints\":2048,\"scanSpeed\":32,\"timeWindow\":21,\"signalPosition\":22,\"signalGain\":[4,4,4,4,4,4,4,4,4],\"automaticGain\":0}");
                activity.dataCollection.setmParams("{\"samplingPoints\":2048,\"scanSpeed\":32,\"timeWindow\":21,\"signalPosition\":22,\"signalGain\":[4,4,4,4,4,4,4,4,4],\"automaticGain\":0，\"detectionPos\":3}");
                activity.dataCollection.start();
                //sunyan 2021.11.4 add end
                activity.radarDetect.startDetect(params, range);
                mDetecting.set(true);

            } else {
                txt = "开始探测";
                activity.radarDetect.stopDetect();
                mDetecting.set(false);
//                stopThread();
                if (activity.fos != null) {
                    try {
                        activity.fos.close();
                    } catch (IOException e) {
                    }
                }
//                }
                Test.endSaveData();
                int ret = Test.stop();
                activity.dataCollection.shutdown();

            }
            ((TextView) mView.findViewById(R.id.detect_button_text)).setText(txt);
        } finally {
            mDetectLock.unlock();
        }
    }

    //sunyan 2021.11.3 add end
    //sunyan 2021.12.8 add begin

    private void testLifeSearchDetectButtonOnClick() {
        mDetectLock.lock();
        try {
            MainActivity activity = (MainActivity) mContext;
            String txt;
            if (!mDetecting.get()) {
                txt = "停止探测";
                SettingsFragment settingsFragment = activity.requireSettingsFragment();
                int[] detectRange = settingsFragment.getDetectRange();
                DetectRange range = new DetectRange();
                //初始范围
                int rs = detectRange[0], re = detectRange[1];
                int interval = settingsFragment.getDetectInterval();
                range.set(rs, re, interval);
                DetectParams.Value v = new DetectParams.Value();
                //sunyan 2021.11.8 update  begin
//                v.signalPos = 12 + (rs * 20) / 3;
                v.signalPos = 22 + (rs * 20) / 3;
                //sunyan 2021.11.8 update end
                v.antennaType = 400;
                if (interval == 3) {
                    v.scanSpeed = 32;
                    v.sampleLen = 2048;
                    v.window = 20;
                } else {
                    v.scanSpeed = 16;
                    v.sampleLen = 8192;
                    v.window = 80;
                }
                DetectParams params = new DetectParams();
                params.set(v);
                //给算法发送参数
                //sunyan 2021.11.4 add begin
                //activity.dataCollection.setFileName(createNewFileName());
                activity.setFileName(activity.createNewLog());
                //发送下位机参数
                activity.dataCollection.setParams(activity.dataCollection.setInitParams(v.signalPos));
                activity.dataCollection.setmParams(activity.dataCollection.setInitParams(v.signalPos));
                activity.dataCollection.start();
                //sunyan 2021.11.4 add end
                activity.radarDetect.startDetect(params, range);
                mDetecting.set(true);

            } else {
                txt = "开始探测";
                activity.radarDetect.stopDetect();
                mDetecting.set(false);
//                stopThread();
                if (activity.fos != null) {
                    try {
                        activity.fos.close();
                    } catch (IOException e) {
                    }
                }
//                }
                Test.endSaveData();
                int ret = Test.stop();
                activity.dataCollection.shutdown();

            }
            ((TextView) mView.findViewById(R.id.detect_button_text)).setText(txt);
        } finally {
            mDetectLock.unlock();
        }
    }
    //sunyan 2021.12.8 add end
    // 本次探测结束回调，接收到雷达主机结束探测响应时调用
    public class DetectFinishedCallback extends MyCallback {

        public DetectFinishedCallback(AbstractLogger logger, String packetName) {
            super(logger, packetName);
        }

        @Override
        public void invoke(Packet pack) {
            super.invoke(pack);
            mLogger.debug("receive detect finished");
            Message msg = new Message();
            msg.what = StateHandler.PERFORM_STOP_DETECT;
            mStateHandler.sendMessage(msg);
        }
    }

    public class NoBreathResultCallback extends MyCallback {
        public NoBreathResultCallback(AbstractLogger logger, String packetName) {
            super(logger, packetName);
        }

        @Override
        public void invoke(Packet pack) {
            super.invoke(pack);
            mLogger.debug("clear breath results");
            mDetectAnimationView.mDetectResults.clearResults(false, false);
        }
    }

    public class BatteryCallback extends MyCallback {
        public BatteryCallback(AbstractLogger logger, String packetName) {
            super(logger, packetName);
        }

        @Override
        public void invoke(Packet pack) {
            super.invoke(pack);
            mLogger.debug("receive battery info");
            short level = pack.getShort();
            int index = level / 10;
            if (index >= 10)
                index = 9;
            mDeviceBatteryView.setImageResource(mBatteryIdArray[index]);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
