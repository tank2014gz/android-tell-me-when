package io.relayr.tellmewhen.service.notif;

import android.util.Log;

import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import io.relayr.tellmewhen.TellMeWhenApplication;
import io.relayr.tellmewhen.model.TMWNotification;
import io.relayr.tellmewhen.service.model.DataMapper;
import io.relayr.tellmewhen.service.model.DbBulkDelete;
import io.relayr.tellmewhen.service.model.DbSearch;
import io.relayr.tellmewhen.service.model.DbStatus;
import io.relayr.tellmewhen.service.NotificationService;
import io.relayr.tellmewhen.service.model.DbNotification;
import io.relayr.tellmewhen.service.model.DbDocuments;
import io.relayr.tellmewhen.storage.Storage;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class NotificationServiceImpl implements NotificationService {

    @Inject @Named("notificationApi") NotificationApi notificationApi;

    public NotificationServiceImpl() {
        TellMeWhenApplication.objectGraph.inject(this);
    }

    @Override
    public void deleteNotifications() {
        List<DbBulkDelete> deleteItems = new ArrayList<DbBulkDelete>();
        ArrayList<TMWNotification> notifications = new Select().from(TMWNotification.class)
                .execute();

        for (TMWNotification notif : notifications) {
            deleteItems.add(new DbBulkDelete(notif.dbId, notif.drRev));
        }

        notificationApi.deleteNotifications(new DbDocuments<DbBulkDelete>(deleteItems))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<DbStatus>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(DbStatus status) {
                        Log.e("NotifService", status.getOk());
                    }
                });
    }

    @Override
    public Observable<Integer> loadRemoteNotifications() {
        return notificationApi.getAllNotifications(new DbSearch(Storage.loadUserId()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<DbDocuments<DbNotification>, Integer>() {
                    @Override
                    public Integer call(DbDocuments<DbNotification> docs) {
                        if (!docs.getDocuments().isEmpty()) {
                            for (DbNotification notif : docs.getDocuments()) {
                                DataMapper.toRuleNotification(notif).save();
                            }

                            deleteNotifications();
                        }

                        return docs.getDocuments().size();
                    }
                });
    }

    @Override
    public List<TMWNotification> getLocalNotifications() {
        return new Select().from(TMWNotification.class).orderBy("timestamp DESC").execute();
    }
}
