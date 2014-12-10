package io.relayr.tellmewhen.service;

import java.util.List;

import io.relayr.tellmewhen.model.TMWNotification;
import io.relayr.tellmewhen.service.model.DbNotification;
import rx.Observable;

public interface NotificationService {

    public  static final int MIN_LIMIT = 7;

    void deleteNotifications(List<DbNotification> documents);

    Observable<Integer> loadRemoteNotifications();

    List<TMWNotification> getLocalNotifications(int totalItemCount);
}
