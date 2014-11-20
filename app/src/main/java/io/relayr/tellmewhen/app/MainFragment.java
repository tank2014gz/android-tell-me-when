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
import io.relayr.tellmewhen.service.RuleService;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.WhenEvents;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class MainFragment extends Fragment {

    private static final String ON_BOARD_APP_PACKAGE = "io.relayr.wunderbar";

    @InjectView(R.id.warning_layout) ViewGroup mWarningLayout;
    @InjectView(R.id.list_view) ListView mListView;

    @InjectView(R.id.tab_rules) View mTabRules;
    @InjectView(R.id.tab_notifications) View mTabNotifications;

    private View mNewRuleBtn;
    private View mClearNotificationsBtn;
    private TextView mNavTitle;

    private RulesAdapter mRulesAdapter;
    private NotificationsAdapter mNotificationsAdapter;

    private Subscription mTransmitterSubscription = Subscriptions.empty();

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment, container, false);

        ButterKnife.inject(this, view);

        mNewRuleBtn = getActivity().findViewById(R.id.navigation_new_rule);
        mNewRuleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new WhenEvents.DoneEvent());
            }
        });

        mClearNotificationsBtn = getActivity().findViewById(R.id.navigation_clear_notif);
        mClearNotificationsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNotificationsAdapter.clear();
                mNotificationsAdapter.notifyDataSetChanged();
            }
        });

        mNavTitle = (TextView) getActivity().findViewById(R.id.navigation_title);

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (!mTransmitterSubscription.isUnsubscribed()) mTransmitterSubscription.unsubscribe();
    }

    @OnClick(R.id.tab_rules)
    public void onRulesClick() {
        showRules();
    }

    @OnClick(R.id.tab_notifications)
    public void onNotificationsClick() {
        showNotifications();
    }

    private void checkOnBoarding() {
        mTransmitterSubscription = RelayrSdk.getRelayrApi()
                .getTransmitters(Storage.loadUserId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Transmitter>>() {
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
        ((TextView) view.findViewById(R.id.button_done)).setText(getString(R.string.warning_onboard_btn_text));
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

        toggleWarningLayout(view, false);
    }

    private void checkRules() {
        loadRules();

        if (mRulesAdapter.isEmpty()) showRulesWarning();
        else showRules();
    }

    private void showRulesWarning() {
        LayoutInflater inflater = (LayoutInflater) getActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.warning_no_rules, null, false);
        ((TextView) view.findViewById(R.id.button_done)).setText(getString(R.string.warning_no_rules_btn_text));
        view.findViewById(R.id.button_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new WhenEvents.DoneEvent());
            }
        });

        toggleWarningLayout(view, true);
    }

    private void toggleWarningLayout(View view, boolean onBoardDone) {
        mWarningLayout.removeAllViews();
        mWarningLayout.addView(view);

        mWarningLayout.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);

        mNewRuleBtn.setVisibility(onBoardDone ? View.VISIBLE : View.GONE);
    }

    private void initiateAdapters() {
        mRulesAdapter = new RulesAdapter(this.getActivity());
        mNotificationsAdapter = new NotificationsAdapter(this.getActivity());
    }

    private void showRules() {
        toggleTabs(true);
        loadRules();

        mListView.setAdapter(mRulesAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                EventBus.getDefault().post(new WhenEvents.StartEditEvent(mRulesAdapter.getItem(pos)));
            }
        });
    }

    private void loadRules() {
        mRulesAdapter.clear();
        mRulesAdapter.addAll(RuleService.getsDbRules());
    }

    private void showNotifications() {
        toggleTabs(false);
        loadNotifications();

        mListView.setAdapter(mNotificationsAdapter);
    }

    private void loadNotifications() {
        mNotificationsAdapter.add(new Notification());
    }

    private void toggleTabs(boolean isRules) {
        mNavTitle.setText(getString(isRules ? R.string.title_tab_rules : R.string.title_tab_notifications));

        mTabRules.setBackgroundResource(isRules ? R.drawable.tab_active : R.color.tab_inactive);
        mNewRuleBtn.setVisibility(isRules ? View.VISIBLE : View.GONE);

        mTabNotifications.setBackgroundResource(isRules ? R.color.tab_inactive : R.drawable.tab_active);
        mClearNotificationsBtn.setVisibility(isRules ? View.GONE : View.VISIBLE);
    }
}
