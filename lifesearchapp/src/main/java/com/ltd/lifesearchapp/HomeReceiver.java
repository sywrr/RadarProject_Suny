package com.ltd.lifesearchapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class HomeReceiver extends BroadcastReceiver {

    static final String SYSTEM_DIALOG_REASON_KEY = "reason";

    static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";

    static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
            String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
            if (reason != null) {
                if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                    ((MainActivity) context).requireExpertFragment().mPlayManager.postStopPlay();
                    Toast.makeText(context, "Home键被监听", Toast.LENGTH_SHORT).show();
                } else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                    ((MainActivity) context).requireExpertFragment().mPlayManager.postStopPlay();
                    Toast.makeText(context, "多任务键被监听", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
