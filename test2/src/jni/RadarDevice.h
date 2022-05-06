/****************************************************************************************
*
* @file    RadarDevice.h
* @version 1.0
* @date    2021-04-25
* @author  ������
*
* @brief   �״��豸������ͷ�ļ�
*
***************************************************************************************/
// ��Ȩ(C) 2009 - 2012 �粨�����о���
// �Ķ���ʷ
// ����         ����     �Ķ�����
// 2021-04-25   ������   �����ļ�
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

		//ɨ�账��
		void onScan();
		//���豸
		bool OpenDevice();
		//�ر��豸
		bool CloseDevice();
		//ִ������
		bool ExecCommond(CmdMsg cmdMsg, string &errorString);
		//��д�ص�����
		virtual void OnReceiveData(char *szBuffer, int nRealReads);
		//�����������
		int HandleSaveSetting(string strMsg, bool isOnLine, string &errorString);
		int HandleStart(string &errorString);
		int HandleStop(string &errorString);
		int HandleRunningStatus();
		//�����������
		int handleSamplingPoints(int value, bool isOnline);
		int handleSignalPosition(int value, bool isOnline);
		int handleTimeWindow(int value, bool isOnline);
		int handleSignalGain(int value, bool isOnline);
		int handleFilterEnabled(int value, bool isOnline);
		int handleScanSpeed(int value, bool isOnline);
		//2700Э��ʵ��
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
		//��ȡ����
		int receivedData(char*data, int *step, int*size);
		//��������
		int beginSaveData(string fileDir, void *user);
		//ֹͣ��������
		int endSaveData();
		//��������
		int lowerComputerConfig(string ip, short devType, string cardSerialNum,
			short deviceSerialNum, short versionNum, short calibrationValue, short antennaCode, short frqValue);
		//���ݱ���
		void dataSave(string data, int frameNum);
		//��ȡGPS����
		int receivedGpsData(double * longitude, short* longdirection, 
			double * latitude, short* latdirection, short *status);
		///��ȡ�ѾȽ������
		int receivedRescueResult(bool *isEnd, short *resultType,
			short *distance, short *detectionBegin, short*detecetionEnd);
		//���ݴ���
		void RealDataProcess();
		//ɨ�����ݴ���
		void ScanDataProcess();
		//����״̬
		int runningStatus(bool*isRunning);
		//����Ԥ����
		int dataPreprocess(string &sampleData);
		//��������
		void removeBack(string &sampleData);
		//�������
		void softSegmentedGain(string &sampleData);
		//�Ѿȴ���
		void rescueProcsss(string &sampleData);
	private:	
		//ʱ��
		int m_scanSpeed; //ɨ��
		int m_timeWindow; //ʱ��
		int m_stepLenCalibration;  //��������У׼
		int m_calibration;  //ʱ��У׼ֵ
		int m_fplulsefreqcount;   //FPULSEƵ�ʼ���ֵ
		int m_samplingPoints;   //������
		int m_signalPosition;  //�ź�λ��
		int m_signalPosInit;  //�Ѿȳ�ʼ�ź�λ��
		int m_EfsChannelmodel;   //ͨ��ģʽ
		int m_EfsChannelnum;   //ͨ����
		int m_antennaRepetition;  //��Ƶ
		int m_lightLevel; //���Ƶȼ�
		int m_dllVersion;  //��̬�����ҵ���ʶ
		int m_fristTime; //��ʼ��̽��
		//����
		SystemParam systemParam;   //ϵͳ����
		TimeParam timeParam;	//ʱ�����
		GainParam gainParam;	//�������
		TriggerModeParam triggerModeParam;	//ģʽ����
		//�㷨
		int m_filterEnabled;   //�Ƿ������˲�
		int m_bRemoveBack;  //�Ƿ񱳾�����
		int m_antenaType;   //��������Ƶ��
		int m_correctZero; //�Ƿ�У����ƫ
		int m_automaticGain; //�Ƿ��Զ�����
		int m_isGainEnd;//�Ƿ��Ѿ��������
		//����
		vector<int> m_vecMark;  //�Ƿ���
		std::mutex m_markMutex; //�����
		IPort *m_iPort;
		int m_maxPocketNum;  //һ���������ְ���
		RadarQuery *m_radarQuery;  //����ת����ָ��
		char szSend[256];  //����
		string m_sampleData;  //�״ﵥ������
		std::mutex m_Mutex;  //���ݽ����л�������
		map<bool, queue<string>> m_SiProcessBuf;  //ʵʱ���ݴ���
		std::mutex m_qMutex;  //�״����ݴ�����
		queue<string> m_radarDataQueue;
		queue<string> m_radarScan;
		bool m_SIProcess;  //�л�״̬λ
		bool m_dataSave;  //�����Ƿ񱣴�
		map<int, vector<int>>m_antennaParams;   //���߲���
		FILE *m_file;  //�ļ�ָ��
		FILE *m_gpsFile;  //GPS�ļ�ָ��
		int  *m_user; //�û�����
		bool m_isStart; //�Ƿ�ʼ
		int m_linkedSize; //�Լ����
		float m_longitude, m_latitude;  //��γ��
		GspInfo m_gspInfo; //GPS��Ϣ
		//�Ѿ����
		Classify_breath *m_classifyBreath;  //�Ѿȴ���
		std::vector<TargetInfo> m_queueTarget; //����������
		std::mutex m_targetMutex;
		std::vector<TargetInfo> m_queueResultTarget; //�������
		bool m_isExistTarget; //�Ƿ����Ŀ��
		int m_targetSize; //�Ƿ����Ŀ�����
		short m_distance; //̽�����
		short m_deteInterval; //̽����
		short m_detectionPos; //̽��λ��
		short m_detectionPosInit; //̽��λ��
		bool m_isDetcEnd; //�Ƿ�̽�����
		bool m_isJump; //��ת��ʶ
		int m_lastTime;//̽����ʷʱ��
		int m_detectionTime; //̽��ʱ��
	};//RadarDevice
}//APP
#endif//__RadarDevice_H__