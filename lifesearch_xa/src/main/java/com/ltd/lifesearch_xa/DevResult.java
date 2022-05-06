package com.ltd.lifesearch_xa;

public class DevResult {
	//探测结果
	public short mTargetNum;
	public short mMiddleTargetNum;                    //中间结果数目 >0有中间结果，<=0无中间结果
	public RESULT[] mResult = new RESULT[20];         //前十个为最终结果，后十个为中间结果
	
	public DevResult()
	{
		super();
		for (int i = 0; i < mResult.length; i++)
		{
			mResult[i] = new RESULT();
		}
	}
}
