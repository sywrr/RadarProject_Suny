
#include "RadarQuery.h"

using namespace DAQ;

/** @brief 构造函数 */
RadarQuery::RadarQuery()
{

}

/** @brief 析构函数 */
RadarQuery::~RadarQuery()
{
}

/** @brief 下行系统参数设置*/
bool RadarQuery::DownSystemParam(char *pCur, int &size, SystemParam sysParam)
{
	char *pHead = pCur;

	//07 ff head
	//00 0e length
	//dd 11 type
	//01 
	//67 00 a8 c0 
	//90 1f 
	//00 00 00 00 00  body
	//03 4e  crc
	//55 55  end

	*pCur++ = 0x07; //帧头 
	*pCur++ = 0xff; 
	
	*pCur++ = 00; //报文数据长度 
	*pCur++ = 00;

	*pCur++ = 0xdd; //报文类型 
	*pCur++ = 0x11;

	//*pCur++ = sysParam.gps | (sysParam.protocal << 1) | (sysParam.enery << 2); //外设设置 
	*pCur++ = sysParam.gps | (sysParam.protocal) | (sysParam.enery); //外设设置

	string ip = sysParam.ip;
	vector<string> vecIp = split(ip, ".");
	for (int i = vecIp.size()-1; i >= 0; i--){
		string ip = vecIp[i];
		int valueIp = 0;
		for (int j = 0; j < ip.length(); j++)
		{
			valueIp += (ip.at(j) - 0x30) * pow(10, ip.length()-1-j);
		}
		*pCur++ = valueIp;
	}

	int port = sysParam.port;
	int port1, port2;
	port1 = (port>>8)&0xff;
	port2 = port&0xff;
	*pCur++ = port2; //端口
	*pCur++ = port1;
	
	*pCur++ = 0x00; //预留字节
	*pCur++ = 0x00;
	*pCur++ = 0x00;
	*pCur++ = 0x00;
	*pCur++ = 0x00;

	size = (int)(pCur - pHead - 4);
	pHead[2] = size >> 8;  //修正長度
	pHead[3] = size & 0x00ff;

	short crc16 = CRC16((short *)(pHead + 4), size / 2);

	*pCur++ = crc16 & 0xff; //CRC
	*pCur++ = crc16 >> 8;

	*pCur++ = 0x55; //帧尾
	*pCur++ = 0x55;

	size += 8; 
	return HISENSE0;
}

/** @brief 下行单指令设置*/
bool RadarQuery::DownSingalCmd(char *pCur, int &size, int messageType)
{
	char *pHead = pCur;

	//07 ff 00 04 aa 10 00 00 aa 10 55 55

	*pCur++ = 0x07; //帧头 
	*pCur++ = 0xff;

	*pCur++ = 00; //报文数据长度 
	*pCur++ = 00;

	*pCur++ = (messageType >> 8) & 0xff; //报文类型 
	*pCur++ = messageType & 0xff;

	*pCur++ = 0x00; //预留字节2B
	*pCur++ = 0x00;

	size = (int)(pCur - pHead - 4);
	pHead[2] = size >> 8;  //修正长度
	pHead[3] = size & 0x00ff;

	short crc16 = CRC16((short *)(pHead + 4), size / 2);

	*pCur++ = crc16 & 0xff; //CRC
	*pCur++ = crc16 >> 8;

	*pCur++ = 0x55; //帧尾
	*pCur++ = 0x55;

	size += 8;
	return HISENSE0;
}


/** @brief 下行亮灯等级设置*/
bool RadarQuery::DownLightLevel(char *pCur, int &size, int lightLevel)
{
	char *pHead = pCur;

	//07 ff 00 04 DD 24 01 00 aa 10 55 55

	*pCur++ = 0x07; //帧头 
	*pCur++ = 0xff;

	*pCur++ = 00; //报文数据长度 
	*pCur++ = 00;

	*pCur++ = 0xDD; //报文类型 
	*pCur++ = 0x24;

	*pCur++ = lightLevel & 0xff;
	*pCur++ = 0x00;

	size = (int)(pCur - pHead - 4);
	pHead[2] = size >> 8;  //修正长度
	pHead[3] = size & 0x00ff;

	short crc16 = CRC16((short *)(pHead + 4), size / 2);

	*pCur++ = crc16 & 0xff; //CRC
	*pCur++ = crc16 >> 8;

	*pCur++ = 0x55; //帧尾
	*pCur++ = 0x55;

	size += 8;
	return HISENSE0;
}

