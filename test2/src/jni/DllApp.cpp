
#include "DllApp.h"

using namespace DAQ;

/** @brief 构造函数 */
DllApp::DllApp(string dllDir)
{
	m_dllDir = dllDir;
	m_ptrCDllFile = new CDllFile();
	data_size = 500;
	data_step = 1024 * 4;
}

/** @brief 析构函数 */
DllApp::~DllApp()
{
	delete m_ptrCDllFile;
}

/** @brief 继承自父类的启动函数 */
bool DllApp::Init()
{
	bool nRet = m_ptrCDllFile->LoadDll(m_dllDir.c_str());
	if (!nRet){
		return false;
	}

	//设备实例
	m_createInstance = (createInstance*)
		m_ptrCDllFile->FindFunction("createInstance");
	//析构设备实例
	m_releaseInstance = (releaseInstance*)
		m_ptrCDllFile->FindFunction("releaseInstance");
	//保存雷达设置参数
	m_saveSetting = (saveSetting*)
		m_ptrCDllFile->FindFunction("saveSetting");
    //打开
	m_radarStart = (RadarStart*)
		m_ptrCDllFile->FindFunction("start");
	//关闭
	m_radarStop = (RadarStop*)
		m_ptrCDllFile->FindFunction("stop");
	//获取数据
	m_receivedData = (receivedData*)
		m_ptrCDllFile->FindFunction("receivedData");
	//保存数据
	m_beginSaveData = (beginSaveData*)
		m_ptrCDllFile->FindFunction("beginSaveData");
	//保存数据
	m_endSaveData = (endSaveData*)
		m_ptrCDllFile->FindFunction("endSaveData");
	//运行状态
	m_runningStatus = (runningStatus*)
		m_ptrCDllFile->FindFunction("runningStatus");
	//整机配置
	m_lowerComputerConfig = (lowerComputerConfig*)
		m_ptrCDllFile->FindFunction("lowerComputerConfig");
	//获取GPS数据
	m_receivedGpsData = (receivedGpsData*)
		m_ptrCDllFile->FindFunction("receivedGpsData");
	//获取搜救结果数据
	m_receivedRescueResult = (receivedRescueResult*)
		m_ptrCDllFile->FindFunction("receivedRescueResult");
	return true;
}


