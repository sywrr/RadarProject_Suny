#ifndef __FILTER_C__
#define __FILTER_C__

#include <stdio.h>
#include <string.h>
#include <math.h>

//#include "Global.h"
//#include "std.h"

#ifdef __cplusplus
extern "C" {
#endif
 
//sunyongmin 2021.12.27 zhushi dakai begin
 //typedef struct
 //{
 //    float re;
 //    float im;
 //} COMPLEX; //定义一个复数结构
 //sunyongmin 2021.12.27 zhushi dakai end

//extern void self_filter(short track_len,short filter_order, float x[], float y[]);
//extern void FFT(COMPLEX *xin, int N);
//extern float Correlation(float x[],float y[],int n);
//自适应滤波//

//float Correlation(float x[],float y[],int n)
//{
//    int j;
//    float xt,yt,t,df;
//    float correof;
//    float sxx=0.0;
//    float sxy=0.0;
//    float syy=0.0;
//    float ay=0.0;
//    float ax=0.0;
//	for(j=0;j<n;j++)
//	{
//		ax+=x[j];
//		ay+=y[j];
//	}
//	ax/=n;
//	ay/=n;
//	for(j=0;j<n;j++)
//	{
//		xt=x[j]-ax;
//		yt=y[j]-ay;
//		sxx+=xt*xt;
//		syy+=yt*yt;
//		sxy+=xt*yt;
//	}
//    correof=sxy/sqrt(sxx*syy);
//    return correof;
//}

#ifdef __cplusplus
}
#endif

#endif
