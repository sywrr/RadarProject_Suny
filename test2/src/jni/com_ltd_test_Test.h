/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_ltd_test_Test */

#ifndef _Included_com_ltd_test_Test
#define _Included_com_ltd_test_Test
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_ltd_test_Test
 * Method:    createInstance
 * Signature: (ILjava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_ltd_test_Test_createInstance
  (JNIEnv *, jclass, jint, jstring, jstring,jint ,jint );

/*
 * Class:     com_ltd_test_Test
 * Method:    releaseInstance
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_ltd_test_Test_releaseInstance
  (JNIEnv *, jclass);

/*
 * Class:     com_ltd_test_Test
 * Method:    lastErrorString
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_ltd_test_Test_lastErrorString
  (JNIEnv *, jclass);

/*
 * Class:     com_ltd_test_Test
 * Method:    saveSetting
 * Signature: (Ljava/lang/String;Z)I
 */
JNIEXPORT jint JNICALL Java_com_ltd_test_Test_saveSetting
  (JNIEnv *, jclass, jstring, jboolean);

/*
 * Class:     com_ltd_test_Test
 * Method:    start
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_ltd_test_Test_start
  (JNIEnv *, jclass);

/*
 * Class:     com_ltd_test_Test
 * Method:    stop
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_ltd_test_Test_stop
  (JNIEnv *, jclass);

/*
 * Class:     com_ltd_test_Test
 * Method:    runningStatus
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_ltd_test_Test_runningStatus
  (JNIEnv *, jclass, jlong);

/*
 * Class:     com_ltd_test_Test
 * Method:    receivedData
 * Signature: ([BJJ)I
 */
JNIEXPORT jint JNICALL Java_com_ltd_test_Test_receivedData
  (JNIEnv *, jclass, jbyteArray, jlong, jlong);

/*
 * Class:     com_ltd_test_Test
 * Method:    getIntPointer
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_ltd_test_Test_getIntPointer
  (JNIEnv *, jclass);

/*
 * Class:     com_ltd_test_Test
 * Method:    freeIntPointer
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_ltd_test_Test_freeIntPointer
  (JNIEnv *, jclass, jlong);

/*
 * Class:     com_ltd_test_Test
 * Method:    getPointerValue
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_ltd_test_Test_getPointerValue
  (JNIEnv *, jclass, jlong);
/*
 * Class:     com_ltd_test_Test
 * Method:    beginSaveData
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_ltd_test_Test_beginSaveData
        (JNIEnv *, jclass, jstring, jlong);
/*
 * Class:     com_ltd_test_Test
 * Method:    endSaveData
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_ltd_test_Test_endSaveData
        (JNIEnv *, jclass);
/*
 * Class:     com_ltd_test_Test
 * Method:    receivedRescueResult
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_ltd_test_Test_receivedRescueResult
        (JNIEnv *env, jclass cls,jlong isEnd,jlong resultType,jlong distance,jlong detectionBegin,jlong detecetionEnd );

JNIEXPORT jint JNICALL Java_com_ltd_test_Test_lowerComputerConfig
        (JNIEnv *env, jclass,jstring , short ,jstring,jint,short,short,short,short,short);

#ifdef __cplusplus
}
#endif
#endif