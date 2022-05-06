package com.ltd.lifesearchapp;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;

import android.R.integer;
import android.os.Message;
import android.util.Log;

public class NetPacket extends Object
{
	//��ARM
	private final static int mMaxBufLength = 16384;
	//��ARM
//	private final static int mMaxBufLength = 1024;    //8192;//4096;
//	private int mSndLength = 16;
	////�������ݰ�����
	private int mPackFlag        = 0xAAAABBBB;  //String mPackFlag="netpacket";      //ÿһ�����ݰ��ı�־
	//���ݰ�����
	private int mCommandPackType  = 5;    //"commpack";   //�����
	private short mAckPackType      = 3;    //Ӧ���
	private int mWavePackType     = Global.WAVE_PACK;    //�������ݰ�
	private int mNetCheckPackType = 9;    //������
	//
//	private int mPackLength      = 0;     //���ݰ�����
	private int mHeadLength = 24;      //���ݰ�ͷ����      ��־(4�ֽ�)+����(4�ֽ�)+���ݰ�����(4�ֽ�)
	
	public  byte[] mHeadBuf = new byte[mHeadLength];
	public  byte[] mBuf = new byte[mMaxBufLength];
	private int mRcvLength = 0;
	private int mCommandLength = 128;   //�������ݰ�����
	//���ݰ�ͷ
	private short mTransType;      //�ô�����������������
	public short mPackType;       //�ô���������ͣ�����/����/Ӧ��/����ͷ/����β......
	private long mTransID;          //��������ID:˵���ð������ĸ�����
	private short mPackMaxLength;  //���ݰ�����󳤶�
	private int mTotalLength;      //�ܳ���
	private int mPackIndex;        //������:˵���ǵڼ������ݰ�
	private int mPackLength;       //���ݰ����ݳ���,��������ͷ
//	private int mPackType = 0;     //���ݰ�����
	////һ�����ݰ�������:mPackFlag + �������͡� + �������ݳ��� + ����
	private int mACKPackLength = mHeadLength;   //Ӧ�������
	private int mCommandPackLength = mHeadLength+mCommandLength;   //���������
	////
	public NetPacket()
	{
		int i;
		for(i=0;i<mMaxBufLength;i++)
			mBuf[i]=0;
		for(i=0;i<mHeadLength;i++)
			mHeadBuf[i]=0;
		mPackMaxLength = mMaxBufLength;
	}
	// ��ʼ�������
	public void Init(short trans_type, short packet_type, int packetLen, int transLen, int packetID, long transID)
	{
		mTransType = trans_type;
		mPackIndex = packetID;
		mPackLength = packetLen;
		mTotalLength = transLen;
		mTransID = transID;
		mPackType = packet_type;
		
		mHeadBuf[0] = (byte)(mTransType);
		mHeadBuf[1] = (byte)(mTransType >> 8);
		
		mHeadBuf[2] = (byte)(mPackType);
		mHeadBuf[3] = (byte)(mPackType >> 8);
		
		mHeadBuf[4] = (byte)(mTransID);
		mHeadBuf[5] = (byte)(mTransID >> 8);
		mHeadBuf[6] = (byte)(mTransID >> 16);
		mHeadBuf[7] = (byte)(mTransID >> 24);
		
		mHeadBuf[8] = (byte)(mPackMaxLength);
		mHeadBuf[9] = (byte)(mPackMaxLength >> 8);
		
		mHeadBuf[12] = (byte)(mTotalLength);
		mHeadBuf[13] = (byte)(mTotalLength >> 8);
		mHeadBuf[14] = (byte)(mTotalLength >> 16);
		mHeadBuf[15] = (byte)(mTotalLength >> 24);
		
		mHeadBuf[16] = (byte)(mPackIndex);
		mHeadBuf[17] = (byte)(mPackIndex >> 8);
		mHeadBuf[18] = (byte)(mPackIndex >> 16);
		mHeadBuf[19] = (byte)(mPackIndex >> 24);
		
		mHeadBuf[20] = (byte)(mPackLength);
		mHeadBuf[21] = (byte)(mPackLength >> 8);
		mHeadBuf[22] = (byte)(mPackLength >> 16);
		mHeadBuf[23] = (byte)(mPackLength >> 24);
	}
	
