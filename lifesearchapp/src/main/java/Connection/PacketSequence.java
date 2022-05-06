package Connection;

public interface PacketSequence {
    Packet get();

    void put(Packet pack);

    void lock();

    void unlock();

    void clear();

    boolean isLocked();

    boolean isEmpty();

    int size();
}
