package io.relayr.tellmewhen.service;

import java.util.List;

import io.relayr.tellmewhen.model.RuleNotification;
import io.relayr.tellmewhen.model.Status;
import rx.Observable;

public interface NotificationService {

     Observable<List<RuleNotification>> getNotifications();

     Observable<Status> deleteNotifications();

     void saveNotification(RuleNotification notification);

     List<RuleNotification> getLocalNotifications();
}
