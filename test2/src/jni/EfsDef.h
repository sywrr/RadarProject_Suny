/****************************************************************************************
*
* @file    EfsDef.h
* @version 1.0
* @date    2021-04-30
* @author  ������
*
* @brief   �������Ͷ���ͷ�ļ�
*
***************************************************************************************/
// ��Ȩ(C) 2009 - 2021 �粨�����о���
// �Ķ���ʷ
// ����         ����     �Ķ�����
// 2021-04-30   ������   �����ļ�
//==============================================================================
#ifndef __RadarDef_H__
#define __RadarDef_H__

#include "iostream"
#include "vector"

using namespace std;

namespace DAQ
{
	//�״����
	struct CmdMsg{
		int msgType;
		string strMsg;  //������Ϣ
		bool isOnLine;
	};

	//֡��ʽ
	#define SETTING		0x8001  //��������
	#define START		0x8002  //����
	#define STOP		0x8003  //�ر�
	#define RUNNING		0x8004  //����״̬

	//֡��ʽ
	#define FRAME_HEAD  ((short)0x07ff)  //֡ͷ
	#define FRAME_END   ((short)0x5555)  //֡β
	#define FRAME_LEN   556  //�״����ݷ�֡����

	//������
	#define HISENSE_1  -1 
	#define HISENSE0   0 
	#define HISENSE1   1
	#define HISENSE2   2

	//��������
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

	///��̬�����ҵ���ʶ
	#define PRO_DLL_WATER  1	//ˮ����Ŀ
	#define PRO_DLL_RESCUE 2	//�Ѿ���Ŀ
	#define PRO_DLL_CONDUIT	3	//�ܵ�
	#define PRO_DLL_CONFIG	4	//��������

	//�״����
	struct RadarParam{
		string strKey;
		int Value;
		bool isUpdate;
		bool isOnline;
	};

//�˿�
#if 1
	const int devicePort = 8081;
	const int localPort = 8080;
#else
	const int devicePort = 9001;
	const int localPort = 9002; 
#endif

	//ϵͳ����
	struct SystemParam{
		int gps;
		int protocal;
		int enery;
		string ip;
		int port;
	};

	//ʱ�����
	struct TimeParam{
		int EfsChannelmodel; //ͨ��ѡ�� 11-˫�� 01-CH1, 10-CH2
		int EfsChannelnum; //ͨ����
		int steplen; //��������
		int stepinterval; //�������
		int cyclesteplen; //��������2B
		int fplulsefreqcount; //FPULSE Ƶ�ʼ���ֵ
		int samplingclockfrequency; //����ʱ�Ӽ���ֵ
		int counterbegin; //ʱ������Ӧ�ļ�������ֵ
		int delaybegin; //ʱ������Ӧ����ʱоƬ��ֵ
		int counterend; //ʱ���յ��Ӧ�ļ�������
		int samplingpoints; //ÿ���Ĳ�������
		int scanSpeed; //ɨ��
	};

	//�������
	struct GainParam{
		int automaticGain; //�Զ�����
		int isGainEnd; //�Զ�������Ʊ�ʶ
		int segmentedGainMax;//��Ӳ���ָ�
		int EfsChannelnum; //ͨ����
		int segmentNum; //����
		int segmentedGain[9]; //Ӳ������ֵ
		int segmentedGainSoft[9]; //�������ֵ
	};

	//�˲�����
	struct FilterParam{
		int filterEnable; //�˲�ʹ��
		int EfsChannelnum; //ͨ����
		int indexValue; //�˲����ͺ�
		char filter[51 * 2]; //�˲�����
	};

	///�����ģʽ
	enum TriggerMode{//����ģʽ
		TriggerMode_Timeout=1,//����
		TriggerMode_Raster,//�����
		TriggerMode_Repeat//���ģʽ
	};

	//����ģʽ����
	struct TriggerModeParam{
		int triggerMode; //����ģʽ =1 ������=2 �ֲ⣬=3 ���
		float pluseInterval; //�����ȡ�����
		int extentMark;  //�����չ
	};

#define _DATABUFLEN 1024*4*500