/** @brief 下行探测方式设置*/
bool RadarQuery::DownTriggerModeCmd(char *pCur, int &size, TriggerModeParam triggerModeParam)
{
	char *pHead = pCur;

	//07 ff 00 04 aa 10 00 00 aa 10 55 55

	*pCur++ = 0x07; //帧头 
	*pCur++ = 0xff;

	*pCur++ = 00; //报文数据长度 
	*pCur++ = 00;

	*pCur++ = 0xBB; //报文类型 
	*pCur++ = 0x31;

	*pCur++ = triggerModeParam.triggerMode; //探测模式1字节, 1-连续测量，2-轮测模式，3-点测模式
	*pCur++ = 0x00; //预留1字节

	size = (int)(pCur - pHead - 4);
	pHead[2] = size >> 8;  //修正长度
	pHead[3] = size & 0x00ff;

	short crc16 = CRC16((short *)(pHead + 4), size / 2);

	*pCur++ = crc16 & 0xff; //CRC
	*pCur++ = crc16 >> 8;

	*pCur++ = 0x55; //帧尾
	*pCur++ = 0x55;

	size += 8;
	return HISENSE0;
}

/** @brief 下行标记扩展设置*/
bool RadarQuery::DownMarkExtendedCmd(char *pCur, int &size, TriggerModeParam triggerModeParam)
{
	char *pHead = pCur;

	//07 ff 00 04 aa 10 00 00 aa 10 55 55

	*pCur++ = 0x07; //帧头 
	*pCur++ = 0xff;

	*pCur++ = 00; //报文数据长度 
	*pCur++ = 00;

	*pCur++ = 0xBB; //报文类型 
	*pCur++ = 0x30;

	*pCur++ = (unsigned char)(triggerModeParam.extentMark);
	*pCur++ = (unsigned char)(triggerModeParam.extentMark>>8);

	for (int i = 0; i < 4; i++)
	{
		*pCur++ = 0x00;
	}

	size = (int)(pCur - pHead - 4);
	pHead[2] = size >> 8;  //修正长度
	pHead[3] = size & 0x00ff;

	short crc16 = CRC16((short *)(pHead + 4), size / 2);

	*pCur++ = crc16 & 0xff; //CRC
	*pCur++ = crc16 >> 8;

	*pCur++ = 0x55; //帧尾
	*pCur++ = 0x55;

	size += 8;
	return HISENSE0;
}

/** @brief 下行增益参数设置*/
bool RadarQuery::DownGainParamCmd(char *pCur, int &size, GainParam gainParam)
{
	char *pHead = pCur;

	*pCur++ = 0x07; //帧头 
	*pCur++ = 0xff;

	*pCur++ = 00; //报文数据长度 
	*pCur++ = 00;

	*pCur++ = 0xDD; //报文类型 
	*pCur++ = 0x22;

	*pCur++ = gainParam.automaticGain; //自动增益  1-自动增益 0-默认
	*pCur++ = gainParam.EfsChannelnum; //通道 1-ch1 ,2-ch2 
	*pCur++ = gainParam.segmentNum; //段数 9

	*pCur++ = gainParam.segmentedGain[0]; //增益系数1
	*pCur++ = gainParam.segmentedGain[1]; //增益系数2
	*pCur++ = gainParam.segmentedGain[2]; //增益系数3
	*pCur++ = gainParam.segmentedGain[3]; //增益系数4
	*pCur++ = gainParam.segmentedGain[4]; //增益系数5
	*pCur++ = gainParam.segmentedGain[5]; //增益系数6
	*pCur++ = gainParam.segmentedGain[6]; //增益系数7
	*pCur++ = gainParam.segmentedGain[7]; //增益系数8
	*pCur++ = gainParam.segmentedGain[8]; //增益系数9

	for (int i = 0; i < 6; i++)
	{
		*pCur++ = 0x00;
	}

	size = (int)(pCur - pHead - 4);
	pHead[2] = size >> 8;  //修正长度
	pHead[3] = size & 0x00ff;

	short crc16 = CRC16((short *)(pHead + 4), size / 2);

	*pCur++ = crc16 & 0xff; //CRC
	*pCur++ = crc16 >> 8;

	*pCur++ = 0x55; //帧尾
	*pCur++ = 0x55;

	size += 8;
	return HISENSE0;
}

