#include "Classify_body.h"
#include <stdlib.h>
#include <math.h>
#include "iirfilter.c"


Classify_body::Classify_body(void)
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
	pre_num = 1;
	slow_num_flag = 4;
}


Classify_body::~Classify_body(void)
{
}
void Classify_body::changeParams(unsigned _fscan, short _antenna_type, unsigned _window, unsigned _ad_element_size) {
	fscan = _fscan;
	antenna_type = _antenna_type;
	window = _window;
	AD_ELEMENT_SIZE = _ad_element_size;
	SLAVE_SIZE = AD_ELEMENT_SIZE / N_rowpoint;
	HARF_AD_SIZE = SLAVE_SIZE / 2;
	REP_LEN = SLAVE_SIZE - WIN - 1;
}
void Classify_body::detect_body_pre(short ad_frame_data1[]) {
	int i, j /*,k*/;
	//int last;

	for (i = 0; i < 20; i++) {
		result_data_body[i] = 0;
		//        result_data_breath[i] = 0;
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
void Classify_body::detect_body(short result_data1[]) 
{
    int i, j /*,k*/;
    //int last;

    pre_process();
    z_ave_process();

    for (i = 0; i < 8; i++) 
	{
        result_data1[i] = result_data_body[i];
    }
}
/*处理程序*/
void Classify_body::pre_process(void) {
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
    BandPass(SLAVE_SIZE, &back_mov_data[0], &send_ad_data[0]); // back_mov_data消除背景之后的数据，send_ad_data为带通滤波后的数据
    //for (i=0;i<SLAVE_SIZE;i++)
    //{
    //	fprintf(fp3,"%d ",send_ad_data[i]);
    //}
    //fprintf(fp3,"\n");

    return;
}
void Classify_body::z_ave_process(void) //两道数据进行平均然后再进行处理
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

void Classify_body::zscore(void) //int nsamp,short send_ad_data[], float processdata[]
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
        processdata_t[i][Bscan_num] = (pro_ad_data[2 * i] - meancol) / std2; //计算完后的数据需要放到数组中并更新数组
                                                                             //processdata_t[i][Bscan_num]=pro_ad_data[2*i];
                                                                             //fprintf(fp,"%f ",processdata_t[i][Bscan_num]);
                                                                             //fflush(fp);
    }
    //fprintf(fp,"\n");
    detect_num++;
    mean_numadd = 1 / (float)detect_num;
    if (detect_num <= BACK_SIZE) //detect_num从1开始，Bscan_num从0开始。累计16道
    {
        detec_start = 0;
        for (i = 0; i < SLAVE_SIZE / 2; i++) //求16道均值
        {
            mean_data[i] = (mean_data[i] * (detect_num - 1) + processdata_t[i][Bscan_num]) * mean_numadd;
        }
    } else {
        pre_num = 1 /*13*/; ////////////////////////////////////////////////////////////////////////
        slow_num++;
        detect_num = BACK_SIZE + 1; //detect_num大于17后都赋值成17
        /*		if (slow_num>=4)
		{
		detec_start=1;
		slow_num=0;
		}*/
		if (detec_start == 0) //16时先检测一次
		{
			body_detect();
		}
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
        Bscan_num = Bscan_num % BACK_SIZE; //取余，满16置为0
    }
    if (detec_start == 1 && slow_num > slow_num_flag) /////////////////////////////////////////////////////////////////改
    {
        body_detect();
        slow_num = 1;
    }
}

