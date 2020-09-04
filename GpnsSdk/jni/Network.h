#ifndef __NETWORK_COMMON_OPER__H_
#define __NETWORK_COMMON_OPER__H_

#include <sys/types.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <sys/socket.h>
#include <sys/poll.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <stdint.h>
#include "common.h"

#define SOCKET int
#define SOCKET_ERR  -1
#define ADDR_ERR    -2
#define MAX_LOOP_COUNT  15

#ifdef __cplusplus
extern "C"
{
#endif

   int interrupted();
   int wouldBlock();
   int connectFailed();
   int connectionRefused();
   int connectInProgress();
   int connectionLost();
   int notConnected();


   int createSocket(int udp);
   int closeSocket(SOCKET fd);
   int shutdownSocketWrite(SOCKET fd);
   int shutdownSocketRead(SOCKET fd);
   int shutdownSocketReadWrite(SOCKET fd);

   int setBlock(SOCKET fd,int block);
   int setTcpNoDelay(SOCKET fd);
   int setKeepAlive(SOCKET fd);
   int setKeepAliveParameter(SOCKET fd,int idletime,int interval,int cnt);
   int setSendBufferSize(SOCKET fd,int sz);
   int getSendBufferSize(SOCKET fd);
   int setRecvBufferSize(SOCKET,int sz);
   int getRecvBufferSize(SOCKET fd);


   int doConnect(SOCKET fd,struct sockaddr_in *addr,int timeout);
   int getHostAddress(const char* host,int port,struct sockaddr_in *addrs,int *psize);
   int compareAddress(const struct sockaddr_in *addr1,const struct sockaddr_in *addr2);

   int createPipe(SOCKET fds[2]);
   void addrToString(const struct sockaddr_in* addr,char *bf,int size,int bigEnd);
   int getSocketErrno();

#ifdef __cplusplus
}
#endif

#endif