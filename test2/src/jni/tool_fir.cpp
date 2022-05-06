#include "tool_fir.h"
#include <math.h>
#include <stdio.h>
#include <time.h>
#include <windows.h>

double bessel0(double x)
{
	int i;
	double d, y, d2, sum;
	y = x / 2.0;
	d = 1.0;
	sum = 1.0;
	for (i = 1; i <= 25; i++)
	{
		d = d*y / i;
		d2 = d*d;
		sum = sum + d2;
		if (d2 < sum*(1.0e-8))  break;
	}
	return(sum);
}

double kaiser(int i, int n, double beta)
{
	double a, w, a2, b1, b2, beta1;
	b1 = bessel0(beta);
	a = 2.0*i / (double)(n - 1) - 1.0;
	a2 = a*a;
	beta1 = beta*sqrt(1.0 - a2);
	b2 = bessel0(beta);
	w = b2 / b1;
	return(w);
}

double window(int type, int n, int i, double beta)
{
	int k;
	double pi, w;
	pi = 4.0*atan(1.0);
	w = 1.0;
	switch (type)
	{
	case 1:
	{
		w = 1.0;
		break;
	}
	case 2:
	{
		k = (n - 2) / 10;
		if (i <= k)
			w = 0.5*(1.0 - cos(i*pi / (k + 1)));
		if (i > n - k - 2)
			w = 0.5*(1.0 - cos((n - i - 1)*pi / (k + 1)));
		break;
	}
	case 3:
	{
		w = 1.0 - fabs(1.0 - 2 * i / (n - 1.0));
		break;
	}
	case 4:
	{
		w = 0.5*(1.0 - cos(2 * i*pi / (n - 1)));
		break;
	}
	case 5:
	{
		w = 0.54 - 0.46*cos(2 * i*pi / (n - 1));
		break;
	}
	case 6:
	{
		w = 0.42 - 0.5*cos(2 * i*pi / (n - 1)) + 0.08*cos(4 * i*pi / (n - 1));
		break;
	}
	case 7:
	{
		w = kaiser(i, n, beta);
		break;
	}
	}
	return(w);
}

int firwin(int n, int band, double fln, double fhn, int wn, double h[])   //因为要求滤波器系数精度高，所以这里取double型
{
	int i, n2, mid;
	double s, pi, wc1, wc2, beta, delay;
	beta = 0.0;
	pi = 4.0*atan(1.0);
	if ((n % 2) == 0)
	{
		n2 = n / 2 - 1;
		mid = 1;
	}
	else
	{
		n2 = n / 2;
		mid = 0;
	}
	delay = n / 2.0;
	wc1 = 2.0*pi*fln;
	if (band >= 3) wc2 = 2.0*pi*fhn;
	switch (band)
	{
	case 1:
	{
		for (i = 0; i <= n2; i++)
		{
			s = i - delay;
			h[i] = (sin(wc1*s) / (pi*s))*window(wn, n + 1, i, beta);
			h[n - i] = h[i];
		}
		if (mid == 1)
		{
			h[n / 2] = wc1 / pi;
		}
		break;
	}
	case 2:
	{
		for (i = 0; i <= n2; i++)
		{
			s = i - delay;
			h[i] = (sin(pi*s) - sin(wc1*s)) / (pi*s);
			h[i] = h[i] * window(wn, n + 1, i, beta);
			h[n - i] = h[i];
		}
		if (mid == 1)
		{
			h[n / 2] = 1.0 - wc1 / pi;
		}
		break;
	}
	case 3:
	{
		for (i = 0; i <= n2; i++)
		{
			s = i - delay;
			h[i] = (sin(wc2*s) - sin(wc1*s)) / (pi*s);
			h[i] = h[i] * window(wn, n + 1, i, beta);
			h[n - i] = h[i];
		}
		if (mid == 1) h[n / 2] = (wc2 - wc1) / pi;
		break;
	}
	case 4:
	{
		for (i = 0; i <= n2; i++)
		{
			s = i - delay;
			h[i] = (sin(wc1*s) + sin(pi*s) - sin(wc2*s)) / (pi*s);
			h[i] = h[i] * window(wn, n + 1, i, beta);
			h[n - i] = h[i];
		}
		if (mid == 1) h[n / 2] = (wc1 + pi - wc2) / pi;
		break;
	}
	}
	return(n2);
}

 void FIRFilter(float** dataOut,float** dataIn, int Row, int Col, int timeWindow,
	int filterLenght, int filterType, int windowType, double lowFreq, double highFreq
	)
{
	int i, j, ii, k;
	double* h = new double[1024];                       //保存滤波器系数
	double fs = (double)Row / timeWindow * 1000;    //雷达采样频率，转化为MHz
	lowFreq = lowFreq / fs;
	highFreq = highFreq / fs;
	firwin(filterLenght, filterType, lowFreq, highFreq, windowType, h);           //函数调用计算滤波器的系数,滤波阶数30,都选择布莱克曼窗函数

	float Data;                             //中间变量
	for (j = 0; j < Col; j++)
	{
		for (i = 0; i < Row; i++)
		{
			Data = 0;
			if (i < 15)
			{
				for (ii = 0; ii < i + 16; ii++)
				{
					Data = Data + (float)h[ii] * (float)dataIn[i - ii + 15][j];
				}
			}
			else
			{
				for (ii = 0; ii <= 30; ii++)
				{
					k = i - ii + 15;
					if (k >= Row - 1)
					{
						k = Row - 1;
					}
					Data = Data + (float)h[ii] * (float)dataIn[k][j];
				}
			}
			dataOut[i][j] = (float)Data;
		}
	}
	delete[] h;
}

