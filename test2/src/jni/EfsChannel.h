/****************************************************************************************
*
* @file    EfsChannel.h
* @version 1.0
* @date    2021-04-25
* @author  孙永民
*
* @brief   雷达通道类头文件
*
***************************************************************************************/
// 版权(C) 2009 - 2012 电波传播研究所
// 改动历史
// 日期         作者     改动内容
// 2021-04-25   孙永民   创建文件
//==============================================================================

#ifndef _EfsChannel_H_
#define _EfsChannel_H_

#include "RadarDevice.h"
#include "ThreadManager.h"

using namespace std;

namespace DAQ
{
	class EfsChannel
	{
	public:
		EfsChannel();
		~EfsChannel();
		//创建通道
		void CreateChannel(int netWork, string localIp, 
			string deviceIp, int antenaType, int dllVersion);
		//开启通道
		void OpenChannel();
		//关闭通道
		void CloseChannel();
		//执行命令
		bool ExecCommond(CmdMsg cmdMsg, string &errorString);
		//获取数据
		int receivedData(char*data, int *step, int*size);
		//保存数据
		int beginSaveData(string fileDir, void *user);
		//停止保存数据
		int endSaveData();
		//整机配置
		int lowerComputerConfig(string ip, short devType, string cardSerialNum,
			short deviceSerialNum, short versionNum, short calibrationValue, short antennaCode, short frqValue);
		//获取GPS数据
		int receivedGpsData(double * longitude, short* longdirection, 
			double * latitude, short* latdirection, short *status);
		///获取搜救结果数据
		int receivedRescueResult(bool *isEnd, short *resultType,
			short *distance, short *detectionBegin, short*detecetionEnd);
		//运行状态
		int runningStatus(bool*isRunning);
		//扫描
		static void onScan(void *pThis);
	private:
		RadarDevice		    *radarDevice;    //设备实例指针对象
		CThreadManager		m_threadScan;    //线程管理类对象
	};//EfsChannel
}//APP
#endif//_EfsChannel_H_