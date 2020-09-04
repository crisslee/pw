package com.muzhi.camerasdk;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.GlideApp;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.util.CommunityPictureUtil;
import com.coomix.app.all.util.GlideUtil;
import com.muzhi.camerasdk.model.CameraSdkParameterInfo;
import com.muzhi.camerasdk.utils.FileType;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher.OnViewTapListener;

/**
 * 图片预览
 *
 * @author zengxiaofeng
 *
 */
public class PreviewActivity extends BaseActivity {

    // private TextView button_delete;
    private int position;
    private View topBar;
    private ImageView tv_left;
    private TextView tv_title;
    private ImageView tv_right;
    private TextView bottomPages;

    private ViewPager mViewPager;
    private CameraSdkParameterInfo mCameraSdkParameterInfo;
    private ArrayList<String> resultList;
    private int iIndex = 0;

    // private String title;
    public static final int TOP_HIDE = 0;
    public static final int TOP_DELETE = 1;
    public static final int TOP_GRID = 2;
    ImagePagerAdapter sAdapter;
    public static final String INTENT_TOP_TYPE = "HintTopType";
    public static final String INTENT_SHOW_ICON = "ShowIcon";
    public static final String INTENT_THUMBNAIL_MAP = "intent_thumbnail_map";
    public static final String INTENT_LIST_IMAGE = "intent_image_list";
    public static final String INTENT_SELECT_POSITION = "intent_ckich_position";
    private int clickNum = 0;
    private int iTopType = 0; //0：默认隐藏，1：删除，2：阵列浏览
    private boolean bShowIcon = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HashMap<String, String> drawables = null;
        if (getIntent() != null) {
            iTopType = getIntent().getIntExtra(INTENT_TOP_TYPE, TOP_HIDE);
            bShowIcon = getIntent().getBooleanExtra(INTENT_SHOW_ICON, false);
            if (iTopType == TOP_HIDE) {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
                requestWindowFeature(Window.FEATURE_NO_TITLE);
            }
            drawables = (HashMap<String, String>) getIntent().getSerializableExtra(INTENT_THUMBNAIL_MAP);
        } else {
            finish();
        }
        //必须放到requestWindowFeature之后
        setContentView(R.layout.camerasdk_activity_preview);

        initViews();

