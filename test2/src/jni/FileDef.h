/****************************************************************************************
*
* @file    FileDef.h
* @version 1.0
* @date    2020-10-14
* @author  孙永民
*
* @brief   文件服务公共文件
*
***************************************************************************************/
// 版权(C) 2009 - 2012 海信网络科技
// 改动历史
// 日期         作者     改动内容
// 2021-02-01   孙永民   创建文件
//==============================================================================

#ifndef __app_web_trans_FileDef_h__
#define __app_web_trans_FileDef_h__

#include <string>

namespace DAQ
{
	//文件头的长度
	#define HEADER_LENGTH			1024		//文件头长度

	//日期参数结构
	struct HEAD_DATE{
		unsigned sec2 : 5;				//秒/2	(0-29)
		unsigned min : 6;				//分		(0-59)
		unsigned hour : 5;				//时		(0-23)
		unsigned day : 5;				//日		(0-31)
		unsigned month : 4;				//月		(1-12)
		unsigned year : 7;				//年		(0-127=1980-2107)
	};
	//3d数据参数结构
	struct HEAD_3DPAR{
		short directionType;			//用来判定是哪个方向的数据(01:x,10:y,11:xy)
		short reverse;					//保留字节，显式的使结构体满足自然对齐要求
		float xDistance;				//x轴方向上的距离(单位cm)
		float yDistance;				//y轴方向上的距离(单位cm)
		float zDistance;				//z轴方向上的距离(单位cm)
		short xLines;					//平行于x轴方向上的测线数
		short yLines;					//平行于y轴方向上的测线数
		float xDistanceTwoLine;			//x方向上的相邻测线的间隔距离(单位m)
		float yDistanceTwoLine;			//y方向上的相邻测线的间隔距离(单位m)
		short xScansPerLine;			//x方向上每条测线包含的数据道数
		short yScansPerLine;			//y方向上每条测线包含的数据道数
	};
	//桩号参数结构
	struct HEAD_PEGPAR{
		int begPeg;						//开始桩号(千米)
		int begPegAdd;					//开始桩号附加部分(米)
		int endPeg;						//结束桩号(千米)
		int endPegAdd;					//结束桩号附加部分(米)
		int pegInterval;				//桩号间隔(米)
	};

	//文件头结构
	struct HEAD_FILEHEADER{
		//共 10 字节(1-10);
		short		lh_tag;				//文件标志(标记文件类型)
		short		lh_data;			//数据偏移位置	由硬件提供
		short		lh_nsamp;			//采样点数		由硬件提供
		short		lh_bits;			//数据位数		由硬件提供
		short		lh_zero;			//数据零偏		由硬件提供
		//共 20 字节(11-30);
		float		lh_sps;				//每秒扫描道数	由硬件提供
		float		lh_spm;				//每米扫描道数	由硬件提供
		float		lh_mpm;				//脉冲间隔		由硬件提供
		float		lh_pos;				//信号位置		由硬件提供
		float		lh_range;			//时窗(ns)		由硬件提供
		//共 24 字节(31-54);
		short		lh_spp;				//天线频率
		HEAD_DATE	lh_create;			//数据生成日期
		HEAD_DATE	lh_modif;			//数据修改日期
		short		lh_rgain;			//增益曲线位置
		short		lh_nrgain;			//增益曲线长度
		short		lh_text;			//解释说明位置
		short		lh_ntext;			//解释说明长度
		short		lh_proc;			//处理数据位置
		short		lh_nproc;			//处理数据长度
		short		lh_nchan;			//单双通道标志(1:单通道,2:双通道)
		//共 12 字节(55-66);
		float		lh_epsr;			//介电常数		由硬件提供
		float		lh_top;				//初始深度		由硬件提供
		float		lh_depth;			//深度范围		由软件提供
		//共 62 字节(67-128);
		short		lh_npass;			//任意
		short		lh_device;			//设备类型
		short		lh_file;			//文件类型
		short		lh_gps;				//GPS标志
		char		lh_gpsform[4];		//GPS格式
		char		lh_anten[4];		//天线名称
		char		lh_reserv1[9];		//保留字节
		char		lh_lrmanual;		//是否人为设置左标尺的刻度间隔(1,2,3:人为设置过,1:时间,2:深度,3:采样)
		char		lh_rrmanual;		//是否人为设置右标尺的刻度间隔(1,2,3:人为设置过,1:时间,2:深度,3:采样)
		float		lh_lrlong;			//左标尺长刻度间隔
		short		lh_lrshort;			//左标尺长刻度间的短刻度数
		float		lh_rrlong;			//右标尺长刻度间隔
		short		lh_rrshort;			//右标尺长刻度间的短刻度数
		short       lh_peg;				//桩号显示方式(0或其它:道号,1:标记)
		short       lh_horuler;			//上方标尺显示方式(1:道号,2:距离,3:桩号,其它:什么也不显示)
		short		lh_extent;			//标记扩展
		char		lh_work;			//工作模式(连续|点测|轮测)(0,1,2)
		short		lh_chanmask;		//任意
		char		lh_fname[12];		//文件名 软件给
		short		lh_chksum;			//任意
		//共 88 字节(129-216);
		float		lh_rgainf[22];		//增益曲线数组
		//共 732 字节(217-948);
		char		lh_reserv2[732];	//保留字节
		//共 52 字节(949-1000);
		HEAD_3DPAR	lh_3dParam;			//3D参数
		HEAD_PEGPAR	lh_pegParam;		//桩号参数
		//共 24 字节(1000-1024);
		char		lh_reserv3[24];		//保留字节
	};

}//APP
#endif// __app_web_trans_FileDef_h__