package com.ltd.multimodelifesearch.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ltd.multimode_lifesearch.R;
import com.ltd.multimodelifesearch.activity.MultiModeLifeSearchActivity;
import com.ltd.multimodelifesearch.adapter.ListGroupAdapter;
import com.ltd.multimodelifesearch.adapter.RadarParamExpandableListAdapter;
import com.ltdpro.AntennaDevice;
import com.ltdpro.DebugUtil;
import com.ltdpro.HRulerView;
import com.ltdpro.LogWriter;
import com.ltdpro.MyApplication;
import com.ltdpro.radarDevice;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class LeftFragment extends Fragment
        implements ExpandableListView.OnChildClickListener, ExpandableListView.OnGroupClickListener {
	private Context mContext;
	private final String TAG = "LFragment";
	private final String THREADTAG = "threadTAG";
	private final String KTAG = "KTAG";
	public static RadarParamExpandableListAdapter mRadarParamAdapter;// hss0714
	public static ExpandableListView mexpListView;
	private static TextView tv_title;

	// ????????
	private LogWriter mLog = null;

	/**
	 * ??????????????
	 */
	public int mTab = 0; // ????????????0,1,2,3????
	/*
	 * 0:????????? 1:???????????? ???????? 2:???????????? ???????? 3:???????????? ???????? 4:???????????? ????????
	 */
	private final int RADARFREQ = 0;
	private final int RADARSET = 1;
	private final int RADARSYSTEM = 2;
	private final int RADARSERV = 3;
	private final int RADARPARAM = 0;

	/**
	 * ??????????????????????????????
	 */
	private String[][] mtabFirstItems = {{"????????", "????????", "????????", "????" }, {"????????", "????????", "????????", "????????", "????????", "????????", "????????", "????????", "????????", "????????", "????????", "????????" }, {"????????", "????????", "??????????" }, {"????????", "????????" }, {"????????", "????????", "??????????" },};

	/**
	 * ????
	 */
	private String[] mlayoutTitle = {"??????", "????????", "????????", "????????", "????????" };

	/**
	 * ????????????????????????????????
	 */
	private String[][][] mtabSecondItems = {{{}, {"????????", "????????", "????????", "????????" },
                                             // { "????????", "????????", "????????", "????????"},
                                             { "????????", "??????" }, { "????????" } }, { {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {} }, { {}, {}, { "??????????", "??????????", "????????(cm)", "??????????" }, }, { {}, {} }, { {}, {}, {} }
                                            // {{},{},{"??????????1","??????????2","????????????","??????JET","??????1#","??????2#","??????3#"}},
	};

	/**
	 * ????
	 *
	 */
	ImageView iv_state;
	TextView tv_antenna;
	/**
	 * ????????????????
	 */
	private int REALTIME_THREADMSG_READDATAS = 1; // ????????????????
	private boolean mRealthreadStop = false; // ????????????????????
	private long mRealthreadSleepTime = 200; // ????????????????????,??????????????????????
	private boolean mRealthreadReadingDatas = false; // ????????????????????

	/**
	 * ????????????????
	 */
	public static int RADARDEVICE_ERROR_NO = 0; // ????????????
	public static int RADARDEVICE_ERROR_CHANGEMODUSBLTD = 0x1000; // ????????????????????
	public static int RADARDEVICE_ERROR_OPEN = 0x1001; // ????????????????
	public static int RADARDEVICE_ERROR_STARTCOMMAND = 0x1002; // ???? '????????' ????
	public static int RADARDEVICE_ERROR_STOPCOMMAND = 0x1003; // ???? '????????' ????
	public static int RADARDEVICE_ERROR_CLOSE = 0x1004; // ????????????????

	private volatile MyApplication mApp; // ????????
	private boolean mBoolState = false; // ??????????????????
	private int freqSelectedID; // ????????????
	private String freqStr = null;
	private int selectedID = 0; // ????????????

	/**
	 * ????????????
	 */
	private final int RADAR_AL2GFRQ_SEL = 0;
	private final int RADAR_GC15FRQ_SEL = 3;

	private AntennaDevice srlport;

	// ????????
	public void loadDriver() {
		mApp.mRadarDevice.loadDriver();
	}

	/**
	 * ????????
	 */
	static {
		DebugUtil.i("MainActivity", "static");
		// System.loadLibrary("//libUSBDriver-JNI");
		// System.loadLibrary("//data//aaa//libUSBDriver-JNI");
		// String storage;
		// str = Environment.getExternalStorageDirectory();
		// storage = str.getAbsolutePath();

		File f = new File("/system/lib/libUSBDriver-JNI.so");

		if (f.exists()) {
			Log.i("MainActivity", "file exists static");
			System.load("/system/lib/libUSBDriver-JNI.so");
		}
	}

	public void loadUSBDriver() {
		DebugUtil.i("MainActivity", "loadDriver");
		// //????????
		try {
			Process su;
			su = Runtime.getRuntime().exec("su");
			String cmd;
			cmd = "insmod " + "/system/lib/modules/usb-skeleton.ko" + "\n" + "exit\n";
			su.getOutputStream().write(cmd.getBytes());
			if ((su.waitFor() != 0)) {
				// throw new SecurityException();
				// Toast.makeText(this, "su????????????!", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
			// throw new SecurityException();
			// Toast.makeText(this, "????????????????!", Toast.LENGTH_SHORT).show();
		}
	}

	public void UnloadDriver() {
		mApp.mRadarDevice.unloadDriver();
		/*
		 * Intent broadcastIntent = new
		 * Intent("com.example.LTDDRIVERLOADBROADCAST"
		 * );//("mobile.android.ch10.MYBROADCAST");
		 * broadcastIntent.addCategory("com.example.mycategory"
		 * );//("mobile.android.ch10.mycategory");
		 * broadcastIntent.putExtra("name", (byte)2);//"unload");
		 * Log.i(TAG,"before unloadDriver");
		 * mContext.sendBroadcast(broadcastIntent);
		 * Log.i(TAG,"end_unloadDriver");
		 */
		/*
		 * /////???????????? try { Process su; su =
		 * Runtime.getRuntime().exec("/system/bin/su"); String cmd; cmd =
		 * "rmmod " + "usb_skeleton" + "\n" + "exit\n";
		 * su.getOutputStream().write(cmd.getBytes()); if ((su.waitFor() != 0) )
		 * { // throw new SecurityException(); Toast.makeText(this, "su????????????!",
		 * 1000).show(); } } catch (Exception e) { e.printStackTrace(); // throw
		 * new SecurityException(); Toast.makeText(this, "????????????????!",
		 * 1000).show(); }
		 */
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}

	protected final Object mAppLock = new Object();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_left, container, false);
		mContext = getActivity();
		synchronized (mAppLock) {
			mApp = (MyApplication) mContext.getApplicationContext();
			mApp.setLeftFragment(this);
		}
		mApp.saveLeftFragmentContext(mContext);

		tv_title = (TextView) view.findViewById(R.id.title);
		tv_title.setText(this.mlayoutTitle[mTab]);

		mRadarParamAdapter = new RadarParamExpandableListAdapter(getActivity(), mtabFirstItems[mTab], mtabSecondItems[mTab], mTab);
		LeftFragment.mexpListView = (ExpandableListView) view.findViewById(R.id.explv);
		mApp.mListView = mexpListView;
		setExpandableListView(mexpListView);

		// this.mexpListView.setAdapter(adapter);
		mexpListView.setAdapter(mRadarParamAdapter);
		mexpListView.setOnChildClickListener(this);
		mexpListView.setOnGroupClickListener(this);
		mexpListView.setOnItemSelectedListener(mListViewOnItemSelectedListener);
		mexpListView.setOnKeyListener(mListViewOnKeyListener);
		mexpListView.setOnFocusChangeListener(mListViewOnFocusChangeListener);
		mWhellCheckCoeff = new double[mApp.mRadarDevice.getWheelDefaultMaxNum()];

		setAntenFrqPopwindow();
		setWheelExtendWindow();
		setSaveRadarParamsPopwindow();
		// setSelectStoragePopwindow();

		setWheelCalibrate(); // ????????
		setCheckFileWindow(); // ????????????????????????
		setUserInforPopWindow();// ????????????

		Button button;
		button = (Button) antenFrqView.findViewById(R.id.buttonAntenfrqParamsOK);
		button.setOnClickListener(mButtonClickHandler);
		button = (Button) antenFrqView.findViewById(R.id.buttonAntenfrqParamsCANCEL);
		button.setOnClickListener(mButtonClickHandler);

		if (mApp.mRadarDevice.loadSystemSetFile()) {
			DebugUtil.i(TAG, "??????????????????");
		} else {
			DebugUtil.i(TAG, "??????????????????");
		}

		// ????????????????????????
		setSelectStoragePopwindow();
		mTab = mApp.getLeftFragmentTab();

		// ????????
		devicePowerUp();

		// ??????????????????
		// mThreadHardplus = new Thread(mThreadHardplusRunnable);
		// mApp.setRunFirstHardplusThread(true);
		// mThreadHardplus.start();

		srlport = new AntennaDevice();

		// ????????????
		try {
			mLog = LogWriter.open();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return view;
	}

	/*
	 * ????????
	 */
	private void devicePowerUp() {
		mApp.mPowerDevice.AntennaPowerUp();// ????????
		mApp.mPowerDevice.DisplayPowerUp();// ????????
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// openAntennaDevice();
		mApp.mPowerDevice.StepPowerUp();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mApp.mPowerDevice.DSPPowerUp();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setExpandableListView(ExpandableListView radarSetExpListView) {
		radarSetExpListView.setGroupIndicator(null);
		radarSetExpListView.setVerticalScrollBarEnabled(false);
		//
		ColorDrawable drawable_divide = new ColorDrawable(Color.LTGRAY);
		// // radarSetExpListView.setCacheColorHint(Color.TRANSPARENT);

		radarSetExpListView.setChildDivider(drawable_divide);
		radarSetExpListView.setDividerHeight(3);
		// radarSetExpListView.setBackgroundDrawable(getResources().getDrawable(R.drawable.listbackground));

		// ColorDrawable drawable_tranparent_ = new ColorDrawable(Color.GRAY);
		// radarSetExpListView.setSelector(R.drawable.listbackground);
	}

	public int mNowItemCommandID = 0; // ????????????????????????????

	/*
	 * 0:??????
	 */
	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * android.widget.ExpandableListView.OnChildClickListener#onChildClick(android
	 * .widget.ExpandableListView, android.view.View, int, int, long)
	 * ????????????????????????
	 */
	private int mCurrentMode = 0;

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		// showToastMsg("onChildClick,tab=" + mTab);
		DebugUtil.i(TAG, "onChildClick,groupPosition:=" + groupPosition + ";childPosition:=" + childPosition + ";mTab:=" + mTab);
		mTab = mApp.getLeftFragmentTab();
		EditText et_wheelExtend = null;
		// ????
		switch (mTab) {
		// ??????
		case 0:
			// ????????????????
			switch (groupPosition) {
			case 0:
				break;
			// ????????|
			case 1:
				switch (childPosition) {
				// ????????/????????
				case 0:
					mTab = 1;
					mRadarParamAdapter = new RadarParamExpandableListAdapter(getActivity(), mtabFirstItems[mTab], mtabSecondItems[mTab], mTab);
					setExpandableListView(mexpListView);
					mexpListView.setAdapter(mRadarParamAdapter);
					break;
				// ????????/????????
				case 1:
					mTab = 2;
					mRadarParamAdapter = new RadarParamExpandableListAdapter(getActivity(), mtabFirstItems[mTab], mtabSecondItems[mTab], mTab);
					setExpandableListView(mexpListView);
					// mRadarParamAdapter.notifyDataSetChanged();
					mexpListView.setAdapter(mRadarParamAdapter);
					break;
				// ????????/????????
				case 2:
					mTab = 3;
					mRadarParamAdapter = new RadarParamExpandableListAdapter(getActivity(), mtabFirstItems[mTab], mtabSecondItems[mTab], mTab);
					setExpandableListView(mexpListView);
					mexpListView.setAdapter(mRadarParamAdapter);
					mexpListView.expandGroup(1);
					break;
				case 3:
					mTab = 4;
					mRadarParamAdapter = new RadarParamExpandableListAdapter(getActivity(), mtabFirstItems[mTab], mtabSecondItems[mTab], mTab);
					setExpandableListView(mexpListView);
					mexpListView.setAdapter(mRadarParamAdapter);
					break;
				default:
					break;
				}
				break;
			// ????????
			case 2:
				switch (childPosition) {
				// ????????
				// case 0:
				// DebugUtil.i(TAG,"onChildClick,Item:=????????");
				// break;
				// ????????
				case 0:
					DebugUtil.i(TAG, "onChildClick,Item:=????????");
					try {
						mLog.print(TAG, "??????????");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					/**
					 * ??????????????sd????usb???????????? ??????????????????????LtdFiles??????
					 */
					showSelectStoragePopwindow(v);
					break;
				// ??????
				case 1:
					DebugUtil.i(TAG, "??????????");
					// ??????????????
					if (mApp.getPowerLightState()) {
						mApp.setPowerLightState(false);
						mApp.mPowerDevice.PowerLightOff();
						DebugUtil.i(TAG, "????????????");
					} else {
						mApp.setPowerLightState(true);
						mApp.mPowerDevice.PowerLightOn();
						DebugUtil.i(TAG, "??????????!");
					}
					// ????????????
					mRadarParamAdapter.notifyDataSetChanged();
					// ????????
				case 2:
					DebugUtil.i(TAG, "onChildClick,Item:=????????");
					break;
				// ????????
				case 3:
					DebugUtil.i(TAG, "onChildClick,Item:=????????");
					break;
				// GPS????
				case 4:
					DebugUtil.i(TAG, "onChildClick,Item:=GPS????");
					break;
				default:
					break;
				}
				break;
			// ????
			case 3:
				switch (childPosition) {
				// ????????
				case 0:
					DebugUtil.i(TAG, "onChildClick,Item:=????????");
					showUserInforPopWindow();
					break;
				default:
					break;
				}
				break;
			default:
				break;
			}
			break;
		case 1:
			break;// ??????
		case 2:
			// {},{},{ "??????????","??????????","??????????" },
			switch (groupPosition) {
			case 0:
				break;
			case 1:
				break;
			case 2:
				switch (childPosition) {
				case 0:// ??????????
					if (mApp.mRadarDevice.isRunningMode())
						showWheelExtendWindow();
					else
						DebugUtil.infoDialog(getActivity(), "??????????", "??????????????");
					break;
				case 1:
					// ??????????????????????
					DebugUtil.i(TAG, "??????????????");
					if (mApp.mRadarDevice.isSetting_ExtendNum_Command()) {
						mNowItemCommandID = 0;
					} else {
						mNowItemCommandID = mApp.mRadarDevice.COMMAND_ID_EXTENDNUM;
					}
					mApp.mRadarDevice.setNowSetting_CommandID(mNowItemCommandID);
					mRadarParamAdapter.notifyDataSetChanged();
					break;
				case 2:
					break;
				case 3:
					// ??????????
					// ????????????
					if (mApp.mRadarDevice.getWheeltypeSel() == mManusetWhelltypeIndex) {
						if (!mIsCustomFileEmpty) {
							showWheelCalibratePopWindow();
						} else
							;
					} else {
						showWheelCalibratePopWindow();
					}
					break;
				/*
				 * // ???????????? 2016.6.10 case 4:
				 * if(mApp.mRadarDevice.isRunningMode() &&
				 * mApp.mRadarDevice.isWhellMode() &&
				 * mApp.mRadarDevice.isBackOrientMode())
				 * mApp.mRadarDevice.discardBackDatas(); break;
				 */
				default:
					break;
				}
				break;
			}
			break;
		default:
			break;
		}
		// ??????????????????
		tv_title.setText(this.mlayoutTitle[mTab]);
		mApp.saveLeftFragmentTab(mTab);
		DebugUtil.i(TAG, "onChildClick_1,groupPosition:=" + groupPosition + ";childPosition:=" + childPosition + ";mTab:=" + mTab);

		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * android.widget.ExpandableListView.OnGroupClickListener#onGroupClick(android
	 * .widget.ExpandableListView, android.view.View, int, long)
	 * ??????????????????Group??????????mTab????????
	 */
	@Override
	public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
		// showToastMsg("onGroupClick tab=" + mTab);
		// TODO Auto-generated method stub
		TextView txtView;
		DebugUtil.i(TAG, "onGroupClick,groupPosition:=" + groupPosition + ";mTab:=" + mTab + ";id=:" + id);
		mTab = mApp.getLeftFragmentTab();
		switch (groupPosition) {
		// ????????????????????????????????????????????????????mTab????????
		case 0:
			try {
				switch (mTab) {
				// ????????
				case 0:
					mLog.print(TAG, "onGroupClick,Item:=????????,mBoolState:=" + mBoolState);
					DebugUtil.i(TAG, "onGroupClick,Item:=????????,mBoolState:=" + mBoolState);
					// if( !this.mBoolState ) //2016.6.10
					if (!mApp.mRadarDevice.isRunningMode()) // 2016.6.10
					{
						showAntenfrqPopwindow(v);
						// iv_state = (ImageView)findViewById(R.id.imgv_state);
						// this.iv_state.setBackgroundResource(R.drawable.greenpoint);
					}// ????????????????????????????
					else {
						// ????????
						// ??????????????????????????
						if (mApp.mRadarDevice.isTemstopSaveMode()) {
							mApp.mRadarDevice.continueSave();
							mApp.mMainActivity.stopSave();
						}
						mApp.mMainActivity.radarStop();
						// ????????
						devicePowerDown();

						iv_state = (ImageView) v.findViewById(R.id.imgv_state);
						this.iv_state.setBackgroundResource(R.drawable.redpoint);

						// tv_antenna =
						// (TextView)v.findViewById(R.id.id_antenna);
						// tv_antenna.setText(String.valueOf(mApp.mRadarDevice.getAntenFrqStr()));

						// ??????????0315
						if (mApp.mPowerDevice.WorkLightOff())
							;
						else {
							DebugUtil.i(TAG, "????????????????");
						}
						mBoolState = false;
						DebugUtil.infoDialog(getActivity(), "????????", "????????????????????5????????????????");
						// closeAntennaDevice();
						try {
							Thread.sleep(1000);
						} catch (Exception e) {
							DebugUtil.i(TAG, "????????????????!");
						}
					}// ????????
					break;
				// ????????:??????????
				case 1:
					mLog.print(TAG, "onGroupClick,Item:=????????");
					DebugUtil.i(TAG, "onGroupClick,Item:=????????");
					int index;
					index = mApp.mRadarDevice.getAntenFrqSel();
					mApp.mRadarDevice.setAntenFrq(index);// ????????????
					mApp.mRadarDevice.setAntenFrqParams();
					mApp.mRadarDevice.setAntenDefaultParams(index);
					// ????????????
					mRadarParamAdapter.notifyDataSetChanged();
					// ????????
					mApp.mTimewndRuler.invalidate();
					mApp.mDeepRuler.invalidate();
					break;
				// ????????
				case 2:
					DebugUtil.i(TAG, "onGroupClick,Item:=????????");
					mLog.print(TAG, "onGroupClick,Item:=????????");
					if (!mApp.mRadarDevice.isRunningMode()) {
						DebugUtil.infoDialog(getActivity(), "??????????", "??????????????");
					} else {
						mexpListView.collapseGroup(2);// 0921??????????????????
						mApp.mRadarDevice.setTimeMode();
						mNowItemCommandID = 0;
						mApp.mRadarDevice.setNowSetting_CommandID(mNowItemCommandID);
						((HRulerView) mApp.mHorRuler).setShowscanMode();
						TextView view = (TextView) this.getActivity().findViewById(R.id.textview_speed);
						view.setVisibility(View.INVISIBLE);
					}
					break;
				// ????????
				case 3:
					DebugUtil.i(TAG, "onGroupClick,Item:=????????");
					mLog.print(TAG, "onGroupClick,Item:=????????");
					if (mApp.mRadarDevice.isSetting_AveScan_Command()) {
						mNowItemCommandID = 0;
					} else {
						mNowItemCommandID = mApp.mRadarDevice.COMMAND_ID_SETAVESCAN;
					}
					mApp.mRadarDevice.setNowSetting_CommandID(mNowItemCommandID);
					mKeyF3Down = false;
					break;
				// ????????
				case 4:
					DebugUtil.i(TAG, "onGroupClick,Item:=????????");
					mLog.print(TAG, "onGroupClick,Item:=????????");
					mApp.mRadarDevice.setShowType_DIB();
					mApp.mMainActivity.mRealTimeDIBView.setShowType_DIB();
					((HRulerView) (mApp.mHorRuler)).setPixsPerScan(1);
					mRadarParamAdapter.notifyDataSetChanged();
					break;
				default:
					break;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		// ?????????????? ???????? | ???????? |???????? | ???????? | ????????.....,??mTab??????????
		case 1:
			switch (mTab) {
			// ????????
			case 0:
				DebugUtil.i(TAG, "onGroupClick,Item:=????????");
				try {
					mLog.print(TAG, "onGroupClick,Item:=????????");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			// ????????
			case 1:
				DebugUtil.i(TAG, "onGroupClick,Item:=????????");
				try {
					mLog.print(TAG, "onGroupClick,Item:=????????");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (mApp.mRadarDevice.isSetting_Scanspeed_Command()) {
					mNowItemCommandID = 0;
				} else {
					mNowItemCommandID = mApp.mRadarDevice.COMMAND_ID_SETSCANSPEED;
				}
				mApp.mRadarDevice.setNowSetting_CommandID(mNowItemCommandID);
				mKeyF3Down = false;
				break;
			// ????????
			case 2:

				DebugUtil.i(TAG, "onGroupClick,Item:=????????");
				try {
					mLog.print(TAG, "onGroupClick,Item:=????????");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (!mApp.mRadarDevice.isRunningMode()) {
					DebugUtil.infoDialog(getActivity(), "??????????", "??????????????");
					if (mApp.mRadarDevice.isSetting_DianCe_Command()) {
						mNowItemCommandID = 0;
					} else
						;
					mApp.mRadarDevice.setNowSetting_CommandID(mNowItemCommandID);
				} else {
					mexpListView.collapseGroup(2);// 0921??????????????????
					if (mApp.mRadarDevice.isSetting_DianCe_Command()) {
						mNowItemCommandID = 0;
					} else {
						mNowItemCommandID = mApp.mRadarDevice.COMMAND_ID_SETDIANCE;
					}
					mApp.mRadarDevice.setNowSetting_CommandID(mNowItemCommandID);
					mKeyF3Down = false;

					int val = mApp.mRadarDevice.getDianceNumber();
					mApp.mRadarDevice.setDianceMode(val);
					((HRulerView) (mApp.mHorRuler)).setShowscanMode();
					mRadarParamAdapter.notifyDataSetChanged();

					TextView view = (TextView) this.getActivity().findViewById(R.id.textview_speed);
					view.setVisibility(View.INVISIBLE);
				}
				break;
			// ????????
			case 3:
				DebugUtil.i(TAG, "onGroupClick,Item:=????????");
				try {
					mLog.print(TAG, "onGroupClick,Item:=????????");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				boolean isRem;
				isRem = mApp.mRadarDevice.isRemback();
				if (isRem)
					mApp.mRadarDevice.setRemoveBack(0);
				else
					mApp.mRadarDevice.setRemoveBack(1);
				mApp.mRadarDevice.setRemoveBackParams();
				mRadarParamAdapter.notifyDataSetChanged();
				break;
			// ????????
			case 4:
				DebugUtil.i(TAG, "onGroupClick,Item:=????????");
				try {
					mLog.print(TAG, "onGroupClick,Item:=????????");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mApp.mMainActivity.mRealTimeDIBView.setShowType_DIB();
				mApp.mRadarDevice.setShowType_WIGGLE();
				((HRulerView) (mApp.mHorRuler)).setPixsPerScan(8);
				mRadarParamAdapter.notifyDataSetChanged();
				break;
			}
			break;
		// ?????? ???????? | ???????? | ?????????? | ??????????
		case 2:
			switch (mTab) {
			// ????????
			case 0:
				DebugUtil.i(TAG, "onGroupClick,Item:=????????");
				try {
					mLog.print(TAG, "onGroupClick,Item:=????????");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			// ????????
			case 1:
				DebugUtil.i(TAG, "onGroupClick,Item:=????????");
				try {
					mLog.print(TAG, "onGroupClick,Item:=????????");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (mApp.mRadarDevice.isSetting_Timewindow_Command()) {
					mNowItemCommandID = 0;
				} else {
					mNowItemCommandID = mApp.mRadarDevice.COMMAND_ID_SETTIMEWINDOW;
				}
				mApp.mRadarDevice.setNowSetting_CommandID(mNowItemCommandID);
				mKeyF3Down = false;
				break;
			// ??????????
			case 2:
				DebugUtil.i(TAG, "onGroupClick,Item:=??????????");
				try {
					mLog.print(TAG, "onGroupClick,Item:=??????????");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				/*
				 * if(mApp.mRadarDevice.isSetting_WhellMode_Command()) {
				 * mNowItemCommandID = 0; } else { mNowItemCommandID =
				 * mApp.mRadarDevice.COMMAND_ID_SETWHELLMODE; }
				 * mApp.mRadarDevice.setNowSetting_CommandID(mNowItemCommandID);
				 * int num = mApp.mRadarDevice.getWheelExtendNumber();
				 * mApp.mRadarDevice.setWheelMode(num);
				 */

				if (!mApp.mRadarDevice.isRunningMode()) {
					DebugUtil.infoDialog(getActivity(), "??????????", "??????????????");
				} else {
					mexpListView.expandGroup(groupPosition);// ??????????????????????????
					// showWheelExtendWindow(); 20160617

					// ????????20160617
					// ??????????????
					int extNumber = mApp.mRadarDevice.getWheelExtendNumber();

					// ????????????
					mApp.setRealThreadReadingDatas(false);
					mApp.mRadarDevice.stopRadar();

					// ??????????????
					if (!mApp.mRadarDevice.setWheelMode(extNumber))
						showToastMsg("????????????????!");
					else
						;

					mApp.setRealThreadReadingDatas(true);
					mApp.mRadarDevice.setBackFillPos(0);
					setManuWhellParams();
					((HRulerView) (mApp.mHorRuler)).setShowdistanceMode();
					setNowSpeedRange();

					// ????????????????
					mRadarParamAdapter.notifyDataSetChanged();// 20160617
					return true;
				}
				break;
			// ??????????
			case 4:
				DebugUtil.i(TAG, "onGroupClick,Item:=??????????");
				try {
					mLog.print(TAG, "onGroupClick,Item:=??????????");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (mApp.mRadarDevice.isSetting_SelectColor_Command()) {
					mNowItemCommandID = 0;
				} else {
					mNowItemCommandID = mApp.mRadarDevice.COMMAND_ID_SETCOLORPAL;
				}
				mApp.mRadarDevice.setNowSetting_CommandID(mNowItemCommandID);
				mRadarParamAdapter.notifyDataSetChanged();
				break;
			}
			break;
		// ?????? ???? | ????????
		case 3:
			switch (mTab) {
			case 0:
				break;
			case 1:
				DebugUtil.i(TAG, "onGroupClick,Item:=????????");
				try {
					mLog.print(TAG, "onGroupClick,Item:=????????");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (mApp.mRadarDevice.isSetting_Scanlength_Command()) {
					mNowItemCommandID = 0;
				} else {
					mNowItemCommandID = mApp.mRadarDevice.COMMAND_ID_SETSCANLENGTH;
				}
				mApp.mRadarDevice.setNowSetting_CommandID(mNowItemCommandID);
				mKeyF3Down = false;
				break;
			}
			break;
		// ??4?? ???????? |
		case 4:
			switch (mTab) {
			case 0:
				break;
			// ????????
			case 1:
				try {
					mLog.print(TAG, "onGroupClick,Item:=????????");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (mApp.mRadarDevice.isSetting_Singlepos_Command()) {
					mNowItemCommandID = 0;
				} else {
					mNowItemCommandID = mApp.mRadarDevice.COMMAND_ID_SETSINGLEPOS;
				}
				mApp.mRadarDevice.setNowSetting_CommandID(mNowItemCommandID);
				mKeyF3Down = false;
				break;
			}
			break;
		case 5:
			// ??5?? ???????? |
			switch (mTab) {
			case 0:
				break;
			// ????????
			case 1:
				try {
					mLog.print(TAG, "onGroupClick,Item:=????????");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (mApp.mRadarDevice.isAutoHardplus()) {
					mApp.mRadarDevice.cancelAutoPlus();
					DebugUtil.i(TAG, "cancelAutoPlus");
				} else {
					mApp.mRadarDevice.autoPlus();
					DebugUtil.i(TAG, "autoPlus");
				}
				mRadarParamAdapter.notifyDataSetChanged();
				MultiModeLifeSearchActivity mainActivity;
				mainActivity = (MultiModeLifeSearchActivity) getActivity();
				// mainActivity.mScanView.invalidate();
				break;
			}
			break;
		// ??6?? ????????
		case 6:
			switch (mTab) {
			case 0:
				break;
			case 1:
				try {
					mLog.print(TAG, "onGroupClick,Item:=????????");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (mApp.mRadarDevice.isSetting_AllHardPlus_Command()) {
					mNowItemCommandID = 0;
					mApp.setIsHardplusRun(false);
					mApp.mRadarDevice.resetScanSpeed();
				} else {
					mNowItemCommandID = mApp.mRadarDevice.COMMAND_ID_SETALLHARDPLUS;
					mApp.setIsHardplusRun(true);
					// mApp.mRadarDevice.send64ScanSpeed(); //0519
					// mThreadHardplus.start();
				}
				mApp.mRadarDevice.setNowSetting_CommandID(mNowItemCommandID);
				mKeyF3Down = false;
				break;
			}
			break;
		// ??7?? ????????
		case 7:
			switch (mTab) {
			case 0:
				break;
			case 1:
				try {
					mLog.print(TAG, "onGroupClick,Item:=????????");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (mApp.mRadarDevice.isSetting_StepHardPlus_Command()) {
					mNowItemCommandID = 0;
					mApp.setIsHardplusRun(false);
					mApp.mRadarDevice.resetScanSpeed();
				} else {
					mNowItemCommandID = mApp.mRadarDevice.COMMAND_ID_SETSTEPHARDPLUS;
					mApp.setIsHardplusRun(true);
					// mApp.mRadarDevice.send64ScanSpeed();//0519
				}
				mApp.mRadarDevice.setNowSetting_CommandID(mNowItemCommandID);
				mKeyF3Down = false;
				break;
			}
			break;
		// ??8?? ????
		case 8:
			switch (mTab) {
			case 0:
				break;
			case 1:
				try {
					mLog.print(TAG, "onGroupClick,Item:=????");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (mApp.mRadarDevice.isSetting_Filter_Command()) {
					mNowItemCommandID = 0;
				} else {
					mNowItemCommandID = mApp.mRadarDevice.COMMAND_ID_SETFILTER;
				}
				mApp.mRadarDevice.setNowSetting_CommandID(mNowItemCommandID);
				break;
			}
			break;
		// ??9?? ????????
		case 9:
			switch (mTab) {
			case 0:
				break;
			case 1:
				try {
					mLog.print(TAG, "onGroupClick,Item:=????????");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (mApp.mRadarDevice.isSetting_JieDianConst_Command()) {
					mNowItemCommandID = 0;
				} else {
					mNowItemCommandID = mApp.mRadarDevice.COMMAND_ID_SETJIEDIANCONST;
				}
				mApp.mRadarDevice.setNowSetting_CommandID(mNowItemCommandID);
				mKeyF3Down = false;
				break;
			}
			break;
		// ??10?? ???????? | .....
		case 10:
			switch (mTab) {
			case 0:
				break;
			// ????????
			case 1:
				try {
					mLog.print(TAG, "onGroupClick,Item:=????????");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				saveParams();
				break;
			}
			break;
		// ??11?? ???????? | ....
		case 11:
			switch (mTab) {
			case 0:
				break;
			// ????????
			case 1:
				try {
					mLog.print(TAG, "onGroupClick,Item:=????????");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mApp.mMainActivity.loadParams();
				break;
			}
			break;
		default:
			break;
		}
		mApp.saveLeftFragmentTab(mTab);
		DebugUtil.i(TAG, "onGroupClick_1,groupPosition:=" + groupPosition + ";mTab:=" + mTab + ";id=:" + id);

		return false;
	}

	/**
	 * ????????
	 */
	private void devicePowerDown() {
		mApp.mPowerDevice.DSPPowerDown();
		mApp.mPowerDevice.StepPowerDown();
		mApp.mPowerDevice.DisplayPowerDown();
		mApp.mPowerDevice.AntennaPowerDown();
	}

	/**
	 * ????AntennaDevice??????
	 */
	private void closeAntennaDevice() {
		srlport.closeSerialPort();
	}

	// ??????????????????????
	private View antenFrqView;
	private PopupWindow mAntenfrqPopWindow;
	private RadioGroup radioGroup;

	public void setAntenFrqPopwindow() {
		antenFrqView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_antenfrq_popwindow, null);
		RadioButton radioButton;
		int sel = 0;
		radioGroup = (RadioGroup) antenFrqView.findViewById(R.id.radioGroupAntenFrq);
		radioButton = (RadioButton) radioGroup.getChildAt(sel);
		radioGroup.check(radioButton.getId());
		// radioGroup.setOnCheckedChangeListener(mRadioGroupSelChangeHandler);
		int i = 0;
		for (i = 0; i < radioGroup.getChildCount(); i++) {
			radioButton = (RadioButton) radioGroup.getChildAt(i);
			radioButton.setOnClickListener(mAntenfrqRadio_OnClickHandler);
		}

		mAntenfrqPopWindow = new PopupWindow(antenFrqView, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		// ????????????????????
		mAntenfrqPopWindow.setFocusable(true);
	}

	private int mFrqSelIndex = 5;
	private int mFrqSelIndex2 = 5;
	private int RADAR_GC400_SEL = 5;
	private int RADAR_GC900_SEL = 4;

	public void showAntenfrqPopwindow(View v) {
		View layout = this.getView();

		int xPos = layout.getWidth();
		int yPos = layout.findViewById(R.id.title).getHeight();

		// ????????????????????radio????
		RadioGroup group;
		group = (RadioGroup) antenFrqView.findViewById(R.id.radioGroupAntenFrq);
		int id = group.getCheckedRadioButtonId();
		RadioButton radio;
		radio = (RadioButton) antenFrqView.findViewById(id);
		radio.requestFocus();

		mAntenfrqPopWindow.setBackgroundDrawable(new BitmapDrawable());
		mAntenfrqPopWindow.showAtLocation(v, Gravity.TOP | Gravity.LEFT, xPos, yPos);
		mAntenfrqPopWindow.update();
	}

	// ??????????????????????
	private View storageSelectView;
	private PopupWindow mstorageSelectPopWindow;
	private RadioGroup radioGroupStorage;

	public void setSelectStoragePopwindow() {
		storageSelectView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_storageselect_popwindow, null);
		radioGroupStorage = (RadioGroup) storageSelectView.findViewById(R.id.radioGroupStorage);
		int sel = mApp.mRadarDevice.getSelectStorageIndex();
		DebugUtil.i(TAG, "setSelectStoragePopWindow sel=" + sel);

		RadioButton radioButton = null;
		radioButton = (RadioButton) radioGroupStorage.getChildAt(sel);
		radioGroupStorage.check(radioButton.getId());
		// radioGroup.setOnCheckedChangeListener(mRadioGroupSelChangeHandler);

		int i;
		for (i = 0; i < radioGroupStorage.getChildCount(); i++) {
			radioButton = (RadioButton) radioGroupStorage.getChildAt(i);
			radioButton.setOnClickListener(mStorageSelect_OnClickHandler);
		}

		mstorageSelectPopWindow = new PopupWindow(storageSelectView, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		// ????????????????????
		mstorageSelectPopWindow.setFocusable(true);
	}

	// ????????????????
	public void showSelectStoragePopwindow(View v) {
		// ????????????????????radio????
		RadioButton radio;
		int sel = mApp.mRadarDevice.getSelectStorageIndex();
		DebugUtil.i(TAG, "setSelectStoragePopWindow sel=" + sel);

		RadioButton radioButton = null;
		radioButton = (RadioButton) radioGroupStorage.getChildAt(sel);
		radioGroupStorage.check(radioButton.getId());

		int id = radioGroupStorage.getCheckedRadioButtonId();
		radio = (RadioButton) storageSelectView.findViewById(id);
		DebugUtil.i(TAG, "showStorage sel=" + sel);
		radio.requestFocus();

		mstorageSelectPopWindow.setBackgroundDrawable(new BitmapDrawable());
		mstorageSelectPopWindow.showAtLocation(v, Gravity.CENTER | Gravity.CENTER, 0, 0);
		mstorageSelectPopWindow.update();
	}

	public MyApplication getApplication() {
	    synchronized (mAppLock) {
            return mApp;
        }
	}

	/**
	 * ????????????,????????????????????????
	 *
	 * @param antenFrqIndex
	 */
	public boolean radarRealStart(int antenFrqIndex) {
		mApp.mRadarDevice.mIsUseSoftPlus = false;
		// ????????
		// ??????????????
		// ??????????????????????

		try {
			devicePowerUp();
		} catch (Exception e) {
			DebugUtil.i(TAG, "????????sleep run fail_sleep!");
			Log.d("debug_radar", "????????????");
		}

		boolean bRet = radarStart(antenFrqIndex);
		if (!bRet) {
			showToastMsg("????????????!");
			Log.d("debug_start_radar", "????????????");
		} else {
			View view1 = mexpListView.getChildAt(0);
			iv_state = (ImageView) view1.findViewById(R.id.imgv_state);
			iv_state.setBackgroundResource(R.drawable.greenpoint);
			mApp.mRadarDevice.continueShow();
			/*
			 * tv_antenna = (TextView)view1.findViewById(R.id.id_antenna);
			 * tv_antenna.setText(freqStr);
			 */
			// ??????????0315
			Log.d("debug_radar", "before work light on");
			if (mApp.mPowerDevice.WorkLightOn()) {
				Log.d("debug_radar", "work light on success");
			} else {
				Log.d("debug_radar", "work light on fail");
				DebugUtil.i(TAG, "????????????????");
			}
		}
		return bRet;
	}

	/*
	 * ??????????????????
	 */
	private void openAntennaDevice() {
		long starttime = System.currentTimeMillis();
		srlport.openSerialPort("/dev/ttySAC3", 9600, 'N', 8, 1, 0);// ????????
		long stoptime = 0;
		long time = 0;
		boolean overtime = true;
		while (srlport.getFreqCode() == -1 || srlport.getRepFreqCode() == -1) {
			// DebugUtil.i("SerialPort",
			// "FreqCode="+String.valueOf(srlport.getFreqCode()));
			// DebugUtil.i("SerialPort",
			// "ReqFreqCode="+String.valueOf(srlport.getRepFreqCode()));
			stoptime = System.currentTimeMillis();
			time = stoptime - starttime;
			if (time > 5000) {
				showToastMsg("??????????????");
				overtime = false;
				break;
			} else
				;
		}

		if (overtime) {
			String tempStr = srlport.getFreqStr();
			if (tempStr != null) {
				this.autoAntennaDialog("????????" + tempStr + "?????");
			} else {
				this.autoAntennaDialog("??????????????????" + String.valueOf(srlport.getFreqCode()));
			}
		} else {
			this.autoAntennaDialog("??????????????????");
		}

		DebugUtil.i("SerialPort", "????=" + String.valueOf(time));
		DebugUtil.i("SerialPort", "FreqCode=" + String.valueOf(srlport.getFreqCode()));
		DebugUtil.i("SerialPort", "ReqFreqCode=" + String.valueOf(srlport.getRepFreqCode()));

		showToastMsg("FreqCode=" + String.valueOf(srlport.getFreqCode()));
		showToastMsg("ReqFreqCode=" + String.valueOf(srlport.getRepFreqCode()));

		srlport.closeSerialPort();
	}

	// //????????????????????
	public boolean radarStart(int frqIndex) {
		int ret;
		boolean bRet = false;
		// ????????????????????
		if (mApp.mRadarDevice.isRunningMode()) {
			DebugUtil.i(TAG, "radarStart : device has start!");
			return true;
		}
		// ????????
		// loadDriver();

		// ??????????????????????????????????????
		String fileName;
		fileName = mApp.mRadarDevice.getInnerStoragePath() + mApp.mRadarDevice.mParamsFilefolderPath;
		fileName += radarDevice.g_antenFrqStr[frqIndex] + ".par";
		// Toast.makeText(this, fileName, Toast.LENGTH_SHORT).show();

		/**
		 * ??????????????????????
		 */
		mApp.mRadarDevice.setAntenFrq(frqIndex);
		if (!mApp.mRadarDevice.onlyLoadParamsFromeFile(fileName)) {
			// DebugUtil.i(TAG,"!!!!!~~~~Now setAntenFrq:"+frqIndex);
			// Toast.makeText(this, "~~~~Now AntenFrq:"+frqIndex,
			// Toast.LENGTH_SHORT).show();

			// ??????????????????????????????????????????
			mApp.mRadarDevice.changeParamsFromeAntenfrq(frqIndex);
		} else {
		}

		/**
		 * ??????????????????????????????
		 */
		if (!mApp.mRadarDevice.loadDefaultWhellcheckParams()) {
			DebugUtil.i(TAG, "loadDefaultWheelCheckParams fail!");
			mApp.mRadarDevice.changeWheelPropertyFromAnteFrq(frqIndex);
		} else {
			DebugUtil.i(TAG, "loadDefaultWheelCheckParams!Success!");
		}

		DebugUtil.i(TAG, "radarstart loaddefaultcheck,extendNumber=" + mApp.mRadarDevice.getWheelExtendNumber());
		DebugUtil.i("radarDevice", "3.leftgetWheeltype" + mApp.mRadarDevice.getWheeltypeSel());
		// ??????????????????
		int nowSel = mApp.mRadarDevice.getAntenFrqSel();
		// DebugUtil.i(TAG,"!!!!!Now AntenFrq:"+nowSel);
		// Toast.makeText(this, "!!!!!~~~~Now AntenFrq:"+nowSel,
		// Toast.LENGTH_SHORT).show();
		mApp.mRadarDevice.refreshFileHeader();
		// ??????????????????????????????????????????
		// changeParamsListFromeRadar();
		// hss2016.6.6
		// ????????
		mRadarParamAdapter.notifyDataSetChanged();
		mApp.mTimewndRuler.invalidate();
		mApp.mDeepRuler.invalidate();
		((HRulerView) mApp.mHorRuler).setShowscanMode();

		// mParamsListAdapter.notifyDataSetChanged();

		/*
		 * //?????????????????????? fileName =
		 * mApp.mRadarDevice.mSDCardPath+mApp.mRadarDevice
		 * .mParamsFilefolderPath; fileName +=
		 * mApp.mRadarDevice.mWhellcheckFilename;
		 * if(!mApp.mRadarDevice.loadWhellcheckParams(fileName)) {
		 *
		 * }
		 */
		// ????????
		ret = mApp.mRadarDevice.start();
		if (ret == RADARDEVICE_ERROR_NO) {
			// mRealtimeDIBView.initDIB();
			// mRealtimeDIBView.invalidate();
			mRealthreadReadingDatas = true; // ????????,????????????
			bRet = true;

			// //??????????????????????activity
			// scanView view = (scanView)findViewById(R.id.viewSinglewave);
			// view.invalidate();

			String name = mApp.mRadarDevice.getParamsPath() + "defSetParams.par";
			// loadSetParamsFile(name); ????????

			// BB80?20170419hss
			// mApp.mRadarDevice.setHandleMode();

			// iv_state.setBackgroundResource(R.drawable.greenpoint);
		} else {
			Log.d("debug_radar", "open radar failed");
			if (ret == RADARDEVICE_ERROR_OPEN) {
				showToastMsg("????????????????!");
			}
			if (ret == RADARDEVICE_ERROR_STARTCOMMAND) {
				showToastMsg("????????????????!");
			}
			// UnloadDriver();
		}

		return bRet;
	}

	// "????????????" ????????????????
	public Button.OnClickListener mButtonClickHandler = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			int id = v.getId();
			switch (id) {
			case R.id.buttonAntenfrqParamsOK:
				dialogButton();
				break;
			case R.id.buttonAntenfrqParamsCANCEL:
				mAntenfrqPopWindow.dismiss();
				break;
			default:
				break;
			}
		}
	};

	/**
	 * ??????????
	 */
	public void dialogButton() {
		// ??????????????
		android.app.AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle("????????");
		builder.setTitle("????????" + freqStr + "??????").setMessage("????????").setNegativeButton("??", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// ??????????????????????
				mAntenfrqPopWindow.dismiss();
				// ??????????????????????????????????????,??????????????????
				mApp.mRadarDevice.setAntenFrq(freqSelectedID);
				// ????????
				radarRealStart(freqSelectedID);
				DebugUtil.i(TAG, "finish radarRealStart!");

				// ????????????????
				setNowSpeedRange();
				mApp.setRealThreadStop(false);
				mApp.setRealThreadReadingDatas(true);
				Log.d("debug_radar", "is true");
				tv_title.setText(mlayoutTitle[mTab]);
				// ??????
				mBoolState = true;
			}
		}).setPositiveButton("??", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		}).show();
	}

	// ??????????????????????
	public void autoAntennaDialog(String freqStr) {
		// ??????????????
		android.app.AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle("????????");
		builder.setTitle(freqStr).setMessage("????????").setNegativeButton("????", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// ????????????????
			}
		}).setPositiveButton("????????", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		}).setNeutralButton("????????", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		}).show();
	}

	/**
	 * ??????????????????????
	 *
	 * @param keyCode
	 * @param event
	 * @return
	 */
	boolean mKeyF3Down = false;
	boolean mKeyF2Down = false;

	public boolean onKeyDown(int keyCode, KeyEvent event, Context inputContext) {
		DebugUtil.i(KTAG, "Left onKeyDown,keycode:=" + keyCode);
		MyApplication mApp = (MyApplication) inputContext.getApplicationContext();
		Context lcontext = mApp.getLeftFragmentContext();

		// ????,??????????????????
		if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			DebugUtil.i("Left", "mListViewOnKeyListener");
			DebugUtil.i(TAG, "onKeyDown,mTab:=" + mTab);
			if (mApp.mRadarDevice.isSetting_Command()) {
				DebugUtil.i(TAG, "isSetting_Command=" + mApp.mRadarDevice.getNowSetting_CommandID());
				return false;
			} else
				;

			int tab;
			tab = mApp.getLeftFragmentTab();
			int sel;
			sel = mApp.mRadarDevice.getAntenFrqSel();

			switch (tab) {
			// ?? ???????? ????????
			case 1:
				mTab = 0;
				mApp.saveLeftFragmentTab(mTab);
				mRadarParamAdapter = new RadarParamExpandableListAdapter(lcontext, mtabFirstItems[mTab], mtabSecondItems[mTab], mTab);
				setExpandableListView(mexpListView);
				mexpListView.setAdapter(mRadarParamAdapter);
				mRadarParamAdapter.notifyDataSetChanged();
				mexpListView.expandGroup(1);
				mexpListView.setSelectedChild(1, 0, true);
				// ????????
				mApp.mRadarDevice.saveParamsFile(mApp.mRadarDevice.INNERSTORAGE + mApp.mRadarDevice.mParamsFilefolderPath + radarDevice.g_antenFrqStr[sel] + ".par");
				mApp.mRadarDevice.saveSystemSetFile();
				break;
			// ?? ???????? ????
			case 2:
				mTab = 0;
				mApp.saveLeftFragmentTab(mTab);
				mRadarParamAdapter = new RadarParamExpandableListAdapter(lcontext, mtabFirstItems[mTab], mtabSecondItems[mTab], mTab);
				setExpandableListView(mexpListView);
				mexpListView.setAdapter(mRadarParamAdapter);
				mRadarParamAdapter.notifyDataSetChanged();
				mexpListView.expandGroup(1);
				mexpListView.setSelectedChild(1, 1, true);
				// ????????
				// ????????????????????????????
				if (mApp.mRadarDevice.getWheeltypeSel() == mManusetWhelltypeIndex) {
					DebugUtil.i(TAG, "==mManusetWhelltypeIndex!");

					String filePath = mApp.mRadarDevice.INNERSTORAGE + mApp.mRadarDevice.mParamsFilefolderPath + mApp.mRadarDevice.getCustomFileName();
					DebugUtil.i("ExtendNumb", "filePath=" + filePath);

					// 0918??????
					// mApp.mRadarDevice.saveWhellcheckParams(filePath);
					// ????????????????????????
					if (mApp.getCustomWheelIndex() > -1)
						mApp.mRadarDevice.saveCustomWheelExtend(filePath);
					else
						;
				} else {
					DebugUtil.i(TAG, "??=mManusetWhelltypeIndex!");
					mApp.mRadarDevice.saveDefaultCheckParamsFile();
				}
				break;
			// ?? ???????? ????
			case 3:
				mTab = 0;
				mApp.saveLeftFragmentTab(mTab);
				mRadarParamAdapter = new RadarParamExpandableListAdapter(lcontext, mtabFirstItems[mTab], mtabSecondItems[mTab], mTab);
				setExpandableListView(mexpListView);
				mexpListView.setAdapter(mRadarParamAdapter);
				mRadarParamAdapter.notifyDataSetChanged();
				mexpListView.expandGroup(1);
				mexpListView.setSelectedChild(1, 2, true);
				break;
			// ??????????????
			case 4:
				mTab = 0;
				mApp.saveLeftFragmentTab(mTab);
				mRadarParamAdapter = new RadarParamExpandableListAdapter(lcontext, mtabFirstItems[mTab], mtabSecondItems[mTab], mTab);
				setExpandableListView(mexpListView);
				mexpListView.setAdapter(mRadarParamAdapter);
				mRadarParamAdapter.notifyDataSetChanged();

				mexpListView.expandGroup(1);
				mexpListView.setSelectedChild(1, 3, true);
				break;
			default:
				mTab = 0;
				mApp.saveLeftFragmentTab(mTab);
				mRadarParamAdapter = new RadarParamExpandableListAdapter(lcontext, mtabFirstItems[mTab], mtabSecondItems[mTab], mTab);
				setExpandableListView(mexpListView);
				mexpListView.setAdapter(mRadarParamAdapter);
				break;
			}
			mApp.saveLeftFragmentTab(mTab);
			tv_title.setText(mlayoutTitle[mTab]);
		}
		DebugUtil.i(TAG, "onKey_BACK,after" + ";mTab:=" + mTab);
		return true;
	}

	/**
	 * @param keyCode
	 * @param event
	 * @param inputContext
	 * @return
	 */
	public boolean onKeyUp(int keyCode, KeyEvent event, Context inputContext) {
		DebugUtil.i(KTAG, "Left onKeyUp!");
		return true;
	}

	/**
	 * ??????????????
	 *
	 * @param keycode
	 *            event inputContext
	 */
	public boolean onLongKeyDown(int keyCode, KeyEvent event, Context inputContext) {
		DebugUtil.i(TAG, "onLongKeyDown");
		if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			DebugUtil.i(TAG, "onLongKeyDown");
		}
		return true;
	}

	// ????????????
	public void showToastMsg(String text) {
		Toast.makeText(mContext, text, Toast.LENGTH_LONG);
	}

	/**
	 * ??????????????????
	 */
	private long tcurrent[] = new long[10];
	private int iKCount = 0;
	private long interRespon = 0;
	public View.OnKeyListener mListViewOnKeyListener = new View.OnKeyListener() {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			DebugUtil.i(KTAG, "Left mListViewOnKeyListener,keyCode:=" + keyCode + "repCount:=" + event.getRepeatCount());

			// ????
			if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||// 4//
                // ||
                keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)// 22 )//
			{
				DebugUtil.i("Left", "mListViewOnKeyListener");
				if (!mApp.mRadarDevice.isSetting_Command()) {
					iKCount++;
					if (iKCount > 2)
						iKCount = iKCount % 2;
					else
						;

					tcurrent[iKCount] = System.currentTimeMillis();

					// ????onkeydown
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						mApp.setRealTimeDraw(false);// ????????
						// ????????????
						long ltemp = Math.abs(tcurrent[iKCount] - tcurrent[Math.abs(iKCount - 1)]);
						DebugUtil.i(KTAG, "Left mListViewOnKeyListener ActionDown,????=" + ltemp);
						// if(ltemp < 100)
						// return true;
						// else;0523????100ms??????????
					} else if (event.getAction() == KeyEvent.ACTION_UP) {
						mApp.setRealTimeDraw(true); // ????????
						// ????????????
						// mApp.setRealThreadReadingDatas(true);
						long up = System.currentTimeMillis();

						long ltemp = Math.abs(up - tcurrent[iKCount]);
						DebugUtil.i(KTAG, "Left mListViewOnKeyListener ActionUp,????=" + ltemp);

						// // if(
						// mThreadCheckMsgHandler.sendEmptyMessageDelayed(1,500)
						// )
						// DebugUtil.i(TAG, "mThreadCheckMsgHandler");
						// else;
					}
					return false;
				}
				return manageOnKey_ForSettingCommand(v, keyCode, event);
			}
			// ????F3????????
			if (keyCode == KeyEvent.KEYCODE_F3) {
				if (mApp.mRadarDevice.isSetting_Command()) {
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						mKeyF3Down = true;

					}
					if (event.getAction() == KeyEvent.ACTION_UP) {
						mKeyF3Down = false;
					}
					DebugUtil.i("Left", "mKeyF3Down=" + mKeyF3Down);
				}
			}
			if (keyCode == KeyEvent.KEYCODE_F2) {
				if (mApp.mRadarDevice.isSetting_Command()) {
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						mKeyF2Down = true;
					}
					if (event.getAction() == KeyEvent.ACTION_UP) {
						mKeyF2Down = false;
					}
				}
			}
			return false;
		}
	};

	public void manageParamsCommand_SetScanSpeed() {

	}

	public void manageParamsCommand_SetDiacCe() {
		// mApp.mRadarDevice.setDianceMode(extNumber);
	}

	public void manageParamsCommand_SetRemback() {
		// mApp.mRadarDevice.setRemoveBack(selIndex);
	}

	private int mKCount = 0;
	private long mcurrent[] = new long[10];

	/**
	 * @param v
	 * @param keyCode
	 * @param event
	 * @return
	 */
	public boolean manageOnKey_ForSettingCommand(View v, int keyCode, KeyEvent event) {
		int val;
		MultiModeLifeSearchActivity mainActivity;
		mainActivity = (MultiModeLifeSearchActivity) getActivity();

		// ??????????
		if (event.getAction() == KeyEvent.ACTION_DOWN) // ACTION_UP)//ACTION_DOWN
														// )//
		{
			DebugUtil.i(KTAG, "manageOnKey_ForSettingCommand,DownKey:=" + keyCode);
			// ????????????????????????????????????????????????????
			mApp.setRealThreadReadingDatas(false);

			// ???? ????????
			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				DebugUtil.i("Left", "manageOnKey_ForSettingCommand");

				// ????????????????
				if (mApp.mRadarDevice.isSetting_StepHardPlus_Command()) {
					if (mKeyF3Down)
						mainActivity.mScanView.manageKeyLeft(10, false);
					else
						mainActivity.mScanView.manageKeyLeft(1, false);
					// mainActivity.mScanView.invalidate();
				}

				// ????????????????
				if (mApp.mRadarDevice.isSetting_AllHardPlus_Command()) {
					float[] mHardpluse = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
					float mixVal = mApp.mRadarDevice.getMixHardplus();
					mHardpluse = mApp.mRadarDevice.getHardplus();
					int i = 0;
					for (i = 0; i < 9; i++) {
						if (mKeyF3Down)
							mHardpluse[i] -= 10;
						else
							mHardpluse[i] -= 1;
						if (mHardpluse[i] < mixVal)
							mHardpluse[i] = mixVal;
					}
					mApp.mRadarDevice.setHardplusValusOnly(mHardpluse);

					// mainActivity.mScanView.invalidate();
				}
			}

			// ???? ????????
			if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)// 22)//
			{
				// ????????????????
				if (mApp.mRadarDevice.isSetting_StepHardPlus_Command()) {
					if (mKeyF3Down)
						mainActivity.mScanView.manageKeyRight(10, false);
					else
						mainActivity.mScanView.manageKeyRight(1, false);
					// mainActivity.mScanView.invalidate();
				}
				// ????????????????
				if (mApp.mRadarDevice.isSetting_AllHardPlus_Command()) {
					float[] mHardpluse = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
					float maxVal = mApp.mRadarDevice.getMaxHardplus();
					mHardpluse = mApp.mRadarDevice.getHardplus();
					int i;
					for (i = 0; i < 9; i++) {
						if (mKeyF3Down)
							mHardpluse[i] += 10;
						else
							mHardpluse[i] += 1;
						if (mHardpluse[i] > maxVal)
							mHardpluse[i] = maxVal;
					}
					mApp.mRadarDevice.setHardplusValusOnly(mHardpluse);

					// mainActivity.mScanView.invalidate();
				}
			}
			// ???? ????????
			if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
				// ????????????????
				if (mApp.mRadarDevice.isSetting_Singlepos_Command()) {
					val = mApp.mRadarDevice.getSignalpos();
					if (mKeyF3Down)
						val = val + 10;
					else
						;

					mApp.mRadarDevice.setSignalposValueOnly(val);
					// DebugUtil.i(TAG,"00????????="+mApp.mRadarDevice.getSignalpos());

					if (event.getRepeatCount() > 10) {
						DebugUtil.i(TAG, ">10,down????=" + event.getRepeatCount());
						DebugUtil.i("DataTime", "Data1.time in");
						mRadarParamAdapter.notifyDataSetChanged();
						DebugUtil.i("DataTime", "Data6.time out");
						val = val + 10;
						mApp.mRadarDevice.setSignalposValueOnly(val);
						mRadarParamAdapter.notifyDataSetChanged();
						return true;
					} else {
						DebugUtil.i(TAG, "down????=" + event.getRepeatCount());
						val = val + 1;
						mApp.mRadarDevice.setSignalposValueOnly(val);
						mRadarParamAdapter.notifyDataSetChanged();
						return true;
					}
				}
				// ????????????,??????????????????????????up????????
				if (mApp.mRadarDevice.isSetting_Timewindow_Command()) {
					val = mApp.mRadarDevice.getTimeWindow();
					if (mKeyF3Down)
						val = val + 10;
					else
						val = val + 1;
					mApp.mRadarDevice.setTimeWindow(val, false);
					mRadarParamAdapter.notifyDataSetChanged();
					// mRadarParamAdapter.updateParam(mexpListView);

					mApp.mTimewndRuler.invalidate();
					mApp.mDeepRuler.invalidate();
				}
				// ????????????
				if (mApp.mRadarDevice.isSetting_Scanspeed_Command()) {
					val = mApp.mRadarDevice.getScanspeedIndexFromeValue(mApp.mRadarDevice.getScanSpeed());
					DebugUtil.i(TAG, "??????????val=" + val);
					// if(mKeyF3Down)
					// val = val+10;
					// else
					// val = val+1;
					// mApp.mRadarDevice.setContinueScanSpeed(val);
					val++;
					if (val >= mApp.mRadarDevice.getSpeedTotalSels())
						val = mApp.mRadarDevice.getSpeedTotalSels() - 1;

					mApp.mRadarDevice.setScanSpeed(val);
					mRadarParamAdapter.notifyDataSetChanged();
					// /2016.6.10
					if (mApp.mRadarDevice.isWhellMode()) {
						setNowSpeedRange();
					} else
						;
				}
				// ??????????????????
				if (mApp.mRadarDevice.isSetting_Scanlength_Command()) {
					val = mApp.mRadarDevice.getScanLengthSel();
					val = val + 1;
					if (val >= mApp.mRadarDevice.getScanLengthTotalSels())
						val = mApp.mRadarDevice.getScanLengthTotalSels() - 1;
					// mApp.mRadarDevice.setScanLengthValue(val); //2016.6.10
					mApp.mRadarDevice.setScanLength(val); // 2016.6.10
					mRadarParamAdapter.notifyDataSetChanged();
				}

				// //????????????????
				// if(mApp.mRadarDevice.isSetting_StepHardPlus_Command())
				// {
				// mainActivity.mScanView.manageKeyUp();
				// }

				// ????????
				if (mApp.mRadarDevice.isSetting_Filter_Command()) {
					val = mApp.mRadarDevice.getFilterSel();
					val = val + 1;
					if (val >= mApp.mRadarDevice.getFilterTotalSels())
						val = mApp.mRadarDevice.getFilterTotalSels() - 1;
					mApp.mRadarDevice.setFilter(val);
					mApp.mRadarDevice.setFilterParams(); // 2016.6.10
					mRadarParamAdapter.notifyDataSetChanged();
				}
				// ????????????????
				if (mApp.mRadarDevice.isSetting_JieDianConst_Command()) {
					float valf = mApp.mRadarDevice.getJieDianConst();
					if (mKeyF3Down) {
						valf += 1.0;
					} else {
						valf += 0.1;
					}
					BigDecimal b = new BigDecimal(valf);
					valf = b.setScale(2, BigDecimal.ROUND_HALF_DOWN).floatValue();// ????????
					mApp.mRadarDevice.setJieDianConst(valf);
					mRadarParamAdapter.notifyDataSetChanged();
					//
					mApp.mTimewndRuler.invalidate();
					mApp.mDeepRuler.invalidate();
				}
				if (mApp.mRadarDevice.isSetting_AveScan_Command()) {
					val = mApp.mRadarDevice.getAveNumber();
					if (mKeyF3Down) {
						val += 10;
					} else {
						val += 1;
					}
					// ????????????
					if (val > 500) {
						val = 500;
					} else if (val < 0) {
						val = 1;
					}

					mApp.mRadarDevice.setScanAve(val);
					mRadarParamAdapter.notifyDataSetChanged();
				}
				// ????
				if (mApp.mRadarDevice.isSetting_DianCe_Command()) {
					val = mApp.mRadarDevice.getDianceNumber();

					if (mKeyF3Down) {
						val += 10;
					} else {
						val += 1;
					}

					// ??????????500
					if (val > 32768)
						val = 32768;
					else
						;

					mApp.mRadarDevice.setDianceMode(val);
					mRadarParamAdapter.notifyDataSetChanged();
				}
				// ????
				if (mApp.mRadarDevice.isSetting_WhellMode_Command()) {
					return false;
				}
				// ??????????????
				if (mApp.mRadarDevice.isSetting_SelectColor_Command()) {
					int sel;
					sel = mApp.mColorPal.getColpalIndex();
					sel = sel - 1;
					if (sel < 0)
						sel = 0;
					mApp.mColorPal.setColorpalIndex(sel);
				}
				// ????????????????1
				else if (mApp.mRadarDevice.isSetting_ExtendNum_Command()) {
					int num = mApp.mRadarDevice.getWheelExtendNumber();
					if (this.mKeyF3Down) {
						num += 10;
					} else
						num++;

					if (num > 5000)
						num = 5000;
					else
						;

					mApp.mRadarDevice.setWheelExtendNumber(num);
					mRadarParamAdapter.notifyDataSetChanged();
				}
				// ????????????
				// else if(mApp.mRadarDevice.isSetting_StepHardPlus_Command())
				// {
				// mainActivity.mScanView.manageKeyUp();
				// }
			}
			// ????
			if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
				// ????????????
				if (mApp.mRadarDevice.isSetting_Singlepos_Command()) {
					val = mApp.mRadarDevice.getSignalpos();
					if (mKeyF3Down)
						val = val - 10;
					else
						;
					if (event.getRepeatCount() > 10) {
						DebugUtil.i(TAG, ">10,down????=" + event.getRepeatCount());
						DebugUtil.i("DataTime", "Data1.time in");
						mRadarParamAdapter.notifyDataSetChanged();
						DebugUtil.i("DataTime", "Data6.time out");
						val = val - 10;
						mApp.mRadarDevice.setSignalposValueOnly(val);
						mRadarParamAdapter.notifyDataSetChanged();
						return true;
					} else {
						DebugUtil.i(TAG, "down????=" + event.getRepeatCount());
						val = val - 1;
						mApp.mRadarDevice.setSignalposValueOnly(val);
						mRadarParamAdapter.notifyDataSetChanged();
						return true;
					}
				}
				// ????????
				if (mApp.mRadarDevice.isSetting_Timewindow_Command()) {
					val = mApp.mRadarDevice.getTimeWindow();
					if (mKeyF3Down)
						val = val - 10;
					else
						val = val - 1;

					mApp.mRadarDevice.setTimeWindow(val, false);
					mRadarParamAdapter.notifyDataSetChanged();

					mApp.mTimewndRuler.invalidate();
					mApp.mDeepRuler.invalidate();
				}
				// ??????????
				if (mApp.mRadarDevice.isSetting_Scanspeed_Command()) {
					val = mApp.mRadarDevice.getScanspeedIndexFromeValue(mApp.mRadarDevice.getScanSpeed());
					// if(mKeyF3Down)
					// val = val-10;
					// else
					// val = val-1;
					// if(val<=1)
					// val = 1;
					val--;
					if (val < 0)
						val = 0;
					mApp.mRadarDevice.setScanSpeed(val);
					mRadarParamAdapter.notifyDataSetChanged();
					// /2016.7.2
					if (mApp.mRadarDevice.isWhellMode()) {
						setNowSpeedRange();
					} else
						;
				}
				// ??????
				if (mApp.mRadarDevice.isSetting_Scanlength_Command()) {
					val = mApp.mRadarDevice.getScanLengthSel();
					val = val - 1;
					if (val <= 0)
						val = 0;
					// mApp.mRadarDevice.setScanLengthValue(val); //2016.6.10

					mApp.mRadarDevice.setScanLength(val); // 2016.6.10
					mRadarParamAdapter.notifyDataSetChanged();
				}

				// //????????????????
				// if(mApp.mRadarDevice.isSetting_StepHardPlus_Command())
				// {
				// mainActivity.mScanView.manageKeyDown();
				// }

				// ????????
				if (mApp.mRadarDevice.isSetting_Filter_Command()) {
					val = mApp.mRadarDevice.getFilterSel();
					val = val - 1;
					if (val <= 0)
						val = 0;
					mApp.mRadarDevice.setFilter(val);
					mApp.mRadarDevice.setFilterParams(); // 2016.6.10
					mRadarParamAdapter.notifyDataSetChanged();
				}
				// ????????????????
				if (mApp.mRadarDevice.isSetting_JieDianConst_Command()) {
					float valf = mApp.mRadarDevice.getJieDianConst();
					if (mKeyF3Down) {
						valf -= 1.0;
					} else {
						valf -= 0.1;
					}
					// if(valf<1)
					// valf = 1;
					// else if(valf>100)
					// valf = 100;

					BigDecimal b = new BigDecimal(valf);
					valf = b.setScale(2, BigDecimal.ROUND_HALF_DOWN).floatValue();// ????????
					mApp.mRadarDevice.setJieDianConst(valf);
					// ??????????mRadarDevice??????????
					mRadarParamAdapter.notifyDataSetChanged();

					mApp.mTimewndRuler.invalidate();
					mApp.mDeepRuler.invalidate();
				}
				if (mApp.mRadarDevice.isSetting_AveScan_Command()) {
					val = mApp.mRadarDevice.getAveNumber();
					if (mKeyF3Down) {
						val -= 10;
					} else {
						val -= 1;
					}

					// ????????????
					if (val > 500) {
						val = 500;
					} else if (val < 0) {
						val = 1;
					}
					mApp.mRadarDevice.setScanAve(val);
					mRadarParamAdapter.notifyDataSetChanged();
				}

				if (mApp.mRadarDevice.isSetting_DianCe_Command()) {
					val = mApp.mRadarDevice.getDianceNumber();
					if (mKeyF3Down) {
						val -= 10;
					} else {
						val -= 1;
					}
					if (val <= 1)
						val = 1;
					mApp.mRadarDevice.setDianceMode(val);
					mRadarParamAdapter.notifyDataSetChanged();
				}
				if (mApp.mRadarDevice.isSetting_WhellMode_Command()) {
					return false;
				}
				// ??????????????
				if (mApp.mRadarDevice.isSetting_SelectColor_Command()) {
					int sel;
					sel = mApp.mColorPal.getColpalIndex();
					int num = mApp.mColorPal.getColorPalNumber();
					sel = sel + 1;
					if (sel >= num)
						sel = num - 1;
					mApp.mColorPal.setColorpalIndex(sel);
				} else if (mApp.mRadarDevice.isSetting_ExtendNum_Command()) {
					int num = mApp.mRadarDevice.getWheelExtendNumber();

					if (this.mKeyF3Down) {
						num -= 10;
					} else {
						num -= 1;
					}

					// ????????
					if (num <= 0) {
						num = 1;
					} else
						;

					mApp.mRadarDevice.setWheelExtendNumber(num);
					mRadarParamAdapter.notifyDataSetChanged();
				}
				// ????????????
				// else if(mApp.mRadarDevice.isSetting_StepHardPlus_Command())
				// {
				// mKCount++;
				// if(mKCount>2)
				// mKCount %=2;
				// else;
				// mcurrent[mKCount] = System.currentTimeMillis();
				// long intertemp =
				// Math.abs(mcurrent[mKCount]-mcurrent[Math.abs(mKCount-1)]);
				// DebugUtil.i(KTAG,
				// "Left mListViewOnKeyListener ActionDown,????="+intertemp);
				// if(intertemp < 100)
				// return true;
				// else;
				// mainActivity.mScanView.manageKeyDown();
				// }
			}
		}
		// ---------------------------------------------------------------------------///
		// ????
		if (event.getAction() == KeyEvent.ACTION_UP) {
			DebugUtil.i(KTAG, "manageOnKey_ForSettingCommand,UpKey:=" + keyCode);
			mApp.setRealThreadReadingDatas(true);
			mainActivity = (MultiModeLifeSearchActivity) getActivity();

			// ???? ????????
			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				// ????????????????
				if (mApp.mRadarDevice.isSetting_StepHardPlus_Command()) {
					mainActivity.mScanView.manageKeyLeft(0, true);
				}

				// ????????????????
				if (mApp.mRadarDevice.isSetting_AllHardPlus_Command()) {
					float[] mHardpluse = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
					mHardpluse = mApp.mRadarDevice.getHardplus();
					mApp.mRadarDevice.setHardplus(mHardpluse);
					// mainActivity.mScanView.invalidate();
				}
			}// ????????????????
				// ???? ????????
			else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				// ????????????????
				if (mApp.mRadarDevice.isSetting_StepHardPlus_Command()) {
					mainActivity.mScanView.manageKeyRight(0, true);
				}

				// ????????????????
				if (mApp.mRadarDevice.isSetting_AllHardPlus_Command()) {
					float[] mHardpluse = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
					mHardpluse = mApp.mRadarDevice.getHardplus();
					mApp.mRadarDevice.setHardplus(mHardpluse);
					// mainActivity.mScanView.invalidate();
				}
			}// ????????????????
				// ????????????
			else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
				// ????????????
				if (mApp.mRadarDevice.isSetting_Timewindow_Command()) {
					val = mApp.mRadarDevice.getTimeWindow();
					mApp.mRadarDevice.setTimeWindow(val, true);
					// mRadarParamAdapter.notifyDataSetChanged();

					mApp.mTimewndRuler.invalidate();
					mApp.mDeepRuler.invalidate();
				}
				// ??????????????
				else if (mApp.mRadarDevice.isSetting_Scanspeed_Command()) {
					val = mApp.mRadarDevice.getScanSpeed();

					mApp.mRadarDevice.setContinueScanSpeed(val);
					// mRadarParamAdapter.notifyDataSetChanged();
				}
				// ??????????????????????????????????
				else if (mApp.mRadarDevice.isSetting_Singlepos_Command()) {
					val = mApp.mRadarDevice.getSignalpos();
					mApp.mRadarDevice.setSignalpos(val);
					// mRadarParamAdapter.notifyDataSetChanged();
				} else if (mApp.mRadarDevice.isSetting_ExtendNum_Command()) {
					if (!mApp.mRadarDevice.setWheelMode(mApp.mRadarDevice.getWheelExtendNumber()))
						showToastMsg("????????????????!");
					else
						;

					// mApp.setRealThreadReadingDatas(true);
					mApp.mRadarDevice.setBackFillPos(0);
					// setManuWhellParams();

					((HRulerView) (mApp.mHorRuler)).setShowdistanceMode();
					setNowSpeedRange();
				}
				// ????????????
				else if (mApp.mRadarDevice.isSetting_StepHardPlus_Command()) {
					mainActivity.mScanView.manageKeyUp();
				}
			}
			// ????????????
			else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
				// ????????
				if (mApp.mRadarDevice.isSetting_Timewindow_Command()) {
					val = mApp.mRadarDevice.getTimeWindow();

					mApp.mRadarDevice.setTimeWindow(val, true);
					// mRadarParamAdapter.notifyDataSetChanged();
					mApp.mTimewndRuler.invalidate();
					mApp.mDeepRuler.invalidate();
				}
				// ??????????
				else if (mApp.mRadarDevice.isSetting_Scanspeed_Command()) {
					val = mApp.mRadarDevice.getScanSpeed();
					mApp.mRadarDevice.setContinueScanSpeed(val);
					// mRadarParamAdapter.notifyDataSetChanged();
				}
				// ????????????
				else if (mApp.mRadarDevice.isSetting_Singlepos_Command()) {
					val = mApp.mRadarDevice.getSignalpos();
					mApp.mRadarDevice.setSignalpos(val);
					// mRadarParamAdapter.notifyDataSetChanged();
				} else if (mApp.mRadarDevice.isSetting_ExtendNum_Command()) {
					if (!mApp.mRadarDevice.setWheelMode(mApp.mRadarDevice.getWheelExtendNumber()))
						showToastMsg("????????????????!");
					else
						;

					// mApp.setRealThreadReadingDatas(true);
					mApp.mRadarDevice.setBackFillPos(0);
					((HRulerView) (mApp.mHorRuler)).setShowdistanceMode();
					setNowSpeedRange();
				}
				// ????????????
				else if (mApp.mRadarDevice.isSetting_StepHardPlus_Command()) {
					mainActivity.mScanView.manageKeyDown();
				}
			}
		}
		return true;
	}

	// ??????????????
	public void manage_WhellTypeSelect() {
		/*
		 * String[] whellNameStr = mApp.mRadarDevice.mWhellName; new
		 * AlertDialog.Builder(this.getActivity()) .setTitle("??????????????")
		 * .setSingleChoiceItems(whellNameStr, 0, new
		 * android.content.DialogInterface.OnClickListener() {
		 *
		 * @Override public void onClick(DialogInterface dialog, int which) {
		 * if(which == 7) {
		 *
		 * } } }).setPositiveButton("????", new
		 * android.content.DialogInterface.OnClickListener() {
		 *
		 * @Override public void onClick(DialogInterface dialog, int which) {
		 *
		 * } }).setNegativeButton("????", new
		 * android.content.DialogInterface.OnClickListener() {
		 *
		 * @Override public void onClick(DialogInterface dialog, int which) {
		 *
		 * } }).show();
		 */
		showWheelExtendWindow();
	}

	/**
	 * ????????????????????
	 */
	public View wheelExtendView;
	private PopupWindow mWheelExtendWindow;
	private int mWheelTypeNumber = 10;
	private int[] mWheeltypeRadiosID = new int[mWheelTypeNumber];
	private int mWheelSelectID = 0;

	/**
	 * ??????????????????,??????????????????????
	 */
	public void setWheelExtendWindow() {
		wheelExtendView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_whellparams, null);
		mWheelExtendWindow = new PopupWindow(wheelExtendView, 280,// LayoutParams.WRAP_CONTENT,
                                             android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		mWheelExtendWindow.setFocusable(true);

		// //??????????????
		RadioGroup radioGroup;
		RadioButton radioButton;
		int num;
		int i;
		int sel = 0;

		// //????????????????
		String txt;
		radioGroup = (RadioGroup) wheelExtendView.findViewById(R.id.radioGroupWhelltype);
		sel = mApp.getWheeltypeSel();
		num = radioGroup.getChildCount();
		for (i = 0; i < num; i++) {
			radioButton = (RadioButton) radioGroup.getChildAt(i);
			mWheeltypeRadiosID[i] = radioButton.getId();
			radioButton.setOnClickListener(mWhellMode_RadioGroupSelChangeHandler);
		}
		radioGroup.check(mWheeltypeRadiosID[sel]);

		CheckBox checkBox;
		checkBox = (CheckBox) wheelExtendView.findViewById(R.id.checkbox_turnwhell);
		mApp.getTurnWheelState();
		checkBox.setChecked(mApp.mRadarDevice.getTurnWheel());
		checkBox.setOnCheckedChangeListener(mWhellMode_CheckboxOnCheckedChangeListener);

		DebugUtil.i(TAG, "setWheelExtendWindow extendnum=" + mApp.mRadarDevice.getWheelExtendNumber());
	}

	/**
	 * ????????????????????????????
	 */
	private View mwheelCheckFileView = null;
	private PopupWindow mWheelCheckFilePopWindow = null;
	private ListView mRadarCheckFileListview = null;
	private boolean mIsNewCheckfile = false;
	private boolean mIsCustomFileEmpty = false;
	private CheckBox mCheckBox = null;
	private RadioGroup mRadioGroupPlus = null;
	private EditText mEditDiameter = null;

	public void setCheckFileWindow() {
		mwheelCheckFileView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_manualsetwheel, null);
		mRadarCheckFileListview = (ListView) mwheelCheckFileView.findViewById(R.id.listView_checkfile);

		Button bt = (Button) mwheelCheckFileView.findViewById(R.id.buttonSavecheckfileOK);
		bt.setOnClickListener(mWheelCheck_ButtonClickHandler);
		bt = (Button) mwheelCheckFileView.findViewById(R.id.buttonSavecheckfileCANCEL);
		bt.setOnClickListener(mWheelCheck_ButtonClickHandler);
		bt = (Button) mwheelCheckFileView.findViewById(R.id.buttonDeleteCheckFile);
		bt.setOnClickListener(mWheelCheck_ButtonClickHandler);

		mEditDiameter = (EditText) mwheelCheckFileView.findViewById(R.id.edittext_setdiameter);
		mEditDiameter.setOnKeyListener(mWhellMode_SetDiameterEditOnKeyListener);
		mEditDiameter.setEnabled(false);

		// 5.0628RadioGroup
		mRadioGroupPlus = (RadioGroup) mwheelCheckFileView.findViewById(R.id.radioGroupAntenPlus);
		mRadioGroupPlus.setEnabled(false);

		mRadioGroupPlus.check(mPulseCheckid);
		this.mSetPulse = this.getPlusFromRadioGroup(mPulseCheckid);
		RadioButton tempradioButton;
		int pnum = mRadioGroupPlus.getChildCount();
		for (int i = 0; i < pnum; i++) {
			tempradioButton = (RadioButton) mRadioGroupPlus.getChildAt(i);
			tempradioButton.setOnClickListener(mPulseValue_RadioGroupSelChangeHandler);
			tempradioButton.setEnabled(false);
		}

		mCheckBox = (CheckBox) mwheelCheckFileView.findViewById(R.id.checkbox_editfilename);
		mCheckBox.setOnCheckedChangeListener(mCheckboxRadarcheck_OnCheckedChangeListener);

		TextView tv_newfile = (TextView) mwheelCheckFileView.findViewById(R.id.newcheckfile);
		tv_newfile.setTextColor(Color.GRAY);
		mEditDiameter.setTextColor(Color.GRAY);

		mWheelCheckFilePopWindow = new PopupWindow(mwheelCheckFileView, 280,// LayoutParams.WRAP_CONTENT,
                                                   android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		mWheelCheckFilePopWindow.setFocusable(true);

	}

	/**
	 * ????????????????
	 */
	private List<String> checkGroups;
	ListGroupAdapter groupCheckAdapter;

	public void showCheckFileWindow() {
		DebugUtil.i(TAG, "Left showCheckFileWindow!");

		// ????????????????
		mCheckBox.setChecked(false);
		mIsNewCheckfile = false;

		// ????sd??????????????????
		String path;
		path = mApp.mRadarDevice.getParamsPath();
		List<String> dataList = new ArrayList<String>();
		if (!mApp.getCheckFileNamesFormSD(dataList, path) || dataList.size() == 0) {
		} else
			;

		// ??????????
		int i;
		int size;
		size = dataList.size();

		// ??????????
		sortFile(dataList);

		// ????????????
		checkGroups = new ArrayList<String>();

		// ????????????
		// ??????????????,??????????defcheck??????
		if (size <= 1) {
			// ????????????check????
			mApp.mRadarDevice.mWhellName[mApp.mRadarDevice.getWheeltypeSel()] = "??????????";
			this.mIsCustomFileEmpty = true;
		} else {
			this.mIsCustomFileEmpty = false;
			String fileName = null;

			for (i = 0; i < size; i++) {
				fileName = dataList.get(i);
				int index, index_d = 0;
				index = fileName.lastIndexOf('/');
				fileName = fileName.substring(index + 1);
				index_d = fileName.lastIndexOf('f');
				DebugUtil.i(TAG, "dataList fileName=" + fileName);
				if (index_d > 0)
					;
				else
					checkGroups.add(fileName);
			}
		}

		groupCheckAdapter = new ListGroupAdapter(this.mContext, checkGroups);
		mRadarCheckFileListview.setAdapter(groupCheckAdapter);
		mRadarCheckFileListview.setOnItemClickListener(mListView_RadarCheckFile_OnClickListener);

		setCheckedItem();

		View layout = this.getView();
		mWheelCheckFilePopWindow.setBackgroundDrawable(new BitmapDrawable());
		mWheelCheckFilePopWindow.showAtLocation(layout, Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);

	}

	// ????????
	private void sortFile(List<String> fileList) {
		String temp = null;
		for (int i = fileList.size() - 1; i > 0; i--) {
			for (int j = 0; j < i; j++) {
				// ????????????
				if (getFileNumber(fileList.get(j)) > getFileNumber(fileList.get(j + 1))) {
					temp = fileList.get(j + 1);
					fileList.set(j + 1, fileList.get(j));
					fileList.set(j, temp);
				} else
					;

			}
		}
	}

	// ??????????????????
	private int getFileNumber(String file) {
		int index_begin = 0;
		int index_end = 0;
		int temp_next = 0;
		int num = 0;
		index_begin = file.lastIndexOf("_");

		// ??????????????
		if (index_begin > 0) {
			index_end = file.lastIndexOf(".");
			DebugUtil.i(TAG, "index_begin=" + index_begin + ",index_end=" + index_end);
			DebugUtil.i(TAG, "Stringsub=" + file.charAt(index_begin + 1));
			temp_next = file.charAt(index_begin + 1) - '0';
			num = Integer.valueOf(file.substring(index_begin + 1, index_end));
			DebugUtil.i(TAG, "temp=" + temp_next + "num=" + num);
		} else
			;

		return num;
	}

	/**
	 * ??????????????
	 */
	public void setCheckedItem() {
		int item = 0;
		item = mApp.getCustomWheelIndex();

		String filePath = mApp.mRadarDevice.getCustomFileName();
		if (filePath != null && !groupCheckAdapter.isEmpty() && item > -1) {
			for (int i = 0; i < checkGroups.size(); i++) {
				if (filePath == groupCheckAdapter.getItem(i).toString())
					item = i;
				else
					;
			}
			groupCheckAdapter.setSelectIndex(item);// ??????????1??
			DebugUtil.i(TAG, "item=" + item);
			filePath = groupCheckAdapter.getItem(item).toString();
			DebugUtil.i(TAG, "filePath=" + filePath);
			loadWhellcheckParamsFile(filePath);
			mApp.mRadarDevice.setCustomFileName(filePath);
			int index = filePath.lastIndexOf('.');
			mApp.mRadarDevice.mWhellName[mApp.mRadarDevice.getWheeltypeSel()] = filePath.substring(0, index);
		} else
			;
		if (item > -1)
			groupCheckAdapter.setSelectIndex(item);// ??????????????
		else
			;
		mRadarParamAdapter.notifyDataSetChanged();
	}

	/**
	 * ????????????????
	 */
	private int MAXCHECKFILENUM = 5;
	private int newIndex = 0;
	public CompoundButton.OnCheckedChangeListener mCheckboxRadarcheck_OnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			// TODO Auto-generated method stub
			mIsNewCheckfile = isChecked;
			if (mIsNewCheckfile)
				mRadarCheckFileListview.setEnabled(false);
			else
				mRadarCheckFileListview.setEnabled(true);

			// ????????????????????????
			mEditDiameter.setEnabled(isChecked);
			mRadioGroupPlus.setEnabled(isChecked);
			RadioButton tempradioButton;
			int pnum = mRadioGroupPlus.getChildCount();
			for (int i = 0; i < pnum; i++) {
				tempradioButton = (RadioButton) mRadioGroupPlus.getChildAt(i);
				tempradioButton.setOnClickListener(mPulseValue_RadioGroupSelChangeHandler);
				tempradioButton.setEnabled(isChecked);
			}

			if (isChecked)
				mEditDiameter.setTextColor(Color.BLACK);
			else
				mEditDiameter.setTextColor(Color.GRAY);

			// ????????????????????????user_1??user_5??????????????????????????
			TextView tv_newfile = (TextView) mwheelCheckFileView.findViewById(R.id.newcheckfile);
			if (isChecked) {
				// ??????????????????
				// mNowItemCommandID = mApp.mRadarDevice.COMMAND_ID_CALIBRATE;
				// mApp.mRadarDevice.setNowSetting_CommandID(mNowItemCommandID);

				tv_newfile.setTextColor(Color.BLACK);
				groupCheckAdapter = new ListGroupAdapter(mContext, checkGroups);
				mRadarCheckFileListview.setAdapter(groupCheckAdapter);
				String fileNameSub = null, fileNameNew = null;
				int index_begin = 0, index_end = 0, temp_pre = 0, temp_next = 0, num = 0;
				List<Integer> fileList = new ArrayList<Integer>();
				int maxNum = 5;

				if (checkGroups.isEmpty())
					;
				else {
					getFileNumber(fileList);
				}

				for (int i = 0; i < checkGroups.size(); i++) {
					DebugUtil.i(TAG, "??????fileList=" + fileList.get(i));
				}

				// ????
				sortFile(fileList);

				for (int i = 0; i < checkGroups.size(); i++) {
					DebugUtil.i(TAG, "??????fileList=" + fileList.get(i));
				}

				// ??????????????????????????????????????????1????????????????5??????
				if (num == 0 && checkGroups.size() > 0) {
					num = fileList.get(checkGroups.size() - 1) + 1;
					if (num > MAXCHECKFILENUM)
						num %= MAXCHECKFILENUM;
					else
						;

					int i = 0;
					while (num == fileList.get(i) && i < (checkGroups.size() - 1)) {
						i++;
						num += 1;
					}

					if (i >= (MAXCHECKFILENUM - 1)) {
						num = 1;
					} else
						;
				} else if (checkGroups.size() == 0)// hss1123
				{
					num = 1;
				}

				fileNameNew = "user_" + num + ".check";
				TextView tv = (TextView) mwheelCheckFileView.findViewById(R.id.newcheckfile);
				tv.setText(fileNameNew);

				// ????????
				mApp.mRadarDevice.setCustomFileName(fileNameNew);
				DebugUtil.i("ExtendNumb", "customFileName=" + fileNameNew);

				// ??????????????
				// ????????fileList????????
				newIndex = getPosition(fileList, num);

			} else {
				tv_newfile.setTextColor(Color.GRAY);
			}
		}

		// ????????,????????
		private void sortFile(List<Integer> fileList) {
			int temp = 0;
			for (int i = checkGroups.size() - 1; i > 0; i--) {
				for (int j = 0; j < i; j++) {
					if (fileList.get(j) > fileList.get(j + 1)) {
						temp = fileList.get(j + 1);
						fileList.set(j + 1, fileList.get(j));
						fileList.set(j, temp);
					} else
						;
				}
			}
		}

		private void getFileNumber(List<Integer> fileList) {
			String fileNameSub = null;
			int index_begin = 0;
			int index_end = 0;
			int temp_next = 0;
			int num = 0;
			for (int j = 0; j < checkGroups.size(); j++) {
				fileNameSub = checkGroups.get(j);
				index_begin = fileNameSub.lastIndexOf("_");
				// ??????????????
				if (index_begin > 0) {
					index_end = fileNameSub.lastIndexOf(".");
					DebugUtil.i(TAG, "index_begin=" + index_begin + ",index_end=" + index_end);
					DebugUtil.i(TAG, "Stringsub=" + fileNameSub.charAt(index_begin + 1));
					temp_next = fileNameSub.charAt(index_begin + 1) - '0';
					DebugUtil.i(TAG, "temp=" + temp_next + "num=" + num);
					fileList.add(temp_next);
				} else
					;
			}
		}

		// ????????????????
		private int getPosition(List<Integer> fileList, int num) {
			int position = 0;
			for (int i = 0; i < checkGroups.size(); i++) {
				if (fileList.get(i) < num)
					position++;
				else
					break;
			}
			return position;
		}
	};

	/**
	 * ??????????
	 */
	public AdapterView.OnItemClickListener mListView_RadarCheckFile_OnClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// TODO Auto-generated method stub
			DebugUtil.i(TAG, "OnClickCheckFileName");
			groupCheckAdapter.setSelectIndex(position);

			if (groupCheckAdapter.mCheckIndex > -1) {
				String fileName = (String) groupCheckAdapter.getItem(groupCheckAdapter.mCheckIndex);
				DebugUtil.i("ExtendNumb", "mWheelCheck_ButtonClickHandler fileName=" + fileName);
				// ????????????????
				mApp.mRadarDevice.setCustomFileName(fileName);
				int index = fileName.lastIndexOf('.');
				mApp.mRadarDevice.mWhellName[mApp.mRadarDevice.getWheeltypeSel()] = fileName.substring(0, index);
				loadWhellcheckParamsFile(fileName);
				setManuWhellParams();
				// ??????????????????
				updateDiamPulse();
			} else
				;

			groupCheckAdapter.notifyDataSetChanged();
			mRadarParamAdapter.notifyDataSetChanged();
		}
	};

	// ????????????????????
	public void updateDiamPulse() {
		mEditDiameter.setText(String.valueOf(this.mDiameter));
		RadioButton radioButton = (RadioButton) mRadioGroupPlus.getChildAt(mPulseCheckid);
		mRadioGroupPlus.check(radioButton.getId());
	}

	/**
	 * ??????????????????
	 */
	public Button.OnClickListener mWheelCheck_ButtonClickHandler = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			DebugUtil.i(TAG, "mWheelCheck_ButtonClickHandler!");
			int id = v.getId();
			ListGroupAdapter groupAdapter = (ListGroupAdapter) mRadarCheckFileListview.getAdapter();

			switch (id) {
			case R.id.buttonSavecheckfileOK:
				// ??????????
				if (mIsNewCheckfile) {
					saveNewCheckFile();// ??????????
					loadWhellcheckParamsFile(mApp.mRadarDevice.getCustomFileName());
					// 0916
					mNowItemCommandID = 0;
					mApp.mRadarDevice.setNowSetting_CommandID(mNowItemCommandID);
					mIsCustomFileEmpty = false;// ????

					// ??????????????????
					// ????????????????????????item??????
					mApp.rememberCustomWheelIndex(newIndex);
				} else {
					if (groupAdapter.mCheckIndex != -1 && !groupAdapter.isEmpty()) {
						String fileName = (String) groupAdapter.getItem(groupAdapter.mCheckIndex);
						DebugUtil.i("ExtendNumb", "mWheelCheck_ButtonClickHandler fileName=" + fileName);
						// ????????????????
						mApp.mRadarDevice.setCustomFileName(fileName);
						loadWhellcheckParamsFile(fileName);
						mApp.rememberCustomWheelIndex(groupAdapter.mCheckIndex);
					} else
						;
				}
				// ????????
				setManuWhellParams();
				// ????????????????????????????????????????
				mApp.setRealThreadReadingDatas(false);
				mApp.mRadarDevice.stopRadar();
				if (!mApp.mRadarDevice.setWheelMode(mApp.mRadarDevice.getWheelExtendNumber()))
					showToastMsg("????????????????!");
				else
					;
				mApp.setRealThreadReadingDatas(true);
				mApp.mRadarDevice.setBackFillPos(0);
				// ????????????
				((HRulerView) (mApp.mHorRuler)).setShowdistanceMode();
				setNowSpeedRange();
				mRadarParamAdapter.notifyDataSetChanged();
				mWheelCheckFilePopWindow.dismiss();

				break;
			case R.id.buttonSavecheckfileCANCEL:
				// 0916
				// DebugUtil.infoDialog(mContext, "??????",
				// "mNowItemCommandID="+String.valueOf(mNowItemCommandID));
				// / mNowItemCommandID = 0;
				// mApp.mRadarDevice.setNowSetting_CommandID(mNowItemCommandID);
				mWheelCheckFilePopWindow.dismiss();
				break;
			case R.id.buttonDeleteCheckFile:
				// ????????
				if (!groupAdapter.isEmpty() && groupAdapter.mCheckIndex > -1) {
					DebugUtil.i(TAG, "mCheckIndex=" + String.valueOf(groupAdapter.mCheckIndex));
					String fileName = (String) groupAdapter.getItem(groupAdapter.mCheckIndex);
					DebugUtil.i(TAG, "mWheelCheck_ButtonClickHandler fileName=" + fileName);
					int size = checkGroups.size();
					deleteOneParamfile(mRadarCheckFileListview, fileName);
					size = checkGroups.size();
					// ??????????????????
					if (size > 0) {
						int item = 0;
						groupCheckAdapter.setSelectIndex(item);// ??????????????
						String filePath = groupCheckAdapter.getItem(item).toString();
						DebugUtil.i(TAG, "filePath=" + filePath);
						loadWhellcheckParamsFile(filePath);
						mApp.mRadarDevice.setCustomFileName(filePath);
						int index = filePath.lastIndexOf('.');
						mApp.mRadarDevice.mWhellName[mApp.mRadarDevice.getWheeltypeSel()] = filePath.substring(0, index);

						mIsCustomFileEmpty = false;

						if (mApp.mRadarDevice.getWheeltypeSel() == mManusetWhelltypeIndex && !mIsCustomFileEmpty) {
							bt_certain.setEnabled(true);
						} else if (mApp.mRadarDevice.getWheeltypeSel() == mManusetWhelltypeIndex && mIsCustomFileEmpty) {
							bt_certain.setEnabled(false);
						}

						mRadarParamAdapter.notifyDataSetChanged();
					} else {
						mIsCustomFileEmpty = true;
						// ????????????????
						bt_certain.setEnabled(false);
						mApp.rememberCustomWheelIndex(-1);
						mApp.mRadarDevice.mWhellName[mApp.mRadarDevice.getWheeltypeSel()] = "??????????";
						mRadarParamAdapter.notifyDataSetChanged();
					}
				} else
					;
				break;
			}
			mApp.setCustomSetting(false);
		}
	};

	/**
	 * ??????????
	 */
	public void showWheelExtendWindow() {
		View layout = this.getView();
		int xPos = layout.getWidth();
		int yPos = layout.getHeight() / 4;

		// ????????????????????radio????
		RadioGroup group;
		group = (RadioGroup) wheelExtendView.findViewById(R.id.radioGroupWhelltype);
		int sel = mApp.mRadarDevice.getWheeltypeSel();
		DebugUtil.i(TAG, "getWheeltypeSel=" + sel);
		RadioButton radio = null;
		radio = (RadioButton) group.getChildAt(sel);
		group.check(radio.getId());
		radio.requestFocus();

		DebugUtil.i("ExtendNumb", "after update radarDevice extend=" + mApp.mRadarDevice.getWheelExtendNumber());

		mWheelExtendWindow.setBackgroundDrawable(new BitmapDrawable());
		mWheelExtendWindow.showAtLocation(layout, Gravity.TOP | Gravity.LEFT, xPos, yPos);
		// resetWheelExtendWindowParams(false);

		mWheelExtendWindow.update();

		mIsSetDiameter = false;
		mIsSetPuls = false;
		mIsSetTouchdistance = false;
	}

	/**
	 * ????????????
	 */
	public void saveNewCheckFile() {
		String filePath = mApp.mRadarDevice.getCustomFileName();
		mApp.mRadarDevice.createWhellCheckFile(filePath);
		int index = filePath.lastIndexOf('.');
		mApp.mRadarDevice.mWhellName[mApp.mRadarDevice.getWheeltypeSel()] = filePath.substring(0, index);
		mRadarParamAdapter.notifyDataSetChanged();
	}

	public CheckBox.OnCheckedChangeListener mCheckboxOnCheckedChangeListener = new CheckBox.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			// TODO Auto-generated method stub
			int id;
			id = arg0.getId();
			MyApplication app;
			app = mApp;
			//
			switch (id) {
			case R.id.checkbox_turnwhell:
				app.mRadarDevice.turnWhell(arg1);
				break;
			}
		}
	};

	// //
	public EditText.OnKeyListener mEditOnKeyListener = new EditText.OnKeyListener() {
		@Override
		public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
			// TODO Auto-generated method stub
			return false;
		}
	};

	// /////////////////////////////////////////
	public int mManusetWhelltypeIndex = 5;// ????????????????
	public int mSetPulse = 2048;
	public double mSetDiameter = 637;

	// isInit:??????????????
	public void resetWheelExtendWindowParams(boolean isInit) {
		MyApplication app;
		app = mApp;
		int whellSel = app.getWheeltypeSel();

		// ??????????????
		RadioGroup radioGroup;
		radioGroup = (RadioGroup) wheelExtendView.findViewById(R.id.radioGroupWhelltype);
		radioGroup.check(mWheeltypeRadiosID[whellSel]);

		// but =
		// (Button)wheelExtendView.findViewById(R.id.button_setDiameterEnter);
		// but.setEnabled(false);
		int defExtNumber = app.mRadarDevice.getWheelExtendNumber();
		if (isInit) {
			switch (whellSel) {
			case 0:
				defExtNumber = 17;
				break;
			case 1:
				defExtNumber = 2;
				break;
			case 2:
				defExtNumber = 8;
				break;
			case 3:
				defExtNumber = 25;
				break;
			case 4:
				defExtNumber = 1;
				break;
			default:
				break;
			}
		}

		// /????????????????????????????
		int extNumber;
		extNumber = defExtNumber;

		app.mRadarDevice.setWheelExtendNumber(extNumber);

		if (whellSel > mManusetWhelltypeIndex) {
			DebugUtil.e(TAG, "resetWheelExtendWindowParams index ??????");
		} else {
			app.mRadarDevice.setWhellType(whellSel);
		}
	}

	// ??????????
	private int mPulseCheckid = 0;
	private View.OnClickListener mPulseValue_RadioGroupSelChangeHandler = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			int checkedId = v.getId();

			switch (checkedId) {
			case R.id.mc100:
				mPulseCheckid = 0;
				break;
			case R.id.mc500:
				mPulseCheckid = 1;
				break;
			case R.id.mc1024:
				mPulseCheckid = 2;
				break;
			case R.id.mc2048:
				mPulseCheckid = 3;
				break;
			default:
				break;
			}
			mApp.mRadarDevice.setmPulseIndex(mPulseCheckid);
			mSetPulse = getPlusFromRadioGroup(mPulseCheckid);
			mApp.mRadarDevice.setmPulse(mSetPulse);
			DebugUtil.i(TAG, "mPulseValue_RadioGroupSelChangeHandler");
		}
	};

	// ????????????
	public int getPulseByIndex() {
		int pulse = 0;
		switch (mPulseCheckid) {
		case 0:
			pulse = 100;
			break;
		case 1:
			pulse = 500;
			break;
		case 2:
			pulse = 1024;
			break;
		case 3:
			pulse = 2048;
			break;
		default:
			break;
		}
		return pulse;
	}

	// ??????????_????????????????????????
	private View.OnClickListener mWhellMode_RadioGroupSelChangeHandler = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			DebugUtil.i(TAG, "mWhellMode_RadioGroupSelChangeHandler");

			int defExtNumber = 1;
			int index = 0;
			int checkedId = v.getId();

			switch (checkedId) {
			case R.id.radioWhelltype2:
				index = 0;
				break;
			case R.id.radioWhelltype3:
				index = 1;
				break;
			case R.id.radioWhelltype3old:
				index = 2;
				break;
			case R.id.radioWhelltype4:
				index = 3;
				break;
			case R.id.radioWhelltype5:
				index = 4;
				break;
			default:
				break;
			}

			switch (checkedId) {
			case R.id.radioWhelltype8:
				index = mManusetWhelltypeIndex;
				setManuWhellParams();
				mApp.mRadarDevice.setWhellType(index);
				changeToWheelMode(defExtNumber);
				// ????????????
				dismissWheelParamWindow(index);
				showCheckFileWindow();
				// ??????????????????
				mApp.setCustomSetting(true);
				break;
			default:
				// /????????????????????????????
				if (index > mApp.mRadarDevice.getWheelDefaultMaxNum()) {
					DebugUtil.e(TAG, "mWhellMode_RadioGroupSelChangeHandler index ??????");
				} else {
					mApp.mRadarDevice.setWhellType(index);
				}
				mApp.mRadarDevice.loadDefaultWhellcheckParams();
				DebugUtil.i("radarDevice", "LeftFragment check LeftFragment=" + mApp.mRadarDevice.getWheeltypeSel());
				changeToWheelMode(mApp.mRadarDevice.getWheelExtendNumber());
				dismissWheelParamWindow(index);
				break;
			}
			mRadarParamAdapter.notifyDataSetChanged();
		}

		/**
		 * ????????????????????
		 *
		 * @param index
		 */
		private void dismissWheelParamWindow(int index) {
			((HRulerView) (mApp.mHorRuler)).setShowdistanceMode();
			setNowSpeedRange();

			DebugUtil.i(TAG, "??????????=" + mApp.mRadarDevice.getWheeltypeSel() + ",index=" + index);
			mRadarParamAdapter.notifyDataSetChanged();
			mWheelExtendWindow.dismiss();
		}

		/**
		 * ??????????????????????????????????????????????????????
		 *
		 * @param defExtNumber
		 */
		private void changeToWheelMode(int defExtNumber) {
			mApp.setRealThreadReadingDatas(false);
			mApp.mRadarDevice.stopRadar();
			if (!mApp.mRadarDevice.setWheelMode(defExtNumber))
				showToastMsg("????????????????!");
			else
				;
			mApp.setRealThreadReadingDatas(true);
			mApp.mRadarDevice.setBackFillPos(0);
		}
	};

	/**
	 * ??????????????
	 */
	public CheckBox.OnCheckedChangeListener mWhellMode_CheckboxOnCheckedChangeListener = new CheckBox.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			// TODO Auto-generated method stub
			int id;
			id = arg0.getId();
			MyApplication app;
			app = mApp;

			switch (id) {
			case R.id.checkbox_turnwhell:
				app.mRadarDevice.turnWhell(arg1);
				app.rememberTurnWheel();// ????????
				break;
			}
		}
	};

	// /?????????????? Edit????????
	public boolean mIsSetDiameter = false;
	private int mDiameter = 0;
	public EditText.OnKeyListener mWhellMode_SetDiameterEditOnKeyListener = new EditText.OnKeyListener() {
		@Override
		public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
			// TODO Auto-generated method stub
			EditText txtView;
			txtView = (EditText) arg0;
			// ????????
			// int num;
			mDiameter = mApp.mRadarDevice.getmDiameter();
			txtView.setText("" + mDiameter);

			// ??????????
			if (arg1 == KeyEvent.KEYCODE_ENTER) {
				// ??????????
				if (arg2.getAction() == KeyEvent.ACTION_UP) {
					if (mIsSetDiameter) {
						mIsSetDiameter = false;
						txtView.setTextColor(Color.BLACK);
					} else {
						mIsSetDiameter = true;
						txtView.setTextColor(Color.RED);
					}
				}
				return true;
			}

			if (arg1 == KeyEvent.KEYCODE_F3) {
				if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
					mKeyF3Down = true;
				}
				if (arg2.getAction() == KeyEvent.ACTION_UP) {
					mKeyF3Down = false;
				}
			}

			return dealDiameterUpDown(arg1, arg2, txtView, mDiameter);
		}

		// ????????
		private boolean dealDiameterUpDown(int arg1, KeyEvent arg2, EditText txtView, int num) {
			// ???? ????
			if (arg1 == KeyEvent.KEYCODE_DPAD_UP) {
				if (!mIsSetDiameter)
					return false;
				if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
					if (mKeyF3Down) {
						num += 10;
					} else
						num += 1;

					// ????????
					if (num > 1000)
						num = 1000;
					else
						;
					txtView.setText("" + num);
					mApp.mRadarDevice.setmDiameter(num);
				}
				return true;
			}
			// ????????
			if (arg1 == KeyEvent.KEYCODE_DPAD_DOWN) {
				if (!mIsSetDiameter)
					return false;
				if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
					if (mKeyF3Down)
						num -= 10;
					else
						num -= 1;

					// ????????
					if (num <= 1)
						num = 1;
					else
						;

					txtView.setText("" + num);
					mApp.mRadarDevice.setmDiameter(num);
				}
				return true;
			}
			if (arg1 == KeyEvent.KEYCODE_DEL || arg1 == KeyEvent.KEYCODE_ESCAPE) {
				return true;
			}
			return false;
		}
	};

	// /???????? ?????? Edit????????
	public boolean mIsSetPuls = false;
	// /???????? ???????? Edit????????
	public boolean mIsSetTouchdistance = false;
	public EditText.OnKeyListener mWhellMode_SetTouchDistanceEditOnKeyListener = new EditText.OnKeyListener() {
		@Override
		public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
			// TODO Auto-generated method stub
			EditText txtView;
			txtView = (EditText) arg0;
			// ???? ????
			int num;
			num = getIntvalFromeEdit(txtView);
			// ???? ??????
			if (arg1 == KeyEvent.KEYCODE_ENTER) {
				// ??????????
				if (arg2.getAction() == KeyEvent.ACTION_UP) {
					if (mIsSetTouchdistance) {
						mIsSetTouchdistance = false;
						txtView.setTextColor(Color.BLACK);
					} else {
						mIsSetTouchdistance = true;
						txtView.setTextColor(Color.RED);
					}
				}

				return true;
			}
			// ???? ????
			if (arg1 == KeyEvent.KEYCODE_DPAD_UP) {
				if (!mIsSetTouchdistance)
					return false;
				//
				if (arg2.getAction() == KeyEvent.ACTION_UP) {
					num += 1;
					txtView.setText("" + num);
				}

				return true;
			}
			// ????????
			if (arg1 == KeyEvent.KEYCODE_DPAD_DOWN) {
				if (!mIsSetTouchdistance)
					return false;
				//
				if (arg2.getAction() == KeyEvent.ACTION_UP) {
					num -= 1;
					if (num <= 1)
						num = 1;
					txtView.setText("" + num);
				}

				return true;
			}
			if (arg1 == KeyEvent.KEYCODE_DEL || arg1 == KeyEvent.KEYCODE_ESCAPE) {
				return true;
			}
			return false;
		}
	};

	/**
	 * ????????????????????????????????????????????????0629
	 */
	public void setManuWhellParams() {
		// EditText edit1,edit2;
		// edit1 =
		// (EditText)wheelExtendView.findViewById(R.id.edittext_setdiameter);
		// String txt = edit1.getText().toString();
		// int length;
		// length = txt.length();
		// char val;
		// int i;
		int diameter = mApp.mRadarDevice.getmDiameter();

		/*
		 * for( i = 0 ; i < length ; i++) { val = txt.charAt(i); if( val >= '0'
		 * && val <= '9' ) { diameter = diameter*10+ (val-'0'); } }
		 */
		// mApp.mRadarDevice.setmDiameter(diameter);// ????????

		radarDevice.mWheelInterDistance[mManusetWhelltypeIndex] = 3.1415926 * diameter / (mSetPulse) / 10.;
	}

	private View.OnFocusChangeListener mListViewOnFocusChangeListener = new View.OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			// TODO Auto-generated method stub
		}
	};

	private AdapterView.OnItemSelectedListener mListViewOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			// TODO Auto-generated method stub
			int tab = mApp.getLeftFragmentTab();
			long itemResponse = System.currentTimeMillis();

			DebugUtil.i(TAG, "onItemSelected,pos:=" + position + ";mTab=" + tab + ";currentTimeMillis=" + String
                    .valueOf(itemResponse));
			// ///?? ???????? ??????
			if (tab != 2)
				return;
			//
			// DebugUtil.i(TAG,"onItemSelected,pos:="+position+";mTab="+mTab);
			// if(position == 2)
			// {
			// mexpListView.expandGroup(2);
			// }
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub
		}
	};

	// ????????????????
	public void setNowSpeedRange() {
		TextView txtView;
		String txt;
		txtView = (TextView) mApp.mMainActivity.findViewById(R.id.textview_speed);
		if (!(mApp.mRadarDevice.isWhellMode())) {
			DebugUtil.i(TAG, "??????????????");
			txt = "????????(km/h):";
			txtView.setText(txt);
			txtView.setVisibility(View.INVISIBLE);
			return;
		} else {
			DebugUtil.i(TAG, "????????????");
		}

		txtView.setVisibility(View.VISIBLE);
		int scanSpeed;
		scanSpeed = mApp.mRadarDevice.getScanSpeed();
		int speed = 0;
		float speed1 = 0;
		if (mApp.mRadarDevice.isWhellMode()) {
			double temVal;
			temVal = scanSpeed * mApp.mRadarDevice.getTouchDistance() / 100. * 3.6;
			speed = (int) temVal;
			int speed3;
			if (speed <= 0) {
				speed3 = (int) (temVal * 100);
				speed1 = (float) (speed3 / 100.);
			}
		}
		//
		if (speed > 0)
			txt = "????????(km/h):" + speed;
		else
			txt = "????????(km/h):" + speed1;
		txtView.setText(txt);
	}

	// ????????????
	public boolean mIsHadLoadWhellcheckFile = false;

	public void loadWhellcheckParamsFile(String fileName) {
		MyApplication app;
		app = mApp;
		String filePathName;
		filePathName = app.mRadarDevice.getInnerStoragePath() + app.mRadarDevice.mParamsFilefolderPath;
		filePathName += fileName;
		int type = app.mRadarDevice.getWheeltypeSel();

		// ??????????????????,????????????????????0718
		if (type != mManusetWhelltypeIndex) {
			app.mRadarDevice.loadWhellcheckParams(filePathName);
		} else {
			app.mRadarDevice.loadCustomWheelCheckParamsFile(filePathName);
			this.mPulseCheckid = app.mRadarDevice.getmPulseIndex();
			this.mSetPulse = this.getPulseByIndex();
			this.setManuWhellParams();// ??????????
			app.mRadarDevice.getTouchDistance();
			// ????????????????
			this.mPulseCheckid = app.mRadarDevice.getmPulseIndex();
			this.mDiameter = app.mRadarDevice.getmDiameter();
		}

		int extendNum = app.mRadarDevice.getWheelExtendNumber();

		for (int i = 0; i < mApp.mRadarDevice.getWheelDefaultMaxNum(); i++) {
			mWhellCheckCoeff[i] = mApp.mRadarDevice.getWhellCheckCoeff(i);
		}

		DebugUtil.i("ExtendNumb", "??????????????????=" + extendNum);

		// ??????????????????????
		updateWhellcheckParams();
		mIsHadLoadWhellcheckFile = true;
	}

	/**
	 * ??????????????????????
	 *
	 * @return
	 */
	public boolean deleteWhellCheckParamsFile(String fileName) {
		DebugUtil.i(TAG, "deleteWhellCheckParamsFile");

		// ??????????????????
		File f = new File(mApp.mRadarDevice.getInnerStoragePath() + mApp.mRadarDevice.mParamsFilefolderPath + fileName);

		if (f.exists()) {
			return (f.delete());
		} else
			return false;
	}

	public String[] loadWhellcheckFileNames() {
		MyApplication app;
		app = mApp;
		String pathName;
		pathName = android.os.Environment.getExternalStorageDirectory() + app.mRadarDevice.mParamsFilefolderPath;
		DebugUtil.i(TAG, "whellcheckfil1:" + pathName);

		File folderFile = new File(pathName);
		if (!folderFile.isDirectory())
			return null;

		File[] files = folderFile.listFiles();
		if (files == null)
			return null;
		int length = 0;
		for (File currentFile : files) {
			// ????????????????????????????
			if (currentFile.isDirectory()) {
				continue;
			} else {
				// ??????????
				String fileName = currentFile.getName();
				if (checkEndsWithInStringArray(fileName, getResources().getStringArray(R.array.fileEndingWHELLCHECK))) {
					length++;
				}
			}
		}
		if (length == 0)
			return null;
		int i = 0;
		String[] subStr = new String[length];
		for (File currentFile : files) {
			// ????????????????????????????
			if (currentFile.isDirectory()) {
				continue;
			} else {
				// ??????????
				String fileName = currentFile.getName();
				if (checkEndsWithInStringArray(fileName, getResources().getStringArray(R.array.fileEndingWHELLCHECK))) {
					// ????????????????????????????????/sdcard/111.txt??????????111.txt
					int index;
					index = currentFile.getAbsolutePath().lastIndexOf('/');
					subStr[i] = currentFile.getAbsolutePath().substring(index + 1);
					DebugUtil.i(TAG, "The " + i + "whellcheckfilename:=" + subStr[i]);
					i++;
				}
			}
		}
		return subStr;
	}

	// ??????????????????????????????
	private boolean checkEndsWithInStringArray(String checkItsEnd, String[] fileEndings) {
		for (String aEnd : fileEndings) {
			if (checkItsEnd.endsWith(aEnd))
				return true;
		}
		return false;
	}

	// public int mWhellCheckFileSel=0;
	// public String mWhellCheckFileName;
	// public boolean mIsLoadWhellcheckFile = false;
	// public void selectWhellcheckFile(String[] fileNames)
	// {
	// DebugUtil.i(TAG, "selectWhellCheckFile!");
	// MyApplication app;
	// mIsLoadWhellcheckFile = false;
	// final String[] names=fileNames;
	// app = mApp;
	// //??????????????
	// new AlertDialog.Builder(this.getActivity()).setTitle("??????????????????")
	// .setSingleChoiceItems(fileNames, mWhellCheckFileSel, new
	// android.content.DialogInterface.OnClickListener()
	// {
	// @Override
	// public void onClick(DialogInterface dialog, int which)
	// {
	// mWhellCheckFileSel = which;
	// mWhellCheckFileName = names[which];
	// }
	// }).setNeutralButton("????", new
	// android.content.DialogInterface.OnClickListener()
	// {
	// @Override
	// public void onClick(DialogInterface dialog, int which)
	// {
	// mIsLoadWhellcheckFile = true;
	// loadWhellcheckParamsFile(mWhellCheckFileName);
	// }
	// }).setPositiveButton("????", new
	// android.content.DialogInterface.OnClickListener()
	// {
	// @Override
	// public void onClick(DialogInterface dialog, int which)
	// {
	//
	// }
	// }).setNegativeButton("????", new
	// android.content.DialogInterface.OnClickListener() {
	//
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// mIsLoadWhellcheckFile = true;
	// if(deleteWhellCheckParamsFile(names[mWhellCheckFileSel]))//????????????????
	// {
	// new AlertDialog.Builder(getActivity()).setTitle("????????")
	// .setPositiveButton("????", new
	// android.content.DialogInterface.OnClickListener()
	// {
	// @Override
	// public void onClick(DialogInterface dialog, int which)
	// { }
	// })
	// .show();
	// }
	// else
	// {
	// new AlertDialog.Builder(getActivity()).setTitle("????????")
	// .setPositiveButton("????", new
	// android.content.DialogInterface.OnClickListener()
	// {
	// @Override
	// public void onClick(DialogInterface dialog, int which)
	// { }
	// })
	// .show();
	// }
	// }
	// }).show();
	// }

	// ????????radio??????????
	public int getPlusFromRadioGroup(int radioId) {
		int tempplus = 100;
		switch (radioId) {
		case 0:
			tempplus = 100;
			break;
		case 1:
			tempplus = 500;
			break;
		case 2:
			tempplus = 1024;
			break;
		case 3:
			tempplus = 2048;
			break;
		default:
			break;
		}
		return tempplus;
	}

	public int getIntvalFromeEdit(EditText edit) {
		String txt = edit.getText().toString();
		int length;
		length = txt.length();
		char val;
		int i;
		int extNumber = 0;
		for (i = 0; i < length; i++) {
			val = txt.charAt(i);
			if (val >= '0' && val <= '9') {
				extNumber = extNumber * 10 + (val - '0');
			}
		}

		return extNumber;
	}

	// ????????????????
	public View.OnClickListener mStorageSelect_OnClickHandler = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int arg1 = v.getId();

			switch (arg1) {
			case R.id.storageInner:
				selectedID = mApp.mRadarDevice.INNER_INDEX;
				break;
			case R.id.storageSdcard:
				selectedID = mApp.mRadarDevice.SDCARD_INDEX;
				break;
			case R.id.storageUSB:
				selectedID = mApp.mRadarDevice.USB_INDEX;
				break;
			}
			mApp.mRadarDevice.setStoragePath(selectedID);
			// ????????????????????????????????????
			if (judgeExistSpace()) {
				// ????????????
				mApp.mRadarDevice.saveSystemSetFile();
				mstorageSelectPopWindow.dismiss();
			} else {
				// RadioButton radioButton = null;
				// radioButton = (RadioButton)radioGroupStorage.getChildAt(0);
				// radioButton.requestFocus();
				// radioGroupStorage.check(radioButton.getId());
				// mApp.mRadarDevice.setStoragePath(0);
			}

		}
	};

	// ??????????????????????????????????
	public boolean judgeExistSpace() {
		// ??????????????
		long size[] = mApp.getSDCardMemory();
		String path = mApp.mRadarDevice.getStoragePath();

		// ??????????????????0??????????????
		if (size[1] == 0) {
			path += "/test.txt";
			File file = new File(path);
			if (file.mkdir()) {
				if (file.delete())
					;
				else
					;
				// ????????????????????
				DebugUtil.infoDialog(getActivity(), "????????????", "????????????" + size[1] + "");
				return true;
			} else {
				// ??????????
				DebugUtil.infoDialog(getActivity(), "??????????????", "????????????");
			}
			return false;
		}
		// ????????????????
		else {
			path += "/test.txt";
			File file = new File(path);
			if (file.mkdir()) {
				if (file.delete())
					;
				else
					;

				String txt = null;
				if (size[1] > 1024 * 1024 * 1024)
					txt = size[1] / 1024 / 1024 / 1024 + "G/" + size[0] / 1024 / 1024 / 1024 + "G";
				else
					txt = size[1] / 1024 / 1024 + "M/" + size[0] / 1024 / 1024 + "M";
				DebugUtil.infoDialog(getActivity(), "????????????", "????????????????/??????????:" + txt);

				return true;
			} else {
				// ??????????
				DebugUtil.infoDialog(getActivity(), "??????????????", "????????????");
			}
			return true;
		}
	}

	public View.OnClickListener mAntenfrqRadio_OnClickHandler = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int arg1 = v.getId();
			switch (arg1) {
			case R.id.radioAntenfrq_AL2000M:
				freqSelectedID = 0;
				freqStr = "AL2000M";
				break;
			case R.id.radioAntenfrq_AL1500M:
				freqSelectedID = 1;
				freqStr = "AL1500M";
				break;
			case R.id.radioAntenfrq_AL1000M:
				freqSelectedID = 2;
				freqStr = "AL1000M";
				break;
			case R.id.radioAntenfrq_GC2000M:
				freqSelectedID = 3;
				freqStr = "GC2000M";
				break;
			case R.id.radioAntenfrq_GC1500M:
				freqSelectedID = 4;
				freqStr = "GC1500M";
				break;
			case R.id.radioAntenfrq_GC900HF:
				freqSelectedID = 5;
				freqStr = "GC900HF";
				break;
			case R.id.radioAntenfrq_GC400HF:
				freqSelectedID = 6;
				freqStr = "GC400HF";
				break;
			case R.id.radioAntenfrq_GC100HF:
				freqSelectedID = 7;
				freqStr = "GC100HF";
				break;
			case R.id.radioAntenfrq_GC400M:
				freqSelectedID = 8;
				freqStr = "GC400M";
				break;
			case R.id.radioAntenfrq_GC270M:
				freqSelectedID = 9;
				freqStr = "GC270M";
				break;
			case R.id.radioAntenfrq_GC150M:
				freqSelectedID = 10;
				freqStr = "GC150M";
				break;
			case R.id.radioAntenfrq_GC100M:
				freqSelectedID = 11;
				freqStr = "GC100M";
				break;
			/*
			 * case R.id.radioAntenfrq_GC100S: freqSelectedID = 12; freqStr =
			 * "GC100S"; break;
			 */
			case R.id.radioAntenfrq_GC50M:
				freqSelectedID = 13;
				freqStr = "GC50M";
				break;
			default:
				freqSelectedID = 0;
				break;
			}
			DebugUtil.i(TAG, "0.set freqSelectedID=" + freqSelectedID);
			// ??????????????
			dialogButton();
		}
	};

	// ??????????????????
	public void updateWhellcheckParams() {
		// //??????????????????????
		MyApplication app;
		app = mApp;
		mApp.mMainActivity.setHasReceiveScansInf();

		int selType;
		selType = app.mRadarDevice.getWheeltypeSel();

		// ??????????????????????
		if (mWheelExtendWindow.isShowing()) {
			// /????????????????????????????
			int extNumber = app.mRadarDevice.getWheelExtendNumber();

			// ????????????????
			double distance;
			distance = radarDevice.mWheelInterDistance[selType] * app.mRadarDevice.getWhellCheckCoeff(selType);
			DebugUtil.i(TAG, "The whellCheckCoeff:=" + app.mRadarDevice.getWhellCheckCoeff(selType));
			// TextView tv_coeff =
			// (TextView)wheelExtendView.findViewById(R.id.tv_wheelcoeff);
			// tv_coeff.setText(String.valueOf(app.mRadarDevice.mWhellcheckCoeff[selType]));
			distance = distance * extNumber;
			distance = Math.ceil(distance * 1000);
			distance = distance / 1000;
			String txt;
			txt = "" + distance + "cm";
			// EditText txtView =
			// (EditText)wheelExtendView.findViewById(R.id.editWhelltouchdistance);
			// txtView.setText(txt);
			// DebugUtil.i(TAG,"The whellCheckCoeff:="+app.mRadarDevice.mWhellcheckCoeff[selType]);
		}
	}

	View saveRadarparamsLayout;
	EditText mRadarparamFilenameEdit;
	ListView mRadarparamFileListview;
	private PopupWindow mSaveRadarparamsPopWindow;
	public boolean mIsNewParamfile = false;

	public void setSaveRadarParamsPopwindow() {
		// ????????
		saveRadarparamsLayout = LayoutInflater.from(getActivity()).inflate(R.layout.layout_saveradarparamfile, null);
		mRadarparamFileListview = (ListView) saveRadarparamsLayout.findViewById(R.id.listView_radarparams);

		// ??????????????????????????????
		mRadarparamFileListview.setOnItemClickListener(mListView_RadarparamSaveFile_OnClickListener);

		// ????????????????????????????????
		mRadarparamFilenameEdit = (EditText) saveRadarparamsLayout.findViewById(R.id.editView_saveparamfilename);
		mRadarparamFilenameEdit.setOnKeyListener(mEditRadarparamFilename_OnKeyListener);
		saveRadarparamsLayout.setOnKeyListener(mEditRadarparamFilename_OnKeyListener);
		mRadarparamFilenameEdit.setEnabled(false);

		CheckBox checkBox;
		checkBox = (CheckBox) saveRadarparamsLayout.findViewById(R.id.checkbox_editfilename);
		checkBox.setOnCheckedChangeListener(mCheckboxRadarparamsave_OnCheckedChangeListener);

		// ????????????????
		Button but;
		but = (Button) saveRadarparamsLayout.findViewById(R.id.buttonSaveparamfileOK);
		but.setOnClickListener(mButtonRadarparamsave_OnClickListener);
		but = (Button) saveRadarparamsLayout.findViewById(R.id.buttonSaveparamfileCANCEL);
		but.setOnClickListener(mButtonRadarparamsave_OnClickListener);
		but = (Button) saveRadarparamsLayout.findViewById(R.id.buttonDeleteParamFile);
		but.setOnClickListener(mButtonRadarparamsave_OnClickListener);

		mSaveRadarparamsPopWindow = new PopupWindow(saveRadarparamsLayout, 260, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		mSaveRadarparamsPopWindow.setFocusable(true);
	}

	/**
	 * ????????????????
	 */
	public void showRadarparamsFilePopwindow() {
		View layout = this.getView();
		int xPos = layout.getWidth();
		int yPos = 0;

		// ????????
		MyApplication app;
		app = mApp;
		// ????sd??????????????????
		String path;
		path = app.mRadarDevice.getParamsPath();
		List<String> dataList = new ArrayList<String>();
		if (!app.getParamFilenamesFromeSD(dataList, path) || dataList.size() == 0) {

		}
		// ??????????
		int i;
		int size;
		List<String> groups;
		groups = new ArrayList<String>();
		size = dataList.size();
		String fileName;
		for (i = 0; i < size; i++) {
			fileName = dataList.get(i);
			int index;
			index = fileName.lastIndexOf('/');
			fileName = fileName.substring(index + 1);
			groups.add(fileName);
		}
		ListGroupAdapter groupAdapter = new ListGroupAdapter(this.mContext, groups);
		mRadarparamFileListview.setAdapter(groupAdapter);

		// ??????????????
		EditText edit = (EditText) saveRadarparamsLayout.findViewById(R.id.editView_saveparamfilename);
		int sel;
		sel = mApp.mRadarDevice.getAntenFrqSel();
		fileName = radarDevice.g_antenFrqStr[sel] + "_1";
		edit.setText(fileName);

		mSaveRadarparamsPopWindow.setBackgroundDrawable(new BitmapDrawable());
		mSaveRadarparamsPopWindow.showAtLocation(layout, Gravity.CENTER_VERTICAL | Gravity.LEFT, xPos, yPos);

		CheckBox checkBox;
		checkBox = (CheckBox) saveRadarparamsLayout.findViewById(R.id.checkbox_editfilename);
		checkBox.setChecked(false);

		mIsNewParamfile = false;
		isEditing_RadarParamFilename = false;
		mSaveRadarparamsPopWindow.update();
	}

	public AdapterView.OnItemClickListener mListView_RadarparamSaveFile_OnClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// TODO Auto-generated method stub
			DebugUtil.i(TAG, "OnClickSaveFileName");
			ListGroupAdapter adapter = (ListGroupAdapter) mRadarparamFileListview.getAdapter();
			adapter.setSelectIndex(position);
			adapter.notifyDataSetChanged();
		}
	};

	public boolean isEditing_RadarParamFilename = false;
	public int mSelectionIndex = 1;
	public View.OnKeyListener mEditRadarparamFilename_OnKeyListener = new View.OnKeyListener() {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			DebugUtil.i(TAG, "mEditRadarparamFilename_OnKeyListener,KEY:=" + keyCode);
			EditText edit = (EditText) v;
			String txt;
			txt = edit.getText().toString();
			// ??????
			if (keyCode == KeyEvent.KEYCODE_DEL)
				return true;
			if (keyCode == KeyEvent.KEYCODE_FORWARD_DEL)
				return true;
			// ??????
			if (keyCode == KeyEvent.KEYCODE_ENTER) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (isEditing_RadarParamFilename) {
						isEditing_RadarParamFilename = false;
						edit.setTextColor(Color.BLACK);
					} else {
						isEditing_RadarParamFilename = true;
						edit.setTextColor(Color.RED);
						//
						edit.setSelection(1);
					}
					return true;
				}
			}
			// ????
			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				DebugUtil.i("Left", "mEditRadarparamFilename_OnKeyListener");
				if (isEditing_RadarParamFilename) {
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						int index;
						index = mSelectionIndex - 1;
						if (index < 1)
							index = 1;
						edit.setSelection(index);
						mSelectionIndex = index;
					}
					//
					return true;
				}
			}
			// ????
			if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				if (isEditing_RadarParamFilename) {
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						int index;
						index = mSelectionIndex + 1;
						if (index > edit.getText().length())
							index = edit.getText().length();
						edit.setSelection(index);
						mSelectionIndex = index;
					}
					return true;
				}
			}
			// ????
			if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
				if (isEditing_RadarParamFilename) {
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						int index = mSelectionIndex;
						char val = txt.charAt(index - 1);// edit.getEditableText().charAt(index-1);
						if (val >= '0' && val <= '9') {
							val = (char) (val - 1);
							if (val <= '0')
								val = '0';
						}
						if (val >= 'a' && val <= 'z') {
							val = (char) (val - 1);
							if (val <= 'a')
								val = 'a';
						}
						if (val >= 'A' && val <= 'Z') {
							val = (char) (val - 1);
							if (val <= 'A')
								val = 'A';
						}
						//
						String newTxt;
						newTxt = txt.substring(0, index - 1);
						DebugUtil.i(TAG, "newTxt:=" + newTxt);
						newTxt += val;
						newTxt += txt.substring(index, txt.length());
						edit.setText(newTxt);
						edit.setSelection(index);
					}
					return true;
				}
			}
			// ????
			if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
				if (isEditing_RadarParamFilename) {
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						int index = mSelectionIndex;
						char val = edit.getEditableText().charAt(index - 1);
						if (val >= '0' && val <= '9') {
							val = (char) (val + 1);
							if (val >= '9')
								val = '9';
						}
						if (val >= 'a' && val <= 'z') {
							val = (char) (val + 1);
							if (val >= 'z')
								val = 'z';
						}
						if (val >= 'A' && val <= 'Z') {
							val = (char) (val + 1);
							if (val >= 'Z')
								val = 'Z';
						}
						//
						String newTxt;
						newTxt = txt.substring(0, index - 1);
						DebugUtil.i(TAG, "newTxt:=" + newTxt);
						newTxt += val;
						newTxt += txt.substring(index, txt.length());
						edit.setText(newTxt);
						edit.setSelection(index);
					}
					return true;
				}
			}
			return false;
		}
	};

	public void saveParams() {
		showRadarparamsFilePopwindow();
	}

	public View.OnClickListener mButtonRadarparamsave_OnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int id = v.getId();
			switch (id) {
			// ????????????
			case R.id.buttonDeleteParamFile:
				ListGroupAdapter groupAdapter = (ListGroupAdapter) mRadarparamFileListview.getAdapter();
				if (groupAdapter.mCheckIndex != -1) {
					String fileName = (String) groupAdapter.getItem(groupAdapter.mCheckIndex);
					deleteOneParamfile(mRadarparamFileListview, fileName);
				}
				break;
			// ????????????
			case R.id.buttonSaveparamfileOK:
				String path;
				path = mApp.mRadarDevice.getParamsPath();
				String fileName = "";
				String txt;
				EditText edit;
				if (mIsNewParamfile) {
					edit = (EditText) saveRadarparamsLayout.findViewById(R.id.editView_saveparamfilename);
					txt = edit.getText().toString();
					fileName = path + txt;
					saveOneParamfile(fileName);
				} else {
					groupAdapter = (ListGroupAdapter) mRadarparamFileListview.getAdapter();
					if (groupAdapter.mCheckIndex != -1) {
						txt = (String) groupAdapter.getItem(groupAdapter.mCheckIndex);
						fileName = path + txt;
						saveOneParamfile(fileName);
					}
				}
				mSaveRadarparamsPopWindow.dismiss();
				break;
			// ????
			case R.id.buttonSaveparamfileCANCEL:
				mSaveRadarparamsPopWindow.dismiss();
				break;
			}
		}
	};

	public CompoundButton.OnCheckedChangeListener mCheckboxRadarparamsave_OnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			// TODO Auto-generated method stub
			mIsNewParamfile = isChecked;
			mRadarparamFilenameEdit.setEnabled(isChecked);
		}
	};

	/*
	 * ????????????????
	 */
	public void deleteOneParamfile(ListView mListView, String fileName) {
		DebugUtil.i(TAG, "deleteOneParamFile!");
		String path;
		path = mApp.mRadarDevice.getParamsPath();
		String delFileName = path += fileName;
		File file = new File(delFileName);
		file.delete();

		ListGroupAdapter groupAdapter = (ListGroupAdapter) mListView.getAdapter();
		if (!groupAdapter.isEmpty())
			groupAdapter.deleteCheckItem();
		else
			;
		groupAdapter.notifyDataSetChanged();
	}

	public void saveOneParamfile(String fileName) {
		mApp.mRadarDevice.saveParamsFile(fileName);
	}

	/**
	 * ??????????????
	 */
	private View wheelCalibrateView;
	private PopupWindow mWheelCalibrateWindow;
	private Button bt_start, bt_stop, bt_certain, bt_cancel;
	private EditText et_distance, et_cfcontent;
	private TextView tv_distance, tv_scan, et_fileName, et_currentcfcontent;
	private double mWhellCheckCoeff[];

	public void setWheelCalibrate() {
		wheelCalibrateView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_wheelcalibrate, null);

		bt_start = (Button) wheelCalibrateView.findViewById(R.id.start);
		bt_stop = (Button) wheelCalibrateView.findViewById(R.id.stop);
		bt_stop.setEnabled(false);
		bt_certain = (Button) wheelCalibrateView.findViewById(R.id.certain);
		bt_certain.setEnabled(false);
		bt_cancel = (Button) wheelCalibrateView.findViewById(R.id.cancel);

		et_distance = (EditText) wheelCalibrateView.findViewById(R.id.edittext_distance);
		// et_extendNumber =
		// (EditText)wheelCalibrateView.findViewById(R.id.edittext_extnumber);

		et_fileName = (TextView) wheelCalibrateView.findViewById(R.id.et_wheelcalibratefilename);
		et_fileName.setEnabled(false);
		et_fileName.setText(mApp.mRadarDevice.getCustomFileName());

		tv_distance = (TextView) wheelCalibrateView.findViewById(R.id.tv_distance);
		tv_scan = (TextView) wheelCalibrateView.findViewById(R.id.tv_scan);

		et_cfcontent = (EditText) wheelCalibrateView.findViewById(R.id.et_cfcontent);
		et_currentcfcontent = (TextView) wheelCalibrateView.findViewById(R.id.tv_cfcurrent);

		bt_start.setOnClickListener(onWheelCalibrateClickListener);
		bt_stop.setOnClickListener(onWheelCalibrateClickListener);
		bt_certain.setOnClickListener(onWheelCalibrateClickListener);
		bt_cancel.setOnClickListener(onWheelCalibrateClickListener);

		et_distance.setOnKeyListener(mWheelEditOnKeyListener);
		et_cfcontent.setOnKeyListener(mWheelEditOnKeyListener);

		for (int i = 0; i < mApp.mRadarDevice.getWheelDefaultMaxNum(); i++) {
			mWhellCheckCoeff[i] = 1;
		}

		mWheelCalibrateWindow = new PopupWindow(wheelCalibrateView, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		mWheelCalibrateWindow.setFocusable(true);
		mWheelCalibrateWindow.setOnDismissListener(onDismissListener);
	}

	// ??????????????????????
	public OnDismissListener onDismissListener = new OnDismissListener() {
		@Override
		public void onDismiss() {
			// TODO Auto-generated method stub
			if (mApp.mRadarDevice.isSetting_Calibrate_Command()) {
				new AlertDialog.Builder(getActivity()).setTitle("????????????????????????????").setPositiveButton("????", new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						showWheelCalibratePopWindow();
					}
				}).show();
			} else
				;
			DebugUtil.i(TAG, "diss calibrate!");
		}
	};

	/**
	 * ????????????????????????
	 */
	public void showWheelCalibratePopWindow() {
		View layout = this.getView();
		// et_extendNumber.setText(""+mApp.mRadarDevice.getWheelExtendNumber());
		int tempType = mApp.mRadarDevice.getWheeltypeSel();
		bt_certain.setEnabled(true);
		// ????????????????
		if (tempType != mManusetWhelltypeIndex) {
			String fileName = mApp.mRadarDevice.PREFIXDEFAULTCHECKFILE + mApp.mRadarDevice.mWhellcheckFileExtname;
			et_fileName.setText(fileName);
			et_fileName.setEnabled(false);
			loadWhellcheckParamsFile(fileName);
			DebugUtil.i(TAG, "mWhellCheckCoeff[" + tempType + "]=" + mApp.mRadarDevice.getWhellCheckCoeff(tempType));
			et_currentcfcontent.setText("(" + String
                    .valueOf(mApp.mRadarDevice.getWhellCheckCoeff(tempType)) + ")");
		}
		// ??????????????
		else {
			if (!this.mIsCustomFileEmpty) {
				loadWhellcheckParamsFile(mApp.mRadarDevice.getCustomFileName());
				et_fileName.setText(mApp.mRadarDevice.getCustomFileName());
				et_currentcfcontent.setText("(" + String
                        .valueOf(mApp.mRadarDevice.getWhellCheckCoeff(tempType)) + ")");
				bt_certain.setEnabled(false);
			} else
				;
		}

		// ??????????????????????????????
		if (mApp.mRadarDevice.isSetting_Calibrate_Command())
			bt_stop.requestFocus();
		else
			;

		mWheelCalibrateWindow.setBackgroundDrawable((new BitmapDrawable()));// ????????????????????????back????????
		mWheelCalibrateWindow.setFocusable(true);
		mWheelCalibrateWindow.showAtLocation(layout, Gravity.CENTER | Gravity.CENTER, 0, 0);
		mWheelCalibrateWindow.update();
	}

	/**
	 * ??????????
	 */
	public View.OnClickListener onWheelCalibrateClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int id = v.getId();
			switch (id) {
			case R.id.start:
				/**
				 * ??????????????????????????????????????????????????
				 */
				if (!mApp.mRadarDevice.isRunningMode()) {
					android.app.AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
					builder.setTitle("??????????").setMessage("??????????????").setNegativeButton("????", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					}).show();
					mWheelCalibrateWindow.dismiss();
				} else {
					// ??????????????1
					for (int i = 0; i < mApp.mRadarDevice.getWheelDefaultMaxNum(); i++) {
						mWhellCheckCoeff[i] = 1;
					}

					// ??????????????
					mCurrentMode = mApp.mRadarDevice.getNowMode();

					bt_start.setEnabled(false);
					bt_stop.setEnabled(true);
					bt_certain.setEnabled(false);
					et_fileName.setEnabled(false);
					et_fileName.setTextColor(getResources().getColor(R.color.deep_gray));
					int type = mApp.mRadarDevice.getWheeltypeSel();
					et_cfcontent.setText("" + mWhellCheckCoeff[type]);

					// ??????????????????
					if (type == mManusetWhelltypeIndex && mIsCustomFileEmpty) {
						bt_certain.setEnabled(false);
					} else {
						bt_certain.setEnabled(true);
					}

					mApp.setRealThreadReadingDatas(false);
					mApp.mRadarDevice.stopRadar();
					mApp.mRadarDevice.setWheelMode(mApp.mRadarDevice.getWheelExtendNumber());
					mApp.setRealThreadReadingDatas(true);
					((HRulerView) (mApp.mHorRuler)).setShowdistanceMode();
					setNowSpeedRange();

					mRadarParamAdapter.notifyDataSetChanged();

					// ????????
					mThreadWhellCheck = new Thread(mThreadWhellcheckRunnable);
					mStopWhellCheckThread = false;
					mThreadWhellCheck.start();

					// ??????????????????
					mNowItemCommandID = mApp.mRadarDevice.COMMAND_ID_CALIBRATE;
					mApp.mRadarDevice.setNowSetting_CommandID(mNowItemCommandID);
				}
				break;
			case R.id.stop:
				/**
				 * ????????????????????????????,??????????????
				 */
				finishCalibrate();

				if (mApp.mRadarDevice.getWheeltypeSel() == mManusetWhelltypeIndex && mIsCustomFileEmpty) {
					bt_certain.setEnabled(false);
				} else if (mApp.mRadarDevice.getWheeltypeSel() == mManusetWhelltypeIndex && !mIsCustomFileEmpty) {
					bt_certain.setEnabled(true);
				}

				break;
			case R.id.certain:
				/**
				 * ??????????????mWhellcheckCoeff[i]????i??mWheelSelectID public boolean
				 * saveWhellcheckParams(String pathName)
				 */
				// ????????????????????
				for (int i = 0; i < mApp.mRadarDevice.getWheelDefaultMaxNum(); i++) {
					mApp.mRadarDevice.setWhellCheckCoeff(mWhellCheckCoeff[i], i);
				}
				// ??????????????????????????????????????????????????
				{
					String fileName = et_fileName.getText().toString();
					if (mApp.mRadarDevice.getWheeltypeSel() != mManusetWhelltypeIndex) {
						mApp.mRadarDevice.saveDefaultCheckParamsFile();
					} else {
						// ????????????????
						mApp.mRadarDevice.createWhellCheckFile(fileName);
					}
				}

				mRadarParamAdapter.notifyDataSetChanged();
				mWheelCalibrateWindow.dismiss();
				break;
			case R.id.cancel:
				// ??????????
				mWheelCalibrateWindow.dismiss();
				break;
			default:
				break;
			}
		}
	};

	/**
	 * ??????????????????????
	 */
	private void finishCalibrate() {
		/**
		 * ????????????????????????????,??????????????
		 */
		bt_start.setEnabled(true);
		DebugUtil.i(TAG, "bt_start=" + bt_start.getId());

		bt_stop.setEnabled(false);
		bt_certain.setEnabled(true);
		et_fileName.setEnabled(true);
		et_fileName.setTextColor(Color.BLACK);

		updateWhellCheckInfs();
		endWhellCheck();

		if (mApp.mRadarDevice.getWheeltypeSel() != mManusetWhelltypeIndex) {
			et_fileName.setEnabled(false);
			String fileName = mApp.mRadarDevice.PREFIXDEFAULTCHECKFILE + mApp.mRadarDevice.mWhellcheckFileExtname;
			et_fileName.setText(fileName);
		} else
			;

		// ????????????????????
		mNowItemCommandID = 0;
		mApp.mRadarDevice.setNowSetting_CommandID(mNowItemCommandID);

		// ????????
		mStopWhellCheckThread = true;

		DebugUtil.i(TAG, "onGroupClick,Item:=??????");
		mApp.mRadarDevice.stopRadar();
		mApp.mRadarDevice.setWheelMode(mApp.mRadarDevice.getWheelExtendNumber());
		mApp.mRadarDevice.setBackFillPos(0);

		setManuWhellParams();

		((HRulerView) (mApp.mHorRuler)).setShowdistanceMode();

		setNowSpeedRange();

		mRadarParamAdapter.notifyDataSetChanged();
	}

	/**
	 * ????????????????????????????????
	 */
	private boolean isEditStateDistance = false;
	private boolean isEditStateExtendNum = false;
	private boolean isEditStateCoeff = false;
	private int mWheelCalibrateDistance = 0;// ????????????????
	private int mWheelCalibrateFileIndex = 0;
	public EditText.OnKeyListener mWheelEditOnKeyListener = new EditText.OnKeyListener() {
		@Override
		public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
			int id = arg0.getId();
			EditText txtView = (EditText) arg0;// ????????
			int extendNum = 0;// ????????
			int temp = 0;
			int type = mApp.mRadarDevice.getWheeltypeSel();
			double coefftemp = mWhellCheckCoeff[type];
			switch (id) {
			case R.id.edittext_distance:
				/**
				 * ????????????????
				 */
				mWheelCalibrateDistance = getIntvalFromeEdit(txtView);

				/**
				 * ??????????????????????????
				 */
				// ???? ??????
				if (arg1 == KeyEvent.KEYCODE_ENTER) {
					// ??????????
					if (arg2.getAction() == KeyEvent.ACTION_UP) {
						if (isEditStateDistance) {
							isEditStateDistance = false;
							txtView.setTextColor(Color.BLACK);
						} else {
							isEditStateDistance = true;
							txtView.setTextColor(Color.RED);
						}
					}
					return true;
				}
				// ???? ????
				if (arg1 == KeyEvent.KEYCODE_DPAD_UP) {
					if (!isEditStateDistance)
						return false;

					if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
						if (mKeyF3Down) {
							// 0-10??1,10-100??10,100-1000??100,1000-10000??1000
							if (mWheelCalibrateDistance > 0 && mWheelCalibrateDistance < 100) {
								mWheelCalibrateDistance += 10;
							} else if (mWheelCalibrateDistance >= 100 && mWheelCalibrateDistance < 1000) {
								mWheelCalibrateDistance += 100;
							} else if (mWheelCalibrateDistance >= 1000 && mWheelCalibrateDistance <= 10000) {
								mWheelCalibrateDistance += 1000;
							}
						} else {
							mWheelCalibrateDistance += 1;
						}

						if (mWheelCalibrateDistance > 10000)
							mWheelCalibrateDistance = 10000;
						else
							;

						txtView.setText("" + mWheelCalibrateDistance);
					} else
						;

					return true;
				}
				// ????????
				if (arg1 == KeyEvent.KEYCODE_DPAD_DOWN) {
					if (!isEditStateDistance)
						return false;

					if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
						if (mKeyF3Down) {
							if (mWheelCalibrateDistance > 0 && mWheelCalibrateDistance <= 100) {
								mWheelCalibrateDistance -= 10;
							} else if (mWheelCalibrateDistance > 100 && mWheelCalibrateDistance <= 1000) {
								mWheelCalibrateDistance -= 100;
							} else if (mWheelCalibrateDistance > 1000 && mWheelCalibrateDistance <= 10000) {
								mWheelCalibrateDistance -= 1000;
							}
						} else {
							mWheelCalibrateDistance -= 1;
						}

						if (mWheelCalibrateDistance < 1)
							mWheelCalibrateDistance = 1;
						else
							;

						txtView.setText("" + mWheelCalibrateDistance);
					}
					// ????????????
					return true;
				}
				if (arg1 == KeyEvent.KEYCODE_DEL || arg1 == KeyEvent.KEYCODE_ESCAPE) {
					return true;
				}
				if (arg1 == KeyEvent.KEYCODE_F3) {
					if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
						mKeyF3Down = true;
					}
					if (arg2.getAction() == KeyEvent.ACTION_UP) {
						mKeyF3Down = false;
					}
				}
				break;

			case R.id.et_cfcontent:
				// ???? ??????
				if (arg1 == KeyEvent.KEYCODE_ENTER) {
					// ??????????
					if (arg2.getAction() == KeyEvent.ACTION_UP) {
						if (isEditStateCoeff) {
							isEditStateCoeff = false;
							txtView.setTextColor(Color.BLACK);
						} else {
							isEditStateCoeff = true;
							txtView.setTextColor(Color.RED);
						}
					}
					return true;
				}
				// ???? ????
				if (arg1 == KeyEvent.KEYCODE_DPAD_UP) {
					if (!isEditStateCoeff)
						return false;

					if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
						if (mKeyF3Down) {
							if (coefftemp > 0 && coefftemp < 0.1) {
								coefftemp += 0.01;
							} else if (coefftemp >= 0.1 && coefftemp < 1) {
								coefftemp += 0.1;
							} else if (coefftemp >= 1 && coefftemp < 10) {
								coefftemp += 1;
							} else if (coefftemp >= 10 && coefftemp < 100) {
								coefftemp += 10;
							} else if (coefftemp >= 100 && coefftemp < 1000) {
								coefftemp += 100;
							}
						} else
							coefftemp += 0.001;

						if (coefftemp > 1000)
							coefftemp = 1000;
						else
							;

						BigDecimal b = new BigDecimal(coefftemp);
						coefftemp = b.setScale(3, BigDecimal.ROUND_HALF_DOWN).doubleValue();// ????????
						txtView.setText("" + coefftemp);
					}
					mWhellCheckCoeff[type] = coefftemp;
					return true;
				}
				// ????????
				if (arg1 == KeyEvent.KEYCODE_DPAD_DOWN) {
					if (!isEditStateCoeff)
						return false;

					if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
						if (mKeyF3Down) {
							if (coefftemp > 0 && coefftemp <= 0.1) {
								coefftemp -= 0.01;
							} else if (coefftemp > 0.1 && coefftemp <= 1) {
								coefftemp -= 0.1;
							} else if (coefftemp > 1 && coefftemp <= 10) {
								coefftemp -= 1;
							} else if (coefftemp > 10 && coefftemp <= 100) {
								coefftemp -= 10;
							} else if (coefftemp > 100 && coefftemp <= 1000) {
								coefftemp -= 100;
							}
						} else
							coefftemp -= 0.001;

						if (coefftemp <= 0.001)
							coefftemp = 0.001;
						else
							;

						BigDecimal b = new BigDecimal(coefftemp);
						coefftemp = b.setScale(3, BigDecimal.ROUND_HALF_DOWN).doubleValue();// ????????
						txtView.setText("" + coefftemp);
					}
					mWhellCheckCoeff[type] = coefftemp;
					return true;
				}
				if (arg1 == KeyEvent.KEYCODE_DEL || arg1 == KeyEvent.KEYCODE_ESCAPE) {
					return true;
				}
				if (arg1 == KeyEvent.KEYCODE_F3) {
					if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
						mKeyF3Down = true;
					}
					if (arg2.getAction() == KeyEvent.ACTION_UP) {
						mKeyF3Down = false;
					}
				}
				break;

			default:
				break;
			}
			return false;
		}
	};

	// //????????????????????????????????????????
	private long mWheelCalibrateScans = 0;
	private double mWheelCalibratedistance = 0;

	public void updateWhellCheckInfs() {
		TextView txtView_distance, txtView_scan;
		txtView_distance = (TextView) wheelCalibrateView.findViewById(R.id.tv_distance);
		txtView_scan = (TextView) wheelCalibrateView.findViewById(R.id.tv_scan);
		int whellSel = mApp.getWheeltypeSel();
		// ????????????(??)
		mWheelCalibrateScans = mApp.mRadarDevice.getHadRcvScans();
		// double temp_interDistance =
		// radarDevice.mWheelInterDistance[whellSel];
		// int temp_extendNum = mApp.mRadarDevice.getWheelExtendNumber();

		// ????????(m)
		mWheelCalibratedistance = mWheelCalibrateScans * mApp.mRadarDevice.getWheelExtendNumber() * radarDevice.mWheelInterDistance[whellSel];
		int intDistance = (int) (mWheelCalibratedistance * 10); // ????
		mWheelCalibratedistance = ((double) intDistance) / 1000;
		mWheelCalibratedistance = ((int) (mWheelCalibratedistance * 1000)) / 1000.;

		txtView_scan.setText(String.valueOf(mWheelCalibrateScans));
		txtView_distance.setText(String.valueOf(mWheelCalibratedistance));
	}

	// ??????????????
	public void endWhellCheck() {
		int whellSel = mApp.getWheeltypeSel();
		// mApp.mRadarDevice.stopRadar();

		// ????????????????
		double result = 1.0;

		if (mWheelCalibrateScans != 0) {
			result = mWheelCalibrateDistance * 100 / (mWheelCalibrateScans * (double) mApp.mRadarDevice.getWheelExtendNumber());
			double tempResult = (result / radarDevice.mWheelInterDistance[whellSel]);
			BigDecimal b = new BigDecimal(tempResult);
			this.mWhellCheckCoeff[whellSel] = b.setScale(3, BigDecimal.ROUND_HALF_DOWN).doubleValue();// ????????
		} else
			;

		BigDecimal b = new BigDecimal(result);
		result = b.setScale(3, BigDecimal.ROUND_HALF_DOWN).doubleValue();// ????????

		// ??????????
		// DebugUtil.i(TAG,"????distance="+mWheelCalibrateDistance
		// +"??????????="+mWheelCalibrateExtendNum
		// +"??????*????????="+mWheelCalibrateScans*mWheelCalibrateExtendNum
		// +"??????="+String.valueOf(result)
		// +"??mWhellCheckCoeff["+whellSel+"]="+String.valueOf(mWhellCheckCoeff[whellSel]));

		// ????????????
		String txt = "";
		BigDecimal temp = new BigDecimal(mWhellCheckCoeff[whellSel]);
		mWhellCheckCoeff[whellSel] = temp.setScale(3, BigDecimal.ROUND_HALF_DOWN).doubleValue();// ????????
		txt += mWhellCheckCoeff[whellSel];// (double)(((int)(mWhellCheckCoeff[mWhellSelIndex]*1000))/1000);
		et_cfcontent.setText(txt);
	}

	private boolean mStopWhellCheckThread = false;
	private Thread mThreadWhellCheck = null;
	final private int MESSAGE_READSCANS = 1;
	private Runnable mThreadWhellcheckRunnable = new Runnable() {
		@Override
		public void run() {
			while (!mStopWhellCheckThread) {
				// ??????????????????????
				Message msg = new Message();
				msg.what = MESSAGE_READSCANS;
				mThreadCheckMsgHandler.sendMessage(msg);

				try {
					Thread.sleep(200);
				} catch (Exception e) {
					DebugUtil.i(TAG, e.getMessage());
				}
			}
		}
	};

	private Handler mThreadCheckMsgHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			//
			int type = msg.what;
			switch (type) {
			case MESSAGE_READSCANS:
				updateWhellCheckInfs();
				break;
			}
		}
	};

	private Stack<float[]> mHardplusRunList = new Stack<float[]>();
	private float[] mTempHardplus = new float[9]; // ????????????????????????????
	final private int MESSAGE_RUNHARDPLUS = 1;
	private Thread mThreadHardplus = null;
	// ??????????????????????
	private Runnable mThreadHardplusRunnable = new Runnable() {
		@Override
		public void run() {
			// DebugUtil.i(THREADTAG, "enter mThreadHardplusRunnable!");
			while (mApp.isRunFirstHardplusThread()) {
				// DebugUtil.i(THREADTAG, "enter isRunFirstHardplusThread!");
				while (mApp.getIsHardplusRun()) {
					// ????????????????
					// if( !mHardplusRunList.isEmpty() )
					// {
					/*
					 * DebugUtil.i(THREADTAG, "mHardplus is not empty!");
					 * //?????????????????????? try{ // synchronized(this) {
					 * DebugUtil.e("threadTAG",
					 * "??????mThreadHardplusRunnable synchronized ");
					 * mApp.mRadarDevice.setHardplus(mHardplusRunList.pop());
					 * mHardplusRunList.clear(); //mTempHardplus = null; }
					 * }catch(Exception e) { DebugUtil.e(TAG,"run hardplus "); }
					 */
					// }
					// else
					{
						DebugUtil.i(TAG, "enter mThreadHardplusRunnable !");
						try {
							Thread.sleep(100);
						} catch (Exception e) {
							DebugUtil.i(TAG, "run sleep error!" + e.getMessage());
						}
					}
				}
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					DebugUtil.i(THREADTAG, e.getMessage());
				}
			}
		}
	};

	// ????????????????
	public void printHardplusList() {
		while (!mHardplusRunList.isEmpty()) {
			float getHardplus[] = mHardplusRunList.pop();
			DebugUtil.i(TAG, "get hardplus list:");
			for (int i = 0; i < 9; i++) {
				DebugUtil.i(TAG, "stack[" + i + "]=" + getHardplus[i]);
			}
		}
	}

	// ????????????
	private Handler mThreadHardplusHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			//
			int type = msg.what;
			switch (type) {
			case MESSAGE_RUNHARDPLUS:
				// mApp.mRadarDevice.setHardplus(mHardplusRunList.pop());// 0519
				break;
			default:
				break;
			}
		}
	};

	// //2016.6.10
	public void setRadarState(boolean state) {
		this.mBoolState = state;
	}

	/**
	 * ????????
	 *
	 * @param ev
	 * @return
	 */
	public boolean dispatchKeyEvent(KeyEvent ev) {
		DebugUtil.i(KTAG, "Left dispatchKeyEvent");
		return false;
	}

	/**
	 * ????????????
	 */
	private View mUserInforView = null;
	private PopupWindow mUserInforWindow = null;

	public void setUserInforPopWindow() {
		mUserInforView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_info, null);
		TextView tv_userinfo = (TextView) mUserInforView.findViewById(R.id.content);
		tv_userinfo.setText("1.??????????????????????????????????????????????\n\n" + "2.????????????????????????????????????????????????????????????????????????????\n\n" + "3.????????????????????????????????????????????????????????????\n\n" + "4.????????????????????????????????????????+????????????????\n\n" + "5.??????????????????????????????????????????????????\n\n" + "6.??????????????????????????????????????????4008110511??v1.30beta");
		mUserInforWindow = new PopupWindow(mUserInforView, 280, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		mUserInforWindow.setFocusable(true);
	}

	public void showUserInforPopWindow() {
		View layout = this.getView();
		mUserInforWindow.setBackgroundDrawable(new BitmapDrawable());
		mUserInforWindow.showAtLocation(layout, Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
	}
}
