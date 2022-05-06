
/****************************************************************************************
*
* @file    ThreadManager.h
* @version 1.0
* @date    2021-04-30
* @author  孙永民
*
* @brief   线程管理类的声明
*
***************************************************************************************/
// 版权(C) 2009 - 2021 电波传播研究所
// 改动历史
// 日期         作者     改动内容
// 2021-04-30   孙永民   创建文件
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
	 * @brief 线程管理类
	 *
	 * 线程管理类，对自定义线程进行管理
	 */
	class CThreadManager  
	{
	private:
		/** @brief 自定义线程 */
		CThread *m_pThread;
		//所属通道
		int m_nChannelId;
		//线程名称
		string m_strName;
		//线程函数运行时间间隔
		int m_nRunInterval;

	public:
		CThreadManager();
		virtual ~CThreadManager();

	public:
		/**
		 * @brief 线程创建
		 *
		 * @param pThis 对象指针
		 * @param pMethod 方法指针
		 * @param strName 线程名称
		 *
		 * @return 无
		 */
		void Start(void *pThis,void *pMethod,const char * strName);
		/**
		 * @brief 停止
		 *
		 * @return 无
		 */
		void Stop();

		/**
		 * @brief 设置线程函数运行间隔时间，单位ms
		 * @param nRunInterval 
		 * @return 
		 */
		void SetRunInterval(const int nRunInterval);
	};

	/** 
	 * @class  "CThread"  
	 *
	 * @brief 自定义线程类
	 *
	 * 自定义线程类，对windows线程进行了封装
	 */
	class CThread 
	{
		
	public:
		CThread();          
		virtual ~CThread();
	private:
		string m_strThreadName;			//线程名称,调试用
		volatile bool m_bRun ;			//是否运行
		volatile bool m_bStop;			//是否停止

		void *m_pMethod;                //方法指针
		void *m_pThis;			        //对象指针
#ifdef _WIN32
		HANDLE m_hThread;				//线程句柄
#else
		pthread_t m_hThread;			//线程ID
#endif
		//所属通道
		int m_nChannelId;

		//线程函数运行时间间隔
		int m_nRunInterval;
	public:
		/**
		 * @brief 线程创建
		 *
		 * @param pThis 对象指针
		 * @param pMethod 方法指针
		 * @param strName 线程名称
		 *
		 * @return 无
		 */
		int  Start(void *pThis,void *pMethod,const char  *strName);

		/**
		 * @brief 线程停止
		 *
		 *
		 * @return 
		 */
		void Stop();
		
		/**
		 * @brief 线程运行
		 *
		 *
		 * @return 
		 */
		int Run();

		/**
		 * @brief 设置线程函数运行间隔时间，单位ms
		 * @param nRunInterval 
		 * @return 
		 */
		void SetRunInterval(const int nRunInterval);
	};//CThread
}//DAQ
#endif//__ThreadManager_H__
