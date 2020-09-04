package com.coomix.app.all.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.coomix.app.all.R;
import com.coomix.app.framework.util.CommonUtil;

import java.util.List;

public class ListViewAMapUpdateAdapter extends BaseAdapter {
    List<OfflineMapCity> listUpdateElements;
    Context context;
    private boolean[] mExpanded;
    private UpdateListener updateListener;
    private int indexOfExpanded;

    public ListViewAMapUpdateAdapter(Context context, List<OfflineMapCity> listUpdateElements) {
        this.context = context;
        this.listUpdateElements = listUpdateElements;
        mExpanded = new boolean[this.listUpdateElements.size()];
        indexOfExpanded = -1;
    }

    public void setData(List<OfflineMapCity> listUpdateElements) {
        this.listUpdateElements = listUpdateElements;
        mExpanded = new boolean[this.listUpdateElements.size()];
        if (indexOfExpanded != -1 && indexOfExpanded < mExpanded.length) {
            mExpanded[indexOfExpanded] = true;
        }
        notifyDataSetChanged();
    }

    public void setUpdateListener(UpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        int count = 0;
        if (listUpdateElements != null) {
            count = listUpdateElements.size();
        }
        return count;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return listUpdateElements.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UpdateView updateView;
        OfflineMapCity element = listUpdateElements.get(position);
        if (convertView == null) {
            updateView = new UpdateView(context, element, mExpanded[position]);
        } else {
            updateView = (UpdateView) convertView;
            updateView.update(element);
            updateView.setExpanded(mExpanded[position]);
        }

        return updateView;
    }

    public void toggle(int position) {
        for (int i = 0; i < mExpanded.length; i++) {
            if (i == position) {
                mExpanded[i] = !mExpanded[i];
                if (mExpanded[i]) {
                    indexOfExpanded = i;
                } else {
                    indexOfExpanded = -1;
                }

            } else {
                mExpanded[i] = false;
            }
        }
        notifyDataSetChanged();
    }

    public String formatDataSize(long size) {
        String ret = "";
        if (size < (1024 * 1024)) {
            ret = String.format("%dK", size / 1024);
        } else {
            ret = String.format("%.1fM", size / (1024 * 1024.0));
        }
        return ret;
    }

    /**
     * We will use a SpeechView to display each speech. It's just a LinearLayout
     * with two text fields.
     * 
     */
    private class UpdateView extends FrameLayout {
        private OfflineMapCity mElement;

