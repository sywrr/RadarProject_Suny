
/****************************************************************************************
*
* @file    IPortCallBack.h
* @version 1.0
* @date    2021-04-30
* @author  ������
*
* @brief   �ص�����ͷ�ļ�
*
***************************************************************************************/
// ��Ȩ(C) 2009 - 2021 �粨�����о���
// �Ķ���ʷ
// ����         ����     �Ķ�����
// 2021-04-30   ������   �����ļ�
//==============================================================================

#ifndef __IPortCallBack_H__
#define __IPortCallBack_H__

namespace DAQ
{
	class IPortCallBack
	{
	public:
		virtual void OnReceiveData(char *szBuffer, int nRealReads) = 0;
	public:
		bool m_isLinked;
	};
}

#endif