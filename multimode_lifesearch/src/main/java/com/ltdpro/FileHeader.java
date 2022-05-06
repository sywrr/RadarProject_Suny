package com.ltdpro;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/*
 * 
//文件头的长度
#define HEADER_LENGTH			1024		//文件头长度
//道间距最大值最小值(cm)
#define MAX_INTERVAL_PER_SCAN	10000		//道间距最大值(cm)
#define MIN_INTERVAL_PER_SCAN	0.01		//道间距最小值(cm)
//时窗最大值(ns)
#define MAX_TIME_WINDOW			10000		//时窗最大值(ns)
//天线频率的最大值最小值(MHz)
#define MAX_ANTENNA_FREQ		10000		//天线频率的最大值(MHz)
#define MIN_ANTENNA_FREQ		1			//天线频率的最小值(MHz)
//桩号最大千米数
#define MAX_KILO_PEG			10000		//桩号最大千米数
//LTE文件的标记值
#define BIG_MARK_VALUE			0x4000		//大标值
#define SMALL_MARK_VALUE		0x8000		//小标值
#define MARK_INDEX				1			//标记数据索引
//水平标尺的显示方式(0或其它:什么也不显示,1:道号,2:距离,3:桩号)
#define HRULER_SHOWNONE			0
#define HRULER_SHOWSCAN			1
#define HRULER_SHOWDISTANCE		2
#define HRULER_SHOWPEG			3
//显示桩号的方式(0或其他:道号,1:标记)
#define SHOWPEGFROMSCAN			0
#define SHOWPEGFROMMARK			1
//左右标尺是否人为设置过(0或其他:未设置,1，2，3:人为设置过,1:时间,2:深度,3:采样)
#define VRULER_MANUAL_TIME		1
#define VRULER_MANUAL_DEPTH		2
#define VRULER_MANUAL_SAMPLE	3
//结构体或指针的偏移位置
#define OFFSET_TEXT				256			//解释文字的偏移位置
#define OFFSET_3DPARAM			948			//3d参数结构的偏移位置
#define OFFSET_PEGPARAM			980			//桩号参数结构的偏移位置
//定义各种设备的标志值(记录在文件的文件头中lh_device值)
#define LTD_DEVICE_2100			0x0001		//LTD-2100
#define LTD_DEVICE_2200			0x0002		//LTD-2200
#define LTD_DEVICE_2600			0x0003		//LTD-2600
#define LTD_DEVICE_80			0x0004		//LTD-80
#define LTD_DEVICE_S1			0x0005		//LTD-S1
#define LTD_DEVICE_R1			0x0006		//LTD-R1
#define LTD_DEVICE_X1			0x0007		//LTD-X1
#define LTD_DEVICE_X2			0x0008		//LTD-X2
#define LTD_DEVICE_ICE			0x0009		//LTD-探冰
#define LTD_DEVICE_BRIDGE		0x000A		//LTD-桥梁
#define LTD_DEVICE_REBAR		0x000B		//LTD-钢筋扫描仪(阵列)
#define LTD_DEVICE_ROADCAR		0x000C		//LTD-检测车
//定义各种文件的标志值(记录在文件的文件头中lh_file值)
#define ORIGINLTE_DATAFILE		0x0001		//原始数据文件类型
#define MANAGELTE_DATAFILE		0x0002		//处理数据文件类型
#define D3_DATAFILE				0x0003		//3d数据文件类型
#define PROJECT_DATAFILE		0x0004		//工程文件类型
#define LAYERREPORT_DATAFILE	0x0005		//层厚报表文件类型
#define TARGETREPORT_DATAFILE   0x0006		//目标报表文件类型
//日期参数结构
struct HEAD_DATE{
	unsigned sec2  :5;				//秒/2	(0-29)
	unsigned min   :6;				//分		(0-59)
	unsigned hour  :5;				//时		(0-23)
	unsigned day   :5;				//日		(0-31)
	unsigned month :4;				//月		(1-12)
	unsigned year  :7;				//年		(0-127=1980-2107)
};
//3d数据参数结构
struct HEAD_3DPAR{
	short directionType;			//用来判定是哪个方向的数据(01:x,10:y,11:xy)
	short reverse;					//保留字节，显式的使结构体满足自然对齐要求
	float xDistance;				//x轴方向上的距离(单位m)
	float yDistance;				//y轴方向上的距离(单位m)
	float zDistance;				//z轴方向上的距离(单位m)
	short xLines;					//平行于x轴方向上的测线数
	short yLines;					//平行于y轴方向上的测线数
	float xDistanceTwoLine;			//x方向上的相邻测线的间隔距离(单位m)
	float yDistanceTwoLine;			//y方向上的相邻测线的间隔距离(单位m)
	short xScansPerLine;			//x方向上每条测线包含的数据道数
	short yScansPerLine;			//y方向上每条测线包含的数据道数
};
//桩号参数结构
struct HEAD_PEGPAR{
	int begPeg;						//开始桩号(千米)
	int begPegAdd;					//开始桩号附加部分(米)
	int endPeg;						//结束桩号(千米)
	int endPegAdd;					//结束桩号附加部分(米)
	int pegInterval;				//桩号间隔(米)
};
//文件头结构
struct HEAD_FILEHEADER{
	//共 10 字节(1-10);
	short		lh_tag;				//文件标志(标记文件类型)
	short		lh_data;			//数据偏移位置	由硬件提供
	short		lh_nsamp;			//采样点数		由硬件提供
	short		lh_bits;			//数据位数		由硬件提供
	short		lh_zero;			//数据零偏		由硬件提供
	//共 20 字节(11-30);
	float		lh_sps;				//每秒扫描道数	由硬件提供
	float		lh_spm;				//每米扫描道数	由硬件提供
	float		lh_mpm;				//脉冲间隔		由硬件提供
	float		lh_pos;				//信号位置		由硬件提供
	float		lh_range;			//时窗(ns)	由硬件提供
	//共 24 字节(31-54);
	short		lh_spp;				//天线频率
	HEAD_DATE	lh_create;			//数据生成日期
	HEAD_DATE	lh_modif;			//数据修改日期
	short		lh_rgain;			//增益曲线位置
	short		lh_nrgain;			//增益曲线长度
	short		lh_text;			//解释说明位置
	short		lh_ntext;			//解释说明长度
	short		lh_proc;			//处理数据位置
	short		lh_nproc;			//处理数据长度
	short		lh_nchan;			//单双通道标志(1:单通道,2:双通道)
	//共 12 字节(55-66);
	float		lh_epsr;			//介电常数		由硬件提供
	float		lh_top;				//初始深度		由硬件提供
	float		lh_depth;			//深度范围		由软件提供
	//共 62 字节(67-128);
	short		lh_npass;			//任意
	short		lh_device;			//设备类型
	short		lh_file;			//文件类型
	short		lh_gps;				//GPS标志
	char		lh_gpsform[4];		//GPS格式
	char		lh_anten[4];		//天线名称
	char		lh_reserv1[9];		//保留字节
	BYTE		lh_lrmanual;		//是否人为设置左标尺的刻度间隔(1,2,3:人为设置过,1:时间,2:深度,3:采样)
	BYTE		lh_rrmanual;		//是否人为设置右标尺的刻度间隔(1,2,3:人为设置过,1:时间,2:深度,3:采样)
	float		lh_lrlong;			//左标尺长刻度间隔
	short		lh_lrshort;			//左标尺长刻度间的短刻度数
	float		lh_rrlong;			//右标尺长刻度间隔
	short		lh_rrshort;			//右标尺长刻度间的短刻度数
	short       lh_peg;				//桩号显示方式(0或其它:道号,1:标记)
	short       lh_horuler;			//上方标尺显示方式(1:道号,2:距离,3:桩号,其它:什么也不显示)
	short		lh_extent;			//标记扩展
	char		lh_work;			//工作模式(连续|点测|轮测)(0,1,2)
	short		lh_chanmask;		//任意
	char		lh_fname[12];		//文件名 软件给
	short		lh_chksum;			//任意
	//共 88 字节(129-216);
	float		lh_rgainf[22];		//增益曲线数组
	//共 732 字节(217-948);
	char		lh_reserv2[732];	//保留字节
	//共 52 字节(949-1000);
	HEAD_3DPAR	lh_3dParam;			//3D参数
	HEAD_PEGPAR	lh_pegParam;		//桩号参数
	//共 24 字节(1000-1024);
	char		lh_reserv3[24];		//保留字节
};
*/

