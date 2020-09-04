package com.coomix.app.all.ui.platformRecharge;

import android.content.Context;
import android.support.transition.TransitionManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import com.coomix.app.all.R;
import com.coomix.app.all.manager.ThemeManager;
import com.coomix.app.all.model.request.ReqRenewOrder;
import com.coomix.app.all.model.response.RespPlatDevList;
import com.coomix.app.all.util.ViewUtil;
import com.coomix.app.pay.CoomixPayManager;
import com.coomix.app.pay.WechatPayManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ly on 2017/12/8 20:53.
 */
public class PlatRechargeAdapter extends RecyclerView.Adapter<PlatRechargeAdapter.PlatRechargeViewHolder> {
    private int payPlatform = CoomixPayManager.PAY_WECHAT;
    private int payManner = WechatPayManager.WECHAT_PAY_APP;
    private List<RespPlatDevList.PlatRechargeBean> mData = new ArrayList<>();

    private OnItemSelectListener mOnItemSelectListener;
    private OnBtnRechargeOnclickListne mOnBtnRechargeOnclickListne;

    private boolean mIsSeletMode = false;
    private boolean mIsSelectAll = false;

    private int mCount;
    private long mTotalMoney;

    /**
     * 会初始化，清空状态
     * @param data
     */
    public void setData(List<RespPlatDevList.PlatRechargeBean> data) {
        processSelectMode(data);
        mData.clear();
        this.mData.addAll(data);
        notifyDataSetChanged();
        mOnItemSelectListener.showEmpty(mData.size() == 0);
        mCount = 0;
        mTotalMoney = 0;
        reCalculate();
    }

    public void addData(List<RespPlatDevList.PlatRechargeBean> data) {
        processSelectMode(data);
        this.mData.addAll(data);
        notifyDataSetChanged();
        mOnItemSelectListener.showEmpty(mData.size() == 0);
        reCalculate();
    }

    private void processSelectMode(List<RespPlatDevList.PlatRechargeBean> data) {
        if (data == null) {
            return;
        }
        if (mIsSeletMode) {
            for (RespPlatDevList.PlatRechargeBean md : data) {
                md.setSelectMode(true);
                md.setSelected(mIsSelectAll);
            }
        }
    }

    public ReqRenewOrder getReqRenewDevOrder(RespPlatDevList.PlatRechargeBean prbean) {
        ReqRenewOrder reqRenewOrder = new ReqRenewOrder();
        long total = 0;
        ReqRenewOrder.DevRenewBean bean = prbean.toDevRenewBean();
        total += bean.getFee_amount();
        reqRenewOrder.getRenewItemsInfo().add(bean);
        reqRenewOrder.setAmount(total);
        reqRenewOrder.setPay_manner(payManner);
        reqRenewOrder.setPay_plat(payPlatform);

        return reqRenewOrder;
    }

    public ReqRenewOrder getReqRenewDevOrders() {
        ReqRenewOrder reqRenewOrder = new ReqRenewOrder();
        long total = 0;
        for (RespPlatDevList.PlatRechargeBean md : mData) {
            if (md.isSelected()) {
                ReqRenewOrder.DevRenewBean bean = md.toDevRenewBean();
                total += bean.getFee_amount();
                reqRenewOrder.getRenewItemsInfo().add(bean);
            }
        }
        reqRenewOrder.setAmount(total);
        reqRenewOrder.setPay_manner(payManner);
        reqRenewOrder.setPay_plat(payPlatform);
        return reqRenewOrder;
    }

    public void setmOnBtnRechargeOnclickListne(OnBtnRechargeOnclickListne mOnBtnRechargeOnclickListne) {
        this.mOnBtnRechargeOnclickListne = mOnBtnRechargeOnclickListne;
    }

    public interface OnItemSelectListener {
        void onItemsSelected(long totalMoney, int totalNum);

        void showEmpty(boolean show);
    }

    public interface OnBtnRechargeOnclickListne {
        void onClick(ReqRenewOrder reqRenewOrder);
    }


    public void setmOnItemSelectListener(OnItemSelectListener mOnItemSelectListener) {
        this.mOnItemSelectListener = mOnItemSelectListener;
    }

    public boolean ismSeletMode() {
        return mIsSeletMode;
    }

    public void setSelectMode(boolean isSelectMode) {
        mIsSeletMode = isSelectMode;
        for (RespPlatDevList.PlatRechargeBean md : mData) {

            md.setSelectMode(isSelectMode);
            if (md.isSelected()) {
                mCount++;
                mTotalMoney += md.getFee().get(0).getAmount();
            }
        }
        notifyDataSetChanged();
        mOnItemSelectListener.onItemsSelected(mTotalMoney, mCount);
    }

    private void reCalculate() {
        for (RespPlatDevList.PlatRechargeBean md : mData) {
            if (md.isSelected()) {
                mCount++;
                mTotalMoney += md.getFee().get(0).getAmount();
            }
        }
        mOnItemSelectListener.onItemsSelected(mTotalMoney, mCount);
    }

