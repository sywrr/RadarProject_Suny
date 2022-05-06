#include "com_ltd_lifesearchapp_RadarData.h"
#include <string.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_com_ltd_lifesearchapp__1RadarData__1memory_1copy
        (JNIEnv *env, jclass cls, jbyteArray src, jint srcIndex, jshortArray dest, jint destIndex,
         jint length) {
    jbyte *pSrc = (*env)->GetByteArrayElements(env, src, NULL);
    jshort *pDest = (*env)->GetShortArrayElements(env, dest, NULL);
    memcpy(pDest + destIndex, pSrc + srcIndex, length * 2);
    (*env)->ReleaseByteArrayElements(env, src, pSrc, 0);
    (*env)->ReleaseShortArrayElements(env, dest, pDest, 0);
}

//#ifdef __cplusplus
//}
//#endif