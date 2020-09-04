#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <pthread.h>
#include "jni.h"
#include "common.h"

JavaVM* g_jvm = NULL;
JNIEnv* g_env = NULL;
jobject g_obj;

static int g_bAttached = 0;

jclass stringCls = NULL;
jclass presenterCls = NULL;
jobject presenterObj = NULL;

char* jstringTostring(JNIEnv* env, jstring jstr) {
    char* rtn = NULL;
    jstring strencode;
    jmethodID mid;
    jbyteArray barr;
    jsize alen;
    jbyte* ba;

#if defined(__cplusplus)
    strencode = env->NewStringUTF("utf-8");
    mid = env->GetMethodID(stringCls, "getBytes", "(Ljava/lang/String;)[B");
    barr = (jbyteArray) (env->CallObjectMethod(jstr, mid, strencode));
    alen = env->GetArrayLength(barr);
    ba = env->GetByteArrayElements(barr, JNI_FALSE);
#else
    strencode = (*env)->NewStringUTF(env,"utf-8");
    mid = (*env)->GetMethodID(env,stringCls, "getBytes", "(Ljava/lang/String;)[B");
    barr= (jbyteArray)((*env)->CallObjectMethod(env,jstr, mid, strencode));
    alen = (*env)->GetArrayLength(env,barr);
    ba = (*env)->GetByteArrayElements(env,barr, JNI_FALSE);
#endif

    if (alen > 0) {
        rtn = (char*) malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }

#if defined(__cplusplus)
    env->ReleaseByteArrayElements(barr, ba, 0);
    env->DeleteLocalRef(strencode);
#else
    (*env)->ReleaseByteArrayElements(env, barr, ba, 0);
    (*env)->DeleteLocalRef(env, strencode);
#endif

    return rtn;
}

void sendAndroidRpl(int status, const char *msg) {
    char szTmp[1024];
    LOGI("sendAndroidRpl status=%d, msg=%s", status, msg);
    snprintf(szTmp, sizeof(szTmp), "%"PRIu64":%s", (uint64_t)pthread_self(), msg);

    JNIEnv* env;
    int result = (*g_jvm)->AttachCurrentThread(g_jvm, &env, NULL);
    if (result < 0)
    {
        LOGI("sendAndroidRpl AttachCurrentThread error");
        return;
    }
    // snprintf(szTmp, sizeof(szTmp), "%s", msg);  // 日志中暂时不打印线程ID，减少系统调用，看看能不能稍微省点电
    if (env == NULL)
    {
        return;
    }

    if(presenterCls==NULL || presenterObj==NULL)
    {
        LOGI("sendAndroidRpl presenterCls==NULL || presenterObj==NULL");
        return;
    }

    //若msg内包含非法modified utf-8字符，就不推送日志
    if (checkUtfBytes(szTmp) != 0) {
        return;
    }

#if defined(__cplusplus)
    //得到里面的方法。
    jmethodID jmethod = env->GetMethodID(presenterCls, "onConnectionStatusChanged", "(ILjava/lang/String;)V");
    jstring jcontent = env->NewStringUTF(szTmp);
    //调用方法
    env->CallVoidMethod(presenterObj, jmethod, status, jcontent);
    env->DeleteLocalRef(jcontent);
#else
    //得到里面的方法。
    jmethodID jmethod = (*env)->GetMethodID(env,presenterCls,"onConnectionStatusChanged","(ILjava/lang/String;)V");
    jstring jcontent = (*env)->NewStringUTF(env,szTmp);
    //调用方法
    (*env)->CallVoidMethod(env,presenterObj,jmethod,status,jcontent);
    (*env)->DeleteLocalRef(env,jcontent);
#endif
}

void sendAndroidMsg(const char *msg) {
    LOGI("sendAndroidMsg msg=%s", msg);
    JNIEnv* env;
    int status = (*g_jvm)->AttachCurrentThread(g_jvm, &env, NULL);
    if (status < 0)
    {
        LOGI("sendAndroidMsg AttachCurrentThread error");
        return;
    }

    if (env == NULL) {
        return;
    }

    //若msg内包含非法modified utf-8字符，就不推送消息，推送错误日志
    if (checkUtfBytes(msg) != 0) {
        sendAndroidRpl(C_DEBUG_INFO, "Invalid modified utf-8 string");
        return;
    }

#if defined(__cplusplus)
    //得到里面的方法。
    jmethodID jmethod = env->GetMethodID(presenterCls, "getMessage", "(Ljava/lang/String;)V");
    jstring jcontent = env->NewStringUTF(msg);
    //调用方法
    env->CallVoidMethod(presenterObj, jmethod, jcontent);
    env->DeleteLocalRef(jcontent);
#else
    //得到里面的方法。
    jmethodID jmethod = (*env)->GetMethodID(env,presenterCls,"getMessage","(Ljava/lang/String;)V");
    jstring jcontent = (*env)->NewStringUTF(env,msg);
    //调用方法
    (*env)->CallVoidMethod(env,presenterObj,jmethod,jcontent);
    (*env)->DeleteLocalRef(env,jcontent);
#endif
}

