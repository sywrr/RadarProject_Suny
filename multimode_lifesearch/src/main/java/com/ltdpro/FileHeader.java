package com.ltdpro;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/*
 * 
//�ļ�ͷ�ĳ���
#define HEADER_LENGTH			1024		//�ļ�ͷ����
//��������ֵ��Сֵ(cm)
#define MAX_INTERVAL_PER_SCAN	10000		//��������ֵ(cm)
#define MIN_INTERVAL_PER_SCAN	0.01		//�������Сֵ(cm)
//ʱ�����ֵ(ns)
#define MAX_TIME_WINDOW			10000		//ʱ�����ֵ(ns)
//����Ƶ�ʵ����ֵ��Сֵ(MHz)
#define MAX_ANTENNA_FREQ		10000		//����Ƶ�ʵ����ֵ(MHz)
#define MIN_ANTENNA_FREQ		1			//����Ƶ�ʵ���Сֵ(MHz)
//׮�����ǧ����
#define MAX_KILO_PEG			10000		//׮�����ǧ����
//LTE�ļ��ı��ֵ
#define BIG_MARK_VALUE			0x4000		//���ֵ
#define SMALL_MARK_VALUE		0x8000		//С��ֵ
#define MARK_INDEX				1			//�����������
//ˮƽ��ߵ���ʾ��ʽ(0������:ʲôҲ����ʾ,1:����,2:����,3:׮��)
#define HRULER_SHOWNONE			0
#define HRULER_SHOWSCAN			1
#define HRULER_SHOWDISTANCE		2
#define HRULER_SHOWPEG			3
//��ʾ׮�ŵķ�ʽ(0������:����,1:���)
#define SHOWPEGFROMSCAN			0
#define SHOWPEGFROMMARK			1
//���ұ���Ƿ���Ϊ���ù�(0������:δ����,1��2��3:��Ϊ���ù�,1:ʱ��,2:���,3:����)
#define VRULER_MANUAL_TIME		1
#define VRULER_MANUAL_DEPTH		2
#define VRULER_MANUAL_SAMPLE	3
//�ṹ���ָ���ƫ��λ��
#define OFFSET_TEXT				256			//�������ֵ�ƫ��λ��
#define OFFSET_3DPARAM			948			//3d�����ṹ��ƫ��λ��
#define OFFSET_PEGPARAM			980			//׮�Ų����ṹ��ƫ��λ��
//��������豸�ı�־ֵ(��¼���ļ����ļ�ͷ��lh_deviceֵ)
#define LTD_DEVICE_2100			0x0001		//LTD-2100
#define LTD_DEVICE_2200			0x0002		//LTD-2200
#define LTD_DEVICE_2600			0x0003		//LTD-2600
#define LTD_DEVICE_80			0x0004		//LTD-80
#define LTD_DEVICE_S1			0x0005		//LTD-S1
#define LTD_DEVICE_R1			0x0006		//LTD-R1
#define LTD_DEVICE_X1			0x0007		//LTD-X1
#define LTD_DEVICE_X2			0x0008		//LTD-X2
#define LTD_DEVICE_ICE			0x0009		//LTD-̽��
#define LTD_DEVICE_BRIDGE		0x000A		//LTD-����
#define LTD_DEVICE_REBAR		0x000B		//LTD-�ֽ�ɨ����(����)
#define LTD_DEVICE_ROADCAR		0x000C		//LTD-��⳵
//��������ļ��ı�־ֵ(��¼���ļ����ļ�ͷ��lh_fileֵ)
#define ORIGINLTE_DATAFILE		0x0001		//ԭʼ�����ļ�����
#define MANAGELTE_DATAFILE		0x0002		//���������ļ�����
#define D3_DATAFILE				0x0003		//3d�����ļ�����
#define PROJECT_DATAFILE		0x0004		//�����ļ�����
#define LAYERREPORT_DATAFILE	0x0005		//��񱨱��ļ�����
#define TARGETREPORT_DATAFILE   0x0006		//Ŀ�걨���ļ�����
//���ڲ����ṹ
struct HEAD_DATE{
	unsigned sec2  :5;				//��/2	(0-29)
	unsigned min   :6;				//��		(0-59)
	unsigned hour  :5;				//ʱ		(0-23)
	unsigned day   :5;				//��		(0-31)
	unsigned month :4;				//��		(1-12)
	unsigned year  :7;				//��		(0-127=1980-2107)
};
//3d���ݲ����ṹ
struct HEAD_3DPAR{
	short directionType;			//�����ж����ĸ����������(01:x,10:y,11:xy)
	short reverse;					//�����ֽڣ���ʽ��ʹ�ṹ��������Ȼ����Ҫ��
	float xDistance;				//x�᷽���ϵľ���(��λm)
	float yDistance;				//y�᷽���ϵľ���(��λm)
	float zDistance;				//z�᷽���ϵľ���(��λm)
	short xLines;					//ƽ����x�᷽���ϵĲ�����
	short yLines;					//ƽ����y�᷽���ϵĲ�����
	float xDistanceTwoLine;			//x�����ϵ����ڲ��ߵļ������(��λm)
	float yDistanceTwoLine;			//y�����ϵ����ڲ��ߵļ������(��λm)
	short xScansPerLine;			//x������ÿ�����߰��������ݵ���
	short yScansPerLine;			//y������ÿ�����߰��������ݵ���
};
//׮�Ų����ṹ
struct HEAD_PEGPAR{
	int begPeg;						//��ʼ׮��(ǧ��)
	int begPegAdd;					//��ʼ׮�Ÿ��Ӳ���(��)
	int endPeg;						//����׮��(ǧ��)
	int endPegAdd;					//����׮�Ÿ��Ӳ���(��)
	int pegInterval;				//׮�ż��(��)
};
//�ļ�ͷ�ṹ
struct HEAD_FILEHEADER{
	//�� 10 �ֽ�(1-10);
	short		lh_tag;				//�ļ���־(����ļ�����)
	short		lh_data;			//����ƫ��λ��	��Ӳ���ṩ
	short		lh_nsamp;			//��������		��Ӳ���ṩ
	short		lh_bits;			//����λ��		��Ӳ���ṩ
	short		lh_zero;			//������ƫ		��Ӳ���ṩ
	//�� 20 �ֽ�(11-30);
	float		lh_sps;				//ÿ��ɨ�����	��Ӳ���ṩ
	float		lh_spm;				//ÿ��ɨ�����	��Ӳ���ṩ
	float		lh_mpm;				//������		��Ӳ���ṩ
	float		lh_pos;				//�ź�λ��		��Ӳ���ṩ
	float		lh_range;			//ʱ��(ns)	��Ӳ���ṩ
	//�� 24 �ֽ�(31-54);
	short		lh_spp;				//����Ƶ��
	HEAD_DATE	lh_create;			//������������
	HEAD_DATE	lh_modif;			//�����޸�����
	short		lh_rgain;			//��������λ��
	short		lh_nrgain;			//�������߳���
	short		lh_text;			//����˵��λ��
	short		lh_ntext;			//����˵������
	short		lh_proc;			//��������λ��
	short		lh_nproc;			//�������ݳ���
	short		lh_nchan;			//��˫ͨ����־(1:��ͨ��,2:˫ͨ��)
	//�� 12 �ֽ�(55-66);
	float		lh_epsr;			//��糣��		��Ӳ���ṩ
	float		lh_top;				//��ʼ���		��Ӳ���ṩ
	float		lh_depth;			//��ȷ�Χ		������ṩ
	//�� 62 �ֽ�(67-128);
	short		lh_npass;			//����
	short		lh_device;			//�豸����
	short		lh_file;			//�ļ�����
	short		lh_gps;				//GPS��־
	char		lh_gpsform[4];		//GPS��ʽ
	char		lh_anten[4];		//��������
	char		lh_reserv1[9];		//�����ֽ�
	BYTE		lh_lrmanual;		//�Ƿ���Ϊ�������ߵĿ̶ȼ��(1,2,3:��Ϊ���ù�,1:ʱ��,2:���,3:����)
	BYTE		lh_rrmanual;		//�Ƿ���Ϊ�����ұ�ߵĿ̶ȼ��(1,2,3:��Ϊ���ù�,1:ʱ��,2:���,3:����)
	float		lh_lrlong;			//���߳��̶ȼ��
	short		lh_lrshort;			//���߳��̶ȼ�Ķ̶̿���
	float		lh_rrlong;			//�ұ�߳��̶ȼ��
	short		lh_rrshort;			//�ұ�߳��̶ȼ�Ķ̶̿���
	short       lh_peg;				//׮����ʾ��ʽ(0������:����,1:���)
	short       lh_horuler;			//�Ϸ������ʾ��ʽ(1:����,2:����,3:׮��,����:ʲôҲ����ʾ)
	short		lh_extent;			//�����չ
	char		lh_work;			//����ģʽ(����|���|�ֲ�)(0,1,2)
	short		lh_chanmask;		//����
	char		lh_fname[12];		//�ļ��� �����
	short		lh_chksum;			//����
	//�� 88 �ֽ�(129-216);
	float		lh_rgainf[22];		//������������
	//�� 732 �ֽ�(217-948);
	char		lh_reserv2[732];	//�����ֽ�
	//�� 52 �ֽ�(949-1000);
	HEAD_3DPAR	lh_3dParam;			//3D����
	HEAD_PEGPAR	lh_pegParam;		//׮�Ų���
	//�� 24 �ֽ�(1000-1024);
	char		lh_reserv3[24];		//�����ֽ�
};
*/

