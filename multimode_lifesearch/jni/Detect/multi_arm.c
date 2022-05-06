// bodydetect.cpp : �������̨Ӧ�ó������ڵ㡣
//

//#include "stdafx.h"
#include <stdio.h>
#include <string.h>
#include <math.h>
//#include <time.h>
// #include <windows.h>
//#include "afxdialogex.h"
#include "./filter.c"
#include "./iirfilter.c"

// /////////////���峣���ͺ���///////////////////
// typedef struct
// {
// 	float re;
// 	float im;
// } COMPLEX; //����һ�������ṹ

#define AD_ELEMENT_SIZE 8192	//512
#define STATIC_LEN 2048 /*512*/ //ÿ����Ԫ�ظ���
#define N_rowpoint 4			//����ѹ������ѡ��
#define WIN 24					//�˻�������С
#define REP_LEN 2000			//STATIC_LEN-WIN-1
#define ACCU_LEN 2000			//STATIC_LEN-WIN-1
#define ROW 640
#define pre_row 128   //�����ź�Ԥ�������ݵ�����c'c
#define STORE_ROW 768 //�����źŴ�����������ݵ���
#define PI 3.1415926536
#define SLAVE_SIZE 2048 /*512*/ //AD_ELEMENT_SIZE/N_rowpoint
#define BACK_SIZE 16
#define HARF_AD_SIZE 1024 /*256*/
#define TH1 3.1
#define TH2 0.68

void detect(short ad_frame_data1[], short result_data[]);
// extern void FFT(COMPLEX *xin, int N); //��ʽ�任������
// extern void self_filter(short track_len, short filter_order, float x[], float y[]);
void pre_breath(void);	 //�����ź�Ԥ�����������ݴ��
void breath_process(short result_data[]); //�����źź�����
extern void iirbcf(int ifilt, int band, int ns, int n, double f1, double f2, double f3, double f4, double db, double b[], double a[]);
void pre_process(void); //�����ֲ��ź�
void z_ave_process(short result_data[]);
void zscore(short result_data[]);
void body_detect(short result_data[]);
void BandPass(int points, float *x, short *xout); //��ͨ�˲�;
void init();

COMPLEX sdata[ROW];
// short result_data[20];						  //�õ��Ĵ�����
float accumdata1[ACCU_LEN];					  //ÿ�����ݼӴ�����
float processdata[STORE_ROW][REP_LEN] = {{0.}}; //�����źź�����������
//unsigned short master_command[20];
short compressdata[STATIC_LEN - 1] = {0.}; //��ѹ��������host_dsp���͹���
float MMax[REP_LEN] = {0.};
float enselect[ROW] = {0.}; //��Ѳ���
float outdata1[ROW] = {0.}; //��Ѳ����˲���������?
float Energy_breath[64] = {0.};
// short ad_frame_data1[AD_ELEMENT_SIZE]; //�˴����ԭʼ����
float compdata;

short ad_pingdata[AD_ELEMENT_SIZE];
// short ad_frame_data1[AD_ELEMENT_SIZE]; //�˴����ԭʼ����
//short compressdata[AD_ELEMENT_SIZE/N_rowpoint-1]={0.};
float compdata;

short preprocess_data1[AD_ELEMENT_SIZE];			   //�˴����Ԥ������
float back_mov_data[AD_ELEMENT_SIZE / N_rowpoint - 1]; //����������ز�
float ad_data_add[AD_ELEMENT_SIZE / N_rowpoint - 1];   //16λAD�����������
float processdata_t[HARF_AD_SIZE][BACK_SIZE];

short send_ad_data[AD_ELEMENT_SIZE / N_rowpoint - 1]; //��ARM���͵�����
float move_data[HARF_AD_SIZE];
float mean_data[HARF_AD_SIZE];
float var_processdata[HARF_AD_SIZE];
//short bodymove_result[20];
short pro_ad_data[SLAVE_SIZE]; //

