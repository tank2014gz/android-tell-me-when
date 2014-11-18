package io.relayr.tellmewhen;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import de.greenrobot.event.EventBus;
import io.relayr.tellmewhen.model.WhenEvents;
import io.relayr.tellmewhen.storage.Storage;

public class MainActivity extends Activity {

    private int currentFragment = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Storage.init(getApplicationContext());

        setContentView(R.layout.activity_main);

        switchFragment(currentFragment);
    }

    @Override
    public void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);
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

}
