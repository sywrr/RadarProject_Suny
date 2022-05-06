/*********************************************************************************
 *
 * @file	RadarQuery.h
 * @version	1.0
 * @date	2020-10-16
 * @author	������
 *
 * @brief	Э���ѯ�������
 *
 *********************************************************************************/
 // ��Ȩ(C) �粨�����о���
 // �Ķ���ʷ
 // ����        ����	�Ķ�����
 // 2020-10-16   ������	�����ļ�
 //================================================================================

#ifndef __RadarQuery_H__
#define __RadarQuery_H__

#include "EfsDef.h"

namespace DAQ
{
	class RadarQuery
	{
	public:
		/** @brief ���캯�� */
		RadarQuery();

		/** @brief �������� */
		~RadarQuery();
	public:
		//CRCУ��
		inline short CRC16(short *data, int length)
		{
			short crc = 0;
			while (length--)
			{
				crc ^= *data++;
			}
			return crc;
		}

		//�ַ����ָ�
		inline vector<string> split(const string& str,const string &delim)
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
				p = strtok(NULL,d);
			}
			return res;
		}
		/**
		* @brief ������������
		* @param strValue,retFrame
		* @return ��
		*/
		bool DownSystemParam(char *pCur, int &size, SystemParam sysParam);
		//bool DownSingalCmd(int messageType, string strValue, CBaseFrame &retFrame);
		bool DownLightLevel(char *pCur, int &size, int lightLevel); //���Ƶȼ�
		bool DownTriggerModeCmd(char *pCur, int &size, TriggerModeParam triggerModeParam); //̽�ⷽʽ����
		bool DownMarkExtendedCmd(char *pCur, int &size, TriggerModeParam triggerModeParam); //�����չ����
		bool DownTimeParamCmd(char *pCur, int &size, TimeParam timeParam); //̽�ⷽʽ����
		bool DownGainParamCmd(char *pCur, int &size, GainParam gainParam); //�����������
		bool DownFilterParamCmd(char *pCur, int &size, FilterParam filterParam); //�˲���������
		bool DownSingalCmd(char *pCur, int &size, int messageType); //��ָ���
		bool DownLowerComputerConfig(char *pCur, int &size,string ip, short devType, string cardSerialNum,
			short deviceSerialNum, short versionNum, short calibrationValue, short antennaCode,short frqValue); //��������
	};
}
#endif //__RadarQuery_H__
