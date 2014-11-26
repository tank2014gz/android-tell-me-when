package io.relayr.tellmewhen.service.impl;

import java.util.List;

import io.relayr.tellmewhen.model.Rule;
import io.relayr.tellmewhen.model.Status;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Path;
import rx.Observable;

public interface RuleApi {

    @GET("/")
    Observable<List<Rule>> getRules();

    @PUT("/{document}")
    Observable<Status> createRule(@Path("document") String userId,
                                  @Body Rule rule);

}
