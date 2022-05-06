package com.ltd.lifesearchapp;

// 多线程下使用CAS更新变量
// 基于atomicReference实现，属于原子引用增强版

public class DetectParamsVariable extends AbstractAtomicVariable<_DetectParams> {

    @Override
    protected _DetectParams copyValue(_DetectParams convertValue, Object... args) {
        if (convertValue == null)
            convertValue = new _DetectParams();
        if (args.length == 1) {
            _DetectParams detectParams = (_DetectParams) args[0];
            convertValue.mDetectStart = detectParams.mDetectStart;
            convertValue.mDetectEnd = detectParams.mDetectEnd;
            convertValue.mDetectInterval = detectParams.mDetectInterval;
        } else if (args.length == 2) {
            convertValue.mDetectStart = (Integer) args[0];
            convertValue.mDetectEnd = (Integer) args[1];
            convertValue.mDetectInterval = get().mDetectInterval;
        } else if (args.length == 3) {
            convertValue.mDetectStart = (Integer) args[0];
            convertValue.mDetectEnd = (Integer) args[1];
            convertValue.mDetectInterval = (Integer) args[2];
        }
        return convertValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof _DetectParams) && !(obj instanceof DetectParamsVariable))
            return false;
        _DetectParams detectParams = get();
        _DetectParams comparedParams = obj instanceof DetectParamsVariable
                                      ? ((DetectParamsVariable) obj).get() : (_DetectParams) obj;
        return detectParams.mDetectStart == comparedParams.mDetectStart &&
               detectParams.mDetectEnd == comparedParams.mDetectEnd &&
               detectParams.mDetectInterval == comparedParams.mDetectInterval;
    }

    @Override
    protected boolean valueEquals(_DetectParams comparedValue, Object... args) {
        if (comparedValue == null)
            return false;
        switch (args.length) {
            case 1:
                if (!(args[0] instanceof _DetectParams))
                    throw new IllegalArgumentException();
                _DetectParams detectParams = (_DetectParams) args[0];
                return comparedValue.mDetectInterval == detectParams.mDetectInterval &&
                       comparedValue.mDetectStart == detectParams.mDetectStart &&
                       comparedValue.mDetectEnd == detectParams.mDetectEnd;
            case 2:
                if (!(args[0] instanceof Integer) || !(args[1] instanceof Integer))
                    throw new IllegalArgumentException();
                return comparedValue.mDetectStart == (Integer) args[0] &&
                       comparedValue.mDetectEnd == (Integer) args[1];
            case 3:
                if (!(args[0] instanceof Integer) || !(args[1] instanceof Integer) ||
                    !(args[2] instanceof Integer))
                    return false;
                return comparedValue.mDetectStart == (Integer) args[0] &&
                       comparedValue.mDetectEnd == (Integer) args[1] &&
                       comparedValue.mDetectInterval == (Integer) args[2];
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    protected _DetectParams getConvertValue() {
        return null;
    }
}
