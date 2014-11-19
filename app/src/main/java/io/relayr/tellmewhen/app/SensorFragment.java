package io.relayr.tellmewhen.app;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.adapter.SensorAdapter;
import io.relayr.tellmewhen.model.WhenEvents;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.SensorUtil;

public class SensorFragment extends Fragment {

    @InjectView(R.id.list_view)
    ListView mListView;

    public static SensorFragment newInstance() {
        return new SensorFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sensor_fragment, container, false);

        ButterKnife.inject(this, view);

        ((TextView)view.findViewById(R.id.navigation_title)).setText(getString(R.string.title_select_sensor));

        mListView.setAdapter(new SensorAdapter(this.getActivity()));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Storage.saveRuleSensor(SensorUtil.getSensors().get(position));
                EventBus.getDefault().post(new WhenEvents.MeasurementSelected());
            }
        });
        return view;
    }

    @OnClick(R.id.navigation_back)
    public void onBackClicked() {
        EventBus.getDefault().post(new WhenEvents.BackClicked());
    }
}
