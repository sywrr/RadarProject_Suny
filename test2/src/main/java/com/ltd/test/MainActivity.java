package com.ltd.test;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

//import pers.xuyan.scl.bits.HeapLittle;

public class MainActivity extends AppCompatActivity {
    //生成新的数据文件名
    //根据索引生成新文件名
    private int mNowFileindex = 0;          //文件索引
    public String fileName;
    private String mLTEFilefolderPath = "/SERLteFiles";
    private String ip = null;
    private short devType=0x0116;//设备类型
    private String cardSerialNum = null;
    private short deviceSerialNum=0x002;//设备序列号
    private short versionNum=0x1411 ;//硬件版本+软件版本
    private short calibrationValue=(short) 0x02e4;//校准值
    private short antennaCode=(short) 0xc9A6;
    private short frqValue=(short) 0x0186;
    private int serialNum = 6;


    public void loop() {
        boolean loop = true;
        byte[] a1 = new byte[6];
        a1[0] = (byte) 0x77;
        a1[1] = (byte) 0x76;
        a1[2] = (byte) 0x33;
        a1[3] = (byte) 0x44;
        a1[4] = (byte) 0x55;
        a1[5] = (byte) 0x66;
        char [] c1= new char[a1.length];
        for (int i = 0; i < a1.length; i++) {
            c1[i] = (char)a1[i];
//            System.err.println("test:"+c1[i]);
        }
        String output2 = String.valueOf(c1); // valueOf 方法
        for (; loop; ) {
            System.out.println("create: " + Test.createInstance(0, "192.168.0.112", "192.168.0.10", 2, 4));
            for (; ; ) {
                boolean complete = false;
                try {
                    boolean state = Test.runningStatus();
                    complete = true;
                    if (state) {
                        System.err.println("running state true");
                        int ret = Test.lowerComputerConfig("10.0.168.192",devType,output2,serialNum, deviceSerialNum, versionNum, calibrationValue,antennaCode,frqValue);
                        System.err.println("ret111:"+ret);
//                        int ret1 = Test.saveSetting("{\"samplingPoints\":2048,\"scanSpeed\":32,\"timeWindow\":21,\"signalPosition\":22,\"signalGain\":[4,4,4,4,4,4,4,4,4],\"automaticGain\":0}", false);
//                        System.err.println("ret1："+ret1);
//                        int ret = Test.beginSaveData(createNewFileName(),0);
//                        System.err.println( "fileName:"+createNewFileName()+"ret:"+ret);

                        loop = false;
                        break;
                    } else {
                        System.err.println("running state false");
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                } finally {
                    if (!complete) {
                        Test.releaseInstance();
                        System.err.println("release");
                    }
                }
            }
        }
//        deviceConfig();
//        int ret = Test.lowerComputerConfig("20.0.168.192",devType,"123456", deviceSerialNum, versionNum, calibrationValue);
//        System.err.println("ret111:"+ret);
//        Test.start();
        //sunyan 2022.1.7 zhushi begin
        //        byte[] data = new byte[2048 * 4 * 500];
        //        boolean initial = true;
        //        int prevIndex = -1;
        //        long st = System.currentTimeMillis();
        //        int sampleLen = 2048;
        //sunyan 2022.1.7 zhushi end
        try {
//            boolean state = Test.runningStatus();
//            System.err.println("设备状态：" + state);

            while (true) {
                System.err.println("设备状态：" + Test.runningStatus());
                //sunyan 2022.1.7 zhushi begin
//                Pair<Integer, Integer> p = Test.receivedData(data);
                //sunyan 2022.1.7 zhushi end
//                Pair<Integer, Integer> p = new Pair<>(1, 2);
//                System.err.println("error:"+Test.receivedData(data));
                //sunyan 2022.1.7 zhushi begin
//                int step = p.first;
//                if (p.second > 0) {
//                    System.err.println("接收数据道数：" + p.second);
//                    ByteBuffer buffer = ByteBuffer.wrap(data);
//                    buffer.order(ByteOrder.LITTLE_ENDIAN);
//                    int offset = 0;
//                    while (offset < p.second) {
//                        int pos = buffer.position();
//                        int index = buffer.getInt(pos + 8);
//                        if (!initial) {
//                            if (index - prevIndex != 1) {
//                                System.err.println("不是连续的数据：" + index + ", " + prevIndex);
//                            }
//                        } else {
//                            initial = false;
//                        }
//                        prevIndex = index;
//                        buffer.position(pos + sampleLen * 4);
//                        ++offset;
//                    }
//                }

                //sunyan 2022.1.7 zhushi end
                //sunyan 2022.1.7 add begin
//                boolean isEnd = Test.receivedRescueResult();
//                System.err.println("isEnd:"+isEnd);
                //sunyan 2022.1.7 add end
//                if ((System.currentTimeMillis() - st) >= 10000) {
////                    st = System.currentTimeMillis();
////                    sampleLen = sampleLen == 512 ? 256 : sampleLen == 256 ? 128 : 512;
////                    System.err.println("修改采样点数: " + sampleLen);
////                    JSONObject jsonObject = new JSONObject();
////                    try {
////                        jsonObject.put("samplingPoints", sampleLen);
////                        Test.saveSetting(jsonObject.toString(), true);
////                    } catch (JSONException e) {
////                        e.printStackTrace();
////                    }
////                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void test2700Interface() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                loop();
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        test2700Interface();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
