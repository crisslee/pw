package com.coomix.app.all.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.amap.api.services.core.PoiItem;
import com.coomix.app.all.R;
import com.coomix.app.all.widget.MyActionbar;
import java.util.List;

/**
 * @author shishenglong
 * @since 2016年10月15日 下午7:08:44
 */
public class PopNearbyListDialog extends Dialog {
    private ListView listView = null;
    private List<PoiItem> listData = null;
    private int iSelect = 0;

    public PopNearbyListDialog(Context context, List<PoiItem> listData) {
        this(context, R.style.add_dialog, listData);
    }

    public PopNearbyListDialog(Context context, int theme, List<PoiItem> listData) {
        super(context, theme);

        this.listData = listData;

        initViews();
    }

    public void setSelect(int iSelect) {
        this.iSelect = iSelect;
    }

    private void initViews() {
        View mainView = LayoutInflater.from(getContext()).inflate(R.layout.popwindow_rp_location_list, null);
        setContentView(mainView);

        MyActionbar actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, R.string.now_location, 0, 0);
        actionbar.setBackgroundColor(getContext().getResources().getColor(R.color.redpacket_red));
        actionbar.setLeftImageVisibility(View.GONE);
        actionbar.setLeftText(R.string.cancel);
        actionbar.setLeftTextCLickListener(view -> dismiss());

        listView = (ListView) mainView.findViewById(R.id.pop_listview);

        setCanceledOnTouchOutside(true);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.windowAnimations = R.style.AnimBottom;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);

        if (listData != null && listData.size() > 0) {
            listView.setAdapter(new LocationAdapter());
        }
    }

    public void setData(List<PoiItem> listData) {
        this.listData = listData;
        listView.setAdapter(new ArrayAdapter<PoiItem>(getContext(), R.layout.pop_item_list, listData));
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        listView.setOnItemClickListener(listener);
    }

    class LocationAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return listData == null ? 0 : listData.size();
        }

        @Override
        public Object getItem(int position) {
            if (position >= 0 && position < listData.size()) {
                return listData.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.rp_location_list_item, null);
                viewHolder.textTitle = (TextView) convertView.findViewById(R.id.textViewTitle);
                viewHolder.textDetail = (TextView) convertView.findViewById(R.id.textViewDetail);
                viewHolder.imageSelected = (ImageView) convertView.findViewById(R.id.imageViewSelected);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Object object = getItem(position);
            PoiItem poiItem = null;
            if (object != null && object instanceof PoiItem) {
                poiItem = (PoiItem) object;
            } else {
                return convertView;
            }

            viewHolder.textTitle.setText(poiItem.getTitle());

            String detail = poiItem.getSnippet();
            if (!TextUtils.isEmpty(poiItem.getAdName())) {
                detail = poiItem.getAdName() + detail;
            }
            if (!TextUtils.isEmpty(poiItem.getCityName())) {
                detail = poiItem.getCityName() + detail;
            }
            if (!TextUtils.isEmpty(poiItem.getProvinceName())) {
                detail = poiItem.getProvinceName() + detail;
            }
            viewHolder.textDetail.setText(detail);

            if (iSelect == position) {
                viewHolder.imageSelected.setVisibility(View.VISIBLE);
            } else {
                viewHolder.imageSelected.setVisibility(View.GONE);
            }

            return convertView;
        }
    }

    class ViewHolder {
        TextView textTitle;
        TextView textDetail;
        ImageView imageSelected;
    }
}
