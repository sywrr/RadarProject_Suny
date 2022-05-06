package com.ltd.multimodelifesearch.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ltd.multimode_lifesearch.R;

import java.util.List;

public class ListGroupAdapter extends BaseAdapter
{ 
	private String TAG="ListGroupAdapter";
	private Context context;
	public int mCheckIndex = -1;
	private List<String> list;

	public ListGroupAdapter(Context context, List<String> list)
	{
		this.context = context;
		this.list = list;
	}

	public void setSelectIndex(int index)
	{
		//È¡ÏûÑ¡Ôñ
		if(mCheckIndex == index);
			//mCheckIndex = -1;
		else
			mCheckIndex = index;
	}
	
	public void deleteCheckItem()
	{
		list.remove(mCheckIndex);
		mCheckIndex = -1;
	}
	
	@Override
	public int getCount() 
	{
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		Log.i(TAG, "getItem");
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup)
	{	
		convertView= LayoutInflater.from(context).inflate(R.layout.layout_group_item_view, null);
		TextView button;
		ImageView image;
		button = (TextView)convertView.findViewById(R.id.groupitem_txt);
		image =(ImageView)convertView.findViewById(R.id.groupitem_image);
		
		if(position == mCheckIndex)
			image.setVisibility(View.VISIBLE);
		else
			image.setVisibility(View.INVISIBLE);
		button.setText(list.get(position));

		return convertView;
	}

	static class ViewHolder {
		TextView groupItem;
	}
}
