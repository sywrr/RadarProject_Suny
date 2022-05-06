package Utils;

public class ConsoleLogger extends AbstractLogger {

    public ConsoleLogger(String tag, boolean flag) {
        super(tag, flag);
    }

    @Override
    protected void logMsg(String level, String msg) {
        if (mDebug)
            System.out.println(getStr(level, msg));
    }

    @Override
    public void error(Throwable e) {
        logMsg("error", e.getMessage());
    }

    @Override
    public void errorStackTrace(Throwable e) {
        if (mDebug)
            e.printStackTrace();
    }
}
