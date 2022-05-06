
#include "PortTCPClient.h"
#include <sstream>
#include "iostream"

#ifndef _WIN32
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/time.h>
#include <unistd.h>
#include <fcntl.h>
#endif

#ifdef _WIN32
#define DELAT_TIME 500
#else
#define DELAT_TIME 500 *1000
#endif
using namespace DAQ;

/** 构造函数 */
CPortTCPClient::CPortTCPClient(const string &strIP,int nPort)
	:m_strIPAddress(strIP),
	 m_nPort(nPort),
	 m_socket(INVALID_SOCKET),
	 m_bOpened(false)
{
#ifdef _WIN32
	WSADATA wsd;
	WSAStartup(MAKEWORD(2, 2), &wsd);
#endif
}

/** 析构函数 */
CPortTCPClient::~CPortTCPClient()
{
	ClosePort();
#ifdef _WIN32
	WSACleanup();
#endif
}

/**
 * @brief 打开端口
 *
 * @return 成功返回真
 */
bool CPortTCPClient::OpenPort()
{
	if(IsOpen())
	{
		return true;
	}

	ostringstream strThread;
	m_threadRead.SetRunInterval(0);
	m_threadRead.Start((void *)this,(void *)ReadThread,strThread.str().c_str());

	if(!Connect())
	{
		m_bOpened = false;
		m_threadRead.Stop();
		return false;
	}
	m_bOpened = true;
	return true;
}

/**
 * @brief 关闭端口
 *
 * 执行顺寻 1.关闭读取线程 2.关闭端口 3.清除缓冲区
 *
 * @return 成功返回真
 */
bool CPortTCPClient::ClosePort()
{
	bool bEverOpened = false;

	if(!IsOpen())
	{
		return true;
	}

	DisConnect();

	m_threadRead.Stop();

	m_bOpened = false;

	return true;
}

//
/**
 * @brief 发送数据
 *
 * @param pDataBuffer 数据缓冲区地址
 * @param nBufferLength  数据长度
 *
 * @return 成功返回TRUE
 */
bool CPortTCPClient::SendData(void *pDataBuffer,int nBufferLength)
{
	if(pDataBuffer == NULL)
		return false;
	if(nBufferLength<=0)
		return false;

	if(!IsOpen())
		return false;

	if(!IsConnect())
		return false;

	int nHaveSend = 0;
	do {

		const char *pData = (const char *)pDataBuffer+nHaveSend;
		int nLeft = nBufferLength-nHaveSend;
		int nRealSend = send(m_socket,pData,nLeft,0);
		if( nRealSend == SOCKET_ERROR)
		{
			DisConnect();
			return false;
		}
		nHaveSend += nRealSend;
	} while(nHaveSend<nBufferLength);
	return true;
}

/**
 * @brief  端口监视线程,读数据取m_bufferReceive中
 *
 *
 * @param pThis
 *
 * @return  无
 */
void CPortTCPClient::ReadThread(void *pThis)
{
	((CPortTCPClient *)pThis)->Read();
}


/**
 * @brief 端口监视线程,读数据取存入m_bufferReceive中
 *
 *
 * @return 无
 */
