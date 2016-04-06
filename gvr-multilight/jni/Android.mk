LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := gvrmultilight
LOCAL_SRC_FILES := gvrmultilight.cpp

include $(BUILD_SHARED_LIBRARY)
