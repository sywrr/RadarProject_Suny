// bodydetect.cpp : �������̨Ӧ�ó������ڵ㡣
//

//#include "stdafx.h"
#include <stdio.h>
#include <string.h>
#include <math.h>
#include <time.h>
//#include "Global.h"
#include "filter.c"
#include "iirfilter.c"

//#include "afxdialogex.h"

/////////////���峣���ͺ���///////////////////
//typedef struct
//{
//	float re;
//	float im;
//}COMPLEX;                     //����һ�������ṹ

#define N_rowpoint 4 //����ѹ������ѡ��
#define WIN 24       //�˻�������С
//#define  ROW             640
//#define  pre_row         128      //�����ź�Ԥ�������ݵ�����
//#define  STORE_ROW       768      //�����źŴ�����������ݵ���
#define PI 3.1415926536
#define BACK_SIZE 16
#define TH1 3.1
#define TH2 0.68

int AD_ELEMENT_SIZE = 8192;    //512
int SLAVE_SIZE = 2048; /*512*/ //AD_ELEMENT_SIZE/N_rowpoint
int HARF_AD_SIZE = 1024;       /*256*/
int REP_LEN = 2000;            //STATIC_LEN-WIN-1
int ROW = 640;
int pre_row = 128;   //�����ź�Ԥ�������ݵ�����
int STORE_ROW = 768; //�����źŴ�����������ݵ���

void changeParams(unsigned _fscan, short _antenna_type, unsigned _window, unsigned _ad_element_size);
void detect_pre(short ad_frame_data1[]);
void detect_body(short result_data1[]);
void detect_breath(short result_data1[], float breath_th);
extern void FFT(COMPLEX *xin, int N); //��ʽ�任������
extern void self_filter(short track_len, short filter_order, float x[], float y[]);
void pre_breath(void);                //�����ź�Ԥ�����������ݴ��
void breath_process(float breath_th); //�����źź�����
extern void iirbcf(int ifilt, int band, int ns, int n, double f1, double f2, double f3, double f4, double db, double b[], double a[]);
void pre_process(void); //�����ֲ��ź�
void z_ave_process(void);
void zscore(void);
void body_detect(void);
void BandPass(int points, float *x, short *xout); //��ͨ�˲�;
void init(void);

COMPLEX sdata[640];
short result_data[20];               //�õ��Ĵ�����
float accumdata1[2048];              //ÿ�����ݼӴ�����
float processdata[768][2048] = {0.}; //�����źź�����������
//unsigned short master_command[20];
// short compressdata[2048 - 1] = {0.}; //��ѹ��������host_dsp���͹���
// �˴��Ķ�������Ϊfloat
float compressdata[2048 - 1] = {0.}; //��ѹ��������host_dsp���͹���
float MMax[2048] = {0.};
float enselect[640] = {0.}; //��Ѳ���
float outdata1[640] = {0.}; //��Ѳ����˲���������?
float Energy_breath[512 / 2] = {0.};
short ad_frame_data1[8192]; //�˴����ԭʼ����
float compdata;

short ad_pingdata[8192];
//short ad_frame_data1[8192];//�˴����ԭʼ����
//short compressdata[AD_ELEMENT_SIZE/N_rowpoint-1]={0.};
float compdata;

short preprocess_data1[8192];           //�˴����Ԥ������
float back_mov_data[8192 / N_rowpoint]; //����������ز�
float ad_data_add[8192 / N_rowpoint];   //16λAD�����������
float processdata_t[1024][BACK_SIZE];

short send_ad_data[8192 / N_rowpoint]; //��ARM���͵�����
float move_data[1024];
float mean_data[1024];
float var_processdata[1024];
//short bodymove_result[20];
short pro_ad_data[2048]; //

