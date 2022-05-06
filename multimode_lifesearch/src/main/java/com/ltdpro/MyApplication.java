package com.ltdpro;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.StatFs;
import android.view.View;
import android.widget.ExpandableListView;

import com.ltd.multimodelifesearch.activity.MultiModeLifeSearchActivity;
import com.ltd.multimodelifesearch.ui.LeftFragment;


/*
 * 自定义全局对象
 */
public class MyApplication extends Application {
    private static int MODE = Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE;
    private static final String PackageName_Default = "com.example.defaultparam";
    private SharedPreferences share = null;
    private SharedPreferences.Editor edit = null;

    private static final String LIGHTKEY = "lightstate";
    private static final String WHEELSTATE = "wheelinversion";
    private static final String FILEDSCEND = "isfiledscend";
    private static final String CUSTOMWHEEL = "customwheel";

    /////////////////////////////////
    //实时采集使用的标尺
    public View mTimewndRuler = null;
    public View mDeepRuler = null;
    public View mHorRuler = null;
    //回放数据使用的标尺
    public View mBTimewndRuler = null;
    public View mBDeepRuler = null;
    public View mBHorRuler = null;

    public MultiModeLifeSearchActivity mMainActivity;

    //雷达设备对象
    public radarDevice mRadarDevice = new radarDevice();
    public RadarDetect mRadarDetect = new RadarDetect(mRadarDevice, 9, 80, 12, 30);
    public colorPalette mColorPal = new colorPalette();
    //电池电量
    public PowerDevice mPowerDevice = new PowerDevice();                //电量获取类

    //实时参数调节视图中参数变量
    private int mRealtimeLayoutLeftnousewidth = 0;     //250;       //在实时采集界面下，左边不用来滚动的范围
    private int mRealtimeLayoutRightnousewidth = 100;
    private int mRealtimeLayoutBottomnouseheight = 600;

    //系统的一些参数
    private int mScreenWidth;                //屏幕宽
    private int mScreenHeight;               //屏幕高

    private int mMaxScrollPosX;              //x方向最大滚动坐标
    private int mMixScrollPosX;              //x方向最小滚动坐标

    //存放雷达每次读取的数据长度
    private int mOnceReadLength = 163840;
    private short[] mReadBuf = new short[163840];   //每次读取的雷达数据存放位置

    public int getOnceMaxReadLength() {
        return mOnceReadLength;
    }

    //
    private String TAG = "MyApplication";

    //
    private boolean mLockScreen = false;   //是否锁定屏幕

    //
    private int mScansPerPIC = 1000;   //每页回放位图显示的道数
    private int mHeightPerPIC = 512;   //每页回放位图显示的高度
    private int mWigglePixs = 8;         //堆积图每道占用的像素点数
    private int mWiggleWndWidth = 16;    //堆积图填充宽度

    //电池电量报警刻度值
    private double mRadarPowerAlaramScale = 0.1;
    private double mPCPowerAlarmScale = 5;
    private byte mFanStartT = 38;

    public int mCurScreen = 1;  //当前显示的Activity

    //无线WiFi网络设备
//	WiFiNetDevice mWifiDevice;
//	
//	//
//	public WhellcheckActivity  mWhellcheckActivity = null;
//	WirelessActivity    mWirelessActivity = null;
//	GPSsetActivity      mGPSActivity = null;
//	ParamsetActivity    mParamsetActivity = null;
//	public LTDMainActivity  mMainActivity = null;

    /**
     * 实时图像
     */
    public realTimeDIBView mRealDibView;
    /**
     * 生成实时处理线程
     */
    public boolean mExit = false;                             //是否退出app
    private boolean mRealthreadStop = false;                     //是否停止实时处理线程
    private long mRealthreadSleepTime = 50;             //实时处理线程休眠时间,休眠时间需要再仔细计算
    private boolean mRealthreadReadingDatas = false;         //是否开始读取雷达数据
    private boolean mIsLightOn = false;                         //你的灯是否亮着
    private boolean isCustomSetting = false;                 //是否在自定义设置测距轮

