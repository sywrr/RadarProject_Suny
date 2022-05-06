
#ifndef _DllApp_H_
#define _DllApp_H_

#include "DllFile.h"
#include "iostream"

using namespace std;

namespace DAQ
{
	class DllApp
	{
	public:
		DllApp(string dllDir);
		~DllApp();
		/**
		* @brief 系统初始化
		@return 成功返回true，失败返回false
		*/
		bool Init();

		//创建设备实例
		typedef int (createInstance)(int netWork, const char *localIP, 
			const char *deviceIP, int antenaType,int dllVersion);
		createInstance *m_createInstance;

		//析构设备实例
		typedef int (releaseInstance)();
		releaseInstance *m_releaseInstance;

		//保存雷达参数设置
		typedef int (saveSetting)(char*, const int, bool);
		saveSetting *m_saveSetting;

		//保存雷达参数设置
		typedef int (RadarStart)();
		RadarStart *m_radarStart;


		//保存数据
		typedef int runningStatus(bool*isRunning);
		runningStatus *m_runningStatus;

		//结束
		typedef int (RadarStop)();
		RadarStop *m_radarStop;

		//获取数据
		typedef int (receivedData)(char*, int*, int*);
		receivedData *m_receivedData;

		//保存数据
		typedef int  beginSaveData(const char *fileDir, void*userParam);
		beginSaveData *m_beginSaveData;

		//结束保存数据
		typedef int  endSaveData();
		endSaveData *m_endSaveData;

		//整机配置
		typedef int lowerComputerConfig(const char*ip, short devType, const char* cardSerialNum,
			short deviceSerialNum, short versionNum, short calibrationValue);
		lowerComputerConfig *m_lowerComputerConfig;

	    //获取GPS数据
		typedef int receivedGpsData(double * longitude, short* longdirection,
			double * latitude, short* latdirection, short *status);
		receivedGpsData *m_receivedGpsData;

		//获取搜救结果数据
		typedef int receivedRescueResult(bool *isEnd, short *resultType, short *distance, 
			short *detectionBegin, short*detecetionEnd);
		receivedRescueResult *m_receivedRescueResult;

	private:
		CDllFile *m_ptrCDllFile;
		string m_dllDir;
		int data_size;
		int data_step;
		char data[500 * 1024 * 4]; 
	};
} //DAQ
#endif //_DllApp_H_