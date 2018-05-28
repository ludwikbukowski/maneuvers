package pl.edu.agh.iisg.asimok.maneuvers.data;

import pl.edu.agh.iisg.asimok.maneuvers.structure.TaggedItem;
import pl.edu.agh.iisg.asimok.maneuvers.structure.TimelineItem;

import java.util.Date;
import java.util.Map;

public class Maneuver implements TimelineItem, TaggedItem {
    private final Date beginning;
    private final Date end;
    private final String type;
    private final Map<String, String> params;

    public Maneuver(Date beginning, Date end, String type, Map<String, String> params) {
        this.beginning = beginning;
        this.end = end;
        this.type = type;
        this.params = params;
    }

    public Date getBeginning() {
        return beginning;
    }

    public Date getEnd() {
        return end;
    }

    public String getType() {
        return type;
    }

    public String getParam(String label) {
        return params.get(label);
    }

    public Map<String, String> getParams() {
        return params;
    }

    @Override
    public long time() {
        return end.getTime();
    }

    @Override
    public String tag() {
        return type;
    }

    @Override
    public String toString() {
        return "Maneuver{"
                + "beginning=" + beginning + ","
                + "end=" + end + ","
                + "type=" + type + ","
                + "params=" + params + "}";
    }
}
