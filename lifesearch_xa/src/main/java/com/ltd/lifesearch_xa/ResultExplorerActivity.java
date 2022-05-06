package com.ltd.lifesearch_xa;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class ResultExplorerActivity extends AppCompatActivity implements
		OnItemClickListener, OnItemLongClickListener
{

	private TextView mCurrentDir;
	private ListView mListView;
	private ListView mAlertListView;
	private TextView mResultTextView1;
	private TextView mResultTextView2;
	private File mRootDir;
	private String mFilePath;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result_explorer);

		setTitle("Check detection results");

		mCurrentDir = (TextView) findViewById(R.id.currentDir);
		mListView = (ListView) findViewById(R.id.listView);

		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);

		mFilePath = getExternalFilesDir(null) + "/"
				+ Global.RESULT_DIRECTORY_NAME;

		// folderpath =
		// Environment.getExternalStorageDirectory().getAbsolutePath();
		File file = new File(mFilePath);

		mRootDir = file;
		if (mRootDir.exists())
		{
			loadFiles(file);
		}
		else
		{
			mCurrentDir.setText("探测结果目录" + mFilePath + "不存在！");
			mCurrentDir.setVisibility(View.GONE);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
							long id)
	{
		// TODO Auto-generated method stub
		Map<String, Object> map = (Map<String, Object>) parent.getItemAtPosition(position);
		final File file = (File) map.get("file");
		String fName = file.getName();
		// 获取后缀名前的分隔符"."在fName中的位置。
		int dotIndex = fName.lastIndexOf(".");
		/* 获取文件的后缀名 */
		String end = fName.substring(dotIndex, fName.length()).toLowerCase();
		if (file.isDirectory())
		{
			try
			{
				loadFiles(file);
			}
			catch (Exception e)
			{
				Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
			}
		}
		else
		{
			if (end.toLowerCase().equals(".png"))
			{
				openFile(file);
			}
			else
			{
				showResultDetail(file);
			}
		}
	}
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
								   int position, long id)
	{
		// TODO Auto-generated method stub
		Map<String, Object> map = (Map<String, Object>) parent
				.getItemAtPosition(position);
		final File file = (File) map.get("file");
		final String tempString = file.getName();
		int length = tempString.length();
		char[] buffer = new char[length - 4];
		tempString.getChars(0, length - 4, buffer, 0);
		final String fileName = String.valueOf(buffer);
		AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle("Operate")
				//				.setIcon(R.drawable.alerticon)
				.setItems(new String[] { "details", "delete" },
						new DialogInterface.OnClickListener()
						{

							@Override
							public void onClick(DialogInterface dialog,
												int which)
							{
								File resultFile;
								// TODO Auto-generated method stub
								switch (which)
								{
									case 0:
										resultFile = new File(mFilePath + "/" + fileName + ".txt");
										showResultDetail(resultFile);
										break;

									case 1:
										int a =tempString.indexOf(".");
										File mFileName;
										String str = tempString.substring(a);
										System.err.println("index:"+a+"str:"+str);
										if(str.equals(".png")){
											String path = mFilePath + "/" + fileName + ".png";
											mFileName = new File(path);
											if(mFileName.exists()){
												try {
													mFileName.delete();
												} catch (Exception e) {
													e.printStackTrace();
												}
												finish();
												overridePendingTransition(0,0);
												Intent intent =new Intent(ResultExplorerActivity.this,ResultExplorerActivity.class);
												startActivity(intent);

												Toast.makeText(ResultExplorerActivity.this,"Image file deleted successfully!",Toast.LENGTH_SHORT).show();
											}
											else{
												Toast.makeText(ResultExplorerActivity.this,"File does not exist！",Toast.LENGTH_SHORT).show();
											}
										}else{
										String path = mFilePath + "/" + fileName + ".txt";
										mFileName =new File(path);
										if(mFileName.exists()){
											try {
												mFileName.delete();
											} catch (Exception e) {
												e.printStackTrace();
											}
											Toast.makeText(ResultExplorerActivity.this,"Text file deleted successfully!",Toast.LENGTH_SHORT).show();
											finish();
											overridePendingTransition(0,0);
											Intent intent =new Intent(ResultExplorerActivity.this,ResultExplorerActivity.class);
											startActivity(intent);
										}
										else{
											Toast.makeText(ResultExplorerActivity.this,"File does not exist！",Toast.LENGTH_SHORT).show();
										}
									}
										//s
//					String msg = "";
//					try
//					{
//						resultFile = new File(mFilePath + "/"
//								+ fileName + ".txt");
//
//						if (resultFile.exists())
//						{
//							FileInputStream in;
//							in = new FileInputStream(resultFile);
//							int size;
//							size = in.available();
//							byte[] buf = new byte[size];
//							in.read(buf);
//							in.close();
//							msg = new String(buf);
//						}
//						else
//						{
//							showToast("结果文件：" + fileName
//									+ ".txt不存在!");
//						}
//					}
//					catch (FileNotFoundException e)
//					{
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					catch (IOException e)
//					{
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//
//					// 短信
//					// Intent intent = new
//					// Intent(Intent.ACTION_SENDTO,
//					// Uri.parse("smsto:" + ""));
//					// intent.putExtra("sms_body", "");
//					// startActivity(intent);
//
//					// 彩信
//					Intent intent = new Intent(
//							Intent.ACTION_SEND);
//					Uri uri;
//					// intent.putExtra("address", "10086");
//					intent.putExtra("sms_body", msg);
//					intent.putExtra("subject", fileName);
//					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//					intent.setClassName("com.android.mms",
//							"com.android.mms.ui.ComposeMessageActivity");
//					resultFile = new File(mFilePath + "/" + fileName + ".png");
//					if (resultFile.exists())
//					{
//						uri = Uri.parse("file://" + mFilePath + "/"
//								+ fileName + ".png");
//						intent.putExtra(Intent.EXTRA_STREAM, uri);
//						intent.setType("image/*");
//					}
//					Log.d("Intent","png");
//					startActivity(intent);
										break;

									default:
										break;
								}
							}
						}).create();
		dialog.show();
		return false;
	}

	private boolean showResultDetail(File file)
	{
		if (!file.exists())
		{
			showToast("Result file：" + file.getName()	+ "not exist!");
			return false;
		}

		LayoutInflater factory = LayoutInflater.from(ResultExplorerActivity.this);
		final View view = factory.inflate(R.layout.view_result, null);
		mAlertListView = (ListView) view.findViewById(R.id.list_result);
		mResultTextView1 = (TextView)view.findViewById(R.id.result_statistic1);
		mResultTextView2 = (TextView)view.findViewById(R.id.result_statistic2);
		loadResult(file);
		AlertDialog dlg = new AlertDialog.Builder(ResultExplorerActivity.this)
				.setTitle("Result details")
				//				.setIcon(R.drawable.alerticon)
				.setView(view)
				.setPositiveButton("Ok", null)
				.create();
		dlg.show();
		return true;
	}

	public void showToast(CharSequence msg)
	{
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
	private SimpleAdapter adapter;
	private void loadFiles(File dir)
	{
		List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
		if (dir != null)
		{
			// 处理上级目录
			if (!dir.getAbsolutePath().equals(mRootDir.getAbsolutePath()))
			{
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("file", dir.getParentFile());
				map.put("filename", "上一级目录");
				map.put("icon", R.drawable.folder);
				listItems.add(map);
			}
			// currentDir.setText(dir.getAbsolutePath());
			File[] files = dir.listFiles();
			sortFiles(files);
			if (files != null)
			{
				for (File f : files)
				{
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("file", f);
					map.put("filename", f.getName());
					map.put("icon",
							f.isDirectory() ? R.drawable.folder
									: (f.getName().toLowerCase()
									.endsWith(".txt") ? R.drawable.text
									: R.drawable.image));
					// 获取文件的最后修改日期
					long modTime = f.lastModified();
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");

					Date date = new Date(modTime);
					// 添加一个最后修改日期
					double length = getLength(f) * 1.0 / 1024;
					DecimalFormat df = new DecimalFormat("0.00");
					String propString = dateFormat.format(date) + " "
							+ df.format(length) + "KB";
					map.put("modify", propString);

					listItems.add(map);
				}
			}

		}
		else
		{
			Toast.makeText(this, "目录不正确，请输入正确的目录!", Toast.LENGTH_LONG).show();
		}
		 adapter = new SimpleAdapter(ResultExplorerActivity.this,
				listItems, R.layout.list_item, new String[] { "filename",
				"icon", "modify" }, new int[] { R.id.file_name,
				R.id.icon, R.id.file_modify });

		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		mListView.setAdapter(adapter);
		try
		{
			mCurrentDir.setText("结果文件路径:" + dir.getCanonicalPath());
			mCurrentDir.setVisibility(View.GONE);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void sortFiles(File[] files)
	{
		Arrays.sort(files, new Comparator<File>()
		{
			public int compare(File file1, File file2)
			{
				if (file1.isDirectory() && file2.isDirectory())
					return 1;
				if (file2.isDirectory())
					return 1;
				return -1;
			}
		});
	}

	private long getLength(File file)
	{
		long totaLength = 0;
		if (file.isDirectory())
		{
			File[] files = file.listFiles();
			if (files == null)
			{
				return 0;
			}
			for (File file2 : files)
			{
				totaLength = totaLength + getLength(file2);
			}
		}
		else
		{
			totaLength = totaLength + file.length();
		}
		return totaLength;
	}

	private void loadResult(File resultFile)
	{
		List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
		if (resultFile != null)
		{
			do
			{
				byte[] buf = new byte[4];
				int retLen;
				int index = 0;
				int val;
				int targetNumber = 0;
				////首先读入"探测次数"(4字节)
				try
				{
					FileInputStream in;

					in = new FileInputStream(resultFile);

					do
					{
						Map<String, Object> map = new HashMap<String, Object>();
						retLen = in.read(buf, 0, 4);
						if(retLen != 4)
						{
							break;
						}

						String cntString = "Number:" + new String(buf);

						map.put("cnt", cntString);
						////读入一个字节的空格
						retLen = in.read(buf, 0, 1);
						if(retLen != 1)
						{
							break;
						}

						////读入"探测结果"(4字节)
						for (int i = 0; i < 4; i++)
						{
							buf[i] = 0;
						}

						retLen = in.read(buf, 0, 4);
						if(retLen != 4)
						{
							break;
						}
						String disString = new String(buf);
						val = Integer.valueOf(disString.toString());
						disString = "Result(cm):" + disString;

						if(val == 0)
						{
							disString = "Result(cm):no target";
						}
						else
						{
							targetNumber++;
						}
						map.put("distance", disString);
						////读入回车和换行
						retLen = in.read(buf, 0, 1);
						/////
						index++;

						listItems.add(map);
					}while(retLen != 0);

					in.close();
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				String txt = "total detection " + String.valueOf(index) + " number";
				mResultTextView1.setText(txt);
				txt = "total detection target " + String.valueOf(targetNumber) + " number";
				mResultTextView2.setText(txt);
			}while(false);

			SimpleAdapter adapter = new SimpleAdapter(ResultExplorerActivity.this,
					listItems, R.layout.list_item_result, new String[] { "cnt",
					"distance"}, new int[] { R.id.result_cnt,
					R.id.result_distance});

			mAlertListView.setAdapter(adapter);
		}
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();

		if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}

	private void openFile(File file) {
		Log.d("Intent","enter");
		Intent intent = new Intent(this,PicActivity.class);
		intent.putExtra("pic", file.getAbsolutePath());
//		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		// 设置intent的Action属性
//		intent.setAction(Intent.ACTION_VIEW);
		// 获取文件file的MIME类型
//		String type = getMIMEType(file);
//		// 设置intent的data和Type属性。
//		Uri uri;
//		uri = FileProvider.getUriForFile(this,"com.ltd.lifesearch_xa.provider",file);
//		intent.setDataAndType(uri, type);
		// 跳转
		startActivity(intent);
//		Log.d("Intent","Url:  "+Uri.fromFile(file)+" file: "+file+ "type:"+type);
	}

	private String getMIMEType(File file) {

		String type = "*/*";
		String fName = file.getName();
		// 获取后缀名前的分隔符"."在fName中的位置。
		int dotIndex = fName.lastIndexOf(".");
		if (dotIndex < 0)
		{
			return type;
		}
		/* 获取文件的后缀名 */
		String end = fName.substring(dotIndex, fName.length()).toLowerCase();
		if (end == "")
			return type;
		// 在MIME和文件类型的匹配表中找到对应的MIME类型。
		for (int i = 0; i < MIME.MIME_MapTable.length; i++)
		{
			if (end.equals(MIME.MIME_MapTable[i][0]))
				type = MIME.MIME_MapTable[i][1];
		}
		return type;
	}
}