public class FileHeader {
    //日期参数结构
    class HEAD_DATE {
        byte[] sec2 = new byte[5];                //5秒/2	(0-29)
        byte[] min = new byte[6];                //6分		(0-59)
        byte[] hour = new byte[5];                //5时		(0-23)
        byte[] day = new byte[5];                //5日		(0-31)
        byte[] month = new byte[4];                //4月		(1-12)
        byte[] year = new byte[7];                //7年		(0-127=1980-2107)
    }

    ;

    //3d数据参数结构
    class HEAD_3DPAR {
        short directionType;            //用来判定是哪个方向的数据(01:x,10:y,11:xy)
        short reverse;                    //保留字节，显式的使结构体满足自然对齐要求
        float xDistance;                //x轴方向上的距离(单位m)
        float yDistance;                //y轴方向上的距离(单位m)
        float zDistance;                //z轴方向上的距离(单位m)
        short xLines;                    //平行于x轴方向上的测线数
        short yLines;                    //平行于y轴方向上的测线数
        float xDistanceTwoLine;            //x方向上的相邻测线的间隔距离(单位m)
        float yDistanceTwoLine;            //y方向上的相邻测线的间隔距离(单位m)
        short xScansPerLine;            //x方向上每条测线包含的数据道数
        short yScansPerLine;            //y方向上每条测线包含的数据道数
    }

