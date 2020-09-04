#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>
#include <errno.h>
#include "process.h"
#include "Network.h"
#include "rdwrn.h"
//#include "cJSON.h"
#include "Log.h"
#include "common.h"
#include "md5.h"


#define  BUF_SIZE_128                    128
#define  BUF_SIZE_256                    256
#define  BUF_SIZE_2K                     2048      // 2K 
#define  START_FLAG                      0xACAC
#define  GPNS_VERSION                    1



#define  BIG_END_FLAG                    0x0D0A   

#define  SMALL_END_FLAG                  0x0A0D





#define PROTOCOL_NO_HELLO                0x1
#define PROTOCOL_NO_GOODBYE              0x2
#define PROTOCOL_NO_HEART                0x3
#define PROTOCOL_NO_PUSHMSG              0x4



#define PROTOCOL_NO_REPLY_HELLO          PROTOCOL_NO_HELLO
#define PROTOCOL_NO_REPLY_GOODBYE        PROTOCOL_NO_GOODBYE
#define PROTOCOL_NO_REPLY_HEART          PROTOCOL_NO_HEART
#define PROTOCOL_NO_REPLY_PUSHMSG        PROTOCOL_NO_PUSHMSG



#define INIT_COMMON_HEAD(po,h,p)  do{ \
h.comHead.start  = START_FLAG; \
h.comHead.ver    = g_isBigEnd ? GPNS_VERSION : hton16(GPNS_VERSION); \
h.comHead.protno = p; \
h.comHead.seq    = g_isBigEnd ? po->m_seq : hton16(po->m_seq); \
++po->m_seq; \
}while(0)



extern uint64_t g_uid;
extern int g_isBigEnd;
extern int g_dwHeartOkCnt;

void hexdump(const char *buf,size_t size)
{
    int i = 0;
    char myBuf[1024] = {0};

    size_t j;
    for(j = 0; j < size; ++j)
    {
        if(i < 1020)
            i += sprintf(myBuf + i,"%02x", (uint8_t)buf[j]); 
        else
            break;

        if(j == 1 || j == 3 || j == 5 || j == 6 || j == 8 || ((size>3)&&(j == size - 3)))
            i += sprintf(myBuf + i,"%s"," ");
    }

    LOG_WARN("-pack: %s\r\n",myBuf);  
}


int isBigEnd()
{
    uint16_t data;

    data = 0x0A0B;
    char c = ((char*)&data)[0];

    return c == 0x0A;
}


void destroyProtocol(struct GpnsProtocol* pro)
{
    if(pro->m_buff)
    {
        free(pro->m_buff);
        pro->m_buff = NULL;
    }

    if(pro->m_msgBuf)
    {
        free(pro->m_msgBuf);
        pro->m_msgBuf = NULL;
    }

    pro->m_tail = 0;
}

// 为CID生成加密验证信息。此处需和server端使用完全一样的盐和算法。
void GenCidEncryption(uint64_t uddwCidHost, char * pMd5)
{
    const char * p1 = "main(int argc, char * argv[])";
    const char * p2 = "1970-01-01";
    uint8_t szRawData[128] = {0};
    size_t udwLen = 0;
    memcpy(szRawData + udwLen, p1, strlen(p1));
    udwLen += strlen(p1);
    memcpy(szRawData + udwLen, &uddwCidHost, sizeof(uddwCidHost));
    udwLen += sizeof(uddwCidHost);
    memcpy(szRawData + udwLen, p2, strlen(p2));
    udwLen += strlen(p2);
    GenMD5((const unsigned char *)szRawData, (int)udwLen, pMd5);
}


