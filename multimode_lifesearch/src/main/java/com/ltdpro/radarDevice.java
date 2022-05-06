package com.ltdpro;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import com.Connection.SendTransfer;
import com.ltd.multimodelifesearch.activity.MultiModeLifeSearchActivity;

//import com.ltd.idsc2600.activity.IDSC2600MainActivity.Detect;

/*
 * 雷达设备对象
 */
public class radarDevice {
    // //无线网络命令
    final private short RADAR_COMMAND_LENGTH = 2; // 雷达命令字字节长度
    final private short RADAR_COMMAND_SHOWWAVE = 0x0001; // 显示雷达波形
    final private short RADAR_COMMAND_SIGNALPOS = 0x0002; // 设置信号位置
    final private short RADAR_COMMAND_HARDPLUS = 0x0003; // 设置硬件增益
    final private short RADAR_COMMAND_JDCONST = 0x0007; // 设置介电常数
    final private short RADAR_COMMAND_ZEROOFF = 0x0008; // 设置零偏
    final private short RADAR_COMMAND_BEGSAVE = 0x000b; // 开始保存数据
    final private short RADAR_COMMAND_ENDSAVE = 0x000c; // 停止保存数据
    final private short RADAR_COMMAND_REPORTALLFILES = 0x000f; // 回传所有保存的文件名
    final private short RADAR_COMMAND_TRANSFILE = 0x0010; // 回传指定的文件
    final private short RADAR_COMMAND_TIMEWND = 0x0011; // 设置时窗
    final private short RADAR_COMMAND_SCANSPEED = 0x0012; // 设置扫速
    final private short RADAR_COMMAND_SAMPLEN = 0x0013; // 设置道长
    final private short RADAR_COMMAND_START = 0x0014; // 开启雷达
    final private short RADAR_COMMAND_STOP = 0x0015; // 停止雷达
    final private short RADAR_COMMAND_GETSTATUS = 0x0016; // 得到雷达的状态
    final private short RADAR_COMMAND_SETRADARFRQ = 0x0018; // 设置雷达频率
    final private short RADAR_COMMAND_LVBO = 0x0019; // 滤波
    final private short RADAR_COMMAND_CONTINUEMODE = 0x001a; // 连续模式
    final private short RADAR_COMMAND_WHELLMODE = 0x001b; // 轮测模式
    final private short RADAR_COMMAND_DIANCEMODE = 0x001c; // 点测模式
    final private short RADAR_COMMAND_GETDIANCEDATA = 0x001e; // 得到点测数据
    final private short RADAR_COMMAND_SETSCANAVE = 0x001f; // 设置平均次数
    final private short RADAR_COMMAND_REMBACKGROUND = 0x0020; // 背景消除

    // 控制命令代码
    final private short IOCTL_CODE_FLAG = (short) 0xABCD;
    final private short CODE_SET_STOPCONTINUE = (short) 0xAA01;
    final private short CODE_SET_STARTCONTINUE = (short) 0xAA00;
    final private short CODE_SET_WHEELBEG = (short) 0xAA02;
    final private short CODE_SET_WHEELEND = (short) 0xAA03;
    final private short CODE_SET_AUTOPLUS = (short) 0xAA05;
    final private short CODE_SET_SCANLEN = (short) 0xBB12; // 扫描点数
    final private short CODE_SET_FAD = (short) 0xBB13; // 发送FAD
    final private short CODE_SET_SCANAVE = (short) 0xBB14;
    final private short CODE_SET_REMBACK = (short) 0xBB15;
    final private short CODE_SET_RADARFRQ = (short) 0xBB16; // 主频
    final private short CODE_SET_WHEELEXTNUMBER = (short) 0xBB30; // 标记扩展
    final private short CODE_SET_HANDLEMODE = (short) 0xBB80;
    final private short CODE_SET_DIANCEEXTNUMBER = (short) 0xBB40;
    final private short CODE_SET_ONCEDIANCE = (short) 0xBB41; // 人工点测
    final private short CODE_SET_HARDPLUSRANGE = (short) 0xBB42;
    final private short CODE_SET_SMALLMARK = (short) 0xBB50; // 打小标
    final private short CODE_SET_BIGMARK = (short) 0xBB51; // 打大标
    final private short CODE_GET_WHELLSPEED = (short) 0xBB60; // 得到测距轮速度
    final private short CODE_SET_READSCANS = (short) 0xBB70; // 设置每次读取的道数
    final private short CODE_SET_HARDPLUS = (short) 0xCC10; // 九点硬件增益
    final private short CODE_SET_ZEROOFF = (short) 0xCC11; // 设置零偏
    final private short CODE_SET_LVBO = (short) 0xCC12; // 发送滤波
    final private short CODE_SET_STEP = (short) 0xCC13; // 设置步进
    final private short CODE_SET_SIGNALPOS = (short) 0xCC14; // 设置信号位置

    private short HANDLEKEY_SAVE = 2; // 手柄保存按键值
    private short HANDLEKEY_MARK = 18; // 手柄打标按键值
    // //
    boolean mIsTurnWhell = false; // 是否翻转大小标记
    public int mDianceDistance = 10; // 点测时两道数据间隔
    // //鞭状天线无线上传数据时，用到的变量
    short[] mOneScanBuf = new short[16384];
    short SCAN_HEAD_FLAG = 0x7fff; // 一道数据头标志
    int m_scanCopyPos = 0; // 组成一道数据时，已拷贝的位置

    short[] mResult = new short[8192];
    short[] mManageResult = new short[8192];
    static int g_antenFrqNumber = 15;

    // 天线主频
    static short[] g_antenFrq = {2000, 1500, 1000, 2000, 1500, 900, 400, 100, 400, 270, 150, 100,
                                 100, 50,};

    // 天线主频字符串
    public static String[] g_antenFrqStr = {"AL2000MHz", "AL1500MHz", "AL1000MHz", "GC2000MHz",
                                            "GC1500MHz", "GC900HF", "GC400HF", "GC100HF",
                                            "GC400MHz", "GC270MHz", "GC150MHz", "GC100MHz",
                                            "GC100S", "GC50MHz",};

    public static short[] g_hardplusrange = {20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
                                             20,};

    public String getSelAntenFrqStr(int sel) {
        return g_antenFrqStr[sel];
    }

    public String[] getAntenFrqStr() {
        return g_antenFrqStr;
    }

    // 各个天线对应的延时间隔
    static double[] g_antenFrqTime = {25, // AL2G
                                      33, // AL1.5G
                                      50, // AL1G
                                      10, // GC1.5G
                                      10, // GC900MHz
                                      125, // GC400MHz
                                      185, // GC270MHz
                                      250, // GC100MHz
                                      500, // GC50MHz
                                      1000, // GC25MHz
                                      250, // GC100FS
                                      500, // GC50FS
    };

    // 天线重复频率
    static int[] g_fixRepFrqNum = {800, // AL2000M
                                   800, // AL1500M
                                   800, // AL1000M
                                   800, // GC2000M
                                   800, // GC1500M
                                   400, // GC900HF
                                   128, // GC400HF
                                   150, // GC100HF
                                   128, // GC400M
                                   128, // GC270M
                                   150, // GC150M
                                   16, // GC100M
                                   32, // GC100S
                                   16, // GC50M
    };

    static int[] g_fixNumRF = {1110, // AL2G
                               1110, // AL1.5G
                               1110, // AL1G
                               1110, // GC1.5G
                               1560, // 900M
                               1560, // 3120, //400M
                               3120, // 270M
                               6240, // 100M
                               12480, // 50M
                               12480, // 25M
                               6240, // GC100FS
                               12480, // GC50FS
    };

    // 采样点数
    // static int g_scanLengthNumber=7;
    // static String[] g_scanLengthStr={
    // "256点/道",
    // "256点/道",
    // "512点/道",
    // "1024点/道",
    // "2048点/道",
    // "4096点/道",
    // "8192点/道"
    // };

    // 取样点数
    static int mTotalScanlenNumber = 6;
    static short[] g_scanLen = {256, 512, 1024, 2048, 4096, 8192};

    // 扫描速度
    static String[] g_scanSpeedStr = {"16道/秒", "32道/秒", "64道/秒", "128道/秒", "256道/秒", "512道/秒",
                                      "1024道/秒",
                                      /*
	 * "2048道/秒", "4096道/秒", "8192道/秒"
	 */};
    //
    static int g_scanSpeedNumber = 7;
    static short[] g_scanSpeed = {16, 32, 64, 128, 256, 512, 1024,
                                  /*
	 * 2048, 4096, 8192
	 */};

    // 参数
    static int g_lvboParamsNumber = 2;// 5;
    static String[] g_lvboStr = {"无滤波", "滤波",
                                 // "一级滤波",
                                 // "二级滤波",
                                 // "三级滤波",
                                 // "四级滤波"
    };

    static short[][] g_lvboParams = {{16800, 10000},
                                     // {60, 10000},
                                     // {129,10000},
                                     // {276,10000},
                                     {607, 10000},};

    // 测距轮相邻触发距离(cm)
    public static double[] mWheelInterDistance = new double[]{
            /* 0.97, */
            0.060, // WDMI-300
            0.50,// WDMI-500
            0.10,// WDMI-55A
            0.13,// WDMI-130
            0.0374,// GC1500MHz
            /*
			 * 0.019, 0.06,
			 */
            0.050,// 自定义
            /*
	 * 0.50, 0.50
	 */};

    // public int WHELLTYPE_2GHZ=5;
    // public int WHELLTYPE_15GHZ=4;

    public int GC100S_INDEX = 12; // GC100S天线索引
    // public int GC50FS_INDEX=11; //GC50FS天线索引
    // 每个天线频率对应的默认参数
    /*
     * 参数包括: 天线索引，取样点，扫速，时窗，信号位置，整体增益，零偏，滤波，默认测距仪型号（0-300,1-500,2-130,3-GC1500M）
     */
    static short[][] g_defParamsForRadar = {
            // //AL2000M天线对应参数
            {0, 1, 256, 10, 4, 0, 2034, 1, 0},
            // //AL1500M天线对应参数
            {1, 1, 256, 15, 4, 0, 2034, 1, 0},
            // //AL1000M天线对应参数
            {2, 1, 256, 20, 4, 0, 2034, 1, 0},
            // //GC2000M天线对应参数
            {3, 1, 256, 10, -10, 0, 2034, 1, 0},
            // //GC1500M天线对应参数
            {4, 1, 256, 15, -10, 0, 2034, 1, 0},
            // //GC900HF天线对应参数
            {5, 1, 256, 25, 5, 0, 2034, 1, 0},
            // //GC400HF天线对应参数
            {6, 1, 256, 50, 12, 0, 2034, 1, 1},
            // //GC100HF天线对应参数
            {7, 2, 64, 600, -5, 0, 2034, 1, 1},
            // //GC400M天线对应参数
            // { 8, 1, 256, 60, 12, 0, 2034, 1, 1 },
            {8, 5, 16, 80, 12, 0, 2034, 1, 1},
            // //GC270M天线对应参数
            {9, 1, 128, 80, 2, 0, 2034, 1, 1},
            // //GC150M天线对应参数
            {10, 2, 64, 120, -5, 0, 2034, 1, 1},
            // //GC100M天线对应参数
            {11, 2, 16, 600, 0, 0, 2034, 1, 1},
            // //GC100S天线对应参数
            {12, 2, 32, 600, 0, 0, 2034, 1, 0},
            // /GC50M天线对应参数
            {13, 2, 16, 650, 0, 0, 2034, 1, 1},};

    /**
     * 测距仪型号对应的标记扩展初始值
     */
    static short[] g_wheelextendnum = {17, // 0,300测距仪
                                       2, // 1,500测距仪
                                       2, // 55A测距仪
                                       8, // 8,130测距链
                                       25, // 1.5G默认标记扩展值
                                       1 // 自定义
    };

    // 每种天线对应的时窗值范围
    // 目前的顺序AL2000,AL1500,AL1000,GC2000M,GC1500M,GC900HF,GC400HF,GC100HF,
    // GC400M,GC270M,GC150M,GC100M,GC100s,GC50M
    static short[][] g_timeWndRange = {
            // //AL2G天线
            {5, 25},
            // //AL1500M天线
            {5, 30},
            // //AL1000M天线
            {8, 40},
            // /GC2000M天线
            {5, 25},
            // //GC1500M天线
            {5, 30},
            // //GC900HF天线
            {8, 40},
            // //GC400HF天线
            {20, 200},
            // //GC100HF天线
            {100, 3000},
            // //GC400M天线
            {20, 200},
            // /GC270M天线
            {20, 300},
            // //GC150M天线
            {50, 1000},
            // //GC100M天线
            {100, 3000},
            // /GC100S天线
            {100, 3000},
            // GC50M
            {100, 4000},};

    private int mMixHardplus = -10;
    private int mMaxHardplus = 130;

    public int getHardplusRange() {
        return mMaxHardplus - mMixHardplus;
    }

    public int getMixHardplus() {
        return mMixHardplus;
    }

    public int getMaxHardplus() {
        return mMaxHardplus;
    }

    /*
     * 根据天线频率检查用户设置的时窗值是否合适(智能模式下)
     */
    public int checkTimewnd(int timeWnd) {
        short mixVal = g_timeWndRange[mAntenFrqSel][0];
        short maxVal = g_timeWndRange[mAntenFrqSel][1];
        int retVal = timeWnd;
        if (retVal < mixVal)
            retVal = mixVal;
        if (retVal > maxVal)
            retVal = maxVal;
        return retVal;
    }

    /*
     * 根据时窗值检查取样道长
     */
    public int getScanlenForTimewnd(int timeWnd) {
        int nowScanlen = mScanLength;
        int needScanlen = nowScanlen;
        do {
            // AL2G
            if (mAntenFrqSel == 0) {
                if (timeWnd < 12) {
                    needScanlen = 512;
                    break;
                }
                if (timeWnd >= 12) {
                    needScanlen = 1024;
                    break;
                }
            }
            // AL1500
            if (mAntenFrqSel == 1) {
                if (timeWnd <= 15) {
                    needScanlen = 512;
                    break;
                }
                if (timeWnd >= 16) {
                    needScanlen = 1024;
                    break;
                }
            }
            // AL1000
            if (mAntenFrqSel == 2) {
                if (timeWnd <= 25) {
                    needScanlen = 512;
                    break;
                }
                if (timeWnd >= 26) {
                    needScanlen = 1024;
                    break;
                }
            }
            // GC1500
            if (mAntenFrqSel == 3) {
                if (timeWnd <= 15) {
                    needScanlen = 512;
                    break;
                }
                if (timeWnd >= 16) {
                    needScanlen = 1024;
                    break;
                }
            }
            // GC900
            if (mAntenFrqSel == 4) {
                if (timeWnd <= 25) {
                    needScanlen = 512;
                    break;
                }
                if (timeWnd >= 26) {
                    needScanlen = 1024;
                    break;
                }
            }
            // GC400
            if (mAntenFrqSel == 5) {
                if (timeWnd <= 60) {
                    needScanlen = 512;
                    break;
                }
                if (timeWnd >= 61 && timeWnd <= 120) {
                    needScanlen = 1024;
                    break;
                }
                if (timeWnd >= 121) {
                    needScanlen = 2048;
                    break;
                }
            }
            // GC270
            if (mAntenFrqSel == 6) {
                if (timeWnd <= 90) {
                    needScanlen = 512;
                    break;
                }
                if (timeWnd >= 91 && timeWnd <= 180) {
                    needScanlen = 1024;
                    break;
                }
                if (timeWnd >= 181) {
                    needScanlen = 2048;
                    break;
                }
            }
            // GC100
            if (mAntenFrqSel == 7) {
                if (timeWnd <= 125) {
                    needScanlen = 512;
                    break;
                }
                if (timeWnd <= 250) {
                    needScanlen = 1024;
                    break;
                }
                if (timeWnd <= 500) {
                    needScanlen = 2048;
                    break;
                }
                if (timeWnd <= 1000) {
                    needScanlen = 4096;
                    break;
                }
                if (timeWnd <= 2000) {
                    needScanlen = 8192;
                    break;
                }
            }
            // GC50 & GC25
            if (mAntenFrqSel == 8 || mAntenFrqSel == 9) {
                if (timeWnd <= 250) {
                    needScanlen = 512;
                    break;
                }
                if (timeWnd <= 500) {
                    needScanlen = 1024;
                    break;
                }
                if (timeWnd <= 1000) {
                    needScanlen = 2048;
                    break;
                }
                if (timeWnd <= 2000) {
                    needScanlen = 4096;
                    break;
                }
                if (timeWnd <= 5000) {
                    needScanlen = 8192;
                    break;
                }
            }
        } while (false);

        return needScanlen;
    }

    /*
     * 根据取样道长，得到对应的扫速
     */
    public int getScanspeedForScanlen(int scanLen) {
        int needSpeed;
        needSpeed = mScanSpeed;
        do {
            // AL2G AL1500 AL1000
            if (mAntenFrqSel == 0 || mAntenFrqSel == 1 || mAntenFrqSel == 2) {
                if (scanLen == 256) {
                    // 限制最高速
                    if (needSpeed >= 720) {
                        needSpeed = 720;
                    }
                    if (needSpeed > 256 || needSpeed < 128)
                        needSpeed = 256;
                    break;
                }
                if (scanLen == 512) {
                    if (needSpeed >= 360) {
                        needSpeed = 360;
                    }
                    if (needSpeed > 256 || needSpeed < 128)
                        needSpeed = 256;
                    break;
                }
                if (scanLen == 1024) {
                    if (needSpeed >= 180) {
                        needSpeed = 180;
                    }
                    if (needSpeed < 128)
                        needSpeed = 128;
                    break;
                }
            }
            // GC1500
            if (mAntenFrqSel == 3) {
                if (scanLen == 256) {
                    if (needSpeed > 1024)
                        needSpeed = 1024;
                }
                if (scanLen == 512) {
                    if (needSpeed > 1024)
                        needSpeed = 1024;
                }
                if (scanLen == 1024) {
                    if (needSpeed > 512)
                        needSpeed = 512;
                }
                if (needSpeed > 256 || needSpeed < 128)
                    needSpeed = 256;
                break;
            }
            // GC900
            if (mAntenFrqSel == 4) {
                if (scanLen == 512) {
                    if (needSpeed > 1024)
                        needSpeed = 1024;
                }
                if (scanLen == 1024) {
                    if (needSpeed > 512)
                        needSpeed = 512;
                }
                if (needSpeed > 256 || needSpeed < 128)
                    needSpeed = 256;
                break;
            }
            // GC400
            if (mAntenFrqSel == 5) {
                if (scanLen == 512) {
                    if (needSpeed > 256)
                        needSpeed = 256;
                    needSpeed = 256;
                    break;
                }
                if (scanLen == 1024) {
                    if (needSpeed > 128)
                        needSpeed = 128;
                    needSpeed = 128;
                    break;
                }
                if (scanLen == 2048) {
                    if (needSpeed > 64)
                        needSpeed = 64;
                    needSpeed = 64;
                    break;
                }
            }
            // GC270
            if (mAntenFrqSel == 6) {
                if (scanLen == 512) {
                    if (needSpeed > 128)
                        needSpeed = 128;
                    needSpeed = 128;
                    break;
                }
                if (scanLen == 1024) {
                    if (needSpeed > 64)
                        needSpeed = 64;
                    needSpeed = 64;
                    break;
                }
                if (scanLen == 2048) {
                    if (needSpeed > 32)
                        needSpeed = 32;
                    needSpeed = 32;
                    break;
                }
            }
            // GC100s
            if (mAntenFrqSel == 7) {
                if (scanLen == 512) {
                    if (needSpeed > 64)
                        needSpeed = 64;
                    needSpeed = 64;
                    break;
                }
                if (scanLen == 1024) {
                    if (needSpeed > 32)
                        needSpeed = 32;
                    needSpeed = 32;
                    break;
                }
                if (scanLen == 2048) {
                    if (needSpeed > 16)
                        needSpeed = 16;
                    needSpeed = 16;
                    break;
                }
                if (scanLen == 4096) {
                    if (needSpeed > 8)
                        needSpeed = 8;
                    needSpeed = 8;
                    break;
                }
                if (scanLen == 8192) {
                    if (needSpeed > 4)
                        needSpeed = 4;
                    needSpeed = 4;
                    break;
                }
            }
            // GC50 & GC25
            if (mAntenFrqSel == 8 || mAntenFrqSel == 9) {
                if (scanLen == 512) {
                    if (needSpeed > 32)
                        needSpeed = 32;
                    needSpeed = 32;
                    break;
                }
                if (scanLen == 1024) {
                    if (needSpeed > 16)
                        needSpeed = 16;
                    needSpeed = 16;
                    break;
                }
                if (scanLen == 2048) {
                    if (needSpeed > 8)
                        needSpeed = 8;
                    needSpeed = 8;
                    break;
                }
                if (scanLen == 4096) {
                    if (needSpeed > 4)
                        needSpeed = 4;
                    needSpeed = 4;
                    break;
                }
                if (scanLen == 8192) {
                    if (needSpeed > 2)
                        needSpeed = 2;
                    needSpeed = 2;
                    break;
                }
            }
        } while (false);
        return needSpeed;
    }

    final private int MAXDEFUALTWHEELNUM = 6; // 默认测距仪个数
    private double[] mWhellcheckCoeff = new double[MAXDEFUALTWHEELNUM];

