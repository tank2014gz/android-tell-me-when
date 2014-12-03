package io.relayr.tellmewhen.util;

import android.content.Context;
import android.util.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.model.TMWRule;

public class SensorUtil {

    private static Map<SensorType, String> sSensorMap = new HashMap<SensorType, String>();
    private static Map<SensorType, Pair<Integer, Integer>> sSensorValues =
            new HashMap<SensorType, Pair<Integer, Integer>>();

    private SensorUtil(Context context) {
        sSensorMap.put(SensorType.TEMP, context.getString(R.string.measurement_temperature));
        sSensorMap.put(SensorType.HUM, context.getString(R.string.measurement_humidity));
        sSensorMap.put(SensorType.SND_LEVEL, context.getString(R.string.measurement_noise));
        sSensorMap.put(SensorType.PROX, context.getString(R.string.measurement_proximity));
        sSensorMap.put(SensorType.LIGHT, context.getString(R.string.measurement_light));
        sSensorMap.put(SensorType.ACCEL, context.getString(R.string.measurement_acceleration));

        sSensorValues.put(SensorType.TEMP, new Pair<Integer, Integer>(-40, 100));
        sSensorValues.put(SensorType.HUM, new Pair<Integer, Integer>(0, 100));
        sSensorValues.put(SensorType.SND_LEVEL, new Pair<Integer, Integer>(0, 100));
        sSensorValues.put(SensorType.PROX, new Pair<Integer, Integer>(0, 100));
        sSensorValues.put(SensorType.LIGHT, new Pair<Integer, Integer>(0, 100));
        sSensorValues.put(SensorType.ACCEL, new Pair<Integer, Integer>(0, 10));
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
        return context.getResources().getIdentifier("ms_" + type.getTitle(),
                "drawable", context.getPackageName());
    }

    public static String buildRuleValue(TMWRule rule) {
        return rule.getSensorType().getTitle() + " " +
                rule.getOperatorType().getValue() + " " + rule.value + rule.getSensorType().getUnit();
    }

    public static int scaleToUiData(SensorType type, float data) {
        switch (type) {
            case PROX:
                return (int) (data / 2048 * getMaxValue(type));
            case LIGHT:
                return (int) (data / 4096 * getMaxValue(type));
            case SND_LEVEL:
                return (int) (data / 1024 * getMaxValue(type));
        }

        return (int) data;
    }

    public static int scaleToServerData(SensorType type, int data) {
        switch (type) {
            case PROX:
                return (int) (((float) data / getMaxValue(type)) * 2048);
            case LIGHT:
                return (int) (((float) data / getMaxValue(type)) * 4096);
            case SND_LEVEL:
                return (int) (((float) data / getMaxValue(type)) * 1024);
        }

        return data;
    }
}