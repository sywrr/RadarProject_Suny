package com.ltd.lifesearchapp;

import java.io.File;

public class FileNode {
    int mIndex;

    int mSubIndex;

    File mFile;

    public FileNode(int index, File file) {
        mIndex = index;
        mSubIndex = 1;
        mFile = file;
    }

    public FileNode(int index, int subIndex, File file) {
        mIndex = index;
        mSubIndex = subIndex;
        mFile = file;
    }
}
