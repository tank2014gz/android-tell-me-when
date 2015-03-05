package io.relayr.tellmewhen.util;

import android.content.Context;
import android.util.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.relayr.model.Reading;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.consts.SensorType;
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
        return context.getResources().getIdentifier("ms_" + type.name().toLowerCase(),
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
        return SensorUtil.scaleToUiData(rule.getSensorType(), round(notif.value, 2)) +
                rule.getSensorType().getUnit();
    }

    public static String formatToUiValue(SensorType type, Reading r) {
        switch (type) {
            case TEMPERATURE:
                return round((Double) r.value, 2) + type.getUnit();
            case HUMIDITY:
                return (int) round((Double) r.value, 2) + type.getUnit();
            case PROXIMITY:
                return scaleToUiData(SensorType.PROXIMITY, round((Double) r.value, 2)) + type.getUnit();
            case NOISE_LEVEL:
                return scaleToUiData(SensorType.NOISE_LEVEL, round((Double) r.value, 2)) + type.getUnit();
            case LUMINOSITY:
                return scaleToUiData(SensorType.LUMINOSITY, round((Double) r.value, 2)) + type.getUnit();
            default:
                return "0";
        }
    }

    public static int scaleToUiData(SensorType type, Double data) {
        switch (type) {
            case PROXIMITY:
                return (int) Math.round(data / 2047 * getMaxValue(type));
            case LUMINOSITY:
                return (int) Math.round(data / 4095 * getMaxValue(type));
            case NOISE_LEVEL:
                return (int) Math.round(data / 1023 * getMaxValue(type));
        }

        return data.intValue();
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

    public static boolean checkReadingType(SensorType sensor, Reading reading) {
        if (sensor == SensorType.TEMPERATURE && reading.meaning.equals("temperature"))
            return true;
        else if (sensor == SensorType.HUMIDITY && reading.meaning.equals("humidity"))
            return true;
        else if (sensor == SensorType.LUMINOSITY && reading.meaning.equals("luminosity"))
            return true;
        else if (sensor == SensorType.NOISE_LEVEL && reading.meaning.equals("noiseLevel"))
            return true;
        else if (sensor == SensorType.PROXIMITY && reading.meaning.equals("proximity"))
            return true;
        else return false;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}