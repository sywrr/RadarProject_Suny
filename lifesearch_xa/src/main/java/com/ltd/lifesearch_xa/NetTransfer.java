package com.ltd.lifesearch_xa;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.R.bool;
import android.util.Log;

public class NetTransfer
{

	private final static String TAG = "NetTransfer";

	private static long gTransID = 1;
	private List<Object> mSndPacketList = new ArrayList<Object>();
	private List<Object> mRcvPackList = new ArrayList<Object>();
	private short mTransType; // ��������(����/����)
	private short mTransLength; // �����ܳ���--�ֽڳ���
	private byte[] mContextBuf; // ��������
	private int mBufPos = 0;
	private short mHasSendLength; // �Ѿ����͵������ֽڳ���
	private short mHasPacketLength; // �Ѿ�����������ֽڳ���
	private long mTransID; // �����ID��
	private int mNowSndPackID; // ���ڷ��͵����ݰ�ID
	private int mPackPacketID; // ���ڴ�������ݰ�ID
	private int mRepSndNum; // ���ݰ�����ʧ�ܺ��ط��Ĵ���

	public NetTransfer()
	{
		// TODO Auto-generated constructor stub
		mTransType = 0;
		mTransLength = 0;
		mHasSendLength = 0;
		mHasPacketLength = 0;
		mTransID = gTransID++;
		mNowSndPackID = 0;
		mPackPacketID = 0;
	}

	private void deleteAllArray()
	{
		mSndPacketList.removeAll(mSndPacketList);
		mRcvPackList.removeAll(mRcvPackList);
	}

	public boolean Create(short type, short tLen)
	{
		mTransType = type;
		if (tLen != 0)
		{
			mContextBuf = new byte[tLen];
			if (mContextBuf == null)
				return false;
			mTransLength = tLen;
		}
		return true;
	}

	public boolean Create(NetPacket packet)
	{
		mTransType = packet.getTransferType();
		mTransID = packet.getTransferID();
		mTransLength = (short) packet.getTransferLength();

		if (mTransLength != 0)
		{
			mContextBuf = new byte[mTransLength];
			if (mContextBuf == null)
				return false;
		}
		return true;
	}

	public void SetDatas(byte[] pBuf, int bufLen)
	{
		for (int i = 0; i < bufLen; i++)
		{
			mContextBuf[i] = pBuf[i];
		}
	}

	public void SetDatas(short[] pBuf, int bufLen)
	{
		for (int i = 0; i < bufLen / 2; i++)
		{
			mContextBuf[i * 2] = (byte) pBuf[i];
			mContextBuf[i * 2 + 1] = (byte) (pBuf[i] >> 8);
		}
	}

	public boolean CreateDivisionPackets()
	{
		boolean bRet = true;
		mHasPacketLength = 0;
		mPackPacketID = 0;

		// //
		// ���ɡ���ʼ���䡱�����
		NetPacket pack;
		pack = CreateTransBegPacket();
		if (pack == null)
		{
			bRet = false;
			Log.e(TAG, "CreateTransBegPacket fail,because mem no enough!\r\n");
			return bRet;
		}

		// ���뷢�Ͷ�����
		mSndPacketList.add(pack);
		// ���Ӵ������
		mPackPacketID++;

		// /////////
		// ѭ���������еĴ����
		while (mHasPacketLength < mTransLength)
		{
			// ���ɴ����
			pack = CreateNextPacket();
			if (pack == null)
			{
				bRet = false;
				String msg = "Create " + String.valueOf(mPackPacketID)
						+ " packet fail!\r\n";
				Log.e(TAG, msg);
				break;
			}
			//
			mSndPacketList.add(pack);
			// ���Ӵ��������
			mPackPacketID++;
			// �����Ѿ����������
			mHasPacketLength += pack.getBufLength();
		}

		// ///////////
		// ���ɡ��������䡱�����
		pack = CreateTransEndPacket();
		if (pack == null)
		{
			bRet = false;
		}
		else
		{
			// ���뷢�Ͷ�����
			mSndPacketList.add(pack);
		}

		//
		return bRet;
	}

	// ���ɴ��俪ʼ���ݰ�
	private NetPacket CreateTransBegPacket()
	{
		NetPacket pPacket;
		// ����"��ʼ����"���ݰ�
		pPacket = new NetPacket();

		pPacket.Init(mTransType, Global.BEGTRANS_PACK, 0, mTransLength,
				mPackPacketID, mTransID);

		return pPacket;
	}

	// ������һ�����ݰ�
	private NetPacket CreateNextPacket()
	{
		NetPacket pack = null;

		// ��������Ѿ�����
		if (mHasPacketLength == mTransLength)
		{
			return null;
		}

		// ���ɴ����
		pack = new NetPacket();
		// ��ʼ�������
		pack.Init(mTransType, Global.DATA_PACK, 0, mTransLength, mPackPacketID,
				mTransID);
		// ������ݰ�
		boolean bRet;

		bRet = FillPacket(pack);
		// ������ݰ��ɹ�
		if (!bRet)
		{
			pack = null;
		}

		//
		return pack;
	}

