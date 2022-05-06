
#include "RadarDevice.h"
#include "tool_fir.h"
#include "FileDef.h"
#include "ctime"

#ifdef _WIN32
#define DELAT_TIME 500
#else
#define DELAT_TIME 500 *1000
#endif

using namespace DAQ;
//int frameNum = 0;   //EX

//构造函数
RadarDevice::RadarDevice(int netWork, string localIp, string deviceIp, int antenaType, int dllVersion)
{
	memset(szSend, 0, sizeof(szSend));
	m_sampleData.clear();
	m_radarQuery = new RadarQuery();
	m_isLinked = false;
	m_SIProcess = false;
	m_SiProcessBuf[false].empty();
	m_SiProcessBuf[true].empty();
	m_file = nullptr;
	m_gpsFile = nullptr;
	m_linkedSize = 0;
	m_lightLevel = 0;
	m_longitude = 0;  //经度
    m_latitude = 0;  //纬度
	memset(&m_gspInfo, 0, sizeof(m_gspInfo));
	m_gspInfo.gps_state = 0x56;  //未定位
	m_dllVersion = dllVersion;

	//重频 频率计数值 中心频率 默认采样点 默认扫速 默认时窗 默认信号位置 默认硬件增益（经验值）
	vector<int> vecParams;
	vecParams  = { 390, 4, 1500, 512, 256, 15, -10, 1 }; 
	m_antennaParams.insert(pair<int, vector<int>>(0, vecParams)); //GC1500MHz
	vecParams = { 781, 2, 2000, 512, 256, -10, -10, 1 };
	m_antennaParams.insert(pair<int, vector<int>>(1, vecParams)); //GC2000MHz
	vecParams = { 120, 13, 270, 512, 128, 80, 2, 1 };
	m_antennaParams.insert(pair<int, vector<int>>(2, vecParams)); //GC270MHz
	vecParams = { 120, 13, 150, 1024, 64, 120, -5, 1 };
	m_antennaParams.insert(pair<int, vector<int>>(3, vecParams)); //GC150MHz
	vecParams = { 781, 2, 2000, 512, 256, 10, 4, 1 };
	m_antennaParams.insert(pair<int, vector<int>>(4, vecParams)); //AL2000MHz
	vecParams = { 781, 2, 1500, 512, 256, 15, 4, 1 };
	m_antennaParams.insert(pair<int, vector<int>>(5, vecParams)); //AL1500MHz
	vecParams = { 120, 13, 1000, 512, 256, 20, 4, 1 };
	m_antennaParams.insert(pair<int, vector<int>>(6, vecParams)); //AL1000MHz
	vecParams = { 390, 4, 900, 512, 256, 25, 5, 1 };
	m_antennaParams.insert(pair<int, vector<int>>(7, vecParams)); //GC900HF
	vecParams = { 120, 13, 400, 512, 256, 50, 12, 1 };
	m_antennaParams.insert(pair<int, vector<int>>(8, vecParams)); //GC400HF
	vecParams = { 120, 13, 100, 1024, 16, 600, 0, 1 };
	m_antennaParams.insert(pair<int, vector<int>>(9, vecParams)); //GC100HF
	vecParams = { 64, 13, 50, 1024, 16, 650, 0, 1 };
	m_antennaParams.insert(pair<int, vector<int>>(10, vecParams)); //GC50MHz

	m_radarDataQueue.empty();
	m_radarScan.empty();
	m_dataSave = false;
	m_user = nullptr;
	m_isStart = false;

	//模式参数
	triggerModeParam.triggerMode = 1; 
	triggerModeParam.pluseInterval = 0.01;  //WDMI300
	triggerModeParam.extentMark = 1;

	//系统
	systemParam.ip = localIp;
	systemParam.port = 8080;
	systemParam.enery = 4;
	systemParam.protocal = 2;
	systemParam.gps = 1;

	//时序
	m_stepLenCalibration = 800; //步进校准值,协议交互重新拿
	m_calibration = 60;  //时序校准	
	m_EfsChannelmodel = 0x02;
	m_EfsChannelnum = 0x02;
	vector<int> antenna;
	antenna = m_antennaParams.find(antenaType)->second;
	if (antenna.size())
	{
		m_fplulsefreqcount = antenna.at(1);
		m_antennaRepetition = antenna.at(0);
		m_antenaType = antenna.at(2);
		m_samplingPoints = antenna.at(3);
		m_maxPocketNum = m_samplingPoints / 128;
		m_scanSpeed = antenna.at(4);
		m_timeWindow = antenna.at(5);
		m_signalPosition = antenna.at(6);
		for (int i = 0; i < 9; i++)
		{
			gainParam.segmentedGain[i] = antenna.at(7);  //硬件增益
			gainParam.segmentedGainSoft[i] = 0x01;  //默认软件增益
		}
	}

	//算法
	m_bRemoveBack = 0;
	m_filterEnabled = 0;
	m_correctZero = 0;
	m_vecMark.clear(); 

	//信号增益
	gainParam.EfsChannelnum = m_EfsChannelnum;
	gainParam.automaticGain = 1;
	gainParam.isGainEnd = 0;
	gainParam.segmentNum = 9;
	gainParam.segmentedGainMax = 15;  //软硬件分割点

	//搜救算法初始化
	m_classifyBreath = nullptr;
	if (m_dllVersion == PRO_DLL_RESCUE)
	{
		m_classifyBreath = new Classify_breath();
		m_isExistTarget = false;
		m_distance = 0;
		m_deteInterval = 3; 
		m_detectionPos = 0;
		m_isJump = false;
		m_queueTarget.clear();
		m_queueResultTarget.clear();
		m_detectionPosInit = 0;
	}

	if (netWork == 0){
		systemParam.protocal = 0;
		m_iPort = new CPortUDP(deviceIp, devicePort, localPort);
	}
	else{
		systemParam.protocal = 2;
		m_iPort = new CPortTCPClient(deviceIp, devicePort);
	}
	m_iPort->SetCallBack(this);
	m_iPort->OpenPort();
	sendSystemParam();
}

//析构函数
RadarDevice::~RadarDevice()
{
	if (m_iPort){
		m_iPort->ClosePort();
		delete m_iPort;
	}
	if (m_radarQuery)
		delete m_radarQuery;
}

//命令执行
bool RadarDevice::ExecCommond(CmdMsg cmdMsg, string &errorString)
{
	int msgType = cmdMsg.msgType;
	int nRet = 0;
	switch (msgType)
	{
	case SETTING:{
		bool isOnLine = cmdMsg.isOnLine;
		string strMsg = cmdMsg.strMsg;
		nRet = HandleSaveSetting(strMsg, isOnLine, errorString);
		break;
	}
	case START:{
		nRet = HandleStart(errorString);
		break;
	}
	case STOP:{
		nRet = HandleStop(errorString);
		break;
	}
	case RUNNING:{
		nRet = HandleRunningStatus();
		break;
	}
	default:
		break;
	}
	return nRet;
}
char test[1000];
//接收数据处理
void RadarDevice::OnReceiveData(char *szBuffer, int nRealReads)
{
	if (nRealReads <= 0)
		return;
	BYTE *byteData = (BYTE*)szBuffer;
	memcpy(test,szBuffer,nRealReads);
	//1 头
	short head = byteData[0] << 8 | byteData[1];
	if (head != FRAME_HEAD)
		return;
	//2 长度
	short frameLen = byteData[2] << 8 | byteData[3];
	if (frameLen + 8 != nRealReads)
		return;
	//3 尾
	short end = byteData[nRealReads - 2] << 8 | byteData[nRealReads - 1];
	if (end != FRAME_END)
		return;

	//4 报文类型/识别码
	// 应答类型
	short msgType = byteData[6] << 8 | byteData[7];
	switch (msgType)
	{
	case Cmd_ReceiveSelfChecking:
	{
		break;
	}
	case Cmd_ReceiveTriggerMode:
	{
		if (triggerModeParam.triggerMode ==
			TriggerMode::TriggerMode_Raster)
		{
			sendMarkExtended();
			Sleep(10);
		}
		sendCheckVersion();
		break;
	}
	case Cmd_ReceiveCheckVersion:
	{
		int coefficient = ((unsigned char)byteData[13]) << 24 | ((unsigned char)byteData[12]) << 16
			| ((unsigned char)byteData[11]) << 8 | (unsigned char)byteData[10];
#ifdef EFS_DEBUG
		cout << "coefficient = " << coefficient << endl;
#endif
		m_stepLenCalibration = coefficient ? coefficient : m_stepLenCalibration;
		sendTimeParam();
		break;
	}
	case Cmd_ReceiveTimeParam:
	{
		sendGainParam();
		break;
	}
	case Cmd_ReceiveGainParam:
	{
		sendFilterParam();
		break;
	}
	case Cmd_ReceiveFilterParam:
	{
		if (m_dllVersion == PRO_DLL_WATER){
			sendLightLevel();
			Sleep(10);
		}
		sendStart();
		break;
	}
	case Cmd_ReceiveSystemParam:
	{
		m_isLinked = true;
		break;
	}
	case Cmd_ReceiveComputerConfig:
	{
		break;
	}
	default:
		break;
	}

	if (nRealReads == 556 && m_isStart) 
	{
		m_Mutex.lock();
		bool receviceBuffer = !m_SIProcess;
		string strMsg = string(szBuffer + 6, nRealReads - (6 + 2 + 2));
		m_SiProcessBuf[receviceBuffer].push(strMsg);
		m_Mutex.unlock();
	}
}

