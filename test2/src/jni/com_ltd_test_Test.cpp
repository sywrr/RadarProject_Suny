//
// Created by Ðìçü on 2021/5/31.
//

#include "com_ltd_test_Test.h"
#include "gpr.hpp"
#include "log.hpp"

#include <cstring>
#include <cstdlib>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL
Java_com_ltd_test_Test_createInstance(JNIEnv *env, jclass cls, int netType, jstring localIp,
                                      jstring deviceIp, jint antenaType, jint dllFlag) {
//    debug("native create instance\n");
    const char *ip = env->GetStringUTFChars(localIp, nullptr);
    return createInstance(netType, ip, env->GetStringUTFChars(deviceIp, nullptr), antenaType,
                          dllFlag);
}

JNIEXPORT jint JNICALL
Java_com_ltd_test_Test_releaseInstance(JNIEnv *env, jclass cls) {
    return releaseInstance();
}

JNIEXPORT jstring JNICALL
Java_com_ltd_test_Test_lastErrorString(JNIEnv *env, jclass cls) {
    char *error = new char[100];
    int size = 0;
    lastErrorString(error, &size);
    jclass strClass = env->FindClass("Ljava/lang/String;");
    jmethodID ctorID = env->GetMethodID(strClass, "<init>", "([BLjava/lang/String;)V");
    jbyteArray bytes = env->NewByteArray(size);
    env->SetByteArrayRegion(bytes, 0, size, (jbyte *) error);
    jstring encoding = env->NewStringUTF("utf-8");
    return (jstring) env->NewObject(strClass, ctorID, bytes, encoding);
}

JNIEXPORT jint JNICALL
Java_com_ltd_test_Test_saveSetting(JNIEnv *env, jclass cls, jstring settings, jboolean isOnline) {
    char *pSettings = nullptr;
    jclass stringClass = env->FindClass("java/lang/String");
    jmethodID mid = env->GetMethodID(stringClass, "getBytes", "(Ljava/lang/String;)[B");
    jstring encode = env->NewStringUTF("utf-8");
    jbyteArray bytes = (jbyteArray) env->CallObjectMethod(settings, mid, encode);
    jsize len = env->GetArrayLength(bytes);
    jbyte *pBytes = env->GetByteArrayElements(bytes, JNI_FALSE);
    int result = -1;
    if (len > 0) {
        pSettings = new char[len + 1];
        memcpy(pSettings, pBytes, len);
        pSettings[len] = 0;
        result = saveSetting(pSettings, len, isOnline);
    }
    env->ReleaseByteArrayElements(bytes, pBytes, 0);
    return result;
}

JNIEXPORT jint JNICALL
Java_com_ltd_test_Test_start(JNIEnv *env, jclass cls) {
    return start();
}

JNIEXPORT jint JNICALL
Java_com_ltd_test_Test_stop(JNIEnv *env, jclass cls) {
    return stop();
}

JNIEXPORT jint JNICALL
Java_com_ltd_test_Test_runningStatus(JNIEnv *env, jclass cls, jlong ptrRet) {
    int *p = (int *) ptrRet;
    bool state;
    int ret = runningStatus(&state);
    *p = state;
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_ltd_test_Test_receivedData(JNIEnv *env, jclass cls, jbyteArray data, jlong ptrStep,
                                    jlong ptrSize) {
    *(int *) ptrSize = 500;
    jbyte *pData = env->GetByteArrayElements(data, 0);
    int ret = receivedData((char *) pData, (int *) ptrStep, (int *) ptrSize);
    env->ReleaseByteArrayElements(data, pData, 0);
    return ret;
}

JNIEXPORT jlong JNICALL Java_com_ltd_test_Test_getIntPointer
        (JNIEnv *env, jclass cls) {
    return (jlong) ::malloc(4);
}

JNIEXPORT void JNICALL Java_com_ltd_test_Test_freeIntPointer
        (JNIEnv *env, jclass cls, jlong ptr) {
    ::free((int *) ptr);
}

JNIEXPORT jint JNICALL Java_com_ltd_test_Test_getPointerValue
        (JNIEnv *env, jclass cls, jlong ptr) {
    return *(int *) ptr;
}
JNIEXPORT jint JNICALL Java_com_ltd_test_Test_beginSaveData
        (JNIEnv *env, jclass cls, jstring fileDir, jlong use) {
    const char *filepath = env->GetStringUTFChars(fileDir, nullptr);
    static int curFrameNum = 0;
    int ret = beginSaveData(filepath, (void *) &curFrameNum);
    return ret;
}
JNIEXPORT jint JNICALL Java_com_ltd_test_Test_endSaveData
        (JNIEnv *env, jclass cls) {
    int ret = endSaveData();
    return ret;
}
//sunyan 2021.1.7 add begin
JNIEXPORT jint JNICALL Java_com_ltd_test_Test_receivedRescueResult
        (JNIEnv *env, jclass cls,jlong isEnd, jlong resultType, jlong distance,
         jlong detectionBegin, jlong detecetionEnd) {
    int *p = (int *) isEnd;
    short *a=(short*) resultType;
    short *b=(short*) distance;
    short *c=(short*) detectionBegin;
    short *d=(short*) detecetionEnd;

    bool isDetect;
    short resultType1,distance1,detectionBegin1,detecetionEnd1;
    int ret = receivedRescueResult(&isDetect, &resultType1, &distance1, &detectionBegin1, &detecetionEnd1);
    *p = isDetect;
    *a = resultType1;
    *b = distance1;
    *c=detectionBegin1;
    *d=detecetionEnd1;
    return ret;
}

JNIEXPORT jshort JNICALL Java_com_ltd_test_Test_getShortPointer
        (JNIEnv *env, jclass cls) {
    return (jlong) ::malloc(4);
}

//sunyan 2021.1.7 add end
//sunyan 2022.3.23 add begin
JNIEXPORT jint JNICALL
Java_com_ltd_test_Test_lowerComputerConfig
        (JNIEnv *env, jclass cls, jstring ip, short devType,
         jstring cardSerialNum, jint serial_num, short deviceSerialNum, short versionNum, short calibrationValue,short antennaCode,short frqValue) {
    const char *deviceIp = env->GetStringUTFChars(ip, nullptr);
    return lowerComputerConfig(deviceIp, devType,env->GetStringUTFChars(cardSerialNum, nullptr),serial_num,deviceSerialNum,
                          versionNum,calibrationValue,antennaCode,frqValue);
}
//sunyan 2022.3.23 add end
#ifdef __cplusplus
}
#endif