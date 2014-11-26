package io.relayr.tellmewhen.service.impl;

import io.relayr.tellmewhen.model.Notification;
import io.relayr.tellmewhen.model.Status;
import io.relayr.tellmewhen.service.NotificationService;
import rx.Observable;

public class MockNotificationService implements NotificationService{


    @Override
    public Observable<Notification> getNotifications() {
        return null;
    }

    @Override
    public Observable<Status> deleteNotifications() {
        return null;
    }
}
