package io.relayr.tellmewhen.util;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.model.TMWNotification;

public class NotificationTimeUtil {

    public static String getDate(Context context, TMWNotification notif) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTimeInMillis(notif.getTimestamp());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar today = Calendar.getInstance();
        today.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH), 0, 0, 0);

        SimpleDateFormat sdf = new SimpleDateFormat(context.getString(R.string.notification_date_format));

        return calendar.getTimeInMillis() > today.getTimeInMillis() ? context
                .getString(R.string.today) : sdf.format(calendar.getTime());
    }

    public static String getTime(TMWNotification notif) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTimeInMillis(notif.getTimestamp());
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
