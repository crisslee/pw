package com.coomix.app.all.ui.carlist;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.coomix.app.all.manager.DeviceManager;
import com.coomix.app.all.R;
import com.coomix.app.all.ui.detail.DeviceDetailInfoActivity;
import com.coomix.app.all.model.bean.DeviceInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by think on 2018/8/14.
 */
public class DeviceListAdapter extends BaseExpandableListAdapter {
    // 分组
    private ArrayList<ArrayList<DeviceInfo>> listGroups;
    private SparseArray<ArrayList<DeviceInfo>> mMaps;
    private LayoutInflater mLayoutInflater;
    private Resources mRes;
    private Context mContext;
    private ArrayList<DeviceInfo> listDevices;

    public DeviceListAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
        listGroups = new ArrayList<ArrayList<DeviceInfo>>();
        listDevices = new ArrayList<DeviceInfo>();
        mMaps = new SparseArray<ArrayList<DeviceInfo>>();
        mRes = context.getResources();
        mContext = context;
    }

    public void setData(List<DeviceInfo> data) {
        if (data == null) {
            return;
        }
        synchronized (listDevices) {
            listDevices.clear();
            listGroups.clear();
            mMaps.clear();
            listDevices.addAll(data);

            // 循环的按照组 将每组的数据添加
            for (DeviceInfo device : listDevices) {
                ArrayList<DeviceInfo> listTemps = mMaps.get(device.getGroup_id());

                if (listTemps == null) {
                    listTemps = new ArrayList<DeviceInfo>();
                    listGroups.add(listTemps);
                    mMaps.put(device.getGroup_id(), listTemps);
                }
                listTemps.add(device);
            }
        }
        notifyDataSetChanged();
    }

    public List<DeviceInfo> getData() {
        return listDevices;
    }

    private class ChildViewHolder {
        public TextView textName, textStatus, textDetail, textAddress;
    }

    private class GroupViewHolder {
        public ImageView imgArrow;
        public TextView tvName;
    }

    @Override
    public int getGroupCount() {
        return listGroups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (listGroups == null || groupPosition < 0 || groupPosition > listGroups.size() - 1) {
            return 0;
        }
        return listGroups.get(groupPosition).size();
    }

    @Override
    public ArrayList<DeviceInfo> getGroup(int groupPosition) {
        return listGroups.get(groupPosition);
    }

    @Override
    public DeviceInfo getChild(int groupPosition, int childPosition) {
        if(groupPosition < 0 || groupPosition > listGroups.size() - 1 || childPosition < 0){
            return null;
        }
        return listGroups.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    // 表示孩子是否和组ID是跨基础数据的更改稳定
    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        DeviceListAdapter.GroupViewHolder viewHolder;
        if (convertView == null || convertView.getTag() == null) {
            viewHolder = new DeviceListAdapter.GroupViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.devicelist_group_item, null, false);
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.textViewGroupName);
            viewHolder.imgArrow = (ImageView) convertView.findViewById(R.id.imageViewArrow);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (DeviceListAdapter.GroupViewHolder) convertView.getTag();
        }

        if (isExpanded) {
            // 打开分组
            viewHolder.imgArrow.setImageResource(R.drawable.open_arraw);
        } else {
            // 关闭分组
            viewHolder.imgArrow.setImageResource(R.drawable.close_arrow);
        }

        // 选取其中的一个
        String groupName = "";
        if(listGroups.get(groupPosition).get(0) != null){
            groupName = DeviceManager.getInstance().getGroupNameById(listGroups.get(groupPosition).get(0).getGroup_id());
        }

        int childrenNumber = getChildrenCount(groupPosition);
        if (TextUtils.isEmpty(groupName)) {
            viewHolder.tvName.setText(mRes.getString(R.string.default_group) + "(" + childrenNumber + ")");
        } else {
            viewHolder.tvName.setText(groupName + "(" + childrenNumber + ")");
        }

        if (getGroupCount() == 1) {
            //只有一个分组直接展开，不显示分组名称。直接返回一个不设置内容的view
            TextView textView = new TextView(mContext);
            textView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 3));
            return textView;
        }

        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        DeviceListAdapter.ChildViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new DeviceListAdapter.ChildViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.devicelist_item, null, false);
            viewHolder.textName = (TextView) convertView.findViewById(R.id.textViewDevName);
            viewHolder.textStatus = (TextView) convertView.findViewById(R.id.textViewStatus);
            viewHolder.textDetail = (TextView) convertView.findViewById(R.id.textViewDetail);
            viewHolder.textAddress = (TextView) convertView.findViewById(R.id.textViewAddress);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (DeviceListAdapter.ChildViewHolder) convertView.getTag();
        }

        final DeviceInfo device = listGroups.get(groupPosition).get(childPosition);
        if (TextUtils.isEmpty(device.getName())) {
            viewHolder.textName.setText(mContext.getString(R.string.car_name_empty));
        } else {
            viewHolder.textName.setText(device.getName());
        }
        MainActivityParent.setDeviceStatusText(device, viewHolder.textStatus);

        if(device.getState() == DeviceInfo.STATE_DISABLE || device.getState() == DeviceInfo.STATE_EXPIRE){
            //未启用或已经过期
            viewHolder.textAddress.setText("");
        }else{
            String cacheAddress = DeviceManager.getInstance().getCachedAddress(device.getLat(), device.getLng());
            if (!TextUtils.isEmpty(cacheAddress)) {
                viewHolder.textAddress.setText(cacheAddress);
                device.setAddress(cacheAddress);
            } else {
                viewHolder.textAddress.setText(R.string.reverse);
            }
        }

        viewHolder.textDetail.setOnClickListener(view -> {
            Intent intentDetail = new Intent(mContext, DeviceDetailInfoActivity.class);
            intentDetail.putExtra(DeviceDetailInfoActivity.DEV_IMEI, device.getImei());
            intentDetail.putExtra(DeviceDetailInfoActivity.DEV_ANGLE, device.getInstallAngle());
            mContext.startActivity(intentDetail);
        });
        return convertView;
    }

    // 孩子在指定的位置是可选的
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}