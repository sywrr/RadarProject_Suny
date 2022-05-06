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
 * �״��豸����
 */
public class radarDevice {
    // //������������
    final private short RADAR_COMMAND_LENGTH = 2; // �״��������ֽڳ���
    final private short RADAR_COMMAND_SHOWWAVE = 0x0001; // ��ʾ�״ﲨ��
    final private short RADAR_COMMAND_SIGNALPOS = 0x0002; // �����ź�λ��
    final private short RADAR_COMMAND_HARDPLUS = 0x0003; // ����Ӳ������
    final private short RADAR_COMMAND_JDCONST = 0x0007; // ���ý�糣��
    final private short RADAR_COMMAND_ZEROOFF = 0x0008; // ������ƫ
    final private short RADAR_COMMAND_BEGSAVE = 0x000b; // ��ʼ��������
    final private short RADAR_COMMAND_ENDSAVE = 0x000c; // ֹͣ��������
    final private short RADAR_COMMAND_REPORTALLFILES = 0x000f; // �ش����б�����ļ���
    final private short RADAR_COMMAND_TRANSFILE = 0x0010; // �ش�ָ�����ļ�
    final private short RADAR_COMMAND_TIMEWND = 0x0011; // ����ʱ��
    final private short RADAR_COMMAND_SCANSPEED = 0x0012; // ����ɨ��
    final private short RADAR_COMMAND_SAMPLEN = 0x0013; // ���õ���
    final private short RADAR_COMMAND_START = 0x0014; // �����״�
    final private short RADAR_COMMAND_STOP = 0x0015; // ֹͣ�״�
    final private short RADAR_COMMAND_GETSTATUS = 0x0016; // �õ��״��״̬
    final private short RADAR_COMMAND_SETRADARFRQ = 0x0018; // �����״�Ƶ��
    final private short RADAR_COMMAND_LVBO = 0x0019; // �˲�
    final private short RADAR_COMMAND_CONTINUEMODE = 0x001a; // ����ģʽ
    final private short RADAR_COMMAND_WHELLMODE = 0x001b; // �ֲ�ģʽ
    final private short RADAR_COMMAND_DIANCEMODE = 0x001c; // ���ģʽ
    final private short RADAR_COMMAND_GETDIANCEDATA = 0x001e; // �õ��������
    final private short RADAR_COMMAND_SETSCANAVE = 0x001f; // ����ƽ������
    final private short RADAR_COMMAND_REMBACKGROUND = 0x0020; // ��������

    // �����������
    final private short IOCTL_CODE_FLAG = (short) 0xABCD;
    final private short CODE_SET_STOPCONTINUE = (short) 0xAA01;
    final private short CODE_SET_STARTCONTINUE = (short) 0xAA00;
    final private short CODE_SET_WHEELBEG = (short) 0xAA02;
    final private short CODE_SET_WHEELEND = (short) 0xAA03;
    final private short CODE_SET_AUTOPLUS = (short) 0xAA05;
    final private short CODE_SET_SCANLEN = (short) 0xBB12; // ɨ�����
    final private short CODE_SET_FAD = (short) 0xBB13; // ����FAD
    final private short CODE_SET_SCANAVE = (short) 0xBB14;
    final private short CODE_SET_REMBACK = (short) 0xBB15;
    final private short CODE_SET_RADARFRQ = (short) 0xBB16; // ��Ƶ
    final private short CODE_SET_WHEELEXTNUMBER = (short) 0xBB30; // �����չ
    final private short CODE_SET_HANDLEMODE = (short) 0xBB80;
    final private short CODE_SET_DIANCEEXTNUMBER = (short) 0xBB40;
    final private short CODE_SET_ONCEDIANCE = (short) 0xBB41; // �˹����
    final private short CODE_SET_HARDPLUSRANGE = (short) 0xBB42;
    final private short CODE_SET_SMALLMARK = (short) 0xBB50; // ��С��
    final private short CODE_SET_BIGMARK = (short) 0xBB51; // ����
    final private short CODE_GET_WHELLSPEED = (short) 0xBB60; // �õ�������ٶ�
    final private short CODE_SET_READSCANS = (short) 0xBB70; // ����ÿ�ζ�ȡ�ĵ���
    final private short CODE_SET_HARDPLUS = (short) 0xCC10; // �ŵ�Ӳ������
    final private short CODE_SET_ZEROOFF = (short) 0xCC11; // ������ƫ
    final private short CODE_SET_LVBO = (short) 0xCC12; // �����˲�
    final private short CODE_SET_STEP = (short) 0xCC13; // ���ò���
    final private short CODE_SET_SIGNALPOS = (short) 0xCC14; // �����ź�λ��

    private short HANDLEKEY_SAVE = 2; // �ֱ����水��ֵ
    private short HANDLEKEY_MARK = 18; // �ֱ���갴��ֵ
    // //
    boolean mIsTurnWhell = false; // �Ƿ�ת��С���
    public int mDianceDistance = 10; // ���ʱ�������ݼ��
    // //��״���������ϴ�����ʱ���õ��ı���
    short[] mOneScanBuf = new short[16384];
    short SCAN_HEAD_FLAG = 0x7fff; // һ������ͷ��־
    int m_scanCopyPos = 0; // ���һ������ʱ���ѿ�����λ��

    short[] mResult = new short[8192];
    short[] mManageResult = new short[8192];
    static int g_antenFrqNumber = 15;

    // ������Ƶ
    static short[] g_antenFrq = {2000, 1500, 1000, 2000, 1500, 900, 400, 100, 400, 270, 150, 100,
                                 100, 50,};

    // ������Ƶ�ַ���
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

    // �������߶�Ӧ����ʱ���
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

    // �����ظ�Ƶ��
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

    // ��������
    // static int g_scanLengthNumber=7;
    // static String[] g_scanLengthStr={
    // "256��/��",
    // "256��/��",
    // "512��/��",
    // "1024��/��",
    // "2048��/��",
    // "4096��/��",
    // "8192��/��"
    // };

    // ȡ������
    static int mTotalScanlenNumber = 6;
    static short[] g_scanLen = {256, 512, 1024, 2048, 4096, 8192};

    // ɨ���ٶ�
    static String[] g_scanSpeedStr = {"16��/��", "32��/��", "64��/��", "128��/��", "256��/��", "512��/��",
                                      "1024��/��",
                                      /*
	 * "2048��/��", "4096��/��", "8192��/��"
	 */};
    //
    static int g_scanSpeedNumber = 7;
    static short[] g_scanSpeed = {16, 32, 64, 128, 256, 512, 1024,
                                  /*
	 * 2048, 4096, 8192
	 */};

    // ����
    static int g_lvboParamsNumber = 2;// 5;
    static String[] g_lvboStr = {"���˲�", "�˲�",
                                 // "һ���˲�",
                                 // "�����˲�",
                                 // "�����˲�",
                                 // "�ļ��˲�"
    };

    static short[][] g_lvboParams = {{16800, 10000},
                                     // {60, 10000},
                                     // {129,10000},
                                     // {276,10000},
                                     {607, 10000},};

