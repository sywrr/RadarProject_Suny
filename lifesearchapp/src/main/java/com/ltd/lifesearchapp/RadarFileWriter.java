package com.ltd.lifesearchapp;

import java.io.IOException;

public class RadarFileWriter extends AbstractLimitedWriter {

    public RadarFileWriter(String path, long totalLimit, long singleLimit) throws IOException {
        super(path, totalLimit, singleLimit);
    }

    @Override
    protected boolean filterFileName(String fileName) {
        return fileName.startsWith("RadarData");
    }

    @Override
    protected String newFileName(String dir, int index) {
        return dir + "/RadarData_" + index + ".lte";
    }
}