int onRecvMsg(struct GpnsProtocol* pro,int fd)
{
    pro->m_status = STATUS_OK;
    pro->m_recvDataTime = time(NULL);

    if(pro->m_tail < BUF_SIZE_2K)
    {
        int recv = read(fd,pro->m_buff + pro->m_tail,BUF_SIZE_2K - pro->m_tail);
        if(recv < 0)
        {
            char msg[1024] = {0};
            snprintf(msg,sizeof (msg),"Read Failed,ErrMsg:%s",strerror(errno));
            sendAndroidRpl(C_DEBUG_INFO,msg);
            LOG_WARN("Read Failed,ErrMsg:%s",strerror(errno));
            return ERR_READ_FAIL;
        }
        else if(recv == 0)
        {
            sendAndroidRpl(C_DEBUG_INFO,"Peer Close Socket");
            LOG_INFO("Peer Close Socket");
            return ERR_PEER_CLOSE;
        }

        pro->m_tail += recv;
    }

    uint16_t leftLen = 0;
    uint16_t packLen = 0;
    int retCode = ERR_OK;

    MSG_COMMON_HEAD *msgHead;
    MSG_TAIL *msgTail;

    char *curP = pro->m_buff;
    char *endP = pro->m_buff + pro->m_tail;
    
    while(curP < endP)
    {
        leftLen = endP - curP;
        if(leftLen < sizeof(MSG_COMMON_HEAD))
        {
            if(curP != pro->m_buff)
            {
                memmove(pro->m_buff,curP, leftLen);
                pro->m_tail = leftLen;
            }

            return ERR_OK;
        }

        msgHead = (MSG_COMMON_HEAD*)curP;
        uint16_t version = g_isBigEnd? msgHead->ver : ntoh16(msgHead->ver);
        if(msgHead->start != START_FLAG || version != GPNS_VERSION)
        {
            char msg[1024] = {0};
            snprintf(msg,sizeof (msg),"Receive Invalidate Msg, start[0X%X],ver[%d]",msgHead->start,version);
            sendAndroidRpl(C_DEBUG_INFO,msg);
            LOG_WARN("Receive Invalidate Msg, start[0X%X],ver[%d]",msgHead->start,version);
            return ERR_INVALIDATE_MSG;
        }

        packLen = g_isBigEnd? msgHead->len : ntoh16(msgHead->len);
        if(packLen >  BUF_SIZE_2K - 6)
        {
            char msg[1024] = {0};
            snprintf(msg,sizeof (msg),"Receive Msg Len Invalidate,len[%d]",packLen);
            sendAndroidRpl(C_DEBUG_INFO,msg);
            LOG_WARN("Receive Msg Len Invalidate,len[%d]",packLen);
            return ERR_INVALIDATE_MSG;
        }

        if(leftLen < packLen + 6)
        {
            if(curP != pro->m_buff)
            {
                memmove(pro->m_buff,curP, leftLen);
                pro->m_tail = leftLen;
            }

            return ERR_OK;
        }

        msgTail = (MSG_TAIL*)(curP + 6 + packLen - sizeof(MSG_TAIL));
        if(msgTail->stop != pro->m_endFlag)
        {
            LOG_WARN("Receive Invalidate Msg,end[0X%X]",msgTail->stop);
            sendAndroidRpl(C_DEBUG_INFO,"Receive Invalidate Msg");
            return ERR_INVALIDATE_MSG;
        }

        if((pro->m_isStart <= 0) && (msgHead->protno != PROTOCOL_NO_REPLY_HELLO))
        {
            char msg[1024] = {0};
            snprintf(msg,sizeof (msg),"Receive Msg Before Hello Reply Just Drop It,TotalLen=%d,prono:0x%x",packLen + 6,msgHead->protno);
            sendAndroidRpl(C_DEBUG_INFO,msg);
            LOG_INFO("Receive Msg Before Hello Reply Just Drop It,TotalLen=%d,prono:0x%x",packLen + 6,msgHead->protno);
            hexdump(curP,packLen + 6);
            curP = curP + 6 + packLen;
            continue;
        }

        switch(msgHead->protno)
        {
            case PROTOCOL_NO_PUSHMSG:
                sendAndroidRpl(C_DEBUG_INFO,"receive push message");
                retCode =  recvPushMsg(pro,fd,curP);
                break;

            case PROTOCOL_NO_REPLY_HELLO:
                sendAndroidRpl(C_DEBUG_INFO,"receive hello reply");
                retCode = recvHelloReply(pro,fd,curP);
                break;

            case PROTOCOL_NO_REPLY_GOODBYE:
            case PROTOCOL_NO_REPLY_HEART:
                sendAndroidRpl(HEART_BEAT_STATUS,"receive heartbeat reply");
                retCode = ERR_OK;         // skip unprocess message
                ++g_dwHeartOkCnt;
                break;

            default:
                sendAndroidRpl(C_DEBUG_INFO,"Receive unknown msg");
                LOG_INFO("Receive Unknow Pro[%d] Msg",msgHead->protno);
                hexdump(curP,6 + packLen);

                retCode = ERR_OK; // skip
        }

        if(retCode != ERR_OK)
        {
            char msg[1024] = {0};
            snprintf(msg,sizeof (msg),"Unknown retCode: %d",retCode);
            sendAndroidRpl(C_DEBUG_INFO,msg);
            return retCode;
        }

        char msg[1024] = {0};
        snprintf(msg,sizeof (msg),"Receive Msg,TotalLen=%d,prono:0x%x",packLen + 6,msgHead->protno);
        sendAndroidRpl(C_DEBUG_INFO,msg);

        LOG_INFO("Receive Msg,TotalLen=%d,prono:0x%x",packLen + 6,msgHead->protno);
        hexdump(curP,packLen + 6);

        curP = curP + 6 + packLen;
    }

    pro->m_tail = 0;

    return ERR_OK;
}

