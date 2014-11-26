package io.relayr.tellmewhen.service.impl;

import android.util.Base64;

import java.util.List;

import io.relayr.tellmewhen.model.Rule;
import io.relayr.tellmewhen.model.Status;
import io.relayr.tellmewhen.service.RuleService;
import io.relayr.tellmewhen.storage.Storage;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import rx.Observable;

public class RuleCloudantService implements RuleService {

    public static final String API_ENDPOINT = "https://relayr.cloudant.com/bp_test";
    private  RuleApi sRuleApi;

    private RuleApi getRuleApi() {
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

    public Observable<Status> saveRule() {
        return getRuleApi().createRule(Storage.loadUserId(), Storage.getRule());
    }

    public Observable<List<Rule>> getRules() {
        return getRuleApi().getRules();
    }
}
