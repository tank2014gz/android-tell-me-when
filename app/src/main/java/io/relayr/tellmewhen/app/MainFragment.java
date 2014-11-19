package io.relayr.tellmewhen.app;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import io.relayr.RelayrSdk;
import io.relayr.model.Transmitter;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.adapter.NotificationsAdapter;
import io.relayr.tellmewhen.adapter.RulesAdapter;
import io.relayr.tellmewhen.model.Notification;
import io.relayr.tellmewhen.model.Rule;
import io.relayr.tellmewhen.util.WhenEvents;
import io.relayr.tellmewhen.service.RuleService;
import io.relayr.tellmewhen.storage.Storage;
import rx.Subscriber;

public class MainFragment extends Fragment {

    private static final String ON_BOARD_APP_PACKAGE = "io.relayr.wunderbar";

    @InjectView(R.id.rf_warning_layout)
    ViewGroup mWarningLayout;
    @InjectView(R.id.rf_list_view)
    ListView mListView;

    @InjectView(R.id.rf_tab_rules)
    View mTabRules;
    @InjectView(R.id.rf_tab_notifications)
    View mTabNotifications;

    @InjectView(R.id.rf_controls_new_rule)
    View mNavNewRule;
    @InjectView(R.id.rf_controls_clear)
    View mNavClear;
    @InjectView(R.id.rf_controls_title)
    TextView mNavTitle;

    private List<Rule> mRules = new ArrayList<Rule>();
    private List<Notification> mNotifications = new ArrayList<Notification>();

    private RulesAdapter mRulesAdapter;
    private NotificationsAdapter mNotificationsAdapter;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment, container, false);

        ButterKnife.inject(this, view);

        initiateAdapters();
        toggleTabs(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!Storage.transmitterExists()) checkOnBoarding();
        else checkRules();
    }

    private void checkOnBoarding() {
        RelayrSdk.getRelayrApi().getTransmitters(Storage.loadUserId()).subscribe(new Subscriber<List<Transmitter>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(List<Transmitter> transmitters) {
                Storage.saveTransmiterState(!transmitters.isEmpty());

                if (transmitters.isEmpty()) showOnBoardWarning();
                else checkRules();
            }
        });
    }

    private void showOnBoardWarning() {
        LayoutInflater inflater = (LayoutInflater) getActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.warning_onboarding, null, false);
        ((TextView)view.findViewById(R.id.button_done)).setText(getString(R.string.warning_onboard_btn_text));
        view.findViewById(R.id.button_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + ON_BOARD_APP_PACKAGE)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + ON_BOARD_APP_PACKAGE)));
                }
            }
        });

        mWarningLayout.addView(view);
        mWarningLayout.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        mNavClear.setVisibility(View.GONE);
        mNavNewRule.setVisibility(View.GONE);
    }

    private void checkRules() {
        loadRules();

        if (mRules.isEmpty()) showRulesWarning();
        else showRules();
    }

    private void showRulesWarning() {
        LayoutInflater inflater = (LayoutInflater) getActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.warning_no_rules, null, false);
        ((TextView)view.findViewById(R.id.button_done)).setText(getString(R.string.warning_no_rules_btn_text));
        view.findViewById(R.id.button_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new WhenEvents.DoneEvent());
            }
        });

        mWarningLayout.addView(view);
        mWarningLayout.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
    }

    private void initiateAdapters() {
        mRulesAdapter = new RulesAdapter(this.getActivity(), mRules);
        mNotificationsAdapter = new NotificationsAdapter(this.getActivity(), mNotifications);
    }

    @OnClick(R.id.rf_tab_rules)
    public void onRulesClick() {
        showRules();
    }

    @OnClick(R.id.rf_tab_notifications)
    public void onNotificationsClick() {
        showNotifications();
    }

    @OnClick(R.id.rf_controls_logout)
    public void onLogoutClick() {
        if (RelayrSdk.isUserLoggedIn()) {
            RelayrSdk.logOut();
            EventBus.getDefault().post(new WhenEvents.BackEvent());
        }
    }

    @OnClick(R.id.rf_controls_new_rule)
    public void onNewRuleClick() {
        EventBus.getDefault().post(new WhenEvents.DoneEvent());
    }

    @OnClick(R.id.rf_controls_clear)
    public void onNavClearClick() {
        mNotifications.clear();
        mNotificationsAdapter.notifyDataSetChanged();
    }

    private void showRules() {
        toggleTabs(true);
        loadRules();

        mListView.setAdapter(mRulesAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EventBus.getDefault().post(new WhenEvents.DoneEvent());
            }
        });
    }

    private void loadRules() {
        mRules.clear();
        mRules.addAll(RuleService.getsDbRules());
    }

    private void showNotifications() {
        toggleTabs(false);
        loadNotifications();

        mListView.setAdapter(mNotificationsAdapter);
    }

    private void loadNotifications() {
        mNotifications.add(new Notification());
    }

    private void toggleTabs(boolean isRules) {
        mNavTitle.setText(getString(isRules ? R.string.ma_tab_rules : R.string.ma_tab_notification));

        mTabRules.setBackgroundResource(isRules ? R.drawable.tab_active : R.color.tab_inactive);
        mNavNewRule.setVisibility(isRules ? View.VISIBLE : View.GONE);

        mTabNotifications.setBackgroundResource(isRules ? R.color.tab_inactive : R.drawable.tab_active);
        mNavClear.setVisibility(isRules ? View.GONE : View.VISIBLE);
    }
}
