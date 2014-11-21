package io.relayr.tellmewhen.app;

import android.app.Fragment;
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
import io.relayr.tellmewhen.util.SensorUtil;
import io.relayr.tellmewhen.util.WhenEvents;

public class RuleValueFragment extends Fragment {

    @InjectView(R.id.vf_object_icon) ImageView mObjectIcon;
    @InjectView(R.id.vf_object_name) TextView mObjectName;
    @InjectView(R.id.vf_object_info) TextView mObjectInfo;

    //    @InjectView(R.id.vf_operator_equals) TextView mOperatorEquals;
    @InjectView(R.id.vf_operator_less) View mOperatorLess;
    @InjectView(R.id.vf_operator_greater) View mOperatorGreater;

    @InjectView(R.id.vf_rule_value_seek) SeekBar mValueSeek;
    @InjectView(R.id.vf_rule_value_indicator) TextView mValueIndicator;

    private OperatorType mCurrentOperator;

    public static RuleValueFragment newInstance() {
        return new RuleValueFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.rule_value_fragment, container, false);

        ButterKnife.inject(this, view);
        EventBus.getDefault().post(new WhenEvents.TitleChangeEvent(R.string
                .title_select_sensor));

        ((TextView) view.findViewById(R.id.button_done)).setText(getString(R.string.button_done));

        toggleOperator(OperatorType.LESS);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        showSavedData();

        mValueIndicator.setText("" + 50);

        mValueSeek.setMax(100);
        mValueSeek.setProgress(50);
        mValueSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mValueIndicator.setText("" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    private void showSavedData() {
        mObjectIcon.setImageResource(SensorUtil.getIcon(getActivity(), Storage.loadRuleSensor()));
        mObjectInfo.setText(SensorUtil.getTitle(Storage.loadRuleSensor()));
        mObjectName.setText(Storage.loadRuleTransName());

        if (Storage.isRuleEditing()) {
            mValueSeek.setProgress(Storage.loadRuleValue());
            mValueIndicator.setText("" + Storage.loadRuleValue());
            toggleOperator(Storage.loadRuleOperator());
        }
    }

    @OnClick(R.id.button_done)
    public void onDoneClicked() {
        Storage.saveRuleValue(mValueSeek.getProgress());
        Storage.saveRuleOperator(mCurrentOperator);

        EventBus.getDefault().post(new WhenEvents.DoneEvent());
    }

//    @OnClick(R.id.vf_operator_equals)
//    public void operatorEqualsClicked() {
//        toggleOperator(OperatorType.EQUALS);
//    }

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

//        mOperatorEquals.setBackgroundResource(android.R.color.transparent);
        mOperatorLess.setBackgroundResource(android.R.color.transparent);
        mOperatorGreater.setBackgroundResource(android.R.color.transparent);

        switch (operator) {
//            case EQUALS:
//                mOperatorEquals.setBackgroundResource(R.drawable.tab_active);
//                break;
            case LESS:
                mOperatorLess.setBackgroundResource(R.drawable.tab_active);
                break;
            case GREATER:
                mOperatorGreater.setBackgroundResource(R.drawable.tab_active);
                break;
        }
    }
}