	public void SetHead(byte[] buf)
	{
		mTransType = (short)(getUByte(buf[0]) + (getUByte(buf[1]) << 8));
		mPackType = (short)(getUByte(buf[2]) + (getUByte(buf[3]) << 8));
		mTransID = (long)(getUByte(buf[4]) + (getUByte(buf[5]) << 8) + (getUByte(buf[6]) << 16) + (getUByte(buf[7]) << 24));
		mPackMaxLength = (short)(getUByte(buf[8]) + (getUByte(buf[9]) << 8));
		mTotalLength = (int)(getUByte(buf[12]) + (getUByte(buf[13]) << 8) + (getUByte(buf[14]) << 16) + (getUByte(buf[15]) << 24));
		mPackIndex = (int)(getUByte(buf[16]) + (getUByte(buf[17]) << 8) + (getUByte(buf[18]) << 16) + (getUByte(buf[19]) << 24));
		mPackLength = (int)(getUByte(buf[20]) + (getUByte(buf[21]) << 8) + (getUByte(buf[22]) << 16) + (getUByte(buf[23]) << 24));
	}
	private int getUByte(byte val) {
		int uVal = ((int)val) & 0xFF;
		return uVal;
	}
	////
	public int getMaxPackLength()
	{
		return mMaxBufLength;
	}
	////
	public byte[] getSndBuffer()
	{
		return mBuf;
	}
	public int getBufLength()
	{
		return mPackLength;
	}
	
	public int getTransferLength()
	{
		return mTotalLength;
	}
	
	public long getTransferID() {
		return mTransID;
	}
	
	public short getTransferType() {
		return mTransType;
	}
	
	////
	public void setDatas(short[] buf,int length)
	{
		int i;
		int index;
		
		if(length > mPackMaxLength)
			length = mPackMaxLength;
		mPackLength = length;
		
		mHeadBuf[20] = (byte)(mPackLength);
		mHeadBuf[21] = (byte)(mPackLength >> 8);
		mHeadBuf[22] = (byte)(mPackLength >> 16);
		mHeadBuf[23] = (byte)(mPackLength >> 24);
		
		int num = length / 2;
		for(i=0; i<num; i++)
		{
			index = i * 2;
			mBuf[index] = (byte)(buf[i]);
			mBuf[index + 1] = (byte)(buf[i] >> 8);
		}
//		mPackLength = length;
	}
	
	public void FillDatas(byte[] buf,int length)
	{
		int i;
		int index;
		
		if(length > mPackMaxLength)
			length = mPackMaxLength;
		mPackLength = length;
		
		mHeadBuf[20] = (byte)(mPackLength);
		mHeadBuf[21] = (byte)(mPackLength >> 8);
		mHeadBuf[22] = (byte)(mPackLength >> 16);
		mHeadBuf[23] = (byte)(mPackLength >> 24);
		
		int num = length;
		for(i=0; i<num; i++)
		{
			index = i;
			mBuf[index] = (byte)(buf[i]);
		}
//		mPackLength = length;
	}
	
	public void createWavePacket(short[] buf, int length)
	{
		Init(Global.TRANSFER_TYPE_WAVE, (short)mWavePackType, length, length, 0, 0);
		setDatas(buf, length);
	}
	
