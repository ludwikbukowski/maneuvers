package pl.edu.agh.iisg.asimok.maneuvers.structure;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class RingFlipRollingBuffer<E extends TimelineItem> implements RollingBuffer<E> {
    private static final int SURPLUS_FACTOR = 2;
    private static final int MAX_FREQUENCY_HZ = 120;

    private final long maxTimeSpanMillis;
    private final int capacity;
    private final Object[] bufferItems;

    private int writePos = 0;
    private int readPos = 0;
    private boolean flipped = false;

    public RingFlipRollingBuffer(long maxTimeSpanMillis) {
        this.maxTimeSpanMillis = maxTimeSpanMillis;
        this.capacity = (int) TimeUnit.MILLISECONDS.toSeconds(maxTimeSpanMillis) * MAX_FREQUENCY_HZ * SURPLUS_FACTOR;
        this.bufferItems = new Object[capacity];
    }

    @Override
    public synchronized void add(E item) {
        append(item);
        trimToSpan();
    }

    @Override
    public synchronized void addAll(Iterable<? extends E> items) {
        for (E item : items) {
            append(item);
        }
        trimToSpan();
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized E get(int idx) {
        if ((idx < 0) || (idx >= size())) {
            throw new IndexOutOfBoundsException("Index: " + idx + ", Size: " + size());
        }
        return (E) bufferItems[(readPos + idx) % capacity];
    }

    @Override
    public synchronized int size() {
        return Math.floorMod(writePos - readPos, capacity);
    }

    private synchronized void append(E item){
        if (size() > 0 && get(size() - 1).time() > item.time()) {
            throw new RuntimeException("Tried to add element out of order");
        }
        if (!flipped) {
            bufferItems[writePos++] = item;
            if (writePos == capacity) {
                writePos = 0;
                flipped = true;
            }
        } else {
            if (writePos < readPos) {
                bufferItems[writePos++] = item;
            } else {
                throw new RuntimeException("Tried to add element to full buffer");
            }
        }
    }

    private synchronized void drop() {
        if (!flipped) {
            if (readPos < writePos) {
                bufferItems[readPos] = null;
                readPos++;
            }
        } else {
            if (readPos >= writePos) {
                bufferItems[readPos] = null;
                readPos++;
                if (readPos == capacity) {
                    readPos = 0;
                    flipped = false;
                }
            }
        }
    }

    public synchronized void clear() {
        while (size() > 0) {
            drop();
        }
        this.writePos = 0;
        this.readPos = 0;
        this.flipped = false;
    }

    private synchronized void trimToSpan() {
        if (size() > 0) {
            long lastTime = get(size() - 1).time();
            while (lastTime - get(0).time() > maxTimeSpanMillis) {
                drop();
            }
        }
    }
    
    @Override
    public String toString() {
        return "RingFlipRollingBuffer{"
                + "maxTimeSpanMillis=" + maxTimeSpanMillis + ","
                + "capacity=" + capacity + ","
                + "readPos=" + readPos + ","
                + "writePos=" + writePos + ","
                + "flipped=" + flipped + ","
                + "bufferItems=" + Arrays.toString(bufferItems) + "}";
    }
}
