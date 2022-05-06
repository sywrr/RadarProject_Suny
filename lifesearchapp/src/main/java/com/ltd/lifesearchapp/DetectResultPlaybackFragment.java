package com.ltd.lifesearchapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import Utils.AbstractLogger;
import Utils.Logcat;

public class DetectResultPlaybackFragment extends Fragment implements Releasable {

    private Context mContext;

    private AbstractLogger mLogger = new Logcat("DetectResultPlaybackFragment", true);

    private View mView;

    private ListView mDetectResultView;

    private DetectResultAdapter mDetectResultAdapter;

    private final List<DetectResultInfo> mInfoList = new LinkedList<>();

    private final DetectResultReader mDetectResultReader = new DetectResultReader();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mDetectResultAdapter = new DetectResultAdapter(mContext);
    }

    private volatile boolean mIsHidden = true;

    public final boolean hidden() { return mIsHidden; }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        mIsHidden = hidden;
        mLogger.debug("hidden: " + hidden);
        if (!hidden) {
            mLogger.debug("refresh detect result view");
        } else {
            mLogger.debug("hidden detect result view");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.detect_result_playback, container, false);
        mView.findViewById(R.id.select_detect_result).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mLogger.debug("click select detect result file");
                        onSelectResult();
                    }
                });
        mDetectResultView = mView.findViewById(R.id.detect_result_view);
        return mView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public synchronized final void showDetectResult(String path) {
        mInfoList.clear();
        mDetectResultReader.setPath(path);
        DetectResultInfo info;
        while (mDetectResultReader.read()) {
            info = new DetectResultInfo((short) mDetectResultReader.getType(),
                                        mDetectResultReader.getScans(),
                                        (short) mDetectResultReader.getTargetPos(),
                                        mDetectResultReader.getDetectStart(),
                                        mDetectResultReader.getDetectEnd());
            mInfoList.add(info);
        }
        mLogger.debug("info list size: " + mInfoList.size());
        mDetectResultAdapter.setInfoList(mInfoList);
        mDetectResultView.setAdapter(mDetectResultAdapter);
    }

    @Override
    public void release() { }

    private void onSelectResult() {
        ((MainActivity) mContext).setRequestCode(202);
        Intent intent = new Intent(mContext, SelectFileActivity.class);
        intent.putExtra("path", DetectFragment.getDetectResultPath());
        intent.putExtra("category", "DetectResult");
        intent.putExtra("requestCode", 202);
        startActivityForResult(intent, 202);
    }
}
