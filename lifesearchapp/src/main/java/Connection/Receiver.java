package Connection;

import android.util.Log;

import java.security.KeyException;
import java.util.HashMap;
import java.util.Map;

import Utils.Utils;

/*
 * process thread for packet receiving
 */
public class Receiver {
    private Map<Integer, PacketCallback> mCallbackMap = new HashMap<>();

    // packet source
    protected ReceiveTransfer mTransfer;

    // if receiver is stopped
    protected volatile boolean mStop;

    // process thread
    private Thread mThread;

    public Receiver() {
        mStop = true;
        mThread = null;
    }

    protected void checkState() {
        if (!mStop)
            throw new RuntimeException("can not change callback when handler is started");
    }

    // add packet callback bind to packetType
    public void addCallback(int packetType, PacketCallback callback) {
        checkState();
        mCallbackMap.put(packetType, callback);
    }

    // add packet callback only non-existed
    public void addCallbackUnique(int packetType, PacketCallback callback) throws KeyException {
        checkState();
        if (mCallbackMap.containsKey(packetType))
            throw new KeyException("callback of type " + packetType + " is not registered");
        mCallbackMap.put(packetType, callback);
    }

    // remove packet callback bind to packetType
    public void removeCallback(int packetType) {
        checkState();
        mCallbackMap.remove(packetType);
    }

    // clear all callbacks
    public void clearCallback() {
        checkState();
        mCallbackMap.clear();
    }

    public void updateCallback(int packetType, PacketCallback callback) throws KeyException {
        if (!mCallbackMap.containsKey(packetType)) {
            throw new KeyException("callback of type " + packetType + " is not registered");
        }
        mCallbackMap.put(packetType, callback);
    }

    public boolean hasCallback(int packetType) {
        return mCallbackMap.containsKey(packetType);
    }

    public int size() {
        return mCallbackMap.size();
    }

    public boolean isEmpty() {
        return mCallbackMap.isEmpty();
    }

    public synchronized void waitStop() {
        Utils.joinThreadUninterruptibly(mThread);
    }

    private void handlePacket(Packet pack) {
        // get packet callback for this packet
        PacketCallback callback = mCallbackMap.get(pack.getPacketType());
        if (callback != null)
            callback.invoke(pack);
    }

    private void doRun() {
        while (!mStop) {
            // receive packet from receiveTransfer
            Packet pack = mTransfer.get();
            if (pack != null)
                handlePacket(pack);
        }
        mCallbackMap.clear();
    }

    public synchronized void start() {
        if (mThread != null) {
            throw new RuntimeException("receiver can only start once");
        }
        if (mTransfer == null) {
            throw new RuntimeException("receive transfer can not be null");
        }
        if (mStop) {
            mStop = false;
            mThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    doRun();
                }
            });
            mThread.start();
        }
    }

    public synchronized void stop() {
        if (!mStop) {
            mStop = true;
            mThread.interrupt();
        }
    }

    // set receive transfer, invoked before start
    public synchronized void setReceiveTransfer(ReceiveTransfer transfer) {
        if (mTransfer != null) {
            throw new RuntimeException("receive transfer is already set");
        }
        mTransfer = transfer;
    }

    public boolean isStarted() {
        return !mStop;
    }
}
