package pl.edu.agh.iisg.asimok.maneuvers.data;

import pl.edu.agh.iisg.asimok.maneuvers.structure.TimelineItem;

import java.util.Arrays;
import java.util.Date;

public class DataPoint implements TimelineItem {
    private final Date timestamp;
    private final DelayType delay;
    private final float[] values;

    public DataPoint(Date timestamp, DelayType delay, float[] values) {
        this.timestamp = timestamp;
        this.delay = delay;
        this.values = values;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public DelayType getDelay() {
        return delay;
    }

    public float getValue(int index) {
        return values[index];
    }

    @Override
    public long time() {
        return timestamp.getTime();
    }

    @Override
    public String toString() {
        return "DataPoint{"
                + "timestamp=" + timestamp + ","
                + "delay=" + delay + ","
                + "values=" + Arrays.toString(values) + "}";
    }
}
