package com.ltdpro;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.Looper;
import android.text.format.Time;


/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,由该类来接管程序,并记录发送错误报告.
 * 
 * @author way
 * 
 */
public class CrashHandler implements UncaughtExceptionHandler {
	private Thread.UncaughtExceptionHandler mDefaultHandler;// 系统默认的UncaughtException处理类
	private static CrashHandler INSTANCE;// CrashHandler实例
	private Context mContext;// 程序的Context对象

	/** 保证只有一个CrashHandler实例 */
	private CrashHandler() {

	}

	/** 获取CrashHandler实例 ,单例模式 */
	public static CrashHandler getInstance() {
		if (INSTANCE == null)
			INSTANCE = new CrashHandler();
		return INSTANCE;
	}

	/**
	 * 初始化
	 *
	 * @param context
	 */
	public void init(Context context) {
		mContext = context;

		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();// 获取系统默认的UncaughtException处理器
		Thread.setDefaultUncaughtExceptionHandler(this);// 设置该CrashHandler为程序的默认处理器
	}

	/**
	 * 当UncaughtException发生时会转入该重写的方法来处理
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {
			// 如果自定义的没有处理则让系统默认的异常处理器来处理
			mDefaultHandler.uncaughtException(thread, ex);
		}
	}

	/**
	 * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
	 *
	 * @param ex
	 *            异常信息
	 * @return true 如果处理了该异常信息;否则返回false.
	 */
	public boolean handleException(Throwable ex) {
		if (ex == null || mContext == null)
			return false;
		final String crashReport = getCrashReport(mContext, ex);
		DebugUtil.i("error", crashReport);
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				File file = save2File(crashReport);
//				sendAppCrashReport(mContext, crashReport, file);
				Looper.loop();
			}

		}.start();
		return true;
	}

	//保存到文件里
	private File save2File(String crashReport) {
		// TODO Auto-generated method stub
		String crashName = getTimeString();
		String fileName = "crash_" + crashName + ".txt";

		if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
			try {
				String filePath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();

				File dir = new File(filePath + File.separator + "crash");
				if (!dir.exists())
					dir.mkdir();
				File file = new File(dir, fileName);
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(crashReport.toString().getBytes());
				fos.close();
				return file;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	//得到以时间命名的字符串
	public String getTimeString()
	{
		//String timeString = null;
		Time time = new Time();
		time.setToNow();
		int thour = time.hour;
		int tmin = time.minute;
		int tsecond = time.second;
		String timeString = thour + "h" + tmin + "m" + tsecond + "s";
		return timeString;
	}

	//设置文件夹以日期建立xx年xx月xx日
	public String getFolderName()
	{
		//查看文件夹是否存在，不存在则创建
		//this.mLTEFilefolderPath;
		String folderName = null;
		Time t=new Time(); //or Time t=new Time("GMT+8"); 加上Time Zone资料
		t.setToNow(); //取得系统时间。
		int tyear = t.year;//年
		int tmonth = t.month + 1;//月期是从0到11，坑。。。需要加1
		int tday = t.monthDay;//日
		folderName = "/"+tyear+"y"+tmonth+"m"+tday+"d/";
		return folderName;
	}

	//设置mSDCardPath的值
	public String setmSDCardPath()
	{
		String mSDCardPath = "/mnt/udisk2/";
		File file = new File(mSDCardPath);
		File createFile = new File(mSDCardPath + "create");

		if( file.exists() && file.isDirectory() )
		{
			try
			{
				if( createFile.exists() );
				else
				{
					boolean createStatus = createFile.mkdir();
					mSDCardPath = "/mnt/udisk2";
					if( createStatus )
						createFile.delete();
					else
					{
						mSDCardPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
					}
				}
			}catch(Exception e)
			{
				DebugUtil.e("setSDCard error!", e.toString());
			}
		}
		else if( !file.exists() && !file.isDirectory() )
		{
			mSDCardPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
		}
		
		return mSDCardPath;
		
	}



//	private void sendAppCrashReport(final Context context,
//			final String crashReport, final File file) {
//		// TODO Auto-generated method stub
//		AlertDialog mDialog = null;
//		AlertDialog.Builder builder = new AlertDialog.Builder(context);
//		builder.setIcon(android.R.drawable.ic_dialog_info);
//		builder.setTitle("程序出错啦");
//		builder.setMessage("请把错误报告以邮件的形式提交给我们，谢谢！");
//		builder.setPositiveButton(android.R.string.ok,
//				new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int which) {
//
//						// 发送异常报告
//						try {
//							//注释部分是已文字内容形式发送错误信息
//							// Intent intent = new Intent(Intent.ACTION_SENDTO);
//							// intent.setType("text/plain");
//							// intent.putExtra(Intent.EXTRA_SUBJECT,
//							// "推聊Android客户端 - 错误报告");
//							// intent.putExtra(Intent.EXTRA_TEXT, crashReport);
//							// intent.setData(Uri
//							// .parse("mailto:way.ping.li@gmail.com"));
//							// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//							// context.startActivity(intent);
//							
//							//下面是以附件形式发送邮件
//							Intent intent = new Intent(Intent.ACTION_SEND);
//							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//							String[] tos = { "way.ping.li@gmail.com" };
//							intent.putExtra(Intent.EXTRA_EMAIL, tos);
//
//							intent.putExtra(Intent.EXTRA_SUBJECT,
//									"推聊Android客户端 - 错误报告");
//							if (file != null) {
//								intent.putExtra(Intent.EXTRA_STREAM,
//										Uri.fromFile(file));
//								intent.putExtra(Intent.EXTRA_TEXT,
//										"请将此错误报告发送给我，以便我尽快修复此问题，谢谢合作！\n");
//							} else {
//								intent.putExtra(Intent.EXTRA_TEXT,
//										"请将此错误报告发送给我，以便我尽快修复此问题，谢谢合作！\n"
//												+ crashReport);
//							}
//							intent.setType("text/plain");
//							intent.setType("message/rfc882");
//							Intent.createChooser(intent, "Choose Email Client");
//							context.startActivity(intent);
//						} catch (Exception e) {
//							Toast.makeText(context,
//									"There are no email clients installed.",
//									Toast.LENGTH_SHORT).show();
//						} finally {
//							dialog.dismiss();
//							// 退出
//							android.os.Process.killProcess(android.os.Process
//									.myPid());
//							System.exit(1);
//						}
//					}
//				});
//		builder.setNegativeButton(android.R.string.cancel,
//				new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.dismiss();
//						// 退出
//						android.os.Process.killProcess(android.os.Process
//								.myPid());
//						System.exit(1);
//					}
//				});
//		mDialog = builder.create();
//		mDialog.getWindow().setType(
//				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//		mDialog.show();
//	}

	/**
	 * 获取APP崩溃异常报告
	 * 
	 * @param ex
	 * @return
	 */
	private String getCrashReport(Context context, Throwable ex) {
		PackageInfo pinfo = getPackageInfo(context);
		StringBuffer exceptionStr = new StringBuffer();
		exceptionStr.append("Version: " + pinfo.versionName + "("
				+ pinfo.versionCode + ")\n");
		exceptionStr.append("Android: " + android.os.Build.VERSION.RELEASE
				+ "(" + android.os.Build.MODEL + ")\n");
		exceptionStr.append("Exception: " + ex.getMessage() + "\n");
		StackTraceElement[] elements = ex.getStackTrace();
		for (int i = 0; i < elements.length; i++) {
			exceptionStr.append(elements[i].toString() + "\n");
		}
		return exceptionStr.toString();
	}

	/**
	 * 获取App安装包信息
	 * 
	 * @return
	 */
	private PackageInfo getPackageInfo(Context context) {
		PackageInfo info = null;
		try {
			info = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// e.printStackTrace(System.err);
			// L.i("getPackageInfo err = " + e.getMessage());
		}
		if (info == null)
			info = new PackageInfo();
		return info;
	}

}