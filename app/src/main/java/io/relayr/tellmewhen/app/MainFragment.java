package io.relayr.tellmewhen.app;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

import com.activeandroid.Model;
import com.activeandroid.query.Delete;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.timroes.android.listview.EnhancedListView;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.app.adapter.NotificationsAdapter;
import io.relayr.tellmewhen.app.adapter.RulesAdapter;
import io.relayr.tellmewhen.app.views.WarningNoNotificationsView;
import io.relayr.tellmewhen.app.views.WarningNoRulesView;
import io.relayr.tellmewhen.app.views.WarningOnBoardView;
import io.relayr.tellmewhen.consts.FragmentName;
import io.relayr.tellmewhen.consts.LogUtil;
import io.relayr.tellmewhen.gcm.GcmIntentService;
import io.relayr.tellmewhen.model.TMWNotification;
import io.relayr.tellmewhen.model.TMWRule;
import io.relayr.tellmewhen.storage.Storage;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;


public class MainFragment extends WhatFragment {

    @InjectView(R.id.warning_layout) ViewGroup mWarningLayout;
    @InjectView(R.id.rules_list_view) EnhancedListView mRulesListView;
    @InjectView(R.id.notifications_list_view) EnhancedListView mNotificationsListView;

    @InjectView(R.id.tab_rules) View mTabRules;
    @InjectView(R.id.tab_notifications) View mTabNotifications;

    @InjectView(R.id.progress_bar) SmoothProgressBar mProgress;

    private RulesAdapter mRulesAdapter;
    private NotificationsAdapter mNotificationsAdapter;

    private Subscription mTransmitterSubscription = Subscriptions.empty();
    private Subscription mRulesSubscription = Subscriptions.empty();

    private MenuItem mMenuNewRule;
    private MenuItem mMenuClear;

    private boolean mLoadingNotifications = false;
    private boolean mLoadingRules = false;

    private ScheduledExecutorService mNotifScheduler;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        onCreateView(inflater, container, savedInstanceState, R.string.title_tab_rules, false);

        View view = inflater.inflate(R.layout.main_fragment, container, false);

        ButterKnife.inject(this, view);
        inject(this);

        setHasOptionsMenu(true);