        initData(drawables);
    }

    private void initViews() {
        topBar = findViewById(R.id.topBar);
        tv_left = (ImageView) findViewById(R.id.actionbar_left);
        tv_title = (TextView) findViewById(R.id.actionbar_title);
        tv_right = (ImageView) findViewById(R.id.actionbar_right);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        bottomPages = (TextView) findViewById(R.id.bottom_pages);

        if (iTopType == TOP_HIDE) {
            topBar.setVisibility(View.GONE);
            bottomPages.setVisibility(View.VISIBLE);
        } else if (iTopType == TOP_GRID) {
            topBar.setVisibility(View.VISIBLE);
            bottomPages.setVisibility(View.GONE);
        } else {
            topBar.setVisibility(View.VISIBLE);
            bottomPages.setVisibility(View.GONE);
        }
    }

    private void initData(HashMap<String, String> drawables) {
        Bundle b = getIntent().getExtras();
        if (b != null) {
            mCameraSdkParameterInfo =
                (CameraSdkParameterInfo) b.getSerializable(CameraSdkParameterInfo.EXTRA_PARAMETER);
            if (mCameraSdkParameterInfo == null) {
                finish();
                return;
            }
            resultList = mCameraSdkParameterInfo.getImage_list();
            if (b.containsKey(CameraSdkParameterInfo.EXTRA_INDEX)) {
                iIndex = b.getInt(CameraSdkParameterInfo.EXTRA_INDEX);
            }
            position = mCameraSdkParameterInfo.getPosition();
            if (tv_right != null || resultList.size() > 0) {
                tv_right.setVisibility(View.VISIBLE);
                tv_title.setText((position + 1) + "/" + resultList.size());
                if (resultList.size() <= 1) {
                    bottomPages.setVisibility(View.INVISIBLE);
                } else {
                    bottomPages.setVisibility(View.VISIBLE);
                    bottomPages.setText((position + 1) + "/" + resultList.size());
                }
                if (drawables != null) {
                    sAdapter = new ImagePagerAdapter(this, resultList, drawables);
                } else {
                    sAdapter = new ImagePagerAdapter(this, resultList);
                }
                mViewPager.setAdapter(sAdapter);
                mViewPager.setCurrentItem(position);
                initEvent();
            } else {
                Toast.makeText(this, "你没有选择图片", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void initEvent() {
        if (iTopType == TOP_GRID) {
            tv_right.setImageResource(R.drawable.actionbar_to_section);
            ImageView imageView = (ImageView) findViewById(R.id.imageGrid);
            imageView.setVisibility(View.VISIBLE);
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToGridImageShow();
                }
            });
        }
        tv_left.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tv_right.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (iTopType == TOP_GRID) {
                    goToGridImageShow();
                } else if (iTopType == TOP_DELETE) {
                    delete();
                }
            }
        });
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                position = arg0;
                mViewPager.setCurrentItem(arg0);
                bottomPages.setVisibility(View.VISIBLE);
                tv_title.setText((position + 1) + "/" + resultList.size());
                bottomPages.setText((position + 1) + "/" + resultList.size());
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                if (iTopType == TOP_HIDE && bottomPages != null && bottomPages.getVisibility() == View.GONE) {
                    bottomPages.setVisibility(View.VISIBLE);
                } else if (iTopType == TOP_DELETE && topBar != null && topBar.getVisibility() == View.GONE) {
                    topBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                if (iTopType == TOP_HIDE && bottomPages != null && bottomPages.getVisibility() == View.GONE) {
                    bottomPages.setVisibility(View.VISIBLE);
                } else if (iTopType == TOP_DELETE && topBar != null && topBar.getVisibility() == View.GONE) {
                    topBar.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 20 && resultCode == RESULT_OK && data != null) {
            position = data.getIntExtra(INTENT_SELECT_POSITION, position);
            tv_title.setText((position + 1) + "/" + resultList.size());
            mViewPager.setCurrentItem(position);
        }
    }

    private void goToGridImageShow() {
        Intent intent = new Intent(this, GridPreviewActivity.class);
        intent.putExtra(PreviewActivity.INTENT_LIST_IMAGE, resultList);
        intent.putExtra(PreviewActivity.INTENT_THUMBNAIL_MAP, sAdapter.getThumbnailList());
        startActivityForResult(intent, 20);
    }

    /**
     * 会删除文件
     */
    public void delete() {
        if (position >= 0 && position < resultList.size()) {
            String str = resultList.remove(position);
            // 删除文件
            File file = new File(str);
            file.deleteOnExit();
            if (position >= resultList.size()) {
                position = resultList.size() - 1;
            }
            bottomPages.setVisibility(View.VISIBLE);
            if (resultList == null || resultList.isEmpty()) {
                tv_title.setText("0/0");
                bottomPages.setText("0/0");
            } else {
                tv_title.setText((position + 1) + "/" + resultList.size());
                bottomPages.setText((position + 1) + "/" + resultList.size());
            }
            sAdapter.notifyDataSetChanged();
        }

        if (resultList == null || resultList.size() <= 0) {
            finish();
        }
    }

    /****************** Adapter********************/
    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        //ViewTarget.useOriginalSize(true);
        //EngineKey.setCacheKeySimple(false);
        super.onStart();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        //ViewTarget.useOriginalSize(false);
        //EngineKey.setCacheKeySimple(true);
        super.onStop();
    }

    class ImagePagerAdapter extends PagerAdapter {
        private Context mContext;
        private List<String> imageList;
        private HashMap<String, String> thumbnailList;
        private Queue<View> mReusableViews;

        public ImagePagerAdapter(Context context, List<String> images, HashMap<String, String>... thumbnails) {
            mContext = context;
            imageList = images;
            thumbnailList = thumbnails != null && thumbnails.length > 0 ? thumbnails[0] : null;
            LayoutInflater lf = LayoutInflater.from(mContext);
            mReusableViews = new ArrayDeque<>(imageList.size());
        }

        public HashMap<String, String> getThumbnailList() {
            return thumbnailList;
        }

        @Override
        public int getCount() {
            return imageList != null ? imageList.size() : 0;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            try {
                if (object instanceof View) {
                    container.removeView((View) object);
                    mReusableViews.add((View) object);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            View v = mReusableViews.poll();
            if (v == null) {
                v = LayoutInflater.from(PreviewActivity.this)
                    .inflate(R.layout.camerasdk_list_item_preview_image, container, false);
            }
            final View view = v;
            try {
                if (view.getParent() == null) {
                    container.addView(view);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            final PhotoView photoView = (PhotoView) view.findViewById(R.id.image);
            final ProgressBar pb_load_local = (ProgressBar) view.findViewById(R.id.pb_load_local);
            photoView.setOnViewTapListener(new OnViewTapListener() {
                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                @Override
                public void onViewTap(View arg0, float arg1, float arg2) {
                    if ((iTopType == TOP_HIDE && bottomPages != null) || iTopType == TOP_GRID) {
                        finish();
                    } else if (iTopType == TOP_DELETE && topBar != null) {
                        if (topBar.getVisibility() == View.VISIBLE) {
                            topBar.setVisibility(View.GONE);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                view.setSystemUiVisibility(View.INVISIBLE);
                            }
                        } else {
                            topBar.setVisibility(View.VISIBLE);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                                view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                            }
                        }
                    }
                }
            });

            final String path = imageList.get(position);
            if (!TextUtils.isEmpty(path)) {
                photoView.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        try {
                            CommunityPictureUtil.showMorePopWindow(PreviewActivity.this, mViewPager, photoView);
                        } catch (Exception e) {
                            if (Constant.IS_DEBUG_MODE) {
                                e.printStackTrace();
                            }
                        }
                        return false;
                    }
                });
                if (path.contains("http://") || path.contains("https://")) {
                    //网络图片需要加载等待提示
                    pb_load_local.setVisibility(View.VISIBLE);
                    GlideApp.with(mContext).load(path).thumbnail(0.1f)
                        .placeholder(getThumbnailDrawable(thumbnailList, path, R.drawable.image_default))
                        .error(R.drawable.image_default_error)
                        .dontAnimate()
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                Target<Drawable> target, boolean isFirstResource) {
                                pb_load_local.setVisibility(View.GONE);
                                if (iTopType == TOP_GRID && photoView != null) {
                                    Drawable drawable = getThumbnailDrawable(thumbnailList, path);
                                    if (drawable != null) {
                                        photoView.setImageDrawable(drawable);
                                    } else {
                                        photoView.setImageResource(R.drawable.image_default_error);
                                    }
                                    return true;
                                }
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                DataSource dataSource, boolean isFirstResource) {
                                pb_load_local.setVisibility(View.GONE);
                                return false;
                            }
                        }).error(R.drawable.image_default_error).into(photoView);
                } else {
                    //本地图片
                    if (FileType.getFileType(path).equals(FileType.GIF)) {
                        GlideApp.with(mContext).load(path).thumbnail(0.1f).placeholder(R.drawable.image_default)
                            .error(R.drawable.image_default_error).into(photoView);
                        photoView.setOnLongClickListener(new OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                try {
                                    CommunityPictureUtil.showMorePopWindow(PreviewActivity.this, mViewPager, photoView);
                                } catch (Exception e) {
                                    if (Constant.IS_DEBUG_MODE) {
                                        e.printStackTrace();
                                    }
                                }
                                return false;
                            }
                        });
                    } else {
                        int[] array = CommunityPictureUtil.getBitmapResizeWidthHeight(path);
                        GlideApp.with(mContext).load(path).override(array[0], array[1]).thumbnail(0.1f)
                            .placeholder(R.drawable.image_default).error(R.drawable.image_default_error)
                            .into(photoView);
                    }
                }
            } else {
                if (bShowIcon) {
                    photoView.setImageResource(R.drawable.login_icon_large);
                    if (AllOnlineApp.getAppConfig().getSave_default_head_image_onoff() != 0) {
                        photoView.setOnLongClickListener(new OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                try {
                                    CommunityPictureUtil.showMorePopWindow(PreviewActivity.this, mViewPager, photoView);
                                } catch (Exception e) {
                                    if (Constant.IS_DEBUG_MODE) {
                                        e.printStackTrace();
                                    }
                                }
                                return false;
                            }
                        });
                    }
                } else {
                    photoView.setImageResource(R.drawable.image_default_error);
                    Toast.makeText(mContext, "图片链接出错了！", Toast.LENGTH_LONG).show();
                }
            }

            return view;
        }

        private Drawable getThumbnailDrawable(HashMap<String, String> thumbnailList, String path, int drawableId) {
            Drawable thumbnail = null;
            if (thumbnailList != null) {
                try {
                    String thumbnailPath = thumbnailList.get(path);
                    if (thumbnailPath != null) {
                        // 本地缩略图
                        File file = new File(thumbnailPath);
                        if (file != null && file.exists()) {
                            thumbnail = BitmapDrawable.createFromPath(thumbnailPath);
                        } else {
                            //表示是从Glide缓存的
                            thumbnail = BitmapDrawable.createFromPath(GlideUtil.getGlideCachePath(thumbnailPath));
                        }
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Throwable e) {
                    e.printStackTrace();
                    System.gc();
                }
            }
            if (thumbnail == null) {
                thumbnail = getResources().getDrawable(drawableId);
            }
            return thumbnail;
        }

        private Drawable getThumbnailDrawable(HashMap<String, String> thumbnailList, String path) {
            Drawable thumbnail = null;
            if (thumbnailList != null && !TextUtils.isEmpty(path)) {
                try {
                    String thumbnailPath = thumbnailList.get(path);
                    if (thumbnailPath != null) {
                        // 本地缩略图
                        File file = new File(thumbnailPath);
                        if (file != null && file.exists()) {
                            thumbnail = BitmapDrawable.createFromPath(thumbnailPath);
                        } else {
                            //表示是从Glide缓存的
                            thumbnail = BitmapDrawable.createFromPath(GlideUtil.getGlideCachePath(thumbnailPath));
                        }
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Throwable e) {
                    e.printStackTrace();
                    System.gc();
                }
            }
            return thumbnail;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
            // return super.getItemPosition(object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    @Override
    public void finish() {
        if (iTopType == TOP_DELETE) {
            mCameraSdkParameterInfo.setImage_list(resultList);
            Intent intent = new Intent();
            intent.putExtra(CameraSdkParameterInfo.EXTRA_PARAMETER, mCameraSdkParameterInfo);
            intent.putExtra(CameraSdkParameterInfo.EXTRA_INDEX, iIndex);
            setResult(RESULT_OK, intent);
        }
        super.finish();
        overridePendingTransition(0, R.anim.zoom_exit);
    }

    @Override
    protected void onDestroy() {
        // GlideApp.with(this).pauseRequests();
        super.onDestroy();
    }

}
