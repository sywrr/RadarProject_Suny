#include "IPort.h"

namespace DAQ
{
	IPort::IPort()
	{

	}

	IPort::~IPort()
	{

	}

	bool IPort::IsOpen()
	{
		return false;
	}

	//��·����״̬
	bool IPort::IsConnect()
	{
		return false;
	}

	//�򿪶˿�
	bool IPort::OpenPort()
	{
		return false;
	}

	//�رն˿�
	bool IPort::ClosePort()
	{
		return false;
	}

	//��������
	bool IPort::SendData(void *pDataBuffer, int nBufferLength)
	{
		return false;
	}

	//���ûص�
	void IPort::SetCallBack(IPortCallBack *pCallBack)
	{
	
	}
}

