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
import io.relayr.tellmewhen.util.MeasurementUtil;

public class MeasurementAdapter extends ArrayAdapter<String> {

    public MeasurementAdapter(Context context) {
        super(context, R.layout.measurement_object, MeasurementUtil.getMeasurementList());
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

        holder.icon.setImageResource(MeasurementUtil.getIcon(getContext(), getItem(position)));
        holder.info.setText(MeasurementUtil.getTitle(getItem(position)));

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
