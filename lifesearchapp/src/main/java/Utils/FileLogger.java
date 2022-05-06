package Utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class FileLogger extends AbstractLogger {

    protected String mPath;
    protected PrintStream mOutputStream;
    protected final Object mLock = new Object();

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

    public boolean open(String path) {
        synchronized (mLock) {
            if (mPath != null) {
                closeStream();
            }
            mPath = path;
            try {
                mOutputStream = new PrintStream(new FileOutputStream(mPath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                mOutputStream = null;
                mPath = null;
                return false;
            }
            return true;
        }
    }

    public void close() {
        synchronized (mLock) {
            closeStream();
        }
    }

    protected void logMsg(String level, String msg) {
        if (mDebug) {
            synchronized (mLock) {
                mOutputStream.println(getStr(level, msg));
                mOutputStream.flush();
            }
        }
    }

    public void error(Throwable e) {
        logMsg("error", e.getMessage());
    }

    public void errorStackTrace(Throwable e) {
        if (mDebug) {
            synchronized (mLock) {
                e.printStackTrace(mOutputStream);
            }
        }
    }
}
