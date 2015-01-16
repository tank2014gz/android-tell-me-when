package io.relayr.tellmewhen.service;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import retrofit.Endpoint;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

public class ServiceUtil {

    public static final String RULE_API_DB = "/tellmewhen_rules";
    public static final String NOTIFICATION_API_DB = "/tellmewhen_notifications";

    private static final String PROPERTIES_FILE_NAME = "tellmewhen.properties";

    public static Properties getProperties(Context context) {
        Properties properties = new Properties();

        try {
            InputStream inputStream = context.getAssets().open(PROPERTIES_FILE_NAME);
            properties.load(inputStream);
            inputStream.close();
        } catch (IOException e) {
            Log.e(ServiceUtil.class.getSimpleName(), "Can not find properties file");
        }
        return properties;
    }

    public static RestAdapter buildAdapter(final Context context,
                                           Endpoint endpoint,
                                           final boolean rulesCre) {
        return new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestInterceptor.RequestFacade request) {
                        final String authorizationValue;
                        authorizationValue = encodeForBasicAuth(context, rulesCre);
                        request.addHeader("Authorization", authorizationValue);
                        request.addHeader("Content-Type", "application/json");
                    }
                }).build();
    }

    public static String encodeForBasicAuth(Context context, boolean rulesCre) {
        final String cre;
        if (rulesCre)
            cre = ServiceUtil.getProperties(context).getProperty("rules");
        else
            cre = ServiceUtil.getProperties(context).getProperty("notifications");

        return "Basic " + Base64.encodeToString(cre.getBytes(), Base64.NO_WRAP);
    }
}
