package com.coomix.app.all.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.GlideApp;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.ActDisplay;
import com.coomix.app.all.model.bean.ActRegister2Bean;
import com.coomix.app.all.model.bean.ImageInfo;
import com.coomix.app.all.log.GoomeLog;
import com.coomix.app.framework.util.PreferenceUtil;
import com.coomix.app.all.util.CommunityPictureUtil;
import com.coomix.app.all.util.ImageCompressUtils;
import com.coomix.app.all.util.StringUtil;
import com.muzhi.camerasdk.model.CameraSdkParameterInfo;
import com.muzhi.camerasdk.view.ThumbnailImageView;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by ly on 2017/6/20.
 */
public class ActRegister2Adapter extends RecyclerView.Adapter {

    private ActRegister2Activity mActRegister2Activity;
    private List<ActDisplay> mData = new ArrayList<>(10);

    private static final int VIEW_TYPE_TEXT = 0;
    private static final int VIEW_TYPE_TEXT_ADDR = 3;
    private static final int VIEW_TYPE_UPLOAD_IMG = 9;

    public ActRegister2Adapter(ActRegister2Activity mActRegisterActivity) {
        this.mActRegister2Activity = mActRegisterActivity;
    }

    public void setData(List<ActDisplay> data) {
        mData = data;
    }

    public void addData(List<ActDisplay> data) {
        mData.addAll(data);
    }

