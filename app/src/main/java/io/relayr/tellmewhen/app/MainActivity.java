package io.relayr.tellmewhen.app;

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
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import io.relayr.tellmewhen.gcm.GcmUtils;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.WhenEvents;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class MainActivity extends ActionBarActivity implements LoginEventListener {

    private static final String SENDER_ID = "731084512451";

    public enum FragNames {
        MAIN, TRANS, SENSOR, RULE_VALUE, RULE_NAME, RULE_EDIT
    }

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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.inject(this);

        if (GcmUtils.checkPlayServices(this)) {
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

        if (!GcmUtils.checkPlayServices(this)) finish();

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_log_out)
            if (RelayrSdk.isUserLoggedIn()) RelayrSdk.logOut();

        if (item.getItemId() == android.R.id.home) onBackClicked();

        return super.onOptionsItemSelected(item);
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
                Log.w("MainActivity", msg);
            }
        }.execute(null, null, null);
    }
}
