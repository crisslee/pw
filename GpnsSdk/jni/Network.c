#include <time.h>
#include <poll.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <arpa/inet.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <netdb.h>
#include "Network.h"

int interrupted()
{
    return errno == EINTR;
}

int wouldBlock()
{
    return (errno == EAGAIN || errno == EWOULDBLOCK);
}

int connectFailed()
{
    return (errno == ECONNREFUSED || 
           errno == ETIMEDOUT ||
           errno == ENETUNREACH || 
           errno == EHOSTUNREACH || 
           errno == ECONNRESET || 
           errno == ESHUTDOWN || 
           errno == ECONNABORTED);
}

int connectionRefused()
{
    return errno == ECONNREFUSED;
}

int connectInProgress()
{
    return errno == EINPROGRESS;
}

int connectionLost()
{
    return (errno == ECONNRESET ||
           errno == ENOTCONN ||
           errno == ESHUTDOWN ||
           errno == ECONNABORTED ||
           errno == EPIPE);
}

int createSocket(int udp)
{
    SOCKET fd;
    
    if(udp)
    {
        fd = socket(AF_INET,SOCK_DGRAM,0);
    }
    else
    {
        fd = socket(AF_INET,SOCK_STREAM,0);
    }

    if(fd < 0)
    {
        return SOCKET_ERR;
    }
    
    if(!udp)
    {
        setTcpNoDelay(fd);
        setKeepAlive(fd);
    }

    return fd;
}


int closeSocket(SOCKET fd)
{
    int err = errno;
    int flag = close(fd);
    errno = err;

    return flag;
}

int shutdownSocketWrite(SOCKET fd)
{
    return shutdown(fd,SHUT_WR);
}

int shutdownSocketRead(SOCKET fd)
{
    return shutdown(fd,SHUT_RD);
}

int shutdownSocketReadWrite(SOCKET fd)
{
    return shutdown(fd,SHUT_RDWR);
}
 
int setBlock(SOCKET fd,int block)
{
    int flag = fcntl(fd,F_GETFL);
    if(block)
    {
        flag &= ~O_NONBLOCK; 
    }
    else 
    {
        flag |= O_NONBLOCK;
    }

    if(fcntl(fd,F_SETFL,flag))
    {
        return SOCKET_ERR;
    }

    return 0;
}

int setTcpNoDelay(SOCKET fd)
{
    int flag = 1;
    if(setsockopt(fd,IPPROTO_TCP,TCP_NODELAY,(char*)&flag,sizeof(int)))
    {
        return SOCKET_ERR;
    }

    return 0;
}

int setKeepAlive(SOCKET fd)
{
    int flag = 1;
    if(setsockopt(fd,SOL_SOCKET,SO_KEEPALIVE,(char*)&flag,sizeof(int)))
    {
        return SOCKET_ERR;
    }

    return 0;
}

int setKeepAliveParameter(SOCKET fd,int idletime,int interval,int cnt)
{
    if(setsockopt(fd,SOL_TCP,TCP_KEEPIDLE,&idletime,sizeof(idletime)) == -1)
    {
        return SOCKET_ERR;
    }

    if(setsockopt(fd,SOL_TCP,TCP_KEEPINTVL,&interval,sizeof(interval)) == -1)
    {
        return SOCKET_ERR;
    }

    if(setsockopt(fd,SOL_TCP,TCP_KEEPCNT,&cnt,sizeof(cnt)) == -1)
    {
        return SOCKET_ERR;
    }

    return 0;
}

int setSendBufferSize(SOCKET fd,int sz)
{
    if(setsockopt(fd,SOL_SOCKET,SO_SNDBUF,(char*)&sz,sizeof(int)))
    {
        return SOCKET_ERR;
    }

    return 0;
}

int getSendBufferSize(SOCKET fd)
{
    int sz = -1;
    socklen_t len = sizeof(int);
    if(getsockopt(fd,SOL_SOCKET,SO_SNDBUF,(char*)&sz,&len) || len != sizeof(int))
    {
        return SOCKET_ERR;
    }

    return sz;
}

int setRecvBufferSize(SOCKET fd,int sz)
{
    if(setsockopt(fd,SOL_SOCKET,SO_RCVBUF,(char*)&sz,sizeof(int)))
    {
        return SOCKET_ERR;
    }

    return 0;
}

int getRecvBufferSize(SOCKET fd)
{
    int rz = -1;
    socklen_t len = sizeof(int);
    if(getsockopt(fd,SOL_SOCKET,SO_RCVBUF,(char*)&rz,&len) || len != sizeof(int))
    {
        return SOCKET_ERR;
    }

    return rz;
}


