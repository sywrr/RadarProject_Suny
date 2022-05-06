#ifndef __GLOBAL_H__
#define __GLOBAL_H__

#include "math.h"

typedef struct
{
    float re;
    float im;
} COMPLEX; //定义一个复数结构

#define N_rowpoint 4 //道内压缩点数选择
#define WIN 24       //此滑动窗大小
//#define  ROW             640
//#define  pre_row         128      //呼吸信号预处理数据道数，
//#define  STORE_ROW       768      //呼吸信号处理所需的数据道数
#define PI 3.1415926536
#define BACK_SIZE 16
#define TH1 3.1
#define TH2 0.68
#define TH3 0.6
int multi_flag=0;

int AD_ELEMENT_SIZE = 8192;    //512
int SLAVE_SIZE = 2048; /*512*/ //AD_ELEMENT_SIZE/N_rowpoint
int HARF_AD_SIZE = 1024;       /*256*/
int REP_LEN = 2000;            //STATIC_LEN-WIN-1
int ROW = 640;
int pre_row = 128;   //呼吸信号预处理数据道数，
int STORE_ROW = 768; //呼吸信号处理所需的数据道数

// void changeParams(unsigned _fscan, short _antenna_type, unsigned _window, unsigned _ad_element_size);
// void detect_pre(short ad_frame_data1[]);
// void detect_body(short result_data1[]);
// void detect_breath(short result_data1[], float breath_th);
// extern void FFT(COMPLEX *xin, int N); //傅式变换处理函数
// extern void self_filter(short track_len, short filter_order, float x[], float y[]);
// void pre_breath(void);                //呼吸信号预处理函数及数据打包
// void breath_process(float breath_th); //呼吸信号后处理函数
// extern void iirbcf(int ifilt, int band, int ns, int n, double f1, double f2, double f3, double f4, double db, double b[], double a[]);
// void pre_process(void); //处理轮测信号
// void z_ave_process(void);
// void zscore(void);
// void body_detect(void);
// void BandPass(int points, float *x, short *xout); //带通滤波;
// void init(void);

COMPLEX sdata[640];
short result_data_body[20];          //得到的处理结果
short result_data_breath[20];        //得到的处理结果
float accumdata1[2048];              //每道数据加窗后结果
float processdata[768][2048] = {0.}; //呼吸信号后处理数组数据
//unsigned short master_command[20];
// short compressdata[2048 - 1] = {0.}; //此压缩数据由host_dsp传送过来
// 此处改动，定义为float
float compressdata[2048 - 1] = {0.}; //此压缩数据由host_dsp传送过来
float MMax[2048] = {0.};
float enselect[640] = {0.}; //最佳波形
float outdata1[640] = {0.}; //最佳波形滤波后输出结果?
float Energy_breath[512 / 2] = {0.};
short ad_frame_data1[8192]; //此处存放原始数据
float compdata;

short ad_pingdata[8192];
//short ad_frame_data1[8192];//此处存放原始数据
//short compressdata[AD_ELEMENT_SIZE/N_rowpoint-1]={0.};

short preprocess_data1[8192];           //此处存放预处理结果
float back_mov_data[8192 / N_rowpoint]; //背景消除后回波
float ad_data_add[8192 / N_rowpoint];   //16位AD采样存放数组
float processdata_t[1024][BACK_SIZE];

short send_ad_data[8192 / N_rowpoint]; //向ARM发送的数组
float move_data[1024];
float mean_data[1024];
float var_processdata[1024];
//short bodymove_result[20];
short pro_ad_data[2048]; //

