package com.ltd.lifesearchapp;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import Utils.AbstractLogger;
import Utils.Logcat;

public class RadarFragment extends Fragment implements Releasable {

    View mView = null;

    private Context mContext;

    private AbstractLogger mLogger = new Logcat("RadarFragment", true);

    private DetectFragment mDetectFragment;
    private ExpertFragment mExpertFragment;
    private SettingsFragment mSettingsFragment;
    private DetectResultPlaybackFragment mDetectResultPlaybackFragment;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.radar_view, container, false);
        initDeclaredFragments();
        initMenu();
        return mView;
    }

    public View getView() { return mView; }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerCallbacks();
    }

    private Fragment initChildFragment(Class<?> fragmentClass, String fragmentTag, boolean hide) {
        Fragment fragment = getChildFragmentManager().findFragmentByTag(fragmentTag);
        if (fragment == null) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            try {
                fragment = (Fragment) fragmentClass.newInstance();
                transaction.add(R.id.radar_fragment, fragment, fragmentTag);
                if (hide) {
                    transaction.hide(fragment);
                } else {
                    transaction.show(fragment);
                }
                transaction.commit();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (java.lang.InstantiationException e) {
                e.printStackTrace();
            }
        } else {
            mLogger.error("fragment with tag " + fragmentTag + " is already added");
        }
        return fragment;
    }

    private void initDeclaredFragments() {
        mExpertFragment = (ExpertFragment) initChildFragment(ExpertFragment.class, "expert_tag",
                                                             true);
        mDetectFragment = (DetectFragment) initChildFragment(DetectFragment.class, "detect_tag",
                                                             false);
        mSettingsFragment = (SettingsFragment) initChildFragment(SettingsFragment.class,
                                                                 "settings_tag", true);
        mDetectResultPlaybackFragment = (DetectResultPlaybackFragment) initChildFragment(
                DetectResultPlaybackFragment.class, "detect_result_playback", true);
    }

    public void registerCallbacks() { }

    RadioGroup mMenu = null;

    private void initMenu() {
        mMenu = mView.findViewById(R.id.radar_menu);
        ((RadioButton) mMenu.findViewById(R.id.detect_mode)).setTextColor(
                getResources().getColor(R.color.orange));
        mMenu.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.hide(mExpertFragment);
                transaction.hide(mDetectFragment);
                transaction.hide(mSettingsFragment);
                transaction.hide(mDetectResultPlaybackFragment);
                ((RadioButton) mMenu.findViewById(R.id.detect_mode)).setTextColor(Color.BLACK);
                ((RadioButton) mMenu.findViewById(R.id.expert_mode)).setTextColor(Color.BLACK);
                ((RadioButton) mMenu.findViewById(R.id.settings)).setTextColor(Color.BLACK);
                ((RadioButton) mMenu.findViewById(R.id.detect_result_playback)).setTextColor(Color.BLACK);
                ((RadioButton) mMenu.findViewById(checkedId)).setTextColor(
                        getResources().getColor(R.color.orange));
                switch (checkedId) {
                    case R.id.detect_mode:
                        mLogger.debug("click detect mode");
                        transaction.show(mDetectFragment);
                        break;
                    case R.id.expert_mode:
                        mLogger.debug("click expert mode");
                        transaction.show(mExpertFragment);
                        break;
                    case R.id.settings:
                        mLogger.debug("click settings");
                        transaction.show(mSettingsFragment);
                        break;
                    case R.id.detect_result_playback:
                        mLogger.debug("click detect result playback");
                        transaction.show(mDetectResultPlaybackFragment);
                        break;
                }
                transaction.commit();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mLogger.debug("destroy view");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void release() {
        for (Fragment fragment : getChildFragmentManager().getFragments()) {
            if (fragment instanceof Releasable)
                ((Releasable) fragment).release();
        }
    }
}