	/////
	private String TAG="NetPacket";
	//�����ݰ���ʼ��ΪackӦ���
	public void createACKPacket()
	{
		mPackLength = 0;
				
		//��������
		mHeadBuf[2] = (byte)mAckPackType;
		mHeadBuf[3] = (byte)(mAckPackType>>8);
				
		//���ó���
		mHeadBuf[20] = (byte)mPackLength;
		mHeadBuf[21] = (byte)(mPackLength>>8);
		mHeadBuf[22] = (byte)(mPackLength>>16);
		mHeadBuf[23] = (byte)(mPackLength>>24);
	}
	//�������������ݰ�
	public void createNETCheckPack()
	{
		mPackLength = 0;
		//�������ݰ���־
		mBuf[0] = (byte)mPackFlag;
		mBuf[1] = (byte)(mPackFlag>>8);
		mBuf[2] = (byte)(mPackFlag>>16);
		mBuf[3] = (byte)(mPackFlag>>24);
						
		//��������
		mBuf[4] = (byte)mNetCheckPackType;
		mBuf[5] = (byte)(mNetCheckPackType>>8);
		mBuf[6] = (byte)(mNetCheckPackType>>16);
		mBuf[7] = (byte)(mNetCheckPackType>>24);
						
		//���ó���
		mBuf[8] = (byte)mPackLength;
		mBuf[9] = (byte)(mPackLength>>8);
		mBuf[10] = (byte)(mPackLength>>16);
		mBuf[11] = (byte)(mPackLength>>24);
	}
	//��������
	private boolean SND_OK=true;        //��������
	private boolean SND_ERROR=false;    //���ͳ���
	public boolean send(DataOutputStream writer)
	{
		boolean bOk=true;
		
		do {
//			byte[] buf = new byte[mHeadLength + mPackLength];
//			
//			for (int i = 0; i < buf.length; i++) {
//				if (i < 24) {
//					buf[i] = mHeadBuf[i];
//				}
//				else {
//					buf[i] = mBuf[i - 24];
//				}
//			}
			try
			{
				//�������ݰ�
				writer.write(mHeadBuf,0,mHeadLength);
				writer.flush();
			}
			catch (Exception e)
			{
				Log.e(TAG,"packet send error!" + e.getMessage());
				bOk = false;
				break;
			}
			
			if (!bOk) {
				break;
			}
			
			if (mPackLength > 0) {
				try
				{
					//�������ݰ�
					writer.write(mBuf,0,mPackLength);
					writer.flush();
				}
				catch (Exception e)
				{
					Log.e(TAG,"packet send error!" + e.getMessage());
					bOk = false;
					break;
				}
			}
			
		} while (false);
		
		//
		return bOk;
	}
	/////��������
	private int RCV_OK=0;        //��������
	private int RCV_TIMEOUT=1;   //���ճ�ʱ
	private int RCV_ERROR=-1;    //���չ��̳���
	public int receive(DataInputStream reader, boolean canTimeOut)
	{
		int ret = RCV_OK;
		boolean mCanTimeOut=canTimeOut;
		try
		{
			////�����ļ�ͷ
			mRcvLength = 0;
//			mRcvLength = reader.read(mBuf);
			mRcvLength = reader.read(mHeadBuf, 0, mHeadLength);
//			Log.i(TAG,"mRcvLength = " + String.valueOf(mRcvLength));
			////�����յ��������Ƿ���һ�����������ݰ�
			//����Ƿ���յ����������ݰ�ͷ
			if(mRcvLength != mHeadLength)
			{
				ret = RCV_ERROR;
				Log.e(TAG,"receive head fail!");
				return ret;
			}
			
			//////��ȡ���ݰ�����
			SetHead(mHeadBuf);
			
			String msg = String.valueOf(mTransType) + " " +
					String.valueOf(mPackType) + " " +
					String.valueOf(mTransID) + " " +
					String.valueOf(mPackMaxLength) + " " +
					String.valueOf(mTotalLength) + " " +
					String.valueOf(mPackIndex) + " " +
					String.valueOf(mPackLength);
//			Log.e(TAG, msg);

			if(mPackLength<0)
			{
				Log.e(TAG,"Receive packlength < 0;so error!");
				ret = RCV_ERROR;
				/*String */msg = String.valueOf(mTransType) + " " +
						String.valueOf(mPackType) + " " +
						String.valueOf(mTransID) + " " +
						String.valueOf(mPackMaxLength) + " " +
						String.valueOf(mTotalLength) + " " +
						String.valueOf(mPackIndex) + " " +
						String.valueOf(mPackLength);
				Log.e(TAG, msg);
				return ret;
			}
			
			if (mPackLength > 0) {
				//��������
				int rcvLen;
				int hadRcv = 0;
				do
				{
					rcvLen = reader.read(mBuf, hadRcv, mPackLength - hadRcv);
					hadRcv += rcvLen;
				}
				while (hadRcv < mPackLength);
				
				if(hadRcv != mPackLength)
				{
					Log.e(TAG,"Receive packlength = " + rcvLen + " != needlength!");
					ret = RCV_ERROR;
					return ret;
				}
				ret = RCV_OK;
			}
			
//			mRcvLength += rcvLen;
		}
		catch(SocketTimeoutException timeOutE)
		{
			Log.e(TAG,"receive_packet Timeout!");
			if(mCanTimeOut)
				ret = RCV_TIMEOUT;
			else
				ret = RCV_ERROR;
			return ret;
		}
		catch (Exception e)
		{
			Log.e(TAG,"receive one packet fail!");
			ret = RCV_ERROR;
		}
		//
		return ret;
	}
	//����Ӧ��
	public boolean receiveAck(DataInputStream reader)
	{
		int ret;
		ret = receive(reader, false);
		if(ret == RCV_OK)
		{
			if(isAckPacket())
				return true;
		}
		//
		return false;

	}
	//�ж��Ƿ���ack���ݰ�
	public boolean isAckPacket()
	{
		//û�н��յ�Ӧ�����ݰ�����
		if(!isRcvAckLength())
			return false;
		
		//��ѯ�Ƿ���յ��˱�־
		if(!isHasFlag())
			return false;
		
		//��ѯ�����Ƿ���Ӧ���־
		if(!isACKType())
			return false;
		
		//
		return true;
	}
	//�ж��Ƿ���յ���Ӧ����ĳ���
	public boolean isRcvAckLength()
	{
		if(mRcvLength == mACKPackLength)
			return true;
		//
		return false;
	}
	//�ж��Ƿ���յ��˱�־
	public boolean isHasFlag()
	{
		return mPackFlag == 0xAAAABBBB;
	}
	//�ж��Ƿ���Ӧ�������
	public boolean isACKType()
	{
		return mPackType == mAckPackType;
	}
	//��ҪӦ���
	public boolean isNeedACK()
	{
		//�����Ӧ���������ҪӦ��
		if(mPackType == mAckPackType ||
			mPackType == mWavePackType)
			return false;
		//
		return true;
	}
	//�Ƿ�������������
	public boolean isNetcheckType()
	{
		return mPackType == mNetCheckPackType;
	}
	//
	public boolean isCommandType()
	{
		return mPackType == mCommandPackType;
	}
	