    ;

    //桩号参数结构
    class HEAD_PEGPAR {
        int begPeg;                        //开始桩号(千米)
        int begPegAdd;                    //开始桩号附加部分(米)
        int endPeg;                        //结束桩号(千米)
        int endPegAdd;                    //结束桩号附加部分(米)
        int pegInterval;                //桩号间隔(米)
    }

    ;

    private String TAG = "FileHeader";
    private short X2Flag = 256;
    private byte WHELL_MODE = 2;      //轮测模式
    private byte DIANCE_MODE = 1;     //点测模式
    private byte TIME_MODE = 0;       //时间模式(连续模式)

    short rh_tag = X2Flag;     //偏移:0 tag
    short rh_data;        //偏移:2 数据位置偏移 由硬件提供         
    short rh_nsamp = 512;       //偏移:4 每道采样点数 由硬件提供 128,256,512,1024,2048    
    short rh_bits = 16;   //偏移:6 数据位数 由硬件提供          512
    short rh_zero;        //偏移:8 数据零偏 由硬件提供         
    //total 10 bytes;
    float rh_sps;         //偏移:10         //每秒扫描道数(扫速) 由硬件提供 8,16,32,64,128
    float rh_spm;         //偏移:14         //每米扫描道数 由硬件提供
    float rh_mpm;         //偏移:18         //标记间距 由硬件提供
    float rh_position;    //偏移:22         //位置 由硬件提供
    float rh_range = 20;       //偏移:26         //时窗(ns) 由硬件提供     
    //total 20 bytes;
    short rh_spp;         //偏移:30  任意　　　　　　WINDOWS采集软件版本中存储天线频率；
    HEAD_DATE rh_creat = new HEAD_DATE();       //偏移:32 数据生成日期
    HEAD_DATE rh_modif = new HEAD_DATE();       //偏移:36 数据修改日期
    short rh_rgain;       //偏移:40 增益曲线位置 由硬件提供
    short rh_nrgain;      //偏移:42 增益曲线长度 由硬件提供
    short rh_text;        //偏移:44 说明位置
    short rh_ntext;       //偏移:46 说明长度
    short rh_proc;        //偏移:48 处理数据位置
    short rh_nproc;       //偏移:50 处理数据长度
    short rh_nchan;       //偏移:52 单双通道标志
    //total 24 bytes;
    float rh_epsr;        //偏移:54  平均介电常数   由硬件提供
    float rh_top;         //偏移:58  信号头对应深度 由硬件提供
    float rh_depth;       //偏移:62  深度范围       软件给
    //total 12 bytes;
    short rh_npass;       //偏移:66 任意
    short rh_device;      //设备类型
    short rh_file;          //文件类型
    short rh_gps;         //GPS标志
    char[] rh_gpsform = new char[4];      //GPS格式
    char[] rh_anten = new char[4];     //偏移：78,天线名称            
    char[] reserved = new char[9];   //偏移:82   保留字节

