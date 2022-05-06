/****************************************************************************************
*
* @file    RadarDevice.h
* @version 1.0
* @date    2021-04-25
* @author  孙永民
*
* @brief   雷达设备处理类头文件
*
***************************************************************************************/
// 版权(C) 2009 - 2012 电波传播研究所
// 改动历史
// 日期         作者     改动内容
// 2021-04-25   孙永民   创建文件
//==============================================================================

#ifndef __RadarDevice_H__
#define __RadarDevice_H__

#include "EfsDef.h"
#include "RadarQuery.h"
#include "mutex"
#include "map"
#include "queue"
#include "PortUdp.h"

//sunyongmin 2021.12.27 begin
#include "Classify_breath.h"
//sunyongmin 2021.12.27 end

#include "IPortCallBack.h"
#include "PortTCPClient.h"
#ifndef _WIN32
#include <unistd.h>
#define Sleep usleep
#endif
using namespace std;

namespace DAQ
{
	class RadarDevice: public IPortCallBack
	{
	public:
		RadarDevice(int netWork, string localIp, string deviceIp, int antenaType, int dllVersion);
		~RadarDevice();

		//扫描处理
		void onScan();
		//打开设备
		bool OpenDevice();
		//关闭设备
		bool CloseDevice();
		//执行命令
		bool ExecCommond(CmdMsg cmdMsg, string &errorString);
		//重写回调函数
		virtual void OnReceiveData(char *szBuffer, int nRealReads);
		//命令解析处理
		int HandleSaveSetting(string strMsg, bool isOnLine, string &errorString);
		int HandleStart(string &errorString);
		int HandleStop(string &errorString);
		int HandleRunningStatus();
		//处理命令参数
		int handleSamplingPoints(int value, bool isOnline);
		int handleSignalPosition(int value, bool isOnline);
		int handleTimeWindow(int value, bool isOnline);
		int handleSignalGain(int value, bool isOnline);
		int handleFilterEnabled(int value, bool isOnline);
		int handleScanSpeed(int value, bool isOnline);
		//2700协议实现
		void sendTimeParam();
		void sendGainParam();
		bool sendSystemParam();
		void sendTriggerMode();
		void sendSelfChecking();
		//void sendCheckModel();
		void sendCheckVersion();
		bool sendSelfCheck();
		bool sendLightLevel();
		void sendCheckSerialNumber();
		void sendMarkExtended();
		void sendFilterParam();
		void sendStop();
		void sendStart();
		//获取数据
		int receivedData(char*data, int *step, int*size);
		//保存数据
		int beginSaveData(string fileDir, void *user);
		//停止保存数据
		int endSaveData();
		//整机配置
		int lowerComputerConfig(string ip, short devType, string cardSerialNum,
			short deviceSerialNum, short versionNum, short calibrationValue, short antennaCode, short frqValue);
		//数据保存
		void dataSave(string data, int frameNum);
		//获取GPS数据
		int receivedGpsData(double * longitude, short* longdirection, 
			double * latitude, short* latdirection, short *status);
		///获取搜救结果数据
		int receivedRescueResult(bool *isEnd, short *resultType,
			short *distance, short *detectionBegin, short*detecetionEnd);
		//数据处理
		void RealDataProcess();
		//扫描数据处理
		void ScanDataProcess();
		//运行状态
		int runningStatus(bool*isRunning);
		//数据预处理
		int dataPreprocess(string &sampleData);
		//背景消除
		void removeBack(string &sampleData);
		//软件增益
		void softSegmentedGain(string &sampleData);
		//搜救处理
		void rescueProcsss(string &sampleData);
	private:	
		//时序
		int m_scanSpeed; //扫速
		int m_timeWindow; //时窗
		int m_stepLenCalibration;  //步进长度校准
		int m_calibration;  //时序校准值
		int m_fplulsefreqcount;   //FPULSE频率计数值
		int m_samplingPoints;   //采样点
		int m_signalPosition;  //信号位置
		int m_signalPosInit;  //搜救初始信号位置
		int m_EfsChannelmodel;   //通道模式
		int m_EfsChannelnum;   //通道号
		int m_antennaRepetition;  //重频
		int m_lightLevel; //亮灯等级
		int m_dllVersion;  //动态库独有业务标识
		int m_fristTime; //初始化探测
		//参数
		SystemParam systemParam;   //系统参数
		TimeParam timeParam;	//时序参数
		GainParam gainParam;	//增益参数
		TriggerModeParam triggerModeParam;	//模式参数
		//算法
		int m_filterEnabled;   //是否启用滤波
		int m_bRemoveBack;  //是否背景消除
		int m_antenaType;   //天线中心频率
		int m_correctZero; //是否校正零偏
		int m_automaticGain; //是否自动增益
		int m_isGainEnd;//是否已经计算完毕
		//其他
		vector<int> m_vecMark;  //是否打标
		std::mutex m_markMutex; //打标锁
		IPort *m_iPort;
		int m_maxPocketNum;  //一道数据最大分包数
		RadarQuery *m_radarQuery;  //报文转换类指针
		char szSend[256];  //发送
		string m_sampleData;  //雷达单道数据
		std::mutex m_Mutex;  //数据接收切换处理锁
		map<bool, queue<string>> m_SiProcessBuf;  //实时数据处理
		std::mutex m_qMutex;  //雷达数据处理锁
		queue<string> m_radarDataQueue;
		queue<string> m_radarScan;
		bool m_SIProcess;  //切换状态位
		bool m_dataSave;  //数据是否保存
		map<int, vector<int>>m_antennaParams;   //天线参数
		FILE *m_file;  //文件指针
		FILE *m_gpsFile;  //GPS文件指针
		int  *m_user; //用户参数
		bool m_isStart; //是否开始
		int m_linkedSize; //自检次数
		float m_longitude, m_latitude;  //经纬度
		GspInfo m_gspInfo; //GPS信息
		//搜救相关
		Classify_breath *m_classifyBreath;  //搜救处理
		std::vector<TargetInfo> m_queueTarget; //结果计算队列
		std::mutex m_targetMutex;
		std::vector<TargetInfo> m_queueResultTarget; //结果队列
		bool m_isExistTarget; //是否存在目标
		int m_targetSize; //是否存在目标次数
		short m_distance; //探测距离
		short m_deteInterval; //探测间隔
		short m_detectionPos; //探测位置
		short m_detectionPosInit; //探测位置
		bool m_isDetcEnd; //是否探测结束
		bool m_isJump; //跳转标识
		int m_lastTime;//探测历史时间
		int m_detectionTime; //探测时间
	};//RadarDevice
}//APP
#endif//__RadarDevice_H__