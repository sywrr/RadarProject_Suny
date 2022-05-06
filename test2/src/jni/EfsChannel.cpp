
#include "EfsChannel.h"

using namespace DAQ;

//构造函数
EfsChannel::EfsChannel()
{
	radarDevice = nullptr;
}

//析构函数
EfsChannel::~EfsChannel()
{
	if (radarDevice)
		delete radarDevice;
}

//创建通道
void EfsChannel::CreateChannel(int netWork, string localIp, string deviceIp, int antenaType, int dllVersion)
{
	radarDevice = new RadarDevice(netWork, localIp, deviceIp, antenaType, dllVersion);
}

//开启通道
void EfsChannel::OpenChannel()
{
	string strThread = "扫描线程";
	m_threadScan.SetRunInterval(10);
	m_threadScan.Start(radarDevice, (void *)&onScan, strThread.c_str());
}

//关闭通道
void EfsChannel::CloseChannel()
{
	m_threadScan.Stop();
}

//执行命令
bool EfsChannel::ExecCommond(CmdMsg cmdMsg, string &errorString)
{
	return radarDevice->ExecCommond(cmdMsg, errorString);
}

//运行状态
int EfsChannel::runningStatus(bool*isRunning)
{
	return radarDevice->runningStatus(isRunning);
}

//获取数据
int EfsChannel::receivedData(char*data, int *step, int*size)
{
	return radarDevice->receivedData(data,step,size);
}

//保存数据
int EfsChannel::beginSaveData(string fileDir, void *user)
{
	return radarDevice->beginSaveData(fileDir, user);
}

//停止保存数据
int EfsChannel::endSaveData()
{
	return radarDevice->endSaveData();
}

//整机配置
int EfsChannel::lowerComputerConfig(string ip, short devType, string cardSerialNum,
	short deviceSerialNum, short versionNum, short calibrationValue, short antennaCode, short frqValue)
{
	return radarDevice->lowerComputerConfig(ip, devType, cardSerialNum,
		deviceSerialNum, versionNum, calibrationValue,antennaCode,frqValue);
}

//获取GPS数据
int EfsChannel::receivedGpsData(double * longitude, short* longdirection, 
	double * latitude, short* latdirection, short *status)
{
	return radarDevice->receivedGpsData(longitude, longdirection, 
		latitude, latdirection, status);
}

///获取搜救结果数据
int EfsChannel::receivedRescueResult(bool *isEnd, short *resultType,
	short *distance, short *detectionBegin, short*detecetionEnd)
{
	return radarDevice->receivedRescueResult(isEnd, resultType,
		distance, detectionBegin, detecetionEnd);
}

//执行扫描
void EfsChannel::onScan(void *pThis)
{
	((RadarDevice*)pThis)->onScan();
}