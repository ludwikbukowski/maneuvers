package pl.edu.agh.iisg.asimok.maneuvers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import pl.edu.agh.iisg.asimok.maneuvers.data.DataPoint;
import pl.edu.agh.iisg.asimok.maneuvers.data.DelayType;
import pl.edu.agh.iisg.asimok.maneuvers.data.Maneuver;
import pl.edu.agh.iisg.asimok.maneuvers.data.SensorTag;
import pl.edu.agh.iisg.asimok.maneuvers.structure.DataPointRepository;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.*;

public final class Parser {
    private Parser () {}

    @SuppressWarnings("unchecked")
    public static void readFromMongoDB(String deviceId, long timestampFrom, long timestampTo,
                                                  DataPointRepository repository) {
        MongoCollection<Document> collection = MongoClients.create()
                .getDatabase("asimok_django")
                .getCollection("data_point_document");

        Bson query = Filters.and(
                Filters.eq("device_id", deviceId),
                Filters.gte("timestamp", Instant.ofEpochMilli(timestampFrom)),
                Filters.lt("timestamp", Instant.ofEpochMilli(timestampTo))
        );

        System.out.printf("Query: %s\n", query);
        System.out.printf("Query found %d points.\n", collection.count(query));

        FindIterable<Document> documents = collection.find(query);
        for (Document document : documents) {
            Date timestamp = document.getDate("timestamp");
            SensorTag sensor = SensorTag.valueOf(document.getString("sensor"));
            DelayType delay = DelayType.valueOf(document.getString("delay"));
            float[] values = sensor.parseValues((Map<String, Object>) document.get("value"));

            repository.addDataPoint(new DataPoint(timestamp, delay, values), sensor);
        }
    }

    public static void writeFile(String filename, List<Maneuver> maneuvers) {
        try (OutputStream stream = new FileOutputStream(filename)) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(stream, maneuvers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
