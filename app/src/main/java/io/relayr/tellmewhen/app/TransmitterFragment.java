package io.relayr.tellmewhen.app;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import io.relayr.RelayrSdk;
import io.relayr.model.Transmitter;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.adapter.TransmitterAdapter;
import io.relayr.tellmewhen.model.WhenEvents;
import io.relayr.tellmewhen.storage.Storage;
import rx.Subscriber;

public class TransmitterFragment extends Fragment {

    @InjectView(R.id.list_view)
    ListView mListView;

    private List<Transmitter> mTransmitters = new ArrayList<Transmitter>();

    public static TransmitterFragment newInstance() {
        return new TransmitterFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.transmitter_fragment, container, false);

        ButterKnife.inject(this, view);

        ((TextView)view.findViewById(R.id.navigation_title)).setText(getString(R.string.title_select_transmitter));

        refreshTransmitters();

        mListView.setAdapter(new TransmitterAdapter(this.getActivity(), mTransmitters));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Storage.createRule(mTransmitters.get(position));
                EventBus.getDefault().post(new WhenEvents.WunderBarSelected());
            }
        });

        return view;
    }

    @OnClick(R.id.navigation_back)
    public void onBackClicked() {
        EventBus.getDefault().post(new WhenEvents.BackClicked());
    }

    private void refreshTransmitters() {
        RelayrSdk.getRelayrApi().getTransmitters(Storage.loadUserId()).subscribe(new Subscriber<List<io.relayr.model.Transmitter>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(List<Transmitter> transmitters) {
                mTransmitters.clear();
                mTransmitters.addAll(transmitters);
            }
        });
    }
}
