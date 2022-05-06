/*********************************************************************************
 *
 * @file	RadarQuery.h
 * @version	1.0
 * @date	2020-10-16
 * @author	孙永民
 *
 * @brief	协议查询类的声明
 *
 *********************************************************************************/
 // 版权(C) 电波传播研究所
 // 改动历史
 // 日期        作者	改动内容
 // 2020-10-16   孙永民	创建文件
 //================================================================================

#ifndef __RadarQuery_H__
#define __RadarQuery_H__

#include "EfsDef.h"

namespace DAQ
{
	class RadarQuery
	{
	public:
		/** @brief 构造函数 */
		RadarQuery();

		/** @brief 析构函数 */
		~RadarQuery();
	public:
		//CRC校验
		inline short CRC16(short *data, int length)
		{
			short crc = 0;
			while (length--)
			{
				crc ^= *data++;
			}
			return crc;
		}

		//字符串分割
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
		* @brief 下行命令数据
		* @param strValue,retFrame
		* @return 无
		*/
		bool DownSystemParam(char *pCur, int &size, SystemParam sysParam);
		//bool DownSingalCmd(int messageType, string strValue, CBaseFrame &retFrame);
		bool DownLightLevel(char *pCur, int &size, int lightLevel); //亮灯等级
		bool DownTriggerModeCmd(char *pCur, int &size, TriggerModeParam triggerModeParam); //探测方式设置
		bool DownMarkExtendedCmd(char *pCur, int &size, TriggerModeParam triggerModeParam); //标记扩展设置
		bool DownTimeParamCmd(char *pCur, int &size, TimeParam timeParam); //探测方式设置
		bool DownGainParamCmd(char *pCur, int &size, GainParam gainParam); //增益参数设置
		bool DownFilterParamCmd(char *pCur, int &size, FilterParam filterParam); //滤波参数设置
		bool DownSingalCmd(char *pCur, int &size, int messageType); //单指令报文
		bool DownLowerComputerConfig(char *pCur, int &size,string ip, short devType, string cardSerialNum,
			short deviceSerialNum, short versionNum, short calibrationValue, short antennaCode,short frqValue); //整机配置
	};
}
#endif //__RadarQuery_H__
