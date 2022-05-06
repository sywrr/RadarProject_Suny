/*********************************************************************************
 *
 * @file	DllFile.cpp
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

#include "DllFile.h"

using namespace DAQ;

/**
 * @brief ���캯��
 */
CDllFile::CDllFile()
	:m_hDll(NULL)
{
}

/**
 * @brief ����������ж�ض�̬��
 */
CDllFile::~CDllFile()
{
	UnLoadDll();
}

/**
 * @brief װ�ض�̬��,ȡ��DLL���
 */
bool CDllFile::LoadDll( const char *pFileName)
{

	//HI_ASSERT(pFileName != NULL);

	UnLoadDll();
#ifdef _WIN32
	m_hDll = ::LoadLibrary((LPCTSTR)pFileName);
#else

	int flags = RTLD_NOW | RTLD_GLOBAL;
#ifdef _AIX
	flags |= RTLD_MEMBER;
#endif
	m_hDll = dlopen(pFileName,flags );
	if (m_hDll == NULL)
	{
//		printf("dlopen err:%s.\n", dlerror());
	}
#endif
	return m_hDll != NULL;
}

/**
 * @brief ж�ض�̬��,���ڴ���ȥ��dll��
 *
 */
void CDllFile::UnLoadDll()
{
	
	if(m_hDll == NULL)
	{
		return;
	}
#ifdef _WIN32
	::FreeLibrary(m_hDll);
#else
	dlclose(m_hDll);
#endif
	

	m_hDll = NULL;
}

/**
* @brief ��dll���ҵ���Ӧ���������
*/
void * CDllFile::FindFunction( const char * pFunctionName) const
{
	if(pFunctionName == NULL)
		return NULL;

	if(m_hDll == NULL)
		return NULL;
#ifdef _WIN32
	return ::GetProcAddress(m_hDll,pFunctionName);
#else
	return dlsym(m_hDll,pFunctionName);
#endif
	
}
