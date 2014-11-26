package io.relayr.tellmewhen.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.model.Rule;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.FragmentName;
import io.relayr.tellmewhen.util.OperatorType;

public class RuleValueCreateFragment extends WhatFragment {

    public static RuleValueCreateFragment newInstance() {
        return new RuleValueCreateFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        onCreateView(inflater, container, savedInstanceState, R.string.title_rule_value);

        Rule rule = Storage.getRule();

        RuleValueView view;
        if (rule.getOperatorType() != null && rule.getValue() != null)
            view = new RuleValueView(getActivity(), rule.getSensorType(),
                    rule.getOperatorType(), rule.getValue());
        else
            view = new RuleValueView(getActivity(), rule.getSensorType(),
                    OperatorType.LESS, null);

        view.setOnDoneClickListener(new RuleValueView.OnDoneClickListener() {
            @Override
            public void onDoneClicked(int value, OperatorType mCurrentOperator) {
                Storage.getRule().setValue(value);
                Storage.getRule().setOperatorType(mCurrentOperator);

                switchTo(FragmentName.RULE_NAME);
            }
        });

        view.setButtonText(R.string.button_next);

        return view;
    }

    @Override
    void onBackPressed() {
        Storage.getRule().setValue(null);
        Storage.getRule().setOperatorType(null);

        switchTo(FragmentName.SENSOR);
    }
}
