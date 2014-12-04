package io.relayr.tellmewhen.service;

import java.util.List;

import io.relayr.tellmewhen.model.TMWNotification;
import io.relayr.tellmewhen.service.model.DbNotification;
import rx.Observable;

public interface NotificationService {

    void deleteNotifications(List<DbNotification> documents);

    Observable<Integer> loadRemoteNotifications();

    List<TMWNotification> getLocalNotifications();
}
