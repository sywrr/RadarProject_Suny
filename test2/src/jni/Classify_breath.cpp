#include "Classify_breath.h"
#include <stdio.h>
#include <string.h>
#include <math.h>
//#include "filter.c"
//#include <time.h>

Classify_breath::Classify_breath(void)
{
	multi_flag=0;
	AD_ELEMENT_SIZE = 8192;    //512
	SLAVE_SIZE = 2048; /*512*/ //AD_ELEMENT_SIZE/N_rowpoint
	HARF_AD_SIZE = 1024;       /*256*/
	REP_LEN = 2000;            //STATIC_LEN-WIN-1
	ROW = 640;
	pre_row = 128;   //呼吸信号预处理数据道数，
	STORE_ROW = 768; //呼吸信号处理所需的数据道数

	ftnum = 512; //傅式变化点数，需要根据扫速来确定
	receive_data_flag = 0;
	command_flag = 0;
	work_flag = 0;
	ad_row = 0; //ad convert 's 列数
	fscan = 16; //扫速
	signal_position = 0;
	detect_flag = 0;
	flag = 0; //包数据计算
	fflag = 0;
	breathmove = 0.;            //呼吸信号距离信息
	breathflag = 0;      //呼吸信号标志位
	pre_breath_flag = 0; //呼吸信号预判结果计数
	epsilon = 1;         //介电常数默认为1
	ad_row_full = 0;
	process_flag = 0;
	pos_val = 0;

	ad_frame_flag = 0;
	detect_flag_cun = 0;
	bodymove_flag = 0;
	signalpos = 0; //信号位置需要从上位机中获取
	bodyflag = 0;  //体动有无标示
	bodymove = 0.;
	ad_row_t = 0;
	background_num1 = 0; //去背景道数
	zscore_flag = 0;
	Bscan_num = 0;
	detect_num = 0;
	detec_start = 0;
	antenna_type = /*900*/ 400;
	window = /*66*/ 80; //时窗
	pre_num = 2;
	slow_num_flag = 4;

	sdata=new COMPLEX[640];
	//short result_data_body[20];          //得到的处理结果
	result_data_breath=new short[20];        //得到的处理结果
	//float accumdata1[2048];              //每道数据加窗后结果
	//float processdata[768][2048]/* = {0.}*/; //呼吸信号后处理数组数据
	processdata=new float*[768];
	for(int i=0;i<768;i++)
	{
		processdata[i]=new float[2048];
	}
	// 此处改动，定义为float
	compressdata=new float[2048 - 1]/* = {0.}*/; //此压缩数据由host_dsp传送过来
	MMax=new float[2048] /*= {0.}*/;
	enselect=new float[640] /*= {0.}*/; //最佳波形
	outdata1=new float[640] /*= {0.}*/; //最佳波形滤波后输出结果?
	Energy_breath=new float[512 / 2] /*= {0.}*/;
	//short preprocess_data1[8192];           //此处存放预处理结果
	//float back_mov_data[8192 / N_rowpoint]; //背景消除后回波
	//float ad_data_add[8192 / N_rowpoint];   //16位AD采样存放数组
	//float processdata_t[1024][BACK_SIZE];

	//short send_ad_data[8192 / N_rowpoint]; //向ARM发送的数组
	//float move_data[1024];
	//float mean_data[1024];
	//float var_processdata[1024];
	//short bodymove_result[20];
	//short pro_ad_data[2048]; //
	//short ad_frame_data1[8192]; //此处存放原始数据
}


Classify_breath::~Classify_breath(void)
{
	if (sdata != NULL)
	{
		delete[] sdata;
	}
	if (result_data_breath != NULL)
	{
		delete[] result_data_breath;
	}
	if (compressdata != NULL)
	{
		delete[] compressdata;
	}
	if (MMax != NULL)
	{
		delete[] MMax;
	}
	if (enselect != NULL)
	{
		delete[] enselect;
	}
	if (outdata1 != NULL)
	{
		delete[] outdata1;
	}
	if (Energy_breath != NULL)
	{
		delete[] Energy_breath;
	}
	if (processdata != NULL)
	{
		for (int i=0;i<768;i++)
		{
			delete [] processdata[i];
		}
		delete [] processdata;
	}
}

void Classify_breath::changeParams(unsigned _fscan, short _antenna_type, unsigned _window, unsigned _ad_element_size) {
	fscan = _fscan;
	antenna_type = _antenna_type;
	window = _window;
	AD_ELEMENT_SIZE = _ad_element_size;
	SLAVE_SIZE = AD_ELEMENT_SIZE / N_rowpoint;
	HARF_AD_SIZE = SLAVE_SIZE / 2;
	REP_LEN = SLAVE_SIZE - WIN - 1;
}
void Classify_breath::detect_breath_pre(short ad_frame_data1[]) {
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
void Classify_breath::detect_breath(short result_data1[], float breath_th, int &isEnd) {
	int i, j /*,k*/;
	//int last;

	//for (i = 0; i < REP_LEN; i++) {
	//    processdata[ad_row][i] = /*accumdata1*/ compressdata[i];
	//}
	// 此处改动，memcpy
	memcpy(processdata[ad_row], compressdata, REP_LEN * sizeof(float));
	//printf("ad_row=%d\n", ad_row);
	//processdata[ad_row][0]=m;/////////////////////////////////////调试
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
			isEnd = 1;
		}
	}
	for (i = 0; i < 8; i++) {
		result_data1[i] = result_data_breath[i];
	}
}