    public boolean isCustomSetting() {
        return isCustomSetting;
    }

    public void setCustomSetting(boolean isCustomSetting) {
        this.isCustomSetting = isCustomSetting;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
    }

    /**
     * 保存fragment的context状态
     */
    public Context leftFragContext;
    private int mTab;

    public void saveLeftFragmentContext(Context inputContext) {
        leftFragContext = inputContext;
    }

    private volatile LeftFragment mFragLeft;

    public void setLeftFragment(LeftFragment fragLeft) { mFragLeft = fragLeft; }

    public LeftFragment getLeftFragment() { return mFragLeft; }

    public Context getLeftFragmentContext() {
        return leftFragContext;
    }

    //保存leftfragment的mTab值
    public void saveLeftFragmentTab(int inputTab) {
        mTab = inputTab;
    }

    public int getLeftFragmentTab() {
        return mTab;
    }

    public long getRealthreadSleepTime() {
        return mRealthreadSleepTime;
    }

    //灯状态设置
    public void setPowerLightState(boolean input_state) {
        this.mIsLightOn = input_state;
        edit.putBoolean(LIGHTKEY, mIsLightOn);
        edit.commit();
    }

    //得到灯的状态信息
    public boolean getPowerLightState() {
        return (this.mIsLightOn);
    }

    /**
     * 记录翻转状态
     */
    public void rememberTurnWheel() {
        edit.putBoolean(WHEELSTATE, mRadarDevice.getTurnWheel());
        edit.commit();
    }

    /**
     * 记录自定义测距轮选择的参数
     */
    public void rememberCustomWheelIndex(int index) {
        edit.putInt(CUSTOMWHEEL, index);
        edit.commit();
    }

    public int getCustomWheelIndex() {
        share = getSharedPreferences(PackageName_Default, MODE);
        edit = share.edit();
        int index = 0;
        if (share.contains(MyApplication.CUSTOMWHEEL)) {
            index = share.getInt(CUSTOMWHEEL, 0);
        } else {
            edit.putInt(CUSTOMWHEEL, 0);
            edit.commit();
        }
        return index;
    }

    /**
     * 设置记录状态
     */
//	public void setBackgroundState()
//	{
//		memoryLightState();		
//		memoryTurnWheelState();		
//		memoryFileDscend();
//	}
    //记录之前是升序还是降序排列
    public void getFileDscend() {
        share = getSharedPreferences(PackageName_Default, MODE);
        edit = share.edit();
        if (share.contains(FILEDSCEND)) {
            isDecendOrder = share.getBoolean(FILEDSCEND, false);
        } else {
            edit.putBoolean(FILEDSCEND, isDecendOrder);
            edit.commit();
        }
    }

    public void rememberFileDscend() {
        edit.putBoolean(FILEDSCEND, isDecendOrder);
        edit.commit();
    }

    public void getTurnWheelState() {
        share = getSharedPreferences(PackageName_Default, MODE);
        edit = share.edit();
        if (share.contains(WHEELSTATE)) {
            boolean wheel_state = false;
            wheel_state = share.getBoolean(WHEELSTATE, false);
            mRadarDevice.turnWhell(wheel_state);
        } else {
            edit.putBoolean(WHEELSTATE, mRadarDevice.getTurnWheel());
            edit.commit();
        }
    }

    public void getLightState() {
        share = getSharedPreferences(PackageName_Default, MODE);
        edit = share.edit();
        if (share.contains(LIGHTKEY)) {
            mIsLightOn = share.getBoolean(LIGHTKEY, false);
            setPowerLightState(mIsLightOn);
        } else {
            //Toast.makeText(this, "没找到KEY！IDSC2600/sharedpref！", Toast.LENGTH_SHORT).show();
            edit.putBoolean(LIGHTKEY, mIsLightOn);
            edit.commit();
        }
    }

    //设置休眠时间
    public void setRealthreadSleepTime(long time) {
        mRealthreadSleepTime = time;
    }

    public boolean isRealThreadStop() {
        return mRealthreadStop;
    }

