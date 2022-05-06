
#include "DllApp.h"
#include "iostream"
#include "thread"
#include "FileDef.h"

using namespace std;
using namespace DAQ; 

int size = 500;
char data[500 * 1024 * 4];   //1024为最大采样点

int m_samplingPoints = 512;  //采样点默认值

int test(int *a)
{
	return *a + 1;
}
int main(int argc, char* argv[])
{
	//1 加载动态库
	string dllDir;
	cout << "dllDir" << endl;
	cin >> dllDir;
	//string dllDir = "C:\\hisense\\SCADA202104281308\\11\\Debug\\gprmeasurement_sharelib.dll";
	dllApp = new DllApp(dllDir);
	if (!dllApp->Init())
		return 0;

	//3 创建实例对象
	
	string localIP;
	cout << "localIp" << endl;
	cin >> localIP;
	int nRet = dllApp->m_createInstance(localIP.c_str());
	if (nRet != 0)
		return 0;

	/*
	HEAD_FILEHEADER fileHeader;
	memset((void*)&fileHeader, 0, sizeof(fileHeader));
	fileHeader.lh_bits = 32;

	////4 设置参数
	//int value;
	//Json::Value jsonValue;
	//Json::FastWriter jsonWriter;
	////4.1 采样点  512
	//cout << "samplingPoints(512)" << endl;
	//cin >> value;
	//m_samplingPoints = value;
	//fileHeader.lh_nsamp = value;
	//jsonValue["samplingPoints"] = (value == -1?512:value);

	////4.2 时窗 40
	//cout << "timeWindow(40)" << endl;
	//cin >> value;
	//fileHeader.lh_range = value;
	//jsonValue["timeWindow"] = (value == -1 ? 40: value);

	////4.3 信号位置 20
	//cout << "signalPosition(20)" << endl;
	//cin >> value;
	//fileHeader.lh_pos = value;
	//jsonValue["signalPosition"] = (value == -1 ? 20 : value);

	////4.4 软件整体增益 1
	//cout << "signalGain(1)" << endl;
	//cin >> value;
	//jsonValue["signalGain"] = (value == -1 ? 1 : value);

	////4.5 滤波使能 1
	//cout << "filterEnabled(1)" << endl;
	//cin >> value;
	//jsonValue["filterEnabled"] = (value == -1 ? 1 : value);

	////4.6 扫速
	//cout << "scanSpeed(256)" << endl;
	//cin >> value;
	//fileHeader.lh_sps = value;
	//jsonValue["scanSpeed"] = (value == -1 ? 256 : value);

	////4.7 背景消除
	//cout << "removeBack(0)" << endl;
	//cin >> value;
	//jsonValue["removeBack"] = (value == -1 ? 0 : value);

	//string strMsg = jsonWriter.write(jsonValue);
	//nRet = dllApp->m_saveSetting((char*)strMsg.c_str(),
	//	strMsg.length(), false);
	//if (nRet != 0)
	//	return 0;
	
	//5 创建文件，保存数据
	int fileNum = 0;
	cout << "fileName" << endl;
	cin >> fileNum;
	string fileName = to_string(fileNum) + ".lte";
	FILE *file = fopen(fileName.c_str(), "ab+");
	//写入文件头
	fwrite((void *)&fileHeader, 1, HEADER_LENGTH, file);
	*/

	//6 开启
	nRet = dllApp->m_radarStart();
	while (true)	
	{
		nRet = dllApp->m_receivedData(data, m_samplingPoints * 4, size);
		if (nRet == 0){
			cout << size << endl;
			//fwrite((void *)&data, 1, size * m_samplingPoints * 4, file);
		}
		Sleep(1);
	}
	//5 释放实例对象
	dllApp->m_releaseInstance();
	return 0;
}
