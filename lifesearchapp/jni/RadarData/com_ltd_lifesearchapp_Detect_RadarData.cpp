#include "com_ltd_lifesearchapp_Detect_RadarData.h"
#include <cstring>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL
Java_com_ltd_lifesearchapp_Detect_RadarData_memCopy(JNIEnv *env, jclass, jshortArray dst,
                                                    jint dstIdx, jbyteArray src, jint srcIdx,
                                                    jint length) {
    jshort *pDst = env->GetShortArrayElements(dst, nullptr);
    jbyte *pSrc = env->GetByteArrayElements(src, nullptr);
    memcpy(pDst + dstIdx, pSrc + srcIdx, length);
    env->ReleaseShortArrayElements(dst, pDst, 0);
    env->ReleaseByteArrayElements(src, pSrc, 0);
}

#ifdef __cplusplus
}
#endif