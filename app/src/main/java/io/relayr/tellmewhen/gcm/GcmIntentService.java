/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.relayr.tellmewhen.gcm;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.activeandroid.query.Select;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.app.MainActivity;
import io.relayr.tellmewhen.model.TMWRule;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.SensorUtil;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {

    public static final String NOTIFICATION_ACTION_DELETE = "tmw_notification_canceled";

    public static final int TMW_NOTIFICATION_ID = 34560;
    public static final int TMW_TEMP_ID = 34561;
    public static final int TMW_HUM_ID = 34562;
    public static final int TMW_LIGHT_ID = 34563;
    public static final int TMW_NOISE_ID = 34564;
    public static final int TMW_PROX_ID = 34565;

    public GcmIntentService() {
        super(GcmIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            switch (messageType) {
                case GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR:
                    sendNotification("Message type send error.");
                    break;
                case GoogleCloudMessaging.MESSAGE_TYPE_DELETED:
                    sendNotification("Message type deleted.");
                    break;
                case GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE:
                    if (!Storage.isNotificationScreenVisible()) {
                        sendNotification(extras);
                        Log.e("GCM", "SEND NOTIFICATION");
                    } else {
                        Log.e("GCM", "NOOOOOOOOO NOTIFICATION FOR YOU");
                    }
                    break;
            }
        }

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(Bundle msg) {
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        String ruleId = msg.getString("rule_id", "null");
        if (ruleId == null) {
            sendNotification(getBaseContext().getString(R.string.please_open_app));
            return;
        }

        TMWRule rule = new Select().from(TMWRule.class).where("dbId = ?", ruleId).executeSingle();
        if (rule == null) {
            sendNotification(getBaseContext().getString(R.string.please_open_app));
            return;
        }

        Storage.startRuleScreen(false);

        Float val = Float.parseFloat(msg.getString("val", "0f"));

        Notification notification = new NotificationCompat.Builder(this)
                .setContentIntent(PendingIntent.getActivity(this, 0,
                        new Intent(this, MainActivity.class), 0))
                .setDeleteIntent(PendingIntent.getBroadcast(getApplicationContext(), 0,
                        new Intent(NOTIFICATION_ACTION_DELETE), 0))
                .setSmallIcon(R.drawable.icon_notifications)
                .setContentTitle(rule.name)
                .setAutoCancel(true)
                .setContentInfo(getString(R.string.notif_triggering_value) + ": " + SensorUtil.scaleToUiData(rule.getSensorType(),
                        val))
                .setContentText(SensorUtil.buildRuleValue(rule))
                .build();

        switch (rule.getSensorType()) {
            case HUMIDITY:
                mNotificationManager.notify(TMW_HUM_ID, notification);
                break;
            case LUMINOSITY:
                mNotificationManager.notify(TMW_LIGHT_ID, notification);
                break;
            case NOISE_LEVEL:
                mNotificationManager.notify(TMW_NOISE_ID, notification);
                break;
            case PROXIMITY:
                mNotificationManager.notify(TMW_PROX_ID, notification);
                break;
            case TEMPERATURE:
                mNotificationManager.notify(TMW_TEMP_ID, notification);
                break;
        }

        playSound();
    }

    private void playSound() {
        try {
            Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), sound);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendNotification(String msg) {
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(TMW_NOTIFICATION_ID, buildNotification(msg));
    }

    private Notification buildNotification(String msg) {
        Storage.startRuleScreen(false);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setContentIntent(contentIntent)
                        .setSmallIcon(R.drawable.icon_notifications)
                        .setContentTitle("Tell me when")
                        .setAutoCancel(true)
                        .setContentText(msg);

        return mBuilder.build();
    }

}