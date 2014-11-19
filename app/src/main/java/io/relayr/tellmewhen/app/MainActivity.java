package io.relayr.tellmewhen.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.Toast;

import de.greenrobot.event.EventBus;
import io.relayr.LoginEventListener;
import io.relayr.RelayrSdk;
import io.relayr.model.User;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.model.WhenEvents;
import io.relayr.tellmewhen.storage.Storage;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class MainActivity extends Activity implements LoginEventListener {

    private int currentFragment = 0;
    private AlertDialog mNetworkDialog;
    private Subscription mUserInfoSubscription = Subscriptions.empty();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
    }

    @Override
    public void onResume() {
        super.onResume();

//        checkWiFi();
        switchFragment(currentFragment);

        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mNetworkDialog != null) mNetworkDialog.dismiss();

        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!mUserInfoSubscription.isUnsubscribed()) {
            mUserInfoSubscription.unsubscribe();
        }
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
                        switchFragment(currentFragment);
                    }
                });
    }

    @Override
    public void onBackPressed() {
        switchToPrevious();
    }

    public void onEvent(WhenEvents.BackClicked bc) {
        switchToPrevious();
    }

    public void onEvent(WhenEvents.NewRule nre) {
        switchToNext();
    }

    public void onEvent(WhenEvents.WunderBarSelected ws) {
        switchToNext();
    }

    public void onEvent(WhenEvents.MeasurementSelected ms) {
        switchToNext();
    }

    public void onEvent(WhenEvents.ValueFragDone vfd) {
        switchToNext();
    }

    public void onEvent(WhenEvents.NameFragDone nfd) {
        switchToNext();
    }

    private void switchToNext() {
        switchFragment(++currentFragment);
    }

    private void switchToPrevious() {
        if (--currentFragment >= 0) {
            switchFragment(currentFragment);
        } else {
            super.onBackPressed();
        }
    }

    private void switchFragment(int fragmentId) {
        Fragment fragment;
        switch (fragmentId) {
            case 0:
                fragment = RulesFragment.newInstance();
                break;
            case 1:
                fragment = TransmitterFragment.newInstance();
                break;
            case 2:
                fragment = MeasurementFragment.newInstance();
                break;
            case 3:
                fragment = ValueFragment.newInstance();
                break;
            case 4:
                fragment = NameFragment.newInstance();
                break;
            default:
                currentFragment = 0;
                fragment = RulesFragment.newInstance();
        }

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
    }

    private void checkWiFi() {
        if (isConnected()) {
            if (RelayrSdk.isUserLoggedIn()) {
                loadUserInfo();
            } else {
                RelayrSdk.logIn(this, this);
            }
        } else {
            showNetworkDialog();
        }
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private void showNetworkDialog() {
        mNetworkDialog = new AlertDialog.Builder(this).setTitle(getString(R.string.please_connect_to_wifi))
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
}
