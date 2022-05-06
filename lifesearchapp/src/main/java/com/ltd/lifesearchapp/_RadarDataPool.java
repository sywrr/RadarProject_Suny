package com.ltd.lifesearchapp;

import android.util.Log;

import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public final class _RadarDataPool {

    private static class Pool {
        private final Queue<_RadarData> mFreeQueue = new ConcurrentLinkedQueue<>();

        private final int mLength;

        private final AtomicInteger mAllocatedBlocks = new AtomicInteger(0);

        private boolean increaseAllocBlocks(int min, int max, int delta) {
            if (delta == 0)
                return true;
            if (min < 0 || max < 0)
                throw new IllegalArgumentException("min or max can not be negative");
            if (min >= max)
                throw new IllegalArgumentException(
                        "minimum value must be smaller than maximum value");
            int allocBlocks;
            do {
                allocBlocks = mAllocatedBlocks.get();
                if (delta > max - allocBlocks || delta < min - allocBlocks)
                    return false;
            } while (!mAllocatedBlocks.compareAndSet(allocBlocks, allocBlocks + delta));
            return true;
        }

        public Pool(int length) { mLength = length; }

        public final void recycle(_RadarData radarData) {
            radarData.clear();
            mFreeQueue.add(radarData);
        }

        public final void shrinkPool(int maxSize, int targetSize) {
            if (targetSize < 0 || targetSize > maxSize)
                throw new IllegalArgumentException(
                        "illegal argument: " + maxSize + ", " + targetSize);
            if (targetSize == maxSize)
                return;
            _RadarData radarData;
            if (mFreeQueue.size() > maxSize) {
                while (mFreeQueue.size() > targetSize) {
                    if ((radarData = mFreeQueue.poll()) == null)
                        break;
                    if (!increaseAllocBlocks(0, Integer.MAX_VALUE, -1)) {
                        mFreeQueue.add(radarData);
                        break;
                    }
                }
            }
        }

        public final _RadarData alloc() {
            _RadarData radarData = mFreeQueue.poll();
            if (radarData == null) {
                if (!increaseAllocBlocks(0, Integer.MAX_VALUE, 1))
                    throw new OutOfMemoryError("can not alloc any blocks");
                return new _RadarData(mLength);
            }
            return radarData;
        }
    }

    private final Map<Integer, Pool> mPoolMap = new TreeMap<>();

    private Pool getPool(int length) {
        Pool pool = mPoolMap.get(length);
        if (pool == null) {
            pool = new Pool(length);
            mPoolMap.put(length, pool);
        }
        return pool;
    }

    public final _RadarData allocRadarData(int length) {
        Pool pool = getPool(length);
        Log.d("RadarDataQueue", "alloc blocks: " + allocBlocks() + ", " + pool.mFreeQueue.size());
        return pool.alloc();
    }

    public final void recycle(_RadarData radarData) {
        getPool(radarData.length()).recycle(radarData);
    }

    public final void shrink(int maxSize, int targetSize) {
        for (Pool pool : mPoolMap.values())
            pool.shrinkPool(maxSize, targetSize);
    }

    public final int allocBlocks() {
        int sum = 0;
        for (Pool pool : mPoolMap.values())
            sum += pool.mAllocatedBlocks.get();
        return sum;
    }

    public final int getFreeSize() {
        int freeSum = 0;
        for (Pool pool : mPoolMap.values())
            freeSum += pool.mFreeQueue.size();
        return freeSum;
    }

}