    byte rh_lrmanual;    //是否人为设置左标尺的刻度间隔(1,2,3:人为设置过,1:时间,2:深度,3:采样)
    byte rh_rrmanual;   //是否人为设置右标尺的刻度间隔(1,2,3:人为设置过,1:时间,2:深度,3:采样)
    float rh_lrlong;   //左标尺长刻度间隔
    short rh_lrshort;  //左标尺长刻度间的短刻度数
    float rh_rrlong;   //右标尺长刻度间隔
    short rh_rrshort;  //右标尺长刻度间的短刻度数
    short rh_peg;      //桩号显示方式(0或其它:道号,1:标记)
    short rh_horuler;  //上方标尺显示方式(1:道号,2:距离,3:桩号,其它:什么也不显示)
    short rh_flagExt;     //偏移:109    标记扩展
    byte rh_workType = TIME_MODE;    //偏移:111    工作模式(连续|点测|轮测)
    short rh_chanmask;    //偏移:112 任意
    char[] rh_fname = new char[12];   //偏移:114 文件名 软件给
    short rh_chksum;      //偏移:126  任意---文件头效验和     
    //total 62 bytes;
    float[] rh_rgainf = new float[22];  //偏移:128 增益曲线数组

    //共732字节(217-948)
    char[] rh_reserv2 = new char[732];    //保留字节

    //共52字节
    HEAD_3DPAR rh_3dParam = new HEAD_3DPAR();                //偏移：948,3D参数,32字节
    HEAD_PEGPAR rh_pegParam = new HEAD_PEGPAR();            //偏移：979,桩号参数，20字节
    //total 88 bytes;
    int m_left = 768;      //剩余字节数

    //共24字节(1000-1024)
    char[] rh_reserv3 = new char[24];    //保留字节

    ////
    public double getDeep() {
        double speed = 0.3 * 100 / Math.sqrt(rh_epsr);     //cm/s;
        double deep = (rh_range) * speed / 200;

        //
        return deep;
    }

    /**
     * 得到增益值
     *
     * @return
     */
    public float[] getHardplus() {
        return rh_rgainf;
    }

    /**
     * 得到时窗值
     *
     * @return
     */
    public int getTimeWindow() {
        return (int) rh_range;
    }

    /**
     * 得到相邻两道数据的触发距离
     *
     * @return
     */
    public double getDistancePerScans() {
        if (rh_workType != WHELL_MODE)
            return 0;
        //
        return rh_mpm * rh_flagExt;
    }

    /**
     * 设置时间模式
     */
    public void setTimeMode() {
        rh_workType = TIME_MODE;
    }

    /**
     * 设置距离模式
     */
    public void setDianceMode() {
        rh_workType = DIANCE_MODE;
    }

    /**
     * 设置轮测模式
     */
    public void setWhellMode() {
        rh_workType = WHELL_MODE;
    }

    public boolean isWhellMode() {
        return rh_workType == WHELL_MODE;
    }

    public void setWhellMode(int num) {
        rh_flagExt = (short) num;
    }

    public void setDianceDistance(int distance) {
        rh_mpm = distance;
    }

    //设置触发距离
    public void setTouchDistance(double touchDistance) {
        rh_mpm = (float) touchDistance;
    }

    public void setHardPlus(float[] vals) {
        for (int i = 0; i < 9; i++)
            rh_rgainf[i] = vals[i];
    }

    //
    public void setJiedianConst(double jdconst) {
        rh_epsr = (float) jdconst;
    }

    //
    public void setScanspeed(short speed) {
        rh_sps = speed;
    }

    //
    public void setFlagExtent(int flagNum) {
        rh_flagExt = (short) flagNum;
    }

