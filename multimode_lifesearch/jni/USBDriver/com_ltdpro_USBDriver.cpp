//
// Created by Ðìçü on 2021/3/29.
//

#include "com_ltdpro_USBDriver.h"
#include <android/log.h>
#include <unistd.h>
#include <fcntl.h>

#ifdef __cplusplus
extern "C" {
#endif

int g_fd = -1;
jshortArray buf[100];

/*
* Class:     com_ltdpro_USBDriver
* Method:    openUSBLTD
* Signature: ()I
*/
JNIEXPORT jint JNICALL Java_com_ltdpro_USBDriver_openUSBLTD
        (JNIEnv *env, jclass clazz) {
    LOGE("***Begin Open USBLTD11111****\n");
    g_fd = open(DEVICE_NAME, O_RDWR);
    if (g_fd == -1) {
        LOGE("Open USBLTD Fail\n");
        return -1;
    }
    return 1;
}

/*
 * Class:     com_ltdpro_USBDriver
 * Method:    closeUSBLTD
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_ltdpro_USBDriver_closeUSBLTD
        (JNIEnv *env, jclass clazz) {
    LOGE("***Begin Close USBLTD****\n");
    if (g_fd == -1) {
        LOGE("USBLTD no open\n");
    } else {
        close(g_fd);
        g_fd = -1;
    }
    return 1;
}

/*
 * Class:     com_ltdpro_USBDriver
 * Method:    startUSBLTD
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_ltdpro_USBDriver_startUSBLTD
        (JNIEnv *env, jclass clazz) {
    LOGE("***Begin Start USBLTD****\n");
    if (g_fd == -1) {
        LOGE("USBLTD no open\n");
        return -1;
    }
    short commands[3];
    commands[0] = IOCTL_CODE_FLAG;
    commands[1] = CODE_CONTINUEMODE_START;
    commands[2] = 0;
    write(g_fd, commands, 6);
    //
    return 1;
}

/*
 * Class:     com_ltdpro_USBDriver
 * Method:    stopUSBLTD
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_ltdpro_USBDriver_stopUSBLTD
        (JNIEnv *env, jclass clazz) {
    LOGE("***Begin Stop USBLTD****\n");
    if (g_fd == -1) {
        LOGE("USBLTD no open\n");
        return -1;
    }

    short commands[3];
    commands[0] = IOCTL_CODE_FLAG;
    commands[1] = CODE_CONTINUEMODE_STOP;
    commands[2] = 0;
    write(g_fd, commands, 6);
    //
    return 1;
}

/*
 * Class:     com_ltdpro_USBDriver
 * Method:    readUSBLTD
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_ltdpro_USBDriver_readUSBLTD
        (JNIEnv *env, jclass clazz) {
    LOGE("***Begin Read USBLTD****\n");
    if (g_fd == -1) {
        LOGE("USBLTD no open\n");
        return -1;
    }
    char val[32];
    read(g_fd, val, 32);
    //
    return 1;
}

/*
 * Class:     com_ltdpro_USBDriver
 * Method:    readOneWave
 * Signature: ([SI)I
 */
JNIEXPORT jint JNICALL Java_com_ltdpro_USBDriver_readOneWave
        (JNIEnv *env, jclass clazz, jshortArray arr, jint length) {
    //	LOGE("***Begin Read OneWave:%d****\n",length);
    jshort *pBuf;
    pBuf = env->GetShortArrayElements(arr, NULL);
    /*
    if(pBuf == NULL)
        LOGE("The array is NULL\n");
    else
        LOGE("The firstBuf:%d\n",pBuf[0]);
    */
    int rLen = read(g_fd, pBuf, length);

    env->ReleaseShortArrayElements(arr, pBuf, 0);

    //
    return rLen;
}

/*
 * Class:     com_ltdpro_USBDriver
 * Method:    sendCommands
 * Signature: ([SS)I
 */
JNIEXPORT jint JNICALL Java_com_ltdpro_USBDriver_sendCommands
        (JNIEnv *env, jclass clazz, jshortArray arr, jshort length) {
    LOGE("***Send Commands****\n");
    jshort *pBuf;
    pBuf = env->GetShortArrayElements(arr, NULL);
    /*
    if(pBuf == NULL)
        LOGE("The array is NULL\n");
    else
        LOGE("The firstBuf:%d\n",pBuf[0]);
    */
    long nWrite = write(g_fd, pBuf, length);
    LOGE("nWrite: %ld\n", nWrite);
    env->ReleaseShortArrayElements(arr, pBuf, 0);

    //
    return 1;
}

#ifdef __cplusplus
}
#endif