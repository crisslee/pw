package com.muzhi.camerasdk;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;
import com.coomix.app.all.dialog.ProgressDialogEx;
import com.coomix.app.all.util.ImageCompressUtils;
import com.coomix.app.all.widget.MyActionbar;
import com.goomeim.utils.GMUserUtil;
import com.muzhi.camerasdk.adapter.FolderAdapter;
import com.muzhi.camerasdk.model.CameraSdkParameterInfo;
import com.muzhi.camerasdk.model.FolderInfo;
import com.muzhi.camerasdk.model.ImageInfo;
import com.muzhi.camerasdk.utils.FileUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PhotoPickActivity extends FragmentActivity {

    public static PhotoPickActivity instance = null;

    private CameraSdkParameterInfo mCameraSdkParameterInfo = new CameraSdkParameterInfo();

    private ArrayList<String> resultList = new ArrayList<String>();// 结果数据
    private ArrayList<FolderInfo> mResultFolder = new ArrayList<FolderInfo>();// 文件夹数据
    // private HashMap<String, ImageView> hashMap = new HashMap<String,
    // ImageView>();// 预览图片集

    // 不同loader定义
    private static final int LOADER_ALL = 0;
    private static final int LOADER_CATEGORY = 1;

    private TextView mCategoryText /* mTimeLineText, */;
    private GridView mGridView;
    private PopupWindow mpopupWindow;
    private RelativeLayout camera_footer;
    private ImageGridAdapter mImageAdapter;
    private FolderAdapter mFolderAdapter;
    private boolean hasFolderGened = false;
    private File mTmpFile;
    private int iIndex = 0;
    //从IM过来选择图片的先压缩再返回数据
    public static final String FROM_IM = "from_im";
    private MyActionbar actionbar;

    // private HorizontalScrollView scrollview;
    // private LinearLayout selectedImageLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camerasdk_activity_main);

        instance = this;

        initExtra();
        initViews();
        initEvent();
        getSupportLoaderManager().restartLoader(LOADER_ALL, null, mLoaderCallback);
    }

    // 获取传过来的参数
    private void initExtra() {

        Intent intent = getIntent();
        try {
            mCameraSdkParameterInfo =
                (CameraSdkParameterInfo) intent.getSerializableExtra(CameraSdkParameterInfo.EXTRA_PARAMETER);
            resultList = mCameraSdkParameterInfo.getImage_list();
            if (intent.hasExtra(CameraSdkParameterInfo.EXTRA_INDEX)) {
                iIndex = intent.getIntExtra(CameraSdkParameterInfo.EXTRA_INDEX, 0);
            }
            if (mCameraSdkParameterInfo == null) {
                mCameraSdkParameterInfo = new CameraSdkParameterInfo();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initViews() {
        findViewById(R.id.button_preview).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 预览
                if (mCameraSdkParameterInfo != null && mCameraSdkParameterInfo.getImage_list() != null
                    && mCameraSdkParameterInfo.getImage_list().size() > 0) {
                    Intent intent = new Intent();
                    intent.setClass(PhotoPickActivity.this, PreviewActivity.class);
                    Bundle b = new Bundle();
                    b.putSerializable(CameraSdkParameterInfo.EXTRA_PARAMETER, mCameraSdkParameterInfo);
                    intent.putExtras(b);
                    intent.putExtra(PreviewActivity.INTENT_TOP_TYPE, PreviewActivity.TOP_DELETE);
                    startActivityForResult(intent, CameraSdkParameterInfo.TAKE_PICTURE_PREVIEW);
                    overridePendingTransition(R.anim.zoom_enter, 0);
                } else {
                    Toast.makeText(PhotoPickActivity.this, "您没有选择图片", Toast.LENGTH_SHORT).show();
                }
            }
        });
        actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, "图片", 0, 0);
        actionbar.setRightTextClickListener(view -> {
            if (resultList != null && resultList.size() > 0) {
                selectComplate();
            } else {
                Toast.makeText(PhotoPickActivity.this, "你没有选择图片", Toast.LENGTH_SHORT).show();
            }
        });

        mCategoryText = (TextView) findViewById(R.id.camerasdk_actionbar_title);
        Drawable drawable = getResources().getDrawable(R.drawable.message_popover_arrow);
        drawable.setBounds(10, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        mCategoryText.setCompoundDrawables(null, null, drawable, null);

        // mTimeLineText = (TextView)findViewById(R.id.timeline_area);

        mGridView = (GridView) findViewById(R.id.gv_list);
        camera_footer = (RelativeLayout) findViewById(R.id.camera_footer);
        // selectedImageLayout = (LinearLayout)
        // findViewById(R.id.selected_image_layout);
        // scrollview = (HorizontalScrollView) findViewById(R.id.scrollview);

        if (mCameraSdkParameterInfo != null) {
            actionbar.setRightText("完成(0/" + mCameraSdkParameterInfo.getMax_image() + ")");
            mImageAdapter = new ImageGridAdapter(PhotoPickActivity.this, mCameraSdkParameterInfo.isShow_camera(),
                mCameraSdkParameterInfo.isSingle_mode());
            mGridView.setAdapter(mImageAdapter);
            mFolderAdapter = new FolderAdapter(PhotoPickActivity.this);
            if (mCameraSdkParameterInfo.isSingle_mode()) {
                camera_footer.setVisibility(View.GONE);
            }
        } else {
            actionbar.setRightText("完成(0/0)");
            mImageAdapter = new ImageGridAdapter(PhotoPickActivity.this, true, false);
            mGridView.setAdapter(mImageAdapter);
            mFolderAdapter = new FolderAdapter(PhotoPickActivity.this);
            camera_footer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        mFolderAdapter = null;
        super.onDestroy();
    }

    // 设置预览图
    private void initSelectImage() {
        if (resultList == null) {
            return;
        }
        for (String path : resultList) {
            addImagePreview(path);
        }
    }

    private void initEvent() {
        mCategoryText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupFolder(view);
            }
        });
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int state) {

                // if (state == SCROLL_STATE_IDLE
                // || state == SCROLL_STATE_TOUCH_SCROLL) {
                // Glide.with(PhotoPickActivity.this).re.resumeTag(PhotoPickActivity.this);
                // } else {
                // picasso.pauseTag(PhotoPickActivity.this);
                // }

                // if(state == SCROLL_STATE_IDLE){
                // // 停止滑动，日期指示器消失
                // mTimeLineText.setVisibility(View.GONE);
                // }else if(state == SCROLL_STATE_FLING){
                // mTimeLineText.setVisibility(View.VISIBLE);
                // }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // if(mTimeLineText.getVisibility() == View.VISIBLE) {
                // int index = firstVisibleItem + 1 ==
                // rootView.getAdapter().getCount() ? rootView.getAdapter().getCount() -
                // 1 : firstVisibleItem + 1;
                // ImageInfo imageInfo = (ImageInfo)
                // rootView.getAdapter().getItem(index);
                // if (imageInfo != null) {
                // mTimeLineText.setText(TimeUtils.formatPhotoDate(imageInfo.path));
                // }
                // }
            }
        });
        mGridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            public void onGlobalLayout() {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    final int width = mGridView.getWidth();
                    final int columnSpace = getResources().getDimensionPixelOffset(R.dimen.space_size);
                    final int numCount = 3;
                    int columnWidth = (width - columnSpace * (numCount - 1)) / numCount;
                    mImageAdapter.setItemSize(columnWidth);
                    mGridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mGridView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (mImageAdapter.isShowCamera()) {
                    if (i == 0) {
                        if (mCameraSdkParameterInfo.getMax_image() == resultList.size()) {
                            Toast.makeText(PhotoPickActivity.this, R.string.camerasdk_msg_amount_limit,
                                Toast.LENGTH_SHORT).show();
                        } else {
                            showCameraAction();
                        }
                        return;
                    }
                }
                ImageInfo imageInfo = (ImageInfo) adapterView.getAdapter().getItem(i);
                selectImageFromGrid(imageInfo, view);
            }
        });
    }

    /**
     * 选择相机
     */
    private void showCameraAction() {
        // 跳转到系统照相机
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(PhotoPickActivity.this.getPackageManager()) != null) {
            // 设置系统相机拍照后的输出路径
            // 创建临时文件
            mTmpFile = FileUtils.createTmpFile(PhotoPickActivity.this);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTmpFile));
            startActivityForResult(cameraIntent, CameraSdkParameterInfo.TAKE_PICTURE_FROM_CAMERA);
        } else {
            Toast.makeText(PhotoPickActivity.this, R.string.camerasdk_msg_no_camera, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // 相机拍照完成后，返回图片路径
            if (requestCode == CameraSdkParameterInfo.TAKE_PICTURE_FROM_CAMERA) {
                if (resultCode == Activity.RESULT_OK) {
                    if (mTmpFile != null) {
                        if (mCameraSdkParameterInfo.isSingle_mode()) {
                            resultList.clear();
                        }
                        resultList.add(mTmpFile.getPath());
                        selectComplate();
                    }
                } else {
                    if (mTmpFile != null && mTmpFile.exists()) {
                        mTmpFile.delete();
                    }
                }
            } else if (requestCode == CameraSdkParameterInfo.TAKE_PICTURE_PREVIEW) {
                if (resultCode == RESULT_OK) {
                    mCameraSdkParameterInfo =
                        (CameraSdkParameterInfo) data.getSerializableExtra(CameraSdkParameterInfo.EXTRA_PARAMETER);
                    resultList = mCameraSdkParameterInfo.getImage_list();
                    actionbar.setRightText(
                        "完成(" + resultList.size() + "/" + mCameraSdkParameterInfo.getMax_image() + ")");
                    mImageAdapter.reSetSelectedList(resultList);
                } else {

                }
            }
        } catch (Exception e) {
            //			if (Constant.IS_DEBUG_MODE)
            //			{
            //				e.printStackTrace();
            //			}
        }
    }

    /**
     * 选择图片操作
     */
    private void selectImageFromGrid(ImageInfo imageInfo, View view) {
        if (imageInfo != null) {
            // 多选模式
            if (!mCameraSdkParameterInfo.isSingle_mode()) {
                if (resultList.contains(imageInfo.path)) {
                    resultList.remove(imageInfo.path);
                    remoreImagePreview(imageInfo.path);
                } else {
                    // 判断选择数量问题
                    if (mCameraSdkParameterInfo.getMax_image() == resultList.size()) {
                        Toast.makeText(PhotoPickActivity.this, R.string.camerasdk_msg_amount_limit, Toast.LENGTH_SHORT)
                            .show();
                        return;
                    }
                    resultList.add(imageInfo.path);
                    addImagePreview(imageInfo.path);
                }
                mImageAdapter.select(imageInfo, view);
            } else {
                // 单选模式
                resultList.clear();
                resultList.add(imageInfo.path);
                selectComplate();
            }
        }
    }

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        private final String[] IMAGE_PROJECTION = {
            MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media._ID, MediaStore.Images.Media.SIZE
        };

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (id == LOADER_ALL) {
                CursorLoader cursorLoader = new CursorLoader(PhotoPickActivity.this,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, null, null,
                    IMAGE_PROJECTION[2] + " DESC");
                return cursorLoader;
            } else if (id == LOADER_CATEGORY) {
                CursorLoader cursorLoader = new CursorLoader(PhotoPickActivity.this,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                    IMAGE_PROJECTION[0] + " like '%" + args.getString("path") + "%'", null,
                    IMAGE_PROJECTION[2] + " DESC");
                return cursorLoader;
            }

            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {

                List<ImageInfo> imageInfos = new ArrayList<ImageInfo>();
                int count = data.getCount();
                if (count > 0) {
                    data.moveToFirst();
                    do {

                        String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                        String name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                        long dateTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
                        int size = data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[4]));
                        boolean show_flag = size > 1024 * 10; // 是否大于10K
                        ImageInfo imageInfo = null;
                        if (!TextUtils.isEmpty(path) && show_flag && name != null && !name.equals(
                            "offlinemapv4.png")/*
                         * && !name.toLowerCase().
                         * endsWith(".gif")
                         */) {
                            imageInfo = new ImageInfo(path, name, dateTime);
                            imageInfos.add(imageInfo);
                        } else {
                            show_flag = false;
                        }

                        if (!hasFolderGened && show_flag) {
                            // 获取文件夹名称
                            File imageFile = new File(path);
                            File folderFile = imageFile.getParentFile();
                            FolderInfo folderInfo = new FolderInfo();
                            folderInfo.name = folderFile.getName();
                            folderInfo.path = folderFile.getAbsolutePath();
                            folderInfo.cover = imageInfo;
                            if (!mResultFolder.contains(folderInfo)) {
                                List<ImageInfo> imageList = new ArrayList<ImageInfo>();
                                imageList.add(imageInfo);
                                folderInfo.imageInfos = imageList;
                                mResultFolder.add(folderInfo);
                            } else {
                                // 更新
                                FolderInfo f = mResultFolder.get(mResultFolder.indexOf(folderInfo));
                                f.imageInfos.add(imageInfo);
                            }
                        }
                    }
                    while (data.moveToNext());

                    mImageAdapter.setData(imageInfos);

                    // 设定默认选择
                    // if (resultList != null && resultList.size() > 0) {
                    mImageAdapter.setSelectedList(resultList);
                    initSelectImage();// 预览图
                    // }
                    if (mFolderAdapter != null) {
                        mFolderAdapter.setData(mResultFolder);
                    }
                    hasFolderGened = true;
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    // 预览选择的图片
    private void addImagePreview(final String path) {
        // int mItemSize = 90;
        // ImageView imageView = (ImageView) LayoutInflater.from(
        // PhotoPickActivity.this).inflate(
        // R.layout.camerasdk_list_item_image_view, selectedImageLayout,
        // false);
        // selectedImageLayout.addView(imageView);
        actionbar.setRightText("完成(" + resultList.size() + "/" + mCameraSdkParameterInfo.getMax_image() + ")");

        // imageView.postDelayed(new Runnable() {
        // @Override
        // public void run() {
        //
        // int off = selectedImageLayout.getMeasuredWidth()
        // - scrollview.getWidth();
        // if (off > 0) {
        // scrollview.smoothScrollTo(off, 0);
        // }
        // }
        // }, 100);
        //
        // hashMap.put(path, imageView);
        // File imageFile = new File(path);
        // Picasso.with(this).load(imageFile)
        // .error(R.drawable.camerasdk_pic_loading)
        // .resize(mItemSize, mItemSize).centerCrop().into(imageView);
        // imageView.setOnClickListener(new View.OnClickListener() {
        // @Override
        // public void onClick(View v) {
        //
        // resultList.remove(path);
        // mImageAdapter.removeOne(path);
        // remoreImagePreview(path);
        // }
        // });
    }

    // 删除图片预览
    private boolean remoreImagePreview(String path) {
        // if (hashMap.containsKey(path)) {
        // selectedImageLayout.removeView(hashMap.get(path));
        // hashMap.remove(path);
        actionbar.setRightText("完成(" + resultList.size() + "/" + mCameraSdkParameterInfo.getMax_image() + ")");
        return true;
        // } else {
        // return false;
        // }
    }

    /**
     * 创建弹出的文件夹ListView
     */
    private void showPopupFolder(View v) {

        View view = getLayoutInflater().inflate(R.layout.camerasdk_popup_folder, null);
        LinearLayout ll_popup = (LinearLayout) view.findViewById(R.id.ll_popup);
        ll_popup.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.camerasdk_push_up_in));

        ListView lsv_folder = (ListView) view.findViewById(R.id.lsv_folder);
        lsv_folder.setAdapter(mFolderAdapter);
        // if(mpopupWindow==null){

        WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        mpopupWindow = new PopupWindow(this);
        mpopupWindow.setWidth(LayoutParams.MATCH_PARENT);
        mpopupWindow.setHeight(LayoutParams.WRAP_CONTENT);

        mpopupWindow.setFocusable(true);
        mpopupWindow.setOutsideTouchable(true);
        // }
        view.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mpopupWindow.dismiss();
            }
        });
        try {
            view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        } catch (Exception e) {
            //			if (Constant.IS_DEBUG_MODE)
            //			{
            //				e.printStackTrace();
            //			}
        }
        int popupWidth = view.getMeasuredWidth();
        int popupHeight = view.getMeasuredHeight();
        if (popupHeight == 0) {
            popupHeight = AllOnlineApp.screenHeight;
        }
        lsv_folder.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                mFolderAdapter.setSelectIndex(arg2);
                final int index = arg2;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mpopupWindow.dismiss();
                        if (index == 0) {
                            getSupportLoaderManager().restartLoader(LOADER_ALL, null, mLoaderCallback);
                            mCategoryText.setText(R.string.camerasdk_album_all);
                            mImageAdapter.setShowCamera(mCameraSdkParameterInfo.isShow_camera());
                        } else {
                            FolderInfo folderInfo = (FolderInfo) mFolderAdapter.getItem(index);
                            if (null != folderInfo) {
                                mImageAdapter.setData(folderInfo.imageInfos);
                                mCategoryText.setText(folderInfo.name);
                                // 设定默认选择
                                if (resultList != null && resultList.size() > 0) {
                                    mImageAdapter.setSelectedList(resultList);
                                }
                            }
                            // mImageAdapter.setShowCamera(false);
                        }
                        // 滑动到最初始位置
                        mGridView.smoothScrollToPosition(0);
                    }
                }, 100);
            }
        });
        int[] location = new int[2];
        mCategoryText.getLocationOnScreen(location);
        mpopupWindow.setContentView(view);
        mpopupWindow.setBackgroundDrawable(new ColorDrawable(0xb0000000));
        mpopupWindow.showAtLocation(mCategoryText, Gravity.NO_GRAVITY, 0, location[1] - popupHeight);
        // AsDropDown(findViewById(R.id.layout_actionbar_root));
    }

    // 选择完成实现跳转
    private void selectComplate() {

        mCameraSdkParameterInfo.setImage_list(resultList);
        final Bundle b = new Bundle();
        b.putSerializable(CameraSdkParameterInfo.EXTRA_PARAMETER, mCameraSdkParameterInfo);
        b.putInt(CameraSdkParameterInfo.EXTRA_INDEX, iIndex);

        final Intent intent;
        if (mCameraSdkParameterInfo.isSingle_mode()) {
            // 单选模式
            if (mCameraSdkParameterInfo.isCroper_image()) {
                // 跳转到图片裁剪
                intent = new Intent(this, CropperImageActivity.class);
                intent.putExtras(b);
                startActivity(intent);
            } else if (mCameraSdkParameterInfo.isFilter_image()) {
                // 跳转到滤镜
                intent = new Intent(this, FilterImageActivity.class);
                intent.putExtras(b);
                startActivity(intent);
            } else {
                intent = new Intent();
                intent.putExtras(b);
                setResult(RESULT_OK, intent);
                finish();
            }
        } else {
            // 多选模式
            if (mCameraSdkParameterInfo.isFilter_image()) {
                // 跳转到滤镜
                intent = new Intent(this, FilterImageActivity.class);
                intent.putExtras(b);
                startActivity(intent);
            } else {
                if (getIntent().getBooleanExtra(FROM_IM, false)) {
                    //从IM过来的先压缩再返回
                    final ProgressDialogEx progressDialogEx = new ProgressDialogEx(PhotoPickActivity.this);
                    progressDialogEx.show(getString(R.string.image_compress_wait));
                    intent = new Intent();
                    GMUserUtil.getInstance().getSonWorkHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            int size = mCameraSdkParameterInfo.getImage_list().size();
                            ArrayList<String> compressedPaths = new ArrayList<String>();
                            for (int i = 0; i < size; i++) {
                                if (mCameraSdkParameterInfo.getImage_list().get(i) != null) {
                                    com.coomix.app.all.model.bean.ImageInfo imageInfo =
                                        new com.coomix.app.all.model.bean.ImageInfo();
                                    imageInfo.setSource_image(mCameraSdkParameterInfo.getImage_list().get(i));
                                    ImageCompressUtils.compress(PhotoPickActivity.this, imageInfo);
                                    compressedPaths.add(imageInfo.getSource_image());
                                }
                            }
                            mCameraSdkParameterInfo.setImage_list(compressedPaths);
                            b.putSerializable(CameraSdkParameterInfo.EXTRA_PARAMETER, mCameraSdkParameterInfo);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialogEx.dismiss();
                                    intent.putExtras(b);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            });
                        }
                    });
                } else {
                    intent = new Intent();
                    intent.putExtras(b);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }
    }

    // 返回特效处理后的图片
    public void getFilterComplate(ArrayList<String> list) {
        mCameraSdkParameterInfo.setImage_list(list);
        Bundle b = new Bundle();
        b.putSerializable(CameraSdkParameterInfo.EXTRA_PARAMETER, mCameraSdkParameterInfo);

        Intent intent = new Intent();
        intent.putExtras(b);
        setResult(RESULT_OK, intent);
        finish();
    }

    // 返回裁剪后的图片
    public void getForResultComplate(String path) {

        ArrayList<String> list = new ArrayList<String>();
        list.add(path);

        Intent intent = new Intent();
        mCameraSdkParameterInfo.setImage_list(list);
        Bundle b = new Bundle();
        b.putSerializable(CameraSdkParameterInfo.EXTRA_PARAMETER, mCameraSdkParameterInfo);
        intent.putExtras(b);
        setResult(RESULT_OK, intent);
        finish();
    }
}
