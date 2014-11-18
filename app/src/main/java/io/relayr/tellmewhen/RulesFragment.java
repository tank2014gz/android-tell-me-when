package io.relayr.tellmewhen;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RulesFragment extends Fragment {

    public static RulesFragment newInstance() {
        return new RulesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.rules_fragment, container, false);


        return view;
    }
}
