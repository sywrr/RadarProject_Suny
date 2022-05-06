package com.ltd.lifesearchapp.Detect;

import android.os.Environment;

import com.ltd.lifesearchapp.CreateFileName;
import com.ltd.lifesearchapp.DataCollection;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/*
 * util for statistics current targets
 * of all detect results stored
 * using ring buffer for implementation of detect window
 */
class DetectResultPipeline {
    private final int threshold;

    private final DetectResult[] detectResults;

    private int first = 0;

    private int last = 0;

    private int targets = 0;
    private MBMReader mbmReader =new MBMReader();
    public DetectResultPipeline(int maxResults, int threshold) {
        this.threshold = threshold;
        detectResults = new DetectResult[maxResults + 1];
        for (int i = 0; i < detectResults.length; i++)
            detectResults[i] = new DetectResult();
    }

    private int next(int idx) {
        return (idx + 1) % detectResults.length;
    }

    private int size() {
        return last >= first ? last - first : detectResults.length - first + last + 1;
    }

    private boolean isFull() {
        return size() == detectResults.length - 1;
    }

    public DetectResult push(DetectResult detectResult) {

        /**
         * if current detect buffer is full
         * drop first detect result
         */
        DetectResult firstResult = detectResults[first];
        if (isFull()) {
            first = next(first);
            // if first result exists target
            // decrease targets
            if (firstResult.existTarget()) {
//                firstResult.reset();//add
                --targets;
                System.err.println("Ì½²âenter 3:"+targets);
            }
        }
        firstResult.reset();
        DetectResult dr = null;
        if (detectResult.existTarget()) {
            ++targets;
            System.err.println("Ì½²âenter 4:"+targets);
            dr = detectResults[last];
            dr.setTargetPos(detectResult.getTargetPos());
            // if targets of current detect buffer == threshold
            // set passing detect result to final detect result
            // otherwise, set it to inter detect result
            if (targets == threshold ) {
                    dr.setFinalType();
            } else {
                    dr.setInterType();
            }
        }
        last = next(last);
        return dr;
    }

    // clear all detect result record
    public void clear() {
        int cur = first;
        while (cur != last) {
            detectResults[cur].reset();
            cur = next(cur);
        }
        first = last = 0;
        targets = 0;
    }
}
