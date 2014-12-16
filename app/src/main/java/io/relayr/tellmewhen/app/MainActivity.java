package io.relayr.tellmewhen.app;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.crashlytics.android.Crashlytics;

import java.util.List;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import io.relayr.LoginEventListener;
import io.relayr.RelayrSdk;
import io.relayr.model.Transmitter;
import io.relayr.model.User;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.TellMeWhenApplication;
import io.relayr.tellmewhen.consts.LogUtil;
import io.relayr.tellmewhen.gcm.GcmIntentService;
import io.relayr.tellmewhen.gcm.GcmUtils;
import io.relayr.tellmewhen.model.TMWNotification;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.consts.FragmentName;
import io.relayr.tellmewhen.util.WhenEvents;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class MainActivity extends ActionBarActivity implements LoginEventListener {

    private static final int ACTION_REACHABILITY = 100;
    private final String CURRENT_FRAGMENT = "io.relayr.tmw.current.frag";

    private Subscription mUserInfoSubscription = Subscriptions.empty();
    private Subscription mTransmitterSubscription = Subscriptions.empty();

    private FragmentName mCurrentFragment;

    private boolean logInStarted = false;
    private boolean mBackPressed = false;

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

        if (getIntent() != null && getIntent().getStringExtra(GcmIntentService
                .NOTIFICATION_ACTION_CLICK) != null)
            RelayrSdk.logMessage(LogUtil.VIEW_WITH_PUSH);

        if (GcmIntentService.pushedRules != null)
            GcmIntentService.pushedRules.clear();

        if (GcmUtils.getInstance().checkPlayServices(this)) {
            GcmUtils.getInstance().init(this);

            if (isConnected())
                checkUserState();
            else
                startActivityForResult(new Intent(this, ReachabilityActivity.class), ACTION_REACHABILITY);
        }

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTION_REACHABILITY && !isConnected()) super.onBackPressed();
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

        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!mUserInfoSubscription.isUnsubscribed()) mUserInfoSubscription.unsubscribe();
        if (!mTransmitterSubscription.isUnsubscribed()) mTransmitterSubscription.unsubscribe();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mCurrentFragment != null) outState.putString(CURRENT_FRAGMENT, mCurrentFragment.name());
    }

    @Override
    public void onSuccessUserLogIn() {
        logInStarted = false;
        Toast.makeText(this, R.string.successfully_logged_in, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onErrorLogin(Throwable e) {
        logInStarted = false;
        Toast.makeText(this, R.string.unsuccessfully_logged_in, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (mCurrentFragment == null || mCurrentFragment.equals(FragmentName.MAIN))
            super.onBackPressed();
        else {
            mBackPressed = true;
            EventBus.getDefault().post(new WhenEvents.BackPressed());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_log_out) {
            mCurrentFragment = null;
            logInStarted = false;

            RelayrSdk.logOut();

            Storage.startRuleScreen(true);
            Storage.clearRuleData();
            Storage.saveGmsRegId(null);
            Storage.saveGmsAppVersion(0);

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
            case RULE_NAME:
                fragment = RuleNameFragment.newInstance();
                break;
            case RULE_EDIT:
                fragment = RuleEditFragment.newInstance();
                break;
            case RULE_VALUE_EDIT:
                fragment = RuleValueEditFragment.newInstance();
                break;
            case NOTIFICATION_DETAILS:
                fragment = NotificationDetailsFragment.newInstance();
                break;
            default:
                fragment = MainFragment.newInstance();
        }

        showFragment(fragment);
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (mBackPressed) {
            mBackPressed = false;
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        } else {
            if (!mCurrentFragment.equals(FragmentName.MAIN))
                transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        }

        transaction.replace(R.id.container, fragment).commit();
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private void loadUserInfo() {
        mUserInfoSubscription = RelayrSdk.getRelayrApi().getUserInfo()
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
                        if (Storage.loadUserId() != null && !Storage.loadUserId().equals(user.id))
                            new Delete().from(TMWNotification.class).execute();

                        Storage.saveUserId(user.id);

                        Crashlytics.setUserEmail(user.email);
                        Crashlytics.setUserName(user.getName());
                        Crashlytics.setUserIdentifier(user.id);

                        loadTransmitters();
                    }
                });
    }

    private void loadTransmitters() {
        mTransmitterSubscription = RelayrSdk.getRelayrApi()
                .getTransmitters(Storage.loadUserId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Transmitter>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(MainActivity.this, R.string.error_loading_transmitters, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(List<Transmitter> transmitters) {
                        Storage.saveTransmitters(transmitters);
                        switchFragment(mCurrentFragment != null ? mCurrentFragment : FragmentName.MAIN);
                    }
                });
    }
}
