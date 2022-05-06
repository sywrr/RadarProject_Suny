package com.ltd.lifesearchapp.Detect;

interface DetectImpl {
    // detect init, call for initialization of every detect progress
    void init();

    // preDetect data of every detect progress, call before detect
    void preDetect(short[] data);

    // detect and write detect result to result
    void detect(short[] result);
}
