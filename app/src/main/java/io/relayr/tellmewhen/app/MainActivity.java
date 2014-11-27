package io.relayr.tellmewhen.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import io.relayr.LoginEventListener;
import io.relayr.RelayrSdk;
import io.relayr.model.User;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.gcm.GcmUtils;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.FragmentName;
import io.relayr.tellmewhen.util.WhenEvents;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class MainActivity extends ActionBarActivity implements LoginEventListener {

    private static final String SENDER_ID = "731084512451";

    private AlertDialog mNetworkDialog;
    private Subscription mUserInfoSubscription = Subscriptions.empty();

    private String mRegId;
    private GoogleCloudMessaging mGoogleCloudMessaging;

    private FragmentName mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.inject(this);

        if (checkPlayServices(this)) {
            mGoogleCloudMessaging = GoogleCloudMessaging.getInstance(this);
            mRegId = GcmUtils.getRegistrationId(getApplicationContext());

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
        checkPlayServices(this);

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

    public void onBackClicked() {
        if (mCurrentFragment.equals(FragmentName.MAIN))
            super.onBackPressed();
        else
            EventBus.getDefault().post(new WhenEvents.BackPressed());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_log_out)
            if (RelayrSdk.isUserLoggedIn()) {
                RelayrSdk.logOut();
                RelayrSdk.logIn(this, this);
            }

        if (item.getItemId() == android.R.id.home) onBackClicked();

        return super.onOptionsItemSelected(item);
    }

    public void onEvent(WhenEvents.ShowFragment sf) {
        switchFragment(sf.getName());
    }

    private void switchFragment(FragmentName name) {
        mCurrentFragment = name;

        Fragment fragment;
        switch (name) {
            case MAIN:
                fragment = MainFragment.newInstance();
                break;
            case TRANS:
                fragment = TransmitterFragment.newInstance();
                break;
            case SENSOR:
                fragment = SensorFragment.newInstance();
                break;
            case RULE_VALUE_CREATE:
                fragment = RuleValueCreateFragment.newInstance();
                break;
            case RULE_VALUE_EDIT:
                fragment = RuleValueEditFragment.newInstance();
                break;
            case RULE_NAME:
                fragment = RuleNameFragment.newInstance();
                break;
            case RULE_EDIT:
                fragment = RuleEditFragment.newInstance();
                break;
            default:
                fragment = MainFragment.newInstance();
        }

        showFragment(fragment);
    }

    private void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

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
        if (Storage.loadUserId() == null) {
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
                            switchFragment(mCurrentFragment != null ? mCurrentFragment : FragmentName.MAIN);
                        }
                    });
        } else {
            switchFragment(mCurrentFragment != null ? mCurrentFragment : FragmentName.MAIN);
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices(Activity context) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, context,
                        GcmUtils.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("MainActivity", "This device is not supported.");
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
                    msg = "Registration ID=" + mRegId;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    GcmUtils.sendRegistrationIdToBackend();

                    GcmUtils.storeRegistrationId(MainActivity.this
                            .getApplicationContext(), mRegId);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.e("MainActivity", msg);
            }
        }.execute(null, null, null);
    }
}
