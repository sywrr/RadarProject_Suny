/****************************************************************************************
*
* @file    EfsChannel.h
* @version 1.0
* @date    2021-04-25
* @author  ������
*
* @brief   �״�ͨ����ͷ�ļ�
*
***************************************************************************************/
// ��Ȩ(C) 2009 - 2012 �粨�����о���
// �Ķ���ʷ
// ����         ����     �Ķ�����
// 2021-04-25   ������   �����ļ�
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
		//����ͨ��
		void CreateChannel(int netWork, string localIp, 
			string deviceIp, int antenaType, int dllVersion);
		//����ͨ��
		void OpenChannel();
		//�ر�ͨ��
		void CloseChannel();
		//ִ������
		bool ExecCommond(CmdMsg cmdMsg, string &errorString);
		//��ȡ����
		int receivedData(char*data, int *step, int*size);
		//��������
		int beginSaveData(string fileDir, void *user);
		//ֹͣ��������
		int endSaveData();
		//��������
		int lowerComputerConfig(string ip, short devType, string cardSerialNum,
			short deviceSerialNum, short versionNum, short calibrationValue, short antennaCode, short frqValue);
		//��ȡGPS����
		int receivedGpsData(double * longitude, short* longdirection, 
			double * latitude, short* latdirection, short *status);
		///��ȡ�ѾȽ������
		int receivedRescueResult(bool *isEnd, short *resultType,
			short *distance, short *detectionBegin, short*detecetionEnd);
		//����״̬
		int runningStatus(bool*isRunning);
		//ɨ��
		static void onScan(void *pThis);
	private:
		RadarDevice		    *radarDevice;    //�豸ʵ��ָ�����
		CThreadManager		m_threadScan;    //�̹߳��������
	};//EfsChannel
}//APP
#endif//_EfsChannel_H_