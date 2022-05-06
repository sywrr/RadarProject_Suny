package com.ltdpro;

import com.Connection.Packet;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DetectResultManager {

    private final DetectResultParser mParser = new DetectResultParser();

    private class ResultHandler {

        private int mTargetNum;

        private int mTargetThreshold;

        private final int mIndex;

        private class ResultNode {
            DetectResult mResult;
            ResultNode mNext;

            ResultNode() {
                mResult = new DetectResult();
                mNext = null;
            }
        }

        ResultNode mHeadNode, mTailNode;

        public ResultHandler(int maxSize, int targetThreshold, int index) {
            mTargetThreshold = targetThreshold;
            mHeadNode = mTailNode = null;
            if (maxSize > 1 && mTargetThreshold > 0) {
                mHeadNode = new ResultNode();
                mTailNode = mHeadNode;
                ResultNode resultNode;
                for (int i = 1; i < maxSize; i++) {
                    resultNode = new ResultNode();
                    mTailNode.mNext = resultNode;
                    mTailNode = resultNode;
                }
                mTailNode = mHeadNode;
            }
            mTargetNum = 0;
            mIndex = index;
        }

        private void dropFirstTarget() {
            ResultNode resultNode = mHeadNode;
            while (resultNode != null) {
                if (resultNode.mResult.existTarget()) {
                    resultNode.mResult.reset();
                    --mTargetNum;
                    break;
                }
                resultNode = resultNode.mNext;
            }
        }

        public DetectResult handle() {
            if (mHeadNode != null) {
                DetectResult detectResult = new DetectResult();
                if (mTailNode.mNext == null) {
                    ResultNode resultNode = mHeadNode;
                    if (resultNode.mNext != null)
                        mHeadNode = resultNode.mNext;
                    if (resultNode.mResult.existTarget()) {
                        --mTargetNum;
                    }
                    resultNode.mResult.reset();
                    resultNode.mNext = null;
                    if (mTailNode != resultNode) {
                        mTailNode.mNext = resultNode;
                        mTailNode = resultNode;
                    }
                } else {
                    mTailNode.mResult.reset();
                }
                mParser.setResult(mTailNode.mResult, mIndex);
                if (mTailNode.mResult.existTarget()) {
                    if (mTargetNum == mTargetThreshold) {
                        dropFirstTarget();
                    }
                    ++mTargetNum;
                    if (mTargetNum == mTargetThreshold) {
                        mTailNode.mResult.setFinalType();
                    } else {
                        mTailNode.mResult.setInterType();
                    }
                    detectResult.copy(mTailNode.mResult);
                } else {
                    mTailNode.mResult.setInterType();
                }
                if (mTailNode.mNext != null)
                    mTailNode = mTailNode.mNext;
                return detectResult;
            }
            return null;
        }

        public void resetHandler() {
            ResultNode resultNode = mHeadNode;
            while (resultNode != null) {
                resultNode.mResult.reset();
                resultNode = resultNode.mNext;
            }
            mTargetNum = 0;
            mTailNode = mHeadNode;
        }
    }

    private final ResultHandler[] mMoveHandlerList;
    private final ResultHandler[] mBreathHandlerList;

    private Lock mLock = new ReentrantLock(true);

    public static final class HandlerConfigure {

        private int mHandlers;
        private int mMaxResults;
        private int mTargetThreshold;

        public HandlerConfigure(int handlers, int maxResults, int targetThreshold) {
            if (handlers <= 0 || maxResults <= 0 || targetThreshold <= 0)
                throw new IllegalArgumentException("params can not be negative");
            mHandlers = handlers;
            mMaxResults = maxResults;
            mTargetThreshold = targetThreshold;
        }
    }

    public DetectResultManager(HandlerConfigure moveConfigure, HandlerConfigure breathConfigure) {
        mMoveHandlerList = new ResultHandler[moveConfigure.mHandlers];
        for (int i = 0; i < moveConfigure.mHandlers; i++) {
            mMoveHandlerList[i] = new ResultHandler(moveConfigure.mMaxResults,
                                                    moveConfigure.mTargetThreshold, i + 1);
        }
        mBreathHandlerList = new ResultHandler[breathConfigure.mHandlers];
        for (int j = 0; j < breathConfigure.mHandlers; j++) {
            mBreathHandlerList[j] = new ResultHandler(breathConfigure.mMaxResults,
                                                      breathConfigure.mTargetThreshold, j + 1);
        }
    }

    public Packet process(short[] result, int scans, int detectStart, int detectEnd) {
        mLock.lock();
        mParser.setOriginalResult(result);
        try {
            int nResults = mParser.isBreathResult()
                           ? mParser.breathResults()
                           : mParser.moveResults();
            if (nResults > 0) {
                DetectResult detectResult;
                ResultHandler[] handlerList = mParser.isBreathResult()
                                              ? mBreathHandlerList
                                              : mMoveHandlerList;
                Packet pack = new Packet(Global.PACKET_DETECT_RESULT, 6 * nResults + 8);
                pack.setPacketFlag(0xAAAABBBB);
                pack.putInt(scans);
                pack.putShort((short) detectStart);
                pack.putShort((short) detectEnd);
                for (int i = 0; i < nResults; i++) {
                    detectResult = handlerList[i].handle();
                    pack.putShort(detectResult.getResultType());
                    pack.putShort(detectResult.existTarget() ? (short) 1 : (short) 0);
                    pack.putShort(detectResult.getTargetPos());
                }
                return pack;
            }
            if (mParser.isBreathResult()) {
                Packet pack = new Packet(Global.DETECT_NO_BREATH_RESULT, 0);
                pack.setPacketFlag(0xAAAABBBB);
                return pack;
            }
            return null;
        } finally {
            mLock.unlock();
        }
    }

    public void resetManager() {
        for (ResultHandler resultHandler : mMoveHandlerList) {
            resultHandler.resetHandler();
        }
        for (ResultHandler resultHandler : mBreathHandlerList) {
            resultHandler.resetHandler();
        }
    }

}