void AndroidWakeLock(int op) {
    LOGI("AndroidWakeLock op=%d", op);
    JNIEnv* env;
    int status = (*g_jvm)->AttachCurrentThread(g_jvm, &env, NULL);
    if (status < 0)
    {
        LOGI("AndroidWakeLock AttachCurrentThread error");
        return;
    }
    if (env == NULL) {
        return;
    }

    // 目前发现在没网络的情况下汽车在线耗电比较多，换了一个不加/解锁的版本后，耗电量显著下降。怀疑是这个操作太耗CPU导致费电。
    // 单次测试不能证实一定是这个的问题，日后需要此功能时可以再好好测一测。
    //////////////////////////////////////////////////////////////////////////
    /*
     #if defined(__cplusplus)
     jclass jclazz = g_env->FindClass(g_env,"com/goome/gpnsjni/NativePresenter");
     //得到里面的方法。
     jmethodID jmethod = g_env->GetMethodID(g_env,jclazz,"setWakeLockCpuStatus","(I)V");
     //调用方法
     g_env->CallVoidMethod(g_env,g_obj,jmethod,op);
     g_env->DeleteLocalRef(g_env,jclazz);
     #else
     jclass jclazz = (*g_env)->FindClass(g_env,"com/goome/gpnsjni/NativePresenter");
     //得到里面的方法。
     jmethodID jmethod = (*g_env)->GetMethodID(g_env,jclazz,"setWakeLockCpuStatus","(I)V");
     //调用方法
     (*g_env)->CallVoidMethod(g_env,g_obj,jmethod,op);
     (*g_env)->DeleteLocalRef(g_env,jclazz);
     #endif
     */
    //////////////////////////////////////////////////////////////////////////
}

void androidWakeCpuAwhile() {
    LOGI("androidWakeCpuAwhile");
    JNIEnv* env;
    int status = (*g_jvm)->AttachCurrentThread(g_jvm, &env, NULL);
    if (status < 0)
    {
        LOGI("AndroidWakeLock AttachCurrentThread error");
        return;
    }

    if (env == NULL)
    {
        return;
    }
#if defined(__cplusplus)
    //得到里面的方法。
    jmethodID jmethod = env->GetMethodID(presenterCls, "wakeCpuAwhile", "()V");
    //调用方法
    env->CallVoidMethod(presenterObj, jmethod);
#else
    //得到里面的方法。
    jmethodID jmethod = (*env)->GetMethodID(env,presenterCls,"wakeCpuAwhile","()V");
    //调用方法
    (*env)->CallVoidMethod(env,presenterObj,jmethod);
#endif
}

//若函数返回为非0，则数据并非合法的modified UTF-8
char checkUtfBytes(const char* bytes) {
    while (*bytes != '\0') {
        char utf8 = *(bytes++);
        switch (utf8 >> 4) {
        //一字节格式，以0开头，均为合法
        case 0x00:
        case 0x01:
        case 0x02:
        case 0x03:
        case 0x04:
        case 0x05:
        case 0x06:
        case 0x07:
            break;
        case 0x08:
        case 0x09:
        case 0x0a:
        case 0x0b:
        case 0x0f:
            //五字节和六字节显示格式均为非法，均以1111或10XX开头
            //1111在普通utf-8中还可存在于四字节格式中，但modified-utf-8认为四字节也是非法的
            return utf8;
        case 0x0e:
            //utf-8中汉字均以三字节显示，格式为0110XXXX 10XXXXXX 10XXXXXX
            //因此首字节前四位必为0X0E，第二字节前两位必为10，若满足则合乎规范
            utf8 = *(bytes++);
            if ((utf8 & 0xc0) != 0x80) {
                return utf8;
            }
            // 判断最后一个字节
        case 0x0c:
        case 0x0d:
            //utf-8中首字节以110开头的，使用两字节格式110XXXXX 10XXXXXX
            utf8 = *(bytes++);
            if ((utf8 & 0xc0) != 0x80) {
                return utf8;
            }
            break;
        }
    }
    return 0;
}

int getAttachedEnv(JNIEnv *env)
{
    LOGI("getAttachedEnv g_jvm=%p", g_jvm);
    if (g_jvm == NULL)
    {
        LOGE("g_jvm==NULL");
        return -1;
    }
    #if defined(__cplusplus)
        LOGE("GetEnv c++");
        int status = g_jvm->GetEnv((void **) &env, JNI_VERSION_1_6);
    #else
        LOGE("GetEnv c");
        int status = (*g_jvm)->GetEnv(g_jvm, (void **) &env, JNI_VERSION_1_6);
    #endif

    if (status < 0)
    {
        LOGE("callback handler:assuming native thread");
        #if defined(__cplusplus)
            status = g_jvm->AttachCurrentThread(&env, NULL);
        #else
            status = (*g_jvm)->AttachCurrentThread(g_jvm, &env, NULL);
        #endif
        if (status < 0)
        {
            LOGE("callback handler: failed to attache current thread");
            return -1;
        }
        g_bAttached = 1;
    }
    return 0;
}

void DetachCurrent()
{
    LOGI("DetachCurrent");
    if (g_bAttached)
    {
        #if defined(__cplusplus)
            LOGI("DetachCurrent c++");
            g_jvm->DetachCurrentThread();
        #else
            LOGI("DetachCurrent c");
            (*g_jvm)->DetachCurrentThread(g_jvm);
        #endif
    }
}
