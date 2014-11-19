package io.relayr.tellmewhen.app;

import android.app.Fragment;
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
import de.greenrobot.event.EventBus;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.model.WhenEvents;
import io.relayr.tellmewhen.storage.Storage;

public class RuleNameFragment extends Fragment {

    @InjectView(R.id.nf_rule_name_et)
    EditText mRuleName;

    public static RuleNameFragment newInstance() {
        return new RuleNameFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.rule_name_fragment, container, false);

        ButterKnife.inject(this, view);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        ((TextView) view.findViewById(R.id.navigation_title)).setText(getString(R.string.title_rule_name));
        ((TextView) view.findViewById(R.id.button_done)).setText(getString(R.string.button_done));

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
    }

    @Override
    public void onPause() {
        super.onPause();

        toggleKeyboard(false);
    }

    @OnClick(R.id.button_done)
    public void onDoneClicked() {
        if (isNameOk()) {
            Storage.saveRuleName(mRuleName.getText().toString());

            EventBus.getDefault().post(new WhenEvents.NameFragDone());
        }
    }

    @OnClick(R.id.navigation_back)
    public void onBackClicked() {
        EventBus.getDefault().post(new WhenEvents.BackClicked());
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