    final private int MAXSPEEDSCANLENTH = 256 * 1024;
    private int mRepFrq = 128; // 设置重复频率
    private int mAntenFrqSel = 5; // 选择天线类型
    private int mScanSpeedSel = 4; // 选择的扫速
    private int mScanSpeed = 256; // 扫描速度
    private int mScanLengthSel = 1; // 选择的道长
    private int mScanLength = 512; // 采集的道长
    private int mTimeWindow = 60; // 时窗
    private int mSignalPos = 0; // 信号位置
    private float[] mHardPlus = {0, 0, 0, 0, 0, 0, 0, 0, 0};
    private float[] mBackHardPlus = {0, 0, 0, 0, 0, 0, 0, 0, 0};
    public float[] mRealHardPlus = {0, 0, 0, 0, 0, 0, 0, 0, 0}; // 根据DA硬件，得到的实际放大dB
    public float[] mSoftPlus = {0, 0, 0, 0, 0, 0, 0, 0, 0};
    public double[] mTotalNeedZoom = new double[8192];
    public double[] mTotalNeedZoom1 = new double[8192];
    public double[] mHardZoom = new double[8192];
    public double[] mSoftZoom = new double[8192];
    private int mZeroOff = 2034; // 零偏
    private float mJiedianConst = 9; // 介电常数
    private int mScanAve = 4; // 平均次数
    private int mRemoveBackSel = 0; // 背景消除
    private short[] mRemoveBackParams = {0, 1};
    private int mFilterSel = 1;
    private int mWheelExtendNumber = 0; // 测距轮扩展值
    private int mWheeltypeSel = 0; // 测距轮类型选择值
    private int MWHEELMAXINDEX = 6; // 轮测的最大选择值
    private int mDianceNumber = 10; // 点测时的重复次数
    private long mHadRcvScans = 0; // 已经采集的道数
    private double mWavespeed = 30.; // 波速(cm/s)
    private boolean mChangeDataListAdapter = false;// 标记是否更改参数

    // 存放雷达数据的缓冲区
    private byte[][] mDatasBufs;
    private int mBufIndex;
    private int mBufsNumber = 4;
    private int mBufLength = 2048 * 128 * 2;
    private int[] mNowWPos = new int[mBufsNumber];
    private int mRBufIndex = 0;
    private int[] mNowRPos = new int[mBufsNumber];
    // 保存文件需要的参数
    private int mNowFileindex = 0; // 文件索引
    private boolean mExistSaveFile = false; // 是否存在保存文件
    private FileOutputStream fSaveOS; //
    private FileHeader mFileHeader = new FileHeader();
    public String mSavingFilePath;

    private short[] mOneScanDatas = new short[8192]; // 存放单道波形
    int mHasRcvDianceNumber = 0;
    private float[] mOneDianCeDatas = new float[8192]; // 存放点测数据
    //
    private Context mContext;

    private String TAG = "radarDevice";

    // 定义生成数据文件的类型
    private int CREATE_NEWFILE_TYPEINDEX = 1;
    private int CREATE_NEWFILE_TYPETIME = 2;
    private int mCreateNewFileType = CREATE_NEWFILE_TYPEINDEX;

    // 存储路径,默认内存
    public static String mStoragePath = "/mnt/udisk2";
    final public String INNERSTORAGE = "/mnt/sdcard";
    final public String SDCARDSTORAGE = "/mnt/udisk2";
    final public String USBSTORAGE = "/mnt/udisk";
    final public String DEFAULTCHECKFILE = "defCheck.check";
    final public String PREFIXDEFAULTCHECKFILE = "defCheck";
    final public int INNER_INDEX = 2;
    final public int SDCARD_INDEX = 0;
    final public int USB_INDEX = 1;

    /**
     * u盘位置尚未确定
     */
    private String mUSBPath = "";
    // 参数文件夹路径
    public String mParamsFilefolderPath = "/radarParams/";
    public String mWhellcheckFileExtname = ".check";
    // 雷达数据文件夹路径
    public String mLTEFilefolderPath = "/LteFiles/";
    public String mWhellcheckFilename = "whellcheck";
    // 使用wifi发送数据
    private boolean mWifiSendDatas = false;

    // 加载默认轮测参数
    public boolean loadDefaultWhellcheckParams() {
        String fileName = getParamsPath() + DEFAULTCHECKFILE;
        if (loadWhellcheckParams(fileName))
            return true;
        else {
            return false;
        }
    }

    // 加载校准参数
    public boolean loadWhellcheckParams(String pathName) {
        DebugUtil.i("ExtendNumb", "LoadWhellcheckFileName:=" + pathName);
        byte[] buf = new byte[1024];

        try {
            // 判断文件是否存在，不存在则保存默认文件
            if (!readCheckFile(pathName, buf))
                return false;
            else
                ;

            // 读取测距轮校正值
            int index = 0;
            int temVal;
            for (int i = 0; i < MAXDEFUALTWHEELNUM; i++) {
                temVal = (0x000000ff & buf[index + i * 8]) |
                         (0x0000ff00 & (buf[index + i * 8 + 1] << 8)) |
                         (0x00ff0000 & (buf[index + i * 8 + 2] << 16)) |
                         (0xff000000 & (buf[index + i * 8 + 3] << 24));
                mWhellcheckCoeff[i] = (float) (temVal / 1.);
                mWhellcheckCoeff[i] = mWhellcheckCoeff[i] / 1000;
                if (mWhellcheckCoeff[i] == 0.)
                    mWhellcheckCoeff[i] = 1.;
                else
                    ;
                DebugUtil.i(TAG, "Now " + i + " whellcheckcoeff:=" + mWhellcheckCoeff[i]);
            }

            // 从第100位起获取标记扩展
            // 根据mWheeltypeSel值获得标记扩展值
            int id = 100 + mWheeltypeSel * 4;
            DebugUtil.i(TAG, "mWheeltypeSel=" + mWheeltypeSel);

            temVal = bitCompute(buf, id);
            DebugUtil.i(TAG, "id=" + id + "buf=" + buf[id] + "," + buf[id + 1] + "," + buf[id + 2] +
                             "," + buf[id + 3]);

            if (temVal == 0)
                mWheelExtendNumber = g_wheelextendnum[mWheeltypeSel];
            else {
                mWheelExtendNumber = temVal;
            }

            DebugUtil.i(TAG, "读取文件获取的值temVal=" + temVal + "mWheeltypeSel=" + mWheeltypeSel);
            DebugUtil.i("ExtendNumb", "mWheelExtendNumber=" + mWheelExtendNumber);
        } catch (Exception e) {
            DebugUtil.i(TAG, "Load params file:" + pathName + " fail!");
            return false;
        }
        return true;
    }

    // 装载自定义的校准参数文件
    private int mDiameter = 773;// 直径值
    private int mPulseIndex = 0;// 脉冲下标值
    private int mPulse = 0;// 脉冲值

    // 得到脉冲值
    public int getmPulse() {
        return mPulse;
    }

    // 根据选择的下标设置脉冲值
    public void setmPulse(int inputPulse) {
        mPulse = inputPulse;
    }

    public int getAntenFrq() {
        return g_antenFrq[mAntenFrqSel];
    }

    // 加载自定义的校准文件
    public boolean loadCustomWheelCheckParamsFile(String pathName) {
        // 加载校准、标记扩展、直径和脉冲值
        DebugUtil.i("ExtendNumb", "LoadWhellcheckFileName:=" + pathName);
        byte[] buf = new byte[1024];

        try {
            // 判断文件是否存在，不存在则保存默认文件
            if (!readCheckFile(pathName, buf))
                return false;
            else
                ;

            // 读取校准值、标记扩展、直径、脉冲值
            // 0-3 校准值、4-7标记扩展、8-11直径、12-16脉冲数
            // 读取校准值
            int index = 0, checkIndex = this.MAXDEFUALTWHEELNUM - 1;
            int temVal = 0;
            temVal = bitCompute(buf, index);
            mWhellcheckCoeff[checkIndex] = (float) (temVal / 1.);
            mWhellcheckCoeff[checkIndex] = mWhellcheckCoeff[checkIndex] / 1000;
            if (mWhellcheckCoeff[checkIndex] == 0.)
                mWhellcheckCoeff[checkIndex] = 1.;
            else
                ;
            DebugUtil.i(TAG, "Now " + index + " whellcheckcoeff:=" + mWhellcheckCoeff[checkIndex]);

            // 读取标记扩展
            index += 4;
            temVal = bitCompute(buf, index);
            mWheelExtendNumber = temVal;
            DebugUtil.i(TAG, "mWheelExtendNumber=" + mWheelExtendNumber);
            // DebugUtil.i(TAG,
            // "id="+index+"buf="+buf[index]+","+buf[index+1]+","+buf[index+2]+","+buf[index+3]);

            // 读取直径
            index += 4;
            int num = 0;
            num = bitCompute(buf, index);
            mDiameter = num;
            DebugUtil.i(TAG, "直径num=" + mDiameter);

            // 读取脉冲下标值
            index += 4;
            int pulseIndex = 0;
            pulseIndex = bitCompute(buf, index);
            mPulseIndex = pulseIndex;
            this.mPulse = this.getmPulseIndex();
            DebugUtil.i(TAG, "脉冲pulse=" + mPulseIndex);

        } catch (Exception e) {
            DebugUtil.i(TAG, "Load params file:" + pathName + " fail!");
            return false;
        }
        return true;
    }

    // int的位操作，index是起始位
    private int bitCompute(byte[] buf, int index) {
        return (0x000000ff & buf[index]) | (0x0000ff00 & (buf[index + 1] << 8)) |
               (0x00ff0000 & (buf[index + 2] << 16)) | (0xff000000 & (buf[index + 3] << 24));
    }

    public int getmPulseIndex() {
        return mPulseIndex;
    }

    public void setmPulseIndex(int mPulseIndex) {
        this.mPulseIndex = mPulseIndex;
    }

    // 得到直径的值
    public int getmDiameter() {
        return mDiameter;
    }

    // 设置直径值，设置范围(0-1000]
    public void setmDiameter(int mDiameter) {
        if (mDiameter < 0) {
            this.mDiameter = 1;
        } else if (mDiameter > 1000) {
            this.mDiameter = 1000;
        }

        this.mDiameter = mDiameter;
    }

    // 读取特定路径check文件
    private boolean readCheckFile(String pathName, byte[] buf)
            throws FileNotFoundException, IOException {
        File file = new File(pathName);
        if (file.exists())
            ;
        else {
            if (saveDefaultCheckParamsFile())
                ;
            else
                return false;
        }
        FileInputStream fileOS = new FileInputStream(pathName);
        fileOS.read(buf, 0, 1024);
        fileOS.close();
        return true;
    }

    /*
     * 保存新建的自定义校准文件
     */
    public void createWhellCheckFile(String filename) {
        // 保存测距轮校正文件
        DebugUtil.i(TAG, "createWheelCheckFile=" + filename);

        String Name = getInnerStoragePath() + mParamsFilefolderPath;
        Name += filename;
        // Name += mApp.mRadarDevice.mWhellcheckFileExtname;

        File f = new File(Name);
        // 判断是否存在
        if (f.exists()) {

        } else
            ;

        try {
            FileOutputStream fileParams = new FileOutputStream(Name);
            // 保存参数内容
            byte[] buf = new byte[1024];
            // 保存0-3校准值、4-7标记扩展、8-11直径、12-16脉冲
            // 校准值
            int index = 0;
            int wheelIndex = this.MAXDEFUALTWHEELNUM - 1;
            int tempValue = (int) (mWhellcheckCoeff[wheelIndex] * 1000);
            valueToBit(buf, index, tempValue);

            // 保存标记扩展值,当前值一般默认为1
            index += 4;
            tempValue = this.mWheelExtendNumber;
            valueToBit(buf, index, tempValue);

            // 保存直径
            index += 4;
            tempValue = this.mDiameter;
            valueToBit(buf, index, tempValue);

            // 保存脉冲下标值
            index += 4;
            tempValue = this.mPulseIndex;
            valueToBit(buf, index, tempValue);

            try {
                fileParams.write(buf, 0, 1024);
            } catch (Exception e) {
                DebugUtil.i(TAG, "save whellcheckparams fail!");
            }

            fileParams.close();
        } catch (Exception e) {

        }
    }

