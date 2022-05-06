/*********************************************************************************
 *
 * @file	PortTCPClient.h
 * @version	1.0
 * @date	2021-9-19
 * @author	孙永民
 *
 * @brief	
 *
 *********************************************************************************/
 // 版权(C) 2009 - 2021 电波传播研究所
 // 改动历史
 // 日期			作者	改动内容
 // 2021-9-19	孙永民	创建文件
 //================================================================================

#ifndef __Daq_PortTCPClient_H__
#define __Daq_PortTCPClient_H__

#include "IPort.h"
#include "ThreadManager.h"

//定义TCP CLient 端口
#ifdef _WIN32
	#include "winsock2.h"
	#pragma comment(lib, "ws2_32.lib")
#else
	#include <sys/socket.h>
	#include <arpa/inet.h>
	#include <netinet/tcp.h>
    #include <unistd.h>
	#define Sleep usleep
	#define closesocket	close
	#define SD_BOTH	SHUT_RDWR
#ifndef INVALID_SOCKET
	#define INVALID_SOCKET (int)(~0)
#endif
#ifndef SOCKET_ERROR
    #define SOCKET_ERROR (int)(-1)
#endif
#endif

namespace DAQ
{
	class CPortTCPClient : public IPort
	{
	private:
		/** 服务器地址 */
		string m_strIPAddress;
		/** 服务器端口 */
		int m_nPort;
		/** 连接的SOCKET句柄 */
#ifdef _WIN32
		SOCKET m_socket;
#else
		int m_socket;
#endif
		/** 读数据线程 */
		CThreadManager m_threadRead;

	public:
		/** 构造函数 */
		CPortTCPClient(const string &strIP,int nPort);
		/** 析构函数 */
		virtual ~CPortTCPClient();

	public:

		/**
		 * @brief  端口是否已打开
		 *
		 * @return true 已打开 false 未打开
		 */
		virtual bool IsOpen();

		/**
		 * @brief 端口是否已连接
		 *
		 * @return  true 已连接 false 未连接
		 */
		virtual bool IsConnect();

		/**
		 * @brief 打开端口
		 *	清空缓冲区
		 *  创建读数据线程
		 *
		 * @return 
		 */
		virtual bool OpenPort();

		/**
		 * @brief 关闭端口
		 *
		 *
		 * @return 
		 */
		virtual bool ClosePort();

		/**
		 * @brief 发送数据
		 *
		 * @param pDataBuffer 数据缓冲区
		 * @param nBufferLength  数据长度
		 *
		 * @return  true 成功 false 失败
		 */
		virtual bool SendData(void *pDataBuffer,int nBufferLength);

	public:

		/**
		 * @brief  断开连接
		 *
		 * @return 
		 */
		virtual void DisConnect();

		/**
		 * @brief 建立连接
		 *
		 * @return true 创建成功 false 创建失败
		 */
		virtual bool Connect();

		/**
		 * @brief  读取数据
		 *
		 * @return 
		 */
		virtual void Read();

		/**
		 * @brief	读取数据线程调用函数
		 *
		 * @param pThis 
		 *
		 * @return 
		 */
		static void ReadThread(void *pThis);

		/**
		 * @brief	设置网络通讯阻塞模式
		 *
		 * @param pThis 
		 *
		 * @return 
		 */
#ifdef _WIN32
		int setBlock(SOCKET fd, bool block);
#else
		int setBlock(int fd , bool block);
#endif
		void SetCallBack(IPortCallBack *pCallBack);

	private:

		/** 端口打开标志  */
		    bool m_bOpened;
			/** 回调指针 */
			IPortCallBack *m_pCallBack;
	};
}
#endif //__Daq_PortTCPClient_H__
