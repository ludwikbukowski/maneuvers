package pl.edu.agh.iisg.asimok.maneuvers;

import pl.edu.agh.iisg.asimok.maneuvers.data.DataPoint;
import pl.edu.agh.iisg.asimok.maneuvers.data.Maneuver;
import pl.edu.agh.iisg.asimok.maneuvers.data.SensorTag;
import pl.edu.agh.iisg.asimok.maneuvers.detector.ManeuverDetector;
import pl.edu.agh.iisg.asimok.maneuvers.structure.AutoBatchedRollingBuffer;
import pl.edu.agh.iisg.asimok.maneuvers.structure.DataPointRepository;
import pl.edu.agh.iisg.asimok.maneuvers.structure.ManeuverRepository;
import pl.edu.agh.iisg.asimok.maneuvers.structure.ManualBatchedRollingBuffer;

import java.util.*;

public class ManeuverDetectorManager implements ManeuverRepository, DataPointRepository {
    private final List<Maneuver> allManeuvers = new ArrayList<>();
    private final List<ManeuverDetector> maneuverDetectors;

    private final AutoBatchedRollingBuffer<DataPoint> dataPoints;
    private final ManualBatchedRollingBuffer<Maneuver> maneuvers;

    public ManeuverDetectorManager(long bufferTimeSpanMillis, long batchTimeSpanMillis,
                                   List<ManeuverDetector> maneuverDetectors) {
        this.maneuverDetectors = maneuverDetectors;
        dataPoints = new AutoBatchedRollingBuffer<>(bufferTimeSpanMillis, batchTimeSpanMillis);
        maneuvers = new ManualBatchedRollingBuffer<>(bufferTimeSpanMillis);
    }

    @Override
    public void addManeuver(Maneuver maneuver) {
        allManeuvers.add(maneuver);
        maneuvers.add(maneuver, maneuver.getType());
    }

    @Override
    public void addDataPoint(DataPoint dataPoint, SensorTag type) {
        while (dataPoints.addOrAppend(dataPoint, type)) {
            maneuvers.pushBatches();
            for (ManeuverDetector maneuverDetector : maneuverDetectors) {
                maneuverDetector.update(dataPoints, maneuvers, this);
            }
        }
    }
    
    public List<Maneuver> getAllManeuvers() {
        return allManeuvers;
    }

    @Override
    public String toString() {
        return "ManeuverDetectorManager{"
                + "allManeuvers=" + allManeuvers.toString() + ","
                + "maneuverDetectors=" + maneuverDetectors.toString() + "}";
    }
}
