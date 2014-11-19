package io.relayr.tellmewhen.app;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.adapter.NotificationsAdapter;
import io.relayr.tellmewhen.adapter.RulesAdapter;
import io.relayr.tellmewhen.model.Notification;
import io.relayr.tellmewhen.model.Rule;
import io.relayr.tellmewhen.model.WhenEvents;

public class RulesFragment extends Fragment {

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
    View mNavClearNotif;

    private List<Rule> rules = new ArrayList<Rule>();
    private List<Notification> notifications = new ArrayList<Notification>();

    private RulesAdapter mRulesAdapter;
    private NotificationsAdapter mNotificationsAdapter;

    public static RulesFragment newInstance() {
        return new RulesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.rules_fragment, container, false);

        ButterKnife.inject(this, view);

        initiateAdapters();
        toggleTabs(true);

//        showOnBoardWarning();
//        showRulesWarning();
        showRules();

        return view;
    }

    private void showOnBoardWarning() {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mWarningLayout.addView(inflater.inflate(R.layout.warning_onboarding, null, false));
        mWarningLayout.setVisibility(View.VISIBLE);

        mListView.setVisibility(View.GONE);
        mNavClearNotif.setVisibility(View.GONE);
        mNavNewRule.setVisibility(View.GONE);
    }

    private void showRulesWarning() {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mWarningLayout.addView(inflater.inflate(R.layout.warning_no_rules, null, false));
        mWarningLayout.setVisibility(View.VISIBLE);

        mListView.setVisibility(View.GONE);
    }

    private void initiateAdapters() {
        mRulesAdapter = new RulesAdapter(this.getActivity(), rules);
        mNotificationsAdapter = new NotificationsAdapter(this.getActivity(), notifications);
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
    public void onLogoutClick(View view) {
    }

    @OnClick(R.id.rf_controls_new_rule)
    public void onNewRuleClick(View view) {
        EventBus.getDefault().post(new WhenEvents.NewRule());
    }

    @OnClick(R.id.rf_controls_clear)
    public void onNavClearClick(View view) {
        notifications.clear();
        mNotificationsAdapter.notifyDataSetChanged();
    }

    private void showRules() {
        toggleTabs(true);

        rules.add(new Rule());

        mListView.setAdapter(mRulesAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });
    }

    private void showNotifications() {
        toggleTabs(false);

        notifications.add(new Notification());
        mListView.setAdapter(mNotificationsAdapter);
    }

    private void toggleTabs(boolean isRules) {
        mTabRules.setBackgroundResource(isRules ? R.drawable.tab_active : R.color.tab_inactive);
        mNavNewRule.setVisibility(isRules ? View.VISIBLE : View.GONE);

        mTabNotifications.setBackgroundResource(isRules ? R.color.tab_inactive : R.drawable.tab_active);
        mNavClearNotif.setVisibility(isRules ? View.GONE : View.VISIBLE);
    }
}
