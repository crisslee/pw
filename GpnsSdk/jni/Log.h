#ifndef __LOG_HEAD_H_
#define __LOG_HEAD_H_

//#define __DEBUG__
#ifndef __DEBUG__
#define LOG_INFO(fmt,...)    {}
#define LOG_DEBUG(fmt,...)   {}
#define LOG_WARN(fmt,...)    {}
#define LOG_ERROR(fmt,...)   {}
#define LOG_FATAL(fmt,...)   {}
#else
#include <stdio.h>
#define LOG_DEBUG(fmt,...)   printf("[%s:%d] "fmt"\r\n",__FUNCTION__,__LINE__,##__VA_ARGS__)
#define LOG_INFO(fmt,...)    printf("[%s:%d] "fmt"\r\n",__FUNCTION__,__LINE__,##__VA_ARGS__)
#define LOG_WARN(fmt,...)    printf("[%s:%d] "fmt"\r\n",__FUNCTION__,__LINE__,##__VA_ARGS__)
#define LOG_ERROR(fmt,...)   printf("[%s:%d] "fmt"\r\n",__FUNCTION__,__LINE__,##__VA_ARGS__)
#define LOG_FATAL(fmt,...)   printf("[%s:%d] "fmt"\r\n",__FUNCTION__,__LINE__,##__VA_ARGS__)
#endif

#endif