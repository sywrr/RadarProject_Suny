//#include "Classify_breath.h"
//#include "Classify_body.h"
#include "Classify_breath.cpp"
#include "Classify_body.cpp"
#include <stdio.h>
#include <stdlib.h>
//#include <string.h>
//#include <math.h>

static Classify_body myClassify_body;
static Classify_breath myClassify_breath;

//void main()
//{
//	Classify_body myClassify_body;
//	Classify_breath myClassify_breath;
//
//	FILE *fp;
//	//FILE *fp1;
//	FILE *fp2;
//	//FILE *fp3;
//	FILE *fw;
//	int last, i, m;
//	short result_data1[20],result_data2[20],result_data[20];
//	float breath_th = 1;
//	short ad_frame_data1[8192]; //此处存放原始数据
//	//clock_t  clockBegin, clockEnd,clockEnd1;
//	myClassify_body.changeParams(16, 400, 80, 8192); //传递参数
//	myClassify_breath.changeParams(16, 400, 80, 8192); //传递参数
//	//changeParams(64,400,80,512);//传递参数
//
//	fp = fopen("result.txt", "w");
//	fp2 = fopen("var_processdata.txt", "w");
//	//fw = fopen("C:\\Users\\wang\\Desktop\\result\\ltefile16.lte", "rb");
//	//fw=fopen("E:\\数据\\搜救\\实验室呼吸\\LteFile-5_resample.lte","rb");
//	fw=fopen("E:\\数据\\搜救\\五楼大厅穿北墙\\20221223\\处理文件夹\\ltefile58_p01.lte","rb");
//	fseek(fw, 0L, SEEK_END);
//	last = ftell(fw);
//	fseek(fw, 1024L, SEEK_SET);
//	myClassify_body.init_body();
//	myClassify_breath.init_breath();
//	myClassify_body.multi_flag=1;
//	//clockBegin = clock();
//	for (m = 0; m < (last - 1024) / 8192 / sizeof(short); m++) {
//		fread(ad_frame_data1, sizeof(short), 8192, fw);
//		//detect(ad_frame_data1,result_data1,breath_th);
//		myClassify_body.detect_body_pre(ad_frame_data1);
//		myClassify_body.detect_body(result_data1);
//		myClassify_breath.detect_breath_pre(ad_frame_data1);
//		myClassify_breath.detect_breath(result_data2,1);
//		//detect_breath(result_data1, breath_th);
//		if (result_data1[0]!=0)
//		{
//			for (i=0;i<8;i++)
//			{
//				result_data[i]=result_data1[i];
//			}
//		}
//		else if (result_data2[0]!=0)
//		{
//			result_data[0]=result_data2[0];
//			result_data[1]=result_data2[1];
//			result_data[4]=result_data2[4];
//			result_data[5]=result_data2[5];
//		}
//		else
//		{
//			for (i=0;i<8;i++)
//			{
//				result_data[i]=0;
//			}
//		}
//		for (i = 0; i < 8; i++) {
//			printf("%d ", result_data[i]);
//			fprintf(fp, "%d ", result_data[i]);
//		}
//		printf("%d ", m);
//		printf("\n");
//		fprintf(fp, "%d ", m);
//		fprintf(fp, "\n");
//	}
//	//printf("		%d\n", clockEnd - clockBegin);
//	fclose(fp);
//	fclose(fp2);
//	fclose(fw);
//	system("pause");
//	//return 0;
//}