volatile unsigned int ftnum = 512; //��ʽ�仯��������Ҫ����ɨ����ȷ��
volatile unsigned int frame_num;
volatile unsigned short reprocess_end_flag; //Ԥ�д�����ɱ�־
volatile unsigned short process_end_flag;   //������ɱ�־
volatile unsigned short receive_data_flag = 0;
volatile unsigned int command_flag = 0;
volatile unsigned int work_flag = 0;
volatile unsigned int ad_row = 0; //ad convert 's ����
volatile unsigned int fscan = 16; //ɨ��
volatile unsigned short signal_position = 0;
//volatile unsigned short time_windows=20;
volatile unsigned short detect_flag = 0;
volatile unsigned int flag = 0; //�����ݼ���
volatile unsigned int fflag = 0;
volatile float breathmove = 0.;            //�����źž�����Ϣ
volatile unsigned int breathflag = 0;      //�����źű�־λ
volatile unsigned int pre_breath_flag = 0; //�����ź�Ԥ�н������
volatile unsigned int epsilon = 1;         //��糣��Ĭ��Ϊ1
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
int window = /*66*/ 80; //ʱ��
int pre_num = 2;
int k;
int slow_num;
int slow_num_flag = 4;
//clock_t  clockBegin, clockEnd,clockEnd1;

// FILE *fp;
//FILE *fp1;
// FILE *fp2;
//FILE *fp3;
//clock_t  clockBegin, clockEnd,clockEnd1;

void changeParams(unsigned _fscan, short _antenna_type, unsigned _window, unsigned _ad_element_size) {
    fscan = _fscan;
    antenna_type = _antenna_type;
    window = _window;
    AD_ELEMENT_SIZE = _ad_element_size;
    SLAVE_SIZE = AD_ELEMENT_SIZE / N_rowpoint;
    HARF_AD_SIZE = SLAVE_SIZE / 2;
    REP_LEN = SLAVE_SIZE - WIN - 1;
}

