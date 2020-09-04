package com.coomix.app.all.ui.platformRecharge;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coomix.app.all.R;
import com.coomix.app.all.model.response.RespRefrshRenewPayOrder;
import com.goome.gpns.utils.DateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ly on 2017/12/21 14:39.
 */

public class PlatRechargeSuccAdapter extends RecyclerView.Adapter<PlatRechargeSuccAdapter.PrsHolder> {

    private List<RespRefrshRenewPayOrder.DataBean.RenewInfosBean> mData = new ArrayList<>();

    @Override
    public PrsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plat_recharge_succ, parent, false);
        return new PrsHolder(v);
    }

    @Override
    public void onBindViewHolder(PrsHolder holder, int position) {
        holder.bind(mData.get(position));

    }

    @Override
    public int getItemCount() {

        return mData.size();
    }

    public void setData(List<RespRefrshRenewPayOrder.DataBean.RenewInfosBean> data) {
        mData.addAll(data);
        notifyDataSetChanged();
    }

    public class PrsHolder extends RecyclerView.ViewHolder {

        private TextView tvDevName, tvDeadline;

        public PrsHolder(View itemView) {
            super(itemView);
            tvDeadline = (TextView) itemView.findViewById(R.id.tvDeadline);
            tvDevName = (TextView) itemView.findViewById(R.id.tvDevName);
        }

        public void bind(RespRefrshRenewPayOrder.DataBean.RenewInfosBean item) {
            tvDevName.setText(item.getUser_name());
            String date = DateUtil.getStringByFormat(item.getExpire_time(), DateUtil.dateFormatYMD);
            tvDeadline.setText( date);
        }
    }
}
