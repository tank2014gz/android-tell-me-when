package io.relayr.tellmewhen.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import io.relayr.RelayrSdk;
import io.relayr.model.Transmitter;
import io.relayr.model.TransmitterDevice;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.app.adapter.TransmitterAdapter;
import io.relayr.tellmewhen.consts.SensorType;
import io.relayr.tellmewhen.util.LogUtil;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.consts.FragmentName;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class TransmitterFragment extends WhatFragment {

    @InjectView(R.id.list_view) ListView mListView;

    private static TransmitterAdapter mTransmitterAdapter;

    public static TransmitterFragment newInstance() {
        return new TransmitterFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        onCreateView(inflater, container, savedInstanceState, R.string.title_select_transmitter, true);

        View view = inflater.inflate(R.layout.transmitter_fragment, container, false);

        ButterKnife.inject(this, view);

        mTransmitterAdapter = new TransmitterAdapter(this.getActivity());
        mListView.setAdapter(mTransmitterAdapter);

        LogUtil.logMessage(Storage.isRuleEditing() ? LogUtil.EDIT_RULE_TRANSMITTER :
                LogUtil.CREATE_RULE_TRANSMITTER);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mTransmitterAdapter.clear();
        mTransmitterAdapter.addAll(Storage.loadTransmitters());
        mTransmitterAdapter.notifyDataSetChanged();
    }

    @OnItemClick(R.id.list_view)
    public void onItemClick(int position) {
        if (Storage.isRuleEditing())
            findSensorId(mTransmitterAdapter.getItem(position), Storage.getRule().getSensorType());
        else {
            saveTransmitterData(mTransmitterAdapter.getItem(position));
            switchToEdit(FragmentName.SENSOR);
        }

        LogUtil.logMessage(Storage.isRuleEditing() ? LogUtil.EDIT_RULE_FINISH :
                LogUtil.CREATE_RULE_FINISH);
    }

    private void findSensorId(final Transmitter transmitter, final SensorType sensorType) {
        RelayrSdk.getRelayrApi()
                .getTransmitterDevices(transmitter.id)
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
                    public void onNext(List<TransmitterDevice> devices) {
                        boolean deviceFound = false;

                        for (TransmitterDevice device : devices) {
                            if (device.getModel().equals(sensorType.getModel())) {
                                Storage.getRule().sensorId = device.id;
                                saveTransmitterData(transmitter);
                                deviceFound = true;
                            }
                        }

                        if (!deviceFound) showToast(R.string.error_finding_device);

                        switchToEdit(FragmentName.SENSOR);
                    }
                });
    }

    private void saveTransmitterData(Transmitter transmitter) {
        Storage.getRule().transmitterId = transmitter.id;
        Storage.getRule().transmitterName = transmitter.getName();
        Storage.getRule().transmitterType = "Relayr WunderBar";
    }

    @Override
    void onBackPressed() {
        LogUtil.logMessage(Storage.isRuleEditing() ? LogUtil.EDIT_RULE_CANCEL :
                LogUtil.CREATE_RULE_CANCEL);

        switchToEdit(FragmentName.MAIN);
    }

}
