/****************************************************************************************
*
* @file    EfsDef.h
* @version 1.0
* @date    2021-04-30
* @author  孙永民
*
* @brief   公共类型定义头文件
*
***************************************************************************************/
// 版权(C) 2009 - 2021 电波传播研究所
// 改动历史
// 日期         作者     改动内容
// 2021-04-30   孙永民   创建文件
//==============================================================================
#ifndef __RadarDef_H__
#define __RadarDef_H__

#include "iostream"
#include "vector"

using namespace std;

namespace DAQ
{
	//雷达参数
	struct CmdMsg{
		int msgType;
		string strMsg;  //命令消息
		bool isOnLine;
	};

	//帧格式
	#define SETTING		0x8001  //参数设置
	#define START		0x8002  //开启
	#define STOP		0x8003  //关闭
	#define RUNNING		0x8004  //运行状态

	//帧格式
	#define FRAME_HEAD  ((short)0x07ff)  //帧头
	#define FRAME_END   ((short)0x5555)  //帧尾
	#define FRAME_LEN   556  //雷达数据分帧长度

	//错误码
	#define HISENSE_1  -1 
	#define HISENSE0   0 
	#define HISENSE1   1
	#define HISENSE2   2

	//参数名称
	const string samplingPoints = "samplingPoints";
	const string signalPosition = "signalPosition";
	const string timeWindow = "timeWindow";
	const string signalGain = "signalGain";
	const string filterEnabled = "filterEnabled";
	const string RemoveBack = "removeBack";
	const string scanSpeed = "scanSpeed";
	const string mark = "mark";
	const string lightLevel = "lightLevel";
	const string correctZero = "correctZero";
	const string automaticGain = "automaticGain";
	const string gainSplit = "gainSplit";
	const string workMode = "workMode";
	const string extentMark = "extentMark";
	const string pluseInterval = "pluseInterval";
	const string gpsEnabled = "gpsEnabled";
	const string detectionPos = "detectionPos";

	///动态库独有业务标识
	#define PRO_DLL_WATER  1	//水下项目
	#define PRO_DLL_RESCUE 2	//搜救项目
	#define PRO_DLL_CONDUIT	3	//管道
	#define PRO_DLL_CONFIG	4	//整机配置

	//雷达参数
	struct RadarParam{
		string strKey;
		int Value;
		bool isUpdate;
		bool isOnline;
	};

//端口
#if 1
	const int devicePort = 8081;
	const int localPort = 8080;
#else
	const int devicePort = 9001;
	const int localPort = 9002; 
#endif

	//系统参数
	struct SystemParam{
		int gps;
		int protocal;
		int enery;
		string ip;
		int port;
	};

	//时序参数
	struct TimeParam{
		int EfsChannelmodel; //通道选择 11-双， 01-CH1, 10-CH2
		int EfsChannelnum; //通道号
		int steplen; //步进长度
		int stepinterval; //步进间隔
		int cyclesteplen; //步进长度2B
		int fplulsefreqcount; //FPULSE 频率计数值
		int samplingclockfrequency; //采样时钟计数值
		int counterbegin; //时窗起点对应的计数器初值
		int delaybegin; //时窗起点对应的延时芯片初值
		int counterend; //时窗终点对应的计数器终
		int samplingpoints; //每道的采样点数
		int scanSpeed; //扫速
	};

	//增益参数
	struct GainParam{
		int automaticGain; //自动增益
		int isGainEnd; //自动增益控制标识
		int segmentedGainMax;//软硬件分割
		int EfsChannelnum; //通道号
		int segmentNum; //段数
		int segmentedGain[9]; //硬件增益值
		int segmentedGainSoft[9]; //软件增益值
	};

	//滤波参数
	struct FilterParam{
		int filterEnable; //滤波使能
		int EfsChannelnum; //通道号
		int indexValue; //滤波器型号
		char filter[51 * 2]; //滤波参数
	};

	///测距轮模式
	enum TriggerMode{//触发模式
		TriggerMode_Timeout=1,//连续
		TriggerMode_Raster,//测距轮
		TriggerMode_Repeat//点测模式
	};