    public final void write(byte[] buf) {
        buf[0] = (byte) rh_tag;
        buf[1] = (byte) (rh_tag >> 8);
        //取样点数
        buf[4] = (byte) rh_nsamp;
        buf[5] = (byte) (rh_nsamp >> 8);
        //数据位数
        buf[6] = (byte) rh_bits;
        buf[7] = (byte) (rh_bits >> 8);
        //扫速
        buf[10] = (byte) ((int) rh_sps);
        buf[11] = (byte) ((int) rh_sps >> 8);
        buf[12] = (byte) ((int) rh_sps >> 16);
        buf[13] = (byte) ((int) rh_sps >> 24);
        //标记间隔
        buf[18] = (byte) ((int) (rh_mpm * 100));
        buf[19] = (byte) ((int) (rh_mpm * 100) >> 8);
        buf[20] = (byte) ((int) (rh_mpm * 100) >> 16);
        buf[21] = (byte) ((int) (rh_mpm * 100) >> 24);
        //时窗
        buf[26] = (byte) ((int) rh_range);
        buf[27] = (byte) ((int) rh_range >> 8);
        buf[28] = (byte) ((int) rh_range >> 16);
        buf[29] = (byte) ((int) rh_range >> 24);
        //主频
        buf[30] = (byte) (rh_spp);
        buf[31] = (byte) (rh_spp >> 8);

        //数据生成时间
        buf[32] = (byte) ((rh_creat.sec2[0] & 1) | ((rh_creat.sec2[1] & 1) << 1) |
                          ((rh_creat.sec2[2] & 1) << 2) | ((rh_creat.sec2[3] & 1) << 3) |
                          ((rh_creat.sec2[4] & 1) << 4) | ((rh_creat.min[0] & 1) << 5) |
                          ((rh_creat.min[1] & 1) << 6) | ((rh_creat.min[2] & 1) << 7));
        getBitArray(buf[32]);

        buf[33] = (byte) ((rh_creat.min[3] & 1) | ((rh_creat.min[4] & 1) << 1) |
                          ((rh_creat.min[5] & 1) << 2) | ((rh_creat.hour[0] & 1) << 3) |
                          ((rh_creat.hour[1] & 1) << 4) | ((rh_creat.hour[2] & 1) << 5) |
                          ((rh_creat.hour[3] & 1) << 6) | ((rh_creat.hour[4] & 1) << 7));
        getBitArray(buf[33]);

        buf[34] = (byte) ((rh_creat.day[0] & 1) | ((rh_creat.day[1] & 1) << 1) |
                          ((rh_creat.day[2] & 1) << 2) | ((rh_creat.day[3] & 1) << 3) |
                          ((rh_creat.day[4] & 1) << 4) | ((rh_creat.month[0] & 1) << 5) |
                          ((rh_creat.month[1] & 1) << 6) | ((rh_creat.month[2] & 1) << 7));
        getBitArray(buf[34]);

        buf[35] = (byte) ((rh_creat.month[3] & 1) | ((rh_creat.year[0] & 1) << 1) |
                          ((rh_creat.year[1] & 1) << 2) | ((rh_creat.year[2] & 1) << 3) |
                          ((rh_creat.year[3] & 1) << 4) | ((rh_creat.year[4] & 1) << 5) |
                          ((rh_creat.year[5] & 1) << 6) | ((rh_creat.year[6] & 1) << 7));
        getBitArray(buf[35]);

        //数据修改时间
        buf[36] = (byte) (rh_modif.sec2[0] | (rh_modif.sec2[1] << 1) | (rh_modif.sec2[2] << 2) |
                          (rh_modif.sec2[3] << 3) | (rh_modif.sec2[4] << 4) |
                          (rh_modif.min[0] << 5) | (rh_modif.min[1] << 6) | (rh_modif.min[2] << 7));
        getBitArray(buf[36]);

        buf[37] = (byte) ((rh_modif.min[3] & 1) | ((rh_modif.min[4] & 1) << 1) |
                          ((rh_modif.min[5] & 1) << 2) | ((rh_modif.hour[0] & 1) << 3) |
                          ((rh_modif.hour[1] & 1) << 4) | ((rh_modif.hour[2] & 1) << 5) |
                          ((rh_modif.hour[3] & 1) << 6) | ((rh_modif.hour[4] & 1) << 7));
        getBitArray(buf[37]);

        buf[38] = (byte) ((rh_modif.day[0] & 1) | ((rh_modif.day[1] & 1) << 1) |
                          ((rh_modif.day[2] & 1) << 2) | ((rh_modif.day[3] & 1) << 3) |
                          ((rh_modif.day[4] & 1) << 4) | ((rh_modif.month[0] & 1) << 5) |
                          ((rh_modif.month[1] & 1) << 6) | ((rh_modif.month[2] & 1) << 7));
        getBitArray(buf[38]);

        buf[39] = (byte) ((rh_modif.month[3] & 1) | ((rh_modif.year[0] & 1) << 1) |
                          ((rh_modif.year[1] & 1) << 2) | ((rh_modif.year[2] & 1) << 3) |
                          ((rh_modif.year[3] & 1) << 4) | ((rh_modif.year[4] & 1) << 5) |
                          ((rh_modif.year[5] & 1) << 6) | ((rh_modif.year[6] & 1) << 7));
        getBitArray(buf[39]);

        //介电常数
        buf[54] = (byte) ((int) (rh_epsr * 10));
        buf[55] = (byte) ((int) (rh_epsr * 10) >> 8);
        buf[56] = (byte) ((int) (rh_epsr * 10) >> 16);
        buf[57] = (byte) ((int) (rh_epsr * 10) >> 24);
        //标记扩展
        buf[109] = (byte) (rh_flagExt);
        buf[110] = (byte) (rh_flagExt >> 8);
        //工作方式
        buf[111] = (rh_workType);
        //硬件增益
        int i;
        for (i = 0; i < 9; i++) {
            buf[128 + i * 4] = (byte) rh_rgainf[i];
            buf[128 + i * 4 + 1] = (byte) ((int) rh_rgainf[i] >> 8);
            buf[128 + i * 4 + 2] = (byte) ((int) rh_rgainf[i] >> 16);
            buf[128 + i * 4 + 3] = (byte) ((int) rh_rgainf[i] >> 24);
        }
    }