public class FileHeader {
    //���ڲ����ṹ
    class HEAD_DATE {
        byte[] sec2 = new byte[5];                //5��/2	(0-29)
        byte[] min = new byte[6];                //6��		(0-59)
        byte[] hour = new byte[5];                //5ʱ		(0-23)
        byte[] day = new byte[5];                //5��		(0-31)
        byte[] month = new byte[4];                //4��		(1-12)
        byte[] year = new byte[7];                //7��		(0-127=1980-2107)
    }

    ;

    //3d���ݲ����ṹ
    class HEAD_3DPAR {
        short directionType;            //�����ж����ĸ����������(01:x,10:y,11:xy)
        short reverse;                    //�����ֽڣ���ʽ��ʹ�ṹ��������Ȼ����Ҫ��
        float xDistance;                //x�᷽���ϵľ���(��λm)
        float yDistance;                //y�᷽���ϵľ���(��λm)
        float zDistance;                //z�᷽���ϵľ���(��λm)
        short xLines;                    //ƽ����x�᷽���ϵĲ�����
        short yLines;                    //ƽ����y�᷽���ϵĲ�����
        float xDistanceTwoLine;            //x�����ϵ����ڲ��ߵļ������(��λm)
        float yDistanceTwoLine;            //y�����ϵ����ڲ��ߵļ������(��λm)
        short xScansPerLine;            //x������ÿ�����߰��������ݵ���
        short yScansPerLine;            //y������ÿ�����߰��������ݵ���
    }

