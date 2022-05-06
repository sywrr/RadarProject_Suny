
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
		* @brief ϵͳ��ʼ��
		@return �ɹ�����true��ʧ�ܷ���false
		*/
		bool Init();

		//�����豸ʵ��
		typedef int (createInstance)(int netWork, const char *localIP, 
			const char *deviceIP, int antenaType,int dllVersion);
		createInstance *m_createInstance;

		//�����豸ʵ��
		typedef int (releaseInstance)();
		releaseInstance *m_releaseInstance;

		//�����״��������
		typedef int (saveSetting)(char*, const int, bool);
		saveSetting *m_saveSetting;

		//�����״��������
		typedef int (RadarStart)();
		RadarStart *m_radarStart;


		//��������
		typedef int runningStatus(bool*isRunning);
		runningStatus *m_runningStatus;

		//����
		typedef int (RadarStop)();
		RadarStop *m_radarStop;

		//��ȡ����
		typedef int (receivedData)(char*, int*, int*);
		receivedData *m_receivedData;

		//��������
		typedef int  beginSaveData(const char *fileDir, void*userParam);
		beginSaveData *m_beginSaveData;

		//������������
		typedef int  endSaveData();
		endSaveData *m_endSaveData;

		//��������
		typedef int lowerComputerConfig(const char*ip, short devType, const char* cardSerialNum,
			short deviceSerialNum, short versionNum, short calibrationValue);
		lowerComputerConfig *m_lowerComputerConfig;

	    //��ȡGPS����
		typedef int receivedGpsData(double * longitude, short* longdirection,
			double * latitude, short* latdirection, short *status);
		receivedGpsData *m_receivedGpsData;

		//��ȡ�ѾȽ������
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