package com.ltd.lifesearch_xa;

public class DevResult {
	//̽����
	public short mTargetNum;
	public short mMiddleTargetNum;                    //�м�����Ŀ >0���м�����<=0���м���
	public RESULT[] mResult = new RESULT[20];         //ǰʮ��Ϊ���ս������ʮ��Ϊ�м���
	
	public DevResult()
	{
		super();
		for (int i = 0; i < mResult.length; i++)
		{
			mResult[i] = new RESULT();
		}
	}
}