    public void setRealThreadStop(boolean setBool) {
        mRealthreadStop = setBool;
    }

    //是否开始读取雷达数据
    public boolean isRealThreadReadingDatas() {
        return mRealthreadReadingDatas;
    }

    //设置是否读取雷达数据
    public void setRealThreadReadingDatas(boolean setBool) {
        mRealthreadReadingDatas = setBool;
    }

    private String mBackplayFilePath;
    private String mRadarParamsFolderPath = "/radarParams/";
    private String mRadarDatasFolderPath = "/LteFiles/";
    public String mGPSSavingFilePath;   //正在保存的GPS文件名称
    public FileOutputStream fGPSSave = null;

    private static MyApplication mInstance = null;
    public boolean m_bKeyRight = true;

    //
    List<Object> mGPSRecordList = null;

    //判断是否正在使用本设备的GPS
    private int GPS_USESELF = 1;
    private int GPS_USEOTHER = 2;
    public int mUseGPSType = GPS_USESELF;
    //
    private int INPUT_USEWHEEL = 1;
    private int INPUT_USEKEY = 2;
    public int mUseInputType = INPUT_USEKEY;
    //
    int POWERERR_CHECK = 1;
    int POWERERR_NOCHECK = 2;
    public int mPowerErrCheckType = POWERERR_NOCHECK;

    public int getInputTypeSel() {
        int sel = 0;
        if (mUseInputType == INPUT_USEWHEEL)
            sel = 0;
        if (mUseInputType == INPUT_USEKEY)
            sel = 1;
        return sel;
    }

    public int getInputType() {
        return mUseInputType;
    }

    public void setInputType(int type) {
        mUseInputType = type;
    }

    public boolean isPowerErrNoCheck() {
        return mPowerErrCheckType == POWERERR_NOCHECK;
    }

    public boolean isPowerErrCheck() {
        return mPowerErrCheckType == POWERERR_CHECK;
    }

    public boolean isUseSelfGPS() {
        return mUseGPSType == GPS_USESELF;
    }

    public boolean isUseWheelInput() {
        return mUseInputType == INPUT_USEWHEEL;
    }

    public boolean isUseKeyInput() {
        return mUseInputType == INPUT_USEKEY;
    }

    public void setFanStartT(byte val) {
        mFanStartT = val;
    }

    public byte getFanStartT() {
        return mFanStartT;
    }

    public MyApplication() {
//		mWifiDevice = new WiFiNetDevice();
//		mWifiDevice.setApplication(this);
        mInstance = this;
        //
        mGPSRecordList = new ArrayList<Object>();

        //////
		/*
		mGPSSavingFilePath = android.os.Environment.getExternalStorageDirectory() + mRadarDevice
		.mLTEFilefolderPath;
		mGPSSavingFilePath +="def.gps";
		setGPSFilePath(mGPSSavingFilePath);
		*/
    }

    public void setGPSFilePath(String path) {
        mGPSSavingFilePath = path;
        try {
            if (fGPSSave != null) {
                fGPSSave.close();
            }
            fGPSSave = new FileOutputStream(mGPSSavingFilePath);
        } catch (Exception e) {

        }
    }

    //保存gps信息
    public synchronized void saveGPSRecord() {
//		int size;
//		size = mGPSRecordList.size();
//		int i;
//		try{
//			//
//			for(i=0;i<size;i++)
//			{
//				GPSRecord r=(GPSRecord) mGPSRecordList.get(i);
//				r.save(fGPSSave);
//				mGPSRecordList.remove(r);
//			}
//		}
//		catch(Exception e)
//		{
//			DebugUtil.i(TAG,"Save GPSRecords fail!");
//		}

    }

    //
    public void setBackplayFilePath(String path) {
        mBackplayFilePath = path;
    }

    public String getBackplayFilePath() {
        return mBackplayFilePath;
    }

    //清除gps信息队列
    public synchronized void clearLocationsArray() {
        mGPSRecordList.clear();
    }

    public static MyApplication getInstance() {
        return mInstance;
    }

