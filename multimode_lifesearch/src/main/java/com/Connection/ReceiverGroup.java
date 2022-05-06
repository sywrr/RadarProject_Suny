package com.Connection;

import java.security.KeyException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ReceiverGroup implements Iterable<Map.Entry<String, Receiver>> {
    private Map<String, Receiver> mReceiverMap = new HashMap<>();

    public static final int MAINTAIN = 0;
    public static final int UPDATE = 1;
    public static final int THROW = 2;

    public void registerReceiver(String tag, ReceiveTransfer transfer) {
        if (mReceiverMap.get(tag) == null) {
            Receiver receiver = new Receiver();
            receiver.setReceiveTransfer(transfer);
            mReceiverMap.put(tag, receiver);
        } else {
            throw new RuntimeException(tag + " is already registered");
        }
    }

    public void registerReceiver(String tag, Receiver receiver) {
        if (mReceiverMap.get(tag) == null) {
            mReceiverMap.put(tag, receiver);
        } else {
            throw new RuntimeException(tag + " is already registered");
        }
    }

    public void startReceiver(String tag) {
        Receiver receiver = mReceiverMap.get(tag);
        if (receiver != null)
            receiver.start();
        else
            throw new RuntimeException("receiver with tag " + tag + " is not registered");
    }

    public boolean isReceiverStarted(String tag) {
        Receiver receiver = mReceiverMap.get(tag);
        if (receiver != null)
            return receiver.isStarted();
        return false;
    }

    public boolean hasReceiver(String tag) {
        return mReceiverMap.containsKey(tag);
    }

    public void startAllReceivers() {
        for (Map.Entry<String, Receiver> entry : mReceiverMap.entrySet()) {
            entry.getValue().start();
        }
    }

    public void unregisterReceiver(String tag) {
        Receiver receiver = mReceiverMap.remove(tag);
        if (receiver != null)
            receiver.stop();
    }

    public void unregisterAllReceivers() {
        Iterator<Map.Entry<String, Receiver>> it = mReceiverMap.entrySet().iterator();
        Map.Entry<String, Receiver> entry;
        while (it.hasNext()) {
            entry = it.next();
            entry.getValue().stop();
            it.remove();
        }
    }

    public Receiver getReceiver(String tag) {
        return mReceiverMap.get(tag);
    }

    public void mergeGroup(ReceiverGroup group, int mode) throws KeyException {
        Iterator<Map.Entry<String, Receiver>> iterator = group.iterator();
        Map.Entry<String, Receiver> entry;
        switch (mode) {
        case MAINTAIN:
            while (iterator.hasNext()) {
                entry = iterator.next();
                iterator.remove();
                if (mReceiverMap.containsKey(entry.getKey()))
                    continue;
                mReceiverMap.put(entry.getKey(), entry.getValue());
            }
            break;
        case UPDATE:
            while (iterator.hasNext()) {
                entry = iterator.next();
                iterator.remove();
                mReceiverMap.put(entry.getKey(), entry.getValue());
            }
            break;
        case THROW:
            while (iterator.hasNext()) {
                entry = iterator.next();
                if (mReceiverMap.containsKey(entry.getKey()))
                    throw new KeyException(
                            "receiver with tag " + entry.getKey() + " is already " + "registered");
                iterator.remove();
                mReceiverMap.put(entry.getKey(), entry.getValue());
            }
            break;
        }
    }

    public int size() {
        return mReceiverMap.size();
    }

    public boolean isEmpty() {
        return mReceiverMap.isEmpty();
    }

    @Override
    public Iterator<Map.Entry<String, Receiver>> iterator() {
        return mReceiverMap.entrySet().iterator();
    }
}
