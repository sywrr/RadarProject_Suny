
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
		//�˿ڴ�״̬
		virtual bool IsOpen() = 0;
		//��·����״̬
		virtual bool IsConnect() = 0;
		//������ݻ�����
		virtual void ClearBuffer() = 0;
		//�򿪶˿�
		virtual bool OpenPort() = 0;
		//�رն˿�
		virtual bool ClosePort() = 0;
		//��������
		virtual bool SendData(void *pDataBuffer, int nBufferLength) = 0;

		virtual bool SendData(void *pDataBuffer, int nBufferLength, int nClientId) = 0;
		//��������
		virtual int  ReceiveData(BYTE *pBuffer, int nMaxLength, int nTimeOut) = 0;

		virtual int ReceiveData(BufferReceive &stBuffer, int nTimeOut) = 0;
		//���ûص�
		virtual void SetCallBack(IPortCallBack *pCallBack) = 0;

		/**
		* @brief ����̽����
		*
		* @param *pMonitor ̽����ָ��
		*
		* @return ��
		*/
		virtual void SetLogger(int channelId, ILogger *pLogger) = 0;

		/**
		* @brief ��ȡ�˿������������˿����ͺ���Ҫ������
		* @return �˿�����
		*/
		virtual const string GetDescription() const = 0;

		//sunyongmin 2021.06.10 add begin
		/**
		* @brief ��ȡ����״̬����������Ӧ�Ƿ��пͻ������ӣ��ͻ��˶�Ӧ�Ƿ����ӵ���������
		* @return ����״̬
		*/
		virtual const bool GetLinkState() const = 0;
		//sunyongmin 2021.06.10 add end

		//�رշ����ָ���˿�
		virtual bool CloseSalverPort(int nClientId) = 0;

		//zhangzhaosen ��չ���ķ�������ӿ� begin
		virtual  bool SetSubTopic(string& subTopic) = 0;
		virtual  bool SetUser(string& user) = 0;
		virtual  bool SetPassWord(string& password) = 0;
		//zhangzhaosen ��չ���ķ�������ӿ� end
	};

};
#endif  //__Daq_PortBase_H__
