package io.relayr.tellmewhen.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import io.relayr.RelayrSdk;
import io.relayr.model.Reading;
import io.relayr.model.TransmitterDevice;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.consts.OperatorType;
import io.relayr.tellmewhen.consts.SensorType;
import io.relayr.tellmewhen.util.SensorUtil;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class RuleValueView extends RelativeLayout {

    public interface OnDoneClickListener {
        public void onDoneClicked(float progress, OperatorType mCurrentOperator);
    }

    @InjectView(R.id.vf_object_icon) ImageView mObjectIcon;
    @InjectView(R.id.vf_object_name) TextView mObjectName;
    @InjectView(R.id.vf_object_info) TextView mObjectInfo;

    @InjectView(R.id.vf_operator_less) View mOperatorLess;
    @InjectView(R.id.vf_operator_greater) View mOperatorGreater;

    @InjectView(R.id.vf_rule_value_seek) SeekBar mValueSeek;
    @InjectView(R.id.vf_rule_value_indicator) TextView mValueIndicator;

    @InjectView(R.id.button_done) TextView mButtonDone;

    @InjectView(R.id.sensor_value) TextView mSensorValue;
    @InjectView(R.id.notif_details_current_sensor_loading) ProgressBar mCurrentSensorProgress;

    private int mButtonTextId;
    private OnDoneClickListener onDoneClickListener;

    private int min;
    private int max;
    private int total;
    private String unit;

    private String mDeviceId;

    private boolean mUnSubscribe;
    private OperatorType mOperator;
    private SensorType mSensor;
    private Float mValue;

    private Subscription mDeviceSubscription = Subscriptions.empty();

    public RuleValueView(Context context, boolean unSubscribe, SensorType sensor,
                         OperatorType operator, Float value) {
        this(context, null);

        this.mUnSubscribe = unSubscribe;
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

        loadDevice();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        ButterKnife.reset(this);

        if (mUnSubscribe) {
            if (!mDeviceSubscription.isUnsubscribed()) mDeviceSubscription.unsubscribe();
            if (mDeviceId != null) RelayrSdk.getWebSocketClient().unSubscribe(mDeviceId);
        }
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
        mObjectInfo.setText(SensorUtil.getTitle(mSensor));
        mObjectName.setText(Storage.getRule().transmitterName);

        if (mValue != null) setValue(mValue + Math.abs(min), mValue, mOperator);
        else setValue(total / 2, (max - Math.abs(min)) / 2, OperatorType.GREATER);
    }

    private void setValue(float seekValue, float indicatorValue, OperatorType type) {
        mValueSeek.setProgress((int) seekValue);
        mValueIndicator.setText((int) indicatorValue + unit);
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

    private void loadDevice() {
        mCurrentSensorProgress.setVisibility(View.VISIBLE);

        if (mDeviceId != null) return;

        mDeviceSubscription = RelayrSdk.getRelayrApi()
                .getTransmitterDevices(Storage.getRule().transmitterId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<TransmitterDevice>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        showToast(R.string.error_loading_device_data);
                    }

                    @Override
                    public void onNext(List<TransmitterDevice> transmitterDevices) {
                        for (TransmitterDevice device : transmitterDevices) {
                            if (device.getModel().equals(mSensor.getModel()))
                                subscribeForDeviceReadings(device);
                        }
                    }
                });
    }

    private void subscribeForDeviceReadings(TransmitterDevice device) {
        mDeviceId = device.id;
        RelayrSdk.getWebSocketClient().subscribe(device)
                .timeout(7, TimeUnit.SECONDS)
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        showToast(R.string.error_loading_device_data);

                        if (mCurrentSensorProgress != null)
                            mCurrentSensorProgress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onNext(Object o) {
                        Reading reading = new Gson().fromJson(o.toString(), Reading.class);

                        if (mCurrentSensorProgress != null) {
                            mCurrentSensorProgress.setVisibility(View.GONE);
                            mSensorValue.setVisibility(View.VISIBLE);

                            mSensorValue.setText(getContext().getString(R.string
                                    .current_reading) + ": " +
                                    SensorUtil.formatToUiValue(mSensor, reading));
                        }
                    }
                });
    }

    private void showToast(int stringId) {
        if (getContext() != null)
            Toast.makeText(getContext(), getContext().getString(stringId), Toast.LENGTH_SHORT).show();
    }
}
