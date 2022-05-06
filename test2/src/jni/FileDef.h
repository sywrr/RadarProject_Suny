/****************************************************************************************
*
* @file    FileDef.h
* @version 1.0
* @date    2020-10-14
* @author  ������
*
* @brief   �ļ����񹫹��ļ�
*
***************************************************************************************/
// ��Ȩ(C) 2009 - 2012 ��������Ƽ�
// �Ķ���ʷ
// ����         ����     �Ķ�����
// 2021-02-01   ������   �����ļ�
//==============================================================================

#ifndef __app_web_trans_FileDef_h__
#define __app_web_trans_FileDef_h__

#include <string>

namespace DAQ
{
	//�ļ�ͷ�ĳ���
	#define HEADER_LENGTH			1024		//�ļ�ͷ����

	//���ڲ����ṹ
	struct HEAD_DATE{
		unsigned sec2 : 5;				//��/2	(0-29)
		unsigned min : 6;				//��		(0-59)
		unsigned hour : 5;				//ʱ		(0-23)
		unsigned day : 5;				//��		(0-31)
		unsigned month : 4;				//��		(1-12)
		unsigned year : 7;				//��		(0-127=1980-2107)
	};
	//3d���ݲ����ṹ
	struct HEAD_3DPAR{
		short directionType;			//�����ж����ĸ����������(01:x,10:y,11:xy)
		short reverse;					//�����ֽڣ���ʽ��ʹ�ṹ��������Ȼ����Ҫ��
		float xDistance;				//x�᷽���ϵľ���(��λcm)
		float yDistance;				//y�᷽���ϵľ���(��λcm)
		float zDistance;				//z�᷽���ϵľ���(��λcm)
		short xLines;					//ƽ����x�᷽���ϵĲ�����
		short yLines;					//ƽ����y�᷽���ϵĲ�����
		float xDistanceTwoLine;			//x�����ϵ����ڲ��ߵļ������(��λm)
		float yDistanceTwoLine;			//y�����ϵ����ڲ��ߵļ������(��λm)
		short xScansPerLine;			//x������ÿ�����߰��������ݵ���
		short yScansPerLine;			//y������ÿ�����߰��������ݵ���
	};
	//׮�Ų����ṹ
	struct HEAD_PEGPAR{
		int begPeg;						//��ʼ׮��(ǧ��)
		int begPegAdd;					//��ʼ׮�Ÿ��Ӳ���(��)
		int endPeg;						//����׮��(ǧ��)
		int endPegAdd;					//����׮�Ÿ��Ӳ���(��)
		int pegInterval;				//׮�ż��(��)
	};

	//�ļ�ͷ�ṹ
	struct HEAD_FILEHEADER{
		//�� 10 �ֽ�(1-10);
		short		lh_tag;				//�ļ���־(����ļ�����)
		short		lh_data;			//����ƫ��λ��	��Ӳ���ṩ
		short		lh_nsamp;			//��������		��Ӳ���ṩ
		short		lh_bits;			//����λ��		��Ӳ���ṩ
		short		lh_zero;			//������ƫ		��Ӳ���ṩ
		//�� 20 �ֽ�(11-30);
		float		lh_sps;				//ÿ��ɨ�����	��Ӳ���ṩ
		float		lh_spm;				//ÿ��ɨ�����	��Ӳ���ṩ
		float		lh_mpm;				//������		��Ӳ���ṩ
		float		lh_pos;				//�ź�λ��		��Ӳ���ṩ
		float		lh_range;			//ʱ��(ns)		��Ӳ���ṩ
		//�� 24 �ֽ�(31-54);
		short		lh_spp;				//����Ƶ��
		HEAD_DATE	lh_create;			//������������
		HEAD_DATE	lh_modif;			//�����޸�����
		short		lh_rgain;			//��������λ��
		short		lh_nrgain;			//�������߳���
		short		lh_text;			//����˵��λ��
		short		lh_ntext;			//����˵������
		short		lh_proc;			//��������λ��
		short		lh_nproc;			//�������ݳ���
		short		lh_nchan;			//��˫ͨ����־(1:��ͨ��,2:˫ͨ��)
		//�� 12 �ֽ�(55-66);
		float		lh_epsr;			//��糣��		��Ӳ���ṩ
		float		lh_top;				//��ʼ���		��Ӳ���ṩ
		float		lh_depth;			//��ȷ�Χ		������ṩ
		//�� 62 �ֽ�(67-128);
		short		lh_npass;			//����
		short		lh_device;			//�豸����
		short		lh_file;			//�ļ�����
		short		lh_gps;				//GPS��־
		char		lh_gpsform[4];		//GPS��ʽ
		char		lh_anten[4];		//��������
		char		lh_reserv1[9];		//�����ֽ�
		char		lh_lrmanual;		//�Ƿ���Ϊ�������ߵĿ̶ȼ��(1,2,3:��Ϊ���ù�,1:ʱ��,2:���,3:����)
		char		lh_rrmanual;		//�Ƿ���Ϊ�����ұ�ߵĿ̶ȼ��(1,2,3:��Ϊ���ù�,1:ʱ��,2:���,3:����)
		float		lh_lrlong;			//���߳��̶ȼ��
		short		lh_lrshort;			//���߳��̶ȼ�Ķ̶̿���
		float		lh_rrlong;			//�ұ�߳��̶ȼ��
		short		lh_rrshort;			//�ұ�߳��̶ȼ�Ķ̶̿���
		short       lh_peg;				//׮����ʾ��ʽ(0������:����,1:���)
		short       lh_horuler;			//�Ϸ������ʾ��ʽ(1:����,2:����,3:׮��,����:ʲôҲ����ʾ)
		short		lh_extent;			//�����չ
		char		lh_work;			//����ģʽ(����|���|�ֲ�)(0,1,2)
		short		lh_chanmask;		//����
		char		lh_fname[12];		//�ļ��� �����
		short		lh_chksum;			//����
		//�� 88 �ֽ�(129-216);
		float		lh_rgainf[22];		//������������
		//�� 732 �ֽ�(217-948);
		char		lh_reserv2[732];	//�����ֽ�
		//�� 52 �ֽ�(949-1000);
		HEAD_3DPAR	lh_3dParam;			//3D����
		HEAD_PEGPAR	lh_pegParam;		//׮�Ų���
		//�� 24 �ֽ�(1000-1024);
		char		lh_reserv3[24];		//�����ֽ�
	};

}//APP
#endif// __app_web_trans_FileDef_h__