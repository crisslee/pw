#include <string.h>
#include <stdlib.h>
#include "jni.h"
#include "GpnsClientI.h"
#include "common.h"

extern JavaVM* g_jvm;
extern JNIEnv* g_env;
extern jobject g_obj;

extern jclass stringCls;
extern jclass presenterCls;
extern jobject presenterObj;

#ifdef __cplusplus
extern "C"
{
#endif

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* jvm, void* reserved)
{
    LOGI("JNI_OnLoad jvm=%p", jvm);
    g_jvm = jvm;
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved)
{
    JNIEnv* env;
    int result = (*g_jvm)->AttachCurrentThread(g_jvm, &env, NULL);
    if (result < 0)
    {
        LOGI("sendAndroidRpl AttachCurrentThread error");
        return;
    }
    #if defined(__cplusplus)
        env->DeleteGlobalRef(stringCls);
        env->DeleteGlobalRef(presenterCls);
        env->DeleteGlobalRef(presenterObj);
    #else
        (*env)->DeleteGlobalRef(env, stringCls);
        (*env)->DeleteGlobalRef(env, presenterCls);
        (*env)->DeleteGlobalRef(env, presenterObj);
    #endif
}

jstring Java_com_goome_gpnsjni_NativePresenter_start(JNIEnv* env, jobject thiz) {

    char errMsg[8] = {0};
    g_env = env;
    g_obj = thiz;

    if(startClientService() != 0)
    {
        strcpy(errMsg,"ERROR");
    }
    else
    {
        strcpy(errMsg,"OK");
    }

    jstring  jstr;
    #if defined(__cplusplus)
        jstr = env->NewStringUTF(errMsg)
    #else
        jstr = (*env)->NewStringUTF(env, errMsg);
    #endif

    return jstr;
}

jstring Java_com_goome_gpnsjni_NativePresenter_stop(JNIEnv* env, jobject thiz) {

    g_env = env;
    g_obj = thiz;

    stopClientService();

    jstring  jstr;
    #if defined(__cplusplus)
       jstr = env->NewStringUTF("OK")
    #else
       jstr = (*env)->NewStringUTF(env, "OK");
    #endif

    return jstr;
}

jstring Java_com_goome_gpnsjni_NativePresenter_init(JNIEnv * env, jobject thiz, jstring host, jstring devinfo,
    jint port, jlong cid, jint mode){

    char *hostP = NULL;
    char *devinfoP = NULL;
    char errMsg[128] = {0};

    g_env = env;
    g_obj = thiz;

    //init cls
    #if defined(__cplusplus)
        stringCls = (jclass)(env->NewGlobalRef(env->FindClass("java/lang/String"));
        presenterCls = (jclass)(env->NewGlobalRef(env->FindClass("com/goome/gpnsjni/NativePresenter"));
        presenterObj = (jobject)(env->NewGlobalRef(thiz));
    #else
        stringCls = (jclass)((*env)->NewGlobalRef(env, (*env)->FindClass(env, "java/lang/String")));
        presenterCls = (jclass)((*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/goome/gpnsjni/NativePresenter")));
        presenterObj = (jobject)((*env)->NewGlobalRef(env, thiz));
    #endif

    do
    {
        hostP = jstringTostring(env,host);
        if(hostP == NULL)
        {
            strncpy(errMsg,"jstringTostring host error",127);
            break;
        }

        devinfoP = jstringTostring(env,devinfo);
        if(devinfoP == NULL)
        {
            strncpy(errMsg,"jstringTostring devinfo error",127);
            break;
        }

        if(initializeClient(hostP,devinfoP,(int)port,(long)cid, (int)mode) != 0)
        {
            strncpy(errMsg,"initializeClient error",127);
            break;
        }

        strncpy(errMsg,"OK",127);
    }while(0);
 
    if(hostP)       free(hostP);
    if(devinfoP)    free(devinfoP);

    jstring  jstr;
    #if defined(__cplusplus)
       jstr = env->NewStringUTF(errMsg)
    #else
       jstr = (*env)->NewStringUTF(env, errMsg);
    #endif

    return jstr;
}

#ifdef __cplusplus
}
#endif
