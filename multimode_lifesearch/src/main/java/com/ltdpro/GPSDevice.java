package com.ltdpro;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.security.InvalidParameterException;

import android.content.Context;
import android.util.Log;

import com.ltd.multimodelifesearch.activity.MultiModeLifeSearchActivity;

import android_serialport_api.SerialPort;

public class GPSDevice 
{
	private static final String TAG = "GPSDevice";
	private SerialPort mSerialPort = null;
	private InputStream mFileInputStream;
	private OutputStream mFileOutputStream;
	private boolean[] mFhead = {false, false, false, false};
	private boolean mFend = false;
	private byte[] mInBuf = new byte[1024];
	private int mCntInBuf = 0;
	private ReadThread mReadThread = null;
	private boolean mIsOpened = false;
	//GPS�Ķ�ȡ�ͽ�����־λ
	private boolean mGPSstart = false;
	private boolean mGPSend = false;
	//GPS�����Ƿ񱣴�
	private boolean mIsSaved = false;
	private String mfilePath = null;		//�ļ�����·�������״������ļ�����һ�£���׺��ͬ����׺ltg
	private String mfileName = null;		//�ļ�����
	private MyApplication mApp;				//����ȫ�ֱ���
	private Context mContext;				//����������
	private byte mFreqCode = -1;
	private byte mRepFreqCode = -1;


	public GPSDevice()
	{

	}

	//
	public void setContext(Context context)
	{
		mContext = context;
		mApp = (MyApplication)((MultiModeLifeSearchActivity)mContext).getApplicationContext();
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

	//�̴߳�����
	private class ReadThread extends Thread
	{
		@Override
		public void run()
		{
			while (mIsOpened)
			{
				//	Log.i(TAG, "Enter Run!");
				long sleeptime = 50;
				int size = 0;
				int gps_count = 0;
				try
				{
					byte buffer[] = new byte[1];//ÿ�ζ�1λ
					byte inputBuffer[] = new byte[100];//���뻺��������һ�ε�gps����

					if (mFileInputStream == null)
					{
						return;
					}
					size = mFileInputStream.read(buffer);

					/*	���ԴӴ��ڷ������ݵ�����				
					mFileOutputStream.write('1');*/
					//DebugUtil.i(TAG, String.valueOf(size));

					if (size > 0)
					{
						DebugUtil.i("SerialPort", "GPSDevice size>0!");

						if( buffer[0] == '$')
						{
							DebugUtil.i(TAG,"�õ���");
							inputBuffer[gps_count++] = '$';
							mFileInputStream.read(buffer);
							while( buffer[0] != '*')
							{
								DebugUtil.i(TAG,"�õ�"+buffer[0]);

								inputBuffer[gps_count++] = buffer[0];//�����ݴ������뻺����					

								mFileInputStream.read(buffer);
							}		
							inputBuffer[gps_count++] = '*';

							//���յ������ݴ�ӡ��һ��
							for(int temp = 0;temp < gps_count;temp++)
							{
								DebugUtil.i(TAG, "inputBuffer[" + String.valueOf(temp)
                                                 + "]=" + String.valueOf(inputBuffer[temp]));
							}						

							String str = new String(inputBuffer);
							str = str.substring(0,gps_count);

							//��byte*תstring
							DebugUtil.i(TAG, "���GPS����:"+str);
							//���ַ��������ļ���
							//�½��ļ���д���ȡ��һ��
							try{
								//����Ǳ�������ģʽ
//								if(mApp.mRadarDevice.isSavingMode())
//								{
									long scanNum[] = {3};//mApp.mRadarDevice.getHadRcvScans();
									
									LongBuffer bnum = LongBuffer.wrap(scanNum);
									DebugUtil.i(TAG, "scanNum=" + String.valueOf(scanNum));
									//���ݳ��ȶ�������е���
									byte gpsBuffer[] = new byte[gps_count];
									System.arraycopy(inputBuffer, 0, gpsBuffer, 0, gps_count);
									//ʹ��byteBuffer�洢inputBuffer
									ByteBuffer bb = ByteBuffer.wrap(gpsBuffer);
									//bb.order(ByteOrder.LITTLE_ENDIAN);
									String file = "/mnt/sdcard/filetestH.gps";
									
									//mApp.mRadarDevice.getSaveFilename();
									if( file == null );
									else
									{
										/*								int lastIndex = file.lastIndexOf(".");
								file = file.substring(0, lastIndex);
								file += ".gps";*/
										//��С��ģʽ��д���ļ�							
										RandomAccessFile fs = new RandomAccessFile(file, "rw");
										fs.seek(fs.length());
										//����д���־�͵���
										fs.writeChars("$GPS");
										fs.writeChars("#");
									//	fs.write(bnum.getLong());
										fs.write(bb.array());
										fs.writeChars("\r\n");
										fs.close();
									}

									//								FileInputStream fi = new FileInputStream(file);
									//								byte getBuffer[] = new byte[100];
									//								fi.read(getBuffer);
									//								String str = new String(getBuffer);
									//								str = str.substring(0,gps_count);
//								}
//								else;
							}catch(IOException e)
							{

							}
						}

						/**
						 * ����GPS�е����ݻ�þ�γ�ȣ�ʹ�ö�GPGGA��GNGGA��ʽ�Ĵ�����
						 * ����⳵�еĴ�������CתJava
						 */
						//onDataReceived(buffer, size);						

						for(int i=0;i<size;i++)
							DebugUtil.i(TAG, String.valueOf(buffer[i]));

						try
						{
							Thread.sleep(sleeptime);
						}
						catch(Exception e)
						{
							DebugUtil.i(TAG,"GPSDeviceThread run fail_sleep!");
						}

					}
				}
				catch (IOException e)
				{
					//e.printStackTrace();
					Log.e(TAG, e.getMessage());
					return;
				}
			}
			Log.e(TAG, "Thread Exit!");
		}
	}

	public void openSerialPort(String devname, int baudrate, int parity, int databits, int stopbits, int flags)
	{
		try
		{
			if (mSerialPort == null)
			{
				/* Check parameters */
				if ((devname.length() == 0))
				{
					throw new InvalidParameterException();
				}

				/* Open the serial port */
				mSerialPort = new SerialPort(new File(devname), baudrate, parity, databits, stopbits, 0);
				mFileOutputStream = mSerialPort.getOutputStream();
				mFileInputStream = mSerialPort.getInputStream();
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

	public void closeSerialPort()
	{
		Log.e(TAG, "Enter closeSerialPort");
		if (mFileInputStream != null)
		{
			try
			{
				mFileInputStream.close();
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

	public byte getFreqCode()
	{
		return mFreqCode;
	}

	public byte getRepFreqCode()
	{
		return mRepFreqCode;
	}
}