int onTimeOut(struct GpnsProtocol* pro,int fd, int maxHelloReplayTimeout, int heartPacketInterval, int heartMaxReplyDelay)
{
    char msg[1024] = {0};
    //LOG_DEBUG("On Time OUT,sendtime:%lu recvtime:%lu",m_sendDataTime,m_recvDataTime);
    time_t nowSec = time(NULL);
    if(pro->m_isStart <= 0)
    {
        // 还未成功（通过hello包方式）登陆服务器，检查登陆是否超时
        if(nowSec - pro->m_sendStartTime >= maxHelloReplayTimeout)
        {
             sendAndroidRpl(C_DEBUG_INFO,"hello replay timeout");
             pro->m_status = STATUS_HELLO_REPLAY_TIMEOUT;   //reconnect
        }
        sendAndroidRpl(C_DEBUG_INFO,"hello replay in time");
        return ERR_OK;
    }

    if (nowSec - pro->m_recvDataTime < heartPacketInterval)
    {
        // 间隔时间内与服务器有过通信，认为连接健康
        return ERR_OK;
    }
    else if(nowSec - pro->m_recvDataTime < heartMaxReplyDelay)
    {
        // 与服务器的正常通信时间在合理范围。
        if (nowSec - pro->m_sendHeartbeatTime < heartPacketInterval)
        {
            // 不久前主动发过心跳包，避免频繁重试发送心跳包，在这里就不发了
            snprintf(msg,sizeof (msg),"last try send hearbeat :%d, now:%d, interval<%d.", (int)pro->m_sendHeartbeatTime, (int)nowSec, (int)heartPacketInterval);
            sendAndroidRpl(C_DEBUG_INFO, msg);
        }
        else
        {
            // 有一段时间没主动发过心跳包了，正常发送心跳
            LOG_INFO("Resend HeartBeat");
            snprintf(msg,sizeof (msg),"Heartbeat time. Last hearbeat:%d, now:%d, interval>=%d.", (int)pro->m_recvDataTime, (int)nowSec, (int)heartPacketInterval);
            sendAndroidRpl(C_DEBUG_INFO, msg);
            if (sendHeartBeat(pro,fd) == ERR_OK)
            {
                sendAndroidRpl(HEART_BEAT_STATUS,"send heart beat ok");
            }
            else
            {
                sendAndroidRpl(HEART_BEAT_STATUS,"send heart beat failed");
            }
        }
    }
    else
    {
        // 已经很长时间没跟服务器正常通信了，认为超时断连
        sendAndroidRpl(HEART_BEAT_STATUS,"HeartBeat Timeout,Reconnect The Socket");
        LOG_INFO("HeartBeat Timeout,Reconnect The Socket");
        pro->m_status = STATUS_HEART_REPLAY_TIMEOUT;
    }

    return ERR_OK;
}

void resetProtocolStatus(struct GpnsProtocol* pro)
{
    pro->m_tail = 0;
    pro->m_sendHeartbeatTime = 0;
    pro->m_recvDataTime  = 0;
    pro->m_status = STATUS_UNKNOW;
    pro->m_isStart = 0;
    pro->m_sendStartTime = 0;
}


int sendHeartBeat(struct GpnsProtocol* pro,int fd)
{
    HEART_MSG hd;

    uint16_t len = (char*)&hd + sizeof(HEART_MSG) - (char*)&(hd.comHead.protno);
    pro->m_sendHeartbeatTime = time(0);
    LOG_DEBUG("Try send HeartBeat,time:%lu", pro->m_sendHeartbeatTime);

    INIT_COMMON_HEAD(pro,hd,PROTOCOL_NO_HEART);
    hd.comHead.len    = g_isBigEnd? len : hton16(len);
    hd.stop           = pro->m_endFlag;

    if(WriteData(fd,&hd,sizeof(HEART_MSG)) != sizeof(HEART_MSG))
    {
        sendAndroidRpl(C_DEBUG_INFO,"Send Heart Beat Package Failed");
        LOG_WARN("Send Heart Beat Package Failed");
        return ERR_WRITE_FAIL;
    }

    return ERR_OK;
}

