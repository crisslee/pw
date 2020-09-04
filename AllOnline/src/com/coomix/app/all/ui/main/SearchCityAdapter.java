package com.coomix.app.all.ui.main;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.OfflineCityItem;
import com.coomix.app.framework.util.CommonUtil;

import java.util.ArrayList;
import java.util.List;

public class SearchCityAdapter extends BaseAdapter {
    private Context context;
    private List<OfflineCityItem> mListObjects;

    public SearchCityAdapter(Context context) {
        this.context = context;
        mListObjects = new ArrayList<OfflineCityItem>();
    }

    public void setData(ArrayList<OfflineCityItem> array) {
        if (array == null) {
            mListObjects = new ArrayList<OfflineCityItem>();
        } else {
            mListObjects = array;
        }
        notifyDataSetChanged();

    }

    public static List<OfflineCityItem> asList(SparseArray<OfflineCityItem> sparseArray) {
        if (sparseArray == null)
            return null;
        List<OfflineCityItem> arrayList = new ArrayList<OfflineCityItem>(sparseArray.size());
        for (int i = 0; i < sparseArray.size(); i++)
            arrayList.add(sparseArray.valueAt(i));
        return arrayList;
    }

    @Override
    public int getCount() {
        // return mObjects.size();
        return mListObjects.size();

    }

    @Override
    public OfflineCityItem getItem(int position) {
        // return mObjects.get(position);
        return mListObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mListObjects.get(position).cityId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (null == convertView) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.xlist_item, null);
            holder.txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);
            holder.textViewStatus = (TextView) convertView.findViewById(R.id.tv_status);
            holder.textViewSize = (TextView) convertView.findViewById(R.id.tv_size);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        OfflineCityItem childItem = mListObjects.get(position);
        if (childItem.cityType == OfflineCityItem.CITY) {
            holder.textViewStatus.setVisibility(View.VISIBLE);
            holder.textViewStatus.setText(CommonUtil.getStatus(context,childItem.status) + " " + childItem.ratio
                    + "% ");
            holder.textViewSize.setText(formatDataSize(childItem.size));
        } else {
            holder.textViewSize.setVisibility(View.GONE);
        }
        holder.txtListChild.setText(childItem.cityName + "(" + childItem.cityId + ")");

        return convertView;
    }


    public String formatDataSize(int size) {
        String ret = "";
        if (size < (1024 * 1024)) {
            ret = String.format("%dK", size / 1024);
        } else {
            ret = String.format("%.1fM", size / (1024 * 1024.0));
        }
        return ret;
    }

    static class ViewHolder {
        TextView txtListChild;
        TextView textViewStatus;
        TextView textViewSize;
    }
}