volatile unsigned int ftnum = 512; //傅式变化点数，需要根据扫速来确定
volatile unsigned int frame_num;
volatile unsigned short reprocess_end_flag; //预判处理完成标志
volatile unsigned short process_end_flag;   //处理完成标志
volatile unsigned short receive_data_flag = 0;
volatile unsigned int command_flag = 0;
volatile unsigned int work_flag = 0;
volatile unsigned int ad_row = 0; //ad convert 's 列数
volatile unsigned int fscan = 16; //扫速
volatile unsigned short signal_position = 0;
//volatile unsigned short time_windows=20;
volatile unsigned short detect_flag = 0;
volatile unsigned int flag = 0; //包数据计算
volatile unsigned int fflag = 0;
volatile float breathmove = 0.;            //呼吸信号距离信息
volatile unsigned int breathflag = 0;      //呼吸信号标志位
volatile unsigned int pre_breath_flag = 0; //呼吸信号预判结果计数
volatile unsigned int epsilon = 1;         //介电常数默认为1
volatile float dao_pre_row;
volatile float dao_pre_row1;
volatile unsigned int ad_row_full = 0;
volatile unsigned int process_flag = 0;
volatile unsigned int pos_val = 0;
//volatile unsigned int m=0;

volatile unsigned short ad_frame_flag = 0;
volatile unsigned short detect_flag_cun = 0;
volatile unsigned short bodymove_flag = 0;
//volatile unsigned int detect_flag=0;
//volatile unsigned int epsilon=1;//介电常数默认为1
volatile unsigned int signalpos = 0; //信号位置需要从上位机中获取
volatile unsigned int bodyflag = 0;  //体动有无标示
volatile float bodymove = 0.;
volatile unsigned int ad_row_t = 0;
volatile unsigned int background_num1 = 0; //去背景道数
volatile unsigned int zscore_flag = 0;
volatile unsigned int Bscan_num = 0;
volatile unsigned int detect_num = 0;
volatile unsigned int detec_start = 0;
//volatile unsigned int pos_val=0;
double FilterA[5], FilterB[5];
volatile double FLcut; /*实际滤波截至频率*/
volatile double FRcut;
short antenna_type = /*900*/ 400;
int window = /*66*/ 80; //时窗
int pre_num = 2;
int k;
int slow_num;
int slow_num_flag = 4;

void changeParams(unsigned _fscan, short _antenna_type, unsigned _window, unsigned _ad_element_size) {
    fscan = _fscan;
    antenna_type = _antenna_type;
    window = _window;
    AD_ELEMENT_SIZE = _ad_element_size;
    SLAVE_SIZE = AD_ELEMENT_SIZE / N_rowpoint;
    HARF_AD_SIZE = SLAVE_SIZE / 2;
    REP_LEN = SLAVE_SIZE - WIN - 1;
}

void detect_body_pre(short ad_frame_data1[]) {
    int i, j /*,k*/;
    //int last;

    for (i = 0; i < 20; i++) {
        result_data_body[i] = 0;
//        result_data_breath[i] = 0;
    }
    //init();

    //for (m=0;m<(last-1024)/AD_ELEMENT_SIZE/sizeof(short);m++)
    //{
    //fread(ad_frame_data1,sizeof(short),AD_ELEMENT_SIZE,fw);
    for (i = 1; i < SLAVE_SIZE; i++) {
        compdata = 0.;
        for (j = 0; j < N_rowpoint; j++) {
            compdata = compdata + ((float)ad_frame_data1[N_rowpoint * i + j]);
        }
        compressdata[i - 1] = floorf(compdata * 0.25);
    }
    ////////////////////////////////////////////////////////////
}

void detect_breath_pre(short ad_frame_data1[]) {
    int i, j /*,k*/;
    //int last;

    for (i = 0; i < 20; i++) {
//        result_data_body[i] = 0;
        result_data_breath[i] = 0;
    }
    //init();

    //for (m=0;m<(last-1024)/AD_ELEMENT_SIZE/sizeof(short);m++)
    //{
    //fread(ad_frame_data1,sizeof(short),AD_ELEMENT_SIZE,fw);
    for (i = 1; i < SLAVE_SIZE; i++) {
        compdata = 0.;
        for (j = 0; j < N_rowpoint; j++) {
            compdata = compdata + ((float)ad_frame_data1[N_rowpoint * i + j]);
        }
        compressdata[i - 1] = floorf(compdata * 0.25);
    }
}

#endif
