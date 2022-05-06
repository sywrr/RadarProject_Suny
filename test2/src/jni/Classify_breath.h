#pragma once
#ifndef __CLASSIFY_BREATH_H__
#define __CLASSIFY_BREATH_H__
//#include "filter.c"
//#include "stdlib.h"
typedef struct
{
	float re;
	float im;
} COMPLEX; //����һ�������ṹ

//extern void FFT(COMPLEX *xin, int N); //��ʽ�任������
//extern void self_filter(short track_len, short filter_order, float x[], float y[]);
class Classify_breath
{
	#define N_rowpoint 4 //����ѹ������ѡ��
	#define WIN 24       //�˻�������С
		//#define  ROW             640
		//#define  pre_row         128      //�����ź�Ԥ�������ݵ�����
		//#define  STORE_ROW       768      //�����źŴ�����������ݵ���
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
	int pre_row;   //�����ź�Ԥ�������ݵ�����
	int STORE_ROW; //�����źŴ�����������ݵ���

	//COMPLEX sdata[640];
	COMPLEX *sdata;
	//short result_data_body[20];          //�õ��Ĵ�����
	short *result_data_breath;        //�õ��Ĵ�����
	//float accumdata1[2048];              //ÿ�����ݼӴ�����
	float **processdata/*[768][2048]*//* = {0.}*/; //�����źź�����������
	// �˴��Ķ�������Ϊfloat
	float *compressdata/*[2048 - 1]*//* = {0.}*/; //��ѹ��������host_dsp���͹���
	float *MMax/*[2048]*/ /*= {0.}*/;
	float *enselect/*[640]*/ /*= {0.}*/; //��Ѳ���
	float *outdata1/*[640]*/ /*= {0.}*/; //��Ѳ����˲���������?
	float *Energy_breath/*[512 / 2]*/ /*= {0.}*/;
	//short ad_frame_data1[8192]; //�˴����ԭʼ����
	float compdata;

	volatile unsigned int ftnum; //��ʽ�仯��������Ҫ����ɨ����ȷ��
	volatile unsigned int frame_num;
	volatile unsigned short reprocess_end_flag; //Ԥ�д�����ɱ�־
	volatile unsigned short process_end_flag;   //������ɱ�־
	volatile unsigned short receive_data_flag;
	volatile unsigned int command_flag;
	volatile unsigned int work_flag;
	volatile unsigned int ad_row; //ad convert 's ����
	volatile unsigned int fscan; //ɨ��
	volatile unsigned short signal_position;
	//volatile unsigned short time_windows=20;
	volatile unsigned short detect_flag;
	volatile unsigned int flag; //�����ݼ���
	volatile unsigned int fflag;
	volatile float breathmove;            //�����źž�����Ϣ
	volatile unsigned int breathflag;      //�����źű�־λ
	volatile unsigned int pre_breath_flag; //�����ź�Ԥ�н������
	volatile unsigned int epsilon;         //��糣��Ĭ��Ϊ1
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
	//volatile unsigned int epsilon=1;//��糣��Ĭ��Ϊ1
	volatile unsigned int signalpos; //�ź�λ����Ҫ����λ���л�ȡ
	volatile unsigned int bodyflag;  //�嶯���ޱ�ʾ
	volatile float bodymove;
	volatile unsigned int ad_row_t;
	volatile unsigned int background_num1; //ȥ��������
	volatile unsigned int zscore_flag;
	volatile unsigned int Bscan_num;
	volatile unsigned int detect_num;
	volatile unsigned int detec_start;
	//volatile unsigned int pos_val=0;
	double FilterA[5], FilterB[5];
	volatile double FLcut; /*ʵ���˲�����Ƶ��*/
	volatile double FRcut;
	short antenna_type;
	int window; //ʱ��
	int pre_num;
	int k;
	int slow_num;
	int slow_num_flag;

	//void FFT(COMPLEX *xin, int N); //��ʽ�任������
	//void self_filter(short track_len, short filter_order, float x[], float y[]);
	void pre_breath(void);                //�����ź�Ԥ�����������ݴ��
	void breath_process(float breath_th); //�����źź�����
	
public:
	//short ad_frame_data1[8192]; //�˴����ԭʼ����

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
