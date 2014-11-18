package io.relayr.tellmewhen.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.model.Transmitter;

public class TransmitterAdapter extends ArrayAdapter<Transmitter> {

    private final Context context;

    public TransmitterAdapter(Context context, List<Transmitter> objects) {
        super(context, R.layout.name_info_object, objects);

        this.context = context;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.name_info_object, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        holder.name.setText("Wunderbar");
        holder.info.setText("info");

        return view;
    }

    static class ViewHolder {
        @InjectView(R.id.object_name)
        TextView name;
        @InjectView(R.id.object_info)
        TextView info;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
