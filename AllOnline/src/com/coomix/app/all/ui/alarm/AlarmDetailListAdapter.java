package com.coomix.app.all.ui.alarm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.Alarm;
import com.coomix.app.all.util.AlarmCategoryUtils;
import com.coomix.app.all.manager.SettingDataManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by herry on 2017/1/13.
 */
public class AlarmDetailListAdapter extends RecyclerView.Adapter<AlarmDetailListAdapter.AlarmCategoryItemViewHodler> {
    public interface OnItemSelectListener {
        public void onHasItemSelected(boolean selected);

        public void onEmpty();
    }

    private List<Alarm> mDataList;
    private boolean mInEditMode;
    private int mContentLeftMargin;
    private RelativeLayout.LayoutParams mLayoutParams;
    private RecyclerView mRecyclerView;

    private List<Alarm> mDelSelectionList;

    private OnItemSelectListener mListener;
    private int alarm_type_id;
    private Context mContext;

    public AlarmDetailListAdapter(Context context, OnItemSelectListener l, int alarm_type_id, List<Alarm> dataList) {
        mContext = context;
        mListener = l;
        this.alarm_type_id = alarm_type_id;
        mDataList = dataList;
        mInEditMode = false;
        mDelSelectionList = new ArrayList<Alarm>();
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public AlarmCategoryItemViewHodler onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AlarmCategoryItemViewHodler(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alarm_category_item_list, parent, false));
    }

    @Override
    public void onBindViewHolder(AlarmCategoryItemViewHodler holder, int position) {
        holder.bindView(position);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public class AlarmCategoryItemViewHodler extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView dateView;
        private TextView hourView;
        private View selectorView;
        private TextView devNameView;
        private ImageView alarmTypeIconView;
        private TextView alarmTypeNameView;
        private TextView alarmTypeOverSpeed;
        private TextView addressView;
        private View contentView;

        public AlarmCategoryItemViewHodler(View itemView) {
            super(itemView);
            dateView = (TextView) itemView.findViewById(R.id.item_date);
            hourView = (TextView) itemView.findViewById(R.id.item_hour);
            selectorView = itemView.findViewById(R.id.cbSelect);
            devNameView = (TextView) itemView.findViewById(R.id.item_dev_name);
            alarmTypeIconView = (ImageView) itemView.findViewById(R.id.item_alarm_type_icon);
            alarmTypeNameView = (TextView) itemView.findViewById(R.id.item_alarm_type_name);
            alarmTypeOverSpeed = (TextView) itemView.findViewById(R.id.item_alarm_overspeed);
            addressView = (TextView) itemView.findViewById(R.id.item_address);
            contentView = itemView.findViewById(R.id.item_content_layout);
            contentView.setOnClickListener(this);
            selectorView.setOnClickListener(this);
        }

        protected void bindView(int position) {
            Alarm itemData = mDataList.get(position);
            if (mContentLeftMargin <= 0) {
                mLayoutParams = (RelativeLayout.LayoutParams) contentView.getLayoutParams();
                mContentLeftMargin = mLayoutParams.leftMargin;
            }
            addressView.setText(itemView.getResources().getString(R.string.alarm_address_text, itemData.getAddress()));
            if (mInEditMode) {
                selectorView.setVisibility(View.VISIBLE);
                if (itemData.isSelected()) {
                    selectorView.setSelected(true);
                } else {
                    selectorView.setSelected(false);
                }
                mLayoutParams.leftMargin = mContentLeftMargin;
                contentView.setLayoutParams(mLayoutParams);
                addressView.post(new Runnable() {
                    @Override
                    public void run() {
                        int lineCount = addressView.getLineCount();
                        addressView.setMaxLines(lineCount);
                    }
                });
            } else {
                selectorView.setVisibility(View.GONE);
                mLayoutParams.leftMargin = 0;
                contentView.setLayoutParams(mLayoutParams);
                addressView.setMaxLines(3);
            }
            dateView.setText(AlarmCategoryUtils.formatAlarmDate(itemData.getAlarm_time()));
            hourView.setText(AlarmCategoryUtils.formatAlarmHour(itemData.getAlarm_time()));
            if(TextUtils.isEmpty(itemData.getDev_name()))
            {
                devNameView.setText(itemView.getResources().getString(R.string.alarm_dev_name_text, itemView.getResources().getString(R.string.car_name_empty)));
            }
            else
            {
                devNameView.setText(itemView.getResources().getString(R.string.alarm_dev_name_text, itemData.getDev_name()));
            }

            alarmTypeIconView.setImageResource(AlarmCategoryUtils.getAlarmItemDrawable(itemData.getAlarm_type_id()));
            if (alarm_type_id == 5) {
                //超速报警
                //alarmTypeIconView.setVisibility(View.GONE);
                alarmTypeNameView.setVisibility(View.GONE);
                alarmTypeOverSpeed.setVisibility(View.VISIBLE);
                alarmTypeOverSpeed.setText(itemView.getResources().getString(R.string.alarm_type_overspeed,
                    String.valueOf(itemData.getSpeed())));
            } else if (alarm_type_id == 44) {
                //碰撞报警
                Context c = itemView.getContext();
                int level = itemData.getCrashLevel();
                switch (level) {
                    case 1:
                        alarmTypeNameView.setVisibility(View.VISIBLE);
                        alarmTypeNameView.setText(c.getString(R.string.crash_l1));
                        alarmTypeNameView.setTextColor(Color.parseColor("#939393"));
                        break;
                    case 2:
                        alarmTypeNameView.setVisibility(View.VISIBLE);
                        alarmTypeNameView.setText(c.getString(R.string.crash_l2));
                        alarmTypeNameView.setTextColor(Color.parseColor("#FFB747"));
                        break;
                    case 3:
                        alarmTypeNameView.setVisibility(View.VISIBLE);
                        alarmTypeNameView.setText(c.getString(R.string.crash_l3));
                        alarmTypeNameView.setTextColor(Color.parseColor("#FF5C47"));
                        break;
                    default:
                        alarmTypeNameView.setVisibility(View.GONE);
                        break;
                }
            } else if (alarm_type_id == -1) {
                //其他报警
                alarmTypeNameView.setText(itemData.getAlarm_type());
            } else {
                //alarmTypeIconView.setVisibility(View.GONE);
                alarmTypeNameView.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            Alarm itemData = mDataList.get(adapterPosition);
            switch (v.getId()) {
                case R.id.cbSelect:
                    handleSelector(adapterPosition, itemData);
                    break;
                case R.id.item_content_layout:
                    presentAlarmInfoOnMap(itemData);
                    break;
            }
        }

        private void handleSelector(int position, Alarm itemData) {
            boolean prevSelected = itemData.isSelected();
            if (prevSelected) {
                mDelSelectionList.remove(itemData);
                selectorView.setSelected(false);
            } else {
                mDelSelectionList.add(itemData);
                selectorView.setSelected(true);
            }
            itemData.setSelected(!prevSelected);
            if (mDelSelectionList.isEmpty()) {
                mListener.onHasItemSelected(false);
            } else {
                mListener.onHasItemSelected(true);
            }
        }

        private void presentAlarmInfoOnMap(Alarm itemData) {
            Intent intent = new Intent();
            int iMapType = SettingDataManager.getInstance(itemView.getContext()).getMapTypeInt();
            /*if (iMapType == Constant.MAP_TYPE_GOOGLE) {
                // 谷歌
                intent.setClass(itemView.getContext(), GAlarmLocationActivity.class);
            } else */
            if (iMapType == Constant.MAP_TYPE_AMAP) {// 高德地图
                // 高德地图
                intent.setClass(itemView.getContext(), AMapAlarmLocationActivity.class);
            } else if (iMapType == Constant.MAP_TYPE_TENCENT) {// 腾讯地图
                // 腾讯地图
                intent.setClass(itemView.getContext(), TAlarmLocationActivity.class);
            } else if(iMapType == Constant.MAP_TYPE_GOOGLE)
            {
                intent.setClass(itemView.getContext(), GAlarmLocationActivity.class);
            }
            else {
                // 百度
                intent.setClass(itemView.getContext(), BAlarmLocationActivity.class);
            }
            intent.putExtra("ALARM", itemData);
            itemView.getContext().startActivity(intent);
        }
    }


    //private

    public class MyLinearLayoutManager extends LinearLayoutManager  {
        public MyLinearLayoutManager(Context context) {
            super(context);
        }

        public MyLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        public MyLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        @Override
        public boolean supportsPredictiveItemAnimations() {
            return false;
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            //override this method and implement code as below
            try {
                super.onLayoutChildren(recycler, state);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        @Override
        public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                return super.scrollVerticallyBy(dy, recycler, state);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }
    }
    //public
    public void setInEditMode(boolean inEditMode) {
        mInEditMode = inEditMode;
        notifyDataSetChanged();
        MyLinearLayoutManager manager= new MyLinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(manager);
        //LinearLayoutManager manager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        int firstPosition = manager.findFirstVisibleItemPosition();
        int lastPosition = manager.findLastVisibleItemPosition();
        notifyItemRangeChanged(firstPosition, lastPosition - firstPosition + 1);
    }

    public void setAllSelected(boolean allSelected) {
        mDelSelectionList.clear();
        if(allSelected)
        {
            mDelSelectionList.addAll(mDataList);
        }
        for (Alarm item : mDataList)
        {
            item.setSelected(allSelected);
        }
        notifyDataSetChanged();
        if (mDelSelectionList.isEmpty()) {
            mListener.onHasItemSelected(false);
        } else {
            mListener.onHasItemSelected(true);
        }
    }
    public ArrayList<String> getDelSelection() {
        ArrayList<String> ids = new ArrayList<String>();
        for (Alarm item : mDelSelectionList) {
            ids.add(item.getId());
        }
        return ids;
    }
    public ArrayList<String> getKeepSelection(ArrayList<String> idsDel){
        ArrayList<String> ids = new ArrayList<String>();
        if(idsDel == null || idsDel.size() == mDataList.size()){
            return ids;
        }
        for (Alarm item : mDataList) {
            if( !idsDel.contains(item.getId()) ) {
                ids.add(item.getId());
            }
        }
        return ids;
    }

    public int getDelSlectedCount() {
        return mDelSelectionList.size();
    }

    public void onDelSuccess() {
        for (Alarm item : mDelSelectionList) {
            mDataList.remove(item);
        }
        mDelSelectionList.clear();
        setInEditMode(false);
        if (getItemCount() <= 0) {
            mListener.onEmpty();
        }
    }

    public void appendNextPageData(List<Alarm> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return;
        }
        mDataList.addAll(dataList);
        notifyDataSetChanged();
    }

    public void appendPrevPageData(ArrayList<Alarm> listAlarms) {
        if (listAlarms == null || listAlarms.isEmpty()) {
            return;
        }
        mDataList.addAll(0, listAlarms);
        notifyDataSetChanged();
    }

    public long getPrevAlarmTime() {
        return mDataList.get(mDataList.size() - 1).getAlarm_time();
    }

    public long getNextAlarmTime() {
        return mDataList.get(0).getAlarm_time();
    }

    public List<Alarm> getmDataList() {
        return mDataList;
    }
}
