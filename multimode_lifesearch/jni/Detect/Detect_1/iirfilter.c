/*   ifilt---整型变量。滤波器的类型。取值为1、2和3，分别对应切比雪夫1、切比雪夫2和巴持沃兹滤波器.
     band---整型变量。滤波器的通带形式。取值为1、2、3和4，分别对应低通、高通、带通和带阻滤波器。
     ns---整型变量。滤波器的n阶节数。
     n---整型变量。滤波器每节的阶数。对于低通和高通滤波器，n＝2；对于带通和带阻滤波器，n＝4。
     f1---双精度实型变量。
     f2---双精度实型变量。
     f3---双精度实型变量。
     f4---双精度实型变量。
     对于巴特沃兹滤波器：低通时，f1是通带边界频率，f2＝f3＝f4＝0；
                         高通时，f2是通带边界频率，fl＝f3＝f4＝0;
                         带通时，f2是通带下边界频率，f3是通带上边界频率，f1＝f4＝0；
                         带阻时，fl是通带下边界频率，f4是通带上边界频率，f2＝f3＝0。
     对于切比雪夫滤波器：低通时，f1是通带边界频率，f2是阻带边界频率，f3＝f4＝0；
                         高通时，f2是通带边界频率，fl是阻带边界频率，f3＝f4＝0；
                         带通时，f2是通带下边界频率，J3是通带上边界频率，
                                 f1是阻带下边界频率，f4是阻带上边界频率；
                         带阻时，f1是通带下边界频率，f4是通带上边界频率，
                                 f2是阻带下边界频率，f3是阻带上边界频率。
    db---双精度实型变量。滤波器的阻带衰减(用dB表示)。
    b---双精度实型数组,存放滤波器分子多项式的系数。
    a---双精度实型数组,存放滤波器分母多项式的系数。*/


#include "math.h"
#include "stdlib.h"
#include <string.h>
#ifndef __IIRFILTER_C__
#define __IIRFILTER_C__
//#include "Global.h"
//static double coshl1(double x);

//void iirbcf(int ifilt,int band,int ns,int n,double f1,double f2,double f3,double f4,double db,double b[],double a[])
////double b[],a[],f1,f2,f3,f4,db;
////int ifilt,band,ns,n;
//{
//  int k;
//  double omega,lamda,epslon,fl,fh;
//  double d[5],c[5];
//  //void chebyi(),chebyii(),bwtf();
//  //double coshl1(),warp(),bpsub(),omin();
//  //void fblt();
//  if ((band==1)  ||  (band==4)) fl=f1;
//  if ((band==2)  ||  (band==3)) fl=f2;
//  if (band<=3)  fh=f3;
//  if (band==4)  fh=f4;
//  if(ifilt<3)
//  {
//    switch(band)
//    {
//      case 1:
//      case 2:
//       { omega=warp(f2)/warp(f1);
//         break;
//        }
//      case 3:
//        {
//          omega=omin(bpsub(warp(f1),fh,fl),bpsub(warp(f4),fh,fl));
//          break;
//         }
//      case 4:
//         {
//           omega=omin(1.0/bpsub(warp(f2),fh,fl),1.0/bpsub(warp(f3),fh,fl));}
//     }
//     lamda=pow(10.0,(db/20.0));
//     epslon=lamda/cosh(2*ns*coshl1(omega));
//   }
//   for(k=0;k<ns;k++)
//   { switch(ifilt)
//    { case 1:
//       { chebyi(2*ns,k,4,epslon,d,c);
//        break;
//       }
//      case 2:
//        { chebyii(2*ns,k,4,omega,lamda,d,c);
//
//          break;
//         }
//      case 3:
//         { bwtf(2*ns,k,4,d,c);
//           break;
//         }
//      }
//      fblt(d,c,n,band,fl,fh,&b[k*(n+1)+0],&a[k*(n+1)+0]);
//    }
// }

