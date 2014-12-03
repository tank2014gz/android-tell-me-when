package io.relayr.tellmewhen.app.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.relayr.model.Transmitter;
import io.relayr.tellmewhen.R;

public class TransmitterAdapter extends ArrayAdapter<Transmitter> {

    public TransmitterAdapter(Context context) {
        super(context, R.layout.transmitter_object);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.transmitter_object, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        holder.name.setText("Relayr WunderBar");
        holder.info.setText(getItem(position).getName());

        return view;
    }

    static class ViewHolder {
        @InjectView(R.id.object_name) TextView name;
        @InjectView(R.id.object_info) TextView info;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
