
#include "EfsChannel.h"

using namespace DAQ;

//���캯��
EfsChannel::EfsChannel()
{
	radarDevice = nullptr;
}

//��������
EfsChannel::~EfsChannel()
{
	if (radarDevice)
		delete radarDevice;
}

//����ͨ��
void EfsChannel::CreateChannel(int netWork, string localIp, string deviceIp, int antenaType, int dllVersion)
{
	radarDevice = new RadarDevice(netWork, localIp, deviceIp, antenaType, dllVersion);
}

//����ͨ��
void EfsChannel::OpenChannel()
{
	string strThread = "ɨ���߳�";
	m_threadScan.SetRunInterval(10);
	m_threadScan.Start(radarDevice, (void *)&onScan, strThread.c_str());
}

//�ر�ͨ��
void EfsChannel::CloseChannel()
{
	m_threadScan.Stop();
}

//ִ������
bool EfsChannel::ExecCommond(CmdMsg cmdMsg, string &errorString)
{
	return radarDevice->ExecCommond(cmdMsg, errorString);
}

//����״̬
int EfsChannel::runningStatus(bool*isRunning)
{
	return radarDevice->runningStatus(isRunning);
}

//��ȡ����
int EfsChannel::receivedData(char*data, int *step, int*size)
{
	return radarDevice->receivedData(data,step,size);
}

//��������
int EfsChannel::beginSaveData(string fileDir, void *user)
{
	return radarDevice->beginSaveData(fileDir, user);
}

//ֹͣ��������
int EfsChannel::endSaveData()
{
	return radarDevice->endSaveData();
}

//��������
int EfsChannel::lowerComputerConfig(string ip, short devType, string cardSerialNum,
	short deviceSerialNum, short versionNum, short calibrationValue, short antennaCode, short frqValue)
{
	return radarDevice->lowerComputerConfig(ip, devType, cardSerialNum,
		deviceSerialNum, versionNum, calibrationValue,antennaCode,frqValue);
}

//��ȡGPS����
int EfsChannel::receivedGpsData(double * longitude, short* longdirection, 
	double * latitude, short* latdirection, short *status)
{
	return radarDevice->receivedGpsData(longitude, longdirection, 
		latitude, latdirection, status);
}

///��ȡ�ѾȽ������
int EfsChannel::receivedRescueResult(bool *isEnd, short *resultType,
	short *distance, short *detectionBegin, short*detecetionEnd)
{
	return radarDevice->receivedRescueResult(isEnd, resultType,
		distance, detectionBegin, detecetionEnd);
}

//ִ��ɨ��
void EfsChannel::onScan(void *pThis)
{
	((RadarDevice*)pThis)->onScan();
}