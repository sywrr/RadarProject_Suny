
#include "PortUDP.h"
using namespace DAQ;

#ifdef _WIN32
#define DELAT_TIME 500
#else
#define DELAT_TIME 500 *1000
#endif

/** @brief 构造函数 */
CPortUDP::CPortUDP(const string &strTargetIp,int nSendPort,int nReceivePort)
{
	m_strTargetIp = strTargetIp;
	m_nSendPort = nSendPort;
	m_nReceivePort = nReceivePort;
	m_bOpened = false;
	m_socketReceive = INVALID_SOCKET;
	m_socketSend = INVALID_SOCKET;
	m_pCallBack = nullptr;
#ifdef _WIN32
	WSADATA wsd;
	WSAStartup(MAKEWORD(2, 2), &wsd);
#endif
}

/** @brief 析构函数 */
CPortUDP::~CPortUDP()
{
	ClosePort();
#ifdef _WIN32
	WSACleanup();
#endif
}

/** @brief 打开端口 */
bool CPortUDP::OpenPort()
{
	bool bRet = false;
	if(!m_bOpened)
	{
		bRet = ConnectReceive();
		if (bRet)
		{
			ConnectSend();
			string strThread = "读UDP:"+ m_strTargetIp;
			m_threadRead.SetRunInterval(0);
			m_threadRead.Start(this, (void *) ReadThread ,strThread.c_str());
			m_bOpened = true;
		}
	}
	return bRet;
}

/** @brief 关闭端口 */
bool CPortUDP::ClosePort()
{
	if(m_bOpened)
	{
		DisConnectSend();
		DisConnectReceive();
		m_threadRead.Stop();
		m_bOpened = false;
	}
	return true;
}

/** @brief 发送数据 */
bool CPortUDP::SendData(void *pDataBuffer,int nBufferLength)
{
	if(pDataBuffer == NULL)
		return false;
	if(nBufferLength<=0)
		return false;

	if(!IsSendOpen())
	{
		ConnectSend();
		return false;
	}
	sockaddr_in address;
	address.sin_family=AF_INET;
	address.sin_port=htons(m_nSendPort);
	if(m_strTargetIp.empty()){
		address.sin_addr.s_addr=htonl(INADDR_BROADCAST);//inet_addr
	}
	else{
		address.sin_addr.s_addr = inet_addr(m_strTargetIp.c_str());
	}
	int nHaveSend = 0;
	do {
		const char *pData = (const char *)pDataBuffer + nHaveSend;
		int nLeft = nBufferLength - nHaveSend;
		int nRealSend = sendto(m_socketReceive,pData,nLeft,0,(sockaddr *) & address, sizeof(address));
		if( nRealSend == SOCKET_ERROR)
		{
			DisConnectSend();
			return false;
		}
		nHaveSend += nRealSend;
	} while(nHaveSend<nBufferLength);
	return true;
}

/** @brief  读UDP端口数据线程 */
void CPortUDP::ReadThread(void *pThis)
{
	((CPortUDP *)pThis)->Read();
}

/** @brief 判断端口是否打开 */
bool CPortUDP::IsOpen()
{
	return 	m_socketReceive != INVALID_SOCKET && 	m_socketSend != INVALID_SOCKET;
}

/** @brief 读线程执行函数 */
void CPortUDP::Read()
{
	if(!IsReceiveOpen())
	{
		ConnectReceive();
		Sleep(DELAT_TIME);
		return;
	}

	sockaddr_in address;
#if defined _WIN32 || defined(_HP_UX)
	int nAddressLength = sizeof(address);
#else
	socklen_t nAddressLength = sizeof(address);
#endif

	char szBuffer[556];
	int nRealRead = recvfrom(m_socketReceive, szBuffer, 556, 0,
							 (sockaddr *)& address, &nAddressLength);
	if (nRealRead == SOCKET_ERROR || nRealRead == 0)
	{
		DisConnectReceive();
	}
	if (nRealRead > 0)
		m_pCallBack->OnReceiveData(szBuffer, nRealRead);

}

/** @brief 停止发送 */
void CPortUDP::DisConnectSend()
{
	if(m_socketSend != INVALID_SOCKET)
	{
		shutdown(m_socketSend,SD_BOTH);
		closesocket(m_socketSend);
		m_socketSend = INVALID_SOCKET;
	}

}

/** @brief 停止接收 */
void CPortUDP::DisConnectReceive()
{
	if(m_socketReceive != INVALID_SOCKET)
	{
		shutdown(m_socketReceive,SD_BOTH);
		closesocket(m_socketReceive);
		m_socketReceive = INVALID_SOCKET;
	}
	m_pCallBack->m_isLinked = false;
}

/** @brief 连接发送 */
bool CPortUDP::ConnectSend()
{
	DisConnectSend();
	SOCKET sock = socket(AF_INET, SOCK_DGRAM, 0);
	if (sock == INVALID_SOCKET)
	{
		return false;
	}

	sockaddr_in address;
	address.sin_family = AF_INET;
	address.sin_port = htons(0);			
	address.sin_addr.s_addr = htonl(INADDR_ANY );

	// Variable to set the broadcast option with setsockopt ().
	BOOL fBroadcast = TRUE;
	if(setsockopt (sock,SOL_SOCKET,SO_BROADCAST,(char *) &fBroadcast,sizeof(BOOL))==SOCKET_ERROR)
	{
		closesocket(sock);
		return false;
	}
	//2绑定
	if(::bind(sock,(sockaddr *) & address, sizeof(address)) == SOCKET_ERROR)
	{
		closesocket(sock);
		return false;
	}
	m_socketSend = sock;
}

/** @brief 连接接收 */
bool CPortUDP::ConnectReceive()
{
	DisConnectReceive();
	//1建立SOCKET
	SOCKET sock = socket(AF_INET, SOCK_DGRAM, 0);
	if (sock == INVALID_SOCKET)
	{
		return false;
	}

	sockaddr_in address;
	address.sin_family = AF_INET;
	address.sin_port = htons(m_nReceivePort);
	address.sin_addr.s_addr = htonl(INADDR_ANY );

	int nRecvBufSize = 1024 * 1024 * 10; //1024/4=256道数据，最小缓存0.5s数据
	setsockopt(sock, SOL_SOCKET, SO_RCVBUF, (const char *)&nRecvBufSize, sizeof(nRecvBufSize));

	//2绑定
	if(::bind(sock,(sockaddr *) & address, sizeof(address)) == SOCKET_ERROR)
	{
		closesocket(sock);
		return false;
	}
	m_socketReceive = sock;

	return true;
}

/** @brief 发送连接是否打开 */
bool CPortUDP::IsSendOpen()
{
	return m_socketSend != INVALID_SOCKET;
}

/** @brief 接受连接是否打开 */
bool CPortUDP::IsReceiveOpen()
{
	return m_socketReceive != INVALID_SOCKET;
}

/** @brief 是否已连接 */
bool CPortUDP::IsConnect()
{
	return 	m_socketReceive != INVALID_SOCKET ;

}

/** @brief 设置回调 */
void CPortUDP::SetCallBack(IPortCallBack *pCallBack)
{
	m_pCallBack = pCallBack;
}