package Utils;

public abstract class AbstractLogger {
    protected String mTag;
    protected boolean mDebug;

    public AbstractLogger(String tag, boolean flag) {
        mTag = tag;
        mDebug = flag;
    }

    protected StringBuilder getStr(String level, String msg) {
        StringBuilder s = new StringBuilder();
        s.append("[");
        s.append(level);
        s.append("]: ");
        s.append(msg);
        return s;
    }

    protected void logMsg(String level, String msg) {}

    public String getTag() {
        return mTag;
    }

    public void verbose(String msg) {
        logMsg("verbose", msg);
    }

    public void info(String msg) {
        logMsg("info", msg);
    }

    public void debug(String msg) {
        logMsg("debug", msg);
    }

    public void warning(String msg) {
        logMsg("warning", msg);
    }

    public void error(String msg) {
        logMsg("error", msg);
    }

    public void error(Throwable e) {
        error(e.getMessage());
    }

    public void errorStackTrace(Throwable e) {
        e.printStackTrace();
    }

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
