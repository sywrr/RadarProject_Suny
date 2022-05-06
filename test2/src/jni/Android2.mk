LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := gpr
LOCAL_SRC_FILES := ../libs/arm64-v8a/libgpr.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := test
LOCAL_SRC_FILES := com_ltd_test_Test.cpp
LOCAL_SHARED_LIBRARIES := gpr
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)