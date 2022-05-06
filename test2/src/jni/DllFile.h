/*********************************************************************************
 *
 * @file	DllFile.h
 * @version	1.0
 * @date	2020-10-21
 * @author	孙永民
 *
 * @brief	
 *
 *********************************************************************************/
 // 版权(C) 电波传播研究所
 // 改动历史
 // 日期			作者	改动内容
 // 2020-10-21	孙永民	创建文件
 //================================================================================

#ifndef _DllFile_H_
#define _DllFile_H_

#ifdef _WIN32
	#include <windows.h>
#else
	#include <dlfcn.h>
#endif

/**
* @brief 采集系统命名空间
*/
namespace DAQ
{
	/** 
	 *
	 * @brief dll函数管理类的声明,可以做为基类,也可以做为成员使用
	 *
	 * 主要功能包括 动态库的加载，卸载，从动态库中找到相应函数的入口。
	 */
	class  CDllFile  
	{
	private:
#ifdef _WIN32
		HMODULE m_hDll;			//动态库句柄
#else
		void *m_hDll;
#endif	
	public:
		/**
		 * @brief 构造函数
		 */
		CDllFile();
		virtual ~CDllFile();
		
	public:
		/**
		 * @brief 从dll中找到相应函数的入口
		 *
		 * @param pFunctionName  函数名称
		 *
		 * @return 函数指针，如果不存在则为NULL
		 */
		virtual void * FindFunction(const char * pFunctionName) const;

		/**
		 * @brief 装载动态库,取得DLL句柄
		 *
		 * @param *pFileName  动态库文件名称
		 *
		 * @return true 装载成功  false 装载失败
		 */
		virtual bool LoadDll(const char *pFileName);

		/**
		 * @brief 卸载动态库,从内存中去除dll库
		 *
		 * @return 无
		 */
		virtual void UnLoadDll();
	};
}; //DAQ

#endif //_DllFile_H_