int recvHelloReply(struct GpnsProtocol* pro,int fd,char *curP)
{
    LOGIN_REPLY_MSG *replyMsgP = (LOGIN_REPLY_MSG*)curP;
    int packLen = g_isBigEnd? replyMsgP->comHead.len : ntoh16(replyMsgP->comHead.len);

    if((packLen != sizeof(LOGIN_REPLY_MSG) - 6) || replyMsgP->id != g_uid)
    {
        sendAndroidRpl(C_DEBUG_INFO,"Receive Invalidate Hello Reply Msg");
        LOG_WARN("Receive Invalidate Hello Reply Msg");
        hexdump(curP,packLen + 6);

        return ERR_INVALIDATE_MSG;
    }
    sendAndroidRpl(HELLO_STATUS,"Receive Hello Reply Success");
    LOG_DEBUG("Receive Hello Reply Success,time=%d",(int)time(NULL));

    pro->m_isStart = 1;

    sendAndroidRpl(CONNECT_OK,"Login ok");
    return ERR_OK;
}

int recvPushMsg(struct GpnsProtocol* pro,int fd,char *buf)
{
    PUSH_MSG *msgHeadP = (PUSH_MSG*)buf;
    int packLen = g_isBigEnd? msgHeadP->comHead.len : ntoh16(msgHeadP->comHead.len);
    int msgLen  = packLen - sizeof(MSG_TAIL) - ((char*)msgHeadP->payLoad - (char*)&(msgHeadP->comHead.protno));

    if(msgLen < 0)
    {
        sendAndroidRpl(C_DEBUG_INFO,"Receive Invalidate Push Msg");
        LOG_WARN("Receive Invalidate Push Msg");
        hexdump(buf,packLen + 6);
        return ERR_OK;
    }

    if(msgLen > 0 && (msgLen < BUF_SIZE_2K - 1))
    {
        memcpy(pro->m_msgBuf,msgHeadP->payLoad,msgLen);
        (pro->m_msgBuf)[msgLen] = '\0';

        char msg[BUF_SIZE_2K] = {0};
        snprintf(msg,sizeof (msg),"msgLen:%d, Receive push Msg:%s",msgLen, pro->m_msgBuf);
        sendAndroidRpl(C_DEBUG_INFO,msg);
        LOG_INFO("Receive push Msg:%s",pro->m_msgBuf);
        sendAndroidMsg(pro->m_msgBuf);
    }

#if 0
    cJSON *root;

    do
    {
        cJSON *temp;
        root = cJSON_Parse(pro->m_msgBuf);
        if(root == NULL)
        {
            LOG_WARN("Parse push Msg failed,msg:%s",pro->m_msgBuf);
            break;
        }

        temp = cJSON_GetObjectItem(root,"content");
        if(temp == NULL || temp->type != cJSON_String)
        {
            break;
        }

        if(temp->valuestring != NULL)
        {
            sendAndroidMsg(temp->valuestring);
        }
    }while(0);

    if(root)   cJSON_Delete(root);
#endif

    //reply push msg
    PUSH_REPLY_MSG pushReplyMsg;
    uint16_t len = (char*)&pushReplyMsg + sizeof(PUSH_REPLY_MSG) - (char*)&(pushReplyMsg.comHead.protno);

    pushReplyMsg.comHead.start  = START_FLAG;
    //pushReplyMsg.comHead.ver    = GPNS_VERSION;
    pushReplyMsg.comHead.ver    = g_isBigEnd? GPNS_VERSION:hton16(GPNS_VERSION);
    pushReplyMsg.comHead.len    = g_isBigEnd? len : hton16(len);
    pushReplyMsg.comHead.protno = PROTOCOL_NO_REPLY_PUSHMSG;
    pushReplyMsg.comHead.seq    = g_isBigEnd? pro->m_seq : hton16(pro->m_seq);
    pushReplyMsg.msgid = msgHeadP->msgid;
    pushReplyMsg.stop  = pro->m_endFlag;

    ++pro->m_seq;
    if(WriteData(fd,&pushReplyMsg,sizeof(PUSH_REPLY_MSG)) != sizeof(PUSH_REPLY_MSG))
    {
        sendAndroidRpl(C_DEBUG_INFO,"Reply Push Msg Failed");
        LOG_WARN("Reply Push Msg Failed");

        //return ERR_WRITE_FAIL;
    }

    return ERR_OK;
}