        public UpdateView(Context context, OfflineMapCity element, boolean expanded) {
            super(context);
            mElement = element;
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            infalInflater.inflate(R.layout.list_item_update, this);
            text1 = (TextView) this.findViewById(R.id.tv_city);
            text1.setTypeface(null, Typeface.BOLD);
            textStatus = (TextView) this.findViewById(R.id.tv_status);
            tv_status_temp = (TextView) this.findViewById(R.id.tv_status_temp);
            textSize = (TextView) this.findViewById(R.id.tv_size);
            indicator = (ImageView) this.findViewById(R.id.img_indicator);
            linearLayoutExpand = (LinearLayout) this.findViewById(R.id.ll_expand);
            mButtonStart = (Button) this.findViewById(R.id.btn_start);
            btn_start_temp = (Button) this.findViewById(R.id.btn_start_temp);
            mButtonDelete = (Button) this.findViewById(R.id.btn_delete);
            mButtonUpdate = (Button) this.findViewById(R.id.btn_update);
            progressHorizontal = (ProgressBar) findViewById(R.id.progress_horizontal);

            // 更新
            mButtonUpdate.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateListener.delete(mElement);
                    updateListener.update(mElement);
                }
            });
            // 删除
            mButtonDelete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateListener.delete(mElement);
                    indexOfExpanded = -1;
                }
            });

            update(mElement);

            linearLayoutExpand.setVisibility(expanded ? VISIBLE : GONE);

        }

        // 判断地图是否有更新
        private boolean isExistMapUpdate(String cityName) {
            try {
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        private ImageView indicator;
        private TextView text1;
        private TextView textStatus, tv_status_temp;
        private TextView textSize;
        private LinearLayout linearLayoutExpand;
        private Button mButtonStart, btn_start_temp;
        private Button mButtonDelete;
        private Button mButtonUpdate;
        private ProgressBar progressHorizontal;

        // 刷新状态
        public void update(final OfflineMapCity element) {
            mElement = element;
            text1.setText(element.getCity());

            if (isExistMapUpdate(element.getCity())) {
                mButtonUpdate.setEnabled(true);// 有更新
                textStatus
                        .setText(context.getString(R.string.status_update) + "   " + element.getcompleteCode() + "% ");
            } else {
                mButtonUpdate.setEnabled(false); // 判断当前的下载状态

                textStatus.setText(CommonUtil.getStatus_amap(context, element.getState()) + "   "
                        + element.getcompleteCode() + "% ");
                if (element.getState() == 0) { // 正在下载
                    mButtonStart.setOnClickListener(new OnClickListener() {
                        @Override // 暂停
                        public void onClick(View v) {
                            updateListener.pause();
                            btn_start_temp.setVisibility(View.VISIBLE);
                            mButtonStart.setVisibility(View.GONE);
                            String str = context.getString(R.string.status_pause);
                            tv_status_temp.setText(str + "   " + element.getcompleteCode() + "% ");
                            tv_status_temp.setVisibility(View.VISIBLE);
                            textStatus.setVisibility(View.GONE);

                            return;
                        }
                    });

                    btn_start_temp.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) { // 开始下载
                            updateListener.start(mElement);
                            btn_start_temp.setVisibility(View.GONE);
                            mButtonStart.setVisibility(View.VISIBLE);
                            String str = context.getString(R.string.status_pause);
                            textStatus.setText(str + "   " + element.getcompleteCode() + "% ");
                            textStatus.setVisibility(View.VISIBLE);
                            tv_status_temp.setVisibility(View.GONE);

                            return;
                        }
                    });
                    btn_start_temp.setEnabled(true);
                    btn_start_temp.setVisibility(View.GONE);
                    mButtonStart.setText(R.string.offline_map_pause);
                    mButtonStart.setEnabled(true);
                    mButtonStart.setVisibility(View.VISIBLE);
                    
                } else if (element.getState() == 3) {// 暂停
                    mButtonStart.setText(R.string.offline_map_pause);// offline_map_pause
                    mButtonStart.setEnabled(true);
                } else if (element.getState() == 4) {// 下载完成的
                    if (element.getState() == OfflineMapStatus.SUCCESS) {
                        mButtonStart.setText(R.string.offline_map_start);
                        btn_start_temp.setVisibility(View.GONE);
                        mButtonStart.setVisibility(View.VISIBLE);
                        mButtonStart.setEnabled(false);
                    }

                }
            }

            textSize.setText(formatDataSize(element.getSize()));
            if (element.getState() == OfflineMapStatus.SUCCESS) {
                progressHorizontal.setProgress(100);
                textStatus.setText(CommonUtil.getStatus_amap(context, element.getState()) + "   "
                        + 100 + "% ");
            } else {
                progressHorizontal.setProgress(element.getcompleteCode());
            }

        }

        /**
         * Convenience method to expand or hide the dialogue
         */
        public void setExpanded(boolean expanded) {
            linearLayoutExpand.setVisibility(expanded ? VISIBLE : GONE);
            if (expanded) {
                indicator.setImageResource(R.drawable.group_indicator_collapse);
            } else {
                indicator.setImageResource(R.drawable.group_indicator_expand);
            }
        }

    }

    public interface UpdateListener {
        void start(OfflineMapCity cityName);

        void pause();

        void delete(OfflineMapCity cityName);

        void update(OfflineMapCity cityName);

    }
}