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

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mTransmitterAdapter.clear();
        mTransmitterAdapter.addAll(Storage.loadTransmitters());
        mTransmitterAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @OnItemClick(R.id.list_view)
    public void onItemClick(int position) {
        Storage.getRule().transmitterId = mTransmitterAdapter.getItem(position).id;
        Storage.getRule().transmitterName = mTransmitterAdapter.getItem(position).getName();
        Storage.getRule().transmitterType = "Relayr WunderBar";

        switchToEdit(FragmentName.SENSOR);
    }

    @Override
    void onBackPressed() {
        switchToEdit(FragmentName.MAIN);
    }

}
