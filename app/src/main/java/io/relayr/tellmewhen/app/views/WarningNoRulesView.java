package io.relayr.tellmewhen.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.storage.Storage;

public class WarningNoRulesView extends LinearLayout {

    private OnClickListener listener = null;

    @InjectView(R.id.button_done) TextView mButtonDone;

    public WarningNoRulesView(Context context, OnClickListener listener) {
        super(context);
        this.listener = listener;
    }

    public WarningNoRulesView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WarningNoRulesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        inflate(getContext(), R.layout.warning_no_rules, this);

        ButterKnife.inject(this, this);

        mButtonDone.setText(getContext().getString(R.string.warn_no_rules_btn));
    }

    @OnClick(R.id.button_done)
    public void onDoneClicked() {
        Storage.prepareRuleForCreate();

        listener.onClick(this);
    }
}
