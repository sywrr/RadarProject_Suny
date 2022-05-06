/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_ltdpro_USBDriver */

#ifndef _Included_com_ltdpro_USBDriver
#define _Included_com_ltdpro_USBDriver
#ifdef __cplusplus
extern "C" {
#endif

    ////定义命令结构
struct Device_Command
{
	unsigned short m_flag;
	unsigned short m_code;
};

////命令
#define IOCTL_CODE_FLAG  0xABCD    //控制命令标志
#define CODE_CONTINUEMODE_START 0xAA00    //开启雷达连续模式
#define CODE_CONTINUEMODE_STOP  0xAA01    //停止雷达连续模式

//
#define LOG_TAG "USBDriver-JNI-forUSBLTD"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

//设备名
#define DEVICE_NAME "/dev/USBLTD"

/*
 * Class:     com_ltdpro_USBDriver
 * Method:    openUSBLTD
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_ltdpro_USBDriver_openUSBLTD
  (JNIEnv *, jclass);

/*
 * Class:     com_ltdpro_USBDriver
 * Method:    closeUSBLTD
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_ltdpro_USBDriver_closeUSBLTD
  (JNIEnv *, jclass);

/*
 * Class:     com_ltdpro_USBDriver
 * Method:    startUSBLTD
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_ltdpro_USBDriver_startUSBLTD
  (JNIEnv *, jclass);

/*
 * Class:     com_ltdpro_USBDriver
 * Method:    stopUSBLTD
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_ltdpro_USBDriver_stopUSBLTD
  (JNIEnv *, jclass);

/*
 * Class:     com_ltdpro_USBDriver
 * Method:    readUSBLTD
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_ltdpro_USBDriver_readUSBLTD
  (JNIEnv *, jclass);

/*
 * Class:     com_ltdpro_USBDriver
 * Method:    readOneWave
 * Signature: ([SI)I
 */
JNIEXPORT jint JNICALL Java_com_ltdpro_USBDriver_readOneWave
  (JNIEnv *, jclass, jshortArray, jint);

/*
 * Class:     com_ltdpro_USBDriver
 * Method:    sendCommands
 * Signature: ([SS)I
 */
JNIEXPORT jint JNICALL Java_com_ltdpro_USBDriver_sendCommands
  (JNIEnv *, jclass, jshortArray, jshort);

#ifdef __cplusplus
}
#endif
#endif
