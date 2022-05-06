package com.ltd.lifesearchapp.Detect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * processor for detect result
 * using DetectResultReader to parse detect result given by algorithm
 * and push processed detect result to pipeline
 */
class DetectResultProcessor {
    // reader for parse of detect result given by algorithm
    private final DetectResultReader reader = new MBMReader();

    private final List<DetectResult> detectResultList = new ArrayList<>(2);

    private final List<DetectResult> publicList = Collections.unmodifiableList(detectResultList);

    // detect pipeline to confirm whether detect result is inter or final result
    private final DetectResultPipeline[] pipelines;

    // if processor is processing move result
    private final boolean isMove;

    public List<DetectResult> process(short[] result) {
        // read original detect result given by algorithm
        reader.read(result);
        // clear previous detect results
        detectResultList.clear();
        // get results of move or breath
        int nResults = isMove ? reader.moveResults() : reader.breathResults();
        for (int i = 0; i < nResults; i++) {
            // get one detect result
            DetectResult dr = reader.getResult(isMove, i+1);
            if (dr == null)
                throw new NullPointerException("detect result should not be null");
            // push detect result to pipeline for last process
            dr = pipelines[i].push(dr);
            if (dr != null)
                detectResultList.add(dr);
        }
        return publicList;
    }

    static class Config {
        // max detect results of detect window
        public int maxResults;

        /*
         * if targets is >= threshold, last detect result
         * exists target will be set to final result
         */
        public int threshold;
    }

    public DetectResultProcessor(boolean isMove, int nResults, Config[] configs) {
        this.isMove = isMove;
        if (nResults < 1)
            throw new IllegalArgumentException("nResult must be >= 1");
        if (configs.length > 1 && configs.length != nResults)
            throw new IllegalArgumentException("can not match config");
        pipelines = new DetectResultPipeline[nResults];
        if (configs.length == 1) {
            for (int i = 0; i < pipelines.length; i++) {
                pipelines[i] = new DetectResultPipeline(configs[0].maxResults,
                                                        configs[0].threshold);
            }
        } else {
            for (int i = 0; i < pipelines.length; i++) {
                pipelines[i] = new DetectResultPipeline(configs[i].maxResults,
                                                        configs[i].threshold);
            }
        }
    }
}
