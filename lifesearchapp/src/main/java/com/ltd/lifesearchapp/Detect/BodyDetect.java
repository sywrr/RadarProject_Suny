package com.ltd.lifesearchapp.Detect;

class BodyDetect extends DetectUnit {
    public BodyDetect(RadarDataPool pool) {
        super(true, new BodyImpl(), pool);
    }
}
