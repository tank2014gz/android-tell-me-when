package io.relayr.tellmewhen.app;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.OperatorType;
import io.relayr.tellmewhen.util.SensorType;
import io.relayr.tellmewhen.util.SensorUtil;
import io.relayr.tellmewhen.util.WhenEvents;

public class RuleValueFragment extends Fragment {

    @InjectView(R.id.vf_object_icon) ImageView mObjectIcon;
    @InjectView(R.id.vf_object_name) TextView mObjectName;
    @InjectView(R.id.vf_object_info) TextView mObjectInfo;

    @InjectView(R.id.vf_operator_less) View mOperatorLess;
    @InjectView(R.id.vf_operator_greater) View mOperatorGreater;

    @InjectView(R.id.vf_rule_value_seek) SeekBar mValueSeek;
    @InjectView(R.id.vf_rule_value_indicator) TextView mValueIndicator;

    private SensorType mSensorType;
    private OperatorType mCurrentOperator;

    public static RuleValueFragment newInstance() {
        return new RuleValueFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.rule_value_fragment, container, false);

        ButterKnife.inject(this, view);
        getActivity().setTitle(R.string
                .title_rule_value);

        ((TextView) view.findViewById(R.id.button_done)).setText(getString(R.string.button_done));

        mSensorType = Storage.loadRuleSensor();

        toggleOperator(OperatorType.LESS);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int maxValue = SensorUtil.getMaxValue(mSensorType) - SensorUtil.getMinValue(mSensorType);

        mValueSeek.setMax(maxValue);
        mValueSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mValueIndicator.setText((progress - Math.abs(SensorUtil.getMinValue
                        (mSensorType))) + mSensorType.getUnit());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        showSavedData(maxValue);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    private void showSavedData(int maxValue) {
        mObjectIcon.setImageResource(SensorUtil.getIcon(getActivity(), Storage.loadRuleSensor()));
        mObjectInfo.setText(SensorUtil.getTitle(Storage.loadRuleSensor()));
        mObjectName.setText(Storage.loadRuleTransName());

        if (Storage.isRuleEditing()) {
            int storedValue = Storage.loadRuleValue();
            mValueSeek.setProgress(storedValue);
            mValueIndicator.setText(storedValue + mSensorType.getUnit());
            toggleOperator(Storage.loadRuleOperator());
        } else {
            mValueSeek.setProgress(maxValue / 2);
            mValueIndicator.setText((maxValue / 2) + mSensorType.getUnit());
        }
    }

    @OnClick(R.id.button_done)
    public void onDoneClicked() {
        Storage.saveRuleValue(mValueSeek.getProgress());
        Storage.saveRuleOperator(mCurrentOperator);

        EventBus.getDefault().post(new WhenEvents.DoneEvent());
    }

    @OnClick(R.id.vf_operator_less)
    public void operatorLessClicked() {
        toggleOperator(OperatorType.LESS);
    }

    @OnClick(R.id.vf_operator_greater)
    public void operatorGreaterClicked() {
        toggleOperator(OperatorType.GREATER);
    }

    private void toggleOperator(OperatorType operator) {
        mCurrentOperator = operator;

        mOperatorLess.setBackgroundResource(android.R.color.transparent);
        mOperatorGreater.setBackgroundResource(android.R.color.transparent);

        switch (operator) {
            case LESS:
                mOperatorLess.setBackgroundResource(R.drawable.tab_active);
                break;
            case GREATER:
                mOperatorGreater.setBackgroundResource(R.drawable.tab_active);
                break;
        }
    }
}
