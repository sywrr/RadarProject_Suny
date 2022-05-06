package com.ltd.lifesearchapp.Detect;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class RadarDataPool {
    private final Deque<RadarData> radarDataDeque;

    // max radar data size
    private final int cap;

    // radar data allocated
    private int allocCount = 0;

    // block threads
    private int waitValue = 0;

    private final Lock lock;

    private final Condition cond;

    public RadarDataPool(int cap, Lock lock) {
        if (cap < 0)
            throw new IllegalArgumentException("cap < 0");
        this.cap = cap;
        radarDataDeque = new ArrayDeque<>(cap);
        this.lock = lock == null ? new ReentrantLock(true) : lock;
        cond = this.lock.newCondition();
    }

    /**
     * alloc radar data
     * if pool has no available radar data, alloc and return
     * if allocated radar data exceed cap, wait other thread
     * to call recycle
     * @return
     */
    public RadarData alloc() {
        RadarData radarData = null;
        lock.lock();
        try {
            boolean loop = true;
            System.err.println("radarDataDeque:"+radarDataDeque.isEmpty());
            while (loop) {
                if (allocCount < cap || !radarDataDeque.isEmpty()) {
                    radarData = radarDataDeque.pollFirst();
                    System.err.println("radarDataDeque1:"+radarDataDeque.isEmpty());
                    // no available radar data, alloc
                    if (radarData == null) {
                        radarData = new RadarData();
                        ++allocCount;
                        System.err.println("allocCount:"+allocCount);
                    }
                    /*
                     * if now has available radar data,
                     * and other threads wait for radar data,
                     * notify them.
                     * this is for thundering herd
                     */
                    if (waitValue > 0 && !radarDataDeque.isEmpty()) {
                        cond.signal();
                        --waitValue;
                    }
                    loop = false;
                } else {
                    /*
                     * no available radar data and
                     * allocated radar data size is exceed
                     * wait for other threads to recycle
                     */
                    boolean complete = false;
                    try {
                        ++waitValue;
                        cond.await();
                        complete = true;
                    } catch (InterruptedException ignore) {
                    } finally {
                        if (!complete) {
                            --waitValue;
                            loop = false;
                        }
                    }
                }
            }
        } finally {
            lock.unlock();
        }
        return radarData;
    }
    /**
     * recycle radar data for alloc use
     */
    public void recycle(RadarData radarData) {
        lock.lock();
        try {
            radarData.clear();
            if (radarDataDeque.size() < cap) {
                radarDataDeque.addLast(radarData);
                if (waitValue > 0) {
                    cond.signal();
                    --waitValue;
                }
            }
            else {
                System.err.println("size:"+radarDataDeque.size());
                throw new IllegalStateException("pool is overflow");
            }
        } finally {
            lock.unlock();
        }
    }

    public int getPooledSize() {
        lock.lock();
        try {
            return radarDataDeque.size();
        } finally {
            lock.unlock();
        }
    }

    public int getAllocCount() {
        lock.lock();
        try {
            return allocCount;
        } finally {
            lock.unlock();
        }
    }
}
