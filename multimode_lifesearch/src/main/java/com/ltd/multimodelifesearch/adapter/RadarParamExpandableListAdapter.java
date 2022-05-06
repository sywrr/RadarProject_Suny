package com.ltd.multimodelifesearch.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ltd.multimode_lifesearch.R;
import com.ltdpro.DebugUtil;
import com.ltdpro.MyApplication;

public class RadarParamExpandableListAdapter extends BaseExpandableListAdapter
{
	private String[] str_group_items;
	private String[][] str_child_items;
	private Context mContext;
	private int mTab = 0;   //指示当前列表显示的是哪一个内容测试
	private String TAG = "RadarParamExpandableListAdapter";
	/*
	 * 0:第一层
	 * 1:雷达参数  层
	 * 2:探测方式  层
	 * 3:实时处理  层
	 * 4:显示方式  层
	 */

	/**
	 * @param context
	 * @param groupItems
	 * @param childItems
	 */
	public RadarParamExpandableListAdapter(Context context, String[] groupItems, String[][] childItems, int tab)
	{
		mContext = context;
		str_group_items = groupItems;
		str_child_items = childItems;
		mTab = tab;
	}

	/**
	 * 
	 */
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return str_child_items[groupPosition][childPosition];
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		return str_child_items[groupPosition].length;
	}

	/**
	 * 子条目项
	 */
	private boolean mbEditWheelExtend = false;
	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                             ViewGroup parent) {
		// TODO 
		TextView txt_child = null;
		TextView txt_child1 = null;

		String title1 = ">";
		MyApplication app = (MyApplication)mContext.getApplicationContext();
		//DebugUtil.i(TAG,"1.getChildView,groupPosition:="+groupPosition+";childPosition:="+childPosition+";mTab:="+mTab);

		////对  雷达操作  项进行处理
		boolean isVisible=true;    //2016.6.10
		ImageView imageView = null; 	//图像显示
		switch(mTab)
		{
		//主菜单下
		case 0:
			switch( groupPosition )
			{
			case 0:
				switch( childPosition )
				{
				case 0:
					convertView = LayoutInflater.from(mContext).inflate(R.layout.radarset_group0_child_item, null);
					txt_child = (TextView)convertView.findViewById(R.id.id_child_txt);
					txt_child.setText(str_child_items[groupPosition][childPosition]);
					break;
				default:
					break;
				}
				break;
			case 1:
				break;
				//系统设置
			case 2:
				switch( childPosition )
				{
				//保存设置
				case 0:
					break;	
					//背景灯
				case 1:
					break;
				}
				break;
			default:
				break;
			}
			break;
			//雷达参数 层下，此时无子项
		case 1:
			break;
			//探测方式 层下，此时对 测距仪控制  层下的子视图进行处理
		case 2:
			convertView = LayoutInflater.from(mContext).inflate(R.layout.radarset_child_item, null);
			txt_child1 = (TextView)convertView.findViewById(R.id.iv_push);
			if(groupPosition == 2)
			{
				switch(childPosition)
				{
				//测距仪型号
				case 0:
					title1 = " "+app.mRadarDevice.getWhell_SelectTypeName()+" >";
					break;
					//测距仪扩展
				case 1:
					title1 = ""+app.mRadarDevice.getWheelExtendNumber();
					//DebugUtil.i("ExtendNumb","childList中标记扩展="+app.mRadarDevice.getWheelExtendNumber());
					break;
				case 2:
					title1 = " "+app.setDigits(app.mRadarDevice.getTouchDistance(),2);
					txt_child1.setText(title1);
					//测距仪标定
				case 3:
					break;
					/*
					//丢弃回退数据  2016.6.10
				case 4:
					if(!app.mRadarDevice.isWhellMode())
						isVisible = false;
					break;
					 */
				default:
					break;
				}				
			}			
			break;
		case 3:
			break;
		case 4:
			break;
		default:
			break;
		}

		if( groupPosition != 0  )
		{
			convertView = LayoutInflater.from(mContext).inflate(R.layout.radarset_child_item, null);

			if(!isVisible)
				convertView.setVisibility(View.INVISIBLE);
			txt_child = (TextView)convertView.findViewById(R.id.id_child_txt);
			txt_child.setText(str_child_items[groupPosition][childPosition]);

			txt_child1 = (TextView)convertView.findViewById(R.id.iv_push);
			txt_child1.setText(title1);

			//设置测距轮的上下加减
			if( groupPosition == 2 && childPosition == 1 && mTab == 2)
			{
				txt_child1.setTextSize(20);
				txt_child1.setText(": ▲"+title1+"▼");
				if( app.mRadarDevice.isSetting_ExtendNum_Command() )
				{					
					txt_child1.setTextColor(Color.RED);
					this.mbEditWheelExtend = true;
				}
				else//默认恢复黑色
				{
					if( mbEditWheelExtend )
					{
						//保存参数
						//根据选择的测距仪型号分别保存
						if( app.mRadarDevice.getWheeltypeSel() == 5 )
						{
							DebugUtil.i(TAG, "==mManusetWhelltypeIndex!");
							String filePath = app.mRadarDevice.INNERSTORAGE
                                              + app.mRadarDevice.mParamsFilefolderPath
                                              + app.mRadarDevice.getCustomFileName();
							DebugUtil.i("ExtendNumb", "filePath="+filePath);	

							//0918注释掉
							//mApp.mRadarDevice.saveWhellcheckParams(filePath);
							//使用自定测距轮的保存方式
							app.mRadarDevice.saveCustomWheelExtend(filePath);
						}
						else
						{
							DebugUtil.i(TAG, "！=mManusetWhelltypeIndex!");
							app.mRadarDevice.saveDefaultCheckParamsFile();
						}
						this.mbEditWheelExtend = false;
					}
					else;
				}
			}
			else;

			//设置背景灯
			if( mTab == 0 && groupPosition == 2 && childPosition == 1 )
			{
				//DebugUtil.i(TAG, "radarAdapter light!");
				convertView = LayoutInflater.from(mContext).inflate(R.layout.radarset_childwithimage, null);
				txt_child = (TextView)convertView.findViewById(R.id.id_child_txt);
				txt_child.setText(str_child_items[groupPosition][childPosition]);
				imageView = (ImageView)convertView.findViewById(R.id.imgv_state);
				if( app.getPowerLightState() )
					imageView.setVisibility(View.VISIBLE);
				else
					imageView.setVisibility(View.INVISIBLE);
			}
			else;
		}
		else;		

		convertView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.childlistbackground));
		//		convertView.setBackgroundColor(color.blue);
		//		DebugUtil.i("DataTime","Data5.time childView");
		return convertView;
	}

	/**
	 * group
	 */
	@Override
	public Object getGroup(int groupPosition)
	{
		// TODO Auto-generated method stub
		return str_group_items[groupPosition];
	}

	@Override
	public int getGroupCount() 
	{
		// TODO Auto-generated method stub
		return str_group_items.length;
	}

	@Override
	public long getGroupId(int groupPosition) 
	{
		// TODO Auto-generated method stub
		return groupPosition;
	}

	/**
	 * 得到父类组视图
	 */
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		//DebugUtil.i(TAG,"0.getGroupView");
		//		DebugUtil.i("DataTime","Data2.time GroupView");
		ViewHolderGroup holdergroup = null;
		// TODO Auto-generated method stub
		TextView txt_group;
		TextView txtView;
		ImageView imageView;
		ImageView iv_state;
		String titleTxt;
		MyApplication app=(MyApplication) mContext.getApplicationContext();
		titleTxt = str_group_items[groupPosition];
		//		convertView.setBackgroundColor(color.red);
		//////////
		int val;
		//		DebugUtil.i(TAG,"1.getGroupView,groupPosition:="+groupPosition+";mTab:="+mTab);

		switch(mTab)
		{
		//第一个页面:主菜单
		case 0:
			switch(groupPosition)
			{
			//雷达操作
			case 0:	
				convertView = LayoutInflater.from(mContext).inflate(R.layout.radarset_group0_item, null);
				//设置雷达状态指示图标
				iv_state = (ImageView)convertView.findViewById(R.id.imgv_state);

				if(app.mRadarDevice.isRunningMode())
					iv_state.setBackgroundResource(R.drawable.greenpoint);
				else
					iv_state.setBackgroundResource(R.drawable.redpoint);
				break;
			default:
				convertView = LayoutInflater.from(mContext).inflate(R.layout.radarset_group_item, null);
				break;
			}
			break;
			//第二个页面：雷达参数
		case 1:
			//app.setIsHardplusRun(false);
			if(convertView == null)
			{
				holdergroup = new ViewHolderGroup();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.radarparam_groupwithparam, null);
				holdergroup.group_txtView = (TextView)convertView.findViewById(R.id.iv_push);
				holdergroup.group_txt_group = (TextView)convertView.findViewById(R.id.id_group_txt);
				convertView.setTag(holdergroup);
			}
			else
			{
				holdergroup = (ViewHolderGroup)convertView.getTag();
				holdergroup.group_txtView.setTextColor(mContext.getResources().getColor(R.color.green_blue));	
				holdergroup.group_txt_group.setTextColor(mContext.getResources().getColor(R.color.green_blue));	
				holdergroup.group_txtView.setText("");				
			}

			switch(groupPosition)
			{
			//恢复参数
			case 0:
				holdergroup.group_txtView.setText("");
				break;
				//扫描速度
			case 1:
				val = app.mRadarDevice.getScanSpeed();
				holdergroup.group_txtView.setText(""+val+" 道/秒");
				if(app.mRadarDevice.isSetting_Scanspeed_Command())
				{
					holdergroup.group_txtView.setTextColor(Color.RED);
				}
				break;
				//时窗设置
			case 2:
				val = app.mRadarDevice.getTimeWindow();
				holdergroup.group_txtView.setText(""+val+" ns");
				if(app.mRadarDevice.isSetting_Timewindow_Command())
				{
					holdergroup.group_txtView.setTextColor(Color.RED);
				}
				break;
				//采样点数
			case 3:
				val = app.mRadarDevice.getScanLength();
				holdergroup.group_txtView.setText(""+val+" 点/道");
				if(app.mRadarDevice.isSetting_Scanlength_Command())
				{
					holdergroup.group_txtView.setTextColor(Color.RED);
				}
				break;
				//信号位置
			case 4:
				val = app.mRadarDevice.getSignalpos();
				holdergroup.group_txtView.setText(""+val);
				if(app.mRadarDevice.isSetting_Singlepos_Command())
				{
					holdergroup.group_txtView.setTextColor(Color.RED);
				}
				break;
				//自动增益
			case 5:
				if(app.mRadarDevice.isAutoHardplus())
				{
					holdergroup.group_txtView.setText("是");
				}
				else
				{
					holdergroup.group_txtView.setText("否");
				}
				break;
				//整体增益
			case 6:
				if(app.mRadarDevice.isSetting_AllHardPlus_Command())
				{
					holdergroup.group_txt_group.setTextColor(Color.RED);
				}
				else;
				break;
				//分段增益
			case 7:
				if(app.mRadarDevice.isSetting_StepHardPlus_Command())
				{
					holdergroup.group_txt_group.setTextColor(Color.RED);
				}
				else;
				break;
				//滤波
			case 8:
				//val = app.mRadarDevice.getFilterSel();
				holdergroup.group_txtView.setText(app.mRadarDevice.getFilterStr());//("第"+(val+1)+"级滤波");
				if(app.mRadarDevice.isSetting_Filter_Command())
				{
					holdergroup.group_txtView.setTextColor(Color.RED);
				}
				break;
				//介电常数
			case 9:
				float val1 = app.mRadarDevice.getJieDianConst();
				holdergroup.group_txtView.setText(""+val1);
				if(app.mRadarDevice.isSetting_JieDianConst_Command())
				{
					holdergroup.group_txtView.setTextColor(Color.RED);
				}
				break;
			default:
				break;
			}
			break;

			//第三个页面：探测方式
		case 2:
			convertView = LayoutInflater.from(mContext).inflate(R.layout.radarset_groupwithimage, null);
			imageView = (ImageView)convertView.findViewById(R.id.imgv_state);
			imageView.setVisibility(View.INVISIBLE);
			switch(groupPosition)
			{
			//连续测量模式
			case 0:
				if(app.mRadarDevice.isTimeMode())
				{
					imageView.setVisibility(View.VISIBLE);
				}
				break;
			//人工点测模式
			case 1:	
				if(app.mRadarDevice.isDianCeMode())
				{
					//DebugUtil.i("LFragment", "isDianceMode!");
					txt_group = (TextView)convertView.findViewById(R.id.id_group_txt);
					if(app.mRadarDevice.isSetting_DianCe_Command())
						txt_group.setTextColor(Color.RED);
					val = app.mRadarDevice.getDianceNumber();
					titleTxt += "  "+val+"次";
					imageView.setVisibility(View.VISIBLE);
				}
				break;
			//测距轮模式
			case 2:
				txt_group = (TextView)convertView.findViewById(R.id.id_group_txt);
				if(app.mRadarDevice.isWhellMode())
				{
					double valD = app.mRadarDevice.getTouchDistance();
					//titleTxt += "("+valD+"厘米)";
					imageView.setVisibility(View.VISIBLE);
				}
				break;
			}
			break;
			//第四个页面：实时处理
		case 3:
			convertView = LayoutInflater.from(mContext).inflate(R.layout.radarset_groupwithimage, null);
			imageView = (ImageView)convertView.findViewById(R.id.imgv_state);

			imageView.setVisibility(View.INVISIBLE);
			switch(groupPosition)
			{
			//道间平均
			case 0:
				val = app.mRadarDevice.getAveNumber();
				txt_group = (TextView)convertView.findViewById(R.id.id_group_txt);
				if(app.mRadarDevice.isSetting_AveScan_Command())
					txt_group.setTextColor(Color.RED);
				titleTxt += "  "+val+"次";
				break;
				//背景消除
			case 1:
				boolean isRem=app.mRadarDevice.isRemback();
				if(isRem)
					imageView.setVisibility(View.VISIBLE);
				else
					imageView.setVisibility(View.INVISIBLE);
				break;
			}
			break;
			//第五个页面：显示方式
		case 4:
			convertView = LayoutInflater.from(mContext).inflate(R.layout.radarset_groupwithimage, null);
			imageView = (ImageView)convertView.findViewById(R.id.imgv_state);

			imageView.setVisibility(View.INVISIBLE);
			switch(groupPosition)
			{
			//伪彩色图
			case 0:
				if(app.mRadarDevice.isDIBShow())
					imageView.setVisibility(View.VISIBLE);
				break;
				//堆积图
			case 1:
				if(app.mRadarDevice.isWiggleShow())
					imageView.setVisibility(View.VISIBLE);
				break;
				//转换调色板
			case 2:
				if(app.mRadarDevice.isSetting_SelectColor_Command())
				{
					txt_group = (TextView)convertView.findViewById(R.id.id_group_txt);
					txt_group.setTextColor(Color.RED);
				}
				break;
			}
			break;
		default:
			convertView = LayoutInflater.from(mContext).inflate(R.layout.radarparam_groupwithparam, null);
			break;
		}
		/*
		if( mTab == 0 && groupPosition != 0 )
		{

		}
		else if( mTab !=0 )
		{
			convertView = LayoutInflater.from(mContext).inflate(R.layout.radarparam_groupwithparam, null);
		}
		 */
		//
		txt_group = (TextView)convertView.findViewById(R.id.id_group_txt);
		txt_group.setText(titleTxt);

		convertView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.listbackground));
		//
		//		DebugUtil.i("DataTime","Data3.time GroupView");
		return convertView;
	}

	public final class ViewHolder
	{
		TextView child_txt_child = null;
		TextView child_txt_child1 = null;
		TextView child_txt_child2 = null;
	}

	public final class ViewHolderGroup
	{
		TextView group_txt_group = null;
		TextView group_txtView = null;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1) 
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean hasStableIds() 
	{
		// TODO Auto-generated method stub
		return false;
	}	

}