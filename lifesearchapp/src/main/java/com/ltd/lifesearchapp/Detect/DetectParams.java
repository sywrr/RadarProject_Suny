package com.ltd.lifesearchapp.Detect;

import org.json.JSONException;
import org.json.JSONObject;

public class DetectParams {
    public static class Value {
        public int scanSpeed;

        public int antennaType;

        public int window;

        public int sampleLen;

        public int signalPos;

        public void assign(Value v) {
            scanSpeed = v.scanSpeed;
            antennaType = v.antennaType;
            window = v.window;
            sampleLen = v.sampleLen;
            signalPos = v.signalPos;
        }
    }

    private Value v = new Value();

    private Value v2 = new Value();

    public void set(int sp, int antenna, int window, int sampleLen, int signal) {
        Value oldValue = v;
        v2.scanSpeed = sp;
        v2.antennaType = antenna;
        v2.sampleLen = sampleLen;
        v2.window = window;
        v2.signalPos = signal;
        v = v2;
        v2 = oldValue;
    }

    public void set(Value v) {
        this.v = v;
    }

    public Value get() {
        v2.assign(v);
        return v2;
    }

}