volatile unsigned int ftnum = 512; //��ʽ�仯��������Ҫ����ɨ����ȷ��
volatile unsigned int frame_num;
volatile unsigned short reprocess_end_flag; //Ԥ�д�����ɱ�־
volatile unsigned short process_end_flag;   //������ɱ�־
volatile unsigned short receive_data_flag = 0;
volatile unsigned int command_flag = 0;
volatile unsigned int work_flag = 0;
volatile unsigned int ad_row = 0; //ad convert 's ����
volatile unsigned int fscan = 32;
volatile unsigned short signal_position = 0;
//volatile unsigned short time_windows=20;
volatile unsigned short detect_flag = 0;
volatile unsigned int flag = 0;			   //�����ݼ���
volatile unsigned int fflag = 0;			   //�����ݼ���
volatile float breathmove = 0.;			   //�����źž�����Ϣ
volatile unsigned int breathflag = 0;	  //�����źű�־λ
volatile unsigned int pre_breath_flag = 0; //�����ź�Ԥ�н������
volatile unsigned int epsilon = 1;		   //��糣��Ĭ��Ϊ1
volatile float dao_pre_row;
volatile float dao_pre_row1;
volatile unsigned int ad_row_full = 0;
volatile unsigned int process_flag = 0;
volatile unsigned int pos_val = 0;
//volatile unsigned int m = 0;

volatile unsigned short ad_frame_flag = 0;
volatile unsigned short detect_flag_cun = 0;
volatile unsigned short bodymove_flag = 0;
//volatile unsigned int detect_flag=0;
//volatile unsigned int epsilon=1;//��糣��Ĭ��Ϊ1
volatile unsigned int signalpos = 0; //�ź�λ����Ҫ����λ���л�ȡ
volatile unsigned int bodyflag = 0;  //�嶯���ޱ�ʾ
volatile float bodymove = 0.;
volatile unsigned int ad_row_t = 0;
volatile unsigned int background_num1 = 0; //ȥ��������
volatile unsigned int zscore_flag = 0;
volatile unsigned int Bscan_num = 0;
volatile unsigned int detect_num = 0;
volatile unsigned int detec_start = 0;
//volatile unsigned int pos_val=0;
double FilterA[5], FilterB[5];
volatile double FLcut; /*ʵ���˲�����Ƶ��*/
volatile double FRcut;
short antenna_type = /*900*/ 400;
unsigned int window = /*66*/ 80; //ʱ��
int pre_num = 2;
int k;
int slow_num;

//FILE *fp;
//FILE *fp1;
//FILE *fp2;
//FILE *fp3;
//clock_t  clockBegin, clockEnd,clockEnd1;

