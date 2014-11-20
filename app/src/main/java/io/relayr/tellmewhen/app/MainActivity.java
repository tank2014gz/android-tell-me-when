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
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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

    public enum FragNames {
        MAIN, TRANS, SENSOR, RULE_VALUE, RULE_NAME, RULE_EDIT
    }

    @InjectView(R.id.navigation_title) TextView mNavigationTitle;
    @InjectView(R.id.navigation_back) View mNavigationBack;
    @InjectView(R.id.navigation_logout) TextView mNavigationLogOut;
    @InjectView(R.id.navigation_new_rule) View mNavigationNewRule;
    @InjectView(R.id.navigation_clear_notif) View mNavigationClear;

    private int fragmentPos = 0;
    private Fragment mCurrentFragment;
    private AlertDialog mNetworkDialog;
    private Subscription mUserInfoSubscription = Subscriptions.empty();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        checkWiFi();

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

        if (!mUserInfoSubscription.isUnsubscribed()) mUserInfoSubscription.unsubscribe();
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

    public void onEvent(WhenEvents.DoneEvent nre) {
        if (Storage.isRuleEditing()) switchFragment(FragNames.RULE_EDIT);
        else switchFragment(FragNames.values()[++fragmentPos]);
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
        if (--fragmentPos >= 0) {
            switchFragment(FragNames.values()[fragmentPos]);
        } else {
            super.onBackPressed();
        }
    }

    private void switchFragment(FragNames name) {
        switch (name) {
            case MAIN:
                fragmentPos = 0;
                mCurrentFragment = MainFragment.newInstance();
                mNavigationTitle.setText(getString(R.string.title_tab_rules));
                break;
            case TRANS:
                mCurrentFragment = TransmitterFragment.newInstance();
                mNavigationTitle.setText(getString(R.string.title_select_transmitter));
                break;
            case SENSOR:
                mCurrentFragment = SensorFragment.newInstance();
                mNavigationTitle.setText(getString(R.string.title_select_sensor));
                break;
            case RULE_VALUE:
                mCurrentFragment = RuleValueFragment.newInstance();
                mNavigationTitle.setText(getString(R.string.title_rule_value));
                break;
            case RULE_NAME:
                mCurrentFragment = RuleNameFragment.newInstance();
                mNavigationTitle.setText(getString(R.string.title_rule_name));
                break;
            case RULE_EDIT:
                mCurrentFragment = RuleEditFragment.newInstance();
                mNavigationTitle.setText(getString(R.string.title_rule_edit));
                break;
            default:
                fragmentPos = 0;
                mCurrentFragment = MainFragment.newInstance();
                mNavigationTitle.setText(getString(R.string.title_tab_rules));
        }

        showFragment(mCurrentFragment);
        toggleNavigationButtons(name.equals(FragNames.MAIN));
    }

    private void toggleNavigationButtons(boolean main) {
        mNavigationClear.setVisibility(main ? View.VISIBLE : View.GONE);
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
}
