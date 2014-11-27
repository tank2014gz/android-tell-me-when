package io.relayr.tellmewhen.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.timroes.android.listview.EnhancedListView;
import io.relayr.RelayrSdk;
import io.relayr.model.Transmitter;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.adapter.NotificationsAdapter;
import io.relayr.tellmewhen.adapter.RulesAdapter;
import io.relayr.tellmewhen.app.views.WarningNoRulesView;
import io.relayr.tellmewhen.app.views.WarningOnBoardView;
import io.relayr.tellmewhen.model.Rule;
import io.relayr.tellmewhen.model.RuleNotification;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.FragmentName;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class MainFragment extends WhatFragment {

    private final String CURRENT_TAB = "io.relayr.tmw.current.tab";

    @InjectView(R.id.warning_layout) ViewGroup mWarningLayout;
    @InjectView(R.id.list_view) EnhancedListView mListView;

    @InjectView(R.id.tab_rules) View mTabRules;
    @InjectView(R.id.tab_notifications) View mTabNotifications;

    private RulesAdapter mRulesAdapter;
    private NotificationsAdapter mNotificationsAdapter;

    private Subscription mTransmitterSubscription = Subscriptions.empty();
    private Subscription mRulesSubscription = Subscriptions.empty();

    private MenuItem mMenuNewRule;
    private MenuItem mMenuClearItem;

    private boolean isNotificationsEmpty = true;
    private boolean isRules = true;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        onCreateView(inflater, container, savedInstanceState, R.string.title_tab_rules);

        View view = inflater.inflate(R.layout.main_fragment, container, false);

        ButterKnife.inject(this, view);

        setHasOptionsMenu(true);

        initiateAdapters();

        if (savedInstanceState != null) toggleTabs(savedInstanceState.getBoolean(CURRENT_TAB));
        else toggleTabs(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Storage.isUserOnBoarded()) checkRules();
        else checkOnBoarding();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (!mTransmitterSubscription.isUnsubscribed()) mTransmitterSubscription.unsubscribe();
        if (!mRulesSubscription.isUnsubscribed()) mRulesSubscription.unsubscribe();
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
            Storage.prepareRuleForCreate();
            switchTo(FragmentName.TRANS);
        }

        if (item.getItemId() == R.id.action_clear_notifications) {
            mNotificationsAdapter.clear();
            mNotificationsAdapter.notifyDataSetChanged();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(CURRENT_TAB, isRules);
    }

    @Override
    void onBackPressed() {
        getActivity().onBackPressed();
    }

    private void initiateAdapters() {
        mRulesAdapter = new RulesAdapter(this.getActivity());
        mNotificationsAdapter = new NotificationsAdapter(this.getActivity());
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
                        Storage.userOnBoarded(!transmitters.isEmpty());

                        if (transmitters.isEmpty()) showOnBoardWarning();
                        else checkRules();
                    }
                });
    }

    private void checkRules() {
        mRulesAdapter.clear();
        mRulesAdapter.addAll(getRuleService().getLocalRules());

        if (mRulesAdapter.isEmpty()) showRulesWarning();
        else showRules();
    }

    private void showOnBoardWarning() {
        toggleMenuItems();
        toggleWarningLayout(new WarningOnBoardView(getActivity()));
    }

    private void showRulesWarning() {
        toggleWarningLayout(new WarningNoRulesView(getActivity(), new View.OnClickListener() {
            public void onClick(View v) {
                switchTo(FragmentName.TRANS);
            }
        }));
    }

    private void toggleWarningLayout(View view) {
        mWarningLayout.removeAllViews();
        if (view != null) mWarningLayout.addView(view);

        mWarningLayout.setVisibility(view != null ? View.VISIBLE : View.GONE);
        mListView.setVisibility(view != null ? View.GONE : View.VISIBLE);
    }

    private void showRules() {
        toggleTabs(true);

        mListView.setAdapter(mRulesAdapter);
        mListView.setDismissCallback(new de.timroes.android.listview.EnhancedListView.OnDismissCallback() {
            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView listView, final int position) {
                final Rule item = mRulesAdapter.getItem(position);

                mRulesAdapter.remove(item);
                if (mRulesAdapter.isEmpty()) showRulesWarning();

                return new EnhancedListView.Undoable() {
                    @Override
                    public void undo() {
                        toggleWarningLayout(null);

                        mRulesAdapter.insert(item, position);
                        mRulesAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void discard() {
                        getRuleService().deleteRule(item)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<Boolean>() {
                                    @Override
                                    public void onCompleted() {
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        undo();
                                    }

                                    @Override
                                    public void onNext(Boolean status) {
                                        if (!status) undo();
                                    }
                                });
                    }
                };
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                Storage.prepareRuleForEdit(mRulesAdapter.getItem(pos));
                switchTo(FragmentName.RULE_EDIT);
            }
        });

        initListView();
    }

    private void showNotifications() {
        toggleTabs(false);

        mNotificationsAdapter.addAll(getNotifService().getLocalNotifications());
        isNotificationsEmpty = mNotificationsAdapter.isEmpty();

        mListView.setAdapter(mNotificationsAdapter);
        mListView.setDismissCallback(new de.timroes.android.listview.EnhancedListView.OnDismissCallback() {
            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView listView, final int position) {
                final RuleNotification item = mNotificationsAdapter.getItem(position);
                mNotificationsAdapter.remove(item);

                return new EnhancedListView.Undoable() {
                    @Override
                    public void undo() {
                        mNotificationsAdapter.insert(item, position);
                        mNotificationsAdapter.notifyDataSetChanged();
                    }
                };
            }
        });

        mListView.setOnItemClickListener(null);

        initListView();
    }

    private void initListView() {
        mListView.setSwipingLayout(R.id.swipe_object);
        mListView.setUndoStyle(EnhancedListView.UndoStyle.COLLAPSED_POPUP);
        mListView.enableSwipeToDismiss();
        mListView.setUndoHideDelay(5000);
        mListView.setRequireTouchBeforeDismiss(false);
        mListView.setSwipeDirection(EnhancedListView.SwipeDirection.START);
    }

    private void toggleTabs(boolean isRules) {
        this.isRules = isRules;

        getActivity().setTitle(isRules ? R.string.title_tab_rules : R.string.title_tab_notifications);

        mTabRules.setBackgroundResource(isRules ? R.drawable.tab_active : R.color.tab_inactive);
        mTabNotifications.setBackgroundResource(isRules ? R.color.tab_inactive : R.drawable.tab_active);

        toggleMenuItems();
    }

    private void toggleMenuItems() {
        if (mMenuNewRule != null) mMenuNewRule.setVisible(isRules && Storage.isUserOnBoarded());
        if (mMenuClearItem != null) mMenuClearItem.setVisible(!isRules && !isNotificationsEmpty);
    }
}
