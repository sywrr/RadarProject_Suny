
#ifndef GRP_HPP
#define GRP_HPP

#ifdef __cplusplus
extern "C" {
#endif

int
createInstance(int netType, const char *localIp, const char *deviceIp,int antenaType,int dllFlag);

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
#ifdef __cplusplus
}
#endif

#endif