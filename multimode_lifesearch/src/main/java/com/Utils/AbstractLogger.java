package com.Utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class AbstractLogger {

    private final String mTag;

    private final boolean mDebug;

    public AbstractLogger(String tag, boolean flag) {
        mTag = tag;
        mDebug = flag;
    }

    protected abstract String getMsg(String level, String msg);

    protected abstract void logMsg(String level, String msg);

    public final String getTag() {
        return mTag;
    }

    public final void verbose(String msg) {
        if (mDebug)
            logMsg("verbose", msg);
    }

    public final void info(String msg) {
        if (mDebug)
            logMsg("info", msg);
    }

    public final void debug(String msg) {
        if (mDebug)
            logMsg("debug", msg);
    }

    public final void warning(String msg) {
        if (mDebug)
            logMsg("warning", msg);
    }

    public final void error(String msg) {
        if (mDebug)
            logMsg("error", msg);
    }

    public final void error(Throwable e) {
        if (mDebug)
            logMsg("error", e.getClass().getName() + ": " + e.getMessage());
    }

    public final void errorStackTrace(Throwable e) {
        if (mDebug) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            logMsg("error", sw.toString());
        }
    }

    public final boolean isDebug() { return mDebug; }

    public boolean open(Object... args) { return false; }

    public void close() {}

    public static void Except(Throwable e, AbstractLogger logger) {
        if (logger != null)
            logger.error(e);
    }

    public static void ExceptStackTrace(Throwable e, AbstractLogger logger) {
        if (logger != null)
            logger.errorStackTrace(e);
    }

    public static void Verbose(String msg, AbstractLogger logger) {
        if (logger != null)
            logger.verbose(msg);
    }

    public static void Info(String msg, AbstractLogger logger) {
        if (logger != null)
            logger.info(msg);
    }

    public static void Debug(String msg, AbstractLogger logger) {
        if (logger != null)
            logger.debug(msg);
    }

    public static void Warning(String msg, AbstractLogger logger) {
        if (logger != null)
            logger.warning(msg);
    }

    public static void Error(String msg, AbstractLogger logger) {
        if (logger != null)
            logger.error(msg);
    }
}
