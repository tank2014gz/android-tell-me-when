package io.relayr.tellmewhen.app.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.activeandroid.query.Select;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.model.TMWRule;
import io.relayr.tellmewhen.model.TMWNotification;
import io.relayr.tellmewhen.util.NotificationUtil;
import io.relayr.tellmewhen.util.SensorUtil;

public class NotificationsAdapter extends ArrayAdapter<TMWNotification> {

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

        TMWNotification item = getItem(position);
        TMWRule rule = new Select().from(TMWRule.class).where("dbId = ?", item.getRuleId()).executeSingle();

        if (rule != null) {
            holder.name.setText(rule.name);
            holder.info.setText(SensorUtil.buildRuleValue(rule));
        }

        holder.date.setText(NotificationUtil.getDate(getContext(), item));
        holder.time.setText(NotificationUtil.getTime(item));

        return view;
    }

    static class ViewHolder {
        @InjectView(R.id.notification_object_name) TextView name;
        @InjectView(R.id.notification_object_value) TextView info;
        @InjectView(R.id.notification_object_date) TextView date;
        @InjectView(R.id.notification_object_time) TextView time;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
