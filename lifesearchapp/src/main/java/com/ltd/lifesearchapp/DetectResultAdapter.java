package com.ltd.lifesearchapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import Utils.AbstractLogger;
import Utils.Logcat;

public class DetectResultAdapter extends BaseAdapter {

    private final AbstractLogger mLogger = new Logcat("DetectResultAdapter", true);

    private List<DetectResultInfo> mInfoList;

    private final LayoutInflater mInflater;

    private final Context mContext;

    DetectResultAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    public final void setInfoList(List<DetectResultInfo> infoList) { mInfoList = infoList; }

    @Override
    public int getCount() {
        return mInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return mInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private int targetRealDistance(DetectResultInfo info) {
        SettingsFragment settingsFragment = ((MainActivity) mContext).requireSettingsFragment();
        int interval = settingsFragment.getDetectInterval();
        int detectStart = info.getDetectStart();
        int nSample = info.getTargetPos();
        int distanceCheck = ((MainActivity) mContext).requireSettingsFragment().getDistanceCheck();
        int originalDistance = detectStart * 100 +
                               nSample * interval * 100 / (8192 * interval / 12);
        return Math.min(originalDistance + distanceCheck, (detectStart + interval) * 100);
    }

    private void setDetectResultInfo(DetectResultInfo info, TextView txtView) {
        String s = "";
        boolean isMove = true;
        short type = info.getType();
        s += "道号: " + info.getScans() + "; 位置: " + targetRealDistance(info) + "cm; ";
        s += "类型: ";
        if ((type & _DetectResult.RESULT_MOVE) != 0) {
            s += "体动";
        } else {
            s += "呼吸";
            isMove = false;
        }
        txtView.setText(s);
        if (!isMove)
            txtView.setTextColor(Color.RED);
        else
            txtView.setTextColor(Color.BLACK);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.detect_result_list_item, parent, false);
        }
        DetectResultInfo info = mInfoList.get(position);
        setDetectResultInfo(info, (TextView) convertView.findViewById(R.id.detect_result_info));
        return convertView;
    }
}
