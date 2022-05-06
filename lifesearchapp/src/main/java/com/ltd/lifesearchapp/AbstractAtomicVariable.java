package com.ltd.lifesearchapp;

import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractAtomicVariable<T> {

    private final AtomicReference<T> mRef = new AtomicReference<>(null);

    // ���ݲ�������һ��ֵ
    protected abstract T copyValue(T convertValue, Object... args);

    // �ж�����ֵ�͸����Ĳ����Ƿ�һ��
    protected abstract boolean valueEquals(T comparedValue, Object... args);

    // ��ȡ�ɸ��ǵ�ֵ���󣬿����ڻ������
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
