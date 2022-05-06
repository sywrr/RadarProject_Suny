#include <string.h>
#include <jni.h>
#include <fcntl.h>
#include <android/log.h>
#include "USBDriver_JNI.h"
#include <unistd.h>

int g_fd=-1;
jshortArray buf[100];
//jstring Java_com_example_hellojni_HelloJni_stringFromJNI(JNIEnv* env,jobject thiz)
//jstring Java_com_ltdpro_usbltdtest_MainActivity_stringFromJNI(JNIEnv* env,jobject thiz)
//{
//	LOGE("hellojni-driver_Init() \n");
//	int fd = 1;//-1;
//	int val = 0;
//	fd = open(DEVICE_NAME, O_RDWR);
//	LOGE("driver open-> fd = %d \n",fd);
//
//	if(fd != -1)
//		close(fd);
//	if(fd == -1)
//	{
//		return (*env)->NewStringUTF(env, "HelloUSBDriver_JNI_Fail!");
//	}
//	else
//	{
//		return (*env)->NewStringUTF(env, "HelloUSBDriver_JNI_OK!");
//	}
//}

JNIEXPORT void JNICALL

//int Java_com_example_usbltdtest_MainActivity_openUSBLTD()
int Java_com_ltdpro_radarDevice_openUSBLTD()
{
	LOGE("***Begin Open USBLTD11111****\n");
	g_fd = open(DEVICE_NAME,O_RDWR);
	if(g_fd == -1)
	{
		LOGE("Open USBLTD Fail\n");
		return -1;
	}
	return 1;
}

int Java_com_ltdpro_radarDevice_closeUSBLTD()
{
	LOGE("***Begin Close USBLTD****\n");
	if(g_fd == -1)
	{
		LOGE("USBLTD no open\n");
	}
	else
	{
		close(g_fd);
		g_fd = -1;
	}
	return 1;
}
int Java_com_ltdpro_radarDevice_startUSBLTD()
{
	LOGE("***Begin Start USBLTD****\n");
	if(g_fd == -1)
	{
		LOGE("USBLTD no open\n");
		return -1;
	}
	short commands[3];
	commands[0] = IOCTL_CODE_FLAG;
	commands[1] = CODE_CONTINUEMODE_START;
	commands[2] = 0;
	write(g_fd,commands,6);
	//
	return 1;
}
int Java_com_ltdpro_radarDevice_stopUSBLTD()
{
	LOGE("***Begin Stop USBLTD****\n");
	if(g_fd == -1)
	{
		LOGE("USBLTD no open\n");
		return -1;
	}

	short commands[3];
	commands[0] = IOCTL_CODE_FLAG;
	commands[1] = CODE_CONTINUEMODE_STOP;
	commands[2] = 0;
	write(g_fd,commands,6);
	//
	return 1;
}
int Java_com_ltdpro_radarDevice_readUSBLTD()
{
	LOGE("***Begin Read USBLTD****\n");
	if(g_fd == -1)
	{
		LOGE("USBLTD no open\n");
		return -1;
	}
	char val[32];
	read(g_fd,val,32);
	//
	return 1;
}
int Java_com_ltdpro_radarDevice_readOneWave(JNIEnv* env,jobject obj,jshortArray arr,int length)
{
//	LOGE("***Begin Read OneWave:%d****\n",length);
	jshort* pBuf;
	pBuf = (*env)->GetShortArrayElements(env,arr,NULL);
	/*
	if(pBuf == NULL)
		LOGE("The array is NULL\n");
	else
		LOGE("The firstBuf:%d\n",pBuf[0]);
	*/
	int rLen = read(g_fd,pBuf,length);

	(*env)->ReleaseShortArrayElements(env,arr,pBuf,0);

	//
	return rLen;
}


int Java_com_ltdpro_radarDevice_sendCommands(JNIEnv* env,jobject obj,jshortArray arr,short length)
{
	LOGE("***Send Commands****\n");
	jshort* pBuf;
	pBuf = (*env)->GetShortArrayElements(env,arr,NULL);
	/*
	if(pBuf == NULL)
		LOGE("The array is NULL\n");
	else
		LOGE("The firstBuf:%d\n",pBuf[0]);
	*/
	long nWrite = write(g_fd,pBuf,length);
	LOGE("nWrite: %ld\n", nWrite);
	(*env)->ReleaseShortArrayElements(env,arr,pBuf,0);

	//
	return 1;
}
//²âÊÔº¯Êý
int Java_com_ltdpro_radarDevice_tryparam(JNIEnv* env,jobject obj,short length)
{
	LOGE("***try params_length:%d****\n",length);
	return 1;
}