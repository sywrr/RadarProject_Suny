package com.ltdpro;

import android.util.Log;

import com.Connection.Connector;
import com.Connection.Packet;
import com.Connection.SendTransfer;
import com.Utils.AbstractLogger;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Uploader {

    private static final class PacketPool {

        private final int mLength;
        private final AtomicInteger mAllocatedBlocks = new AtomicInteger(0);

        private final ConcurrentLinkedQueue<Packet> mAvailableQueue = new ConcurrentLinkedQueue<>();

        public PacketPool(int length) { mLength = length; }

        public Packet alloc() {
            Packet pack = mAvailableQueue.poll();
            if (pack == null) {
                if (!increaseAllocBlocks(0, Integer.MAX_VALUE, 1))
                    throw new OutOfMemoryError("can not alloc any blocks");
                return new Packet(Global.PACKET_DATA, mLength);
            }
            pack.seek(0);
            return pack;
        }

        public void recycle(Packet pack) {
            mAvailableQueue.offer(pack);
        }

        public int shrink(int maxSize, int targetSize) {
            if (targetSize > maxSize || maxSize < 0)
                throw new IllegalArgumentException("maxSize or targetSize can not be negative");
            if (maxSize == targetSize || mAvailableQueue.size() < maxSize)
                return 0;
            int realShrink = 0;
            Packet pack;
            while (mAvailableQueue.size() > targetSize) {
                if ((pack = mAvailableQueue.poll()) == null)
                    break;
                if (!increaseAllocBlocks(0, Integer.MAX_VALUE, -1)) {
                    mAvailableQueue.offer(pack);
                    break;
                }
                ++realShrink;
            }
            return realShrink;
        }

        public int allocBlocks() { return mAllocatedBlocks.get(); }

        private boolean increaseAllocBlocks(int min, int max, int delta) {
            if (delta == 0)
                return true;
            if (min < 0 || max < 0)
                throw new IllegalArgumentException("min or max can not be negative");
            int allocBlocks;
            do {
                allocBlocks = mAllocatedBlocks.get();
                if ((allocBlocks + delta) > max || allocBlocks + delta < min)
                    return false;
            } while (!mAllocatedBlocks.compareAndSet(allocBlocks, allocBlocks + delta));
            return true;
        }

    }

    private UploadTransfer mUploadTransfer;

    private final Map<Integer, PacketPool> mPacketPoolMap = new TreeMap<>(
            new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return o2 - o1;
                }
            });

    private PacketPool getPacketPool(int length) {
        PacketPool packetPool = mPacketPoolMap.get(length);
        if (packetPool == null) {
            packetPool = new PacketPool(length);
            mPacketPoolMap.put(length, packetPool);
        }
        return packetPool;
    }

    private Packet allocPacket(int length) {
        return getPacketPool(length).alloc();
    }

    private void deallocPacket(Packet pack) {
        if (pack != null)
            getPacketPool(pack.getPacketLength()).recycle(pack);
    }

    public final class UploadTransfer extends SendTransfer {
        public UploadTransfer(Connector connector, AbstractLogger logger) {
            super(connector, logger);
        }

        @Override
        protected boolean sendPacket(Packet pack) throws IOException {
            boolean complete = false;
            try {
                complete = super.sendPacket(pack);
                return complete;
            } finally {
                if (complete) {
                    deallocPacket(pack);
                }
            }
        }
    }

    public Uploader() { mUploadTransfer = null; }

    public final void setUploadTransfer(UploadTransfer uploadTransfer) {
        if (mUploadTransfer != null)
            throw new IllegalStateException("upload transfer is already set");
        if (uploadTransfer == null)
            throw new NullPointerException("can not accept null upload transfer");
        mUploadTransfer = uploadTransfer;
    }

    public final void putFileHeader(FileHeader fileHeader) {
        Packet pack = allocPacket(1024);
        pack.setPacketType(Global.PACKET_FILE_HEAD);
        pack.setPacketFlag(0xAAAABBBB);
        fileHeader.write(pack.data());
        pack.seek(1024);
        mUploadTransfer.put(pack);
        Log.d("Uploader", "length: " + pack.getPacketLength());
    }

    public final void putData(short[] buf, int length, short scanLength) {
        Packet pack = allocPacket(length * 2 + 2);
        pack.seek(0);
        pack.setPacketFlag(0xAAAABBBB);
        pack.putShort(scanLength);
        for (int i = 0; i < length; i++)
            pack.putShort(buf[i]);
        mUploadTransfer.put(pack);
    }

    public final void putUploadEnd() {
        Packet pack = new Packet(Global.PACKET_ACK, 2);
        pack.setPacketFlag(0xAAAABBBB);
        pack.putShort((short) 0);
        mUploadTransfer.put(pack);
    }

    public final void shrinkAll(int maxSize, int targetSize) {
        for (PacketPool packetPool : mPacketPoolMap.values()) {
            packetPool.shrink(maxSize, targetSize);
        }
    }

    public final int allocatedBlocks() {
        int sum = 0;
        for (PacketPool packetPool : mPacketPoolMap.values())
            sum += packetPool.allocBlocks();
        return sum;
    }

}