//扫描处理
void RadarDevice::onScan()
{
	//数据组合
	if (m_SiProcessBuf[m_SIProcess].size())
	{
		RealDataProcess();
		m_SiProcessBuf[m_SIProcess].empty();
	}
	
	//数据处理
	if (m_radarScan.size())
	{
		m_linkedSize = 0;
		m_isLinked = true;
		ScanDataProcess();
		m_radarScan.empty();
	}
	else
	{//无数据
		if (m_isStart)
		{
			if (m_linkedSize++ > 100){
				if (triggerModeParam.triggerMode !=
					TriggerMode::TriggerMode_Raster)
				{
					m_isLinked = false;
				}			
				m_linkedSize = 0;
			}
		}
	}

	//处理完后切换
	m_Mutex.lock();
	m_SIProcess = !m_SIProcess;
	m_Mutex.unlock();
}

//扫描数据处理
void RadarDevice::ScanDataProcess()
{
	while (!m_radarScan.empty())
	{
		string data = m_radarScan.front();
		//缓存数据
		m_qMutex.lock();
		if (m_radarDataQueue.size()<500)
			m_radarDataQueue.push(data);
		else{
			m_radarDataQueue.pop();
			m_radarDataQueue.push(data);
		}
		m_qMutex.unlock();
		m_radarScan.pop();
	}
}

void RadarDevice::dataSave(string data,int frameNum)
{
	if (!m_dataSave)
		return;
	if (!m_file || data.size()<=0)
		return;
	if (!m_user) 
		*m_user = frameNum;		
	//雷达数据
	fwrite((char *)&data.at(0), data.length(), 1, m_file);
	///GPS数据
	if (!m_gspInfo.gpsEnabled || !m_gpsFile)
		return;
	if (m_gspInfo.hasData)
	{
		string dataGpsArray;
		//数据标志
		dataGpsArray.append((char*)&m_gspInfo.mflag, sizeof(m_gspInfo.mflag));
		//定位状态
		dataGpsArray.append((char*)&m_gspInfo.gps_state, sizeof(m_gspInfo.gps_state));
		//道号
		dataGpsArray.append((char*)&m_gspInfo.scanIndex, sizeof(m_gspInfo.scanIndex));
		//纬度值
		dataGpsArray.append((char*)&m_gspInfo.latitude, sizeof(m_gspInfo.latitude));
		//经度值
		dataGpsArray.append((char*)&m_gspInfo.longitude, sizeof(m_gspInfo.longitude));
		//年
		int year = m_gspInfo.year;
		short year4 = year / 1000 + 48;
		dataGpsArray.append((char*)&year4, sizeof(year4));
		short year3 = year / 100 % 10 + 48;
		dataGpsArray.append((char*)&year3, sizeof(year3));
		short year2 = year / 10 % 10 + 48;
		dataGpsArray.append((char*)&year2, sizeof(year2));
		short year1 = year % 10 + 48;
		dataGpsArray.append((char*)&year1, sizeof(year1));
		//            //月
		//            short mon = m_gspInfo.mon;
		//            short mon1 = mon/10 + 48;
		//            dataGpsArray.append((char*)&mon1,sizeof(mon1));
		//            short mon2 = mon%10 + 48;
		//            dataGpsArray.append((char*)&mon2,sizeof(mon2));
		//            //日
		//            short day = m_gspInfo.day;
		//            short day1 = day/10 + 48;
		//            dataGpsArray.append((char*)&day1,sizeof(day1));
		//            short day2 = day%10 + 48;
		//            dataGpsArray.append((char*)&day2,sizeof(day2));
		//月
		short mon = m_gspInfo.mon + 48;
		dataGpsArray.append((char*)&mon, sizeof(mon));
		//日
		short day = m_gspInfo.day + 48;
		dataGpsArray.append((char*)&day, sizeof(day));
		//时
		short hour = m_gspInfo.hour;
		short hour1 = hour / 10 + 48;
		dataGpsArray.append((char*)&hour1, sizeof(hour1));
		short hour2 = hour % 10 + 48;
		dataGpsArray.append((char*)&hour2, sizeof(hour2));
		//分
		short min = m_gspInfo.min;
		short min1 = min / 10 + 48;
		dataGpsArray.append((char*)&min1, sizeof(min1));
		short min2 = min % 10 + 48;
		dataGpsArray.append((char*)&min2, sizeof(min2));
		//秒
		short sec = m_gspInfo.sec;
		short sec1 = sec / 10 + 48;
		dataGpsArray.append((char*)&sec1, sizeof(sec1));
		short sec2 = min % 10 + 48;
		dataGpsArray.append((char*)&sec2, sizeof(sec2));
		dataGpsArray.append("\x2E\x00\x33\x00\x30\x00\x30\x00", 8); //小数
		//卫星个数
		fwrite(dataGpsArray.c_str(), dataGpsArray.length(), 1, m_gpsFile);
	}
}

