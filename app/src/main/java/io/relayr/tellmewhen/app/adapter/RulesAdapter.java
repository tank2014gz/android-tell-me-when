package io.relayr.tellmewhen.app.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Comparator;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.relayr.tellmewhen.R;
import io.relayr.tellmewhen.consts.FragmentName;
import io.relayr.tellmewhen.model.TMWRule;
import io.relayr.tellmewhen.service.RuleService;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.SensorUtil;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RulesAdapter extends ArrayAdapter<TMWRule> {

    public RulesAdapter(Context context) {
        super(context, R.layout.main_rule_object);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        final TMWRule rule = getItem(position);

        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.main_rule_object, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        holder.status.setText(getContext().getString(rule.isNotifying ? R.string.on : R.string.off));
        holder.image.setImageResource(SensorUtil.getIcon(getContext(), rule.getSensorType()));
        holder.name.setText(rule.name);
        holder.value.setText(SensorUtil.buildRuleValue(rule));

        return view;
    }

    public void sortRules() {
        sort(new Comparator<TMWRule>() {
            @Override
            public int compare(TMWRule lhs, TMWRule rhs) {
                return Boolean.valueOf(rhs.isNotifying).compareTo(lhs.isNotifying);
            }
        });
    }

    static class ViewHolder {
        @InjectView(R.id.object_image) ImageView image;
        @InjectView(R.id.object_name) TextView name;
        @InjectView(R.id.object_value) TextView value;
        @InjectView(R.id.object_status) TextView status;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
