package com.ltd.lifesearchapp;

import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractAtomicVariable<T> {

    private final AtomicReference<T> mRef = new AtomicReference<>(null);

    // 根据参数生成一个值
    protected abstract T copyValue(T convertValue, Object... args);

    // 判断已有值和给定的参数是否一致
    protected abstract boolean valueEquals(T comparedValue, Object... args);

    // 获取可覆盖的值对象，可用于缓存对象
    protected abstract T getConvertValue();

    public final void set(Object... args) {
        boolean copied = false;
        T oldValue, newValue = null;
        do {
            oldValue = mRef.get();
            if (valueEquals(oldValue, args))
                return;
            if (!copied) {
                newValue = copyValue(getConvertValue(), args);
                copied = true;
            }
        } while (!mRef.compareAndSet(oldValue, newValue));
    }

    public final T get() { return mRef.get(); }

}
