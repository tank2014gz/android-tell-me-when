package io.relayr.tellmewhen.util;

import android.content.Context;
import android.util.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.relayr.tellmewhen.R;

public class SensorUtil {

    private static Map<SensorType, String> sSensorMap = new HashMap<SensorType, String>();
    private static Map<SensorType, Pair<Integer, Integer>> sSensorValues =
            new HashMap<SensorType, Pair<Integer, Integer>>();

    private SensorUtil(Context context) {
        sSensorMap.put(SensorType.TEMP, context.getString(R.string.measurement_temperature));
        sSensorMap.put(SensorType.HUM, context.getString(R.string.measurement_humidity));
        sSensorMap.put(SensorType.NOISE, context.getString(R.string.measurement_noise));
        sSensorMap.put(SensorType.PROX, context.getString(R.string.measurement_proximity));
        sSensorMap.put(SensorType.LIGHT, context.getString(R.string.measurement_light));
        sSensorMap.put(SensorType.ACC, context.getString(R.string.measurement_acceleration));

        sSensorValues.put(SensorType.TEMP, new Pair<Integer, Integer>(-20, 40));
        sSensorValues.put(SensorType.HUM, new Pair<Integer, Integer>(0, 100));
        sSensorValues.put(SensorType.NOISE, new Pair<Integer, Integer>(0, 10));
        sSensorValues.put(SensorType.PROX, new Pair<Integer, Integer>(0, 100));
        sSensorValues.put(SensorType.LIGHT, new Pair<Integer, Integer>(0, 100));
        sSensorValues.put(SensorType.ACC, new Pair<Integer, Integer>(-3, 3));
    }

    public static void init(Context context) {
        new SensorUtil(context);
    }

    public static List<SensorType> getSensors() {
        return Arrays.asList(SensorType.values());
    }

    public static String getTitle(SensorType type) {
        return sSensorMap.get(type);
    }

    public static int getMinValue(SensorType type) {
        return sSensorValues.get(type).first;
    }

    public static int getMaxValue(SensorType type) {
        return sSensorValues.get(type).second;
    }

    public static int getIcon(Context context, SensorType type) {
        return context.getResources().getIdentifier("ms_" + type.getName(),
                "drawable", context.getPackageName());
    }
}