    ;

    //׮�Ų����ṹ
    class HEAD_PEGPAR {
        int begPeg;                        //��ʼ׮��(ǧ��)
        int begPegAdd;                    //��ʼ׮�Ÿ��Ӳ���(��)
        int endPeg;                        //����׮��(ǧ��)
        int endPegAdd;                    //����׮�Ÿ��Ӳ���(��)
        int pegInterval;                //׮�ż��(��)
    }

    ;

    private String TAG = "FileHeader";
    private short X2Flag = 256;
    private byte WHELL_MODE = 2;      //�ֲ�ģʽ
    private byte DIANCE_MODE = 1;     //���ģʽ
    private byte TIME_MODE = 0;       //ʱ��ģʽ(����ģʽ)

    short rh_tag = X2Flag;     //ƫ��:0 tag
    short rh_data;        //ƫ��:2 ����λ��ƫ�� ��Ӳ���ṩ         
    short rh_nsamp = 512;       //ƫ��:4 ÿ���������� ��Ӳ���ṩ 128,256,512,1024,2048    
    short rh_bits = 16;   //ƫ��:6 ����λ�� ��Ӳ���ṩ          512
    short rh_zero;        //ƫ��:8 ������ƫ ��Ӳ���ṩ         
    //total 10 bytes;
    float rh_sps;         //ƫ��:10         //ÿ��ɨ�����(ɨ��) ��Ӳ���ṩ 8,16,32,64,128
    float rh_spm;         //ƫ��:14         //ÿ��ɨ����� ��Ӳ���ṩ
    float rh_mpm;         //ƫ��:18         //��Ǽ�� ��Ӳ���ṩ
    float rh_position;    //ƫ��:22         //λ�� ��Ӳ���ṩ
    float rh_range = 20;       //ƫ��:26         //ʱ��(ns) ��Ӳ���ṩ     
    //total 20 bytes;
    short rh_spp;         //ƫ��:30  ���⡡����������WINDOWS�ɼ�����汾�д洢����Ƶ�ʣ�
    HEAD_DATE rh_creat = new HEAD_DATE();       //ƫ��:32 ������������
    HEAD_DATE rh_modif = new HEAD_DATE();       //ƫ��:36 �����޸�����
    short rh_rgain;       //ƫ��:40 ��������λ�� ��Ӳ���ṩ
    short rh_nrgain;      //ƫ��:42 �������߳��� ��Ӳ���ṩ
    short rh_text;        //ƫ��:44 ˵��λ��
    short rh_ntext;       //ƫ��:46 ˵������
    short rh_proc;        //ƫ��:48 ��������λ��
    short rh_nproc;       //ƫ��:50 �������ݳ���
    short rh_nchan;       //ƫ��:52 ��˫ͨ����־
    //total 24 bytes;
    float rh_epsr;        //ƫ��:54  ƽ����糣��   ��Ӳ���ṩ
    float rh_top;         //ƫ��:58  �ź�ͷ��Ӧ��� ��Ӳ���ṩ
    float rh_depth;       //ƫ��:62  ��ȷ�Χ       �����
    //total 12 bytes;
    short rh_npass;       //ƫ��:66 ����
    short rh_device;      //�豸����
    short rh_file;          //�ļ�����
    short rh_gps;         //GPS��־
    char[] rh_gpsform = new char[4];      //GPS��ʽ
    char[] rh_anten = new char[4];     //ƫ�ƣ�78,��������            
    char[] reserved = new char[9];   //ƫ��:82   �����ֽ�

