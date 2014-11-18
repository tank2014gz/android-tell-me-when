package io.relayr.tellmewhen.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.model.Measurement;
import io.relayr.tellmewhen.model.Transmitter;

public class MeasurementAdapter extends ArrayAdapter<Measurement> {

    private final Context context;

    public MeasurementAdapter(Context context, List<Measurement> objects) {
        super(context, R.layout.measurement_object, objects);

        this.context = context;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.measurement_object, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        holder.icon.setImageResource(R.drawable.ic_launcher);
        holder.info.setText("name");

        return view;
    }

    static class ViewHolder {
        @InjectView(R.id.measurement_object_icon)
        ImageView icon;
        @InjectView(R.id.measurement_object_name)
        TextView info;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
