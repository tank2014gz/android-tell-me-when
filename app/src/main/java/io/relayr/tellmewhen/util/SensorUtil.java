package io.relayr.tellmewhen.util;

import android.content.Context;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.relayr.tellmewhen.R;

public class SensorUtil {

    private static Map<SensorType, String> sSensorMap = new HashMap<SensorType, String>();

    private SensorUtil(Context context) {
        sSensorMap.put(SensorType.TEMP, context.getString(R.string.measurement_temperature));
        sSensorMap.put(SensorType.HUM, context.getString(R.string.measurement_humidity));
        sSensorMap.put(SensorType.NOISE, context.getString(R.string.measurement_noise));
        sSensorMap.put(SensorType.PROX, context.getString(R.string.measurement_proximity));
        sSensorMap.put(SensorType.LIGHT, context.getString(R.string.measurement_light));
        sSensorMap.put(SensorType.ACC, context.getString(R.string.measurement_acceleration));
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

    public static int getIcon(Context context, SensorType type) {
        return context.getResources().getIdentifier("ms_" + type.getName(),
                "drawable", context.getPackageName());
    }
}
