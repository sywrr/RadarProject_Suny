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
	String mHostIP = "192.168.1.14"; // ���ӵ�������ip��ַ
	String mLocalIP = "";
	int mRcvPort = 5000;// 5003;//59671; //�������������ݵĶ˿�
	int mSndPort = 5001;// 5002;//59670; //�������������ݵĶ˿�
	boolean mSndHadConnect = false; // �����߳��Ѿ����ӱ�־
	static boolean mRcvHadConnect = false; // �����߳��Ѿ����ӱ�־

	private int mMaxPackBufLength = 1024 * 5;
	// /�����߳����
	private Thread mThreadRcv = null;
	private Socket mSocketRcv = null;
	private ServerSocket mSvrSocketRcv = null;
	DataInputStream rcvdis = null;
	DataOutputStream rcvdos = null;
	private byte[] mRcvBuffer = new byte[mMaxPackBufLength];
	private int mRcvCount;
	private boolean mNeedRcvConnect = false; // Ҫ�����ӵ�����

	// /�����߳����
	private Thread mThreadSnd = null;
	private Socket mSocketSnd = null;
	private ServerSocket mSvrSocketSnd = null;
	DataInputStream snddis = null;
	DataOutputStream snddos = null;
	private boolean mNeedSndConnect = false; // Ҫ�����ӵ�����

	private int WIFISTATUS_NOTCONNECTHOST = 1; // ��û�����ӵ�����
	private int WIFISTATUS_CONNECTINGHOST = 2; // ���ڽ�����������
	private int WIFISTATUS_HADCONNECTHOST = 3; // �Ѿ����ӵ�����
	private int WIFISTATUS_DISCONNECTINGHOST = 4; // ���ڶϿ���������
	private int mWiFiConnectStatus = WIFISTATUS_NOTCONNECTHOST;

	private NetTransfer mNetCheckTransfer = null;

	//
	// private MyApplication mApplication;

	//
	private boolean mIsSendWifiDatas = false; // �Ƿ����ڷ���wifi����
	private Context mContext = null;

	//
	public WiFiNetDevice() {
		if (TCP_SERVER) {
			createSvrSocket();
		}
		// ���ɷ����߳�
		mThreadSnd = new Thread(mThreadSndRunnable);
		mStopSndThread = false;
		mThreadSnd.start();

		// ���ɽ����߳�
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
		// ���ɷ����߳�
		mThreadSnd = new Thread(mThreadSndRunnable);
		mStopSndThread = false;
		mThreadSnd.start();

		// ���ɽ����߳�
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
	// ��û������
	public boolean isWiFiNotConnect() {
		return mWiFiConnectStatus == WIFISTATUS_NOTCONNECTHOST;
	}

	// ��������
	public boolean isWiFiConnecting() {
		return mWiFiConnectStatus == WIFISTATUS_CONNECTINGHOST;
	}

	// ���ڶϿ�����
	public boolean isWiFiDisconnecting() {
		return mWiFiConnectStatus == WIFISTATUS_DISCONNECTINGHOST;
	}

	// �Ѿ�����
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
		// Ѱ�ұ���ip��ַ
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
			String errStr = "��ȡIP��ַ�쳣:" + ex.getMessage() + "\n";
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

	// ����wifi����״̬Ϊ����������
	public void setWiFiStatus_Connecting() {
		mWiFiConnectStatus = WIFISTATUS_CONNECTINGHOST;
	}

	//
	public void setWiFiStatus_NotConnect() {
		mWiFiConnectStatus = WIFISTATUS_NOTCONNECTHOST;
	}

	// �ж��Ƿ��Ѿ�����
	public boolean isHadConnect() {
		return isRcvHadConnect() && isSndHadConnect();
	}

	public boolean isRcvHadConnect() {
		return mRcvHadConnect;
	}

	public boolean isSndHadConnect() {
		return mSndHadConnect;
	}

	// �ӷ������Ͽ�����
	public void disconnectToHost() {
		Log.i(TAG, "disconnectToHost!");
		disconnectRcv();
		Log.i(TAG, "disconnectRcv");
		disconnectSnd();
		Log.i(TAG, "disconnectSnd");
	}

	// //�Ͽ������߳�:
	public void disconnectRcv() {
		mNeedRcvConnect = false;
		// �ر�socket�׽��֣��Ͽ�����
		closeRcvSockets();
	}

	// //�Ͽ������߳�
	public void disconnectSnd() {
		mNeedSndConnect = false;
		// �ر�socket�׽��֣��Ͽ�����
		closeSndSockets();
	}

	// //�رշ���socket
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

	// ���ӵ�����
	public void connectToHost() {
		if (!mSndHadConnect)
			connectSnd();
		//
		if (!mRcvHadConnect)
			connectRcv();
	}

	// ���ӵ������߳�
	public void connectSnd() {
		mNeedSndConnect = true;
	}

	// ��������
	public void sendDatas(short[] buf, int size) {
		if (!isSndHadConnect()) {
			return;
		}
		// if(!isWiFiHadConnect())
		// return;
		// if(!isWiFiSendDatas())
		// return;
		// �������ݰ�
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

	// �����߳�:������������������Ϣ
	private boolean mStopSndThread = false;
	private List<Object> mSndPacketList = new ArrayList<Object>();
	private List<Object> mSndTransferList = new ArrayList<Object>();
	private List<Object> mStatusTransferList = new ArrayList<Object>();
	private Runnable mThreadSndRunnable = new Runnable() {
		public void run() {
			// �����Ҫ��ֹͣ�߳�
			while (!mStopSndThread)// true
			{
				// //�������Ҫ���ӵ����������߳�����
				if (!mNeedSndConnect) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					//
					continue;
				}

				// //��Ҫ���ӵ����������һ�û�����ӣ���ʱ���ӵ�������
				if (!mSndHadConnect) {
					try {
						closeSndSockets();
						// ����socket������

						if (TCP_SERVER) {
							Log.d("serversnd", "before accept");
							mSocketSnd = mSvrSocketSnd.accept();
							Log.d("serversnd", "after accept");
							mSocketSnd.setSoTimeout(5000);
						} else {
							mSocketSnd = new Socket(mHostIP, mSndPort);
							mSocketSnd.setSoTimeout(1000);
						}

						// ȡ�����롢�����
						snddis = new DataInputStream(mSocketSnd.getInputStream());
						snddos = new DataOutputStream(mSocketSnd.getOutputStream());
						Log.i(TAG, "Enter Create SND Socket!");
					} catch (Exception e) {
						// Log.e(TAG, e.getMessage());
						Message msg = new Message();
						msg.what = EXCEPTION_SNDSOCKET_CREATE;
						mMsgHandler.sendMessage(msg);
						// �Ͽ�����
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
						// �Ͽ�����

						closeSndSockets();
						Log.e(TAG, "send error, send socket disconnect!");
					}
				}

				// //�������ݰ�
				// �õ����ݰ�
				NetPacket packet = getSendPacket();
				if (packet != null) {
					// �������ݰ�
					boolean bOk;
					bOk = packet.send(snddos);
					Log.i(TAG, "Had send one packet!");
					// ����Ӧ���
					if (bOk) {
						// �鿴�Ƿ���ҪӦ���
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
						// �Ͽ�����

						closeSndSockets();
						Log.e(TAG, "send one packet error!");
					}
					//
					packet = null;
					continue;
				}
				// //Ҫ����ʵʱ���ݣ���ʱ���������ߴ���,��������ʵʱ����
				if (mIsSendWifiDatas) {
					continue;
				}
				// //û�����ݰ�Ҫ����
				// try
				// {
				// //��ʱ�����߳�
				// Thread.sleep(200);
				// //�������������ݰ�
				// NetPacket netCheckPack = new NetPacket();
				// netCheckPack.createNETCheckPack();
				// // netCheckPack.createTryDatasPacket();
				// //�������ݰ�
				// boolean bOk;
				// bOk = netCheckPack.send(snddos);
				// //
				// Log.i(TAG,"Had send one net_check_packet,type:="+(byte)netCheckPack.mBuf[4]);
				// //����Ӧ���
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
				// //�Ͽ�����
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
			// �����һ�����������
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

	// //�õ����ݰ�
	public synchronized NetPacket getSendPacket() {
		int num;
		num = mSndPacketList.size();
		if (num <= 0)
			return null;
		//
		NetPacket ret = (NetPacket) mSndPacketList.remove(0);
		return ret;
	}

	// //�������ݰ�
	public synchronized void addSendPacket(NetPacket packet) {
		mSndPacketList.add(packet);
	}

	// //ɾ�����ݰ�
	public synchronized void clearSendPacket() {
		mSndPacketList.clear();
	}

	// //�������ݰ�
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

		// ���������ָ�ɴ����
		comTrans.CreateDivisionPackets();

		// ����һ�����䵽���Ͷ���
		addSendTransfer(comTrans);
		// }
	}

	// ������Ϣ
	final private int EXCEPTION_SNDSOCKET_CREATE = 1; // ���ɡ�����socket������
	final private int EXCEPTION_SNDSOCKET_READ = 2; // ����socket��ȡ���ݴ���
	final private int MESSAGE_SNDSOCKET_RCVDATAS = 3; // ����socket���յ�����
	final private int EXCEPTION_RCVSOCKET_CREATE = 4; // ����"����socket"����
	final private int EXCEPTION_RCVSOCKET_READ = 5; // ����socket��ȡ���ݴ���
	final private int EXCEPTION_SNDSOCKET_SENDPACKET = 6; // ����socket�������ݰ�����
	final private int MESSAGE_RCVSOCKET_HADCONNECTED = 100; // ����socket�Ѿ����ӵ�������
	final private int MESSAGE_SNDSOCKET_HADCONNECTED = 101; // ����socket�Ѿ����ӵ�������
	final private int MESSAGE_RCVSOCKET_DISCONNECTED = 102; // ����socket�Ѿ��Ͽ�������
	final private int MESSAGE_SNDSOCKET_DISCONNECTED = 103; // ����socket�Ѿ��Ͽ�������
	final private int MESSAGE_MANAGE_ONECOMMAND = 104; // ����һ������

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
				// ���͵�ǰ�豸״̬
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
			// ����һ������
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

	// //���ӵ������߳�
	public void connectRcv() {
		mNeedRcvConnect = true;
	}

	// //
	public void stopThreads() {
		mStopRcvThread = true;
		mStopSndThread = true;
	}

	private boolean mStopRcvThread = false;
	final private int RCV_OK = 0; // ��������
	final private int RCV_TIMEOUT = 1; // ���ճ�ʱ
	final private int RCV_ERROR = -1; // ���չ��̳���
	private Runnable mThreadRcvRunnable = new Runnable() {
		public void run() {
			// û��Ҫ��ֹͣ�߳�
			while (!mStopRcvThread) {
				// �������Ҫ���ӵ����������߳�����
				if (!mNeedRcvConnect) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					//
					continue;
				}
				// ���ӵ�������
				if (!mRcvHadConnect) {
					try {
						closeRcvSockets();
						// Log.i(TAG,"before receive_socket!");
						// ����socket������
						if (TCP_SERVER) {
							Log.d("mSocketRcv", "before accept");
							mSocketRcv = mSvrSocketRcv.accept();
							Log.d("mSocketRcv", "after accept");
							mSocketRcv.setSoTimeout(5000);
						} else {
							mSocketRcv = new Socket(mHostIP, mRcvPort);
							mSocketRcv.setSoTimeout(1000);
						}

						// ȡ�����롢�����
						rcvdis = new DataInputStream(mSocketRcv.getInputStream());
						rcvdos = new DataOutputStream(mSocketRcv.getOutputStream());
						Log.i(TAG, "after receive_socket!");
						// �����Ѿ�������Ϣ
						Message msg = new Message();
						msg.what = MESSAGE_RCVSOCKET_HADCONNECTED;
						mMsgHandler.sendMessage(msg);
					} catch (Exception e) {
						// Log.e(TAG,e.getMessage());
						Message msg = new Message();
						msg.what = EXCEPTION_RCVSOCKET_CREATE;
						mMsgHandler.sendMessage(msg);
						// �Ͽ�����
						closeRcvSockets();
						//
						continue;
					}
					// �ɹ�����
					mRcvHadConnect = true;
					Log.e(TAG, "Rcv Connect OK!");
				}

				// �������ݰ�
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
				// ��ʱ
				case RCV_TIMEOUT:
					// Log.e(TAG, "RCV_Thread receive one packet timeout!");
					// ��������Ѿ����󣬴�ʱ˵����������������
					if (!mSndHadConnect) {
						closeRcvSockets();
						Log.e(TAG, "RCV_Thread receive one packet timeout, recv socket disconnect!");
					}
					break;
				case RCV_ERROR:
					Message msg = new Message();
					msg.what = EXCEPTION_RCVSOCKET_READ;
					mMsgHandler.sendMessage(msg);
					// �Ͽ�����
					closeRcvSockets();
					Log.e(TAG, "RCV_Thread receive one packet error, recv socket disconnect!");
					break;
				}
			}
			// ֹͣ�̣߳���ʱ�Ͽ�����
			closeRcvSockets();
			Log.e(TAG, "RCV thread exit!");
		}
	};

	// //�رս���socket
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

	// ������յ������ݰ�
	public void manageRcvDatas() {
		String txt;
		txt = "receive:" + mRcvCount + " bytes!";
		Log.i(TAG, txt);
	}

	// ������յ������ݰ�
	public void manageRcvPacket(NetPacket pack) {
		// Log.e(TAG, "manage one packet");
		// Ӧ���|������������账��
		// if(pack.isACKType() ||
		// pack.isNetcheckType())
		// {
		// pack = null;
		// return;
		// }
		//
		// //�����
		// if(pack.isCommandType())
		// {
		// manageCommandPacket(pack);
		// }
		// pack = null;
		short type = pack.getPackType();

		switch (type) {
		case Global.BEGTRANS_PACK:
			// ��ÿ�������µĴ�������ǰ,ɾ�����ڰ�������û�д�����ɵ�����
			DeleteAllErrTrans();
			// ���յ�"��ʼ����"���������������һ���µĴ���������������
			CreateOneRcvTrans(pack);
			// ��"��ʼ����"��������뵽������
			AddOnePackToRcvTrans(pack);
			// Log.e(TAG, "Recv one BEGTRANS_PACK!");
			break;
		case Global.ENDTRANS_PACK:
			// ��"��������"��������뵽������
			AddOnePackToRcvTrans(pack);
			// ���յ�"��������"����������һ�����մ������
			EndOneRcvTransfer(pack);
			// Log.i(TAG, "Recv one ENDTRANS_PACK!");
			break;
		case Global.DATA_PACK:
		case Global.COM_PACK:
			// ���յ�"����/����"����������뵽ָ���Ĵ�����
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
		// Ѱ����ͬID�����д���
		transID = packet.getTransferID();
		trans = FindOneRcvTransferFromeID(transID);
		if (trans != null) {
			// �ҵ�����ͬID�Ĵ��䣬ɾ��ǰһ��ID
			Log.e(TAG, "Now find one same ID Transfer!\r\n");
			DelOneRcvTransferFromeID(transID);
		}
		// �õ��������ͣ������������ɴ������
		short transType;
		transType = packet.getTransferType();
		switch (transType) {
		// ��һ�����ݴ���
		case Global.TRANSFER_TYPE_DATAS:
			//
			trans = new NetTransfer();
			if (!trans.Create(packet)) {
				trans = null;
				Log.e(TAG, "Create Datas_Transfer fail!\r\n");
			}
			break;
		// ��һ�������
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

	// ����һ�ν�������:���Խ��д�����
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

		// ���д���
		if (trans != null) {
			byte[] ptrBuf;
			// ������յ����������ݰ�
			if (trans.ManageReceivePackets()) {
				short type;
				type = trans.getTansferType();
				ptrBuf = trans.getContextBuf();
				switch (type) {
				// ��һ�������
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
				// ��һ��"�ز�"���ݴ���
				case Global.TRANSFER_TYPE_DATAS: // �������
					WaveData data = new WaveData();

					// Log.e(TAG, "Now Recv One Wave data Infs!");
					if (trans.getDatas(data)) {
						//
					}
					break;
				// ��һ���豸״̬����̽��������
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

	// ɾ��ָ���ɣĵĴ������
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

	// ��һ�����ݰ����뵽���մ�����
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

	// ���������
	public void manageCommandPacket(NetPacket pack) {
		// NetCommand com;
		// com = pack.getCommand();
		//
		// // ����������д���
		// Message msg = new Message();
		// msg.what = MESSAGE_MANAGE_ONECOMMAND;
		// msg.obj = com;
		// mMsgHandler.sendMessage(msg);

	}

	// ���͵�ǰ�״�״̬
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