    public void cancelSelectall() {
        mIsSelectAll = false;
        for (RespPlatDevList.PlatRechargeBean md : mData) {
            md.setSelected(false);
        }
        mCount = 0;
        mTotalMoney = 0;
        notifyDataSetChanged();
        mOnItemSelectListener.onItemsSelected(0, 0);
    }

    public void selectAll() {
        mIsSelectAll = true;
        for (RespPlatDevList.PlatRechargeBean md : mData) {
            if (md.couldRecharge()) {
                try {
                    mTotalMoney += md.getFee().get(0).getAmount();
                    mCount++;
                    md.setSelected(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        mOnItemSelectListener.onItemsSelected(mTotalMoney, mCount);
        notifyDataSetChanged();
    }

    @Override
    public PlatRechargeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_plat_recharge, parent, false);
        return new PlatRechargeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PlatRechargeViewHolder holder, int position) {
        holder.bind(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class PlatRechargeViewHolder extends RecyclerView.ViewHolder {
        private View rootview;
        private Context mContext;
        CheckBox cbSelect;
        TextView tvStatus;
        TextView tvName;
        TextView tvInfo;
        TextView btnRecharge;

        public PlatRechargeViewHolder(View itemView) {
            super(itemView);
            rootview = itemView;
            mContext = rootview.getContext();
            cbSelect = (CheckBox) itemView.findViewById(R.id.cbSelect);
            tvStatus = (TextView) itemView.findViewById(R.id.tvStatus);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvInfo = (TextView) itemView.findViewById(R.id.tvInfo);
            btnRecharge = (TextView) itemView.findViewById(R.id.btnRecharge);
            ViewUtil.setBg(btnRecharge, ThemeManager.getInstance().getBGColorDrawable(mContext));
            btnRecharge.setTextColor(ThemeManager.getInstance().getThemeAll().getThemeColor().getColorNavibarText());
        }

        public void bind(RespPlatDevList.PlatRechargeBean platRechargeBean) {
            tvName.setText(platRechargeBean.getUiShownName());
            tvInfo.setText(mContext.getString(R.string.expiry_date, platRechargeBean.getExpire_time(),
                platRechargeBean.getFee().get(0).getFeeInfoStr(mContext)));
            switch (platRechargeBean.getExpire_type()) {
                case RespPlatDevList.PlatRechargeBean.TYPE_EXPIRED:
                    tvStatus.setVisibility(View.VISIBLE);
                    tvStatus.setTextColor(mContext.getResources().getColor(R.color.recharge_red));
                    break;
                case RespPlatDevList.PlatRechargeBean.TYPE_ABOUT_TO_EXPIRED:
                    tvStatus.setVisibility(View.VISIBLE);
                    tvStatus.setTextColor(mContext.getResources().getColor(R.color.recharge_yellow));
                    break;
                case RespPlatDevList.PlatRechargeBean.TYPE_NEVER_EXPIRED:
                    tvStatus.setVisibility(View.GONE);
                    tvInfo.setText(platRechargeBean.getExpireTypeMessage(mContext));
                    break;
                case RespPlatDevList.PlatRechargeBean.TYPE_NORMAL:
                default:
                    tvStatus.setVisibility(View.GONE);
                    break;
            }
            tvStatus.setText(platRechargeBean.getExpireTypeMessage(mContext));
            btnRecharge.setOnClickListener(
                v -> mOnBtnRechargeOnclickListne.onClick(getReqRenewDevOrder(platRechargeBean)));
            TransitionManager.beginDelayedTransition((ViewGroup) rootview);
            if (platRechargeBean.isSelectMode()) {
                btnRecharge.setVisibility(View.GONE);
                if (platRechargeBean.couldRecharge()) {
                    cbSelect.setVisibility(View.VISIBLE);
                } else {
                    cbSelect.setVisibility(View.INVISIBLE);
                }
                cbSelect.setChecked(platRechargeBean.isSelected());
                cbSelect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (cbSelect.isChecked()) {
                            platRechargeBean.setSelected(true);
                            mCount++;
                            mTotalMoney += platRechargeBean.getFee().get(0).getAmount();
                            mOnItemSelectListener.onItemsSelected(mTotalMoney, mCount);
                        } else {
                            platRechargeBean.setSelected(false);
                            mCount--;
                            mTotalMoney -= platRechargeBean.getFee().get(0).getAmount();
                            mOnItemSelectListener.onItemsSelected(mTotalMoney, mCount);
                        }
                    }
                });
            } else {
                if (platRechargeBean.couldRecharge()) {
                    btnRecharge.setVisibility(View.VISIBLE);
                } else {
                    btnRecharge.setVisibility(View.GONE);
                }
                cbSelect.setVisibility(View.GONE);
            }
        }
    }
}