    //得到雷达电量报警刻度值
    public double getRadarPowerAlarmScale() {
        return mRadarPowerAlaramScale;
    }

    public void setRadarPowerAlarmLevel(float level) {
        mRadarPowerAlaramScale = level;
    }

    //得到平板电脑报警刻度值
    public double getPCPowerAlarmScale() {
        return mPCPowerAlarmScale;
    }

    public void setPCPowerAlaramLevel(float level) {
        mPCPowerAlarmScale = level;
    }

    //回放数据时，每个位图切片显示的道数
    public int getScansPerPIC() {
        return mScansPerPIC;
    }

    //
    public int getHeightPerPIC() {
        return mHeightPerPIC;
    }

    //
    public short[] GetRadarDatasBuf() {
        return mReadBuf;
    }

    //
    public int getWigglePixsPerScan() {
        return mWigglePixs;
    }

    public int getWiggleWndWidth() {
        return mWiggleWndWidth;
    }

    //得到点测时重复次数
    public int getDianceNumber() {
        return mRadarDevice.getDianceNumber();
    }

    //得到测距轮控制下的标记扩展数
    public int getWheelExtendNumber() {
        return mRadarDevice.getWheelExtendNumber();
    }

    //得到选择的测距轮类型
    public int getWheeltypeSel() {
        return mRadarDevice.getWheeltypeSel();
    }

    //得到天线类型
    public int getAntenFrqSel() {
        return mRadarDevice.getAntenFrqSel();
    }

    //得到扫苏选择
    public int getScanSpeedSel() {
        return mRadarDevice.getScanSpeedSel();
    }

    //
    public int getScanLengthSel() {
        return mRadarDevice.getScanLengthSel();
    }

    //
    public int getFilterSel() {
        return mRadarDevice.getFilterSel();
    }

    //
    public int getRemoveBackSel() {
        return mRadarDevice.getRemoveBackSel();
    }

    //
    public int getScrollPosXMin() {
        return mMixScrollPosX;
    }

    public int getScrollPosXMax() {
        return mMaxScrollPosX;
    }

    public void setScrollPosXRange(int mix, int max) {
        mMixScrollPosX = mix;
        mMaxScrollPosX = max;
    }

    //
    public int getRealtimeLayoutLeftnousewidth() {
        return mRealtimeLayoutLeftnousewidth;
    }

    public int getRealtimeLayoutRightnousewidth() {
        return mScreenWidth - mRealtimeLayoutRightnousewidth;
    }

    public int getRealtimeLayoutBottomnouseheight() {
        return mRealtimeLayoutBottomnouseheight;
    }

    //得到sd卡存储容量
    public long[] getSDCardMemory() {
        long[] sdCardInfo = new long[2];

        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
//	          File sdcardDir = this.mRadarDevice.mStoragePath;//Environment
//	          .getExternalStorageDirectory();

            StatFs sf = new StatFs(radarDevice.mStoragePath);//(sdcardDir.getPath());

            long bSize = sf.getBlockSize();
//	          long bsizeL = sf.getBlockSizeLong();

            long bCount = sf.getBlockCount();
//	          long bCountL = sf.getBlockCountLong();

            long availBlocks = sf.getAvailableBlocks();
//	          long availBlocksL = sf.getAvailableBlocksLong();

            sdCardInfo[0] = bSize * bCount;//总大小

            sdCardInfo[1] = bSize * availBlocks;//可用大小
        }