//////////////////////////////////////////
void Classify_breath::pre_breath(void) //呼吸信号预判断及数据打包处理
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
void Classify_breath::breath_process(float breath_th) //呼吸信号后处理过程
{
	unsigned int i = 0, j = 0, k = 0, l = 0, f_position = 0, N_breath = 0;
	float MMean = 0.;
	float signalpower = 0.;
	float totalpower = 0.;
	float Max[20] = {0.};
	//float MMax[STATIC_LEN/N_rowpoint-1-WIN]={0.};
	int position = 0, MMMaxposition = 0, MMaxposition = 0;
	//int fs=16;//扫速
	int N_columncompress = 1;                                    //道间压缩
	int starp = ceil((0.22 / fscan * N_columncompress) * ftnum); //频域能量起始点
	int endp = floor((0.44 / fscan * N_columncompress) * ftnum); //频域能量终止点
	/*COMPLEX* sdata=new COMPLEX[640];*/

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
	totalpower = totalpower - signalpower; //实际上指的是噪声的能量值

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

	result_data_breath[0] = /*0xDDAA*/ 0xDDBB;
	result_data_breath[1] = 4;
	//result_data_breath[2]=0;
	//result_data_breath[3]=0;
	result_data_breath[4] = breathflag;
	result_data_breath[5] = (short)/*(breathmove)*/ MMMaxposition * 4; //上传呼吸信号判别结果
	process_end_flag = 1;                                       //1表示处理完成
}
void Classify_breath::init_breath(void) {
	unsigned int i, j = 0;
	detect_flag = 0;
	//	fscan=16;
	dao_pre_row = 1.0 / pre_row;
	dao_pre_row1 = 1.0 / (pre_row - 1);
	//ftnum = 256; //% 傅立叶变换的点数,最好不小于512
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
	for (i = 0; i < 20; i++) //此处改动
	{
		result_data_breath[i] = 0; //此处改动
		//master_command[i]=0;  //此处改动
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
		pre_row = 128;   //呼吸信号预处理数据道数，
		STORE_ROW = 768; //呼吸信号处理所需的数据道数
		ftnum = 512;
	} else if (fscan == 32) {
		pre_num = 0;
		slow_num_flag = 2;
		ROW = 640;
		pre_row = 128;    //呼吸信号预处理数据道数，
		STORE_ROW = 768; //呼吸信号处理所需的数据道数
		ftnum = 512;
	} else if (fscan == 16) {
		pre_num = 0;
		slow_num_flag = 2;
		//        ROW=320;
		//        pre_row=64;      //呼吸信号预处理数据道数，
		//        STORE_ROW=384;      //呼吸信号处理所需的数据道数
		//		ftnum = 256;
		ROW = 640;
		pre_row = 128;   //呼吸信号预处理数据道数，
		STORE_ROW = 768; //呼吸信号处理所需的数据道数
		ftnum = 512;
	}

}

//sunyongmin 2021.12.27 add begin
void Classify_breath::self_filter(short track_len, short filter_order, float x[], float y[]) {
	unsigned int i = 0, j, k, l, p, q = 0;
	float u = 0.000000001; //收敛因子
	float err = 0.0;       //误差
	//float *w=(float *)calloc(filter_order,sizeof(float));
	//float *ss=(float *)calloc(filter_order,sizeof(float));
	float w[200] = { 0. };
	float ss[200] = { 0. };
	//float w[10]={0.};
	//float ss[10];
	//  for(i=0;i<track_len;i++)
	// y[i] = 0.
	// 此处改动，使用memset初始化
	memset(y, 0, track_len * sizeof(float));
	for (i = 0; i < filter_order; i++)
		w[i] = 1 / (float)(filter_order);
	for (j = filter_order; j < track_len; j++) {
		q = 0;
		for (k = j - filter_order + 1; k <= j; k++) {
			ss[q] = x[k];
			y[j] = y[j] + w[q] * ss[q];
			q++;
		}
		err = x[j] - y[j];
		for (l = 0; l < filter_order; l++) {
			w[l] = w[l] + 2 * u * err * ss[l];
		}
	}
	for (p = 0; p < filter_order; p++) {
		y[p] = y[filter_order];
	}

	//free(w);
	//free(ss);
	// return;
}

COMPLEX Classify_breath::Mul(COMPLEX c1, COMPLEX c2) //实现复数的乘运算
{
	COMPLEX c;
	c.re = c1.re * c2.re - c1.im * c2.im;
	c.im = c1.re * c2.im + c2.re * c1.im;
	return c;
}

void Classify_breath::FFT(COMPLEX *xin, int N) {
	volatile unsigned int f, m, nv2, nm1;
	volatile unsigned int i, k, j = 1, l;
	volatile unsigned int le, lei, ip;
	float pi;
	COMPLEX v, w, t;
	nv2 = N / 2;
	f = N;
	pi = 3.14159265;

	for (m = 1; (f = f / 2) != 1; m++) {
		;
	}
	nm1 = N - 1;
	for (i = 1; i <= N - 1; i++) {
		if (i < j) {
			t = xin[j];
			xin[j] = xin[i];
			xin[i] = t;
		}
		k = nv2;
		while (k < j) {
			j = j - k;
			k = k / 2;
		}
		j = j + k;
	}
	for (l = 1; l <= m; l++) {
		le = pow(2.0, (int)l);
		lei = le / 2;
		v.re = 1.0;
		v.im = 0.0;
		w.re = cos(pi / (float)lei);
		w.im = -sin(pi / (float)lei);
		for (j = 1; j <= lei; j++) {
			for (i = j; i <= N; i = i + le) {
				ip = i + lei;
				t = Mul(xin[ip], v);
				xin[ip].re = xin[i].re - t.re;
				xin[ip].im = xin[i].im - t.im;
				xin[i].re = xin[i].re + t.re;
				xin[i].im = xin[i].im + t.im;
			}
			v = Mul(v, w);
		}
	}
}

//sunyongmin 2021.12.27 add end