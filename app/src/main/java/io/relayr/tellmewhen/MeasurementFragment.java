package io.relayr.tellmewhen;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import io.relayr.tellmewhen.adapter.MeasurementAdapter;
import io.relayr.tellmewhen.adapter.TransmitterAdapter;
import io.relayr.tellmewhen.model.Measurement;
import io.relayr.tellmewhen.model.Transmitter;
import io.relayr.tellmewhen.model.WhenEvents;

public class MeasurementFragment extends Fragment {

    @InjectView(R.id.mf_list_view)
    ListView listView;

    private List<Measurement> mMeasurements = new ArrayList<Measurement>();

    public static MeasurementFragment newInstance() {
        return new MeasurementFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.measurement_fragment, container, false);

        ButterKnife.inject(this, view);

        refreshMeasurements();

        listView.setAdapter(new MeasurementAdapter(this.getActivity(), mMeasurements));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EventBus.getDefault().post(new WhenEvents.MeasurementSelected());
            }
        });
        return view;
    }

    @OnClick(R.id.mf_back_button)
    public void onBackClicked() {
        EventBus.getDefault().post(new WhenEvents.BackClicked());
    }

    private void refreshMeasurements() {
        mMeasurements.add(new Measurement());
    }
}
