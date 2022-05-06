/*********************************************************************************
 *
 * @file	PortTCPClient.h
 * @version	1.0
 * @date	2021-9-19
 * @author	������
 *
 * @brief	
 *
 *********************************************************************************/
 // ��Ȩ(C) 2009 - 2021 �粨�����о���
 // �Ķ���ʷ
 // ����			����	�Ķ�����
 // 2021-9-19	������	�����ļ�
 //================================================================================

#ifndef __Daq_PortTCPClient_H__
#define __Daq_PortTCPClient_H__

#include "IPort.h"
#include "ThreadManager.h"

//����TCP CLient �˿�
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
		/** ��������ַ */
		string m_strIPAddress;
		/** �������˿� */
		int m_nPort;
		/** ���ӵ�SOCKET��� */
#ifdef _WIN32
		SOCKET m_socket;
#else
		int m_socket;
#endif
		/** �������߳� */
		CThreadManager m_threadRead;

	public:
		/** ���캯�� */
		CPortTCPClient(const string &strIP,int nPort);
		/** �������� */
		virtual ~CPortTCPClient();

	public:

		/**
		 * @brief  �˿��Ƿ��Ѵ�
		 *
		 * @return true �Ѵ� false δ��
		 */
		virtual bool IsOpen();

		/**
		 * @brief �˿��Ƿ�������
		 *
		 * @return  true ������ false δ����
		 */
		virtual bool IsConnect();

		/**
		 * @brief �򿪶˿�
		 *	��ջ�����
		 *  �����������߳�
		 *
		 * @return 
		 */
		virtual bool OpenPort();

		/**
		 * @brief �رն˿�
		 *
		 *
		 * @return 
		 */
		virtual bool ClosePort();

		/**
		 * @brief ��������
		 *
		 * @param pDataBuffer ���ݻ�����
		 * @param nBufferLength  ���ݳ���
		 *
		 * @return  true �ɹ� false ʧ��
		 */
		virtual bool SendData(void *pDataBuffer,int nBufferLength);

	public:

		/**
		 * @brief  �Ͽ�����
		 *
		 * @return 
		 */
		virtual void DisConnect();

		/**
		 * @brief ��������
		 *
		 * @return true �����ɹ� false ����ʧ��
		 */
		virtual bool Connect();

		/**
		 * @brief  ��ȡ����
		 *
		 * @return 
		 */
		virtual void Read();

		/**
		 * @brief	��ȡ�����̵߳��ú���
		 *
		 * @param pThis 
		 *
		 * @return 
		 */
		static void ReadThread(void *pThis);

		/**
		 * @brief	��������ͨѶ����ģʽ
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

		/** �˿ڴ򿪱�־  */
		    bool m_bOpened;
			/** �ص�ָ�� */
			IPortCallBack *m_pCallBack;
	};
}
#endif //__Daq_PortTCPClient_H__
