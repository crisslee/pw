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

import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.coomix.app.all.R;
import com.coomix.app.framework.util.CommonUtil;

import java.util.ArrayList;
import java.util.List;

public class ListViewUpdateAdapter extends BaseAdapter {
    ArrayList<MKOLUpdateElement> listUpdateElements;
    private boolean[] mExpanded;
    Context context;
    private UpdateListener updateListener;
    private int indexOfExpanded;
    private List<Integer> updateCityIds; // 新增的有更新的城市id列表

    public ListViewUpdateAdapter(Context context, ArrayList<MKOLUpdateElement> listUpdateElements) {
        this.context = context;
        this.listUpdateElements = listUpdateElements;
        mExpanded = new boolean[this.listUpdateElements.size()];
        updateCityIds = new ArrayList<Integer>();
        indexOfExpanded = -1;
    }

    public void setData(ArrayList<MKOLUpdateElement> listUpdateElements) {
        this.listUpdateElements = listUpdateElements;
        if (this.listUpdateElements == null) {
            mExpanded = new boolean[0];
        } else {
            mExpanded = new boolean[this.listUpdateElements.size()];
        }
        notifyDataSetChanged();
        if (indexOfExpanded != -1 && indexOfExpanded < mExpanded.length) {
            mExpanded[indexOfExpanded] = true;
        }
    }

    public void setUpdateListener(UpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    @Override
    public int getCount() {
        if (listUpdateElements == null) {
            return 0;
        }
        return listUpdateElements.size();
    }

    @Override
    public Object getItem(int position) {
        if (listUpdateElements == null || position < 0 || position >= listUpdateElements.size()) {
            return null;
        }
        return listUpdateElements.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // ViewHolder viewHolder = null;
        UpdateView updateView;
        MKOLUpdateElement element = listUpdateElements.get(position);
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

    public String formatDataSize(int size) {
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
        private MKOLUpdateElement mElement;

        public UpdateView(Context context, MKOLUpdateElement element, boolean expanded) {
            super(context);
            mElement = element;
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // Here we build the child views in code. They could also have
            // been specified in an XML file.
            infalInflater.inflate(R.layout.list_item_update, this);
            text1 = (TextView) this.findViewById(R.id.tv_city);
            text1.setTypeface(null, Typeface.BOLD);
            textStatus = (TextView) this.findViewById(R.id.tv_status);
            textSize = (TextView) this.findViewById(R.id.tv_size);
            indicator = (ImageView) this.findViewById(R.id.img_indicator);
            linearLayoutExpand = (LinearLayout) this.findViewById(R.id.ll_expand);
            mButtonStart = (Button) this.findViewById(R.id.btn_start);
            mButtonDelete = (Button) this.findViewById(R.id.btn_delete);
            mButtonUpdate = (Button) this.findViewById(R.id.btn_update);
            progressHorizontal = (ProgressBar) findViewById(R.id.progress_horizontal);

            // 下载更新
            mButtonUpdate.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateListener.update(mElement.cityID);
                }
            });
            // 删除
            mButtonDelete.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    updateListener.delete(mElement.cityID);
                    indexOfExpanded = -1;
                }
            });
            update(mElement);

            linearLayoutExpand.setVisibility(expanded ? VISIBLE : GONE);

        }

        private ImageView indicator;
        private TextView text1;
        private TextView textStatus;
        private TextView textSize;
        private LinearLayout linearLayoutExpand;
        private Button mButtonStart;
        private Button mButtonDelete;
        private Button mButtonUpdate;
        private ProgressBar progressHorizontal;

        public void update(MKOLUpdateElement element) {
            mElement = element;
            // text1.setText(element.cityName + "(" + element.cityID + ")");
            text1.setText(element.cityName);

            if (element.update) { // 有地图更新提示
                updateCityIds.clear();// /////////////
                mButtonUpdate.setEnabled(true); // 可用
                // updateListener.hasNewMap(true);

                updateCityIds.add(element.cityID); // ---------------
                textStatus.setText(context.getString(R.string.status_update) + "   " + element.ratio + "% ");
                // updateListener.sendUpdateAllCities(updateCityIds);

            } else {
                mButtonUpdate.setEnabled(false); // 不可点击 下载完成提示
                // updateListener.hasNewMap(false);
                textStatus.setText(CommonUtil.getStatus(context, element.status) + "   " + element.ratio + "% ");
            }
            textSize.setText(formatDataSize(element.size));
            progressHorizontal.setProgress(element.ratio);
            if (element.status == MKOLUpdateElement.FINISHED) {
                mButtonStart.setText(R.string.offline_map_start);
                mButtonStart.setEnabled(false);
            } else {
                mButtonStart.setEnabled(true);
                if (element.status == MKOLUpdateElement.SUSPENDED) {
                    mButtonStart.setText(R.string.offline_map_start);
                    mButtonStart.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            updateListener.start(mElement.cityID);
                        }
                    });
                } else {
                    mButtonStart.setText(R.string.offline_map_pause);
                    mButtonStart.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            updateListener.pause(mElement.cityID);
                        }
                    });
                }
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
        void start(int cityId);

        void pause(int cityId);

        void delete(int cityId);

        void update(int cityId);
    }
}
