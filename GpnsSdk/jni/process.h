#ifndef _PROCESS_HEAD_H
#define _PROCESS_HEAD_H

#include <sys/types.h>
#include <stdint.h>
#include <time.h>
#include "Log.h"

#define hton16(A) ((((uint16_t)(A) & 0xff00) >> 8) | \
                (((uint16_t)(A) & 0x00ff) << 8))
#define hton32(A) ((((uint32_t)(A) & 0xff000000) >> 24) | \
                (((uint32_t)(A) & 0x00ff0000) >> 8)  | \
                (((uint32_t)(A) & 0x0000ff00) << 8)  | \
                (((uint32_t)(A) & 0x000000ff) << 24))
#define hton64(A) (((((uint64_t)A)<<56) & 0xFF00000000000000ULL)  | \
                ((((uint64_t)A)<<40) & 0x00FF000000000000ULL)  | \
                ((((uint64_t)A)<<24) & 0x0000FF0000000000ULL)  | \
                ((((uint64_t)A)<< 8) & 0x000000FF00000000ULL)  | \
                ((((uint64_t)A)>> 8) & 0x00000000FF000000ULL)  | \
                ((((uint64_t)A)>>24) & 0x0000000000FF0000ULL)  | \
                ((((uint64_t)A)>>40) & 0x000000000000FF00ULL)  | \
                ((((uint64_t)A)>>56) & 0x00000000000000FFULL))

#define ntoh16  hton16
#define ntoh32  hton32
#define ntoh64  hton64

enum 
{
    ERR_OK = 0,
    ERR_DOMAIN,
    ERR_CREATE_SOCK,
    ERR_ALLOC,
    ERR_CONNECT_FAIL,
    ERR_PEER_CLOSE,
    ERR_READ_FAIL,
    ERR_WRITE_FAIL,
    ERR_INVALIDATE_MSG,
    ERR_UNKNOWN_MSG,
    ERR_INVALIDATE,
    ERR_INVALIDATE_PARAM,
    ERR_FAIL
};

enum
{
    STATUS_OK = 0,
    STATUS_UNKNOW,
    STATUS_HELLO_REPLAY_TIMEOUT,
    STATUS_HEART_REPLAY_TIMEOUT
};

typedef struct _MSG_COMMON_HEAD
{
    uint16_t  start;
    uint16_t  ver;
    uint16_t  len;
    uint8_t   protno;
    uint16_t  seq;
}__attribute__((packed)) MSG_COMMON_HEAD;

typedef struct _MSG_TAIL
{
    uint16_t  stop;
} __attribute((packed)) MSG_TAIL;

typedef struct _REQ_ID_MSG
{
    MSG_COMMON_HEAD comHead;
    uint16_t  stop;
} __attribute__((packed)) REQ_ID_MSG;

typedef struct _REQ_ID_REPLY_MSG
{
    MSG_COMMON_HEAD comHead;
    uint64_t  id;
    uint16_t  stop;
} __attribute__((packed)) REQ_ID_REPLY_MSG;

typedef struct _LOGIN_REPLY_MSG
{
    MSG_COMMON_HEAD comHead;
    uint64_t  id;
    uint16_t  stop;
}__attribute__((packed)) LOGIN_REPLY_MSG;

typedef struct _LOGIN_HEAD
{
    MSG_COMMON_HEAD comHead;
    uint64_t id;
    char verification[16];
    uint8_t  payload[0];
}__attribute__((packed)) LOGIN_HEAD;

typedef struct _HEART_MSG
{
    MSG_COMMON_HEAD comHead;
    uint16_t  stop;
} __attribute__((packed)) HEART_MSG;

typedef struct _PUSH_MSG
{
    MSG_COMMON_HEAD comHead;
    uint64_t msgid;
    uint8_t  payLoad[0];
}__attribute__((packed)) PUSH_MSG;

typedef struct 
{
    MSG_COMMON_HEAD comHead;
    uint64_t msgid;
    uint16_t stop;
} __attribute__((packed))PUSH_REPLY_MSG;


typedef PUSH_MSG            SET_ALIAS_MSG;
typedef LOGIN_REPLY_MSG     LOGIN_OUT_MSG;
typedef LOGIN_REPLY_MSG     LOGIN_OUT_REPLY_MSG;
typedef HEART_MSG           HEART_REPLY_MSG;


#define  LOST_RECONNECT_INTERVAL         60*2       // s
#define  HEART_PACKET_INTERVAL           285      // s
#define  MAX_HELLO_REPLAY_TIMEOUT        60       // s

#define  SOCK_CONNECT_TIMEOUT            10000     //ms
#define  EPOLL_WAIT_INTERVAL             5000    //ms

#define  LOST_RECONNECT_INTERVAL_DEBUG         5      // s    
#define  HEART_PACKET_INTERVAL_DEBUG           10     // s
#define  MAX_HELLO_REPLAY_TIMEOUT_DEBUG        10    // s

#define  SOCK_CONNECT_TIMEOUT_DEBUG            8000     // ms
#define  EPOLL_WAIT_INTERVAL_DEBUG             1000    // ms

struct GpnsProtocol
{
    char *m_buff;
    char *m_msgBuf;
    int   m_tail;
    int   m_status;
    int   m_cacheSize;
    int   m_seq;
    uint16_t m_endFlag;

    int    m_isStart;                   // 长连接是否已经正常建立（并且已经登录成功）；1=成功；0=失败。
    time_t m_sendStartTime;             // 发送HELLO包的时间
    time_t m_sendHeartbeatTime;         // 最近一次发送心跳包的时间
    time_t m_recvDataTime;              // 最近一次收到服务器返回数据的时间
};


int connectServer();

int isBigEnd();

void destroyProtocol(struct GpnsProtocol* pro);

int onRecvMsg(struct GpnsProtocol* pro,int fd);

int onTimeOut(struct GpnsProtocol* pro,int fd, int maxHelloReplayTimeout,int heartPacketInterval,int heartMaxReplyDelay);

void resetProtocolStatus(struct GpnsProtocol* pro);

int init(struct GpnsProtocol* pro);

int Login(struct GpnsProtocol* pro,int fd);

int NotifyStart(struct GpnsProtocol* pro,int fd,const char* devInfo);

int NotifyEnd(struct GpnsProtocol* pro,int fd);

int sendHeartBeat(struct GpnsProtocol* pro,int fd);

int recvPushMsg(struct GpnsProtocol* pro,int fd,char *buf);

int recvHelloReply(struct GpnsProtocol* pro,int fd,char *buf);

ssize_t WriteData(int fd, const void *buf, size_t len);

#endif