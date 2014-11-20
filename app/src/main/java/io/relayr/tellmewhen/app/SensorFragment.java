package io.relayr.tellmewhen.app;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import de.greenrobot.event.EventBus;
import io.relayr.RelayrSdk;
import io.relayr.model.TransmitterDevice;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.adapter.SensorAdapter;
import io.relayr.tellmewhen.util.SensorType;
import io.relayr.tellmewhen.util.WhenEvents;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.SensorUtil;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class SensorFragment extends Fragment {

    @InjectView(R.id.list_view) ListView mListView;

    private SensorType mSensorType;
    private Subscription mDevicesSubscription = Subscriptions.empty();
    private List<TransmitterDevice> mTransmitters = new ArrayList<TransmitterDevice>();

    public static SensorFragment newInstance() {
        return new SensorFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadDevices();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sensor_fragment, container, false);

        ButterKnife.inject(this, view);

        mListView.setAdapter(new SensorAdapter(this.getActivity()));

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (!mDevicesSubscription.isUnsubscribed()) mDevicesSubscription.unsubscribe();
    }

    @OnItemClick(R.id.list_view)
    public void onItemClick(int position) {
        mSensorType = SensorUtil.getSensors().get(position);
        saveSensorData();
    }

    private void loadDevices() {
        mDevicesSubscription = RelayrSdk.getRelayrApi()
                .getTransmitterDevices(Storage.loadRuleTransId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<TransmitterDevice>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

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
                    Storage.saveRuleSensor(mSensorType);
                    Storage.saveRuleSensorId(transmitter.id);

                    EventBus.getDefault().post(new WhenEvents.DoneEvent());
                }
            }
        }
    }
}
