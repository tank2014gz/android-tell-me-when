package io.relayr.tellmewhen.service.notif;

import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.inject.Inject;
import javax.inject.Named;

import io.relayr.tellmewhen.TellMeWhenApplication;
import io.relayr.tellmewhen.model.TMWNotification;
import io.relayr.tellmewhen.model.TMWRule;
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

    private void deleteNotifications(List<DbNotification> notifications) {
        List<DbBulkDelete> deleteItems = new ArrayList<>();

        for (DbNotification notif : notifications) {
            deleteItems.add(new DbBulkDelete(notif.getDbId(), notif.getDrRev()));
        }

        notificationApi.deleteNotifications(new DbDocuments<>(deleteItems))
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<DbStatus>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(NotificationServiceImpl.class.getSimpleName(), e.getMessage());
                    }

                    @Override
                    public void onNext(DbStatus status) {
                        Log.v(NotificationServiceImpl.class.getSimpleName(), status.getOk());
                    }
                });
    }

    @Override
    public Observable<Integer> loadRemoteNotifications() {
        return notificationApi.getAllNotifications(new DbSearch(Storage.loadUserId()))
                .subscribeOn(Schedulers.io())
                .map(new Func1<DbDocuments<DbNotification>, Integer>() {
                    @Override
                    public Integer call(DbDocuments<DbNotification> docs) {
                        if (!docs.getDocuments().isEmpty()) {
                            for (DbNotification notif : docs.getDocuments()) {
                                DataMapper.toRuleNotification(notif).save();
                            }

                            deleteNotifications(docs.getDocuments());
                        }

                        return docs.getDocuments().size();
                    }
                });
    }

    @Override
    public List<TMWNotification> getLocalNotifications(int offset) {
        List<String> currentRuleIds = new ArrayList<>();
        List<TMWRule> rules = new Select().from(TMWRule.class).execute();
        for (TMWRule rule : rules) {
            currentRuleIds.add(rule.dbId);
        }

        if(currentRuleIds.isEmpty()){
            new Delete().from(TMWNotification.class).execute();
            return new ArrayList<>();
        }

        List<TMWNotification> notifications = new Select().from(TMWNotification.class)
                .orderBy("timestamp DESC")
                .offset(offset)
                .limit(MIN_LIMIT)
                .execute();

        ListIterator<TMWNotification> iterator = notifications.listIterator();
        while (iterator.hasNext()) {
            TMWNotification notif = iterator.next();
            if (!currentRuleIds.contains(notif.ruleId))
                notif.delete();
        }

        return notifications;
    }
}