void detect(short ad_frame_data1[], short result_data[])
{
	int i, j /*,k*/;
	// int last;

	//init(result_data);

	// for (m = 0; m <  AD_ELEMENT_SIZE / sizeof(short); m++)
	{
		// fread(ad_frame_data1, sizeof(short), AD_ELEMENT_SIZE, fw);
		for (i = 1; i < STATIC_LEN; i++)
		{
			compdata = 0.;
			for (j = 0; j < N_rowpoint; j++)
			{
				compdata = compdata + ((float)ad_frame_data1[N_rowpoint * i + j]);
			}
			compressdata[i - 1] = (short)(compdata * 0.25);
		}
		////////////////////////////////////////////////////////////
		pre_process();
		z_ave_process(result_data);
		//if(bodymove_flag==1 /*&& msignal_flag>1*/)
		//{
		//	bodymove_flag=0;
		//	//fprintf(fp,"%d�� %d %d\n",k,bodymove_result[2],pos_val);
		//	//fflush(fp);
		//	bodymove_result[2]=0;
		//	bodymove_result[3]=0;
		//}
		///////////////////////////////////////////////////////
		accumdata1[0] = 0.;
		for (i = 0; i <= WIN; i++)
			accumdata1[0] = accumdata1[0] + (float)compressdata[i];
		for (i = 1; i < ACCU_LEN; i++)
			accumdata1[i] = accumdata1[i - 1] + ((float)compressdata[i + WIN - 1] - (float)compressdata[i - 1]); /////�����Ӵ�

		for (i = 0; i < REP_LEN; i++)
		{
			processdata[ad_row][i] = accumdata1[i];
		}
		// processdata[ad_row][0] = m; /////////////////////////////////////����
		frame_num = ad_row % pre_row;
		if (frame_num == (pre_row - 1))
			process_flag = 1;
		ad_row++;
		if (ad_row >= ROW)
			ad_row_full = 1;
//		else
//			ad_row_full = ad_row_full;
		if (ad_row == STORE_ROW)
			ad_row = 0;
		receive_data_flag = 1;
		//////////////////////////////////////////////////////
		if (process_flag == 1)
		{
			process_flag = 0;
			flag++;
			//pre_breath();
			if (flag == 6)
				flag = 0;
			if (ad_row_full == 1)
			{
				if (flag == 0)
					fflag = 6;
				else
					fflag = flag;
				breath_process(result_data);
			}
		}
		//if(process_end_flag==1)	 //1��ʾ�������
		//{
		//	process_end_flag=0;
		//	//fprintf(fp,"%d�� %d %d\n",m,result_data[4],pos_val);
		//	//fflush(fp);
		//	result_data[4]=0;
		//	result_data[5]=0;
		//	pos_val=0;
		//}
	}
	//clockEnd = clock();
	//printf("%d\n", clockEnd - clockBegin);
	//fclose(fp);
	//fclose(fp1);
	//fclose(fp2);
	//fclose(fp3);
	//system("pause");
}

