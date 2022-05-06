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
	//GPS的读取和结束标志位
	private boolean mGPSstart = false;
	private boolean mGPSend = false;
	//GPS数据是否保存
	private boolean mIsSaved = false;
	private String mfilePath = null;		//文件保存路径，与雷达数据文件命名一致，后缀不同，后缀ltg
	private String mfileName = null;		//文件名称
	private MyApplication mApp;				//设置全局变量
	private Context mContext;				//设置上下文
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

	//线程处理函数
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
					byte buffer[] = new byte[1];//每次读1位
					byte inputBuffer[] = new byte[100];//输入缓存区，存一次的gps数据

					if (mFileInputStream == null)
					{
						return;
					}
					size = mFileInputStream.read(buffer);

					/*	测试从串口发送数据调试用				
					mFileOutputStream.write('1');*/
					//DebugUtil.i(TAG, String.valueOf(size));

					if (size > 0)
					{
						DebugUtil.i("SerialPort", "GPSDevice size>0!");

						if( buffer[0] == '$')
						{
							DebugUtil.i(TAG,"得到￥");
							inputBuffer[gps_count++] = '$';
							mFileInputStream.read(buffer);
							while( buffer[0] != '*')
							{
								DebugUtil.i(TAG,"得到"+buffer[0]);

								inputBuffer[gps_count++] = buffer[0];//将数据存入输入缓冲区					

								mFileInputStream.read(buffer);
							}		
							inputBuffer[gps_count++] = '*';

							//将收到的数据打印看一下
							for(int temp = 0;temp < gps_count;temp++)
							{
								DebugUtil.i(TAG, "inputBuffer[" + String.valueOf(temp)
                                                 + "]=" + String.valueOf(inputBuffer[temp]));
							}						

							String str = new String(inputBuffer);
							str = str.substring(0,gps_count);

							//将byte*转string
							DebugUtil.i(TAG, "获得GPS数据:"+str);
							//将字符串存入文件中
							//新建文件，写入读取试一试
							try{
								//如果是保存数据模式
//								if(mApp.mRadarDevice.isSavingMode())
//								{
									long scanNum[] = {3};//mApp.mRadarDevice.getHadRcvScans();
									
									LongBuffer bnum = LongBuffer.wrap(scanNum);
									DebugUtil.i(TAG, "scanNum=" + String.valueOf(scanNum));
									//根据长度对数组进行调整
									byte gpsBuffer[] = new byte[gps_count];
									System.arraycopy(inputBuffer, 0, gpsBuffer, 0, gps_count);
									//使用byteBuffer存储inputBuffer
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
										//将小端模式的写入文件							
										RandomAccessFile fs = new RandomAccessFile(file, "rw");
										fs.seek(fs.length());
										//首先写入标志和道号
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
						 * 解析GPS中的数据获得经纬度，使用对GPGGA和GNGGA格式的处理方法
						 * 将检测车中的处理方法由C转Java
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
