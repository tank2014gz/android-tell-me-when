package io.relayr.tellmewhen.gcm;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import io.relayr.tellmewhen.storage.Storage;

public class GcmUtils {

    private final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String SENDER_ID = "731084512451";

    private static GcmUtils sGcmUtils = null;
    private static GoogleCloudMessaging mGcm;

    public static GcmUtils getInstance() {
        if (sGcmUtils == null)
            sGcmUtils = new GcmUtils();
        return sGcmUtils;
    }

    public void init(Context context) {
        mGcm = GoogleCloudMessaging.getInstance(context);

        if (getRegistrationId(context.getApplicationContext()) == null) {
            registerInBackground(context);
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public boolean checkPlayServices(Activity context) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, context,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private static void registerInBackground(final Context context) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg;
                try {
                    if (mGcm == null)
                        mGcm = GoogleCloudMessaging.getInstance(context.getApplicationContext());

                    String mRegId = mGcm.register(SENDER_ID);
                    msg = "Registration ID=" + mRegId;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    GcmUtils.sendRegistrationIdToBackend(mRegId);
                    GcmUtils.storeRegistrationId(context.getApplicationContext(), mRegId);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.d("MainActivity", msg);
            }
        }.execute(null, null, null);
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param regId registration ID
     */
    public static void storeRegistrationId(Context context, String regId) {
        Storage.saveGmsRegId(regId);
        Storage.saveGmsAppVersion(getAppVersion(context));
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     *
     * @return registration ID, or empty string if there is no existing registration ID.
     */
    public static String getRegistrationId(Context context) {
        String registrationId = Storage.loadGmsRegistrationId();

        if (registrationId == null) return null;

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = Storage.loadGmsAppVersion();
        int currentVersion = getAppVersion(context);

        if (registeredVersion != currentVersion) return null;

        return registrationId;
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app. Not needed for this demo since the device sends upstream messages
     * to a server that echoes back the message using the 'from' address in the message.
     *
     * @param mRegId
     */
    public static void sendRegistrationIdToBackend(String mRegId) {
        // Your implementation here.
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

}
