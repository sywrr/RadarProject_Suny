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

	// 日志文件
	private LogWriter mLog = null;

	/**
	 * 组名以及子列名
	 */
	public int mTab = 0; // 记录是哪一组0,1,2,3测试
	/*
	 * 0:雷达操作? 1:参数调节下的 雷达参数 2:参数调节下的 探测方式 3:参数调节下的 实时处理 4:参数调节下的 显示方式
	 */
	private final int RADARFREQ = 0;
	private final int RADARSET = 1;
	private final int RADARSYSTEM = 2;
	private final int RADARSERV = 3;
	private final int RADARPARAM = 0;

	/**
	 * 设置的每个布局第一层的参数内容
	 */
	private String[][] mtabFirstItems = {{"雷达操作", "参数调节", "系统设置", "服务" }, {"恢复参数", "扫描速度", "时窗设置", "采样点数", "信号位置", "自动增益", "整体增益", "分段增益", "滤波设置", "介电常数", "保存参数", "调入参数" }, {"连续测量", "人工点测", "测距仪控制" }, {"道间平均", "背景消除" }, {"伪彩色图", "堆积波形", "转换调色板" },};

	/**
	 * 题目
	 */
	private String[] mlayoutTitle = {"主菜单", "雷达参数", "探测方式", "实时处理", "显示方式" };

	/**
	 * 设置的每个布局的第二层的参数内容
	 */
	private String[][][] mtabSecondItems = {{{}, {"雷达参数", "探测方式", "实时处理", "显示方式" },
                                             // { "系统时间", "保存设置", "文件浏览", "设备信息"},
                                             { "保存设置", "背景灯" }, { "用户指南" } }, { {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {} }, { {}, {}, { "测距仪型号", "测距仪扩展", "取样间隔(cm)", "测距仪标定" }, }, { {}, {} }, { {}, {}, {} }
                                            // {{},{},{"中精度彩虹1","中精度彩虹2","中精度蓝紫灰","中精度JET","高精度1#","高精度2#","高精度3#"}},
	};

	/**
	 * 控件
	 *
	 */
	ImageView iv_state;
	TextView tv_antenna;
	/**
	 * 生成实时处理线程
	 */
	private int REALTIME_THREADMSG_READDATAS = 1; // 读取雷达数据消息
	private boolean mRealthreadStop = false; // 是否停止实时处理线程
	private long mRealthreadSleepTime = 200; // 实时处理线程休眠时间,休眠时间需要再仔细计算
	private boolean mRealthreadReadingDatas = false; // 是否开始读取雷达数据

	/**
	 * 雷达设备错误代码
	 */
	public static int RADARDEVICE_ERROR_NO = 0; // 设备错误正确
	public static int RADARDEVICE_ERROR_CHANGEMODUSBLTD = 0x1000; // 更改设备文件属性错误
	public static int RADARDEVICE_ERROR_OPEN = 0x1001; // 打开设备文件错误
	public static int RADARDEVICE_ERROR_STARTCOMMAND = 0x1002; // 发送 '开始命令' 错误
	public static int RADARDEVICE_ERROR_STOPCOMMAND = 0x1003; // 发送 '停止命令' 错误
	public static int RADARDEVICE_ERROR_CLOSE = 0x1004; // 关闭设备文件错误

	private volatile MyApplication mApp; // 全局参数
	private boolean mBoolState = false; // 获得雷达的开关状态
	private int freqSelectedID; // 天线频率选择
	private String freqStr = null;
	private int selectedID = 0; // 存储位置选择

	/**
	 * 雷达类型代码
	 */
	private final int RADAR_AL2GFRQ_SEL = 0;
	private final int RADAR_GC15FRQ_SEL = 3;

	private AntennaDevice srlport;

	// 加载驱动
	public void loadDriver() {
		mApp.mRadarDevice.loadDriver();
	}

	/**
	 * 加载驱动
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
		// //装载驱动
		try {
			Process su;
			su = Runtime.getRuntime().exec("su");
			String cmd;
			cmd = "insmod " + "/system/lib/modules/usb-skeleton.ko" + "\n" + "exit\n";
			su.getOutputStream().write(cmd.getBytes());
			if ((su.waitFor() != 0)) {
				// throw new SecurityException();
				// Toast.makeText(this, "su命令执行错误!", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
			// throw new SecurityException();
			// Toast.makeText(this, "驱动程序已经装载!", Toast.LENGTH_SHORT).show();
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
		 * /////卸载驱动程序 try { Process su; su =
		 * Runtime.getRuntime().exec("/system/bin/su"); String cmd; cmd =
		 * "rmmod " + "usb_skeleton" + "\n" + "exit\n";
		 * su.getOutputStream().write(cmd.getBytes()); if ((su.waitFor() != 0) )
		 * { // throw new SecurityException(); Toast.makeText(this, "su命令执行错误!",
		 * 1000).show(); } } catch (Exception e) { e.printStackTrace(); // throw
		 * new SecurityException(); Toast.makeText(this, "卸载驱动程序错误!",
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

		setWheelCalibrate(); // 设置校正
		setCheckFileWindow(); // 设置自定义测距轮弹出选择
		setUserInforPopWindow();// 设置用户帮助

		Button button;
		button = (Button) antenFrqView.findViewById(R.id.buttonAntenfrqParamsOK);
		button.setOnClickListener(mButtonClickHandler);
		button = (Button) antenFrqView.findViewById(R.id.buttonAntenfrqParamsCANCEL);
		button.setOnClickListener(mButtonClickHandler);

		if (mApp.mRadarDevice.loadSystemSetFile()) {
			DebugUtil.i(TAG, "加载保存设置成功！");
		} else {
			DebugUtil.i(TAG, "加载保存设置失败！");
		}

		// 在加载文件后设置存储选项
		setSelectStoragePopwindow();
		mTab = mApp.getLeftFragmentTab();

		// 设备上电
		devicePowerUp();

		// 开启硬件增益的线程
		// mThreadHardplus = new Thread(mThreadHardplusRunnable);
		// mApp.setRunFirstHardplusThread(true);
		// mThreadHardplus.start();

		srlport = new AntennaDevice();

		// 生成日志文件
		try {
			mLog = LogWriter.open();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return view;
	}

	/*
	 * 设备上电
	 */
	private void devicePowerUp() {
		mApp.mPowerDevice.AntennaPowerUp();// 天线上电
		mApp.mPowerDevice.DisplayPowerUp();// 高压置高
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

	public int mNowItemCommandID = 0; // 用来标注当前正在调整哪个命令

	/*
	 * 0:第一层
	 */
	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * android.widget.ExpandableListView.OnChildClickListener#onChildClick(android
	 * .widget.ExpandableListView, android.view.View, int, int, long)
	 * 第二层子项的单击响应函数
	 */
	private int mCurrentMode = 0;

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		// showToastMsg("onChildClick,tab=" + mTab);
		DebugUtil.i(TAG, "onChildClick,groupPosition:=" + groupPosition + ";childPosition:=" + childPosition + ";mTab:=" + mTab);
		mTab = mApp.getLeftFragmentTab();
		EditText et_wheelExtend = null;
		// 首先
		switch (mTab) {
		// 主界面
		case 0:
			// 根据条目进行跳转
			switch (groupPosition) {
			case 0:
				break;
			// 参数调节|
			case 1:
				switch (childPosition) {
				// 参数调节/雷达参数
				case 0:
					mTab = 1;
					mRadarParamAdapter = new RadarParamExpandableListAdapter(getActivity(), mtabFirstItems[mTab], mtabSecondItems[mTab], mTab);
					setExpandableListView(mexpListView);
					mexpListView.setAdapter(mRadarParamAdapter);
					break;
				// 参数调节/探测方式
				case 1:
					mTab = 2;
					mRadarParamAdapter = new RadarParamExpandableListAdapter(getActivity(), mtabFirstItems[mTab], mtabSecondItems[mTab], mTab);
					setExpandableListView(mexpListView);
					// mRadarParamAdapter.notifyDataSetChanged();
					mexpListView.setAdapter(mRadarParamAdapter);
					break;
				// 参数调节/实时处理
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
			// 系统设置
			case 2:
				switch (childPosition) {
				// 系统时间
				// case 0:
				// DebugUtil.i(TAG,"onChildClick,Item:=系统时间");
				// break;
				// 保存设置
				case 0:
					DebugUtil.i(TAG, "onChildClick,Item:=保存设置");
					try {
						mLog.print(TAG, "保存设置！");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					/**
					 * 弹框选择内存、sd卡、usb（位置不明） 并把回放的目录设置为该LtdFiles文件夹
					 */
					showSelectStoragePopwindow(v);
					break;
				// 背景灯
				case 1:
					DebugUtil.i(TAG, "背景灯开关");
					// 点击时状态反转
					if (mApp.getPowerLightState()) {
						mApp.setPowerLightState(false);
						mApp.mPowerDevice.PowerLightOff();
						DebugUtil.i(TAG, "背景灯关闭！");
					} else {
						mApp.setPowerLightState(true);
						mApp.mPowerDevice.PowerLightOn();
						DebugUtil.i(TAG, "背景灯打开!");
					}
					// 更新保存设置
					mRadarParamAdapter.notifyDataSetChanged();
					// 文件浏览
				case 2:
					DebugUtil.i(TAG, "onChildClick,Item:=文件浏览");
					break;
				// 设备信息
				case 3:
					DebugUtil.i(TAG, "onChildClick,Item:=设备信息");
					break;
				// GPS信息
				case 4:
					DebugUtil.i(TAG, "onChildClick,Item:=GPS信息");
					break;
				default:
					break;
				}
				break;
			// 服务
			case 3:
				switch (childPosition) {
				// 操作显示
				case 0:
					DebugUtil.i(TAG, "onChildClick,Item:=操作显示");
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
			break;// 无子列
		case 2:
			// {},{},{ "测距仪型号","测距仪扩展","测距仪标定" },
			switch (groupPosition) {
			case 0:
				break;
			case 1:
				break;
			case 2:
				switch (childPosition) {
				case 0:// 测距仪型号
					if (mApp.mRadarDevice.isRunningMode())
						showWheelExtendWindow();
					else
						DebugUtil.infoDialog(getActivity(), "雷达未开启", "请先开启雷达！");
					break;
				case 1:
					// 增加测距仪参数调节响应
					DebugUtil.i(TAG, "标记扩展设置！");
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
					// 测距仪标定
					// 有自定义文件
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
				 * // 丢及回退数据 2016.6.10 case 4:
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
		// 更改全局的变量状态
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
	 * 命令列表最多两层，Group是哪个组由mTab进行区分
	 */
	@Override
	public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
		// showToastMsg("onGroupClick tab=" + mTab);
		// TODO Auto-generated method stub
		TextView txtView;
		DebugUtil.i(TAG, "onGroupClick,groupPosition:=" + groupPosition + ";mTab:=" + mTab + ";id=:" + id);
		mTab = mApp.getLeftFragmentTab();
		switch (groupPosition) {
		// 第一组，可能是“雷达操作”，也可能是“恢复参数”，由mTab进行区分
		case 0:
			try {
				switch (mTab) {
				// 雷达操作
				case 0:
					mLog.print(TAG, "onGroupClick,Item:=雷达操作,mBoolState:=" + mBoolState);
					DebugUtil.i(TAG, "onGroupClick,Item:=雷达操作,mBoolState:=" + mBoolState);
					// if( !this.mBoolState ) //2016.6.10
					if (!mApp.mRadarDevice.isRunningMode()) // 2016.6.10
					{
						showAntenfrqPopwindow(v);
						// iv_state = (ImageView)findViewById(R.id.imgv_state);
						// this.iv_state.setBackgroundResource(R.drawable.greenpoint);
					}// 天线未开启，选择主频开启天线
					else {
						// 关闭雷达
						// 如果正在保存数据，停止保存
						if (mApp.mRadarDevice.isTemstopSaveMode()) {
							mApp.mRadarDevice.continueSave();
							mApp.mMainActivity.stopSave();
						}
						mApp.mMainActivity.radarStop();
						// 设备断电
						devicePowerDown();

						iv_state = (ImageView) v.findViewById(R.id.imgv_state);
						this.iv_state.setBackgroundResource(R.drawable.redpoint);

						// tv_antenna =
						// (TextView)v.findViewById(R.id.id_antenna);
						// tv_antenna.setText(String.valueOf(mApp.mRadarDevice.getAntenFrqStr()));

						// 关闭工作灯0315
						if (mApp.mPowerDevice.WorkLightOff())
							;
						else {
							DebugUtil.i(TAG, "工作灯关闭失败！");
						}
						mBoolState = false;
						DebugUtil.infoDialog(getActivity(), "参数保存", "正在保存参数，请等待5秒后再关闭电源！");
						// closeAntennaDevice();
						try {
							Thread.sleep(1000);
						} catch (Exception e) {
							DebugUtil.i(TAG, "等待保存数据异常!");
						}
					}// 关闭天线
					break;
				// 恢复参数:设置默认值
				case 1:
					mLog.print(TAG, "onGroupClick,Item:=恢复参数");
					DebugUtil.i(TAG, "onGroupClick,Item:=恢复参数");
					int index;
					index = mApp.mRadarDevice.getAntenFrqSel();
					mApp.mRadarDevice.setAntenFrq(index);// 设置天线频率
					mApp.mRadarDevice.setAntenFrqParams();
					mApp.mRadarDevice.setAntenDefaultParams(index);
					// 更新保存设置
					mRadarParamAdapter.notifyDataSetChanged();
					// 更新标尺
					mApp.mTimewndRuler.invalidate();
					mApp.mDeepRuler.invalidate();
					break;
				// 连续测量
				case 2:
					DebugUtil.i(TAG, "onGroupClick,Item:=连续测量");
					mLog.print(TAG, "onGroupClick,Item:=连续测量");
					if (!mApp.mRadarDevice.isRunningMode()) {
						DebugUtil.infoDialog(getActivity(), "雷达未开启", "请先开启雷达！");
					} else {
						mexpListView.collapseGroup(2);// 0921把测距轮控制收回来
						mApp.mRadarDevice.setTimeMode();
						mNowItemCommandID = 0;
						mApp.mRadarDevice.setNowSetting_CommandID(mNowItemCommandID);
						((HRulerView) mApp.mHorRuler).setShowscanMode();
						TextView view = (TextView) this.getActivity().findViewById(R.id.textview_speed);
						view.setVisibility(View.INVISIBLE);
					}
					break;
				// 道间平均
				case 3:
					DebugUtil.i(TAG, "onGroupClick,Item:=道间平均");
					mLog.print(TAG, "onGroupClick,Item:=道间平均");
					if (mApp.mRadarDevice.isSetting_AveScan_Command()) {
						mNowItemCommandID = 0;
					} else {
						mNowItemCommandID = mApp.mRadarDevice.COMMAND_ID_SETAVESCAN;
					}
					mApp.mRadarDevice.setNowSetting_CommandID(mNowItemCommandID);
					mKeyF3Down = false;
					break;
				// 伪彩色图
				case 4:
					DebugUtil.i(TAG, "onGroupClick,Item:=伪彩色图");
					mLog.print(TAG, "onGroupClick,Item:=伪彩色图");
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
		// 第二组，可能是 参数调节 | 扫描速度 |人工点测 | 背景消除 | 堆积波形.....,由mTab进一步区分
		case 1:
			switch (mTab) {
			// 参数调节
			case 0:
				DebugUtil.i(TAG, "onGroupClick,Item:=参数调节");
				try {
					mLog.print(TAG, "onGroupClick,Item:=参数调节");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			// 扫描速度
			case 1:
				DebugUtil.i(TAG, "onGroupClick,Item:=扫描速度");
				try {
					mLog.print(TAG, "onGroupClick,Item:=扫描速度");
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
			// 人工点测
			case 2:

				DebugUtil.i(TAG, "onGroupClick,Item:=人工点测");
				try {
					mLog.print(TAG, "onGroupClick,Item:=人工点测");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (!mApp.mRadarDevice.isRunningMode()) {
					DebugUtil.infoDialog(getActivity(), "雷达未开启", "请先开启雷达！");
					if (mApp.mRadarDevice.isSetting_DianCe_Command()) {
						mNowItemCommandID = 0;
					} else
						;
					mApp.mRadarDevice.setNowSetting_CommandID(mNowItemCommandID);
				} else {
					mexpListView.collapseGroup(2);// 0921把测距轮控制收回来
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
			// 背景消除
			case 3:
				DebugUtil.i(TAG, "onGroupClick,Item:=背景消除");
				try {
					mLog.print(TAG, "onGroupClick,Item:=背景消除");
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
			// 堆积波形
			case 4:
				DebugUtil.i(TAG, "onGroupClick,Item:=堆积波形");
				try {
					mLog.print(TAG, "onGroupClick,Item:=堆积波形");
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
		// 第三组 系统设置 | 时窗设置 | 测距仪控制 | 转换调色板
		case 2:
			switch (mTab) {
			// 系统设置
			case 0:
				DebugUtil.i(TAG, "onGroupClick,Item:=系统设置");
				try {
					mLog.print(TAG, "onGroupClick,Item:=系统设置");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			// 时窗设置
			case 1:
				DebugUtil.i(TAG, "onGroupClick,Item:=时窗设置");
				try {
					mLog.print(TAG, "onGroupClick,Item:=时窗设置");
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
			// 测距仪控制
			case 2:
				DebugUtil.i(TAG, "onGroupClick,Item:=测距仪控制");
				try {
					mLog.print(TAG, "onGroupClick,Item:=测距仪控制");
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
					DebugUtil.infoDialog(getActivity(), "雷达未开启", "请先开启雷达！");
				} else {
					mexpListView.expandGroup(groupPosition);// 像是自动开启，此处控制无效
					// showWheelExtendWindow(); 20160617

					// 开启轮测20160617
					// 得到轮测扩展值
					int extNumber = mApp.mRadarDevice.getWheelExtendNumber();

					// 设置轮测参数
					mApp.setRealThreadReadingDatas(false);
					mApp.mRadarDevice.stopRadar();

					// 设置测距轮模式
					if (!mApp.mRadarDevice.setWheelMode(extNumber))
						showToastMsg("设置轮测模式错误!");
					else
						;

					mApp.setRealThreadReadingDatas(true);
					mApp.mRadarDevice.setBackFillPos(0);
					setManuWhellParams();
					((HRulerView) (mApp.mHorRuler)).setShowdistanceMode();
					setNowSpeedRange();

					// 显示丢弃数据按钮
					mRadarParamAdapter.notifyDataSetChanged();// 20160617
					return true;
				}
				break;
			// 转换调色板
			case 4:
				DebugUtil.i(TAG, "onGroupClick,Item:=转换调色板");
				try {
					mLog.print(TAG, "onGroupClick,Item:=转换调色板");
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
		// 第三组 服务 | 采样点数
		case 3:
			switch (mTab) {
			case 0:
				break;
			case 1:
				DebugUtil.i(TAG, "onGroupClick,Item:=采样点数");
				try {
					mLog.print(TAG, "onGroupClick,Item:=采样点数");
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
		// 第4组 信号位置 |
		case 4:
			switch (mTab) {
			case 0:
				break;
			// 信号位置
			case 1:
				try {
					mLog.print(TAG, "onGroupClick,Item:=信号位置");
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
			// 第5组 自动增益 |
			switch (mTab) {
			case 0:
				break;
			// 自动增益
			case 1:
				try {
					mLog.print(TAG, "onGroupClick,Item:=自动增益");
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
		// 第6组 整体增益
		case 6:
			switch (mTab) {
			case 0:
				break;
			case 1:
				try {
					mLog.print(TAG, "onGroupClick,Item:=整体增益");
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
		// 第7组 分段增益
		case 7:
			switch (mTab) {
			case 0:
				break;
			case 1:
				try {
					mLog.print(TAG, "onGroupClick,Item:=分段增益");
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
		// 第8组 滤波
		case 8:
			switch (mTab) {
			case 0:
				break;
			case 1:
				try {
					mLog.print(TAG, "onGroupClick,Item:=滤波");
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
		// 第9组 介电常数
		case 9:
			switch (mTab) {
			case 0:
				break;
			case 1:
				try {
					mLog.print(TAG, "onGroupClick,Item:=介电常数");
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
		// 第10组 保存参数 | .....
		case 10:
			switch (mTab) {
			case 0:
				break;
			// 保存参数
			case 1:
				try {
					mLog.print(TAG, "onGroupClick,Item:=保存参数");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				saveParams();
				break;
			}
			break;
		// 第11组 装载参数 | ....
		case 11:
			switch (mTab) {
			case 0:
				break;
			// 装载参数
			case 1:
				try {
					mLog.print(TAG, "onGroupClick,Item:=装载参数");
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
	 * 设备断电
	 */
	private void devicePowerDown() {
		mApp.mPowerDevice.DSPPowerDown();
		mApp.mPowerDevice.StepPowerDown();
		mApp.mPowerDevice.DisplayPowerDown();
		mApp.mPowerDevice.AntennaPowerDown();
	}

	/**
	 * 关闭AntennaDevice的串口
	 */
	private void closeAntennaDevice() {
		srlport.closeSerialPort();
	}

	// 设置天线频率选择对话框
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
		// 取消外面点击消失功能
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

		// 根据当前的选择，设置radio选择
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

	// 设置存储位置选择对话框
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
		// 取消外面点击消失功能
		mstorageSelectPopWindow.setFocusable(true);
	}

	// 弹出存储选择窗口
	public void showSelectStoragePopwindow(View v) {
		// 根据当前的选择，设置radio选择
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
	 * 实时开启雷达,如果开启成功更改图标状态
	 *
	 * @param antenFrqIndex
	 */
	public boolean radarRealStart(int antenFrqIndex) {
		mApp.mRadarDevice.mIsUseSoftPlus = false;
		// 分时上电
		// 上电后读取串口
		// 读取天线芯片的串口信息

		try {
			devicePowerUp();
		} catch (Exception e) {
			DebugUtil.i(TAG, "分时上电sleep run fail_sleep!");
			Log.d("debug_radar", "设备上电错误");
		}

		boolean bRet = radarStart(antenFrqIndex);
		if (!bRet) {
			showToastMsg("开启雷达错误!");
			Log.d("debug_start_radar", "开启雷达错误");
		} else {
			View view1 = mexpListView.getChildAt(0);
			iv_state = (ImageView) view1.findViewById(R.id.imgv_state);
			iv_state.setBackgroundResource(R.drawable.greenpoint);
			mApp.mRadarDevice.continueShow();
			/*
			 * tv_antenna = (TextView)view1.findViewById(R.id.id_antenna);
			 * tv_antenna.setText(freqStr);
			 */
			// 开启工作灯0315
			Log.d("debug_radar", "before work light on");
			if (mApp.mPowerDevice.WorkLightOn()) {
				Log.d("debug_radar", "work light on success");
			} else {
				Log.d("debug_radar", "work light on fail");
				DebugUtil.i(TAG, "工作灯开启失败！");
			}
		}
		return bRet;
	}

	/*
	 * 开启主频读取的串口
	 */
	private void openAntennaDevice() {
		long starttime = System.currentTimeMillis();
		srlport.openSerialPort("/dev/ttySAC3", 9600, 'N', 8, 1, 0);// 开启串口
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
				showToastMsg("主频读取超时！");
				overtime = false;
				break;
			} else
				;
		}

		if (overtime) {
			String tempStr = srlport.getFreqStr();
			if (tempStr != null) {
				this.autoAntennaDialog("确定开启" + tempStr + "雷达?");
			} else {
				this.autoAntennaDialog("未检测到雷达主频！" + String.valueOf(srlport.getFreqCode()));
			}
		} else {
			this.autoAntennaDialog("未检测到雷达主频！");
		}

		DebugUtil.i("SerialPort", "用时=" + String.valueOf(time));
		DebugUtil.i("SerialPort", "FreqCode=" + String.valueOf(srlport.getFreqCode()));
		DebugUtil.i("SerialPort", "ReqFreqCode=" + String.valueOf(srlport.getRepFreqCode()));

		showToastMsg("FreqCode=" + String.valueOf(srlport.getFreqCode()));
		showToastMsg("ReqFreqCode=" + String.valueOf(srlport.getRepFreqCode()));

		srlport.closeSerialPort();
	}

	// //根据天线频率开启雷达
	public boolean radarStart(int frqIndex) {
		int ret;
		boolean bRet = false;
		// 如果雷达处于工作状态
		if (mApp.mRadarDevice.isRunningMode()) {
			DebugUtil.i(TAG, "radarStart : device has start!");
			return true;
		}
		// 装载驱动
		// loadDriver();

		// 根据选择的天线频率，装载指定的参数文件
		String fileName;
		fileName = mApp.mRadarDevice.getInnerStoragePath() + mApp.mRadarDevice.mParamsFilefolderPath;
		fileName += radarDevice.g_antenFrqStr[frqIndex] + ".par";
		// Toast.makeText(this, fileName, Toast.LENGTH_SHORT).show();

		/**
		 * 由主频参数文件设置参数
		 */
		mApp.mRadarDevice.setAntenFrq(frqIndex);
		if (!mApp.mRadarDevice.onlyLoadParamsFromeFile(fileName)) {
			// DebugUtil.i(TAG,"!!!!!~~~~Now setAntenFrq:"+frqIndex);
			// Toast.makeText(this, "~~~~Now AntenFrq:"+frqIndex,
			// Toast.LENGTH_SHORT).show();

			// 根据天线频率索引，仅仅更改参数，不发送命令
			mApp.mRadarDevice.changeParamsFromeAntenfrq(frqIndex);
		} else {
		}

		/**
		 * 由默认测距轮文件设置测距仪参数
		 */
		if (!mApp.mRadarDevice.loadDefaultWhellcheckParams()) {
			DebugUtil.i(TAG, "loadDefaultWheelCheckParams fail!");
			mApp.mRadarDevice.changeWheelPropertyFromAnteFrq(frqIndex);
		} else {
			DebugUtil.i(TAG, "loadDefaultWheelCheckParams!Success!");
		}

		DebugUtil.i(TAG, "radarstart loaddefaultcheck,extendNumber=" + mApp.mRadarDevice.getWheelExtendNumber());
		DebugUtil.i("radarDevice", "3.leftgetWheeltype" + mApp.mRadarDevice.getWheeltypeSel());
		// 更新参数设置显示值
		int nowSel = mApp.mRadarDevice.getAntenFrqSel();
		// DebugUtil.i(TAG,"!!!!!Now AntenFrq:"+nowSel);
		// Toast.makeText(this, "!!!!!~~~~Now AntenFrq:"+nowSel,
		// Toast.LENGTH_SHORT).show();
		mApp.mRadarDevice.refreshFileHeader();
		// 根据当前的雷达参数设置参数列表框内容？？？
		// changeParamsListFromeRadar();
		// hss2016.6.6
		// 更新列表
		mRadarParamAdapter.notifyDataSetChanged();
		mApp.mTimewndRuler.invalidate();
		mApp.mDeepRuler.invalidate();
		((HRulerView) mApp.mHorRuler).setShowscanMode();

		// mParamsListAdapter.notifyDataSetChanged();

		/*
		 * //装载测距轮校正参数文件 fileName =
		 * mApp.mRadarDevice.mSDCardPath+mApp.mRadarDevice
		 * .mParamsFilefolderPath; fileName +=
		 * mApp.mRadarDevice.mWhellcheckFilename;
		 * if(!mApp.mRadarDevice.loadWhellcheckParams(fileName)) {
		 *
		 * }
		 */
		// 开启雷达
		ret = mApp.mRadarDevice.start();
		if (ret == RADARDEVICE_ERROR_NO) {
			// mRealtimeDIBView.initDIB();
			// mRealtimeDIBView.invalidate();
			mRealthreadReadingDatas = true; // 设置标志,开始读取数据
			bRet = true;

			// //更新增益曲线显示放到主activity
			// scanView view = (scanView)findViewById(R.id.viewSinglewave);
			// view.invalidate();

			String name = mApp.mRadarDevice.getParamsPath() + "defSetParams.par";
			// loadSetParamsFile(name); 参数文件

			// BB80?20170419hss
			// mApp.mRadarDevice.setHandleMode();

			// iv_state.setBackgroundResource(R.drawable.greenpoint);
		} else {
			Log.d("debug_radar", "open radar failed");
			if (ret == RADARDEVICE_ERROR_OPEN) {
				showToastMsg("雷达设备打开错误!");
			}
			if (ret == RADARDEVICE_ERROR_STARTCOMMAND) {
				showToastMsg("发送开启命令错误!");
			}
			// UnloadDriver();
		}

		return bRet;
	}

	// "快捷操作视图" 按钮点击处理函数
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
	 * 对话框处理
	 */
	public void dialogButton() {
		// 弹出提示对话框
		android.app.AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle("开启雷达");
		builder.setTitle("确定开启" + freqStr + "雷达？").setMessage("确定开启").setNegativeButton("是", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// 关闭主频选择的弹出窗口
				mAntenfrqPopWindow.dismiss();
				// 设置雷达主频，发送主频以及开启雷达命令,将标志状态置为绿色
				mApp.mRadarDevice.setAntenFrq(freqSelectedID);
				// 开启雷达
				radarRealStart(freqSelectedID);
				DebugUtil.i(TAG, "finish radarRealStart!");

				// 更新最大时速状态
				setNowSpeedRange();
				mApp.setRealThreadStop(false);
				mApp.setRealThreadReadingDatas(true);
				Log.d("debug_radar", "is true");
				tv_title.setText(mlayoutTitle[mTab]);
				// 不弹出
				mBoolState = true;
			}
		}).setPositiveButton("否", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		}).show();
	}

	// 主频自动识别对话框处理
	public void autoAntennaDialog(String freqStr) {
		// 弹出提示对话框
		android.app.AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle("开启雷达");
		builder.setTitle(freqStr).setMessage("确定开启").setNegativeButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// 根据主频加载参数
			}
		}).setPositiveButton("重新检测", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		}).setNeutralButton("手动设置", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		}).show();
	}

	/**
	 * 处理按下按键的事件处理
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

		// 回退,同时保存设置的参数
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
			// 从 雷达参数 设置跳回
			case 1:
				mTab = 0;
				mApp.saveLeftFragmentTab(mTab);
				mRadarParamAdapter = new RadarParamExpandableListAdapter(lcontext, mtabFirstItems[mTab], mtabSecondItems[mTab], mTab);
				setExpandableListView(mexpListView);
				mexpListView.setAdapter(mRadarParamAdapter);
				mRadarParamAdapter.notifyDataSetChanged();
				mexpListView.expandGroup(1);
				mexpListView.setSelectedChild(1, 0, true);
				// 保存参数
				mApp.mRadarDevice.saveParamsFile(mApp.mRadarDevice.INNERSTORAGE + mApp.mRadarDevice.mParamsFilefolderPath + radarDevice.g_antenFrqStr[sel] + ".par");
				mApp.mRadarDevice.saveSystemSetFile();
				break;
			// 从 探测方式 跳回
			case 2:
				mTab = 0;
				mApp.saveLeftFragmentTab(mTab);
				mRadarParamAdapter = new RadarParamExpandableListAdapter(lcontext, mtabFirstItems[mTab], mtabSecondItems[mTab], mTab);
				setExpandableListView(mexpListView);
				mexpListView.setAdapter(mRadarParamAdapter);
				mRadarParamAdapter.notifyDataSetChanged();
				mexpListView.expandGroup(1);
				mexpListView.setSelectedChild(1, 1, true);
				// 保存参数
				// 根据选择的测距仪型号分别保存
				if (mApp.mRadarDevice.getWheeltypeSel() == mManusetWhelltypeIndex) {
					DebugUtil.i(TAG, "==mManusetWhelltypeIndex!");

					String filePath = mApp.mRadarDevice.INNERSTORAGE + mApp.mRadarDevice.mParamsFilefolderPath + mApp.mRadarDevice.getCustomFileName();
					DebugUtil.i("ExtendNumb", "filePath=" + filePath);

					// 0918注释掉
					// mApp.mRadarDevice.saveWhellcheckParams(filePath);
					// 使用自定测距轮的保存方式
					if (mApp.getCustomWheelIndex() > -1)
						mApp.mRadarDevice.saveCustomWheelExtend(filePath);
					else
						;
				} else {
					DebugUtil.i(TAG, "！=mManusetWhelltypeIndex!");
					mApp.mRadarDevice.saveDefaultCheckParamsFile();
				}
				break;
			// 从 实时处理 跳回
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
			// 从显示方式跳回
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
	 * 长按的事件响应
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

	// 调试信息输出
	public void showToastMsg(String text) {
		Toast.makeText(mContext, text, Toast.LENGTH_LONG);
	}

	/**
	 * 左键点击事件的处理
	 */
	private long tcurrent[] = new long[10];
	private int iKCount = 0;
	private long interRespon = 0;
	public View.OnKeyListener mListViewOnKeyListener = new View.OnKeyListener() {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			DebugUtil.i(KTAG, "Left mListViewOnKeyListener,keyCode:=" + keyCode + "repCount:=" + event.getRepeatCount());

			// 上键
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

					// 测试onkeydown
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						mApp.setRealTimeDraw(false);// 停止画图
						// 停止读取线程
						long ltemp = Math.abs(tcurrent[iKCount] - tcurrent[Math.abs(iKCount - 1)]);
						DebugUtil.i(KTAG, "Left mListViewOnKeyListener ActionDown,间隔=" + ltemp);
						// if(ltemp < 100)
						// return true;
						// else;0523小于100ms不响应去掉
					} else if (event.getAction() == KeyEvent.ACTION_UP) {
						mApp.setRealTimeDraw(true); // 开始画图
						// 开始读取线程
						// mApp.setRealThreadReadingDatas(true);
						long up = System.currentTimeMillis();

						long ltemp = Math.abs(up - tcurrent[iKCount]);
						DebugUtil.i(KTAG, "Left mListViewOnKeyListener ActionUp,间隔=" + ltemp);

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
			// 记录F3键的状态
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

		// 键按下事件
		if (event.getAction() == KeyEvent.ACTION_DOWN) // ACTION_UP)//ACTION_DOWN
														// )//
		{
			DebugUtil.i(KTAG, "manageOnKey_ForSettingCommand,DownKey:=" + keyCode);
			// 按下键只响应数值，将读取线程停到，抬起键时再开启线程
			mApp.setRealThreadReadingDatas(false);

			// 按下 “左键”
			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				DebugUtil.i("Left", "manageOnKey_ForSettingCommand");

				// 正在调解分段增益
				if (mApp.mRadarDevice.isSetting_StepHardPlus_Command()) {
					if (mKeyF3Down)
						mainActivity.mScanView.manageKeyLeft(10, false);
					else
						mainActivity.mScanView.manageKeyLeft(1, false);
					// mainActivity.mScanView.invalidate();
				}

				// 正在设置整体增益
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

			// 按下 “右键”
			if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)// 22)//
			{
				// 正在调解分段增益
				if (mApp.mRadarDevice.isSetting_StepHardPlus_Command()) {
					if (mKeyF3Down)
						mainActivity.mScanView.manageKeyRight(10, false);
					else
						mainActivity.mScanView.manageKeyRight(1, false);
					// mainActivity.mScanView.invalidate();
				}
				// 正在设置整体增益
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
			// 按下 “上键”
			if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
				// 正在调节信号位置
				if (mApp.mRadarDevice.isSetting_Singlepos_Command()) {
					val = mApp.mRadarDevice.getSignalpos();
					if (mKeyF3Down)
						val = val + 10;
					else
						;

					mApp.mRadarDevice.setSignalposValueOnly(val);
					// DebugUtil.i(TAG,"00信号位置="+mApp.mRadarDevice.getSignalpos());

					if (event.getRepeatCount() > 10) {
						DebugUtil.i(TAG, ">10,down次数=" + event.getRepeatCount());
						DebugUtil.i("DataTime", "Data1.time in");
						mRadarParamAdapter.notifyDataSetChanged();
						DebugUtil.i("DataTime", "Data6.time out");
						val = val + 10;
						mApp.mRadarDevice.setSignalposValueOnly(val);
						mRadarParamAdapter.notifyDataSetChanged();
						return true;
					} else {
						DebugUtil.i(TAG, "down次数=" + event.getRepeatCount());
						val = val + 1;
						mApp.mRadarDevice.setSignalposValueOnly(val);
						mRadarParamAdapter.notifyDataSetChanged();
						return true;
					}
				}
				// 正在设置时窗,只是更改数值，不发命令，在up时发命令
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
				// 正在设置扫速
				if (mApp.mRadarDevice.isSetting_Scanspeed_Command()) {
					val = mApp.mRadarDevice.getScanspeedIndexFromeValue(mApp.mRadarDevice.getScanSpeed());
					DebugUtil.i(TAG, "设置扫速度val=" + val);
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
				// 正在设置取样点长度
				if (mApp.mRadarDevice.isSetting_Scanlength_Command()) {
					val = mApp.mRadarDevice.getScanLengthSel();
					val = val + 1;
					if (val >= mApp.mRadarDevice.getScanLengthTotalSels())
						val = mApp.mRadarDevice.getScanLengthTotalSels() - 1;
					// mApp.mRadarDevice.setScanLengthValue(val); //2016.6.10
					mApp.mRadarDevice.setScanLength(val); // 2016.6.10
					mRadarParamAdapter.notifyDataSetChanged();
				}

				// //正在调解分段增益
				// if(mApp.mRadarDevice.isSetting_StepHardPlus_Command())
				// {
				// mainActivity.mScanView.manageKeyUp();
				// }

				// 正在滤波
				if (mApp.mRadarDevice.isSetting_Filter_Command()) {
					val = mApp.mRadarDevice.getFilterSel();
					val = val + 1;
					if (val >= mApp.mRadarDevice.getFilterTotalSels())
						val = mApp.mRadarDevice.getFilterTotalSels() - 1;
					mApp.mRadarDevice.setFilter(val);
					mApp.mRadarDevice.setFilterParams(); // 2016.6.10
					mRadarParamAdapter.notifyDataSetChanged();
				}
				// 正在设置介电常数
				if (mApp.mRadarDevice.isSetting_JieDianConst_Command()) {
					float valf = mApp.mRadarDevice.getJieDianConst();
					if (mKeyF3Down) {
						valf += 1.0;
					} else {
						valf += 0.1;
					}
					BigDecimal b = new BigDecimal(valf);
					valf = b.setScale(2, BigDecimal.ROUND_HALF_DOWN).floatValue();// 向下取整
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
					// 限制道间平均
					if (val > 500) {
						val = 500;
					} else if (val < 0) {
						val = 1;
					}

					mApp.mRadarDevice.setScanAve(val);
					mRadarParamAdapter.notifyDataSetChanged();
				}
				// 点测
				if (mApp.mRadarDevice.isSetting_DianCe_Command()) {
					val = mApp.mRadarDevice.getDianceNumber();

					if (mKeyF3Down) {
						val += 10;
					} else {
						val += 1;
					}

					// 点测的上限500
					if (val > 32768)
						val = 32768;
					else
						;

					mApp.mRadarDevice.setDianceMode(val);
					mRadarParamAdapter.notifyDataSetChanged();
				}
				// 轮测
				if (mApp.mRadarDevice.isSetting_WhellMode_Command()) {
					return false;
				}
				// 正在设置调色板
				if (mApp.mRadarDevice.isSetting_SelectColor_Command()) {
					int sel;
					sel = mApp.mColorPal.getColpalIndex();
					sel = sel - 1;
					if (sel < 0)
						sel = 0;
					mApp.mColorPal.setColorpalIndex(sel);
				}
				// 测距仪标记扩展加1
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
				// 调节部分增益
				// else if(mApp.mRadarDevice.isSetting_StepHardPlus_Command())
				// {
				// mainActivity.mScanView.manageKeyUp();
				// }
			}
			// 下键
			if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
				// 下键信号位置
				if (mApp.mRadarDevice.isSetting_Singlepos_Command()) {
					val = mApp.mRadarDevice.getSignalpos();
					if (mKeyF3Down)
						val = val - 10;
					else
						;
					if (event.getRepeatCount() > 10) {
						DebugUtil.i(TAG, ">10,down次数=" + event.getRepeatCount());
						DebugUtil.i("DataTime", "Data1.time in");
						mRadarParamAdapter.notifyDataSetChanged();
						DebugUtil.i("DataTime", "Data6.time out");
						val = val - 10;
						mApp.mRadarDevice.setSignalposValueOnly(val);
						mRadarParamAdapter.notifyDataSetChanged();
						return true;
					} else {
						DebugUtil.i(TAG, "down次数=" + event.getRepeatCount());
						val = val - 1;
						mApp.mRadarDevice.setSignalposValueOnly(val);
						mRadarParamAdapter.notifyDataSetChanged();
						return true;
					}
				}
				// 设置时窗
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
				// 设置扫速度
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
				// 取样点
				if (mApp.mRadarDevice.isSetting_Scanlength_Command()) {
					val = mApp.mRadarDevice.getScanLengthSel();
					val = val - 1;
					if (val <= 0)
						val = 0;
					// mApp.mRadarDevice.setScanLengthValue(val); //2016.6.10

					mApp.mRadarDevice.setScanLength(val); // 2016.6.10
					mRadarParamAdapter.notifyDataSetChanged();
				}

				// //正在调解分段增益
				// if(mApp.mRadarDevice.isSetting_StepHardPlus_Command())
				// {
				// mainActivity.mScanView.manageKeyDown();
				// }

				// 正在滤波
				if (mApp.mRadarDevice.isSetting_Filter_Command()) {
					val = mApp.mRadarDevice.getFilterSel();
					val = val - 1;
					if (val <= 0)
						val = 0;
					mApp.mRadarDevice.setFilter(val);
					mApp.mRadarDevice.setFilterParams(); // 2016.6.10
					mRadarParamAdapter.notifyDataSetChanged();
				}
				// 正在设置介电常数
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
					valf = b.setScale(2, BigDecimal.ROUND_HALF_DOWN).floatValue();// 向下取整
					mApp.mRadarDevice.setJieDianConst(valf);
					// 更新列表从mRadarDevice中重新获取
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

					// 限制道间平均
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
				// 正在设置调色板
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

					// 限制范围
					if (num <= 0) {
						num = 1;
					} else
						;

					mApp.mRadarDevice.setWheelExtendNumber(num);
					mRadarParamAdapter.notifyDataSetChanged();
				}
				// 调节部分增益
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
				// "Left mListViewOnKeyListener ActionDown,间隔="+intertemp);
				// if(intertemp < 100)
				// return true;
				// else;
				// mainActivity.mScanView.manageKeyDown();
				// }
			}
		}
		// ---------------------------------------------------------------------------///
		// 抬起
		if (event.getAction() == KeyEvent.ACTION_UP) {
			DebugUtil.i(KTAG, "manageOnKey_ForSettingCommand,UpKey:=" + keyCode);
			mApp.setRealThreadReadingDatas(true);
			mainActivity = (MultiModeLifeSearchActivity) getActivity();

			// 按下 “左键”
			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				// 正在调解分段增益
				if (mApp.mRadarDevice.isSetting_StepHardPlus_Command()) {
					mainActivity.mScanView.manageKeyLeft(0, true);
				}

				// 正在设置整体增益
				if (mApp.mRadarDevice.isSetting_AllHardPlus_Command()) {
					float[] mHardpluse = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
					mHardpluse = mApp.mRadarDevice.getHardplus();
					mApp.mRadarDevice.setHardplus(mHardpluse);
					// mainActivity.mScanView.invalidate();
				}
			}// 结束按下左键操作
				// 按下 “右键”
			else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				// 正在调解分段增益
				if (mApp.mRadarDevice.isSetting_StepHardPlus_Command()) {
					mainActivity.mScanView.manageKeyRight(0, true);
				}

				// 正在设置整体增益
				if (mApp.mRadarDevice.isSetting_AllHardPlus_Command()) {
					float[] mHardpluse = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
					mHardpluse = mApp.mRadarDevice.getHardplus();
					mApp.mRadarDevice.setHardplus(mHardpluse);
					// mainActivity.mScanView.invalidate();
				}
			}// 结束按下右键操作
				// 按下上键操作
			else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
				// 正在设置时窗
				if (mApp.mRadarDevice.isSetting_Timewindow_Command()) {
					val = mApp.mRadarDevice.getTimeWindow();
					mApp.mRadarDevice.setTimeWindow(val, true);
					// mRadarParamAdapter.notifyDataSetChanged();

					mApp.mTimewndRuler.invalidate();
					mApp.mDeepRuler.invalidate();
				}
				// 正在设置扫速度
				else if (mApp.mRadarDevice.isSetting_Scanspeed_Command()) {
					val = mApp.mRadarDevice.getScanSpeed();

					mApp.mRadarDevice.setContinueScanSpeed(val);
					// mRadarParamAdapter.notifyDataSetChanged();
				}
				// 设置信号位置上键，正在调节信号位置
				else if (mApp.mRadarDevice.isSetting_Singlepos_Command()) {
					val = mApp.mRadarDevice.getSignalpos();
					mApp.mRadarDevice.setSignalpos(val);
					// mRadarParamAdapter.notifyDataSetChanged();
				} else if (mApp.mRadarDevice.isSetting_ExtendNum_Command()) {
					if (!mApp.mRadarDevice.setWheelMode(mApp.mRadarDevice.getWheelExtendNumber()))
						showToastMsg("设置轮测模式错误!");
					else
						;

					// mApp.setRealThreadReadingDatas(true);
					mApp.mRadarDevice.setBackFillPos(0);
					// setManuWhellParams();

					((HRulerView) (mApp.mHorRuler)).setShowdistanceMode();
					setNowSpeedRange();
				}
				// 调节分段增益
				else if (mApp.mRadarDevice.isSetting_StepHardPlus_Command()) {
					mainActivity.mScanView.manageKeyUp();
				}
			}
			// 按下下键操作
			else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
				// 设置时窗
				if (mApp.mRadarDevice.isSetting_Timewindow_Command()) {
					val = mApp.mRadarDevice.getTimeWindow();

					mApp.mRadarDevice.setTimeWindow(val, true);
					// mRadarParamAdapter.notifyDataSetChanged();
					mApp.mTimewndRuler.invalidate();
					mApp.mDeepRuler.invalidate();
				}
				// 设置扫速度
				else if (mApp.mRadarDevice.isSetting_Scanspeed_Command()) {
					val = mApp.mRadarDevice.getScanSpeed();
					mApp.mRadarDevice.setContinueScanSpeed(val);
					// mRadarParamAdapter.notifyDataSetChanged();
				}
				// 设置信号位置
				else if (mApp.mRadarDevice.isSetting_Singlepos_Command()) {
					val = mApp.mRadarDevice.getSignalpos();
					mApp.mRadarDevice.setSignalpos(val);
					// mRadarParamAdapter.notifyDataSetChanged();
				} else if (mApp.mRadarDevice.isSetting_ExtendNum_Command()) {
					if (!mApp.mRadarDevice.setWheelMode(mApp.mRadarDevice.getWheelExtendNumber()))
						showToastMsg("设置轮测模式错误!");
					else
						;

					// mApp.setRealThreadReadingDatas(true);
					mApp.mRadarDevice.setBackFillPos(0);
					((HRulerView) (mApp.mHorRuler)).setShowdistanceMode();
					setNowSpeedRange();
				}
				// 调节分段增益
				else if (mApp.mRadarDevice.isSetting_StepHardPlus_Command()) {
					mainActivity.mScanView.manageKeyDown();
				}
			}
		}
		return true;
	}

	// 选择测距轮类型
	public void manage_WhellTypeSelect() {
		/*
		 * String[] whellNameStr = mApp.mRadarDevice.mWhellName; new
		 * AlertDialog.Builder(this.getActivity()) .setTitle("选择测距仪型号")
		 * .setSingleChoiceItems(whellNameStr, 0, new
		 * android.content.DialogInterface.OnClickListener() {
		 *
		 * @Override public void onClick(DialogInterface dialog, int which) {
		 * if(which == 7) {
		 *
		 * } } }).setPositiveButton("确定", new
		 * android.content.DialogInterface.OnClickListener() {
		 *
		 * @Override public void onClick(DialogInterface dialog, int which) {
		 *
		 * } }).setNegativeButton("取消", new
		 * android.content.DialogInterface.OnClickListener() {
		 *
		 * @Override public void onClick(DialogInterface dialog, int which) {
		 *
		 * } }).show();
		 */
		showWheelExtendWindow();
	}

	/**
	 * 设置轮测模式下的参数
	 */
	public View wheelExtendView;
	private PopupWindow mWheelExtendWindow;
	private int mWheelTypeNumber = 10;
	private int[] mWheeltypeRadiosID = new int[mWheelTypeNumber];
	private int mWheelSelectID = 0;

	/**
	 * 设置测距轮标记扩展,点击加载相应测距仪型号
	 */
	public void setWheelExtendWindow() {
		wheelExtendView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_whellparams, null);
		mWheelExtendWindow = new PopupWindow(wheelExtendView, 280,// LayoutParams.WRAP_CONTENT,
                                             android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		mWheelExtendWindow.setFocusable(true);

		// //设置测量轮选择
		RadioGroup radioGroup;
		RadioButton radioButton;
		int num;
		int i;
		int sel = 0;

		// //生成轮测控制视图
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
	 * 设置弹出自定义测距仪文件列表
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
	 * 弹出校准文件列表
	 */
	private List<String> checkGroups;
	ListGroupAdapter groupCheckAdapter;

	public void showCheckFileWindow() {
		DebugUtil.i(TAG, "Left showCheckFileWindow!");

		// 新建文件取消选中
		mCheckBox.setChecked(false);
		mIsNewCheckfile = false;

		// 寻找sd卡中的雷达参数文件
		String path;
		path = mApp.mRadarDevice.getParamsPath();
		List<String> dataList = new ArrayList<String>();
		if (!mApp.getCheckFileNamesFormSD(dataList, path) || dataList.size() == 0) {
		} else
			;

		// 得到文件名
		int i;
		int size;
		size = dataList.size();

		// 对文件排序
		sortFile(dataList);

		// 设置列表内容
		checkGroups = new ArrayList<String>();

		// 缺少空值判断
		// 在空值时的处理,默认有一个defcheck的文件
		if (size <= 1) {
			// 文件列表没有check文件
			mApp.mRadarDevice.mWhellName[mApp.mRadarDevice.getWheeltypeSel()] = "自定义设置";
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

	// 文件排序
	private void sortFile(List<String> fileList) {
		String temp = null;
		for (int i = fileList.size() - 1; i > 0; i--) {
			for (int j = 0; j < i; j++) {
				// 从文件中取得
				if (getFileNumber(fileList.get(j)) > getFileNumber(fileList.get(j + 1))) {
					temp = fileList.get(j + 1);
					fileList.set(j + 1, fileList.get(j));
					fileList.set(j, temp);
				} else
					;

			}
		}
	}

	// 得到文件名中的数字
	private int getFileNumber(String file) {
		int index_begin = 0;
		int index_end = 0;
		int temp_next = 0;
		int num = 0;
		index_begin = file.lastIndexOf("_");

		// 将纯数字取出来
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
	 * 设置选中的条目
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
			groupCheckAdapter.setSelectIndex(item);// 默认选择第1个
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
			groupCheckAdapter.setSelectIndex(item);// 默认选择第二个
		else
			;
		mRadarParamAdapter.notifyDataSetChanged();
	}

	/**
	 * 选择新建文件选项
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

			// 设置编辑直径和脉冲数的值
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

			// 设置生成新文件，文件名由user_1到user_5，获取文件名判断取出最大值
			TextView tv_newfile = (TextView) mwheelCheckFileView.findViewById(R.id.newcheckfile);
			if (isChecked) {
				// 更改状态为调节直径
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
					DebugUtil.i(TAG, "排序前fileList=" + fileList.get(i));
				}

				// 排序
				sortFile(fileList);

				for (int i = 0; i < checkGroups.size(); i++) {
					DebugUtil.i(TAG, "排序后fileList=" + fileList.get(i));
				}

				// 如果中间没有空隔开的数字，把最大的那个值加1如果大于最大值与5取余数
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

				// 创建文件
				mApp.mRadarDevice.setCustomFileName(fileNameNew);
				DebugUtil.i("ExtendNumb", "customFileName=" + fileNameNew);

				// 记录选中的位置
				// 得到其在fileList中的位置
				newIndex = getPosition(fileList, num);

			} else {
				tv_newfile.setTextColor(Color.GRAY);
			}
		}

		// 文件排序,从小到大
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
				// 将纯数字取出来
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

		// 得到新加入的位置
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
	 * 点击的响应
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
				// 保存使用的文件名
				mApp.mRadarDevice.setCustomFileName(fileName);
				int index = fileName.lastIndexOf('.');
				mApp.mRadarDevice.mWhellName[mApp.mRadarDevice.getWheeltypeSel()] = fileName.substring(0, index);
				loadWhellcheckParamsFile(fileName);
				setManuWhellParams();
				// 更新直径和选中列表
				updateDiamPulse();
			} else
				;

			groupCheckAdapter.notifyDataSetChanged();
			mRadarParamAdapter.notifyDataSetChanged();
		}
	};

	// 更新直径和脉冲数显示
	public void updateDiamPulse() {
		mEditDiameter.setText(String.valueOf(this.mDiameter));
		RadioButton radioButton = (RadioButton) mRadioGroupPlus.getChildAt(mPulseCheckid);
		mRadioGroupPlus.check(radioButton.getId());
	}

	/**
	 * 轮测校准的响应处理
	 */
	public Button.OnClickListener mWheelCheck_ButtonClickHandler = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			DebugUtil.i(TAG, "mWheelCheck_ButtonClickHandler!");
			int id = v.getId();
			ListGroupAdapter groupAdapter = (ListGroupAdapter) mRadarCheckFileListview.getAdapter();

			switch (id) {
			case R.id.buttonSavecheckfileOK:
				// 生成新文件
				if (mIsNewCheckfile) {
					saveNewCheckFile();// 保存新文件
					loadWhellcheckParamsFile(mApp.mRadarDevice.getCustomFileName());
					// 0916
					mNowItemCommandID = 0;
					mApp.mRadarDevice.setNowSetting_CommandID(mNowItemCommandID);
					mIsCustomFileEmpty = false;// 非空

					// 保存下次选中的位置
					// 重新排序，找到新建文件的item，记录
					mApp.rememberCustomWheelIndex(newIndex);
				} else {
					if (groupAdapter.mCheckIndex != -1 && !groupAdapter.isEmpty()) {
						String fileName = (String) groupAdapter.getItem(groupAdapter.mCheckIndex);
						DebugUtil.i("ExtendNumb", "mWheelCheck_ButtonClickHandler fileName=" + fileName);
						// 保存使用的文件名
						mApp.mRadarDevice.setCustomFileName(fileName);
						loadWhellcheckParamsFile(fileName);
						mApp.rememberCustomWheelIndex(groupAdapter.mCheckIndex);
					} else
						;
				}
				// 刷新左侧
				setManuWhellParams();
				// 改变雷达状态，变为轮测模式，设置轮测参数
				mApp.setRealThreadReadingDatas(false);
				mApp.mRadarDevice.stopRadar();
				if (!mApp.mRadarDevice.setWheelMode(mApp.mRadarDevice.getWheelExtendNumber()))
					showToastMsg("设置轮测模式错误!");
				else
					;
				mApp.setRealThreadReadingDatas(true);
				mApp.mRadarDevice.setBackFillPos(0);
				// 隐藏设置窗口
				((HRulerView) (mApp.mHorRuler)).setShowdistanceMode();
				setNowSpeedRange();
				mRadarParamAdapter.notifyDataSetChanged();
				mWheelCheckFilePopWindow.dismiss();

				break;
			case R.id.buttonSavecheckfileCANCEL:
				// 0916
				// DebugUtil.infoDialog(mContext, "状态值",
				// "mNowItemCommandID="+String.valueOf(mNowItemCommandID));
				// / mNowItemCommandID = 0;
				// mApp.mRadarDevice.setNowSetting_CommandID(mNowItemCommandID);
				mWheelCheckFilePopWindow.dismiss();
				break;
			case R.id.buttonDeleteCheckFile:
				// 删除按键
				if (!groupAdapter.isEmpty() && groupAdapter.mCheckIndex > -1) {
					DebugUtil.i(TAG, "mCheckIndex=" + String.valueOf(groupAdapter.mCheckIndex));
					String fileName = (String) groupAdapter.getItem(groupAdapter.mCheckIndex);
					DebugUtil.i(TAG, "mWheelCheck_ButtonClickHandler fileName=" + fileName);
					int size = checkGroups.size();
					deleteOneParamfile(mRadarCheckFileListview, fileName);
					size = checkGroups.size();
					// 更新选中和左侧信息
					if (size > 0) {
						int item = 0;
						groupCheckAdapter.setSelectIndex(item);// 默认选择第二个
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
						// 将保存置为不可用
						bt_certain.setEnabled(false);
						mApp.rememberCustomWheelIndex(-1);
						mApp.mRadarDevice.mWhellName[mApp.mRadarDevice.getWheeltypeSel()] = "自定义设置";
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
	 * 测距仪控制
	 */
	public void showWheelExtendWindow() {
		View layout = this.getView();
		int xPos = layout.getWidth();
		int yPos = layout.getHeight() / 4;

		// 根据当前的选择，设置radio选择
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
	 * 保存新的文件
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
	public int mManusetWhelltypeIndex = 5;// 手动调节的下标数
	public int mSetPulse = 2048;
	public double mSetDiameter = 637;

	// isInit:是否恢复初始值
	public void resetWheelExtendWindowParams(boolean isInit) {
		MyApplication app;
		app = mApp;
		int whellSel = app.getWheeltypeSel();

		// 设置测量轮选择
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

		// /得到标记扩展值并设置显示距离
		int extNumber;
		extNumber = defExtNumber;

		app.mRadarDevice.setWheelExtendNumber(extNumber);

		if (whellSel > mManusetWhelltypeIndex) {
			DebugUtil.e(TAG, "resetWheelExtendWindowParams index 越界！");
		} else {
			app.mRadarDevice.setWhellType(whellSel);
		}
	}

	// 设置脉冲值
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

	// 脉冲索引对应
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

	// 测距轮设置_单选按钮选择变化处理函数
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
				// 隐藏设置窗口
				dismissWheelParamWindow(index);
				showCheckFileWindow();
				// 设置自定义设置状态
				mApp.setCustomSetting(true);
				break;
			default:
				// /得到标记扩展值并设置显示距离
				if (index > mApp.mRadarDevice.getWheelDefaultMaxNum()) {
					DebugUtil.e(TAG, "mWhellMode_RadioGroupSelChangeHandler index 越界！");
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
		 * 隐藏测距轮设置的窗口
		 *
		 * @param index
		 */
		private void dismissWheelParamWindow(int index) {
			((HRulerView) (mApp.mHorRuler)).setShowdistanceMode();
			setNowSpeedRange();

			DebugUtil.i(TAG, "测距仪型号=" + mApp.mRadarDevice.getWheeltypeSel() + ",index=" + index);
			mRadarParamAdapter.notifyDataSetChanged();
			mWheelExtendWindow.dismiss();
		}

		/**
		 * 设置测距轮状态，雷达命令和状态设置，回退清零，开始读数
		 *
		 * @param defExtNumber
		 */
		private void changeToWheelMode(int defExtNumber) {
			mApp.setRealThreadReadingDatas(false);
			mApp.mRadarDevice.stopRadar();
			if (!mApp.mRadarDevice.setWheelMode(defExtNumber))
				showToastMsg("设置轮测模式错误!");
			else
				;
			mApp.setRealThreadReadingDatas(true);
			mApp.mRadarDevice.setBackFillPos(0);
		}
	};

	/**
	 * 是否反转测距轮
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
				app.rememberTurnWheel();// 记录状态
				break;
			}
		}
	};

	// /手动设置直径值 Edit响应函数
	public boolean mIsSetDiameter = false;
	private int mDiameter = 0;
	public EditText.OnKeyListener mWhellMode_SetDiameterEditOnKeyListener = new EditText.OnKeyListener() {
		@Override
		public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
			// TODO Auto-generated method stub
			EditText txtView;
			txtView = (EditText) arg0;
			// 得到数值
			// int num;
			mDiameter = mApp.mRadarDevice.getmDiameter();
			txtView.setText("" + mDiameter);

			// 处理回车键
			if (arg1 == KeyEvent.KEYCODE_ENTER) {
				// 抬起做处理
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

		// 处理直径
		private boolean dealDiameterUpDown(int arg1, KeyEvent arg2, EditText txtView, int num) {
			// 处理 上键
			if (arg1 == KeyEvent.KEYCODE_DPAD_UP) {
				if (!mIsSetDiameter)
					return false;
				if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
					if (mKeyF3Down) {
						num += 10;
					} else
						num += 1;

					// 设定限值
					if (num > 1000)
						num = 1000;
					else
						;
					txtView.setText("" + num);
					mApp.mRadarDevice.setmDiameter(num);
				}
				return true;
			}
			// 处理下键
			if (arg1 == KeyEvent.KEYCODE_DPAD_DOWN) {
				if (!mIsSetDiameter)
					return false;
				if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
					if (mKeyF3Down)
						num -= 10;
					else
						num -= 1;

					// 设定限值
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

	// /手动设置 脉冲数 Edit响应函数
	public boolean mIsSetPuls = false;
	// /手动设置 触发距离 Edit响应函数
	public boolean mIsSetTouchdistance = false;
	public EditText.OnKeyListener mWhellMode_SetTouchDistanceEditOnKeyListener = new EditText.OnKeyListener() {
		@Override
		public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
			// TODO Auto-generated method stub
			EditText txtView;
			txtView = (EditText) arg0;
			// 得到 数值
			int num;
			num = getIntvalFromeEdit(txtView);
			// 处理 回车键
			if (arg1 == KeyEvent.KEYCODE_ENTER) {
				// 抬起做处理
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
			// 处理 上键
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
			// 处理下键
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
	 * 设置自定义测距轮参数，脉冲间隔的值，更新触发距离0629
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
		// mApp.mRadarDevice.setmDiameter(diameter);// 设置直径

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
			// ///对 测量方式 做处理
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

	// 设置当前速度范围
	public void setNowSpeedRange() {
		TextView txtView;
		String txt;
		txtView = (TextView) mApp.mMainActivity.findViewById(R.id.textview_speed);
		if (!(mApp.mRadarDevice.isWhellMode())) {
			DebugUtil.i(TAG, "不是轮测模式！");
			txt = "最快时速(km/h):";
			txtView.setText(txt);
			txtView.setVisibility(View.INVISIBLE);
			return;
		} else {
			DebugUtil.i(TAG, "是轮测模式！");
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
			txt = "最快时速(km/h):" + speed;
		else
			txt = "最快时速(km/h):" + speed1;
		txtView.setText(txt);
	}

	// 装载轮测参数
	public boolean mIsHadLoadWhellcheckFile = false;

	public void loadWhellcheckParamsFile(String fileName) {
		MyApplication app;
		app = mApp;
		String filePathName;
		filePathName = app.mRadarDevice.getInnerStoragePath() + app.mRadarDevice.mParamsFilefolderPath;
		filePathName += fileName;
		int type = app.mRadarDevice.getWheeltypeSel();

		// 加载测距轮校准文件,拟与自定义使用的分开0718
		if (type != mManusetWhelltypeIndex) {
			app.mRadarDevice.loadWhellcheckParams(filePathName);
		} else {
			app.mRadarDevice.loadCustomWheelCheckParamsFile(filePathName);
			this.mPulseCheckid = app.mRadarDevice.getmPulseIndex();
			this.mSetPulse = this.getPulseByIndex();
			this.setManuWhellParams();// 设置间隔值
			app.mRadarDevice.getTouchDistance();
			// 得到脉冲的索引值
			this.mPulseCheckid = app.mRadarDevice.getmPulseIndex();
			this.mDiameter = app.mRadarDevice.getmDiameter();
		}

		int extendNum = app.mRadarDevice.getWheelExtendNumber();

		for (int i = 0; i < mApp.mRadarDevice.getWheelDefaultMaxNum(); i++) {
			mWhellCheckCoeff[i] = mApp.mRadarDevice.getWhellCheckCoeff(i);
		}

		DebugUtil.i("ExtendNumb", "装载后的标记扩展值=" + extendNum);

		// 更新轮测设置对话框参数
		updateWhellcheckParams();
		mIsHadLoadWhellcheckFile = true;
	}

	/**
	 * 删除当前选择的校准文件
	 *
	 * @return
	 */
	public boolean deleteWhellCheckParamsFile(String fileName) {
		DebugUtil.i(TAG, "deleteWhellCheckParamsFile");

		// 删除当前选择的文件
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
			// 判断是一个文件夹还是一个文件
			if (currentFile.isDirectory()) {
				continue;
			} else {
				// 取得文件名
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
			// 判断是一个文件夹还是一个文件
			if (currentFile.isDirectory()) {
				continue;
			} else {
				// 取得文件名
				String fileName = currentFile.getName();
				if (checkEndsWithInStringArray(fileName, getResources().getStringArray(R.array.fileEndingWHELLCHECK))) {
					// 确保只显示文件名、不显示路径如：/sdcard/111.txt就只是显示111.txt
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

	// 通过文件名判断是什么类型的文件
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
	// //对话框显示队列
	// new AlertDialog.Builder(this.getActivity()).setTitle("选择测距轮校正文件")
	// .setSingleChoiceItems(fileNames, mWhellCheckFileSel, new
	// android.content.DialogInterface.OnClickListener()
	// {
	// @Override
	// public void onClick(DialogInterface dialog, int which)
	// {
	// mWhellCheckFileSel = which;
	// mWhellCheckFileName = names[which];
	// }
	// }).setNeutralButton("确定", new
	// android.content.DialogInterface.OnClickListener()
	// {
	// @Override
	// public void onClick(DialogInterface dialog, int which)
	// {
	// mIsLoadWhellcheckFile = true;
	// loadWhellcheckParamsFile(mWhellCheckFileName);
	// }
	// }).setPositiveButton("取消", new
	// android.content.DialogInterface.OnClickListener()
	// {
	// @Override
	// public void onClick(DialogInterface dialog, int which)
	// {
	//
	// }
	// }).setNegativeButton("删除", new
	// android.content.DialogInterface.OnClickListener() {
	//
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// mIsLoadWhellcheckFile = true;
	// if(deleteWhellCheckParamsFile(names[mWhellCheckFileSel]))//删除当前校准文件
	// {
	// new AlertDialog.Builder(getActivity()).setTitle("删除成功")
	// .setPositiveButton("确定", new
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
	// new AlertDialog.Builder(getActivity()).setTitle("删除失败")
	// .setPositiveButton("确定", new
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

	// 从选择的radio得到脉冲值
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

	// 存储位置选择响应
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
			// 判断是否存在，以及是否有空间进行存储
			if (judgeExistSpace()) {
				// 保存到文件中
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

	// 判断是否有该外设以及存储空间的大小
	public boolean judgeExistSpace() {
		// 如果存在此外设
		long size[] = mApp.getSDCardMemory();
		String path = mApp.mRadarDevice.getStoragePath();

		// 如果可用存储空间为0，判断是否存在
		if (size[1] == 0) {
			path += "/test.txt";
			File file = new File(path);
			if (file.mkdir()) {
				if (file.delete())
					;
				else
					;
				// 存在提示存储空间大小
				DebugUtil.infoDialog(getActivity(), "存储空间不足", "可用存储空间" + size[1] + "");
				return true;
			} else {
				// 提示不存在
				DebugUtil.infoDialog(getActivity(), "存储空间不存在", "未找到该外设");
			}
			return false;
		}
		// 提示存储空间大小
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
				DebugUtil.infoDialog(getActivity(), "存储空间容量", "可用存储空间大小/总存储大小:" + txt);

				return true;
			} else {
				// 提示不存在
				DebugUtil.infoDialog(getActivity(), "存储空间不存在", "未找到该外设");
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
			// 弹出提示对话框
			dialogButton();
		}
	};

	// 更新了测距轮校正值
	public void updateWhellcheckParams() {
		// //设置测距轮选择距离信息
		MyApplication app;
		app = mApp;
		mApp.mMainActivity.setHasReceiveScansInf();

		int selType;
		selType = app.mRadarDevice.getWheeltypeSel();

		// 从文件读取更新校正系数
		if (mWheelExtendWindow.isShowing()) {
			// /得到标记扩展值并设置显示距离
			int extNumber = app.mRadarDevice.getWheelExtendNumber();

			// 设置显示触发距离
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
		// 生成窗口
		saveRadarparamsLayout = LayoutInflater.from(getActivity()).inflate(R.layout.layout_saveradarparamfile, null);
		mRadarparamFileListview = (ListView) saveRadarparamsLayout.findViewById(R.id.listView_radarparams);

		// 设置参数文件列表框单击响应函数
		mRadarparamFileListview.setOnItemClickListener(mListView_RadarparamSaveFile_OnClickListener);

		// 设置参数文件名编辑框按键响应函数
		mRadarparamFilenameEdit = (EditText) saveRadarparamsLayout.findViewById(R.id.editView_saveparamfilename);
		mRadarparamFilenameEdit.setOnKeyListener(mEditRadarparamFilename_OnKeyListener);
		saveRadarparamsLayout.setOnKeyListener(mEditRadarparamFilename_OnKeyListener);
		mRadarparamFilenameEdit.setEnabled(false);

		CheckBox checkBox;
		checkBox = (CheckBox) saveRadarparamsLayout.findViewById(R.id.checkbox_editfilename);
		checkBox.setOnCheckedChangeListener(mCheckboxRadarparamsave_OnCheckedChangeListener);

		// 设置按钮响应函数
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
	 * 弹出雷达参数文件
	 */
	public void showRadarparamsFilePopwindow() {
		View layout = this.getView();
		int xPos = layout.getWidth();
		int yPos = 0;

		// 加载数据
		MyApplication app;
		app = mApp;
		// 寻找sd卡中的雷达参数文件
		String path;
		path = app.mRadarDevice.getParamsPath();
		List<String> dataList = new ArrayList<String>();
		if (!app.getParamFilenamesFromeSD(dataList, path) || dataList.size() == 0) {

		}
		// 得到文件名
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

		// 设置默认文件名
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
			// 删除键
			if (keyCode == KeyEvent.KEYCODE_DEL)
				return true;
			if (keyCode == KeyEvent.KEYCODE_FORWARD_DEL)
				return true;
			// 回车键
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
			// 左键
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
			// 右键
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
			// 下键
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
			// 上键
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
			// 删除参数文件
			case R.id.buttonDeleteParamFile:
				ListGroupAdapter groupAdapter = (ListGroupAdapter) mRadarparamFileListview.getAdapter();
				if (groupAdapter.mCheckIndex != -1) {
					String fileName = (String) groupAdapter.getItem(groupAdapter.mCheckIndex);
					deleteOneParamfile(mRadarparamFileListview, fileName);
				}
				break;
			// 保存参数文件
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
			// 取消
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
	 * 删除雷达参数文件
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
	 * 设置校正的弹框
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

	// 校准没结束退出时的提示
	public OnDismissListener onDismissListener = new OnDismissListener() {
		@Override
		public void onDismiss() {
			// TODO Auto-generated method stub
			if (mApp.mRadarDevice.isSetting_Calibrate_Command()) {
				new AlertDialog.Builder(getActivity()).setTitle("请先在测距仪标定中结束校准！").setPositiveButton("确定", new android.content.DialogInterface.OnClickListener() {
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
	 * 设置测距轮校正的弹框显示
	 */
	public void showWheelCalibratePopWindow() {
		View layout = this.getView();
		// et_extendNumber.setText(""+mApp.mRadarDevice.getWheelExtendNumber());
		int tempType = mApp.mRadarDevice.getWheeltypeSel();
		bt_certain.setEnabled(true);
		// 非自定义类型处理
		if (tempType != mManusetWhelltypeIndex) {
			String fileName = mApp.mRadarDevice.PREFIXDEFAULTCHECKFILE + mApp.mRadarDevice.mWhellcheckFileExtname;
			et_fileName.setText(fileName);
			et_fileName.setEnabled(false);
			loadWhellcheckParamsFile(fileName);
			DebugUtil.i(TAG, "mWhellCheckCoeff[" + tempType + "]=" + mApp.mRadarDevice.getWhellCheckCoeff(tempType));
			et_currentcfcontent.setText("(" + String
                    .valueOf(mApp.mRadarDevice.getWhellCheckCoeff(tempType)) + ")");
		}
		// 自定义类型处理
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

		// 如果没结束，则将焦点设在结束上
		if (mApp.mRadarDevice.isSetting_Calibrate_Command())
			bt_stop.requestFocus();
		else
			;

		mWheelCalibrateWindow.setBackgroundDrawable((new BitmapDrawable()));// 不加这个看似无用的设置，back退出无效
		mWheelCalibrateWindow.setFocusable(true);
		mWheelCalibrateWindow.showAtLocation(layout, Gravity.CENTER | Gravity.CENTER, 0, 0);
		mWheelCalibrateWindow.update();
	}

	/**
	 * 测距轮校准
	 */
	public View.OnClickListener onWheelCalibrateClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int id = v.getId();
			switch (id) {
			case R.id.start:
				/**
				 * 开始轮测，将开始的状态置不可用，结束的状态置为可用
				 */
				if (!mApp.mRadarDevice.isRunningMode()) {
					android.app.AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
					builder.setTitle("雷达未开启").setMessage("请先开启雷达！").setNegativeButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					}).show();
					mWheelCalibrateWindow.dismiss();
				} else {
					// 将校正系数值为1
					for (int i = 0; i < mApp.mRadarDevice.getWheelDefaultMaxNum(); i++) {
						mWhellCheckCoeff[i] = 1;
					}

					// 保存上一个状态
					mCurrentMode = mApp.mRadarDevice.getNowMode();

					bt_start.setEnabled(false);
					bt_stop.setEnabled(true);
					bt_certain.setEnabled(false);
					et_fileName.setEnabled(false);
					et_fileName.setTextColor(getResources().getColor(R.color.deep_gray));
					int type = mApp.mRadarDevice.getWheeltypeSel();
					et_cfcontent.setText("" + mWhellCheckCoeff[type]);

					// 自定义文件列表是空
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

					// 生成线程
					mThreadWhellCheck = new Thread(mThreadWhellcheckRunnable);
					mStopWhellCheckThread = false;
					mThreadWhellCheck.start();

					// 更改状态为正在校准
					mNowItemCommandID = mApp.mRadarDevice.COMMAND_ID_CALIBRATE;
					mApp.mRadarDevice.setNowSetting_CommandID(mNowItemCommandID);
				}
				break;
			case R.id.stop:
				/**
				 * 结束置为不可用，开始置为可用,计算得到系数值
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
				 * 更改校准系数的mWhellcheckCoeff[i]的值i为mWheelSelectID public boolean
				 * saveWhellcheckParams(String pathName)
				 */
				// 保存时将内存中的修改
				for (int i = 0; i < mApp.mRadarDevice.getWheelDefaultMaxNum(); i++) {
					mApp.mRadarDevice.setWhellCheckCoeff(mWhellCheckCoeff[i], i);
				}
				// 根据文件名判断是默认的校准文件还是自定义的校准文件
				{
					String fileName = et_fileName.getText().toString();
					if (mApp.mRadarDevice.getWheeltypeSel() != mManusetWhelltypeIndex) {
						mApp.mRadarDevice.saveDefaultCheckParamsFile();
					} else {
						// 生成新的校准文件
						mApp.mRadarDevice.createWhellCheckFile(fileName);
					}
				}

				mRadarParamAdapter.notifyDataSetChanged();
				mWheelCalibrateWindow.dismiss();
				break;
			case R.id.cancel:
				// 神马都不做
				mWheelCalibrateWindow.dismiss();
				break;
			default:
				break;
			}
		}
	};

	/**
	 * 结束校准，计算校准系数
	 */
	private void finishCalibrate() {
		/**
		 * 结束置为不可用，开始置为可用,计算得到系数值
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

		// 更改状态栏为校准结束
		mNowItemCommandID = 0;
		mApp.mRadarDevice.setNowSetting_CommandID(mNowItemCommandID);

		// 停止线程
		mStopWhellCheckThread = true;

		DebugUtil.i(TAG, "onGroupClick,Item:=轮测量");
		mApp.mRadarDevice.stopRadar();
		mApp.mRadarDevice.setWheelMode(mApp.mRadarDevice.getWheelExtendNumber());
		mApp.mRadarDevice.setBackFillPos(0);

		setManuWhellParams();

		((HRulerView) (mApp.mHorRuler)).setShowdistanceMode();

		setNowSpeedRange();

		mRadarParamAdapter.notifyDataSetChanged();
	}

	/**
	 * 对标记扩展和距离的标记窗口的处理
	 */
	private boolean isEditStateDistance = false;
	private boolean isEditStateExtendNum = false;
	private boolean isEditStateCoeff = false;
	private int mWheelCalibrateDistance = 0;// 设定的校准距离值
	private int mWheelCalibrateFileIndex = 0;
	public EditText.OnKeyListener mWheelEditOnKeyListener = new EditText.OnKeyListener() {
		@Override
		public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
			int id = arg0.getId();
			EditText txtView = (EditText) arg0;// 编辑文本
			int extendNum = 0;// 标记扩展
			int temp = 0;
			int type = mApp.mRadarDevice.getWheeltypeSel();
			double coefftemp = mWhellCheckCoeff[type];
			switch (id) {
			case R.id.edittext_distance:
				/**
				 * 设定校准的距离值
				 */
				mWheelCalibrateDistance = getIntvalFromeEdit(txtView);

				/**
				 * 处理键盘操作，回车进行编辑
				 */
				// 处理 回车键
				if (arg1 == KeyEvent.KEYCODE_ENTER) {
					// 抬起做处理
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
				// 处理 上键
				if (arg1 == KeyEvent.KEYCODE_DPAD_UP) {
					if (!isEditStateDistance)
						return false;

					if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
						if (mKeyF3Down) {
							// 0-10加1,10-100加10,100-1000加100,1000-10000加1000
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
				// 处理下键
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
					// 更新显示参数
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
				// 处理 回车键
				if (arg1 == KeyEvent.KEYCODE_ENTER) {
					// 抬起做处理
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
				// 处理 上键
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
						coefftemp = b.setScale(3, BigDecimal.ROUND_HALF_DOWN).doubleValue();// 向下取整
						txtView.setText("" + coefftemp);
					}
					mWhellCheckCoeff[type] = coefftemp;
					return true;
				}
				// 处理下键
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
						coefftemp = b.setScale(3, BigDecimal.ROUND_HALF_DOWN).doubleValue();// 向下取整
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

	// //更新测距轮校正信息距离、道数以及校准系数
	private long mWheelCalibrateScans = 0;
	private double mWheelCalibratedistance = 0;

	public void updateWhellCheckInfs() {
		TextView txtView_distance, txtView_scan;
		txtView_distance = (TextView) wheelCalibrateView.findViewById(R.id.tv_distance);
		txtView_scan = (TextView) wheelCalibrateView.findViewById(R.id.tv_scan);
		int whellSel = mApp.getWheeltypeSel();
		// 采集到的道数(道)
		mWheelCalibrateScans = mApp.mRadarDevice.getHadRcvScans();
		// double temp_interDistance =
		// radarDevice.mWheelInterDistance[whellSel];
		// int temp_extendNum = mApp.mRadarDevice.getWheelExtendNumber();

		// 得到距离(m)
		mWheelCalibratedistance = mWheelCalibrateScans * mApp.mRadarDevice.getWheelExtendNumber() * radarDevice.mWheelInterDistance[whellSel];
		int intDistance = (int) (mWheelCalibratedistance * 10); // 毫米
		mWheelCalibratedistance = ((double) intDistance) / 1000;
		mWheelCalibratedistance = ((int) (mWheelCalibratedistance * 1000)) / 1000.;

		txtView_scan.setText(String.valueOf(mWheelCalibrateScans));
		txtView_distance.setText(String.valueOf(mWheelCalibratedistance));
	}

	// 结束测距轮标定
	public void endWhellCheck() {
		int whellSel = mApp.getWheeltypeSel();
		// mApp.mRadarDevice.stopRadar();

		// 计算测距轮修正值
		double result = 1.0;

		if (mWheelCalibrateScans != 0) {
			result = mWheelCalibrateDistance * 100 / (mWheelCalibrateScans * (double) mApp.mRadarDevice.getWheelExtendNumber());
			double tempResult = (result / radarDevice.mWheelInterDistance[whellSel]);
			BigDecimal b = new BigDecimal(tempResult);
			this.mWhellCheckCoeff[whellSel] = b.setScale(3, BigDecimal.ROUND_HALF_DOWN).doubleValue();// 四舍五入
		} else
			;

		BigDecimal b = new BigDecimal(result);
		result = b.setScale(3, BigDecimal.ROUND_HALF_DOWN).doubleValue();// 向下取整

		// 输出校准值
		// DebugUtil.i(TAG,"校准distance="+mWheelCalibrateDistance
		// +"标记扩展值="+mWheelCalibrateExtendNum
		// +"，道数*标记扩展="+mWheelCalibrateScans*mWheelCalibrateExtendNum
		// +"，结果="+String.valueOf(result)
		// +"，mWhellCheckCoeff["+whellSel+"]="+String.valueOf(mWhellCheckCoeff[whellSel]));

		// 显示校正系数
		String txt = "";
		BigDecimal temp = new BigDecimal(mWhellCheckCoeff[whellSel]);
		mWhellCheckCoeff[whellSel] = temp.setScale(3, BigDecimal.ROUND_HALF_DOWN).doubleValue();// 向下取整
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
				// 发出一次更新显示的消息
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
	private float[] mTempHardplus = new float[9]; // 设置线程处理发送命令的增益值
	final private int MESSAGE_RUNHARDPLUS = 1;
	private Thread mThreadHardplus = null;
	// 创建增益命令处理的线程
	private Runnable mThreadHardplusRunnable = new Runnable() {
		@Override
		public void run() {
			// DebugUtil.i(THREADTAG, "enter mThreadHardplusRunnable!");
			while (mApp.isRunFirstHardplusThread()) {
				// DebugUtil.i(THREADTAG, "enter isRunFirstHardplusThread!");
				while (mApp.getIsHardplusRun()) {
					// 如果队列中有数据
					// if( !mHardplusRunList.isEmpty() )
					// {
					/*
					 * DebugUtil.i(THREADTAG, "mHardplus is not empty!");
					 * //发出一次更新显示的消息 try{ // synchronized(this) {
					 * DebugUtil.e("threadTAG",
					 * "子线程mThreadHardplusRunnable synchronized ");
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

	// 打出增益数组的值
	public void printHardplusList() {
		while (!mHardplusRunList.isEmpty()) {
			float getHardplus[] = mHardplusRunList.pop();
			DebugUtil.i(TAG, "get hardplus list:");
			for (int i = 0; i < 9; i++) {
				DebugUtil.i(TAG, "stack[" + i + "]=" + getHardplus[i]);
			}
		}
	}

	// 增益线程处理
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
	 * 事件分发
	 *
	 * @param ev
	 * @return
	 */
	public boolean dispatchKeyEvent(KeyEvent ev) {
		DebugUtil.i(KTAG, "Left dispatchKeyEvent");
		return false;
	}

	/**
	 * 弹出操作提示
	 */
	private View mUserInforView = null;
	private PopupWindow mUserInforWindow = null;

	public void setUserInforPopWindow() {
		mUserInforView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_info, null);
		TextView tv_userinfo = (TextView) mUserInforView.findViewById(R.id.content);
		tv_userinfo.setText("1.首先根据天线选择主频开启雷达，再进行其它操作。\n\n" + "2.在参数调节菜单中进行相应实时采集参数的设置，系统设置下选择保存位置和背景灯。\n\n" + "3.左键在参数调节时点击可返回主菜单，人工点测时左键为点击采集。\n\n" + "4.退出键可退出当前弹窗或返回到主菜单，切换+返回切换到桌面。\n\n" + "5.关闭主机前请先停止雷达，否则参数可能无法完整保存！\n\n" + "6.如有其它问题，请联系技术支持人员，服务热线4008110511！v1.30beta");
		mUserInforWindow = new PopupWindow(mUserInforView, 280, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		mUserInforWindow.setFocusable(true);
	}

	public void showUserInforPopWindow() {
		View layout = this.getView();
		mUserInforWindow.setBackgroundDrawable(new BitmapDrawable());
		mUserInforWindow.showAtLocation(layout, Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
	}
}
