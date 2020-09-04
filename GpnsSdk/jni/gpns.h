#ifndef __GPNS__HEAD_H
#define __GPNS__HEAD_H

#include "jni.h"

#ifdef __cplusplus
extern "C"
{
#endif

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* jvm, void* reserved);
JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved);

jstring Java_com_goome_gpnsjni_NativePresenter_start(JNIEnv* env, jobject thiz);

jstring Java_com_goome_gpnsjni_NativePresenter_stop(JNIEnv* env, jobject thiz);

jstring Java_com_goome_gpnsjni_NativePresenter_init(JNIEnv * env, jobject thiz, jstring host, jstring devinfo,
    jint port, jlong cid, jint mode);

#ifdef __cplusplus
}
#endif

#endif