void CPortTCPClient::Read()
{
	if (m_bOpened == false)
	{
		Sleep(DELAT_TIME);
		return;
	}

	if(!IsConnect())
	{
		Sleep(DELAT_TIME);
		return;
	}

	char szBuffer[8096];
	fd_set readfds;
	fd_set exceptfds;
	struct timeval timeout;
	string m_buff;
	while (true)
	{
		if (false == m_bOpened || INVALID_SOCKET == m_socket)
		{
			return;
		}
		FD_ZERO(&readfds);
		FD_SET(m_socket, &readfds);

		FD_ZERO(&exceptfds);
		FD_SET(m_socket, &exceptfds);

		timeout.tv_sec = 1;
		timeout.tv_usec = 0;
		int nResult = select((int)m_socket + 1, &readfds, NULL, &exceptfds, &timeout);
		if (nResult >= 0)
		{
#ifdef _WIN32
			if (exceptfds.fd_count > 0)
			{
				return;
			}
			else if (readfds.fd_count > 0)
			{
				int nRealRead = recv(m_socket, szBuffer, 8096, 0);//取头
				if (nRealRead == SOCKET_ERROR) //SOCKET读取错误
				{
					DisConnect();
				}
				else if (nRealRead == 0)  //服务器断开连接
				{
					DisConnect();
					return;
				}
				m_buff.append(szBuffer, nRealRead);
				while (true)
				{
					if (m_buff.size() < 40){
						m_buff.clear();
						return;
					}

					if (m_buff.size() <= 556)
					{//小于等于556
						int posHead = m_buff.find("\x07\xFF");
						if (posHead == 0)
						{//在头部
							short length = m_buff.at(posHead + 2) << 8 | m_buff.at(posHead + 3);
							if (length == 32)//命令
							{
								short end = m_buff.at(length + 8 - 2) << 8 | m_buff.at(length + 8 - 1);
								if (end == 0x5555){
									if (nRealRead)
										m_pCallBack->OnReceiveData((char*)&m_buff.at(0), m_buff.size());
									m_buff.erase(m_buff.begin(), m_buff.begin() + length + 8);
								}
							}
							else if (length == 548)//雷达
							{
								if (m_buff.size() == length + 8) //整帧
								{
									short end = m_buff.at(length + 8 - 2) << 8 | m_buff.at(length + 8 - 1);
									if (end == 0x5555){
										if (nRealRead)
											m_pCallBack->OnReceiveData((char*)&m_buff.at(0), m_buff.size());
										m_buff.erase(m_buff.begin(), m_buff.begin() + length + 8);
									}
								}
								else if (m_buff.size() < length + 8) //半帧
								{
									break;
								}
							}
						}
						else if (posHead>0)//没有头
							m_buff.erase(m_buff.begin(), m_buff.begin() + posHead + 2);
						else
							m_buff.clear();
					}
					else{//大于556
						int posHead = m_buff.find("\x07\xFF");
						if (posHead == 0)
						{//头在开始
							short length = m_buff.at(posHead + 2) << 8 | m_buff.at(posHead + 3);
							if (length == 548 || length == 32)//分帧数据
							{
								short end = m_buff.at(length + 8 - 2) << 8 | m_buff.at(length + 8 - 1);
								if (end == 0x5555){
									if (nRealRead)
										m_pCallBack->OnReceiveData((char*)&m_buff.at(0), length + 8);
									m_buff.erase(m_buff.begin(), m_buff.begin() + length + 8);
								}
							}//length
						}
						else if (posHead>0)//没有头
							m_buff.erase(m_buff.begin(), m_buff.begin() + posHead + 2);
						else
							m_buff.clear();
					}//else
			    }//while
			}
#else
			int nsocket = m_socket;
			if(INVALID_SOCKET == nsocket)
			{
				return;
			}
			if( FD_ISSET(nsocket,&exceptfds) )
			{
				return;
			}

			else if(FD_ISSET(nsocket,&readfds))
			{
				int nRealRead = recv(m_socket, szBuffer, 8096, 0);//取头
				if (nRealRead == SOCKET_ERROR) //SOCKET读取错误
				{
					DisConnect();
				}
				else if (nRealRead == 0)  //服务器断开连接
				{
					DisConnect();
					return;
				}
				m_buff.append(szBuffer, nRealRead);
				while (true)
				{
					if (m_buff.size() < 40){
						m_buff.clear();
						return;
					}

					if (m_buff.size() <= 556)
					{//小于等于556
						int posHead = m_buff.find("\x07\xFF");
						if (posHead == 0)
						{//在头部
							short length = m_buff.at(posHead + 2) << 8 | m_buff.at(posHead + 3);
							if (length == 32)//命令
							{
								short end = m_buff.at(length + 8 - 2) << 8 | m_buff.at(length + 8 - 1);
								if (end == 0x5555){
									if (nRealRead)
										m_pCallBack->OnReceiveData((char*)&m_buff.at(0), m_buff.size());
									m_buff.erase(m_buff.begin(), m_buff.begin() + length + 8);
								}
							}
							else if (length == 548)//雷达
							{
								if (m_buff.size() == length + 8) //整帧
								{
									short end = m_buff.at(length + 8 - 2) << 8 | m_buff.at(length + 8 - 1);
									if (end == 0x5555){
										if (nRealRead)
											m_pCallBack->OnReceiveData((char*)&m_buff.at(0), m_buff.size());
										m_buff.erase(m_buff.begin(), m_buff.begin() + length + 8);
									}
								}
								else if (m_buff.size() < length + 8) //半帧
								{
									break;
								}
							}
						}
						else if (posHead>0)//没有头
							m_buff.erase(m_buff.begin(), m_buff.begin() + posHead + 2);
						else
							m_buff.clear();
					}
					else{//大于556
						int posHead = m_buff.find("\x07\xFF");
						if (posHead == 0)
						{//头在开始
							short length = m_buff.at(posHead + 2) << 8 | m_buff.at(posHead + 3);
							if (length == 548 || length == 32)//分帧数据
							{
								short end = m_buff.at(length + 8 - 2) << 8 | m_buff.at(length + 8 - 1);
								if (end == 0x5555){
									if (nRealRead)
										m_pCallBack->OnReceiveData((char*)&m_buff.at(0), length + 8);
									m_buff.erase(m_buff.begin(), m_buff.begin() + length + 8);
								}
							}//length
						}
						else if (posHead>0)//没有头
							m_buff.erase(m_buff.begin(), m_buff.begin() + posHead + 2);
						else
							m_buff.clear();
					}//else
				}//while
			}
#endif
		}
		else
		{
			DisConnect();
		}
	}
}

/**
 * @brief 端口是否打开
 *
 *
 * @return TRUE 打开
 */
bool CPortTCPClient::IsOpen()
{
	return m_bOpened;
}

/**
 * @brief 是否已连接
 *
 *
 * @return  TRUE 已连接服务器
 */
bool CPortTCPClient::IsConnect()
{
	return m_socket != INVALID_SOCKET;
}

/**
 * @brief 建立与服务器的联接
 *
 *
 * @return 无
 */
