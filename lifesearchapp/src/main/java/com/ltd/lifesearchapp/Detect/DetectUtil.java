package com.ltd.lifesearchapp.Detect;

class DetectUtil {
    // detect body target and write detect result to result_data
    public static native void detect_body(short[] result_data);

    // init body detect, called on every detect progress
    public static native void init_body();

    // init breath detect, called on every detect progress
    public static native void init_breath();

    // set detect params for algorithm
    public static native void changeParams(int fscans, int antenna_type, int window, int ad_element_size);

    // pretreatment ad_frame_data for body detect
    public static native void detect_body_pre(short[] ad_frame_data);

    // pretreatment ad_frame_data for breath detect
    public static native void detect_breath_pre(short[] ad_frame_data);

    // detect breath target and write breath result to result_data
    public static native void detect_breath(short[] result_data, float breath_th);

    // set detect mode, multi mode if flag is 1, otherwise single target mode
    public static native void setMultiMode(int flag);

    // get detect mode
    public static native int getMultiMode();
}
