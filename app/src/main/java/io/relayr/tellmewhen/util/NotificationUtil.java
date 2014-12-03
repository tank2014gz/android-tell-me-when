package io.relayr.tellmewhen.util;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.model.TMWNotification;

public class NotificationUtil {

    public static String getDate(Context context, TMWNotification notif) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(notif.getTimestamp());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar today = Calendar.getInstance();
        today.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        return calendar.getTimeInMillis() > today.getTimeInMillis() ? context
                .getString(R.string.today) : sdf.format(calendar.getTime());
    }

    public static String getTime(TMWNotification notif) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(notif.getTimestamp());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);

        return (hour < 10 ? "0" + hour : hour) + ":" +
                (minutes < 10 ? "0" + minutes : minutes) + ":" +
                (seconds < 10 ? "0" + seconds : seconds);
    }
}
