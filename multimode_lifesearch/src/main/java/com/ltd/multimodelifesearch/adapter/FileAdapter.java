package com.ltd.multimodelifesearch.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ltd.multimode_lifesearch.R;
import com.ltdpro.DebugUtil;
import com.ltdpro.MyApplication;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class FileAdapter extends BaseAdapter {
	private static final String TAG = "FileAdapter";
	//����viewʱ����Ҫ�ṩcontext
	public Activity activity;
	//�ṩ����Դ���ļ��б�
	public List<File> list=new LinkedList<File>();
	//��ǰ�б�·��
	public String currPath;
	private Bitmap bmp_folder,bmp_file;
	private MyApplication app=null;

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View arg1, ViewGroup arg2) {
		//����һ����ͼװ��listView��Ŀ
		View v= View.inflate(activity, R.layout.file_list, null);
		TextView textPath=(TextView) v.findViewById(R.id.text_path);
		TextView textSize=(TextView) v.findViewById(R.id.text_size);
		ImageView img=(ImageView) v.findViewById(R.id.imageView1);
		//��ȡ��ǰλ��
		File f=list.get(position);
		//��ȡ�ļ������ļ���С
		textPath.setText(f.getName());
		textSize.setText("("+getFileSize(f)+")");
		//ʶ�����ļ������ļ��У���ʾ����ͬ��ͼƬ
		if(f.isDirectory()){
			img.setImageBitmap(bmp_folder);
		}else 
			img.setImageBitmap(bmp_file);
		//������ͼ
		return v;
	}

	//�õ��ļ���С
	private static String getFileSize(File f) {
		//��������
		int sub_index=0;
		String show="";
		//�����ļ���С
		if(f.isFile()){
			long length=f.length();

			if( length >= 1073741824 )
			{
				sub_index= String.valueOf((float)length / 1073741824).indexOf(".");
				show= ((float) length/1073741824 + "000").substring(0, sub_index + 3)+ "GB";
			}
			else if( length >= 1048576 )
			{
				sub_index=(String.valueOf((float)length / 1048576)).indexOf(".");
				show=((float)length/1048576+"000").substring(0,sub_index+3)+"MB";
			}
			else if(length>=1024)
			{
				sub_index=(String.valueOf((float)length / 1024)).indexOf(".");
				show=((float)length/1024+"000").substring(0,sub_index+3)+"KB";
			}
			else if(length<1024)
				show= String.valueOf(length) + "B";
		}
		return show;
	}

	//ɨ���ļ���
	//��������0503hss
	public void scanFiles(String path) {
		list.clear();
		File dir=new File(path);
		File[] subFiles=dir.listFiles();
		
		//�����ļ��б�
		if(subFiles!=null)
		{
			for(File f:subFiles)
			{
				boolean isD = f.isDirectory();
				if( !isD && isLteFile(f) && getFileNum(f) != null )
					list.add(f);
				else
				{
					DebugUtil.i(TAG, "folder!");
				}
			}
		}
		else;
		//����ûд��hss20170504
		if(app.getisDecendOrder())
		{
			SortByDescend sortdescend = new SortByDescend();		
			Collections.sort(list, sortdescend);
		}
		else
		{
			SortByAscend sortascend = new SortByAscend();
			Collections.sort(list, sortascend);
		}

		for(int i = 0;i < list.size();i++)
		{
			DebugUtil.i(TAG, "list[" + i + "]=" + list.get(i));
		}

		this.notifyDataSetChanged();		
		currPath=path;
	}

	public FileAdapter(Activity activity){
		this.activity=activity;
		//����ʾ�ļ�ͼ��
		bmp_folder= BitmapFactory.decodeResource(activity.getResources(), R.drawable.bmp_folder);
		bmp_file= BitmapFactory.decodeResource(activity.getResources(), R.drawable.bmp_file);
		app=(MyApplication) activity.getApplicationContext();
	}

	//����
	class SortByAscend implements Comparator<File> {
		@Override
		public int compare(File f1, File f2) {
			String s2 = getFileNum(f2);
			String s1 = getFileNum(f1);
	        return (Integer.valueOf(s1) - Integer.valueOf(s2));
	    }	
	}
	
	//��׺�ж�
	public boolean isLteFile(File inputf)
	{
		String s = inputf.getName();
		String prefix = s.substring(s.lastIndexOf(".") + 1);
		if( prefix.equalsIgnoreCase("lte"))
			return true;
		else
			return false;
	}
	
	//�õ��ļ��м������
	public String getFileNum(File f1)
	{
		String s1 = f1.getName().toString();
		if( s1.lastIndexOf(".") > 0 )
			s1 = s1.substring(0, s1.lastIndexOf("."));	
		else
			return null;
		if( s1.lastIndexOf("e") > 0 )
			s1 = s1.substring(s1.lastIndexOf("e")+1);
		else 
			return null;
		return s1;
	}	
	
	class SortByDescend implements Comparator<File>
	{
		@Override
		public int compare(File f1, File f2)
		{
			String s1 = getFileNum(f1);
			String s2 = getFileNum(f2);
	        return (Integer.valueOf(s2) - Integer.valueOf(s1));
		}
	}	
	
}
