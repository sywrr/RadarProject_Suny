
/****************************************************************************************
*
* @file    IPort.h
* @version 1.0
* @date    2021-04-30
* @author  孙永民
*
* @brief   驱动基类
*
***************************************************************************************/
// 版权(C) 2009 - 2021 电波传播研究所
// 改动历史
// 日期         作者     改动内容
// 2021-04-30   孙永民   创建文件
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
		//端口打开状态
		virtual bool IsOpen()=0;
		//链路连接状态
		virtual bool IsConnect()=0;
		//打开端口
		virtual bool OpenPort()=0;
		//关闭端口
		virtual bool ClosePort()=0;
		//发送数据
		virtual bool SendData(void *pDataBuffer,int nBufferLength)=0;
		//设置回调
		virtual void SetCallBack(IPortCallBack *pCallBack)=0;
	};
}

#endif /* __Daq_IPort_H__ */
