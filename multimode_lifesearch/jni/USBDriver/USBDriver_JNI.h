#ifndef _USBDRIVER_JNI_H
#define _USBDRIVER_JNI_H

////��������ṹ
struct Device_Command
{
	unsigned short m_flag;
	unsigned short m_code;
};

////����
#define IOCTL_CODE_FLAG  0xABCD    //���������־
#define CODE_CONTINUEMODE_START 0xAA00    //�����״�����ģʽ
#define CODE_CONTINUEMODE_STOP  0xAA01    //ֹͣ�״�����ģʽ

//
#define LOG_TAG "USBDriver-JNI-forUSBLTD"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

//�豸��
#define DEVICE_NAME "/dev/USBLTD"


#endif
