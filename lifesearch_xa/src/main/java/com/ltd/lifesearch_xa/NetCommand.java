package com.ltd.lifesearch_xa;

public class NetCommand {
	short mComCode;      //�������
	short mParamLen;     //��������
	byte[] mParams = new byte[40];   //��������
	
	public NetCommand()
	{
//		mLength = 0;
//		for(int i=0;i<mMaxLength;i++)
//			mParams[i] = 0;
	}
	//���������
	public void setCommandLength(int length)
	{
//		mLength = (short) length;
	}
	//��������
	public void setCommand(short command)
	{
//		mCommand = command;
	}
	//���ò���
	public void setCommandParams(short[] params,int length)
	{
//		int i;
//		for(i=0;i<length;i++)
//			mParams[i] = params[i];
	}
	//�õ�����
	public short getCommandCode()
	{
		return mComCode;
	}
	//�õ��������
	public byte[] getCommandParams()
	{
		return mParams;
	}
}
