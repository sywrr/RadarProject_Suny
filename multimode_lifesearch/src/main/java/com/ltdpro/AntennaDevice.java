package com.ltdpro;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android.util.Log;
import android_serialport_api.SerialPort;
/*
1	��ʼʶ����0	0x55	
2	��ʼʶ����1	0xaa	
3	��ʼʶ����2	0xbb	
4	��ʼʶ����3	0xee	
5	������Ƶ	0xa0	GC25M
		0xa1	GC50M
		0xa4	GC100M
		0xa5	GC100S
		0xa6	GC100HF
		0xa7	GC270M
		0xa9	GC400M
		0xaa	GC400HF
		0xab~0xaf	RESERVED
		0xb3	GC900HF
		0xb5	GC1200M
		0xb6	GC1500HF
		0xb7	GC2000HF
		0xbb~0xbf	RESERVED
		0xc1	AL1000M
		0xc2	AL1500HF
		0xc3	AL2000HF
		0xc4	AL2500HF
		0xc5~0xcf	RESERVED
6	�����ظ�Ƶ��	0x01	16kHz
		0x02	32kHz
		0x03	64kHz
		0x04	128kHz
		0x05	150kHz
		0x06	400kHz
		0x07	512kHz
		0x08	800kHz
		0x09	1000kHz
		0x0a~0xff	RESERVED
*/

public class AntennaDevice
{
	private static final String TAG = "AntennaDevice";
	private SerialPort mSerialPort = null;
	private InputStream mFileInputStream;
	private OutputStream mFileOutputStream;
	private boolean[] mFhead = {false, false, false, false};
	private boolean mFend = false;
	private byte[] mInBuf = new byte[1024];
	private int mCntInBuf = 0;
	private ReadThread mReadThread = null;
	private boolean mIsOpened = false;
	
	private byte mFreqCode = -1;			//��Ƶֵ
	private byte mRepFreqCode = -1;		    //�ظ�Ƶ��ֵ
	
	//�������ļ���
	private FileOutputStream mWriteOutputStream;	//д�ļ��������
	private FileInputStream mReadOutputStream;		//���ļ���������

	public AntennaDevice()
	{
	}

	// Getters and setters
	public InputStream getInputStream()
	{
		return mFileInputStream;
	}

	public OutputStream getOutputStream()
	{
		return mFileOutputStream;
	}
	
