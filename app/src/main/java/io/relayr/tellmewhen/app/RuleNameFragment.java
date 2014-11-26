package io.relayr.tellmewhen.app;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.FragmentName;

public class RuleNameFragment extends WhatFragment {

    @InjectView(R.id.nf_rule_name_et) EditText mRuleName;

    public static RuleNameFragment newInstance() {
        return new RuleNameFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        onCreateView(inflater, container, savedInstanceState, R.string.title_rule_name);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        View view = inflater.inflate(R.layout.rule_name_fragment, container, false);

        ButterKnife.inject(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRuleName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onDoneClicked();
                    return true;
                } else {
                    return false;
                }
            }
        });

        toggleKeyboard(true);

        mRuleName.requestFocus();
        if (Storage.isRuleEditing()) mRuleName.setText(Storage.getRule().getName());
    }

    @Override
    public void onPause() {
        super.onPause();

        toggleKeyboard(false);
    }

    @OnClick(R.id.button_done)
    public void onDoneClicked() {
        if (isNameOk()) {
            Storage.getRule().setName(mRuleName.getText().toString());

            mRuleService.saveRule();

            switchToEdit(FragmentName.MAIN);
        }
    }

    @Override
    void onBackPressed() {
        switchToEdit(FragmentName.RULE_VALUE_CREATE);
    }

    private void toggleKeyboard(boolean show) {
        InputMethodManager imm = (InputMethodManager) mRuleName
                .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        if (show) imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        else imm.hideSoftInputFromWindow(mRuleName.getWindowToken(), 0);
    }

    private boolean isNameOk() {
        if (mRuleName.getText().toString() == null || mRuleName.getText().toString().isEmpty()) {
            mRuleName.setError(getActivity().getString(R.string.nf_rule_name_empty));
            return false;
        }

        toggleKeyboard(false);

        return true;
    }
}