        initiateAdapters();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgress.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Storage.isUserOnBoarded()) {
            if (Storage.isStartScreenRules()) {
                loadRulesData();
            } else {
                loadNotificationsData(false);
            }
            startDynamicNotificationLoading();
        } else {
            showOnBoardWarning();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        Storage.setNotificationScreeVisible(false);

        if (mNotifScheduler != null && !mNotifScheduler.isShutdown()) mNotifScheduler.shutdown();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (!mTransmitterSubscription.isUnsubscribed()) mTransmitterSubscription.unsubscribe();
        if (!mRulesSubscription.isUnsubscribed()) mRulesSubscription.unsubscribe();
    }

    @OnClick(R.id.tab_rules)
    public void onRulesClick() {
        loadRulesData();
    }

    @OnClick(R.id.tab_notifications)
    public void onNotificationsClick() {
        loadNotificationsData(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_rules, menu);

        mMenuNewRule = menu.findItem(R.id.action_new_rule);
        mMenuClear = menu.findItem(R.id.action_clear_notifications);

        refreshMenuItems();

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

            List<Model> execute = new Delete().from(TMWNotification.class).execute();
            LogUtil.logMessage(String.format(LogUtil.DELETE_ALL_NOTIFICATIONS,
                    execute != null ? "" + execute.size() : ""));

            showNoNotificationsWarning();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    void onBackPressed() {
        getActivity().onBackPressed();
    }

    private void initiateAdapters() {
        mRulesAdapter = new RulesAdapter(this.getActivity());
        mNotificationsAdapter = new NotificationsAdapter(this.getActivity());

        initRulesList();
        initNotificationList();
    }

    private void showOnBoardWarning() {
        toggleTabs(true);
        toggleWarningLayout(new WarningOnBoardView(getActivity()));
    }

    private void showRulesWarning() {
        toggleTabs(true);
        toggleWarningLayout(new WarningNoRulesView(getActivity(), new View.OnClickListener() {
            public void onClick(View v) {
                switchTo(FragmentName.TRANS);
            }
        }));
    }

    private void showNoNotificationsWarning() {
        toggleTabs(false);
        toggleWarningLayout(new WarningNoNotificationsView(getActivity()));
    }

    private void toggleWarningLayout(View view) {
        if (mWarningLayout == null)
            return;

        mNotificationsListView.setVisibility(View.GONE);
        mRulesListView.setVisibility(View.GONE);

        mWarningLayout.removeAllViews();
        mWarningLayout.setVisibility(View.VISIBLE);

        if (view != null) mWarningLayout.addView(view);
    }

    private void startDynamicNotificationLoading() {
        if (mNotifScheduler != null && !mNotifScheduler.isShutdown())
            return;

        mNotifScheduler = Executors.newSingleThreadScheduledExecutor();
        mNotifScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!Storage.isStartScreenRules()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadNotificationsData(true);
                        }
                    });
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    private void initRulesList() {
        mRulesListView.setAdapter(mRulesAdapter);
        mRulesListView.setDismissCallback(new de.timroes.android.listview.EnhancedListView.OnDismissCallback() {
            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView listView, final int position) {
                final TMWRule item = mRulesAdapter.getItem(position);

                mRulesAdapter.remove(item);
                if (mRulesAdapter.isEmpty()) showRulesWarning();

                return new EnhancedListView.Undoable() {
                    @Override
                    public void undo() {
                        toggleList(true);

                        mRulesAdapter.insert(item, position);
                        mRulesAdapter.sortRules();
                    }

                    @Override
                    public void discard() {
                        ruleService.deleteRule(item)
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
                                        if (!status) {
                                            undo();
                                        } else {
                                            LogUtil.logMessage(LogUtil.DELETE_RULE);
                                            item.delete();
                                        }
                                    }
                                });
                    }
                };
            }
        });

        mRulesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                Storage.prepareRuleForEdit(mRulesAdapter.getItem(pos));
                switchTo(FragmentName.RULE_EDIT);
            }
        });

        initList(mRulesListView);
        refreshMenuItems();
    }

    private void toggleList(boolean rules) {
        mWarningLayout.setVisibility(View.GONE);

        mRulesListView.setVisibility(rules ? View.VISIBLE : View.GONE);
        mNotificationsListView.setVisibility(rules ? View.GONE : View.VISIBLE);
    }

    private void initNotificationList() {
        mNotificationsListView.setAdapter(mNotificationsAdapter);
        mNotificationsListView.setDismissCallback(new de.timroes.android.listview.EnhancedListView.OnDismissCallback() {
            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView listView, final int position) {
                final TMWNotification item = mNotificationsAdapter.getItem(position);
                mNotificationsAdapter.remove(item);

                if (mNotificationsAdapter.isEmpty()) showNoNotificationsWarning();

                return new EnhancedListView.Undoable() {
                    @Override
                    public void undo() {
                        toggleList(false);

                        mNotificationsAdapter.add(item);
                        mNotificationsAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void discard() {
                        LogUtil.logMessage(LogUtil.DELETE_NOTIFICATION);
                        item.delete();
                    }
                };
            }
        });


        mNotificationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                Storage.showNotification(mNotificationsAdapter.getItem(pos));
                switchTo(FragmentName.NOTIFICATION_DETAILS);
            }
        });

        initList(mNotificationsListView);
        enableDynamicLoading();
        refreshMenuItems();
    }

    private void initList(EnhancedListView list) {
        list.setSwipingLayout(R.id.main_list_object);
        list.setUndoStyle(EnhancedListView.UndoStyle.SINGLE_POPUP);
        list.enableSwipeToDismiss();
        list.setUndoHideDelay(3000);
        list.setRequireTouchBeforeDismiss(false);
        list.setSwipeDirection(EnhancedListView.SwipeDirection.START);
    }

    private void enableDynamicLoading() {
        mNotificationsListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisible, int visibleCount, int total) {
                int lastInScreen = firstVisible + visibleCount;

                if ((lastInScreen == total)) {
                    List<TMWNotification> local = notificationService.getLocalNotifications(total);
                    if (!local.isEmpty()) {
                        mNotificationsAdapter.addAll(local);
                        mNotificationsAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    private void toggleTabs(boolean isRules) {
        if (!Storage.isUserOnBoarded()) return;

        Storage.startRuleScreen(isRules);
        Storage.setNotificationScreeVisible(!isRules);

        getActivity().setTitle(isRules ? R.string.title_tab_rules : R.string.title_tab_notifications);

        mTabRules.setBackgroundResource(isRules ? R.drawable.tab_active : R.color.tab_inactive);
        mTabNotifications.setBackgroundResource(isRules ? R.color.tab_inactive : R.drawable.tab_active);

        refreshMenuItems();
    }

    private void refreshMenuItems() {
        if (mMenuNewRule != null)
            mMenuNewRule.setVisible(Storage.isStartScreenRules() && Storage.isUserOnBoarded());
        if (mMenuClear != null)
            mMenuClear.setVisible(!Storage.isStartScreenRules() && !mNotificationsAdapter.isEmpty());
    }

    private void stopProgressBar() {
        if (mProgress != null)
            mProgress.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mProgress != null)
                        mProgress.progressiveStop();
                }
            }, 100);
    }

    private void loadRulesData() {
        if (!Storage.isUserOnBoarded() || mLoadingRules) return;

        mLoadingRules = true;
        mLoadingNotifications = false;

        mProgress.setVisibility(View.VISIBLE);
        mProgress.progressiveStart();

        toggleTabs(true);

        ruleService.loadRemoteRules()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<TMWRule>>() {
                    @Override
                    public void onCompleted() {
                        mLoadingRules = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                        stopProgressBar();
                        showToast(R.string.error_loading_rules);
                        mLoadingRules = false;
                    }

                    @Override
                    public void onNext(List<TMWRule> rules) {
                        mRulesAdapter.clear();
                        mRulesAdapter.addAll(rules);
                        mRulesAdapter.sortRules();

                        refreshMenuItems();

                        stopProgressBar();

                        if (mRulesAdapter.isEmpty()) {
                            showRulesWarning();
                        } else {
                            toggleList(true);
                        }

                        mLoadingRules = false;
                    }
                });
    }

    private void loadNotificationsData(final boolean dynamic) {
        if (!Storage.isUserOnBoarded() || mLoadingNotifications) return;

        mLoadingRules = false;
        mLoadingNotifications = true;

        mProgress.setVisibility(View.VISIBLE);
        mProgress.progressiveStart();

        toggleTabs(false);
        clearStatusBar();

        notificationService.loadRemoteNotifications()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                        mLoadingNotifications = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                        stopProgressBar();
                        showToast(R.string.error_loading_notifications);
                        mLoadingNotifications = false;
                    }

                    @Override
                    public void onNext(Integer totalNotifications) {
                        if (!dynamic || totalNotifications > 0) {
                            mNotificationsAdapter.clear();
                            mNotificationsAdapter.addAll(notificationService.getLocalNotifications(0));

                            if (mNotificationsAdapter.isEmpty()) {
                                showNoNotificationsWarning();
                            } else {
                                toggleList(false);
                            }
                        }

                        refreshMenuItems();

                        stopProgressBar();
                        mLoadingNotifications = false;
                    }
                });
    }

    private void clearStatusBar() {
        NotificationManager manager = (NotificationManager) getActivity()
                .getSystemService(Context.NOTIFICATION_SERVICE);

        manager.cancel(GcmIntentService.TMW_NOTIFICATION_ID);
    }
}
