package io.relayr.tellmewhen.service.rule;

import io.relayr.tellmewhen.service.model.DbSearch;
import io.relayr.tellmewhen.service.model.DbStatus;
import io.relayr.tellmewhen.service.model.DbRule;
import io.relayr.tellmewhen.service.model.DbDocuments;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

import static io.relayr.tellmewhen.service.ServiceUtil.RULE_API_DB;

public interface RuleApi {

    @POST(RULE_API_DB)
    Observable<DbStatus> createRule(@Body DbRule rule);

    @DELETE(RULE_API_DB + "/{document}")
    Observable<DbStatus> deleteRule(@Path("document") String docId,
                                  @Query("rev") String revNum);

    @PUT(RULE_API_DB + "/{document}")
    Observable<DbStatus> updateRule(@Path("document") String docId,
                                  @Query("rev") String revNum,
                                  @Body DbRule rule);

    @POST(RULE_API_DB + "/_find")
    Observable<DbDocuments<DbRule>> getAllRules(@Body DbSearch search);
}
