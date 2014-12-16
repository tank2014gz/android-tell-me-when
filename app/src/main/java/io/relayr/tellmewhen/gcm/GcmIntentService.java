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
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.Pair;

import com.activeandroid.query.Select;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.HashMap;
import java.util.Map;

import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.app.MainActivity;
import io.relayr.tellmewhen.model.TMWRule;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.consts.SensorType;
import io.relayr.tellmewhen.util.SensorUtil;

import static android.support.v4.app.NotificationCompat.Builder;
import static android.support.v4.app.NotificationCompat.InboxStyle;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {

    public static final String NOTIFICATION_ACTION_DELETE = "tmw_notification_canceled";
    public static final String NOTIFICATION_ACTION_CLICK = "tmw_notification_click";

    public static final int TMW_NOTIFICATION_ID = 34560;

    public static Map<Pair<SensorType, String>, Float> pushedRules = new HashMap<>();

    public GcmIntentService() {
        super(GcmIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        if (!intent.getExtras().isEmpty()) {
            switch (messageType) {
                case GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR:
                    sendNotification("Message type send error.");
                    break;
                case GoogleCloudMessaging.MESSAGE_TYPE_DELETED:
                    sendNotification("Message type deleted.");
                    break;
                case GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE:
                    if (!Storage.isNotificationScreenVisible())
                        sendNotification(intent.getExtras());
                    break;
            }
        }

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(Bundle msg) {
        Storage.startRuleScreen(false);

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

        Float notificationValue = 0f;
        try {
            notificationValue = Float.parseFloat(msg.getString("val", "0"));
        } catch (NumberFormatException e) {
            Log.e(GcmIntentService.class.getSimpleName(), e.getMessage());
        }

        pushedRules.put(new Pair<>(rule.getSensorType(), rule.name), notificationValue);

        String title = pushedRules.size() + " " + (pushedRules.size() > 1 ? getString(R.string
                .push_notification_title) : getString(R.string.push_notification_title_one_rule));

        Spannable spanTitle = new SpannableString(title);
        spanTitle.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        Notification notification = new Builder(getApplicationContext())
                .setContentIntent(createContentIntent())
                .setDeleteIntent(createDeleteIntent())
                .setSmallIcon(R.drawable.icon_notifications)
                .setContentTitle(spanTitle)
                .setContentText(getString(R.string.push_notification_summary))
                .setAutoCancel(true)
                .setStyle(prepareBigNotificationDetails(spanTitle))
                .build();

        showNotification(TMW_NOTIFICATION_ID, notification);

        playSound();
    }

    private InboxStyle prepareBigNotificationDetails(Spannable spanTitle) {
        NotificationCompat.InboxStyle result = new InboxStyle();
        result.setBigContentTitle(spanTitle);

        for (Map.Entry<Pair<SensorType, String>, Float> entry : pushedRules.entrySet()) {
            SensorType sensorType = entry.getKey().first;

            String ruleName = entry.getKey().second;
            if (ruleName.length() <= 20) {
                int empty = 20 - ruleName.length();
                for (int i = 0; i < empty; i++) {
                    ruleName += " ";
                }
            } else {
                ruleName = entry.getKey().second.substring(0, 20);
            }

            String notificationText = getString(R.string.push_notification_value) + " " +
                    SensorUtil.scaleToUiData(sensorType, entry.getValue()) +
                    sensorType.getUnit();

            String all = ruleName + " (" + notificationText + ")";

            Spannable spanNotif = new SpannableString(all);
            spanNotif.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                    0, ruleName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            result.addLine(spanNotif);
        }

        result.setSummaryText(getString(R.string.push_notification_summary));

        return result;
    }

    private void sendNotification(String msg) {
        Storage.startRuleScreen(false);

        Notification notification = new Builder(this)
                .setContentIntent(createContentIntent())
                .setSmallIcon(R.drawable.icon_notifications)
                .setContentTitle(getString(R.string.app_name))
                .setAutoCancel(true)
                .setContentText(msg)
                .build();

        showNotification(TMW_NOTIFICATION_ID, notification);
    }

    private PendingIntent createContentIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(NOTIFICATION_ACTION_CLICK, NOTIFICATION_ACTION_CLICK);
        return PendingIntent.getActivity(this, 0, intent, 0);
    }

    private PendingIntent createDeleteIntent() {
        return PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(NOTIFICATION_ACTION_DELETE), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void showNotification(int id, Notification notification) {
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(id, notification);
    }

    private boolean playingSound = false;

    private void playSound() {
        if (playingSound) return;

        playingSound = true;

        Handler mHandler = new Handler(getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                RingtoneManager.getRingtone(getApplicationContext(), sound).play();
                playingSound = false;
            }
        });
    }
}