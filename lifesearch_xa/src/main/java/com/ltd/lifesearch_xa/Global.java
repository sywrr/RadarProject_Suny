package com.ltd.lifesearch_xa;

public class Global {

	//���崫�������
	public final static short BEGTRANS_PACK = 1;      //����ͷ
	public final static short ENDTRANS_PACK = 2;      //����β
	public final static short ACK_PACK = 3;           //Ӧ���
	public final static short DATA_PACK = 4;          //���ݰ�
	public final static short COM_PACK = 5;           //�����
	public final static short WAVE_PACK = 8;          //"��������"���ݰ�
	
	//��������
	public final static short TRANSFER_TYPE_DATAS = 1;          //�ز����ݴ�������
	public final static short TRANSFER_TYPE_COM = 2;            //���������
	public final static short TRANSFER_TYPE_DEVICESTATUS = 4;   //�豸״̬��������
	public final static short TRANSFER_TYPE_DETECTRESULT = 5;   //̽������������
	public final static short TRANSFER_TYPE_WAVE = 9; 			//�״ﲨ��
	
	public final static short MESSAGE_RCV_DEVICE_DATA = 100;    // �����豸״̬����
	public final static short MESSAGE_RCV_COMMAND = 101;        // �����������
	public final static short MESSAGE_SND_COMMAND = 102;        // ����������
	public final static short MESSAGE_RCV_WAVE = 103;           // ����ز�����
	
	//�����״�����
	public static final short RADAR_COMMAND_LENGTH = 2;              //�״��������ֽڳ���
	public static final short RADAR_COMMAND_SHOWWAVE = 0x0001;       //��ʾ�״ﲨ��
	public static final short RADAR_COMMAND_SIGNALPOS = 0x0002;      //�����ź�λ��
	public static final short RADAR_COMMAND_HARDPLUS = 0x0003;       //����Ӳ������
	public static final short RADAR_COMMAND_DETECTRANGE = 0x0004;    //����̽�ⷶΧ
	public static final short RADAR_COMMAND_DETECTMODE = 0x0005;     //����̽��ģʽ
	public static final short RADAR_COMMAND_DETECTBEGPOS = 0x0006;   //���ÿ�ʼ̽��λ��
	public static final short RADAR_COMMAND_JDCONST = 0x0007;        //���ý�糣��
	public static final short RADAR_COMMAND_ZEROOFF = 0x0008;        //������ƫ
	public static final short RADAR_COMMAND_BEGDETECT = 0x0009;      //��ʼ̽��
	public static final short RADAR_COMMAND_ENDDETECT = 0x000a;      //ֹͣ̽��
	public static final short RADAR_COMMAND_BEGSAVE = 0x000b;        //��ʼ��������
	public static final short RADAR_COMMAND_ENDSAVE = 0x000c;        //ֹͣ��������
	public static final short RADAR_COMMAND_BEGCONSAVE = 0x000d;     //��ʼ������������
	public static final short RADAR_COMMAND_ENDCONSAVE = 0x000e;     //ֹͣ������������
	public static final short RADAR_COMMAND_REPORTALLFILES = 0x000f; //�ش����б�����ļ���
	public static final short RADAR_COMMAND_TRANSFILE = 0x0010;      //�ش�ָ�����ļ�
	public static final short RADAR_COMMAND_TIMEWND = 0x0011;        //����ʱ��
	public static final short RADAR_COMMAND_SCANSPEED = 0x0012;      //����ɨ��
	public static final short RADAR_COMMAND_SAMPLEN = 0x0013;        //���õ���
	
	//̽�ⷽʽ
	public static final short CYC_DETECTMODE = 0;      //ѭ��ɨ��̽��ģʽ
	public static final short FIX_DETECTMODE = 1;      //�̶�ɨ��̽��ģʽ
	public static final short SINGLE_DETECTMODE = 2;   //��Ŀ�귽ʽ
	public static final short MANY_DETECTMODE = 3;     //��Ŀ�귽ʽ
	
	public static final short MIX_DETECTRANGE = 12;       //��С̽�ⷶΧ
	public static final short MAX_DETECTRANGE = 24;       //���̽�ⷶΧ
	public static final short DEFAULT_SCAN_RANGE = 300;    //Ĭ��̽�ⷶΧ
	
	public static final short DEFAULT_DETECTRANGE = 3300;   //Ĭ��̽�ⷶΧ
	
	//�����Ѿ�ǰ���豸��״̬
	public static final short RADAR_STATUS_INIT = 0;  				//�豸��ʼ��״̬
	public static final short RADAR_STATUS_SNDINGBEGDETECT = 0x1;   //���ڷ��Ϳ�ʼ̽������
	public static final short RADAR_STATUS_DETECTING = 0x2;			//����̽��
	public static final short RADAR_STATUS_SNDINGENDDETECT = 0x3;   //���ڷ���ֹͣ̽������
	public static final short RADAR_STATUS_READY = 0x4;   			//�豸׼������
	public static final short RADAR_STATUS_NOTREADY = 0x5;		    //�豸��û�о���
	public static final short RADAR_STATUS_SAVING = 0x6;		    //���ڱ�������
	public static final short RADAR_STATUS_CONSAVING = 0x7;		    //����������������
	public static final short RADAR_STATUS_BACKSHOWING = 0x8;	    //���ڻط�̽����
	
	public static final short DEVICE_DETECTING = 1;   //�豸����̽��
	public static final short DEVICE_DETECTEND = 2;   //�豸ֹͣ̽��
	public static final short DEVICE_READY = 3;   //�豸����
	public static final short DEVICE_SAVING = 4;   //���ڱ�������
	public static final short DEVICE_CONTINUE_SAVING = 5;   //����������������
	public static final short DEVICE_RESULT = 6;      //���汾��̽����(����̽�����)
	public static final short DEVICE_INTER_RESULT = 7;     //��������м���
	
	public static final short DEVICE_STATUS = 1;    //�豸״̬
	public static final short DEVICE_DETECT_RESULT = 2;   //̽����
	public static final short DEVICE_ALL_INFS = 3;    //������Ϣ����

	public static final short BLUE_TARGET_MOVE = 1;    //��Ŀ��
	public static final short BLUE_TARGET_BREATH = 2;    //��Ŀ��
	public static final short BLUE_TARGET_FLASH = 3;    //Ŀ������
	public static final short BLUE_TARGET_SCAN = 4;    //��ɨ
	
	public static final short DETECTRANGE_BEGINPIX = 36;   //��ʼ̽�ⷶΧ��λͼ�϶�Ӧ�����ص�
	public static final String RESULT_DIRECTORY_NAME = "SJ6000Data";
	
	//��̽�����ʾ�̶�ֵλ����λͼ�϶�Ӧ�����ص�
	public static int mScalePos[][]={
		 {170, 51},
         {175, 68},
		 {180, 87},
		 {185, 106},
		 {190, 127},
		 {195, 148},
         {200, 167},
         {205, 184},
		 {210, 202},
		 {215, 223},
		 {220, 235}
		 };
	
	public static int mResultPos[][]={
		{35, 51},
		{51, 68},
		{68, 87},
		{87, 106},
		{106, 127},
		{127, 148},
		{148, 167},
		{167, 184},
		{184, 202},
		{202, 223},
		{223, 235}
	};
	
	public static final boolean GRIDVIEW = true;
	public static final short MAXVAL = 0x7fff;
	public static final short MINVAL = -0x7fff;
	
	public static int getUByte(byte val) {
		int uVal = ((int)val) & 0xFF;
		return uVal;
	}
}