	//��ȡ�߳�
	private class ReadThread extends Thread
	{
		@Override
		public void run()
		{
//			super.run();
			while (mIsOpened)
			{
//				Log.e(TAG, "Enter Run!");
				DebugUtil.i(TAG, "open ReadThread!");
				int size = 0;
				try
				{
					
					byte[] buffer = new byte[7];
					if (mFileInputStream == null)
					{
						return;
					}
					size = mFileInputStream.read(buffer);
					
					//ģ����ļ���ȡ��Ƶ���� 
//					size = 7;
//					File file = new File("/mnt/sdcard/antenna.txt");
//					if( file.exists() )
//					{ 
//						DebugUtil.i(TAG, "file exist!");
//						mReadOutputStream = new FileInputStream(file);
//						mReadOutputStream.read(buffer);
//						DebugUtil.i(TAG, String.valueOf(size));
//						mReadOutputStream.close();
//					}						
//					else
//					{
//						DebugUtil.i(TAG, "file not exist!");
//					}
					
					if (buffer[0] > 0)
					{
						DebugUtil.i(TAG, "AntennaSerial size>0!");
						
						if( onDataReceived(buffer, size) )
							mIsOpened = false;
						else;
						
						// fOut.write(buffer);
						// ���յ�������
						DebugUtil.i(TAG, "�Լ����0x55=" + String.valueOf(0x55));
						DebugUtil.i(TAG, "size=" + String.valueOf(size));
						for(int i=0;i<size;i++)
							DebugUtil.i(TAG, String.valueOf(buffer[i]));
					}					
			
					Thread.sleep(100);
				}
				catch (IOException e)
				{
					//e.printStackTrace();
					Log.e(TAG, e.getMessage());
					return;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Log.e(TAG, "Thread Exit!");
		}
	}

	//���յ������ݽ��д���
	protected boolean onDataReceived(final byte[] buffer, final int size)
	{
		DebugUtil.i(TAG, "enter onDataReceived!");
		for (int i = 0; i < size; i++)
		{
			byte RXBuff = buffer[i];

			if (!mFhead[0])
			{
				if (RXBuff == (byte)0x55)
				{
					DebugUtil.i(TAG, "ƥ��0x55!");
					mInBuf[mCntInBuf++] = RXBuff;
					mFhead[0] = true;
				}
			}
			else if (!mFhead[1])
			{
				if (RXBuff == (byte)0xaa)
				{
					DebugUtil.i(TAG, "ƥ��0xaa!");
					mInBuf[mCntInBuf++] = RXBuff;
					mFhead[1] = true;
				}
				else 
				{
					mFhead[0] = false;
					mCntInBuf = 0;
					if (RXBuff == (byte)0x55)
					{
						mInBuf[mCntInBuf++] = RXBuff;
						mFhead[0] = true;
					}
				}
			}
			else if (!mFhead[2])
			{
				if (RXBuff == (byte)0xbb)
				{
					DebugUtil.i(TAG, "ƥ��0xbb!");
					mInBuf[mCntInBuf++] = RXBuff;
					mFhead[2] = true;
				}
				else 
				{
					mFhead[0] = false;
					mFhead[1] = false;
					mCntInBuf = 0;
					if (RXBuff == 0x55)
					{
						mInBuf[mCntInBuf++] = RXBuff;
						mFhead[0] = true;
					}
				}
			}
			else if (!mFhead[3])
			{
				if (RXBuff == (byte)0xee)
				{
					DebugUtil.i(TAG, "ƥ��0xee!");
					mInBuf[mCntInBuf++] = RXBuff;
					mFhead[3] = true;
				}
				else 
				{
					mFhead[0] = false;
					mFhead[1] = false;
					mFhead[2] = false;
					mCntInBuf = 0;
					if (RXBuff == (byte)0x55)
					{
						mInBuf[mCntInBuf++] = RXBuff;
						mFhead[0] = true;
					}
				}
			}
			else
			{
				if (!mFend)
				{
					if (RXBuff == (byte)0xf0)
					{
						DebugUtil.i(TAG, "ƥ��0xf0!");
						mInBuf[mCntInBuf++] = RXBuff;
						mFend = true;
						mFreqCode = mInBuf[4];
						mRepFreqCode = mInBuf[5];
					DebugUtil.e(TAG, "ƥ��ɹ���mFreqCode=" + String.valueOf(mFreqCode)
                                     + ";mRepFreqCode=" + String.valueOf(mRepFreqCode));
					DebugUtil.i(TAG, "mFreqCode  = " + String.valueOf(mFreqCode)
                                     + "mRepFreqCode = " + String.valueOf(mRepFreqCode));
				}
					else
					{
						mInBuf[mCntInBuf++] = RXBuff;
						//�ҵ�ͷ��һֱ�Ҳ���β
						if (mCntInBuf > 7)
						{
							mCntInBuf = 0;
							for (int j = 0; j < mFhead.length; j++)
							{
								mFhead[j] = false;
							}
							mFend = false;
						}
					}
				}
			}
		}
		return false;
	}
	
	private FileOutputStream fOut = null;
	//�򿪴��ڶ˿�
	public void openSerialPort(String devname, int baudrate, int parity, int databits, int stopbits, int flags)
	{
		DebugUtil.i(TAG,"enter openSerialPort!");
		try
		{
			if (mSerialPort == null)
			{
				/* Check parameters */
				if ((devname.length() == 0))
				{
					throw new InvalidParameterException();
				}
				
				mFreqCode = -1;			//��Ƶֵ
				mRepFreqCode = -1;	
			
				/* Open the serial port */
				mSerialPort = new SerialPort(new File(devname), baudrate, parity, databits, stopbits, 0);
				mFileOutputStream = mSerialPort.getOutputStream();
				mFileInputStream = mSerialPort.getInputStream();
				
				
			//	mWriteOutputStream = new FileOutputStream(f);
								
				try 
				{
					//����
					File f = new File("/mnt/sdcard/antenna.txt");
					fOut = new FileOutputStream(f);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					DebugUtil.e(TAG, "error");
				}
			
				mIsOpened = true;
				
				/* Create a receiving thread */
				mReadThread = new ReadThread();
				mReadThread.start();
			}
			else
			{
				closeSerialPort();
			}
		}
		catch (SecurityException e)
		{
		}
		catch (IOException e)
		{
		}
		catch (InvalidParameterException e)
		{
		}
	}

	//�رմ��ڶ˿�
	public void closeSerialPort()
	{
		Log.e(TAG, "Enter closeSerialPort");
		
	
		//��־λ
		for (int j = 0; j < mFhead.length; j++)
		{
			mFhead[j] = false;
		}
		
		
		mCntInBuf = 0;
		for (int j = 0; j < mFhead.length; j++)
		{
			mFhead[j] = false;
		}
		mFend = false;
		
		if (mFileInputStream != null)
		{
			try
			{
				mFileInputStream.close();
				fOut.close();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mFileInputStream = null;
		}
		
		if (mFileOutputStream != null)
		{
			try
			{
				mFileOutputStream.close();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mFileOutputStream = null;
		}

		if (mSerialPort != null)
		{
			mSerialPort.close();
			mSerialPort = null;
		}
		
		if (mReadThread != null)
		{
			mIsOpened = false;
			
			while (mReadThread.isAlive())
			{
				try
				{
					Thread.sleep(200);
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			mReadThread = null;
		}
		Log.e(TAG, "closeSerialPort");
	}
	
	//д���ֽ�
	public boolean writeBytes(byte[] buf)
	{
		try
		{
			if (mFileOutputStream == null)
			{
				return false;
			}
			mFileOutputStream.write(new String(buf).getBytes());
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean isOpened()
	{
		return mIsOpened;
	}
	
	//��Ƶֵ�����ݽӿ��ĵ������ж�
	public byte getFreqCode()
	{
		return mFreqCode;
	}
	
	//������Ƶ���ж���ʲô����
/*	0xa1	GC50M
	0xa4	GC100M
	0xa5	GC100S
	0xa6	GC100HF
	0xa7	GC270M
	0xa9	GC400M
	0xaa	GC400HF
	0xab~0xaf	RESERVED
	0xb3	GC900HF
	0xb5	GC1200M
	0xb6	GC1500HF
	0xb7	GC2000HF
	0xbb~0xbf	RESERVED
	0xc1	AL1000M
	0xc2	AL1500HF
	0xc3	AL2000HF
	0xc4	AL2500HF*/
	
	public String getFreqStr()
	{
		if( mFreqCode == (byte)0xa1 )
			return "GC50M";
		else if( mFreqCode == (byte)0xa4 )
			return "GC100M";
		else if( mFreqCode == (byte)0xa5 )
			return "GC100S";
		else if( mFreqCode == (byte)0xa6 )
			return "GC100HF";
		else if( mFreqCode == (byte)0xa7 )
			return "GC270M";
		else if( mFreqCode == (byte)0xa9 )
			return "GC400M";
		else if( mFreqCode == (byte)0xaa )
			return "GC400HF";
		else if( mFreqCode == (byte)0xb3 )	
			return "GC900HF";
		else if( mFreqCode == (byte)0xb5 )
			return "GC1200M";
		else if( mFreqCode == (byte)0xb6 )
			return "GC1500HF";
		else if( mFreqCode == (byte)0xb7 )
			return "GC2000HF";
		else if( mFreqCode == (byte)0xc1 )
			return "AL1000M";
		else if( mFreqCode == (byte)0xc2 )
			return "AL1500HF";
		else if( mFreqCode == (byte)0xc3 )
			return "AL2000HF";
		else if( mFreqCode == (byte)0xc4 )
			return "AL2500HF";
		else return null;
	}
	
	//�ظ�Ƶ��
	public byte getRepFreqCode()
	{
		return mRepFreqCode;
	}
}
