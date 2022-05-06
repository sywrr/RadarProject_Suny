
#include "ThreadManager.h"
#include <time.h>
#ifndef _WIN32
	#include <pthread.h>
	#include <errno.h>
#endif

namespace DAQ
{
	typedef void (THREAD_CALL)(void *pThis);
	CThread::CThread()
	{
		m_bRun = false;
		m_bStop = false;
		m_pMethod = NULL;
		m_pThis = NULL;
		m_nChannelId = 0;
		m_nRunInterval = 1;
#ifdef _WIN32
		m_hThread = NULL;				//线程句柄
#else
		m_hThread = 0;				//线程句柄
#endif
	}

	CThread::~CThread()
	{
		Stop();
	}

	// CThread message handlers
	/** @brief 停止线程 */
	void CThread::Stop()
	{
		m_bStop = true;
		if(!m_bRun)//已经停止
		{
			return;
		}
		m_bRun = false;
#ifdef _WIN32
		while(ResumeThread(m_hThread)+1>1);//线程当前可能是挂起的
		DWORD nExitCode;
		if(GetExitCodeThread(m_hThread, &nExitCode) 
			&& (nExitCode == STILL_ACTIVE)
			&& (WaitForSingleObject (m_hThread, 60000 ) == WAIT_TIMEOUT))//!=WAIT_OBJECT_0)
		{
			TerminateThread(m_hThread,1);
		}	
		else
		{
		}
		CloseHandle(m_hThread);
#else
		if(0 == pthread_join(m_hThread,NULL) )
		{
		}
		else
		{
		}
#endif
	}

#ifdef _WIN32
	unsigned __stdcall RunFunc(void *pThread)
	{
		return(((CThread *)pThread)->Run());
	}
#else
	void *RunFunc(void *pThread)
	{
		((CThread *)pThread)->Run();
		return NULL;
	}
#endif

	/**
	 * @brief 线程的主调用过程
	 *
	 * @return 无
	 */
	int CThread::Run()
	{
		m_bRun = true;

		while (true)
		{
			if (!m_bRun || m_bStop)
			{
				break;
			}

			try
			{
				((THREAD_CALL*)m_pMethod)(m_pThis);
			}
			catch (...)
			{
			}
#ifdef _WIN32
			DWORD dwWaitReturn = WaitForSingleObject(m_hThread, m_nRunInterval);
			switch (dwWaitReturn)
			{
			case WAIT_OBJECT_0:		//thread stop event
				//m_ThreadLock.Lock();
				//m_bRun = false;//退出
				//m_ThreadLock.Unlock();
				break;
			case WAIT_TIMEOUT://超时,正常现象
				break;
			default:
				//m_ThreadLock.Lock();
				//m_bRun = false;//退出
				//m_ThreadLock.Unlock();
				break;
			}
		}
#else
			Sleep(m_nRunInterval*1000);
	    }
#endif
		m_bRun = false;//退出
		return 0;
	}
	
	/**
	 * @brief 激活线程
	 *
	 * @param pThis 
	 * @param pMethod 
	 * @param strName 
	 *
	 * @return 创建的线程号
	 */
	int  CThread::Start(void *pThis,void *pMethod,const char *strName)
	{
		
		if ((pMethod == NULL) || (pThis == NULL))
		{
			return -1;
		}
		m_bStop = false;
		m_pMethod = pMethod;
		m_pThis = pThis;
		m_strThreadName = strName;
		unsigned int nThreadID = 0;
#ifdef _WIN32
		m_hThread = (HANDLE)_beginthreadex(NULL, 0, &RunFunc, (void *)this, 0, &nThreadID);
		if (m_hThread == NULL)
		{
			return -1;
		}
#else
		int nError ;
		nError = pthread_create(&m_hThread,NULL,RunFunc,(void *)this);
		
		if (nError != 0)
		{
			return -1;
		}
		nThreadID = m_hThread;
#endif
		//创建成功，线程就开始运行
		m_bRun = true;
		return (int)m_hThread;	
	}

	void CThread::SetRunInterval( const int nRunInterval )
	{
		if (nRunInterval >= 0)
		{
			m_nRunInterval = nRunInterval;
		}
	}
	/** @brief 构造函数 */
	CThreadManager::CThreadManager()
	{
		m_pThread = NULL;
		m_nChannelId = 0;
		m_nRunInterval = 1;	//线程函数运行间隔时间
	}
	
	/** @brief 析构函数 */
	CThreadManager::~CThreadManager()
	{
		Stop();	
	}


	/**
	 * @brief 开始线程
	 *
	 * @param pThis 对象体
	 * @param pMethod 对象方法
	 * @param strName 线程名称
	 *
	 * @return 无
	 */
	void CThreadManager::Start(void *pThis,void *pMethod,const char * strName)
	{
		m_strName = strName;
		if ((pMethod == NULL) || (pThis == NULL))
		{
			return;
		}

		Stop();

		m_pThread = new CThread();
		m_pThread->SetRunInterval(m_nRunInterval);
		int nThread = m_pThread->Start(pThis,pMethod,strName);
	}
	
	/** @brief 中止线程的执行 */
	void CThreadManager::Stop()
	{
		if(m_pThread != NULL)
		{	
			m_pThread->Stop(); 
			delete m_pThread;
			m_pThread = NULL;
		}
	}

	/**
	 * @brief 设置线程函数运行间隔时间，单位ms*/
	void CThreadManager::SetRunInterval( const int nRunInterval )
	{
		m_nRunInterval = nRunInterval;
	}
}
