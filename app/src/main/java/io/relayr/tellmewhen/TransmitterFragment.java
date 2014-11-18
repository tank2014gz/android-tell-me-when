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
import io.relayr.tellmewhen.adapter.TransmitterAdapter;
import io.relayr.tellmewhen.model.Transmitter;
import io.relayr.tellmewhen.model.WhenEvents;

public class TransmitterFragment extends Fragment {

    @InjectView(R.id.tf_list_view)
    ListView listView;

    private List<Transmitter> mTransmitters = new ArrayList<Transmitter>();

    public static TransmitterFragment newInstance() {
        return new TransmitterFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.transmitter_fragment, container, false);

        ButterKnife.inject(this, view);

        refreshTransmitters();

        listView.setAdapter(new TransmitterAdapter(this.getActivity(), mTransmitters));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EventBus.getDefault().post(new WhenEvents.WunderBarSelected());
            }
        });
        return view;
    }

    @OnClick(R.id.tf_back_button)
    public void onBackClicked() {
        EventBus.getDefault().post(new WhenEvents.BackClicked());
    }

    private void refreshTransmitters() {
        mTransmitters.add(new Transmitter());
    }


}
