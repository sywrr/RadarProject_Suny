package com.ltd.lifesearch_xa;

public class Global {

	//定义传输包类型
	public final static short BEGTRANS_PACK = 1;      //传输头
	public final static short ENDTRANS_PACK = 2;      //传输尾
	public final static short ACK_PACK = 3;           //应答包
	public final static short DATA_PACK = 4;          //数据包
	public final static short COM_PACK = 5;           //命令包
	public final static short WAVE_PACK = 8;          //"波形数据"数据包
	
	//传输类型
	public final static short TRANSFER_TYPE_DATAS = 1;          //回波数据传输类型
	public final static short TRANSFER_TYPE_COM = 2;            //命令传输类型
	public final static short TRANSFER_TYPE_DEVICESTATUS = 4;   //设备状态传输类型
	public final static short TRANSFER_TYPE_DETECTRESULT = 5;   //探测结果传输类型
	public final static short TRANSFER_TYPE_WAVE = 9; 			//雷达波形
	
	public final static short MESSAGE_RCV_DEVICE_DATA = 100;    // 处理设备状态数据
	public final static short MESSAGE_RCV_COMMAND = 101;        // 处理接收命令
	public final static short MESSAGE_SND_COMMAND = 102;        // 处理发送命令
	public final static short MESSAGE_RCV_WAVE = 103;           // 处理回波数据
	
	//定义雷达命令
	public static final short RADAR_COMMAND_LENGTH = 2;              //雷达命令字字节长度
	public static final short RADAR_COMMAND_SHOWWAVE = 0x0001;       //显示雷达波形
	public static final short RADAR_COMMAND_SIGNALPOS = 0x0002;      //设置信号位置
	public static final short RADAR_COMMAND_HARDPLUS = 0x0003;       //设置硬件增益
	public static final short RADAR_COMMAND_DETECTRANGE = 0x0004;    //设置探测范围
	public static final short RADAR_COMMAND_DETECTMODE = 0x0005;     //设置探测模式
	public static final short RADAR_COMMAND_DETECTBEGPOS = 0x0006;   //设置开始探测位置
	public static final short RADAR_COMMAND_JDCONST = 0x0007;        //设置介电常数
	public static final short RADAR_COMMAND_ZEROOFF = 0x0008;        //设置零偏
	public static final short RADAR_COMMAND_BEGDETECT = 0x0009;      //开始探测
	public static final short RADAR_COMMAND_ENDDETECT = 0x000a;      //停止探测
	public static final short RADAR_COMMAND_BEGSAVE = 0x000b;        //开始保存数据
	public static final short RADAR_COMMAND_ENDSAVE = 0x000c;        //停止保存数据
	public static final short RADAR_COMMAND_BEGCONSAVE = 0x000d;     //开始连续保存数据
	public static final short RADAR_COMMAND_ENDCONSAVE = 0x000e;     //停止连续保存数据
	public static final short RADAR_COMMAND_REPORTALLFILES = 0x000f; //回传所有保存的文件名
	public static final short RADAR_COMMAND_TRANSFILE = 0x0010;      //回传指定的文件
	public static final short RADAR_COMMAND_TIMEWND = 0x0011;        //设置时窗
	public static final short RADAR_COMMAND_SCANSPEED = 0x0012;      //设置扫速
	public static final short RADAR_COMMAND_SAMPLEN = 0x0013;        //设置道长
	
	//探测方式
	public static final short CYC_DETECTMODE = 0;      //循环扫描探测模式
	public static final short FIX_DETECTMODE = 1;      //固定扫描探测模式
	public static final short SINGLE_DETECTMODE = 2;   //单目标方式
	public static final short MANY_DETECTMODE = 3;     //多目标方式
	
	public static final short MIX_DETECTRANGE = 12;       //最小探测范围
	public static final short MAX_DETECTRANGE = 24;       //最大探测范围
	public static final short DEFAULT_SCAN_RANGE = 300;    //默认探测范围
	
	public static final short DEFAULT_DETECTRANGE = 3300;   //默认探测范围
	
	//定义搜救前端设备的状态
	public static final short RADAR_STATUS_INIT = 0;  				//设备初始化状态
	public static final short RADAR_STATUS_SNDINGBEGDETECT = 0x1;   //正在发送开始探测命令
	public static final short RADAR_STATUS_DETECTING = 0x2;			//正在探测
	public static final short RADAR_STATUS_SNDINGENDDETECT = 0x3;   //正在发送停止探测命令
	public static final short RADAR_STATUS_READY = 0x4;   			//设备准备就绪
	public static final short RADAR_STATUS_NOTREADY = 0x5;		    //设备还没有就绪
	public static final short RADAR_STATUS_SAVING = 0x6;		    //正在保存数据
	public static final short RADAR_STATUS_CONSAVING = 0x7;		    //正在连续保存数据
	public static final short RADAR_STATUS_BACKSHOWING = 0x8;	    //正在回放探测结果
	
	public static final short DEVICE_DETECTING = 1;   //设备正在探测
	public static final short DEVICE_DETECTEND = 2;   //设备停止探测
	public static final short DEVICE_READY = 3;   //设备就绪
	public static final short DEVICE_SAVING = 4;   //正在保存数据
	public static final short DEVICE_CONTINUE_SAVING = 5;   //正在连续保存数据
	public static final short DEVICE_RESULT = 6;      //报告本次探测结果(本段探测结束)
	public static final short DEVICE_INTER_RESULT = 7;     //报告出现中间结果
	
	public static final short DEVICE_STATUS = 1;    //设备状态
	public static final short DEVICE_DETECT_RESULT = 2;   //探测结果
	public static final short DEVICE_ALL_INFS = 3;    //两种信息都有

	public static final short BLUE_TARGET_MOVE = 1;    //动目标
	public static final short BLUE_TARGET_BREATH = 2;    //静目标
	public static final short BLUE_TARGET_FLASH = 3;    //目标类型
	public static final short BLUE_TARGET_SCAN = 4;    //空扫
	
	public static final short DETECTRANGE_BEGINPIX = 36;   //开始探测范围在位图上对应的像素点
	public static final String RESULT_DIRECTORY_NAME = "SJ6000Data";
	
	//各探测段显示刻度值位置在位图上对应的像素点
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
