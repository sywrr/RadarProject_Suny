
/*********************************************************************************
*
* @file	lte2700Interface.cpp
* @version	1.0
* @date	2020-04-22
* @author	孙永民
*
* @brief	雷达协协议接口文件
*
*********************************************************************************/
// 版权(C) 电波传播研究所
// 改动历史
// 日期			作者	改动内容
// 2021-04-22	孙永民	创建文件
//================================================================================

#ifdef _WIN32
#define LTD_API extern "C" __declspec(dllexport)
#else
#define LTD_API extern "C" 
#endif

#include "EfsChannel.h"
#include "EfsDef.h"
using namespace DAQ;

EfsChannel *efsChannel = nullptr;
string errorString;

//1 创建对象
LTD_API 
int createInstance(int netType, const char*localIp, const char*deviceIp, int antenaType,int dllVersion)
{
#ifdef _DEBUG
	cout << "createInstance" << endl;
#endif
	errorString.clear();
	if (efsChannel == nullptr)
	{
		efsChannel = new EfsChannel();
		if (efsChannel != nullptr){
			efsChannel->CreateChannel(netType, localIp, deviceIp, antenaType,dllVersion);
			efsChannel->OpenChannel();
		   return HISENSE0;
		}
		return HISENSE2; 
	}
	return HISENSE1;
}

//2 析构对象
LTD_API
int releaseInstance()
{
#ifdef _DEBUG
	cout << "releaseInstance" << endl;
#endif
	if (efsChannel != nullptr){
		efsChannel->CloseChannel();
		delete efsChannel;
		efsChannel = nullptr;
		return HISENSE0;
	}
	return HISENSE1;
}

//3 返回错误
LTD_API
void lastErrorString(char*error, int*size)
{
#ifdef _DEBUG
	cout << "lastErrorString,errorString=" << errorString << endl;
#endif
	*size = errorString.length();
	memcpy(error, errorString.c_str(), *size);
	return;
}

//4 保存雷达参数设置
LTD_API
int saveSetting(char*settingChar, const int size, bool isOnline)
{
#ifdef _DEBUG
	cout << "saveSetting" << endl;
#endif
	CmdMsg cmdMsg;
	cmdMsg.isOnLine = isOnline;
	cmdMsg.msgType = SETTING;
	cmdMsg.strMsg = string(settingChar, size);
	return efsChannel->ExecCommond(cmdMsg, errorString);
}

//5 开启雷达
LTD_API
int start()
{
#ifdef _DEBUG
	cout << "start" << endl;
#endif
	CmdMsg cmdMsg;
	cmdMsg.msgType = START;
	return efsChannel->ExecCommond(cmdMsg, errorString);
}

//6 关闭雷达
LTD_API
int stop()
{
#ifdef _DEBUG
	cout << "stop" << endl;
#endif
	CmdMsg cmdMsg;
	cmdMsg.msgType = STOP;
	return efsChannel->ExecCommond(cmdMsg, errorString);
}

//7 查询设备运行状态
LTD_API
int runningStatus(bool*isRunning)
{
#ifdef _DEBUG
	cout << "runningStatus" << endl;
#endif
	return efsChannel->runningStatus(isRunning);
}

//8 接收扫描数据
LTD_API
int receivedData(char*data, int *step, int *size)
{
	return efsChannel->receivedData(data,step,size);
}

//9 保存数据
LTD_API
int beginSaveData(const char*fileDir,void *user)
{
#ifdef _DEBUG
	cout << "beginSaveData" << endl;
#endif
	return efsChannel->beginSaveData(fileDir, user);
}

//10 停止保存数据
LTD_API
int endSaveData()
{
#ifdef _DEBUG
	cout << "endSaveData" << endl;
#endif
	return efsChannel->endSaveData();
}


//11 整机配置
LTD_API
int lowerComputerConfig(const char*ip, short devType, const char* cardSerialNum, int serialNum,
short deviceSerialNum, short versionNum, short calibrationValue, short antennaCode, short frqValue)
{
#ifdef _DEBUG
	cout << "lowerComputerConfig" << endl;
#endif
	string serial(cardSerialNum, serialNum);
	return efsChannel->lowerComputerConfig(ip, devType, serial, deviceSerialNum, versionNum, calibrationValue,antennaCode,frqValue);
}

//12 获取GPS数据
LTD_API
int receivedGpsData(double * longitude, short* longdirection, 
	double * latitude, short* latdirection, short *status)
{
	return efsChannel->receivedGpsData(longitude,
		longdirection, latitude, latdirection, status);
}

//13 获取搜救结果数据
LTD_API
int receivedRescueResult(bool *isEnd, short *resultType, 
	short *distance, short *detectionBegin, short*detecetionEnd)
{
	return efsChannel->receivedRescueResult(isEnd,resultType,
		distance,detectionBegin,detecetionEnd);
}