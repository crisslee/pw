package com.coomix.app.all.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.dialog.ProgressDialogEx;
import com.coomix.app.all.model.bean.ImageInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ssl on 2017/1/3.
 */
public class CompressImageTask extends AsyncTask<ArrayList<ImageInfo>, Integer, ArrayList<ImageInfo>> {
    private Activity mActivity;
    private OnCompressListener listener;
    private ProgressDialogEx mProgressdialog;

    public CompressImageTask(Activity activity) {
        this(activity, null);
    }

    public CompressImageTask(Activity activity, OnCompressListener listener) {
        this.mActivity = activity;
        this.listener = listener;
        showCompressDialog();
    }

    @Override
    protected ArrayList<ImageInfo> doInBackground(ArrayList<ImageInfo>... params) {
        final ArrayList<ImageInfo> infos = new ArrayList<ImageInfo>();
        List<ImageInfo> pic_list = params[0];
        int size = pic_list.size();
        long ms = System.currentTimeMillis();
        for (int i = 0; i < size; i++) {
            // 生成
            int result = ImageCompressUtils.compress(mActivity, pic_list.get(i));
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
        if (mActivity.isFinishing()) {
            return;
        }
        super.onProgressUpdate(values);
        String error = "";
        int result = values[0];
        int index = values[1];
        switch (result) {
            case CommunityPictureUtil.RESULT_COMPRESS_EX:
                error = mActivity.getString(R.string.image_compress_error);
                break;

            case CommunityPictureUtil.RESULT_GIF_TOO_LARGE:
                error = mActivity.getString(R.string.image_gif_too_large);
                break;

            case CommunityPictureUtil.RESULT_PATH_NULL:
                error = mActivity.getString(R.string.image_path_null);
                break;

            case CommunityPictureUtil.RESULT_FAIL:
            default:
                error = mActivity.getString(R.string.image_fail);
                break;
        }
        Toast.makeText(mActivity, String.format(mActivity.getString(R.string.image_error_at), index + 1) + "," + error,
            Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPostExecute(ArrayList<ImageInfo> result) {
        if (mActivity.isFinishing()) {
            return;
        }
        super.onPostExecute(result);
        if (mProgressdialog != null) {
            mProgressdialog.dismiss();
        }
        if (listener != null) {
            listener.onCompressed(result);
        }
    }

    private void showCompressDialog() {
        mProgressdialog = ProgressDialogEx.show(mActivity, "", mActivity.getString(R.string.image_compressing), true,
            Constant.DIALOG_AUTO_DISMISS, new ProgressDialogEx.OnCancelListener2() {
                @Override
                public void onCancel(DialogInterface dialog) {
                }

                @Override
                public void onAutoCancel(DialogInterface dialog) {
                }
            });
    }

    public interface OnCompressListener {
        public void onCompressed(ArrayList<ImageInfo> result);
    }

    public void setOnCompressListener(OnCompressListener listener) {
        this.listener = listener;
    }
}