//数据处理
void RadarDevice::RealDataProcess()
{
	static int totle_size = 0;
	static int lastPocketNum = 0;   //上次数据包序号
	int size = m_SiProcessBuf[m_SIProcess].size();
	for (int i = 0; i < size; i++)
	{		
		string strMsg = m_SiProcessBuf[m_SIProcess].front();
		int length = strMsg.length(); //1+1+4+8+512+20
		//子包序号 1
		int curPocketNum = strMsg.at(0);  //子包序号
		//信息标识 1
		//帧序列号 4
		int frameNum = (BYTE)strMsg.at(5) << 24 | (BYTE)strMsg.at(4) << 16 | (BYTE)strMsg.at(3) << 8 | (BYTE)strMsg.at(2);
		//电池电量 8
		//雷达数据 512
		string data = strMsg.substr(14);
		string strSampleData = data.substr(0,data.size()-20);
		//GPS数据
		string strGpsData = data.substr(data.size() - 20);
		//处理GPS数据
		if (strGpsData.size())
		{
			//21 05 24 05 20 10 年月日时分秒 BCD
			//01 20 24 65 28 96 经度
			//45 经向
			//36 14 55 08 50 4E
			//41
			//00
			m_gspInfo.mflag = 36;
			m_gspInfo.year = ((strGpsData.at(0) >> 4) * 10) +
				(strGpsData.at(0) & 0x0f) + 2000;//年 21
			m_gspInfo.mon = ((strGpsData.at(1) >> 4) * 10) +
				(strGpsData.at(1) & 0x0f);//月 5
			m_gspInfo.day = ((strGpsData.at(2) >> 4) * 10) +
				(strGpsData.at(2) & 0x0f);//日
			m_gspInfo.hour = ((strGpsData.at(3) >> 4) * 10) +
				(strGpsData.at(3) & 0x0f);//时
			m_gspInfo.min = ((strGpsData.at(4) >> 4) * 10) +
				(strGpsData.at(4) & 0x0f);//分
			m_gspInfo.sec = ((strGpsData.at(5) >> 4) * 10) +
				(strGpsData.at(5) & 0x0f);//秒
			int long1 = ((strGpsData.at(6) >> 4) * 10) + (strGpsData.at(6) & 0x0f);
			int long2 = ((strGpsData.at(7) >> 4) * 10) + (strGpsData.at(7) & 0x0f);
			int long3 = ((strGpsData.at(8) >> 4) * 10) + (strGpsData.at(8) & 0x0f);
			int long4 = ((strGpsData.at(9) >> 4) * 10) + (strGpsData.at(9) & 0x0f);
			int long5 = ((strGpsData.at(10) >> 4) * 10) + (strGpsData.at(10) & 0x0f);
			int long6 = ((strGpsData.at(11) >> 4) * 10) + (strGpsData.at(11) & 0x0f);
			double longitude = (long1)* 10000 + (long2)* 100 + (long3)* 1 + (long4)* 0.01 + (long5)* 0.0001 + (long6)* 0.000001;
			m_gspInfo.wE = strGpsData.at(12) == 0x45 ? 1 : 0;
			int lat1 = ((strGpsData.at(13) >> 4) * 10) + (strGpsData.at(13) & 0x0f);
			int lat2 = ((strGpsData.at(14) >> 4) * 10) + (strGpsData.at(14) & 0x0f);
			int lat3 = ((strGpsData.at(15) >> 4) * 10) + (strGpsData.at(15) & 0x0f);
			int lat4 = ((strGpsData.at(16) >> 4) * 10) + (strGpsData.at(16) & 0x0f);
			int lat5 = ((strGpsData.at(17) >> 4) * 10) + (strGpsData.at(17) & 0x0f);
			double latitude = (lat1)* 100 + (lat2)* 1 + (lat3)* 0.01 + (lat4)* 0.0001 + (lat5)* 0.000001;
			m_gspInfo.longitude = (int)(longitude / 100) + (longitude - (int)(longitude / 100) * 100) / 60;
			m_gspInfo.latitude = (int)(latitude / 100) + (latitude - (int)(latitude / 100) * 100) / 60;
			m_gspInfo.sN = strGpsData.at(18)==0x53?1:0;//南北
			m_gspInfo.gps_state = strGpsData.at(19); // A/V
			m_gspInfo.hasData = 1;
			m_gspInfo.scanIndex = frameNum;
		}

		//处理雷达数据
		if (strSampleData.size())
		{
			if (curPocketNum == 0)
			{//子包序号为0
				lastPocketNum = curPocketNum;
				m_sampleData.clear();
				m_sampleData.append(strSampleData);
				if (curPocketNum == m_maxPocketNum - 1)//128采样点仅有一包数据
				{ //合成数据
					dataPreprocess(m_sampleData);
					/*
					0 标志头 1 int 00007FFFF
					1 打标信息 1 int 0x00004000 大标0x00008000 小标
					2 道号信息 1 int
					3 雷达 （采样点数-3）*4B  缺少3个采样点，目前是开头3个
					*/
					int head = 0x7FFF;
					int mark = 0x0000; //打标
					m_markMutex.lock();
					if (m_vecMark.size())
					{
						if (m_vecMark.at(0) == 0) //短标
						{
							mark = 0x4000;
						}
						else if (m_vecMark.at(0) == 1){//长标
							mark = 0x8000;
						}
						m_vecMark.erase(m_vecMark.begin());
						cout << "mark =" << mark << endl;
					}
					m_markMutex.unlock();
					m_sampleData.replace(0, 4, reinterpret_cast<const char*>(&head), 4);
					m_sampleData.replace(4, 4, reinterpret_cast<const char*>(&mark), 4);
					m_sampleData.replace(8, 4, reinterpret_cast<const char*>(&frameNum), 4);
					//文件保存
					dataSave(m_sampleData, frameNum);
					//背景消除
					removeBack(m_sampleData);
					//软件增益
					softSegmentedGain(m_sampleData);
					m_sampleData.replace(0, 4, reinterpret_cast<const char*>(&head), 4);
					m_sampleData.replace(4, 4, reinterpret_cast<const char*>(&mark), 4);
					m_sampleData.replace(8, 4, reinterpret_cast<const char*>(&frameNum), 4);
					m_radarScan.push(m_sampleData);
				}
			}
			else
			{ //子包序号为0
				bool nRet = (lastPocketNum + 1 == curPocketNum) && m_sampleData.size(); //序号满足要求且存储过首包数据
				if (nRet)
				{
					lastPocketNum = curPocketNum;
					m_sampleData.append(strSampleData);
					if (curPocketNum == m_maxPocketNum - 1)
					{//是否为最后一包数据
						dataPreprocess(m_sampleData);
						/*
						0 标志头 1 int 00007FFFF
						1 打标信息 1 int 0x00004000 大标0x00008000 小标
						2 道号信息 1 int
						3 雷达 （采样点数-3）*4B  缺少3个采样点，目前是开头3个
						*/
						int head = 0x7FFF;
						int mark = 0x00;
						m_markMutex.lock();
						if (m_vecMark.size())
						{
							if (m_vecMark.at(0) == 0) //短标
							{
								mark = 0x4000;
							}
							else if (m_vecMark.at(0) == 1){//长标
								mark = 0x8000;
							}
							m_vecMark.erase(m_vecMark.begin());
							cout << "mark =" << mark << endl;
						}
						m_markMutex.unlock();

						m_sampleData.replace(0, 4, reinterpret_cast<const char*>(&head), 4);
						m_sampleData.replace(4, 4, reinterpret_cast<const char*>(&mark), 4);
						m_sampleData.replace(8, 4, reinterpret_cast<const char*>(&frameNum), 4);
						//文件保存
						dataSave(m_sampleData, frameNum);
						//搜救处理
						rescueProcsss(m_sampleData);
						//背景消除
						removeBack(m_sampleData);
						//软件增益
						softSegmentedGain(m_sampleData);
						m_sampleData.replace(0, 4, reinterpret_cast<const char*>(&head), 4);
						m_sampleData.replace(4, 4, reinterpret_cast<const char*>(&mark), 4);
						m_sampleData.replace(8, 4, reinterpret_cast<const char*>(&frameNum), 4);
						m_radarScan.push(m_sampleData);
					}
				}
				else{
					m_sampleData.clear();
				}
			}//else
		}//strSampleData
		m_SiProcessBuf[m_SIProcess].pop();
	}
}

//获取数据
int RadarDevice::receivedData(char*data, int *step, int*size)
{
	m_qMutex.lock();
	*size = m_radarDataQueue.size();
	if (*size <= 0){
		m_qMutex.unlock();
		return HISENSE1;
	}
	int length = 0;
	int samplePopint = 0;
	for (int i = 0; i < *size; i++){
		string strMsg = m_radarDataQueue.front();
		*step = strMsg.length();
		memcpy(data + length, (void*)strMsg.c_str(), strMsg.length());
		length += strMsg.length();
		m_radarDataQueue.pop();
	}
	m_qMutex.unlock();	
	return HISENSE0;
}

