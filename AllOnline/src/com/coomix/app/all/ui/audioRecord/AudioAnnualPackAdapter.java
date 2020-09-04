package com.coomix.app.all.ui.audioRecord;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.AudioPackMd;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ly on 2018/2/1 13:58.
 */

public class AudioAnnualPackAdapter extends RecyclerView.Adapter<AudioAnnualPackAdapter.ViewHolderAnnualPack> {

    public void setData(List<AudioPackMd> mData) {
        this.mData = mData;
        notifyDataSetChanged();
    }

    private List<AudioPackMd> mData = new ArrayList<>();
    private int mSelectedItem = -1;


    @Override
    public AudioAnnualPackAdapter.ViewHolderAnnualPack onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_audio_annul_pack, parent, false);
        return new AudioAnnualPackAdapter.ViewHolderAnnualPack(v);
    }


    @Override
    public void onBindViewHolder(AudioAnnualPackAdapter.ViewHolderAnnualPack holder, int position) {
        holder.bind(mData.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
    private void check(int position ) {
        mData.get(position).setChecked(true);
        notifyItemChanged(position);
        if(mSelectedItem != position) {
            if(mSelectedItem >= 0 && mSelectedItem < getItemCount()) {
                mData.get(mSelectedItem).setChecked(false);
                notifyItemChanged(mSelectedItem);
            }
            mSelectedItem = position;
        }
    }

    public class ViewHolderAnnualPack extends RecyclerView.ViewHolder {
        private TextView tvPrice, tvTitle;
        private CheckBox cb;
        private View layoutAnnualPayment;
        private Context mContext;
        public ViewHolderAnnualPack(View itemView) {
            super(itemView);
            tvPrice = (TextView) itemView.findViewById(R.id.tvPrice);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            cb = (CheckBox) itemView.findViewById(R.id.cb);
            layoutAnnualPayment = itemView.findViewById(R.id.layoutAnnualPayment);
            mContext = itemView.getContext();
        }

        public void bind(AudioPackMd audioPackMd, int position) {
            tvTitle.setText(audioPackMd.getTitle());
            tvPrice.setText(audioPackMd.getPrice());
            cb.setChecked(audioPackMd.isChecked());
            cb.setOnClickListener(v -> check(position));
            layoutAnnualPayment.setOnClickListener(v -> check(position));
        }
    }
}
