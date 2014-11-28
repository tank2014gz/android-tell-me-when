package io.relayr.tellmewhen.service.notif;

import com.activeandroid.query.Select;

import java.util.List;

import io.relayr.tellmewhen.model.RuleNotification;
import io.relayr.tellmewhen.model.Status;
import io.relayr.tellmewhen.service.NotificationService;
import rx.Observable;

public class NotificationServiceImpl implements NotificationService {

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
