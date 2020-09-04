LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := GpnsSDK_1.4.7
LOCAL_SRC_FILES := gpns.c \
common.c \
GpnsClientI.c \
md5.c \
Network.c \
process.c \
rdwrn.c \

LOCAL_LDLIBS := -llog
#LOCAL_LDLIBS := -lGLESv1_CM -llog -lz

include $(BUILD_SHARED_LIBRARY)
