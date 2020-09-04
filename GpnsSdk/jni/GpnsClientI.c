#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <time.h>
#include <sys/epoll.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <unistd.h>
#include <signal.h>
#include "GpnsClientI.h"
#include "Network.h"
#include "process.h"
#include "Log.h"
#include "common.h"


#define MAX_DOMAIN_LEN        128
#define MAX_DEVINFO_LEN       2048

#define NAME_UID          "UID"
#define INVALIDATE_UID    0xFFFFFFFFFFFFFFFF
#define CHECK_FD_CLOSE(fd)   if(fd >= 0){ close(fd); fd = -1;}

#define DEBUG_MODE          0
#define NOT_DEBUG_MODE      1

//////////////////////////////////////////////////////////////////////////
// 使用线程局部变量，不要用进程内全局变量。init的时候设置一些全局的信息，start的时候才加载进线程局部变量（并创建epoll等）。
// 以避免java杀线程的时候没清理干净变量，泄露资源
//////////////////////////////////////////////////////////////////////////
int g_efd = -1;
int g_fd  = -1;
int g_port;
int g_mode = NOT_DEBUG_MODE;

int  g_stop = 0;
char g_domain[MAX_DOMAIN_LEN] = {0};
char g_devInfo[MAX_DEVINFO_LEN] = {0};
time_t g_connectTime;

uint64_t g_uid   = INVALIDATE_UID;
uint32_t g_appid = 0;
struct sockaddr_in g_addr;
int g_isBigEnd = 0;

int g_pipeFd[2] = {-1,-1};
struct GpnsProtocol g_GpnsProcess = {NULL,NULL,0};
int g_dwConnFailCnt = 0;    // 连接网关连续失败次数
int g_dwHeartOkCnt = 0;     // 心跳成功次数

int connectServer()
{

#define ADDR_SIZE  10
    char msg[1024] = {0};
    char szAddr[512] = {0};
    int ret,errCode,count;
    struct sockaddr_in addrs[ADDR_SIZE];
    memset(&addrs, 0, sizeof(struct sockaddr_in)*ADDR_SIZE);
    count = ADDR_SIZE;
    g_connectTime = time(NULL);
    if((ret =  getHostAddress(g_domain,g_port,addrs,&count)) != 0)
    {
        snprintf(msg,sizeof (msg),"Get host of domain[%s] fail",g_domain);
        sendAndroidRpl(C_DEBUG_INFO, msg);
        return ERR_DOMAIN;
    }

    unsigned int i;
    for(i = 0; i < count; ++i)
    {
        if(compareAddress(&g_addr,&addrs[i]))
        {
            continue;
        }

        g_addr = addrs[i];
        break;
    }

    addrToString(&g_addr, szAddr, sizeof(szAddr),g_isBigEnd);
    snprintf(msg,sizeof (msg),"Gateway domain[%s], addr:%s",g_domain, szAddr);
    sendAndroidRpl(C_DEBUG_INFO, msg);

    if(g_addr.sin_addr.s_addr != INADDR_NONE)
    {
        errCode = ERR_OK;

        do
        {
            if((g_fd = createSocket(0)) < 0)
            {
                errCode = ERR_CREATE_SOCK;
                break;
            }

            if((ret = setBlock(g_fd,0)) != 0)
            {
                errCode = ERR_FAIL;
                break;
            }

            if((ret = doConnect(g_fd,&g_addr,SOCK_CONNECT_TIMEOUT)) !=  0)
            {
                errCode = ERR_CONNECT_FAIL;
                break;
            }
        }while(0);

        if(errCode != ERR_OK)
        {
            snprintf(msg,sizeof (msg),"TCP connect fail, ret=%d.", errCode);
            sendAndroidRpl(CONNECT_FAILURE, msg);
            CHECK_FD_CLOSE(g_fd);
            return errCode;
        }

        snprintf(msg,sizeof (msg),"TCP connect ok, fd=%d.", g_fd);
        sendAndroidRpl(C_DEBUG_INFO, msg);

        return ERR_OK;
    }

    return ERR_FAIL;
}


void closeConnected(int *fdp)
{
    char msg[1024] = {0};
    g_connectTime = time(NULL); // 避免马上重连
    g_dwHeartOkCnt = 0;

    if(*fdp >= 0)
    {
        snprintf(msg, sizeof(msg), "Closing connection, fd:%d", *fdp);
        epoll_ctl(g_efd,EPOLL_CTL_DEL,*fdp,NULL);
        close(*fdp);
        *fdp = -1;
    }
    else
    {
        //尝试唤醒手机
        //androidWakeCpuAwhile();
        snprintf(msg, sizeof(msg), "Connection is closed now.");
    }
    sendAndroidRpl(C_DEBUG_INFO, msg);
}

