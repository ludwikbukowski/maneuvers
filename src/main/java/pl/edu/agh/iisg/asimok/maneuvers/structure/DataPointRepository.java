package pl.edu.agh.iisg.asimok.maneuvers.structure;

import pl.edu.agh.iisg.asimok.maneuvers.data.DataPoint;
import pl.edu.agh.iisg.asimok.maneuvers.data.SensorTag;

public interface DataPointRepository {
    void addDataPoint(DataPoint dataPoint, SensorTag type);
}
