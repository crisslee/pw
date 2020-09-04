package com.coomix.app.all.ui.audioRecord;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.AudioPackMd;
import com.coomix.app.all.widget.CheckableLinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ly on 2018/2/1 13:57.
 */

public class AudioPackAdapter extends RecyclerView.Adapter<AudioPackAdapter.ViewHolderPack> {

    public void setData(List<AudioPackMd> mData) {
        this.mData = mData;
        notifyDataSetChanged();
    }

    private List<AudioPackMd> mData = new ArrayList<>();
    private int mSelectedItem = -1;

    @Override
    public ViewHolderPack onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_audio_pack, parent, false);
        return new ViewHolderPack(v);
    }


    @Override
    public void onBindViewHolder(ViewHolderPack holder, int position) {
        holder.bind(mData.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    private void check(int position ) {
        mData.get(position).setChecked(true);

        if(mSelectedItem != position) {
            if(mSelectedItem >= 0 && mSelectedItem < getItemCount()) {
                mData.get(mSelectedItem).setChecked(false);
                notifyItemChanged(mSelectedItem);
            }
            mSelectedItem = position;
        }
    }



    public class ViewHolderPack extends RecyclerView.ViewHolder {

        private CheckableLinearLayout layout_pack;
        private TextView tvTitlePack, tvPricePack;
        private boolean isChecked;
        private Context mContext;

        public ViewHolderPack(View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            layout_pack = (CheckableLinearLayout) itemView.findViewById(R.id.layout_pack);
            tvTitlePack = (TextView) itemView.findViewById(R.id.tvTitlePack);
            tvPricePack = (TextView) itemView.findViewById(R.id.tvPricePack);
        }

        public void bind(AudioPackMd audioPackMd, int position) {
            isChecked = audioPackMd.isChecked();
            tvTitlePack.setText(audioPackMd.getTitle());
            tvPricePack.setText(audioPackMd.getPrice());
            if(isChecked) {
                layout_pack.setChecked(true);
                tvPricePack.setTextColor(mContext.getResources().getColor(R.color.primary));
                tvTitlePack.setTextColor(mContext.getResources().getColor(R.color.primary));
            } else {
                layout_pack.setChecked(false);
                tvPricePack.setTextColor(mContext.getResources().getColor(R.color.color_main_text));
                tvTitlePack.setTextColor(mContext.getResources().getColor(R.color.color_main_text));
            }
            layout_pack.setOnClickListener(v -> {
                if(!isChecked) {
                    layout_pack.setChecked(true);
                    tvPricePack.setTextColor(mContext.getResources().getColor(R.color.primary));
                    tvTitlePack.setTextColor(mContext.getResources().getColor(R.color.primary));
                    check(position);
                }
            });
        }
    }
}
