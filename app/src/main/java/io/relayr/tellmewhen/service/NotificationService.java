package io.relayr.tellmewhen.service;

import io.relayr.tellmewhen.model.Notification;
import io.relayr.tellmewhen.model.Status;
import rx.Observable;

public interface NotificationService {

    public Observable<Notification> getNotifications();

    public Observable<Status> deleteNotifications();
}
