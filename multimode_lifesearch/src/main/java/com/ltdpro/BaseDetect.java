package com.ltdpro;

public class BaseDetect {

    public static native void detect_body(short[] result_data);

    public static native void init_body();

    public static native void init_breath();

    public static native void changeParams(int fscans, int antenna_type, int window, int ad_element_size);

    public static native void detect_body_pre(short[] ad_frame_data);

    public static native void detect_breath_pre(short[] ad_frame_data);

    public static native void detect_breath(short[] result_data, float breath_th);

    public static native void setMultiMode(int flag);

    public static native int getMultiMode();

}