//停止保存数据
int RadarDevice::beginSaveData(string fileDir, void *user)
{
	if (!fileDir.size())
		return HISENSE_1;
	if (!fileDir.find(".lte"))
		return HISENSE_1;

	vector<string> vecFile = split(fileDir, ".lte");
	string gpsFileName = vecFile.at(0) + ".gps";

	//创建雷达文件
	m_file = fopen(fileDir.c_str(), "ab+");
	if (m_file == nullptr)
		return HISENSE_1;
	if (m_gspInfo.gpsEnabled){
		m_gpsFile = fopen(gpsFileName.c_str(), "ab+");
		if (m_gpsFile == nullptr)
			return HISENSE_1;
	}
	///雷达文件头
	{
		HEAD_FILEHEADER	m_headRadar;
		memset(&m_headRadar, 0, sizeof(m_headRadar));
		m_headRadar.lh_bits = 32; //数据位数
		m_headRadar.lh_nsamp = m_samplingPoints; // 采样点
		m_headRadar.lh_spp = m_antenaType; //天线频率
		m_headRadar.lh_pos = m_signalPosition; //信号位
		m_headRadar.lh_range = m_timeWindow; //时窗(ns)
		m_headRadar.lh_work = triggerModeParam.triggerMode == 1 ? 
			0 : (triggerModeParam.triggerMode == 2?2:1); //工作模式(连续|点测|轮测)(0,1,2)
		m_headRadar.lh_extent = triggerModeParam.extentMark; //标记扩展
		m_headRadar.lh_mpm = triggerModeParam.pluseInterval; //脉冲间隔
		m_headRadar.lh_sps = m_scanSpeed; //模式
		for (int i = 9; i < 18; i++){
			m_headRadar.lh_rgainf[i] = gainParam.segmentedGainSoft[i-9];
		}
		//创建文件头
		fwrite(&m_headRadar.lh_tag, 1, sizeof(m_headRadar.lh_tag), m_file);
		fwrite(&m_headRadar.lh_data, 1, sizeof(m_headRadar.lh_data), m_file);
		fwrite(&m_headRadar.lh_nsamp, 1, sizeof(m_headRadar.lh_nsamp), m_file);//取样点
		fwrite(&m_headRadar.lh_bits, 1, sizeof(m_headRadar.lh_bits), m_file);
		fwrite(&m_headRadar.lh_zero, 1, sizeof(m_headRadar.lh_zero), m_file);
		fwrite(&m_headRadar.lh_sps, 1, sizeof(m_headRadar.lh_sps), m_file); //扫速
		fwrite(&m_headRadar.lh_spm, 1, sizeof(m_headRadar.lh_spm), m_file);
		fwrite(&m_headRadar.lh_mpm, 1, sizeof(m_headRadar.lh_mpm), m_file);
		fwrite(&m_headRadar.lh_pos, 1, sizeof(m_headRadar.lh_pos), m_file);
		fwrite(&m_headRadar.lh_range, 1, sizeof(m_headRadar.lh_range), m_file); //时窗
		fwrite(&m_headRadar.lh_spp, 1, sizeof(m_headRadar.lh_spp), m_file);    //天线
		fwrite(&m_headRadar.lh_create, 1, sizeof(m_headRadar.lh_create), m_file);
		fwrite(&m_headRadar.lh_modif, 1, sizeof(m_headRadar.lh_modif), m_file);
		fwrite(&m_headRadar.lh_rgain, 1, sizeof(m_headRadar.lh_rgain), m_file);
		fwrite(&m_headRadar.lh_nrgain, 1, sizeof(m_headRadar.lh_nrgain), m_file);
		fwrite(&m_headRadar.lh_text, 1, sizeof(m_headRadar.lh_text), m_file);
		fwrite(&m_headRadar.lh_ntext, 1, sizeof(m_headRadar.lh_ntext), m_file);
		fwrite(&m_headRadar.lh_proc, 1, sizeof(m_headRadar.lh_proc), m_file);
		fwrite(&m_headRadar.lh_nproc, 1, sizeof(m_headRadar.lh_nproc), m_file);
		fwrite(&m_headRadar.lh_nchan, 1, sizeof(m_headRadar.lh_nchan), m_file);
		fwrite(&m_headRadar.lh_epsr, 1, sizeof(m_headRadar.lh_epsr), m_file);
		fwrite(&m_headRadar.lh_top, 1, sizeof(m_headRadar.lh_top), m_file);
		fwrite(&m_headRadar.lh_depth, 1, sizeof(m_headRadar.lh_depth), m_file);//66
		fwrite(&m_headRadar.lh_npass, 1, sizeof(m_headRadar.lh_npass), m_file);
		fwrite(&m_headRadar.lh_device, 1, sizeof(m_headRadar.lh_device), m_file);
		fwrite(&m_headRadar.lh_file, 1, sizeof(m_headRadar.lh_file), m_file);
		fwrite(&m_headRadar.lh_gps, 1, sizeof(m_headRadar.lh_gps), m_file);
		fwrite(&m_headRadar.lh_gpsform, 1, 4, m_file);
		fwrite(&m_headRadar.lh_anten, 1, 4, m_file);
		fwrite(&m_headRadar.lh_reserv1, 1, 9, m_file);
		fwrite(&m_headRadar.lh_lrmanual, 1, sizeof(m_headRadar.lh_lrmanual), m_file);
		fwrite(&m_headRadar.lh_rrmanual, 1, sizeof(m_headRadar.lh_rrmanual), m_file);
		fwrite(&m_headRadar.lh_lrlong, 1, sizeof(m_headRadar.lh_lrlong), m_file);
		fwrite(&m_headRadar.lh_lrshort, 1, sizeof(m_headRadar.lh_lrshort), m_file);
		fwrite(&m_headRadar.lh_rrlong, 1, sizeof(m_headRadar.lh_rrlong), m_file);
		fwrite(&m_headRadar.lh_rrshort, 1, sizeof(m_headRadar.lh_rrshort), m_file);
		fwrite(&m_headRadar.lh_peg, 1, sizeof(m_headRadar.lh_peg), m_file);
		fwrite(&m_headRadar.lh_horuler, 1, sizeof(m_headRadar.lh_horuler), m_file);
		fwrite(&m_headRadar.lh_extent, 1, sizeof(m_headRadar.lh_extent), m_file);
		fwrite(&m_headRadar.lh_work, 1, sizeof(m_headRadar.lh_work), m_file);
		fwrite(&m_headRadar.lh_chanmask, 1, sizeof(m_headRadar.lh_chanmask), m_file);
		fwrite(&m_headRadar.lh_fname, 1, 12, m_file);
		fwrite(&m_headRadar.lh_chksum, 1, sizeof(m_headRadar.lh_chksum), m_file);//128
		fwrite(&m_headRadar.lh_rgainf, 1, 88, m_file);
		fwrite(&m_headRadar.lh_reserv2, 1, 732, m_file);
		fwrite(&m_headRadar.lh_3dParam, 1, sizeof(m_headRadar.lh_3dParam), m_file);
		fwrite(&m_headRadar.lh_pegParam, 1, sizeof(m_headRadar.lh_pegParam), m_file);
		fwrite(&m_headRadar.lh_reserv3, 1, 24, m_file);
		m_user = (int*)user;
	}
	///GPS头文件
	if (m_gpsFile)
	{
		string byteArray;
		//文件标识1-6
		string fileFlag("\x47\x50\x53\x00\x00\x00", 6);
		byteArray.append(fileFlag);
		//gps数据偏移量7-8
		short gps_data = 1024;
		byteArray.append((char*)&gps_data, sizeof(gps_data));
		//定位设备类型9-10
		short gps_device = 0;
		byteArray.append((char*)&gps_device, sizeof(gps_device));
		//定位设备编号11-30
		string gps_num(20, '\x0');
		byteArray.append(gps_num);
		//数据生成日期31-34
		int gps_create = 0;
		gps_create |= (m_gspInfo.year - 1980) << 25;
		gps_create |= m_gspInfo.mon << 21;
		gps_create |= m_gspInfo.day << 16;
		gps_create |= m_gspInfo .hour<< 11;
		gps_create |= m_gspInfo.min << 5;
		gps_create |= m_gspInfo.sec / 2;
		byteArray.append((char*)&gps_create, sizeof(gps_create));
		//数据修改日期35-38
		string gps_modif(4, '\x0');
		byteArray.append(gps_modif);
		//存储的数据格式39-44
		string gps_tag("\x52\x4D\x43\x00\x00\x00", 6);
		byteArray.append(gps_tag);
		///GPS定位状态 45-46
		short gps_state = m_gspInfo.gps_state;
		byteArray.append((char*)&gps_state,sizeof(gps_state));
		//纬向47-48
		short gps_latdirection = m_gspInfo.sN;//北纬
		byteArray.append((char*)&gps_latdirection, sizeof(gps_latdirection));
		//经向49-50
		short gps_longdirection = m_gspInfo.wE;//东经
		byteArray.append((char*)&gps_longdirection, sizeof(gps_longdirection));
		//每道数据长度51-52
		short gps_nsamp = 0x3A;
		byteArray.append((char*)&gps_nsamp, sizeof(gps_nsamp));
		//道头53-54
		short gps_mflag = 0x38;
		byteArray.append((char*)&gps_mflag, sizeof(gps_mflag));
		//索引偏移55-56
		short gps_index = 0;
		byteArray.append((char*)&gps_index, sizeof(gps_index));
		//存储经度长度57-58
		short gps_islatitude = 0x0008;
		byteArray.append((char*)&gps_islatitude, sizeof(gps_islatitude));
		//存储纬度长度59-60
		short gps_islongitude = 0x0008;
		byteArray.append((char*)&gps_islongitude, sizeof(gps_islongitude));
		//存储高度长度61-62
		short gps_ishigh = 0x0008;
		byteArray.append((char*)&gps_ishigh, sizeof(gps_ishigh));
		//设备类型63-64
		short use_device = 0x0000;
		byteArray.append((char*)&use_device, sizeof(use_device));
		//卫星星数65
		short gps_star = 0x0000;
		byteArray.append((char*)&gps_star, 1);
		//预留66-1024
		string gps_resver(959, '\x0');
		byteArray.append(gps_resver);
		fwrite(byteArray.c_str(),byteArray.length(), 1, m_gpsFile);
	}
	m_dataSave = true;
	return HISENSE0;
}

