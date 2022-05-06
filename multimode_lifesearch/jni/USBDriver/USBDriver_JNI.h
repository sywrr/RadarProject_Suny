#ifndef _USBDRIVER_JNI_H
#define _USBDRIVER_JNI_H

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


#endif
