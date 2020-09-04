package com.coomix.app.all.ui.alarm;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.coomix.app.all.R;
import com.coomix.app.all.widget.dragable.OnDragVHListener;
import com.coomix.app.all.widget.dragable.OnItemMoveListener;
import com.coomix.app.framework.util.ExtraConstants;
import com.coomix.app.framework.util.PreferenceUtil;
import com.coomix.app.framework.util.TimeUtils;
import com.coomix.app.all.model.response.AlarmCategoryItem;
import com.coomix.app.all.util.AlarmCategoryUtils;
import java.util.List;

/**
 * Created by herry on 2017/1/11.
 */
public class AlarmCategoryListAdapter extends RecyclerView.Adapter<AlarmCategoryListAdapter.DragListViewHolder>
        implements OnItemMoveListener {

    private Activity mActivity;
    private List<AlarmCategoryItem> mDataList;
    private String mImei;
    private int mItemBgSelector;
//    private SharedPreferences sharedPrefs;
//    private String alarmPrefix;
    private int allAlarmLocalCount = 0;

    public AlarmCategoryListAdapter(Activity activity, String imei, List<AlarmCategoryItem> dataList) {
        mActivity = activity;
        mImei = imei;
        mDataList = dataList;
        mItemBgSelector = R.drawable.bottom_action_bar_item_bg_selector;

//        sharedPrefs = activity.getSharedPreferences(AlarmCategoryUtils.ALARM_TYPE_LIST, Context.MODE_PRIVATE);
//        alarmPrefix = getsharedPrefix();
        allAlarmLocalCount = AlarmCategoryUtils.getAllAlarmLocalCount(activity);
    }

    @Override
    public AlarmCategoryListAdapter.DragListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DragListViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_drag, parent, false));
    }

    @Override
    public void onBindViewHolder(AlarmCategoryListAdapter.DragListViewHolder holder, int position) {
        holder.bindView(position);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        AlarmCategoryItem item = mDataList.get(fromPosition);
        mDataList.remove(fromPosition);
        mDataList.add(toPosition, item);
        notifyItemMoved(fromPosition, toPosition);
    }

    public class DragListViewHolder extends RecyclerView.ViewHolder implements OnDragVHListener {
        private ImageView iconView;
        private TextView alarmTypeNameView;
        private TextView timestampView;
        private TextView lastDeviceName;
        private TextView newCountView;

        public DragListViewHolder(final View itemView) {
            super(itemView);
            iconView = (ImageView) itemView.findViewById(R.id.item_icon);
            alarmTypeNameView = (TextView) itemView.findViewById(R.id.item_alarm_type_name);
            timestampView = (TextView) itemView.findViewById(R.id.item_timestamp);
            lastDeviceName = (TextView) itemView.findViewById(R.id.item_last_device_name);
            newCountView = (TextView) itemView.findViewById(R.id.item_new_count);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    AlarmCategoryItem item = mDataList.get(position);
                    saveAlarmCategorySendType(item);
                    saveReadAlarmCount(item);
                    notifyItemChanged(position);

                    mActivity.startActivityForResult(new Intent(mActivity, AlarmDetailListActivity.class)
                                    .putExtra(ExtraConstants.EXTRA_ALARM_CATEGORY_POSITION, getAdapterPosition())
                                    .putExtra(ExtraConstants.EXTRA_IMEI, mImei)
                                    .putExtra(ExtraConstants.EXTRA_ALARM_CATEGORY_ITEM, item),
                            ExtraConstants.REQ_CODE_ALARM_ITEM_LIST);
                }
            });
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemFinish() {
            itemView.setBackgroundResource(mItemBgSelector);
        }

        protected void bindView(int position) {
            AlarmCategoryItem item = mDataList.get(position);
            int alarmTypeId = getAlarmTypeId(item);
            int localAlarmCount =  AlarmCategoryUtils.getAlarmLocalCountByType(mActivity, alarmTypeId);
            int count = 0;
            if (item.isAlias())
            {
                alarmTypeNameView.setText(item.getAlarm_type_alias());
                count = item.getAlarm_num_alias() - localAlarmCount;
            }
            else
            {
                alarmTypeNameView.setText(item.getAlarm_type());
                count = item.getAlarm_num() - localAlarmCount;
            }

            if(count > 99){
                newCountView.setVisibility(View.VISIBLE);
                newCountView.setText("99+");
            }
            else if(count > 0){
                newCountView.setVisibility(View.VISIBLE);
                newCountView.setText(String.valueOf(count));
            }
            else{
                newCountView.setVisibility(View.GONE);
            }

            if(TextUtils.isEmpty(item.getUser_name()))
            {
                lastDeviceName.setText(itemView.getResources().getString(R.string.car_name_empty));
            }
            else
            {
                lastDeviceName.setText(item.getUser_name());
            }

            iconView.setImageResource(AlarmCategoryUtils.getAlarmDrawable(item.getAlarm_type_id()));
            timestampView.setText(TimeUtils.formatMyTime(item.getSend_time() * 1000, itemView.getContext()));
        }
    }

    private void saveAlarmCategorySendType(AlarmCategoryItem item) {
        String prefKey = AlarmCategoryUtils.getAlarmPrefKey(item.getAlarm_type_id());
        long savedSendTime = PreferenceUtil.getLong(prefKey, -1L);
        if (savedSendTime >= item.getSend_time()) {
            return;
        }
        PreferenceUtil.commitLong(prefKey, item.getSend_time());
    }

    private boolean doesItemReceiveNewAlarm(AlarmCategoryItem item) {
        String prefKey = AlarmCategoryUtils.getAlarmPrefKey(item.getAlarm_type_id());
        long savedSendTime = PreferenceUtil.getLong(prefKey, -1L);
        if (savedSendTime >= item.getSend_time()) {
            return false;
        }
        return true;
    }

    private int getAlarmTypeId(AlarmCategoryItem item) {
        if (item.isAlias()) {
            //其他报警本地存储用10000代替-1
            return 10000;
        } else {
            return item.getAlarm_type_id();
        }
    }

    public void saveReadAlarmCount(AlarmCategoryItem item) {
        int alarmTypeId = getAlarmTypeId(item);
        int localCount = AlarmCategoryUtils.getAlarmLocalCountByType(mActivity, alarmTypeId);
        int count = 0;
        if (item.isAlias()) {
            count = item.getAlarm_num_alias() - localCount;
            allAlarmLocalCount += count;
            AlarmCategoryUtils.setAlarmLocalCountByType(mActivity, alarmTypeId, item.getAlarm_num_alias());
        } else {
            count = item.getAlarm_num() - localCount;
            allAlarmLocalCount += count;
            AlarmCategoryUtils.setAlarmLocalCountByType(mActivity, alarmTypeId, item.getAlarm_num());
        }

        AlarmCategoryUtils.setAllAlarmLocalCount(mActivity, allAlarmLocalCount);
    }
}
