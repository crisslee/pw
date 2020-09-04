package com.muzhi.camerasdk;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import com.bumptech.GlideApp;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.widget.MyActionbar;
import com.muzhi.camerasdk.view.ThumbnailImageView;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 图片预览
 *
 * @author zengxiaofeng
 */
public class GridPreviewActivity extends BaseActivity
{
    private ArrayList<String> imageList = null;
    private HashMap<String, String> thumbnailMaps = null;
    private GridView gridViewImages = null;
    private int mItemSize ;
    private GridView.LayoutParams mItemLayoutParams;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.camerasdk_activity_gridpreview);

        if (getIntent() != null)
        {
            imageList = getIntent().getStringArrayListExtra(PreviewActivity.INTENT_LIST_IMAGE);
            thumbnailMaps = (HashMap<String, String>) getIntent().getSerializableExtra(PreviewActivity.INTENT_THUMBNAIL_MAP);
        }

        if (imageList == null || thumbnailMaps == null || imageList.size() <= 0 || thumbnailMaps.size() <= 0)
        {
            finish();
            return;
        }

        initViews();

        initData();
    }

    private void initViews()
    {
        MyActionbar actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, R.string.chat_images, 0, 0);

        gridViewImages = (GridView) findViewById(R.id.gridViewImages);
        initData();
    }

    private void initData()
    {
        final int columnSpace = getResources().getDimensionPixelOffset(R.dimen.space_size);
        mItemSize = (AllOnlineApp.screenWidth - columnSpace * 6) / 4;
        mItemLayoutParams = new GridView.LayoutParams(mItemSize, mItemSize);
        gridViewImages.setAdapter(new ImageAdapter());
        gridViewImages.setOnItemClickListener(itemClickListener);
    }

    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            Intent intent = new Intent();
            intent.putExtra(PreviewActivity.INTENT_SELECT_POSITION, position);
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    private class ImageAdapter extends BaseAdapter
    {
        @Override
        public int getCount()
        {
            return imageList == null ? 0 : imageList.size();
        }

        @Override
        public Object getItem(int position)
        {
            String path = null;
            if (imageList != null && position >= 0 && position < imageList.size())
            {
                path = imageList.get(position);
            }
            if (thumbnailMaps != null && position >= 0 && position < thumbnailMaps.size())
            {
                if (!TextUtils.isEmpty(thumbnailMaps.get(path)))
                {
                    path = thumbnailMaps.get(path);
                }
            }
            return path;
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ThumbnailImageView iv = null;
            if (convertView == null)
            {
                convertView = LayoutInflater.from(GridPreviewActivity.this).inflate(R.layout.item_gridview_addtopic, null);
                iv = (ThumbnailImageView) convertView.findViewById(R.id.item_gridview_addtopic_iv);
                convertView.setTag(R.layout.item_gridview_addtopic, iv);
            }
            else
            {
                iv = (ThumbnailImageView) convertView.getTag(R.layout.item_gridview_addtopic);
            }

            String path = (String) getItem(position);

            GlideApp.with(GridPreviewActivity.this).load(path).placeholder(R.drawable.image_default).error(R.drawable.image_default_error)
                    .override(mItemSize, mItemSize).centerCrop().into(iv.getImageView());

            /** Fixed View Size */
            GridView.LayoutParams lp = (GridView.LayoutParams) convertView.getLayoutParams();
            if ((lp == null) || (lp != null && lp.height != mItemSize))
            {
                if(mItemLayoutParams == null)
                {
                    mItemLayoutParams = new GridView.LayoutParams(mItemSize, mItemSize);
                }
                convertView.setLayoutParams(mItemLayoutParams);
            }

            return convertView;
        }
    }
}
