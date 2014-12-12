package io.relayr.tellmewhen.util;

import android.content.Context;
import android.util.Pair;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.relayr.model.Reading;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.model.TMWNotification;
import io.relayr.tellmewhen.model.TMWRule;

public class SensorUtil {

    private static Map<SensorType, String> sSensorMap = new HashMap<>();
    private static Map<SensorType, Pair<Integer, Integer>> sSensorValues = new HashMap<>();

    private SensorUtil(Context context) {
        sSensorMap.put(SensorType.TEMPERATURE, context.getString(R.string.measurement_temperature));
        sSensorMap.put(SensorType.HUMIDITY, context.getString(R.string.measurement_humidity));
        sSensorMap.put(SensorType.NOISE_LEVEL, context.getString(R.string.measurement_noise));
        sSensorMap.put(SensorType.PROXIMITY, context.getString(R.string.measurement_proximity));
        sSensorMap.put(SensorType.LUMINOSITY, context.getString(R.string.measurement_light));

        sSensorValues.put(SensorType.TEMPERATURE, new Pair<>(-40, 140));
        sSensorValues.put(SensorType.HUMIDITY, new Pair<>(0, 100));
        sSensorValues.put(SensorType.NOISE_LEVEL, new Pair<>(0, 100));
        sSensorValues.put(SensorType.PROXIMITY, new Pair<>(0, 100));
        sSensorValues.put(SensorType.LUMINOSITY, new Pair<>(0, 100));
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
        return context.getResources().getIdentifier("ms_" + SensorUtil.getTitle(type),
                "drawable", context.getPackageName());
    }

    public static String buildRuleValue(TMWRule rule) {
        if (rule == null) return "null";
        return SensorUtil.getTitle(rule.getSensorType()) + " " +
                rule.getOperatorType().getValue() + " " +
                rule.value.intValue() + rule.getSensorType().getUnit();
    }

    public static String buildNotificationValue(TMWRule rule, TMWNotification notif) {
        if (rule == null || notif == null) return "null";
        return SensorUtil.scaleToUiData(rule.getSensorType(),
                notif.getValue()) + rule.getSensorType().getUnit();
    }

    public static String formatToUiValue(SensorType type, Reading r) {
        switch (type) {
            case TEMPERATURE:
                return r.temp + type.getUnit();
            case HUMIDITY:
                return r.hum + type.getUnit();
            case PROXIMITY:
                return scaleToUiData(SensorType.PROXIMITY, r.prox) + type.getUnit();
            case NOISE_LEVEL:
                return scaleToUiData(SensorType.NOISE_LEVEL, r.snd_level) + type.getUnit();
            case LUMINOSITY:
                return scaleToUiData(SensorType.LUMINOSITY, r.light) + type.getUnit();
            default:
                return "0";
        }
    }

    public static float scaleToUiData(SensorType type, float data) {
        switch (type) {
            case PROXIMITY:
                return Math.round(data / 2047 * getMaxValue(type));
            case LUMINOSITY:
                return Math.round(data / 4095 * getMaxValue(type));
            case NOISE_LEVEL:
                return Math.round(data / 1023 * getMaxValue(type));
        }

        return data;
    }

    public static float scaleToServerData(SensorType type, float data) {
        switch (type) {
            case PROXIMITY:
                return (data / getMaxValue(type) * 2047);
            case LUMINOSITY:
                return (data / getMaxValue(type) * 4095);
            case NOISE_LEVEL:
                return (data / getMaxValue(type) * 1023);
        }

        return data;
    }
}