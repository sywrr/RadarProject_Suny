package com.ltd.lifesearchapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class MyTimer
{
	private Context mContext = null;
	private Handler mTimerHandler = null;
	HashMap<String, Timer> mTimerMap = new HashMap<String, Timer>();
	
	public MyTimer(Context context, Handler handler)
	{
		// TODO Auto-generated constructor stub
		mContext = context;
		mTimerHandler = handler;
		mTimerMap.clear();
	}

	public int setTimer(final int id, int elapse)
	{
		TimerTask task = new TimerTask()
		{
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				Message msg = new Message();
				msg.what = id;
				mTimerHandler.sendMessage(msg);
			}
		};
		
		Timer timer = new Timer();
		mTimerMap.put(String.valueOf(id), timer);
		timer.schedule(task, elapse, elapse);
		
		return id;
	}
	
	public void killTimer(int id)
	{
		String key = String.valueOf(id);
		mTimerMap.get(key).cancel();
		mTimerMap.remove(key);
	}
	
	public boolean isTimerOn(int id)
	{
		String key = String.valueOf(id);
		return mTimerMap.get(key) == null ? false : true;
	}
	
	public void killAllTimers()
	{
		Set timerSet = mTimerMap.entrySet(); 
		Iterator iterator  = timerSet.iterator();
		
		while (iterator.hasNext())
		{
			Map.Entry entry = (Map.Entry)iterator.next();
			((Timer)entry.getValue()).cancel();
//			mTimerMap.remove(entry.getKey());
		}
		mTimerMap.clear();
	}
}