void detect_pre(short ad_frame_data1[]) {
    int i, j /*,k*/;
    //int last;

    for (i = 0; i < 6; i++) {
        result_data[i] = 0;
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
void detect_body(short result_data1[]) {
    int i, j /*,k*/;
    //int last;

    pre_process();
    z_ave_process();

    for (i = 0; i < 6; i++) {
        result_data1[i] = result_data[i];
    }
}
void detect_breath(short result_data1[], float breath_th) {
    int i, j /*,k*/;
    //int last;

    // for (i = 0; i < REP_LEN; i++) {
    //     processdata[ad_row][i] = /*accumdata1*/ compressdata[i];
    // }
    // �˴��Ķ���memcpy
    memcpy(processdata[ad_row], compressdata, REP_LEN * sizeof(float));
    //processdata[ad_row][0]=m;/////////////////////////////////////����
    frame_num = ad_row % pre_row;
    if (frame_num == (pre_row - 1)) process_flag = 1;
    ad_row++;
    if (ad_row >= ROW)
        ad_row_full = 1;
    //		else
    //			ad_row_full=ad_row_full;
    if (ad_row == STORE_ROW)
        ad_row = 0;
    receive_data_flag = 1;
    //////////////////////////////////////////////////////
    if (process_flag == 1) {
        process_flag = 0;
        flag++;
        pre_breath();
        if (flag == 6) flag = 0;
        if (ad_row_full == 1) {
            if (flag == 0)
                fflag = 6;
            else
                fflag = flag;
            //clockBegin = clock();
            breath_process(breath_th);
            //clockEnd = clock();
            //printf("		%d\n", clockEnd - clockBegin);
        }
    }
    for (i = 0; i < 6; i++) {
        result_data1[i] = result_data[i];
    }
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
    for (i = 0; i < REP_LEN; i++) {
        Mean_processdata = 0.;
        std_processdata = 0.;
        for (j = 0; j < pre_row; j++)
            Mean_processdata += processdata[j + flagnum][i];
        Mean_processdata = Mean_processdata * dao_pre_row;

        for (j = 0; j < pre_row; j++)
            std_processdata += (processdata[j + flagnum][i] - Mean_processdata) * (processdata[j + flagnum][i] - Mean_processdata);
        std_processdata = sqrt(std_processdata * dao_pre_row1);
        if (std_processdata == 0.0) std_processdata = 1.;
        dao_std_data = 1.0 / std_processdata;
        for (j = 0; j < pre_row; j++) {
            processdata[j + flagnum][i] = (processdata[j + flagnum][i] - Mean_processdata) * dao_std_data;
        }
    }
}
void breath_process(float breath_th) //�����źź������
{
    unsigned int i = 0, j = 0, k = 0, l = 0, f_position = 0, N_breath = 0;
    float MMean = 0.;
    float signalpower = 0.;
    float totalpower = 0.;
    float Max[20] = {0.};
    //float MMax[STATIC_LEN/N_rowpoint-1-WIN]={0.};
    int position = 0, MMMaxposition = 0, MMaxposition = 0;
    //int fs=16;//ɨ��
    int N_columncompress = 1;                                    //����ѹ��
    int starp = ceil((0.22 / fscan * N_columncompress) * ftnum); //Ƶ��������ʼ��
    int endp = floor((0.44 / fscan * N_columncompress) * ftnum); //Ƶ��������ֹ��

    //clockBegin = clock();
    for (i = 0; i < REP_LEN /*STATIC_LEN/N_rowpoint-1-WIN*/; i++) {
        //clockBegin = clock();

        //for(l=0;l<ROW;l++)
        //{switch(fflag)
        //{
        //case 1:
        //	{if(l>pre_row-1)
        //	{sdata[l].re = processdata[l-pre_row][i]; sdata[l].im =0.0;}

        //	else
        //	{sdata[l].re = processdata[l+5*pre_row][i]; sdata[l].im =0.0;}

        //	break;
        //	}
        //case 2:
        //	{if(l>2*pre_row-1)
        //	{sdata[l].re = processdata[l-2*pre_row][i]; sdata[l].im =0.0;}

        //	else
        //	{sdata[l].re = processdata[l+4*pre_row][i]; sdata[l].im =0.0;}

        //	break;  }
        //case 3:
        //	{if(l>3*pre_row-1)
        //	{sdata[l].re = processdata[l-3*pre_row][i]; sdata[l].im =0.0;}

        //	else
        //	{sdata[l].re = processdata[l+3*pre_row][i]; sdata[l].im =0.0;}

        //	break;  }

        //case 4:
        //	{if(l>4*pre_row-1)
        //	{sdata[l].re = processdata[l-4*pre_row][i]; sdata[l].im =0.0;}

        //	else
        //	{sdata[l].re = processdata[l+2*pre_row][i]; sdata[l].im =0.0;}

        //	break; }

        //case 5:
        //	{
        //		sdata[l].re = processdata[l][i];
        //		sdata[l].im =0.0;
        //		break;
        //	}
        //case 6:
        //	{
        //		sdata[l].re = processdata[l+pre_row][i];
        //		sdata[l].im =0.0;
        //		break;
        //	}
        //default: break;

        //}
        //}

        switch (fflag) {
        case 1: {
            for (l = 0; l < 4 * pre_row; l++) {
                sdata[l].re = processdata[l + 2 * pre_row][i];
                sdata[l].im = 0.0;
            }
            for (l = 4 * pre_row; l < ROW; l++) {
                sdata[l].re = processdata[l - 4 * pre_row][i];
                sdata[l].im = 0.0;
            }
            break;
        }
        case 2: {
            for (l = 0; l < 3 * pre_row; l++) {
                sdata[l].re = processdata[l + 3 * pre_row][i];
                sdata[l].im = 0.0;
            }
            for (l = 3 * pre_row; l < ROW; l++) {
                sdata[l].re = processdata[l - 3 * pre_row][i];
                sdata[l].im = 0.0;
            }
            break;
        }
        case 3: {
            for (l = 0; l < 2 * pre_row; l++) {
                sdata[l].re = processdata[l + 4 * pre_row][i];
                sdata[l].im = 0.0;
            }
            for (l = 2 * pre_row; l < ROW; l++) {
                sdata[l].re = processdata[l - 2 * pre_row][i];
                sdata[l].im = 0.0;
            }
            break;
        }
        case 4: {
            for (l = 0; l < pre_row; l++) {
                sdata[l].re = processdata[l + 5 * pre_row][i];
                sdata[l].im = 0.0;
            }
            for (l = pre_row; l < ROW; l++) {
                sdata[l].re = processdata[l - pre_row][i];
                sdata[l].im = 0.0;
            }

            break;
        }
        case 5: {
            for (l = 0; l < ROW; l++) {
                sdata[l].re = processdata[l][i];
                sdata[l].im = 0.0;
            }
            //clockEnd1 = clock();
            //printf("	%d\n", clockEnd1 - clockBegin);
            break;
        }
        case 6: {
            for (l = 0; l < ROW; l++) {
                sdata[l].re = processdata[l + pre_row][i];
                sdata[l].im = 0.0;
            }
            break;
        }
        default:
            break;
        }

        FFT(sdata, ftnum);
        for (k = starp - 1; k < endp; k++) {
            Max[k] = sqrt((float)(sdata[k].re * sdata[k].re) + (float)(sdata[k].im * sdata[k].im));
            if (Max[k] > Max[MMaxposition]) MMaxposition = k;
        }
        MMax[i] = Max[MMaxposition];
        if (MMax[i] > MMax[MMMaxposition]) MMMaxposition = i;
        MMean = MMean + MMax[i];

        //clockEnd1 = clock();
        //printf("	%d\n", clockEnd1 - clockBegin);
    }
    //clockEnd = clock();
    //printf("	%d", clockEnd - clockBegin);
    MMean = MMean / (REP_LEN);
    for (j = 0; j < ROW; j++) {
        switch (flag) {
        //case 1:
        //	{if(j>pre_row-1)
        //	{enselect[j] = processdata[j-pre_row][MMMaxposition];}

        //	else
        //	{enselect[j] = processdata[j+5*pre_row][MMMaxposition];}
        //	break;
        //	}

        //case 2:
        //	{if(j>2*pre_row-1)
        //	{enselect[j]= processdata[j-2*pre_row][MMMaxposition];}

        //	else
        //	{enselect[j] = processdata[j+4*pre_row][MMMaxposition];}
        //	break;
        //	}
        //case 3:

        //	{if(j>3*pre_row-1)
        //	{enselect[j]= processdata[j-3*pre_row][MMMaxposition];}

        //	else
        //	{enselect[j] = processdata[j+3*pre_row][MMMaxposition];}
        //	break;
        //	}

        //case 4:
        //	{if(j>4*pre_row-1)
        //	{enselect[j] = processdata[j-4*pre_row][MMMaxposition]; }

        //	else
        //	{enselect[j] = processdata[j+2*pre_row][MMMaxposition]; }
        //	break;
        //	}
        //case 5:
        //	{
        //		enselect[j] = processdata[j][MMMaxposition];
        //		break;
        //	}
        //case 6:
        //	{
        //		enselect[j] = processdata[j+pre_row][MMMaxposition];
        //		break;
        //	}
        case 1: {
            if (j > 4 * pre_row - 1) {
                enselect[j] = processdata[j - 4 * pre_row][MMMaxposition];
            }

            else {
                enselect[j] = processdata[j + 2 * pre_row][MMMaxposition];
            }
            break;
        }

        case 2:

        {
            if (j > 3 * pre_row - 1) {
                enselect[j] = processdata[j - 3 * pre_row][MMMaxposition];
            }

            else {
                enselect[j] = processdata[j + 3 * pre_row][MMMaxposition];
            }
            break;
        }

        case 3: {
            if (j > 2 * pre_row - 1) {
                enselect[j] = processdata[j - 2 * pre_row][MMMaxposition];
            }

            else {
                enselect[j] = processdata[j + 4 * pre_row][MMMaxposition];
            }
            break;
        }

        case 4:

        {
            if (j > pre_row - 1) {
                enselect[j] = processdata[j - pre_row][MMMaxposition];
            }

            else {
                enselect[j] = processdata[j + 5 * pre_row][MMMaxposition];
            }
            break;
        }

        case 5: {
            enselect[j] = processdata[j][MMMaxposition];
            break;
        }

        case 6: {
            enselect[j] = processdata[j + pre_row][MMMaxposition];
            break;
        }
        default:
            break;
        }
    }

    self_filter(ROW, 2 * fscan, enselect, outdata1);
    for (l = 0; l < ROW - 2 * fscan; l++) {
        sdata[l].re = outdata1[l + 2 * fscan];
        sdata[l].im = 0.0;
    }
    //for(l=0;l<ROW;l++)
    //{
    //	sdata[l].re =enselect[l];
    //	sdata[l].im =0.0;
    //}
    FFT(sdata, ftnum);
    for (k = starp - 1; k < ftnum / 2; k++) {
        if (k >= starp - 1 && k < endp)
            signalpower = signalpower + sqrt((float)(sdata[k].re * sdata[k].re) + (float)(sdata[k].im * sdata[k].im));
        totalpower = totalpower + sqrt((float)(sdata[k].re * sdata[k].re) + (float)(sdata[k].im * sdata[k].im));
        Energy_breath[k] = sqrt((float)(sdata[k].re * sdata[k].re) + (float)(sdata[k].im * sdata[k].im));
        if (Energy_breath[k] > Energy_breath[f_position]) { f_position = k; }
    }
    totalpower = totalpower - signalpower; //ʵ����ָ��������������ֵ

    if ((signalpower / totalpower) > /*0.683*/ /*0.3*/ /*1*/ breath_th && MMax[MMMaxposition] / MMean >= /*0.8*/ 1.5) {
        breathflag = 1;
        breathmove = (signal_position + window * (MMMaxposition + 1) / 496.0) * 15.0 / sqrt((float)(epsilon));
        N_breath = ceil((f_position + 1) / (float)(ftnum)*60.0 * (float)(fscan));
        pos_val = MMMaxposition;
    }

    else {
        breathflag = 0;
        breathmove = 0;
    }

    result_data[0] = /*0xDDAA*/ 0xDDBB;
    result_data[1] = 4;
    //result_data[2]=0;
    //result_data[3]=0;
    result_data[4] = breathflag;
    result_data[5] = (short)/*(breathmove)*/ MMMaxposition * 4; //�ϴ������ź��б���
    process_end_flag = 1;                                       //1��ʾ�������
}
/*�������*/
void pre_process(void) {
    unsigned int i = 0;
    volatile float ave_numadd = 1.;
    volatile float back_numadd = 1.;

    background_num1++;
    back_numadd = 1 / (float)(background_num1);
    for (i = 0; i < SLAVE_SIZE; i++) {
        back_mov_data[i] = compressdata[i] - ad_data_add[i];
        if (background_num1 <= BACK_SIZE) //background_row1)
            ad_data_add[i] = (ad_data_add[i] * (background_num1 - 1) + compressdata[i]) * back_numadd;
        else {
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
void z_ave_process(void) //�������ݽ���ƽ��Ȼ���ٽ��д���
{
    int i = 0;
    if (ad_row_t < pre_num) {
        for (i = 0; i < SLAVE_SIZE; i++)
            //pro_ad_data[i]=send_ad_data[i];
            pro_ad_data[i] = (pro_ad_data[i] * ad_row_t + send_ad_data[i]) / (ad_row_t + 1);
        ad_row_t++;
        zscore_flag = 0;
        return;
    } else {
        for (i = 0; i < SLAVE_SIZE; i++) {
            pro_ad_data[i] = (short)((pro_ad_data[i] + send_ad_data[i]) * 0.5);
            //pro_ad_data[i]=(pro_ad_data[i]*ad_row_t+send_ad_data[i])/(ad_row_t+1);
            //fprintf(fp1,"%d ",pro_ad_data[i]);
        }
        //fprintf(fp1,"\n");
        ad_row_t = 0;
        zscore_flag = 1;
        zscore();
    }
}

void zscore(void) //int nsamp,short send_ad_data[], float processdata[]
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
    if (std1 == 0) {
        std1 = 1;
    }
    std2 = sqrt(std1 / (float)(SLAVE_SIZE));
    for (i = 0; i < SLAVE_SIZE / 2; i++) {
        move_data[i] = processdata_t[i][Bscan_num];
        processdata_t[i][Bscan_num] = (pro_ad_data[2 * i] - meancol) / std2; //��������������Ҫ�ŵ������в���������
                                                                             //processdata_t[i][Bscan_num]=pro_ad_data[2*i];
                                                                             //fprintf(fp,"%f ",processdata_t[i][Bscan_num]);
                                                                             //fflush(fp);
    }
    //fprintf(fp,"\n");
    detect_num++;
    mean_numadd = 1 / (float)detect_num;
    if (detect_num <= BACK_SIZE) //detect_num��1��ʼ��Bscan_num��0��ʼ���ۼ�16��
    {
        detec_start = 0;
        for (i = 0; i < SLAVE_SIZE / 2; i++) //��16����ֵ
        {
            mean_data[i] = (mean_data[i] * (detect_num - 1) + processdata_t[i][Bscan_num]) * mean_numadd;
        }
    } else {
        pre_num = 2 /*13*/; ////////////////////////////////////////////////////////////////////////
        slow_num++;
        detect_num = BACK_SIZE + 1; //detect_num����17�󶼸�ֵ��17
        /*		if (slow_num>=4)
		{
		detec_start=1;
		slow_num=0;
		}*/
        detec_start = 1;
        for (i = 0; i < SLAVE_SIZE / 2; i++) {
            mean_data[i] = mean_data[i] + (processdata_t[i][Bscan_num] - move_data[i]) / BACK_SIZE;
            /*if(i==249)
			{
			i=250;
			}*/
            //fprintf(fp,"%f ",mean_data[i]);
        }
        //fprintf(fp,"\n");
        //fflush(fp);
    }
    Bscan_num++;
    if (Bscan_num >= BACK_SIZE) {
        Bscan_num = Bscan_num % BACK_SIZE; //ȡ�࣬��16��Ϊ0
    }
    if (detec_start == 1 && slow_num >= slow_num_flag) /////////////////////////////////////////////////////////////////��
    {
        body_detect();
        slow_num = 0;
    }
}

void body_detect(void) {
    //���¶�Ӧ���ǵ�5��������Ҫ���ڼ��㷽��
    int pflag = 0; //�Ƿ���������˶�Ŀ���־λ
    int row0 = 0, row1 = 0;
    float mean_v, std_v;
    float std_p = 0.;
    int i = 0, j = 0;
    bodymove = 0;

    for (i = 0; i < SLAVE_SIZE / 2; i++) {
        std_p = 0.;
        for (j = 0; j < BACK_SIZE; j++)
            std_p = std_p + (processdata_t[i][j] - mean_data[i]) * (processdata_t[i][j] - mean_data[i]);
        var_processdata[i] = std_p / (float)BACK_SIZE; //������ķ������ݱ��浽������
        // fprintf(fp2, "%f ", var_processdata[i]);
    }
    // fprintf(fp2, "\n");
    //��ȷ����ϻ����ж�s1����Ϊrow0��row1
    for (i = /*2*/ 10; i < SLAVE_SIZE / 2 - 1; i++) {
        pflag = 0;
        row0 = i - ceil(BACK_SIZE * 0.5);
        row1 = i + floor(BACK_SIZE * 0.5);
        if (row0 < 0) row0 = 0;
        if (row1 > SLAVE_SIZE / 2) row1 = SLAVE_SIZE / 2;
        if ((var_processdata[i] - var_processdata[i - 1] > 0) & (var_processdata[i] - var_processdata[i + 1] > 0) & (var_processdata[i] > TH1)) {
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
            //bodymove=(signalpos+window*(pos_val+1)/(SLAVE_SIZE/2))*15.0;                    //���������Ϣ
            bodymove = pos_val * 8;
    }
    if (result_data[0] != 0xDDBB) {
        result_data[0] = 0xDDAA;
    }
    //result_data[0]=0xDDAA;
    result_data[1] = 4;
    result_data[2] = pflag;
    result_data[3] = (short)bodymove /*pos_val*8*/; //
    //result_data[4]=0;
    //result_data[5]=0;  //�ϴ������ź��б���
    bodymove_flag = 1; //1��ʾ�������
}

//points  ������x����Ч�˲�����
//xΪ���������飬������
//flag  :��־=1
//�˲�����ʽ��a(0)*y(n) = b(0)*x(n) + b(1)*x(n-1) + ... + b(nb)*x(n-nb)- a(1)*y(n-1) - ... - a(na)*y(n-na)

void BandPass(int points, float *x, short *xout) {
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
    for (j = 0; j < 8; j++) {
        Fdata1 = MidVar[j] /**multidata1[j]*/;
        if (Fdata1 >= 29000)
            xout[j] = 29000;
        else if (Fdata1 <= -29000)
            xout[j] = -29000;
        else
            xout[j] = (short)(Fdata1);
    }
    for (i = 8; i < points; i++) {
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
void init(void) {
    unsigned int i, j = 0;
    detect_flag = 0;
    //	fscan=16;
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
    for (i = 0; i < 20; i++) //�˴��Ķ�
    {
        result_data[i] = 0; //�˴��Ķ�
                            //master_command[i]=0;  //�˴��Ķ�
    }

    //unsigned int i=0,j=0;
    //unsigned short l=0;
    //msignal_flag=0;
    zscore_flag = 0;
    Bscan_num = 0;
    detect_num = 0;
    detec_start = 0;
    pos_val = 0;
    //pre_num=2;
    slow_num = 0;
    if (fscan >= 64) {
        pre_num = 2;
        slow_num_flag = 4;
        ROW = 640;
        pre_row = 128;   //�����ź�Ԥ�������ݵ�����
        STORE_ROW = 768; //�����źŴ�����������ݵ���
    } else if (fscan == 32) {
        pre_num = 0;
        slow_num_flag = 2;
        ROW = 320;
        pre_row = 64;    //�����ź�Ԥ�������ݵ�����
        STORE_ROW = 384; //�����źŴ�����������ݵ���
    } else if (fscan == 16) {
        pre_num = 0;
        slow_num_flag = 2;
        //ROW=320;
        //pre_row=64;      //�����ź�Ԥ�������ݵ�����
        //STORE_ROW=384;      //�����źŴ�����������ݵ���
        ROW = 640;
        pre_row = 128;   //�����ź�Ԥ�������ݵ�����
        STORE_ROW = 768; //�����źŴ�����������ݵ���
    }
    for (i = 0; i < AD_ELEMENT_SIZE; i++)
        ad_frame_data1[i] = 0;
    background_num1 = 0;
    for (i = 0; i < SLAVE_SIZE; i++) {
        ad_data_add[i] = 0.;
        back_mov_data[i] = 0.;
        send_ad_data[i] = 0.;
        pro_ad_data[i] = 0.;
    }
    for (i = 0; i < SLAVE_SIZE / 2; i++) {
        move_data[i] = 0.;
        mean_data[i] = 0.;
        var_processdata[i] = 0.;
        for (j = 0; j < BACK_SIZE; j++) {
            processdata_t[i][j] = 0.;
        }
    }
    FLcut = 0.001 * (antenna_type)*window / (4 * AD_ELEMENT_SIZE); // 1/4 Freantenna
    FRcut = 0.001 * 2 * (antenna_type)*window / AD_ELEMENT_SIZE;   // 2 Freantenna
    if (FLcut > 0.5) FLcut = 0.495;
    if (FRcut > 0.5) FRcut = 0.495;
    iirbcf(3, 3, 1, 4, 0, FLcut, FRcut, 0, 40, FilterB, FilterA);
}
// int main() {
//     FILE *fw;
//     int last, i, m;
//     short result_data1[20];
//     float breath_th = 1;
//     changeParams(16, 400, 80, 8192); //���ݲ���
//     //changeParams(64,400,80,512);//���ݲ���

//     fp = fopen("result.txt", "w");
//     fp2 = fopen("var_processdata.txt", "w");
//     fw = fopen("ltefile16.lte", "rb");
//     //fw=fopen("E:\\����\\�Ѿ�\\ʵ���Һ���\\LteFile-5_resample.lte","rb");
//     //fw=fopen("E:\\����\\�Ѿ�\\������\\20200727\\ltefile1.lte","rb");
//     fseek(fw, 0L, SEEK_END);
//     last = ftell(fw);
//     fseek(fw, 1024L, SEEK_SET);
//     init();
//     clock_t st, et;
//     for (m = 0; m < (last - 1024) / AD_ELEMENT_SIZE / sizeof(short); m++) {
//         fread(ad_frame_data1, sizeof(short), AD_ELEMENT_SIZE, fw);
//         //detect(ad_frame_data1,result_data1,breath_th);
//         st = clock();
//         detect_pre(ad_frame_data1);
//         detect_body(result_data1);
//         detect_breath(result_data1, breath_th);
//         et = clock();
//         if (et - st > 1)
//         printf("total detect cost time: %ld\n", et - st);
//         // for (i = 0; i < 6; i++) {
//         //     printf("%d ", result_data1[i]);
//         //     fprintf(fp, "%d ", result_data1[i]);
//         // }
//         // printf("%d ", m);
//         // printf("\n");
//         fprintf(fp, "%d ", m);
//         fprintf(fp, "\n");
//     }
//     fclose(fp);
//     fclose(fp2);
//     fclose(fw);
//     return 0;
// }
