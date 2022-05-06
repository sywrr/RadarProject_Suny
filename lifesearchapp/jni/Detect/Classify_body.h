#pragma once
#ifndef __CLASSIFY_BODY_H__
#define __CLASSIFY_BODY_H__
//#include "iirfilter.c"
//extern void iirbcf(int ifilt, int band, int ns, int n, double f1, double f2, double f3, double f4, double db, double b[], double a[]);

class Classify_body
{
	//typedef struct
	//{
	//	float re;
	//	float im;
	//} COMPLEX; //����һ�������ṹ

	#define N_rowpoint 4 //����ѹ������ѡ��
	#define WIN 24       //�˻�������С
		//#define  ROW             640
		//#define  pre_row         128      //�����ź�Ԥ�������ݵ�����
		//#define  STORE_ROW       768      //�����źŴ�����������ݵ���
	#define PI 3.1415926536
	#define BACK_SIZE 16
	#define TH1 3.1
	#define TH2 /*0.68*/0.2
	#define TH3 1.5

	

	int AD_ELEMENT_SIZE;    //512
	int SLAVE_SIZE; /*512*/ //AD_ELEMENT_SIZE/N_rowpoint
	int HARF_AD_SIZE;       /*256*/
	int REP_LEN;            //STATIC_LEN-WIN-1
	int ROW;
	int pre_row;   //�����ź�Ԥ�������ݵ�����
	int STORE_ROW; //�����źŴ�����������ݵ���

	//COMPLEX sdata[640];
	short result_data_body[20];          //�õ��Ĵ������
	// �˴��Ķ�������Ϊfloat
	float compressdata[2048 - 1]/* = {0.}*/; //��ѹ��������host_dsp���͹���
	float compdata;
	float back_mov_data[8192 / N_rowpoint]; //����������ز�
	float ad_data_add[8192 / N_rowpoint];   //16λAD�����������
	float processdata_t[1024][BACK_SIZE];

	short send_ad_data[8192 / N_rowpoint]; //��ARM���͵�����
	float move_data[1024];
	float mean_data[1024];
	float var_processdata[1024];
	//short bodymove_result[20];
	short pro_ad_data[2048]; //

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


	//void detect_breath(short result_data1[], float breath_th);
	//extern void FFT(COMPLEX *xin, int N); //��ʽ�任��������
	//extern void self_filter(short track_len, short filter_order, float x[], float y[]);
	//void pre_breath(void);                //�����ź�Ԥ�������������ݴ��
	//void breath_process(float breath_th); //�����źź�������

	//void iirbcf(int ifilt, int band, int ns, int n, double f1, double f2, double f3, double f4, double db, double b[], double a[]);
	void pre_process(void); //�����ֲ��ź�
	void z_ave_process(void);
	void zscore(void);
	void body_detect(void);
	void BandPass(int points, float *x, short *xout); //��ͨ�˲�;
	

public:
	//short ad_frame_data1[8192]; //�˴����ԭʼ����
	int multi_flag;

	Classify_body(void);
	~Classify_body(void);
	void init_body(void);
	void changeParams(unsigned _fscan, short _antenna_type, unsigned _window, unsigned _ad_element_size);
	void detect_body_pre(short ad_frame_data1[]);
	void detect_body(short result_data1[]);
};

#endif