//结束保存数据
int RadarDevice::endSaveData()
{
	m_dataSave = false;
	if (m_file)
		fclose(m_file);
	if (m_gpsFile)
		fclose(m_gpsFile);
	m_file = nullptr;
	m_gpsFile = nullptr;
	m_user = nullptr;
	return 0;
}

//整机配置
int RadarDevice::lowerComputerConfig(string ip, short devType, string cardSerialNum,
	short deviceSerialNum, short versionNum, short calibrationValue, short antennaCode, short frqValue)
{
#ifdef EFS_DEBUG
	cout << "sendGainParam" << endl;
#endif
	if (m_dllVersion != PRO_DLL_CONFIG)
		return HISENSE1;
	memset(szSend, 0, sizeof(szSend));
	int size = 0;
	m_radarQuery->DownLowerComputerConfig(szSend,size,ip, devType, cardSerialNum,
		deviceSerialNum, versionNum, calibrationValue,antennaCode,frqValue);
	m_iPort->SendData(szSend, size);
	return HISENSE0;
}

//获取GPS数据
int RadarDevice::receivedGpsData(double * longitude, short* longdirection, 
	double * latitude, short* latdirection, short *status)
{
	if (m_gspInfo.hasData == 0)
		return HISENSE_1;
	*longitude = m_gspInfo.longitude;
	*longdirection = m_gspInfo.wE;
	*latitude = m_gspInfo.latitude;
	*latdirection = m_gspInfo.sN;
	*status = m_gspInfo.gps_state == 0x41 ? 1 : 0;
	return HISENSE0;
}

///获取搜救结果数据
int RadarDevice::receivedRescueResult(bool *isEnd, short *resultType,
	short *distance, short *detectionBegin, short*detecetionEnd)

{
	m_targetMutex.lock();
	int size = m_queueResultTarget.size();
	if (size > 0)
	{
		cout << "size=" << size << endl;
		auto info = m_queueResultTarget.at(0);
		*resultType = info.isExistResult ? 0x01 : 0x00;
		*isEnd = info.isDetectionEnd;
		*distance = info.detectionBegin * 100 + info.targetPos * 3.0 * pow(10, 8) * 100 / 2048 * 21 / 2 / pow(10, 9);
		*detectionBegin = info.detectionBegin;
		*detecetionEnd = info.detectionBegin + info.detectionInterval;
		m_queueResultTarget.erase(m_queueResultTarget.begin());
		m_targetMutex.unlock();
		return HISENSE0;
	}
	m_targetMutex.unlock();
	return HISENSE1;
}
//运行状态
int RadarDevice::runningStatus(bool*isRunning)
{
	*isRunning = m_isLinked;
	return HISENSE0;
}