	//触发模式参数
	struct TriggerModeParam{
		int triggerMode; //工作模式 =1 连续，=2 轮测，=3 点测
		float pluseInterval; //测距轮取样间隔
		int extentMark;  //标记扩展
	};

#define _DATABUFLEN 1024*4*500

	enum Cmd{//指令1
		Cmd_Unknow,
		Cmd_SendSelfChecking = (short)0xAA10,//上位机发送下位机自检
		Cmd_ReceiveSelfChecking = (short)0xAA10,//下位机应答自检
		Cmd_SendCheckModel = 0xAA51,//查询设备型号
		Cmd_ReceiveCheckModel = 0xAA51,//位机应答查询设备型号
		Cmd_SendCheckVersion = 0xAA52,//查询设备版本号
		Cmd_ReceiveCheckVersion = (short)0xAA52,//位机应答查询设备版本号
		Cmd_SendCheckSerialNumber = 0xAA53,//查询设备序列号
		Cmd_ReceiveCheckSerialNumber = 0xAA53,//位机应答查询设备序列号
		Cmd_ReceiveResponse = 0xDD02,//下位机应答指令
		Cmd_ReceiveSample = (short)0xDD01,//下位机雷达数据指令
		Cmd_SendSetting = 0x03,//上位机发送下位机配置
		Cmd_ReceiveSetting = 0x04,//下位机应答配置
		Cmd_SendStart = 0xAA00,//上位机发送下位机开始运行
		Cmd_ReceiveStart = 0xAA00,//下位机应答开始运行
		Cmd_SendSystemParam = (short)0xDD11,//上位机发送系统参数设置指令
		Cmd_ReceiveSystemParam = (short)0xDD11,//下位机应答系统参数设置指令
		Cmd_SendMarkExtended = 0xBB30,//上位机发送标记扩展设置指令
		Cmd_ReceiveMarkExtended = 0xBB30,//下位机应答标记扩展设置指令
		Cmd_SendTriggerMode = 0xBB31,//上位机发送探测模式设置指令
		Cmd_ReceiveTriggerMode = (short)0xBB31,//下位机应答探测模式设置指令
		Cmd_SendTimeParam = 0xCC52,//上位机发送时序参数设置指令
		Cmd_ReceiveTimeParam = (short)0xCC52,//下位机应答时序参数设置指令
		Cmd_SendGainParam = 0xDD22,//上位机发送增益参数设置指令
		Cmd_ReceiveGainParam = (short)0xDD22,//下位机应答增益参数设置指令
		Cmd_SendFilterParam = 0xDD23,//上位机发送滤波参数设置指令
		Cmd_ReceiveFilterParam = (short)0xDD23,//下位机应答滤波参数设置指令
		Cmd_SendLightLevel = 0xDD24,//上位机发送亮灯等级设置指令
		Cmd_ReceiveLightLevel = (short)0xDD24,//下位机应答亮灯等级设置指令
		Cmd_SendComputerConfig = (short)0xCC70,//上位机发送整机配置
		Cmd_ReceiveComputerConfig = (short)0xCC70,//上位机接收整机配置
		Cmd_ReceiveData = 0x06,//下位机 子命令0应答运行 1应答数据
		Cmd_SendPause = 0xAA01,//上位机发送下位机暂停
		Cmd_ReceivePause = 0xAA01,//下位机应答暂停
		Cmd_SendStop = 0xAA01,//上位机发送下位机停止
		Cmd_ReceiveStop = 0xAA01,//下位机应答停止
		Cmd_SendHeart = 0xAA12,//上位机发送下位机心跳
		Cmd_ReceiveHeart = 0xAA12,//下位机应答心跳
		Cmd_SendQureyGps = 0x0B,//上位机查询下位机GPS信息
		Cmd_ReceiveGps = 0x0C,//下位机应答GPS信息
		Cmd_SendQureyStat = 0x0D,//上位机查询下位机状态
		Cmd_ReceiveStat = 0x0E,//下位机应答状态
		Cmd_SendQureyFailure = 0xE9,//上位机查询下位机故障代码
		Cmd_ReceiveFailure = 0xEA//下位机应答故障
	};

