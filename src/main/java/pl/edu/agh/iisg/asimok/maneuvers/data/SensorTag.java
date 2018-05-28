package pl.edu.agh.iisg.asimok.maneuvers.data;

import java.util.HashMap;
import java.util.Map;

public enum SensorTag {
    PROXIMITY(Labels.XYZ_LABELS),
    ACCELEROMETER(Labels.XYZ_LABELS),
    GYROSCOPE(Labels.XYZ_LABELS),
    MAGNETOMETER(Labels.XYZ_LABELS),
    GPS_LOCATION(Labels.LOCATION_LABELS),
    NETWORK_LOCATION(Labels.LOCATION_LABELS),
    PASSIVE_LOCATION(Labels.LOCATION_LABELS),
    FUSED_LOCATION(Labels.LOCATION_LABELS),
    ACTIVITY(Labels.ACTIVITIES_LABELS),
    PHONE_STATE(Labels.V_LABELS),
    SCREEN_STATE(Labels.V_LABELS),
    SCREEN_TOUCH(Labels.V_LABELS),
    FOREGROUND_APPLICATION(Labels.S_LABELS) {
        public final Map<Float, String> floatToApp = new HashMap<>();
        private final Map<String, Float> appToFloat = new HashMap<>();
        @Override
        public float[] parseValues(Map<String, Object> valuesRaw) {
            String app = (String) valuesRaw.get("s");
            if (!appToFloat.containsKey(app)) {
                float code = app.hashCode();
                appToFloat.put(app, code);
                floatToApp.put(code, app);
            }
            return new float[] {appToFloat.get(app)};
        }
    },
    AUDIO_MODE(Labels.V_LABELS),
    LIGHT(Labels.XYZ_LABELS),
    GRAVITY(Labels.XYZ_LABELS),
    ROTATION_VECTOR(Labels.ROTATION_VECTOR_LABELS);

    private static class Labels {
        private static final String[] V_LABELS =
                {"v"};
        private static final String[] S_LABELS =
                {"s"};
        private static final String[] XYZ_LABELS =
                {"x", "y", "z"};
        private static final String[] ROTATION_VECTOR_LABELS =
                {"x", "y", "z", "cos", "acc"};
        private static final String[] LOCATION_LABELS =
                {"t", "lat", "lon", "alt", "s", "b", "h_acc", "v_acc", "s_acc", "b_acc", "sat"};
        private static final String[] ACTIVITIES_LABELS =
                {"IN_VEHICLE", "ON_BICYCLE", "ON_FOOT", "RUNNING", "STILL", "TILTING", "UNKNOWN", "WALKING"};
    }

    protected final String[] labels;

    SensorTag(String[] labels) {
        this.labels = labels;
    }

    public int getLabelIndex(String label) {
        for (int i = 0; i < labels.length; i++) {
            if (labels[i].equals(label)) {
                return i;
            }
        }
        return -1;
    }

    public float[] parseValues(Map<String, Object> valuesRaw) {
        float[] values = new float[labels.length];
        for (int i = 0; i < labels.length; i++) {
            values[i] = ((Number) valuesRaw.getOrDefault(labels[i], 0.0)).floatValue();
        }

        return values;
    }
}
