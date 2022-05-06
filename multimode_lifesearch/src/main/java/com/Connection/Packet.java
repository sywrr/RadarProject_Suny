package com.Connection;

import com.Utils.Utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class Packet {

    private int mPacketType;
    private int mPacketLength;
    private byte[] mData;
    private final byte[] mTmpBytes = new byte[12];
    private int mIndex;
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

    public void setPacketType(int packetType) { mPacketType = packetType; }

    public void setPacketFlag(int packetFlag) {
        mPacketFlag = packetFlag;
    }

    public int getPacketFlag() {
        return mPacketFlag;
    }

    public boolean isFull() {
        return mIndex == mPacketLength;
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

    public void fillData(byte[] b) {
        if (b != null && b.length > 0)
            System.arraycopy(b, 0, mData, 0, b.length);
    }

    public void fillData(int startIndex, byte[] b, int offset, int len) {
        if (b != null && offset > 0 && offset + len <= b.length)
            System.arraycopy(b, offset, mData, startIndex, len);
    }

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

    private int getData(int index, int size) {
        int data = 0;
        for (int i = 0; i < size; i++) {
            data |= ((mData[index++] & 0xff) << (8 * i));
        }
        return data;
    }

    private int getData(int size) {
        int data = 0;
        for (int i = 0; i < size; i++) {
            data |= ((mData[mIndex++] & 0xff) << (8 * i));
        }
        return data;
    }

    private void putData(int index, int size, int data) {
        for (int i = 0; i < size; i++) {
            mData[index++] = (byte) (data & 0xff);
            data >>= 8;
        }
    }

    private void putData(int size, int data) {
        for (int i = 0; i < size; i++) {
            mData[mIndex++] = (byte) (data & 0xff);
            data >>= 8;
        }
    }

    public void putInt(int data) {
        putData(4, data);
    }

    public int getInt() {
        return getData(4);
    }

    public int getInt(int index) {
        return getData(index, 4);
    }

    public boolean canGetInt() {
        return mData != null && mIndex + 4 <= mData.length;
    }

    public void putShort(short data) {
        putData(2, data);
    }

    public short getShort() {
        return (short) getData(2);
    }

    public short getShort(int index) {
        return (short) getData(index, 2);
    }

    public boolean canGetShort() {
        return mData != null && mIndex + 2 <= mData.length;
    }

    public void putByte(byte data) {
        mData[mIndex++] = data;
    }

    public byte getByte() {
        return mData[mIndex++];
    }

    public byte getByte(int index) {
        return mData[index];
    }

    public boolean canGetByte() {
        return mData != null && mIndex < mData.length;
    }

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
        Utils.intToBytes(mPacketFlag, mTmpBytes, 0);
        Utils.intToBytes(mPacketType, mTmpBytes, 4);
        int bytesToSend = Math.min(mIndex, mPacketLength);
        Utils.intToBytes(bytesToSend, mTmpBytes, 8);
        if (!send(os, mTmpBytes, 12))
            return false;
        return send(os, mData, bytesToSend);
    }

    public byte[] data() { return mData; }

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
        if (!receive(is, mTmpBytes, 12))
            return false;
        int packetFlag = Utils.bytesToInt(mTmpBytes, 0);
        if (packetFlag != mPacketFlag)
            return false;
        mPacketType = Utils.bytesToInt(mTmpBytes, 4);
        mPacketLength = Utils.bytesToInt(mTmpBytes, 8);
        createBuf(mPacketLength);
        return receive(is, mData, mPacketLength);
    }

    public boolean receiveAsPacket(Packet pack, InputStream is) {
        if (mPacketFlag != pack.mPacketFlag)
            return false;
        if (!receive(is, mTmpBytes, 12))
            return false;
        int packetFlag = Utils.bytesToInt(mTmpBytes, 0);
        if (packetFlag != mPacketFlag)
            return false;
        mPacketType = Utils.bytesToInt(mTmpBytes, 4);
        mPacketLength = Utils.bytesToInt(mTmpBytes, 8);
        if (mPacketLength > 0) {
            createBuf(mPacketLength);
            if (!receive(is, mData, mPacketLength))
                return false;
            return Arrays.equals(mData, pack.mData);
        }
        return true;
    }

    public boolean equals(Packet pack) {
        if (mPacketFlag != pack.mPacketFlag)
            return false;
        if (mPacketType != pack.mPacketType || mPacketLength != pack.mPacketLength) {
            return false;
        }
        int length = Math.min(mPacketLength, mIndex);
        for (int i = 0; i < length; i++) {
            if (mData[i] != pack.mData[i])
                return false;
        }
        return true;
    }
}