/** @brief 下行时序参数设置 */
bool RadarQuery::DownTimeParamCmd(char *pCur, int &size, TimeParam timeParam)
{
	char *pHead = pCur;

	//07 ff 00 28 cc 52 00 01 a9 03 01 02 06 01 5c 03 0d 01 9f 06 06 02 c8 03 0c 04 04 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 6a 50 55 55
	*pCur++ = 0x07; //帧头 
	*pCur++ = 0xff;

	*pCur++ = 00; //报文数据长度 
	*pCur++ = 00;

	*pCur++ = 0xCC; //报文类型 
	*pCur++ = 0x52;

	*pCur++ = timeParam.EfsChannelmodel; //通道选择 11-双， 01-CH1, 10-CH2
	*pCur++ = timeParam.EfsChannelnum; //通道号

	int stepLen = timeParam.steplen; //步进长度
	*pCur++ = (unsigned char)stepLen;
	*pCur++ = (unsigned char)(stepLen >> 8);
	*pCur++ = (unsigned char)(stepLen >> 16);
	*pCur++ = (unsigned char)(stepLen >> 24);

	int stepInterval = timeParam.stepinterval; //步进间隔
	*pCur++ = (unsigned char)stepInterval;
	*pCur++ = (unsigned char)(stepInterval >> 8);

	int cycleStepLen = timeParam.cyclesteplen; //步进长度2B
	*pCur++ = (unsigned char)cycleStepLen;
	*pCur++ = (unsigned char)(cycleStepLen >> 8);

	int fplulsefreqCount = timeParam.fplulsefreqcount; //FPULSE 频率计数值
	*pCur++ = (unsigned char)fplulsefreqCount;
	*pCur++ = (unsigned char)(fplulsefreqCount >> 8);
	
	int samplingClockFrequency = timeParam.samplingclockfrequency; //采样时钟计数值
	*pCur++ = (unsigned char)samplingClockFrequency;
	*pCur++ = (unsigned char)(samplingClockFrequency >> 8);

	int counterBegin = timeParam.counterbegin; //时窗起点对应的计数器初值
	*pCur++ = (unsigned char)counterBegin;
	*pCur++ = (unsigned char)(counterBegin >> 8);

	int delayBegin = timeParam.delaybegin; //时窗起点对应的延时芯片初值
	*pCur++ = (unsigned char)delayBegin;
	*pCur++ = (unsigned char)(delayBegin >> 8);

	int counterEnd = timeParam.counterend; //时窗终点对应的计数器终止
	*pCur++ = (unsigned char)counterEnd;
	*pCur++ = (unsigned char)(counterEnd >> 8);
	
	int samplingPoint = timeParam.samplingpoints; //每道的采样点数
	*pCur++ = (unsigned char)samplingPoint;
	*pCur++ = (unsigned char)(samplingPoint >> 8);

	int timeConsumption = 1;
	*pCur++ = (unsigned char)timeConsumption;  //叠加次数
	*pCur++ = 0;

	for (int i = 0; i < 14; i++) //预留16字节
	{
		*pCur++ = 0x00;
	}

	size = (int)(pCur - pHead - 4);
	pHead[2] = size >> 8;  //修正长度
	pHead[3] = size & 0x00ff;

	short crc16 = CRC16((short *)(pHead + 4), size / 2);

	*pCur++ = crc16 & 0xff; //CRC
	*pCur++ = crc16 >> 8;

#if 1
	*pCur++ = 0x55; //帧尾
	*pCur++ = 0x55;
#else
	*pCur++ = timeParam.scanSpeed >> 8; //帧尾
	*pCur++ = timeParam.scanSpeed && 0xff;
#endif

	size += 8;
	return HISENSE0;
}

