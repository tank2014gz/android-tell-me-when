package io.relayr.tellmewhen.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.FragmentName;
import io.relayr.tellmewhen.util.SensorUtil;

public class RuleEditFragment extends WhatFragment {

    @InjectView(R.id.ref_notification_switch) Switch mRuleSwitch;

    @InjectView(R.id.ref_rule_name) TextView mRuleName;

    @InjectView(R.id.ref_transmitter_type) TextView mTransType;
    @InjectView(R.id.ref_transmitter_name) TextView mTransName;

    @InjectView(R.id.ref_sensor_icon) ImageView mSensorIcon;
    @InjectView(R.id.ref_sensor_name) TextView mSensorName;
    @InjectView(R.id.ref_sensor_info) TextView mSensorInfo;

    @InjectView(R.id.ref_rule_value) TextView mRuleValue;

    public static RuleEditFragment newInstance() {
        return new RuleEditFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        onCreateView(inflater, container, savedInstanceState, R.string.title_rule_edit);

        View view = inflater.inflate(R.layout.rule_edit_fragment, container, false);

        ButterKnife.inject(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRuleSwitch.setChecked(Storage.getRule().isNotifying());

        mRuleName.setText(Storage.getRule().getName());

        mTransType.setText(Storage.getRule().getTransmitterType());
        mTransName.setText(Storage.getRule().getTransmitterName());

        mSensorIcon.setImageResource(SensorUtil.getIcon(getActivity(), Storage.getRule().getSensorType()));
        mSensorName.setText(SensorUtil.getTitle(Storage.getRule().getSensorType()));

        mRuleValue.setText(Storage.getRule().getOperatorType().getName() + " " + Storage.getRule()
                .getValue() + " " + Storage.getRule().getSensorType().getUnit());
    }

    @OnClick(R.id.button_done)
    public void onDoneClicked() {
        mRuleService.saveRule();
        switchTo(FragmentName.MAIN);
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
}
