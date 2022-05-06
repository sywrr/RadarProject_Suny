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
 * �Զ���ȫ�ֶ���
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
    //ʵʱ�ɼ�ʹ�õı��
    public View mTimewndRuler = null;
    public View mDeepRuler = null;
    public View mHorRuler = null;
    //�ط�����ʹ�õı��
    public View mBTimewndRuler = null;
    public View mBDeepRuler = null;
    public View mBHorRuler = null;

    public MultiModeLifeSearchActivity mMainActivity;

    //�״��豸����
    public radarDevice mRadarDevice = new radarDevice();
    public RadarDetect mRadarDetect = new RadarDetect(mRadarDevice, 9, 80, 12, 30);
    public colorPalette mColorPal = new colorPalette();
    //��ص���
    public PowerDevice mPowerDevice = new PowerDevice();                //������ȡ��

    //ʵʱ����������ͼ�в�������
    private int mRealtimeLayoutLeftnousewidth = 0;     //250;       //��ʵʱ�ɼ������£���߲����������ķ�Χ
    private int mRealtimeLayoutRightnousewidth = 100;
    private int mRealtimeLayoutBottomnouseheight = 600;

    //ϵͳ��һЩ����
    private int mScreenWidth;                //��Ļ��
    private int mScreenHeight;               //��Ļ��

    private int mMaxScrollPosX;              //x��������������
    private int mMixScrollPosX;              //x������С��������

    //����״�ÿ�ζ�ȡ�����ݳ���
    private int mOnceReadLength = 163840;
    private short[] mReadBuf = new short[163840];   //ÿ�ζ�ȡ���״����ݴ��λ��

    public int getOnceMaxReadLength() {
        return mOnceReadLength;
    }

    //
    private String TAG = "MyApplication";

    //
    private boolean mLockScreen = false;   //�Ƿ�������Ļ

    //
    private int mScansPerPIC = 1000;   //ÿҳ�ط�λͼ��ʾ�ĵ���
    private int mHeightPerPIC = 512;   //ÿҳ�ط�λͼ��ʾ�ĸ߶�
    private int mWigglePixs = 8;         //�ѻ�ͼÿ��ռ�õ����ص���
    private int mWiggleWndWidth = 16;    //�ѻ�ͼ�����

    //��ص��������̶�ֵ
    private double mRadarPowerAlaramScale = 0.1;
    private double mPCPowerAlarmScale = 5;
    private byte mFanStartT = 38;

    public int mCurScreen = 1;  //��ǰ��ʾ��Activity

    //����WiFi�����豸
//	WiFiNetDevice mWifiDevice;
//	
//	//
//	public WhellcheckActivity  mWhellcheckActivity = null;
//	WirelessActivity    mWirelessActivity = null;
//	GPSsetActivity      mGPSActivity = null;
//	ParamsetActivity    mParamsetActivity = null;
//	public LTDMainActivity  mMainActivity = null;

    /**
     * ʵʱͼ��
     */
    public realTimeDIBView mRealDibView;
    /**
     * ����ʵʱ�����߳�
     */
    public boolean mExit = false;                             //�Ƿ��˳�app
    private boolean mRealthreadStop = false;                     //�Ƿ�ֹͣʵʱ�����߳�
    private long mRealthreadSleepTime = 50;             //ʵʱ�����߳�����ʱ��,����ʱ����Ҫ����ϸ����
    private boolean mRealthreadReadingDatas = false;         //�Ƿ�ʼ��ȡ�״�����
    private boolean mIsLightOn = false;                         //��ĵ��Ƿ�����
    private boolean isCustomSetting = false;                 //�Ƿ����Զ������ò����

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
     * ����fragment��context״̬
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

    //����leftfragment��mTabֵ
    public void saveLeftFragmentTab(int inputTab) {
        mTab = inputTab;
    }

    public int getLeftFragmentTab() {
        return mTab;
    }

    public long getRealthreadSleepTime() {
        return mRealthreadSleepTime;
    }

    //��״̬����
    public void setPowerLightState(boolean input_state) {
        this.mIsLightOn = input_state;
        edit.putBoolean(LIGHTKEY, mIsLightOn);
        edit.commit();
    }

    //�õ��Ƶ�״̬��Ϣ
    public boolean getPowerLightState() {
        return (this.mIsLightOn);
    }

    /**
     * ��¼��ת״̬
     */
    public void rememberTurnWheel() {
        edit.putBoolean(WHEELSTATE, mRadarDevice.getTurnWheel());
        edit.commit();
    }

    /**
     * ��¼�Զ�������ѡ��Ĳ���
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
     * ���ü�¼״̬
     */