	struct GspInfo{
		int mflag; //数据头标识
		short gps_state; //定位状态
		int scanIndex;//道号
		double longitude;//经度
		short wE;//东西
		double latitude;//纬度
		short sN;//南北
		int year;
		short mon;
		short day;
		short hour;
		short min;
		short sec;
		short sateNum; //卫星个数
		short hasData; //是否有数据
		short gpsEnabled; //gps是否使能
	};

	//搜救结果信息
	#define RESCUE_QUEUE_SIZE   5   //搜救队列长度
	#define RESCUE_TARGET_SIZE  3   //结果次数
	struct TargetInfo{
		bool isExist; //是否存在目标
		short targetPos; //目标位置
		int detectionPos; //探测位置
		bool isDetectionEnd; //是否探测结束
		short detectionBegin; //探测开始范围
		short detectionInterval; //探测间隔
		bool isExistResult;   //是否存在最终结果
		bool isJump; //是否跳跃
	};

	//将字节数据组转换为float数组
	inline int byteToFloat(string strData, float* dataIn)
	{
		int size = strData.length();
		for (int j = 0, column = 0; j<size / 4; j++, column += 4)
		{
			long long  value = 0;
			bool isNegativeNumber = false;
			unsigned char  numberChar;
			for (int i = 3, leftScroll = 24; i >= 0; i--, leftScroll -= 8)//小端方式读取
			{
				numberChar = (unsigned char)(strData.at(column + i));
				if (i == 3){
					isNegativeNumber = numberChar & 0x80;
					if (isNegativeNumber){
						value += ((numberChar - 0x100) << (leftScroll));
					}
					else
						value += (numberChar << (leftScroll));
				}
				else{
					value += (numberChar << (leftScroll));
				}
			}
			*dataIn++ = value;
		}
		return size / 4;
	}

	//将字节数据组转换为int数组
	inline int transformToInt(string strData, int* dataIn)
	{
		int size = strData.length();
		for (int j = 0, column = 0; j<size / 4; j++, column += 4)
		{
			int value = 0;
			bool isNegativeNumber = false;
			unsigned char numberChar;
			for (int i = 3, leftScroll = 24; i >= 0; i--, leftScroll -= 8)//小端方式读取
			{
				numberChar = (unsigned char)(strData.at(column + i));
				if (i == 3){
					isNegativeNumber = numberChar & 0x80;
					if (isNegativeNumber){
						value += ((numberChar - 0x100) << (leftScroll));
					}
					else
						value += (numberChar << (leftScroll));
				}
				else{
					value += (numberChar << (leftScroll));
				}
			}
			*dataIn++ = value;
		}
		return size / 4;
	}


	//将字节数据组转换为short数组
	inline int transformToShort(string strData, short* dataIn)
	{
		int size = strData.length();
		for (int j = 0, column = 0; j<size / 4; j++, column += 4)
		{
			int value = 0;
			bool isNegativeNumber = false;
			unsigned char numberChar;
			for (int i = 3, leftScroll = 24; i >= 0; i--, leftScroll -= 8)//小端方式读取
			{
				numberChar = (unsigned char)(strData.at(column + i));
				if (i == 3){
					isNegativeNumber = numberChar & 0x80;
					if (isNegativeNumber){
						value += ((numberChar - 0x100) << (leftScroll));
					}
					else
						value += (numberChar << (leftScroll));
				}
				else{
					value += (numberChar << (leftScroll));
				}
			}
			*dataIn++ = (float)value/pow(2,24)*pow(2,16);
		}
		return size / 4;
	}

	//字符串分割
	inline vector<string> split(const string& str, const string &delim)
	{
		vector<string> res;
		if ("" == str) return res;
		char *strs = new char[str.length() + 1];
		strcpy(strs, str.c_str());
		char *d = new char[delim.length() + 1];
		strcpy(d, delim.c_str());
		char * p = strtok(strs, d);
		while (p)
		{
			string s = p;
			res.push_back(s);
			p = strtok(NULL, d);
		}
		return res;
	}
	
}//APP
#endif//__RadarAppDef_H__