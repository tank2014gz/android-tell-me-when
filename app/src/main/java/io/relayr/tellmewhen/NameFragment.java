package io.relayr.tellmewhen;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import io.relayr.tellmewhen.model.WhenEvents;

public class NameFragment extends Fragment {

    @InjectView(R.id.nf_rule_name_et)
    EditText mRuleName;

    public static NameFragment newInstance() {
        return new NameFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.name_fragment, container, false);

        ButterKnife.inject(this, view);

        return view;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRuleName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                return (actionId == EditorInfo.IME_ACTION_DONE && isNameOk());
            }
        });
        mRuleName.requestFocus();
    }

    @OnClick(R.id.nf_button_done)
    public void onDoneClicked() {
        if (isNameOk()) EventBus.getDefault().post(new WhenEvents.NameFragDone());
    }

    @OnClick(R.id.nf_back_button)
    public void onBackClicked() {
        EventBus.getDefault().post(new WhenEvents.BackClicked());
    }

    private boolean isNameOk() {
        if (mRuleName.getText().toString() == null || mRuleName.getText().toString().isEmpty()) {
            mRuleName.setError(getActivity().getString(R.string.nf_rule_name_empty));
            return false;
        }

        InputMethodManager imm = (InputMethodManager) mRuleName
                .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mRuleName.getWindowToken(), 0);

        return true;
    }
}
