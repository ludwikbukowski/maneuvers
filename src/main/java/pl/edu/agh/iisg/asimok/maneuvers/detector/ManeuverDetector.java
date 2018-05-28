package pl.edu.agh.iisg.asimok.maneuvers.detector;

import pl.edu.agh.iisg.asimok.maneuvers.data.DataPoint;
import pl.edu.agh.iisg.asimok.maneuvers.data.Maneuver;
import pl.edu.agh.iisg.asimok.maneuvers.structure.ManeuverRepository;
import pl.edu.agh.iisg.asimok.maneuvers.structure.MultiStreamDataProvider;

public interface ManeuverDetector {
    void update(MultiStreamDataProvider<DataPoint> dataPointsMultiStream,
                MultiStreamDataProvider<Maneuver> maneuversMultiStream,
                ManeuverRepository repository);
}
