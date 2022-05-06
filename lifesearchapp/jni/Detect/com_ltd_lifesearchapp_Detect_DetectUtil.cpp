#include "com_ltd_lifesearchapp_Detect_DetectUtil.h"

#include "Detect/use_classify1.cpp"

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL
Java_com_ltd_lifesearchapp_Detect_DetectUtil_detect_1body(JNIEnv *env, jclass cls,
                                                          jshortArray array) {
    jshort *pArray = env->GetShortArrayElements(array, nullptr);
    myClassify_body.detect_body(pArray);
    env->ReleaseShortArrayElements(array, pArray, 0);
}

JNIEXPORT void JNICALL
Java_com_ltd_lifesearchapp_Detect_DetectUtil_init_1body(JNIEnv *, jclass) {
    myClassify_body.init_body();
}

JNIEXPORT void JNICALL
Java_com_ltd_lifesearchapp_Detect_DetectUtil_init_1breath(JNIEnv *, jclass) {
    myClassify_breath.init_breath();
}

JNIEXPORT void JNICALL
Java_com_ltd_lifesearchapp_Detect_DetectUtil_changeParams(JNIEnv *, jclass, jint fscans,
                                                          jint antenna_type, jint window,
                                                          jint element_size) {
    myClassify_breath.changeParams(fscans, antenna_type, window, element_size);
    myClassify_body.changeParams(fscans, antenna_type, window, element_size);
}

JNIEXPORT void JNICALL
Java_com_ltd_lifesearchapp_Detect_DetectUtil_detect_1body_1pre(JNIEnv *env, jclass,
                                                               jshortArray array) {
    jshort *pArray = env->GetShortArrayElements(array, nullptr);
    myClassify_body.detect_body_pre(pArray);
    env->ReleaseShortArrayElements(array, pArray, 0);
}

JNIEXPORT void JNICALL
Java_com_ltd_lifesearchapp_Detect_DetectUtil_detect_1breath_1pre(JNIEnv *env, jclass,
                                                                 jshortArray array) {
    jshort *pArray = env->GetShortArrayElements(array, nullptr);
    myClassify_breath.detect_breath_pre(pArray);
    env->ReleaseShortArrayElements(array, pArray, 0);
}

JNIEXPORT void JNICALL
Java_com_ltd_lifesearchapp_Detect_DetectUtil_detect_1breath(JNIEnv *env, jclass, jshortArray array,
                                                            jfloat threshold) {
    jshort *pArray = env->GetShortArrayElements(array, nullptr);
    myClassify_breath.detect_breath(pArray, threshold);
    env->ReleaseShortArrayElements(array, pArray, 0);
}

JNIEXPORT void JNICALL
Java_com_ltd_lifesearchapp_Detect_DetectUtil_setMultiMode(JNIEnv *, jclass, jint mode) {
    myClassify_body.multi_flag = mode;
}

JNIEXPORT jint JNICALL
Java_com_ltd_lifesearchapp_Detect_DetectUtil_getMultiMode(JNIEnv *, jclass) {
    return myClassify_body.multi_flag;
}

#ifdef __cplusplus
}
#endif