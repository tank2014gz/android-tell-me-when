package io.relayr.tellmewhen.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.service.RuleService;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.SensorUtil;
import io.relayr.tellmewhen.util.WhenEvents;

public class RuleEditFragment extends Fragment {

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
        View view = inflater.inflate(R.layout.rule_edit_fragment, container, false);

        ButterKnife.inject(this, view);
        getActivity().setTitle(R.string
                .title_rule_edit);

        ((TextView) view.findViewById(R.id.button_done)).setText(getString(R.string.button_done));

        populateRuleData();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    private void populateRuleData() {
        mRuleSwitch.setChecked(Storage.loadRuleNotifyState());

        mRuleName.setText(Storage.loadRuleName());

        mTransType.setText(Storage.loadRuleTransType());
        mTransName.setText(Storage.loadRuleTransName());

        mSensorIcon.setImageResource(SensorUtil.getIcon(getActivity(), Storage.loadRuleSensor()));
        mSensorName.setText(SensorUtil.getTitle(Storage.loadRuleSensor()));

        mRuleValue.setText(Storage.loadRuleOperator().getName() + " " + Storage.loadRuleValue());
    }

    @OnClick(R.id.button_done)
    public void onDoneClicked() {
        RuleService.saveRule();
        EventBus.getDefault().post(new WhenEvents.DoneEditEvent());
    }

    @OnClick(R.id.ref_rule_name_edit)
    public void onNameEdit() {
        EventBus.getDefault().post(new WhenEvents.EditEvent(MainActivity.FragNames.RULE_NAME));
    }

    @OnClick(R.id.ref_transmitter_edit)
    public void onTransmitterEdit() {
        EventBus.getDefault().post(new WhenEvents.EditEvent(MainActivity.FragNames.TRANS));
    }

    @OnClick(R.id.ref_sensor_edit)
    public void onSensorEdit() {
        EventBus.getDefault().post(new WhenEvents.EditEvent(MainActivity.FragNames.SENSOR));
    }

    @OnClick(R.id.ref_rule_value_edit)
    public void onValueEdit() {
        EventBus.getDefault().post(new WhenEvents.EditEvent(MainActivity.FragNames.RULE_VALUE));
    }
}
