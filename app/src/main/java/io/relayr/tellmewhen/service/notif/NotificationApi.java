package io.relayr.tellmewhen.service.notif;

import io.relayr.tellmewhen.service.model.DbBulkDelete;
import io.relayr.tellmewhen.service.model.DbDocuments;
import io.relayr.tellmewhen.service.model.DbSearch;
import io.relayr.tellmewhen.service.model.DbStatus;
import io.relayr.tellmewhen.service.model.DbNotification;
import retrofit.http.Body;
import retrofit.http.POST;
import rx.Observable;

import static io.relayr.tellmewhen.AppModule.NOTIFICATION_API_DB;

public interface NotificationApi {

    @POST(NOTIFICATION_API_DB + "/_bulk_docs")
    Observable<DbStatus> deleteNotifications(@Body DbDocuments<DbBulkDelete> bulk);

    @POST(NOTIFICATION_API_DB + "/_find")
    Observable<DbDocuments<DbNotification>> getAllNotifications(@Body DbSearch search);
}
