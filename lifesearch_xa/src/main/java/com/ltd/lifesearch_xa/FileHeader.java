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
	   short xPoints;     //���������Ŀ(ƽ����X�᷽�������Ŀ)
	   short yPoints;     //���������Ŀ(ƽ����Y�᷽�������Ŀ)
	   short xinter;      //�����ֵ����
       short yinter;      //�����ֵ����
	   float xgrid;             //���ߺ�����(��ʱ��¼ÿ��x���߰����ĵ���)
	   float ygrid;             //����������(��ʱ��¼ÿ��y���߰����ĵ���)
	   short pcr_startB;  //��λУ���������
	   short pcr_endB;    //��λУ������ֹ��
	   short pcr_startP;  //��λУ���������
	   short pcr_endP;    //��λУ������ֹ��
	   short bkr_off;     //��������ƫ��
	   short rowtracks;   //ԭʼ���ݼ�¼����
	   short image_l;     //��άͼ��ĳ����(x,y,z)
	   short image_w;     //     Ĭ�ϳ�=�� = 200���ص�
	   short image_h;     //     Ĭ�ϸ�=128����Ƭ
	   short apr_x;       //��ά�ϳɿ׾��������
	   short apr_y;
	   short ground_pos;  //�ر��ڼ�¼���е�λ�ã�������
	};                          //��ռ��40�ֽ�
	//
	private String TAG="FileHeader";
	private short  X1Flag=256;
	short rh_tag=X1Flag;     //ƫ��:0 tag     
    public short rh_data;        //ƫ��:2 ����λ��ƫ�� ��Ӳ���ṩ         
    public short rh_nsamp;       //ƫ��:4 ÿ���������� ��Ӳ���ṩ 128,256,512,1024,2048    
    short rh_bits = 16;   //ƫ��:6 ����λ�� ��Ӳ���ṩ          
    public short rh_zero;        //ƫ��:8 ������ƫ ��Ӳ���ṩ         
	                            //total 10 bytes;
    public float rh_sps;         //ƫ��:10         //ÿ��ɨ�����(ɨ��) ��Ӳ���ṩ 8,16,32,64,128
    float rh_spm;         //ƫ��:14         //ÿ��ɨ����� ��Ӳ���ṩ
    float rh_mpm;         //ƫ��:18         //��Ǽ�� ��Ӳ���ṩ
    public float rh_position;    //ƫ��:22         //λ�� ��Ӳ���ṩ
    public float rh_range;       //ƫ��:26         //ʱ��(ns) ��Ӳ���ṩ     
                                //total 20 bytes;
    public short rh_spp;         //ƫ��:30  ���⡡����������WINDOWS�ɼ�����汾�д洢����Ƶ�ʣ�
    int   rh_creat;       //ƫ��:32 ������������
    int   rh_modif;       //ƫ��:36 �����޸�����
    short rh_rgain;       //ƫ��:40 ��������λ�� ��Ӳ���ṩ
    public short rh_nrgain;      //ƫ��:42 �������߳��� ��Ӳ���ṩ
    short rh_text;        //ƫ��:44 �ı�λ��
    short rh_ntext;       //ƫ��:46 �ı�����
    short rh_proc;        //ƫ��:48 ��������λ��
    short rh_nproc;       //ƫ��:50 �������ݳ���
    short rh_nchan;       //ƫ��:52 ����
	                            //total 24 bytes;
    
    public float rh_epsr;        //ƫ��:54  ƽ����糣��   ��Ӳ���ṩ
    float rh_top;         //ƫ��:58  �ź�ͷ��Ӧ��� ��Ӳ���ṩ
    float rh_depth;       //ƫ��:62  ��ȷ�Χ       �����
	                           //total 12bytes;
    short rh_npass;       //ƫ��:66 ����         
	byte[]  reserved=new byte[41];   //ƫ��:68   �����ֽ�
	short rh_flagExt;     //ƫ��:109    �����չ
	private byte WHELL_MODE=2;      //�ֲ�ģʽ
	private byte DIANCE_MODE=1;     //���ģʽ
	private byte TIME_MODE=0;       //ʱ��ģʽ(����ģʽ)
	byte  rh_workType = TIME_MODE;    //ƫ��:111    ����ģʽ(����|���|�ֲ�)
    short rh_chanmask;    //ƫ��:112 ����
    char[]  rh_fname = new char[12];   //ƫ��:114 �ļ��� �����
    short rh_chksum;      //ƫ��:126  ����---�ļ�ͷЧ��� 
	                         //total 62 bytes;
    public float[] rh_rgainf = new float[22];  //ƫ��:128 ������������
	                         //total 88 bytes;
    Image_Par rh_image;   //ƫ��:216(��40���ֽ�)  
	int   m_left = 768;      //ʣ���ֽ���
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
	////�õ������������ݵĴ�������
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
	//���ô�������
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
		//ȡ������
		buf[4] = (byte)rh_nsamp;
		buf[5] = (byte)(rh_nsamp>>8);
		//����λ��
		buf[6] = (byte)rh_bits;
		buf[7] = (byte)(rh_bits>>8);
		//ɨ��
		buf[10] = (byte)((int)rh_sps);
		buf[11] = (byte)((int)rh_sps>>8);
		buf[12] = (byte)((int)rh_sps>>16);
		buf[13] = (byte)((int)rh_sps>>24);
		//��Ǽ��
		buf[18] = (byte)((int)(rh_mpm*100));
		buf[19] = (byte)((int)(rh_mpm*100) >> 8);
		buf[20] = (byte)((int)(rh_mpm*100) >>16);
		buf[21] = (byte)((int)(rh_mpm*100) >>24);
		
		//ʱ��
		buf[26] = (byte)((int)rh_range);
		buf[27] = (byte)((int)rh_range>>8);
		buf[28] = (byte)((int)rh_range>>16);
		buf[29] = (byte)((int)rh_range>>24);
		
		//
		//��糣��
		buf[54] = (byte)((int)(rh_epsr*10));
		buf[55] = (byte)((int)(rh_epsr*10)>>8);
		buf[56] = (byte)((int)(rh_epsr*10)>>16);
		buf[57] = (byte)((int)(rh_epsr*10)>>24);
		
		//��Ǽ��
		buf[109] = (byte)(rh_flagExt);
		buf[110] = (byte)(rh_flagExt>>8);
		
		//������ʽ
		buf[111] = (byte)(rh_workType);
		
		//Ӳ������
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
			//ȡ������
			rh_nsamp  = buf[4];
			rh_nsamp |= (buf[5]<<8);
			
			//����λ��
			rh_bits = buf[6];
			rh_bits |= buf[7]<<8;
			
			//ɨ��
			temVal = 0;
			rh_sps = buf[10];
			temVal = buf[11]<<8;
			rh_sps += temVal;
			temVal = buf[12]<<16;
			rh_sps += temVal;
			temVal = buf[13]<<24;
			rh_sps += temVal;
			rh_sps = rh_sps/10;
			
			//��Ǽ��
			rh_mpm = buf[18];
			temVal = buf[19]<<8;
			rh_mpm += temVal;
			temVal = buf[20]<<16;
			rh_mpm += temVal;
			temVal = buf[21]<<24;
			rh_mpm += temVal;
			rh_mpm = rh_mpm/100;
			
			//ʱ��
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
			
			//��糣��
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
			
			//��Ǽ��
			rh_flagExt = buf[109];
			rh_flagExt += buf[110]<<8;
			
			//������ʽ
			rh_workType = buf[111];
			
			//Ӳ������
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
			
			//�Բ������м��
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
