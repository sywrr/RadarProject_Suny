
#include "DllApp.h"

using namespace DAQ;

/** @brief ���캯�� */
DllApp::DllApp(string dllDir)
{
	m_dllDir = dllDir;
	m_ptrCDllFile = new CDllFile();
	data_size = 500;
	data_step = 1024 * 4;
}

/** @brief �������� */
DllApp::~DllApp()
{
	delete m_ptrCDllFile;
}

/** @brief �̳��Ը������������ */
bool DllApp::Init()
{
	bool nRet = m_ptrCDllFile->LoadDll(m_dllDir.c_str());
	if (!nRet){
		return false;
	}

	//�豸ʵ��
	m_createInstance = (createInstance*)
		m_ptrCDllFile->FindFunction("createInstance");
	//�����豸ʵ��
	m_releaseInstance = (releaseInstance*)
		m_ptrCDllFile->FindFunction("releaseInstance");
	//�����״����ò���
	m_saveSetting = (saveSetting*)
		m_ptrCDllFile->FindFunction("saveSetting");
    //��
	m_radarStart = (RadarStart*)
		m_ptrCDllFile->FindFunction("start");
	//�ر�
	m_radarStop = (RadarStop*)
		m_ptrCDllFile->FindFunction("stop");
	//��ȡ����
	m_receivedData = (receivedData*)
		m_ptrCDllFile->FindFunction("receivedData");
	//��������
	m_beginSaveData = (beginSaveData*)
		m_ptrCDllFile->FindFunction("beginSaveData");
	//��������
	m_endSaveData = (endSaveData*)
		m_ptrCDllFile->FindFunction("endSaveData");
	//����״̬
	m_runningStatus = (runningStatus*)
		m_ptrCDllFile->FindFunction("runningStatus");
	//��������
	m_lowerComputerConfig = (lowerComputerConfig*)
		m_ptrCDllFile->FindFunction("lowerComputerConfig");
	//��ȡGPS����
	m_receivedGpsData = (receivedGpsData*)
		m_ptrCDllFile->FindFunction("receivedGpsData");
	//��ȡ�ѾȽ������
	m_receivedRescueResult = (receivedRescueResult*)
		m_ptrCDllFile->FindFunction("receivedRescueResult");
	return true;
}


