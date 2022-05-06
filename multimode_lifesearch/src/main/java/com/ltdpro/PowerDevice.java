package com.ltdpro;

import java.io.File;

public class PowerDevice
{
	static
	{		
		File f = new File("/system/lib/libPowerDriver-JNI.so");
		if(f.exists())
		{
			DebugUtil.i("MainActivity", "file exists static");
			System.load("/system/lib/libPowerDriver-JNI.so");
		}
		else
		{
			DebugUtil.i("MainActivity", "file not exists static");
		}
	}
	
	public PowerDevice()
	{
		// TODO Auto-generated constructor stub
//		try
//		{
//			// Missing read/write permission, trying to chmod the file
//			Process su;
//			su = Runtime.getRuntime().exec("su");
//			String cmd;
//			cmd = "insmod " + "/system/lib/modules/BatteryDevice.ko" + "\n"
//					+ "exit\n";
//			su.getOutputStream().write(cmd.getBytes());
//			if ((su.waitFor() != 0))
//			{
//				// throw new SecurityException();
//			}
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//			// throw new SecurityException();
//		}
//		
//		try
//		{
//			// Missing read/write permission, trying to chmod the file
//			Process su;
//			su = Runtime.getRuntime().exec("su");
//			String cmd = "chmod 777 " + "/dev/bat" + "\n" + "exit\n";
//			su.getOutputStream().write(cmd.getBytes());
//			if ((su.waitFor() != 0))
//			{
//				throw new SecurityException();
//			}
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//			// throw new SecurityException();
//		}
	}

	// JNI函数
	public native int openPowerDevice();

	public native int closePowerDevice();

	public native boolean PowerLightOn();

	public native boolean PowerLightOff();//不控制，有电时亮

	public native boolean WorkLightOn();

	public native boolean WorkLightOff();

	public native boolean BatLightOn();

	public native boolean BatLightOff();
	
	public native boolean StepPowerUp();

	public native boolean StepPowerDown();
	
	public native boolean DSPPowerUp();

	public native boolean DSPPowerDown();
	
	public native boolean DisplayPowerUp(); 
	//高压控制（不是背光灯），实际用做高压，与AntennaPowerUp同时开关，

	public native boolean DisplayPowerDown(); 
	
	public native boolean AntennaPowerUp();

	public native boolean AntennaPowerDown();

	public native int getRemainTime();
	
	public native int getBatteryAttribute(BatteryAttribute attr);
}
