package com.ltd.lifesearchapp;

import android.os.Environment;
import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DataCollection {
    private Thread collectThread;

    private volatile boolean terminate = true;

    private volatile int collectInterval = 100;

    private final String localIp;

    private final String deviceIp;

    private final int antenaType;

    private final  int dllFlag;
    private String params;
    private String mParams;
    private String lifeSearchFileName ;
    public void setFileName(String fileName){
        this.lifeSearchFileName= fileName;
    }
    //sunyan 2021.11.18 add begin

    //�����µ������ļ���
    public String createNewFileName() {
        return createNewFileName_ByIndex();
    }

    //���������������ļ���
    private int mNowFileindex = 0;    //�ļ�����
    private String mFraFileName ; //�����ļ���

    public void setmFraFileName(String mFraFileName) {
        this.mFraFileName = mFraFileName;
    }

    public String getmFraFileName() {
        return mFraFileName;
    }

    private String mLTEFilefolderPath="/LteFiles";
    public String createNewFileName_ByIndex() {
        String fileName;
        int index = 1;
        do {
            fileName = "/ltefile" + index + ".lte";
            mFraFileName = fileName;
            fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + mLTEFilefolderPath +
                    fileName;
            File file = new File(fileName);
            if (!file.exists())
                break;
            index++;
        } while (true);
        //
        mNowFileindex = index;
        return fileName;
    }
    //sunyan 2021.11.18 add end
    public String getFileName(){
        return lifeSearchFileName;
    }
    public interface DataHandler {
        void handleReceiveError(String err);

        void handleReceiveComplete(byte[] b, int offset, int prevIdx, int curIdx);
    }

    private final DataHandler dataHandler;

    public DataCollection(String localIp, String deviceIp,int antenaType,int dllFlag, DataHandler dataHandler) {
        this.localIp = localIp;
        this.deviceIp = deviceIp;
        this.dataHandler = dataHandler;
        this.antenaType =antenaType;
        this.dllFlag = dllFlag;
        this.initParams = null;
    }
    public synchronized void setParams(String par) {
        notify();
        this.params = par;
    }
    public synchronized void setmParams(String mParams) {
        notify();
        this.mParams = mParams;
    }
    //sunyan 2021.12.8 add begin
    private String initParams;
    public  String setInitParams(int initSignalPosition){
        try {
            initParams="{\"samplingPoints\":2048,\"scanSpeed\":32,\"timeWindow\":21,\"signalPosition\":22,\"signalGain\":[4,4,4,4,4,4,4,4,4],\"automaticGain\":0}";
            JSONObject parObj = new JSONObject(String.valueOf(initParams));
            parObj.put("signalPosition",initSignalPosition);
            this.initParams =parObj.toString();

        }catch (JSONException e){
            e.printStackTrace();
        }
        return initParams;
    }
    //sunyan 2021.12.8 add end
    //�趨�ɼ����
    public void setCollectInterval(int interval) {
        this.collectInterval = interval;
    }

    private final byte[] data = new byte[2048 * 4 * 500];
    //sunyan 2021.11.17 add begin
//    private String mParams ="{\"samplingPoints\":2048,\"scanSpeed\":32,\"timeWindow\":20,\"signalPosition\":22,\"signalGain\":5}";
    public void changeDeviceParams()  {
        try {
            JSONObject parObj = new JSONObject(String.valueOf(mParams));
            int signalPosition = parObj.getInt("signalPosition");
            int timeWindow = parObj.getInt("timeWindow");
            signalPosition += 20;
            parObj.put("signalPosition",signalPosition);
            this.mParams =parObj.toString();
            int num = Test.saveSetting(mParams, true);
            System.err.println("̽��ʱ������"+"breathDetect:"+"change ������"+"mParams:"+mParams);
//            System.err.println("param:"+mParams +"signalPosition:"+signalPosition+"timeWindow:"+timeWindow+"parObj"+parObj);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
    //sunyan 2021.11.17 add end

    private boolean state = false;

    public void setState(boolean state) {
        this.state = state;
    }
    public boolean getState() {
        return state;
    }
    private void collect() {
        boolean loop = true;
        for (; loop && !terminate; ) {
            System.out.println("create: " +Test.createInstance(0, localIp, deviceIp,antenaType,dllFlag));
            while (!terminate) {
                boolean complete = false;
                try {
                    state = Test.runningStatus();
                    complete = true;
                    if (state) {
                        System.err.println("running state true");
//                        setState(state);
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
//                    break; //������ɣ�����ѭ�������´�������
                }
            }
        }
        if (terminate) {
            return;
        }
        Test.start();
        //int num = Test.saveSetting("{\"samplingPoints\":2048,\"scanSpeed\":32,\"timeWindow\":20��\"signalPosition\":22,\"signalGain\":5}", true);
        setFileName(createNewFileName());
//        setmFraFileName(mFraFileName);
        try {
            int num = Test.saveSetting(params, true);
            Thread.sleep(500);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        int ret = Test.beginSaveData(getFileName(), 0);
        boolean initial = true;
        int prevIndex = -1;
        long time = System.nanoTime();
        try {
            while (!terminate) {
                try {
                    synchronized (this) {
                        long et = System.nanoTime();
                        long duration = (et - time) / 1000000;
                        if(duration>=69000){
//                            Test.saveSetting("{\"samplingPoints\":2048,\"scanSpeed\":32,\"timeWindow\":20��\"signalPosition\":42,\"signalGain\":5}", true);
                            int num1 =  Test.endSaveData();
                            try {
                                changeDeviceParams();
                                Thread.sleep(100);//�ı�����߳�����100ms
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
//                            int num1 = Test.saveSetting(params, true);
                            setFileName(createNewFileName());
                            int state = Test.beginSaveData(getFileName(),0);
//                            setmFraFileName(mFraFileName);
                            time =et;
                            System.err.println("̽��ʱ������"+"breathDetect:"+"�ɼ����ݲ����·���"+ " " +"num1:"+num1+ " " + "state:" +state);
                        }
                        state = Test.runningStatus();
                        setState(state);

                        System.err.println("�豸״̬��" + state);
                        JSONObject parObj = new JSONObject(String.valueOf(params));
                        int sampleLen = parObj.getInt("samplingPoints");
                        Pair<Integer, Integer> p = Test.receivedData(data);
                        if (p.second > 0) {
//                            System.err.println("�������ݵ�����" + p.second);
                            ByteBuffer buffer = ByteBuffer.wrap(data);
                            buffer.order(ByteOrder.LITTLE_ENDIAN);
                            int offset = 0;
                            while (offset < p.second) {
                                //ÿ������ʼλ��
                                int pos = buffer.position();
                                //����
                                int index = buffer.getInt(pos + 8);
                                if (initial) {
                                    initial = false;
                                    prevIndex = index;
                                }
                                //�ҵ���һ������ʼλ��
                                buffer.position(pos + sampleLen * 4);
                                ++offset;
                                dataHandler.handleReceiveComplete(data, pos, prevIndex, index);
                                prevIndex = index;
                            }
                        }
                        if (collectInterval > 0) {
                            try {
                                wait(collectInterval);
                            } catch (InterruptedException ignore) {

                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    dataHandler.handleReceiveError(Test.lastErrorString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            Test.releaseInstance();
        }
    }

    public  void shutdown() {
        synchronized (this) {
            if (!terminate) {
                terminate = true;
                collectThread.interrupt();
            }
        }
        for (; ; ) {
            try {
                collectThread.join();
                break;
            } catch (InterruptedException ignore) {
            }
        }
    }

    public synchronized void start() {
        if (terminate) {
            terminate = false;
            collectThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    collect();
                }
            });
            collectThread.start();
        }
    }
}