//处理保存设置命令
int RadarDevice::HandleSaveSetting(string strMsg, bool isOnLine, 
	string &errorString)
{
#ifdef EFS_DEBUG
	cout << "HandleSaveSetting,strMsg=" << strMsg 
		 << ",isOnLine=" << isOnLine << endl;
#endif

	if (!m_isLinked)
	{
		errorString = "network connection Failed!!!";
		return HISENSE2;
	}

	try{
		vector<string> vecMsg = split(strMsg, "\"");
		if (isOnLine)
			sendStop();
		//2 设置
		if (strMsg.find(samplingPoints) != string::npos)
		{
			string strValue;
			for (int i = 0; i < vecMsg.size(); i++)
			{
				if (vecMsg[i] == samplingPoints)
				{
					strValue = vecMsg[i + 1];
					break;
				}
			}
			vector<string> vecValue = split(strValue, ":");
			vecValue = split(vecValue[0], ",");
			m_samplingPoints = stod(vecValue[0]);
			cout << "m_samplingPoints =" << m_samplingPoints << endl;
			Sleep(DELAT_TIME);
			m_maxPocketNum = m_samplingPoints / 128;
			m_qMutex.lock();
			m_radarDataQueue.empty();
			m_qMutex.unlock();
			if (isOnLine)
				sendTimeParam();
		}

		if (strMsg.find(signalPosition) != string::npos)
		{
			string strValue;
			for (int i = 0; i < vecMsg.size(); i++)
			{
				if (vecMsg[i] == signalPosition)
				{
					strValue = vecMsg[i + 1];
					break;
				}
			}
			vector<string> vecValue = split(strValue, ":");
			vecValue = split(vecValue[0], ",");
			m_signalPosInit = stod(vecValue[0]);
			m_signalPosition = m_signalPosInit;
			cout << "m_signalPosInit =" << m_signalPosInit << endl;
			if (isOnLine)
				sendTimeParam();
		}

		if (strMsg.find(timeWindow) != string::npos)
		{
			string strValue;
			for (int i = 0; i < vecMsg.size(); i++)
			{
				if (vecMsg[i] == timeWindow)
				{
					strValue = vecMsg[i + 1];
					break;
				}
			}
			vector<string> vecValue = split(strValue, ":");
			vecValue = split(vecValue[0], ",");
			m_timeWindow = stod(vecValue[0]);
			cout << "m_timeWindow =" << m_timeWindow << endl;
			if (isOnLine)
				sendTimeParam();
		}

		if (strMsg.find(signalGain) != string::npos)
		{
			string strValue;
			for (int i = 0; i < vecMsg.size(); i++)
			{
				if (vecMsg[i] == signalGain)
				{
					strValue = vecMsg[i + 1];
					break;
				}
			}
			vector<string> vecValue = split(strValue, ":");
			vecValue = split(vecValue[0], "[");
			vecValue = split(vecValue[0], "]"); 
			vecValue = split(vecValue[0], ",");
			for (int i = 0; i < 9; i++){
				int manualGain = (short)(4 * (pow(10, 0.05*(stod(vecValue[i]) + 10)) - 1));
				gainParam.segmentedGain[i] = manualGain <= gainParam.segmentedGainMax ? manualGain : gainParam.segmentedGainMax;
				gainParam.segmentedGainSoft[i] = manualGain <= gainParam.segmentedGainMax ? 0 : manualGain-gainParam.segmentedGainMax;
			}
			if (isOnLine)
				sendGainParam();
		}

		if (strMsg.find(scanSpeed) != string::npos)
		{
			string strValue;
			for (int i = 0; i < vecMsg.size(); i++)
			{
				if (vecMsg[i] == scanSpeed)
				{
					strValue = vecMsg[i + 1];
					break;
				}
			}
			vector<string> vecValue = split(strValue, ":");
			vecValue = split(vecValue[0], ",");
			m_scanSpeed = stod(vecValue[0]);
			cout << "m_scanSpeed =" << m_scanSpeed << endl;
			if (isOnLine)
				sendTimeParam();
		}

		if (strMsg.find(filterEnabled) != string::npos)
		{
			string strValue;
			for (int i = 0; i < vecMsg.size(); i++)
			{
				if (vecMsg[i] == filterEnabled)
				{
					strValue = vecMsg[i + 1];
					break;
				}
			}
			vector<string> vecValue = split(strValue, ":");
			vecValue = split(vecValue[0], ",");
			m_filterEnabled = stod(vecValue[0]);
			cout << "m_filterEnabled =" << m_filterEnabled << endl;
		}

		if (strMsg.find(RemoveBack) != string::npos)
		{
			string strValue;
			for (int i = 0; i < vecMsg.size(); i++)
			{
				if (vecMsg[i] == RemoveBack)
				{
					strValue = vecMsg[i + 1];
					break;
				}
			}
			vector<string> vecValue = split(strValue, ":");
			vecValue = split(vecValue[0], ",");
			m_bRemoveBack = stod(vecValue[0]);
			cout << "m_bRemoveBack =" << m_bRemoveBack << endl;
		}

		if (strMsg.find(mark) != string::npos)
		{
			string strValue;
			for (int i = 0; i < vecMsg.size(); i++)
			{
				if (vecMsg[i] == mark)
				{
					strValue = vecMsg[i + 1];
					break;
				}
			}
			vector<string> vecValue = split(strValue, ":");
			vecValue = split(vecValue[0], ",");
			int m_mark = stod(vecValue[0]);
			m_markMutex.lock();
			m_vecMark.push_back(m_mark);
			m_markMutex.unlock();
			cout << "m_mark =" << m_mark << endl;
		}

		if (strMsg.find(lightLevel) != string::npos)
		{
			string strValue;
			for (int i = 0; i < vecMsg.size(); i++)
			{
				if (vecMsg[i] == lightLevel)
				{
					strValue = vecMsg[i + 1];
					break;
				}
			}
			vector<string> vecValue = split(strValue, ":");
			vecValue = split(vecValue[0], ",");
			m_lightLevel = stod(vecValue[0]);
			cout << "m_lightLevel =" << m_lightLevel << endl;
			if (isOnLine && m_dllVersion == PRO_DLL_WATER)	
				sendLightLevel();
		}
	
		if (strMsg.find(correctZero) != string::npos)
		{
			string strValue;
			for (int i = 0; i < vecMsg.size(); i++)
			{
				if (vecMsg[i] == correctZero)
				{
					strValue = vecMsg[i + 1];
					break;
				}
			}
			vector<string> vecValue = split(strValue, ":");
			vecValue = split(vecValue[0], ",");
			m_correctZero = stod(vecValue[0]);
			cout << "m_correctZero =" << m_correctZero << endl;
		}

		if (strMsg.find(automaticGain) != string::npos)
		{
			string strValue;
			for (int i = 0; i < vecMsg.size(); i++)
			{
				if (vecMsg[i] == automaticGain)
				{
					strValue = vecMsg[i + 1];
					break;
				}
			}
			vector<string> vecValue = split(strValue, ":");
			vecValue = split(vecValue[0], ",");
			gainParam.automaticGain = stod(vecValue[0]);
			gainParam.isGainEnd = gainParam.automaticGain ? 0 : gainParam.isGainEnd;
			cout << "automaticGain =" << gainParam.automaticGain <<
				",isGainEnd =" << gainParam.isGainEnd << endl;
		}

		if (strMsg.find(gainSplit) != string::npos)
		{
			string strValue;
			for (int i = 0; i < vecMsg.size(); i++)
			{
				if (vecMsg[i] == gainSplit)
				{
					strValue = vecMsg[i + 1];
					break;
				}
			}
			vector<string> vecValue = split(strValue, ":");
			vecValue = split(vecValue[0], ",");
			gainParam.segmentedGainMax = stod(vecValue[0]);
			cout << "segmentedGainMax = " << 
				gainParam.segmentedGainMax << endl;
		}
		
		if (strMsg.find(workMode) != string::npos)
		{
			string strValue;
			for (int i = 0; i < vecMsg.size(); i++)
			{
				if (vecMsg[i] == workMode)
				{
					strValue = vecMsg[i + 1];
					break;
				}
			}
			vector<string> vecValue = split(strValue, ":");
			vecValue = split(vecValue[0], ",");
			triggerModeParam.triggerMode = stod(vecValue[0]);
			if (isOnLine)
				sendTriggerMode();
			cout << "triggerMode = " << triggerModeParam.triggerMode << endl;
		}

		if (strMsg.find(extentMark) != string::npos)
		{
			string strValue;
			for (int i = 0; i < vecMsg.size(); i++)
			{
				if (vecMsg[i] == extentMark)
				{
					strValue = vecMsg[i + 1];
					break;
				}
			}
			vector<string> vecValue = split(strValue, ":");
			vecValue = split(vecValue[0], ",");
			triggerModeParam.extentMark = stod(vecValue[0]);
			if (isOnLine)
				sendMarkExtended();
			cout << "extentMark = " << triggerModeParam.extentMark << endl;
		}

		if (strMsg.find(pluseInterval) != string::npos)
		{
			string strValue;
			for (int i = 0; i < vecMsg.size(); i++)
			{
				if (vecMsg[i] == pluseInterval)
				{
					strValue = vecMsg[i + 1];
					break;
				}
			}
			vector<string> vecValue = split(strValue, ":");
			vecValue = split(vecValue[0], ",");
			triggerModeParam.pluseInterval = stod(vecValue[0]);
			cout << " pluseInterval = " << triggerModeParam.pluseInterval << endl;
		}

		if (strMsg.find(gpsEnabled) != string::npos)
		{
			string strValue;
			for (int i = 0; i < vecMsg.size(); i++)
			{
				if (vecMsg[i] == gpsEnabled)
				{
					strValue = vecMsg[i + 1];
					break;
				}
			}
			vector<string> vecValue = split(strValue, ":");
			vecValue = split(vecValue[0], ",");
			m_gspInfo.gpsEnabled = stod(vecValue[0]);
			cout << " gpsEnabled = " << gpsEnabled  << endl;
		}

		if (m_dllVersion == PRO_DLL_RESCUE)
		{	
			m_classifyBreath->changeParams(m_scanSpeed, m_antenaType, m_timeWindow, m_samplingPoints);
			m_classifyBreath->init_breath();
			///探测位置
			if (strMsg.find(detectionPos) != string::npos)
			{
				string strValue;
				for (int i = 0; i < vecMsg.size(); i++)
				{
					if (vecMsg[i] == detectionPos)
					{
						strValue = vecMsg[i + 1];
						break;
					}
				}
				vector<string> vecValue = split(strValue, ":");
				vecValue = split(vecValue[0], ",");
				m_detectionPosInit = stod(vecValue[0]);
				m_detectionPos = m_detectionPosInit;
				m_signalPosInit += m_detectionPosInit / m_deteInterval * m_timeWindow;
				m_signalPosition = m_signalPosInit;
				cout << " m_detectionPosInit = " << m_detectionPosInit << endl;
			}
		}

		//3 开
		if (isOnLine)
			sendStart();
	}
	catch (exception e){
		cout << "HandlSaveSetting:Exception!!!strMsg=" << strMsg << endl;
		return HISENSE2;
	}
	return HISENSE0;
}

//数据预处理
int RadarDevice::dataPreprocess(string &sampleData)
{
	//自动增益
	if (gainParam.automaticGain)
	{
		if (!gainParam.isGainEnd)
		{//首次计算
			int segmentedGain[9] = { 0 };
			int segmentedGainSoft[9] = { 0 };
			int dataIn[8192] = { 0 };//最大采样点
			transformToInt(sampleData, dataIn);
			ZDZY(m_samplingPoints, (int*)dataIn, m_antenaType, gainParam.segmentedGainMax, segmentedGain, segmentedGainSoft, gainParam.isGainEnd);
			if (gainParam.isGainEnd)
			{
				gainParam.automaticGain = 0;
				for (int i = 0; i < 9; i++){
					gainParam.segmentedGain[i] = segmentedGain[i];
					gainParam.segmentedGainSoft[i] = segmentedGainSoft[i];
				}
				sendStop();
				sendGainParam();
				sendStart();
			}
		}
	}

	if (!m_filterEnabled && !m_correctZero)
		return HISENSE0;

	float dataOut[8192 * 4] = { 0 };
	float dataIn[8192 * 4] = { 0 };
	float dataBuf[8192 * 4] = { 0 };

	int size = byteToFloat(sampleData, dataIn);
	//校正零偏
	if (m_correctZero)
		CorrectZeroOffset(dataIn, dataBuf, m_samplingPoints, m_antenaType, m_timeWindow);
	else{
		std::swap(dataBuf, dataIn);
	}
	//软件滤波
	if (m_filterEnabled)
		FIRFilterSingle(dataOut, dataBuf, m_samplingPoints, m_timeWindow, 30, 3, 1, m_antenaType / 2, m_antenaType*2);
	else{
		std::swap(dataBuf, dataOut);
	}

	sampleData.clear();
	for (int i = 0; i < size; i++){
		int value = *(dataOut + i);  //丢掉小数
		sampleData.append(1, (unsigned char)value);
		sampleData.append(1, (unsigned char)(value >> 8));
		sampleData.append(1, (unsigned char)(value >> 16));
		sampleData.append(1, (unsigned char)(value >> 24));
	}
	return HISENSE0;
}

