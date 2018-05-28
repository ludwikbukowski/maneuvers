package pl.edu.agh.iisg.asimok.maneuvers;

import pl.edu.agh.iisg.asimok.maneuvers.detector.*;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        long t1 = System.nanoTime();

        if (args.length != 4) {
            System.out.println("Usage: jarfile deviceId timestampFrom timestampTo outfile");
        } else {
            List<ManeuverDetector> maneuverDetectors = new ArrayList<>();

            // add instance of your detector
            maneuverDetectors.add(new DecelerateDetector());

            ManeuverDetectorManager maneuverDetectorManager = new ManeuverDetectorManager(
                    TimeUnit.MINUTES.toMillis(1), TimeUnit.SECONDS.toMillis(1), maneuverDetectors);

            Parser.readFromMongoDB(args[0], Long.valueOf(args[1]), Long.valueOf(args[2]), maneuverDetectorManager);

            Parser.writeFile(args[3], maneuverDetectorManager.getAllManeuvers());
        }

        long t2 = System.nanoTime();

        System.out.printf("Elapsed time: %s", Duration.of(t2 - t1, ChronoUnit.NANOS));
    }
}
