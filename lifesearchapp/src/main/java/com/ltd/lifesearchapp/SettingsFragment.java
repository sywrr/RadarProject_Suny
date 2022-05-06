package com.ltd.lifesearchapp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
//import androidx.fragment.app.Fragment;

import java.io.IOException;

import Utils.AbstractLogger;
import Utils.Logcat;

public class SettingsFragment extends Fragment implements Releasable {
    private Context mContext;

    private View mView;

    private TextView mDetectModeView;

    private PopupWindow mDetectModeWindow;

    private TextView mDetectRangeView;

    private PopupWindow mDetectLargeRangeWindow;

    private PopupWindow mDetectSmallRangeWindow;

    private TextView mDetectIntervalView;

    private PopupWindow mDetectIntervalWindow;

    private EditText mDistanceCheckText;

    private AbstractLogger mLogger = new Logcat("SettingsFragment", true);

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.settings_view, container, false);
        initView();
        return mView;
    }

    private boolean isDetecting() {
        return ((MainActivity) mContext).requireDetectFragment().isDetecting();
    }

    public final int getDetectInterval() {
        String txt = ((TextView) mView.findViewById(R.id.detect_interval)).getText().toString();
        return parseDetectInterval(txt);
    }

    private volatile boolean mIsHidden = true;

    // 当页面切换时，销毁所有的弹窗
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        synchronized (SettingsFragment.this) {
            mIsHidden = hidden;
            if (hidden) {
                mDetectModeWindow.dismiss();
                mDetectLargeRangeWindow.dismiss();
                mDetectSmallRangeWindow.dismiss();
            }
        }
        mDistanceCheckText.setEnabled(!isDetecting());
    }

    private abstract static class PopupWindowItemClickHandler {

        public void onClickHandle(View item, TextView itemValueView,
                                  final PopupWindow popupWindow) {
            item.setBackgroundColor(Color.GRAY);
            if (item instanceof TextView)
                itemValueView.setText(((TextView) item).getText());
            item.post(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignore) {}
                item.setBackgroundColor(Color.WHITE);
                popupWindow.dismiss();
            });
        }

    }


    private final PopupWindowItemClickHandler mSignalPosItemClickHandler
            = new PopupWindowItemClickHandler() {
        @Override
        public void onClickHandle(View item, TextView itemValueView, PopupWindow popupWindow) {
            super.onClickHandle(item, itemValueView, popupWindow);
        }
    };

    private final PopupWindowItemClickHandler mDetectModeItemClickHandler
            = new PopupWindowItemClickHandler() {
        @Override
        public void onClickHandle(View item, TextView itemValueView, PopupWindow popupWindow) {
            super.onClickHandle(item, itemValueView, popupWindow);
        }
    };

    private final PopupWindowItemClickHandler mDetectIntervalItemClickHandler
            = new PopupWindowItemClickHandler() {
        @Override
        public void onClickHandle(View item, TextView itemValueView, PopupWindow popupWindow) {
            super.onClickHandle(item, itemValueView, popupWindow);
            switch (item.getId()) {
                case R.id.detect_interval_1:
                    mLogger.debug("选择探测间隔为3米");
                    mDetectRangeView.setText("0-3米");
                    break;
                case R.id.detect_interval_2:
                    mLogger.debug("选择探测间隔12米");
                    mDetectRangeView.setText("0-12米");
                    break;
            }
        }
    };

    /**
     * @param res              弹窗视图的layout
     * @param itemValueView    显示弹窗选中值的text view
     * @param functional       获取弹窗所有项的视图组
     * @param itemClickHandler 弹窗项被点击时调用
     * @return 生成的弹窗
     */
    private PopupWindow createPopupWindow(int res, final TextView itemValueView,
                                          Functional<View, ViewGroup> functional,
                                          final PopupWindowItemClickHandler itemClickHandler) {
        final View contentView = LayoutInflater.from(mContext).inflate(res, null, false);
        final PopupWindow popupWindow = new PopupWindow(contentView, LayoutParams.WRAP_CONTENT,
                                                        LayoutParams.WRAP_CONTENT, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setAnimationStyle(R.anim.popup_window_anim);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(20);
        }
        final ViewGroup viewGroup = functional.apply(contentView);
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            final View item = viewGroup.getChildAt(i);
            item.setOnClickListener(v -> {
                if (itemClickHandler != null)
                    itemClickHandler.onClickHandle(item, itemValueView, popupWindow);
            });
        }
        return popupWindow;
    }

    private void setItemValueViewClickListener(View itemValueView, PopupWindow[] popupWindows,
                                               ObjectArraySelector<PopupWindow> popupWindowSelector) {
        itemValueView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isDetecting()) {
                    synchronized (SettingsFragment.this) {
                        if (!mIsHidden) {
                            popupWindowSelector.select(popupWindows).showAsDropDown(itemValueView);
                        }
                    }
                }
            }
        });
    }

    private final int[] mDetectRange = new int[2];

    private int parseDetectInterval(String s) {
        int index = s.lastIndexOf('米');
        if (index == -1)
            throw new IllegalArgumentException("illegal string of detect interval");
        return Integer.parseInt(s.substring(0, index));
    }

    private ParamSaver paramSaver;

    private void initView() {
        mDetectModeView = mView.findViewById(R.id.detect_mode);
        mDetectModeWindow = createPopupWindow(R.layout.detect_mode_window, mDetectModeView,
                                              arg -> (ViewGroup) arg, mDetectModeItemClickHandler);
        setItemValueViewClickListener(mDetectModeView, new PopupWindow[]{mDetectModeWindow},
                                      popupWindows -> popupWindows[0]);

        mDetectRangeView = mView.findViewById(R.id.detect_range);
        Functional<View, ViewGroup> detectRangeFunction = arg -> {
            if (arg.getId() == R.id.detect_large_range_window)
                return (ViewGroup) arg;
            if (arg.getId() == R.id.detect_small_range_window)
                return (ViewGroup) (((ViewGroup) arg).getChildAt(0));
            return null;
        };
        mDetectLargeRangeWindow = createPopupWindow(R.layout.detect_large_range_window,
                                                    mDetectRangeView, detectRangeFunction,
                                                    mSignalPosItemClickHandler);
        mDetectSmallRangeWindow = createPopupWindow(R.layout.detect_small_range_window,
                                                    mDetectRangeView, detectRangeFunction,
                                                    mSignalPosItemClickHandler);
        setItemValueViewClickListener(mDetectRangeView, new PopupWindow[]{mDetectLargeRangeWindow,
                                                                          mDetectSmallRangeWindow},
                                      popupWindows -> {
                                          String detectIntervalStr = mDetectIntervalView.getText()
                                                                                        .toString();
                                          if (detectIntervalStr.equals("3米"))
                                              return mDetectSmallRangeWindow;
                                          if (detectIntervalStr.equals("12米"))
                                              return mDetectLargeRangeWindow;
                                          return null;
                                      });

        mDetectIntervalView = mView.findViewById(R.id.detect_interval);
        mDetectIntervalWindow = createPopupWindow(R.layout.detect_interval_window,
                                                  mDetectIntervalView, arg -> (ViewGroup) arg,
                                                  mDetectIntervalItemClickHandler);
        setItemValueViewClickListener(mDetectIntervalView, new PopupWindow[]{mDetectIntervalWindow},
                                      popupWindows -> popupWindows[0]);

        final EditText editText = mView.findViewById(R.id.distance_check);
        mDistanceCheckText = editText;
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mLogger.debug("获得焦点");
                } else {
                    mLogger.debug("失去焦点");
                    saveDistanceCheck();
                }
            }
        });
        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mLogger.debug("触摸设置界面");
                clearEditTextFocus(mView);
                return true;
            }
        });

        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLogger.debug("点击设置界面");
                clearEditTextFocus(mView);
            }
        });

        View leftView = mView.findViewById(R.id.settings_key);
        leftView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLogger.debug("点击left");
                clearEditTextFocus(leftView);
            }
        });

        View rightView = mView.findViewById(R.id.settings_value);
        rightView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLogger.debug("点击right");
                clearEditTextFocus(rightView);
            }
        });

        try {
            paramSaver = new ParamSaver(
                    Environment.getExternalStorageDirectory() + "/params/params.par");
            Object distanceCheckValue = paramSaver.getParam("distance_check");
            if (distanceCheckValue != null) {
                editText.setText(distanceCheckValue.toString());
            } else {
                paramSaver.putParam("distance_check", editText.getText().toString());
                paramSaver.save();
            }
        } catch (IOException e) {
            mLogger.errorStackTrace(e);
        }
    }

    private void saveDistanceCheck() {
        if (paramSaver != null) {
            CharSequence sequence = mDistanceCheckText.getText();
            if (!sequence.toString().equals("")) {
                paramSaver.putParam("distance_check", sequence.toString());
            } else {
                Object distanceCheckValue = paramSaver.getParam("distance_check");
                String txt = distanceCheckValue == null ? "60" : distanceCheckValue.toString();
                mDistanceCheckText.setText(txt);
                paramSaver.putParam("distance_check", txt);
            }
            paramSaver.save();
        }
    }

    private void clearEditTextFocus(View focusView) {
        mDistanceCheckText.clearFocus();
        focusView.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                getActivity().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mView.getWindowToken(), 0);
    }

    public final int[] getDetectRange() {
        String txt = mDetectRangeView.getText().toString();
        int splitIndex = txt.indexOf('-');
        if (splitIndex == -1)
            throw new IllegalArgumentException("illegal signal pos txt");
        int start = splitIndex, end = splitIndex;
        char ch;
        while ((--start) >= 0) {
            ch = txt.charAt(start);
            if (ch < '0' || ch > '9')
                break;
        }
        while ((++end) < txt.length()) {
            ch = txt.charAt(end);
            if (ch < '0' || ch > '9')
                break;
        }
        if (++start == splitIndex)
            throw new IllegalArgumentException("no signal start value");
        String signalStart = txt.substring(start, splitIndex);
        if (splitIndex + 1 == end)
            throw new IllegalArgumentException("no signal end value");
        String signalEnd = txt.substring(splitIndex + 1, end);
        mDetectRange[0] = Integer.parseInt(signalStart);
        mDetectRange[1] = Integer.parseInt(signalEnd);
        return mDetectRange;
    }

    public final boolean isSingleMode() {
        return mDetectModeView.getText().toString().equals("单目标模式");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void release() { }

    public final int getDistanceCheck() {
        return Integer.parseInt(mDistanceCheckText.getText().toString());
    }
}
