package com.coomix.app.all.ui.detail;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.StatisticsKey;
import java.util.ArrayList;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2018/11/6.
 */
public class StatisticsAdapter extends RecyclerView.Adapter<StatisticsAdapter.StatisticsHolder> {

    private ArrayList<ArrayList<String>> items;
    private StatisticsKey key;

    public void setData(ArrayList<ArrayList<String>> lists, StatisticsKey key) {
        items = lists;
        this.key = key;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(StatisticsHolder holder, int position) {
        if (items == null || key == null) {
            return;
        }
        ArrayList<String> item = items.get(position);
        holder.date.setText(item.get(key.datetime));
        holder.distance.setText(item.get(key.milestat));
        holder.overspeed.setText(item.get(key.outspeed));
        holder.stay.setText(item.get(key.stop));
    }

    @Override
    public StatisticsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_statistics, parent, false);
        return new StatisticsHolder(v);
    }

    @Override
    public int getItemCount() {
        if (items == null) return 0;
        return items.size();
    }

    class StatisticsHolder extends RecyclerView.ViewHolder {
        TextView date, distance, overspeed, stay;

        public StatisticsHolder(View itemView) {
            super(itemView);
            date = (TextView) itemView.findViewById(R.id.date);
            distance = (TextView) itemView.findViewById(R.id.distance);
            overspeed = (TextView) itemView.findViewById(R.id.overspeed);
            stay = (TextView) itemView.findViewById(R.id.stay);
        }
    }
}
