package io.relayr.tellmewhen.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.FragmentName;
import io.relayr.tellmewhen.util.WhenEvents;

public abstract class WhatFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState, int titleId) {
        getActivity().setTitle(getString(titleId));

        EventBus.getDefault().register(this);

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

    public void onEvent(WhenEvents.BackPressed back) {
        onBackPressed();
    }

    abstract void onBackPressed();
}
