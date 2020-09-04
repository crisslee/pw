#include "GpnsClientI.h"
#include <stdio.h>
#include <string.h>

int main(int argc,char *argv[])
{
    printf("ready to start! \n");
    char host[128] = {0};
    char cfgName[] = "./gpnsclient.cfg";
    char devInfo[] = "{\"appVersion\":\"V1.7.0(784512)\",\"language\":\"zh\",\"deviceName\":\"google 4&5.0\",\"deviceModel\":\"android\"}";
    int mode = 0;
 
    if(argc > 1 && argv[1] != NULL)
    {
        strncpy(host,argv[1],127);
    }
    else
    {
        strncpy(host,"192.168.1.10",127);
    }

    if(initializeClient(host,devInfo,7727,0x0102,mode) != 0)
    {
        printf("initializeClient failed\r\n");
        return -1;
    }

    if(startClientService() != 0)
    {
        printf("startClientService return failed\r\n");
    }

    return 0;
}