    byte rh_lrmanual;    //�Ƿ���Ϊ�������ߵĿ̶ȼ��(1,2,3:��Ϊ���ù�,1:ʱ��,2:���,3:����)
    byte rh_rrmanual;   //�Ƿ���Ϊ�����ұ�ߵĿ̶ȼ��(1,2,3:��Ϊ���ù�,1:ʱ��,2:���,3:����)
    float rh_lrlong;   //���߳��̶ȼ��
    short rh_lrshort;  //���߳��̶ȼ�Ķ̶̿���
    float rh_rrlong;   //�ұ�߳��̶ȼ��
    short rh_rrshort;  //�ұ�߳��̶ȼ�Ķ̶̿���
    short rh_peg;      //׮����ʾ��ʽ(0������:����,1:���)
    short rh_horuler;  //�Ϸ������ʾ��ʽ(1:����,2:����,3:׮��,����:ʲôҲ����ʾ)
    short rh_flagExt;     //ƫ��:109    �����չ
    byte rh_workType = TIME_MODE;    //ƫ��:111    ����ģʽ(����|���|�ֲ�)
    short rh_chanmask;    //ƫ��:112 ����
    char[] rh_fname = new char[12];   //ƫ��:114 �ļ��� �����
    short rh_chksum;      //ƫ��:126  ����---�ļ�ͷЧ���     
    //total 62 bytes;
    float[] rh_rgainf = new float[22];  //ƫ��:128 ������������

