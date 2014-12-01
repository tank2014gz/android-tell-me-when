package io.relayr.tellmewhen.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.ButterKnife;
import dagger.ObjectGraph;
import de.greenrobot.event.EventBus;
import io.relayr.LoginEventListener;
import io.relayr.RelayrSdk;
import io.relayr.model.User;
import io.relayr.tellmewhen.AppModule;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.TellMeWhenApplication;
import io.relayr.tellmewhen.gcm.GcmUtils;
import io.relayr.tellmewhen.service.RuleService;
import io.relayr.tellmewhen.service.rule.RuleServiceImpl;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.FragmentName;
import io.relayr.tellmewhen.util.WhenEvents;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class MainActivity extends ActionBarActivity implements LoginEventListener {

    private final String CURRENT_FRAGMENT = "io.relayr.tmw.current.frag";

    private AlertDialog mNetworkDialog;
    private Subscription mUserInfoSubscription = Subscriptions.empty();

    private FragmentName mCurrentFragment;

    private boolean logInStarted = false;

    @Inject RuleService ruleService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);
        TellMeWhenApplication.objectGraph.inject(this);

        if (savedInstanceState != null) {
            String fragName = savedInstanceState.getString(CURRENT_FRAGMENT, null);

            if (fragName != null) mCurrentFragment = FragmentName.valueOf(fragName);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        //PlayServices are necessary for Notifications
        if (GcmUtils.getInstance().checkPlayServices(this)) {
            GcmUtils.getInstance().init(this);

            if (isConnected()) checkUserState();
            else showNetworkDialog();
        }

        EventBus.getDefault().register(this);
    }

    private void checkUserState() {
        if (RelayrSdk.isUserLoggedIn()) {
            loadUserInfo();
        } else {
            if (logInStarted) {
                onBackPressed();
            } else {
                logInStarted = true;
                RelayrSdk.logIn(this, this);
            }
        }
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

        if (!mUserInfoSubscription.isUnsubscribed()) mUserInfoSubscription.unsubscribe();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mCurrentFragment != null) outState.putString(CURRENT_FRAGMENT, mCurrentFragment.name());
    }

    @Override
    public void onSuccessUserLogIn() {
        Toast.makeText(this, R.string.successfully_logged_in, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onErrorLogin(Throwable e) {
        Toast.makeText(this, R.string.unsuccessfully_logged_in, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (mCurrentFragment == null || mCurrentFragment.equals(FragmentName.MAIN))
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
        if (item.getItemId() == R.id.action_log_out) {
            RelayrSdk.logOut();
            checkUserState();
        }

        if (item.getItemId() == android.R.id.home) onBackPressed();

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
                Storage.clearRuleData();
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
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.container, fragment)
                .commit();
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
                        Log.e("MA", e.toString());
                        Toast.makeText(MainActivity.this, R.string.err_loading_user_data,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(User user) {
                         switchFragment(mCurrentFragment != null ? mCurrentFragment : FragmentName.MAIN);
//                        if (Storage.loadUserId() == null) createUserData(user.id);
//                        else checkUserData(user.id);
                    }
                });
    }

    private void createUserData(String id) {
        Storage.saveUserId(id);

        ruleService.loadRemoteRules()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("MA loading all rules", e.toString());
                    }

                    @Override
                    public void onNext(Boolean o) {
                        Log.e("MA", o.toString());
                        switchFragment(mCurrentFragment != null ? mCurrentFragment : FragmentName.MAIN);
                    }
                });
    }

    private void checkUserData(String id) {
        if (!Storage.loadUserId().equals(id)) createUserData(id);
        else switchFragment(mCurrentFragment != null ? mCurrentFragment : FragmentName.MAIN);
    }
}
