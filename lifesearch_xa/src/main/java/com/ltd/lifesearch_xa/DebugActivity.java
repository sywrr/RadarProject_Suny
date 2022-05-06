package com.ltd.lifesearch_xa;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class DebugActivity extends AppCompatActivity implements OnClickListener{
	private LifeSearchApplication mApp;
	private Button mComButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debug);
		
		mApp = (LifeSearchApplication)getApplicationContext();
		
		mComButton = (Button)findViewById(R.id.button1);
		mComButton.setOnClickListener(this);
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		
		super.onDestroy();
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.equals(mComButton)) {
			byte[] comBuf = new byte[10];
			comBuf[0] = 0x11;
			comBuf[1] = 0x00;
			comBuf[2] = 20;
			comBuf[3] = 0;
//			mApp.mWifiDevice.sendCommand(comBuf, 4);
		}
	}
}
