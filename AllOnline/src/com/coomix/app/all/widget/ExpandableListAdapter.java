package com.coomix.app.all.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.OfflineCityItem;
import com.coomix.app.framework.util.CommonUtil;

import java.util.ArrayList;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    ArrayList<OfflineCityItem> arrayListOfflineItem;
    Handler handler;
    private DownloadClickListener downloadClickListener;

    public ExpandableListAdapter(Context context, ArrayList<OfflineCityItem> arrayListOfflineItem) {
        this._context = context;
        this.arrayListOfflineItem = arrayListOfflineItem;
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                notifyDataSetChanged();
                super.handleMessage(msg);
            }
        };
    }

    public void setDownloadClickListener(DownloadClickListener downloadClickListener) {
        this.downloadClickListener = downloadClickListener;
    }

    public void setData(ArrayList<OfflineCityItem> arrayListOfflineItem) {
        this.arrayListOfflineItem = arrayListOfflineItem;
        notifyDataSetChanged();
    }

    public void refresh() {
        handler.sendMessage(new Message());
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        OfflineCityItem groupItem = arrayListOfflineItem.get(groupPosition);
        return groupItem.childCitites.get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        OfflineCityItem groupItem = (OfflineCityItem) getGroup(groupPosition);
        if (groupItem.cityType == OfflineCityItem.PROVINCE) {
            return groupItem.childCitites.get(childPosition).cityId;
        }
        return -1;
        // return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) {

        final OfflineCityItem childItem = (OfflineCityItem) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.xlist_item, null);
        }

        TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);
        TextView textViewStatus = (TextView) convertView.findViewById(R.id.tv_status);
        TextView textViewSize = (TextView) convertView.findViewById(R.id.tv_size);
        ImageButton imageButtonDownload = (ImageButton) convertView
                .findViewById(R.id.ibtn_indicator);
        
        
        if (childItem.cityType == OfflineCityItem.CITY) {
            textViewStatus.setVisibility(View.VISIBLE);
            textViewStatus.setText(CommonUtil.getStatus(_context,childItem.status));
            textViewSize.setText(formatDataSize(childItem.size));
        } else {
            textViewStatus.setVisibility(View.GONE);
        }
        // txtListChild.setText(childItem.cityName + "(" + childItem.cityId +
        // ")");
        txtListChild.setText(childItem.cityName);
        imageButtonDownload.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                downloadClickListener.download(childItem.cityId);
            }
        });
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        OfflineCityItem groupItem = arrayListOfflineItem.get(groupPosition);
        return (groupItem.childCitites != null && groupItem.childCitites.size() > 0) ? groupItem.childCitites
                .size() : 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return arrayListOfflineItem.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        if (arrayListOfflineItem == null) {
            return 0;
        }
        return this.arrayListOfflineItem.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        OfflineCityItem groupItem = (OfflineCityItem) getGroup(groupPosition);
        if (groupItem.cityType != OfflineCityItem.HEADER) {
            return groupItem.cityId;
        }
        return -1;
    }

    private class GroupViewHolder {
        public ImageButton indicator;
        public TextView text1;
        public TextView textStatus;
        public TextView textSize;
    }

    private class HeaderViewHolder {
        public TextView text1;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
            ViewGroup parent) {

        final OfflineCityItem groupItem = (OfflineCityItem) getGroup(groupPosition);
        if (OfflineCityItem.HEADER == groupItem.cityType) {
            HeaderViewHolder headerViewHolder = null;
            if (convertView == null || !(convertView.getTag() instanceof HeaderViewHolder)) {
                headerViewHolder = new HeaderViewHolder();
                convertView = LayoutInflater.from(_context).inflate(R.layout.xlist_header, null);
                headerViewHolder.text1 = (TextView) convertView.findViewById(R.id.tv_header);
                convertView.setTag(headerViewHolder);
            } else {
                headerViewHolder = (HeaderViewHolder) convertView.getTag();
            }
            headerViewHolder.text1.setText(groupItem.label);
             
            return convertView;
        }

        GroupViewHolder viewHolder = null;
        if (convertView == null || !(convertView.getTag() instanceof GroupViewHolder)) {
            viewHolder = new GroupViewHolder();
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.xlist_group, null);
            viewHolder.text1 = (TextView) convertView.findViewById(R.id.tv_city);
            // viewHolder.text1.setTypeface(null, Typeface.BOLD);
            viewHolder.textStatus = (TextView) convertView.findViewById(R.id.tv_status);
            viewHolder.textSize = (TextView) convertView.findViewById(R.id.tv_size);
            viewHolder.indicator = (ImageButton) convertView.findViewById(R.id.ibtn_indicator);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (GroupViewHolder) convertView.getTag();
        }
        // viewHolder.text1.setText(groupItem.cityName + "(" + groupItem.cityId
        // + ")");
        viewHolder.text1.setText(groupItem.cityName);
        viewHolder.textSize.setText("");
        if (groupItem.cityType == OfflineCityItem.CITY
                || groupItem.cityType == OfflineCityItem.COUNTRY) {
            // viewHolder.textStatus.setVisibility(View.VISIBLE);
            // viewHolder.textSize.setVisibility(View.VISIBLE);
            viewHolder.textStatus.setText(CommonUtil.getStatus(_context,groupItem.status));
            viewHolder.textSize.setText(formatDataSize(groupItem.size));
        } else {
            viewHolder.textStatus.setText("");
            viewHolder.textSize.setText("");
            // viewHolder.textSize.setVisibility(View.GONE);
        }
        if (getChildrenCount(groupPosition) > 0) {
            viewHolder.indicator.setFocusable(false);
            if (isExpanded) {
                viewHolder.indicator.setImageResource(R.drawable.group_indicator_collapse);
            } else {
                viewHolder.indicator.setImageResource(R.drawable.group_indicator_expand);
            }
            viewHolder.indicator.setOnClickListener(null);
        } else {
            viewHolder.indicator.setFocusable(true);
            viewHolder.indicator.setImageResource(R.drawable.btn_icon_offman_download);
            viewHolder.indicator.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    downloadClickListener.download(groupItem.cityId);

                }
            });
        }

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


    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public interface DownloadClickListener {
        void download(int cityId);
    }
}
