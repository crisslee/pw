package com.muzhi.camerasdk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;

import com.bumptech.GlideApp;
import com.muzhi.camerasdk.model.ImageInfo;
import com.muzhi.camerasdk.view.ThumbnailImageView;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片选择Adapter
 */
public class ImageGridAdapter extends BaseAdapter
{

	private static final int TYPE_CAMERA = 0;
	private static final int TYPE_NORMAL = 1;

	private Context mContext;

	private LayoutInflater mInflater;
	private boolean showCamera = true;
	private boolean mode_single = false; // 是否单选 默认为多选

	private List<ImageInfo> mImages = new ArrayList<ImageInfo>();
	private List<ImageInfo> mSelectedImages = new ArrayList<ImageInfo>();

	private int mItemSize;
	private GridView.LayoutParams mItemLayoutParams;

	public ImageGridAdapter(Context context, boolean showCamera, boolean mode_single)
	{
		mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.showCamera = showCamera;
		this.mode_single = mode_single;
		mItemLayoutParams =
				new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT);
	}

	public void setShowCamera(boolean b)
	{
		if (showCamera == b)
			return;

		showCamera = b;
		notifyDataSetChanged();
	}

	public boolean isShowCamera()
	{
		return showCamera;
	}

	/**
	 * 选择某个图片，改变选择状态
	 * 
	 * @param imageInfo
	 */
	public void select(ImageInfo imageInfo, View view)
	{
		if (mSelectedImages.contains(imageInfo))
		{
			mSelectedImages.remove(imageInfo);
		}
		else
		{
			mSelectedImages.add(imageInfo);
		}

		int index = mImages.indexOf(imageInfo);
		if (index >= 0 && index < mImages.size())
		{
			if (view != null)
			{
				CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkmark);
				View mask = view.findViewById(R.id.mask);
				if (checkBox == null || view == null)
				{
					notifyDataSetChanged();
					return;
				}
				if (!mode_single)
				{
					// 多选状态
					checkBox.setVisibility(View.VISIBLE);
					if (mSelectedImages.contains(imageInfo))
					{
						// 设置选中状态
						checkBox.setChecked(true);
						mask.setVisibility(View.VISIBLE);
					}
					else
					{
						// 未选择
						checkBox.setChecked(false);
						mask.setVisibility(View.GONE);
					}
				}
				else
				{
					checkBox.setVisibility(View.GONE);
				}
			}
		}
		else
		{
			notifyDataSetChanged();
		}
	}
	/*
	 * public void removeOne(ImageInfo imageInfo){
	 * if(mSelectedImages.contains(imageInfo)){
	 * mSelectedImages.remove(imageInfo); } notifyDataSetChanged(); }
	 */

	public void removeOne(String path)
	{
		ImageInfo imageInfo = getImageByPath(path);
		if (mSelectedImages.contains(imageInfo))
		{
			mSelectedImages.remove(imageInfo);
		}
		notifyDataSetChanged();
	}

	/**
	 * 通过图片路径设置默认选择
	 * 
	 * @param resultList
	 */
	public void setSelectedList(ArrayList<String> resultList)
	{
		for (String path : resultList)
		{
			ImageInfo imageInfo = getImageByPath(path);
			if (imageInfo != null)
			{
				mSelectedImages.add(imageInfo);
			}
		}
		if (mSelectedImages.size() > 0)
		{
			notifyDataSetChanged();
		}
	}

	/**
	 * 通过图片路径设置默认选择,先清空原选择的图片
	 * 
	 * @param resultList
	 */
	public void reSetSelectedList(ArrayList<String> resultList)
	{
		mSelectedImages.clear();
		if (resultList != null)
		{
			for (String path : resultList)
			{
				ImageInfo imageInfo = getImageByPath(path);
				if (imageInfo != null)
				{
					mSelectedImages.add(imageInfo);
				}
			}
		}
		notifyDataSetChanged();
	}

	private ImageInfo getImageByPath(String path)
	{
		if (mImages != null && mImages.size() > 0)
		{
			for (ImageInfo imageInfo : mImages)
			{
				if (imageInfo.path.equalsIgnoreCase(path))
				{
					return imageInfo;
				}
			}
		}
		return null;
	}

	/**
	 * 设置数据集
	 * 
	 * @param imageInfos
	 */
	public void setData(List<ImageInfo> imageInfos)
	{
		mSelectedImages.clear();

		if (imageInfos != null && imageInfos.size() > 0)
		{
			mImages = imageInfos;
		}
		else
		{
			mImages.clear();
		}
		notifyDataSetChanged();
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
	public int getViewTypeCount()
	{
		return 2;
	}

	@Override
	public int getItemViewType(int position)
	{
		if (showCamera)
		{
			return position == 0 ? TYPE_CAMERA : TYPE_NORMAL;
		}
		return TYPE_NORMAL;
	}

	@Override
	public int getCount()
	{
		return showCamera ? mImages.size() + 1 : mImages.size();
	}

	@Override
	public ImageInfo getItem(int i)
	{
		if (showCamera)
		{
			if (i == 0)
			{
				return null;
			}
			return mImages.get(i - 1);
		}
		else
		{
			return mImages.get(i);
		}
	}

	@Override
	public long getItemId(int i)
	{
		return i;
	}

	@Override
	public View getView(int position, View view, ViewGroup viewGroup)
	{
		int type = getItemViewType(position);
		if (type == TYPE_CAMERA)
		{
			view = mInflater.inflate(R.layout.camerasdk_list_item_camera, viewGroup, false);
		}
		else if (type == TYPE_NORMAL)
		{
			ViewHolde holde;
			if (view == null)
			{
				view = mInflater.inflate(R.layout.camerasdk_list_item_image, viewGroup, false);
				holde = new ViewHolde(view);
				view.setTag(R.layout.camerasdk_list_item_image, holde);
			}
			else
			{
				holde = (ViewHolde) view.getTag(R.layout.camerasdk_list_item_image);
				if (holde == null)
				{
					view = mInflater.inflate(R.layout.camerasdk_list_item_image, viewGroup, false);
					holde = new ViewHolde(view);
					view.setTag(R.layout.camerasdk_list_item_image, holde);
				}
			}
			if (holde != null)
			{
				holde.bindData(getItem(position), position);
			}
		}

		/** Fixed View Size */
		GridView.LayoutParams lp = (GridView.LayoutParams) view.getLayoutParams();
		if (lp.height != mItemSize)
		{
			view.setLayoutParams(mItemLayoutParams);
		}

		return view;
	}

	/**
	 * 给CheckBox加点击动画，利用开源库nineoldandroids设置动画
	 * 
	 * @param view
	 */
	private void addAnimation(View view)
	{
		float[] vaules =
				new float[] { 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f, 1.1f, 1.2f, 1.3f, 1.25f, 1.2f, 1.15f, 1.1f, 1.0f };
		AnimatorSet set = new AnimatorSet();
		set.playTogether(ObjectAnimator.ofFloat(view, "scaleX", vaules),
				ObjectAnimator.ofFloat(view, "scaleY", vaules));
		set.setDuration(150);
		set.start();
	}

	class ViewHolde
	{
		ThumbnailImageView thImageView;
		CheckBox checkBox;
		View mask;

		ViewHolde(View view)
		{
			thImageView = (ThumbnailImageView) view.findViewById(R.id.thumbImageview);
			checkBox = (CheckBox) view.findViewById(R.id.checkmark);
			mask = view.findViewById(R.id.mask);
		}

		void bindData(final ImageInfo data, int position)
		{
			if (data == null)
				return;
			if (!mode_single)
			{
				// 多选状态
				checkBox.setVisibility(View.VISIBLE);
				if (mSelectedImages.contains(data))
				{
					// 设置选中状态
					// indicator.setImageResource(R.drawable.camerasdk_checkbox_checked);
					checkBox.setChecked(true);
					mask.setVisibility(View.VISIBLE);
				}
				else
				{
					// 未选择
					// indicator.setImageResource(R.drawable.camerasdk_checkbox_normal);
					checkBox.setChecked(false);
					mask.setVisibility(View.GONE);
				}
			}
			else
			{
				checkBox.setVisibility(View.GONE);
			}
			File imageFile = new File(data.path);
			if (mItemSize > 0)
			{
				// 显示图片
				thImageView.setImageType(data.path);

				GlideApp.with(mContext).load(imageFile).placeholder(R.drawable.image_default)
						.error(R.drawable.image_default_error).skipMemoryCache(true).override(mItemSize, mItemSize)
						.centerCrop().into(thImageView.getImageView());
			}
		}
	}
}
