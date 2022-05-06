/*********************************************************************************
 *
 * @file	DllFile.h
 * @version	1.0
 * @date	2020-10-21
 * @author	������
 *
 * @brief	
 *
 *********************************************************************************/
 // ��Ȩ(C) �粨�����о���
 // �Ķ���ʷ
 // ����			����	�Ķ�����
 // 2020-10-21	������	�����ļ�
 //================================================================================

#ifndef _DllFile_H_
#define _DllFile_H_

#ifdef _WIN32
	#include <windows.h>
#else
	#include <dlfcn.h>
#endif

/**
* @brief �ɼ�ϵͳ�����ռ�
*/
namespace DAQ
{
	/** 
	 *
	 * @brief dll���������������,������Ϊ����,Ҳ������Ϊ��Աʹ��
	 *
	 * ��Ҫ���ܰ��� ��̬��ļ��أ�ж�أ��Ӷ�̬�����ҵ���Ӧ��������ڡ�
	 */
	class  CDllFile  
	{
	private:
#ifdef _WIN32
		HMODULE m_hDll;			//��̬����
#else
		void *m_hDll;
#endif	
	public:
		/**
		 * @brief ���캯��
		 */
		CDllFile();
		virtual ~CDllFile();
		
	public:
		/**
		 * @brief ��dll���ҵ���Ӧ���������
		 *
		 * @param pFunctionName  ��������
		 *
		 * @return ����ָ�룬�����������ΪNULL
		 */
		virtual void * FindFunction(const char * pFunctionName) const;

		/**
		 * @brief װ�ض�̬��,ȡ��DLL���
		 *
		 * @param *pFileName  ��̬���ļ�����
		 *
		 * @return true װ�سɹ�  false װ��ʧ��
		 */
		virtual bool LoadDll(const char *pFileName);

		/**
		 * @brief ж�ض�̬��,���ڴ���ȥ��dll��
		 *
		 * @return ��
		 */
		virtual void UnLoadDll();
	};
}; //DAQ

#endif //_DllFile_H_