	// ���ɽ����������ݰ�
	private NetPacket CreateTransEndPacket()
	{
		NetPacket pack;
		// ����"��ʼ����"���ݰ�
		pack = new NetPacket();
		if (pack != null)
		{
			pack.Init(mTransType, Global.ENDTRANS_PACK, 0, mTransLength,
					mPackPacketID, mTransID);
		}

		return pack;
	}

	// �����ݰ��������
	boolean FillPacket(NetPacket pack)
	{
		boolean bRet = true;
		//
		byte[] buf = new byte[mTransLength - mHasPacketLength];

		for (int i = 0; i < buf.length; i++)
		{
			buf[i] = mContextBuf[mHasPacketLength + i];
		}
		pack.FillDatas(buf, mTransLength - mHasPacketLength);

		return bRet;
	}

	// �������е����ݰ�
	public boolean SendAllPackets(DataOutputStream writer,
			DataInputStream reader)
	{
		//
		NetPacket pack;
		int packLen;
		int sndNum;
		boolean rcvAck = false;
		int ret;
		boolean bOk = false;

		mRepSndNum = 1; // �ط�����
		mNowSndPackID = 0;
		packLen = mSndPacketList.size();
		for (int i = mNowSndPackID; i < packLen; i++)
		{
			// ȡ��һ�����ݰ�
			pack = (NetPacket) mSndPacketList.get(i);
			// ���͸ô����
			mNowSndPackID = i;
			sndNum = mRepSndNum; // �ط�����
			do
			{
				bOk = pack.send(writer);
				if (!bOk)
				{
					Log.e(TAG, "Send one packet fail1!");
					continue;
				}
				// �ȴ�Ӧ���
				rcvAck = false;
				NetPacket pPack = new NetPacket();
				pPack.Init((short) 0, (short) 0, 0, 0, 0, 0);
				ret = pPack.receive(reader, false);
				
				if (ret != 0)
				{
					bOk = false;
				}
				else
				{
					bOk = true;
				}
				if (!bOk)
				{
					Log.e(TAG, "Send one packet fail2!");
					continue;
				}
				// ��ȷ���յ�Ӧ���
				if (pPack.mPackType == Global.ACK_PACK)
				{
					rcvAck = true;
					break;
				}
				else
				{
				}
			}
			while ((--sndNum) != 0);

			// һ�����ݰ��������,�����������,�����˳�
			if (!rcvAck)
			{
				Log.e(TAG, "Send one packet fail3!");
				bOk = false;
			}
			if (!bOk)
			{
				break;
			}
			else
			{

			}
		}

		//
		return bOk;
	}

	public void createNETCheckTransfer()
	{
		Create(Global.TRANSFER_TYPE_DATAS, (short) 0);
		CreateDivisionPackets();
	}

	public boolean AddOneRcvPacket(NetPacket packet)
	{
		mRcvPackList.add(packet);
		return true;
	}

	public boolean IsNotRcvAllPackets()
	{
		int num = mRcvPackList.size();
		if (num == 0)
			return true;
		NetPacket pack;
		pack = (NetPacket) mRcvPackList.get(num - 1);
		if (pack.mPackType == Global.ENDTRANS_PACK)
			return false;
		return true;
	}

	public long getID()
	{
		return mTransID;
	}

	public short getTansferType()
	{
		return mTransType;
	}

	public byte[] getContextBuf()
	{
		return mContextBuf;
	}

	// �ô����Ѿ���ɣ�������յ������ݰ�
	public boolean ManageReceivePackets()
	{
		int i, num;
		short type;
		NetPacket packet;

		// ���������ݰ������ݿ����������ڴ�
		num = mRcvPackList.size();

		for (i = 0; i < num; i++)
		{
			packet = (NetPacket) mRcvPackList.get(i);
			type = packet.mPackType;
			switch (type)
			{
			// ����ǿ�ʼ�������ݰ�����������
			case Global.ENDTRANS_PACK:
			case Global.BEGTRANS_PACK:
				break;
			case Global.DATA_PACK:
			case Global.COM_PACK:
			{
				// ÿ�δ���ֻ��һ�����ݻ�����������ǿ��Ե�
				int len = packet.getBufLength();
				if (len != 0)
				{
					for (int j = 0; j < len; j++)
					{
						mContextBuf[mBufPos + j] = packet.mBuf[j];
					}
					mBufPos += len;
				}
				break;
			}
			}
		}
		return true;
	}

