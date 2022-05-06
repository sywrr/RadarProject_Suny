LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := libDetectRadarData
LOCAL_SRC_FILES := RadarData/com_ltd_lifesearchapp_Detect_RadarData.cpp
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE := libDetect
LOCAL_SRC_FILES := Detect/com_ltd_lifesearchapp_Detect_DetectUtil.cpp

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE := gpr
LOCAL_SRC_FILES := Test/arm64-v8a/libgpr.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libTest
LOCAL_SRC_FILES := Test/com_ltd_lifesearchapp_Test.cpp
LOCAL_SHARED_LIBRARIES := gpr
LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)