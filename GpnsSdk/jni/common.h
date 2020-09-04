#ifndef _COMMON_HEAD_H_201506041713
#define _COMMON_HEAD_H_201506041713

#include "jni.h"
#include <inttypes.h>
#include <android/log.h>

#define CONNECT_OK              1           //连接成功
#define CONNECT_FAILURE         -1          //连接失败
#define CONNECT_BREAK           0           //连接中断
#define RECONNECT_OK            2           //重连成功
#define RECONNECT_FAILURE       -2          //重连失败
#define HEART_BEAT_STATUS       3           //心跳包状态(发送成功或失败，接收成功或失败)
#define HELLO_STATUS            4           //HELLO包状态(发送成功或失败，接收成功或失败)
#define SERVICE_STOP            5           //主动关闭服务
#define C_KEEP_ALIVE            10          // 通知上层，逻辑仍在运行
#define C_DEBUG_INFO            99          //调试信息

#ifndef LOG_TAG
#define LOG_TAG "fei"
#define DEBUG 0
#define LOGI(...) if(DEBUG){__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);}
#define LOGE(...) if(DEBUG){__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);}
#endif

#ifdef __cplusplus
extern "C"
{
#endif

char* jstringTostring(JNIEnv* env, jstring jstr);

void sendAndroidMsg(const char *msg);
void sendAndroidRpl(int status, const char *msg);
char checkUtfBytes(const char* bytes);

// 安卓手机随时可能进入睡眠模式，让CPU停止运行。因此关键代码（比如网络连接、写操作）时加锁，让CPU没法睡。执行完关键代码后再解锁。
// op=1是加锁，=0是解锁
void AndroidWakeLock(int op);

void androidWakeCpuAwhile();

int getAttachedEnv(JNIEnv* env);

void DetachCurrent();

#ifdef __cplusplus
}
#endif

#endif