int socketReconnect()
{
    char msg[1024] = {0};
    uint64_t cid_host = (g_isBigEnd) ? g_uid : ntoh64(g_uid);
    closeConnected(&g_fd);
    snprintf(msg, sizeof(msg), "Ready to Connect, cid=%"PRIu64, cid_host);
    sendAndroidRpl(C_DEBUG_INFO, msg);

    AndroidWakeLock(1);
    int ret = connectServer();
    AndroidWakeLock(0);

    if (ret != ERR_OK)
    {
        ++g_dwConnFailCnt;
        if (g_dwConnFailCnt >= 3)
        {
            // 连续若干次连接网关失败则退出程序，等待被外部拉起
            g_stop = 1;
            sendAndroidRpl(CONNECT_FAILURE, "Try to connect gateway continue fail, bye.");
        }
        
        return ret;
    }
    g_dwConnFailCnt = 0;

    if (g_mode == DEBUG_MODE)
    {
        char addrstr[512] = {0};
        addrToString(&g_addr,addrstr,512,g_isBigEnd);
        LOG_DEBUG("Connect Server[%s] success",addrstr);
    }

    resetProtocolStatus(&g_GpnsProcess);
    if(NotifyStart(&g_GpnsProcess,g_fd,g_devInfo))
    {
        sendAndroidRpl(CONNECT_FAILURE,"failed in notifying start");
        CHECK_FD_CLOSE(g_fd);
        return ERR_FAIL;
    }

    LOG_DEBUG("Send Hello Packet Success,time=%d",(int)time(NULL));

    struct epoll_event event;
    event.data.fd = g_fd;
    event.events  = EPOLLIN;

    if(epoll_ctl(g_efd,EPOLL_CTL_ADD,g_fd,&event))
    {
        sendAndroidRpl(CONNECT_FAILURE,"failed in registering the fd in epfd");
        CHECK_FD_CLOSE(g_fd);
        return ERR_FAIL;
    }

    return ERR_OK;
}