        return sdCardInfo;
    }

    //从sd卡中得到lte文件名列表
    public boolean getLTDFilenamesFromeSD(List<String> nameList) {
        String fileName;
        String pathName;
        pathName = android.os.Environment.getExternalStorageDirectory() +
                   mRadarDevice.mLTEFilefolderPath;
        File f = new File(pathName);
        File[] files = f.listFiles();
        if (files == null) {
            nameList.clear();
            return true;
        }
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (isLTDFileName(file.getPath()))
                nameList.add(file.getPath());
//			DebugUtil.i(TAG,fileName);
        }
        //
        return true;
    }

    //从sd卡中德奥雷达参数文件名列表
    public boolean getParamFilenamesFromeSD(List<String> nameList, String path) {
        File f = new File(path);
        File[] files = f.listFiles();
        if (files == null)
            return false;
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (isParamFileName(file.getPath()))
                    nameList.add(file.getPath());
                //			DebugUtil.i(TAG,fileName);
            }
        }
        //
        return true;
    }

    /**
     * @param nameList
     * @param path
     * @return
     */
    public boolean getCheckFileNamesFormSD(List<String> nameList, String path) {
        File f = new File(path);
        File[] files = f.listFiles();
        if (files == null)
            return false;
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (isCheckFileName(file.getPath()))
                    nameList.add(file.getPath());
                //			DebugUtil.i(TAG,fileName);
            }
        }
        return true;
    }

    public void setRadardatasFolderpath(String path) {
        mRadarDatasFolderPath = path;
    }

    /**
     * 返回雷达文件夹名称
     *
     * @return
     */
    public String getRadardatasFolderpath() {
        return mRadarDatasFolderPath;
    }

    //判断是否是雷达数据文件
    public boolean isLTDFileName(String fileName) {
        String end = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length())
                             .toLowerCase();
        if (end.equals("lte"))
            return true;
        return false;
    }

    //判断是否是雷达参数文件
    public boolean isParamFileName(String fileName) {
        String end = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length())
                             .toLowerCase();
        if (end.equals("par"))
            return true;
        return false;
    }

    //判断是否是校准参数文件
    public boolean isCheckFileName(String fileName) {
        String end = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length())
                             .toLowerCase();
        if (end.equals("check"))
            return true;
        return false;
    }

    //
    public boolean isScreenLock() {
        return mLockScreen;
    }

    //
    public void setScreenRange(int w, int h) {
        mScreenWidth = w;
        mScreenHeight = h;
    }

    public int getScreenWidth() {
        return mScreenWidth;
    }

    public int getScreenHeight() {
        return mScreenHeight;
    }

    //读取一次数据
    public int radarReadMaxWaveDatas() {
        //读取指定长度的数据
        mReadBuf[0] = (short) 0;
        mReadBuf[1] = (short) 1;
        return mRadarDevice.readDatas(mReadBuf, (mOnceReadLength * 2));
    }

    //
    public void setLockScreen(boolean lock) {
        mLockScreen = lock;
    }

    public boolean getLockScreen() {
        return mLockScreen;
    }

    //////////////
    public ExpandableListView mListView = null;
    private boolean isDraw = true;//是否画图

    public boolean isDraw() {
        return isDraw;
    }

    public void setRealTimeDraw(boolean inputDraw) {
        isDraw = inputDraw;
    }

    /**
     * 小数保留位数计算
     */
    public double setDigits(double inputNum, int digitNum) {
        double tempNum = inputNum;
        BigDecimal b = new BigDecimal(tempNum);
        tempNum = b.setScale(digitNum, BigDecimal.ROUND_HALF_DOWN).doubleValue();//向下取整
        return tempNum;
    }

    private boolean misHardplusRun = false;//是否开启读取线程的线程

    public boolean getIsHardplusRun() {
        return misHardplusRun;
    }

    public void setIsHardplusRun(boolean inputBool) {
        misHardplusRun = inputBool;
        DebugUtil.i("threadTAG", "setIsHardplusRun=" + misHardplusRun);
    }

    private boolean misRunFirstHardplusThread = false;

    //开启线程
    public boolean isRunFirstHardplusThread() {
        return misRunFirstHardplusThread;
    }

    //关闭线程
    public void setRunFirstHardplusThread(boolean inputHardplusThread) {
        misRunFirstHardplusThread = inputHardplusThread;
    }

    private boolean isDecendOrder = false;

    //得到降序状态
    public boolean getisDecendOrder() {
        return isDecendOrder;
    }

    //设置是否降序
    public void setIsDecendOrder(boolean inputState) {
        isDecendOrder = inputState;
    }
}
