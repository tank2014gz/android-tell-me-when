package io.relayr.tellmewhen.service;

import java.util.List;

import io.relayr.tellmewhen.model.TMWNotification;
import io.relayr.tellmewhen.service.model.DbNotification;
import io.relayr.tellmewhen.util.SensorType;
import rx.Observable;

public interface NotificationService {

    public  static final int MIN_LIMIT = 10;

    Observable<Integer> loadRemoteNotifications();

    List<TMWNotification> getLocalNotifications(int totalItemCount);
}
