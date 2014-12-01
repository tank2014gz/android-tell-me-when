package io.relayr.tellmewhen.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import io.relayr.tellmewhen.TellMeWhenApplication;
import io.relayr.tellmewhen.service.NotificationService;
import io.relayr.tellmewhen.service.RuleService;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.FragmentName;
import io.relayr.tellmewhen.util.WhenEvents;

public abstract class WhatFragment extends Fragment {

    @Inject RuleService ruleService;
    @Inject NotificationService notificationService;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState, int titleId, boolean showBack) {
        getActivity().setTitle(getString(titleId));

        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(showBack);

        EventBus.getDefault().register(this);

        inject(this);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ButterKnife.reset(this);

        EventBus.getDefault().unregister(this);
    }

    protected void switchTo(FragmentName name) {
        EventBus.getDefault().post(new WhenEvents.ShowFragment(name));
    }

    protected void switchToEdit(FragmentName name) {
        if (Storage.isRuleEditing())
            switchTo(FragmentName.RULE_EDIT);
        else
            switchTo(name);
    }

    protected void showToast(int stringId) {
        Toast.makeText(getActivity(), getActivity().getString(stringId),
                Toast.LENGTH_SHORT).show();
    }

    protected void inject(Object o){
        TellMeWhenApplication.objectGraph.inject(o);
    }

    public void onEvent(WhenEvents.BackPressed back) {
        onBackPressed();
    }

    abstract void onBackPressed();
}
