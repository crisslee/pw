package com.coomix.app.all.ui.activity;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.GlideApp;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.ImageInfo;
import com.muzhi.camerasdk.view.ThumbnailImageView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by ssl on 2017/3/15.
 */
public class ImageAddActAdapter extends BaseAdapter
{
    public static final int MAX_IMAGE = 4;
    private ArrayList<ImageInfo> list;
    private Activity context;
    private int mItemSize;
    private GridView.LayoutParams mItemLayoutParams;
    private int iAdapterIndex = 0;
    private Handler mHandler = new Handler();

    public ImageAddActAdapter(Activity context, ArrayList<ImageInfo> list)
    {
        this.list = new ArrayList<ImageInfo>();
        if (list != null)
        {
            this.list.addAll(list);
        }
        setAddButton();
        this.context = context;
        mItemLayoutParams = new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT);
    }

    public void addList(ArrayList<ImageInfo> list)
    {
        if (list == null || list.isEmpty())
        {
            return;
        }

        removeBlankImage(this.list);

        this.list.addAll(list);

        setAddButton();

        notifyDataSetChanged();
    }

    public ArrayList<ImageInfo> getAllListData()
    {
        return list;
    }

    @Override
    public void notifyDataSetChanged()
    {
        super.notifyDataSetChanged();
    }

    private void removeBlankImage(ArrayList<ImageInfo> list)
    {
        for(int i =0; i < list.size(); )
        {
            if(isBlankImageInfo(list.get(i)))
            {
                list.remove(i);
            }
            else
            {
                i ++;
            }
        }
    }

    /**
     * 获取所有图片
     *
     * @return
     */
    public ArrayList<ImageInfo> getImageList()
    {
        ArrayList<ImageInfo> list = new ArrayList<ImageInfo>();
        if (this.list != null && this.list.size() > 0)
        {
            list.addAll(this.list);

            removeBlankImage(list);
        }
        return list;
    }

    public boolean isBlankImageInfo(ImageInfo imageInfo)
    {
        if(imageInfo != null)
        {
            if(imageInfo.isAddButton)
            {
                return true;
            }
            if(!TextUtils.isEmpty(imageInfo.getSource_image()) || !TextUtils.isEmpty(imageInfo.getImg_path()) || !TextUtils.isEmpty(imageInfo.getDomain()))
            {
                return false;
            }
        }
        return true;
    }




    public void setList(ArrayList<ImageInfo> list)
    {
        this.list.clear();
        if (list != null)
        {
            this.list.addAll(list);
        }
        setAddButton();
        notifyDataSetChanged();
    }

    private void setAddButton()
    {
        while (list.size() < MAX_IMAGE)
        {
            ImageInfo item = new ImageInfo();
            if(list.size() == MAX_IMAGE - 1)
            {
                item.setAddButton(true);
            }
            list.add(item);
        }
    }

    /**
     * 重置每个Column的Size
     *
     * @param columnWidth
     */
    public void setItemSize(int columnWidth)
    {
        if (mItemSize == columnWidth)
        {
            return;
        }

        mItemSize = columnWidth;

        mItemLayoutParams = new GridView.LayoutParams(mItemSize, mItemSize);

        notifyDataSetChanged();
    }

    @Override
    public int getCount()
    {
        return MAX_IMAGE;
    }

    @Override
    public ImageInfo getItem(int position)
    {
        if (list == null || position < 0 || position > list.size() - 1)
        {
            return null;
        }
        return list.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        ViewHolder viewHolder= null;
        if (convertView == null)
        {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_gridview_addtopic, null);
            viewHolder.thumbnailImageView = (ThumbnailImageView) convertView.findViewById(R.id.item_gridview_addtopic_iv);
            viewHolder.deleteImage = (ImageView)convertView.findViewById(R.id.item_gridview_addtopic_clear);
            convertView.setTag(R.layout.item_gridview_addtopic, viewHolder);
            viewHolder.deleteImage.setVisibility(View.GONE);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag(R.layout.item_gridview_addtopic);
        }

        /** Fixed View Size */
        GridView.LayoutParams lp = (GridView.LayoutParams) convertView.getLayoutParams();
        if (lp == null || lp.height != mItemSize)
        {
            convertView.setLayoutParams(mItemLayoutParams);
        }

        ImageInfo info = null;
        if (position < list.size())
        {
            info = list.get(position);
        }
        if (info == null)
        {
            return convertView;
        }

        final ImageView imageView = viewHolder.thumbnailImageView.getImageView();
        if (info.isAddButton())
        {
            imageView.setImageResource(R.drawable.act_add_icon);
        }
        else if(isBlankImageInfo(info))
        {
            imageView.setImageResource(R.color.transparent);
        }
        else if (mItemSize > 0)
        {
            if(!TextUtils.isEmpty(info.getSource_image()))
            {
                try
                {
                    File imageFile = new File(info.getSource_image());
                    viewHolder.thumbnailImageView.setImageType(info.getSource_image());
                    //Glide.with(context).load(imageFile).asBitmap().placeholder(R.drawable.image_default).error(R.drawable.image_default_error).override(mItemSize, mItemSize).centerCrop().into(imageView);
                    GlideApp.with(context).load(imageFile).placeholder(R.drawable.image_default).error(R.drawable.image_default_error).override(mItemSize, mItemSize).centerCrop()
                            .into(new SimpleTarget<Drawable>(mItemSize, mItemSize)
                    {
                        @Override
                        public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                            imageView.setImageDrawable(resource);
                            //存在刷新不到的问题，所以此处再刷新一次
                            notifyDataSetChanged();
                        }

                    });
                }
                catch (Exception e)
                {
                }
            }
            else if(!TextUtils.isEmpty(info.getDomain()) || !TextUtils.isEmpty(info.getImg_path()))
            {
                String url = (info.getDomain() == null ? "" : info.getDomain()) + (info.getImg_path() == null ? "" : info.getImg_path());
                viewHolder.thumbnailImageView.setImageType(url);
                GlideApp.with(context).load(url).placeholder(R.drawable.image_default).error(R.drawable.image_default_error).override(mItemSize, mItemSize).centerCrop().into(imageView);

            }
        }

        return convertView;
    }

    private class ViewHolder
    {
        ThumbnailImageView thumbnailImageView;
        ImageView deleteImage;
    }

    public void setAdapterIndex(int index)
    {
        this.iAdapterIndex = index;
    }

    public int getAdapterIndex()
    {
        return iAdapterIndex;
    }
}
