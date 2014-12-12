package io.relayr.tellmewhen.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;

import io.relayr.tellmewhen.model.TMWRule;
import io.relayr.tellmewhen.service.ServiceUtil;
import io.relayr.tellmewhen.service.model.DataMapper;
import io.relayr.tellmewhen.service.model.DbBulkDelete;
import io.relayr.tellmewhen.service.model.DbDocuments;
import io.relayr.tellmewhen.service.model.DbNotification;
import io.relayr.tellmewhen.service.model.DbSearch;
import io.relayr.tellmewhen.service.model.DbStatus;
import io.relayr.tellmewhen.service.notif.NotificationApi;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.SensorType;
import retrofit.Endpoint;
import retrofit.Endpoints;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class NotificationBroadcastReceiver extends BroadcastReceiver {

    private final String TAG = NotificationBroadcastReceiver.class.getSimpleName();

    private NotificationApi api;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (api == null)
            api = getApi(context);

        if (intent.getAction().equals(GcmIntentService.NOTIFICATION_ACTION_DELETE)) {

            GcmIntentService.pushedRules.clear();
            loadRemoteNotifications().subscribe(new Subscriber<Integer>() {
                @Override
                public void onCompleted() {
                }

                @Override
                public void onError(Throwable e) {
                    Log.e(TAG, "Error loading notifications");
                }

                @Override
                public void onNext(Integer integer) {
                    Log.d(TAG, "Loaded " + integer + " notifications.");
                }
            });
        }
    }

    private Observable<Integer> loadRemoteNotifications() {
        final List<String> existingRules = new ArrayList<>();
        List<TMWRule> rules = new Select().from(TMWRule.class).execute();
        for (TMWRule rule : rules) {
            existingRules.add(rule.dbId);
        }

        return api.getAllNotifications(new DbSearch(Storage.loadUserId()))
                .subscribeOn(Schedulers.io())
                .map(new Func1<DbDocuments<DbNotification>, Integer>() {
                    @Override
                    public Integer call(DbDocuments<DbNotification> docs) {
                        if (!docs.getDocuments().isEmpty()) {
                            for (DbNotification notif : docs.getDocuments()) {
                                if (existingRules.contains(notif.getRuleId()))
                                    DataMapper.toRuleNotification(notif).save();
                            }

                            deleteNotifications(docs.getDocuments());
                        }

                        return docs.getDocuments().size();
                    }
                });
    }

    private void deleteNotifications(List<DbNotification> notifications) {
        List<DbBulkDelete> deleteItems = new ArrayList<>();

        for (DbNotification notif : notifications) {
            deleteItems.add(new DbBulkDelete(notif.getDbId(), notif.getDrRev()));
        }

        api.deleteNotifications(new DbDocuments<>(deleteItems))
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<DbStatus>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Error while deleting notifications");
                    }

                    @Override
                    public void onNext(DbStatus status) {
                        Log.d(TAG, "Deleted remote notifications: " + status.getOk());
                    }
                });
    }

    private NotificationApi getApi(Context context) {
        Endpoint endpoint = Endpoints.newFixedEndpoint(ServiceUtil.getProperties(context)
                .getProperty("api_endpoint"));

        return ServiceUtil.buildAdapter(context, endpoint, false).create(NotificationApi.class);
    }

}
