package Connection;

import Utils.Utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * packet for head-body structure
 * packet head is 12 bytes
 */
public class Packet {
    // packet type
    private int mPacketType;

    // packet length
    private int mPacketLength;

    // packet data array
    private byte[] mData;

    // array for packet head processing
    private final byte[] mTmpBytes = new byte[12];

    // current read-write position
    private int mIndex;

    // max bytes in one packet read-write progress
    private final static int mThreshold = 1024;

    private int mPacketFlag;

    public final static int OK = 1;
    public final static int TIMEOUT = 2;
    public final static int ERROR = 3;

    public Packet() {
        mIndex = 0;
        mData = null;
        mPacketFlag = 0;
    }

    public Packet(int packetType, int packetLength) {
        mPacketType = packetType;
        mPacketLength = packetLength;
        mIndex = 0;
        mPacketFlag = 0;
        createBuf(packetLength);
    }

    public void setPacketFlag(int packetFlag) {
        mPacketFlag = packetFlag;
    }

    public int getPacketType() {
        return mPacketType;
    }

    public int getPacketLength() {
        return mPacketLength;
    }

    public int getIndex() {
        return mIndex;
    }

    public void seek(int index) {
        this.mIndex = index;
    }

    // fill data from b to packet array
    public void fillData(byte[] b) {
        if (b != null && b.length > 0)
//            Utils.byteCopy(mData, 0, b, 0, b.length);
            System.arraycopy(b, 0, mData, 0, b.length);
    }

    // get Packet copy
    public Packet copy() {
        Packet pack = new Packet(mPacketType, mPacketLength);
        pack.setPacketFlag(mPacketFlag);
        pack.fillData(mData);
        return pack;
    }

    public void createBuf(int packetLength) {
        if (packetLength > 0) {
            mData = new byte[packetLength];
        }
    }

    /**
     * get data from index
     * @param index bytes position
     * @param size data size
     * @return byte, char, short or integer
     */
    private int getData(int index, int size) {
        int data = 0;
        for (int i = 0; i < size; i++) {
            data |= ((mData[index++] & 0xff) << (8 * i));
        }
        return data;
    }

    // get data from current index
    private int getData(int size) {
        int data = 0;
        for (int i = 0; i < size; i++) {
            data |= ((mData[mIndex++] & 0xff) << (8 * i));
        }
        return data;
    }

    /**
     *
     * @param index put position
     * @param size data size
     * @param data byte, char, short, int
     */
    private void putData(int index, int size, int data) {
        for (int i = 0; i < size; i++) {
            mData[index++] = (byte) (data & 0xff);
            data >>= 8;
        }
    }

    // put data at current index
    private void putData(int size, int data) {
        for (int i = 0; i < size; i++) {
            mData[mIndex++] = (byte) (data & 0xff);
            data >>= 8;
        }
    }

    // put int at current index
    public void putInt(int data) {
        putData(4, data);
    }

    // get int at current index
    public int getInt() {
        return getData(4);
    }

    // get int at index
    public int getInt(int index) {
        return getData(index, 4);
    }

    public boolean canGetInt() {
        return mData != null && mIndex + 4 <= mData.length;
    }

    // put short at current index
    public void putShort(short data) {
        putData(2, data);
    }

    // put short at index
    public void putShort(int index, short data) { putData(index, 2, data); }

    // get short from current index
    public short getShort() {
        return (short) getData(2);
    }

    // get short at index
    public short getShort(int index) {
        return (short) getData(index, 2);
    }

    public boolean canGetShort() {
        return mData != null && mIndex + 2 <= mData.length;
    }

    // put byte at current index
    public void putByte(byte data) {
        mData[mIndex++] = data;
    }

    // get byte at current index
    public byte getByte() {
        return mData[mIndex++];
    }

    // get byte at index
    public byte getByte(int index) {
        return mData[index];
    }

    public boolean canGetByte() {
        return mData != null && mIndex < mData.length;
    }

    /**
     *
     * @param os output stream for send
     * @param b source byte array
     * @param totalBytes total send bytes
     * @return if send successfully
     */
    private static boolean send(OutputStream os, byte[] b, int totalBytes) {
        int sendBytes = 0;
        int offset = 0;
        try {
            while (totalBytes > 0) {
                sendBytes = Math.min(totalBytes, mThreshold);
                os.write(b, offset, sendBytes);
                totalBytes -= sendBytes;
                offset += sendBytes;
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean sendPacket(OutputStream os) {
        // write packet flag, type, length to packet head
        Utils.intToBytes(mPacketFlag, mTmpBytes, 0);
        Utils.intToBytes(mPacketType, mTmpBytes, 4);
        int bytesToSend = Math.min(mIndex, mPacketLength);
        Utils.intToBytes(bytesToSend, mTmpBytes, 8);
        // send packet head
        if (!send(os, mTmpBytes, 12))
            return false;
        // send packet body
        return send(os, mData, bytesToSend);
    }

    /**
     *
     * @param is input stream for receive
     * @param b dest byte array
     * @param totalBytes total receive bytes
     * @return if receive successfully
     */
    private static boolean receive(InputStream is, byte[] b, int totalBytes) {
        int result;
        int recvBytes = 0;
        int offset = 0;
        try {
            while (totalBytes > 0) {
                recvBytes = Math.min(totalBytes, mThreshold);
                result = is.read(b, offset, recvBytes);
                if (result == -1)
                    return false;
                totalBytes -= result;
                offset += result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean receivePacket(InputStream is) {
        // receive packet head
        if (!receive(is, mTmpBytes, 12))
            return false;
        // parse packet flag, type, and length
        int packetFlag = Utils.bytesToInt(mTmpBytes, 0);
        if (packetFlag != mPacketFlag)
            return false;
        mPacketType = Utils.bytesToInt(mTmpBytes, 4);
        mPacketLength = Utils.bytesToInt(mTmpBytes, 8);
        // create packet buffer
        createBuf(mPacketLength);
        // receive packet body
        return receive(is, mData, mPacketLength);
    }

    /**
     *
     * @param pack compared packet
     * @param is input stream for receive
     * @return receive packet successfully and equals to pack
     */
    public boolean receiveAsPacket(Packet pack, InputStream is) {
        if (mPacketFlag != pack.mPacketFlag)
            return false;
        // receive packet head
        if (!receive(is, mTmpBytes, 12))
            return false;
        // parse packet head for flag, type, length
        int packetFlag = Utils.bytesToInt(mTmpBytes, 0);
        if (packetFlag != mPacketFlag)
            return false;
        mPacketType = Utils.bytesToInt(mTmpBytes, 4);
        mPacketLength = Utils.bytesToInt(mTmpBytes, 8);
        if (mPacketLength > 0) {
            // create packet buffer and receive packet body
            createBuf(mPacketLength);
            if (!receive(is, mData, mPacketLength))
                return false;
            // compare two packets
            return Arrays.equals(mData, pack.mData);
        }
        return true;
    }

    public boolean equals(Packet pack) {
        // compare packet head between pack and current packet
        if (mPacketFlag != pack.mPacketFlag)
            return false;
        if (mPacketType != pack.mPacketType || mPacketLength != pack.mPacketLength) {
            return false;
        }
        // bytes available to compare
        int length = Math.min(mPacketLength, mIndex);
        for (int i = 0; i < length; i++) {
            if (mData[i] != pack.mData[i])
                return false;
        }
        return true;
    }

    public final byte[] data() { return mData; }
}
