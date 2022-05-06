
/****************************************************************************************
*
* @file    IPortCallBack.h
* @version 1.0
* @date    2021-04-30
* @author  孙永民
*
* @brief   回调定义头文件
*
***************************************************************************************/
// 版权(C) 2009 - 2021 电波传播研究所
// 改动历史
// 日期         作者     改动内容
// 2021-04-30   孙永民   创建文件
//==============================================================================

#ifndef __IPortCallBack_H__
#define __IPortCallBack_H__

namespace DAQ
{
	class IPortCallBack
	{
	public:
		virtual void OnReceiveData(char *szBuffer, int nRealReads) = 0;
	public:
		bool m_isLinked;
	};
}

#endif