    // 保存自定义
    public boolean saveCustomWheelExtend(String filePath) {
        try {
            RandomAccessFile fileParams = new RandomAccessFile(filePath, "rw");
            int index = 4;

            fileParams.seek(4);
            byte[] buf = new byte[4];
            int tempValue = 0;

            tempValue = this.mWheelExtendNumber;
            valueToBit(buf, 0, tempValue);

            try {
                fileParams.write(buf, 0, 4);
                byte[] buffer = new byte[1024];
                fileParams.read(buffer, 0, 1024);

                int ret = bitCompute(buffer, 4);
                DebugUtil.i(TAG, "ret=" + ret);
            } catch (Exception e) {
                DebugUtil.i(TAG, "save whellcheckparams fail!");
            }

            fileParams.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }

    private final byte[] mSystemSettingsData = new byte[8];

    private String checkParamFile() {
        String paramDirPath = this.INNERSTORAGE + mParamsFilefolderPath;
        File paramDir = new File(paramDirPath);
        if (!paramDir.exists()) {
            if (!paramDir.mkdirs()) {
                return null;
            }
        }
        return paramDirPath + "systemset.par";
    }

    /**
     * @param buf最终赋值的变量
     * @param index下标
     * @param tempValue需要转换的值
     */
    private void valueToBit(byte[] buf, int index, int tempValue) {
        buf[index] = (byte) tempValue;
        buf[index + 1] = (byte) (tempValue >> 8);
        buf[index + 2] = (byte) (tempValue >> 16);
        buf[index + 3] = (byte) (tempValue >> 24);
    }

    // 保存轮测参数
    public boolean saveWhellcheckParams(String pathName) {
        try {
            DebugUtil.i("ExtendNumb", "saveWhellcheckParams pathName=" + pathName);
            // FileOutputStream fileParams = new FileOutputStream(pathName);
            RandomAccessFile file;
            try {
                file = new RandomAccessFile(pathName, "rw");
            } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return false;
            }

            // 保存参数内容
            byte[] buf = new byte[8];
            // 测距轮校正值
            int index = 0;
            int temVal;
            int temp = 0;
            int i = this.mWheeltypeSel;
            // for(int i = 0 ; i < MAXDEFUALTWHEELNUM ; i++)
            {
                temVal = 0;
                temVal = (int) (mWhellcheckCoeff[i] * 1000);
                buf[0] = (byte) temVal;
                buf[1] = (byte) (temVal >> 8);
                buf[2] = (byte) (temVal >> 16);
                buf[3] = (byte) (temVal >> 24);

                // 100开始存标记扩展，如果不是选中的测距轮型号存默认值,只保存当前选中的
                if (mWheelExtendNumber != 0) {
                    temVal = mWheelExtendNumber;
                    DebugUtil.i("ExtendNumb",
                                "****mWheeltypeSel=" + this.mWheeltypeSel + ";mWheelExtendNumber=" +
                                this.mWheelExtendNumber);
                } else {
                    temVal = radarDevice.g_wheelextendnum[i];
                }
                DebugUtil.i("ExtendNumb",
                            "saveWhellcheckParams mWheeltypeSel=" + mWheeltypeSel + "temVal=" +
                            temVal);

                buf[4] = (byte) temVal;
                buf[5] = (byte) (temVal >> 8);
                buf[6] = (byte) (temVal >> 16);
                buf[7] = (byte) (temVal >> 24);

                for (int j = 0; j < 4; j++)
                    DebugUtil.i("ExtendNumb", "id=" + temp + "buf" + buf[j]);
            }
            try {
                // 先保存校准值
                temp = mWheeltypeSel * 8;
                file.seek(temp);
                file.write(buf, 0, 4);
                // 再保存该类型的标记扩展值
                temp = 100 + mWheeltypeSel * 4;
                file.seek(temp);
                // fileParams.write(buf, 0, 1024);
                file.write(buf, 4, 4);
            } catch (Exception e) {
                DebugUtil.i(TAG, "save whellcheckparams fail!");
            }

            file.close();

            // 测试读取
            /*
             * FileInputStream fileOS = new FileInputStream(pathName); byte[]
             * buff = new byte[1024]; fileOS.read(buff,0,1024); fileOS.close();
             * int id = 100 + mWheeltypeSel*4; DebugUtil.i("ExtendNumb",
             * "saveWhellcheckParams mWheeltypeSel="+mWheeltypeSel);
             *
             * temVal = bitCompute(buff, id);
             *
             * DebugUtil.i("ExtendNumb", "save temVal="+temVal);
             * this.mWheelExtendNumber = temVal; DebugUtil.i("ExtendNumb",
             * "radarDevice mWheelExtendNumber="+mWheelExtendNumber);
             */
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    // 保存默认参数文件
    public boolean saveDefaultParamsFile() {
        String pathName;
        pathName = this.INNERSTORAGE + mParamsFilefolderPath + "defparams.par";
        return saveParamsFile(pathName);
    }

    // 保存默认系统文件
    public boolean saveSystemSetFile() {
        System.out.println("保存系统设置参数");
        String path = checkParamFile();
        if (path == null)
            return false;
        FileOutputStream fileParams = null;
        try {
            fileParams = new FileOutputStream(path);
            DebugUtil.i(TAG, "storageId=" + mSelectStorageIndex);
            mSystemSettingsData[0] = (byte) mSelectStorageIndex;
            mSystemSettingsData[1] = (byte) (mSelectStorageIndex >> 8);
            mSystemSettingsData[2] = (byte) (mSelectStorageIndex >> 16);
            mSystemSettingsData[3] = (byte) (mSelectStorageIndex >> 24);
            fileParams.write(mSystemSettingsData, 0, mSystemSettingsData.length);
            DebugUtil.i(TAG, "保存的位置id=" + mSelectStorageIndex);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileParams != null) {
                try {
                    fileParams.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private void readSystemSettings(FileInputStream fis, int offset, int length)
            throws IOException {
        int totalRead = 0;
        int readLen;
        while (totalRead < length) {
            readLen = fis.read(mSystemSettingsData, offset, length - totalRead);
            if (readLen == -1)
                throw new IOException(
                        "acquire " + length + " bytes but only read " + totalRead + " bytes");
            totalRead += readLen;
            offset += readLen;
        }
    }

    /**
     * 加载系统设置文件，只有一个保存位置记录
     *
     * @return
     */
    public boolean loadSystemSetFile() {
        String path = checkParamFile();
        if (path == null)
            return false;
        FileInputStream fileIs = null;
        try {
            fileIs = new FileInputStream(path);
            System.out.println("path: " + path);
            // 保存参数内容
            readSystemSettings(fileIs, 0, mSystemSettingsData.length);
            // 获得存储位置
            int storageId = (0x000000ff & mSystemSettingsData[0]) |
                            (0x0000ff00 & mSystemSettingsData[1] << 8) |
                            (0x00ff0000 & mSystemSettingsData[2] << 16) |
                            (0xff000000 & mSystemSettingsData[3] << 24);
            DebugUtil.i(TAG, "读取文件获得保存的位置id=" + storageId);
            // ((IDSC2600MainActivity)mContext).showToastMsg("读取文件获得保存的位置id="+storageId);
            this.setStoragePath(storageId);// 设置存储位置
            int intBits = 0;
            for (int i = 0; i < 4; i++)
                intBits |= ((mSystemSettingsData[4 + i] & 0xff) << (8 * i));
            float delay = Float.intBitsToFloat(intBits);
            if (delay >= 3f && delay <= 6f)
                DELAY_TIME = delay;
            System.out.println("delay_time: " + DELAY_TIME);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileIs != null) {
                try {
                    fileIs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    // 保存默认的校正文件
    public boolean saveDefaultCheckParamsFile() {
        String checkFileName;
        checkFileName = this.INNERSTORAGE + mParamsFilefolderPath + DEFAULTCHECKFILE;
        return saveWhellcheckParams(checkFileName);
    }

    // 判断是否是参数文件
    public boolean isParamsFilename(String fileName) {
        String end = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length())
                             .toLowerCase();
        if (end.equals("par"))
            return true;
        return false;
    }

    // 得到存储位置的ID
    public int getPlaybackSelectId() {
        int playbackSelectid = 0;
        String fileString = getStoragePath();
        if (INNERSTORAGE == fileString) {
            playbackSelectid = 2;
        } else if (SDCARDSTORAGE == fileString) {
            playbackSelectid = 0;
        } else if (USBSTORAGE == fileString) {
            playbackSelectid = 1;
        }

        return playbackSelectid;
    }

    /**
     * 保存雷达参数
     *
     * @return
     */
    public boolean saveParamsFile(String pathName) {
        DebugUtil.i(TAG, "saveParamsFile=" + pathName);

        if (!isParamsFilename(pathName)) {
            pathName += ".par";
        } else
            ;

        try {
            FileOutputStream fileParams = new FileOutputStream(pathName);
            // 保存参数内容
            byte[] buf = new byte[1024];
            buf[0] = (byte) mAntenFrqSel; // 主频
            // buf[1] = (byte)mScanSpeedSel; //扫速
            buf[1] = (byte) mScanSpeed;
            buf[2] = (byte) (mScanSpeed >> 8);
            buf[3] = (byte) mScanLengthSel; // 道长
            buf[4] = (byte) mFilterSel; // 滤波
            // 时窗
            buf[5] = (byte) mTimeWindow;
            buf[6] = (byte) (mTimeWindow >> 8);
            // 信号位
            buf[7] = (byte) (mSignalPos);
            buf[8] = (byte) (mSignalPos >> 8);
            // 保存介电常数
            long temp = Float.floatToIntBits(mJiedianConst);
            buf[9] = (byte) (temp);
            buf[10] = (byte) ((int) temp >> 8);
            buf[11] = (byte) ((int) temp >> 16);
            buf[12] = (byte) ((int) temp >> 24);
            // 零偏
            buf[13] = (byte) (mZeroOff);
            buf[14] = (byte) (mZeroOff >> 8);
            buf[15] = (byte) (mZeroOff >> 16);
            buf[16] = (byte) (mZeroOff >> 24);
            // 道间平均
            buf[17] = (byte) mScanAve;
            buf[18] = (byte) (mScanAve >> 8);
            buf[19] = (byte) (mScanAve >> 16);
            buf[20] = (byte) (mScanAve >> 24);
            // 背景消除
            buf[21] = (byte) mRemoveBackSel;
            // 增益
            int i;
            int index = 22;
            int hardVal;
            for (i = 0; i < 9; i++) {
                hardVal = (int) mHardPlus[i];
                buf[index + i * 4] = (byte) hardVal;
                buf[index + i * 4 + 1] = (byte) (hardVal >> 8);
                buf[index + i * 4 + 2] = (byte) (hardVal >> 16);
                buf[index + i * 4 + 3] = (byte) (hardVal >> 24);
            }

            // 点测时相邻两道间隔
            buf[1022] = (byte) mDianceDistance;
            buf[1023] = (byte) (mDianceDistance >> 8);
            index = 22 + 4 * 9;
            // 保存选择的测距仪类型
            buf[59] = (byte) mWheeltypeSel;
            buf[60] = (byte) (mWheeltypeSel >> 8);
            buf[61] = (byte) (mWheeltypeSel >> 16);
            buf[62] = (byte) (mWheeltypeSel >> 24);
            DebugUtil.i(TAG, "saveParamsFile save mWheeltypeSel!=" + mWheeltypeSel);

            /*
             * //测距轮校正值 index = 22+4*9; long temVal; for(i=0;i<10;i++) { temVal
             * = (long)mWhellcheckCoeff[i]; buf[index+i*8] = (byte)temVal;
             * buf[index+i*8+1] = (byte)(temVal>>8); buf[index+i*8+2] =
             * (byte)(temVal>>16); buf[index+i*8+3] = (byte)(temVal>>24);
             * buf[index+i*8+4] = (byte)(temVal>>32); buf[index+i*8+5] =
             * (byte)(temVal>>40); buf[index+i*8+6] = (byte)(temVal>>48);
             * buf[index+i*8+7] = (byte)(temVal>>56); }
             */
            try {
                fileParams.write(buf, 0, 1024);
            } catch (Exception e) {
                DebugUtil.i(TAG, "save paramsfile fail!");
            }
            // fileParams.flush(); 20170519未实验
            fileParams.close();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * 从文件中载入,开机选择天线主频后使用的加载函数，根据主频进行选择
     *
     * @param pathName
     * @return
     */
    public boolean onlyLoadParamsFromeFile(String pathName) {
        byte[] buf = new byte[1024];
        try {
            DebugUtil.i(TAG, "0.onlyLoadParamsFromeFile=" + pathName);

            File fOS = new File(pathName);
            if (fOS.length() < 0)
                return false;
            else
                ;

            FileInputStream fileOS = new FileInputStream(pathName);
            fileOS.read(buf, 0, 1024);
            fileOS.close();
            //
            // for(int i = 0;i<10;i++)
            // DebugUtil.i(TAG, "load buf["+i+"]="+buf[i]);

            // 天线主频
            // mAntenFrqSel = buf[0];
            // hss修改为不从文件中读取，避免反复写入失败，
            // 增加参数文件读取情况判断
            int inputAntenFrqSel = buf[0];
            // 重复频率
            mRepFrq = g_fixRepFrqNum[mAntenFrqSel];
            // 扫速
            mScanSpeed = (0x00ff & buf[1]) | (buf[2] << 8);

            // 增加文件读取值的判断
            // 判断天线主频读取与文件中读取的值是否一样，0时可能文件中无值，还需要进一步判断
            if (inputAntenFrqSel == mAntenFrqSel) {
                if (inputAntenFrqSel == 0)// 在读取到0时还需要再判断一位值
                {
                    if (mScanSpeed == 0)
                        return false; // 如果扫速为零，则判断是文件写入失败
                    else
                        ; // 如果扫速有值，则表示文件中有值
                } else
                    ;
            } else
                return false;

            // 道长
            mScanLengthSel = buf[3];
            mScanLength = g_scanLen[mScanLengthSel];
            // 滤波
            mFilterSel = buf[4];
            // 时窗
            mTimeWindow = (0x000000ff & buf[5]) | (0x0000ff00 & (buf[6] << 8));
            // 信号位置
            mSignalPos = (0x00ff & buf[7]) | (buf[8] << 8);

            // 介电常数
            // mJiedianConst = (double)(buf[9] | buf[10]<<8 | buf[11]<<16 |
            // buf[12]<<24);
            // mJiedianConst = (0x000000ff & buf[9]) |
            // (0x0000ff00 & (buf[10]<<8)) |
            // (0x00ff0000 & (buf[11]<<16))|
            // (0xff000000 & (buf[12]<<24));
            // DebugUtil.i(TAG,"Now mJiedianConst:="+mJiedianConst);

            int temp = 0;
            temp = 0x000000ff & buf[9];
            temp |= ((long) buf[10] << 8);
            temp &= 0xffff;
            temp |= ((long) buf[11] << 16);
            temp &= 0xffffff;
            temp |= ((long) buf[12] << 24);
            mJiedianConst = Float.intBitsToFloat(temp);
            DebugUtil.i(TAG, "mJiedianConst=" + String.valueOf(mJiedianConst));

            if (mJiedianConst == 0)
                mJiedianConst = 1;
            else
                ;

            // 零偏
            mZeroOff = (0x000000ff & buf[13]) | (0x0000ff00 & buf[14] << 8) |
                       (0x00ff0000 & buf[15] << 16) | (0xff000000 & buf[16] << 24);
            // 道间平均
            mScanAve = (0x000000ff & buf[17]) | (0x0000ff00 & buf[18] << 8) |
                       (0x00ff0000 & buf[19] << 16) | (0xff000000 & buf[20] << 24);
            // 背景消除
            mRemoveBackSel = buf[21];
            // 增益
            int i;
            int index = 22;
            int hardVal;
            for (i = 0; i < 9; i++) {
                mHardPlus[i] = buf[index + i * 4] | buf[index + i * 4 + 1] << 8 |
                               buf[index + i * 4 + 2] << 16 | buf[index + i * 4 + 3] << 24;
                // DebugUtil.i(TAG,"Now "+i+" hardplusval="+mHardPlus[i]);
            }

            // 获取轮测类型
            // this.mWheeltypeSel = (0x000000ff & buf[59]) |
            // (0x0000ff00 & buf[60]<<8) |
            // (0x00ff0000 & buf[61]<<16) |
            // (0xff000000 & buf[62]<<24);
            // mWheeltypeSel默认值并不保存
            this.mWheeltypeSel = g_defParamsForRadar[mAntenFrqSel][8];

            // 由轮测类型得到标记扩展值，0920不该在此处得到，应由文件读取获得。
            // mWheelExtendNumber = g_wheelextendnum[mWheeltypeSel];
            // DebugUtil.i(TAG,
            // "1.load mWheeltypSel="+mWheeltypeSel+",extendNum="+mWheelExtendNumber);

            /*
             * //读取测距轮校正值 index = 22+4*9; long temVal; for(i=0;i<10;i++) {
             * mWhellcheckCoeff[i] = (double)(buf[index+i*8] |
             * buf[index+i*8+1]<<8 | buf[index+i*8+2]<<16 | buf[index+i*8+3]<<24
             * | buf[index+i*8+4]<<32 | buf[index+i*8+5]<<40 |
             * buf[index+i*8+6]<<48 | buf[index+i*8+7]<<56 );
             * DebugUtil.i(TAG,"Now "
             * +i+" whellcheckcoeff:="+mWhellcheckCoeff[i]); }
             */
            //
            // mScanAve = 4;
        } catch (Exception e) {
            DebugUtil.e(TAG, "Load params file fail!");
            return false;
        }
        return true;
    }

    /**
     * 加载调入参数，在雷达参数列表项中处理
     *
     * @param pathName 文件路径
     * @return 加载是否成功
     */
    public boolean loadParamsFile(String pathName) {
        DebugUtil.i(TAG, "loadParamsFile=" + pathName);

        byte[] buf = new byte[1024];

        try {
            // 从文件中读取参数
            onlyLoadParamsFromeFile(pathName);
            // ///开始设置参数
            this.CalStepParams();
            this.CalHardplusParams();
            // 停止雷达
            stopRadar();
            // 设置天线频率
            short[] commands = new short[3];
            commands[0] = IOCTL_CODE_FLAG;
            commands[1] = CODE_SET_RADARFRQ;
            commands[2] = g_antenFrq[mAntenFrqSel];
            sendCommands_1(commands, (short) 6);
            DebugUtil.i(TAG, "loadParamsFile 主频=" + g_antenFrq[mAntenFrqSel]);

            // 设置取样道长
            setScanLenParams();
            // 设置信号位置参数
            setSignalParams();
            // 设置步进参数
            setStepParams();
            // 设置fad参数
            setFADParams();
            // 设置增益
            CalHardplusParams();
            setHardplusParams();
            // 设置滤波参数
            setFilterParams();
            // 开启雷达
            startRadar();
        } catch (Exception e) {
            DebugUtil.i(TAG, "Load fileheader file fail!");
        }
        return true;
    }

    // 根据当前的设备状态，更新文件头内容
    public synchronized void refreshFileHeader() {
        DebugUtil.i(TAG, "refreshFileHeader");
        mFileHeader.rh_data = 0;
        mFileHeader.rh_nsamp = (short) mScanLength;
        mFileHeader.rh_zero = 0;
        mFileHeader.rh_sps = mScanSpeed;

        mFileHeader.rh_position = mSignalPos;
        mFileHeader.rh_range = mTimeWindow;
        mFileHeader.rh_spp = g_antenFrq[mAntenFrqSel];
        DebugUtil.i(TAG, "天线主频=" + mFileHeader.rh_spp + "mAntenFrqSel=" + g_antenFrq[mAntenFrqSel]);

        // 保存系统时间，修改时间与之同
        /*
         * class HEAD_DATE { byte []sec2 = new byte[5]; //5秒/2 (0-29) byte []min
         * = new byte[6]; //6分 (0-59) byte []hour = new byte[5]; //5时 (0-23)
         * byte []day = new byte[5]; //5日 (0-31) byte []month = new byte[4];
         * //4月 (1-12) byte []year = new byte[7]; //7年 (0-127=1980-2107) };
         */

        Time t = new Time();
        t.setToNow();
        // DebugUtil.i(TAG,
        // "时间="+t.year+t.month+t.monthDay+t.hour+t.minute+t.second);

        byte[] bitArray = new byte[8];
        bitArray = getBitArray((byte) (t.second / 2));
        for (int i = 0; i < 5; i++) {
            mFileHeader.rh_creat.sec2[i] = bitArray[i];
            // DebugUtil.i(TAG, "second="+mFileHeader.rh_creat.sec2[i]);
        }

        bitArray = new byte[8];
        bitArray = getBitArray((byte) t.minute);
        for (int i = 0; i < 6; i++) {
            mFileHeader.rh_creat.min[i] = bitArray[i];
            // DebugUtil.i(TAG, "min="+mFileHeader.rh_creat.min[i]);
        }

        bitArray = new byte[8];
        bitArray = getBitArray((byte) t.hour);
        for (int i = 0; i < 5; i++) {
            mFileHeader.rh_creat.hour[i] = bitArray[i];
            // DebugUtil.i(TAG, "hour="+mFileHeader.rh_creat.hour[i]);
        }

        bitArray = new byte[8];
        bitArray = getBitArray((byte) t.monthDay);
        for (int i = 0; i < 5; i++) {
            mFileHeader.rh_creat.day[i] = bitArray[i];
            // DebugUtil.i(TAG, "monthday="+mFileHeader.rh_creat.day[i]);
        }

        bitArray = new byte[8];
        bitArray = getBitArray((byte) t.month);
        for (int i = 0; i < 4; i++) {
            mFileHeader.rh_creat.month[i] = bitArray[i];
            // DebugUtil.i(TAG, "month="+mFileHeader.rh_creat.month[i]);
        }

        bitArray = new byte[7];
        byte year = (byte) (t.year - 1980);
        bitArray = getBitArray(year);
        for (int i = 0; i < 7; i++) {
            mFileHeader.rh_creat.year[i] = bitArray[i];
            // DebugUtil.i(TAG, "year="+mFileHeader.rh_creat.year[i]);
        }

        // 修改时间与创建时间相同
        mFileHeader.rh_modif = mFileHeader.rh_creat;

        // 探测模式
        mFileHeader.rh_workType = (byte) getWorkMode();

        mFileHeader.rh_nrgain = 9;
        mFileHeader.rh_epsr = mJiedianConst;
        mFileHeader.rh_flagExt = (short) mWheelExtendNumber;
        mFileHeader.rh_workType = (byte) getWorkMode();
        for (int i = 0; i < 9; i++) {
            mFileHeader.rh_rgainf[i] = mHardPlus[i];
        }
    }

    // 将byte转化为有8个数据的byte数组
    public byte[] getBitArray(byte b) {
        byte[] array = new byte[8];
        DebugUtil.i(TAG, "orignByte=" + b);
        for (int i = 0; i < 8; i++) {
            array[i] = (byte) (b & 1);
            b = (byte) (b >> 1);
            DebugUtil.i(TAG, "array[" + i + "]=" + array[i]);
        }
        return array;
    }

    private byte WHELL_MODE = 2; // 轮测模式
    private byte DIANCE_MODE = 1; // 点测模式
    private byte TIME_MODE = 0; // 时间模式(连续模式)

    public int getWorkMode() {
        if ((mNowMode & RADARDEVICE_DIANCE) == RADARDEVICE_DIANCE)
            return DIANCE_MODE;
        if ((mNowMode & RADARDEVICE_WHEEL) == RADARDEVICE_WHEEL)
            return WHELL_MODE;
        return TIME_MODE;
    }

    // 得到雷达参数文件路径
    public String getParamsPath() {
        return INNERSTORAGE + mParamsFilefolderPath;
    }

    // 根据选择指定路径
    private int mSelectStorageIndex = 0;

    public int getSelectStorageIndex() {
        DebugUtil.i(TAG, "getSelectStorageIndex mSelectStorageIndex=" + mSelectStorageIndex);
        return mSelectStorageIndex;
    }

    /**
     * 设置存储路径
     *
     * @param selectID
     */
    public void setStoragePath(int selectID) {
        DebugUtil.i(TAG, "setStoragePath!");
        switch (selectID) {
            case INNER_INDEX:
                radarDevice.mStoragePath = this.INNERSTORAGE;
                break;
            case SDCARD_INDEX:
                radarDevice.mStoragePath = this.SDCARDSTORAGE;
                break;
            case USB_INDEX:
                radarDevice.mStoragePath = this.USBSTORAGE;
                break;
            default:
                break;
        }
        mSelectStorageIndex = selectID;
        DebugUtil.i(TAG, "set mSelectStorageIndex=" + mSelectStorageIndex);
    }

    /**
     * 通过index获得路径，0 内存，1sdcard，2usb盘
     *
     * @param index
     * @return
     */
    public String getStoragePathByIndex(int index) {
        String path = null;
        switch (index) {
            case INNER_INDEX:
                path = this.INNERSTORAGE;
                break;
            case SDCARD_INDEX:
                path = this.SDCARDSTORAGE;
                break;
            case USB_INDEX:
                path = this.USBSTORAGE;
                break;
            default:
                break;
        }
        return path;
    }

    public String getStoragePath() {
        return mStoragePath;
    }

    public String getInnerStoragePath() {
        return INNERSTORAGE;
    }

    // 得到取样点数
    public int getScanLength() {
        return mScanLength;
    }

    //
    public int getScanLengthFromeIndex(int index) {
        return g_scanLen[index];
    }

    //
    public int getScanlenIndexFromeValue(int value) {
        int i;
        for (i = 0; i < mTotalScanlenNumber; i++) {
            if (g_scanLen[i] == value)
                return i;
        }
        //
        return 0;
    }

    // 得到扫速
    public int getScanSpeed() {
        return mScanSpeed;
    }

    // 得到时窗值
    public int getTimeWindow() {
        return mTimeWindow;
    }

    // 得到触发距离
    public double getTouchDistance() {
        double result = mWheelInterDistance[mWheeltypeSel] * mWheelExtendNumber *
                        mWhellcheckCoeff[mWheeltypeSel];
        // BigDecimal b = new BigDecimal(result);
        // result =
        // b.setScale(4,BigDecimal.ROUND_HALF_DOWN).doubleValue();//向下取整
        return result;
    }

    // 设置时窗
    /*
     * 根据主频值，确定时窗的范围[1-8000]
     */
    public void setTimeWindow(int wndVal, boolean isPro) {
        DebugUtil.i("setTimeWindow", "enter setTimeWindow!");

        // 限制值的范围
        if (wndVal < 1) {
            wndVal = 1;
        } else if (wndVal > 8000) {
            wndVal = 8000;
        }

        mTimeWindow = checkTimewnd(wndVal);
        mFileHeader.rh_range = mTimeWindow;

        if (isPro) {
            if (!isCanSendCommand())
                return;
            // //计算步进参数
            CalStepParams();
            if (!isDianCeMode())
                // 发送停止命令
                stopRadar();
            // 设置步进参数
            setStepParams();
            // 设置fad参数
            setFADParams();
            // 设置信号位置20170321
            setSignalParams();
            // 发送开启命令
            mHadRcvScans = 0;

            if (!isDianCeMode())
                startRadar();
            else
                ;
        } else
            ;
    }

    public radarDevice() {
        int i;
        //
        mDatasBufs = new byte[mBufsNumber][];
        mBufIndex = 0;
        for (i = 0; i < mBufsNumber; i++) {
            mDatasBufs[i] = new byte[mBufLength];
            mNowWPos[i] = 0;
        }
        for (i = 0; i < mBufsNumber; i++) {
            mNowRPos[i] = 0;
        }
        //
        // mStoragePath =
        // "/mnt/sdcard";//android.os.Environment.getExternalStorageDirectory().getAbsolutePath();

        //
        mScanLength = 512;
        mTimeWindow = 40;

        //
        mFileHeader.rh_nsamp = (short) mScanLength;
        mFileHeader.rh_range = mTimeWindow;

        // 生成参数文件夹
        String pathName;
        pathName = INNERSTORAGE + mParamsFilefolderPath;
        File destDir = new File(pathName);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        // 生成雷达数据文件夹
        pathName = INNERSTORAGE + mLTEFilefolderPath;
        File lteDir = new File(pathName);
        if (!lteDir.exists()) {
            lteDir.mkdirs();
        }

        // 初始化校准值都为1
        // mWhellcheckCoeff = new double[MAXDEFUALTWHEELNUM];

        for (i = 0; i < MAXDEFUALTWHEELNUM; i++)
            mWhellcheckCoeff[i] = 1;

        for (i = 0; i < 8192; i++)
            mTotalNeedZoom1[i] = 1;
    }

    /**
     * 设置某项的校正值
     *
     * @param coeff
     * @param which
     */
    public void setWhellCheckCoeff(double coeff, int which) {
        if (coeff < 0.001)
            coeff = 0.001;
        else if (coeff > 1000)
            coeff = 1000;
        mWhellcheckCoeff[which] = coeff;
    }

    // 得到某项校准值
    public double getWhellCheckCoeff(int which) {
        return mWhellcheckCoeff[which];
    }

    /**
     * 得到默认测距仪个数 返回默认的测距仪的个数
     */
    public int getWheelDefaultMaxNum() {
        return this.MAXDEFUALTWHEELNUM;
    }

    // 开始保存数据
    public boolean beginSave() {
        // 停止雷达
        // if(stopUSBLTD() == -1)
        // return false;
        // 停止雷达
        if (!isDianCeMode())
            if (stopRadar() == -1)
                return false;

        // 设置内存变量
        mBufIndex = 0;
        for (int i = 0; i < mBufsNumber; i++) {
            mNowWPos[i] = 0;
        }

        // 开启雷达
        if (!isDianCeMode()) {
            if (startRadar() == -1)
                return false;
        }

        mNowMode |= RADARDEVICE_SAVING;
        mSaveMode = RADARDEVICE_SAVING_CONTINUE;

        mHadRcvScans = 0;

        if (isBackOrientMode()) {
            // 回退定位结束
            mBackOrientFlag = false;
            mHadBackScans = 0;
        }

        return true;
    }

    // 停止保存数据
    public boolean stopSave() {

        // //
        if (!this.isDianCeMode()) {
            // 停止雷达
            if (stopRadar() == -1)
                return false;
        }
        // 进行最后一次保存
        lastSaveDatas1();

        // 设置内存变量
        mBufIndex = 0;
        for (int i = 0; i < mBufsNumber; i++) {
            mNowWPos[i] = 0;
        }
        //
        mNowMode &= (~RADARDEVICE_SAVING);
        if (mExistSaveFile) {
            try {
                fSaveOS.close();
                //
                mExistSaveFile = false;
            } catch (Exception e) {
                Toast.makeText(mContext, e.getMessage(), 1000).show();
            }
        }
        mSaveMode = RADARDEVICE_NOSAVING;
        // 开启雷达
        // if(startUSBLTD() == -1)
        // return false;
        if (!this.isDianCeMode()) {
            if (startRadar() == -1)
                return false;
        }

        return true;
    }

    //
    public int getAntenFrqSel() {
        return mAntenFrqSel;
    }

    public int getScanSpeedSel() {
        return mScanSpeedSel;
    }

    public int getScanspeedIndexFromeValue(int value) {
        int i;
        for (i = 0; i < g_scanSpeedNumber; i++) {
            if (g_scanSpeed[i] == value)
                return i;
        }
        //
        return 0;
    }

    public int getScanLengthSel() {
        return mScanLengthSel;
    }

    public int getFilterSel() {
        return mFilterSel;
    }

    public int getRemoveBackSel() {
        return mRemoveBackSel;
    }

    // //
    public long getHadRcvScans() {
        return mHadRcvScans;
    }

    // /
    public int getAveNumber() {
        return mScanAve;
    }

    // //
    public void delFillposCursor(int delNum) {
        mFillposCursor = mFillposCursor - delNum;
        if (mFillposCursor < 0)
            mFillposCursor = 0;
    }

    public int getFillposCursor() {
        return mFillposCursor;
    }

    // //得到已经行进的距离(cm)
    public double getHasRcvDistance() {
        return mHadRcvScans * mWheelExtendNumber * mWheelInterDistance[mWheeltypeSel];
    }

    // //得到硬件增益
    public float[] getHardplus() {
        return mHardPlus;
    }

    // //
    public int getScanAveNumber() {
        return mScanAve;
    }

    // //
    public float getJieDianConst() {
        return mJiedianConst;
    }

    // //根据介电常数和时窗计算深度值
    public double getDeep() {
        double speed = 0.3 * 100 / Math.sqrt(mJiedianConst); // cm/s;
        double deep = (mTimeWindow) * speed / 200;

        return deep;
    }

    // //判断是否已经超速
    public boolean isOverSpeed() {
        return mOverspeedFlag;
    }

    // 判断是否更改datalistadapter中的数据，主要是参数列表中的数据
    public boolean isChangeDataListAdapter() {
        return mChangeDataListAdapter;
    }

    // //得到零偏
    public int getZeroOffVal() {
        return mZeroOff;
    }

    // //得到信号位置
    public int getSignalpos() {
        return mSignalPos;
    }

    // //设置雷达波速
    public void setWavespeed(double speed) {
        mWavespeed = speed;
    }

    // //从波速得到介电常数
    public double getJDConstFromeWavespeed(double speed) {
        double jdConst;
        jdConst = 30. / speed;
        return jdConst * jdConst;
    }

    // //得到波速
    public double getWavespeed() {
        double speed = 0.3 * 100 / Math.sqrt(mJiedianConst); // cm/s;
        return speed;
    }

    // //设置时间模式
    public boolean setTimeMode() {
        if (!isCanSendCommand())
            return false;
        // 如果处于轮测模式
        if (isWhellMode()) {
            stopWhellMode();
        }

        // 如果处于点测模式
        if (isDianCeMode()) {
            stopDianCeMode();
        }

        setReadScansParams();// 20170616

        // 设置标记扩展为0
        short[] commands = new short[4];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_WHEELEXTNUMBER;
        commands[2] = 1;
        commands[3] = (short) 0;
        sendCommands_1(commands, (short) 8);

//        USBDriver.stopUSBLTD();
        stopUSB();

        mNowMode &= (~0xF0);
        mNowMode |= RADARDEVICE_CONTINUE;
        //
        mHadRcvScans = 0;
        int ret = startUSB();
        if (ret == -1)
            return false;
        //
        mFileHeader.setTimeMode();

        // 复位超速报警
        mOverspeedFlag = false;

        return true;
    }

    // 停止轮测模式
    public void stopWhellMode() {
        short[] commands = new short[2];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_WHEELEND;
        //
        sendCommands_1(commands, (short) 4);

        mNowMode &= (~0xF0);
    }

    // 停止点测模式
    public void stopDianCeMode() {
        // 停止雷达
        stopRadar();

        //
        mNowMode &= (~0xF0);
    }

    // //仅仅设置增益值,不放松命令
    public void setHardplusValusOnly(float[] vals) {
        DebugUtil.i(TAG, "enter setHardplusValusOnly!");
        mHardPlus = vals;
        int i;
        for (i = 0; i < 9; i++) {
            mRealHardPlus[i] = mHardPlus[i];// (mHardPlus[i]+4.39);
            DebugUtil.i(TAG, "mRealhardplus[" + i + "]=" + mRealHardPlus[i]);
        }
    }

    // //得到点测时的重复次数
    public int getDianceNumber() {
        return mDianceNumber;
    }

    // //
    public double getWheelCoeff() {
        return mWhellcheckCoeff[mWheeltypeSel];
    }

    // //得到测距轮相邻标记间的距离
    public double getWheelInterDistance() {
        return mWheelInterDistance[mWheeltypeSel];
    }

    /**
     * 设置测距轮扩展值，设定范围(0,5000]
     */
    public void setWheelExtendNumber(int extendNum) {
        if (mWheelExtendNumber < 0) {
            this.mWheelExtendNumber = 1;
        } else if (mWheelExtendNumber > 5000) {
            this.mWheelExtendNumber = 5000;
        }
        mWheelExtendNumber = extendNum;
    }

    // //得到测距轮控制时的扩展值
    public int getWheelExtendNumber() {
        return mWheelExtendNumber;
    }

    // //得到测距轮类型选择值
    public int getWheeltypeSel() {
        return mWheeltypeSel;
    }

    public int getHandleTypeSel() {
        int sel = 0;
        if (mHandleMode == HANDLEMODE_WHEEL)
            sel = 0;
        if (mHandleMode == HANDLEMODE_NORMAL)
            sel = 1;
        if (mHandleMode == HANDLEMODE_COM)
            sel = 2;

        return sel;
    }

    // //设置零篇
    public void setZerooff(short zerooff) {
        mZeroOff = zerooff;

        if (!isCanSendCommand())
            return;
        //
        if (!isDianCeMode())
            stopRadar();

        //
        short[] commands = new short[12];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_ZEROOFF;
        commands[2] = 9;
        for (int i = 0; i < 9; i++)
            commands[3 + i] = (short) mZeroOff;

        sendCommands_1(commands, (short) 24);

        mHadRcvScans = 0;
        if (!isDianCeMode())
            startRadar();
    }

    public void resetHardPlus() {
        setHardplus(mHardPlus);
    }

    // //设置硬件增益
    public void setHardplus(float[] vals) {
        DebugUtil.i(TAG, "sethardplus send command!");
        mHardPlus = vals;
        int i;
        for (i = 0; i < 9; i++) {
            mRealHardPlus[i] = mHardPlus[i];
            DebugUtil.i(TAG, "hardplus[" + i + "]=" + mHardPlus[i]);
        }

        CalHardplusParams();

        // 停止雷达
        if (!isDianCeMode())
            stopRadar();
        // hss0427
        setHardplusParams();

        // 开启雷达
        mHadRcvScans = 0;
        if (!isDianCeMode())
            startRadar();

        // 重复一次开始命令，防止出现死机
        // if(!isDianCeMode())
        // {
        // startRadar();
        // //重复一次开始命令，防止出现死机
        // //startRadar();
        // }

        mFileHeader.setHardPlus(vals);

        // hss0426
        // if(isTimeMode())
        // {
        // setTimeMode();
        // }

        //
        mIsAutoplus = false;
    }

    public boolean isFSAnten(int antenFrqIndex) {
        if (antenFrqIndex == GC100S_INDEX) {
            return true;
        }
        return false;
    }

    // 计算步进参数
    public int GetModifyScanL(short scanL, short scanSpeed, short repFrq) {
        int add = 6;
        if (scanL == 1024 && scanSpeed == 128 && repFrq == 32)
            add = 12;
        if (scanL == 2048 && scanSpeed == 64 && repFrq == 32)
            add = 10;
        if (scanL == 2048 && scanSpeed == 64 && repFrq == 64)
            add = 10;
        if (scanL == 2048 && scanSpeed == 32 && repFrq == 128)
            add = 10;
        if (scanL == 2048 && scanSpeed == 64 && repFrq == 128)
            add = 12;
        //
        return add;
    }

    // 延时芯片在不同温度下的延时间隔{30~50,50~}
    public float[] mDelayTimes = {(float) 4.590, (float) 4.660};
    private float DELAY_TIME = (float) 4.40;// 4.660;//4.530; //(float)4.796;
    // //(float) 4.807; //延时芯片固定延时

    public void setDelayTime(float time) {
        if (time != DELAY_TIME) {
            DELAY_TIME = time;
            String txt;
            txt = "set delay_time:=" + DELAY_TIME;

            // ((IDSC2600MainActivity)mContext).showToastMsg(txt);
            setTimeWindow(mTimeWindow, false);
        }

        DebugUtil.i(TAG, "Delay_Time=" + String.valueOf(DELAY_TIME));
    }

    public float getDelayTime() {
        return DELAY_TIME;
    }

    // 计算步进参数
    public int CalStepParams() {
        DebugUtil.i(TAG, "Enter CalStepParams!");

        short sigPosBeg1, sigPosBeg2, sigPosBeg4, sigPosEnd1, sigPosEnd2;
        short FAD; // FAD频率包含的主时钟个数
        short repFrqNum; // 重复频率包含的主时钟个数
        short shiftRegPreSet, shiftRegEndSet;
        short stepInter;
        short stepLength;
        short stepLPer8ns; // 一个主时钟(125MHz)包含的
        short repFrq; // 重复频率
        repFrq = (short) mRepFrq;
        short sigPosBeg, sigPosEnd;
        short timeWnd;
        short scanSpeed;
        short scanL;
        float tem;
        short ShiftRegPreSet, ShiftRegEndSet;
        timeWnd = (short) mTimeWindow;
        scanSpeed = (short) mScanSpeed;
        scanL = (short) mScanLength;
        ShiftRegPreSet = 6;
        ShiftRegEndSet = 6;

        // ////LTD2100计算公式
        /*
         * int mStepWnd = 20; sigPosBeg=(short)(mSignalPos+196);
         * sigPosBeg1=(short)(sigPosBeg/mStepWnd);
         *
         * sigPosBeg2=(short)(sigPosBeg%mStepWnd); if(sigPosBeg2 >=0&&sigPosBeg2
         * < 5) ShiftRegPreSet=6; if(sigPosBeg2 >=5 && sigPosBeg2 <10)
         * ShiftRegPreSet=3; if(sigPosBeg2 >=10 && sigPosBeg2 <15)
         * ShiftRegPreSet=9; if(sigPosBeg2 >=15 && sigPosBeg2 <20)
         * ShiftRegPreSet=12; /////////////// ////2009.1.31 if(scanSpeed==0)
         * return 0; ////2009.1.31 int nPlus; nPlus=repFrq*1000/scanSpeed;
         * //计算步进间隔 float tem1,tem2; tem1=(float)timeWnd; tem2=(float)scanSpeed;
         * tem=tem1*tem2; tem1=10*repFrq; tem=tem/tem1; int temInt;
         * temInt=(int)(tem); if(tem*10-temInt*10 >=5) temInt++;
         * stepInter=(short)temInt; if(stepInter<1) stepInter=1; do{ //计算5ns步进间隔
         * tem1=(float)(9.2592); tem2=(float)(stepInter); tem=tem1*tem2;
         * tem=(float)(5.*1000./tem); temInt=(int)tem; if(tem*10-temInt*10>=5)
         * temInt++; stepLPer5ns=(short)temInt; stepLPer5ns+=1; //计算步进长度
         * stepLength=(short)((float)timeWnd*stepLPer5ns/5); // if(stepLength >
         * nPlus) stepInter++; else break; }while(true); //////////////////////
         * sigPosBeg2=(short)(sigPosBeg%mStepWnd%5*stepLPer5ns/5);
         * sigPosEnd=(short)(sigPosBeg+timeWnd);
         * sigPosEnd1=(short)(sigPosEnd/mStepWnd);
         * sigPosEnd2=(short)(sigPosEnd%mStepWnd); if(sigPosEnd2 >=0 &&
         * sigPosEnd2 < 5) ShiftRegEndSet=6; if(sigPosEnd2 >=5 && sigPosEnd2
         * <10) ShiftRegEndSet=3; if(sigPosEnd2 >=10 && sigPosEnd2 <15)
         * ShiftRegEndSet=9; if(sigPosEnd2 >=15 && sigPosEnd2 <20)
         * ShiftRegEndSet=12; //
         * sigPosEnd2=(short)(sigPosEnd%mStepWnd%5*stepLPer5ns/5.);
         * sigPosBeg4=(short)((sigPosBeg%20)/5.0*stepLPer5ns);
         *
         * // int add=GetModifyScanL(scanL,scanSpeed,repFrq);
         * tem=(float)((float)repFrq*1000./(float)(stepLength));
         * tem=200000000/tem; tem=tem/(scanL+add); FAD=(short)tem;
         * repFrqNum=(short)(g_fixNumRF[mAntenFrqSel]);//1560;//3120;//
         * stepLength=(short)nPlus; mFAD = FAD;
         *
         * // mSignalPosParams[0]=sigPosBeg1; mSignalPosParams[1]=sigPosBeg2;
         * mSignalPosParams[2]=sigPosEnd1; mSignalPosParams[3]=sigPosEnd2;
         * mSignalPosParams[4]=ShiftRegPreSet;
         * mSignalPosParams[5]=ShiftRegEndSet; mSignalPosParams[6]=sigPosBeg4;
         * mSignalPosParams[7]=sigPosBeg;
         *
         * // mStepParams[0]=stepLength; mStepParams[1]=stepInter;
         * mStepParams[2]=stepLPer5ns; mStepParams[3]=repFrqNum;
         * mStepParams[4]=timeWnd;
         */

        // ///新主机LTDPro计算公式
        int lStepWnd = 8;
        sigPosBeg = (short) (mSignalPos + 96);
        sigPosBeg1 = (short) (sigPosBeg / lStepWnd);
        sigPosBeg2 = (short) (sigPosBeg % lStepWnd);
        if (sigPosBeg2 >= 0 && sigPosBeg2 < 5)
            ShiftRegPreSet = 6;
        if (sigPosBeg2 >= 5 && sigPosBeg2 < 10)
            ShiftRegPreSet = 3;
        if (sigPosBeg2 >= 10 && sigPosBeg2 < 15)
            ShiftRegPreSet = 9;
        if (sigPosBeg2 >= 15 && sigPosBeg2 < 20)
            ShiftRegPreSet = 12;
        // /////////////
        // //2009.1.31
        if (scanSpeed == 0)
            return 0;
        // //2009.1.31
        int nPlus;
        nPlus = repFrq * 1000 / scanSpeed;
        // 计算步进间隔
        float tem1, tem2;
        tem1 = timeWnd;
        tem2 = scanSpeed;
        tem = tem1 * tem2;
        tem1 = 5 * repFrq;
        tem = tem / tem1;
        int temInt;
        temInt = (int) (tem);
        if (tem * 10 - temInt * 10 >= 5)
            temInt++;
        stepInter = (short) temInt;
        if (stepInter < 1)
            stepInter = 1;
        do {
            // 计算8ns步进间隔
            tem1 = (DELAY_TIME);
            tem2 = (stepInter);
            tem = tem1 * tem2;
            tem = (float) (8. * 1000. / tem);

            temInt = (int) tem;
            if (tem * 10 - temInt * 10 >= 5)
                temInt++;
            DebugUtil.i("setTimeWindow", "tem=" + tem + ",temInt=" + temInt);

            stepLPer8ns = (short) temInt;
            // 计算步进长度
            stepLength = (short) ((float) timeWnd * stepLPer8ns / 8);

            if (stepLength > nPlus)
                stepInter++;
            else
                break;
        } while (true);

        sigPosBeg2 = (short) (sigPosBeg % lStepWnd % 5 * stepLPer8ns / 8);
        sigPosEnd = (short) (sigPosBeg + timeWnd);
        sigPosEnd1 = (short) (sigPosEnd / lStepWnd);
        sigPosEnd2 = (short) (sigPosEnd % lStepWnd);
        if (sigPosEnd2 >= 0 && sigPosEnd2 < 5)
            ShiftRegEndSet = 6;
        if (sigPosEnd2 >= 5 && sigPosEnd2 < 10)
            ShiftRegEndSet = 3;
        if (sigPosEnd2 >= 10 && sigPosEnd2 < 15)
            ShiftRegEndSet = 9;
        if (sigPosEnd2 >= 15 && sigPosEnd2 < 20)
            ShiftRegEndSet = 12;

        sigPosEnd2 = (short) (sigPosEnd % lStepWnd % 5 * stepLPer8ns / 8.);
        sigPosBeg4 = (short) ((sigPosBeg % 8) / 8.0 * stepLPer8ns);

        int add = GetModifyScanL(scanL, scanSpeed, repFrq);
        tem = (float) (repFrq * 1000. / (stepLength));
        tem = 125000000 / tem;
        tem = tem / (scanL + add);
        FAD = (short) tem;
        repFrqNum = (short) (125000000 / (repFrq * 1000));
        stepLength = (short) nPlus;
        mFAD = FAD;

        mSignalPosParams[0] = sigPosBeg1;
        mSignalPosParams[1] = sigPosBeg2;
        mSignalPosParams[2] = sigPosEnd1;
        mSignalPosParams[3] = sigPosEnd2;
        mSignalPosParams[4] = ShiftRegPreSet;
        mSignalPosParams[5] = ShiftRegEndSet;
        mSignalPosParams[6] = sigPosBeg4;
        mSignalPosParams[7] = sigPosBeg;

        for (int i = 0; i <= 7; i++)
            DebugUtil.i("setTimeWindow",
                        "CalStepParams mSignalPos[" + i + "]=" + mSignalPosParams[i]);
        //
        mStepParams[0] = stepLength;
        mStepParams[1] = stepInter;
        mStepParams[2] = stepLPer8ns;
        mStepParams[3] = repFrqNum;
        mStepParams[4] = timeWnd;

        for (int i = 0; i <= 4; i++)
            DebugUtil.i("setTimeWindow", "CalStepParams mStepParams[" + i + "]=" + mStepParams[i]);

        return 1;
    }

    // 计算软增益
    public void calSoftPlus() {
        int i;
        // 计算波形的放大倍数
        // 计算倍数
        int scanLen = mScanLength;
        double[] zoomBase = new double[9];

        // //计算需要的增益
        int j;
        double temVal;
        float[] mNeedPlus = mRealHardPlus; // 用户设置的增益,总增益

        // 计算总的放大倍数
        double[] zoomNeed = new double[9]; // 用户设置对应的放大倍数
        for (j = 0; j < 9; j++) {
            temVal = mNeedPlus[j] / 20.;
            temVal = Math.pow(10, temVal);
            zoomNeed[j] = temVal;
        }

        // 利用线性插值算法计算总的放大倍数
        double scanLenPer;
        double zoom1;
        int index;
        scanLenPer = scanLen / 8.;
        for (i = 0; i < 8; i++) {
            // 计算当前段的放大斜率
            zoom1 = (zoomNeed[i + 1] - zoomNeed[i]) / scanLenPer;
            for (j = 0; j < (int) scanLenPer; j++) {
                index = (int) (j + i * scanLenPer);
                mTotalNeedZoom[index] = zoomNeed[i] + zoom1 * j;
            }
        }

        // ///计算硬件增益的放大倍数
        for (j = 0; j < 9; j++) {
            temVal = mRealHardPlus[j];
            if (temVal > 5)
                temVal = 5;
            temVal = temVal / 20.;
            temVal = Math.pow(10, temVal);
            zoomNeed[j] = temVal;
        }

        for (i = 0; i < 8; i++) {
            // 计算当前段的放大斜率
            zoom1 = (zoomNeed[i + 1] - zoomNeed[i]) / scanLenPer;
            for (j = 0; j < (int) scanLenPer; j++) {
                index = (int) (j + i * scanLenPer);
                mHardZoom[index] = zoomNeed[i] + zoom1 * j;
            }
        }
        // ///计算软件放大倍数
        for (i = 0; i < scanLen; i++) {
            mSoftZoom[i] = mTotalNeedZoom[i] / mHardZoom[i];
            // DebugUtil.e(TAG,"The "+i+"softZoom:="+mSoftZoom[i]);
        }

        if (!mIsUseSoftPlus) {
            for (i = 0; i < scanLen; i++) {
                mSoftZoom[i] = 1;
                mTotalNeedZoom[i] = 1;
            }
        }
    }

    public boolean mIsUseSoftPlus = false;

    // 计算硬件增益:
    public void CalHardplusParams() {
        if (!mIsUseSoftPlus) {
            double val;
            for (int i = 0; i < 9; i++) {
                val = mHardPlus[i] + 4.39; // 7.17;
                val /= 20;
                val = Math.pow(10, val);
                val = Math.sqrt(val);
                val = 4096 / val;
                mHardplusParams[i] = (short) val;
            }
        } else {
            double val;
            int i;
            for (i = 0; i < 9; i++) {
                val = 0.;
                val /= 20;
                val = Math.pow(10, val);
                val = Math.sqrt(val);
                val = 4096 / val;
                mHardplusParams[i] = (short) val;
            }
        }
        calSoftPlus();
    }

    // 步进参数
    private short[] mSignalPosParams = new short[8];
    private short[] mStepParams = new short[5];
    private short[] mHardplusParams = new short[9];
    private short mFAD;

    private static boolean staticUSBDriverExists() {
        return (new File("/system/lib/libUSBDriver-JNI.so")).exists();
    }

    static boolean useStatic;

    static {
        useStatic = staticUSBDriverExists();
        if (!useStatic) {
            System.loadLibrary("USBDriver-JNI");
        }
    }

    // JNI函数
    public native int openUSBLTD();

    public native int closeUSBLTD();

    public native int startUSBLTD();

    public native int stopUSBLTD();

    public native int readUSBLTD();

    public native int readOneWave(short[] Bufs, int size);

    public native int sendCommands(short[] Coms, short length);

    int openUSB() {
        return useStatic ? openUSBLTD() : USBDriver.openUSBLTD();
    }

    int closeUSB() {
        return useStatic ? closeUSBLTD() : USBDriver.closeUSBLTD();
    }

    int startUSB() {
        return useStatic ? startUSBLTD() : USBDriver.startUSBLTD();
    }

    int stopUSB() {
        return useStatic ? stopUSBLTD() : USBDriver.stopUSBLTD();
    }

    int readUSB() {
        return useStatic ? readUSBLTD() : USBDriver.readUSBLTD();
    }

    int readData(short[] buf, int size) {
        return useStatic ? readOneWave(buf, size) : USBDriver.readOneWave(buf, size);
    }

    int writeCommandsToUSB(short[] commands, short length) {
        return useStatic ? sendCommands(commands, length) : USBDriver.sendCommands(commands,
                                                                                   length);
    }

    // 设备错误代码
    private int RADARDEVICE_ERROR_NO = 0; // 设备错误---正确
    private int RADARDEVICE_ERROR_CHANGEMODUSBLTD = 0x1000; // 更改设备文件属性错误
    private int RADARDEVICE_ERROR_OPEN = 0x1001; // 打开设备文件错误
    private int RADARDEVICE_ERROR_STARTCOMMAND = 0x1002; // 发送 '开始命令' 错误
    private int RADARDEVICE_ERROR_STOPCOMMAND = 0x1003; // 发送 '停止命令' 错误
    private int RADARDEVICE_ERROR_CLOSE = 0x1004; // 关闭设备文件错误

    // //雷达设备状态
    // 总状态字
    private int RADARDEVICE_NOOPEN = 0x0; // 设备没有打开
    private int RADARDEVICE_READY = 0x1; // 设备就绪，已经打开驱动程序，还没有开启工作
    private int RADARDEVICE_REALTIME = 0x100; // 实时工作状态
    private int RADARDEVICE_CONTINUE = 0x10; // 连续工作模式(时间模式)
    private int RADARDEVICE_DIANCE = 0x20; // 点测模式
    private int RADARDEVICE_WHEEL = 0x40; // 轮侧模式
    private int RADARDEVICE_PLAYBACK = 0x80; // 回放模式
    private int RADARDEVICE_CALIBRATE = 0x50; // 校准模式
    private int RADARDEVICE_SAVING = 0x200; // 正在保存数据
    private int RADARDEVICE_BACKPLAYING = 0x400; // 正在回放数据
    private int RADARDEVICE_CUSTOMFILE = 0x800; // 自定义测距轮文件

    // 保存数据时设备子状态
    private int RADARDEVICE_NOSAVING = 0; // 没有保存
    private int RADARDEVICE_SAVING_TEMSTOP = 0x1; // 暂停保存
    private int RADARDEVICE_SAVING_CONTINUE = 0x2; // 正在保存

    // 回放数据时状态
    private int RADARDEVICE_NOBACKPLAY = 0; // 没有回放
    private int RADARDEVICE_BACKPLAYING_TEMSTOP = 0x1; // 暂停回放

    private int mNowMode = RADARDEVICE_NOOPEN; // 雷达当前状态(总)
    private int mSaveMode = RADARDEVICE_NOSAVING; // 雷达保存数据时的子状态
    private int mBackplayMode = RADARDEVICE_NOBACKPLAY; // 雷达数据回放时的子状态

    private int HANDLEMODE_WHEEL = 1; // 测距论
    private int HANDLEMODE_NORMAL = 2; // 简易手柄
    private int HANDLEMODE_COM = 3; // COM口手柄
    private int mHandleMode = HANDLEMODE_WHEEL;
    // 回退功能
    private boolean mBackOrientFlag = false; // 回退定位标志
    public int mHadBackScans = 0; // 已经回退的道数

    // 得到手柄模式
    public int getHandleMode() {
        return mHandleMode;
    }

    public void setHandle_wheelMode() {
        mHandleMode = HANDLEMODE_WHEEL;
        if (!isCanSendCommand())
            return;

        // 设置标记扩展为0
        short[] commands = new short[4];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_HANDLEMODE;
        commands[2] = 1;
        commands[3] = (short) 1;
        sendCommands_1(commands, (short) 8);
    }

    public void setHandle_normalMode() {
        mHandleMode = HANDLEMODE_NORMAL;
        if (!isCanSendCommand())
            return;
        // 设置标记扩展为0
        short[] commands = new short[4];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_HANDLEMODE;
        commands[2] = 1;
        commands[3] = (short) 2;
        sendCommands_1(commands, (short) 8);
    }

    public void setHandle_COMMode() {
        mHandleMode = HANDLEMODE_COM;
        if (!isCanSendCommand())
            return;
        // 设置标记扩展为0
        short[] commands = new short[4];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_HANDLEMODE;
        commands[2] = 1;
        commands[3] = (short) 3;
        sendCommands_1(commands, (short) 8);
    }

    public int getHandleType() {
        return mHandleMode;
    }

    public void setHandleType(int type) {
        mHandleMode = type;
    }

    // /
    public int startForCheckPower() {
        int ret = RADARDEVICE_ERROR_NO;

        Intent broadcastIntent = new Intent("com.example.LTDDRIVERLOADBROADCAST");
        broadcastIntent.addCategory("com.example.mycategory");
        broadcastIntent.putExtra("name", (byte) 3);
        ((MultiModeLifeSearchActivity) mContext).sendBroadcast(broadcastIntent);

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            DebugUtil.e(TAG, "PowerDevice:powerUpCEBAN fail!");
        }

        // //打开设备
        ret = openUSB();
        if (ret == -1) {
            ret = RADARDEVICE_ERROR_OPEN;
            return ret;
        }

        return ret;
    }

    // 设置手柄模式
    public void setHandleMode() {
        if (mHandleMode == HANDLEMODE_WHEEL)
            setHandle_wheelMode();
        if (mHandleMode == HANDLEMODE_NORMAL)
            setHandle_normalMode();
        if (mHandleMode == HANDLEMODE_COM)
            setHandle_COMMode();
    }

    // /开启雷达
    public int start() {
        int ret = RADARDEVICE_ERROR_NO;
        /*
         * ////更改设备的属性 try { // Missing read/write permission, trying to chmod
         * the file Process su; su =
         * Runtime.getRuntime().exec("/system/bin/su"); String cmd =
         * "chmod 777 " + "/dev/USBLTD" + "\n" + "exit\n";
         * su.getOutputStream().write(cmd.getBytes()); if ((su.waitFor() != 0) )
         * { throw new SecurityException(); } } catch (Exception e) {
         * e.printStackTrace(); return RADARDEVICE_ERROR_CHANGEMODUSBLTD; //
         * throw new SecurityException(); }
         */
        // //打开设备
        ret = openUSB();
        if (ret == -1) {
            Log.d("debug_radar", "can not open usb ltd");
            ret = RADARDEVICE_ERROR_OPEN;
            return ret;
        } else {
            Log.d("debug_radar", "open usb: " + ret);
        }
        mNowMode = RADARDEVICE_READY; // 设备就绪状态

        // ///设置参数
        int selIndex = mAntenFrqSel;
        // 计算步进参数
        // CalStepParams1();
        CalStepParams();
        // 设置天线频率
        short[] commands = new short[4];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_RADARFRQ;
        commands[2] = 1;
        commands[3] = g_antenFrq[selIndex];
        sendCommands_1(commands, (short) 8);
        DebugUtil.i(TAG, "start 主频=" + g_antenFrq[selIndex]);
        // 设置取样道长
        setScanLenParams();
        // 设置增益
        setHardplus(mHardPlus);
        CalHardplusParams();
        setHardplusParams();
        // 设置滤波参数
        setFilterParams();
        // 设置信号位置参数
        setSignalParams();
        // 设置步进参数
        setStepParams();
        // 设置fad参数
        setFADParams();
        // 发送读取道数
        setReadScansParams();

        // 设置标记扩展值为0
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_WHEELEXTNUMBER;
        commands[2] = 1;
        commands[3] = (short) 0;
        sendCommands_1(commands, (short) 8);

        // 自动增益
        /*
         * short[] autoPlus=new short[12]; int i; for(i=0;i<12;i++) {
         * autoPlus[i] = 0; } autoPlus[0] = IOCTL_CODE_FLAG; autoPlus[1] =
         * CODE_SET_AUTOPLUS; autoPlus[2] = 0;
         * sendCommands_1(autoPlus,(short)24); for(i=0;i<9;i++) mHardPlus[i] =
         * autoPlus[3+i];
         */
        // //发送停止命令，清空dsp的缓存
        stopUSB();
        // //发送开始命令
        ret = startUSB();
        if (ret == -1) {
            Log.d("debug_radar", "can not start usb ltd");
            ret = RADARDEVICE_ERROR_STARTCOMMAND;
            return ret;
        }

        ret = RADARDEVICE_ERROR_NO;
        mNowMode = RADARDEVICE_REALTIME; // 实时采集模式
        mNowMode |= RADARDEVICE_CONTINUE; // 连续采集模式

        return ret;
    }

    // /停止雷达
    public int stop() {
        int ret = RADARDEVICE_ERROR_NO;

        // 发送开始命令
        ret = stopUSB();
        if (ret == -1) {
            ret = RADARDEVICE_ERROR_STOPCOMMAND;
            return ret;
        }

        ret = closeUSB();
        if (ret == -1) {
            ret = RADARDEVICE_ERROR_CLOSE;
            return ret;
        }

        //
        mNowMode = RADARDEVICE_NOOPEN;

        //
        ret = RADARDEVICE_ERROR_NO;
        return ret;
    }

    // //根据当前模式发送停止命令
    public int stopRadar() {
        if (!isCanSendCommand())
            return -1;
        short[] commands = new short[3];
        commands[0] = IOCTL_CODE_FLAG;
        if (isTimeMode()) {
            commands[1] = CODE_SET_STOPCONTINUE;
        }
        if (isWhellMode()) {
            commands[1] = CODE_SET_WHEELEND;
        }
        if (isDianCeMode()) {
            commands[1] = CODE_SET_STOPCONTINUE;
        }
        commands[2] = 0;

        sendCommands_1(commands, (short) 6);

        return 1;
    }

    // //根据当前的模式，发送开启命令
    public int startRadar() {
        if (!isCanSendCommand())
            return -1;
        short[] commands = new short[3];
        commands[0] = IOCTL_CODE_FLAG;
        if (isTimeMode()) {
            commands[1] = CODE_SET_STARTCONTINUE;
        }
        if (isWhellMode()) {
            commands[1] = CODE_SET_WHEELBEG;
        }
        if (isDianCeMode()) {
            commands[1] = CODE_SET_STARTCONTINUE;
        }
        commands[2] = 0;

        sendCommands_1(commands, (short) 6);

        return 1;
    }

    // /打开雷达设备
    public int openDevice() {
        // //更改设备的属性
        // try {
        // // Missing read/write permission, trying to chmod the file
        // Process su;
        // su = Runtime.getRuntime().exec("/system/bin/su");
        // String cmd = "chmod 777 " + "/dev/USBLTD" + "\n"
        // + "exit\n";
        // su.getOutputStream().write(cmd.getBytes());
        // if ((su.waitFor() != 0) ) {
        // throw new SecurityException();
        // }
        // } catch (Exception e) {
        // e.printStackTrace();
        // return RADARDEVICE_ERROR_CHANGEMODUSBLTD;
        // // throw new SecurityException();
        // }

        //
        int ret = openUSB();
        if (ret == -1)
            ret = RADARDEVICE_ERROR_OPEN;
        else
            ret = RADARDEVICE_ERROR_NO;

        return ret;
    }

    // //关闭雷达设备
    public void closeDevice() {
        // 如果处于正在保存数据状态
        if (isSavingMode()) {
            endSaveFile();
        }

        // 如果处于正在开启状态
        if (isRunningMode()) {
            stopUSB();
        }

        // 关闭设备
        closeUSB();

        mNowMode = RADARDEVICE_NOOPEN;
    }

    // 装载动态库
    public boolean loadDriver() {
        // //装载驱动
        try {
            // Missing read/write permission, trying to chmod the file
            Process su;
            su = Runtime.getRuntime().exec("su");
            String cmd;
            cmd = "insmod " + "/system/lib/modules/usb-skeleton.ko" + "\n" + "exit\n";
            su.getOutputStream().write(cmd.getBytes());
            if ((su.waitFor() != 0)) {
                return false;
                // throw new SecurityException();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
            // throw new SecurityException();
        }
        return true;
    }

    // 卸载动态库
    public void unloadDriver() {
        // ///卸载驱动程序
        try {
            // Missing read/write permission, trying to chmod the file
            Process su;
            // su = Runtime.getRuntime().exec("/system/bin/su");
            su = Runtime.getRuntime().exec("su");
            String cmd;
            cmd = "rmmod " + "usb_skeleton" + "\n" + "exit\n";
            su.getOutputStream().write(cmd.getBytes());
            if ((su.waitFor() != 0)) {
                // throw new SecurityException();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // throw new SecurityException();
        }
    }

    // 是否处于保存数据状态
    public boolean isSavingMode() {
        int ret;
        ret = mNowMode & RADARDEVICE_SAVING;
        if (ret == 0)
            return false;

        return true;
    }

    // 结束保存
    public void endSaveFile() {
        // 去掉正在保存数据标志
        mNowMode = mNowMode & (~RADARDEVICE_SAVING);
    }

    // 是否处于正在运行状态
    public boolean isRunningMode() {
        //
        int ret;
        ret = mNowMode & RADARDEVICE_REALTIME;
        if (ret == 0)
            return false;

        return true;
    }

    // ///读取指定长度的数据(字节数)并做处理
    public int FLAG_INDEX = 1;
    public int SAVEFLAG_INDEX = 2;
    private short POSITIVE_FLAG = 0x4000; // 正转标记
    private short NEGATIVE_FLAG = (short) 0x8000; // 反转标记
    private short OVERSPEED_FLAG = (short) 0x0001; // 轮测模式下，已经超速
    private boolean mOverspeedFlag = false; // 标记是否超速
    public int mFillposCursor = 0;

    // 是否处于回退模式
    public boolean isBackOrientMode() {
        return mBackOrientFlag;
    }

    //
    public void setBackFillPos(int pos) {
        mFillposCursor = 0;
    }

    // 开始回退模式
    public void beginBackOrient() {
        DebugUtil.i(TAG, "1beginBackOrient enter!");
        mBackOrientFlag = true;
        mHadBackScans = 1;
        // 发送读取一道参数
        setReadOneScanSpeedParams();
        MyApplication app;
        app = (MyApplication) (mContext).getApplicationContext();
        app.setRealthreadSleepTime(50);
        DebugUtil.i(TAG, "2finish beginBackOrient!");
    }

    // 退出回退模式
    public void endBackOrient1() {
        DebugUtil.i(TAG, "3endBackOrient1 enter!");
        mBackOrientFlag = false;
        mHadBackScans = 0;
        // 恢复读取道数
        setReadScansParams();
        MyApplication app;
        app = (MyApplication) (mContext).getApplicationContext();
        app.setRealthreadSleepTime(200);
        DebugUtil.i(TAG, "4finish beginBackOrient!");
    }

    // 退出回退模式
    public void endBackOrient() {
        // 回退数据指针，使得回退数据无效
        int needBackLength = mHadBackScans * mScanLength * 2;
        int bufIndex = mBufIndex;
        int wPos = mNowWPos[bufIndex];
        // 要会退的数据在最后一个缓冲区内
        if (wPos >= needBackLength) {
            mNowWPos[bufIndex] = wPos - needBackLength;
        } else
        // 此时需要切换到上一个缓冲区
        {
            // 复位当前缓冲区
            mNowWPos[bufIndex] = 0;
            needBackLength = needBackLength - wPos;
            /*
             * //切换到前一个缓冲区 bufIndex = bufIndex-1; if(bufIndex<0) bufIndex =
             * mBufsNumber-1; // mBufIndex = bufIndex; mNowWPos[bufIndex] =
             * mNowWPos[bufIndex]-needBackLength;
             */
            // 如果正在保存数据，此时应该却掉会退的数据
            if (isSavingMode()) {

            }
        }

        mBackOrientFlag = false;
        //
        mHadBackScans = 0;
    }

    private SendTransfer mDataTransfer;

    public void setDataTransfer(SendTransfer t) {
        mDataTransfer = t;
    }

    // 读取数据
    private short temScanData = -0x3fff;

    private short[] positiveDatas = null;

    private volatile boolean mUpload = false;
    private volatile boolean mUploading = false;

    public void setUpload(boolean upload) { mUpload = upload; }

    public int readDatas(short[] Bufs, int size) {
        // Log.d("debug_read_datas", "read data");
        int rLen = 0;
        Bufs[0] = 0;
        Bufs[1] = 0;
        long st, et;
        st = System.currentTimeMillis();
        rLen = readData(Bufs, size);
        et = System.currentTimeMillis();

        if (rLen <= 0)
            return 0;

        // total_time += (et - st);

        // if( rLen > 0 )
        // DebugUtil.i(TAG,"This read data length:="+rLen);
        // else;

        // //如果处于暂停保存模式，直接返回
        if (isSavingMode() && (isTemstopSaveMode()))
            return 0;

        // 打印数据
        // for(int i = 0; i < 10;i++)
        // {
        // DebugUtil.i(TAG,"数据" + i + String.valueOf(Bufs[i]));
        // }

        // //模拟读取数据
        /*
         * int needScan = 10; //// if(!isRunningMode()) //{ //mNowMode &=
         * (~0xF0); //mNowMode |= RADARDEVICE_WHEEL; rLen =
         * mScanLength*2*needScan; for(int i=0;i<needScan;i++) { for(int
         * j=0;j<mScanLength;j++) { Bufs[j+i*mScanLength]=(short) (temScanData);
         * temScanData++; if(temScanData>0x3fff) temScanData = -0x3fff; }
         * Bufs[i*mScanLength] = 0x7fff; // Bufs[i*mScanLength+1] =
         * NEGATIVE_FLAG; Bufs[i*mScanLength+1] = (short)
         * (POSITIVE_FLAG+0x0001); }
         */
        // }
        // else
        // {
        // rLen = readOneWave(Bufs,size);
        // }

        // //对标记进行处理
        boolean isDiance; // 是否是点测
        int i;
        int j;
        int rScans; // 要处理的雷达数据
        short flagVal; // 标记
        int positiveScans = 0; // 正转的数据道数
        int negativeScans = 0; // 反转的数据道数
        boolean isSaveManage = false;
//		short[] positiveDatas = new short[rLen / 2];
        short flag;
        isDiance = isDianCeMode();
        rScans = rLen / 2 / mScanLength;

        boolean isConnected = ((MultiModeLifeSearchActivity) mContext).getNetwork().isConnected();

        Uploader uploader = ((MultiModeLifeSearchActivity) mContext).getNetwork().getUploader();
        if (mUpload && isConnected) {
            if (!mUploading) {
                mUploading = true;
                uploader.putFileHeader(mFileHeader);
            }
            uploader.putData(Bufs, rLen / 2, (short) mScanLength);
            Log.d("debug", "allocated blocks: " + uploader.allocatedBlocks());
            st = System.nanoTime();
            uploader.shrinkAll(10, 5);
            Log.d("debug", "shrinkAll: " + (System.nanoTime() - st) / 1000000);
            Log.d("debug", "after: " + uploader.allocatedBlocks());
        } else if (!mUpload) {
            if (mUploading) {
                mUploading = false;
                Log.d("debug", "send upload end");
                uploader.putUploadEnd();
            }
        }

        // 判断是否是手柄保存键
        for (i = 0; i < rScans; i++) {
            flag = Bufs[i * mScanLength + SAVEFLAG_INDEX];
            /*
             * //是保存键 if(flag == HANDLEKEY_SAVE) { isSaveManage = true; } //是标记键
             * if(flag == HANDLEKEY_MARK) { //在对应的数据道上设置标记
             * if(!isWhellMode()&&!isDiance) Bufs[i*mScanLength+FLAG_INDEX] |=
             * 0x4000; }
             */
        }

        MyApplication app;
        app = (MyApplication) (mContext).getApplicationContext();
        int srcW;
        srcW = app.getScreenWidth(); // 显示屏宽度
        if (this.isDianCeMode()) {
            // 对每一道数据进行处理：处理标记和超速等
            for (i = 0; i < rScans; i++) {
                Bufs[i * mScanLength + FLAG_INDEX] = 0;
            }
        }
        // //轮测模式下处理 数据
        else if (isWhellMode()) {
            // 没有读取到数据，说明测距论已经停止
            if (rScans == 0) {
                // 超速标记清零
                mOverspeedFlag = false;
            }

            // 对每一道数据进行处理：处理标记和超速等
            for (i = 0; i < rScans; i++) {
                short negativeFlag = NEGATIVE_FLAG;
                short positiveFlag = POSITIVE_FLAG;
                // 得到标记
                flagVal = Bufs[i * mScanLength + FLAG_INDEX];

                // 得到手柄标记
                // flag = Bufs[i*mScanLength+SAVEFLAG_INDEX];
                // 标记清零，轮测模式下不显示标记
                Bufs[i * mScanLength + FLAG_INDEX] = 0;
                /*
                 * //用手柄打标，说明用户在轮测模式下打标记录，此时设置标记值 if((flag == HANDLEKEY_MARK))
                 * Bufs[i*mScanLength+FLAG_INDEX] |= POSITIVE_FLAG;
                 */

                // 检测是否超速：只要有一道数据存在超速标志，即超速
                if ((flagVal & OVERSPEED_FLAG) != 0) {
                    mOverspeedFlag = true;
                    DebugUtil.i(TAG, "mOverspeedFlag 为true！");
                } else {
                    mOverspeedFlag = false;
                }

                // 如果翻转标记,重设标记对应值
                if (mIsTurnWhell) {
                    negativeFlag = POSITIVE_FLAG;
                    positiveFlag = NEGATIVE_FLAG;
                }
                // 反转
                if ((flagVal & negativeFlag) != 0) {
                    // 反转数据道计数加一
                    negativeScans++;
                    continue;
                }
                // 正转
                else if ((flagVal & positiveFlag) != 0) {
                    // 记录正向数据
                    for (j = 0; j < mScanLength; j++)
                        positiveDatas[positiveScans * mScanLength + j] = Bufs[i * mScanLength + j];
                    // 正转数据道计数加一
                    positiveScans++;
                    continue;
                }
                // 如果正反标志都没有则不读取
                else
                    return 0;
            }
            // 反向的数据道数 > 正向道数:此时回退定位模式
            if (negativeScans > positiveScans) {
                // 已经是回退定位模式，此时增加回退道数计数
                if (isBackOrientMode()) {
                    mHadBackScans += (negativeScans - positiveScans);
                } else
                // 还没有进入回退定位模式
                {
                    // 进入回退定位模式
                    beginBackOrient();
                    // 设置回退道数计数值
                    mHadBackScans = (negativeScans - positiveScans);
                }
                // //设置屏幕显示的回退线位置
                mFillposCursor += (negativeScans - positiveScans);
                // 回退线最多到屏幕的左端
                if (mFillposCursor > srcW)
                    mFillposCursor = srcW;
            }
            // 反向数据道数小于等于正向数据道数(总体正向滚动)
            else {
                // 回退定位模式下
                if (isBackOrientMode()) {
                    // 更改回退道数计数值
                    mHadBackScans = mHadBackScans - (positiveScans - negativeScans);
                    // 减小回退线位置
                    delFillposCursor(positiveScans - negativeScans);
                    // 如果回退道数计数已经<=0,说明回退结束
                    if (mHadBackScans <= 0) {
                        // 结束回退操作
                        endBackOrient1();
                        mHadBackScans = 0;
                    } else
                    // 回退还没有结束，此时直接返回
                    {
                        // 此时是用户没有选择“丢弃回退数据”，原来数据有效，无需覆盖
                        return 0;
                    }
                }
                // ///不处于回退定位模式或者回退定位结束，
                // 此时如果同时存在 正转和反转 ,有效数据为正转与反转数据之差
                if (negativeScans > 0) {
                    // 重新填充数据，并设置读取数据长度
                    int num;
                    num = positiveScans - negativeScans;
                    rLen = num * mScanLength * 2;
                    for (int m = 0; m < num; m++) {
                        for (int mm = 0; mm < mScanLength; mm++) {
                            Bufs[m * mScanLength + mm] = positiveDatas[
                                    (negativeScans + m) * mScanLength + mm];
                        }
                    }
                    // //不是回退定位模式，或者在回退定位后用户选择了“丢弃回退数据”选项，此时要减小 回退线位置
                    delFillposCursor(num);
                }
                /*
                 * ////对 屏幕打标 进行处理 if(mNeedSmallMark) { Bufs[FLAG_INDEX] =
                 * NEGATIVE_FLAG; mNeedSmallMark = false; } if(mNeedBigMark) {
                 * Bufs[FLAG_INDEX] = POSITIVE_FLAG; mNeedBigMark = false; }
                 */
            }
            // //判断是否是回退操作
            if (mHadBackScans > 0) {
                // 记录当前的填充位置
                return 0;
            }
        }

        // //如果没有得到数据
        if (rLen <= 0)
            return 0;
        // //对 屏幕打标 进行处理
        if (mNeedSmallMark) {
            Bufs[FLAG_INDEX] = NEGATIVE_FLAG;
            mNeedSmallMark = false;
        }
        if (mNeedBigMark) {
            Bufs[FLAG_INDEX] = POSITIVE_FLAG;
            mNeedBigMark = false;
        }
        RadarDetect radarDetect = ((MyApplication) (mContext.getApplicationContext())).mRadarDetect;
        radarDetect.setStoragePath(mStoragePath);
        radarDetect.setData(Bufs, 0, rLen / 2, mScanLength);

        // 保存当前一道数据，用于在单道波形视图上显示
        int shortLen = rLen / 2;
        int k;
        if (!isTempstopShow()) {
            if (shortLen >= mScanLength) {
                for (k = 0; k < mScanLength; k++) {
                    mOneScanDatas[k] = Bufs[k + (shortLen - mScanLength)];
                }
            }
        } else
            ;
        /*
         * //检测是否用户点击了 操作手柄 的保存按键 if(isSaveManage) { manageHandleSaveKey(); }
         */
        // 将数据拷贝到缓冲区中
        if (isWhellMode()) {
            // 如果还没有补足以前的回退定位数据
            if (shortLen / mScanLength > mFillposCursor)
                mHadRcvScans += (shortLen / mScanLength - mFillposCursor);
        } else // 连续模式
        {
            mHadRcvScans += shortLen / mScanLength;
        }

        // //将数据填充到数据缓冲区，并进行缓冲区切换和保存数据操作
        int bufIndex;
        int wPos;
        int wLength;
        int addNum;
        addNum = 0;
        bufIndex = mBufIndex; // 当前缓冲区索引
        wPos = mNowWPos[bufIndex]; // 当前缓冲区写入位置
        wLength = rLen; // 要写入的数据长度

        // 向缓冲区中填充数据
        if (wPos + wLength > mBufLength) {
            wLength = mBufLength - wPos;
            for (i = 0; i < wLength / 2; i++) {
                mDatasBufs[bufIndex][wPos + i * 2] = (byte) (Bufs[i]);
                mDatasBufs[bufIndex][wPos + i * 2 + 1] = (byte) (Bufs[i] >> 8);
            }
            // 缓冲区已满，如处于保存模式，此时保存数据
            if (isSavingMode() && (!isTemstopSaveMode())) {
                SaveDatasToSDCard1(bufIndex);
                // SaveGPSRecord();
            }

            mNowWPos[bufIndex] = mBufLength;
            addNum = wLength;
            //
            wLength = rLen - wLength;
            bufIndex++;
            bufIndex = bufIndex % mBufsNumber;
            mBufIndex = bufIndex;
            mNowWPos[bufIndex] = 0;
            wPos = 0;
        }
        // 拷贝剩余的数据
        if (wLength < 0)
            wLength = 0;
        for (i = 0; i < wLength / 2; i++) {
            mDatasBufs[bufIndex][wPos + i * 2] = (byte) (Bufs[i + addNum / 2]);
            mDatasBufs[bufIndex][wPos + i * 2 + 1] = (byte) (Bufs[i + addNum / 2] >> 8);
        }
        mNowWPos[bufIndex] += wLength;

        // 如果正在进行无线控制，生成网络数据包

        // if(mWifiSendDatas)
        // app.mWifiDevice.sendDatas(Bufs,rLen);

        return rLen;
    }

    // 放弃回退数据
    public void discardBackDatas() {
        // 回退数据指针，使得回退数据无效
        int needBackLength = mHadBackScans * mScanLength * 2;
        int bufIndex = mBufIndex;
        int wPos = mNowWPos[bufIndex];
        // 要会退的数据在最后一个缓冲区内
        if (wPos >= needBackLength) {
            mNowWPos[bufIndex] = wPos - needBackLength;
        } else
        // 此时需要切换到上一个缓冲区
        {
            // 复位当前缓冲区,从当前位置开始
            mNowWPos[bufIndex] = 0;
            // 如果正在保存数据，此时应该却掉会退的数据
            if (isSavingMode()) {
                // 要抛弃的数据
                int discardLen;
                discardLen = needBackLength - wPos;
                RandomAccessFile file = null;
                try {
                    file = new RandomAccessFile(mSavingFilePath, "rw");
                } catch (FileNotFoundException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }
                try {
                    file.setLength(file.length() - discardLen);
                    file.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return;
                }
            }
        }

        // 回退定位结束
        mBackOrientFlag = false;
        mHadBackScans = 0;
    }

    // 处理保存按键
    public int REALTIME_HANDLEMSG_SAVE = 9;

    public void manageHandleSaveKey() {
        MyApplication app;
        app = (MyApplication) mContext.getApplicationContext();
        // app.mMainActivity.manageHandleSaveKey();
        /*
         * try { Message msg = new Message(); msg.obj = app.mMainActivity;
         * msg.arg1 = REALTIME_HANDLEMSG_SAVE; msg.arg2 = 1; //
         * app.mMainActivity.mRealtimeThreadHandler.sendMessage(msg); }
         * catch(Exception e) { DebugUtil.i(TAG,"RealtimeThread run fail_6!"); }
         */
    }

    // 判断是否暂停保存
    public boolean isTemstopSaveMode() {
        return ((mNowMode & RADARDEVICE_SAVING) != 0) && (mSaveMode == RADARDEVICE_SAVING_TEMSTOP);
    }

    // 打小标
    public void smallMark() {
        DebugUtil.i(TAG, "smallMark");
        /*
         * //// if(isFSAnten(mAntenFrqSel)) { FSAnten_smallMark(); return; }
         * if(!isCanSendCommand()) return; // short[] commands = new short[2];
         * commands[0] = IOCTL_CODE_FLAG; commands[1] = CODE_SET_SMALLMARK; //
         * sendCommands_1(commands,(short)4); if(isWhellMode()) mNeedSmallMark =
         * true;
         */
        mNeedSmallMark = true;
    }

    // 打大标
    boolean mNeedBigMark = false;
    boolean mNeedSmallMark = false;

    public void bigMark() {
        /*
         * if(isFSAnten(mAntenFrqSel)) { FSAnten_bigMark(); return; }
         * DebugUtil.i(TAG,"bigMark"); if(!isCanSendCommand()) return; //
         * short[] commands = new short[2]; commands[0] = IOCTL_CODE_FLAG;
         * commands[1] = CODE_SET_BIGMARK; // sendCommands_1(commands,(short)4);
         * if(isWhellMode()) mNeedBigMark = true;
         */
        mNeedBigMark = true;
    }

    // 继续保存
    public void continueSave() {
        mSaveMode = RADARDEVICE_SAVING_CONTINUE;
    }

    // 暂停保存
    public void tempStopSave() {
        mSaveMode = RADARDEVICE_SAVING_TEMSTOP;
    }

    // 保存GPS信息
    public void SaveGPSRecord() {
        MyApplication app;
        app = (MyApplication) mContext.getApplicationContext();
        app.saveGPSRecord();
    }

    // 将一个缓冲区的数据保存到sd卡中
    public void SaveDatasToSDCard1(int bufIndex) {
        {
            RandomAccessFile file = null;
            try {
                file = new RandomAccessFile(mSavingFilePath, "rw");
            } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                DebugUtil.e(TAG, "file new error!");
                e1.printStackTrace();
                return;
            }
            try {
                file.seek(file.length());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                DebugUtil.e(TAG, "file seek error!");
                e.printStackTrace();
                return;
            }
            try {
                file.write(mDatasBufs[bufIndex]);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                DebugUtil.e(TAG, "file write error!");
                e.printStackTrace();
                return;
            }
            try {
                file.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                DebugUtil.e(TAG, "file close error!");
                e.printStackTrace();
                return;
            }
        }
    } // 进行最后一次数据保存

    public void lastSaveDatas1() {
        if (mExistSaveFile) {
            int bufIndex = mBufIndex;
            int pos = mNowWPos[bufIndex];

            RandomAccessFile file = null;
            try {
                file = new RandomAccessFile(mSavingFilePath, "rw");
            } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return;
            }
            try {
                file.seek(file.length());
            } catch (IOException e) {

                // TODO Auto-generated catch block
                e.printStackTrace();
                return;
            }
            try {
                file.write(mDatasBufs[bufIndex], 0, pos);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return;
            }
            try {
                file.close();
            } catch (IOException e) {

                // TODO Auto-generated catch block
                e.printStackTrace();
                return;
            }
        }
    }

    // 得到最近一道数据
    public short[] getRecentScanDatas() {
        //
        return mOneScanDatas;
    }

    // 判断是否可以发送命令(驱动程序是否已经打开)
    public boolean isCanSendCommand() {
        // DebugUtil.i(TAG,"Now isCanSendCommand:"+mNowMode);
        if (mNowMode == RADARDEVICE_NOOPEN)
            return false;
        //
        return true;
    }

    // 设置进入点测模式
    public boolean setDianceMode(int extNumber) {
        if (extNumber <= 0) {
            extNumber = 1;
        } else if (extNumber >= 32768) {
            extNumber = 32768;
        }

        mDianceNumber = extNumber;

        if (!isCanSendCommand())
            return false;
        // 停止雷达
        stopRadar();

        if (this.isWhellMode())
            setBackFillPos(0);
        else
            ;

        // 发送命令
        short[] commands = new short[4];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_DIANCEEXTNUMBER;
        commands[2] = 1;
        commands[3] = (short) mDianceNumber;
        sendCommands_1(commands, (short) 8);

        mNowMode &= (~0xF0);
        mNowMode |= RADARDEVICE_DIANCE;

        // 开启雷达
        mHadRcvScans = 0;
        startRadar();

        mFileHeader.setDianceMode();

        return true;
    }

    public void setDianceDistance(int distance) {
        mFileHeader.setDianceDistance(distance);
    }

    // 设置进入轮测模式
    public boolean setWheelMode(int extNumber) {
        mWheelExtendNumber = extNumber;

        if (!isCanSendCommand())
            return false;

        // 停止雷达
        stopRadar();
        // 发送读取道数
        setReadScansParams();

        // 发送命令
        short[] commands = new short[4];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_WHEELEXTNUMBER;
        commands[2] = 1;
        commands[3] = (short) mWheelExtendNumber;
        sendCommands_1(commands, (short) 8);

        mNowMode &= (~0xF0);
        mNowMode |= RADARDEVICE_WHEEL;

        // 恢复回退定位参数
        mBackOrientFlag = false;
        mHadBackScans = 0;

        // 开启雷达
        mHadRcvScans = 0;
        startRadar();

        // 头文件
        mFileHeader.setWhellMode();
        mFileHeader.setFlagExtent(extNumber);
        mFileHeader.setTouchDistance(
                mWheelInterDistance[mWheeltypeSel] * mWhellcheckCoeff[mWheeltypeSel]);

        return true;
    }

    // 设置回放模式
    public boolean setPlayBackMode() {
        mNowMode &= (~0xF0);
        mNowMode |= RADARDEVICE_PLAYBACK;
        return true;
    }

    public void turnWhell(boolean isTurn) {
        mIsTurnWhell = isTurn;
    }

    // 得到测距轮是否翻转的状态
    public boolean getTurnWheel() {
        return mIsTurnWhell;
    }

    // 设置扫速,（扫描速度取样点相乘的值不要大于256*1024）
    public void setScanSpeed(int selIndex) {
        if (selIndex < 0)
            selIndex = 0;
        else if (selIndex > g_scanSpeedNumber)
            selIndex = g_scanSpeedNumber;
        // 对参数进行合理性检验
        int scanSpeed = g_scanSpeed[selIndex];
        // 如果本次值与之前值相同
        if (scanSpeed == mScanSpeed)
            return;
        else
            ;

        // 判断值的范围
        if (scanSpeed * mScanLength <= MAXSPEEDSCANLENTH) {
            mScanSpeedSel = selIndex;
            mScanSpeed = scanSpeed;

            if (!isCanSendCommand())
                return;
            DebugUtil.i(TAG, "setScanSpeed:=" + mScanSpeed + "/s");
            sendScanSpeed();// 发送扫描速度
            mFileHeader.setScanspeed((short) mScanSpeed);
        } else {
            // DebugUtil.infoDialog(mContext, "提示", "参数超限!");
        }
    }

    public void directSetScanSpeed(short scanSpeed) {
        int scanSpeedSel = -1;
        for (int i = 0; i < g_scanSpeed.length; i++) {
            if (scanSpeed == g_scanSpeed[i]) {
                scanSpeedSel = i;
                break;
            }
        }
        if (scanSpeedSel == -1)
            throw new IllegalArgumentException("invalid scanSpeed");
        setScanSpeed(scanSpeedSel);
    }

    // 发送扫描速度相关指令
    private void sendScanSpeed() {
        DebugUtil.i(TAG, "mScanspeed = " + mScanSpeed);
        // //计算步进参数
        CalStepParams();
        // //发送步进参数
        // 停止雷达
        if (!isDianCeMode())
            stopRadar();
        // 设置信号位置参数
        setSignalParams();
        // 设置步进参数
        setStepParams();
        // 设置fad参数
        setFADParams();
        // 根据扫描速度，设置每次采集的道数
        setReadScansParams();
        // 开启雷达
        if (!isDianCeMode())
            startRadar();
        mHadRcvScans = 0;
    }

    // 调节增益时发送的扫描速度
    public void send64ScanSpeed() {
        DebugUtil.i(TAG, "enter send64ScanSpeed!");
        if (this.mScanSpeed <= 64)
            ;
        else {
            int preScanSpeed = this.mScanSpeed;
            mScanSpeed = 64;
            sendScanSpeed();
            mScanSpeed = preScanSpeed;
        }
    }

    // 增益调节结束，恢复扫描速度
    public void resetScanSpeed() {
        DebugUtil.i(TAG, "enter resetScanSpeed");
        sendScanSpeed();
    }

    public void setContinueScanSpeedValue(int speed) {
        mScanSpeed = speed;
    }

    // 设置连续测量的速度
    public void setContinueScanSpeed(int speed) {
        // 如果没变化就不发参数
        if (mScanSpeed == speed)
            return;
        else
            ;

        mScanSpeed = speed;
        DebugUtil.i(TAG, "setScanSpeed:=" + mScanSpeed + "/s");
        // //如果是鞭状天线

        if (!isCanSendCommand())
            return;

        // //计算步进参数
        CalStepParams();
        // //发送步进参数
        // 停止雷达
        if (!isDianCeMode())
            stopRadar();
        // 设置取样道长参数
        setScanLenParams();
        // 设置信号位置参数
        setSignalParams();
        // 设置步进参数
        setStepParams();
        // 设置fad参数
        setFADParams();
        // 设置扫速
        setReadScansParams();
        // 开启雷达
        mHadRcvScans = 0;
        if (!isDianCeMode())
            startRadar();

        mFileHeader.setScanspeed((short) mScanSpeed);
        MultiModeLifeSearchActivity activity;
        activity = (MultiModeLifeSearchActivity) mContext;
        // activity.setNowSpeedRange();
    }

    // 仅仅设置信号位置的值
    public void setSignalposValueOnly(int pos) {
        mSignalPos = pos;
    }

    // 设置信号位置
    public void setSignalpos(int pos) {
        mSignalPos = pos;

        if (!isCanSendCommand())
            return;
        // //计算步进参数
        CalStepParams();
        // //发送步进参数
        // 停止雷达
        DebugUtil.i(TAG, "1.setSignalpos stopRadar");
        if (!isDianCeMode())
            stopRadar();
        DebugUtil.i(TAG, "2.setSignalpos stopRadar setSignalParams begin");
        setSignalParams();
        DebugUtil.i(TAG, "3.setSignalpos setSignalParams finish");
        // 开启雷达
        mHadRcvScans = 0;
        DebugUtil.i(TAG, "4.setSignalpos startradar begin");
        if (!isDianCeMode()) {
            startRadar();
            // 重复一次开始命令，防止出现死机
            // startRadar();
        }
        DebugUtil.i(TAG, "5.setSignalpos startradar finish");
    }

    // 设置取样点数值，不发命令
    // 判断扫速与取样点的乘是否小于MAXSPEEDSCANLENTH
    public void setScanLengthValue(int selIndex) {
        // 判断值范围
        int scanLength = g_scanLen[selIndex];
        if (scanLength * mScanSpeed <= MAXSPEEDSCANLENTH) {
            mScanLengthSel = selIndex;
            mScanLength = scanLength;
            mFileHeader.rh_nsamp = (short) mScanLength;
        } else {
            // DebugUtil.infoDialog(mContext, "提示", "请先调低扫描速度!");
        }
    }

    public void directSetScanLength(short scanLength) {
        boolean isValid = false;
        int scanLengthSel = mScanLengthSel;
        for (int i = 0; i < g_scanLen.length; i++) {
            if (scanLength == g_scanLen[i]) {
                isValid = true;
                scanLengthSel = i;
                break;
            }
        }
        if (!isValid)
            throw new IllegalArgumentException("invalid scanLength: " + scanLength);
        setScanLength(scanLengthSel);
    }

    // 设置取样点数
    public void setScanLength(int selIndex) {
        if (mScanLength == g_scanLen[selIndex])
            return;
        else
            ;
        setScanLengthValue(selIndex);
        // //计算步进参数
        CalStepParams();
        // //发送步进参数
        // 停止雷达
        if (!isDianCeMode())
            stopRadar();
        // 设置取样道长参数
        setScanLenParams();
        // 设置信号位置参数
        setSignalParams();
        // 设置步进参数
        setStepParams();
        // 设置fad参数
        setFADParams();

        // 开启雷达
        mHadRcvScans = 0;
        if (!isDianCeMode())
            startRadar();
    }

    // 自动增益
    public boolean autoPlus() {
        // 自动增益
        short[] autoPlus = new short[12];
        int i = 0;

        for (i = 0; i < 9; i++) {
            mBackHardPlus[i] = mHardPlus[i];
        }
        for (i = 0; i < 12; i++) {
            autoPlus[i] = 0;
        }
        autoPlus[0] = IOCTL_CODE_FLAG;
        autoPlus[1] = CODE_SET_AUTOPLUS;
        autoPlus[2] = 0;
        sendCommands_1(autoPlus, (short) 24);

        for (i = 0; i < 9; i++) {
            mHardPlus[i] = autoPlus[3 + i];
            DebugUtil.i(TAG, "0.Autoplus autoPlus[" + i + "]=" + autoPlus[i]);
            DebugUtil.i(TAG, "00.Autoplus mHardPlus[" + i + "]=" + mHardPlus[i]);
            if (mHardPlus[i] >= mMaxHardplus) {
                DebugUtil.i(TAG, "1.mHardPlus>=100");
                mHardPlus[i] = mMaxHardplus;
            } else if (mHardPlus[i] <= mMixHardplus) {
                DebugUtil.i(TAG, "2.mHardPlus<=-10");
                mHardPlus[i] = mMixHardplus;
            }
            DebugUtil.i(TAG, "3.Autoplus mHardPlus[" + i + "]=" + mHardPlus[i]);
        }

        // 开启雷达
        mHadRcvScans = 0;

        // if(!isDianCeMode())
        // startRadar();

        mIsAutoplus = true;
        DebugUtil.i(TAG, "mIsAutoplus:=" + mIsAutoplus);

        return true;
    }

    // 设置滤波参数
    public void setFilter(int selIndex) {
        mFilterSel = selIndex;
        if (mFilterSel > 1)
            mFilterSel = 1;
    }

    //
    public boolean isFilter() {
        if (mFilterSel == 0)
            return false;
        return true;
    }

    // 设置测距轮类型
    public void setWhellType(int type) {
        try {
            if (type > this.MWHEELMAXINDEX) {
                throw new Exception();
            } else
                ;
            mWheeltypeSel = type;
        } catch (Exception e) {
            DebugUtil.e(TAG, "setWhellType wheel index越界！");
        }
    }

    /**
     * 设置并发送滤波参数
     */
    public void setFilterParams() {
        if (!isCanSendCommand())
            return;
        short[] commands = new short[10];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_LVBO;
        commands[2] = 2;
        commands[3] = g_lvboParams[mFilterSel][0];
        commands[4] = g_lvboParams[mFilterSel][1];
        //
        // 停止雷达
        if (!isDianCeMode())
            stopRadar();
        sendCommands_1(commands, (short) 10);
        // 开启雷达
        mHadRcvScans = 0;
        if (!isDianCeMode())
            startRadar();
    }

    // 设置介电常数范围
    // 介电常数介于[1,100]
    public void setJieDianConst(float jdconst) {
        if (jdconst > 100) {
            jdconst = 100;
        } else if (jdconst < 1) {
            jdconst = 1;
        } else if (jdconst >= 1) {
            mJiedianConst = jdconst;
            mFileHeader.setJiedianConst(jdconst);
        } else
            ;
    }

    // 设置道间平均
    public void setScanAve(int ave) {
        // 限制取值范围
        if (ave > 500) {
            ave = 500;
        } else if (ave < 0) {
            ave = 1;
        }

        mScanAve = ave;

        if (!isCanSendCommand())
            return;
        // 停止雷达
        if (!isDianCeMode())
            stopRadar();

        // 设置背景消除
        short[] commands = new short[10];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_SCANAVE;
        commands[2] = 1;
        commands[3] = (short) mScanAve;

        sendCommands_1(commands, (short) 8);
        // 开启雷达
        mHadRcvScans = 0;
        if (!isDianCeMode())
            startRadar();
    }

    public boolean isAve() {
        if (mScanAve <= 1)
            return false;
        return true;
    }

    // 设置背景消除
    public void setRemoveBack(int selIndex) {
        mRemoveBackSel = selIndex;
    }

    public boolean isRemback() {
        if (mRemoveBackSel == 0)
            return false;
        return true;
    }

    public void setRemoveBackParams() {
        // if(isFSAnten(mAntenFrqSel))
        // {
        // FSAnten_setRemoveBackParams();
        // return;
        // }
        if (!isCanSendCommand())
            return;
        short isRem = mRemoveBackParams[mRemoveBackSel];
        // 停止雷达
        if (!isDianCeMode())
            stopRadar();

        // 设置背景消除
        short[] commands = new short[10];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_REMBACK;
        commands[2] = 1;
        commands[3] = isRem;
        //
        sendCommands_1(commands, (short) 8);
        // 开启雷达
        mHadRcvScans = 0;
        if (!isDianCeMode())
            startRadar();
    }

    // 设置天线主频
    public void setAntenFrq(int selIndex) {
        // 设置天线主频
        DebugUtil.i(TAG, "设置天线主频selIndex=" + selIndex);
        Log.d("debug", "selIndex: " + selIndex);
        mAntenFrqSel = selIndex;
        mFileHeader.rh_spp = g_antenFrq[mAntenFrqSel];
        mRepFrq = g_fixRepFrqNum[selIndex];
        // DebugUtil.i(TAG, "设置重复频率="+String.valueOf(mRepFrq));
        // DebugUtil.infoDialog(mContext, "重复频率",
        // "重复频率="+String.valueOf(mRepFrq));
    }

    // 设置默认雷达参数，在恢复默认雷达参数时使用
    public void setAntenFrqParams() {
        DebugUtil.i(TAG, "setAntenFrqParams!");

        if (!isCanSendCommand())
            return;

        short[] commands = new short[4];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_RADARFRQ;
        commands[2] = 1;
        commands[3] = g_antenFrq[mAntenFrqSel];
        DebugUtil.i(TAG, "setAntenFrqParams=" + g_antenFrq[mAntenFrqSel]);

        if (!isDianCeMode())
            stopRadar();
        mHadRcvScans = 0;
        sendCommands_1(commands, (short) 8);

        /*
         * CalStepParams(); setScanLenParams(); setSignalParams();
         * setStepParams(); setFADParams();
         */

        // 设置硬件增益范围值
        commands[1] = CODE_SET_HARDPLUSRANGE;
        commands[2] = 1;
        commands[3] = g_hardplusrange[mAntenFrqSel];
        sendCommands_1(commands, (short) 8);

        if (!isDianCeMode())
            startRadar();
    }

    // /只设置道长参数
    public void setScanLenParams() {

        if (!isCanSendCommand())
            return;
        short[] commands = new short[4];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_SCANLEN;
        commands[2] = 1;
        commands[3] = (short) mScanLength;
        //
        sendCommands_1(commands, (short) 8);
    }

    // /设置每次读取的道数
    public void setReadScansParams() {
        if (!isCanSendCommand())
            return;
        short[] commands = new short[4];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_READSCANS;
        commands[2] = 1;
        short scans = 1;
        do {
            if (mScanSpeed <= 256)// 500)
            {
                scans = 4;// 4;
                break;
            }
            if (mScanSpeed <= 1000) {
                scans = 8;// 4;
                break;
            }

            if (mScanSpeed <= 2000) {
                scans = 16;
                break;
            }

            scans = 32;
        } while (false);

        commands[3] = scans;

        DebugUtil.i(TAG, "setReadScansParams scans=" + scans + "before send!");
        sendCommands_1(commands, (short) 8);
        DebugUtil.i(TAG, "send scans finish!");
    }

    // 设置读取道数为1,在回退的时候使用
    public boolean setReadOneScanSpeedParams() {
        if (!isCanSendCommand())
            return false;
        short[] commands = new short[4];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_READSCANS;
        commands[2] = 1;
        commands[3] = 1;
        if (sendCommands_1(commands, (short) 8) > 0)
            return true;
        else
            return false;
    }

    // /只设置步进参数
    public void setStepParams() {

        if (!isCanSendCommand())
            return;
        short[] commands = new short[8];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_STEP;
        commands[2] = 5;
        commands[3] = mStepParams[0];
        commands[4] = mStepParams[1];
        commands[5] = mStepParams[2];
        commands[6] = mStepParams[3];
        commands[7] = mStepParams[4];

        // DebugUtil.infoDialog(mContext, "重复频率=",
        // String.valueOf(mStepParams[3]));

        sendCommands_1(commands, (short) 16);
    }

    // /发送设备的当前状态
    private int mStatusLength = 20; //

    public void sendNowSystemStatus() {
        MyApplication app;
        app = (MyApplication) mContext.getApplicationContext();
        // if(!app.mWifiDevice.isSndHadConnect())
        // return;
        // ////生成当前雷达状态的数据包
        // NetPacket pack = new NetPacket();
        short[] sendBuf = new short[mStatusLength];
        sendBuf[0] = (short) mAntenFrqSel;
        sendBuf[1] = (short) mScanSpeed;
        sendBuf[2] = (short) mTimeWindow;
        sendBuf[3] = (short) mSignalPos;
        sendBuf[4] = (short) mScanLength;
        sendBuf[5] = (short) mFilterSel;
        sendBuf[6] = mRemoveBackParams[mRemoveBackSel];
        sendBuf[7] = (short) mScanAve;
        if (this.isSavingMode())
            sendBuf[8] = 1;
        else
            sendBuf[8] = 0;
        if (this.isWhellMode())
            sendBuf[9] = (short) RADARDEVICE_WHEEL;
        if (this.isDianCeMode())
            sendBuf[9] = (short) RADARDEVICE_DIANCE;
        if (this.isTimeMode())
            sendBuf[9] = (short) RADARDEVICE_CONTINUE;
        int i;
        for (i = 0; i < 9; i++)
            sendBuf[10 + i] = (short) mHardPlus[i];
        if (this.isRunningMode())
            sendBuf[19] = (short) 1;
        else
            sendBuf[19] = (short) 0;
        // pack.setDatas(sendBuf,mStatusLength*2);
        // pack.createSystemStatusPacket();
        // app.mWifiDevice.addSendPacket(pack);
    }

    // /只设置信号位置参数
    public void setSignalParams() {
        if (!isCanSendCommand())
            return;
        short[] commands = new short[11];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_SIGNALPOS;
        commands[2] = 8;
        for (int i = 0; i < 8; i++)
            commands[3 + i] = mSignalPosParams[i];

        sendCommands_1(commands, (short) 22);
        for (int i = 0; i < 11; i++) {
            DebugUtil.i("setSignalParams", "commands[" + i + "]=" + commands[i]);
        }
    }

    public void setFADParams() {
        if (!isCanSendCommand())
            return;
        short[] commands = new short[4];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_FAD;
        commands[2] = 1;
        commands[3] = mFAD;
        //
        sendCommands_1(commands, (short) 8);
        DebugUtil.i("mFAD", "commands[3]=" + commands[3]);
    }

    public void setHardplusParams() {
        if (!isCanSendCommand())
            return;
        short[] commands = new short[12];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_HARDPLUS;
        commands[2] = 9;
        for (int i = 0; i < 9; i++) {
            commands[3 + i] = mHardplusParams[i];
            // DebugUtil.i(TAG,
            // "send mHardplusParams"+String.valueOf(mHardplusParams[i]));
        }

        sendCommands_1(commands, (short) 24);
    }

    //
    public void setContext(Context context) {
        mContext = context;
    }

    public void newPositiveData() {
        MultiModeLifeSearchActivity activity = (MultiModeLifeSearchActivity) mContext;
        MyApplication app = (MyApplication) (activity.getApplicationContext());
        positiveDatas = new short[app.getOnceMaxReadLength()];
    }

    // 根据索引生成新文件名
    public String createNewFileName_ByIndex(int index) {
        String fileName;
        // int index=1;
        do {
            fileName = "ltefile" + index + ".lte";
            fileName = mStoragePath // android.os.Environment.getExternalStorageDirectory()
                       + mLTEFilefolderPath + fileName;
            File file = new File(fileName);
            if (!file.exists())
                break;
            index++;
        } while (true);

        mNowFileindex = index;
        DebugUtil.i(TAG, "createNewFileName:" + fileName);
        return fileName;
    }

    //
    public void setRadardatasFolderpath(String path) {
        mLTEFilefolderPath = path;
    }

    public String getRadardatasFolderpath() {
        return mLTEFilefolderPath;
    }

    // 生成新的数据文件名
    public String createNewFileName() {
        // 搜索所有的文件名中数字最大的那个
        String folderPath;
        int index = 1;
        folderPath = mStoragePath + mLTEFilefolderPath;
        File lteDir = new File(folderPath);
        if (!lteDir.exists()) {
            lteDir.mkdirs();
        }
        File[] files = lteDir.listFiles();
        if (files != null) {
            for (File currentFile : files) {
                // 判断是一个文件夹还是一个文件
                if (currentFile.isDirectory()) {
                } else {
                    // 取得文件名
                    String fileName = currentFile.getName();
                    if (fileName.endsWith("lte") || fileName.endsWith("LTE")) {
                        int pos1;
                        int pos2;
                        pos1 = fileName.lastIndexOf("ltefile");
                        pos2 = fileName.lastIndexOf('.');
                        String indexStr;
                        indexStr = fileName.substring(pos1 + 7, pos2);
                        int subIndex = 0;
                        for (int i = 0; i < indexStr.length(); i++) {
                            char c = indexStr.charAt(i);
                            subIndex = subIndex * 10 + (c - '0');
                        }
                        if (subIndex >= index)
                            index = subIndex + 1;
                    }
                }
            }
        }
        if (mCreateNewFileType == CREATE_NEWFILE_TYPEINDEX)
            return createNewFileName_ByIndex(index);
        // if(mCreateNewFileType == CREATE_NEWFILE_TYPETIME)
        // return createNewFileName_ByTime();
        return "";
    }

    // 生成新的保存数据文件
    public boolean createNewDatasFile() {
        // 生成文件夹
        String folderPath;
        folderPath = mStoragePath // android.os.Environment.getExternalStorageDirectory()
                     + mLTEFilefolderPath;
        File lteDir = new File(folderPath);
        if (!lteDir.exists()) {
            lteDir.mkdirs();
        }

        // 生成保存文件句柄
        if (!mExistSaveFile) {
            try {
                String fileName = createNewFileName();
                fSaveOS = new FileOutputStream(fileName);
                mSavingFilePath = fileName;

                mExistSaveFile = true;
                refreshFileHeader();
                mFileHeader.save(fSaveOS);

                fSaveOS.close();
            } catch (Exception e) {
                DebugUtil.i(TAG, "createNewDatasFile fail!");
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    // 得到保存数据文件名
    public String getSaveFilename() {
        return mSavingFilePath;
    }

    // 得到当前的状态置
    public int getNowMode() {
        return mNowMode;
    }

    // 设置模式状态
    public void setMode(int inputMode) {
        mNowMode &= (~0xF0);
        mNowMode |= inputMode;
    }

    // 判断是否是轮测模式
    public boolean isWhellMode() {
        if ((mNowMode & RADARDEVICE_WHEEL) == RADARDEVICE_WHEEL)
            return true;
        return false;
    }

    // 判断是否是时间模式
    public boolean isTimeMode() {
        if ((mNowMode & RADARDEVICE_CONTINUE) == RADARDEVICE_CONTINUE)
            return true;
        return false;
    }

    // 判断是否是点测模式
    public boolean isDianCeMode() {
        if ((mNowMode & RADARDEVICE_DIANCE) == RADARDEVICE_DIANCE)
            return true;
        return false;
    }

    // 判断是否是回放模式
    public boolean isPlayBackMode() {
        if ((mNowMode & RADARDEVICE_PLAYBACK) == RADARDEVICE_PLAYBACK)
            return true;
        return false;
    }

    // 发送一次点测命令
    public void onceDianCe() {
        // 停止雷达
        // stopRadar();

        // 发送命令
        short[] commands = new short[3];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_ONCEDIANCE;
        commands[2] = (short) mDianceNumber;
        //
        sendCommands_1(commands, (short) 6);

        //
        mNowMode &= (~0xF0);
        mNowMode |= RADARDEVICE_DIANCE;

        // 开启雷达
        // startRadar();
    }

    //
    public void beginWifiSendDatas() {
        mWifiSendDatas = true;
        // 开启wifi发送线程

    }

    public void stopWifiSendDatas() {
        mWifiSendDatas = false;
        // 停止wifi发送线程
    }

    // //处理网络命令
    // public void manageNetCommand(NetCommand comObj)
    // {
    // short code;
    // short[] params;
    // short val;
    // LTDMainActivity activity;
    // activity = (LTDMainActivity)mContext;
    // params = comObj.getCommandParams();
    // code = comObj.getCommandCode();
    // switch(code)
    // {
    // //开始保存数据
    // case RADAR_COMMAND_BEGSAVE:
    // {
    // activity.beginSave();
    // break;
    // }
    // //停止保存数据
    // case RADAR_COMMAND_ENDSAVE:
    // {
    // activity.stopSave();
    // break;
    // }
    // //信号位置
    // case RADAR_COMMAND_SIGNALPOS:
    // {
    // int val1 = params[0];
    // short fu = params[1];
    // if(fu==0)
    // val1 = -val1;
    // setSignalpos(val1);
    // stopRadar();
    // CalStepParams();
    // setSignalParams();
    // startRadar();
    // //重新设置信号位置显示值
    // activity.setRealtimeParamsListAdapterText(val1,
    // activity.COMMAND_SIGNALPOS_ID);
    // break;
    // }
    // //时窗
    // case RADAR_COMMAND_TIMEWND:
    // {
    // val = params[0];
    // setTimeWindow(val,false);
    // //
    // activity.setRealtimeParamsListAdapterText(val,
    // activity.COMMAND_TIMEWINDOW_ID);
    // activity.setRealtimeParamsListAdapterText(val,
    // activity.COMMAND_PROTIMEWINDOW_ID);
    // break;
    // }
    // //扫速
    // case RADAR_COMMAND_SCANSPEED:
    // {
    // val = params[0];
    // setContinueScanSpeed(val);
    // //
    // activity.setRealtimeParamsListAdapterText(val,
    // activity.COMMAND_SCANSPEED_ID);
    // break;
    // }
    // //扫描点数
    // case RADAR_COMMAND_SAMPLEN:
    // {
    // val = params[0];
    // setScanLength(val);
    // stopRadar();
    // CalStepParams();
    // setScanLenParams();
    // startRadar();
    // //
    // activity.setRealtimeParamsListAdapterText(val,
    // activity.COMMAND_SCANLENGTH_ID);
    // break;
    // }
    // //滤波
    // case RADAR_COMMAND_LVBO:
    // {
    // val = params[0];
    // setFilter(val);
    // stopRadar();
    // setFilterParams();
    // startRadar();
    // //
    // activity.setRealtimeParamsListAdapterText(val,
    // activity.COMMAND_FILTER_ID);
    // break;
    // }
    // //增益调节
    // case RADAR_COMMAND_HARDPLUS:
    // {
    // float[] plus = new float[9];
    // val = params[0];
    // int i;
    // for(i=0;i<9;i++)
    // plus[i]=params[i];
    // setHardplus(plus);
    // stopRadar();
    // setHardplusParams();
    // startRadar();
    // //
    // activity.setRealtimeParamsListAdapterText(val,
    // activity.COMMAND_ALLHARDPLUS_ID);
    // break;
    // }
    // //天线频率
    // case RADAR_COMMAND_SETRADARFRQ:
    // {
    // val = params[0];
    // setAntenFrq(val);
    // setAntenFrqParams();
    // //
    // activity.setRealtimeParamsListAdapterText(val,
    // activity.COMMAND_ANTENFRQ_ID);
    // break;
    // }
    // //开启雷达
    // case RADAR_COMMAND_START:
    // {
    // startRadar();
    // break;
    // }
    // //停止雷达
    // case RADAR_COMMAND_STOP:
    // {
    // stopRadar();
    // break;
    // }
    // //设置平均次数
    // case RADAR_COMMAND_SETSCANAVE:
    // {
    // short scanAve = params[0];
    // activity.setRealtimeParamsListAdapterText(scanAve,activity.COMMAND_AVERAGE_ID);
    // activity.radarSetScanave(scanAve);
    // break;
    // }
    // //背景消除
    // case RADAR_COMMAND_REMBACKGROUND:
    // {
    // short isRemove = params[0];
    // activity.manageRemoveBack(isRemove);
    // break;
    // }
    // //连续工作模式
    // case RADAR_COMMAND_CONTINUEMODE:
    // {
    // activity.radarTimeMode();
    // break;
    // }
    // //测距轮工作模式
    // case RADAR_COMMAND_WHELLMODE:
    // {
    // val = params[0];
    // setWheelMode(val);
    // break;
    // }
    //
    // //点测工作模式
    // case RADAR_COMMAND_DIANCEMODE:
    // {
    // val = params[0];
    // setDianceMode(val);
    // String txt="Diance mode:"+val;
    // activity.showToastMsg(txt);
    // break;
    // }
    // //得到点测数据
    // case RADAR_COMMAND_GETDIANCEDATA:
    // {
    // activity.showToastMsg("Diance once get datas");
    // onceDianCe();
    // break;
    // }
    // }
    // }

    //
    public void changeParamsFromeAntenfrq(int index) {
        DebugUtil.i(TAG, "2.changeParamsFromeAntenfrq");
        mAntenFrqSel = index;
        mRepFrq = g_fixRepFrqNum[index];

        // 设置取样点数
        mScanLengthSel = g_defParamsForRadar[index][1];
        int scanLength = g_scanLen[mScanLengthSel];
        mScanLength = scanLength;
        mFileHeader.rh_nsamp = (short) mScanLength;

        // 设置扫速
        // mScanSpeedSel = g_defParamsForRadar[index][2];
        // int scanSpeed = g_scanSpeed[mScanSpeedSel];
        int scanSpeed = g_defParamsForRadar[index][2];
        mScanSpeed = scanSpeed;

        // 设置时窗
        mTimeWindow = g_defParamsForRadar[index][3];
        mFileHeader.rh_range = mTimeWindow;
        // 设置信号位置
        mSignalPos = g_defParamsForRadar[index][4];
        // 设置整体增益
        int i;
        for (i = 0; i < 9; i++) {
            mHardPlus[i] = g_defParamsForRadar[index][5];
        }
        // 设置零偏
        mZeroOff = g_defParamsForRadar[index][6];
        // 设置滤波
        mFilterSel = g_defParamsForRadar[index][7];
        // 设置默认测距仪
        mWheeltypeSel = g_defParamsForRadar[index][8];
        DebugUtil.i(TAG, "2.get default value!=" + mWheeltypeSel);
        mWheelExtendNumber = g_wheelextendnum[mWheeltypeSel];
        //
        // mScanAve = 1;

        // //
        /*
         * String txt; txt = "load params:"; for(i=0;i<8;i++) txt +=
         * g_defParamsForRadar[index][i]+";";
         * ((LTDMainActivity)mContext).showToastMsg(txt);
         */
    }

    /**
     * 文件加载失败在使用默认参数设置
     *
     * @param index
     */
    public void changeWheelPropertyFromAnteFrq(int index) {
        // 设置默认测距仪
        mWheeltypeSel = g_defParamsForRadar[index][8];
        // 设置默认标记扩展
        mWheelExtendNumber = g_wheelextendnum[mWheeltypeSel];
    }

    // 根据天线频率设置默认参数
    /*
     * index:天线频率索引值; channel:通道索引
     */
    public void setAntenDefaultParams(int index) {
        // 设置取样点数
        mScanLengthSel = g_defParamsForRadar[index][1];
        int scanLength = g_scanLen[mScanLengthSel];
        mScanLength = scanLength;
        mFileHeader.rh_nsamp = (short) mScanLength;
        // 设置扫速
        // mScanSpeedSel = g_defParamsForRadar[index][2];
        // int scanSpeed = g_scanSpeed[mScanSpeedSel];
        int scanSpeed = g_defParamsForRadar[index][2];
        mScanSpeed = scanSpeed;
        // 设置时窗
        mTimeWindow = g_defParamsForRadar[index][3];
        mFileHeader.rh_range = mTimeWindow;
        // 设置信号位置
        mSignalPos = g_defParamsForRadar[index][4];
        // 设置整体增益
        int i;
        for (i = 0; i < 9; i++) {
            mHardPlus[i] = g_defParamsForRadar[index][5];
        }
        // 设置零偏
        mZeroOff = g_defParamsForRadar[index][6];
        // 设置滤波
        mFilterSel = g_defParamsForRadar[index][7];
        // 设置默认测距仪
        mWheeltypeSel = g_defParamsForRadar[index][8];
        DebugUtil.i(TAG, "恢复参数mWheeltypeSel=" + mWheeltypeSel);

        if (!isCanSendCommand())
            return;
        // //停止雷达
        if (!isDianCeMode())
            stopRadar();
        // //计算步进参数
        // CalStepParams1();
        CalStepParams();
        // 设置步进参数
        short[] commands = new short[20];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_STEP;
        commands[2] = 5;
        commands[3] = mStepParams[0];
        commands[4] = mStepParams[1];
        commands[5] = mStepParams[2];
        commands[6] = mStepParams[3];
        commands[7] = mStepParams[4];
        sendCommands_1(commands, (short) 16);
        // 设置fad参数
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_FAD;
        commands[2] = 1;
        commands[3] = mFAD;
        sendCommands_1(commands, (short) 8);
        // 设置信号位置参数
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_SIGNALPOS;
        commands[2] = 8;
        for (i = 0; i < 8; i++)
            commands[3 + i] = mSignalPosParams[i];
        sendCommands_1(commands, (short) 22);
        // 设置滤波参数
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_LVBO;
        commands[2] = 2;
        commands[3] = g_lvboParams[mFilterSel][0];
        commands[4] = g_lvboParams[mFilterSel][1];
        sendCommands_1(commands, (short) 10);
        // 设置取样道长
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_SCANLEN;
        commands[2] = 1;
        commands[3] = (short) mScanLength;
        sendCommands_1(commands, (short) 8);
        // 计算硬增益，设置硬增益
        CalHardplusParams();
        setHardplusParams();
        // 已采集的道数
        mHadRcvScans = 0;
        // 道间平均
        mScanAve = 4;
        // 介电常数
        mJiedianConst = 9;// hss20161210
        // 保存位置
        mSelectStorageIndex = 0;
        setStoragePath(mSelectStorageIndex);
        // 开启雷达
        if (!isDianCeMode())
            startRadar();
    }

    //
    public long getMaxValue() {
        return 0x7148;
    }

    public long getMixValue() {
        return -0x7148;
    }

    // //////////////////鞭状天线命令
    // public void FSAnten_Start()
    // {
    // FSAntenCommand com = new FSAntenCommand();
    // com.m_IsRadarStart = 1;
    //
    // //生成网络数据包
    // FSAntenNetPacket packet = new FSAntenNetPacket();
    // packet.createCOMPacket(com);
    //
    // //将网络数据包加到发送队列中
    // LTDMainActivity activity;
    // MyApplication app;
    // activity = (LTDMainActivity)mContext;
    // app = (MyApplication)activity.getApplication();
    // app.mWifiDevice.addFSAntenSendPacket(packet);
    //
    // //设置模式
    // mNowMode = RADARDEVICE_REALTIME; //实时采集模式
    // mNowMode |= RADARDEVICE_CONTINUE; //连续采集模式
    //
    // //
    // mBufIndex = 0;
    // mRBufIndex = 0;
    // for(int i=0;i<mBufsNumber;i++)
    // {
    // mNowWPos[i] = mNowRPos[i] = 0;
    // }
    //
    // //
    // // mIsUseSoftPlus = true;
    // }

    // /////
    // public void FSAnten_Stop()
    // {
    // FSAntenCommand com = new FSAntenCommand();
    // com.m_IsRadarStop = 1;
    //
    // //生成网络数据包
    // FSAntenNetPacket packet = new FSAntenNetPacket();
    // packet.createCOMPacket(com);
    //
    // //将网络数据包加到发送队列中
    // LTDMainActivity activity;
    // MyApplication app;
    // activity = (LTDMainActivity)mContext;
    // app = (MyApplication)activity.getApplication();
    // // app.mWifiDevice.addFSAntenSendPacket(packet);
    //
    // //
    // mBufIndex = 0;
    // mRBufIndex = 0;
    // for(int i=0;i<mBufsNumber;i++)
    // {
    // mNowWPos[i] = mNowRPos[i] = 0;
    // }
    // mNowMode = RADARDEVICE_NOOPEN;
    //
    // mIsUseSoftPlus = false;
    // }

    // /////////////////////////数据处理方法
    public void FIRFilter() {
        int i, j, ii, k;
        double fs;
        double[] h = new double[1024];
        float Data; // 中间变量
        fs = mScanLength / 1.;
        fs = fs / mTimeWindow * 1000; // 雷达采样频率，转化为MHz
        double fln;
        double fhn;
        fln = g_antenFrq[mAntenFrqSel] / 4.;
        fhn = g_antenFrq[mAntenFrqSel] * 2;
        fln = fln / fs;
        fhn = fhn / fs;

        firwin(30, 3, fln, fhn, 1, h); // 函数调用计算滤波器的系数,滤波阶数30,都选择布莱克曼窗函数
        // for(i=0;i<10;i++)
        // DebugUtil.i(TAG,"^^^^The:"+i+"h:="+h[i]);
        for (i = 1; i < mScanLength; i++) {
            Data = 0;
            if (i < 15) {
                for (ii = 0; ii < i + 16; ii++) {
                    Data = (float) (Data + h[ii] * mResult[i - ii + 15]);
                }
            } else {
                for (ii = 0; ii <= 30; ii++) {
                    k = i - ii + 15;
                    if (k >= mScanLength - 1)
                        k = mScanLength - 1;
                    Data = (float) (Data + h[ii] * mResult[k]);
                }
            }

            mManageResult[i] = (short) Data;
        }
    }

    int firwin(int n, int band, double fln, double fhn, int wn,
               double h[]) // 因为要求滤波器系数精度高，所以这里取double型
    {
        int i, n2, mid;
        double s, pi, wc1, wc2, beta, delay;
        beta = 0.0;
        if (wn == 7) // 如果选的是凯塞窗则需要另外再输入一个窗函数参数beta
        {
            // scanf("%lf",&beta);
        }
        pi = 4.0 * Math.atan(1);
        if ((n % 2) == 0) {
            n2 = n / 2 - 1;
            mid = 1;
        } else {
            n2 = n / 2;
            mid = 0;
        }
        delay = n / 2.0;
        wc1 = 2.0 * pi * fln;
        wc2 = 2.0 * pi * fhn;
        if (band >= 3)
            wc2 = 2.0 * pi * fhn;
        switch (band) {
            case 1: {
                for (i = 0; i <= n2; i++) {
                    s = i - delay;
                    h[i] = (Math.sin(wc1 * s) / (pi * s)) * window(wn, n + 1, i, beta);
                    h[n - i] = h[i];
                }
                if (mid == 1)
                    h[n / 2] = wc1 / pi;
                break;
            }
            case 2: {
                for (i = 0; i <= n2; i++) {
                    s = i - delay;
                    h[i] = (Math.sin(pi * s) - Math.sin(wc1 * s)) / (pi * s);
                    h[i] = h[i] * window(wn, n + 1, i, beta);
                    h[n - i] = h[i];
                }
                if (mid == 1)
                    h[n / 2] = 1.0 - wc1 / pi;
                break;
            }
            case 3: {
                for (i = 0; i <= n2; i++) {
                    s = i - delay;
                    h[i] = (Math.sin(wc2 * s) - Math.sin(wc1 * s)) / (pi * s);
                    h[i] = h[i] * window(wn, n + 1, i, beta);
                    h[n - i] = h[i];
                }
                if (mid == 1)
                    h[n / 2] = (wc2 - wc1) / pi;
                break;
            }
            case 4: {
                for (i = 0; i <= n2; i++) {
                    s = i - delay;
                    h[i] = (Math.sin(wc1 * s) + Math.sin(pi * s) - Math.sin(wc2 * s)) / (pi * s);
                    h[i] = h[i] * window(wn, n + 1, i, beta);
                    h[n - i] = h[i];
                }
                if (mid == 1)
                    h[n / 2] = (wc1 + pi - wc2) / pi;
                break;
            }
        }
        return (n2);
    }

    double window(int type, int n, int i, double beta) {
        int k;
        double pi, w;
        pi = 4.0 * Math.atan(1.0);
        w = 1.0;
        switch (type) {
            case 1: {
                w = 1.0;
                break;
            }
            case 2: {
                k = (n - 2) / 10;
                if (i <= k)
                    w = 0.5 * (1.0 - Math.cos(i * pi / (k + 1)));
                if (i > n - k - 2)
                    w = 0.5 * (1.0 - Math.cos((n - i - 1) * pi / (k + 1)));
                break;
            }
            case 3: {
                w = 1.0 - Math.abs(1.0 - 2 * i / (n - 1.0));
                break;
            }
            case 4: {
                w = 0.5 * (1.0 - Math.cos(2 * i * pi / (n - 1)));
                break;
            }
            case 5: {
                w = 0.54 - 0.46 * Math.cos(2 * i * pi / (n - 1));
                break;
            }
            case 6: {
                w = 0.42 - 0.5 * Math.cos(2 * i * pi / (n - 1)) + 0.08 * Math.cos(
                        4 * i * pi / (n - 1));
                break;
            }
            case 7: {
                w = kaiser(i, n, beta);
                break;
            }
        }
        return (w);
    }

    double kaiser(int i, int n, double beta) {
        double a, w, a2, b1, b2, beta1;
        b1 = bessel0(beta);
        a = 2.0 * i / (n - 1) - 1.0;
        a2 = a * a;
        beta1 = beta * Math.sqrt(1.0 - a2);
        b2 = bessel0(beta);
        w = b2 / b1;
        return (w);
    }

    double bessel0(double x) {
        int i;
        double d, y, d2, sum;
        y = x / 2.0;
        d = 1.0;
        sum = 1.0;
        for (i = 1; i <= 25; i++) {
            d = d * y / i;
            d2 = d * d;
            sum = sum + d2;
            if (d2 < sum * (1.0e-8))
                break;
        }
        return (sum);
    }

    // /////平均处理
    public int AVESCANS = 10;
    public short[] m_aveInterRes = new short[8192];
    public int ave_num = 0;

    public void AVEProcess() {
        int i = 0, j = 0;
        float ave_numadd = 1;

        // aveScans为平均道数
        float ave_row1 = (float) (1.0 / mScanAve);
        int aveScans = mScanAve;
        ave_num = ave_num + 1;
        ave_numadd = 1 / (float) ave_num;
        for (i = 1; i < mScanLength; i++) {
            if (ave_num <= aveScans) {
                m_aveInterRes[i] = (short) ((m_aveInterRes[i] * (ave_num - 1) + mResult[i]) *
                                            ave_numadd);
            } else {
                m_aveInterRes[i] = (short) ((m_aveInterRes[i] * (aveScans - 1) + mResult[i]) *
                                            (ave_row1));
                ave_num = aveScans + 1;
            }
            mManageResult[i] = m_aveInterRes[i];
        }
    }

    public short[] m_backInterRes = new short[8192];
    public int m_backgroundNum = 0;

    public void REMbackProcess() {
        int i = 0, j = 0;
        float back_numadd = 1;
        m_backgroundNum++;
        back_numadd = 1 / (float) (m_backgroundNum);
        for (j = 1; j < mScanLength; j++) {
            mManageResult[j] = (short) (mResult[j] - m_backInterRes[j]);
            if (m_backgroundNum <= 50)// background_row1)
            {
                m_backInterRes[j] = (short) (
                        (m_backInterRes[j] * (m_backgroundNum - 1) + mResult[j]) * back_numadd);
            } else {
                m_backInterRes[j] = (short) ((m_backInterRes[j] * 49 + mResult[j]) * 1.0 / 50);
                m_backgroundNum = 301;
            }
        }
    }

    // //////////////////
    // 测距轮名称
    public String[] mWhellName = {
            /* "WDMI-900", */
            "WDMI-300", "WDMI-500", "WDMI-55A", "LDMI-130", "GC1500MHz",
            /*
	 * "GC2000MHz天线", "HF900MHz天线",
	 */
            "自定义设置",};

    public int mNowManageCommandID = 0; // 表示正在处理哪个命令
    public int COMMAND_ID_SETSCANSPEED = 2; // 设置扫速
    public int COMMAND_ID_SETTIMEWINDOW = 3; // 设置时窗
    public int COMMAND_ID_SETSCANLENGTH = 4; // 设置道长
    public int COMMAND_ID_SETSINGLEPOS = 5; // 设置信号位置
    public int COMMAND_ID_SETALLHARDPLUS = 7; // 设置整体增益
    public int COMMAND_ID_SETSTEPHARDPLUS = 8; // 设置单点增益
    public int COMMAND_ID_SETFILTER = 9; // 设置滤波
    public int COMMAND_ID_SETJIEDIANCONST = 10;// 设置介电常数
    public int COMMAND_ID_SETAVESCAN = 11; // 设置
    public int COMMAND_ID_SETDIANCE = 12; // 设置点测
    public int COMMAND_ID_SETWHELLMODE = 13; // 设置轮测
    public int COMMAND_ID_SETCOLORPAL = 14; // 设置调色板
    public int COMMAND_ID_SETPLAYBACK = 15; // 设置回放
    public int COMMAND_ID_SETDELETE = 16; // 设置删除
    public int COMMAND_ID_CALIBRATE = 17; // 设置校正
    public int COMMAND_ID_EXTENDNUM = 18; // 标记扩展
    public int COMMAND_ID_DIAMETER = 19; // 设置直径
    public boolean mIsAutoplus = false;

    // 设置用户正在设置的命令Id
    public void setNowSetting_CommandID(int id) {
        mNowManageCommandID = id;
    }

    // 获得当前命令值
    public int getNowSetting_CommandID() {
        return mNowManageCommandID;
    }

    public boolean isSetting_Scanspeed_Command() {
        return mNowManageCommandID == COMMAND_ID_SETSCANSPEED;
    }

    public boolean isSetting_Timewindow_Command() {
        return mNowManageCommandID == COMMAND_ID_SETTIMEWINDOW;
    }

    public boolean isSetting_Scanlength_Command() {
        return mNowManageCommandID == COMMAND_ID_SETSCANLENGTH;
    }

    public boolean isSetting_Singlepos_Command() {
        return mNowManageCommandID == COMMAND_ID_SETSINGLEPOS;
    }

    public boolean isSetting_JieDianConst_Command() {
        return mNowManageCommandID == COMMAND_ID_SETJIEDIANCONST;
    }

    public boolean isSetting_Filter_Command() {
        return mNowManageCommandID == COMMAND_ID_SETFILTER;
    }

    public boolean isSetting_StepHardPlus_Command() {
        return mNowManageCommandID == COMMAND_ID_SETSTEPHARDPLUS;
    }

    public boolean isSetting_AllHardPlus_Command() {
        return mNowManageCommandID == COMMAND_ID_SETALLHARDPLUS;
    }

    public boolean isSetting_AveScan_Command() {
        return mNowManageCommandID == COMMAND_ID_SETAVESCAN;
    }

    public boolean isSetting_DianCe_Command() {
        return mNowManageCommandID == COMMAND_ID_SETDIANCE;
    }

    public boolean isSetting_WhellMode_Command() {
        return mNowManageCommandID == COMMAND_ID_SETWHELLMODE;
    }

    public boolean isSetting_SelectColor_Command() {
        return mNowManageCommandID == COMMAND_ID_SETCOLORPAL;
    }

    // 设置回放
    public boolean isSetting_PlayBack_Command() {
        return mNowManageCommandID == COMMAND_ID_SETPLAYBACK;
    }

    // 设置删除
    public boolean isSetting_Delete_Command() {
        return mNowManageCommandID == COMMAND_ID_SETDELETE;
    }

    // 设置校正
    public boolean isSetting_Calibrate_Command() {
        return mNowManageCommandID == COMMAND_ID_CALIBRATE;
    }

    // 设置标记扩展
    public boolean isSetting_ExtendNum_Command() {
        return mNowManageCommandID == COMMAND_ID_EXTENDNUM;
    }

    public boolean isSetting_Command() {
        return mNowManageCommandID != 0;
    }

    public int getScanLengthTotalSels() {
        return mTotalScanlenNumber;
    }

    public int getSpeedTotalSels() {
        return g_scanSpeedNumber;
    }

    public boolean isAutoHardplus() {
        return mIsAutoplus;
    }

    // 取消自动增益
    public void cancelAutoPlus() {
        int i;
        for (i = 0; i < 9; i++)
            mHardPlus[i] = mBackHardPlus[i];
        for (i = 0; i < 9; i++)
            mRealHardPlus[i] = mHardPlus[i];

        CalHardplusParams();

        // 停止雷达
        if (!isDianCeMode())
            stopRadar();

        setHardplusParams();

        // 开启雷达
        mHadRcvScans = 0;

        if (!isDianCeMode())
            startRadar();

        // 重复一次开始命令，防止出现死机
        // if(!isDianCeMode())
        // {
        // startRadar();
        // //重复一次开始命令，防止出现死机
        // startRadar();
        // }

        mFileHeader.setHardPlus(mHardPlus);

        // 设置时间模式
        if (isTimeMode()) {
            setTimeMode();
        }

        mIsAutoplus = false;
    }

    public int getFilterTotalSels() {
        return g_lvboParamsNumber;
    }

    public String getFilterStr() {
        return g_lvboStr[mFilterSel];
    }

    public String getWhell_SelectTypeName() {
        return mWhellName[mWheeltypeSel];
    }

    // ////
    public int DIB_SHOW = 1;
    public int WIGGLE_SHOW = 2;
    public int mShowType = DIB_SHOW;

    public void setShowType_WIGGLE() {
        mShowType = WIGGLE_SHOW;
    }

    public void setShowType_DIB() {
        mShowType = DIB_SHOW;
    }

    public boolean isDIBShow() {
        return mShowType == DIB_SHOW;
    }

    public boolean isWiggleShow() {
        return mShowType == WIGGLE_SHOW;
    }

    public boolean mIsTempstopShow = false;

    public boolean isTempstopShow() {
        return mIsTempstopShow;
    }

    public void continueShow() {
        mIsTempstopShow = false;
    }

    public void tempStopShow() {
        mIsTempstopShow = true;
    }

    public Activity mMainActivity = null;

    public int sendCommands_1(short[] Coms, short length) {
        int ret;
        ret = writeCommandsToUSB(Coms, length);
        return ret;
    }

    public boolean isSelectSDCard() {
        return mSelectStorageIndex == this.SDCARD_INDEX;
    }

    public boolean isSelectUSB() {
        return mSelectStorageIndex == this.USB_INDEX;
    }

    public boolean isSelectMemory() {
        return mSelectStorageIndex == this.INNER_INDEX;
    }

    // 保存自定义文件路径
    private String customFileName = null;

    public void setCustomFileName(String filePath) {
        DebugUtil.i(TAG, "setCustomFIleName=" + filePath);
        customFileName = filePath;
    }

    public String getCustomFileName() {
        DebugUtil.i(TAG, "getCustomFileName=" + customFileName);
        return customFileName;
    }

}
