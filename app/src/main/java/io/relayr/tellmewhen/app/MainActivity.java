package io.relayr.tellmewhen.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import io.relayr.LoginEventListener;
import io.relayr.RelayrSdk;
import io.relayr.model.User;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.WhenEvents;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class MainActivity extends Activity implements LoginEventListener {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String SENDER_ID = "731084512451";

    public enum FragNames {
        MAIN, TRANS, SENSOR, RULE_VALUE, RULE_NAME, RULE_EDIT
    }

    @InjectView(R.id.navigation_title) TextView mNavigationTitle;
    @InjectView(R.id.navigation_back) View mNavigationBack;
    @InjectView(R.id.navigation_logout) TextView mNavigationLogOut;
    @InjectView(R.id.navigation_new_rule) View mNavigationNewRule;
    @InjectView(R.id.navigation_clear_notif) View mNavigationClear;

    private int mFragPosition = 0;
    private Fragment mCurrentFragment;
    private AlertDialog mNetworkDialog;
    private Subscription mUserInfoSubscription = Subscriptions.empty();

    private String mRegId;
    private GoogleCloudMessaging mGoogleCloudMessaging;
    private AtomicInteger msgId = new AtomicInteger();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        if (checkPlayServices()) {
            mGoogleCloudMessaging = GoogleCloudMessaging.getInstance(this);
            mRegId = getRegistrationId();

            if (mRegId.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i("MainActivity", "No valid Google Play Services APK found.");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        checkWiFi();
        checkPlayServices();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mNetworkDialog != null) mNetworkDialog.dismiss();

        if (!mUserInfoSubscription.isUnsubscribed()) mUserInfoSubscription.unsubscribe();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onSuccessUserLogIn() {
        Toast.makeText(this, R.string.successfully_logged_in, Toast.LENGTH_SHORT).show();
        loadUserInfo();
    }

    @Override
    public void onErrorLogin(Throwable e) {
        Toast.makeText(this, R.string.unsuccessfully_logged_in, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        onBackClicked();
    }

    @OnClick(R.id.navigation_logout)
    public void logOutClicked() {
        if (RelayrSdk.isUserLoggedIn()) RelayrSdk.logOut();
    }

    @OnClick(R.id.navigation_back)
    public void onBackClicked() {
        if (Storage.isRuleEditing()) {
            if (mCurrentFragment instanceof RuleEditFragment) {
                onEvent(new WhenEvents.DoneEditEvent());
            } else {
                switchFragment(FragNames.RULE_EDIT);
            }
        } else {
            switchToPrevious();
        }
    }

    public void onEvent(WhenEvents.TitleChangeEvent tce) {
        mNavigationTitle.setText(getString(tce.getTitle()));
    }

    public void onEvent(WhenEvents.DoneEvent nre) {
        if (Storage.isRuleEditing()) switchFragment(FragNames.RULE_EDIT);
        else switchFragment(FragNames.values()[++mFragPosition]);
    }

    public void onEvent(WhenEvents.DoneCreateEvent nfd) {
        if (Storage.isRuleEditing()) switchFragment(FragNames.RULE_EDIT);
        else switchFragment(FragNames.MAIN);
    }

    public void onEvent(WhenEvents.StartEditEvent dee) {
        Storage.setRuleEditing(true);
        Storage.prepareRuleForEdit(dee.getRule());
        switchFragment(FragNames.RULE_EDIT);
    }

    public void onEvent(WhenEvents.EditEvent ee) {
        switchFragment(ee.getFrag());
    }

    public void onEvent(WhenEvents.DoneEditEvent dee) {
        Storage.setRuleEditing(false);
        switchFragment(FragNames.MAIN);
    }

    private void switchToPrevious() {
        if (--mFragPosition >= 0) {
            switchFragment(FragNames.values()[mFragPosition]);
        } else {
            super.onBackPressed();
        }
    }

    private void switchFragment(FragNames name) {
        switch (name) {
            case MAIN:
                mFragPosition = 0;
                mCurrentFragment = MainFragment.newInstance();
                break;
            case TRANS:
                mCurrentFragment = TransmitterFragment.newInstance();
                break;
            case SENSOR:
                mCurrentFragment = SensorFragment.newInstance();
                break;
            case RULE_VALUE:
                mCurrentFragment = RuleValueFragment.newInstance();
                break;
            case RULE_NAME:
                mCurrentFragment = RuleNameFragment.newInstance();
                break;
            case RULE_EDIT:
                mCurrentFragment = RuleEditFragment.newInstance();
                break;
            default:
                mFragPosition = 0;
                mCurrentFragment = MainFragment.newInstance();
        }

        showFragment(mCurrentFragment);
        toggleNavigationButtons(name.equals(FragNames.MAIN));
    }

    private void toggleNavigationButtons(boolean main) {
        mNavigationClear.setVisibility(View.GONE);
        mNavigationNewRule.setVisibility(main ? View.VISIBLE : View.GONE);
        mNavigationLogOut.setVisibility(main ? View.VISIBLE : View.GONE);

        mNavigationBack.setVisibility(main ? View.GONE : View.VISIBLE);
    }

    private void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.pop_enter, 0);

        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
    }

    private void checkWiFi() {
        if (isConnected()) {
            if (RelayrSdk.isUserLoggedIn()) loadUserInfo();
            else RelayrSdk.logIn(this, this);
        } else {
            showNetworkDialog();
        }
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private void showNetworkDialog() {
        mNetworkDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.please_connect_to_wifi))
                .setPositiveButton(getString(R.string.connect), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                }).show();
    }

    private void loadUserInfo() {
        mUserInfoSubscription = RelayrSdk.getRelayrApi()
                .getUserInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<User>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(MainActivity.this, R.string.err_loading_user_data,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(User user) {
                        Storage.saveUserId(user.id);
                        switchFragment(FragNames.MAIN);
                    }
                });
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("MainActivity", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
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

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param regId registration ID
     */
    private void storeRegistrationId(String regId) {
        Storage.saveGmsRegId(regId);
        Storage.saveGmsAppVersion(getAppVersion(getApplicationContext()));
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     *
     * @return registration ID, or empty string if there is no existing registration ID.
     */
    private String getRegistrationId() {
        String registrationId = Storage.loadGmsRegistrationId();

        if (registrationId.isEmpty()) {
            return "";
        }

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = Storage.loadGmsAppVersion();
        int currentVersion = getAppVersion(getApplicationContext());
        if (registeredVersion != currentVersion) {
            Log.i("MainActivity", "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg;
                try {
                    if (mGoogleCloudMessaging == null) {
                        mGoogleCloudMessaging = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    mRegId = mGoogleCloudMessaging.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + mRegId;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend();

                    storeRegistrationId(mRegId);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.w("MainActivity", msg);
            }
        }.execute(null, null, null);
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

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app. Not needed for this demo since the device sends upstream messages
     * to a server that echoes back the message using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        // Your implementation here.
    }
}