#ifdef __cplusplus
extern "C" {
#endif

static double coshl1(double x)
//double x;
{
    double z;
    z = log(x + sqrt(x * x - 1.0));
    return (z);
}
static double warp(double f)
//double f;
{
    double pi, z;
    pi = 4.0 * atan(1.0);
    z = tan(pi * f);
    return (z);
}

static double bpsub(double om, double fh, double fl)
//double om,fh,fl;
{
    double z;
    z = (om * om - warp(fh) * warp(fl)) / ((warp(fh) - warp(fl)) * om);
    return (z);
}

static double omin(double om1, double om2)
//double om1,om2;
{
    double z, z1, z2;
    z1 = fabs(om1);
    z2 = fabs(om2);
    z = (z1 < z2) ? z1 : z2;
    return (z);
}

static void bwtf(int ln, int k, int n, double d[], double c[])
//int ln,k,n;
//double d[],c [];
{
    int i;
    double pi, tmp;
    pi = 4.0 * atan(1.0);
    d[0] = 1.0;
    c[0] = 1.0;
    //  for(i=1;i<=n;i++)
    //  {
    //   d[i]=0.0;
    //   c[i]=0.0;
    //  }
    //此处改动，memset初始化
    memset(d + 1, 0, n * sizeof(double));
    memset(c + 1, 0, n * sizeof(double));
    tmp = (k + 1) - (ln + 1.0) / 2.0;
    if (tmp == 0.0) {
        c[1] = 1.0;
    } else {
        c[1] = -2.0 * cos((2 * (k + 1) + ln - 1) * pi / (2 * ln));
        c[2] = 1.0;
    }
}

static void chebyi(int ln, int k, int n, double ep, double d[], double c[])
//double d[],c[],ep;
//int ln,k,n;
{
    int i;
    double pi, gam, omega, sigma;
    pi = 4.0 * atan(1.0);
    gam = pow(((1.0 + sqrt(1.0 + ep * ep)) / ep), 1.0 / ln);
    sigma = 0.5 * (1.0 / gam - gam) * sin((2 * (k + 1) - 1) * pi / (2 * ln));
    omega = 0.5 * (1.0 / gam + gam) * cos((2 * (k + 1) - 1) * pi / (2 * ln));
    // for (i = 0; i <= n; i++) {
    //     d[i] = 0.0;
    //     c[i] = 0.0;
    // }
    // 此处改动，memset初始化
    memset(d, 0, (n + 1) * sizeof(double));
    memset(c, 0, (n + 1) * sizeof(double));
    if (((ln % 2) == 1) && ((k + 1) == (ln + 1) / 2)) {
        d[0] = -sigma;
        c[0] = d[0];
        c[1] = 1.0;
    }

    else {
        c[0] = sigma * sigma + omega * omega;
        c[1] = -2.0 * sigma;
        c[2] = 1.0;
        d[0] = c[0];
        if (((ln % 2) == 0) && (k == 0))
            d[0] = d[0] / sqrt(1.0 + ep * ep);
    }
}

static void chebyii(int ln, int k, int n, double ws, double att, double d[], double c[])
//double d[],c[],ws,att;
//int ln,k,n;
{
    int i;
    double pi, gam, alpha, beta, sigma, omega, scln, scld;
    pi = 4.0 * atan(1.0);
    gam = pow((att + sqrt(att * att - 1.0)), 1.0 / ln);
    alpha = 0.5 * (1.0 / gam - gam) * sin((2 * (k + 1) - 1) * pi / (2 * ln));
    beta = 0.5 * (1.0 / gam + gam) * cos((2 * (k + 1) - 1) * pi / (2 * ln));
    sigma = ws * alpha / (alpha * alpha + beta * beta);
    omega = -1.0 * ws * beta / (alpha * alpha + beta * beta);
    // for (i = 0; i <= n; i++) {
    //     d[i] = 0.0;
    //     c[i] = 0.0;
    // }
    // 此处改动，memset初始化
    memset(d, 0, (n + 1) * sizeof(double));
    memset(c, 0, (n + 1) * sizeof(double));
    if (((ln % 2) == 1) && ((k + 1) == (ln + 1) / 2)) {
        d[0] = -1.0 * sigma;
        c[0] = d[0];
        c[1] = 1.0;
    } else {
        scln = sigma * sigma + omega * omega;
        scld = pow((ws / cos((2 * (k + 1) - 1) * pi / (2 * ln))), 2);
        d[0] = scln * scld;
        d[2] = scln;
        c[0] = d[0];
        c[1] = -2.0 * sigma * scld;
        c[2] = scld;
    }
}

static double combin(int i1, int i2)
//int i1,i2;
{
    int i;
    double s;
    s = 1.0;
    if (i2 == 0) return (s);
    for (i = i1; i > (i1 - i2); i--) {
        s *= i;
    }
    return (s);
}

static void bilinear(double d[], double c[], double b[], double a[], int n)
//int n;
//double d[],c[],b[],a[];
{
    int i, j, n1;
    double sum, atmp, scale, *temp;
    n1 = n + 1;
    temp = (double *)malloc(n1 * n1 * sizeof(double));
    for (j = 0; j <= n; j++) {
        temp[j * n1 + 0] = 1.0;
    }
    sum = 1.0;
    for (i = 1; i <= n; i++) {
        sum = sum * (double)(n - i + 1) / (double)i;
        temp[0 * n1 + i] = sum;
    }
    for (i = 1; i <= n; i++)
        for (j = 1; j <= n; j++) {
            temp[j * n1 + i] = temp[(j - 1) * n1 + i] - temp[j * n1 + i - 1] - temp[(j - 1) * n1 + i - 1];
        }
    for (i = n; i >= 0; i--) {
        b[i] = 0.0;
        atmp = 0.0;
        for (j = 0; j <= n; j++) {
            b[i] = b[i] + temp[j * n1 + i] * d[j];
            atmp = atmp + temp[j * n1 + i] * c[j];
        }
        scale = atmp;
        if (i != 0) a[i] = atmp;
    }
    for (i = 0; i <= n; i++) {
        b[i] = b[i] / scale;
        a[i] = a[i] / scale;
    }
    a[0] = 1.0;
    free(temp);
}

//#include"stdlib.h"
static void fblt(double d[], double c[], int n, int band, double fln, double fhn, double b[], double a[])
//int n,band;
//double fln,fhn,d[],c[],b[],a[];
{
    int i, k, m, n1, n2, ls;
    double pi, w, w0, w1, w2, tmp, tmpd, tmpc, *work;
    //double combin();
    //void bilinear();
    pi = 4.0 * atan(1.0);
    w1 = tan(pi * fln);
    for (i = n; i >= 0; i--) {
        if ((c[i] != 0.0) || (d[i] != 0.0))
            break;
    }
    m = i;

    switch (band) {
    case 1:
    case 2: {
        n2 = m;
        n1 = n2 + 1;
        if (band == 2) {
            for (i = 0; i <= m / 2; i++) {
                tmp = d[i];
                d[i] = d[m - i];
                d[m - i] = tmp;
                tmp = c[i];
                c[i] = c[m - i];
                c[m - i] = tmp;
            }
        }
        for (i = 0; i <= m; i++) {
            d[i] = d[i] / pow(w1, i);
            c[i] = c[i] / pow(w1, i);
        }
        break;
    }
    case 3:
    case 4: {
        n2 = 2 * m;
        n1 = n2 + 1;
        work = (double *)malloc(n1 * n1 * sizeof(double));
        w2 = tan(pi * fhn);
        w = w2 - w1;
        w0 = w1 * w2;
        if (band == 4) {
            for (i = 0; i <= m / 2; i++) {
                tmp = d[i];
                d[i] = d[m - i];
                d[m - i] = tmp;
                tmp = c[i];
                c[i] = c[m - i];
                c[m - i] = tmp;
            }
        }
        // for (i = 0; i <= n2; i++) {
        //     work[0 * n1 + i] = 0.0;
        //     work[1 * n1 + i] = 0.0;
        // }
        //此处改动，memset初始化
        memset(work, 0, (n2 + 1) * sizeof(double));
        memset(work + n1, 0, (n2 + 1) * sizeof(double));
        for (i = 0; i <= m; i++) {
            tmpd = d[i] * pow(w, (m - i));
            tmpc = c[i] * pow(w, (m - i));
            for (k = 0; k <= i; k++) {
                ls = m + i - 2 * k;
                tmp = combin(i, i) / (combin(k, k) * combin(i - k, i - k));
                work[0 * n1 + ls] += tmpd * pow(w0, k) * tmp;
                work[1 * n1 + ls] += tmpc * pow(w0, k) * tmp;
            }
        }
        // for (i = 0; i <= n2; i++) {
        //     d[i] = work[0 * n1 + i];
        //     c[i] = work[1 * n1 + i];
        // }
        // 此处改动，memcpy
        memcpy(d, work, (n2 + 1) * sizeof(double));
        memcpy(c, work + n1, (n2 + 1) * sizeof(double));
        free(work);
    }
    }
    bilinear(d, c, b, a, n);
}

void iirbcf(int ifilt, int band, int ns, int n, double f1, double f2, double f3, double f4, double db, double b[], double a[])
//double b[],a[],f1,f2,f3,f4,db;
//int ifilt,band,ns,n;
{
    int k;
    double omega, lamda, epslon, fl, fh;
    double d[5], c[5];
    //void chebyi(),chebyii(),bwtf();
    //double coshl1(),warp(),bpsub(),omin();
    //void fblt();
    if ((band == 1) || (band == 4)) fl = f1;
    if ((band == 2) || (band == 3)) fl = f2;
    if (band <= 3) fh = f3;
    if (band == 4) fh = f4;
    if (ifilt < 3) {
        switch (band) {
        case 1:
        case 2: {
            omega = warp(f2) / warp(f1);
            break;
        }
        case 3: {
            omega = omin(bpsub(warp(f1), fh, fl), bpsub(warp(f4), fh, fl));
            break;
        }
        case 4: {
            omega = omin(1.0 / bpsub(warp(f2), fh, fl), 1.0 / bpsub(warp(f3), fh, fl));
        }
        }
        lamda = pow(10.0, (db / 20.0));
        epslon = lamda / cosh(2 * ns * coshl1(omega));
    }
    for (k = 0; k < ns; k++) {
        switch (ifilt) {
        case 1: {
            chebyi(2 * ns, k, 4, epslon, d, c);
            break;
        }
        case 2: {
            chebyii(2 * ns, k, 4, omega, lamda, d, c);

            break;
        }
        case 3: {
            bwtf(2 * ns, k, 4, d, c);
            break;
        }
        }
        fblt(d, c, n, band, fl, fh, &b[k * (n + 1) + 0], &a[k * (n + 1) + 0]);
    }
}

#ifdef __cplusplus
}
#endif

#endif
