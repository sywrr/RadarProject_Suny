package com.ltd.lifesearchapp;

import android.os.Build;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

// �ļ�д����������ָ��·�������Զ�����ÿ���ļ���С��д���ļ����ܴ�С
public abstract class AbstractLimitedWriter {

    // ���ڱ�����ļ���·��
    private final String mSavingDir;

    private FileOutputStream mFileSaveOs;

    // ָ��·�������з���ɸѡ��ʽ���ļ����ܴ�С
    private long mTotalSize;

    // ָ��·�������з���ɸѡ��ʽ���ļ����������
    private final long mTotalLimit;

    // �����ļ������ߴ�
    private final long mSingleLimit;

    // ���ڱ�����ļ�·��
    private String mSavingPath;

    // ���д������ݳ���һ���ļ��Ĵ�Сʱ�ļ���������
    private int mSubFileIndex;

    // ��ǰ����д����ļ���С
    private long mCurrentFileLength;

    // ��ǰ����д����ļ�������
    private int mCurrentFileIndex;

    // ����д��С������Ż��ռ�
    private final byte[] mTmpBytes = new byte[8];

    public AbstractLimitedWriter(String path, long totalLimit, long singleLimit)
            throws IOException {
        mCurrentFileIndex = 0;
        File file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs())
                throw new IOException("can not mkdir: " + path);
            mTotalSize = 0;
        } else {
            if (file.isFile())
                throw new IllegalArgumentException("path must be a directory");
            mTotalSize = scanFile(file);
        }
        if (mTotalSize >= totalLimit)
            throw new IOException("out of memory, can not do any write");
        mSavingDir = path;
        mFileSaveOs = null;
        mTotalLimit = totalLimit;
        mSingleLimit = singleLimit;
        mSubFileIndex = -1;
    }

    protected abstract boolean filterFileName(String fileName);

    private long scanFile(File file) {
        if (file.isFile()) {
            String name = file.getName();
            int index;
            if (filterFileName(name) && (index = fileIndex(name)) != -1) {
                mCurrentFileIndex = Math.max(mCurrentFileIndex, index);
                return file.length();
            }
            return 0;
        }
        File[] subFiles = file.listFiles();
        if (subFiles == null || subFiles.length == 0)
            return 0;
        long size = 0;
        for (File subFile : subFiles) {
            size += scanFile(subFile);
        }
        return size;
    }

    private int fileIndex(String fileName) {
        int start = fileName.lastIndexOf('_');
        if (start == -1)
            return -1;
        int end = fileName.lastIndexOf('.');
        if (end == -1)
            return -1;
        String str = fileName.substring(start + 1, end);
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) < '0' || str.charAt(i) > '9')
                return -1;
        }
        return Integer.parseInt(str);
    }

    protected abstract String newFileName(String dir, int index);

    private String newSubFileName() {
        String subFileName;
        int prefixIndex = mSavingPath.lastIndexOf('.');
        if (prefixIndex == -1)
            throw new IllegalArgumentException("unknown file format");
        String prefix = mSavingPath.substring(prefixIndex);
        if (mSubFileIndex == -1) {
            mSubFileIndex = 2;
            subFileName = mSavingPath.substring(0, prefixIndex) + "_(" + mSubFileIndex + ")" +
                          prefix;
        } else {
            ++mSubFileIndex;
            subFileName = mSavingPath.substring(0, mSavingPath.lastIndexOf('(')) + "(" +
                          mSubFileIndex + ")" + prefix;
        }
        return subFileName;
    }

    private void initWrite(String path) throws FileNotFoundException {
        File file = new File(path);
        if (file.exists())
            throw new IllegalArgumentException(
                    "file is already exists, this may cause overwrite!!");
        mFileSaveOs = new FileOutputStream(file);
        mSavingPath = path;
    }

    private boolean writeToFile(byte[] buf, int offset, int length) throws IOException {
        if (mCurrentFileLength + length < mSingleLimit) {
            mFileSaveOs.write(buf, offset, length);
            mTotalSize += length;
            mCurrentFileLength += length;
            return true;
        }
        return false;
    }

    private void checkWriteRange(byte[] buf, int offset, int length) {
        if (buf == null)
            throw new NullPointerException("write buffer is null");
        if (offset < 0 || length < 0 || offset + length > buf.length)
            throw new IllegalArgumentException("invalid range: " + offset + ", " + length);
    }

    private void checkWriteLength(int length) throws IOException {
        if (length > mSingleLimit) {
            throw new IOException("write length is over single file limit");
        }
        if (mTotalSize + length > mTotalLimit) {
            IOException ioException = new IOException("out of memory, can not write");
            try {
                finishWrite();
            } catch (IOException e) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    ioException.addSuppressed(e);
                }
            }
            throw ioException;
        }
    }

    public final void write(byte[] buf, int offset, int length) throws IOException {
        checkWriteRange(buf, offset, length);
        if (length == 0)
            return;
        checkWriteLength(length);
        if (mFileSaveOs == null) {
            initWrite(newFileName(mSavingDir, ++mCurrentFileIndex));
            mSubFileIndex = -1;
        }
        if (!writeToFile(buf, offset, length)) {
            finishWrite();
            initWrite(newSubFileName());
            writeToFile(buf, offset, length);
        }
    }

    public final void write(byte b) throws IOException {
        mTmpBytes[0] = b;
        write(mTmpBytes, 0, 1);
    }

    public final void write(short val) throws IOException {
        mTmpBytes[0] = (byte) (val);
        mTmpBytes[1] = (byte) ((val >> 8));
        write(mTmpBytes, 0, 2);
    }

    public final void write(char c) throws IOException {
        write((short) c);
    }

    public final void write(int val) throws IOException {
        for (int i = 0; i < 4; i++) {
            mTmpBytes[i] = (byte) ((val >> (8 * i)));
        }
        write(mTmpBytes, 0, 4);
    }

    public final void write(float val) throws IOException {
        write(Float.floatToIntBits(val));
    }

    public final void write(long val) throws IOException {
        for (int i = 0; i < 8; i++) {
            mTmpBytes[i] = (byte) ((val >> (8 * i)));
        }
        write(mTmpBytes, 0, 8);
    }

    public final void write(double val) throws IOException {
        write(Double.doubleToRawLongBits(val));
    }

    public final void write(char[] charArray) throws IOException {
        for (char ch : charArray)
            write(ch);
    }

    public final void write(String str) throws IOException {
        byte[] buffer = str.getBytes();
        write(buffer, 0, buffer.length);
    }

    public final void finishWrite() throws IOException {
        if (mFileSaveOs != null) {
            mSavingPath = null;
            try {
                mFileSaveOs.close();
            } finally {
                mFileSaveOs = null;
                mCurrentFileLength = 0;
            }
        }
    }

    public final String getSavingPath() { return mSavingPath; }

    public final boolean isWriting() { return mFileSaveOs != null; }

}
