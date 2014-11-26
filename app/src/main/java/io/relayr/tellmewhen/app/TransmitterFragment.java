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
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.adapter.TransmitterAdapter;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.FragmentName;
import io.relayr.tellmewhen.util.WhenEvents;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class TransmitterFragment extends WhatFragment {

    @InjectView(R.id.list_view) ListView mListView;

    private static TransmitterAdapter mTransmitterAdapter;

    private Subscription mTransmitterSubscription = Subscriptions.empty();

    public static TransmitterFragment newInstance() {
        return new TransmitterFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        onCreateView(inflater, container, savedInstanceState, R.string.title_select_transmitter);

        View view = inflater.inflate(R.layout.transmitter_fragment, container, false);

        ButterKnife.inject(this, view);

        mTransmitterAdapter = new TransmitterAdapter(this.getActivity());
        mListView.setAdapter(mTransmitterAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        loadTransmitters();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (!mTransmitterSubscription.isUnsubscribed()) mTransmitterSubscription.unsubscribe();
    }

    @OnItemClick(R.id.list_view)
    public void onItemClick(int position) {
        Storage.getRule().setTransmitterId(mTransmitterAdapter.getItem(position).id);
        Storage.getRule().setTransmitterName(mTransmitterAdapter.getItem(position).getName());
        Storage.getRule().setTransmitterType("Relayr WunderBar");

        switchToEdit(FragmentName.SENSOR);
    }

    @Override
    void onBackPressed() {
        switchToEdit(FragmentName.MAIN);
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
