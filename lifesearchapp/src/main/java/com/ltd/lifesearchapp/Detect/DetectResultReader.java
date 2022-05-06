package com.ltd.lifesearchapp.Detect;

public interface DetectResultReader {
    // read original detect result given by algorithm
    void read(short[] result);

    // all move results exists target
    int moveResults();

    // all breath results exists target
    int breathResults();

    // get idx th detect result
    DetectResult getResult(boolean isMove, int idx);
}