    /**
     * 保存头文件
     *
     * @param fileOS
     */
    public void save(FileOutputStream fileOS) {
        //标志
        byte[] buf = new byte[1024];
        write(buf);
//		buf[0] = (byte)rh_tag;
//		buf[1] = (byte)(rh_tag>>8);
//		//取样点数
//		buf[4] = (byte)rh_nsamp;
//		buf[5] = (byte)(rh_nsamp>>8);
//		//数据位数
//		buf[6] = (byte)rh_bits;
//		buf[7] = (byte)(rh_bits>>8);
//		//扫速
//		buf[10] = (byte)((int)rh_sps);
//		buf[11] = (byte)((int)rh_sps>>8);
//		buf[12] = (byte)((int)rh_sps>>16);
//		buf[13] = (byte)((int)rh_sps>>24);
//		//标记间隔
//		buf[18] = (byte)((int)(rh_mpm*100));
//		buf[19] = (byte)((int)(rh_mpm*100)>>8);
//		buf[20] = (byte)((int)(rh_mpm*100)>>16);
//		buf[21] = (byte)((int)(rh_mpm*100)>>24);
//		//时窗
//		buf[26] = (byte)((int)rh_range);
//		buf[27] = (byte)((int)rh_range>>8);
//		buf[28] = (byte)((int)rh_range>>16);
//		buf[29] = (byte)((int)rh_range>>24);
//		//主频
//		buf[30] = (byte)(rh_spp);
//		buf[31] = (byte)(rh_spp>>8);
//
//		//数据生成时间
//		buf[32] = (byte)((rh_creat.sec2[0]&1)|((rh_creat.sec2[1]&1)<<1)
//						 |((rh_creat.sec2[2]&1)<<2)|((rh_creat.sec2[3]&1)<<3)
//						 |((rh_creat.sec2[4]&1)<<4)|((rh_creat.min[0]&1)<<5)
//						 |((rh_creat.min[1]&1)<<6)|((rh_creat.min[2]&1)<<7));
//		getBitArray(buf[32]);
//
//		buf[33] = (byte)((rh_creat.min[3]&1)|((rh_creat.min[4]&1)<<1)
//						|((rh_creat.min[5]&1)<<2)|((rh_creat.hour[0]&1)<<3)
//						|((rh_creat.hour[1]&1)<<4)|((rh_creat.hour[2]&1)<<5)
//						|((rh_creat.hour[3]&1)<<6)|((rh_creat.hour[4]&1)<<7));
//		getBitArray(buf[33]);
//
//		buf[34] = (byte)((rh_creat.day[0]&1)|((rh_creat.day[1]&1)<<1)
//						|((rh_creat.day[2]&1)<<2)|((rh_creat.day[3]&1)<<3)
//						|((rh_creat.day[4]&1)<<4)|((rh_creat.month[0]&1)<<5)
//						|((rh_creat.month[1]&1)<<6)|((rh_creat.month[2]&1)<<7));
//		getBitArray(buf[34]);
//
//		buf[35] = (byte)((rh_creat.month[3]&1)|((rh_creat.year[0]&1)<<1)
//						|((rh_creat.year[1]&1)<<2)|((rh_creat.year[2]&1)<<3)
//						|((rh_creat.year[3]&1)<<4)|((rh_creat.year[4]&1)<<5)
//						|((rh_creat.year[5]&1)<<6)|((rh_creat.year[6]&1)<<7));
//		getBitArray(buf[35]);
//
//		//数据修改时间
//		buf[36] = (byte)(rh_modif.sec2[0]|(rh_modif.sec2[1]<<1)
//				 |(rh_modif.sec2[2]<<2)|(rh_modif.sec2[3]<<3)
//				 |(rh_modif.sec2[4]<<4)|(rh_modif.min[0]<<5)
//				 |(rh_modif.min[1]<<6)|(rh_modif.min[2]<<7));
//		getBitArray(buf[36]);
//
//		buf[37] = (byte)((rh_modif.min[3]&1)|((rh_modif.min[4]&1)<<1)
//				|((rh_modif.min[5]&1)<<2)|((rh_modif.hour[0]&1)<<3)
//				|((rh_modif.hour[1]&1)<<4)|((rh_modif.hour[2]&1)<<5)
//				|((rh_modif.hour[3]&1)<<6)|((rh_modif.hour[4]&1)<<7));
//		getBitArray(buf[37]);
//
//		buf[38] = (byte)((rh_modif.day[0]&1)|((rh_modif.day[1]&1)<<1)
//				|((rh_modif.day[2]&1)<<2)|((rh_modif.day[3]&1)<<3)
//				|((rh_modif.day[4]&1)<<4)|((rh_modif.month[0]&1)<<5)
//				|((rh_modif.month[1]&1)<<6)|((rh_modif.month[2]&1)<<7));
//		getBitArray(buf[38]);
//
//		buf[39] = (byte)((rh_modif.month[3]&1)|((rh_modif.year[0]&1)<<1)
//				|((rh_modif.year[1]&1)<<2)|((rh_modif.year[2]&1)<<3)
//				|((rh_modif.year[3]&1)<<4)|((rh_modif.year[4]&1)<<5)
//				|((rh_modif.year[5]&1)<<6)|((rh_modif.year[6]&1)<<7));
//		getBitArray(buf[39]);
//
//		//介电常数
//		buf[54] = (byte)((int)(rh_epsr*10));
//		buf[55] = (byte)((int)(rh_epsr*10)>>8);
//		buf[56] = (byte)((int)(rh_epsr*10)>>16);
//		buf[57] = (byte)((int)(rh_epsr*10)>>24);
//		//标记扩展
//		buf[109] = (byte)(rh_flagExt);
//		buf[110] = (byte)(rh_flagExt>>8);
//		//工作方式
//		buf[111] = (rh_workType);
//		//硬件增益
//		int i;
//		for(i=0;i<9;i++)
//		{
//			buf[128+i*4]   = (byte) rh_rgainf[i];
//			buf[128+i*4+1] = (byte)((int)rh_rgainf[i]>>8);
//			buf[128+i*4+2] = (byte)((int)rh_rgainf[i]>>16);
//			buf[128+i*4+3] = (byte)((int)rh_rgainf[i]>>24);
//		}

        try {
            fileOS.write(buf, 0, 1024);
        } catch (Exception e) {
            DebugUtil.i(TAG, "save fileheader fail!");
        }
    }

