package io.relayr.tellmewhen.gcm;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import io.relayr.tellmewhen.storage.Storage;

public class GcmUtils {

    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

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

        if (registrationId.isEmpty()) {
            return "";
        }

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = Storage.loadGmsAppVersion();
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i("MainActivity", "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app. Not needed for this demo since the device sends upstream messages
     * to a server that echoes back the message using the 'from' address in the message.
     */
    public static void sendRegistrationIdToBackend() {
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

    //    public void sendEcho() {
//        new AsyncTask<Void, Void, String>() {
//            @Override
//            protected String doInBackground(Void... params) {
//                String msg;
//                try {
//                    Bundle data = new Bundle();
//                    data.putString("my_message", "Hello World");
//                    data.putString("my_action", "com.google.android.gcm.demo.app.ECHO_NOW");
//                    String id = Integer.toString(msgId.incrementAndGet());
//                    mGoogleCloudMessaging.send(SENDER_ID + "@gcm.googleapis.com", id, data);
//                    msg = "Sent message";
//                } catch (IOException ex) {
//                    msg = "Error :" + ex.getMessage();
//                }
//
//                return msg;
//            }
//
//            @Override
//            protected void onPostExecute(String msg) {
//                Log.e("MA", msg + "\n");
//            }
//        }.execute(null, null, null);
//    }
}
