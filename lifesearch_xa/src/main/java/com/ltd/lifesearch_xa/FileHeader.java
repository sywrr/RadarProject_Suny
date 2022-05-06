package com.ltd.lifesearch_xa;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.util.Log;
import android.widget.Toast;
public class FileHeader {
	class  Image_Par
	{
	   short xPoints;     //横向测线数目(平行于X轴方向测线数目)
	   short yPoints;     //纵向测线数目(平行于Y轴方向测线数目)
	   short xinter;      //横向插值点数
       short yinter;      //纵向插值点数
	   float xgrid;             //测线横向间隔(暂时记录每条x测线包含的道数)
	   float ygrid;             //测线纵向间隔(暂时记录每条y测线包含的道数)
	   short pcr_startB;  //相位校正因子起点
	   short pcr_endB;    //相位校正因子止点
	   short pcr_startP;  //相位校正因子起点
	   short pcr_endP;    //相位校正因子止点
	   short bkr_off;     //背景消除偏移
	   short rowtracks;   //原始数据记录道数
	   short image_l;     //三维图象的长宽高(x,y,z)
	   short image_w;     //     默认长=宽 = 200像素点
	   short image_h;     //     默认高=128个切片
	   short apr_x;       //二维合成孔径横向点数
	   short apr_y;
	   short ground_pos;  //地表在纪录道中的位置（点数）
	};                          //共占用40字节
	//
	private String TAG="FileHeader";
	private short  X1Flag=256;
	short rh_tag=X1Flag;     //偏移:0 tag     
    public short rh_data;        //偏移:2 数据位置偏移 由硬件提供         
    public short rh_nsamp;       //偏移:4 每道采样点数 由硬件提供 128,256,512,1024,2048    
    short rh_bits = 16;   //偏移:6 数据位数 由硬件提供          
    public short rh_zero;        //偏移:8 数据零偏 由硬件提供         
	                            //total 10 bytes;
    public float rh_sps;         //偏移:10         //每秒扫描道数(扫速) 由硬件提供 8,16,32,64,128
    float rh_spm;         //偏移:14         //每米扫描道数 由硬件提供
    float rh_mpm;         //偏移:18         //标记间距 由硬件提供
    public float rh_position;    //偏移:22         //位置 由硬件提供
    public float rh_range;       //偏移:26         //时窗(ns) 由硬件提供     
                                //total 20 bytes;
    public short rh_spp;         //偏移:30  任意　　　　　　WINDOWS采集软件版本中存储天线频率；
    int   rh_creat;       //偏移:32 数据生成日期
    int   rh_modif;       //偏移:36 数据修改日期
    short rh_rgain;       //偏移:40 增益曲线位置 由硬件提供
    public short rh_nrgain;      //偏移:42 增益曲线长度 由硬件提供
    short rh_text;        //偏移:44 文本位置
    short rh_ntext;       //偏移:46 文本长度
    short rh_proc;        //偏移:48 处理数据位置
    short rh_nproc;       //偏移:50 处理数据长度
    short rh_nchan;       //偏移:52 任意
	                            //total 24 bytes;
    