//	public void setBackgroundState()
//	{
//		memoryLightState();		
//		memoryTurnWheelState();		
//		memoryFileDscend();
//	}
    //��¼֮ǰ�������ǽ�������
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
            //Toast.makeText(this, "û�ҵ�KEY��IDSC2600/sharedpref��", Toast.LENGTH_SHORT).show();
            edit.putBoolean(LIGHTKEY, mIsLightOn);
            edit.commit();
        }
    }

    //��������ʱ��
    public void setRealthreadSleepTime(long time) {
        mRealthreadSleepTime = time;
    }

    public boolean isRealThreadStop() {
        return mRealthreadStop;
    }

    public void setRealThreadStop(boolean setBool) {
        mRealthreadStop = setBool;
    }

    //�Ƿ�ʼ��ȡ�״�����
    public boolean isRealThreadReadingDatas() {
        return mRealthreadReadingDatas;
    }

    //�����Ƿ��ȡ�״�����
    public void setRealThreadReadingDatas(boolean setBool) {
        mRealthreadReadingDatas = setBool;
    }

    private String mBackplayFilePath;
    private String mRadarParamsFolderPath = "/radarParams/";
    private String mRadarDatasFolderPath = "/LteFiles/";
    public String mGPSSavingFilePath;   //���ڱ����GPS�ļ�����
    public FileOutputStream fGPSSave = null;

    private static MyApplication mInstance = null;
    public boolean m_bKeyRight = true;

    //
    List<Object> mGPSRecordList = null;

    //�ж��Ƿ�����ʹ�ñ��豸��GPS
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

    //����gps��Ϣ
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

    //���gps��Ϣ����
    public synchronized void clearLocationsArray() {
        mGPSRecordList.clear();
    }

    public static MyApplication getInstance() {
        return mInstance;
    }

    //�õ��״���������̶�ֵ
    public double getRadarPowerAlarmScale() {
        return mRadarPowerAlaramScale;
    }

    public void setRadarPowerAlarmLevel(float level) {
        mRadarPowerAlaramScale = level;
    }

    //�õ�ƽ����Ա����̶�ֵ
    public double getPCPowerAlarmScale() {
        return mPCPowerAlarmScale;
    }

    public void setPCPowerAlaramLevel(float level) {
        mPCPowerAlarmScale = level;
    }

    //�ط�����ʱ��ÿ��λͼ��Ƭ��ʾ�ĵ���
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

    //�õ����ʱ�ظ�����
    public int getDianceNumber() {
        return mRadarDevice.getDianceNumber();
    }

    //�õ�����ֿ����µı����չ��
    public int getWheelExtendNumber() {
        return mRadarDevice.getWheelExtendNumber();
    }

    //�õ�ѡ��Ĳ��������
    public int getWheeltypeSel() {
        return mRadarDevice.getWheeltypeSel();
    }

    //�õ���������
    public int getAntenFrqSel() {
        return mRadarDevice.getAntenFrqSel();
    }

    //�õ�ɨ��ѡ��
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

    //�õ�sd���洢����
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

            sdCardInfo[0] = bSize * bCount;//�ܴ�С

            sdCardInfo[1] = bSize * availBlocks;//���ô�С
        }

        return sdCardInfo;
    }

    //��sd���еõ�lte�ļ����б�
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

    //��sd���е°��״�����ļ����б�
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
     * �����״��ļ�������
     *
     * @return
     */
    public String getRadardatasFolderpath() {
        return mRadarDatasFolderPath;
    }

    //�ж��Ƿ����״������ļ�
    public boolean isLTDFileName(String fileName) {
        String end = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length())
                             .toLowerCase();
        if (end.equals("lte"))
            return true;
        return false;
    }

    //�ж��Ƿ����״�����ļ�
    public boolean isParamFileName(String fileName) {
        String end = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length())
                             .toLowerCase();
        if (end.equals("par"))
            return true;
        return false;
    }

    //�ж��Ƿ���У׼�����ļ�
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

    //��ȡһ������
    public int radarReadMaxWaveDatas() {
        //��ȡָ�����ȵ�����
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
    private boolean isDraw = true;//�Ƿ�ͼ

    public boolean isDraw() {
        return isDraw;
    }

    public void setRealTimeDraw(boolean inputDraw) {
        isDraw = inputDraw;
    }

    /**
     * С������λ������
     */
    public double setDigits(double inputNum, int digitNum) {
        double tempNum = inputNum;
        BigDecimal b = new BigDecimal(tempNum);
        tempNum = b.setScale(digitNum, BigDecimal.ROUND_HALF_DOWN).doubleValue();//����ȡ��
        return tempNum;
    }

    private boolean misHardplusRun = false;//�Ƿ�����ȡ�̵߳��߳�

    public boolean getIsHardplusRun() {
        return misHardplusRun;
    }

    public void setIsHardplusRun(boolean inputBool) {
        misHardplusRun = inputBool;
        DebugUtil.i("threadTAG", "setIsHardplusRun=" + misHardplusRun);
    }

    private boolean misRunFirstHardplusThread = false;

    //�����߳�
    public boolean isRunFirstHardplusThread() {
        return misRunFirstHardplusThread;
    }

    //�ر��߳�
    public void setRunFirstHardplusThread(boolean inputHardplusThread) {
        misRunFirstHardplusThread = inputHardplusThread;
    }

    private boolean isDecendOrder = false;

    //�õ�����״̬
    public boolean getisDecendOrder() {
        return isDecendOrder;
    }

    //�����Ƿ���
    public void setIsDecendOrder(boolean inputState) {
        isDecendOrder = inputState;
    }
}
