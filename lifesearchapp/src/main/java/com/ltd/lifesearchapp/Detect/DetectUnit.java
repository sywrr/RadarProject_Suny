package com.ltd.lifesearchapp.Detect;

import com.ltd.lifesearchapp.Blocker;
import com.ltd.lifesearchapp.Test;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class DetectUnit {
    private Thread workThread = null;

    private final DetectImpl impl;

    private final BlockingQueue<RadarData> radarDataQueue = new LinkedBlockingQueue<>();

    private DetectResultHandler handler = null;

    public final void setHandler(DetectResultHandler handler) {
        this.handler = handler;
    }

    // handler for process detect result given by algorithm
    protected final DetectResultProcessor processor;

    private final short[] detectResult = new short[20];

    private final Lock detectLock = new ReentrantLock(true);

    // blocker for pause of work thread
    private final Blocker detectBlocker = new Blocker(detectLock);

    // if detect unit is stopping
    private volatile boolean stopping = false;

    private volatile boolean terminate = false;

    // radar data poll for object recycle and allocation
    private final RadarDataPool pool;

    private long startTimeStamp = 0;

    public final void push(RadarData radarData) {
        if (radarData != null) {
            radarDataQueue.add(radarData);
        }
    }

    protected final void sendParams(DetectParams.Value v) {
        String sb = "send params: " + v.scanSpeed + ", " + v.antennaType + ", " + v.window + ", " +
                    v.signalPos;
        System.out.println(sb);
    }
    public void start(DetectParams params) {
        detectLock.lock();
        try {
            DetectParams.Value v = params.get();
            // send params, usually send to device
            // sendParams(v);
            // changeDeviceParams();
            // set params to algorithm
            DetectUtil.changeParams(v.scanSpeed, v.antennaType, v.window, v.sampleLen);
            System.err.println("探测时间间隔："+"算法信号位置参数："+v.signalPos);
            // init timestamp for breath detect to statistics detect interval
            startTimeStamp = System.nanoTime();
            // init every detect progress
            impl.init();
            if (workThread == null || !workThread.isAlive()) {
                terminate = false;
                workThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        execLoop();
                    }
                },"探测线程");
                workThread.start();
            }
        } finally {
            detectLock.unlock();
        }
    }

    public final void stop() {
        detectLock.lock();
        try {
            if (!stopping) {
                stopping = true;
                // add dummy data to send signal to work thread
                // stop often invoked with join
                // do not push radar data after stop
                // otherwise, join is useless
                radarDataQueue.add(RadarData.dummyData);
            }
        } finally {
            detectLock.unlock();
        }
    }

    public final void join() {
        detectLock.lock();
        try {
            if (stopping) {
                // if detect unit is stopping, block current thread
                // util work thread send signal to current thread
                detectBlocker.block();
                stopping = false;
            }
        } finally {
            detectLock.unlock();
        }
    }

    // shutdown work thread to completely stop detect progress
    public final void shutdown() {
        detectLock.lock();
        try {
            if (workThread != null) {
                // send block signal to work thread
                stop();
                /**
                 * join util detect unit to process all radar data
                 */
                join();
                terminate = true;
                workThread.interrupt();
                while (workThread.isAlive()) {
                    try {
                        workThread.join();
                        break;
                    } catch (InterruptedException ignore) {}
                }
                workThread = null;
            }
        } finally {
            detectLock.unlock();
        }
    }

    // invoked after every detect progress
    // usually use in breath to handle detect timeout
    protected void onLoopFinished() {}
     DetectResult mDetectResult =new DetectResult();
    private int number = 0;
    private void implDetect(RadarData radarData) {
        Arrays.fill(detectResult, (short) 0);
        // pretreatment radar data
        impl.preDetect(radarData.getData());
        // process detect and write result to detectResult
        impl.detect(detectResult);
        // decrease usage of radar data
        radarData.drop();
        // if radar data is not using by any thread
        if (radarData.isUseless()) {
            // recycle radar data
//            pool.recycle(radarData);

            radarData = null;
            System.gc();
        }
        if (handler != null) {
            // handle detect result
            handler.handle(processor.process(detectResult));
            mDetectResult.reset();
        }
    }

    private void processRadarData(RadarData radarData) {
        if (radarData.isDummy()) {
            // if receiving dummy radar data
            // send signal to external thread to
            // stop block
            detectLock.lock();
            try {
                stopping = false;
                detectBlocker.unblock();
            } finally {
                detectLock.unlock();
            }
        } else {
            implDetect(radarData);
        }
    }

    private void execLoop() {
        RadarData radarData;
        startTimeStamp = System.nanoTime();
        while (!terminate) {
            radarData = null;
            try {
                // get radar data from queue
                radarData = radarDataQueue.poll(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // radarData == null only for interrupt or no radar data pushed
            if (radarData != null) {
                processRadarData(radarData);
                onLoopFinished();
            }
        }
    }

    protected final long getStartTimeStamp() {
        return startTimeStamp;
    }

    public DetectUnit(boolean isMove, DetectImpl impl, RadarDataPool pool) {
        this.impl = impl;
        this.pool = pool;
        int nResults = isMove ? 2 : 1;
        DetectResultProcessor.Config[] configs = new DetectResultProcessor.Config[1];
        configs[0] = new DetectResultProcessor.Config();
        configs[0].maxResults = 5;
        configs[0].threshold = 3;
        processor = new DetectResultProcessor(isMove, nResults, configs);
    }
}
