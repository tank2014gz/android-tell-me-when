package io.relayr.tellmewhen.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.OperatorType;
import io.relayr.tellmewhen.util.SensorType;
import io.relayr.tellmewhen.util.SensorUtil;

public class RuleValueView extends RelativeLayout {

    public interface OnDoneClickListener {
        public void onDoneClicked(int progress, OperatorType mCurrentOperator);
    }

    @InjectView(R.id.vf_object_icon) ImageView mObjectIcon;
    @InjectView(R.id.vf_object_name) TextView mObjectName;
    @InjectView(R.id.vf_object_info) TextView mObjectInfo;

    @InjectView(R.id.vf_operator_less) View mOperatorLess;
    @InjectView(R.id.vf_operator_greater) View mOperatorGreater;

    @InjectView(R.id.vf_rule_value_seek) SeekBar mValueSeek;
    @InjectView(R.id.vf_rule_value_indicator) TextView mValueIndicator;

    @InjectView(R.id.button_done) TextView mButtonDone;

    private int mButtonTextId;
    private OnDoneClickListener onDoneClickListener;

    private int min;
    private int max;
    private int total;
    private String unit;

    private OperatorType mOperator;
    private SensorType mSensor;
    private Integer mValue;

    public RuleValueView(Context context, SensorType sensor, OperatorType operator, Integer value) {
        this(context, null);

        this.mSensor = sensor;
        this.mOperator = operator;
        this.mValue = value;

        loadSensorData(sensor);
    }

    public RuleValueView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RuleValueView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        inflate(getContext(), R.layout.rule_value_fragment, this);

        ButterKnife.inject(this, this);

        initData();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        ButterKnife.reset(this);
    }

    @OnClick(R.id.button_done)
    public void onDoneClicked() {
        onDoneClickListener.onDoneClicked(mValueSeek.getProgress() - Math.abs(min), mOperator);
    }

    @OnClick(R.id.vf_operator_less)
    public void operatorLessClicked() {
        toggleOperator(OperatorType.LESS);
    }

    @OnClick(R.id.vf_operator_greater)
    public void operatorGreaterClicked() {
        toggleOperator(OperatorType.GREATER);
    }

    public void setOnDoneClickListener(OnDoneClickListener listener) {
        this.onDoneClickListener = listener;
    }

    public void setButtonText(int stringId) {
        this.mButtonTextId = stringId;
    }

    private void loadSensorData(SensorType sensorType) {
        unit = sensorType.getUnit();

        min = SensorUtil.getMinValue(sensorType);
        max = SensorUtil.getMaxValue(sensorType);
        total = max - min;
    }

    private void initData() {
        mButtonDone.setText(getContext().getString(mButtonTextId));

        mValueSeek.setMax(total);
        mValueSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mValueIndicator.setText((progress - Math.abs(min)) + unit);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        showSavedData();
    }

    private void showSavedData() {
        mObjectIcon.setImageResource(SensorUtil.getIcon(getContext(), mSensor));
        mObjectInfo.setText(SensorUtil.getTitle(mSensor.ordinal()));
        mObjectName.setText(Storage.getRule().transmitterName);

        if (mValue != null) setValue(mValue + Math.abs(min), mValue, mOperator);
        else setValue(total / 2, (max - Math.abs(min)) / 2, OperatorType.LESS);
    }

    private void setValue(int seekValue, int indicatorValue, OperatorType type) {
        mValueSeek.setProgress(seekValue);
        mValueIndicator.setText(indicatorValue + unit);
        toggleOperator(type);
    }

    private void toggleOperator(OperatorType operator) {
        mOperator = operator;

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
