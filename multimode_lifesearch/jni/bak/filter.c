#include <stdio.h>
#include <math.h> 
//#include "std.h"
#include "Global.h"


// typedef struct
// {
// 	float re;
// 	float im;
// }COMPLEX;                  //定义一个复数结构

//extern void self_filter(short track_len,short filter_order, float x[], float y[]);
//extern void FFT(COMPLEX *xin, int N);
//extern float Correlation(float x[],float y[],int n);
//自适应滤波//
void self_filter(short track_len,short filter_order, float x[],float y[])
{
 unsigned int i=0,j,k,l,p,q=0;
 float u=0.000000001;   //收敛因子
 float err=0.0;           //误差
 //float *w=(float *)calloc(filter_order,sizeof(float));
 //float *ss=(float *)calloc(filter_order,sizeof(float));
 float w[100]={0.};
 float ss[100]={0.};
 //float w[10]={0.};
 //float ss[10];
 for(i=0;i<track_len;i++) y[i]=0.0;
 for(i=0;i<filter_order;i++) w[i]=1/(float)(filter_order);
 for(j=filter_order;j<track_len;j++)
 { 
   q=0;
   for(k=j-filter_order+1;k<=j;k++)
    {
     ss[q]=x[k];
     y[j]=y[j]+w[q]*ss[q];
     q++;
    }
    err=x[j]-y[j];
    for(l=0;l<filter_order;l++)
    {
      w[l]=w[l]+2*u*err*ss[l];
    }
  }
  for(p=0;p<filter_order;p++)
  {
   y[p]=y[filter_order];
  }
  
  //free(w);
  //free(ss);
 // return;
}
 

COMPLEX Mul(COMPLEX c1,COMPLEX c2)//实现复数的乘运算
{
	COMPLEX c;
	c.re=c1.re*c2.re-c1.im*c2.im;
	c.im=c1.re*c2.im+c2.re*c1.im;
	return c;
}


void FFT(COMPLEX *xin,int N)
{
 volatile unsigned int f,m,nv2,nm1;
 volatile unsigned int i,k,j=1,l;
 volatile unsigned int le,lei,ip;
 float pi;
 COMPLEX v,w,t;
 nv2=N/2;
 f=N;
 pi=3.14159265;
 
 for(m=1;(f=f/2)!=1;m++) {;}
 nm1=N-1;
 for(i=1;i<=N-1;i++)
 {if(i<j) {t=xin[j];xin[j]=xin[i];xin[i]=t;}
  k=nv2;
  while(k<j) {j=j-k;k=k/2;}
  j=j+k;
 }
 for(l=1;l<=m;l++)
 { 
   le=pow(2,l);
   lei=le/2;
   v.re=1.0;
   v.im=0.0;
   w.re=cos(pi/(float)lei);
   w.im=-sin(pi/(float)lei);
   for(j=1;j<=lei;j++)
     { for(i=j;i<=N;i=i+le)
        { ip=i+lei;
          t=Mul(xin[ip],v);
          xin[ip].re=xin[i].re-t.re;
          xin[ip].im=xin[i].im-t.im;
          xin[i].re=xin[i].re+t.re;
          xin[i].im=xin[i].im+t.im;
        }
         v=Mul(v,w);
      }
  }
}   

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