#ifdef __cplusplus
extern "C"
{
#endif

int initializeClient(char *domain,char *devInfo,int port,long cid, int mode)
{
    // Java调这里的时候，只设置全局变量，不实际做其他事
    g_mode = mode;

    memset(g_domain,0,MAX_DOMAIN_LEN);
    memset(g_devInfo,0,MAX_DEVINFO_LEN);

    if(domain != NULL)      strncpy(g_domain,domain,MAX_DOMAIN_LEN - 1);
    if(devInfo != NULL)     strncpy(g_devInfo ,devInfo,MAX_DEVINFO_LEN - 1);

    g_port = port;
    g_stop = 0;

    g_addr.sin_family = AF_INET;
    g_addr.sin_port   = 0;
    g_addr.sin_addr.s_addr = INADDR_NONE;

    g_uid   = (uint64_t)cid;
    g_isBigEnd = isBigEnd();
    if(!g_isBigEnd)
    {
        g_uid   = hton64(g_uid);
    }

    if(strlen(g_domain) == 0)
    {
        return ERR_FAIL;
    }

    CHECK_FD_CLOSE(g_efd);
    if((g_efd = epoll_create(2)) < 0)
    {
        LOG_WARN("Create Epoll Failed,errMsg:%s",strerror(errno));
        return ERR_FAIL;
    }

    CHECK_FD_CLOSE(g_fd);
    if(init(&g_GpnsProcess))
    {
        LOG_WARN("Initialize Gpns Process Failed");
        return ERR_FAIL;
    }

    CHECK_FD_CLOSE(g_pipeFd[0]);
    CHECK_FD_CLOSE(g_pipeFd[1]);

    if(createPipe(g_pipeFd))
    {
        LOG_WARN("Create Pipe Failed");
        return ERR_FAIL;
    }

    struct epoll_event event;
    event.data.fd = g_pipeFd[0];
    event.events  = EPOLLIN;

    if(epoll_ctl(g_efd,EPOLL_CTL_ADD,g_pipeFd[0],&event))
    {
        sendAndroidRpl(CONNECT_FAILURE,"failed in registering the pipeFd in epfd");
        return ERR_FAIL;
    }

    signal(SIGPIPE,SIG_IGN);

    char msg[1024] = {0};
    snprintf(msg, sizeof(msg), "Init OK! epoll_fd:%d, pipe_fd:%d", g_efd, g_pipeFd[0]);
    sendAndroidRpl(C_DEBUG_INFO, msg);

    return ERR_OK;
}


int startClientService()
{
    sendAndroidRpl(C_KEEP_ALIVE, "long connect start!");

    //////////////////////////////////////////////////////////////////////////
    // 纯sleep版，测耗电
    /*while(!g_stop)
    {
        sleep(5);
        sendAndroidRpl(C_KEEP_ALIVE, "pretending long connect running!");
    }*/
    //////////////////////////////////////////////////////////////////////////

    if(g_efd < 0)
    {
        sendAndroidRpl(CONNECT_FAILURE,"Invalid efd");
        return ERR_FAIL;
    }

    if (socketReconnect() != ERR_OK)
    {
        sendAndroidRpl(CONNECT_FAILURE,"Connect failed");
    }
    int recv,i;
    struct epoll_event events[10];
    char msg[1024] = {0};

    int lostReconnectInterval = (g_mode == NOT_DEBUG_MODE) ? LOST_RECONNECT_INTERVAL : LOST_RECONNECT_INTERVAL_DEBUG;
    int maxHelloReplayTimeout = (g_mode == NOT_DEBUG_MODE) ? MAX_HELLO_REPLAY_TIMEOUT : MAX_HELLO_REPLAY_TIMEOUT_DEBUG;
    int ePollWaitInterval	= (g_mode == NOT_DEBUG_MODE) ? EPOLL_WAIT_INTERVAL : EPOLL_WAIT_INTERVAL_DEBUG;

    //给予每个设备一个不同的重连时长，波动范围在标准重连时长的1到1.5倍之间
    srand((unsigned)time(0));
    int range = lostReconnectInterval * 0.5;
    int rd = rand() % range;
    lostReconnectInterval += rd;

    time_t lastTimeCheckTimeOut = time(0);
    int dwInterCnt = 0; // epoll连续被INTR中断的次数
    int checkTimeOut = 0;   // =1时表示epoll连续被INTR中断太多次，影响了进入正常的逻辑，必须强制检查一次是否需要做正常的逻辑。否则=0
    // 可以考虑本机listen一个端口，使用epoll监听。再使用AlarmManger每隔几分钟连接一次这个端口，触发epollwait事件

    while(!g_stop)
    {
        // 可考虑动态调整心跳频率，启动时可以短一些（如1分钟），当发心跳OK了几次后，慢慢提高到心跳最大间隔时间。目前暂不这么做
        int dwHeartInterval = (g_mode == NOT_DEBUG_MODE) ? HEART_PACKET_INTERVAL : HEART_PACKET_INTERVAL_DEBUG;
        int heartMaxReplyDelay	= (int)(2.2*dwHeartInterval);   // 允许的间隔为比两倍心跳时间略长

        // 由于某些安卓手机的睡眠功能，此处epoll可能沉睡很久（最坏情况下有上千秒），会影响后续逻辑。
        // 每次epoll睡醒后，通知一下上游自己还活着。如果安卓的AlarmManager定时器发现很久没收到这个通知，则会杀了本进程
        recv = epoll_wait(g_efd,events,10,ePollWaitInterval);
        sendAndroidRpl(C_KEEP_ALIVE, "long connect running!");

        if(g_stop)
        {
            sendAndroidRpl(CONNECT_BREAK,"Service Stop");
            break;
        }

        if(recv < 0)
        {
            if(errno != EINTR)
            {
                // TODO: 连续出现这种错误的话，是否考虑退出服务，避免狂写日志？
                snprintf(msg,sizeof (msg),"epoll fd:%d error: no[%d],des[%s]", g_efd, errno,strerror(errno));
                sendAndroidRpl(C_DEBUG_INFO,msg);
                continue;
            }

            //每次心跳包发送间隔中，若发生被INTR中断
            //必然有一次跳出continue循环检测心跳包是否超时，避免一直中断
            if (time(0) - lastTimeCheckTimeOut > 0.5*dwHeartInterval)
            {
                lastTimeCheckTimeOut = time(0);
                snprintf(msg, sizeof(msg), "Ignore frequent epoll interrupt. cnt=%d", dwInterCnt);
                sendAndroidRpl(C_DEBUG_INFO, msg);
                checkTimeOut = 1;
            }
            else
            {
                ++dwInterCnt;
                continue;
            }
        }

        dwInterCnt = 0;

        if(recv == 0 || checkTimeOut == 1)
        {
            checkTimeOut = 0;

            //当设备超时超过2-3min会重连
            if((g_fd < 0)
                && (time(NULL) - g_connectTime > lostReconnectInterval)
                && !g_stop)
            {
                sendAndroidRpl(C_DEBUG_INFO,"Try to reconnecting...");
                if (socketReconnect() == ERR_OK)
                {
                    sendAndroidRpl(RECONNECT_OK,"reconnect OK");
                }
                else
                {
                    sendAndroidRpl(RECONNECT_OK,"reconnect failed");
                }
            }

            if(g_fd >= 0)
            {
                //hello包及心跳包超时会重连
                onTimeOut(&g_GpnsProcess,g_fd,maxHelloReplayTimeout,dwHeartInterval,heartMaxReplyDelay);
                if((g_GpnsProcess.m_status == STATUS_HEART_REPLAY_TIMEOUT || g_GpnsProcess.m_status == STATUS_HELLO_REPLAY_TIMEOUT)
                    && !g_stop)
                {
                    sendAndroidRpl(CONNECT_FAILURE,"connection time out, trying to reconnect");
                    // 当长时间无心跳时，重试连接网关失败则立即退出程序。（以改失败计数的方式）
                    g_dwConnFailCnt = 2;
                    if (socketReconnect() == ERR_OK)
                    {
                        sendAndroidRpl(RECONNECT_OK,"reconnect OK");
                    }
                    else
                    {
                        sendAndroidRpl(RECONNECT_FAILURE,"reconnect failed");
                    }
                }
            }
        }

        for(i = 0; i < recv; ++i)
        {
            if(events[i].data.fd != g_fd && events[i].data.fd != g_pipeFd[0])
            {

                sendAndroidRpl(CONNECT_BREAK,"close fd");
                epoll_ctl(g_efd,EPOLL_CTL_DEL,events[i].data.fd,NULL);
                close(events[i].data.fd);
                continue;
            }

            if(events[i].data.fd == g_fd)
            {
                if(events[i].events & EPOLLIN)
                {
                    int dwRecvRet = onRecvMsg(&g_GpnsProcess,g_fd);
                    if(dwRecvRet != ERR_OK)
                    {
                        snprintf(msg,sizeof (msg),"failed in receiving msg from fd:%d, error[%d]", g_fd, dwRecvRet);
                        sendAndroidRpl(CONNECT_BREAK,msg);
                        closeConnected(&g_fd);
                    }
                }
                else //if((events[i].events & (EPOLLHUP | EPOLLERR)))
                {
                    sendAndroidRpl(CONNECT_BREAK,"receiving hup or error events from fd");
                    closeConnected(&g_fd);
                }
            }
            else
            {
                if(events[i].events & EPOLLIN)
                {
                    sendAndroidRpl(RECONNECT_OK,"EPOLLIN");
                    char buf[64] = {0};
                    read(events[i].data.fd,buf,sizeof(buf));
                }
                else //if(events[i].events & (EPOLLHUP | EPOLLERR))
                {
                    sendAndroidRpl(CONNECT_BREAK,"receiving hup or error events from fd");
                    epoll_ctl(g_efd,EPOLL_CTL_DEL,events[i].data.fd,NULL);
                    close(events[i].data.fd);
                }
            }
        }
    }

    return ERR_OK;
}

void stopClientService()
{
    sendAndroidRpl(SERVICE_STOP,"ready to stop service");
    g_stop = 1;

    if (NotifyEnd(&g_GpnsProcess,g_fd) == ERR_OK)
    {
        sendAndroidRpl(SERVICE_STOP,"notify end successfully");
    }
    else
    {
        sendAndroidRpl(SERVICE_STOP,"failed to notify end");
    }

    write(g_pipeFd[1],"F",1);

    CHECK_FD_CLOSE(g_fd);
    CHECK_FD_CLOSE(g_pipeFd[1]);
    CHECK_FD_CLOSE(g_pipeFd[0]);
    CHECK_FD_CLOSE(g_efd);

    sendAndroidRpl(SERVICE_STOP,"stop service successfully");
}

#ifdef __cplusplus
}
#endif
