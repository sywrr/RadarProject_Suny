package com.ltd.lifesearchapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import Utils.AbstractLogger;
import Utils.Logcat;

public class DetectResultWriter extends AbstractLimitedWriter {

    public DetectResultWriter(String path, long totalLimit, long singleLimit) throws IOException {
        super(path, totalLimit, singleLimit);
    }

    @Override
    protected boolean filterFileName(String fileName) {
        return fileName.startsWith("DetectResult");
    }

    @Override
    protected String newFileName(String dir, int index) {
        return dir + "/DetectResult_" + index + ".txt";
    }
}
