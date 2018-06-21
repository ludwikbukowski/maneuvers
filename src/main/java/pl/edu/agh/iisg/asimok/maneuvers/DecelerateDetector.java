package pl.edu.agh.iisg.asimok.maneuvers;

import pl.edu.agh.iisg.asimok.maneuvers.data.DataPoint;
import pl.edu.agh.iisg.asimok.maneuvers.data.Maneuver;
import pl.edu.agh.iisg.asimok.maneuvers.data.SensorTag;
import pl.edu.agh.iisg.asimok.maneuvers.structure.Deceleration;
import pl.edu.agh.iisg.asimok.maneuvers.structure.ManeuverRepository;
import pl.edu.agh.iisg.asimok.maneuvers.structure.MultiStreamDataProvider;
import pl.edu.agh.iisg.asimok.maneuvers.structure.StreamDataProvider;

import java.util.*;
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
    // current timestamp
    private Date timestamp;

    private final int treshold  = 1;
    private final int verbose = 1;

    // time difference netween deceleration so that they can be merged
    private final long interval_time  = 2;
    // What is minimum speed change so its considered to be  violent deceleration.
    private final double interval_speed  = 5;

    // for aggregation
    private ArrayList<Deceleration> aggregator = new ArrayList();

    private void logMessage(String S){
        if(verbose == 1){
            System.out.println(S);
        }
    }

    private void aggregate(Deceleration dec){
        dec.log("Saving deceleration");
        aggregator.add(dec);
    }
    /// when treshold hit
    // called only when its actually peeding up
    private void processAggregations(ManeuverRepository repository){
        ArrayList<Deceleration> tosend = new ArrayList<>();
        logMessage("Aggregator size is " + aggregator.size());
        Iterator<Deceleration> itr = aggregator.iterator();
        Deceleration d1 = itr.next();
        Deceleration d2 = d1;
        while (itr.hasNext()) {
            d2 = itr.next();
            double speedDiff = d2.startSpeed - d1.endSpeed;
            long diff  = getDateDiff(d2.startTimestamp, d1.endTimestamp,
                    TimeUnit.SECONDS);
            if(diff <= interval_time && speedDiff < interval_speed){
                logMessage("Merging decelerations...");
                d1.log(" Merge this");
                d2.log(" with this");
                Deceleration d3 = d1.merge(d2);
                d3.log("after merge ");
                d1 = d3;
                d2 = d1;
            }else{
                tosend.add(d1);
                d1 = d2;
            }
        }
        // Handling last element
        long diff2 = getDateDiff(timestamp, d2.endTimestamp, TimeUnit.SECONDS);
        if(diff2>=interval_time){
            tosend.add(d2);
            aggregator.clear();
        }else{
            aggregator.clear();
            aggregator.add(d2);
        }
        for(Deceleration dec : tosend){
            createAndStoreManevuer(dec, repository);
        }
        logMessage("Fluhing aggregator");


    }

    private void handleNewDataPoint(DataPoint step, ManeuverRepository repo){
        final double newSpeed = step.getValue(4); // value 's'/'speed'
        timestamp = step.getTimestamp();
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
            logMessage("Saved deceleration manevuer");
//            createAndStoreManevuer(repo);
            storeCurrentDeceleration();
            //maybeProcessAggregations(repo);
        }else if(!inProgress && speedDiff < 0){
            // New Manevour detected - start saving data
            inProgress = true;
            startSpeed = previousSpeed;
            startAcc = acc;
            begin = previousTimestamp;
            //maybeProcessAggregations(repo);
//            System.out.println("Started deceleration manevuer");
        }else if(!inProgress && speedDiff > 0){
            // do nothing - its not deceleration
            maybeProcessAggregations(repo);
        }
        previousSpeed = newSpeed;
        previousAcc = acc;
        previousTimestamp = timestamp;
    }
    private void logDataPoint(DataPoint dt){
        logMessage("--------");
        logMessage("[DataPoint]: timestamp: " + dt.getTimestamp());
        logMessage("[DataPoint]: speed: " + dt.getValue(4));
    }

    private void maybeProcessAggregations(ManeuverRepository repo){
        if(aggregator.size() >= treshold){
            processAggregations(repo);
        }
    }
    private void storeCurrentDeceleration() {
        final Deceleration dec = new Deceleration(startAcc, endAcc, startSpeed, endSpeed, begin, end);
        aggregate(dec);
    }
    // TODO
    private void createAndStoreManevuer(Deceleration dec, ManeuverRepository repository){
        long duration = getDateDiff(dec.startTimestamp, dec.endTimestamp, TimeUnit.SECONDS);
        final Maneuver savedManeuver = buildManeuver(dec, duration);
        dec.log("Building manevour");
        repository.addManeuver(savedManeuver);
    }
    public static double computeAcc(double speed1, double speed2, Date time1, Date time2){
        double speedDiff = speed2 - speed1;
        long timeDiff = getDateDiff(time1, time2, TimeUnit.SECONDS);
        return speedDiff / timeDiff;
    }

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies =  mod(date2.getTime() - date1.getTime());
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }

    // Im not going to use library for that.
    // Writing such a helper function instead of importing
    // keeps my brain in shape
    public static long mod(long X){
        if(X<0){
            return -X;
        }
        return X;
    }

    private Maneuver buildManeuver(final Deceleration dec, final long duration){

        final Map<String, String> params = new HashMap<>();
        String startAccString = String.format("%.2f", dec.startAcc);
        String endAccString = String.format("%.2f", dec.endAcc);
        String startSpeedString = String.format("%.2f", dec.startSpeed);
        String endSpeedString = String.format("%.2f", dec.endSpeed);
        params.put("START_ACCELERATION", startAccString);
        params.put("END_ACCELERATION", endAccString);
        params.put("START_SPEED", startSpeedString);
        params.put("END_SPEED", endSpeedString);
        params.put("DURATION", Double.toString(duration));
        return new Maneuver(dec.startTimestamp, dec.endTimestamp, "DECELERATION", params);

    }
}
