package io.relayr.tellmewhen.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import io.relayr.RelayrSdk;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.app.adapter.TransmitterAdapter;
import io.relayr.tellmewhen.consts.LogUtil;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.consts.FragmentName;

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
        Storage.getRule().transmitterId = mTransmitterAdapter.getItem(position).id;
        Storage.getRule().transmitterName = mTransmitterAdapter.getItem(position).getName();
        Storage.getRule().transmitterType = "Relayr WunderBar";

        LogUtil.logMessage(Storage.isRuleEditing() ? LogUtil.EDIT_RULE_FINISH :
                LogUtil.CREATE_RULE_FINISH);

        switchToEdit(FragmentName.SENSOR);
    }

    @Override
    void onBackPressed() {
        LogUtil.logMessage(Storage.isRuleEditing() ? LogUtil.EDIT_RULE_CANCEL :
                LogUtil.CREATE_RULE_CANCEL);

        switchToEdit(FragmentName.MAIN);
    }

}
