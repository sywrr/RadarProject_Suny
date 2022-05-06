package com.ltd.lifesearchapp.Detect;

class RadarData {
    static {
        System.loadLibrary("DetectRadarData");
    }

    private static native void memCopy(short[] dst, int dstIdx, byte[] src, int srcIdx, int length);

    private final short[] data;

    private int sampleLen = 0;

    private int ref = 0;

    private int size = 0;

    public static RadarData dummyData = new RadarData(0);

    public RadarData() {
        this(8192);
    }

    private RadarData(int cap) {
        data = cap > 0 ? new short[cap] : null;
    }

    private void incRef(int delta) {
        synchronized (this) {
            if (ref + delta - Integer.MAX_VALUE > 0)
                throw new IllegalArgumentException("ref overflow");
            if (ref + delta < 0)
                throw new IllegalArgumentException("ref + delta < 0");
            ref += delta;
        }
    }

    public void use(int usage) {
        incRef(usage);
    }

    public void drop() {
        incRef(-1);
    }

    public void setSampleLen(int len) {
        sampleLen = len;
    }

    public int getSampleLen() {
        return sampleLen;
    }

    public void clear() {
        synchronized (this) {
            if (ref > 0) {
                throw new IllegalStateException("radar data is in use");
            }
            size = 0;
        }
    }

    //    public void setData(short[] buf, int offset, int length) {
//        if (sampleLen != length)
//            throw new IllegalArgumentException("can only set one scan data");
//        if (offset < 0 || length < 0 || offset + length > buf.length)
//            throw new IllegalArgumentException("invalid buf range");
//        System.arraycopy(buf, offset, data, 0, length);
//        size = length;
//    }

//    private int[] scanValue = new int[2048*500];

    private int j =0;
    public static int  bytesTo32Int(byte[] ary, int offset) {
        int value;
        value = (int) ((ary[offset] & 0xFF)
                | ((ary[offset + 1] & 0xFF) << 8)
                | ((ary[offset + 2] & 0xFF) << 16)
                | ((ary[offset + 3] & 0xFF) << 24));
        return value;
    }

    public static int number =0;
    public void setData(byte[] buf, int offset, int length) {
        if ((length >> 2) != sampleLen)
            throw new IllegalArgumentException("can only set one scan data");
        if (offset < 0 || length < 0 || offset + length > buf.length)
            throw new IllegalArgumentException("invalid buf range");
        for (int i= offset ;i<buf.length ;i +=4){

//            if(number == 405){
//                if( i<offset+8192 ) {
//                    data[j] = (short) ((float) (bytesTo32Int(buf, i)) / (Math.pow(2, 24)) * (Math.pow(2, 16)));
//                    System.err.println("算法接收道数a："+number);
//                }else{
//                    break;
//                }
//            }
//            else {
            //判断转换长度是否为采样点*4
            if( i<offset+(length<<2) ) {
                data[j] = (short) ((float) (bytesTo32Int(buf, i)) / (Math.pow(2, 24)) * (Math.pow(2, 16)));
            }else{
                break;
            }
//            }
            j++;
        }
        data[0] =0;
        data[1] =0;
        data[2] =0;
        j=0;
        number++;

        //for(int k =0; k < scanValue.length ; k++){
        //scanValue[k] = scanValue[k]/2^24*2^16;
        //data [k] = (short) scanValue[k];
        //}
        //sunyan 2021.11.9 zhushi begin
        // memCopy(data, 0, buf, offset, length);
        //sunyan 2021.11.9 zhushi end
        size = sampleLen;
    }

    public short[] getData() {
        synchronized (this) {
            if (ref == 0) {
                throw new IllegalStateException("radar data may be not available");
            }
            return data;
        }
    }

    public int getSize() {
        return size;
    }

    public boolean isUseless() {
        synchronized (this) {
            return ref == 0;
        }
    }

    public boolean isDummy() {
        return data == null;
    }
}