//////////////////////////////////////////
void pre_breath(void) //�����ź�Ԥ�жϼ����ݴ������
{
	unsigned i = 0, j = 0;
	unsigned int flagnum = 0;
	double Mean_processdata;
	double std_processdata;
	double dao_std_data;

	flagnum = pre_row * (flag - 1);
	for (i = 0; i < REP_LEN; i++)
	{
		Mean_processdata = 0.;
		std_processdata = 0.;
		for (j = 0; j < pre_row; j++)
			Mean_processdata += processdata[j + flagnum][i];
		Mean_processdata = Mean_processdata * dao_pre_row;

		for (j = 0; j < pre_row; j++)
			std_processdata += (processdata[j + flagnum][i] - Mean_processdata) * (processdata[j + flagnum][i] - Mean_processdata);
		std_processdata = sqrt(std_processdata * dao_pre_row1);
		if (std_processdata == 0.0)
			std_processdata = 1.;
		dao_std_data = 1.0 / std_processdata;
		for (j = 0; j < pre_row; j++)
		{
			processdata[j + flagnum][i] = (processdata[j + flagnum][i] - Mean_processdata) * dao_std_data;
		}
	}
}
void breath_process(short result_data[]) //�����źź������
{
	unsigned int i = 0, j = 0, k = 0, l = 0, f_position = 0, N_breath = 0;
	float MMean = 0.;
	float signalpower = 0.;
	float totalpower = 0.;
	float Max[7] = {0.};
	//float MMax[STATIC_LEN/N_rowpoint-1-WIN]={0.};
	int position = 0, MMMaxposition = 0, MMaxposition = 0;

	for (i = 0; i < REP_LEN /*STATIC_LEN/N_rowpoint-1-WIN*/; i++)
	{

		// for (l = 0; l < ROW; l++)
		// {
		// 	switch (fflag)
		// 	{
		// 	case 1:
		// 	{
		// 		if (l > pre_row - 1)
		// 		{
		// 			sdata[l].re = processdata[l - pre_row][i];
		// 			sdata[l].im = 0.0;
		// 		}

		// 		else
		// 		{
		// 			sdata[l].re = processdata[l + 5 * pre_row][i];
		// 			sdata[l].im = 0.0;
		// 		}

		// 		break;
		// 	}
		// 	case 2:
		// 	{
		// 		if (l > 2 * pre_row - 1)
		// 		{
		// 			sdata[l].re = processdata[l - 2 * pre_row][i];
		// 			sdata[l].im = 0.0;
		// 		}

		// 		else
		// 		{
		// 			sdata[l].re = processdata[l + 4 * pre_row][i];
		// 			sdata[l].im = 0.0;
		// 		}

		// 		break;
		// 	}
		// 	case 3:
		// 	{
		// 		if (l > 3 * pre_row - 1)
		// 		{
		// 			sdata[l].re = processdata[l - 3 * pre_row][i];
		// 			sdata[l].im = 0.0;
		// 		}

		// 		else
		// 		{
		// 			sdata[l].re = processdata[l + 3 * pre_row][i];
		// 			sdata[l].im = 0.0;
		// 		}

		// 		break;
		// 	}

		// 	case 4:
		// 	{
		// 		if (l > 4 * pre_row - 1)
		// 		{
		// 			sdata[l].re = processdata[l - 4 * pre_row][i];
		// 			sdata[l].im = 0.0;
		// 		}

		// 		else
		// 		{
		// 			sdata[l].re = processdata[l + 2 * pre_row][i];
		// 			sdata[l].im = 0.0;
		// 		}

		// 		break;
		// 	}

		// 	case 5:
		// 	{
		// 		sdata[l].re = processdata[l][i];
		// 		sdata[l].im = 0.0;
		// 		break;
		// 	}
		// 	case 6:
		// 	{
		// 		sdata[l].re = processdata[l + pre_row][i];
		// 		sdata[l].im = 0.0;
		// 		break;
		// 	}
		// 	default:
		// 		break;
		// 	}
		// }

		switch (fflag)
		{
		case 1:
		{
			for (l = 0; l < 4 * pre_row; l++)
			{
				sdata[l].re = processdata[l + 2 * pre_row][i];
				sdata[l].im = 0.0;
			}
			for (l = 4 * pre_row; l < ROW; l++)
			{
				sdata[l].re = processdata[l - 4 * pre_row][i];
				sdata[l].im = 0.0;
			}
			break;
		}
		case 2:
		{
			for (l = 0; l < 3 * pre_row; l++)
			{
				sdata[l].re = processdata[l + 3 * pre_row][i];
				sdata[l].im = 0.0;
			}
			for (l = 3 * pre_row; l < ROW; l++)
			{
				sdata[l].re = processdata[l - 3 * pre_row][i];
				sdata[l].im = 0.0;
			}
			break;
		}
		case 3:
		{
			for (l = 0; l < 2 * pre_row; l++)
			{
				sdata[l].re = processdata[l + 4 * pre_row][i];
				sdata[l].im = 0.0;
			}
			for (l = 2 * pre_row; l < ROW; l++)
			{
				sdata[l].re = processdata[l - 2 * pre_row][i];
				sdata[l].im = 0.0;
			}
			break;
		}
		case 4:
		{
			for (l = 0; l < pre_row; l++)
			{
				sdata[l].re = processdata[l + 5 * pre_row][i];
				sdata[l].im = 0.0;
			}
			for (l = pre_row; l < ROW; l++)
			{
				sdata[l].re = processdata[l - pre_row][i];
				sdata[l].im = 0.0;
			}

			break;
		}
		case 5:
		{
			for (l = 0; l < ROW; l++)
			{
				sdata[l].re = processdata[l][i];
				sdata[l].im = 0.0;
			}
			break;
		}
		case 6:
		{
			for (l = 0; l < ROW; l++)
			{
				sdata[l].re = processdata[l + pre_row][i];
				sdata[l].im = 0.0;
			}
			break;
		}
		default:
			break;
		}

		FFT(sdata, ftnum);
		for (k = 3; k < 7; k++)
		{
			Max[k] = sqrt((float)(sdata[k].re * sdata[k].re) + (float)(sdata[k].im * sdata[k].im));
			if (Max[k] > Max[MMaxposition])
				MMaxposition = k;
		}
		MMax[i] = Max[MMaxposition];
		if (MMax[i] > MMax[MMMaxposition])
			MMMaxposition = i;
		MMean = MMean + MMax[i];
	}
	MMean = MMean / (STATIC_LEN / N_rowpoint - 1 - WIN);
	for (j = 0; j < ROW; j++)
	{
		switch (flag)
		{
		case 1:
		{
			if (j > pre_row - 1)
			{
				enselect[j] = processdata[j - pre_row][MMMaxposition];
			}

			else
			{
				enselect[j] = processdata[j + 5 * pre_row][MMMaxposition];
			}
			break;
		}

		case 2:
		{
			if (j > 2 * pre_row - 1)
			{
				enselect[j] = processdata[j - 2 * pre_row][MMMaxposition];
			}

			else
			{
				enselect[j] = processdata[j + 4 * pre_row][MMMaxposition];
			}
			break;
		}
		case 3:

		{
			if (j > 3 * pre_row - 1)
			{
				enselect[j] = processdata[j - 3 * pre_row][MMMaxposition];
			}

			else
			{
				enselect[j] = processdata[j + 3 * pre_row][MMMaxposition];
			}
			break;
		}

		case 4:
		{
			if (j > 4 * pre_row - 1)
			{
				enselect[j] = processdata[j - 4 * pre_row][MMMaxposition];
			}

			else
			{
				enselect[j] = processdata[j + 2 * pre_row][MMMaxposition];
			}
			break;
		}
		case 5:
		{
			enselect[j] = processdata[j][MMMaxposition];
			break;
		}
		case 6:
		{
			enselect[j] = processdata[j + pre_row][MMMaxposition];
			break;
		}
		default:
			break;
		}
	}

	self_filter(ROW, 2 * fscan, enselect, outdata1);
	for (l = 0; l < ROW - 2 * fscan; l++)
	{
		sdata[l].re = outdata1[l + 2 * fscan];
		sdata[l].im = 0.0;
	}
	FFT(sdata, ftnum);
	for (k = 0; k < ftnum / 2; k++)
	{
		if (k >= 3 && k < 7)
			signalpower = signalpower + sqrt((float)(sdata[k].re * sdata[k].re) + (float)(sdata[k].im * sdata[k].im));
		totalpower = totalpower + sqrt((float)(sdata[k].re * sdata[k].re) + (float)(sdata[k].im * sdata[k].im));
		//Energy_breath[k]=sqrt((float)(sdata[k].re*sdata[k].re)+(float)(sdata[k].im*sdata[k].im));
		//if(Energy_breath[k]>Energy_breath[f_position])
		//{f_position=k;}
	}
	totalpower = totalpower - signalpower; //ʵ����ָ��������������ֵ

	if (MMax[MMMaxposition] / MMean >= 0.8)
	{
		breathflag = 1;
		breathmove = (signal_position + 27.0 * (MMMaxposition + 1) / 496.0) * 15.0 / sqrt((float)(epsilon));
		N_breath = ceil((f_position + 1) / (float)(ftnum)*60.0 * (float)(fscan));
		pos_val = MMMaxposition;
	}

	else
	{
		breathflag = 0;
		breathmove = 0;
	}

	result_data[0] = 0xDDAA;
	result_data[1] = 4;
	//result_data[2]=0;
	//result_data[3]=0;
	result_data[4] = breathflag;
	result_data[5] = (short)(breathmove); //�ϴ������ź��б���
	process_end_flag = 1;				  //1��ʾ�������
}
/*�������*/
void pre_process(void)
{
	unsigned int i = 0;
	volatile float ave_numadd = 1.;
	volatile float back_numadd = 1.;

	background_num1++;
	back_numadd = 1 / (float)(background_num1);
	for (i = 0; i < SLAVE_SIZE; i++)
	{
		back_mov_data[i] = compressdata[i] - ad_data_add[i];
		if (background_num1 <= BACK_SIZE) //background_row1)
			ad_data_add[i] = (ad_data_add[i] * (background_num1 - 1) + compressdata[i]) * back_numadd;
		else
		{
			ad_data_add[i] = (ad_data_add[i] * (BACK_SIZE - 1) + compressdata[i]) / BACK_SIZE;
			background_num1 = 301;
		}
		//fprintf(fp3,"%f ",back_mov_data[i]);
	}
	//fprintf(fp3,"\n");
	BandPass(SLAVE_SIZE, &back_mov_data[0], &send_ad_data[0]); // back_mov_data��������֮������ݣ�send_ad_dataΪ��ͨ�˲��������
	//for (i=0;i<SLAVE_SIZE;i++)
	//{
	//	fprintf(fp3,"%d ",send_ad_data[i]);
	//}
	//fprintf(fp3,"\n");

	return;
}
void z_ave_process(short result_data[]) //�������ݽ���ƽ��Ȼ���ٽ��д���
{
	int i = 0;
	if (ad_row_t < pre_num)
	{
		for (i = 0; i < SLAVE_SIZE; i++)
			pro_ad_data[i] = send_ad_data[i];
		//pro_ad_data[i]=(pro_ad_data[i]*ad_row_t+send_ad_data[i])/(ad_row_t+1);
		ad_row_t++;
		zscore_flag = 0;
		return;
	}
	else
	{
		for (i = 0; i < SLAVE_SIZE; i++)
		{
			pro_ad_data[i] = (short)((pro_ad_data[i] + send_ad_data[i]) * 0.5);
			//pro_ad_data[i]=(pro_ad_data[i]*ad_row_t+send_ad_data[i])/(ad_row_t+1);
			//fprintf(fp1,"%d ",pro_ad_data[i]);
		}
		//fprintf(fp1,"\n");
		ad_row_t = 0;
		zscore_flag = 1;
		zscore(result_data);
	}
}

