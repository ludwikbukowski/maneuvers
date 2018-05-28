package pl.edu.agh.iisg.asimok.maneuvers.structure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoBatchedRollingBuffer<E extends TimelineItem> extends BatchedRollingBuffer<E> {
    private final long batchTimeSpanMillis;
    private long batchesSeparationTime = -1;
    private Map<Object, List<E>> futureBatches = new HashMap<>();
    

    public AutoBatchedRollingBuffer(long bufferTimeSpanMillis, long batchTimeSpanMillis) {
        super(bufferTimeSpanMillis);
        this.batchTimeSpanMillis = batchTimeSpanMillis;
    }

    public synchronized boolean addOrAppend(E item, Object tag) {
        if (batchesSeparationTime == -1) {
            batchesSeparationTime = item.time();
        }

        if (item.time() < batchesSeparationTime) {
            addToMap(batches, item, tag);
        } else if (item.time() - batchesSeparationTime < batchTimeSpanMillis) {
            addToMap(futureBatches, item, tag);
        } else {
            appendBatches();
            batches = futureBatches;
            futureBatches = new HashMap<>();
            batchesSeparationTime += batchTimeSpanMillis;
            return true;
        }
        return false;
    }

}
