package pl.edu.agh.iisg.asimok.maneuvers;

import pl.edu.agh.iisg.asimok.maneuvers.data.DataPoint;
import pl.edu.agh.iisg.asimok.maneuvers.data.Maneuver;
import pl.edu.agh.iisg.asimok.maneuvers.data.SensorTag;
import pl.edu.agh.iisg.asimok.maneuvers.structure.ManeuverRepository;
import pl.edu.agh.iisg.asimok.maneuvers.structure.MultiStreamDataProvider;
import pl.edu.agh.iisg.asimok.maneuvers.structure.StreamDataProvider;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by ludwikbukowski on 27/05/18.
 */
public class DecelerateDetector implements pl.edu.agh.iisg.asimok.maneuvers.detector.ManeuverDetector {
    @Override
    public void update(MultiStreamDataProvider<DataPoint> dataPointsMultiStream, MultiStreamDataProvider<Maneuver> maneuversMultiStream, ManeuverRepository repository) {
        if (dataPointsMultiStream.containsStreamTag(SensorTag.GPS_LOCATION)) {
            StreamDataProvider<DataPoint> stream =
                    dataPointsMultiStream.getStream(SensorTag.GPS_LOCATION);
            for (int idx = stream.lastSeen() + 1; idx < stream.size(); idx++){
                DataPoint dataPoint = stream.get(idx);
                logDataPoint(dataPoint);
                handleNewDataPoint(dataPoint, repository);
            }
        }
    }
    private boolean inProgress = false;
    private double previousSpeed = 0;
    private double previousAcc = 0;
    private double startSpeed = 0;
    private double startAcc = 0;
    private double endAcc = 0;
    private double endSpeed = 0;
    private Date previousTimestamp = null;
    private Date begin = null;
    private Date end = null;
    private void handleNewDataPoint(DataPoint step, ManeuverRepository repo){
        final double newSpeed = step.getValue(4); // value 's'/'speed'
        final Date timestamp = step.getTimestamp();
        double speedDiff = newSpeed - previousSpeed;
        double acc;
        if(previousTimestamp != null) {
            acc = computeAcc(previousSpeed, newSpeed, previousTimestamp, timestamp);
        }else{
            acc = 0;
        }
        if(inProgress && speedDiff < 0) {
            // continue saving data
        }else if(inProgress && speedDiff >= 0){
            // stop saving data and create manevur. Store data from last measurement
            inProgress = false;
            endSpeed = previousSpeed;
            endAcc = previousAcc;
            end = previousTimestamp;
            System.out.println("Saved deceleration manevuer");
            createAndStoreManevuer(repo);
        }else if(!inProgress && speedDiff < 0){
            // New Manevour detected - start saving data
            inProgress = true;
            startSpeed = previousSpeed;
            startAcc = acc;
            begin = previousTimestamp;
            System.out.println("Started deceleration manevuer");
        }else if(!inProgress && speedDiff > 0){
            // do nothing - its not deceleration
        }
        previousSpeed = newSpeed;
        previousAcc = acc;
        previousTimestamp = timestamp;
    }
    private void logDataPoint(DataPoint dt){
        System.out.println("--------");
        System.out.println("[DataPoint]: timestamp: " + dt.getTimestamp());
        System.out.println("[DataPoint]: speed: " + dt.getValue(4));
    }
    private void createAndStoreManevuer(ManeuverRepository repository){
        long duration = getDateDiff(begin, end, TimeUnit.SECONDS);
        final Maneuver savedManeuver = buildManeuver(begin, end, startAcc, endAcc, startSpeed, endSpeed, duration);
        repository.addManeuver(savedManeuver);
    }
    public static double computeAcc(double speed1, double speed2, Date time1, Date time2){
        double speedDiff = speed2 - speed1;
        long timeDiff = getDateDiff(time1, time2, TimeUnit.SECONDS);
        return speedDiff / timeDiff;
    }

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }

    private Maneuver buildManeuver(final Date begin, final Date end, final Double startAcc,
                                    final Double endAcc, final Double startSpeed, final Double endSpeed, final long duration){

        final Map<String, String> params = new HashMap<>();
        String startAccString = String.format("%.2f", startAcc);
        String endAccString = String.format("%.2f", endAcc);
        String startSpeedString = String.format("%.2f", startSpeed);
        String endSpeedString = String.format("%.2f", endSpeed);
        params.put("START_ACCELERATION", startAccString);
        params.put("END_ACCELERATION", endAccString);
        params.put("START_SPEED", startSpeedString);
        params.put("END_SPEED", endSpeedString);
        params.put("DURATION", Double.toString(duration));
        return new Maneuver(begin, end, "DECELERATION", params);

    }
}
