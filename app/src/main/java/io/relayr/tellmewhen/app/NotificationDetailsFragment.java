package io.relayr.tellmewhen.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.google.gson.Gson;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import io.relayr.RelayrSdk;
import io.relayr.model.Reading;
import io.relayr.model.TransmitterDevice;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.model.TMWNotification;
import io.relayr.tellmewhen.model.TMWRule;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.FragmentName;
import io.relayr.tellmewhen.util.NotificationUtil;
import io.relayr.tellmewhen.util.SensorType;
import io.relayr.tellmewhen.util.SensorUtil;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class NotificationDetailsFragment extends WhatFragment {

    @InjectView(R.id.notif_details_rule_name) TextView mRuleName;
    @InjectView(R.id.notif_details_rule_value) TextView mRuleValue;
    @InjectView(R.id.notif_details_timestamp) TextView mTimestamp;
    @InjectView(R.id.notif_details_value) TextView mValue;
    @InjectView(R.id.notif_details_current_sensor) TextView mCurrentSensor;

    private Subscription mWebSocketSubscription = Subscriptions.empty();
    private String mSensorDeviceId;

    public static NotificationDetailsFragment newInstance() {
        return new NotificationDetailsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        onCreateView(inflater, container, savedInstanceState, R.string.title_notif_details, true);

        View view = inflater.inflate(R.layout.notification_details_fragment, container, false);

        ButterKnife.inject(this, view);

        inject(this);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TMWNotification notif = Storage.getNotificationDetails();
        TMWRule rule = new Select().from(TMWRule.class)
                .where("dbId = ?", notif.getRuleId()).executeSingle();

        mRuleName.setText(rule.name);
        mRuleValue.setText(SensorUtil.buildRuleValue(rule));

        mTimestamp.setText(NotificationUtil.getDate(getActivity(),
                notif) + " " + NotificationUtil.getTime(notif));

        mValue.setText(SensorUtil.buildNotificationValue(rule, notif));

        loadDevice(rule.transmitterId, rule.getSensorType());
    }

    @OnClick(R.id.button_done)
    public void onDoneClicked() {
        switchTo(FragmentName.MAIN);
    }

    @Override
    void onBackPressed() {
        switchTo(FragmentName.MAIN);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (!mWebSocketSubscription.isUnsubscribed()) mWebSocketSubscription.unsubscribe();
        if (mSensorDeviceId != null) RelayrSdk.getWebSocketClient().unSubscribe(mSensorDeviceId);
    }

    private void loadDevice(String transmitterId, final SensorType sensor) {
        RelayrSdk.getRelayrApi()
                .getTransmitterDevices(transmitterId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<TransmitterDevice>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(List<TransmitterDevice> transmitterDevices) {
                        for (TransmitterDevice device : transmitterDevices) {
                            if (device.getModel().equals(sensor.getModel()))
                                subscribeForDeviceReadings(device, sensor);
                        }

                    }
                });
    }

    private void subscribeForDeviceReadings(TransmitterDevice device, final SensorType sensor) {
        mSensorDeviceId = device.id;
        mWebSocketSubscription = RelayrSdk.getWebSocketClient()
                .subscribe(device, new Subscriber<Object>() {

                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Object o) {
                        Reading reading = new Gson().fromJson(o.toString(), Reading.class);

                        float value = 0f;
                        switch (sensor) {
                            case TEMPERATURE:
                                value = reading.temp;
                                break;
                            case HUMIDITY:
                                value = reading.hum;
                                break;
                            case PROXIMITY:
                                value = SensorUtil.scaleToUiData(SensorType.PROXIMITY, reading.prox);
                                break;
                            case NOISE_LEVEL:
                                value = SensorUtil.scaleToUiData(SensorType.NOISE_LEVEL, reading.snd_level);
                                break;
                            case LUMINOSITY:
                                value = SensorUtil.scaleToUiData(SensorType.LUMINOSITY, reading.light);
                                break;
                        }

                        mCurrentSensor.setText(value + sensor.getUnit());
                    }
                });
    }
}
