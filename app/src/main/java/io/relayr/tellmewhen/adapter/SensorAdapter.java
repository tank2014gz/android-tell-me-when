package io.relayr.tellmewhen.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.util.SensorType;
import io.relayr.tellmewhen.util.SensorUtil;

public class SensorAdapter extends ArrayAdapter<SensorType> {

    public SensorAdapter(Context context) {
        super(context, R.layout.sensor_object, SensorUtil.getSensors());
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = inflater.inflate(R.layout.sensor_object, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        holder.icon.setImageResource(SensorUtil.getIcon(getContext(), getItem(position)));
        holder.info.setText(SensorUtil.getTitle(getItem(position)));

        return view;
    }

    static class ViewHolder {
        @InjectView(R.id.sensor_object_icon) ImageView icon;
        @InjectView(R.id.sensor_object_name) TextView info;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
