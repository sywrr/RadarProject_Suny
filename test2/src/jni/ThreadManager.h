
/****************************************************************************************
*
* @file    ThreadManager.h
* @version 1.0
* @date    2021-04-30
* @author  ������
*
* @brief   �̹߳����������
*
***************************************************************************************/
// ��Ȩ(C) 2009 - 2021 �粨�����о���
// �Ķ���ʷ
// ����         ����     �Ķ�����
// 2021-04-30   ������   �����ļ�
//==============================================================================

#ifndef _ThreadManager_H_
#define _ThreadManager_H_

#ifdef _WIN32
	#include <windows.h>
	#include <process.h>
#else
	#include <unistd.h>
#define Sleep usleep
#endif

#include <string>

using namespace std;

namespace DAQ
{
	class CThread;

	/** 
	 * @class  "CThread"  
	 *
	 * @brief �̹߳�����
	 *
	 * �̹߳����࣬���Զ����߳̽��й���
	 */
	class CThreadManager  
	{
	private:
		/** @brief �Զ����߳� */
		CThread *m_pThread;
		//����ͨ��
		int m_nChannelId;
		//�߳�����
		string m_strName;
		//�̺߳�������ʱ����
		int m_nRunInterval;

	public:
		CThreadManager();
		virtual ~CThreadManager();

	public:
		/**
		 * @brief �̴߳���
		 *
		 * @param pThis ����ָ��
		 * @param pMethod ����ָ��
		 * @param strName �߳�����
		 *
		 * @return ��
		 */
		void Start(void *pThis,void *pMethod,const char * strName);
		/**
		 * @brief ֹͣ
		 *
		 * @return ��
		 */
		void Stop();

		/**
		 * @brief �����̺߳������м��ʱ�䣬��λms
		 * @param nRunInterval 
		 * @return 
		 */
		void SetRunInterval(const int nRunInterval);
	};

	/** 
	 * @class  "CThread"  
	 *
	 * @brief �Զ����߳���
	 *
	 * �Զ����߳��࣬��windows�߳̽����˷�װ
	 */
	class CThread 
	{
		
	public:
		CThread();          
		virtual ~CThread();
	private:
		string m_strThreadName;			//�߳�����,������
		volatile bool m_bRun ;			//�Ƿ�����
		volatile bool m_bStop;			//�Ƿ�ֹͣ

		void *m_pMethod;                //����ָ��
		void *m_pThis;			        //����ָ��
#ifdef _WIN32
		HANDLE m_hThread;				//�߳̾��
#else
		pthread_t m_hThread;			//�߳�ID
#endif
		//����ͨ��
		int m_nChannelId;

		//�̺߳�������ʱ����
		int m_nRunInterval;
	public:
		/**
		 * @brief �̴߳���
		 *
		 * @param pThis ����ָ��
		 * @param pMethod ����ָ��
		 * @param strName �߳�����
		 *
		 * @return ��
		 */
		int  Start(void *pThis,void *pMethod,const char  *strName);

		/**
		 * @brief �߳�ֹͣ
		 *
		 *
		 * @return 
		 */
		void Stop();
		
		/**
		 * @brief �߳�����
		 *
		 *
		 * @return 
		 */
		int Run();

		/**
		 * @brief �����̺߳������м��ʱ�䣬��λms
		 * @param nRunInterval 
		 * @return 
		 */
		void SetRunInterval(const int nRunInterval);
	};//CThread
}//DAQ
#endif//__ThreadManager_H__
