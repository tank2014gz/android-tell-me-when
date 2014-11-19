package io.relayr.tellmewhen.app;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.adapter.MeasurementAdapter;
import io.relayr.tellmewhen.model.WhenEvents;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.MeasurementUtil;

public class MeasurementFragment extends Fragment {

    @InjectView(R.id.mf_list_view)
    ListView mListView;

    public static MeasurementFragment newInstance() {
        return new MeasurementFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.measurement_fragment, container, false);

        ButterKnife.inject(this, view);

        mListView.setAdapter(new MeasurementAdapter(this.getActivity()));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Storage.saveMeasurement(MeasurementUtil.getMeasurementList().get(position));
                EventBus.getDefault().post(new WhenEvents.MeasurementSelected());
            }
        });
        return view;
    }

    @OnClick(R.id.mf_back_button)
    public void onBackClicked() {
        EventBus.getDefault().post(new WhenEvents.BackClicked());
    }
}