	// //�õ��豸״̬����
	public boolean getDevData(DevData devData)
	{
		if (mTransType != Global.TRANSFER_TYPE_DEVICESTATUS
				&& mTransType != Global.TRANSFER_TYPE_DETECTRESULT)
		{
			Log.e(TAG, "The Transfer is not one DEVICE_DATA_TRANSFER!");
			return false;
		}
		if (mTransLength == 0)
		{
			Log.e(TAG, "This Transfer Length = 0");
			return false;
		}

		// Log.e(TAG, "This Transfer Length = " + String.valueOf(mTransLength));
		// ����������ֵ
		devData.mType = (short) ((getUByte(mContextBuf[1]) << 8) + getUByte(mContextBuf[0]));
		devData.mStatus.mType = (short) ((getUByte(mContextBuf[5]) << 8) + getUByte(mContextBuf[4]));
		devData.mStatus.mRangeUp = (short) ((getUByte(mContextBuf[7]) << 8) + getUByte(mContextBuf[6]));
		devData.mStatus.mRangeDown = (short) ((getUByte(mContextBuf[9]) << 8) + getUByte(mContextBuf[8]));

		if (mContextBuf[10] != 0)
		{
			devData.mStatus.mErrBattery = true;
		}
		else
		{
			devData.mStatus.mErrBattery = false;
		}

		if (mContextBuf[11] != 0)
		{
			devData.mStatus.mBatteryUse = true;
		}
		else
		{
			devData.mStatus.mBatteryUse = false;
		}

		devData.mStatus.mBatteryLeftTime = ((getUByte(mContextBuf[15]) << 24)
				+ (getUByte(mContextBuf[14]) << 16) + ((getUByte(mContextBuf[13]) << 8) + getUByte(mContextBuf[12])));

		devData.mResult.mTargetNum = (short) ((getUByte(mContextBuf[17]) << 8) + getUByte(mContextBuf[16]));
		devData.mResult.mMiddleTargetNum = (short) ((getUByte(mContextBuf[19]) << 8) + getUByte(mContextBuf[18]));

		for (int i = 0; i < 20; i++)
		{
			devData.mResult.mResult[i].mExistMove = (short) ((getUByte(mContextBuf[21 + 8 * i]) << 8) + getUByte(mContextBuf[20 + 8 * i]));
			devData.mResult.mResult[i].mMovePos = (short) ((getUByte(mContextBuf[23 + 8 * i]) << 8) + getUByte(mContextBuf[22 + 8 * i]));
			devData.mResult.mResult[i].mExistBreath = (short) ((getUByte(mContextBuf[25 + 8 * i]) << 8) + getUByte(mContextBuf[24 + 8 * i]));
			devData.mResult.mResult[i].mBreathPos = (short) ((getUByte(mContextBuf[27 + 8 * i]) << 8) + getUByte(mContextBuf[26 + 8 * i]));
		}
		return true;
	}

	private int getUByte(byte val)
	{
		int uVal = ((int) val) & 0xFF;
		return uVal;
	}

	// �Ӵ�������ȡ����
	public boolean getDatas(WaveData data)
	{
		if (mTransType != Global.TRANSFER_TYPE_DATAS)
		{
			Log.e(TAG, "The Transfer is not one DATAS_TRANSFER!");
			return false;
		}
		if (mTransLength == 0)
		{
			// Log.e(TAG, "This Transfer Length = 0");
			return false;
		}

		//
		data.mLength = mTransLength;
		data.mBuf = new byte[mTransLength];

		for (int i = 0; i < mTransLength; i++)
		{
			data.mBuf[i] = mContextBuf[i];
		}

		return true;
	}

	public boolean isOneCommandTransfer()
	{
		return mTransType == Global.TRANSFER_TYPE_COM;
	}

	// �Ӵ������õ�����
	public Boolean getCommand(NetCommand com)
	{
		if (mTransType != Global.TRANSFER_TYPE_COM)
		{
			return false;
		}
		if (mTransLength == 0)
		{
			return false;
		}
		// �������ṹ
		com.mComCode = (short) ((getUByte(mContextBuf[1]) << 8) + getUByte(mContextBuf[0]));
		com.mParamLen = (short) (mTransLength - 2);

		for (int i = 0; i < com.mParamLen; i++)
		{
			com.mParams[i] = mContextBuf[2 + i];
		}

		return true;
	}
	
	//�����̽��������ô�ط�
	public boolean isNeedReSend()
	{
		return (mTransType == Global.TRANSFER_TYPE_DEVICESTATUS) || 
			    (mTransType == Global.TRANSFER_TYPE_DETECTRESULT);
	}
	//�ж��Ƿ����̽��Ŀ��
	public boolean existTargets()
	{
		if(mTransType != Global.DEVICE_DETECT_RESULT && mTransType != Global.TRANSFER_TYPE_DETECTRESULT)
			return false;
		if(mContextBuf == null)
			return false;
		
		DevData data = new DevData();
		getDevData(data);
		
		return ((data.mResult.mTargetNum == 0) &&
			    (data.mResult.mMiddleTargetNum == 0))? false : true;
	}
}
