package com.coomix.app.all.ui.unlock;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.coomix.app.all.R;
import com.coomix.app.all.model.response.LockInfo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class UnLockAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    private List<LockInfo> mDataList = new ArrayList<>();
    private List<LockInfo> mSelectList = new ArrayList<>();

    private boolean isSelectMode = false;

    public UnLockAdapter(Context context){
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }


    public void refreshData(List<LockInfo> lockInfos){
        if(lockInfos != null){
            mDataList.clear();
            mDataList.addAll(lockInfos);
            notifyDataSetChanged();
        }
    }

    public String getInfos(){
        if(mSelectList.isEmpty()){
            return "";
        }

        StringBuilder infos = new StringBuilder();
        for(LockInfo lockInfo : mSelectList){
            infos.append(lockInfo.imei).append(",").append(lockInfo.eid).append(";");
        }
        return infos.toString();
    }

    public void setSelectMode(boolean isSelect){
        isSelectMode = isSelect;
        notifyDataSetChanged();
    }


    public void setSelectAll(boolean all){
        mSelectList.clear();
        if(all){
            mSelectList.addAll(mDataList);
        }
        notifyDataSetChanged();
    }


    public boolean isEmpty(){
        return mDataList.isEmpty();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new LockHolder(mLayoutInflater.inflate(R.layout.act_unlock_item , parent , false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        LockInfo info = mDataList.get(position);
        if(info != null){
            ((LockHolder)holder).bind(info);
        }

    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }



    private class LockHolder extends RecyclerView.ViewHolder{

        private CheckBox checkBox;
        private TextView title;
        private TextView sub;
        private TextView sub2;
        private TextView approve;
        private TextView reject;

        public LockHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.lock_item_check);
            title = itemView.findViewById(R.id.lock_item_title);
            sub = itemView.findViewById(R.id.lock_item_sub);
            sub2 = itemView.findViewById(R.id.lock_item_sub2);
            approve = itemView.findViewById(R.id.lock_item_approve);
            reject = itemView.findViewById(R.id.lock_item_reject);
        }

        public void bind(LockInfo info){

            title.setText(mContext.getString(R.string.lock_account , info.lname));
            sub.setText(mContext.getString(R.string.lock_device , info.devName));
            sub2.setText(info.imei);

            if(isSelectMode){
                checkBox.setVisibility(View.VISIBLE);
                approve.setVisibility(View.GONE);
                reject.setVisibility(View.GONE);
            }else {
                checkBox.setVisibility(View.GONE);
                approve.setVisibility(View.VISIBLE);
                reject.setVisibility(View.VISIBLE);
            }

            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(mSelectList.contains(info));
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked && !mSelectList.contains(info)){
                        mSelectList.add(info);
                        if(mSelectList.size() == mDataList.size()){
                            ((UnLockActivity)mContext).selectAll(true);
                        }
                    }else if(!isChecked && mSelectList.contains(info)){
                        mSelectList.remove(info);
                        ((UnLockActivity)mContext).selectAll(false);
                    }
                }
            });

            itemView.setOnClickListener(View -> checkBox.setChecked(!checkBox.isChecked()));

            approve.setOnClickListener(View -> ((UnLockActivity)mContext).actApproveLock(info.imei +"," + info.eid +";"));
            reject.setOnClickListener(View -> ((UnLockActivity)mContext).actRejectLock(info.imei +"," + info.eid +";"));

        }
    }
}
