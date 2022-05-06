LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := libgpr
LOCAL_SRC_FILES += EfsChannel.cpp
LOCAL_SRC_FILES += IPort.cpp
LOCAL_SRC_FILES += lte2700Interface.cpp
LOCAL_SRC_FILES += PortTCPClient.cpp
LOCAL_SRC_FILES += PortUdp.cpp
LOCAL_SRC_FILES += RadarDevice.cpp
LOCAL_SRC_FILES += RadarQuery.cpp
LOCAL_SRC_FILES += ThreadManager.cpp
LOCAL_SRC_FILES += tool_fir.cpp
LOCAL_SRC_FILES += com_ltd_test_Test.cpp
LOCAL_SRC_FILES += Classify_breath.cpp
LOCAL_SRC_FILES += DllFile.cpp
LOCAL_SRC_FILES += DllApp.cpp
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)
