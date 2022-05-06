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

	//链路连接状态
	bool IPort::IsConnect()
	{
		return false;
	}

	//打开端口
	bool IPort::OpenPort()
	{
		return false;
	}

	//关闭端口
	bool IPort::ClosePort()
	{
		return false;
	}

	//发送数据
	bool IPort::SendData(void *pDataBuffer, int nBufferLength)
	{
		return false;
	}

	//设置回调
	void IPort::SetCallBack(IPortCallBack *pCallBack)
	{
	
	}
}

