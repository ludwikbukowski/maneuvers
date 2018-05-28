package pl.edu.agh.iisg.asimok.maneuvers.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BatchedRollingBuffer<E extends TimelineItem> implements MultiStreamDataProvider<E> {
    protected final long bufferTimeSpanMillis;
    protected final Map<Object, RollingBuffer<E>> buffers = new HashMap<>();
    protected Map<Object, List<E>> batches = new HashMap<>();
    protected Map<Object, Integer> lastSeenOffsets = new HashMap<>();


    public BatchedRollingBuffer(long bufferTimeSpanMillis) {
        this.bufferTimeSpanMillis = bufferTimeSpanMillis;
    }

    protected synchronized void appendBatches() {
        lastSeenOffsets.clear();
        for (Object tag : batches.keySet()) {
            if (!buffers.containsKey(tag)) {
                buffers.put(tag, new RingFlipRollingBuffer<>(bufferTimeSpanMillis));
            }
            buffers.get(tag).addAll(batches.get(tag));
            lastSeenOffsets.put(tag, batches.get(tag).size());
        }
        batches = new HashMap<>();
    }

    @Override
    public Iterable<Object> getStreamTags() {
        return buffers.keySet();
    }

    @Override
    public boolean containsStreamTag(Object tag) {
        return buffers.containsKey(tag);
    }

    @Override
    public StreamDataProvider<E> getStream(Object tag) {
        return new BufferDataProvider<>(buffers.get(tag), lastSeenOffsets.getOrDefault(tag, 0));
    }

    protected void addToMap(Map<Object, List<E>> map, E item, Object tag) {
        if (!map.containsKey(tag)) {
            map.put(tag, new ArrayList<>());
        }
        map.get(tag).add(item);
    }

    @Override
    public String toString() {
        return "BatchedRollingBuffer{"
                + "buffers=" + buffers.toString() + ","
                + "batches=" + batches.toString() + "}";
    }
    
}