bool CPortTCPClient::Connect()
{
#ifdef _WIN32
	fd_set wset;
#else
	fd_set rset, wset, exset;
#endif
	//1建立SOCKET
#ifdef _WIN32
	SOCKET sock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
#else
	int sock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
#endif
	if (sock == INVALID_SOCKET)
	{
		return false;
	}

#ifdef _WIN32
	BOOL bOptVal = TRUE;
	setsockopt(sock,IPPROTO_TCP,TCP_NODELAY,(char *)&bOptVal,sizeof(bOptVal));
#else
	int nOptVal = 1;
	setsockopt(sock,IPPROTO_TCP,TCP_NODELAY,&nOptVal,sizeof(nOptVal));

	//连接，发送，接收时间超时设定
	struct timeval timeout={3,0};//5s
	setsockopt(sock,SOL_SOCKET,SO_SNDTIMEO,&timeout,sizeof(timeout));
	setsockopt(sock,SOL_SOCKET,SO_RCVTIMEO,&timeout,sizeof(timeout));

#endif
	//2设置参数
	sockaddr_in address;
	address.sin_family = AF_INET;
	address.sin_port = htons(m_nPort);
	address.sin_addr.s_addr = inet_addr(m_strIPAddress.c_str());//htonl(dwInetAddr);
	int nRecvBufSize = 1024 * 1024 * 10;
	setsockopt(sock, SOL_SOCKET, SO_RCVBUF, (const char *)&nRecvBufSize, sizeof(nRecvBufSize));
#ifdef _WIN32

	setBlock(sock,false);

	struct timeval tval;

	tval.tv_sec = 2;
	tval.tv_usec = 0;

	FD_ZERO(&wset);
	FD_SET(sock, &wset);

	connect(sock, (sockaddr *) & address, sizeof(address));

	int nRet = select(0, 0, &wset, 0, &tval);

	if ( nRet <= 0 )
	{
		closesocket(sock);
		return false;
	}

	setBlock(sock,true);


#else

	setBlock(sock,false);

	struct timeval tval;


	//3链接
	int nConnectRet = connect(sock, (sockaddr *) & address, sizeof(address));
	if (nConnectRet < 0)
	{
		if(errno != EINPROGRESS)
		{
			closesocket(sock);
			return false;
		}
	}
	if(nConnectRet == 0)
	{
		closesocket(sock);
		return false;
	}

	FD_ZERO(&rset);
	FD_SET(sock,&rset);
	wset=rset;
	exset = rset;
	tval.tv_sec = 3;
	tval.tv_usec = 0;
	int nSelectRet = select(sock+1,&rset,&wset,&exset,&tval);
	if(nSelectRet == 0)
	{
		closesocket(sock);
		return false;
	}
	if(FD_ISSET(sock,&exset))
	{
		closesocket(sock);
		return false;
	}
	if(FD_ISSET(sock,&rset) || FD_ISSET(sock,&wset))
	{
		int error = 0;
#ifdef  _HP_UX
        int len = sizeof(error);
#else
        socklen_t len = (socklen_t)sizeof(error);
#endif //  _HP_UX


		if(getsockopt(sock,SOL_SOCKET,SO_ERROR,&error,&len)<0)
		{
			closesocket(sock);
			return false;
		}
		else
		{
			if(error != 0)
			{
				closesocket(sock);
				return false;
			}
		}

	}
	else
	{
		closesocket(sock);
		return false;
	}

	setBlock(sock,true);
#endif
	m_socket = sock;
	return true;
}

/**
 * @brief 断开连接
 *
 * @return 无
 */
void CPortTCPClient::DisConnect()
{
	if(m_socket != INVALID_SOCKET)
	{
		shutdown(m_socket,SD_BOTH);
		closesocket(m_socket);
		m_socket = INVALID_SOCKET;
	}
	m_bOpened = false;
}

/**设置阻塞模式 */
#ifdef _WIN32
	int CPortTCPClient::setBlock(SOCKET fd, bool block)
#else
	int CPortTCPClient::setBlock(int fd , bool block)
#endif
{
	if(block)
	{
#ifdef _WIN32
		unsigned long arg = 0;
		if(ioctlsocket(fd, FIONBIO, &arg) == SOCKET_ERROR)
		{
			return -1;
		}
#else
		int flags = fcntl(fd, F_GETFL);
		flags &= ~O_NONBLOCK;
		if(fcntl(fd, F_SETFL, flags) == SOCKET_ERROR)
		{
			return -1;
		}
#endif
	}
	else
	{
#ifdef _WIN32
		unsigned long arg = 1;
		if(ioctlsocket(fd, FIONBIO, &arg) == SOCKET_ERROR)
		{
			return -1;
		}
#else
		int flags = fcntl(fd, F_GETFL);
		flags |= O_NONBLOCK;
		if(fcntl(fd, F_SETFL, flags) == SOCKET_ERROR)
		{
			return -1;
		}
#endif
	}

	return 0;
}

/** @brief 设置回调 */
void CPortTCPClient::SetCallBack(IPortCallBack *pCallBack)
{
	m_pCallBack = pCallBack;
}