void Classify_body::body_detect(void) {
    //以下对应的是第5步处理，主要是在计算方差
    int pflag = 0; //是否存在人体运动目标标志位
	int pflag_second = 0; //是否存在人体运动目标标志位
	int pos_val_second=0;
    int row0 = 0, row1 = 0;
    float mean_v, std_v;
    float std_p = 0.;
    int i = 0, j = 0;
    bodymove = 0;

    for (i = 0; i < SLAVE_SIZE / 2; i++) {
        std_p = 0.;
        for (j = 0; j < BACK_SIZE; j++)
            std_p = std_p + (processdata_t[i][j] - mean_data[i]) * (processdata_t[i][j] - mean_data[i]);
        var_processdata[i] = std_p / (float)BACK_SIZE; //将计算的方差数据保存到数组中
        // fprintf(fp2, "%f ", var_processdata[i]);
    }
    // fprintf(fp2, "\n");
    //深度方向上滑窗判断s1，窗为row0到row1
	if (multi_flag==0)
	{
		//深度方向上滑窗判断s1，窗为row0到row1
		for (i = /*2*/ /*10*/(AD_ELEMENT_SIZE/160); i < SLAVE_SIZE / 2 - 1; i++) {
			pflag = 0;
			row0 = i - ceil(BACK_SIZE * 0.5);
			row1 = i + floor(BACK_SIZE * 0.5);
			if (row0 < 0) row0 = 0;
			if (row1 > SLAVE_SIZE / 2) row1 = SLAVE_SIZE / 2;
			if ((var_processdata[i] - var_processdata[i - 1] > 0) & (var_processdata[i] - var_processdata[i + 1] > 0) & (var_processdata[i] > TH1)) {
				pflag = 1;
				pos_val = i;
				//排除可能存在的虚警信号
				mean_v = 0.;
				std_v = 0.;
				for (j = row0; j < row1; j++)
					mean_v = mean_v + var_processdata[j];
				mean_v = mean_v / (float)(row1 - row0 + 1);
				for (j = row0; j < row1; j++)
					std_v = std_v + (var_processdata[j] - mean_v) * (var_processdata[j] - mean_v);
				std_v = std_v / (float)(row1 - row0 + 1);
				if ((std_v < TH2) || (pro_ad_data[2*i]<12500/i/*20*/))//增加条件
				{
					pflag = 0;
				}
				else
				{
					//bodymove=(signalpos+window*(pos_val+1)/(SLAVE_SIZE/2))*15.0;                    //计算距离信息
					bodymove = pos_val * 8;
					break;
				}
			}
		}
		//if (pflag == 1) //排除可能存在的虚警信号
		//{
		//	mean_v = 0.;
		//	std_v = 0.;
		//	for (j = row0; j < row1; j++)
		//		mean_v = mean_v + var_processdata[j];
		//	mean_v = mean_v / (float)(row1 - row0 + 1);
		//	for (j = row0; j < row1; j++)
		//		std_v = std_v + (var_processdata[j] - mean_v) * (var_processdata[j] - mean_v);
		//	std_v = std_v / (float)(row1 - row0 + 1);
		//	if (std_v < TH2)
		//		pflag = 0;
		//	else
		//		//bodymove=(signalpos+window*(pos_val+1)/(SLAVE_SIZE/2))*15.0;                    //计算距离信息
		//		bodymove = pos_val * 8;
		//}
	} 
	else
	{
		//深度方向上滑窗判断s1，窗为row0到row1
		for (i = /*2*/ /*10*/(AD_ELEMENT_SIZE/160); i < SLAVE_SIZE / 2 - 1; i++) {
			pflag = 0;
			row0 = i - ceil(BACK_SIZE * 0.5);
			row1 = i + floor(BACK_SIZE * 0.5);
			if (row0 < 0) row0 = 0;
			if (row1 > SLAVE_SIZE / 2) row1 = SLAVE_SIZE / 2;
			if ((var_processdata[i] - var_processdata[i - 1] > 0) & (var_processdata[i] - var_processdata[i + 1] > 0) & (var_processdata[i] > TH1)) {
				pflag = 1;
				pos_val = i;
				//排除可能存在的虚警信号
				mean_v = 0.;
				std_v = 0.;
				for (j = row0; j < row1; j++)
					mean_v = mean_v + var_processdata[j];
				mean_v = mean_v / (float)(row1 - row0 + 1);
				for (j = row0; j < row1; j++)
					std_v = std_v + (var_processdata[j] - mean_v) * (var_processdata[j] - mean_v);
				std_v = std_v / (float)(row1 - row0 + 1);
				if ((std_v < TH2) || (pro_ad_data[2*i]<12500/i/*20*/))//增加条件  100/((i*8)/1000)
				{
					pflag = 0;
				}
				else
				{
					//bodymove=(signalpos+window*(pos_val+1)/(SLAVE_SIZE/2))*15.0;                    //计算距离信息
					bodymove = pos_val * 8;
					break;
				}
			}
		}
		//if (pflag == 1) //排除可能存在的虚警信号
		//{
		//	mean_v = 0.;
		//	std_v = 0.;
		//	for (j = row0; j < row1; j++)
		//		mean_v = mean_v + var_processdata[j];
		//	mean_v = mean_v / (float)(row1 - row0 + 1);
		//	for (j = row0; j < row1; j++)
		//		std_v = std_v + (var_processdata[j] - mean_v) * (var_processdata[j] - mean_v);
		//	std_v = std_v / (float)(row1 - row0 + 1);
		//	if (std_v < TH2)
		//		pflag = 0;
		//	else
		//		//bodymove=(signalpos+window*(pos_val+1)/(SLAVE_SIZE/2))*15.0;                    //计算距离信息
		//		bodymove = pos_val * 8;
		//}
		if (pflag == 1)//找第二个目标
		{
			//for (i =pos_val+HARF_AD_SIZE/8; i < SLAVE_SIZE / 2 - 1; i++)
			for (i =SLAVE_SIZE / 2; i > pos_val+HARF_AD_SIZE/8; i--)
			{
				pflag_second = 0;
				if ((var_processdata[i] - var_processdata[i - 1] > 0) & (var_processdata[i] - var_processdata[i + 1] > 0) & (var_processdata[i] > TH3)) {
					pflag_second = 1;
					pos_val_second = i;
					//排除可能存在的虚警信号
					mean_v = 0.;
					std_v = 0.;
					for (j = row0; j < row1; j++)
						mean_v = mean_v + var_processdata[j];
					mean_v = mean_v / (float)(row1 - row0 + 1);
					for (j = row0; j < row1; j++)
						std_v = std_v + (var_processdata[j] - mean_v) * (var_processdata[j] - mean_v);
					std_v = std_v / (float)(row1 - row0 + 1);
					if ((std_v < TH2) || (pro_ad_data[2*i]<12500/i/*20*/))//增加条件
					{
						pflag_second = 0;
					}
					else
					{
						//bodymove=(signalpos+window*(pos_val+1)/(SLAVE_SIZE/2))*15.0;                    //计算距离信息
						//bodymove = pos_val_second * 8;
						break;
					}
					//break;
				}
			}
		}
	}

    result_data_body[0] = 0xDDAA;
    result_data_body[1] = 4;
    result_data_body[2] = pflag;
    result_data_body[3] = (short)bodymove /*pos_val*8*/; //
    //result_data[4]=0;
    //result_data[5]=0;  //上传呼吸信号判别结果
	if (multi_flag!=0)
	{
		result_data_body[1] = 6;
		result_data_body[6] = pflag_second;
		result_data_body[7] = pos_val_second*8; //
	}
    bodymove_flag = 1; //1表示处理完成
}

//points  ：数组x的有效滤波长度
//x为输入数据组，输出结果
//flag  :标志=1
//滤波程序公式：a(0)*y(n) = b(0)*x(n) + b(1)*x(n-1) + ... + b(nb)*x(n-nb)- a(1)*y(n-1) - ... - a(na)*y(n-na)

void Classify_body::BandPass(int points, float *x, short *xout) {
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
void Classify_body::init_body(void) {
    unsigned int i, j = 0;
    detect_flag = 0;
    //	fscan=16;
    dao_pre_row = 1.0 / pre_row;
    dao_pre_row1 = 1.0 / (pre_row - 1);
    ftnum = 512; //% 傅立叶变换的点数,最好不小于512
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
        result_data_body[i] = 0; //此处改动
                                 //master_command[i]=0;  //此处改动
    }

    zscore_flag = 0;
    Bscan_num = 0;
    detect_num = 0;
    detec_start = 0;
    pos_val = 0;
    //pre_num=2;
    slow_num = 0;
    //for (i = 0; i < AD_ELEMENT_SIZE; i++)
    //    ad_frame_data1[i] = 0;
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