void zscore(short result_data[]) //int nsamp,short send_ad_data[], float processdata[]
{
	int i = 0;
	float meancol = 0.0, std1 = 0.0, std2 = 0.0;
	float mean_numadd = 0;
	meancol = 0.0;
	std1 = 0.0;
	std2 = 0.0;
	for (i = 0; i < SLAVE_SIZE; i++)
		meancol = meancol + pro_ad_data[i];
	meancol = meancol / (SLAVE_SIZE);
	for (i = 0; i < SLAVE_SIZE; i++)
		std1 = std1 + (pro_ad_data[i] - meancol) * (pro_ad_data[i] - meancol);
	std2 = sqrt(std1 / (float)(SLAVE_SIZE));
	for (i = 0; i < SLAVE_SIZE / 2; i++)
	{
		move_data[i] = processdata_t[i][Bscan_num];
		processdata_t[i][Bscan_num] = (pro_ad_data[2 * i] - meancol) / std2; //��������������Ҫ�ŵ������в���������
																			 //processdata_t[i][Bscan_num]=pro_ad_data[2*i];
																			 //fprintf(fp3,"%f ",processdata_t[i][Bscan_num]);
	}
	//fprintf(fp3,"\n");
	detect_num++;
	mean_numadd = 1 / (float)detect_num;
	if (detect_num <= BACK_SIZE) //detect_num��1��ʼ��Bscan_num��0��ʼ���ۼ�16��
	{
		detec_start = 0;
		for (i = 0; i < SLAVE_SIZE / 2; i++) //��16����ֵ
		{
			mean_data[i] = (mean_data[i] * (detect_num - 1) + processdata_t[i][Bscan_num]) * mean_numadd;
		}
	}
	else
	{
		pre_num = 2 /*13*/; ////////////////////////////////////////////////////////////////////////
		slow_num++;
		detect_num = BACK_SIZE + 1; //detect_num����17�󶼸�ֵ��17
		/*		if (slow_num>=4)
		{
		detec_start=1;
		slow_num=0;
		}*/
		detec_start = 1;
		for (i = 0; i < SLAVE_SIZE / 2; i++)
		{
			mean_data[i] = mean_data[i] + (processdata_t[i][Bscan_num] - move_data[i]) / BACK_SIZE;
			/*if(i==249)
			{
			i=250;
			}*/
		}
	}
	Bscan_num++;
	if (Bscan_num >= BACK_SIZE)
	{
		Bscan_num = Bscan_num % BACK_SIZE; //ȡ�࣬��16��Ϊ0
	}
	if (detec_start == 1 && slow_num >= 4) /////////////////////////////////////////////////////////////////��
	{
		body_detect(result_data);
		slow_num = 0;
	}
}

