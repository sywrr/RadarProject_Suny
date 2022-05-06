package com.ltd.lifesearchapp;

import android.os.Environment;
import android.util.Pair;

import java.io.File;
import java.io.IOException;

public class Test {

    static {
        System.loadLibrary("gpr");
        System.loadLibrary("Test");
    }

    public static native int createInstance(int netType, String localIp, String deviceIp,int antenaType,int dllFlag);

    public static native int releaseInstance();

    public static native String lastErrorString();

    public static native int saveSetting(String settings, boolean isOnline);

    public static native int start();

    public static native int stop();

    private static native int runningStatus(long ptrRet);

    private static native int receivedData(byte[] data, long ptrStep, long ptrSize);

    private static native long getIntPointer();

    private static native void freeIntPointer(long ptr);

        private static native int getPointerValue(long ptr);

        public static native int beginSaveData(String fileDir, long user);

        public  static native int endSaveData();
        public static boolean runningStatus() {
            long ptr = getIntPointer();
        try {
            int ret = runningStatus(ptr);
            if (ret != 0) {
                throw new IllegalStateException("check device state error");
            }
            return getPointerValue(ptr) == 1;
        } finally {
            freeIntPointer(ptr);
        }
    }

    public static Pair<Integer, Integer> receivedData(byte[] data) throws IOException {
        long ptrStep = getIntPointer();
        long ptrSize = getIntPointer();
        try {
            int ret = receivedData(data, ptrStep, ptrSize);
            if (ret == 2) {
                throw new IOException("receive data error");
            }
            if (ret == 1) {
                return new Pair<>(0, 0);
            } else {
                return new Pair<>(getPointerValue(ptrStep), getPointerValue(ptrSize));
            }
        } finally {
            freeIntPointer(ptrStep);
            freeIntPointer(ptrSize);
        }
    }



}
