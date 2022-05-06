
/****************************************************************************************
*
* @file    PortUdp.h
* @version 1.0
* @date    2021-04-30
* @author  孙永民
*
* @brief   公共类型定义头文件
*
***************************************************************************************/
// 版权(C) 2009 - 2021 电波传播研究所
// 改动历史
// 日期         作者     改动内容
// 2021-04-30   孙永民   创建文件
//==============================================================================

#ifndef _PortUDP_H_
#define _PortUDP_H_

#include "iostream"
#include "ThreadManager.h"
#include "IPortCallBack.h"
#include "IPort.h"

using namespace std;
#ifdef _WIN32
	#include "winsock2.h"
	#pragma comment(lib, "ws2_32.lib")
#else
	#include <sys/socket.h>
	#include <arpa/inet.h>
    #include <unistd.h>
    #define Sleep usleep
	#define closesocket	close
	#define SD_BOTH	SHUT_RDWR
	#define SOCKET int
    #define BOOL int
    #define TRUE 1
#ifndef INVALID_SOCKET
#define INVALID_SOCKET (int)(~0)
#endif
#ifndef SOCKET_ERROR
#define SOCKET_ERROR (int)(-1)
#endif
#endif

namespace DAQ
{
	/** 
	* @class  CPortUDP 
	*
	* @brief  UDP通信类
	*
	* 建立UDP的发送和接收连接，接收和发送数据
	*/
	class 	CPortUDP :public IPort
	{
	private:
		/** 接受端口号 */
		int m_nReceivePort;

		/** 发送IP地址 */
		string m_strTargetIp;
		/** 发送端口号 */
		int m_nSendPort;

#ifdef _WIN32
		/** 发送SOCKET句柄 */
		SOCKET m_socketSend;
		/** 接受SOCKET句柄 */
		SOCKET m_socketReceive;
#else
		/** 发送SOCKET句柄 */
		int m_socketSend;
		/** 接受SOCKET句柄 */
		int m_socketReceive;
#endif
		/** 数据读取线程 */
		CThreadManager m_threadRead;
		/** 回调指针 */
		IPortCallBack *m_pCallBack;
	private:

		/** 端口打开标志  */
		bool m_bOpened;	

	public:

		/**
		* @brief UDP类构造函数
		*
		* @param strTargetIp	发送IP地址
		* @param nSendPort		发送端口号
		* @param nReceivePort	接收端口号
		*
		* @return 无
		*/
		CPortUDP(const string &strTargetIp,int nSendPort,int nReceivePort);

		/**
		* @brief 析构函数
		*
		* @return 
		*/
		virtual ~CPortUDP();

		/**
		* @brief 取得打开状态
		*
		* @return TRUE 已打开 FALSE 未打开
		*/
		virtual bool IsOpen();

		/**
		* @brief 取得连接状态
		*
		* @return  TRUE 已连接 FALSE 未连接
		*/
		virtual bool IsConnect();

		/**
		* @brief 打开端口
		* 清空缓冲区，初始化端口参数，打开端口
		*
		* @return TRUE 打开端口成功 FALSE 打开端口失败
		*/
		virtual bool OpenPort();

		/**
		* @brief 关闭端口
		*
		* @return TRUE 关闭端口成功 FALSE 关闭端口失败
		*/
		virtual bool ClosePort();

		/**
		* @brief 发送数据
		*
		* @param pDataBuffer 数据缓冲区地址
		* @param nBufferLength 数据缓冲区长度
		*
		* @return TRUE 发送成功 FALSE 发送失败
		*/
		virtual bool SendData(void *pDataBuffer,int nBufferLength);	

		/**
		* @brief 接收端口是否打开
		*
		* @return TRUE 已打开 FALSE 未打开
		*/
		bool IsReceiveOpen();

		/**
		* @brief 发送端口是否打开
		*
		* @return TRUE 已打开 FALSE 未打开
		*/
		bool IsSendOpen();

		/**
		* @brief 打开接收连接
		*
		* @return 
		*/
		bool ConnectReceive();

		/**
		* @brief 打开发送连接
		*
		* @return 
		*/
		bool ConnectSend();

		/**
		* @brief 关闭接受连接
		*
		* @return 
		*/
		void DisConnectReceive();

		/**
		* @brief 关闭发送连接
		*
		* @return 
		*/
		void DisConnectSend();

		/**
		* @brief 读取数据
		*
		* @return 
		*/
		void Read();

		/**
		* @brief 创建读取线程
		*
		* @param pThis 
		*
		* @return 
		*/
		static void ReadThread(void *pThis);//读线程调用

		/**
		* @brief 设置数据接收回调
		*
		* @return
		*/
		void SetCallBack(IPortCallBack *pCallBack);
	};
};
#endif //PortUDP
