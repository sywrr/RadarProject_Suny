#pragma once
#ifndef __CLASSIFY_BREATH_H__
#define __CLASSIFY_BREATH_H__
//#include "filter.c"
//#include "stdlib.h"
typedef struct
{
	float re;
	float im;
} COMPLEX; //定义一个复数结构

//extern void FFT(COMPLEX *xin, int N); //傅式变换处理函数
//extern void self_filter(short track_len, short filter_order, float x[], float y[]);
class Classify_breath
{
	#define N_rowpoint 4 //道内压缩点数选择
	#define WIN 24       //此滑动窗大小
		//#define  ROW             640
		//#define  pre_row         128      //呼吸信号预处理数据道数，
		//#define  STORE_ROW       768      //呼吸信号处理所需的数据道数
	#define PI 3.1415926536
	#define BACK_SIZE 16
	//#define TH1 3.1
	//#define TH2 0.68
	//#define TH3 0.6

	int multi_flag;

	int AD_ELEMENT_SIZE;    //512
	int SLAVE_SIZE; /*512*/ //AD_ELEMENT_SIZE/N_rowpoint
	int HARF_AD_SIZE;       /*256*/
	int REP_LEN;            //STATIC_LEN-WIN-1
	int ROW;
	int pre_row;   //呼吸信号预处理数据道数，
	int STORE_ROW; //呼吸信号处理所需的数据道数

	//COMPLEX sdata[640];
	COMPLEX *sdata;
	//short result_data_body[20];          //得到的处理结果
	short *result_data_breath;        //得到的处理结果
	//float accumdata1[2048];              //每道数据加窗后结果
	float **processdata/*[768][2048]*//* = {0.}*/; //呼吸信号后处理数组数据
	// 此处改动，定义为float
	float *compressdata/*[2048 - 1]*//* = {0.}*/; //此压缩数据由host_dsp传送过来
	float *MMax/*[2048]*/ /*= {0.}*/;
	float *enselect/*[640]*/ /*= {0.}*/; //最佳波形
	float *outdata1/*[640]*/ /*= {0.}*/; //最佳波形滤波后输出结果?
	float *Energy_breath/*[512 / 2]*/ /*= {0.}*/;
	//short ad_frame_data1[8192]; //此处存放原始数据
	float compdata;

	volatile unsigned int ftnum; //傅式变化点数，需要根据扫速来确定
	volatile unsigned int frame_num;
	volatile unsigned short reprocess_end_flag; //预判处理完成标志
	volatile unsigned short process_end_flag;   //处理完成标志
	volatile unsigned short receive_data_flag;
	volatile unsigned int command_flag;
	volatile unsigned int work_flag;
	volatile unsigned int ad_row; //ad convert 's 列数
	volatile unsigned int fscan; //扫速
	volatile unsigned short signal_position;
	//volatile unsigned short time_windows=20;
	volatile unsigned short detect_flag;
	volatile unsigned int flag; //包数据计算
	volatile unsigned int fflag;
	volatile float breathmove;            //呼吸信号距离信息
	volatile unsigned int breathflag;      //呼吸信号标志位
	volatile unsigned int pre_breath_flag; //呼吸信号预判结果计数
	volatile unsigned int epsilon;         //介电常数默认为1
	volatile float dao_pre_row;
	volatile float dao_pre_row1;
	volatile unsigned int ad_row_full;
	volatile unsigned int process_flag;
	volatile unsigned int pos_val;
	//volatile unsigned int m=0;

	volatile unsigned short ad_frame_flag;
	volatile unsigned short detect_flag_cun;
	volatile unsigned short bodymove_flag;
	//volatile unsigned int detect_flag=0;
	//volatile unsigned int epsilon=1;//介电常数默认为1
	volatile unsigned int signalpos; //信号位置需要从上位机中获取
	volatile unsigned int bodyflag;  //体动有无标示
	volatile float bodymove;
	volatile unsigned int ad_row_t;
	volatile unsigned int background_num1; //去背景道数
	volatile unsigned int zscore_flag;
	volatile unsigned int Bscan_num;
	volatile unsigned int detect_num;
	volatile unsigned int detec_start;
	//volatile unsigned int pos_val=0;
	double FilterA[5], FilterB[5];
	volatile double FLcut; /*实际滤波截至频率*/
	volatile double FRcut;
	short antenna_type;
	int window; //时窗
	int pre_num;
	int k;
	int slow_num;
	int slow_num_flag;

	//void FFT(COMPLEX *xin, int N); //傅式变换处理函数
	//void self_filter(short track_len, short filter_order, float x[], float y[]);
	void pre_breath(void);                //呼吸信号预处理函数及数据打包
	void breath_process(float breath_th); //呼吸信号后处理函数
	
public:
	//short ad_frame_data1[8192]; //此处存放原始数据

	Classify_breath(void);
	~Classify_breath(void);
	void init_breath(void);
	void changeParams(unsigned _fscan, short _antenna_type, unsigned _window, unsigned _ad_element_size);
	void detect_breath_pre(short ad_frame_data1[]);
	void detect_breath(short result_data1[], float breath_th, int &isEnd);
	//sunyongmin 2021.12.27 add begin
	void self_filter(short track_len, short filter_order, float x[], float y[]);
	COMPLEX Mul(COMPLEX c1, COMPLEX c2);
	void FFT(COMPLEX *xin, int N);
	//sunyongmin 2021.12.27 add end
};
#endif
