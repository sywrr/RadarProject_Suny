package Utils;

public class Pair<T, U> {
    private T mFirst;

    private U mSecond;

    public Pair(T first, U second) {
        mFirst = first;
        mSecond = second;
    }

    public T getFirst() {
        return mFirst;
    }

    public U getSecond() {
        return mSecond;
    }
}