int NotifyStart(struct GpnsProtocol* pro,int fd,const char *devInfo)
{
    char msg[1024] = {0};
    int msglen;
    LOGIN_HEAD *msgHeadP = (LOGIN_HEAD*)pro->m_buff;

    msgHeadP->comHead.start  = START_FLAG;
    msgHeadP->comHead.ver    = g_isBigEnd? GPNS_VERSION : hton16(GPNS_VERSION);
    msgHeadP->comHead.protno = PROTOCOL_NO_HELLO;
    msgHeadP->comHead.seq    = g_isBigEnd? pro->m_seq : hton16(pro->m_seq);
    msgHeadP->id             = g_uid;
    uint64_t cid_host = (g_isBigEnd) ? g_uid : ntoh64(g_uid);
    GenCidEncryption(cid_host, (char*)msgHeadP->verification);

    ++pro->m_seq;
    msglen = sizeof(LOGIN_HEAD);


    if(devInfo)
    {
        unsigned int devinfLen = strlen(devInfo);
        if(devinfLen > BUF_SIZE_2K - sizeof(LOGIN_HEAD) - sizeof(MSG_TAIL) - 1)
        {
            return ERR_FAIL;
        }

        memcpy(msgHeadP->payload,devInfo,devinfLen);
        msglen += devinfLen;
    }

    MSG_TAIL *msgTailP = (MSG_TAIL*)(pro->m_buff + msglen);
    msgTailP->stop = pro->m_endFlag;

    msglen += sizeof(MSG_TAIL);
    uint16_t len = pro->m_buff + msglen - (char*)&(msgHeadP->comHead.protno);
    msgHeadP->comHead.len = g_isBigEnd ? len : hton16(len);

    if(WriteData(fd,pro->m_buff,msglen) != msglen)
    {
        snprintf(msg, sizeof(msg), "Send hello package fail, cid=%llu", (unsigned long long)cid_host);
        sendAndroidRpl(HELLO_STATUS, msg);
        return ERR_WRITE_FAIL;
    }
    pro->m_sendStartTime = time(NULL);
    snprintf(msg, sizeof(msg), "Send hello package successfully, cid=%llu", (unsigned long long)cid_host);
    sendAndroidRpl(HELLO_STATUS, msg);

    return ERR_OK;
}



int NotifyEnd(struct GpnsProtocol* pro,int fd)
{
    LOGIN_OUT_MSG msgOut;

    uint16_t len = (char*)&msgOut + sizeof(LOGIN_OUT_MSG) - (char*)&(msgOut.comHead.protno);
    INIT_COMMON_HEAD(pro,msgOut,PROTOCOL_NO_GOODBYE);
    msgOut.comHead.len = g_isBigEnd? len : hton16(len);
    msgOut.id = g_uid;
    msgOut.stop  = pro->m_endFlag;

    if(WriteData(fd,&msgOut,sizeof(LOGIN_OUT_MSG)) != sizeof(LOGIN_OUT_MSG))
    {
        return ERR_WRITE_FAIL;
    }

    return ERR_OK;
}

int init(struct GpnsProtocol* pro)
{
    pro->m_seq  = 1;
    pro->m_tail = 0;
    pro->m_status = STATUS_UNKNOW;
    pro->m_endFlag = g_isBigEnd? BIG_END_FLAG : SMALL_END_FLAG;
    pro->m_isStart = 0;
    pro->m_sendStartTime = 0;

    if(pro->m_buff == NULL)
    {
        pro->m_buff = (char*)malloc(BUF_SIZE_2K);
        if(pro->m_buff == NULL)
        {
            return ERR_ALLOC;
        }
    }

    if(pro->m_msgBuf == NULL)
    {
        pro->m_msgBuf = (char*)malloc(BUF_SIZE_2K);
        if(pro->m_msgBuf == NULL)
        {
            return ERR_ALLOC;
        }
    }

    pro->m_sendHeartbeatTime = 0;
    pro->m_recvDataTime  = 0;

    return ERR_OK;
}

ssize_t WriteData(int fd, const void *buf, size_t len)
{
    AndroidWakeLock(1);
    ssize_t ret = writen(fd, buf, len);
    AndroidWakeLock(0);
    return ret;
}

