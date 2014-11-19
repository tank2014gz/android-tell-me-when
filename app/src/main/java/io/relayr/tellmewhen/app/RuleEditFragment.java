package io.relayr.tellmewhen.app;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.util.WhenEvents;

public class RuleEditFragment extends Fragment {

    public static RuleEditFragment newInstance() {
        return new RuleEditFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.rule_edit_fragment, container, false);

        ButterKnife.inject(this, view);

        ((TextView) view.findViewById(R.id.navigation_title)).setText(getString(R.string.title_edit_rule));
        ((TextView)view.findViewById(R.id.button_done)).setText(getString(R.string.button_done));

        return view;
    }

    @OnClick(R.id.button_done)
    public void onDoneClicked() {
        EventBus.getDefault().post(new WhenEvents.DoneEditEvent());
    }

    @OnClick(R.id.navigation_back)
    public void onBackClicked() {
        EventBus.getDefault().post(new WhenEvents.BackEvent());
    }

}
