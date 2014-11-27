package io.relayr.tellmewhen.service.notif;

import io.relayr.tellmewhen.model.Status;
import io.relayr.tellmewhen.service.rule.DbRule;
import io.relayr.tellmewhen.service.rule.Search;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface NotificationApi {

    public static final String API_DB = "/bp_test";

    @POST(API_DB)
    Observable<Status> createRule(@Body DbRule rule);

    @DELETE(API_DB + "/{document}")
    Observable<Status> deleteRule(@Path("document") String docId,
                                  @Query("rev") String revNum);

    @PUT(API_DB + "/{document}")
    Observable<Status> updateRule(@Path("document") String docId,
                                  @Query("rev") String revNum,
                                  @Body DbRule rule);

    @POST(API_DB + "/_find")
    Observable<Object> getAllRules(@Body Search search);
}
