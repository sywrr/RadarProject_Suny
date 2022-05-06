package com.ltd.lifesearch_xa;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.w3c.dom.Text;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class WiFiNetDevice {
	String TAG = "WiFiNetDevice";
	private final static Boolean TCP_SERVER = true;
	private List<Object> mRCVTransList = new ArrayList<Object>();
	// /
	String mHostIP = "192.168.1.14"; // 连接到的主机ip地址
	String mLocalIP = "";
	int mRcvPort = 5000;// 5003;//59671; //从主机接收数据的端口
	int mSndPort = 5001;// 5002;//59670; //向主机发送数据的端口
	boolean mSndHadConnect = false; // 发送线程已经连接标志
	static boolean mRcvHadConnect = false; // 接收线程已经连接标志

	private int mMaxPackBufLength = 1024 * 5;
	// /接收线程相关
	private Thread mThreadRcv = null;
	private Socket mSocketRcv = null;
	private ServerSocket mSvrSocketRcv = null;
	DataInputStream rcvdis = null;
	DataOutputStream rcvdos = null;
	private byte[] mRcvBuffer = new byte[mMaxPackBufLength];
	private int mRcvCount;
	private boolean mNeedRcvConnect = false; // 要求连接到主机

	// /发送线程相关
	private Thread mThreadSnd = null;
	private Socket mSocketSnd = null;
	private ServerSocket mSvrSocketSnd = null;
	DataInputStream snddis = null;
	DataOutputStream snddos = null;
	private boolean mNeedSndConnect = false; // 要求连接到主机

	private int WIFISTATUS_NOTCONNECTHOST = 1; // 还没有连接到主机
	private int WIFISTATUS_CONNECTINGHOST = 2; // 正在进行主机连接
	private int WIFISTATUS_HADCONNECTHOST = 3; // 已经连接到主机
	private int WIFISTATUS_DISCONNECTINGHOST = 4; // 正在断开主机连接
	private int mWiFiConnectStatus = WIFISTATUS_NOTCONNECTHOST;

	private NetTransfer mNetCheckTransfer = null;

	//
	// private MyApplication mApplication;

	//
	private boolean mIsSendWifiDatas = false; // 是否正在发送wifi数据
	private Context mContext = null;

	//
	public WiFiNetDevice() {
		if (TCP_SERVER) {
			createSvrSocket();
		}
		// 生成发送线程
		mThreadSnd = new Thread(mThreadSndRunnable);
		mStopSndThread = false;
		mThreadSnd.start();

		// 生成接收线程
		mThreadRcv = new Thread(mThreadRcvRunnable);
		mStopRcvThread = false;
		mThreadRcv.start();
	}

	//
	public WiFiNetDevice(Context context) {
		mContext = context;
		if (TCP_SERVER) {
			createSvrSocket();
		}
		// 生成发送线程
		mThreadSnd = new Thread(mThreadSndRunnable);
		mStopSndThread = false;
		mThreadSnd.start();

		// 生成接收线程
		mThreadRcv = new Thread(mThreadRcvRunnable);
		mStopRcvThread = false;
		mThreadRcv.start();
	}

	public void setContext(Context context) {
		mContext = context;
	}

	public void createSvrSocket() {
		try {
			mSvrSocketRcv = new ServerSocket(mRcvPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// ((LifeSearchActivity)mActivity).showToast(e.getMessage());
		}

		try {
			mSvrSocketSnd = new ServerSocket(mSndPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// ((LifeSearchActivity)mActivity).showToast(e.getMessage());
		}
	}

	// public void setApplication(MyApplication app)
	// {
	// mApplication = app;
	// }
	//
	// 还没有连接
	public boolean isWiFiNotConnect() {
		return mWiFiConnectStatus == WIFISTATUS_NOTCONNECTHOST;
	}

	// 正在连接
	public boolean isWiFiConnecting() {
		return mWiFiConnectStatus == WIFISTATUS_CONNECTINGHOST;
	}

	// 正在断开连接
	public boolean isWiFiDisconnecting() {
		return mWiFiConnectStatus == WIFISTATUS_DISCONNECTINGHOST;
	}

	// 已经连接
	public boolean isWiFiHadConnect() {
		return mWiFiConnectStatus == WIFISTATUS_HADCONNECTHOST;
	}

	public boolean isWiFiSendDatas() {
		return mIsSendWifiDatas;
	}

	public void setHostIP(String ip) {
		mHostIP = ip;
	}

	public String getHostIP() {
		return mHostIP;
	}

	public String getLocalIP() {
		// 寻找本机ip地址
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						if (inetAddress.isSiteLocalAddress()) {
							mLocalIP = inetAddress.getHostAddress();
						}
					}
				}
			}
		} catch (SocketException ex) {
			String errStr = "获取IP地址异常:" + ex.getMessage() + "\n";
			Log.i(TAG, errStr);
		}

		return mLocalIP;
	}

	//
	public void setReceivePort(int port) {
		mRcvPort = port;
	}

	public int getReceivePort() {
		return mRcvPort;
	}

	//
	public void setSendPort(int port) {
		mSndPort = port;
	}

	public int getSendPort() {
		return mSndPort;
	}

	// 设置wifi连接状态为：正在连接
	public void setWiFiStatus_Connecting() {
		mWiFiConnectStatus = WIFISTATUS_CONNECTINGHOST;
	}

	//
	public void setWiFiStatus_NotConnect() {
		mWiFiConnectStatus = WIFISTATUS_NOTCONNECTHOST;
	}

	// 判断是否已经连接
	public boolean isHadConnect() {
		return isRcvHadConnect() && isSndHadConnect();
	}

	public boolean isRcvHadConnect() {
		return mRcvHadConnect;
	}

	public boolean isSndHadConnect() {
		return mSndHadConnect;
	}

	// 从服务器断开连接
	public void disconnectToHost() {
		Log.i(TAG, "disconnectToHost!");
		disconnectRcv();
		Log.i(TAG, "disconnectRcv");
		disconnectSnd();
		Log.i(TAG, "disconnectSnd");
	}

	// //断开接收线程:
	public void disconnectRcv() {
		mNeedRcvConnect = false;
		// 关闭socket套接字，断开连接
		closeRcvSockets();
	}

	// //断开发送线程
	public void disconnectSnd() {
		mNeedSndConnect = false;
		// 关闭socket套接字，断开连接
		closeSndSockets();
	}

	// //关闭发送socket
	public void closeSndSockets() {
		mSndHadConnect = false;
		try {
			if (mSocketSnd != null) {
				mSocketSnd.close();
				mSocketSnd = null;
			}

			if (snddis != null) {
				snddis.close();
				snddis = null;
			}
			if (snddos != null) {
				snddos.close();
				snddos = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void closeSvrSndSockets() {
		mSndHadConnect = false;
		try {
			if (mSvrSocketSnd != null) {
				mSvrSocketSnd.close();
				mSvrSocketSnd = null;
			}

			if (snddis != null) {
				snddis.close();
				snddis = null;
			}
			if (snddos != null) {
				snddos.close();
				snddos = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 连接到主机
	public void connectToHost() {
		if (!mSndHadConnect)
			connectSnd();
		//
		if (!mRcvHadConnect)
			connectRcv();
	}

	// 连接到发送线程
	public void connectSnd() {
		mNeedSndConnect = true;
	}

	// 发送数据
	public void sendDatas(short[] buf, int size) {
		if (!isSndHadConnect()) {
			return;
		}
		// if(!isWiFiHadConnect())
		// return;
		// if(!isWiFiSendDatas())
		// return;
		// 生成数据包
		int packNum;
		int scanLen;
		int lastSndLen = 0;
		NetPacket temPack = new NetPacket();
		int maxPackLength = temPack.getMaxPackLength();
		scanLen = maxPackLength / 2;
		packNum = size / maxPackLength;
		if (size % maxPackLength != 0) {
			lastSndLen = size - packNum * maxPackLength;
		}
		int i;
		short[] sendBuf;
		for (i = 0; i < packNum; i++) {
			NetPacket pack = new NetPacket();
			sendBuf = new short[maxPackLength / 2];
			for (int j = 0; j < scanLen; j++)
				sendBuf[j] = buf[j + i * scanLen];
			pack.createWavePacket(sendBuf, maxPackLength);
			addSendPacket(pack);
			// mSndPacketList.add(pack);
		}
		if (lastSndLen > 0) {
			NetPacket pack = new NetPacket();
			sendBuf = new short[lastSndLen / 2];
			for (int j = 0; j < lastSndLen / 2; j++)
				sendBuf[j] = buf[j + packNum * scanLen];
			pack.createWavePacket(sendBuf, lastSndLen);
			addSendPacket(pack);
			// mSndPacketList.add(pack);
		}
		Log.i(TAG, "has add:" + packNum + " packets!");
	}

	// 发送线程:监听服务器发来的消息
	private boolean mStopSndThread = false;
	private List<Object> mSndPacketList = new ArrayList<Object>();
	private List<Object> mSndTransferList = new ArrayList<Object>();
	private List<Object> mStatusTransferList = new ArrayList<Object>();
	private Runnable mThreadSndRunnable = new Runnable() {
		public void run() {
			// 如果不要求停止线程
			while (!mStopSndThread)// true
			{
				// //如果不需要连接到主机，该线程休眠
				if (!mNeedSndConnect) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					//
					continue;
				}

				// //需要连接到主机，并且还没有连接，此时连接到服务器
				if (!mSndHadConnect) {
					try {
						closeSndSockets();
						// 生成socket并连接

						if (TCP_SERVER) {
							Log.d("serversnd", "before accept");
							mSocketSnd = mSvrSocketSnd.accept();
							Log.d("serversnd", "after accept");
							mSocketSnd.setSoTimeout(5000);
						} else {
							mSocketSnd = new Socket(mHostIP, mSndPort);
							mSocketSnd.setSoTimeout(1000);
						}

						// 取得输入、输出流
						snddis = new DataInputStream(mSocketSnd.getInputStream());
						snddos = new DataOutputStream(mSocketSnd.getOutputStream());
						Log.i(TAG, "Enter Create SND Socket!");
					} catch (Exception e) {
						// Log.e(TAG, e.getMessage());
						Message msg = new Message();
						msg.what = EXCEPTION_SNDSOCKET_CREATE;
						mMsgHandler.sendMessage(msg);
						// 断开连接
						closeSndSockets();
						//
						continue;
					}
					Message msg = new Message();
					msg.what = MESSAGE_SNDSOCKET_HADCONNECTED;
					mMsgHandler.sendMessage(msg);
					//
					mSndHadConnect = true;
					Log.e(TAG, "send connect OK!");
				}

				NetTransfer transfer = getSendTransfer();

				if (transfer != null) {
					boolean bOk;

					bOk = sendTransfer(transfer);

					if (!bOk) {
						Message msg = new Message();
						msg.what = EXCEPTION_SNDSOCKET_SENDPACKET;
						mMsgHandler.sendMessage(msg);
						// 断开连接

						closeSndSockets();
						Log.e(TAG, "send error, send socket disconnect!");
					}
				}

				// //发送数据包
				// 得到数据包
				NetPacket packet = getSendPacket();
				if (packet != null) {
					// 发送数据包
					boolean bOk;
					bOk = packet.send(snddos);
					Log.i(TAG, "Had send one packet!");
					// 接收应答包
					if (bOk) {
						// 查看是否需要应答包
						if (packet.isNeedACK()) {
							NetPacket ackPack = new NetPacket();
							bOk = ackPack.receiveAck(snddis);
							if (!bOk) {
								Log.i(TAG, "SndThread receive ACK_PACKET Error!");
							}
						}
					}
					if (!bOk) {
						Message msg = new Message();
						msg.what = EXCEPTION_SNDSOCKET_SENDPACKET;
						mMsgHandler.sendMessage(msg);
						// 断开连接

						closeSndSockets();
						Log.e(TAG, "send one packet error!");
					}
					//
					packet = null;
					continue;
				}
				// //要求发送实时数据，此时不进行休眠处理,继续发送实时数据
				if (mIsSendWifiDatas) {
					continue;
				}
				// //没有数据包要发送
				// try
				// {
				// //此时休眠线程
				// Thread.sleep(200);
				// //发送网络监测数据包
				// NetPacket netCheckPack = new NetPacket();
				// netCheckPack.createNETCheckPack();
				// // netCheckPack.createTryDatasPacket();
				// //发送数据包
				// boolean bOk;
				// bOk = netCheckPack.send(snddos);
				// //
				// Log.i(TAG,"Had send one net_check_packet,type:="+(byte)netCheckPack.mBuf[4]);
				// //接收应答包
				// if(bOk)
				// {
				// if(netCheckPack.isNeedACK())
				// {
				// NetPacket ackPack = new NetPacket();
				// bOk = ackPack.receiveAck(snddis);
				// if(!bOk)
				// {
				// Log.i(TAG,"SndThread send NetCheckPacket receive ACK_PACKET Error!");
				// }
				// }
				// }
				// if(!bOk)
				// {
				// Message msg = new Message();
				// msg.what = EXCEPTION_SNDSOCKET_SENDPACKET;
				// mMsgHandler.sendMessage(msg);
				// //断开连接
				// closeSndSockets();
				// Log.i(TAG,"send one netcheck packet error!");
				// }
				// netCheckPack = null;
				// }
				// catch (InterruptedException e)
				// {
				// e.printStackTrace();
				// }
			}
			closeSndSockets();
			Log.e(TAG, "send thread exit!");
		}

	};

	private boolean sendTransfer(NetTransfer trans) {
		// Log.e(TAG, "Enter sendTransfer!");
		if (trans == null)
			return false;

		boolean bOk;

		if (mSndHadConnect) {
			// 如果是一个命令传输事务
			if (trans.isOneCommandTransfer()) {
				Message msg = new Message();
				msg.what = Global.MESSAGE_SND_COMMAND;
				msg.obj = "Sending command......";
				((LifeSearchActivity) mContext).mNetHandler.sendMessage(msg);
			}

			bOk = trans.SendAllPackets(snddos, snddis);

			if (trans.isOneCommandTransfer()) {
				Message msg = new Message();
				msg.what = Global.MESSAGE_SND_COMMAND;

				if (bOk) {
					msg.obj = "Send command successfully!";
				} else {
					msg.obj = "Failed to send command!";
				}
				((LifeSearchActivity) mContext).mNetHandler.sendMessage(msg);
			}

			if (!bOk) {
				Log.e(TAG, "SendAllPackets fail!");
				return false;
			}
		} else {
			bOk = false;
		}
		return true;
	}

	// //得到数据包
	public synchronized NetPacket getSendPacket() {
		int num;
		num = mSndPacketList.size();
		if (num <= 0)
			return null;
		//
		NetPacket ret = (NetPacket) mSndPacketList.remove(0);
		return ret;
	}

	// //增加数据包
	public synchronized void addSendPacket(NetPacket packet) {
		mSndPacketList.add(packet);
	}

	// //删除数据包
	public synchronized void clearSendPacket() {
		mSndPacketList.clear();
	}

	// //增加数据包
	public synchronized void addNetcheckPacket() {
		if (!isSndHadConnect()) {
			return;
		}

		NetPacket netCheckPack = new NetPacket();
		netCheckPack.createNETCheckPack();
		mSndPacketList.add(netCheckPack);
	}

	public synchronized NetTransfer getSendTransfer() {
		int size;
		NetTransfer trans = null;
		do {
			size = mSndTransferList.size();
			if (size > 0) {
				trans = (NetTransfer) mSndTransferList.remove(0);
				break;
			}
			size = mStatusTransferList.size();
			if (size > 0) {
				trans = (NetTransfer) mStatusTransferList.remove(0);
				break;
			}
		} while (false);
		return trans;
	}

	public synchronized void addSendTransfer(NetTransfer transfer) {
		mSndTransferList.add(transfer);
	}

	public synchronized void addDeviceStatusTransfer(NetTransfer transfer) {
		mStatusTransferList.add(transfer);
	}

	public synchronized void addNetcheckTransfer() {
		if (!isSndHadConnect()) {
			return;
		}

		if (mNetCheckTransfer == null) {
			mNetCheckTransfer = new NetTransfer();
			mNetCheckTransfer.createNETCheckTransfer();
		}

		if (mSndHadConnect) {
			mStatusTransferList.add(mNetCheckTransfer);
		}
	}

	public void sendCommand(byte[] buf, int size) {
		if (!isSndHadConnect()) {
			return;
		}

		NetTransfer comTrans = new NetTransfer();

		// if(comTrans != null)
		// {
		if (!comTrans.Create(Global.TRANSFER_TYPE_COM, (short) size)) {
			return;
		}
		comTrans.SetDatas(buf, size);

		// 将传输对象分割成传输包
		comTrans.CreateDivisionPackets();

		// 增加一个传输到发送队列
		addSendTransfer(comTrans);
		// }
	}

	// 定义消息
	final private int EXCEPTION_SNDSOCKET_CREATE = 1; // 生成“发送socket”错误
	final private int EXCEPTION_SNDSOCKET_READ = 2; // 发送socket读取数据错误
	final private int MESSAGE_SNDSOCKET_RCVDATAS = 3; // 发送socket接收到数据
	final private int EXCEPTION_RCVSOCKET_CREATE = 4; // 生成"接收socket"错误
	final private int EXCEPTION_RCVSOCKET_READ = 5; // 接收socket读取数据错误
	final private int EXCEPTION_SNDSOCKET_SENDPACKET = 6; // 发送socket发送数据包错误
	final private int MESSAGE_RCVSOCKET_HADCONNECTED = 100; // 接收socket已经连接到服务器
	final private int MESSAGE_SNDSOCKET_HADCONNECTED = 101; // 发送socket已经连接到服务器
	final private int MESSAGE_RCVSOCKET_DISCONNECTED = 102; // 接收socket已经断开服务器
	final private int MESSAGE_SNDSOCKET_DISCONNECTED = 103; // 发送socket已经断开服务区
	final private int MESSAGE_MANAGE_ONECOMMAND = 104; // 处理一个命令

	Handler mMsgHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			//
			int type = msg.what;
			switch (type) {
			case MESSAGE_RCVSOCKET_HADCONNECTED:
				mWiFiConnectStatus = WIFISTATUS_HADCONNECTHOST;

				break;
			case MESSAGE_SNDSOCKET_HADCONNECTED:
				mWiFiConnectStatus = WIFISTATUS_HADCONNECTHOST;
				// 发送当前设备状态
				// ((WirelessActivity)mActivity).sendNowSystemStatus();
				break;
			case MESSAGE_RCVSOCKET_DISCONNECTED:
			case EXCEPTION_RCVSOCKET_CREATE:
			case EXCEPTION_RCVSOCKET_READ:
				mWiFiConnectStatus = WIFISTATUS_NOTCONNECTHOST;
				// mIsSendWifiDatas = false;
				break;
			case MESSAGE_SNDSOCKET_DISCONNECTED:
			case EXCEPTION_SNDSOCKET_CREATE:
			case EXCEPTION_SNDSOCKET_SENDPACKET:
				mWiFiConnectStatus = WIFISTATUS_NOTCONNECTHOST;
				// mIsSendWifiDatas = false;
				// ((HelloAndroidActivity)mActivity).showAlertDialog();
				break;
			// 处理一个命令
			case MESSAGE_MANAGE_ONECOMMAND:
				NetCommand com = (NetCommand) msg.obj;
				// mApplication.mRadarDevice.manageNetCommand(com);
				// ((WifiTestActivity)mActivity).showToast();
				// Toast.makeText(mActivity, "Recv One Net Command!",
				// Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	// //
	public void setWifiSendDatasFlag(boolean flag) {
		mIsSendWifiDatas = flag;
	}

	// //连接到接收线程
	public void connectRcv() {
		mNeedRcvConnect = true;
	}

	// //
	public void stopThreads() {
		mStopRcvThread = true;
		mStopSndThread = true;
	}

	private boolean mStopRcvThread = false;
	final private int RCV_OK = 0; // 接收正常
	final private int RCV_TIMEOUT = 1; // 接收超时
	final private int RCV_ERROR = -1; // 接收过程出错
	private Runnable mThreadRcvRunnable = new Runnable() {
		public void run() {
			// 没有要求停止线程
			while (!mStopRcvThread) {
				// 如果不需要连接到主机，该线程休眠
				if (!mNeedRcvConnect) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					//
					continue;
				}
				// 连接到服务器
				if (!mRcvHadConnect) {
					try {
						closeRcvSockets();
						// Log.i(TAG,"before receive_socket!");
						// 生成socket并连接
						if (TCP_SERVER) {
							Log.d("mSocketRcv", "before accept");
							mSocketRcv = mSvrSocketRcv.accept();
							Log.d("mSocketRcv", "after accept");
							mSocketRcv.setSoTimeout(5000);
						} else {
							mSocketRcv = new Socket(mHostIP, mRcvPort);
							mSocketRcv.setSoTimeout(1000);
						}

						// 取得输入、输出流
						rcvdis = new DataInputStream(mSocketRcv.getInputStream());
						rcvdos = new DataOutputStream(mSocketRcv.getOutputStream());
						Log.i(TAG, "after receive_socket!");
						// 接收已经连接消息
						Message msg = new Message();
						msg.what = MESSAGE_RCVSOCKET_HADCONNECTED;
						mMsgHandler.sendMessage(msg);
					} catch (Exception e) {
						// Log.e(TAG,e.getMessage());
						Message msg = new Message();
						msg.what = EXCEPTION_RCVSOCKET_CREATE;
						mMsgHandler.sendMessage(msg);
						// 断开连接
						closeRcvSockets();
						//
						continue;
					}
					// 成功连接
					mRcvHadConnect = true;
					Log.e(TAG, "Rcv Connect OK!");
				}

				// 接收数据包
				NetPacket pack = new NetPacket();
				int bOk;
				bOk = pack.receive(rcvdis, true);
				switch (bOk) {
				case RCV_OK:
					if (pack.isNeedACK()) {
						NetPacket ackPack = new NetPacket();
						ackPack.createACKPacket();
						ackPack.send(rcvdos);
					}

					// Log.i(TAG, "packet type = " +
					// String.valueOf(pack.mPackType));
					manageRcvPacket(pack);
					break;
				// 超时
				case RCV_TIMEOUT:
					// Log.e(TAG, "RCV_Thread receive one packet timeout!");
					// 如果发送已经错误，此时说明出现了网络问题
					if (!mSndHadConnect) {
						closeRcvSockets();
						Log.e(TAG, "RCV_Thread receive one packet timeout, recv socket disconnect!");
					}
					break;
				case RCV_ERROR:
					Message msg = new Message();
					msg.what = EXCEPTION_RCVSOCKET_READ;
					mMsgHandler.sendMessage(msg);
					// 断开连接
					closeRcvSockets();
					Log.e(TAG, "RCV_Thread receive one packet error, recv socket disconnect!");
					break;
				}
			}
			// 停止线程，此时断开连接
			closeRcvSockets();
			Log.e(TAG, "RCV thread exit!");
		}
	};

	// //关闭接收socket
	public void closeRcvSockets() {
		mRcvHadConnect = false;
		try {
			if (mSocketRcv != null) {
				mSocketRcv.close();
				mSocketRcv = null;
			}
			if (rcvdis != null) {
				rcvdis.close();
				rcvdis = null;
			}
			if (rcvdos != null) {
				rcvdos.close();
				rcvdos = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void closesvrRcvSockets() {
		mRcvHadConnect = false;
		try {
			if (mSvrSocketRcv != null) {
				mSvrSocketRcv.close();
				mSvrSocketRcv = null;
			}
			if (rcvdis != null) {
				rcvdis.close();
				rcvdis = null;
			}
			if (rcvdos != null) {
				rcvdos.close();
				rcvdos = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 处理接收到的数据包
	public void manageRcvDatas() {
		String txt;
		txt = "receive:" + mRcvCount + " bytes!";
		Log.i(TAG, txt);
	}

	// 处理接收到的数据包
	public void manageRcvPacket(NetPacket pack) {
		// Log.e(TAG, "manage one packet");
		// 应答包|网络监测包，不需处理
		// if(pack.isACKType() ||
		// pack.isNetcheckType())
		// {
		// pack = null;
		// return;
		// }
		//
		// //命令包
		// if(pack.isCommandType())
		// {
		// manageCommandPacket(pack);
		// }
		// pack = null;
		short type = pack.getPackType();

		switch (type) {
		case Global.BEGTRANS_PACK:
			// 在每次生成新的传输事务前,删除由于包错误传输没有传输完成的事务
			DeleteAllErrTrans();
			// 接收到"开始传输"传输包，根据生成一个新的传输用来接收数据
			CreateOneRcvTrans(pack);
			// 将"开始传输"传输包加入到传输中
			AddOnePackToRcvTrans(pack);
			// Log.e(TAG, "Recv one BEGTRANS_PACK!");
			break;
		case Global.ENDTRANS_PACK:
			// 将"结束传输"传输包加入到传输中
			AddOnePackToRcvTrans(pack);
			// 接收到"结束传输"传输包，完成一个接收传输过程
			EndOneRcvTransfer(pack);
			// Log.i(TAG, "Recv one ENDTRANS_PACK!");
			break;
		case Global.DATA_PACK:
		case Global.COM_PACK:
			// 接收到"数据/命令"传输包，加入到指定的传输中
			AddOnePackToRcvTrans(pack);
			// Log.i(TAG, "Recv one DATA_PACK!");
			break;
		case Global.WAVE_PACK:
			Message msg = new Message();
			msg.what = Global.MESSAGE_RCV_WAVE;
			msg.obj = pack.getSndBuffer();
			msg.arg1 = pack.getBufLength();
			((LifeSearchActivity) mContext).mNetHandler.sendMessage(msg);
			// Log.e(TAG, "Recv one WAVE_PACK!");
			break;
		default:
			Log.e(TAG, "Recv one Unknown PACK!");
			break;
		}
	}

	private void DeleteAllErrTrans() {
		int i;
		NetTransfer trans;

		//
		for (i = 0; i < mRCVTransList.size(); i++) {
			trans = (NetTransfer) mRCVTransList.get(i);
			if (trans.IsNotRcvAllPackets()) {
				trans = null;
				mRCVTransList.remove(i);
				i--;
			}
		}
	}

	private boolean CreateOneRcvTrans(NetPacket packet) {
		boolean bOk = false;
		NetTransfer trans = null;
		long transID;
		// 寻找相同ID的已有传输
		transID = packet.getTransferID();
		trans = FindOneRcvTransferFromeID(transID);
		if (trans != null) {
			// 找到了相同ID的传输，删除前一个ID
			Log.e(TAG, "Now find one same ID Transfer!\r\n");
			DelOneRcvTransferFromeID(transID);
		}
		// 得到传输类型，根据类型生成传输对象
		short transType;
		transType = packet.getTransferType();
		switch (transType) {
		// 是一个数据传输
		case Global.TRANSFER_TYPE_DATAS:
			//
			trans = new NetTransfer();
			if (!trans.Create(packet)) {
				trans = null;
				Log.e(TAG, "Create Datas_Transfer fail!\r\n");
			}
			break;
		// 是一个命令传输
		case Global.TRANSFER_TYPE_COM:
			trans = new NetTransfer();
			if (!trans.Create(packet)) {
				trans = null;
				Log.e(TAG, "Create COMMAND_TRANSFER fail!\r\n");
			}
			break;
		case Global.TRANSFER_TYPE_DEVICESTATUS:
		case Global.TRANSFER_TYPE_DETECTRESULT:
			trans = new NetTransfer();
			if (!trans.Create(packet)) {
				trans = null;
				Log.e(TAG, "Create TRANSFER_TYPE_DETECTRESULT fail!\r\n");
			}
			break;
		default:
			break;

		}
		if (trans != null) {
			AddOneReceiveTransfer(trans);
			bOk = true;
		}
		return bOk;
	}

	private boolean AddOneReceiveTransfer(NetTransfer trans) {
		mRCVTransList.add(trans);

		return false;
	}

	// 结束一次接收事务:可以进行处理了
	private boolean EndOneRcvTransfer(NetPacket packet) {
		NetTransfer trans = null;

		//
		boolean bOk = false;
		long ID;
		int num, i;
		ID = packet.getTransferID();

		num = mRCVTransList.size();
		for (i = 0; i < num; i++) {
			trans = (NetTransfer) mRCVTransList.get(i);

			if (trans.getID() == ID) {
				mRCVTransList.remove(i);
				break;
			}
		}
		if (i == num)
			trans = null;

		// 进行处理
		if (trans != null) {
			byte[] ptrBuf;
			// 处理接收到的所有数据包
			if (trans.ManageReceivePackets()) {
				short type;
				type = trans.getTansferType();
				ptrBuf = trans.getContextBuf();
				switch (type) {
				// 是一个命令传输
				case Global.TRANSFER_TYPE_COM:
					NetCommand com = new NetCommand();

					if (trans.getCommand(com)) {
						Message msg = new Message();
						msg.what = Global.MESSAGE_RCV_COMMAND;
						msg.obj = com;
						((LifeSearchActivity) mContext).mNetHandler.sendMessage(msg);
					}
					Log.e(TAG, "Now Recv One Command Transfer!" + String.valueOf(com.mComCode));
					break;
				// 是一个"回波"数据传输
				case Global.TRANSFER_TYPE_DATAS: // 网络检查包
					WaveData data = new WaveData();

					// Log.e(TAG, "Now Recv One Wave data Infs!");
					if (trans.getDatas(data)) {
						//
					}
					break;
				// 是一个设备状态或者探测结果传输
				case Global.TRANSFER_TYPE_DEVICESTATUS:
				case Global.TRANSFER_TYPE_DETECTRESULT:
					DevData devData = new DevData();

					if (trans.getDevData(devData)) {
						// Log.e(TAG,
						// "Now Recv One Message of Dev_Status_Infs!");
						Message msg = new Message();
						msg.what = Global.MESSAGE_RCV_DEVICE_DATA;
						msg.obj = devData;
						((LifeSearchActivity) mContext).mNetHandler.sendMessage(msg);
					} else {
						Log.e(TAG, "The Transfer GetDevData() fail!");
					}
					break;
				}
			}

			bOk = true;
		}

		return bOk;
	}

	private NetTransfer FindOneRcvTransferFromeID(long transID) {
		NetTransfer trans = null;

		int num, i;
		num = mRCVTransList.size();
		for (i = 0; i < num; i++) {
			trans = (NetTransfer) mRCVTransList.get(i);
			if (trans.getID() == transID)
				break;
		}
		if (i == num)
			trans = null;

		return trans;
	}

	// 删除指定ＩＤ的传输对象
	private void DelOneRcvTransferFromeID(long ID) {
		NetTransfer trans = null;

		int num, i;
		num = mRCVTransList.size();
		for (i = 0; i < num; i++) {
			trans = (NetTransfer) mRCVTransList.get(i);
			if (trans.getID() == ID) {
				mRCVTransList.remove(i);
				break;
			}
		}
	}

	// 将一个数据包加入到接收传输中
	private boolean AddOnePackToRcvTrans(NetPacket packet) {
		long ID;
		ID = packet.getTransferID();
		int num, i;
		boolean bOk = false;
		NetTransfer trans;

		//
		num = mRCVTransList.size();
		for (i = 0; i < num; i++) {
			trans = (NetTransfer) mRCVTransList.get(i);
			if (trans.getID() == ID) {
				trans.AddOneRcvPacket(packet);
				bOk = true;
				break;
			}
		}

		return bOk;
	}

	// 处理命令包
	public void manageCommandPacket(NetPacket pack) {
		// NetCommand com;
		// com = pack.getCommand();
		//
		// // 根据命令进行处理
		// Message msg = new Message();
		// msg.what = MESSAGE_MANAGE_ONECOMMAND;
		// msg.obj = com;
		// mMsgHandler.sendMessage(msg);

	}

	// 发送当前雷达状态
	public void sendRadarStatus() {
		// mApplication.mRadarDevice.sendNowSystemStatus();
	}

	public void sendNetCommand(byte[] comBuf, int len) {
		NetTransfer comTrans = new NetTransfer();

		if (!comTrans.Create(Global.TRANSFER_TYPE_COM, (short) len)) {
			return;
		}
		comTrans.SetDatas(comBuf, len);
		comTrans.CreateDivisionPackets();
		addSendTransfer(comTrans);
	}
}
