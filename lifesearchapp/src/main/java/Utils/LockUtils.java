package Utils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class LockUtils {

    public static Pair<Boolean, Long> lockInterruptibly(Lock lock, long timeout)
            throws InterruptedException {
        if (timeout == 0) {
            return new Pair<>(lock.tryLock(), (long) 0);
        }
        if (timeout < 0) {
            lock.lockInterruptibly();
            return new Pair<>(true, timeout);
        }
        long st = System.nanoTime();
        boolean flag = lock.tryLock(timeout, TimeUnit.MILLISECONDS);
        long leftTimeout;
        if (!flag) {
            leftTimeout = 0;
        } else {
            long costTime = (System.nanoTime() - st) / 1000000;
            leftTimeout = timeout > costTime ? timeout - costTime : 0;
        }
        return new Pair<>(true, leftTimeout);
    }

    public static Pair<Boolean, Long> lockUninterruptibly(Lock lock, long timeout) {
        if (timeout == 0) {
            return new Pair<>(lock.tryLock(), (long) 0);
        }
        if (timeout < 0) {
            lock.lock();
            return new Pair<>(true, timeout);
        }
        long leftTimeout = timeout;
        boolean flag;
        long st;
        while (leftTimeout > 0) {
            st = System.nanoTime();
            try {
                flag = lock.tryLock(leftTimeout, TimeUnit.MILLISECONDS);
                if (!flag) {
                    leftTimeout = 0;
                } else {
                    leftTimeout -= (System.nanoTime() - st) / 1000000;
                    if (leftTimeout < 0)
                        leftTimeout = 0;
                }
                return new Pair<>(flag, leftTimeout);
            } catch (InterruptedException ignore) {
                leftTimeout -= (System.nanoTime() - st) / 1000000;
            }
        }
        if (leftTimeout < 0)
            leftTimeout = 0;
        return new Pair<>(false, leftTimeout);
    }

    public static Pair<Boolean, Long> waitInterruptibly(Condition cond, long timeout) throws InterruptedException {
        if (timeout == 0)
            return new Pair<>(false, (long) 0);
        if (timeout < 0) {
            cond.await();
            return new Pair<>(true, timeout);
        }
        long leftTimeout = timeout;
        boolean flag;
        long st = System.nanoTime();
        flag = cond.await(leftTimeout, TimeUnit.MILLISECONDS);
        if (!flag) {
            leftTimeout = 0;
        } else {
            leftTimeout -= (System.nanoTime() - st) / 1000000;
            if (leftTimeout < 0)
                leftTimeout = 0;
        }
        return new Pair<>(flag, leftTimeout);
    }

    public static Pair<Boolean, Long> waitUninterruptibly(Condition cond, long timeout) {
        if (timeout == 0)
            return new Pair<>(false, (long) 0);
        if (timeout < 0) {
            cond.awaitUninterruptibly();
            return new Pair<>(true, timeout);
        }
        long leftTimeout = timeout;
        boolean flag = false;
        long st;
        while (leftTimeout > 0) {
            st = System.nanoTime();
            try {
                flag = cond.await(leftTimeout, TimeUnit.MILLISECONDS);
                if (!flag) {
                    leftTimeout = 0;
                } else {
                    leftTimeout -= (System.nanoTime() - st) / 1000000;
                    if (leftTimeout < 0)
                        leftTimeout = 0;
                }
                break;
            } catch (InterruptedException ignored) {
                leftTimeout -= (System.nanoTime() - st) / 1000000;
            }
        }
        if (leftTimeout < 0)
            leftTimeout = 0;
        return new Pair<>(flag, leftTimeout);
    }

}
