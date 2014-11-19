package io.relayr.tellmewhen.util;

import android.content.Context;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.relayr.tellmewhen.R;

public class MeasurementUtil {

    private static Map<String, String> sMeasurementMap = new HashMap<String, String>();
    private static List<String> sMeasurementList = Arrays.asList("temperature", "humidity",
            "noise", "proximity", "light", "acceleration");

    public static void init(Context context) {
        new MeasurementUtil(context);
    }

    private MeasurementUtil(Context context) {
        initMeasurements(context);
    }

    private void initMeasurements(Context context) {
        sMeasurementMap.put(sMeasurementList.get(0), context.getString(R.string.measurement_temperature));
        sMeasurementMap.put(sMeasurementList.get(1), context.getString(R.string.measurement_humidity));
        sMeasurementMap.put(sMeasurementList.get(2), context.getString(R.string.measurement_noise));
        sMeasurementMap.put(sMeasurementList.get(3), context.getString(R.string.measurement_proximity));
        sMeasurementMap.put(sMeasurementList.get(4), context.getString(R.string.measurement_light));
        sMeasurementMap.put(sMeasurementList.get(5), context.getString(R.string.measurement_acceleration));
    }

    public static List<String> getMeasurementList() {
        return sMeasurementList;
    }

    public static String getTitle(String measurementId) {
        return sMeasurementMap.get(measurementId);
    }

    public static int getIcon(Context context, String id) {
        return context.getResources().getIdentifier("ms_" + id, "drawable", context.getPackageName());
    }
}
