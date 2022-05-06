
#ifndef GRP_HPP
#define GRP_HPP

#ifdef __cplusplus
extern "C" {
#endif

int
createInstance(int netType, const char *localIp, const char *deviceIp , int antenaType,int dllFlag);

int
releaseInstance();

void
lastErrorString(char *error, int *pSize);

int
saveSetting(char *settingChar, const int size, bool isOnline);

int
start();

int
stop();

int
runningStatus(bool *pIsRunning);

int
receivedData(char *data, int *pStep, int *pSize);


int
beginSaveData(const char*fileDir,void *user);

int
endSaveData();

int
receivedRescueResult(bool *isEnd,short *resultType, short *distance,short *detectionBegin,short *detecetionEnd);

int
lowerComputerConfig(const char*ip,short devType, const char* cardSerialNum,int serialNum,short deviceSerialNum,short versionNum,short calibrationValue, short antennaCode, short frqValue);
#ifdef __cplusplus
}
#endif

#endif