int m_backScans = 0;
int backWnd = 100;
int dataOff = 4;
int m_backDatas[4096] = {0};
#define LTE_MAX_VALUE  /*2,147,483,647*/8388607
#define LTE_MIN_VALUE  /*-2,147,483,647*/-8388607

//背景消除
void RadarDevice::removeBack(string &sampleData)
{
	//背景消除
	if (m_bRemoveBack)
	{
		if (m_backScans<backWnd)
		{
			for (int i = dataOff; i< m_samplingPoints; i++)
			{
				int *data = (int*)&sampleData[4*i];
				m_backDatas[i] = (m_backDatas[i] * m_backScans + *data) / (m_backScans + 1);
				int tempData = (*data - m_backDatas[i]);
				if (tempData>LTE_MAX_VALUE)
				{
					tempData = LTE_MAX_VALUE;
				}
				if (tempData<LTE_MIN_VALUE)
				{
					tempData = LTE_MIN_VALUE;
				}
				sampleData[4 * i] = tempData & 0xff;
				sampleData[4 * i + 1] = tempData>>8 & 0xff;
				sampleData[4 * i + 2] = tempData>>16 & 0xff;
				sampleData[4 * i + 3] = tempData>>24 & 0xff;
			}
			m_backScans++;
		}
		else
		{
			for (int i = dataOff; i<m_samplingPoints; i++)
			{
				int *data = (int*)&sampleData[4 * i];
				m_backDatas[i] = (m_backDatas[i] * (backWnd - 1) + *data) / backWnd;
				int tempData = (*data - m_backDatas[i]);
				if (tempData>LTE_MAX_VALUE)
				{
					tempData = LTE_MAX_VALUE;
				}
				if (tempData<LTE_MIN_VALUE)
				{
					tempData = LTE_MIN_VALUE;
				}
				sampleData[4 * i] = tempData & 0xff;
				sampleData[4 * i + 1] = tempData >> 8 & 0xff;
				sampleData[4 * i + 2] = tempData >> 16 & 0xff;
				sampleData[4 * i + 3] = tempData >> 24 & 0xff;
			}
		}
	}
}

