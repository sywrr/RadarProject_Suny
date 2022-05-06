package com.ltdpro;

import java.io.File;

public class USBDriver {

    public static native int openUSBLTD();

    public static native int closeUSBLTD();

    public static native int startUSBLTD();

    public static native int stopUSBLTD();

    public static native int readUSBLTD();

    public static native int readOneWave(short[] Bufs, int size);

    public static native int sendCommands(short[] Coms, short length);

}
