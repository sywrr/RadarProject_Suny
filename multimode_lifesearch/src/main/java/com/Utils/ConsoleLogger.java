package com.Utils;

public class ConsoleLogger extends AbstractLogger {

    public ConsoleLogger(String tag, boolean flag) {
        super(tag, flag);
    }

    protected String getMsg(String level, String msg) {
        return "[" + level + "/" + getTag() + "]: " + msg;
    }

    @Override
    protected void logMsg(String level, String msg) {
        System.out.println(getMsg(level, msg));
    }
}
