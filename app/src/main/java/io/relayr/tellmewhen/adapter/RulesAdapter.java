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
import io.relayr.tellmewhen.model.Rule;

public class RulesAdapter extends ArrayAdapter<Rule> {

    public RulesAdapter(Context context, List<Rule> objects) {
        super(context, R.layout.main_rule_object, objects);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.main_rule_object, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        Rule rule = getItem(position);
        holder.name.setText(rule.getName());
        holder.value.setText(rule.getSensorType().getName() + " " +
                rule.getOperatorType().getName() + " " + rule.getValue());

        return view;
    }

    static class ViewHolder {
        @InjectView(R.id.object_name)
        TextView name;
        @InjectView(R.id.object_value)
        TextView value;
        @InjectView(R.id.object_info)
        TextView info;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
