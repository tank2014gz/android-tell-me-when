package io.relayr.tellmewhen.service.rule;

import io.relayr.tellmewhen.model.Status;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface RuleApi {

    public static final String API_DB = "/bp_tmw_rules";

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
    Observable<Documents> getAllRules(@Body Search search);
}