//软件增益
void RadarDevice::softSegmentedGain(string &sampleData)
{
	int k = 1;
	float valueTmp = 0;
	int m_wordLength = 4;
	int wordLength = (m_wordLength>4) ? 4 : m_wordLength;
	int m_columnCount = sampleData.length() / m_wordLength;
	int leftScroll_max = ((m_wordLength - 1) << 3);
	bool m_isSigned = true;
	int m_stepSize = m_columnCount>>3;

	for (int j = 0, column = 0; j < m_columnCount; j++, column += m_wordLength)
	{
		int value = 0;
		if (m_isSigned)//是否有符号数据
		{//有符号数据
			bool isNegativeNumber = false;
			unsigned char numberChar;
			for (int i = m_wordLength - 1, leftScroll = leftScroll_max;i >= m_wordLength - wordLength;i--, leftScroll -= 8)//小端方式读取
			{
				numberChar = (unsigned char)(sampleData.at(column + i));
				if (i == m_wordLength - 1)
				{
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
		}
		else
		{//无符号数据
			for (int i = m_wordLength - 1, leftScroll = leftScroll_max;i >= m_wordLength - wordLength;
			i--, leftScroll -= 8)//小端方式读取
			{
				int tmp = (unsigned char)(sampleData.at(column + i));
				value += (tmp << (leftScroll));
			}
		}
		//软件增益算法
		if (j < m_stepSize*k){
			float tmp = (float)(gainParam.segmentedGainSoft[k] - gainParam.segmentedGainSoft[k - 1]) / m_stepSize;
			valueTmp = gainParam.segmentedGainSoft[k - 1] + (j - m_stepSize*(k - 1))*tmp;
		}
		else{
			valueTmp = gainParam.segmentedGainSoft[k];
			k++;
		}

		if (j == m_columnCount - 1){
			valueTmp = gainParam.segmentedGainSoft[k];
		}
		valueTmp = pow(10, valueTmp / 20);
		value *= valueTmp;
		sampleData[4 * j] = value & 0xff;
		sampleData[4 * j + 1] = value >> 8 & 0xff;
		sampleData[4 * j + 2] = value >> 16 & 0xff;
		sampleData[4 * j + 3] = value >> 24 & 0xff;
	}//for
}

//搜救处理
void RadarDevice::rescueProcsss(string &sampleData)
{
#ifdef EFS_DEBUG
	cout << "rescueProcsss" << endl;
#endif
	if (m_dllVersion != PRO_DLL_RESCUE)
		return;
	//1 预处理
	short dataIn[8192] = { 0 };
	transformToShort(sampleData, dataIn);
	m_classifyBreath->detect_breath_pre(dataIn);
	//2 处理
	float breath_th = 1;
	short dataOut[20];
	int isEnd = 0;
	m_classifyBreath->detect_breath(dataOut, breath_th, isEnd);
	//3 结果处理
	if (isEnd)
	{
		TargetInfo info;
		info.isExist = dataOut[4] == 1;
		info.targetPos = dataOut[5];
		info.detectionBegin = m_detectionPos;
		info.detectionInterval = m_deteInterval;
		info.isExistResult = false;
		info.isDetectionEnd = false;
		info.isJump = false;

		cout << "isEnd!!!,targetPos=" << info.targetPos 
			<< ",isExist=" << info.isExist 
			<< ",m_detectionPos = " << info.detectionBegin
			<< ",time = " << time(0) << endl;

		m_queueTarget.push_back(info);  //结果计算队列
		if (m_queueTarget.size() == RESCUE_QUEUE_SIZE)
		{
			m_targetSize = 0;
			TargetInfo targetInfo;
			for (int k = 0; k < m_queueTarget.size(); k++){
				if (m_queueTarget[k].isExist){
					m_targetSize += 1;
					targetInfo = m_queueTarget[k];
				}
			}
			if (m_targetSize >= RESCUE_TARGET_SIZE){
				info = targetInfo;
				info.isExistResult = true;
				cout << "existResult!!!!!" << endl;
				info.isJump = true;
			}
			m_queueTarget.erase(m_queueTarget.begin());
		}

		if (!info.isJump)
		{//不跳的时候
			int dTime = time(0) - m_lastTime;
			if (dTime > 70 && dTime < 140){
				info.isJump = m_targetSize < RESCUE_TARGET_SIZE && m_targetSize>0 ? false : true;
			}
			else if (dTime > 140)
				info.isJump = true;
		}

		if (info.isJump)
		{
			cout << "jump!!!!" << endl;
			m_detectionPos += m_deteInterval; 
			m_lastTime = time(0);
			if (m_detectionPos >= 36)
			{
				cout << "detectionEnd!!!!" << endl;
				info.isDetectionEnd = true; 
				m_detectionPos = m_detectionPosInit;
				m_targetMutex.lock();
				m_queueResultTarget.push_back(info);
				m_targetMutex.unlock();
				sendStop();
				return;
			}
			else
			{//探测中
				sendStop();
				m_queueTarget.clear();
				m_signalPosition += m_timeWindow;
				m_classifyBreath->changeParams(m_scanSpeed, m_antenaType, m_timeWindow, m_samplingPoints);
				m_classifyBreath->init_breath();
				sendTimeParam();
				endSaveData();
				//beginSaveData(to_string(m_detectionPos) + ".lte", &frameNum);  //EX
				sendStart();
			}
		}

		if (info.isExist)
		{
			m_targetMutex.lock();
			m_queueResultTarget.push_back(info);
			m_targetMutex.unlock();
		}
	}
}

//处理开始命令
int RadarDevice::HandleStart(string &errorString)
{
#ifdef EFS_DEBUG
	cout << "HandleStart" << endl;
#endif
	if (!m_isLinked)
	{
		errorString = "network connection Failed!!!";
		return HISENSE2;
	}
	if (m_dllVersion == PRO_DLL_RESCUE){
		m_lastTime = time(0);
		m_detectionPos = m_detectionPosInit;
		m_isExistTarget = false;
		m_queueTarget.clear();
		m_queueResultTarget.clear();
		m_signalPosition = m_signalPosInit;
		m_isDetcEnd = false;
		//beginSaveData(to_string(m_detectionPos) + ".lte", &frameNum);   //EX
	}
	//先停止
	sendStop();
	Sleep(DELAT_TIME);
	sendTriggerMode();
	return HISENSE0;
}

//处理关闭命令
int RadarDevice::HandleStop(string &errorString)
{
#ifdef EFS_DEBUG
	cout << "HandleStop" << endl;
#endif
	if (!m_isLinked)
	{
		errorString = "network connection Failed!!!";
		return HISENSE2;
	}
	sendStop();
	return HISENSE0;
}

//处理运行状态命令
int RadarDevice::HandleRunningStatus()
{
#ifdef EFS_DEBUG
	cout << "HandleRunningStatus" << endl;
#endif
	return m_isLinked;
}

//发送结束
void RadarDevice::sendStop()
{
#ifdef EFS_DEBUG
	cout << "sendStop" << endl;
#endif
	memset(szSend, 0, sizeof(szSend));
	int size = 0;
	m_radarQuery->DownSingalCmd(szSend, size, Cmd_SendStop);
	bool nRet = m_iPort->SendData(szSend, size);
	m_isStart = false;
}

//发送开始
void RadarDevice::sendStart()
{
#ifdef EFS_DEBUG
	cout << "sendStart" << endl;
#endif
	memset(szSend, 0, sizeof(szSend));
	int size = 0;
	m_radarQuery->DownSingalCmd(szSend, size, Cmd_SendStart);
	m_isStart = true;
	m_iPort->SendData(szSend, size);
}

//发送查询版本
void RadarDevice::sendCheckVersion()
{
#ifdef EFS_DEBUG
	cout << "sendCheckVersion" << endl;
#endif
	memset(szSend, 0, sizeof(szSend));
	int size = 0;
	m_radarQuery->DownSingalCmd(szSend, size, Cmd_SendCheckVersion);
	m_iPort->SendData(szSend, size);
}


//发送自检
bool RadarDevice::sendSelfCheck()
{
#ifdef EFS_DEBUG
	cout << "sendSelfCheck" << endl;
#endif
	cout << "sendSelfCheck " << endl;
	memset(szSend, 0, sizeof(szSend));
	int size = 0;
	m_radarQuery->DownSingalCmd(szSend, size, Cmd_SendSelfChecking);
	return m_iPort->SendData(szSend, size);
}

//发送亮灯等级
bool RadarDevice::sendLightLevel()
{
#ifdef EFS_DEBUG
	cout << "sendLightLevel" << endl;
#endif
	memset(szSend, 0, sizeof(szSend));
	int size = 0;
	m_radarQuery->DownLightLevel(szSend, size, m_lightLevel);
	return m_iPort->SendData(szSend, size);
}


//系统参数
bool RadarDevice::sendSystemParam()
{
#ifdef EFS_DEBUG
	cout << "sendSystemParam" << endl;
#endif
	memset(szSend, 0, sizeof(szSend));
	int size = 0;
	m_radarQuery->DownSystemParam(szSend, size, systemParam);;
	return m_iPort->SendData(szSend, size);
}

//探测模式设置指令
void RadarDevice::sendTriggerMode()
{
#ifdef EFS_DEBUG
	cout << "sendTriggerMode" << endl;
#endif
	memset(szSend, 0, sizeof(szSend));
	int size = 0;
	m_radarQuery->DownTriggerModeCmd(szSend, size, triggerModeParam);
	m_iPort->SendData(szSend, size);
}

//时序参数设置指令
void RadarDevice::sendTimeParam()
{
#ifdef EFS_DEBUG
	cout << "sendTimeParam" << endl;
#endif
	memset(szSend, 0, sizeof(szSend));
	int size = 0;
	timeParam.EfsChannelmodel = m_EfsChannelmodel;   //CH2  2
	timeParam.EfsChannelnum = m_EfsChannelnum;   //CH2 2
	timeParam.steplen = m_antennaRepetition * 1000 / 1.0 / m_scanSpeed;  //最接近的整数  469
	timeParam.stepinterval = m_timeWindow * 1000 / (timeParam.steplen*10) + 1; //向上取整  11
	timeParam.cyclesteplen = m_stepLenCalibration + m_calibration; //800
	timeParam.fplulsefreqcount = m_fplulsefreqcount; //13
	timeParam.samplingclockfrequency = (static_cast<long long>(12500000) * 128 * m_timeWindow*1.0 / timeParam.stepinterval / m_antennaRepetition / m_samplingPoints / 137); //向上取整  864
	timeParam.counterbegin = (m_signalPosition + 40) / 8;  //6
	timeParam.delaybegin = ((m_signalPosition + 40) % 8) * 100; //400
	timeParam.counterend = (m_signalPosition + 40 + m_timeWindow) / 8; //12
	timeParam.samplingpoints = m_samplingPoints; //512
	timeParam.scanSpeed = m_scanSpeed; //256
	m_radarQuery->DownTimeParamCmd(szSend, size, timeParam);
	m_iPort->SendData(szSend, size);
}

//增益参数设置指令
void RadarDevice::sendGainParam()
{
#ifdef EFS_DEBUG
	cout << "sendGainParam" << endl;
#endif
	memset(szSend, 0, sizeof(szSend));
	int size = 0;
	m_radarQuery->DownGainParamCmd(szSend, size, gainParam);
	m_iPort->SendData(szSend, size);
}

//滤波参数设置指令
void RadarDevice::sendFilterParam()
{
#ifdef EFS_DEBUG
	cout << "sendFilterParam" << endl;
#endif
	memset(szSend, 0, sizeof(szSend));
	int size = 0;
	FilterParam filterParam;
	filterParam.EfsChannelnum = m_EfsChannelnum;
	filterParam.filterEnable = 0;
	filterParam.indexValue = 0x04;
	char filter[51 * 2] = {
		0x00, 0xfc, 0x8f, 0xfb, 0x35, 0xfb, 0xfc, 0xfa, 0xe6, 0xfa, 0xf3, 0xfa, 0x1c, 
		0xfb,0x55,0xfb,0x92, 0xfb, 0xc3, 0xfb, 0xdc, 0xfb, 0xd6, 0xfb, 0xae, 0xfb, 0x6a, 0xfb, 
		0x17, 0xfb, 0xc6, 0xfa, 0x8e, 0xfa, 0x86, 0xfa, 0xc0, 0xfa, 0x49, 0xfb, 0x22, 
		0xfc, 0x40, 0xfd, 0x8a, 0xfe, 0xdc, 0xff, 0x05, 0x01, 0xd3, 0x01, 0x14, 0x02, 
		0x9b, 0x01, 0x4f, 0x00, 0x28, 0xfe, 0x3a, 0xfb, 0xb3, 0xf7, 0xde, 0xf3, 0x1f, 
		0xf0, 0xe7, 0xec, 0xb3, 0xea, 0xfa, 0xe9, 0x26, 0xeb, 0x86, 0xee, 0x44, 0xf4, 
		0x5c, 0xfc, 0x9d, 0x06, 0xa4, 0x12, 0xe3, 0x1f, 0xab, 0x2d, 0x33, 0x3b, 0xaf, 
		0x47, 0x57, 0x52, 0x7d, 0x5a, 0x9b, 0x5f, 0x59, 0x61 };
 	memcpy(filterParam.filter, filter,102);
	m_radarQuery->DownFilterParamCmd(szSend, size, filterParam);
	m_iPort->SendData(szSend, size);
}

//标记扩展设置指令
void RadarDevice::sendMarkExtended()
{
#ifdef EFS_DEBUG
	cout << "sendMarkExtended" << endl;
#endif
	memset(szSend, 0, sizeof(szSend));
	int size = 0;
	m_radarQuery->DownMarkExtendedCmd(szSend, size, triggerModeParam);
	m_iPort->SendData(szSend, size);
}