    public float rh_epsr;        //偏移:54  平均介电常数   由硬件提供
    float rh_top;         //偏移:58  信号头对应深度 由硬件提供
    float rh_depth;       //偏移:62  深度范围       软件给
	                           //total 12bytes;
    short rh_npass;       //偏移:66 任意         
	byte[]  reserved=new byte[41];   //偏移:68   保留字节
	short rh_flagExt;     //偏移:109    标记扩展
	private byte WHELL_MODE=2;      //轮测模式
	private byte DIANCE_MODE=1;     //点测模式
	private byte TIME_MODE=0;       //时间模式(连续模式)
	byte  rh_workType = TIME_MODE;    //偏移:111    工作模式(连续|点测|轮测)
    short rh_chanmask;    //偏移:112 任意
    char[]  rh_fname = new char[12];   //偏移:114 文件名 软件给
    short rh_chksum;      //偏移:126  任意---文件头效验和 
	                         //total 62 bytes;
    public float[] rh_rgainf = new float[22];  //偏移:128 增益曲线数组
	                         //total 88 bytes;
    Image_Par rh_image;   //偏移:216(共40个字节)  
	int   m_left = 768;      //剩余字节数
	////
	public double getDeep()
	{
		double speed=0.3*100/Math.sqrt(rh_epsr);     //cm/s;
		double deep=(rh_range)*speed/200;

		//
		return deep;
	}
	////
	public float[] getHardplus()
	{
		return rh_rgainf;
	}
	////
	public int getTimeWindow()
	{
		return (int) rh_range;
	}
	////得到相邻两道数据的触发距离
	public double getDistancePerScans()
	{
		if(rh_workType != WHELL_MODE)
			return 0;
		//
		return rh_mpm*rh_flagExt;
	}
	//
	public void setTimeMode()
	{
		rh_workType = TIME_MODE;
	}
	//
	public void setDianceMode()
	{
		rh_workType = DIANCE_MODE;
	}
	//
	public void setWhellMode()
	{
		rh_workType = WHELL_MODE;
	}
	//
	public boolean isWhellMode()
	{
		return rh_workType == WHELL_MODE;
	}
	//
	public void setWhellMode(int num)
	{
		rh_flagExt = (short) num;
	}
	//设置触发距离
	public void setTouchDistance(double touchDistance)
	{
		rh_mpm = (float) touchDistance;
	}
	//
	public void setHardPlus(float[] vals)
	{
		for(int i=0;i<9;i++)
			rh_rgainf[i] = vals[i];
	}
	//
	public void setJiedianConst(double jdconst)
	{
		rh_epsr = (float) jdconst;
	}
	//
	public void setScanspeed(short speed)
	{
		rh_sps = speed;
	}
	//
	public void setFlagExtent(int flagNum)
	{
		rh_flagExt = (short) flagNum;
	}
	////
	public void save(String fileName)
	{
		try{
			int i;
			File file = new File(fileName);
			FileOutputStream fOut = new FileOutputStream(file);
			DataOutputStream ds = new DataOutputStream(fOut);
			
			ds.writeShort(rh_tag);
			ds.writeShort(rh_data);
			ds.writeShort(rh_nsamp);
			ds.writeShort(rh_bits);
			ds.writeShort(rh_zero);
			
			ds.writeFloat(rh_sps);
			ds.writeFloat(rh_spm);
			ds.writeFloat(rh_mpm);
			ds.writeFloat(rh_position);
			ds.writeFloat(rh_range);
			
			ds.writeShort(rh_spp);
			ds.writeInt(rh_creat);
			ds.writeInt(rh_modif);
			ds.writeShort(rh_rgain);
			ds.writeShort(rh_nrgain);
			ds.writeShort(rh_text);
			ds.writeShort(rh_ntext);
			ds.writeShort(rh_proc);
			ds.writeShort(rh_nproc);
			ds.writeShort(rh_nchan);
			
			ds.writeFloat(rh_epsr);
			ds.writeFloat(rh_top);
			ds.writeFloat(rh_depth);
			
			ds.writeShort(rh_npass);
			ds.write(reserved, 0, 41);
			ds.writeShort(rh_flagExt);
			ds.writeByte(rh_workType);
			ds.writeShort(rh_chanmask);
			for(i=0;i<12;i++)
				ds.writeChar(rh_fname[i]);
			ds.writeShort(rh_chksum);
			
			for(i=0;i<22;i++)
				ds.writeFloat(rh_rgainf[i]);
			
			byte[] tem = new byte[808];
			ds.write(tem,0,808);
			ds.flush();
			ds.close();
			fOut.close();
		}
		catch(Exception e)
		{
			Log.i(TAG,"save Filehead fail!");
		}
	}
	////
	public void save(FileOutputStream fileOS)
	{
		/*
		int i;
		try{
			DataOutputStream ds = new DataOutputStream(fileOS);
			
			ds.writeShort(rh_tag);
			ds.writeShort(rh_data);
			ds.writeShort(rh_nsamp);
			ds.writeShort(rh_bits);
			ds.writeShort(rh_zero);
			
			ds.writeFloat(rh_sps);
			ds.writeFloat(rh_spm);
			ds.writeFloat(rh_mpm);
			ds.writeFloat(rh_position);
			ds.writeFloat(rh_range);
			
			ds.writeShort(rh_spp);
			ds.writeInt(rh_creat);
			ds.writeInt(rh_modif);
			ds.writeShort(rh_rgain);
			ds.writeShort(rh_nrgain);
			ds.writeShort(rh_text);
			ds.writeShort(rh_ntext);
			ds.writeShort(rh_proc);
			ds.writeShort(rh_nproc);
			ds.writeShort(rh_nchan);
			
			ds.writeFloat(rh_epsr);
			ds.writeFloat(rh_top);
			ds.writeFloat(rh_depth);
			
			ds.writeShort(rh_npass);
			ds.write(reserved, 0, 41);
			ds.writeShort(rh_flagExt);
			ds.writeByte(rh_workType);
			ds.writeShort(rh_chanmask);
			for(i=0;i<12;i++)
				ds.writeChar(rh_fname[i]);
			ds.writeShort(rh_chksum);
			
			for(i=0;i<22;i++)
				ds.writeFloat(rh_rgainf[i]);
			
			byte[] tem = new byte[808];
			ds.write(tem,0,808);
			ds.flush();
			ds.close();
		}
		catch(Exception e)
		{
			Log.i(TAG,"save Filehead fail!");
		}
		*/
		
		byte[] buf = new byte[1024];
		buf[0] = (byte)rh_tag;
		buf[1] = (byte)(rh_tag>>8);
		//取样点数
		buf[4] = (byte)rh_nsamp;
		buf[5] = (byte)(rh_nsamp>>8);
		//数据位数
		buf[6] = (byte)rh_bits;
		buf[7] = (byte)(rh_bits>>8);
		//扫速
		buf[10] = (byte)((int)rh_sps);
		buf[11] = (byte)((int)rh_sps>>8);
		buf[12] = (byte)((int)rh_sps>>16);
		buf[13] = (byte)((int)rh_sps>>24);
		//标记间隔
		buf[18] = (byte)((int)(rh_mpm*100));
		buf[19] = (byte)((int)(rh_mpm*100) >> 8);
		buf[20] = (byte)((int)(rh_mpm*100) >>16);
		buf[21] = (byte)((int)(rh_mpm*100) >>24);
		
		//时窗
		buf[26] = (byte)((int)rh_range);
		buf[27] = (byte)((int)rh_range>>8);
		buf[28] = (byte)((int)rh_range>>16);
		buf[29] = (byte)((int)rh_range>>24);
		
		//
		//介电常数
		buf[54] = (byte)((int)(rh_epsr*10));
		buf[55] = (byte)((int)(rh_epsr*10)>>8);
		buf[56] = (byte)((int)(rh_epsr*10)>>16);
		buf[57] = (byte)((int)(rh_epsr*10)>>24);
		
		//标记间隔
		buf[109] = (byte)(rh_flagExt);
		buf[110] = (byte)(rh_flagExt>>8);
		
		//工作方式
		buf[111] = (byte)(rh_workType);
		
		//硬件增益
		int i;
		for(i=0;i<9;i++)
		{
			buf[128+i*4+0]   = (byte) ((int)rh_rgainf[i]);
			buf[128+i*4+1] = (byte)((int)rh_rgainf[i]>>8);
			buf[128+i*4+2] = (byte)((int)rh_rgainf[i]>>16);
			buf[128+i*4+3] = (byte)((int)rh_rgainf[i]>>24);
		}
		
		//
		try
		{
			fileOS.write(buf, 0, 1024);
		}
		catch (Exception e)
		{
			Log.i(TAG,"save fileheader fail!");
		}
		
	}
	
