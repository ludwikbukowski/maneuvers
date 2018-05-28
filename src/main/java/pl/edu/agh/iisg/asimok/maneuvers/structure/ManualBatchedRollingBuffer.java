package pl.edu.agh.iisg.asimok.maneuvers.structure;

public class ManualBatchedRollingBuffer<E extends TimelineItem> extends BatchedRollingBuffer<E> {

    public ManualBatchedRollingBuffer(long bufferTimeSpanMillis) {
        super(bufferTimeSpanMillis);
    }
    
    public synchronized void add(E item, Object tag) {
        addToMap(batches, item, tag);
    }
    
    public synchronized void pushBatches() {
        appendBatches();
    }
}
