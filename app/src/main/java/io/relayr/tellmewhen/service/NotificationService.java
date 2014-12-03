package io.relayr.tellmewhen.service;

import java.util.List;

import io.relayr.tellmewhen.model.TMWNotification;
import rx.Observable;

public interface NotificationService {

    void deleteNotifications();

    Observable<Integer> loadRemoteNotifications();

    List<TMWNotification> getLocalNotifications();
}
