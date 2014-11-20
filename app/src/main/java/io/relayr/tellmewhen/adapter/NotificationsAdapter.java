package io.relayr.tellmewhen.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.model.Notification;

public class NotificationsAdapter extends ArrayAdapter<Notification> {

    public NotificationsAdapter(Context context) {
        super(context, R.layout.main_notification_object);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
             LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.main_notification_object, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        holder.name.setText("temp watch");
        holder.info.setText("something");

        return view;
    }

    static class ViewHolder {
        @InjectView(R.id.notification_object_name) TextView name;
        @InjectView(R.id.notification_object_info) TextView info;
        @InjectView(R.id.notification_object_date) TextView date;
        @InjectView(R.id.notification_object_time) TextView time;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