    // ��������ڴ�������(cm)
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
            0.050,// �Զ���
            /*
	 * 0.50, 0.50
	 */};

    // public int WHELLTYPE_2GHZ=5;
    // public int WHELLTYPE_15GHZ=4;

    public int GC100S_INDEX = 12; // GC100S��������
    // public int GC50FS_INDEX=11; //GC50FS��������
    // ÿ������Ƶ�ʶ�Ӧ��Ĭ�ϲ���
    /*
     * ��������: ����������ȡ���㣬ɨ�٣�ʱ�����ź�λ�ã��������棬��ƫ���˲���Ĭ�ϲ�����ͺţ�0-300,1-500,2-130,3-GC1500M��
     */
    static short[][] g_defParamsForRadar = {
            // //AL2000M���߶�Ӧ����
            {0, 1, 256, 10, 4, 0, 2034, 1, 0},
            // //AL1500M���߶�Ӧ����
            {1, 1, 256, 15, 4, 0, 2034, 1, 0},
            // //AL1000M���߶�Ӧ����
            {2, 1, 256, 20, 4, 0, 2034, 1, 0},
            // //GC2000M���߶�Ӧ����
            {3, 1, 256, 10, -10, 0, 2034, 1, 0},
            // //GC1500M���߶�Ӧ����
            {4, 1, 256, 15, -10, 0, 2034, 1, 0},
            // //GC900HF���߶�Ӧ����
            {5, 1, 256, 25, 5, 0, 2034, 1, 0},
            // //GC400HF���߶�Ӧ����
            {6, 1, 256, 50, 12, 0, 2034, 1, 1},
            // //GC100HF���߶�Ӧ����
            {7, 2, 64, 600, -5, 0, 2034, 1, 1},
            // //GC400M���߶�Ӧ����
            // { 8, 1, 256, 60, 12, 0, 2034, 1, 1 },
            {8, 5, 16, 80, 12, 0, 2034, 1, 1},
            // //GC270M���߶�Ӧ����
            {9, 1, 128, 80, 2, 0, 2034, 1, 1},
            // //GC150M���߶�Ӧ����
            {10, 2, 64, 120, -5, 0, 2034, 1, 1},
            // //GC100M���߶�Ӧ����
            {11, 2, 16, 600, 0, 0, 2034, 1, 1},
            // //GC100S���߶�Ӧ����
            {12, 2, 32, 600, 0, 0, 2034, 1, 0},
            // /GC50M���߶�Ӧ����
            {13, 2, 16, 650, 0, 0, 2034, 1, 1},};

    /**
     * ������ͺŶ�Ӧ�ı����չ��ʼֵ
     */
    static short[] g_wheelextendnum = {17, // 0,300�����
                                       2, // 1,500�����
                                       2, // 55A�����
                                       8, // 8,130�����
                                       25, // 1.5GĬ�ϱ����չֵ
                                       1 // �Զ���
    };

    // ÿ�����߶�Ӧ��ʱ��ֵ��Χ
    // Ŀǰ��˳��AL2000,AL1500,AL1000,GC2000M,GC1500M,GC900HF,GC400HF,GC100HF,
    // GC400M,GC270M,GC150M,GC100M,GC100s,GC50M
    static short[][] g_timeWndRange = {
            // //AL2G����
            {5, 25},
            // //AL1500M����
            {5, 30},
            // //AL1000M����
            {8, 40},
            // /GC2000M����
            {5, 25},
            // //GC1500M����
            {5, 30},
            // //GC900HF����
            {8, 40},
            // //GC400HF����
            {20, 200},
            // //GC100HF����
            {100, 3000},
            // //GC400M����
            {20, 200},
            // /GC270M����
            {20, 300},
            // //GC150M����
            {50, 1000},
            // //GC100M����
            {100, 3000},
            // /GC100S����
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
     * ��������Ƶ�ʼ���û����õ�ʱ��ֵ�Ƿ����(����ģʽ��)
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
     * ����ʱ��ֵ���ȡ������
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
     * ����ȡ���������õ���Ӧ��ɨ��
     */
    public int getScanspeedForScanlen(int scanLen) {
        int needSpeed;
        needSpeed = mScanSpeed;
        do {
            // AL2G AL1500 AL1000
            if (mAntenFrqSel == 0 || mAntenFrqSel == 1 || mAntenFrqSel == 2) {
                if (scanLen == 256) {
                    // ���������
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

    final private int MAXDEFUALTWHEELNUM = 6; // Ĭ�ϲ���Ǹ���
    private double[] mWhellcheckCoeff = new double[MAXDEFUALTWHEELNUM];

    final private int MAXSPEEDSCANLENTH = 256 * 1024;
    private int mRepFrq = 128; // �����ظ�Ƶ��
    private int mAntenFrqSel = 5; // ѡ����������
    private int mScanSpeedSel = 4; // ѡ���ɨ��
    private int mScanSpeed = 256; // ɨ���ٶ�
    private int mScanLengthSel = 1; // ѡ��ĵ���
    private int mScanLength = 512; // �ɼ��ĵ���
    private int mTimeWindow = 60; // ʱ��
    private int mSignalPos = 0; // �ź�λ��
    private float[] mHardPlus = {0, 0, 0, 0, 0, 0, 0, 0, 0};
    private float[] mBackHardPlus = {0, 0, 0, 0, 0, 0, 0, 0, 0};
    public float[] mRealHardPlus = {0, 0, 0, 0, 0, 0, 0, 0, 0}; // ����DAӲ�����õ���ʵ�ʷŴ�dB
    public float[] mSoftPlus = {0, 0, 0, 0, 0, 0, 0, 0, 0};
    public double[] mTotalNeedZoom = new double[8192];
    public double[] mTotalNeedZoom1 = new double[8192];
    public double[] mHardZoom = new double[8192];
    public double[] mSoftZoom = new double[8192];
    private int mZeroOff = 2034; // ��ƫ
    private float mJiedianConst = 9; // ��糣��
    private int mScanAve = 4; // ƽ������
    private int mRemoveBackSel = 0; // ��������
    private short[] mRemoveBackParams = {0, 1};
    private int mFilterSel = 1;
    private int mWheelExtendNumber = 0; // �������չֵ
    private int mWheeltypeSel = 0; // ���������ѡ��ֵ
    private int MWHEELMAXINDEX = 6; // �ֲ�����ѡ��ֵ
    private int mDianceNumber = 10; // ���ʱ���ظ�����
    private long mHadRcvScans = 0; // �Ѿ��ɼ��ĵ���
    private double mWavespeed = 30.; // ����(cm/s)
    private boolean mChangeDataListAdapter = false;// ����Ƿ���Ĳ���

    // ����״����ݵĻ�����
    private byte[][] mDatasBufs;
    private int mBufIndex;
    private int mBufsNumber = 4;
    private int mBufLength = 2048 * 128 * 2;
    private int[] mNowWPos = new int[mBufsNumber];
    private int mRBufIndex = 0;
    private int[] mNowRPos = new int[mBufsNumber];
    // �����ļ���Ҫ�Ĳ���
    private int mNowFileindex = 0; // �ļ�����
    private boolean mExistSaveFile = false; // �Ƿ���ڱ����ļ�
    private FileOutputStream fSaveOS; //
    private FileHeader mFileHeader = new FileHeader();
    public String mSavingFilePath;

    private short[] mOneScanDatas = new short[8192]; // ��ŵ�������
    int mHasRcvDianceNumber = 0;
    private float[] mOneDianCeDatas = new float[8192]; // ��ŵ������
    //
    private Context mContext;

    private String TAG = "radarDevice";

    // �������������ļ�������
    private int CREATE_NEWFILE_TYPEINDEX = 1;
    private int CREATE_NEWFILE_TYPETIME = 2;
    private int mCreateNewFileType = CREATE_NEWFILE_TYPEINDEX;

    // �洢·��,Ĭ���ڴ�
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
     * u��λ����δȷ��
     */
    private String mUSBPath = "";
    // �����ļ���·��
    public String mParamsFilefolderPath = "/radarParams/";
    public String mWhellcheckFileExtname = ".check";
    // �״������ļ���·��
    public String mLTEFilefolderPath = "/LteFiles/";
    public String mWhellcheckFilename = "whellcheck";
    // ʹ��wifi��������
    private boolean mWifiSendDatas = false;

    // ����Ĭ���ֲ����
    public boolean loadDefaultWhellcheckParams() {
        String fileName = getParamsPath() + DEFAULTCHECKFILE;
        if (loadWhellcheckParams(fileName))
            return true;
        else {
            return false;
        }
    }

    // ����У׼����
    public boolean loadWhellcheckParams(String pathName) {
        DebugUtil.i("ExtendNumb", "LoadWhellcheckFileName:=" + pathName);
        byte[] buf = new byte[1024];

        try {
            // �ж��ļ��Ƿ���ڣ��������򱣴�Ĭ���ļ�
            if (!readCheckFile(pathName, buf))
                return false;
            else
                ;

            // ��ȡ�����У��ֵ
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

            // �ӵ�100λ���ȡ�����չ
            // ����mWheeltypeSelֵ��ñ����չֵ
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

            DebugUtil.i(TAG, "��ȡ�ļ���ȡ��ֵtemVal=" + temVal + "mWheeltypeSel=" + mWheeltypeSel);
            DebugUtil.i("ExtendNumb", "mWheelExtendNumber=" + mWheelExtendNumber);
        } catch (Exception e) {
            DebugUtil.i(TAG, "Load params file:" + pathName + " fail!");
            return false;
        }
        return true;
    }

    // װ���Զ����У׼�����ļ�
    private int mDiameter = 773;// ֱ��ֵ
    private int mPulseIndex = 0;// �����±�ֵ
    private int mPulse = 0;// ����ֵ

    // �õ�����ֵ
    public int getmPulse() {
        return mPulse;
    }

    // ����ѡ����±���������ֵ
    public void setmPulse(int inputPulse) {
        mPulse = inputPulse;
    }

    public int getAntenFrq() {
        return g_antenFrq[mAntenFrqSel];
    }

    // �����Զ����У׼�ļ�
    public boolean loadCustomWheelCheckParamsFile(String pathName) {
        // ����У׼�������չ��ֱ��������ֵ
        DebugUtil.i("ExtendNumb", "LoadWhellcheckFileName:=" + pathName);
        byte[] buf = new byte[1024];

        try {
            // �ж��ļ��Ƿ���ڣ��������򱣴�Ĭ���ļ�
            if (!readCheckFile(pathName, buf))
                return false;
            else
                ;

            // ��ȡУ׼ֵ�������չ��ֱ��������ֵ
            // 0-3 У׼ֵ��4-7�����չ��8-11ֱ����12-16������
            // ��ȡУ׼ֵ
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

            // ��ȡ�����չ
            index += 4;
            temVal = bitCompute(buf, index);
            mWheelExtendNumber = temVal;
            DebugUtil.i(TAG, "mWheelExtendNumber=" + mWheelExtendNumber);
            // DebugUtil.i(TAG,
            // "id="+index+"buf="+buf[index]+","+buf[index+1]+","+buf[index+2]+","+buf[index+3]);

            // ��ȡֱ��
            index += 4;
            int num = 0;
            num = bitCompute(buf, index);
            mDiameter = num;
            DebugUtil.i(TAG, "ֱ��num=" + mDiameter);

            // ��ȡ�����±�ֵ
            index += 4;
            int pulseIndex = 0;
            pulseIndex = bitCompute(buf, index);
            mPulseIndex = pulseIndex;
            this.mPulse = this.getmPulseIndex();
            DebugUtil.i(TAG, "����pulse=" + mPulseIndex);

        } catch (Exception e) {
            DebugUtil.i(TAG, "Load params file:" + pathName + " fail!");
            return false;
        }
        return true;
    }

    // int��λ������index����ʼλ
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

    // �õ�ֱ����ֵ
    public int getmDiameter() {
        return mDiameter;
    }

    // ����ֱ��ֵ�����÷�Χ(0-1000]
    public void setmDiameter(int mDiameter) {
        if (mDiameter < 0) {
            this.mDiameter = 1;
        } else if (mDiameter > 1000) {
            this.mDiameter = 1000;
        }

        this.mDiameter = mDiameter;
    }

    // ��ȡ�ض�·��check�ļ�
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
     * �����½����Զ���У׼�ļ�
     */
    public void createWhellCheckFile(String filename) {
        // ��������У���ļ�
        DebugUtil.i(TAG, "createWheelCheckFile=" + filename);

        String Name = getInnerStoragePath() + mParamsFilefolderPath;
        Name += filename;
        // Name += mApp.mRadarDevice.mWhellcheckFileExtname;

        File f = new File(Name);
        // �ж��Ƿ����
        if (f.exists()) {

        } else
            ;

        try {
            FileOutputStream fileParams = new FileOutputStream(Name);
            // �����������
            byte[] buf = new byte[1024];
            // ����0-3У׼ֵ��4-7�����չ��8-11ֱ����12-16����
            // У׼ֵ
            int index = 0;
            int wheelIndex = this.MAXDEFUALTWHEELNUM - 1;
            int tempValue = (int) (mWhellcheckCoeff[wheelIndex] * 1000);
            valueToBit(buf, index, tempValue);

            // ��������չֵ,��ǰֵһ��Ĭ��Ϊ1
            index += 4;
            tempValue = this.mWheelExtendNumber;
            valueToBit(buf, index, tempValue);

            // ����ֱ��
            index += 4;
            tempValue = this.mDiameter;
            valueToBit(buf, index, tempValue);

            // ���������±�ֵ
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

    // �����Զ���
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
     * @param buf���ո�ֵ�ı���
     * @param index�±�
     * @param tempValue��Ҫת����ֵ
     */
    private void valueToBit(byte[] buf, int index, int tempValue) {
        buf[index] = (byte) tempValue;
        buf[index + 1] = (byte) (tempValue >> 8);
        buf[index + 2] = (byte) (tempValue >> 16);
        buf[index + 3] = (byte) (tempValue >> 24);
    }

    // �����ֲ����
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

            // �����������
            byte[] buf = new byte[8];
            // �����У��ֵ
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

                // 100��ʼ������չ���������ѡ�еĲ�����ͺŴ�Ĭ��ֵ,ֻ���浱ǰѡ�е�
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
                // �ȱ���У׼ֵ
                temp = mWheeltypeSel * 8;
                file.seek(temp);
                file.write(buf, 0, 4);
                // �ٱ�������͵ı����չֵ
                temp = 100 + mWheeltypeSel * 4;
                file.seek(temp);
                // fileParams.write(buf, 0, 1024);
                file.write(buf, 4, 4);
            } catch (Exception e) {
                DebugUtil.i(TAG, "save whellcheckparams fail!");
            }

            file.close();

            // ���Զ�ȡ
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

    // ����Ĭ�ϲ����ļ�
    public boolean saveDefaultParamsFile() {
        String pathName;
        pathName = this.INNERSTORAGE + mParamsFilefolderPath + "defparams.par";
        return saveParamsFile(pathName);
    }

    // ����Ĭ��ϵͳ�ļ�
    public boolean saveSystemSetFile() {
        System.out.println("����ϵͳ���ò���");
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
            DebugUtil.i(TAG, "�����λ��id=" + mSelectStorageIndex);
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
     * ����ϵͳ�����ļ���ֻ��һ������λ�ü�¼
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
            // �����������
            readSystemSettings(fileIs, 0, mSystemSettingsData.length);
            // ��ô洢λ��
            int storageId = (0x000000ff & mSystemSettingsData[0]) |
                            (0x0000ff00 & mSystemSettingsData[1] << 8) |
                            (0x00ff0000 & mSystemSettingsData[2] << 16) |
                            (0xff000000 & mSystemSettingsData[3] << 24);
            DebugUtil.i(TAG, "��ȡ�ļ���ñ����λ��id=" + storageId);
            // ((IDSC2600MainActivity)mContext).showToastMsg("��ȡ�ļ���ñ����λ��id="+storageId);
            this.setStoragePath(storageId);// ���ô洢λ��
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

    // ����Ĭ�ϵ�У���ļ�
    public boolean saveDefaultCheckParamsFile() {
        String checkFileName;
        checkFileName = this.INNERSTORAGE + mParamsFilefolderPath + DEFAULTCHECKFILE;
        return saveWhellcheckParams(checkFileName);
    }

    // �ж��Ƿ��ǲ����ļ�
    public boolean isParamsFilename(String fileName) {
        String end = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length())
                             .toLowerCase();
        if (end.equals("par"))
            return true;
        return false;
    }

    // �õ��洢λ�õ�ID
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
     * �����״����
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
            // �����������
            byte[] buf = new byte[1024];
            buf[0] = (byte) mAntenFrqSel; // ��Ƶ
            // buf[1] = (byte)mScanSpeedSel; //ɨ��
            buf[1] = (byte) mScanSpeed;
            buf[2] = (byte) (mScanSpeed >> 8);
            buf[3] = (byte) mScanLengthSel; // ����
            buf[4] = (byte) mFilterSel; // �˲�
            // ʱ��
            buf[5] = (byte) mTimeWindow;
            buf[6] = (byte) (mTimeWindow >> 8);
            // �ź�λ
            buf[7] = (byte) (mSignalPos);
            buf[8] = (byte) (mSignalPos >> 8);
            // �����糣��
            long temp = Float.floatToIntBits(mJiedianConst);
            buf[9] = (byte) (temp);
            buf[10] = (byte) ((int) temp >> 8);
            buf[11] = (byte) ((int) temp >> 16);
            buf[12] = (byte) ((int) temp >> 24);
            // ��ƫ
            buf[13] = (byte) (mZeroOff);
            buf[14] = (byte) (mZeroOff >> 8);
            buf[15] = (byte) (mZeroOff >> 16);
            buf[16] = (byte) (mZeroOff >> 24);
            // ����ƽ��
            buf[17] = (byte) mScanAve;
            buf[18] = (byte) (mScanAve >> 8);
            buf[19] = (byte) (mScanAve >> 16);
            buf[20] = (byte) (mScanAve >> 24);
            // ��������
            buf[21] = (byte) mRemoveBackSel;
            // ����
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

            // ���ʱ�����������
            buf[1022] = (byte) mDianceDistance;
            buf[1023] = (byte) (mDianceDistance >> 8);
            index = 22 + 4 * 9;
            // ����ѡ��Ĳ��������
            buf[59] = (byte) mWheeltypeSel;
            buf[60] = (byte) (mWheeltypeSel >> 8);
            buf[61] = (byte) (mWheeltypeSel >> 16);
            buf[62] = (byte) (mWheeltypeSel >> 24);
            DebugUtil.i(TAG, "saveParamsFile save mWheeltypeSel!=" + mWheeltypeSel);

            /*
             * //�����У��ֵ index = 22+4*9; long temVal; for(i=0;i<10;i++) { temVal
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
            // fileParams.flush(); 20170519δʵ��
            fileParams.close();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * ���ļ�������,����ѡ��������Ƶ��ʹ�õļ��غ�����������Ƶ����ѡ��
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

            // ������Ƶ
            // mAntenFrqSel = buf[0];
            // hss�޸�Ϊ�����ļ��ж�ȡ�����ⷴ��д��ʧ�ܣ�
            // ���Ӳ����ļ���ȡ����ж�
            int inputAntenFrqSel = buf[0];
            // �ظ�Ƶ��
            mRepFrq = g_fixRepFrqNum[mAntenFrqSel];
            // ɨ��
            mScanSpeed = (0x00ff & buf[1]) | (buf[2] << 8);

            // �����ļ���ȡֵ���ж�
            // �ж�������Ƶ��ȡ���ļ��ж�ȡ��ֵ�Ƿ�һ����0ʱ�����ļ�����ֵ������Ҫ��һ���ж�
            if (inputAntenFrqSel == mAntenFrqSel) {
                if (inputAntenFrqSel == 0)// �ڶ�ȡ��0ʱ����Ҫ���ж�һλֵ
                {
                    if (mScanSpeed == 0)
                        return false; // ���ɨ��Ϊ�㣬���ж����ļ�д��ʧ��
                    else
                        ; // ���ɨ����ֵ�����ʾ�ļ�����ֵ
                } else
                    ;
            } else
                return false;

            // ����
            mScanLengthSel = buf[3];
            mScanLength = g_scanLen[mScanLengthSel];
            // �˲�
            mFilterSel = buf[4];
            // ʱ��
            mTimeWindow = (0x000000ff & buf[5]) | (0x0000ff00 & (buf[6] << 8));
            // �ź�λ��
            mSignalPos = (0x00ff & buf[7]) | (buf[8] << 8);

            // ��糣��
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

            // ��ƫ
            mZeroOff = (0x000000ff & buf[13]) | (0x0000ff00 & buf[14] << 8) |
                       (0x00ff0000 & buf[15] << 16) | (0xff000000 & buf[16] << 24);
            // ����ƽ��
            mScanAve = (0x000000ff & buf[17]) | (0x0000ff00 & buf[18] << 8) |
                       (0x00ff0000 & buf[19] << 16) | (0xff000000 & buf[20] << 24);
            // ��������
            mRemoveBackSel = buf[21];
            // ����
            int i;
            int index = 22;
            int hardVal;
            for (i = 0; i < 9; i++) {
                mHardPlus[i] = buf[index + i * 4] | buf[index + i * 4 + 1] << 8 |
                               buf[index + i * 4 + 2] << 16 | buf[index + i * 4 + 3] << 24;
                // DebugUtil.i(TAG,"Now "+i+" hardplusval="+mHardPlus[i]);
            }

            // ��ȡ�ֲ�����
            // this.mWheeltypeSel = (0x000000ff & buf[59]) |
            // (0x0000ff00 & buf[60]<<8) |
            // (0x00ff0000 & buf[61]<<16) |
            // (0xff000000 & buf[62]<<24);
            // mWheeltypeSelĬ��ֵ��������
            this.mWheeltypeSel = g_defParamsForRadar[mAntenFrqSel][8];

            // ���ֲ����͵õ������չֵ��0920�����ڴ˴��õ���Ӧ���ļ���ȡ��á�
            // mWheelExtendNumber = g_wheelextendnum[mWheeltypeSel];
            // DebugUtil.i(TAG,
            // "1.load mWheeltypSel="+mWheeltypeSel+",extendNum="+mWheelExtendNumber);

            /*
             * //��ȡ�����У��ֵ index = 22+4*9; long temVal; for(i=0;i<10;i++) {
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
     * ���ص�����������״�����б����д���
     *
     * @param pathName �ļ�·��
     * @return �����Ƿ�ɹ�
     */
    public boolean loadParamsFile(String pathName) {
        DebugUtil.i(TAG, "loadParamsFile=" + pathName);

        byte[] buf = new byte[1024];

        try {
            // ���ļ��ж�ȡ����
            onlyLoadParamsFromeFile(pathName);
            // ///��ʼ���ò���
            this.CalStepParams();
            this.CalHardplusParams();
            // ֹͣ�״�
            stopRadar();
            // ��������Ƶ��
            short[] commands = new short[3];
            commands[0] = IOCTL_CODE_FLAG;
            commands[1] = CODE_SET_RADARFRQ;
            commands[2] = g_antenFrq[mAntenFrqSel];
            sendCommands_1(commands, (short) 6);
            DebugUtil.i(TAG, "loadParamsFile ��Ƶ=" + g_antenFrq[mAntenFrqSel]);

            // ����ȡ������
            setScanLenParams();
            // �����ź�λ�ò���
            setSignalParams();
            // ���ò�������
            setStepParams();
            // ����fad����
            setFADParams();
            // ��������
            CalHardplusParams();
            setHardplusParams();
            // �����˲�����
            setFilterParams();
            // �����״�
            startRadar();
        } catch (Exception e) {
            DebugUtil.i(TAG, "Load fileheader file fail!");
        }
        return true;
    }

    // ���ݵ�ǰ���豸״̬�������ļ�ͷ����
    public synchronized void refreshFileHeader() {
        DebugUtil.i(TAG, "refreshFileHeader");
        mFileHeader.rh_data = 0;
        mFileHeader.rh_nsamp = (short) mScanLength;
        mFileHeader.rh_zero = 0;
        mFileHeader.rh_sps = mScanSpeed;

        mFileHeader.rh_position = mSignalPos;
        mFileHeader.rh_range = mTimeWindow;
        mFileHeader.rh_spp = g_antenFrq[mAntenFrqSel];
        DebugUtil.i(TAG, "������Ƶ=" + mFileHeader.rh_spp + "mAntenFrqSel=" + g_antenFrq[mAntenFrqSel]);

        // ����ϵͳʱ�䣬�޸�ʱ����֮ͬ
        /*
         * class HEAD_DATE { byte []sec2 = new byte[5]; //5��/2 (0-29) byte []min
         * = new byte[6]; //6�� (0-59) byte []hour = new byte[5]; //5ʱ (0-23)
         * byte []day = new byte[5]; //5�� (0-31) byte []month = new byte[4];
         * //4�� (1-12) byte []year = new byte[7]; //7�� (0-127=1980-2107) };
         */

        Time t = new Time();
        t.setToNow();
        // DebugUtil.i(TAG,
        // "ʱ��="+t.year+t.month+t.monthDay+t.hour+t.minute+t.second);

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

        // �޸�ʱ���봴��ʱ����ͬ
        mFileHeader.rh_modif = mFileHeader.rh_creat;

        // ̽��ģʽ
        mFileHeader.rh_workType = (byte) getWorkMode();

        mFileHeader.rh_nrgain = 9;
        mFileHeader.rh_epsr = mJiedianConst;
        mFileHeader.rh_flagExt = (short) mWheelExtendNumber;
        mFileHeader.rh_workType = (byte) getWorkMode();
        for (int i = 0; i < 9; i++) {
            mFileHeader.rh_rgainf[i] = mHardPlus[i];
        }
    }

    // ��byteת��Ϊ��8�����ݵ�byte����
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

    private byte WHELL_MODE = 2; // �ֲ�ģʽ
    private byte DIANCE_MODE = 1; // ���ģʽ
    private byte TIME_MODE = 0; // ʱ��ģʽ(����ģʽ)

    public int getWorkMode() {
        if ((mNowMode & RADARDEVICE_DIANCE) == RADARDEVICE_DIANCE)
            return DIANCE_MODE;
        if ((mNowMode & RADARDEVICE_WHEEL) == RADARDEVICE_WHEEL)
            return WHELL_MODE;
        return TIME_MODE;
    }

    // �õ��״�����ļ�·��
    public String getParamsPath() {
        return INNERSTORAGE + mParamsFilefolderPath;
    }

    // ����ѡ��ָ��·��
    private int mSelectStorageIndex = 0;

    public int getSelectStorageIndex() {
        DebugUtil.i(TAG, "getSelectStorageIndex mSelectStorageIndex=" + mSelectStorageIndex);
        return mSelectStorageIndex;
    }

    /**
     * ���ô洢·��
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
     * ͨ��index���·����0 �ڴ棬1sdcard��2usb��
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

    // �õ�ȡ������
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

    // �õ�ɨ��
    public int getScanSpeed() {
        return mScanSpeed;
    }

    // �õ�ʱ��ֵ
    public int getTimeWindow() {
        return mTimeWindow;
    }

    // �õ���������
    public double getTouchDistance() {
        double result = mWheelInterDistance[mWheeltypeSel] * mWheelExtendNumber *
                        mWhellcheckCoeff[mWheeltypeSel];
        // BigDecimal b = new BigDecimal(result);
        // result =
        // b.setScale(4,BigDecimal.ROUND_HALF_DOWN).doubleValue();//����ȡ��
        return result;
    }

    // ����ʱ��
    /*
     * ������Ƶֵ��ȷ��ʱ���ķ�Χ[1-8000]
     */
    public void setTimeWindow(int wndVal, boolean isPro) {
        DebugUtil.i("setTimeWindow", "enter setTimeWindow!");

        // ����ֵ�ķ�Χ
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
            // //���㲽������
            CalStepParams();
            if (!isDianCeMode())
                // ����ֹͣ����
                stopRadar();
            // ���ò�������
            setStepParams();
            // ����fad����
            setFADParams();
            // �����ź�λ��20170321
            setSignalParams();
            // ���Ϳ�������
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

        // ���ɲ����ļ���
        String pathName;
        pathName = INNERSTORAGE + mParamsFilefolderPath;
        File destDir = new File(pathName);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        // �����״������ļ���
        pathName = INNERSTORAGE + mLTEFilefolderPath;
        File lteDir = new File(pathName);
        if (!lteDir.exists()) {
            lteDir.mkdirs();
        }

        // ��ʼ��У׼ֵ��Ϊ1
        // mWhellcheckCoeff = new double[MAXDEFUALTWHEELNUM];

        for (i = 0; i < MAXDEFUALTWHEELNUM; i++)
            mWhellcheckCoeff[i] = 1;

        for (i = 0; i < 8192; i++)
            mTotalNeedZoom1[i] = 1;
    }

    /**
     * ����ĳ���У��ֵ
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

    // �õ�ĳ��У׼ֵ
    public double getWhellCheckCoeff(int which) {
        return mWhellcheckCoeff[which];
    }

    /**
     * �õ�Ĭ�ϲ���Ǹ��� ����Ĭ�ϵĲ���ǵĸ���
     */
    public int getWheelDefaultMaxNum() {
        return this.MAXDEFUALTWHEELNUM;
    }

    // ��ʼ��������
    public boolean beginSave() {
        // ֹͣ�״�
        // if(stopUSBLTD() == -1)
        // return false;
        // ֹͣ�״�
        if (!isDianCeMode())
            if (stopRadar() == -1)
                return false;

        // �����ڴ����
        mBufIndex = 0;
        for (int i = 0; i < mBufsNumber; i++) {
            mNowWPos[i] = 0;
        }

        // �����״�
        if (!isDianCeMode()) {
            if (startRadar() == -1)
                return false;
        }

        mNowMode |= RADARDEVICE_SAVING;
        mSaveMode = RADARDEVICE_SAVING_CONTINUE;

        mHadRcvScans = 0;

        if (isBackOrientMode()) {
            // ���˶�λ����
            mBackOrientFlag = false;
            mHadBackScans = 0;
        }

        return true;
    }

    // ֹͣ��������
    public boolean stopSave() {

        // //
        if (!this.isDianCeMode()) {
            // ֹͣ�״�
            if (stopRadar() == -1)
                return false;
        }
        // �������һ�α���
        lastSaveDatas1();

        // �����ڴ����
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
        // �����״�
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

    // //�õ��Ѿ��н��ľ���(cm)
    public double getHasRcvDistance() {
        return mHadRcvScans * mWheelExtendNumber * mWheelInterDistance[mWheeltypeSel];
    }

    // //�õ�Ӳ������
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

    // //���ݽ�糣����ʱ���������ֵ
    public double getDeep() {
        double speed = 0.3 * 100 / Math.sqrt(mJiedianConst); // cm/s;
        double deep = (mTimeWindow) * speed / 200;

        return deep;
    }

    // //�ж��Ƿ��Ѿ�����
    public boolean isOverSpeed() {
        return mOverspeedFlag;
    }

    // �ж��Ƿ����datalistadapter�е����ݣ���Ҫ�ǲ����б��е�����
    public boolean isChangeDataListAdapter() {
        return mChangeDataListAdapter;
    }

    // //�õ���ƫ
    public int getZeroOffVal() {
        return mZeroOff;
    }

    // //�õ��ź�λ��
    public int getSignalpos() {
        return mSignalPos;
    }

    // //�����״ﲨ��
    public void setWavespeed(double speed) {
        mWavespeed = speed;
    }

    // //�Ӳ��ٵõ���糣��
    public double getJDConstFromeWavespeed(double speed) {
        double jdConst;
        jdConst = 30. / speed;
        return jdConst * jdConst;
    }

    // //�õ�����
    public double getWavespeed() {
        double speed = 0.3 * 100 / Math.sqrt(mJiedianConst); // cm/s;
        return speed;
    }

    // //����ʱ��ģʽ
    public boolean setTimeMode() {
        if (!isCanSendCommand())
            return false;
        // ��������ֲ�ģʽ
        if (isWhellMode()) {
            stopWhellMode();
        }

        // ������ڵ��ģʽ
        if (isDianCeMode()) {
            stopDianCeMode();
        }

        setReadScansParams();// 20170616

        // ���ñ����չΪ0
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

        // ��λ���ٱ���
        mOverspeedFlag = false;

        return true;
    }

    // ֹͣ�ֲ�ģʽ
    public void stopWhellMode() {
        short[] commands = new short[2];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_WHEELEND;
        //
        sendCommands_1(commands, (short) 4);

        mNowMode &= (~0xF0);
    }

    // ֹͣ���ģʽ
    public void stopDianCeMode() {
        // ֹͣ�״�
        stopRadar();

        //
        mNowMode &= (~0xF0);
    }

    // //������������ֵ,����������
    public void setHardplusValusOnly(float[] vals) {
        DebugUtil.i(TAG, "enter setHardplusValusOnly!");
        mHardPlus = vals;
        int i;
        for (i = 0; i < 9; i++) {
            mRealHardPlus[i] = mHardPlus[i];// (mHardPlus[i]+4.39);
            DebugUtil.i(TAG, "mRealhardplus[" + i + "]=" + mRealHardPlus[i]);
        }
    }

    // //�õ����ʱ���ظ�����
    public int getDianceNumber() {
        return mDianceNumber;
    }

    // //
    public double getWheelCoeff() {
        return mWhellcheckCoeff[mWheeltypeSel];
    }

    // //�õ���������ڱ�Ǽ�ľ���
    public double getWheelInterDistance() {
        return mWheelInterDistance[mWheeltypeSel];
    }

    /**
     * ���ò������չֵ���趨��Χ(0,5000]
     */
    public void setWheelExtendNumber(int extendNum) {
        if (mWheelExtendNumber < 0) {
            this.mWheelExtendNumber = 1;
        } else if (mWheelExtendNumber > 5000) {
            this.mWheelExtendNumber = 5000;
        }
        mWheelExtendNumber = extendNum;
    }

    // //�õ�����ֿ���ʱ����չֵ
    public int getWheelExtendNumber() {
        return mWheelExtendNumber;
    }

    // //�õ����������ѡ��ֵ
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

    // //������ƪ
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

    // //����Ӳ������
    public void setHardplus(float[] vals) {
        DebugUtil.i(TAG, "sethardplus send command!");
        mHardPlus = vals;
        int i;
        for (i = 0; i < 9; i++) {
            mRealHardPlus[i] = mHardPlus[i];
            DebugUtil.i(TAG, "hardplus[" + i + "]=" + mHardPlus[i]);
        }

        CalHardplusParams();

        // ֹͣ�״�
        if (!isDianCeMode())
            stopRadar();
        // hss0427
        setHardplusParams();

        // �����״�
        mHadRcvScans = 0;
        if (!isDianCeMode())
            startRadar();

        // �ظ�һ�ο�ʼ�����ֹ��������
        // if(!isDianCeMode())
        // {
        // startRadar();
        // //�ظ�һ�ο�ʼ�����ֹ��������
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

    // ���㲽������
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

    // ��ʱоƬ�ڲ�ͬ�¶��µ���ʱ���{30~50,50~}
    public float[] mDelayTimes = {(float) 4.590, (float) 4.660};
    private float DELAY_TIME = (float) 4.40;// 4.660;//4.530; //(float)4.796;
    // //(float) 4.807; //��ʱоƬ�̶���ʱ

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

    // ���㲽������
    public int CalStepParams() {
        DebugUtil.i(TAG, "Enter CalStepParams!");

        short sigPosBeg1, sigPosBeg2, sigPosBeg4, sigPosEnd1, sigPosEnd2;
        short FAD; // FADƵ�ʰ�������ʱ�Ӹ���
        short repFrqNum; // �ظ�Ƶ�ʰ�������ʱ�Ӹ���
        short shiftRegPreSet, shiftRegEndSet;
        short stepInter;
        short stepLength;
        short stepLPer8ns; // һ����ʱ��(125MHz)������
        short repFrq; // �ظ�Ƶ��
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

        // ////LTD2100���㹫ʽ
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
         * //���㲽����� float tem1,tem2; tem1=(float)timeWnd; tem2=(float)scanSpeed;
         * tem=tem1*tem2; tem1=10*repFrq; tem=tem/tem1; int temInt;
         * temInt=(int)(tem); if(tem*10-temInt*10 >=5) temInt++;
         * stepInter=(short)temInt; if(stepInter<1) stepInter=1; do{ //����5ns�������
         * tem1=(float)(9.2592); tem2=(float)(stepInter); tem=tem1*tem2;
         * tem=(float)(5.*1000./tem); temInt=(int)tem; if(tem*10-temInt*10>=5)
         * temInt++; stepLPer5ns=(short)temInt; stepLPer5ns+=1; //���㲽������
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

        // ///������LTDPro���㹫ʽ
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
        // ���㲽�����
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
            // ����8ns�������
            tem1 = (DELAY_TIME);
            tem2 = (stepInter);
            tem = tem1 * tem2;
            tem = (float) (8. * 1000. / tem);

            temInt = (int) tem;
            if (tem * 10 - temInt * 10 >= 5)
                temInt++;
            DebugUtil.i("setTimeWindow", "tem=" + tem + ",temInt=" + temInt);

            stepLPer8ns = (short) temInt;
            // ���㲽������
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

    // ����������
    public void calSoftPlus() {
        int i;
        // ���㲨�εķŴ���
        // ���㱶��
        int scanLen = mScanLength;
        double[] zoomBase = new double[9];

        // //������Ҫ������
        int j;
        double temVal;
        float[] mNeedPlus = mRealHardPlus; // �û����õ�����,������

        // �����ܵķŴ���
        double[] zoomNeed = new double[9]; // �û����ö�Ӧ�ķŴ���
        for (j = 0; j < 9; j++) {
            temVal = mNeedPlus[j] / 20.;
            temVal = Math.pow(10, temVal);
            zoomNeed[j] = temVal;
        }

        // �������Բ�ֵ�㷨�����ܵķŴ���
        double scanLenPer;
        double zoom1;
        int index;
        scanLenPer = scanLen / 8.;
        for (i = 0; i < 8; i++) {
            // ���㵱ǰ�εķŴ�б��
            zoom1 = (zoomNeed[i + 1] - zoomNeed[i]) / scanLenPer;
            for (j = 0; j < (int) scanLenPer; j++) {
                index = (int) (j + i * scanLenPer);
                mTotalNeedZoom[index] = zoomNeed[i] + zoom1 * j;
            }
        }

        // ///����Ӳ������ķŴ���
        for (j = 0; j < 9; j++) {
            temVal = mRealHardPlus[j];
            if (temVal > 5)
                temVal = 5;
            temVal = temVal / 20.;
            temVal = Math.pow(10, temVal);
            zoomNeed[j] = temVal;
        }

        for (i = 0; i < 8; i++) {
            // ���㵱ǰ�εķŴ�б��
            zoom1 = (zoomNeed[i + 1] - zoomNeed[i]) / scanLenPer;
            for (j = 0; j < (int) scanLenPer; j++) {
                index = (int) (j + i * scanLenPer);
                mHardZoom[index] = zoomNeed[i] + zoom1 * j;
            }
        }
        // ///��������Ŵ���
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

    // ����Ӳ������:
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

    // ��������
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

    // JNI����
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

    // �豸�������
    private int RADARDEVICE_ERROR_NO = 0; // �豸����---��ȷ
    private int RADARDEVICE_ERROR_CHANGEMODUSBLTD = 0x1000; // �����豸�ļ����Դ���
    private int RADARDEVICE_ERROR_OPEN = 0x1001; // ���豸�ļ�����
    private int RADARDEVICE_ERROR_STARTCOMMAND = 0x1002; // ���� '��ʼ����' ����
    private int RADARDEVICE_ERROR_STOPCOMMAND = 0x1003; // ���� 'ֹͣ����' ����
    private int RADARDEVICE_ERROR_CLOSE = 0x1004; // �ر��豸�ļ�����

    // //�״��豸״̬
    // ��״̬��
    private int RADARDEVICE_NOOPEN = 0x0; // �豸û�д�
    private int RADARDEVICE_READY = 0x1; // �豸�������Ѿ����������򣬻�û�п�������
    private int RADARDEVICE_REALTIME = 0x100; // ʵʱ����״̬
    private int RADARDEVICE_CONTINUE = 0x10; // ��������ģʽ(ʱ��ģʽ)
    private int RADARDEVICE_DIANCE = 0x20; // ���ģʽ
    private int RADARDEVICE_WHEEL = 0x40; // �ֲ�ģʽ
    private int RADARDEVICE_PLAYBACK = 0x80; // �ط�ģʽ
    private int RADARDEVICE_CALIBRATE = 0x50; // У׼ģʽ
    private int RADARDEVICE_SAVING = 0x200; // ���ڱ�������
    private int RADARDEVICE_BACKPLAYING = 0x400; // ���ڻط�����
    private int RADARDEVICE_CUSTOMFILE = 0x800; // �Զ��������ļ�

    // ��������ʱ�豸��״̬
    private int RADARDEVICE_NOSAVING = 0; // û�б���
    private int RADARDEVICE_SAVING_TEMSTOP = 0x1; // ��ͣ����
    private int RADARDEVICE_SAVING_CONTINUE = 0x2; // ���ڱ���

    // �ط�����ʱ״̬
    private int RADARDEVICE_NOBACKPLAY = 0; // û�лط�
    private int RADARDEVICE_BACKPLAYING_TEMSTOP = 0x1; // ��ͣ�ط�

    private int mNowMode = RADARDEVICE_NOOPEN; // �״ﵱǰ״̬(��)
    private int mSaveMode = RADARDEVICE_NOSAVING; // �״ﱣ������ʱ����״̬
    private int mBackplayMode = RADARDEVICE_NOBACKPLAY; // �״����ݻط�ʱ����״̬

    private int HANDLEMODE_WHEEL = 1; // �����
    private int HANDLEMODE_NORMAL = 2; // �����ֱ�
    private int HANDLEMODE_COM = 3; // COM���ֱ�
    private int mHandleMode = HANDLEMODE_WHEEL;
    // ���˹���
    private boolean mBackOrientFlag = false; // ���˶�λ��־
    public int mHadBackScans = 0; // �Ѿ����˵ĵ���

    // �õ��ֱ�ģʽ
    public int getHandleMode() {
        return mHandleMode;
    }

    public void setHandle_wheelMode() {
        mHandleMode = HANDLEMODE_WHEEL;
        if (!isCanSendCommand())
            return;

        // ���ñ����չΪ0
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
        // ���ñ����չΪ0
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
        // ���ñ����չΪ0
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

        // //���豸
        ret = openUSB();
        if (ret == -1) {
            ret = RADARDEVICE_ERROR_OPEN;
            return ret;
        }

        return ret;
    }

    // �����ֱ�ģʽ
    public void setHandleMode() {
        if (mHandleMode == HANDLEMODE_WHEEL)
            setHandle_wheelMode();
        if (mHandleMode == HANDLEMODE_NORMAL)
            setHandle_normalMode();
        if (mHandleMode == HANDLEMODE_COM)
            setHandle_COMMode();
    }

    // /�����״�
    public int start() {
        int ret = RADARDEVICE_ERROR_NO;
        /*
         * ////�����豸������ try { // Missing read/write permission, trying to chmod
         * the file Process su; su =
         * Runtime.getRuntime().exec("/system/bin/su"); String cmd =
         * "chmod 777 " + "/dev/USBLTD" + "\n" + "exit\n";
         * su.getOutputStream().write(cmd.getBytes()); if ((su.waitFor() != 0) )
         * { throw new SecurityException(); } } catch (Exception e) {
         * e.printStackTrace(); return RADARDEVICE_ERROR_CHANGEMODUSBLTD; //
         * throw new SecurityException(); }
         */
        // //���豸
        ret = openUSB();
        if (ret == -1) {
            Log.d("debug_radar", "can not open usb ltd");
            ret = RADARDEVICE_ERROR_OPEN;
            return ret;
        } else {
            Log.d("debug_radar", "open usb: " + ret);
        }
        mNowMode = RADARDEVICE_READY; // �豸����״̬

        // ///���ò���
        int selIndex = mAntenFrqSel;
        // ���㲽������
        // CalStepParams1();
        CalStepParams();
        // ��������Ƶ��
        short[] commands = new short[4];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_RADARFRQ;
        commands[2] = 1;
        commands[3] = g_antenFrq[selIndex];
        sendCommands_1(commands, (short) 8);
        DebugUtil.i(TAG, "start ��Ƶ=" + g_antenFrq[selIndex]);
        // ����ȡ������
        setScanLenParams();
        // ��������
        setHardplus(mHardPlus);
        CalHardplusParams();
        setHardplusParams();
        // �����˲�����
        setFilterParams();
        // �����ź�λ�ò���
        setSignalParams();
        // ���ò�������
        setStepParams();
        // ����fad����
        setFADParams();
        // ���Ͷ�ȡ����
        setReadScansParams();

        // ���ñ����չֵΪ0
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_WHEELEXTNUMBER;
        commands[2] = 1;
        commands[3] = (short) 0;
        sendCommands_1(commands, (short) 8);

        // �Զ�����
        /*
         * short[] autoPlus=new short[12]; int i; for(i=0;i<12;i++) {
         * autoPlus[i] = 0; } autoPlus[0] = IOCTL_CODE_FLAG; autoPlus[1] =
         * CODE_SET_AUTOPLUS; autoPlus[2] = 0;
         * sendCommands_1(autoPlus,(short)24); for(i=0;i<9;i++) mHardPlus[i] =
         * autoPlus[3+i];
         */
        // //����ֹͣ������dsp�Ļ���
        stopUSB();
        // //���Ϳ�ʼ����
        ret = startUSB();
        if (ret == -1) {
            Log.d("debug_radar", "can not start usb ltd");
            ret = RADARDEVICE_ERROR_STARTCOMMAND;
            return ret;
        }

        ret = RADARDEVICE_ERROR_NO;
        mNowMode = RADARDEVICE_REALTIME; // ʵʱ�ɼ�ģʽ
        mNowMode |= RADARDEVICE_CONTINUE; // �����ɼ�ģʽ

        return ret;
    }

    // /ֹͣ�״�
    public int stop() {
        int ret = RADARDEVICE_ERROR_NO;

        // ���Ϳ�ʼ����
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

    // //���ݵ�ǰģʽ����ֹͣ����
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

    // //���ݵ�ǰ��ģʽ�����Ϳ�������
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

    // /���״��豸
    public int openDevice() {
        // //�����豸������
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

    // //�ر��״��豸
    public void closeDevice() {
        // ����������ڱ�������״̬
        if (isSavingMode()) {
            endSaveFile();
        }

        // ����������ڿ���״̬
        if (isRunningMode()) {
            stopUSB();
        }

        // �ر��豸
        closeUSB();

        mNowMode = RADARDEVICE_NOOPEN;
    }

    // װ�ض�̬��
    public boolean loadDriver() {
        // //װ������
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

    // ж�ض�̬��
    public void unloadDriver() {
        // ///ж����������
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

    // �Ƿ��ڱ�������״̬
    public boolean isSavingMode() {
        int ret;
        ret = mNowMode & RADARDEVICE_SAVING;
        if (ret == 0)
            return false;

        return true;
    }

    // ��������
    public void endSaveFile() {
        // ȥ�����ڱ������ݱ�־
        mNowMode = mNowMode & (~RADARDEVICE_SAVING);
    }

    // �Ƿ�����������״̬
    public boolean isRunningMode() {
        //
        int ret;
        ret = mNowMode & RADARDEVICE_REALTIME;
        if (ret == 0)
            return false;

        return true;
    }

    // ///��ȡָ�����ȵ�����(�ֽ���)��������
    public int FLAG_INDEX = 1;
    public int SAVEFLAG_INDEX = 2;
    private short POSITIVE_FLAG = 0x4000; // ��ת���
    private short NEGATIVE_FLAG = (short) 0x8000; // ��ת���
    private short OVERSPEED_FLAG = (short) 0x0001; // �ֲ�ģʽ�£��Ѿ�����
    private boolean mOverspeedFlag = false; // ����Ƿ���
    public int mFillposCursor = 0;

    // �Ƿ��ڻ���ģʽ
    public boolean isBackOrientMode() {
        return mBackOrientFlag;
    }

    //
    public void setBackFillPos(int pos) {
        mFillposCursor = 0;
    }

    // ��ʼ����ģʽ
    public void beginBackOrient() {
        DebugUtil.i(TAG, "1beginBackOrient enter!");
        mBackOrientFlag = true;
        mHadBackScans = 1;
        // ���Ͷ�ȡһ������
        setReadOneScanSpeedParams();
        MyApplication app;
        app = (MyApplication) (mContext).getApplicationContext();
        app.setRealthreadSleepTime(50);
        DebugUtil.i(TAG, "2finish beginBackOrient!");
    }

    // �˳�����ģʽ
    public void endBackOrient1() {
        DebugUtil.i(TAG, "3endBackOrient1 enter!");
        mBackOrientFlag = false;
        mHadBackScans = 0;
        // �ָ���ȡ����
        setReadScansParams();
        MyApplication app;
        app = (MyApplication) (mContext).getApplicationContext();
        app.setRealthreadSleepTime(200);
        DebugUtil.i(TAG, "4finish beginBackOrient!");
    }

    // �˳�����ģʽ
    public void endBackOrient() {
        // ��������ָ�룬ʹ�û���������Ч
        int needBackLength = mHadBackScans * mScanLength * 2;
        int bufIndex = mBufIndex;
        int wPos = mNowWPos[bufIndex];
        // Ҫ���˵����������һ����������
        if (wPos >= needBackLength) {
            mNowWPos[bufIndex] = wPos - needBackLength;
        } else
        // ��ʱ��Ҫ�л�����һ��������
        {
            // ��λ��ǰ������
            mNowWPos[bufIndex] = 0;
            needBackLength = needBackLength - wPos;
            /*
             * //�л���ǰһ�������� bufIndex = bufIndex-1; if(bufIndex<0) bufIndex =
             * mBufsNumber-1; // mBufIndex = bufIndex; mNowWPos[bufIndex] =
             * mNowWPos[bufIndex]-needBackLength;
             */
            // ������ڱ������ݣ���ʱӦ��ȴ�����˵�����
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

    // ��ȡ����
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

        // //���������ͣ����ģʽ��ֱ�ӷ���
        if (isSavingMode() && (isTemstopSaveMode()))
            return 0;

        // ��ӡ����
        // for(int i = 0; i < 10;i++)
        // {
        // DebugUtil.i(TAG,"����" + i + String.valueOf(Bufs[i]));
        // }

        // //ģ���ȡ����
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

        // //�Ա�ǽ��д���
        boolean isDiance; // �Ƿ��ǵ��
        int i;
        int j;
        int rScans; // Ҫ������״�����
        short flagVal; // ���
        int positiveScans = 0; // ��ת�����ݵ���
        int negativeScans = 0; // ��ת�����ݵ���
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

        // �ж��Ƿ����ֱ������
        for (i = 0; i < rScans; i++) {
            flag = Bufs[i * mScanLength + SAVEFLAG_INDEX];
            /*
             * //�Ǳ���� if(flag == HANDLEKEY_SAVE) { isSaveManage = true; } //�Ǳ�Ǽ�
             * if(flag == HANDLEKEY_MARK) { //�ڶ�Ӧ�����ݵ������ñ��
             * if(!isWhellMode()&&!isDiance) Bufs[i*mScanLength+FLAG_INDEX] |=
             * 0x4000; }
             */
        }

        MyApplication app;
        app = (MyApplication) (mContext).getApplicationContext();
        int srcW;
        srcW = app.getScreenWidth(); // ��ʾ�����
        if (this.isDianCeMode()) {
            // ��ÿһ�����ݽ��д��������Ǻͳ��ٵ�
            for (i = 0; i < rScans; i++) {
                Bufs[i * mScanLength + FLAG_INDEX] = 0;
            }
        }
        // //�ֲ�ģʽ�´��� ����
        else if (isWhellMode()) {
            // û�ж�ȡ�����ݣ�˵��������Ѿ�ֹͣ
            if (rScans == 0) {
                // ���ٱ������
                mOverspeedFlag = false;
            }

            // ��ÿһ�����ݽ��д��������Ǻͳ��ٵ�
            for (i = 0; i < rScans; i++) {
                short negativeFlag = NEGATIVE_FLAG;
                short positiveFlag = POSITIVE_FLAG;
                // �õ����
                flagVal = Bufs[i * mScanLength + FLAG_INDEX];

                // �õ��ֱ����
                // flag = Bufs[i*mScanLength+SAVEFLAG_INDEX];
                // ������㣬�ֲ�ģʽ�²���ʾ���
                Bufs[i * mScanLength + FLAG_INDEX] = 0;
                /*
                 * //���ֱ���꣬˵���û����ֲ�ģʽ�´���¼����ʱ���ñ��ֵ if((flag == HANDLEKEY_MARK))
                 * Bufs[i*mScanLength+FLAG_INDEX] |= POSITIVE_FLAG;
                 */

                // ����Ƿ��٣�ֻҪ��һ�����ݴ��ڳ��ٱ�־��������
                if ((flagVal & OVERSPEED_FLAG) != 0) {
                    mOverspeedFlag = true;
                    DebugUtil.i(TAG, "mOverspeedFlag Ϊtrue��");
                } else {
                    mOverspeedFlag = false;
                }

                // �����ת���,�����Ƕ�Ӧֵ
                if (mIsTurnWhell) {
                    negativeFlag = POSITIVE_FLAG;
                    positiveFlag = NEGATIVE_FLAG;
                }
                // ��ת
                if ((flagVal & negativeFlag) != 0) {
                    // ��ת���ݵ�������һ
                    negativeScans++;
                    continue;
                }
                // ��ת
                else if ((flagVal & positiveFlag) != 0) {
                    // ��¼��������
                    for (j = 0; j < mScanLength; j++)
                        positiveDatas[positiveScans * mScanLength + j] = Bufs[i * mScanLength + j];
                    // ��ת���ݵ�������һ
                    positiveScans++;
                    continue;
                }
                // ���������־��û���򲻶�ȡ
                else
                    return 0;
            }
            // ��������ݵ��� > �������:��ʱ���˶�λģʽ
            if (negativeScans > positiveScans) {
                // �Ѿ��ǻ��˶�λģʽ����ʱ���ӻ��˵�������
                if (isBackOrientMode()) {
                    mHadBackScans += (negativeScans - positiveScans);
                } else
                // ��û�н�����˶�λģʽ
                {
                    // ������˶�λģʽ
                    beginBackOrient();
                    // ���û��˵�������ֵ
                    mHadBackScans = (negativeScans - positiveScans);
                }
                // //������Ļ��ʾ�Ļ�����λ��
                mFillposCursor += (negativeScans - positiveScans);
                // ��������ൽ��Ļ�����
                if (mFillposCursor > srcW)
                    mFillposCursor = srcW;
            }
            // �������ݵ���С�ڵ����������ݵ���(�����������)
            else {
                // ���˶�λģʽ��
                if (isBackOrientMode()) {
                    // ���Ļ��˵�������ֵ
                    mHadBackScans = mHadBackScans - (positiveScans - negativeScans);
                    // ��С������λ��
                    delFillposCursor(positiveScans - negativeScans);
                    // ������˵��������Ѿ�<=0,˵�����˽���
                    if (mHadBackScans <= 0) {
                        // �������˲���
                        endBackOrient1();
                        mHadBackScans = 0;
                    } else
                    // ���˻�û�н�������ʱֱ�ӷ���
                    {
                        // ��ʱ���û�û��ѡ�񡰶����������ݡ���ԭ��������Ч�����踲��
                        return 0;
                    }
                }
                // ///�����ڻ��˶�λģʽ���߻��˶�λ������
                // ��ʱ���ͬʱ���� ��ת�ͷ�ת ,��Ч����Ϊ��ת�뷴ת����֮��
                if (negativeScans > 0) {
                    // ����������ݣ������ö�ȡ���ݳ���
                    int num;
                    num = positiveScans - negativeScans;
                    rLen = num * mScanLength * 2;
                    for (int m = 0; m < num; m++) {
                        for (int mm = 0; mm < mScanLength; mm++) {
                            Bufs[m * mScanLength + mm] = positiveDatas[
                                    (negativeScans + m) * mScanLength + mm];
                        }
                    }
                    // //���ǻ��˶�λģʽ�������ڻ��˶�λ���û�ѡ���ˡ������������ݡ�ѡ���ʱҪ��С ������λ��
                    delFillposCursor(num);
                }
                /*
                 * ////�� ��Ļ��� ���д��� if(mNeedSmallMark) { Bufs[FLAG_INDEX] =
                 * NEGATIVE_FLAG; mNeedSmallMark = false; } if(mNeedBigMark) {
                 * Bufs[FLAG_INDEX] = POSITIVE_FLAG; mNeedBigMark = false; }
                 */
            }
            // //�ж��Ƿ��ǻ��˲���
            if (mHadBackScans > 0) {
                // ��¼��ǰ�����λ��
                return 0;
            }
        }

        // //���û�еõ�����
        if (rLen <= 0)
            return 0;
        // //�� ��Ļ��� ���д���
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

        // ���浱ǰһ�����ݣ������ڵ���������ͼ����ʾ
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
         * //����Ƿ��û������ �����ֱ� �ı��水�� if(isSaveManage) { manageHandleSaveKey(); }
         */
        // �����ݿ�������������
        if (isWhellMode()) {
            // �����û�в�����ǰ�Ļ��˶�λ����
            if (shortLen / mScanLength > mFillposCursor)
                mHadRcvScans += (shortLen / mScanLength - mFillposCursor);
        } else // ����ģʽ
        {
            mHadRcvScans += shortLen / mScanLength;
        }

        // //��������䵽���ݻ������������л������л��ͱ������ݲ���
        int bufIndex;
        int wPos;
        int wLength;
        int addNum;
        addNum = 0;
        bufIndex = mBufIndex; // ��ǰ����������
        wPos = mNowWPos[bufIndex]; // ��ǰ������д��λ��
        wLength = rLen; // Ҫд������ݳ���

        // �򻺳������������
        if (wPos + wLength > mBufLength) {
            wLength = mBufLength - wPos;
            for (i = 0; i < wLength / 2; i++) {
                mDatasBufs[bufIndex][wPos + i * 2] = (byte) (Bufs[i]);
                mDatasBufs[bufIndex][wPos + i * 2 + 1] = (byte) (Bufs[i] >> 8);
            }
            // �������������紦�ڱ���ģʽ����ʱ��������
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
        // ����ʣ�������
        if (wLength < 0)
            wLength = 0;
        for (i = 0; i < wLength / 2; i++) {
            mDatasBufs[bufIndex][wPos + i * 2] = (byte) (Bufs[i + addNum / 2]);
            mDatasBufs[bufIndex][wPos + i * 2 + 1] = (byte) (Bufs[i + addNum / 2] >> 8);
        }
        mNowWPos[bufIndex] += wLength;

        // ������ڽ������߿��ƣ������������ݰ�

        // if(mWifiSendDatas)
        // app.mWifiDevice.sendDatas(Bufs,rLen);

        return rLen;
    }

    // ������������
    public void discardBackDatas() {
        // ��������ָ�룬ʹ�û���������Ч
        int needBackLength = mHadBackScans * mScanLength * 2;
        int bufIndex = mBufIndex;
        int wPos = mNowWPos[bufIndex];
        // Ҫ���˵����������һ����������
        if (wPos >= needBackLength) {
            mNowWPos[bufIndex] = wPos - needBackLength;
        } else
        // ��ʱ��Ҫ�л�����һ��������
        {
            // ��λ��ǰ������,�ӵ�ǰλ�ÿ�ʼ
            mNowWPos[bufIndex] = 0;
            // ������ڱ������ݣ���ʱӦ��ȴ�����˵�����
            if (isSavingMode()) {
                // Ҫ����������
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

        // ���˶�λ����
        mBackOrientFlag = false;
        mHadBackScans = 0;
    }

    // �����水��
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

    // �ж��Ƿ���ͣ����
    public boolean isTemstopSaveMode() {
        return ((mNowMode & RADARDEVICE_SAVING) != 0) && (mSaveMode == RADARDEVICE_SAVING_TEMSTOP);
    }

    // ��С��
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

    // ����
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

    // ��������
    public void continueSave() {
        mSaveMode = RADARDEVICE_SAVING_CONTINUE;
    }

    // ��ͣ����
    public void tempStopSave() {
        mSaveMode = RADARDEVICE_SAVING_TEMSTOP;
    }

    // ����GPS��Ϣ
    public void SaveGPSRecord() {
        MyApplication app;
        app = (MyApplication) mContext.getApplicationContext();
        app.saveGPSRecord();
    }

    // ��һ�������������ݱ��浽sd����
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
    } // �������һ�����ݱ���

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

    // �õ����һ������
    public short[] getRecentScanDatas() {
        //
        return mOneScanDatas;
    }

    // �ж��Ƿ���Է�������(���������Ƿ��Ѿ���)
    public boolean isCanSendCommand() {
        // DebugUtil.i(TAG,"Now isCanSendCommand:"+mNowMode);
        if (mNowMode == RADARDEVICE_NOOPEN)
            return false;
        //
        return true;
    }

    // ���ý�����ģʽ
    public boolean setDianceMode(int extNumber) {
        if (extNumber <= 0) {
            extNumber = 1;
        } else if (extNumber >= 32768) {
            extNumber = 32768;
        }

        mDianceNumber = extNumber;

        if (!isCanSendCommand())
            return false;
        // ֹͣ�״�
        stopRadar();

        if (this.isWhellMode())
            setBackFillPos(0);
        else
            ;

        // ��������
        short[] commands = new short[4];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_DIANCEEXTNUMBER;
        commands[2] = 1;
        commands[3] = (short) mDianceNumber;
        sendCommands_1(commands, (short) 8);

        mNowMode &= (~0xF0);
        mNowMode |= RADARDEVICE_DIANCE;

        // �����״�
        mHadRcvScans = 0;
        startRadar();

        mFileHeader.setDianceMode();

        return true;
    }

    public void setDianceDistance(int distance) {
        mFileHeader.setDianceDistance(distance);
    }

    // ���ý����ֲ�ģʽ
    public boolean setWheelMode(int extNumber) {
        mWheelExtendNumber = extNumber;

        if (!isCanSendCommand())
            return false;

        // ֹͣ�״�
        stopRadar();
        // ���Ͷ�ȡ����
        setReadScansParams();

        // ��������
        short[] commands = new short[4];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_WHEELEXTNUMBER;
        commands[2] = 1;
        commands[3] = (short) mWheelExtendNumber;
        sendCommands_1(commands, (short) 8);

        mNowMode &= (~0xF0);
        mNowMode |= RADARDEVICE_WHEEL;

        // �ָ����˶�λ����
        mBackOrientFlag = false;
        mHadBackScans = 0;

        // �����״�
        mHadRcvScans = 0;
        startRadar();

        // ͷ�ļ�
        mFileHeader.setWhellMode();
        mFileHeader.setFlagExtent(extNumber);
        mFileHeader.setTouchDistance(
                mWheelInterDistance[mWheeltypeSel] * mWhellcheckCoeff[mWheeltypeSel]);

        return true;
    }

    // ���ûط�ģʽ
    public boolean setPlayBackMode() {
        mNowMode &= (~0xF0);
        mNowMode |= RADARDEVICE_PLAYBACK;
        return true;
    }

    public void turnWhell(boolean isTurn) {
        mIsTurnWhell = isTurn;
    }

    // �õ�������Ƿ�ת��״̬
    public boolean getTurnWheel() {
        return mIsTurnWhell;
    }

    // ����ɨ��,��ɨ���ٶ�ȡ������˵�ֵ��Ҫ����256*1024��
    public void setScanSpeed(int selIndex) {
        if (selIndex < 0)
            selIndex = 0;
        else if (selIndex > g_scanSpeedNumber)
            selIndex = g_scanSpeedNumber;
        // �Բ������к����Լ���
        int scanSpeed = g_scanSpeed[selIndex];
        // �������ֵ��֮ǰֵ��ͬ
        if (scanSpeed == mScanSpeed)
            return;
        else
            ;

        // �ж�ֵ�ķ�Χ
        if (scanSpeed * mScanLength <= MAXSPEEDSCANLENTH) {
            mScanSpeedSel = selIndex;
            mScanSpeed = scanSpeed;

            if (!isCanSendCommand())
                return;
            DebugUtil.i(TAG, "setScanSpeed:=" + mScanSpeed + "/s");
            sendScanSpeed();// ����ɨ���ٶ�
            mFileHeader.setScanspeed((short) mScanSpeed);
        } else {
            // DebugUtil.infoDialog(mContext, "��ʾ", "��������!");
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

    // ����ɨ���ٶ����ָ��
    private void sendScanSpeed() {
        DebugUtil.i(TAG, "mScanspeed = " + mScanSpeed);
        // //���㲽������
        CalStepParams();
        // //���Ͳ�������
        // ֹͣ�״�
        if (!isDianCeMode())
            stopRadar();
        // �����ź�λ�ò���
        setSignalParams();
        // ���ò�������
        setStepParams();
        // ����fad����
        setFADParams();
        // ����ɨ���ٶȣ�����ÿ�βɼ��ĵ���
        setReadScansParams();
        // �����״�
        if (!isDianCeMode())
            startRadar();
        mHadRcvScans = 0;
    }

    // ��������ʱ���͵�ɨ���ٶ�
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

    // ������ڽ������ָ�ɨ���ٶ�
    public void resetScanSpeed() {
        DebugUtil.i(TAG, "enter resetScanSpeed");
        sendScanSpeed();
    }

    public void setContinueScanSpeedValue(int speed) {
        mScanSpeed = speed;
    }

    // ���������������ٶ�
    public void setContinueScanSpeed(int speed) {
        // ���û�仯�Ͳ�������
        if (mScanSpeed == speed)
            return;
        else
            ;

        mScanSpeed = speed;
        DebugUtil.i(TAG, "setScanSpeed:=" + mScanSpeed + "/s");
        // //����Ǳ�״����

        if (!isCanSendCommand())
            return;

        // //���㲽������
        CalStepParams();
        // //���Ͳ�������
        // ֹͣ�״�
        if (!isDianCeMode())
            stopRadar();
        // ����ȡ����������
        setScanLenParams();
        // �����ź�λ�ò���
        setSignalParams();
        // ���ò�������
        setStepParams();
        // ����fad����
        setFADParams();
        // ����ɨ��
        setReadScansParams();
        // �����״�
        mHadRcvScans = 0;
        if (!isDianCeMode())
            startRadar();

        mFileHeader.setScanspeed((short) mScanSpeed);
        MultiModeLifeSearchActivity activity;
        activity = (MultiModeLifeSearchActivity) mContext;
        // activity.setNowSpeedRange();
    }

    // ���������ź�λ�õ�ֵ
    public void setSignalposValueOnly(int pos) {
        mSignalPos = pos;
    }

    // �����ź�λ��
    public void setSignalpos(int pos) {
        mSignalPos = pos;

        if (!isCanSendCommand())
            return;
        // //���㲽������
        CalStepParams();
        // //���Ͳ�������
        // ֹͣ�״�
        DebugUtil.i(TAG, "1.setSignalpos stopRadar");
        if (!isDianCeMode())
            stopRadar();
        DebugUtil.i(TAG, "2.setSignalpos stopRadar setSignalParams begin");
        setSignalParams();
        DebugUtil.i(TAG, "3.setSignalpos setSignalParams finish");
        // �����״�
        mHadRcvScans = 0;
        DebugUtil.i(TAG, "4.setSignalpos startradar begin");
        if (!isDianCeMode()) {
            startRadar();
            // �ظ�һ�ο�ʼ�����ֹ��������
            // startRadar();
        }
        DebugUtil.i(TAG, "5.setSignalpos startradar finish");
    }

    // ����ȡ������ֵ����������
    // �ж�ɨ����ȡ����ĳ��Ƿ�С��MAXSPEEDSCANLENTH
    public void setScanLengthValue(int selIndex) {
        // �ж�ֵ��Χ
        int scanLength = g_scanLen[selIndex];
        if (scanLength * mScanSpeed <= MAXSPEEDSCANLENTH) {
            mScanLengthSel = selIndex;
            mScanLength = scanLength;
            mFileHeader.rh_nsamp = (short) mScanLength;
        } else {
            // DebugUtil.infoDialog(mContext, "��ʾ", "���ȵ���ɨ���ٶ�!");
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

    // ����ȡ������
    public void setScanLength(int selIndex) {
        if (mScanLength == g_scanLen[selIndex])
            return;
        else
            ;
        setScanLengthValue(selIndex);
        // //���㲽������
        CalStepParams();
        // //���Ͳ�������
        // ֹͣ�״�
        if (!isDianCeMode())
            stopRadar();
        // ����ȡ����������
        setScanLenParams();
        // �����ź�λ�ò���
        setSignalParams();
        // ���ò�������
        setStepParams();
        // ����fad����
        setFADParams();

        // �����״�
        mHadRcvScans = 0;
        if (!isDianCeMode())
            startRadar();
    }

    // �Զ�����
    public boolean autoPlus() {
        // �Զ�����
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

        // �����״�
        mHadRcvScans = 0;

        // if(!isDianCeMode())
        // startRadar();

        mIsAutoplus = true;
        DebugUtil.i(TAG, "mIsAutoplus:=" + mIsAutoplus);

        return true;
    }

    // �����˲�����
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

    // ���ò��������
    public void setWhellType(int type) {
        try {
            if (type > this.MWHEELMAXINDEX) {
                throw new Exception();
            } else
                ;
            mWheeltypeSel = type;
        } catch (Exception e) {
            DebugUtil.e(TAG, "setWhellType wheel indexԽ�磡");
        }
    }

    /**
     * ���ò������˲�����
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
        // ֹͣ�״�
        if (!isDianCeMode())
            stopRadar();
        sendCommands_1(commands, (short) 10);
        // �����״�
        mHadRcvScans = 0;
        if (!isDianCeMode())
            startRadar();
    }

    // ���ý�糣����Χ
    // ��糣������[1,100]
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

    // ���õ���ƽ��
    public void setScanAve(int ave) {
        // ����ȡֵ��Χ
        if (ave > 500) {
            ave = 500;
        } else if (ave < 0) {
            ave = 1;
        }

        mScanAve = ave;

        if (!isCanSendCommand())
            return;
        // ֹͣ�״�
        if (!isDianCeMode())
            stopRadar();

        // ���ñ�������
        short[] commands = new short[10];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_SCANAVE;
        commands[2] = 1;
        commands[3] = (short) mScanAve;

        sendCommands_1(commands, (short) 8);
        // �����״�
        mHadRcvScans = 0;
        if (!isDianCeMode())
            startRadar();
    }

    public boolean isAve() {
        if (mScanAve <= 1)
            return false;
        return true;
    }

    // ���ñ�������
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
        // ֹͣ�״�
        if (!isDianCeMode())
            stopRadar();

        // ���ñ�������
        short[] commands = new short[10];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_REMBACK;
        commands[2] = 1;
        commands[3] = isRem;
        //
        sendCommands_1(commands, (short) 8);
        // �����״�
        mHadRcvScans = 0;
        if (!isDianCeMode())
            startRadar();
    }

    // ����������Ƶ
    public void setAntenFrq(int selIndex) {
        // ����������Ƶ
        DebugUtil.i(TAG, "����������ƵselIndex=" + selIndex);
        Log.d("debug", "selIndex: " + selIndex);
        mAntenFrqSel = selIndex;
        mFileHeader.rh_spp = g_antenFrq[mAntenFrqSel];
        mRepFrq = g_fixRepFrqNum[selIndex];
        // DebugUtil.i(TAG, "�����ظ�Ƶ��="+String.valueOf(mRepFrq));
        // DebugUtil.infoDialog(mContext, "�ظ�Ƶ��",
        // "�ظ�Ƶ��="+String.valueOf(mRepFrq));
    }

    // ����Ĭ���״�������ڻָ�Ĭ���״����ʱʹ��
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

        // ����Ӳ�����淶Χֵ
        commands[1] = CODE_SET_HARDPLUSRANGE;
        commands[2] = 1;
        commands[3] = g_hardplusrange[mAntenFrqSel];
        sendCommands_1(commands, (short) 8);

        if (!isDianCeMode())
            startRadar();
    }

    // /ֻ���õ�������
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

    // /����ÿ�ζ�ȡ�ĵ���
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

    // ���ö�ȡ����Ϊ1,�ڻ��˵�ʱ��ʹ��
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

    // /ֻ���ò�������
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

        // DebugUtil.infoDialog(mContext, "�ظ�Ƶ��=",
        // String.valueOf(mStepParams[3]));

        sendCommands_1(commands, (short) 16);
    }

    // /�����豸�ĵ�ǰ״̬
    private int mStatusLength = 20; //

    public void sendNowSystemStatus() {
        MyApplication app;
        app = (MyApplication) mContext.getApplicationContext();
        // if(!app.mWifiDevice.isSndHadConnect())
        // return;
        // ////���ɵ�ǰ�״�״̬�����ݰ�
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

    // /ֻ�����ź�λ�ò���
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

    // ���������������ļ���
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

    // �����µ������ļ���
    public String createNewFileName() {
        // �������е��ļ��������������Ǹ�
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
                // �ж���һ���ļ��л���һ���ļ�
                if (currentFile.isDirectory()) {
                } else {
                    // ȡ���ļ���
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

    // �����µı��������ļ�
    public boolean createNewDatasFile() {
        // �����ļ���
        String folderPath;
        folderPath = mStoragePath // android.os.Environment.getExternalStorageDirectory()
                     + mLTEFilefolderPath;
        File lteDir = new File(folderPath);
        if (!lteDir.exists()) {
            lteDir.mkdirs();
        }

        // ���ɱ����ļ����
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

    // �õ����������ļ���
    public String getSaveFilename() {
        return mSavingFilePath;
    }

    // �õ���ǰ��״̬��
    public int getNowMode() {
        return mNowMode;
    }

    // ����ģʽ״̬
    public void setMode(int inputMode) {
        mNowMode &= (~0xF0);
        mNowMode |= inputMode;
    }

    // �ж��Ƿ����ֲ�ģʽ
    public boolean isWhellMode() {
        if ((mNowMode & RADARDEVICE_WHEEL) == RADARDEVICE_WHEEL)
            return true;
        return false;
    }

    // �ж��Ƿ���ʱ��ģʽ
    public boolean isTimeMode() {
        if ((mNowMode & RADARDEVICE_CONTINUE) == RADARDEVICE_CONTINUE)
            return true;
        return false;
    }

    // �ж��Ƿ��ǵ��ģʽ
    public boolean isDianCeMode() {
        if ((mNowMode & RADARDEVICE_DIANCE) == RADARDEVICE_DIANCE)
            return true;
        return false;
    }

    // �ж��Ƿ��ǻط�ģʽ
    public boolean isPlayBackMode() {
        if ((mNowMode & RADARDEVICE_PLAYBACK) == RADARDEVICE_PLAYBACK)
            return true;
        return false;
    }

    // ����һ�ε������
    public void onceDianCe() {
        // ֹͣ�״�
        // stopRadar();

        // ��������
        short[] commands = new short[3];
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_ONCEDIANCE;
        commands[2] = (short) mDianceNumber;
        //
        sendCommands_1(commands, (short) 6);

        //
        mNowMode &= (~0xF0);
        mNowMode |= RADARDEVICE_DIANCE;

        // �����״�
        // startRadar();
    }

    //
    public void beginWifiSendDatas() {
        mWifiSendDatas = true;
        // ����wifi�����߳�

    }

    public void stopWifiSendDatas() {
        mWifiSendDatas = false;
        // ֹͣwifi�����߳�
    }

    // //������������
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
    // //��ʼ��������
    // case RADAR_COMMAND_BEGSAVE:
    // {
    // activity.beginSave();
    // break;
    // }
    // //ֹͣ��������
    // case RADAR_COMMAND_ENDSAVE:
    // {
    // activity.stopSave();
    // break;
    // }
    // //�ź�λ��
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
    // //���������ź�λ����ʾֵ
    // activity.setRealtimeParamsListAdapterText(val1,
    // activity.COMMAND_SIGNALPOS_ID);
    // break;
    // }
    // //ʱ��
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
    // //ɨ��
    // case RADAR_COMMAND_SCANSPEED:
    // {
    // val = params[0];
    // setContinueScanSpeed(val);
    // //
    // activity.setRealtimeParamsListAdapterText(val,
    // activity.COMMAND_SCANSPEED_ID);
    // break;
    // }
    // //ɨ�����
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
    // //�˲�
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
    // //�������
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
    // //����Ƶ��
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
    // //�����״�
    // case RADAR_COMMAND_START:
    // {
    // startRadar();
    // break;
    // }
    // //ֹͣ�״�
    // case RADAR_COMMAND_STOP:
    // {
    // stopRadar();
    // break;
    // }
    // //����ƽ������
    // case RADAR_COMMAND_SETSCANAVE:
    // {
    // short scanAve = params[0];
    // activity.setRealtimeParamsListAdapterText(scanAve,activity.COMMAND_AVERAGE_ID);
    // activity.radarSetScanave(scanAve);
    // break;
    // }
    // //��������
    // case RADAR_COMMAND_REMBACKGROUND:
    // {
    // short isRemove = params[0];
    // activity.manageRemoveBack(isRemove);
    // break;
    // }
    // //��������ģʽ
    // case RADAR_COMMAND_CONTINUEMODE:
    // {
    // activity.radarTimeMode();
    // break;
    // }
    // //����ֹ���ģʽ
    // case RADAR_COMMAND_WHELLMODE:
    // {
    // val = params[0];
    // setWheelMode(val);
    // break;
    // }
    //
    // //��⹤��ģʽ
    // case RADAR_COMMAND_DIANCEMODE:
    // {
    // val = params[0];
    // setDianceMode(val);
    // String txt="Diance mode:"+val;
    // activity.showToastMsg(txt);
    // break;
    // }
    // //�õ��������
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

        // ����ȡ������
        mScanLengthSel = g_defParamsForRadar[index][1];
        int scanLength = g_scanLen[mScanLengthSel];
        mScanLength = scanLength;
        mFileHeader.rh_nsamp = (short) mScanLength;

        // ����ɨ��
        // mScanSpeedSel = g_defParamsForRadar[index][2];
        // int scanSpeed = g_scanSpeed[mScanSpeedSel];
        int scanSpeed = g_defParamsForRadar[index][2];
        mScanSpeed = scanSpeed;

        // ����ʱ��
        mTimeWindow = g_defParamsForRadar[index][3];
        mFileHeader.rh_range = mTimeWindow;
        // �����ź�λ��
        mSignalPos = g_defParamsForRadar[index][4];
        // ������������
        int i;
        for (i = 0; i < 9; i++) {
            mHardPlus[i] = g_defParamsForRadar[index][5];
        }
        // ������ƫ
        mZeroOff = g_defParamsForRadar[index][6];
        // �����˲�
        mFilterSel = g_defParamsForRadar[index][7];
        // ����Ĭ�ϲ����
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
     * �ļ�����ʧ����ʹ��Ĭ�ϲ�������
     *
     * @param index
     */
    public void changeWheelPropertyFromAnteFrq(int index) {
        // ����Ĭ�ϲ����
        mWheeltypeSel = g_defParamsForRadar[index][8];
        // ����Ĭ�ϱ����չ
        mWheelExtendNumber = g_wheelextendnum[mWheeltypeSel];
    }

    // ��������Ƶ������Ĭ�ϲ���
    /*
     * index:����Ƶ������ֵ; channel:ͨ������
     */
    public void setAntenDefaultParams(int index) {
        // ����ȡ������
        mScanLengthSel = g_defParamsForRadar[index][1];
        int scanLength = g_scanLen[mScanLengthSel];
        mScanLength = scanLength;
        mFileHeader.rh_nsamp = (short) mScanLength;
        // ����ɨ��
        // mScanSpeedSel = g_defParamsForRadar[index][2];
        // int scanSpeed = g_scanSpeed[mScanSpeedSel];
        int scanSpeed = g_defParamsForRadar[index][2];
        mScanSpeed = scanSpeed;
        // ����ʱ��
        mTimeWindow = g_defParamsForRadar[index][3];
        mFileHeader.rh_range = mTimeWindow;
        // �����ź�λ��
        mSignalPos = g_defParamsForRadar[index][4];
        // ������������
        int i;
        for (i = 0; i < 9; i++) {
            mHardPlus[i] = g_defParamsForRadar[index][5];
        }
        // ������ƫ
        mZeroOff = g_defParamsForRadar[index][6];
        // �����˲�
        mFilterSel = g_defParamsForRadar[index][7];
        // ����Ĭ�ϲ����
        mWheeltypeSel = g_defParamsForRadar[index][8];
        DebugUtil.i(TAG, "�ָ�����mWheeltypeSel=" + mWheeltypeSel);

        if (!isCanSendCommand())
            return;
        // //ֹͣ�״�
        if (!isDianCeMode())
            stopRadar();
        // //���㲽������
        // CalStepParams1();
        CalStepParams();
        // ���ò�������
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
        // ����fad����
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_FAD;
        commands[2] = 1;
        commands[3] = mFAD;
        sendCommands_1(commands, (short) 8);
        // �����ź�λ�ò���
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_SIGNALPOS;
        commands[2] = 8;
        for (i = 0; i < 8; i++)
            commands[3 + i] = mSignalPosParams[i];
        sendCommands_1(commands, (short) 22);
        // �����˲�����
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_LVBO;
        commands[2] = 2;
        commands[3] = g_lvboParams[mFilterSel][0];
        commands[4] = g_lvboParams[mFilterSel][1];
        sendCommands_1(commands, (short) 10);
        // ����ȡ������
        commands[0] = IOCTL_CODE_FLAG;
        commands[1] = CODE_SET_SCANLEN;
        commands[2] = 1;
        commands[3] = (short) mScanLength;
        sendCommands_1(commands, (short) 8);
        // ����Ӳ���棬����Ӳ����
        CalHardplusParams();
        setHardplusParams();
        // �Ѳɼ��ĵ���
        mHadRcvScans = 0;
        // ����ƽ��
        mScanAve = 4;
        // ��糣��
        mJiedianConst = 9;// hss20161210
        // ����λ��
        mSelectStorageIndex = 0;
        setStoragePath(mSelectStorageIndex);
        // �����״�
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

    // //////////////////��״��������
    // public void FSAnten_Start()
    // {
    // FSAntenCommand com = new FSAntenCommand();
    // com.m_IsRadarStart = 1;
    //
    // //�����������ݰ�
    // FSAntenNetPacket packet = new FSAntenNetPacket();
    // packet.createCOMPacket(com);
    //
    // //���������ݰ��ӵ����Ͷ�����
    // LTDMainActivity activity;
    // MyApplication app;
    // activity = (LTDMainActivity)mContext;
    // app = (MyApplication)activity.getApplication();
    // app.mWifiDevice.addFSAntenSendPacket(packet);
    //
    // //����ģʽ
    // mNowMode = RADARDEVICE_REALTIME; //ʵʱ�ɼ�ģʽ
    // mNowMode |= RADARDEVICE_CONTINUE; //�����ɼ�ģʽ
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
    // //�����������ݰ�
    // FSAntenNetPacket packet = new FSAntenNetPacket();
    // packet.createCOMPacket(com);
    //
    // //���������ݰ��ӵ����Ͷ�����
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

    // /////////////////////////���ݴ�����
    public void FIRFilter() {
        int i, j, ii, k;
        double fs;
        double[] h = new double[1024];
        float Data; // �м����
        fs = mScanLength / 1.;
        fs = fs / mTimeWindow * 1000; // �״����Ƶ�ʣ�ת��ΪMHz
        double fln;
        double fhn;
        fln = g_antenFrq[mAntenFrqSel] / 4.;
        fhn = g_antenFrq[mAntenFrqSel] * 2;
        fln = fln / fs;
        fhn = fhn / fs;

        firwin(30, 3, fln, fhn, 1, h); // �������ü����˲�����ϵ��,�˲�����30,��ѡ��������������
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
               double h[]) // ��ΪҪ���˲���ϵ�����ȸߣ���������ȡdouble��
    {
        int i, n2, mid;
        double s, pi, wc1, wc2, beta, delay;
        beta = 0.0;
        if (wn == 7) // ���ѡ���ǿ���������Ҫ����������һ������������beta
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

    // /////ƽ������
    public int AVESCANS = 10;
    public short[] m_aveInterRes = new short[8192];
    public int ave_num = 0;

    public void AVEProcess() {
        int i = 0, j = 0;
        float ave_numadd = 1;

        // aveScansΪƽ������
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
    // ���������
    public String[] mWhellName = {
            /* "WDMI-900", */
            "WDMI-300", "WDMI-500", "WDMI-55A", "LDMI-130", "GC1500MHz",
            /*
	 * "GC2000MHz����", "HF900MHz����",
	 */
            "�Զ�������",};

    public int mNowManageCommandID = 0; // ��ʾ���ڴ����ĸ�����
    public int COMMAND_ID_SETSCANSPEED = 2; // ����ɨ��
    public int COMMAND_ID_SETTIMEWINDOW = 3; // ����ʱ��
    public int COMMAND_ID_SETSCANLENGTH = 4; // ���õ���
    public int COMMAND_ID_SETSINGLEPOS = 5; // �����ź�λ��
    public int COMMAND_ID_SETALLHARDPLUS = 7; // ������������
    public int COMMAND_ID_SETSTEPHARDPLUS = 8; // ���õ�������
    public int COMMAND_ID_SETFILTER = 9; // �����˲�
    public int COMMAND_ID_SETJIEDIANCONST = 10;// ���ý�糣��
    public int COMMAND_ID_SETAVESCAN = 11; // ����
    public int COMMAND_ID_SETDIANCE = 12; // ���õ��
    public int COMMAND_ID_SETWHELLMODE = 13; // �����ֲ�
    public int COMMAND_ID_SETCOLORPAL = 14; // ���õ�ɫ��
    public int COMMAND_ID_SETPLAYBACK = 15; // ���ûط�
    public int COMMAND_ID_SETDELETE = 16; // ����ɾ��
    public int COMMAND_ID_CALIBRATE = 17; // ����У��
    public int COMMAND_ID_EXTENDNUM = 18; // �����չ
    public int COMMAND_ID_DIAMETER = 19; // ����ֱ��
    public boolean mIsAutoplus = false;

    // �����û��������õ�����Id
    public void setNowSetting_CommandID(int id) {
        mNowManageCommandID = id;
    }

    // ��õ�ǰ����ֵ
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

    // ���ûط�
    public boolean isSetting_PlayBack_Command() {
        return mNowManageCommandID == COMMAND_ID_SETPLAYBACK;
    }

    // ����ɾ��
    public boolean isSetting_Delete_Command() {
        return mNowManageCommandID == COMMAND_ID_SETDELETE;
    }

    // ����У��
    public boolean isSetting_Calibrate_Command() {
        return mNowManageCommandID == COMMAND_ID_CALIBRATE;
    }

    // ���ñ����չ
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

    // ȡ���Զ�����
    public void cancelAutoPlus() {
        int i;
        for (i = 0; i < 9; i++)
            mHardPlus[i] = mBackHardPlus[i];
        for (i = 0; i < 9; i++)
            mRealHardPlus[i] = mHardPlus[i];

        CalHardplusParams();

        // ֹͣ�״�
        if (!isDianCeMode())
            stopRadar();

        setHardplusParams();

        // �����״�
        mHadRcvScans = 0;

        if (!isDianCeMode())
            startRadar();

        // �ظ�һ�ο�ʼ�����ֹ��������
        // if(!isDianCeMode())
        // {
        // startRadar();
        // //�ظ�һ�ο�ʼ�����ֹ��������
        // startRadar();
        // }

        mFileHeader.setHardPlus(mHardPlus);

        // ����ʱ��ģʽ
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

    // �����Զ����ļ�·��
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
