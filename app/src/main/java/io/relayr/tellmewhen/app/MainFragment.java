package io.relayr.tellmewhen.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    private RulesAdapter mRulesAdapter;
    private NotificationsAdapter mNotificationsAdapter;

    private Subscription mTransmitterSubscription = Subscriptions.empty();

    private MenuItem mMenuNewRule;
    private MenuItem mMenuClearItem;

    private boolean isOnBoarded = true;
    private boolean isNotificationsEmpty = true;
    private boolean isRules = true;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment, container, false);

        ButterKnife.inject(this, view);

        setHasOptionsMenu(true);

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_rules, menu);

        mMenuNewRule = menu.findItem(R.id.action_new_rule);
        mMenuClearItem = menu.findItem(R.id.action_clear_notifications);

        toggleMenuItems();

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_new_rule) {
            EventBus.getDefault().post(new WhenEvents.DoneEvent());
        }

        if (item.getItemId() == R.id.action_clear_notifications) {
            mNotificationsAdapter.clear();
            mNotificationsAdapter.notifyDataSetChanged();
        }

        return super.onOptionsItemSelected(item);
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

                        isOnBoarded = !transmitters.isEmpty();

                        if (transmitters.isEmpty()) showOnBoardWarning();
                        else checkRules();
                    }
                });
    }

    private void showOnBoardWarning() {
        toggleMenuItems();

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

        toggleWarningLayout(view);
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

        toggleWarningLayout(view);
    }

    private void toggleWarningLayout(View view) {
        mWarningLayout.removeAllViews();
        mWarningLayout.addView(view);

        mWarningLayout.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
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
        isNotificationsEmpty = mNotificationsAdapter.isEmpty();
    }

    private void toggleTabs(boolean isRules) {
        this.isRules = isRules;

        getActivity().setTitle(isRules ? R.string.title_tab_rules : R.string.title_tab_notifications);

        mTabRules.setBackgroundResource(isRules ? R.drawable.tab_active : R.color.tab_inactive);
        mTabNotifications.setBackgroundResource(isRules ? R.color.tab_inactive : R.drawable.tab_active);

        toggleMenuItems();
    }

    private void toggleMenuItems() {
        if (mMenuNewRule != null) mMenuNewRule.setVisible(isRules && isOnBoarded);
        if (mMenuClearItem != null) mMenuClearItem.setVisible(!isRules && !isNotificationsEmpty);
    }
}
