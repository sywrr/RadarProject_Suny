package Utils;

import android.util.Log;

public class Logcat extends AbstractLogger {
    public Logcat(String tag, boolean flag) {
        super(tag, flag);
    }

    public void verbose(String msg) {
        if (mDebug)
            Log.v(mTag, msg);
    }

    public void info(String msg) {
        if (mDebug)
            Log.i(mTag, msg);
    }

    public void debug(String msg) {
        if (mDebug)
            Log.d(mTag, msg);
    }

    public void warning(String msg) {
        if (mDebug)
            Log.w(mTag, msg);
    }

    public void error(String msg) {
        if (mDebug)
            Log.e(mTag, msg);
    }

    public void error(Throwable e) {
        if (mDebug)
            Log.e(mTag, "got error: " + e.getMessage());
    }

    @Override
    public void errorStackTrace(Throwable e) {
        if (mDebug)
            Log.e(mTag, Log.getStackTraceString(e));
    }
}