	public short getPackType()
	{
		return mPackType;
	}
	////�õ�����
//	public NetCommand getCommand()
//	{
//		NetCommand obj = new NetCommand();
//		int commandLen = 0;
//		int tem=0;
//		short command = 0;
//		//�õ������;
//		int index;
//		index = mHeadLength;
//		tem = mBuf[index];
//		commandLen = tem;
//		commandLen &= 0xff;
//		//
//		index++;
//		tem = mBuf[index]<<8;
//		commandLen += tem;
//		obj.setCommandLength(commandLen);
//		
//		//�õ�����
//		index++;
//		command = mBuf[index];
//		command &= 0xff;
//		//
//		index++;
//		tem = mBuf[index];
//		command += tem;
//		obj.setCommand(command);
//		
//		//��������
//		int i;
//		int num=commandLen-4;
//		num = num/2;
//		short temShort;
//		index++;
//		if(num>0)
//		{
//			short[] params = new short[num];
//			for(i=0;i<num;i++)
//			{
//				temShort = 0;
//				temShort = mBuf[index+i*2];
//				temShort &= 0xff;
//				//
//				temShort += mBuf[index+i*2+1];
//				//
//				params[i] = temShort;
//				Log.i(TAG,"Command_"+(i+1)+":="+temShort);
//			}
//			obj.setCommandParams(params,num);
//		}
//		Log.i(TAG,"GetCommand:="+command+";commandLength:="+num);
//		//
//		return obj;
//	}
}