void body_detect(short result_data[])
{
	//���¶�Ӧ���ǵ�5��������Ҫ���ڼ��㷽��
	int pflag = 0; //�Ƿ���������˶�Ŀ���־λ
	int row0 = 0, row1 = 0;
	float mean_v, std_v;
	float std_p = 0.;
	int i = 0, j = 0;

	for (i = 0; i < SLAVE_SIZE / 2; i++)
	{
		std_p = 0.;
		for (j = 0; j < BACK_SIZE; j++)
			std_p = std_p + (processdata_t[i][j] - mean_data[i]) * (processdata_t[i][j] - mean_data[i]);
		var_processdata[i] = std_p / (float)BACK_SIZE; //������ķ������ݱ��浽������
													   //fprintf(fp2,"%f ",var_processdata[i]);
	}
	//fprintf(fp2,"\n");
	//��ȷ����ϻ����ж�s1����Ϊrow0��row1
	for (i = 2; i < SLAVE_SIZE / 2 - 1; i++)
	{
		pflag = 0;
		row0 = i - ceil(BACK_SIZE * 0.5);
		row1 = i + floor(BACK_SIZE * 0.5);
		if (row0 < 0)
			row0 = 0;
		if (row1 > SLAVE_SIZE / 2)
			row1 = SLAVE_SIZE / 2;
		if ((var_processdata[i] - var_processdata[i - 1] > 0) & (var_processdata[i] - var_processdata[i + 1] > 0) & (var_processdata[i] > TH1))
		{
			pflag = 1;
			pos_val = i;
			break;
		}
	}
	if (pflag == 1) //�ų����ܴ��ڵ��龯�ź�
	{
		mean_v = 0.;
		std_v = 0.;
		for (j = row0; j < row1; j++)
			mean_v = mean_v + var_processdata[j];
		mean_v = mean_v / (float)(row1 - row0 + 1);
		for (j = row0; j < row1; j++)
			std_v = std_v + (var_processdata[j] - mean_v) * (var_processdata[j] - mean_v);
		std_v = std_v / (float)(row1 - row0 + 1);
		if (std_v < TH2)
			pflag = 0;
		else
			bodymove = (signalpos + 27.0 * (pos_val + 1) / 256) * 15.0; //���������Ϣ
	}
	result_data[0] = 0xDDAA;
	result_data[1] = 4;
	result_data[2] = pflag;
	result_data[3] = (short)bodymove; //
	//result_data[4]=0;
	//result_data[5]=0;  //�ϴ������ź��б���
	bodymove_flag = 1; //1��ʾ�������
}

