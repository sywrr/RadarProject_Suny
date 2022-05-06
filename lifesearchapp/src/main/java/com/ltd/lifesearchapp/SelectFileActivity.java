package com.ltd.lifesearchapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;


import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import Utils.AbstractLogger;
import Utils.Logcat;

public class SelectFileActivity extends AppCompatActivity
        implements Thread.UncaughtExceptionHandler {

    private AbstractLogger mLogger = new Logcat("SelectFileActivity", true);

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        mLogger.errorStackTrace(e);
    }

    private void setLandScape() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    private final List<FileNode> mFileList = new LinkedList<>();

    private FileNode checkFile(File file) {
        String fileName = file.getName();
        int start = fileName.indexOf(mCategory + "_");
        if (start == -1)
            return null;
        start += mCategory.length() + 1;
        int end = start;
        char ch = 0;
        while (end < fileName.length()) {
            ch = fileName.charAt(end);
            if (ch < '0' || ch > '9')
                break;
            ++end;
        }
        if ((ch != '_' && ch != '.') || start == end)
            return null;
        int index = Integer.parseInt(fileName.substring(start, end));
        int subIndex = 1;
        if (ch == '_' && end < fileName.length() - 1 && (ch = fileName.charAt(end + 1)) == '(') {
            start = end + 2;
            end = start;
            while (end < fileName.length()) {
                ch = fileName.charAt(end);
                if (ch < '0' || ch > '9')
                    break;
                ++end;
            }
            if (start < end && ch == ')') {
                subIndex = Integer.parseInt(fileName.substring(start, end));
            }
        }
        return new FileNode(index, subIndex, file);
    }

    private void scanFiles(File file) {
        if (file.isFile())
            throw new IllegalArgumentException("dir must be a directory");
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Toast.makeText(this, "创建文件夹: " + file.getName() + "失败", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        File[] subFiles = file.listFiles();
        if (subFiles == null || subFiles.length == 0)
            return;
        FileNode fileNode;
        for (File subFile : subFiles) {
            if (subFile.isFile()) {
                fileNode = checkFile(subFile);
                if (fileNode != null)
                    mFileList.add(fileNode);
            } else {
                scanFiles(subFile);
            }
        }
        Collections.sort(mFileList, new Comparator<FileNode>() {
            @Override
            public int compare(FileNode o1, FileNode o2) {
                return o2.mIndex != o1.mIndex ? o2.mIndex - o1.mIndex : o1.mSubIndex - o2.mSubIndex;
            }
        });
    }

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(this);
        setLandScape();
        setContentView(R.layout.select_file);
        Intent intent = getIntent();
        mFilePath = intent.getStringExtra("path");
        mCategory = intent.getStringExtra("category");
        int requestCode = intent.getIntExtra("requestCode", -1);
        if (requestCode == -1)
            throw new IllegalArgumentException("no requestCode message");
        scanFiles(new File(getLteFilesPath()));
        mFileAdapter = new FileAdapter(this, mFileList, requestCode);
        ListView listView = findViewById(R.id.list_view);
        View headView = LayoutInflater.from(this).inflate(R.layout.file_list_head, null, false);
        listView.addHeaderView(headView);
        listView.setAdapter(mFileAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFileAdapter.clearAllMessage();
        mLogger.debug("clear all callbacks and message");
    }

    private FileAdapter mFileAdapter;

    private String mFilePath;

    private String mCategory;

    public final String getLteFilesPath() { return mFilePath; }
}
