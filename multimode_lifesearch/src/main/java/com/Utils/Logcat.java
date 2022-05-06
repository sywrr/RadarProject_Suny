package com.Utils;

import android.util.Log;

public class Logcat extends AbstractLogger {
    public Logcat(String tag, boolean flag) {
        super(tag, flag);
    }

    @Override
    protected String getMsg(String level, String msg) {
        return msg;
    }

    @Override
    protected void logMsg(String level, String msg) {
        switch (level.charAt(0)) {
            case 'v':
                Log.v(getTag(), getMsg(level, msg));
                break;
            case 'i':
                Log.i(getTag(), getMsg(level, msg));
                break;
            case 'd':
                Log.d(getTag(), getMsg(level, msg));
                break;
            case 'w':
                Log.w(getTag(), getMsg(level, msg));
                break;
            case 'e':
                Log.e(getTag(), getMsg(level, msg));
                break;
        }
    }
}
