//
// Created by Ðìçü on 2021/1/11.
//

#include "com_ltdpro_BaseDetect.h"
//#include "multi_arm1.c"
//#include "multi_arm_9_body.c"
//#include "multi_arm_6_breath.c"
#include "Detect_1/use_classify1.cpp"

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_com_ltdpro_BaseDetect_detect_1body(
        JNIEnv *env, jclass obj, jshortArray result_data) {
    jshort *result_short_array =
            (env)->GetShortArrayElements(result_data, nullptr);
    myClassify_body.detect_body(result_short_array);
    (env)->ReleaseShortArrayElements(result_data, result_short_array, 0);
}

JNIEXPORT void JNICALL Java_com_ltdpro_BaseDetect_detect_1body_1pre
        (JNIEnv *env, jclass cls, jshortArray ad_frame_data) {
    jshort *ad_frame_short_array =
            (env)->GetShortArrayElements(ad_frame_data, nullptr);
    myClassify_body.detect_body_pre(ad_frame_short_array);
    (env)->ReleaseShortArrayElements(ad_frame_data, ad_frame_short_array, 0);
}

JNIEXPORT void JNICALL Java_com_ltdpro_BaseDetect_detect_1breath_1pre
        (JNIEnv *env, jclass cls, jshortArray ad_frame_data) {
    jshort *ad_frame_short_array =
            (env)->GetShortArrayElements(ad_frame_data, nullptr);
    myClassify_breath.detect_breath_pre(ad_frame_short_array);
    (env)->ReleaseShortArrayElements(ad_frame_data, ad_frame_short_array, 0);
}

JNIEXPORT void JNICALL Java_com_ltdpro_BaseDetect_detect_1breath(
        JNIEnv *env, jclass obj, jshortArray result_data, jfloat breath_th) {
    jshort *result_short_array =
            (env)->GetShortArrayElements(result_data, nullptr);
    myClassify_breath.detect_breath(result_short_array, breath_th);
    (env)->ReleaseShortArrayElements(result_data, result_short_array, 0);
}

JNIEXPORT void JNICALL Java_com_ltdpro_BaseDetect_init_1body(JNIEnv *env, jclass obj) {
    myClassify_body.init_body();
}

JNIEXPORT void JNICALL Java_com_ltdpro_BaseDetect_init_1breath(JNIEnv *env, jclass obj) {
    myClassify_breath.init_breath();
}

JNIEXPORT void JNICALL Java_com_ltdpro_BaseDetect_changeParams(
        JNIEnv *env, jclass obj, jint fscans, jint __antenna_type, jint __window,
        jint ad_element_size) {
    myClassify_breath.changeParams((unsigned) fscans, (unsigned) __antenna_type, (unsigned)
                                           __window,
                                   (unsigned) ad_element_size);
    myClassify_body.changeParams((unsigned) fscans, (unsigned) __antenna_type, (unsigned)
                                         __window,
                                 (unsigned) ad_element_size);
}

JNIEXPORT void JNICALL Java_com_ltdpro_BaseDetect_setMultiMode
        (JNIEnv *env, jclass obj, jint __multi_flag) {
    myClassify_body.multi_flag = __multi_flag;
}

JNIEXPORT jint JNICALL Java_com_ltdpro_BaseDetect_getMultiMode
        (JNIEnv *env, jclass obj) {
    return myClassify_body.multi_flag;
}

#ifdef __cplusplus
}
#endif