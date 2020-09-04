#ifndef __CLIENT_HEAD_H_
#define __CLIENT_HEAD_H_

#ifdef __cplusplus
extern "C"
{
#endif

int initializeClient(char *domain,char *devInfo,int port,long cid, int mode);

int startClientService();

void stopClientService();

#ifdef __cplusplus
}
#endif

#endif