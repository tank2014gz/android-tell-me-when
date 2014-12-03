package io.relayr.tellmewhen.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import io.relayr.RelayrSdk;
import io.relayr.model.TransmitterDevice;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.app.adapter.SensorAdapter;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.FragmentName;
import io.relayr.tellmewhen.util.SensorType;
import io.relayr.tellmewhen.util.SensorUtil;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class SensorFragment extends WhatFragment {

    @InjectView(R.id.list_view) ListView mListView;

    private SensorType mSensorType;
    private Subscription mDevicesSubscription = Subscriptions.empty();
    private List<TransmitterDevice> mTransmitters = new ArrayList<TransmitterDevice>();

    public static SensorFragment newInstance() {
        return new SensorFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        onCreateView(inflater, container, savedInstanceState, R.string.title_select_sensor, true);

        View view = inflater.inflate(R.layout.sensor_fragment, container, false);

        ButterKnife.inject(this, view);

        mListView.setAdapter(new SensorAdapter(this.getActivity()));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        loadDevices();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (!mDevicesSubscription.isUnsubscribed()) mDevicesSubscription.unsubscribe();
    }

    @OnItemClick(R.id.list_view)
    public void onItemClick(int position) {
        mSensorType = SensorUtil.getSensors().get(position);
        saveSensorData();
    }

    @Override
    void onBackPressed() {
        switchToEdit(FragmentName.TRANS);
    }

    private void loadDevices() {
        mDevicesSubscription = RelayrSdk.getRelayrApi()
                .getTransmitterDevices(Storage.getRule().transmitterId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<TransmitterDevice>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        showToast(R.string.error_loading_devices);
                    }

                    @Override
                    public void onNext(List<TransmitterDevice> transmitterDevices) {
                        mTransmitters = transmitterDevices;
                        saveSensorData();
                    }
                });
    }

    private void saveSensorData() {
        if (mSensorType != null && !mTransmitters.isEmpty()) {
            for (TransmitterDevice transmitter : mTransmitters) {
                if (transmitter.getModel().equals(mSensorType.getModel())) {
                    Storage.getRule().sensorType = mSensorType.ordinal();
                    Storage.getRule().sensorId = transmitter.id;

                    if (Storage.isRuleEditing()) switchTo(FragmentName.RULE_VALUE_EDIT);
                    else switchTo(FragmentName.RULE_VALUE_CREATE);
                }
            }
        }
    }
}
