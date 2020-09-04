package com.coomix.app.all.ui.cardRecharge;

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
import com.coomix.app.all.model.response.WLCardRecharge;
import com.coomix.app.all.util.CommunityUtil;
import com.coomix.app.all.util.ViewUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ly on 2017/12/8 20:53.
 */
public class CardRechargeAdapter extends RecyclerView.Adapter<CardRechargeAdapter.PlatRechargeViewHolder> {
    private List<WLCardRecharge> mData = new ArrayList<>();
    private OnItemSelectListener mOnItemSelectListener;
    private OnBtnRechargeOnclickListne mOnBtnRechargeOnclickListne;
    private boolean mIsSeletMode = false;
    private int mCount;
    private long mTotalMoney;

    /**
     * 会初始化，清空状态
     *
     * @param data
     */
    public void setData(List<WLCardRecharge> data) {
        mData.clear();
        this.mData.addAll(data);
        notifyDataSetChanged();
        mOnItemSelectListener.showEmpty(mData.size() == 0);
        mCount = 0;
        mTotalMoney = 0;
    }

    public void addData(List<WLCardRecharge> data) {
        this.mData.addAll(data);
        notifyDataSetChanged();
        mOnItemSelectListener.showEmpty(mData.size() == 0);
    }


    public void setmOnBtnRechargeOnclickListne(OnBtnRechargeOnclickListne mOnBtnRechargeOnclickListne) {
        this.mOnBtnRechargeOnclickListne = mOnBtnRechargeOnclickListne;
    }

    public interface OnItemSelectListener {
        void onItemsSelected(long totalMoney, int totalNum);

        void showEmpty(boolean show);
    }

    public interface OnBtnRechargeOnclickListne {
        void onClick(WLCardRecharge wlCardRecharge);
    }

    public void setmOnItemSelectListener(OnItemSelectListener mOnItemSelectListener) {
        this.mOnItemSelectListener = mOnItemSelectListener;
    }

    public boolean isSeletMode() {
        return mIsSeletMode;
    }

    public void setSelectMode(boolean bSelect) {
        this.mIsSeletMode = bSelect;
        if(!bSelect){
            for (WLCardRecharge md : mData) {
                md.setSelected(false);
            }
            mCount = 0;
            mTotalMoney = 0;
        }
        notifyDataSetChanged();
    }

    public void setSelectedItem(int position) {
        if (position >= 0 && position < mData.size()) {
            WLCardRecharge cardPrice = mData.get(position);
            if (cardPrice != null) {
                cardPrice.setSelected(cardPrice.isSelected());
            }
            if (cardPrice.isCanRecharge()) {
                if (cardPrice.isSelected()) {
                    mCount++;
                    mTotalMoney += cardPrice.getPrice();
                } else {
                    mCount--;
                    mTotalMoney -= cardPrice.getPrice();
                }
            }
            if (mTotalMoney < 0) {
                mTotalMoney = 0;
            }
            if (mCount <= 0) {
                mCount = 0;
            }
        }
        notifyDataSetChanged();
        mOnItemSelectListener.onItemsSelected(mTotalMoney, mCount);
    }

    public void setSelectAll(boolean bAll) {
        for (WLCardRecharge md : mData) {
            if (bAll) {
                md.setSelected(true);
                if (md.isSelected() && md.isCanRecharge()) {
                    mCount++;
                    mTotalMoney += md.getPrice();
                }
            } else {
                md.setSelected(false);
            }
        }
        if (!bAll) {
            mCount = 0;
            mTotalMoney = 0;
        }
        notifyDataSetChanged();
        mOnItemSelectListener.onItemsSelected(mTotalMoney, mCount);
    }

    public ArrayList<WLCardRecharge> getSelectedCardList(){
        ArrayList<WLCardRecharge> list = new ArrayList<WLCardRecharge>();
        for(WLCardRecharge md : mData){
            if(md != null && md.isSelected() && md.isCanRecharge()){
                list.add(md);
            }
        }
        return list;
    }

    @Override
    public PlatRechargeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card_recharge, parent, false);
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
        TextView tvCardNO;
        TextView tvName;
        TextView tvInfo;
        TextView btnRecharge;

        public PlatRechargeViewHolder(View itemView) {
            super(itemView);
            rootview = itemView;
            mContext = rootview.getContext();
            cbSelect = (CheckBox) itemView.findViewById(R.id.cbSelect);
            tvCardNO = (TextView) itemView.findViewById(R.id.tvCardNO);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvInfo = (TextView) itemView.findViewById(R.id.tvInfo);
            btnRecharge = (TextView) itemView.findViewById(R.id.btnRecharge);
            ViewUtil.setBg(btnRecharge, ThemeManager.getInstance().getBGColorDrawable(mContext));
            btnRecharge.setTextColor(ThemeManager.getInstance().getThemeAll().getThemeColor().getColorNavibarText());
        }

        public void bind(final WLCardRecharge cardInfo) {
            tvName.setText(mContext.getString(R.string.dev_recahge_name, cardInfo.getUserName()));
            tvCardNO.setText(mContext.getString(R.string.card_no, cardInfo.getMsisdn()));

            if(cardInfo.isCanRecharge()){
                String price = mContext.getString(R.string.recharge_format, CommunityUtil.getDecimalStrByInt( cardInfo.getPrice(), 2));
                tvInfo.setText(mContext.getString(R.string.expiry_date, cardInfo.getC_out_time(), price));
            }else{
                tvInfo.setText(mContext.getString(R.string.expiry_date, cardInfo.getC_out_time(), ""));
            }

            TransitionManager.beginDelayedTransition((ViewGroup) rootview);

            if (mIsSeletMode && cardInfo.isCanRecharge()) {
                cbSelect.setVisibility(View.VISIBLE);
                btnRecharge.setVisibility(View.GONE);
            } else {
                cbSelect.setVisibility(View.GONE);
                if (cardInfo.isCanRecharge()) {
                    btnRecharge.setVisibility(View.VISIBLE);
                } else {
                    btnRecharge.setVisibility(View.GONE);
                }
            }

            btnRecharge.setOnClickListener(view -> mOnBtnRechargeOnclickListne.onClick(cardInfo));

            cbSelect.setChecked(cardInfo.isSelected());
            cbSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (cbSelect.isChecked()) {
                        cardInfo.setSelected(true);
                        mCount++;
                        mTotalMoney += cardInfo.getPrice();
                        mOnItemSelectListener.onItemsSelected(mTotalMoney, mCount);
                    } else {
                        cardInfo.setSelected(false);
                        mCount--;
                        mTotalMoney -= cardInfo.getPrice();
                        mOnItemSelectListener.onItemsSelected(mTotalMoney, mCount);
                    }
                }
            });
        }
    }
}
