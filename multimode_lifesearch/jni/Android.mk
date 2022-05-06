LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := USBDriver-JNI
LOCAL_SRC_FILES := USBDriver/com_ltdpro_USBDriver.cpp
LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
#
#LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := libDetect
LOCAL_SRC_FILES := Detect/com_ltdpro_BaseDetect.cpp
LOCAL_CFLAGS += -O2 -Wall

include $(BUILD_SHARED_LIBRARY)