	enum Cmd{//ָ��1
		Cmd_Unknow,
		Cmd_SendSelfChecking = (short)0xAA10,//��λ��������λ���Լ�
		Cmd_ReceiveSelfChecking = (short)0xAA10,//��λ��Ӧ���Լ�
		Cmd_SendCheckModel = 0xAA51,//��ѯ�豸�ͺ�
		Cmd_ReceiveCheckModel = 0xAA51,//λ��Ӧ���ѯ�豸�ͺ�
		Cmd_SendCheckVersion = 0xAA52,//��ѯ�豸�汾��
		Cmd_ReceiveCheckVersion = (short)0xAA52,//λ��Ӧ���ѯ�豸�汾��
		Cmd_SendCheckSerialNumber = 0xAA53,//��ѯ�豸���к�
		Cmd_ReceiveCheckSerialNumber = 0xAA53,//λ��Ӧ���ѯ�豸���к�
		Cmd_ReceiveResponse = 0xDD02,//��λ��Ӧ��ָ��
		Cmd_ReceiveSample = (short)0xDD01,//��λ���״�����ָ��
		Cmd_SendSetting = 0x03,//��λ��������λ������
		Cmd_ReceiveSetting = 0x04,//��λ��Ӧ������
		Cmd_SendStart = 0xAA00,//��λ��������λ����ʼ����
		Cmd_ReceiveStart = 0xAA00,//��λ��Ӧ��ʼ����
		Cmd_SendSystemParam = (short)0xDD11,//��λ������ϵͳ��������ָ��
		Cmd_ReceiveSystemParam = (short)0xDD11,//��λ��Ӧ��ϵͳ��������ָ��
		Cmd_SendMarkExtended = 0xBB30,//��λ�����ͱ����չ����ָ��
		Cmd_ReceiveMarkExtended = 0xBB30,//��λ��Ӧ������չ����ָ��
		Cmd_SendTriggerMode = 0xBB31,//��λ������̽��ģʽ����ָ��
		Cmd_ReceiveTriggerMode = (short)0xBB31,//��λ��Ӧ��̽��ģʽ����ָ��
		Cmd_SendTimeParam = 0xCC52,//��λ������ʱ���������ָ��
		Cmd_ReceiveTimeParam = (short)0xCC52,//��λ��Ӧ��ʱ���������ָ��
		Cmd_SendGainParam = 0xDD22,//��λ�����������������ָ��
		Cmd_ReceiveGainParam = (short)0xDD22,//��λ��Ӧ�������������ָ��
		Cmd_SendFilterParam = 0xDD23,//��λ�������˲���������ָ��
		Cmd_ReceiveFilterParam = (short)0xDD23,//��λ��Ӧ���˲���������ָ��
		Cmd_SendLightLevel = 0xDD24,//��λ���������Ƶȼ�����ָ��
		Cmd_ReceiveLightLevel = (short)0xDD24,//��λ��Ӧ�����Ƶȼ�����ָ��
		Cmd_SendComputerConfig = (short)0xCC70,//��λ��������������
		Cmd_ReceiveComputerConfig = (short)0xCC70,//��λ��������������
		Cmd_ReceiveData = 0x06,//��λ�� ������0Ӧ������ 1Ӧ������
		Cmd_SendPause = 0xAA01,//��λ��������λ����ͣ
		Cmd_ReceivePause = 0xAA01,//��λ��Ӧ����ͣ
		Cmd_SendStop = 0xAA01,//��λ��������λ��ֹͣ
		Cmd_ReceiveStop = 0xAA01,//��λ��Ӧ��ֹͣ
		Cmd_SendHeart = 0xAA12,//��λ��������λ������
		Cmd_ReceiveHeart = 0xAA12,//��λ��Ӧ������
		Cmd_SendQureyGps = 0x0B,//��λ����ѯ��λ��GPS��Ϣ
		Cmd_ReceiveGps = 0x0C,//��λ��Ӧ��GPS��Ϣ
		Cmd_SendQureyStat = 0x0D,//��λ����ѯ��λ��״̬
		Cmd_ReceiveStat = 0x0E,//��λ��Ӧ��״̬
		Cmd_SendQureyFailure = 0xE9,//��λ����ѯ��λ�����ϴ���
		Cmd_ReceiveFailure = 0xEA//��λ��Ӧ�����
	};

	struct GspInfo{
		int mflag; //����ͷ��ʶ
		short gps_state; //��λ״̬
		int scanIndex;//����
		double longitude;//����
		short wE;//����
		double latitude;//γ��
		short sN;//�ϱ�
		int year;
		short mon;
		short day;
		short hour;
		short min;
		short sec;
		short sateNum; //���Ǹ���
		short hasData; //�Ƿ�������
		short gpsEnabled; //gps�Ƿ�ʹ��
	};

	//�ѾȽ����Ϣ
	#define RESCUE_QUEUE_SIZE   5   //�Ѿȶ��г���
	#define RESCUE_TARGET_SIZE  3   //�������
	struct TargetInfo{
		bool isExist; //�Ƿ����Ŀ��
		short targetPos; //Ŀ��λ��
		int detectionPos; //̽��λ��
		bool isDetectionEnd; //�Ƿ�̽�����
		short detectionBegin; //̽�⿪ʼ��Χ
		short detectionInterval; //̽����
		bool isExistResult;   //�Ƿ�������ս��
		bool isJump; //�Ƿ���Ծ
	};

	//���ֽ�������ת��Ϊfloat����
	inline int byteToFloat(string strData, float* dataIn)
	{
		int size = strData.length();
		for (int j = 0, column = 0; j<size / 4; j++, column += 4)
		{
			long long  value = 0;
			bool isNegativeNumber = false;
			unsigned char  numberChar;
			for (int i = 3, leftScroll = 24; i >= 0; i--, leftScroll -= 8)//С�˷�ʽ��ȡ
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

	//���ֽ�������ת��Ϊint����
	inline int transformToInt(string strData, int* dataIn)
	{
		int size = strData.length();
		for (int j = 0, column = 0; j<size / 4; j++, column += 4)
		{
			int value = 0;
			bool isNegativeNumber = false;
			unsigned char numberChar;
			for (int i = 3, leftScroll = 24; i >= 0; i--, leftScroll -= 8)//С�˷�ʽ��ȡ
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


	//���ֽ�������ת��Ϊshort����
	inline int transformToShort(string strData, short* dataIn)
	{
		int size = strData.length();
		for (int j = 0, column = 0; j<size / 4; j++, column += 4)
		{
			int value = 0;
			bool isNegativeNumber = false;
			unsigned char numberChar;
			for (int i = 3, leftScroll = 24; i >= 0; i--, leftScroll -= 8)//С�˷�ʽ��ȡ
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

	//�ַ����ָ�
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