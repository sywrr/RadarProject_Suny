package com.ltd.lifesearchapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.Inflater;

import Utils.AbstractLogger;
import Utils.Logcat;

public class FileAdapter extends BaseAdapter {

    private final Context mContext;

    private final AbstractLogger mLogger = new Logcat("FileAdapter", true);

    private final int mRequestCode;

    public FileAdapter(Context context, List<FileNode> list, int requestCode) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mFileList = list;
        mRequestCode = requestCode;
    }

    private final List<FileNode> mFileList;

    @Override
    public int getCount() {
        return mFileList.size();
    }

    @Override
    public Object getItem(int position) {
        return mFileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private LayoutInflater mInflater;

    private String doubleToString(double d) {
        return String.format("%.2f", d);
    }

    private String getSizeString(long size) {
        String s = "size: ";
        if (size < 1024L)
            return s + size + "B";
        if (size < 1024L * 1024)
            return s + doubleToString((double) size / 1024) + "K";
        if (size < 1024L * 1024 * 1024)
            return s + doubleToString((double) size / (1024L * 1024)) + "M";
        return s + doubleToString((double) size / (1024L * 1024 * 1024)) + "G";
    }

    public final void clearAllMessage() {
        mItemHandler.removeCallbacksAndMessages(null);
    }

    private final class ItemHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case 0:
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignore) { }
                View view = (View) msg.obj;
                view.setBackgroundColor(Color.WHITE);
                final File file = mFileList.get(msg.arg1).mFile;
                showDialog(msg.arg1, "选择文件", new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        intent.putExtra("result", file.getAbsolutePath());
                        ((SelectFileActivity) mContext).setResult(mRequestCode + 1, intent);
                        ((SelectFileActivity) mContext).finish();
                    }
                }, null);
                break;
            case 1:
                final int position = msg.arg1;
                showDialog(position, "删除文件", new Runnable() {
                    @Override
                    public void run() {
                        FileNode fileNode = mFileList.remove(position);
                        if (!fileNode.mFile.delete()) {
                            Toast.makeText(mContext, "删除文件" + fileNode.mFile.getName() + "失败",
                                           Toast.LENGTH_SHORT).show();
                        } else {
                            notifyDataSetChanged();
                            Toast.makeText(mContext, "删除文件" + fileNode.mFile.getName() + "成功",
                                           Toast.LENGTH_SHORT).show();
                        }
                    }
                }, null);
                break;
            }
        }
    }

    private void showDialog(int position, final String prefix, final Runnable confirmRunnable,
                            final Runnable cancelRunnable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final File selectedFile = mFileList.get(position).mFile;
        builder.setTitle(prefix);
        builder.setMessage("确定" + prefix + selectedFile.getName() + "?");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mLogger.debug(prefix + ": " + selectedFile.getName());
                if (confirmRunnable != null)
                    confirmRunnable.run();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (cancelRunnable != null)
                    cancelRunnable.run();
            }
        });
        builder.show();
    }

    private final ItemHandler mItemHandler = new ItemHandler();

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.file_list_item, parent, false);
        }
        FileNode fileNode = mFileList.get(position);
        ((TextView) convertView.findViewById(R.id.file_name)).setText(fileNode.mFile.getName());
        ((TextView) convertView.findViewById(R.id.file_info)).setText(
                fileNode.mFile.getAbsolutePath());
        ((TextView) convertView.findViewById(R.id.file_size)).setText(
                getSizeString(fileNode.mFile.length()));
        final View itemView = convertView.findViewById(R.id.file_item);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemView.setBackgroundColor(Color.GRAY);
                Message msg = new Message();
                msg.obj = itemView;
                msg.what = 0;
                msg.arg1 = position;
                mItemHandler.sendMessage(msg);
            }
        });
        convertView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLogger.debug("点击删除按钮");
                Message msg = new Message();
                msg.what = 1;
                msg.arg1 = position;
                mItemHandler.sendMessage(msg);
            }
        });
        return convertView;
    }
}
