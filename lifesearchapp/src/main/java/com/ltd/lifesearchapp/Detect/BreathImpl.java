package com.ltd.lifesearchapp.Detect;

class BreathImpl implements DetectImpl {
    @Override
    public void init() {
        DetectUtil.init_breath();
        System.out.println("init_breath()");
    }

    @Override
    public void preDetect(short[] data) {
        DetectUtil.detect_breath_pre(data);
        System.out.println("detect_breath_pre()");
    }

    @Override
    public void detect(short[] result) {
        DetectUtil.detect_breath(result, 1);
        System.out.println("detect_breath()");
    }
}
