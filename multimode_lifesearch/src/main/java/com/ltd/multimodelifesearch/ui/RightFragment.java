package com.ltd.multimodelifesearch.ui;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ltd.multimode_lifesearch.R;
import com.ltdpro.DebugUtil;

public class RightFragment extends Fragment {
	private String TAG = "RightFragment";
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		//
		View view = inflater.inflate(R.layout.fragment_right, container, false);
		return view;
	}
	
	public boolean onKeyUp(int keyCode, KeyEvent event, Context inputContext)
	{
		DebugUtil.i(TAG,"onKeyup");
		switch(keyCode)
		{
		case KeyEvent.KEYCODE_DEL:
			
			DebugUtil.i(TAG,"KeyCode_del!");
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			DebugUtil.i("Left","keyCode_left!");
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			DebugUtil.i(TAG,"Keycode_right!");
			break;
		default:
			break;
		}
		return true;
	}
}