    //��732�ֽ�(217-948)
    char[] rh_reserv2 = new char[732];    //�����ֽ�

    //��52�ֽ�
    HEAD_3DPAR rh_3dParam = new HEAD_3DPAR();                //ƫ�ƣ�948,3D����,32�ֽ�
    HEAD_PEGPAR rh_pegParam = new HEAD_PEGPAR();            //ƫ�ƣ�979,׮�Ų�����20�ֽ�
    //total 88 bytes;
    int m_left = 768;      //ʣ���ֽ���

    //��24�ֽ�(1000-1024)
    char[] rh_reserv3 = new char[24];    //�����ֽ�

    ////
    public double getDeep() {
        double speed = 0.3 * 100 / Math.sqrt(rh_epsr);     //cm/s;
        double deep = (rh_range) * speed / 200;

        //
        return deep;
    }

    /**
     * �õ�����ֵ
     *
     * @return
     */
    public float[] getHardplus() {
        return rh_rgainf;
    }

    /**
     * �õ�ʱ��ֵ
     *
     * @return
     */
    public int getTimeWindow() {
        return (int) rh_range;
    }

    /**
     * �õ������������ݵĴ�������
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
     * ����ʱ��ģʽ
     */
    public void setTimeMode() {
        rh_workType = TIME_MODE;
    }

    /**
     * ���þ���ģʽ
     */
    public void setDianceMode() {
        rh_workType = DIANCE_MODE;
    }

