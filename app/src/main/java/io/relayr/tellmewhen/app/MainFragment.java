package io.relayr.tellmewhen.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.activeandroid.query.Delete;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.timroes.android.listview.EnhancedListView;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.app.adapter.NotificationsAdapter;
import io.relayr.tellmewhen.app.adapter.RulesAdapter;
import io.relayr.tellmewhen.app.views.WarningNoRulesView;
import io.relayr.tellmewhen.app.views.WarningOnBoardView;
import io.relayr.tellmewhen.model.TMWRule;
import io.relayr.tellmewhen.model.TMWNotification;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.FragmentName;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;


public class MainFragment extends WhatFragment {

    @InjectView(R.id.warning_layout) ViewGroup mWarningLayout;
    @InjectView(R.id.list_view) EnhancedListView mListView;

    @InjectView(R.id.tab_rules) View mTabRules;
    @InjectView(R.id.tab_notifications) View mTabNotifications;

    @InjectView(R.id.progress_bar) SmoothProgressBar mProgress;

    private RulesAdapter mRulesAdapter;
    private NotificationsAdapter mNotificationsAdapter;

    private Subscription mTransmitterSubscription = Subscriptions.empty();
    private Subscription mRulesSubscription = Subscriptions.empty();

    private MenuItem mMenuNewRule;
    private MenuItem mMenuClear;

    public TMWNotification mSelectedNotification;

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
    public void onResume() {
        super.onResume();

        if (Storage.isUserOnBoarded())
            if (Storage.isStartScreenRules()) loadRulesData();
            else loadNotificationsData();
        else showOnBoardWarning();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mProgress != null) mProgress.setVisibility(View.GONE);
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
        loadNotificationsData();
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
            new Delete().from(TMWNotification.class).execute();
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

    private void toggleWarningLayout(View view) {
        mWarningLayout.removeAllViews();
        if (view != null) mWarningLayout.addView(view);

        mWarningLayout.setVisibility(view != null ? View.VISIBLE : View.GONE);
        mListView.setVisibility(view != null ? View.GONE : View.VISIBLE);
    }

    private void showRules() {
        mListView.setAdapter(mRulesAdapter);
        mListView.setDismissCallback(new de.timroes.android.listview.EnhancedListView.OnDismissCallback() {
            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView listView, final int position) {
                final TMWRule item = mRulesAdapter.getItem(position);

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
                                        if (!status) undo();
                                        else item.delete();
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
        mNotificationsAdapter.clear();
        mNotificationsAdapter.addAll(notificationService.getLocalNotifications());

        mListView.setAdapter(mNotificationsAdapter);
        mListView.setDismissCallback(new de.timroes.android.listview.EnhancedListView.OnDismissCallback() {
            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView listView, final int position) {
                final TMWNotification item = mNotificationsAdapter.getItem(position);
                mNotificationsAdapter.remove(item);

                return new EnhancedListView.Undoable() {
                    @Override
                    public void undo() {
                        mNotificationsAdapter.insert(item, position);
                        mNotificationsAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void discard() {
                        item.delete();
                    }
                };
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                Storage.showNotification(mNotificationsAdapter.getItem(pos));
                switchTo(FragmentName.NOTIFICATION_DETAILS);
            }
        });

        initListView();
    }

    private void initListView() {
        mListView.setSwipingLayout(R.id.swipe_object);
        mListView.setUndoStyle(EnhancedListView.UndoStyle.SINGLE_POPUP);
        mListView.enableSwipeToDismiss();
        mListView.setUndoHideDelay(5000);
        mListView.setRequireTouchBeforeDismiss(false);
        mListView.setSwipeDirection(EnhancedListView.SwipeDirection.START);
    }

    private void toggleTabs(boolean isRules) {
        Storage.startRuleScreen(isRules);

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
        mProgress.progressiveStart();
        toggleTabs(true);

        ruleService.loadRemoteRules()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<TMWRule>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        stopProgressBar();
                        Toast.makeText(getActivity(), R.string.error_loading_rules,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(List<TMWRule> rules) {
                        mRulesAdapter.clear();
                        mRulesAdapter.addAll(rules);

                        stopProgressBar();

                        if (rules.isEmpty()) showRulesWarning();
                        else showRules();
                    }
                });
    }

    private void loadNotificationsData() {
        mProgress.progressiveStart();
        toggleTabs(false);

        notificationService.loadRemoteNotifications()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();

                        stopProgressBar();

                        showNotifications();

                        showToast(R.string.error_loading_notifications);
                    }

                    @Override
                    public void onNext(Integer totalNotifications) {
                        stopProgressBar();

                        showNotifications();
                    }
                });
    }
}