//points  ������x����Ч�˲�����
//xΪ���������飬������
//flag  :��־=1
//�˲�����ʽ��a(0)*y(n) = b(0)*x(n) + b(1)*x(n-1) + ... + b(nb)*x(n-nb)- a(1)*y(n-1) - ... - a(na)*y(n-na)

void BandPass(int points, float *x, short *xout)
{
	int i, j;
	double Fdata1;
	//float MidVar;
	float *MidVar = (float *)calloc(points, sizeof(float));
	MidVar[0] = x[0];
	MidVar[1] = x[1];
	MidVar[2] = x[2];
	MidVar[3] = FilterB[0] * x[3];
	MidVar[4] = FilterB[0] * x[4] + FilterB[1] * x[3] - FilterA[1] * MidVar[3];
	MidVar[5] = FilterB[0] * x[5] + FilterB[1] * x[4] + FilterB[2] * x[3] - FilterA[1] * MidVar[4] - FilterA[2] * MidVar[3];
	MidVar[6] = FilterB[0] * x[6] + FilterB[1] * x[5] + FilterB[2] * x[4] + FilterB[1] * x[3] - FilterA[1] * MidVar[5] - FilterA[2] * MidVar[4] - FilterA[3] * MidVar[3];
	MidVar[7] = FilterB[0] * x[7] + FilterB[1] * x[6] + FilterB[2] * x[5] + FilterB[1] * x[4] + FilterB[0] * x[3] - FilterA[1] * MidVar[6] - FilterA[2] * MidVar[5] - FilterA[3] * MidVar[4] - FilterA[4] * MidVar[3];
	for (j = 0; j < 8; j++)
	{
		Fdata1 = MidVar[j] /**multidata1[j]*/;
		if (Fdata1 >= 29000)
			xout[j] = 29000;
		else if (Fdata1 <= -29000)
			xout[j] = -29000;
		else
			xout[j] = (short)(Fdata1);
	}
	for (i = 8; i < points; i++)
	{
		MidVar[i] = FilterB[0] * x[i] + FilterB[1] * x[i - 1] + FilterB[2] * x[i - 2] + FilterB[1] * x[i - 3] + FilterB[0] * x[i - 4] - FilterA[1] * MidVar[i - 1] - FilterA[2] * MidVar[i - 2] - FilterA[3] * MidVar[i - 3] - FilterA[4] * MidVar[i - 4];
		Fdata1 = MidVar[i] /**multidata1[i]*/;
		if (Fdata1 >= 29000)
			xout[i] = 29000;
		else if (Fdata1 <= -29000)
			xout[i] = -29000;
		else
			xout[i] = (short)(Fdata1);
	}
	// }
	free(MidVar);
}
void init(/*short result_data[]*/)
{
	unsigned int i, j = 0;
	detect_flag = 0;
	fscan = 32;
	dao_pre_row = 1.0 / pre_row;
	dao_pre_row1 = 1.0 / (pre_row - 1);
	ftnum = 512; //% ����Ҷ�任�ĵ���,��ò�С��512
	ad_row = 0;
	ad_row_full = 0;
	process_end_flag = 0;
	reprocess_end_flag = 0;
	receive_data_flag = 0;
	process_flag = 0;
	pos_val = 0;

	flag = 0;
	fflag = 0;
	frame_num = 0;
//	for (i = 0; i < 20; i++) //�˴��Ķ�
//	{
//		result_data[i] = 0; //�˴��Ķ�
//							//master_command[i]=0;  //�˴��Ķ�
//	}

	//unsigned int i=0,j=0;
	//unsigned short l=0;
	//msignal_flag=0;
	zscore_flag = 0;
	Bscan_num = 0;
	detect_num = 0;
	detec_start = 0;
	pos_val = 0;
	pre_num = 2;
	slow_num = 0;
	// for (i = 0; i < AD_ELEMENT_SIZE; i++)
	// 	ad_frame_data1[i] = 0;
	background_num1 = 0;
	for (i = 0; i < SLAVE_SIZE; i++)
	{
		ad_data_add[i] = 0.;
		back_mov_data[i] = 0.;
		send_ad_data[i] = 0.;
		pro_ad_data[i] = 0.;
	}
	for (i = 0; i < SLAVE_SIZE / 2; i++)
	{
		move_data[i] = 0.;
		mean_data[i] = 0.;
		var_processdata[i] = 0.;
		for (j = 0; j < BACK_SIZE; j++)
		{
			processdata_t[i][j] = 0.;
		}
	}
	FLcut = 0.001 * (antenna_type)*window / (4 * AD_ELEMENT_SIZE); // 1/4 Freantenna
	FRcut = 0.001 * 2 * (antenna_type)*window / AD_ELEMENT_SIZE;   // 2 Freantenna
	if (FLcut > 0.5)
		FLcut = 0.495;
	if (FRcut > 0.5)
		FRcut = 0.495;
	iirbcf(3, 3, 1, 4, 0, FLcut, FRcut, 0, 40, FilterB, FilterA);
}