/** @brief 下行滤波参数设置 */
bool RadarQuery::DownFilterParamCmd(char *pCur, int &size, FilterParam filterParam)
{
	char *pHead = pCur;
	//07 ff 00 70 dd 23 00 01 64 12 23 34 45 56 67 78 89 9a ab 12 23 34 45 56 67 78 89 9a ab
	//12 23 34 45 56 67 78 89 9a ab 12 23 34 45 56 67 78 89 9a ab 12 23 34 45 56 67 78 89 9a ab
	//12 23 34 45 56 67 78 89 9a ab 12 23 34 45 56 67 78 89 9a ab 12 23 34 45 56 67 78 89 9a ab
	//12 23 34 45 56 67 78 89 9a ab 12 23 34 45 56 67 78 89 9a ab 12 23 34 45 56 67 78 B8 2A 55 55

	*pCur++ = 0x07; //帧头 
	*pCur++ = 0xff;

	*pCur++ = 00; //报文数据长度 
	*pCur++ = 00;

	*pCur++ = 0xDD; //报文类型 
	*pCur++ = 0x23;

	*pCur++ = (unsigned char)filterParam.filterEnable; //滤波器使能 11-双， 0-不使能，1-使能， 默认00
	*pCur++ = (unsigned char)filterParam.EfsChannelnum; //通道号，1-ch1 ,2-ch2 
	*pCur++ = (unsigned char)filterParam.indexValue; //滤波器型号，0-9

	for (int i = 0; i < 102; i++) //滤波器系数, 51个滤波器参数，short类型
	{
		*pCur++ = filterParam.filter[i];
	}

	for (int i = 0; i < 5; i++) //预留5字节
	{
		*pCur++ = 0x00;
	}

	size = (int)(pCur - pHead - 4);
	pHead[2] = size >> 8;  //修正长度
	pHead[3] = size & 0x00ff;

	short crc16 = CRC16((short *)(pHead + 4), size / 2);

	*pCur++ = crc16 & 0xff; //CRC
	*pCur++ = crc16 >> 8;

	*pCur++ = 0x55; //帧尾
	*pCur++ = 0x55;

	size += 8;
	return HISENSE0;
}

/** @brief 整机参数设置 */
bool RadarQuery::DownLowerComputerConfig(char *pCur, int &size, string ip, short devType, string cardSerialNum,
	short deviceSerialNum, short versionNum, short calibrationValue,short antennaCode, short frqValue)
{
	char *pHead = pCur;
	// 07 ff
	//00 20
	//cc 70 
	//0a 00 a8 c0 
	//01 16 
	//11 22 33 44 55 66 
	// 00 02 
	//11 11 
	//e4 02 00 00 00 00 00 00 00 00 00 00 00 00 ED B7 55 55  (740 - 0002)

	*pCur++ = 0x07; //帧头 
	*pCur++ = 0xff;

	*pCur++ = 00; //报文数据长度 
	*pCur++ = 20;

	*pCur++ = 0xCC; //报文类型 
	*pCur++ = 0x70;

	//IP地址
	vector<string> vecIp = split(ip, ".");
	for (int i = 0; i <vecIp.size(); i++){
		string ip = vecIp[i];
		int valueIp = 0;
		for (int j = 0; j < ip.length(); j++)
		{
			valueIp += (ip.at(j) - 0x30) * pow(10, ip.length() - 1 - j);
		}
		*pCur++ = valueIp;
	}
	//设备型号
	*pCur++ = devType>>8;
	*pCur++ = devType;

	//板卡序列号
	for (int i = 0; i < cardSerialNum.size(); i++) 
	{
		*pCur++ = cardSerialNum.at(i);
	}

	//设备序列号
	*pCur++ = deviceSerialNum >> 8;
	*pCur++ = deviceSerialNum;

	//版本号
	*pCur++ = versionNum>>8;
	*pCur++ = versionNum;

	//校准值
	*pCur++ = calibrationValue;
	*pCur++ = calibrationValue>>8;

	//天线识别码
	*pCur++ = antennaCode >> 8;
	*pCur++ = antennaCode;

	//重频值
	*pCur++ = frqValue;
	*pCur++ = frqValue >> 8;
	//预留
	for (int i = 0; i < 8; i++) //预留5字节
	{
		*pCur++ = 0x00;
	}

	size = (int)(pCur - pHead - 4);
	pHead[2] = size >> 8;  //修正长度
	pHead[3] = size & 0x00ff;

	short crc16 = CRC16((short *)(pHead + 4), size / 2);

	*pCur++ = crc16 & 0xff; //CRC
	*pCur++ = crc16 >> 8;

	*pCur++ = 0x55; //帧尾
	*pCur++ = 0x55;

	size += 8;
	return HISENSE0;

}