package io.relayr.tellmewhen.app;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import de.greenrobot.event.EventBus;
import io.relayr.RelayrSdk;
import io.relayr.model.Transmitter;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.adapter.TransmitterAdapter;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.WhenEvents;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class TransmitterFragment extends Fragment {

    @InjectView(R.id.list_view) ListView mListView;

    private static TransmitterAdapter mTransmitterAdapter;

    private Subscription mTransmitterSubscription = Subscriptions.empty();

    public static TransmitterFragment newInstance() {
        return new TransmitterFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadTransmitters();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.transmitter_fragment, container, false);

        ButterKnife.inject(this, view);

        mTransmitterAdapter = new TransmitterAdapter(this.getActivity());
        mListView.setAdapter(mTransmitterAdapter);

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

        if (!mTransmitterSubscription.isUnsubscribed()) mTransmitterSubscription.unsubscribe();
    }

    @OnItemClick(R.id.list_view)
    public void onItemClick(int position) {
        Storage.saveRuleTransId(mTransmitterAdapter.getItem(position).id);
        Storage.saveRuleTransName(mTransmitterAdapter.getItem(position).getName());
        Storage.saveRuleTransType("Relayr WunderBar");

        EventBus.getDefault().post(new WhenEvents.DoneEvent());
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
                    }

                    @Override
                    public void onNext(List<Transmitter> transmitters) {
                        mTransmitterAdapter.clear();
                        mTransmitterAdapter.addAll(transmitters);
                        mTransmitterAdapter.notifyDataSetChanged();
                    }
                });
    }
}
