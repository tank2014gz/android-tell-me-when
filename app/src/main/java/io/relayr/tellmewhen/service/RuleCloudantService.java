package io.relayr.tellmewhen.service;

import android.util.Base64;
import android.util.Log;

import java.util.List;

import io.relayr.tellmewhen.model.Rule;
import io.relayr.tellmewhen.model.Status;
import io.relayr.tellmewhen.storage.Storage;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class RuleCloudantService {

    public static final String API_ENDPOINT = "https://relayr.cloudant.com/bp_test";
    private static RuleApi sRuleApi;

    private static RuleApi getRuleApi() {
        if (sRuleApi == null) {
            sRuleApi = new RestAdapter.Builder()
                    .setEndpoint(API_ENDPOINT)
                    .setRequestInterceptor(new RequestInterceptor() {
                        @Override
                        public void intercept(RequestFacade request) {
                            final String authorizationValue = encodeCredentialsForBasicAuthorization();
                            request.addHeader("Authorization", authorizationValue);
                            request.addHeader("Content-Type", "application/json");
                        }

                        private String encodeCredentialsForBasicAuthorization() {
                            final String userAndPassword = "therfectaredurprederferi:cPGohDvGOlO1Ifg7aCQLkhPP";
                            return "Basic " + Base64.encodeToString(userAndPassword.getBytes(), Base64.NO_WRAP);
                        }
                    })
                    .build()
                    .create(RuleApi.class);
        }

        return sRuleApi;
    }

    public static void saveRule() {
        getRuleApi().createRule(Storage.loadUserId(), Storage.getRule())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Status>() {
                    @Override
                    public void onCompleted() {
                        Log.e("saveRule", "Completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("saveRule", e.getMessage());
                    }

                    @Override
                    public void onNext(Status status) {
                        Log.e("saveRule", status.toString());
                    }
                });
    }

    public static Observable<List<Rule>> getRules() {
        return getRuleApi().getRules();
    }
}