    /**
     * �����ֲ�ģʽ
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

    //���ô�������
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
        //ȡ������
        buf[4] = (byte) rh_nsamp;
        buf[5] = (byte) (rh_nsamp >> 8);
        //����λ��
        buf[6] = (byte) rh_bits;
        buf[7] = (byte) (rh_bits >> 8);
        //ɨ��
        buf[10] = (byte) ((int) rh_sps);
        buf[11] = (byte) ((int) rh_sps >> 8);
        buf[12] = (byte) ((int) rh_sps >> 16);
        buf[13] = (byte) ((int) rh_sps >> 24);
        //��Ǽ��
        buf[18] = (byte) ((int) (rh_mpm * 100));
        buf[19] = (byte) ((int) (rh_mpm * 100) >> 8);
        buf[20] = (byte) ((int) (rh_mpm * 100) >> 16);
        buf[21] = (byte) ((int) (rh_mpm * 100) >> 24);
        //ʱ��
        buf[26] = (byte) ((int) rh_range);
        buf[27] = (byte) ((int) rh_range >> 8);
        buf[28] = (byte) ((int) rh_range >> 16);
        buf[29] = (byte) ((int) rh_range >> 24);
        //��Ƶ
        buf[30] = (byte) (rh_spp);
        buf[31] = (byte) (rh_spp >> 8);

        //��������ʱ��
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

        //�����޸�ʱ��
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

        //��糣��
        buf[54] = (byte) ((int) (rh_epsr * 10));
        buf[55] = (byte) ((int) (rh_epsr * 10) >> 8);
        buf[56] = (byte) ((int) (rh_epsr * 10) >> 16);
        buf[57] = (byte) ((int) (rh_epsr * 10) >> 24);
        //�����չ
        buf[109] = (byte) (rh_flagExt);
        buf[110] = (byte) (rh_flagExt >> 8);
        //������ʽ
        buf[111] = (rh_workType);
        //Ӳ������
        int i;
        for (i = 0; i < 9; i++) {
            buf[128 + i * 4] = (byte) rh_rgainf[i];
            buf[128 + i * 4 + 1] = (byte) ((int) rh_rgainf[i] >> 8);
            buf[128 + i * 4 + 2] = (byte) ((int) rh_rgainf[i] >> 16);
            buf[128 + i * 4 + 3] = (byte) ((int) rh_rgainf[i] >> 24);
        }
    }

    /**
     * ����ͷ�ļ�
     *
     * @param fileOS
     */
    public void save(FileOutputStream fileOS) {
        //��־
        byte[] buf = new byte[1024];
        write(buf);
//		buf[0] = (byte)rh_tag;
//		buf[1] = (byte)(rh_tag>>8);
//		//ȡ������
//		buf[4] = (byte)rh_nsamp;
//		buf[5] = (byte)(rh_nsamp>>8);
//		//����λ��
//		buf[6] = (byte)rh_bits;
//		buf[7] = (byte)(rh_bits>>8);
//		//ɨ��
//		buf[10] = (byte)((int)rh_sps);
//		buf[11] = (byte)((int)rh_sps>>8);
//		buf[12] = (byte)((int)rh_sps>>16);
//		buf[13] = (byte)((int)rh_sps>>24);
//		//��Ǽ��
//		buf[18] = (byte)((int)(rh_mpm*100));
//		buf[19] = (byte)((int)(rh_mpm*100)>>8);
//		buf[20] = (byte)((int)(rh_mpm*100)>>16);
//		buf[21] = (byte)((int)(rh_mpm*100)>>24);
//		//ʱ��
//		buf[26] = (byte)((int)rh_range);
//		buf[27] = (byte)((int)rh_range>>8);
//		buf[28] = (byte)((int)rh_range>>16);
//		buf[29] = (byte)((int)rh_range>>24);
//		//��Ƶ
//		buf[30] = (byte)(rh_spp);
//		buf[31] = (byte)(rh_spp>>8);
//
//		//��������ʱ��
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
//		//�����޸�ʱ��
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
//		//��糣��
//		buf[54] = (byte)((int)(rh_epsr*10));
//		buf[55] = (byte)((int)(rh_epsr*10)>>8);
//		buf[56] = (byte)((int)(rh_epsr*10)>>16);
//		buf[57] = (byte)((int)(rh_epsr*10)>>24);
//		//�����չ
//		buf[109] = (byte)(rh_flagExt);
//		buf[110] = (byte)(rh_flagExt>>8);
//		//������ʽ
//		buf[111] = (rh_workType);
//		//Ӳ������
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

    //��byteת��Ϊ��8�����ݵ�byte����
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

        //ȡ������
        rh_nsamp = buf[4];
        rh_nsamp |= (buf[5] << 8);
        if (rh_nsamp == -128)
            rh_nsamp = 128;

        //����λ��
        rh_bits = buf[6];
        rh_bits |= buf[7] << 8;

        //ɨ��
        temVal = 0;
        rh_sps = buf[10];
        temVal = buf[11] << 8;
        rh_sps += temVal;
        temVal = buf[12] << 16;
        rh_sps += temVal;
        temVal = buf[13] << 24;
        rh_sps += temVal;

        //��Ǽ��
        rh_mpm = buf[18];
        temVal = buf[19] << 8;
        rh_mpm += temVal;
        temVal = buf[20] << 16;
        rh_mpm += temVal;
        temVal = buf[21] << 24;
        rh_mpm += temVal;
        rh_mpm = rh_mpm / 100;

        //ʱ��
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

        //��Ƶ
        this.rh_spp = buf[30];
        this.rh_spp += buf[31] << 8;

        //��糣��
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

        //��Ǽ��
        rh_flagExt = buf[109];
        rh_flagExt += buf[110] << 8;

        //������ʽ
        rh_workType = buf[111];

        //Ӳ������
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

        //�Բ������м��
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
