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

public class WarningNoNotificationsView extends LinearLayout {

    public WarningNoNotificationsView(Context context) {
        super(context, null);
    }

    public WarningNoNotificationsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WarningNoNotificationsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        inflate(getContext(), R.layout.warning_no_notifications, this);
    }
}
