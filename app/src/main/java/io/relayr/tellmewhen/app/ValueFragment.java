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
import io.relayr.tellmewhen.model.WhenEvents;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.MeasurementUtil;

public class ValueFragment extends Fragment {

    private final int OP_EQUALS = 1;
    private final int OP_LESS = 2;
    private final int OP_GREATER = 3;

    @InjectView(R.id.vf_object_icon)
    ImageView mObjectIcon;
    @InjectView(R.id.vf_object_name)
    TextView mObjectName;
    @InjectView(R.id.vf_object_info)
    TextView mObjectInfo;

    @InjectView(R.id.vf_operator_equals)
    TextView mOperatorEquals;
    @InjectView(R.id.vf_operator_less)
    TextView mOperatorLess;
    @InjectView(R.id.vf_operator_greater)
    TextView mOperatorGreater;

    @InjectView(R.id.vf_rule_value_seek)
    SeekBar mValueSeek;
    @InjectView(R.id.vf_rule_value_indicator)
    TextView mValueIndicator;

    public static ValueFragment newInstance() {
        return new ValueFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.value_fragment, container, false);

        ButterKnife.inject(this, view);

        changeOperator(OP_GREATER);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        showSavedData();

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

    private void showSavedData() {
        mObjectIcon.setImageResource(MeasurementUtil.getIcon(getActivity(), Storage.loadMeasurement()));
        mObjectInfo.setText(MeasurementUtil.getTitle(Storage.loadMeasurement()));
        mObjectName.setText(Storage.loadWunderbarName());
    }

    @OnClick(R.id.vf_button_done)
    public void onDoneClicked() {
        EventBus.getDefault().post(new WhenEvents.ValueFragDone());
    }

    @OnClick(R.id.vf_operator_equals)
    public void operatorEqualsClicked() {
        changeOperator(OP_EQUALS);
    }

    @OnClick(R.id.vf_operator_less)
    public void operatorLessClicked() {
        changeOperator(OP_LESS);
    }

    @OnClick(R.id.vf_operator_greater)
    public void operatorGreaterClicked() {
        changeOperator(OP_GREATER);
    }

    private void changeOperator(int operator) {
        mOperatorEquals.setBackgroundResource(android.R.color.transparent);
        mOperatorLess.setBackgroundResource(android.R.color.transparent);
        mOperatorGreater.setBackgroundResource(android.R.color.transparent);

        switch (operator) {
            case OP_EQUALS:
                mOperatorEquals.setBackgroundResource(R.drawable.tab_active);
                break;
            case OP_LESS:
                mOperatorLess.setBackgroundResource(R.drawable.tab_active);
                break;
            case OP_GREATER:
                mOperatorGreater.setBackgroundResource(R.drawable.tab_active);
                break;
        }
    }

    @OnClick(R.id.vf_back_button)
    public void onBackClicked() {
        EventBus.getDefault().post(new WhenEvents.BackClicked());
    }

}
