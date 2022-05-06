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
 * UncaughtException������,��������Uncaught�쳣��ʱ��,�ɸ������ӹܳ���,����¼���ʹ��󱨸�.
 * 
 * @author way
 * 
 */
public class CrashHandler implements UncaughtExceptionHandler {
	private Thread.UncaughtExceptionHandler mDefaultHandler;// ϵͳĬ�ϵ�UncaughtException������
	private static CrashHandler INSTANCE;// CrashHandlerʵ��
	private Context mContext;// �����Context����

	/** ��ֻ֤��һ��CrashHandlerʵ�� */
	private CrashHandler() {

	}

	/** ��ȡCrashHandlerʵ�� ,����ģʽ */
	public static CrashHandler getInstance() {
		if (INSTANCE == null)
			INSTANCE = new CrashHandler();
		return INSTANCE;
	}

	/**
	 * ��ʼ��
	 *
	 * @param context
	 */
	public void init(Context context) {
		mContext = context;

		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();// ��ȡϵͳĬ�ϵ�UncaughtException������
		Thread.setDefaultUncaughtExceptionHandler(this);// ���ø�CrashHandlerΪ�����Ĭ�ϴ�����
	}

	/**
	 * ��UncaughtException����ʱ��ת�����д�ķ���������
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {
			// ����Զ����û�д�������ϵͳĬ�ϵ��쳣������������
			mDefaultHandler.uncaughtException(thread, ex);
		}
	}

	/**
	 * �Զ��������,�ռ�������Ϣ ���ʹ��󱨸�Ȳ������ڴ����.
	 *
	 * @param ex
	 *            �쳣��Ϣ
	 * @return true ��������˸��쳣��Ϣ;���򷵻�false.
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

	//���浽�ļ���
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

	//�õ���ʱ���������ַ���
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

	//�����ļ��������ڽ���xx��xx��xx��
	public String getFolderName()
	{
		//�鿴�ļ����Ƿ���ڣ��������򴴽�
		//this.mLTEFilefolderPath;
		String folderName = null;
		Time t=new Time(); //or Time t=new Time("GMT+8"); ����Time Zone����
		t.setToNow(); //ȡ��ϵͳʱ�䡣
		int tyear = t.year;//��
		int tmonth = t.month + 1;//�����Ǵ�0��11���ӡ�������Ҫ��1
		int tday = t.monthDay;//��
		folderName = "/"+tyear+"y"+tmonth+"m"+tday+"d/";
		return folderName;
	}

	//����mSDCardPath��ֵ
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
//		builder.setTitle("���������");
//		builder.setMessage("��Ѵ��󱨸����ʼ�����ʽ�ύ�����ǣ�лл��");
//		builder.setPositiveButton(android.R.string.ok,
//				new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int which) {
//
//						// �����쳣����
//						try {
//							//ע�Ͳ�����������������ʽ���ʹ�����Ϣ
//							// Intent intent = new Intent(Intent.ACTION_SENDTO);
//							// intent.setType("text/plain");
//							// intent.putExtra(Intent.EXTRA_SUBJECT,
//							// "����Android�ͻ��� - ���󱨸�");
//							// intent.putExtra(Intent.EXTRA_TEXT, crashReport);
//							// intent.setData(Uri
//							// .parse("mailto:way.ping.li@gmail.com"));
//							// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//							// context.startActivity(intent);
//							
//							//�������Ը�����ʽ�����ʼ�
//							Intent intent = new Intent(Intent.ACTION_SEND);
//							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//							String[] tos = { "way.ping.li@gmail.com" };
//							intent.putExtra(Intent.EXTRA_EMAIL, tos);
//
//							intent.putExtra(Intent.EXTRA_SUBJECT,
//									"����Android�ͻ��� - ���󱨸�");
//							if (file != null) {
//								intent.putExtra(Intent.EXTRA_STREAM,
//										Uri.fromFile(file));
//								intent.putExtra(Intent.EXTRA_TEXT,
//										"�뽫�˴��󱨸淢�͸��ң��Ա��Ҿ����޸������⣬лл������\n");
//							} else {
//								intent.putExtra(Intent.EXTRA_TEXT,
//										"�뽫�˴��󱨸淢�͸��ң��Ա��Ҿ����޸������⣬лл������\n"
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
//							// �˳�
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
//						// �˳�
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
	 * ��ȡAPP�����쳣����
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
	 * ��ȡApp��װ����Ϣ
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