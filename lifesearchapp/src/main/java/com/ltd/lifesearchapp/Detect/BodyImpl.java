package com.ltd.lifesearchapp.Detect;

class BodyImpl implements DetectImpl {

    @Override
    public void init() {
        DetectUtil.init_body();
        System.out.println("init_body()");
    }

    @Override
    public void preDetect(short[] data) {
        DetectUtil.detect_body_pre(data);
        System.out.println("detect_body_pre()");
    }

    @Override
    public void detect(short[] result) {
        DetectUtil.detect_body(result);
        System.out.println("detect_body()");
    }
}
