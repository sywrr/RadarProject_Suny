package com.Utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class FileLogger extends AbstractLogger {

    protected String mPath;

    protected PrintStream mOutputStream;

    protected String getMsg(String level, String msg) {
        return "[" + level + "/" + getTag() + "]: " + msg;
    }

    public FileLogger(String tag, boolean flag) {
        super(tag, flag);
        mPath = null;
        mOutputStream = null;
    }

    private void closeStream() {
        if (mOutputStream != null) {
            mOutputStream.close();
            mOutputStream = null;
        }
        mPath = null;
    }

    public synchronized boolean open(Object... args) {
        if (args.length != 1)
            throw new IllegalArgumentException("can only pass 1 argument");
        if (!(args[0] instanceof String))
            throw new IllegalArgumentException("path must be String type");
        if (mPath != null) {
            closeStream();
        }
        String path = (String) args[0];
        mPath = null;
        try {
            mOutputStream = new PrintStream(new FileOutputStream(path));
            mPath = path;
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public synchronized void close() {
        closeStream();
    }

    protected void logMsg(String level, String msg) {
        mOutputStream.println(getMsg(level, msg));
    }
}
