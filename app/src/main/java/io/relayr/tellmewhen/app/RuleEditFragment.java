package io.relayr.tellmewhen.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import io.relayr.RelayrSdk;
import io.relayr.model.Reading;
import io.relayr.model.TransmitterDevice;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.model.TMWRule;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.FragmentName;
import io.relayr.tellmewhen.util.SensorType;
import io.relayr.tellmewhen.util.SensorUtil;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class RuleEditFragment extends WhatFragment {

    @InjectView(R.id.ref_notification_switch) Switch mRuleSwitch;

    @InjectView(R.id.ref_rule_name) TextView mRuleName;

    @InjectView(R.id.ref_transmitter_type) TextView mTransType;
    @InjectView(R.id.ref_transmitter_name) TextView mTransName;

    @InjectView(R.id.ref_sensor_icon) ImageView mSensorIcon;
    @InjectView(R.id.ref_sensor_name) TextView mSensorName;
    @InjectView(R.id.ref_sensor_info) TextView mSensorInfo;

    @InjectView(R.id.ref_rule_value) TextView mRuleValue;

    @InjectView(R.id.sensor_value) TextView mSensorValue;
    @InjectView(R.id.notif_details_current_sensor_loading) ProgressBar mCurrentSensorProgress;

    private Subscription mWebSocketSubscription = Subscriptions.empty();
    private String mSensorDeviceId;

    public static RuleEditFragment newInstance() {
        return new RuleEditFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        onCreateView(inflater, container, savedInstanceState, R.string.title_rule_edit, true);

        View view = inflater.inflate(R.layout.rule_edit_fragment, container, false);

        ButterKnife.inject(this, view);
        inject(this);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TMWRule rule = Storage.getRule();

        loadDevice(rule.transmitterId, rule.getSensorType());

        mRuleSwitch.setChecked(rule.isNotifying);
        mRuleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Storage.getRule().isNotifying = isChecked;
            }
        });

        mRuleName.setText(rule.name);

        mTransType.setText(rule.transmitterType);
        mTransName.setText(rule.transmitterName);

        mSensorIcon.setImageResource(SensorUtil.getIcon(getActivity(), rule.getSensorType()));
        mSensorName.setText(SensorUtil.getTitle(rule.getSensorType()));

        mRuleValue.setText((rule.getOperatorType().getValue() + " " +
                rule.value + " " + rule.getSensorType().getUnit()));
    }

    @Override
    public void onPause() {
        super.onPause();

        if (!mWebSocketSubscription.isUnsubscribed()) mWebSocketSubscription.unsubscribe();
        if (mSensorDeviceId != null) RelayrSdk.getWebSocketClient().unSubscribe(mSensorDeviceId);
    }

    @OnClick(R.id.button_done)
    public void onDoneClicked(final View button) {
        button.setEnabled(false);

        ruleService.updateRule(Storage.getRule())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        button.setEnabled(true);
                        showToast(R.string.error_saving_rule);
                    }

                    @Override
                    public void onNext(Boolean status) {
                        if (status) {
                            Storage.clearRuleData();
                            switchTo(FragmentName.MAIN);
                        } else {
                            onError(new Throwable());
                        }

                        button.setEnabled(true);
                    }
                });
    }

    @OnClick(R.id.ref_rule_name_edit)
    public void onNameEdit() {
        switchTo(FragmentName.RULE_NAME);
    }

    @OnClick(R.id.ref_transmitter_edit)
    public void onTransmitterEdit() {
        switchTo(FragmentName.TRANS);
    }

    @OnClick(R.id.ref_sensor_edit)
    public void onSensorEdit() {
        switchTo(FragmentName.SENSOR);
    }

    @OnClick(R.id.ref_rule_value_edit)
    public void onValueEdit() {
        switchTo(FragmentName.RULE_VALUE_EDIT);
    }

    @Override
    void onBackPressed() {
        switchTo(FragmentName.MAIN);
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
                        mCurrentSensorProgress.setVisibility(View.GONE);
                        mSensorValue.setVisibility(View.VISIBLE);

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

                        mSensorValue.setText(getString(R.string
                                .current_reading) + ": " + value + sensor.getUnit());
                    }
                });
    }
}