    //将byte转化为有8个数据的byte数组
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

    public final void read(byte[] buf) {
        float temVal;

        //取样点数
        rh_nsamp = buf[4];
        rh_nsamp |= (buf[5] << 8);
        if (rh_nsamp == -128)
            rh_nsamp = 128;

        //数据位数
        rh_bits = buf[6];
        rh_bits |= buf[7] << 8;

        //扫速
        temVal = 0;
        rh_sps = buf[10];
        temVal = buf[11] << 8;
        rh_sps += temVal;
        temVal = buf[12] << 16;
        rh_sps += temVal;
        temVal = buf[13] << 24;
        rh_sps += temVal;

        //标记间隔
        rh_mpm = buf[18];
        temVal = buf[19] << 8;
        rh_mpm += temVal;
        temVal = buf[20] << 16;
        rh_mpm += temVal;
        temVal = buf[21] << 24;
        rh_mpm += temVal;
        rh_mpm = rh_mpm / 100;

        //时窗
        rh_range = buf[26];
        temVal = 0;
        temVal = buf[27] << 8;
        rh_range += temVal;
        temVal = 0;
        temVal = buf[28] << 16;
        rh_range += temVal;
        temVal = 0;
        temVal = buf[29] << 24;
        rh_range += temVal;

        //主频
        this.rh_spp = buf[30];
        this.rh_spp += buf[31] << 8;

        //介电常数
        rh_epsr = buf[54];
        temVal = 0;
        temVal = buf[55] << 8;
        rh_epsr += temVal;
        temVal = 0;
        temVal = buf[56] << 16;
        rh_epsr += temVal;
        temVal = 0;
        temVal = buf[57] << 24;
        rh_epsr += temVal;
        rh_epsr = (float) (rh_epsr / 10.);

        //标记间隔
        rh_flagExt = buf[109];
        rh_flagExt += buf[110] << 8;

        //工作方式
        rh_workType = buf[111];

        //硬件增益
        int i;
        for (i = 0; i < 9; i++) {
            rh_rgainf[i] = buf[128 + i * 4];
            //
            temVal = buf[128 + i * 4 + 1] << 8;
            rh_rgainf[i] += temVal;
            //
            temVal = buf[128 + i * 4 + 2] << 16;
            rh_rgainf[i] += temVal;
            //
            temVal = buf[128 + i * 4 + 3] << 24;
            rh_rgainf[i] += temVal;
        }

        //对参数进行检查
        if (rh_epsr < 0 || rh_epsr > 200)
            rh_epsr = 1;
        if (rh_nsamp <= 0)
            rh_nsamp = 512;
        DebugUtil.i(TAG, "rh_nsamp:=" + rh_nsamp + "rh_range:=" + rh_range);

    }

