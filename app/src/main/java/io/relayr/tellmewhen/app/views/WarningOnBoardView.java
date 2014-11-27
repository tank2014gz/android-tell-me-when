package io.relayr.tellmewhen.app.views;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import io.relayr.tellmewhen.R;

public class WarningOnBoardView extends LinearLayout {

    private final String APP_PACKAGE = "io.relayr.wunderbar";

    @InjectView(R.id.button_done) TextView mButtonDone;

    public WarningOnBoardView(Context context) {
        this(context, null);
    }

    public WarningOnBoardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WarningOnBoardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        inflate(getContext(), R.layout.warning_onboarding, this);

        ButterKnife.inject(this, this);

        mButtonDone.setText(getContext().getString(R.string.warning_onboard_btn_text));
    }

    @OnClick(R.id.button_done)
    public void onDoneClicked() {
        PackageManager manager = getContext().getPackageManager();
        try {
            Intent startApp = manager.getLaunchIntentForPackage(APP_PACKAGE);
            if (startApp == null)  throw new PackageManager.NameNotFoundException();

            startApp.addCategory(Intent.CATEGORY_LAUNCHER);
            getContext().startActivity(startApp);
        } catch (PackageManager.NameNotFoundException e) {
            try {
                getContext().startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + APP_PACKAGE)));
            } catch (android.content.ActivityNotFoundException anfe) {
                getContext().startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + APP_PACKAGE)));
            }
        }
    }
}