void CorrectZeroOffset(float *m_dataBuf,float *m_dataBufProc,int scanLen,int antennaFreq,int timeWindow)
{
	int i,k;
	int moveWnd=int(1.0/antennaFreq*1000/timeWindow*scanLen/2);
	for (i=0;i<4;i++)
	{
		m_dataBufProc[i]=m_dataBuf[i];
	}
	for (i=4;i<scanLen;i++)
	{
		double sum=0;
		if (i-moveWnd<0)
		{
			for (k=0;k<2*moveWnd;k++)
			{
				sum+=m_dataBuf[k];
			}
		}
		else if (i+moveWnd>scanLen)
		{
			for (k=scanLen-2*moveWnd;k<scanLen;k++)
			{
				sum+=m_dataBuf[k];
			}
		}
		else
		{
			for (k=i-moveWnd;k<i+moveWnd;k++)
			{
				sum+=m_dataBuf[k];
			}
		}
		m_dataBufProc[i]=float(m_dataBuf[i]-sum/(2*moveWnd));
	}
}

 void FIRFilterSingle(float* dataOut, float* dataIn, int Row, int timeWindow,
	int filterLenght, int filterType, int windowType, double lowFreq, double highFreq)
{
	int i, ii, k;
	double* h = new double[1024];                       //保存滤波器系数
	double fs = (double)Row / timeWindow * 1000;    //雷达采样频率，转化为MHz
	lowFreq = lowFreq / fs;
	highFreq = highFreq / fs;
	firwin(filterLenght, filterType, lowFreq, highFreq, windowType, h);           //函数调用计算滤波器的系数,滤波阶数30,都选择布莱克曼窗函数
		
	float Data;                             //中间变量
	for (i = 0; i < Row; i++)
	{
		Data = 0;
		if (i < 15)
		{
			for (ii = 0; ii < i + 16; ii++)
			{
				Data = Data + (float)h[ii] * (float)dataIn[i - ii + 15];
			}
		}
		else
		{
			for (ii = 0; ii <= 30; ii++)
			{
				k = i - ii + 15;
				if (k >= Row - 1)
				{
					k = Row - 1;
				}
				Data = Data + (float)h[ii] * (float)dataIn[k];
			}
		}
		dataOut[i] = (float)Data;
	}
	delete[] h;
}

