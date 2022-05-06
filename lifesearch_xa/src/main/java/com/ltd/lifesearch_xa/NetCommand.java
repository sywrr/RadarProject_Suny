package com.ltd.lifesearch_xa;

public class NetCommand {
	short mComCode;      //命令代码
	short mParamLen;     //参数长度
	byte[] mParams = new byte[40];   //参数数组
	
	public NetCommand()
	{
//		mLength = 0;
//		for(int i=0;i<mMaxLength;i++)
//			mParams[i] = 0;
	}
	//设置命令长度
	public void setCommandLength(int length)
	{
//		mLength = (short) length;
	}
	//设置命令
	public void setCommand(short command)
	{
//		mCommand = command;
	}
	//设置参数
	public void setCommandParams(short[] params,int length)
	{
//		int i;
//		for(i=0;i<length;i++)
//			mParams[i] = params[i];
	}
	//得到命令
	public short getCommandCode()
	{
		return mComCode;
	}
	//得到命令参数
	public byte[] getCommandParams()
	{
		return mParams;
	}
}
