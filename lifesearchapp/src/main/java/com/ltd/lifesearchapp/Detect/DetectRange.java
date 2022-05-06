package com.ltd.lifesearchapp.Detect;

public class DetectRange {
    public static class Value {
        public int start;

        public int end;
    }

    private int rangeStart;

    private int rangeEnd;

    private int interval;

    private int cur;

    public void set(int rs, int re, int interval) {
        rangeStart = rs;
        rangeEnd = re;
        this.interval = interval;
        cur = rs;
    }

    public void set(DetectRange range) {
        this.rangeStart = range.rangeStart;
        rangeEnd = range.rangeEnd;
        interval = range.interval;
        cur = range.cur;
    }

    public Value get() {
        Value v = new Value();
        v.start = cur;
        v.end = cur + interval;
        return v;
    }

    public void next() {
        cur += interval;
    }

    public Value getInitialValue() {
        Value v = new Value();
        v.start = rangeStart;
        v.end = rangeEnd;
        return v;
    }
}
