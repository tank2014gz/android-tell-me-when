package io.relayr.tellmewhen.service.notif;

import com.activeandroid.query.Select;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import io.relayr.tellmewhen.TellMeWhenApplication;
import io.relayr.tellmewhen.model.RuleNotification;
import io.relayr.tellmewhen.model.Status;
import io.relayr.tellmewhen.service.NotificationService;
import io.relayr.tellmewhen.service.rule.RuleApi;
import rx.Observable;

public class NotificationServiceImpl implements NotificationService {

    @Inject @Named("notificationApi") NotificationApi notificationApi;

    public NotificationServiceImpl(){
        TellMeWhenApplication.objectGraph.inject(this);
    }

    @Override
    public Observable<List<RuleNotification>> getNotifications() {
        return null;
    }

    @Override
    public Observable<Status> deleteNotifications() {
        return null;
    }

    @Override
    public void saveNotification(RuleNotification notification) {
        notification.save();
    }

    @Override
    public List<RuleNotification> getLocalNotifications() {
        return new Select().from(RuleNotification.class).execute();
    }
}
