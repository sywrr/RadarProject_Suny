package com.ltd.lifesearchapp.Detect;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RadarDetect {
    static {
        System.loadLibrary("Detect");
        DetectUtil.setMultiMode(1);
    }

    // lock for data control
    private final Lock dataLock = new ReentrantLock(true);

    private final RadarDataPool pool = new RadarDataPool(10, dataLock);

//    private final BodyDetect bodyDetect = new BodyDetect(pool);

    /*
     * set breath detect timeout is 70s when use small window params
     */
    private final BreathDetect breathDetect = new BreathDetect(70000, pool);

    // if accept radar data
    private boolean acceptData = false;

    private final DetectParams params = new DetectParams();

    private final DetectRange range = new DetectRange();

    /**
     *
     * @param buf src byte array
     * @param offset offset of source array
     * @param length available data length
     * @param sampleLen radar scan length
     */
    private int num = 0;
    public final void pushData(byte[] buf, int offset, int length, int sampleLen) {
            dataLock.lock();
        try {
            if (length % sampleLen != 0)
                throw new IllegalArgumentException("length must be times of sampleLen");
            int totalSet = 0;

            while (acceptData && length > totalSet) {
                RadarData radarData = new RadarData();
                // increase 2 usage for body detect and breath detect
                radarData.use(1);
                radarData.setSampleLen(sampleLen);
                radarData.setData(buf, offset , sampleLen << 2);
//                bodyDetect.push(radarData);
                breathDetect.push(radarData);
                totalSet += (sampleLen << 2);
            }
        } finally {
            dataLock.unlock();
        }
    }

    public final void startDetect(DetectParams params, DetectRange range) {
        dataLock.lock();
        try {
            if (acceptData) {
                throw new IllegalStateException("radar detect has started");
            }
            acceptData = true;
            // set detect params and detect range to start
            if (this.params != params)
                this.params.set(params.get());
            if (this.range != range)
                this.range.set(range);
            //bodyDetect.start(this.params);
            breathDetect.start(this.params);
        } finally {
            dataLock.unlock();
        }
    }

    public final void stopDetect() {
        dataLock.lock();
        try {
            /*
             * stop accept radar data and wait
             * body and breath detect finished
             */
            if (acceptData) {
                acceptData = false;
//                bodyDetect.stop();
                breathDetect.stop();
//                bodyDetect.join();
                breathDetect.join();
            }
        } finally {
            dataLock.unlock();
        }
    }

    public final void shutdown() {
        dataLock.lock();
        try {
            if (acceptData) {
                acceptData = false;
//                bodyDetect.shutdown();
                breathDetect.shutdown();
            }
        } finally {
            dataLock.unlock();
        }
    }

    public RadarDetect() {
        breathDetect.setInterrupter(new BreathDetect.Interrupter() {
            @Override
            public void interrupt() {
                /*
                 * if current state is in small window
                 * and detect time is over 70s
                 * stop current breath detect
                 */
                RadarDetect.this.stopDetect();
            }

            @Override
            public void recover() {
                /*
                 * increase signal pos and start next breath detect
                 */
                DetectParams.Value v = params.get();
                v.signalPos += v.window;
                params.set(v);
                range.next();
                RadarDetect.this.startDetect(params, range);
            }
        });
    }

    public final void setHandlers(DetectResultHandler bodyHandler,
                                  DetectResultHandler breathHandler) {
        dataLock.lock();
        try {
            if (acceptData) {
                throw new IllegalStateException("radar detect has started");
            }
//            bodyDetect.setHandler(bodyHandler);
            breathDetect.setHandler(breathHandler);
        } finally {
            dataLock.unlock();
        }
    }

    public final void setAutoDetect(boolean auto) {
        dataLock.lock();
        try {
            if (acceptData) {
                throw new IllegalStateException("radar detect has started");
            }
            breathDetect.setAuto(auto);
        } finally {
            dataLock.unlock();
        }
    }
}