int doConnect(SOCKET fd,struct sockaddr_in *addr,int timeout)
{

   int ret;
   struct pollfd pollfd[1];
   
repeatConnect:
    if(connect(fd,(struct sockaddr*)addr,sizeof(struct sockaddr_in)))
    {
        if(interrupted())
        {
            goto repeatConnect;
        }

        if(connectInProgress())
        {
            pollfd[0].fd = fd;
            pollfd[0].events = POLLOUT;

        repeatPoll:
            ret = poll(pollfd,1,timeout);
            if(ret == 0)
            {
                char msg[1024] = {0};
                snprintf(msg,sizeof (msg),"doConnect error, poll being out of time");
                sendAndroidRpl(C_DEBUG_INFO,msg);
                return SOCKET_ERR;
            }
            else if(ret < 0)
            {
                char msg[1024] = {0};

                if(interrupted())
                {
                    sendAndroidRpl(C_DEBUG_INFO, "poll interrupted by system, wait again...");
                    goto repeatPoll;
                }

                snprintf(msg,sizeof (msg),"doConnect error, poll error: no[%d],des[%s]",errno,strerror(errno));
                sendAndroidRpl(C_DEBUG_INFO,msg);
                return SOCKET_ERR;
            }

            int value = 0;
            socklen_t len = sizeof(int);
            int get_ret = getsockopt(fd,SOL_SOCKET,SO_ERROR,(char*)&value,&len);
            if((0 != get_ret) || (value > 0))
            {
                char msg[1024] = {0};
                if (0 != get_ret)
                {
                    snprintf(msg,sizeof (msg),"getsockopt error, ret=%d, connection status: no[%d],des[%s]", get_ret, errno, strerror(errno));
                }
                else
                {
                    snprintf(msg,sizeof (msg),"getsockopt error, ret=%d, SO_ERROR=%d(%s)", get_ret, value, strerror(value));
                }
                sendAndroidRpl(C_DEBUG_INFO,msg);
                return SOCKET_ERR;
            }

            return 0;
        }

        char msg[1024] = {0};
        snprintf(msg,sizeof (msg),"doConnect error, connect error, not interrupted nor in progress: no[%d],des[%s]",errno,strerror(errno));
        sendAndroidRpl(C_DEBUG_INFO,msg);
        return SOCKET_ERR;
    }

    return 0;
}


int getHostAddress(const char* host,int port,struct sockaddr_in *addrs,int *psize)
{
    int retry = 5;
    struct addrinfo hints = {0};
    struct addrinfo *res = NULL;

    hints.ai_family = AF_INET;
    hints.ai_socktype = SOCK_STREAM;
    int rs = 0;
    char buf[128] = {0};
    sprintf(buf,"%d",port);

    int totalsize = *psize;
    *psize = 0;

    do {
       rs = getaddrinfo(host, buf,&hints,&res);
    } while(res == NULL && rs == EAI_AGAIN && --retry >=0);

    if(rs != 0 || res == NULL)
    {
        return SOCKET_ERR;
    }

    int count = 0;
    struct addrinfo *cur = NULL;
    for(cur = res; cur != NULL;cur = cur->ai_next)
    {
        if(cur->ai_family != AF_INET || !cur->ai_addr)
        {
            continue;
        }

        if(++count > totalsize)   break;
        addrs[count - 1] = *((struct sockaddr_in*)cur->ai_addr);
    }

    if(res)
    {
        freeaddrinfo(res);
    }

    *psize = count > totalsize? totalsize : count;

    return 0;
}


int compareAddress(const struct sockaddr_in*  addr1,const struct sockaddr_in* addr2)
{
    if((addr1->sin_family == addr2->sin_family) &&
       (addr1->sin_port == addr2->sin_port) &&
       (addr1->sin_addr.s_addr == addr2->sin_addr.s_addr))
    {
        return 1;
    }

    return 0;
}

int createPipe(SOCKET fds[2])
{
    if(pipe(fds))
    {
        return SOCKET_ERR;
    } 

    setBlock(fds[0],0);
    setBlock(fds[1],0);

    return 0;
}



void addrToString(const struct sockaddr_in* addr,char *bf,int size,int bigEnd)
{
    char buf[128] =  {0};

    memset(bf,0,size);
    if(inet_ntop(AF_INET,(const void*)&(addr->sin_addr),buf,128)!= NULL)
    {
       strcat(bf,buf);
    }

    strcat(bf,":");
    sprintf(buf, "%d", bigEnd>0? addr->sin_port : ntohs(addr->sin_port));
    strcat(bf,buf);
}


int getSocketErrno()
{
    return errno;
}