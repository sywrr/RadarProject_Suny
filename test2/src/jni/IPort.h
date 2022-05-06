
/****************************************************************************************
*
* @file    IPort.h
* @version 1.0
* @date    2021-04-30
* @author  ������
*
* @brief   ��������
*
***************************************************************************************/
// ��Ȩ(C) 2009 - 2021 �粨�����о���
// �Ķ���ʷ
// ����         ����     �Ķ�����
// 2021-04-30   ������   �����ļ�
//==============================================================================

#ifndef __Daq_IPort_H__
#define __Daq_IPort_H__

#include "IPortCallBack.h"

typedef unsigned char BYTE;

namespace DAQ
{
	class IPort  
	{
	public:
		IPort();
		virtual ~IPort();

	public:	
		//�˿ڴ�״̬
		virtual bool IsOpen()=0;
		//��·����״̬
		virtual bool IsConnect()=0;
		//�򿪶˿�
		virtual bool OpenPort()=0;
		//�رն˿�
		virtual bool ClosePort()=0;
		//��������
		virtual bool SendData(void *pDataBuffer,int nBufferLength)=0;
		//���ûص�
		virtual void SetCallBack(IPortCallBack *pCallBack)=0;
	};
}

#endif /* __Daq_IPort_H__ */