    ////
    public void load(FileInputStream fileOS) {
        byte[] buf = new byte[1024];
        try {
            fileOS.read(buf, 0, 1024);
			read(buf);
        } catch (Exception e) {
            DebugUtil.i(TAG, "load Fileheader file fail!");
        }
    }

    public boolean isDianCeMode() {
        // TODO Auto-generated method stub
        return rh_workType == DIANCE_MODE;
    }

    //
    public double getDianCeDistancePerScans() {
        return rh_mpm;
    }

    ///2016.6.10
    public void copyFrome(FileHeader fileHeader) {
        rh_tag = fileHeader.rh_tag;
        rh_data = fileHeader.rh_data;
        rh_nsamp = fileHeader.rh_nsamp;
        rh_bits = fileHeader.rh_bits;
        rh_zero = fileHeader.rh_zero;

        rh_sps = fileHeader.rh_sps;
        rh_spm = fileHeader.rh_spm;
        rh_mpm = fileHeader.rh_mpm;
        rh_position = fileHeader.rh_position;
        rh_range = fileHeader.rh_range;

        rh_spp = fileHeader.rh_spp;
        rh_creat = fileHeader.rh_creat;
        rh_modif = fileHeader.rh_modif;
        rh_rgain = fileHeader.rh_rgain;
        rh_nrgain = fileHeader.rh_nrgain;
        rh_text = fileHeader.rh_text;
        rh_ntext = fileHeader.rh_ntext;
        rh_proc = fileHeader.rh_proc;
        rh_nproc = fileHeader.rh_nproc;
        rh_nchan = fileHeader.rh_nchan;
        rh_epsr = fileHeader.rh_epsr;
        rh_top = fileHeader.rh_top;
        rh_depth = fileHeader.rh_depth;

        rh_npass = fileHeader.rh_npass;
        for (int i = 0; i < 9; i++)
            reserved[i] = fileHeader.reserved[i];
        rh_flagExt = fileHeader.rh_flagExt;
        rh_workType = fileHeader.rh_workType;
        rh_chanmask = fileHeader.rh_chanmask;
//	    char[]  rh_fname = new char[12];   
        rh_chksum = fileHeader.rh_chksum;

        for (int i = 0; i < 22; i++)
            rh_rgainf[i] = fileHeader.rh_rgainf[i];
		/*                         
	    rh_image.apr_x = fileHeader.rh_image.apr_x;   
	    rh_image.apr_y = fileHeader.rh_image.apr_y;
	    rh_image.bkr_off = fileHeader.rh_image.bkr_off;
	    rh_image.ground_pos = fileHeader.rh_image.ground_pos;
	    rh_image.image_h = fileHeader.rh_image.image_h;
	    rh_image.image_l = fileHeader.rh_image.image_l;
	    rh_image.image_w = fileHeader.rh_image.image_w;
	    rh_image.pcr_endB = fileHeader.rh_image.pcr_endB;
	    rh_image.pcr_endP = fileHeader.rh_image.pcr_endP;
	    rh_image.pcr_startB = fileHeader.rh_image.pcr_startB;
	    rh_image.pcr_startP = fileHeader.rh_image.pcr_startP;
	    rh_image.rowtracks = fileHeader.rh_image.rowtracks;
	    rh_image.xgrid = fileHeader.rh_image.xgrid;
	    rh_image.xinter = fileHeader.rh_image.xinter;
	    rh_image.xPoints = fileHeader.rh_image.xPoints;
	    rh_image.ygrid = fileHeader.rh_image.ygrid;
	    rh_image.yinter = fileHeader.rh_image.yinter;
	    rh_image.yPoints = fileHeader.rh_image.yPoints;
	    */
    }
}
