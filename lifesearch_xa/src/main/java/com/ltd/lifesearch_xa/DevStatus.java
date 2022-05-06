package com.ltd.lifesearch_xa;

public class DevStatus {

	//设备状态
	public short mType;            //设备状态，如上宏所示
	public short mRangeUp;     	//探测范围起始位置
	public short mRangeDown;       //探测范围终止位置
	//电池信息
	public boolean  mErrBattery;          //电池错误
	public boolean  mBatteryUse;          //电池是否正在使用 1:正在使用 0:正在使用外接电源
	public long mBatteryLeftTime;         //电池剩余的时间
}
