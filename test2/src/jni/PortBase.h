
#ifndef __Daq_PortBase_H__
#define __Daq_PortBase_H__

namespace DAQ
{
	class CPortBase
	{
		CPortBase();
		~CPortBase();

	};
	class DAQ_EXT_CLASS IPort
	{
	public:
		virtual ~IPort(){};

	public:
		//端口打开状态
		virtual bool IsOpen() = 0;
		//链路连接状态
		virtual bool IsConnect() = 0;
		//清空数据缓冲区
		virtual void ClearBuffer() = 0;
		//打开端口
		virtual bool OpenPort() = 0;
		//关闭端口
		virtual bool ClosePort() = 0;
		//发送数据
		virtual bool SendData(void *pDataBuffer, int nBufferLength) = 0;

		virtual bool SendData(void *pDataBuffer, int nBufferLength, int nClientId) = 0;
		//接收数据
		virtual int  ReceiveData(BYTE *pBuffer, int nMaxLength, int nTimeOut) = 0;

		virtual int ReceiveData(BufferReceive &stBuffer, int nTimeOut) = 0;
		//设置回调
		virtual void SetCallBack(IPortCallBack *pCallBack) = 0;

		/**
		* @brief 设置探测器
		*
		* @param *pMonitor 探测器指针
		*
		* @return 无
		*/
		virtual void SetLogger(int channelId, ILogger *pLogger) = 0;

		/**
		* @brief 获取端口描述（包括端口类型和主要参数）
		* @return 端口描述
		*/
		virtual const string GetDescription() const = 0;

		//sunyongmin 2021.06.10 add begin
		/**
		* @brief 获取连接状态（服务器对应是否有客户端连接，客户端对应是否连接到服务器）
		* @return 连接状态
		*/
		virtual const bool GetLinkState() const = 0;
		//sunyongmin 2021.06.10 add end

		//关闭服务端指定端口
		virtual bool CloseSalverPort(int nClientId) = 0;

		//zhangzhaosen 扩展订阅发布主题接口 begin
		virtual  bool SetSubTopic(string& subTopic) = 0;
		virtual  bool SetUser(string& user) = 0;
		virtual  bool SetPassWord(string& password) = 0;
		//zhangzhaosen 扩展订阅发布主题接口 end
	};

};
#endif  //__Daq_PortBase_H__
