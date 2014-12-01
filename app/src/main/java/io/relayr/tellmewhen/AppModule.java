package io.relayr.tellmewhen;

import android.util.Base64;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.relayr.tellmewhen.app.MainActivity;
import io.relayr.tellmewhen.app.MainFragment;
import io.relayr.tellmewhen.app.RuleEditFragment;
import io.relayr.tellmewhen.app.RuleNameFragment;
import io.relayr.tellmewhen.app.RuleValueCreateFragment;
import io.relayr.tellmewhen.app.RuleValueEditFragment;
import io.relayr.tellmewhen.app.SensorFragment;
import io.relayr.tellmewhen.app.TransmitterFragment;
import io.relayr.tellmewhen.app.WhatFragment;
import io.relayr.tellmewhen.service.NotificationService;
import io.relayr.tellmewhen.service.RuleService;
import io.relayr.tellmewhen.service.notif.NotificationApi;
import io.relayr.tellmewhen.service.notif.NotificationServiceImpl;
import io.relayr.tellmewhen.service.rule.RuleApi;
import io.relayr.tellmewhen.service.rule.RuleServiceImpl;
import retrofit.Endpoint;
import retrofit.Endpoints;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

@Module(
        complete = false,
        library = true,
        injects = {
                MainActivity.class,
                WhatFragment.class,
                MainFragment.class,
                TransmitterFragment.class,
                SensorFragment.class,
                RuleValueCreateFragment.class,
                RuleValueEditFragment.class,
                RuleEditFragment.class,
                RuleNameFragment.class,
                RuleServiceImpl.class,
                NotificationServiceImpl.class
        }
)
public class AppModule {

    public static final String API_ENDPOINT = "https://relayr.cloudant.com";

    @Provides
    @Singleton
    Endpoint provideEndpoint() {
        return Endpoints.newFixedEndpoint(API_ENDPOINT);
    }

    @Provides
    @Singleton
    RestAdapter provideRestAdapter(Endpoint endpoint) {
        return new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestInterceptor.RequestFacade request) {
                        final String authorizationValue = encodeCredentialsForBasicAuthorization();
                        request.addHeader("Authorization", authorizationValue);
                        request.addHeader("Content-Type", "application/json");
                    }

                    private String encodeCredentialsForBasicAuthorization() {
                        final String userAndPassword = "ckdatencenewhormenuldson:MPKNgKi2wus7emvrh2OKAUoL";
                        return "Basic " + Base64.encodeToString(userAndPassword.getBytes(), Base64.NO_WRAP);
                    }
                })
                .build();
    }

    @Provides
    @Named("ruleApi")
    RuleApi provideRuleApi(RestAdapter adapter) {
        return adapter.create(RuleApi.class);
    }

    @Provides
    @Singleton
    RuleService provideRuleService() {
        return new RuleServiceImpl();
    }

    @Provides
    @Named("notificationApi")
    NotificationApi provideNotificationApi(RestAdapter adapter) {
        return adapter.create(NotificationApi.class);
    }

    @Provides
    @Singleton
    NotificationService provideNotificationService() {
        return new NotificationServiceImpl();
    }
}