void auto_gainjust(int len,int antenna_type1, int segmentedGainMax,int *datain,int *FANGDA1,int *multinum1)
{
	//定义静态变量
	//static int antenna_type1 = 400;
	float BM1[9];
	int dbvalue[10];
    float Amp[9];

	int Num=0,i=0,j=0,ii=0; 
	int dbvalue_1;
	//900 和1500  使用此参数可以
	if(antenna_type1== 1500)
	{
		BM1[0] = 5000;
		BM1[1] = 6500;
		BM1[2] = 6500;
		BM1[3] = 6000;
		BM1[4] = 5500;
		BM1[5] = 5200;
		BM1[6] = 4800;
		BM1[7] = 4400;
		BM1[8] = 4000;
		//BM1[9] = 4000;

	}
	else if (antenna_type1== 400)
	{
		BM1[0] = 5000;
		BM1[1] = 5200;
		BM1[2] = 5200;
		BM1[3] = 5200;
		BM1[4] = 5000;
		BM1[5] = 4000;
		BM1[6] = 3200;
		BM1[7] = 3000;
		BM1[8] = 2800;
		//BM1[9] = 2500;
	}
	else if (antenna_type1== 900)
	{
		BM1[0] = 5000;
		BM1[1] = 6500;
		BM1[2] = 6500;
		BM1[3] = 6000;
		BM1[4] = 5500;
		BM1[5] = 5200;
		BM1[6] = 4800;
		BM1[7] = 4400;
		BM1[8] = 4000;
		//BM1[9] = 4000;
	} 
	else 
	{
		BM1[0] = 5000;
		BM1[1] = 6500;
		BM1[2] = 6500;
		BM1[3] = 6000;
		BM1[4] = 5500;
		BM1[5] = 5200;
		BM1[6] = 4800;
		BM1[7] = 4400;
		BM1[8] = 4000;
		//BM1[9] = 4000;
	}       	   
	Num=len/8;
	Amp[0]=0;
	for(i=4;i<Num;i++)
		Amp[0]=Amp[0]+abs(datain[i]);
	Amp[0]=Amp[0]/(Num-4);
	for(j=1;j<8;j++)
	{
		Amp[j]=0;
		for(ii=(j-0.5)*Num;ii<(j+1)*Num;ii++)
			Amp[j]=Amp[j]+abs(datain[ii]);
		Amp[j]=Amp[j]/(1.5*Num);        
	}
	Amp[8]=0;
	for(ii=7*Num;ii<len;ii++)
		Amp[8]=Amp[8]+abs(datain[ii]);
	Amp[8]=Amp[8]/Num;  

	for(i=0;i<9;i++)
	{
		Amp[i]=Amp[i]/256;
		if( Amp[i] > BM1[i])
		{
			BM1[i] = Amp[i];
		}
		dbvalue[i]=(int)(20*log10(BM1[i]/Amp[i]));
		dbvalue_1 = dbvalue[i];
		if(dbvalue[i]>segmentedGainMax) 
		{
			//multinum1[i]=pow(segmentedGainMax,((dbvalue[i]-5)*0.05));
			multinum1[i]=dbvalue[i]-segmentedGainMax;
			dbvalue_1=segmentedGainMax; 		  
		}
		else 
		{	       
			multinum1[i]=1.0;
		}
		//FANGDA1[i]=(short)(4096/(sqrt(pow(10,0.05*(dbvalue_1+4.39)))));
	    FANGDA1[i]=dbvalue_1;
		Amp[i]=Amp[i]*100;
	}
	for(i = 9;i > 0;i--)
	{
	  dbvalue[i] = dbvalue[i - 1]; 
	 }
	//dbvalue[0] = 0xcc00;

	//Write_cmdtoUsb((int * ) dbvalue);	
}

 void ZDZY(int record_len1,int *datain, int antennaType, int segmentedGainMax, int *FANGDA1,int *multinum1,int &isEnd)
{
	//static int length =0;
	static int ad_frame_data1[8192];   //保存送入数据                     
	memcpy(ad_frame_data1,datain, record_len1*sizeof(int));
	//length += len*4;

	////定义静态变量
	//static int record_len1=512;
	static int average_row1 = 10;                       
	static int ad_data1[8192]={0};    //平均数据保存为int类型                            //sdram  
	static float average_data1[8192]={0};  //保存平均数据
	static float  ave_row1 = 1.,ave_row11 = 1.;
	static int average_num1 = 0; 
	static int  filter_flag1 = 1,filter_flag11 = 1; 
	static int background_flag1 = 0,background_flag11 = 0;

	int i = 0;
	volatile float ave_numadd = 1;
	average_num1++;//////
	ave_numadd = 1 / (float)average_num1;
	//BandPass((record_len1 - 4), & ad_frame_data1[3], & ad_pongdataa[4]);

	for(i = 1;i < record_len1;i++) 
	{
		if(average_num1 <= average_row1)
		{
			average_data1[i] = (average_data1[i] * (average_num1 - 1) + /*ad_pongdataa*/ad_frame_data1[i]) * ave_numadd;
			ad_data1[i] = (int)average_data1[i];
		}
		else
		{
			average_data1[i] = (average_data1[i] * (average_row1 - 1) + /*ad_pongdataa*/ad_frame_data1[i]) * (ave_row1);
			average_num1 = average_row1 + 1;
			ad_data1[i] = (int)average_data1[i];
		}
	}
	if(average_num1 == 10)
	{
		//gain_just1 = 0;
		average_num1 = 0;
		ave_row1 = ave_row11;
		average_row1 = 10;
		filter_flag1 = filter_flag11;
		background_flag1 = background_flag11;
		//sp_max(ad_data1, record_len1);
	    auto_gainjust(record_len1, antennaType, segmentedGainMax, ad_data1, FANGDA1, multinum1);
		isEnd = 1;
	}
}
