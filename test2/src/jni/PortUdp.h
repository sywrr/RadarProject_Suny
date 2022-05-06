
/****************************************************************************************
*
* @file    PortUdp.h
* @version 1.0
* @date    2021-04-30
* @author  ������
*
* @brief   �������Ͷ���ͷ�ļ�
*
***************************************************************************************/
// ��Ȩ(C) 2009 - 2021 �粨�����о���
// �Ķ���ʷ
// ����         ����     �Ķ�����
// 2021-04-30   ������   �����ļ�
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
	* @brief  UDPͨ����
	*
	* ����UDP�ķ��ͺͽ������ӣ����պͷ�������
	*/
	class 	CPortUDP :public IPort
	{
	private:
		/** ���ܶ˿ں� */
		int m_nReceivePort;

		/** ����IP��ַ */
		string m_strTargetIp;
		/** ���Ͷ˿ں� */
		int m_nSendPort;

#ifdef _WIN32
		/** ����SOCKET��� */
		SOCKET m_socketSend;
		/** ����SOCKET��� */
		SOCKET m_socketReceive;
#else
		/** ����SOCKET��� */
		int m_socketSend;
		/** ����SOCKET��� */
		int m_socketReceive;
#endif
		/** ���ݶ�ȡ�߳� */
		CThreadManager m_threadRead;
		/** �ص�ָ�� */
		IPortCallBack *m_pCallBack;
	private:

		/** �˿ڴ򿪱�־  */
		bool m_bOpened;	

	public:

		/**
		* @brief UDP�๹�캯��
		*
		* @param strTargetIp	����IP��ַ
		* @param nSendPort		���Ͷ˿ں�
		* @param nReceivePort	���ն˿ں�
		*
		* @return ��
		*/
		CPortUDP(const string &strTargetIp,int nSendPort,int nReceivePort);

		/**
		* @brief ��������
		*
		* @return 
		*/
		virtual ~CPortUDP();

		/**
		* @brief ȡ�ô�״̬
		*
		* @return TRUE �Ѵ� FALSE δ��
		*/
		virtual bool IsOpen();

		/**
		* @brief ȡ������״̬
		*
		* @return  TRUE ������ FALSE δ����
		*/
		virtual bool IsConnect();

		/**
		* @brief �򿪶˿�
		* ��ջ���������ʼ���˿ڲ������򿪶˿�
		*
		* @return TRUE �򿪶˿ڳɹ� FALSE �򿪶˿�ʧ��
		*/
		virtual bool OpenPort();

		/**
		* @brief �رն˿�
		*
		* @return TRUE �رն˿ڳɹ� FALSE �رն˿�ʧ��
		*/
		virtual bool ClosePort();

		/**
		* @brief ��������
		*
		* @param pDataBuffer ���ݻ�������ַ
		* @param nBufferLength ���ݻ���������
		*
		* @return TRUE ���ͳɹ� FALSE ����ʧ��
		*/
		virtual bool SendData(void *pDataBuffer,int nBufferLength);	

		/**
		* @brief ���ն˿��Ƿ��
		*
		* @return TRUE �Ѵ� FALSE δ��
		*/
		bool IsReceiveOpen();

		/**
		* @brief ���Ͷ˿��Ƿ��
		*
		* @return TRUE �Ѵ� FALSE δ��
		*/
		bool IsSendOpen();

		/**
		* @brief �򿪽�������
		*
		* @return 
		*/
		bool ConnectReceive();

		/**
		* @brief �򿪷�������
		*
		* @return 
		*/
		bool ConnectSend();

		/**
		* @brief �رս�������
		*
		* @return 
		*/
		void DisConnectReceive();

		/**
		* @brief �رշ�������
		*
		* @return 
		*/
		void DisConnectSend();

		/**
		* @brief ��ȡ����
		*
		* @return 
		*/
		void Read();

		/**
		* @brief ������ȡ�߳�
		*
		* @param pThis 
		*
		* @return 
		*/
		static void ReadThread(void *pThis);//���̵߳���

		/**
		* @brief �������ݽ��ջص�
		*
		* @return
		*/
		void SetCallBack(IPortCallBack *pCallBack);
	};
};
#endif //PortUDP