    //把图片的参数，拼接成json数组，放到input字段中
    private void prepareUploadedImage() {
        for (ActDisplay md : mData) {

            switch (getItemViewType(md)) {
                case VIEW_TYPE_UPLOAD_IMG:
                    JSONArray jsonArray = new JSONArray();
                    int i = 0;
                    for (ImageInfo infor : md.getImageList()) {
                        try {
                            jsonArray.put(i, infor.getDomain() + "/" + infor.getImg_path());
                            i++;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    md.setInput(jsonArray.toString());
                    break;
            }
        }

    }

    public Map<String, String> getInputData() {
        prepareUploadedImage();

        HashMap<String, String> map = new HashMap<>();
        for (ActDisplay md : mData) {
            map.put(md.getParam(), md.getInput());
        }

        return map;
    }

    public ArrayList<ImageInfo> getAllImage() {
        ArrayList<ImageInfo> lst = new ArrayList<>();
        for (ActDisplay md : mData) {

            switch (getItemViewType(md)) {
                case VIEW_TYPE_UPLOAD_IMG:
                    lst.addAll(md.getImageList());
                    break;
            }
        }
        return lst;
    }


    public int getItemViewType(ActDisplay md) {

        int type = md.getType();
        switch (type) {
            case 9:
                return VIEW_TYPE_UPLOAD_IMG;
            case 1:
            case 2:
            case 3:
                if (md.getParam().equals("addr")) {
                    return VIEW_TYPE_TEXT_ADDR;
                } else {
                    return VIEW_TYPE_TEXT;
                }
            default:
                return VIEW_TYPE_TEXT;

        }

    }

    public int getItemViewType(int position) {

        return getItemViewType(mData.get(position));
    }

    public List<ActDisplay> getData() {
        return mData;
    }

    /**
     * 检查参数
     *
     * @return
     */
    public String checkParams() {
        for (ActDisplay md : mData) {

            switch (getItemViewType(md)) {
                case VIEW_TYPE_TEXT:
                    if (TextUtils.isEmpty(md.getInput())) {
                        return "请输入" + md.getName();
                    }
                    if ("idcard".equals(md.getParam())) {
                        if (!StringUtil.isIdcardValid(md.getInput())) {
                            return "请输入正确的身份证";
                        }
                    }
                    if ("tel".equals(md.getParam())) {
                        if (!StringUtil.isPhoneValid(md.getInput())) {
                            return "请输入正确的电话号码";
                        }
                    }
                    break;
                case VIEW_TYPE_UPLOAD_IMG:
                    if (md.getImageList() == null ||
                            (md.getImageList().size() == 1 && md.getImageList().get(0).isAddButton())) {
                        return "请上传" + md.getName();
                    }
                    break;
                default:
                    if (TextUtils.isEmpty(md.getInput())) {
                        return "请输入" + md.getName();
                    }
            }

        }
        return "";
    }


    public void loadDataFromFile() {
        Object obj = PreferenceUtil.getObj(ActRegister2Bean.TAG);
        if (obj != null) {
            HashMap<String, String> map = ((ActRegister2Bean) obj).getData();
            for (ActDisplay md : mData) {
                md.setInput(map.get(md.getParam()));
            }
        }
    }

    /**
     * 把para 和input存储在字典中
     */
    public void saveDataToFile() {
        ActRegister2Bean bean = new ActRegister2Bean();
        for (ActDisplay md : mData) {
            bean.getData().put(md.getParam(), md.getInput());
        }
        PreferenceUtil.commitObj(ActRegister2Bean.TAG, bean);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_TEXT:
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_act_resister2, parent, false);
                return new ViewHolderTxtY(v);
            case VIEW_TYPE_TEXT_ADDR:
                View vaddr = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_act_addr_resister2, parent, false);
                return new ViewHolderTxtAddrY(vaddr);
            case VIEW_TYPE_UPLOAD_IMG:
                View vpic = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_act_pic_resister2, parent, false);
                return new ViewHolderUploadPicY(vpic);
            default:
                View def = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_act_resister2, parent, false);
                return new ViewHolderTxtY(def);

        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_TEXT:
                bindViewHolderTxt((ViewHolderTxtY) holder, position);
                break;
            case VIEW_TYPE_UPLOAD_IMG:
                bindViewHolderUploadPic((ViewHolderUploadPicY) holder, position);
                break;
            case VIEW_TYPE_TEXT_ADDR:
                bindViewHolderAddrTxt((ViewHolderTxtAddrY) holder, position);
                break;
        }
    }

    private void bindViewHolderUploadPic(final ViewHolderUploadPicY holder, int position) {
        ActDisplay md = mData.get(position);
        holder.tvName.setText(md.getName());
        final GridViewAdapter adapter = new GridViewAdapter(mActRegister2Activity, md.getImageList(), position);
        holder.gvPic.setAdapter(adapter);
        holder.gvPic.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("NewApi")
            @Override
            public void onGlobalLayout() {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    adapter.setItemSize(holder.gvPic.getColumnWidth());
                    holder.gvPic.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    int width = 0;
                    try {
                        Field field = GridView.class.getDeclaredField("mColumnWidth");
                        field.setAccessible(true);
                        Integer value = (Integer) field.get(this);
                        field.setAccessible(false);
                        width = value.intValue();
                    } catch (Exception e) {
                    }
                    if (width <= 100) {
                        width = holder.gvPic.getWidth() / 4 - (holder.gvPic.getPaddingLeft() / 2);
                    }
                    adapter.setItemSize(width);
                    holder.gvPic.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
        adapter.notifyDataSetChanged();
    }


    private void bindViewHolderAddrTxt(ViewHolderTxtAddrY holder, int position) {
        ActDisplay md = mData.get(position);
        holder.setTextWatcher(new SwitchTextWatcher(holder.etInput, md));
        holder.etInput.setText(md.getInput());
        holder.etInput.setError(md.getErrMsg());
        holder.tvName.setText(md.getName());
        holder.etInput.setHint(md.getPlaceHolder());
    }

    private void bindViewHolderTxt(ViewHolderTxtY holder, int position) {
        ActDisplay md = mData.get(position);

        int type = 0;
        switch (md.getType()) {
            case ActDisplay.INPUT_NUMBERS:
                type = InputType.TYPE_CLASS_NUMBER;
                break;
            default:
                type = InputType.TYPE_CLASS_TEXT;

        }
        holder.etInput.setInputType(type);

        holder.setTextWatcher(new SwitchTextWatcher(holder.etInput, md));
        holder.etInput.setText(md.getInput());
        holder.etInput.setError(md.getErrMsg());

        holder.tvName.setText(md.getName());
        holder.etInput.setHint(md.getPlaceHolder());

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolderTxtAddrY extends RecyclerView.ViewHolder {
        public TextView tvName;
        public AppCompatEditText etInput;
        public TextWatcher mTextWatcher;

        public void setTextWatcher(TextWatcher watcher) {
            if (mTextWatcher != null) {
                etInput.removeTextChangedListener(mTextWatcher);
            }
            etInput.addTextChangedListener(watcher);
            mTextWatcher = watcher;
        }

        public ViewHolderTxtAddrY(View itemView) {
            super(itemView);
            etInput = (AppCompatEditText) itemView.findViewById(R.id.etInput);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
        }
    }


    public class ViewHolderTxtY extends RecyclerView.ViewHolder {
        public TextView tvName;
        public AppCompatEditText etInput;
        public TextWatcher mTextWatcher;

        public void setTextWatcher(TextWatcher watcher) {
            if (mTextWatcher != null) {
                etInput.removeTextChangedListener(mTextWatcher);
            }
            etInput.addTextChangedListener(watcher);
            mTextWatcher = watcher;
        }

        public ViewHolderTxtY(View itemView) {
            super(itemView);
            etInput = (AppCompatEditText) itemView.findViewById(R.id.etInput);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
        }
    }

    public class ViewHolderUploadPicY extends RecyclerView.ViewHolder {
        public TextView tvName;
        public GridView gvPic;

        public ViewHolderUploadPicY(View itemView) {
            super(itemView);
            gvPic = (GridView) itemView.findViewById(R.id.gvPic);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
        }
    }

    // 这个参数是为了区别那个item是正在传图的
    private int mCurrentUploadPicIndex = -1;

    public void parsePicResult(Bundle bundle, boolean isAdd) {
        if (bundle != null && mCurrentUploadPicIndex >= 0) {
            final ArrayList<ImageInfo> pic_list = new ArrayList<ImageInfo>();
            CameraSdkParameterInfo mCameraSdkParameterInfo = (CameraSdkParameterInfo) bundle.getSerializable(CameraSdkParameterInfo.EXTRA_PARAMETER);
            ArrayList<String> list = mCameraSdkParameterInfo.getImage_list();
            if (list != null) {
                ArrayList<ImageInfo> currentPicList = mData.get(mCurrentUploadPicIndex).getImageList();
                for (int i = 0; i < list.size(); i++) {
                    ImageInfo img = new ImageInfo();
                    if (!isAdd) {
                        for (int j = 0; j < currentPicList.size(); j++) {
                            ImageInfo info = currentPicList.get(j);
                            if (info != null && !info.isAddButton && list.get(i).equals((info.getSource_image()))) {
                                img = info;
                                break;
                            }
                        }
                    }
                    img.setSource_image(list.get(i));
                    pic_list.add(img);
                }
                if (isAdd) {
                    compressPics(pic_list);
                }
            }
            if (isAdd) {
                // 处理图片，子线程处理
            } else {
                mData.get(mCurrentUploadPicIndex).setImageList(pic_list);
            }
        }
    }

    //压缩图片
    private void compressPics(final ArrayList<ImageInfo> pic_list) {
        mActRegister2Activity.showProgressDialog();
        new CompressTask().execute(pic_list);
    }

    private class CompressTask
            extends AsyncTask<ArrayList<ImageInfo>, Integer, ArrayList<ImageInfo>> {
        @Override
        protected ArrayList<ImageInfo> doInBackground(ArrayList<ImageInfo>... params) {
            final ArrayList<ImageInfo> infos = new ArrayList<ImageInfo>();
            List<ImageInfo> pic_list = params[0];
            int size = pic_list.size();
            long ms = System.currentTimeMillis();
            for (int i = 0; i < size; i++) {
                // 生成
                int result = ImageCompressUtils.compress(mActRegister2Activity, pic_list.get(i));
                if (result >= CommunityPictureUtil.RESULT_OK) {
                    //图片添加
                    infos.add(pic_list.get(i));
                } else {
                    publishProgress(result, i);
                }
            }
            return infos;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (mActRegister2Activity.isFinishing()) {
                return;
            }
            super.onProgressUpdate(values);
            String error = "";
            int result = values[0];
            int index = values[1];
            switch (result) {
                case CommunityPictureUtil.RESULT_COMPRESS_EX:
                    error = mActRegister2Activity.getString(R.string.image_compress_error);
                    break;

                case CommunityPictureUtil.RESULT_GIF_TOO_LARGE:
                    error = mActRegister2Activity.getString(R.string.image_gif_too_large);
                    break;

                case CommunityPictureUtil.RESULT_PATH_NULL:
                    error = mActRegister2Activity.getString(R.string.image_path_null);
                    break;

                case CommunityPictureUtil.RESULT_FAIL:
                default:
                    error = mActRegister2Activity.getString(R.string.image_fail);
                    break;
            }
            Toast.makeText(mActRegister2Activity, String.format(mActRegister2Activity.getString(R.string.image_error_at), index + 1) + "," + error, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(ArrayList<ImageInfo> result) {
            if (mActRegister2Activity.isFinishing()) {
                return;
            }
            super.onPostExecute(result);

            mData.get(mCurrentUploadPicIndex).addImageList(result);
            ActRegister2Adapter.super.notifyDataSetChanged();
            mActRegister2Activity.dismissProgressDialog();
        }
    }

    class GridViewAdapter extends BaseAdapter {
        ArrayList<ImageInfo> list;
        Context context;
        private int mItemSize;
        //在上一级列表的位置
        private int mActDisplayIndex = -1;
        private GridView.LayoutParams mItemLayoutParams;

        public GridViewAdapter(Context context, ArrayList<ImageInfo> list, int actDisplayIndex) {
            mActDisplayIndex = actDisplayIndex;
            this.list = new ArrayList<ImageInfo>();
            if (list != null) {
                this.list.addAll(list);
            }
            setAddButton();
            this.context = context;
            mItemLayoutParams = new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT);
        }

        public void addList(ArrayList<ImageInfo> list) {
            if (list == null || list.isEmpty()) {
                return;
            }
            if (this.list.get(this.list.size() - 1).isAddButton) {
                this.list.remove(this.list.size() - 1);
            }

            this.list.addAll(list);

            setAddButton();
            notifyDataSetChanged();
        }

        /**
         * 获取所有图片
         *
         * @return
         */
        public ArrayList<ImageInfo> getList() {
            ArrayList<ImageInfo> list = new ArrayList<ImageInfo>();
            if (this.list != null && this.list.size() > 0) {
                list.addAll(this.list);
                if (list.get(list.size() - 1).isAddButton) {
                    list.remove(list.size() - 1);
                }
            }
            return list;
        }

        public void setList(ArrayList<ImageInfo> list) {
            this.list.clear();
            if (list != null) {
                this.list.addAll(list);
            }
            setAddButton();
            notifyDataSetChanged();
        }

        private void setAddButton() {
            int size = list.size();
            if (size == 0 || !list.get(size - 1).isAddButton) {
                ImageInfo item = new ImageInfo();
                item.setAddButton(true);
                list.add(item);
            }
        }

        /**
         * 重置每个Column的Size
         *
         * @param columnWidth
         */
        public void setItemSize(int columnWidth) {

            if (mItemSize == columnWidth) {
                return;
            }

            mItemSize = columnWidth;

            mItemLayoutParams = new GridView.LayoutParams(mItemSize, mItemSize);

            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if (this.list == null || this.list.size() == 0 || (this.list.size() == 1 && this.list.get(0).isAddButton)) {

                return 1;
            } else if (this.list.size() >= 4) {
                return 4;
            } else {
                return this.list.size();
            }
        }

        public int getActualCount() {
            if (list == null)
                return 0;
            int count = list.size();
            if (count == 0) {
                return 0;
            } else {
                if (list.get(count - 1).isAddButton()) {
                    return count - 1;
                } else {
                    return count;
                }
            }
        }

        @Override
        public ImageInfo getItem(int position) {
            if (list == null || position < 0 || position > list.size() - 1) {
                return null;
            }
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_gridview_addtopic, null);
            // }
            ThumbnailImageView iv = (ThumbnailImageView) convertView.findViewById(R.id.item_gridview_addtopic_iv);

            /** Fixed View Size */
            GridView.LayoutParams lp = (GridView.LayoutParams) convertView.getLayoutParams();
            if (lp == null || lp.height != mItemSize) {
                convertView.setLayoutParams(mItemLayoutParams);
            }

            ImageInfo info = null;
            if (position < list.size()) {
                info = list.get(position);
            }
            if (info == null || info.isAddButton()) {
                iv.getImageView().setImageResource(R.drawable.icon_add_pic);
            } else if (mItemSize > 0) {
                try {
                    File imageFile = new File(info.getSource_image());
                    iv.setImageType(info.getSource_image());
                    GlideApp.with(context).load(imageFile).placeholder(R.drawable.image_default).error(R.drawable.image_default_error).override(mItemSize, mItemSize).centerCrop().into(iv.getImageView());
                } catch (Exception e) {
                    String fileMethodLine = "";
                    fileMethodLine += "File: " + Thread.currentThread().getStackTrace()[2].getFileName();//文件名
                    fileMethodLine += ",Method: " + Thread.currentThread().getStackTrace()[2].getMethodName();//函数名
                    fileMethodLine += ",Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber();//行号
                    GoomeLog.getInstance().logE(fileMethodLine, e.getMessage(), 0);
                }
            }
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position < 0 || position > list.size() - 1) {
                        return;
                    }
                    if (list.get(position) != null && list.get(position).isAddButton) {
                        mActRegister2Activity.toPhotoPick(4 - getActualCount());
                        //表示位置，便于区分
                        mCurrentUploadPicIndex = mActDisplayIndex;
                    } else {
                        Intent intent = new Intent();
                        intent.setClassName(mActRegister2Activity, "com.muzhi.camerasdk.PreviewActivity");
                        Bundle b = new Bundle();
                        CameraSdkParameterInfo info = new CameraSdkParameterInfo();
                        info.setPosition(position);
                        ArrayList<String> image_list = new ArrayList<String>();
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i) != null && !list.get(i).isAddButton) {
                                image_list.add(list.get(i).getSource_image());
                            }
                        }
                        if (image_list.size() > 0) {
                            info.setImage_list(image_list);
                            b.putSerializable(CameraSdkParameterInfo.EXTRA_PARAMETER, info);
                            intent.putExtras(b);
                            mActRegister2Activity.startActivityForResult(intent, CameraSdkParameterInfo.TAKE_PICTURE_PREVIEW);
                            mActRegister2Activity.overridePendingTransition(R.anim.zoom_enter, 0);
                        }
                    }
                }
            });
            return convertView;
        }

        class ViewHolder {
            ImageView iv;
            ImageView remove;
        }
    }
}
