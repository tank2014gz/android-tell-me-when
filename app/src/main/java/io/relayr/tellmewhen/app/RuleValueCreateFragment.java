package io.relayr.tellmewhen.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.app.views.RuleValueView;
import io.relayr.tellmewhen.model.TMWRule;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.FragmentName;
import io.relayr.tellmewhen.util.OperatorType;

public class RuleValueCreateFragment extends WhatFragment {

    public static RuleValueCreateFragment newInstance() {
        return new RuleValueCreateFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        onCreateView(inflater, container, savedInstanceState, R.string.title_rule_value, true);

        TMWRule rule = Storage.getRule();

        RuleValueView view;
        if (rule.operatorType != -1 && rule.value != null)
            view = new RuleValueView(getActivity(), rule.getSensorType(),
                    rule.getOperatorType(), rule.value);
        else
            view = new RuleValueView(getActivity(), rule.getSensorType(),
                    OperatorType.LESS, null);

        view.setOnDoneClickListener(new RuleValueView.OnDoneClickListener() {
            @Override
            public void onDoneClicked(int value, OperatorType mCurrentOperator) {
                Storage.getRule().value = value;
                Storage.getRule().operatorType = mCurrentOperator.ordinal();

                switchTo(FragmentName.RULE_NAME);
            }
        });

        view.setButtonText(R.string.button_next);

        return view;
    }

    @Override
    void onBackPressed() {
        Storage.getRule().value = null;
        Storage.getRule().operatorType = -1;

        switchTo(FragmentName.SENSOR);
    }
}
