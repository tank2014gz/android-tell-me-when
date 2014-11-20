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
import io.relayr.tellmewhen.model.Rule;

public class RulesAdapter extends ArrayAdapter<Rule> {

    public RulesAdapter(Context context) {
        super(context, R.layout.main_rule_object);
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

        holder.name.setText(getItem(position).getName());
        holder.value.setText(buildRuleValue(getItem(position)));

        return view;
    }

    private String buildRuleValue(Rule rule){
       return rule.getSensorType().getName() + " " +
                rule.getOperatorType().getName() + " " + rule.getValue();
    }

    static class ViewHolder {
        @InjectView(R.id.object_name) TextView name;
        @InjectView(R.id.object_value) TextView value;
        @InjectView(R.id.object_info) TextView info;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
