package io.relayr.tellmewhen;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.relayr.RelayrApp;
import io.relayr.tellmewhen.app.MainActivity;
import io.relayr.tellmewhen.app.MainFragment;
import io.relayr.tellmewhen.app.NotificationDetailsFragment;
import io.relayr.tellmewhen.app.RuleEditFragment;
import io.relayr.tellmewhen.app.RuleNameFragment;
import io.relayr.tellmewhen.app.RuleValueCreateFragment;
import io.relayr.tellmewhen.app.RuleValueEditFragment;
import io.relayr.tellmewhen.app.SensorFragment;
import io.relayr.tellmewhen.app.TransmitterFragment;
import io.relayr.tellmewhen.app.WhatFragment;
import io.relayr.tellmewhen.gcm.NotificationBroadcastReceiver;
import io.relayr.tellmewhen.service.NotificationService;
import io.relayr.tellmewhen.service.RuleService;
import io.relayr.tellmewhen.service.ServiceUtil;
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
                NotificationDetailsFragment.class,
                RuleServiceImpl.class,
                NotificationServiceImpl.class
        }
)
public class AppModule {

    private final Context context;

    public AppModule(Context applicationContext) {
        this.context = applicationContext;
    }

    @Provides
    @Singleton
    Endpoint provideEndpoint() {
        return Endpoints.newFixedEndpoint(ServiceUtil.getProperties(context).getProperty
                ("api_endpoint"));
    }

    @Provides
    @Singleton
    @Named("rulesAdapter")
    RestAdapter provideRulesRestAdapter(Endpoint endpoint) {
        return ServiceUtil.buildAdapter(context, endpoint, true);
    }

    @Provides
    @Singleton
    @Named("notificationsAdapter")
    RestAdapter provideNotificationsRestAdapter(Endpoint endpoint) {
        return ServiceUtil.buildAdapter(context, endpoint, false);
    }

    @Provides
    @Named("ruleApi")
    RuleApi provideRuleApi(@Named("rulesAdapter") RestAdapter adapter) {
        return adapter.create(RuleApi.class);
    }

    @Provides
    @Singleton
    RuleService provideRuleService() {
        return new RuleServiceImpl();
    }

    @Provides
    @Named("notificationApi")
    NotificationApi provideNotificationApi(@Named("notificationsAdapter") RestAdapter adapter) {
        return adapter.create(NotificationApi.class);
    }

    @Provides
    @Singleton
    NotificationService provideNotificationService() {
        return new NotificationServiceImpl();
    }
}
