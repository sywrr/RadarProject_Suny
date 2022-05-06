package Connection;

/*
 * call when packet is received
 */
public interface PacketCallback {
    void invoke(Packet pack);
}
