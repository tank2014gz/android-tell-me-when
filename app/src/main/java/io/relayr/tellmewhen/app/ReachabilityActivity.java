package io.relayr.tellmewhen.app;

import android.app.Activity;
import android.os.Bundle;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.relayr.tellmewhen.R;

public class ReachabilityActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.warning_reachability);

        ButterKnife.inject(this, this);
    }

    @OnClick(R.id.button_done)
    public void onDoneClicked() {
        finish();
    }

}