	////
	public void load(FileInputStream fileOS)
	{
		/*
		int i;
		try{
			DataInputStream ds = new DataInputStream(fileOS);
			
			rh_tag = ds.readShort();
			rh_data=ds.readShort();
			rh_nsamp = ds.readShort();
			rh_bits=ds.readShort();
			rh_zero=ds.readShort();
			
			rh_sps=ds.readFloat();
			rh_spm=ds.readFloat();
			rh_mpm=ds.readFloat();
			rh_position=ds.readFloat();
			rh_range=ds.readFloat();
			
			rh_spp=ds.readShort();
			rh_creat=ds.readInt();
			rh_modif=ds.readInt();
			rh_rgain=ds.readShort();
			rh_nrgain=ds.readShort();
			rh_text=ds.readShort();
			rh_ntext=ds.readShort();
			rh_ntext=ds.readShort();
			rh_ntext=ds.readShort();
			rh_ntext=ds.readShort();
			
			rh_epsr=ds.readFloat();
			rh_top=ds.readFloat();
			rh_depth=ds.readFloat();
			
			rh_npass=ds.readShort();
			ds.read(reserved, 0, 41);
			rh_flagExt=ds.readShort();
			rh_workType=ds.readByte();
			rh_chanmask=ds.readShort();
			for(i=0;i<12;i++)
				rh_fname[i]=ds.readChar();
			rh_chksum=ds.readShort();
			
			for(i=0;i<22;i++)
				rh_rgainf[i]=ds.readFloat();
			
			byte[] tem = new byte[808];
			ds.read(tem,0,808);
			ds.close();
		}
		catch(Exception e)
		{
			Log.i(TAG,"save Filehead fail!");
		}
		*/
		
		byte[] buf = new byte[1024];
		float temVal;
		try
		{
			fileOS.read(buf,0,1024);
			//取样点数
			rh_nsamp  = buf[4];
			rh_nsamp |= (buf[5]<<8);
			
			//数据位数
			rh_bits = buf[6];
			rh_bits |= buf[7]<<8;
			
			//扫速
			temVal = 0;
			rh_sps = buf[10];
			temVal = buf[11]<<8;
			rh_sps += temVal;
			temVal = buf[12]<<16;
			rh_sps += temVal;
			temVal = buf[13]<<24;
			rh_sps += temVal;
			rh_sps = rh_sps/10;
			
			//标记间隔
			rh_mpm = buf[18];
			temVal = buf[19]<<8;
			rh_mpm += temVal;
			temVal = buf[20]<<16;
			rh_mpm += temVal;
			temVal = buf[21]<<24;
			rh_mpm += temVal;
			rh_mpm = rh_mpm/100;
			
			//时窗
			rh_range=buf[26];
			temVal = 0;
			temVal = buf[27]<<8;
			rh_range += temVal;
			temVal = 0;
			temVal = buf[28]<<16;
			rh_range += temVal;
			temVal = 0;
			temVal = buf[29]<<24;
			rh_range += temVal;
			
			//介电常数
			rh_epsr = buf[54];
			temVal = 0;
			temVal = buf[55]<<8;
			rh_epsr += temVal;
			temVal = 0;
			temVal = buf[56]<<16;
			rh_epsr += temVal;
			temVal = 0;
			temVal = buf[57]<<24;
			rh_epsr += temVal;
			rh_epsr = (float)(rh_epsr/10.);
			
			//标记间隔
			rh_flagExt = buf[109];
			rh_flagExt += buf[110]<<8;
			
			//工作方式
			rh_workType = buf[111];
			
			//硬件增益
			int i;
			for(i=0;i<9;i++)
			{
				rh_rgainf[i] = buf[128+i*4+0];
				//
				temVal = buf[128+i*4+1]<<8;
				rh_rgainf[i] += temVal;
				//
				temVal = buf[128+i*4+2]<<16;
				rh_rgainf[i] += temVal;
				//
				temVal = buf[128+i*4+3]<<24;
				rh_rgainf[i] += temVal;
			}
			
			//对参数进行检查
			if(rh_epsr<0 || rh_epsr>200)
				rh_epsr = 1;
			if(rh_nsamp <=0)
				rh_nsamp=512;
			Log.i(TAG,"rh_nsamp:=" + rh_nsamp + "rh_range:=" + rh_range);
		}
		catch(Exception e)
		{
			Log.i(TAG,"load Fileheader file fail!");
		}
		
	}
}
