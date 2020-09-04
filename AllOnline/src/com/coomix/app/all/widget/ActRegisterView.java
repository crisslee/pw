package com.coomix.app.all.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.PoiItem;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;
import com.coomix.app.all.dialog.PopListDialog;
import com.coomix.app.all.dialog.PopSelectorDialog;
import com.coomix.app.all.ui.activity.ActRegisterExtendActivity;
import com.coomix.app.all.ui.activity.ImageAddActAdapter;
import com.coomix.app.all.model.bean.ActCommitItem;
import com.coomix.app.all.model.bean.ActCommitItemOption;
import com.coomix.app.all.model.bean.ActPrice;
import com.coomix.app.all.model.bean.CommunityActDetail;
import com.coomix.app.all.model.bean.ImageInfo;
import com.coomix.app.all.model.bean.LocationInfo;
import com.coomix.app.all.model.bean.PriceRule;
import com.coomix.app.all.dialog.AskDialog;
import com.coomix.app.all.util.CommunityUtil;
import com.coomix.app.all.util.CompressImageTask;
import com.muzhi.camerasdk.PhotoPickActivity;
import com.muzhi.camerasdk.PreviewActivity;
import com.muzhi.camerasdk.model.CameraSdkParameterInfo;

import java.util.ArrayList;

/**
 * Created by think on 2017/3/15.
 */

public class ActRegisterView extends LinearLayout
        implements View.OnClickListener
{
    private CommunityActDetail commActDetailInfo;
    private LinearLayout layoutMain;
    private Context mContext;
    private TextView textmainTitle;
    private ImageView imageclose;
    private int iCurViewIndex = 0; //只有第一个人的信息才填写后台给的（如果给了）已知的信息
    private OnViewOrDataChangeListener onViewDeleteListener = null;
    private int iViewCount = 0;
    private int iTextViewClickPos = -1;
    private boolean isToOtherActivity = false;
    private int iPriceIndex = -1;
    private int iGridViewItemWidth = 0;
    private String userName;

    public ActRegisterView(Context context, CommunityActDetail commActDetailInfo, int iViewIndex)
    {
        this(context, null, commActDetailInfo, iViewIndex);
    }

    public ActRegisterView(Context context, AttributeSet attrs, CommunityActDetail commActDetailInfo, int iViewIndex)
    {
        this(context, attrs, 0, commActDetailInfo, iViewIndex);
    }

    public ActRegisterView(Context context, AttributeSet attrs, int defStyle, CommunityActDetail commActDetailInfo, int iViewIndex)
    {
        super(context, attrs, defStyle);
        this.commActDetailInfo = commActDetailInfo;
        this.mContext = context;
        this.iCurViewIndex = iViewIndex;

        initViews();
    }

    public void resetCurViewIndex(int iViewIndex)
    {
        this.iCurViewIndex = iViewIndex;
    }

    public int getPriceIndex()
    {
        return iPriceIndex;
    }

    public boolean isJumpToOtherActivity()
    {
        return isToOtherActivity;
    }

    public void setViewTitle(int position)
    {
        setTitle(position);
    }

    private void initViews()
    {
        View mainView = LayoutInflater.from(getContext()).inflate(R.layout.act_register_main, this);
        layoutMain = (LinearLayout) mainView.findViewById(R.id.linearLayoutMain);
        textmainTitle = (TextView) mainView.findViewById(R.id.textViewMainTitle);
        imageclose = (ImageView) mainView.findViewById(R.id.imageViewClose);
        imageclose.setOnClickListener(this);
        if (iCurViewIndex == 0)
        {
            imageclose.setVisibility(GONE);
        }

        addPackageView(commActDetailInfo.getPrice());

        ArrayList<ActCommitItem> commitItems = commActDetailInfo.getCommit_items();
        if (commitItems != null && commitItems.size() > 0)
        {
            int size = commitItems.size();
            for (int i = 0; i < size; i++)
            {
                ActCommitItem item = commitItems.get(i);
                if (item != null)
                {
                    drawUiByType(item, i == size - 1);
                }
            }
        }
    }

    private void drawUiByType(ActCommitItem item, boolean bHideGrid)
    {
        switch (item.getType())
        {
            case ActCommitItem.INPUT_TEXT_NORMAL:
            case ActCommitItem.INPUT_NUMBERS:
                //一般的输入内容
                addEditTextView(item, bHideGrid);
                break;

            case ActCommitItem.RADIO_CONTENT:
                //类似性别的单项选择器
                addRadioButtonView(item, bHideGrid);
                break;

            case ActCommitItem.POP_SELECT_CONTENT:
                //类似弹窗选择省市区多级信息，或弹出一级选择
                addPopSelectView(item, bHideGrid);
                break;

            case ActCommitItem.MAP_SELECT_LOCATION:
                //从地图选取位置信息
                addSelectLocationView(item, bHideGrid);
                break;

            case ActCommitItem.UPLOAD_IMAGE:
                //上传图片
                addUploadImageView(item, bHideGrid);
                break;

            default:
                break;
        }
    }

    /***
     * 添加套餐的选择view
     */
    private void addPackageView(final ActPrice actPrice)
    {
        if (actPrice == null || actPrice.getType() != ActPrice.PRICE_PACKAGE)
        {
            //只有套餐模式才添加
            return;
        }
        if (actPrice.getPrice_rule() == null || actPrice.getPrice_rule().size() <= 0)
        {
            return;
        }

        View view = LayoutInflater.from(getContext()).inflate(R.layout.act_register_item_textview, null);
        layoutMain.addView(view);
        view.findViewById(R.id.imageViewMust).setVisibility(VISIBLE);
        TextView textTitle = (TextView) view.findViewById(R.id.textViewTitle);
        final TextView textViewContent = (TextView) view.findViewById(R.id.textViewAct);

        String title = actPrice.getPrice_rule_title();
        if (TextUtils.isEmpty(title))
        {
            title = getContext().getString(R.string.act_package);
        }
        textTitle.setText(title);

        textViewContent.setHint(getContext().getString(R.string.act_package_tips_input, title));

        CommunityUtil.setTextDrawable(getContext(), textViewContent, R.drawable.actionbar_right, "", CommunityUtil.DRAWABLE_RIGHT);

        iPriceIndex = actPrice.getPrice_index();
        if (iPriceIndex >= 0 && iPriceIndex < actPrice.getPrice_rule().size())
        {
            textViewContent.setText(actPrice.getPrice_rule().get(iPriceIndex).getName());
        }
        else
        {
            textViewContent.setText("");
        }

        textViewContent.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showPopList(actPrice, textViewContent);
            }
        });
    }

    private void showPopList(final ActPrice actPrice, final TextView textView)
    {
        if (actPrice == null || actPrice.getPrice_rule() == null)
        {
            return;
        }
        final ArrayList<String> listData = new ArrayList<String>();
        for (PriceRule rule : actPrice.getPrice_rule())
        {
            if (rule != null)
            {
                listData.add(rule.getName());
            }
        }

        final PopListDialog popListDialog = new PopListDialog(mContext, listData);
        popListDialog.setTextTitle(textView.getHint().toString());
        popListDialog.show();
        popListDialog.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                popListDialog.dismiss();
                if (position >= 0 && position < actPrice.getPrice_rule().size())
                {
                    if (isPackagesInLimit(position))
                    {
                        actPrice.setPrice_index(position);
                        textView.setText(actPrice.getPrice_rule().get(position).getName());
                        if (onViewDeleteListener != null)
                        {
                            onViewDeleteListener.onPackageChange();
                        }
                    }
                }
            }
        });
    }

    /**
     * 判断是否达到套餐的限制
     */
    private boolean isPackagesInLimit(int position)
    {
        if (commActDetailInfo.getPrice() == null || commActDetailInfo.getPrice().getType() != ActPrice.PRICE_PACKAGE)
        {
            return false;
        }

        //减去之前已经选择的套餐的计数
        if (ActRegisterExtendActivity.mapItemPackages.containsKey(iPriceIndex))
        {
            int iCount = ActRegisterExtendActivity.mapItemPackages.get(iPriceIndex);
            if (iCount > 0)
            {
                ActRegisterExtendActivity.mapItemPackages.put(iPriceIndex, iCount - 1);
            }
        }

        //判断是否超过限制，如果没有则对选择的套餐计数
        if (ActRegisterExtendActivity.mapItemPackages.containsKey(position))
        {
            int iCount = ActRegisterExtendActivity.mapItemPackages.get(position);
            int iMaxCount = commActDetailInfo.getPrice().getPrice_rule().get(position).getMax_cnt();
            if (iMaxCount > 0 && iCount >= iMaxCount)
            {
                Toast.makeText(mContext, R.string.act_sign_max_package, Toast.LENGTH_SHORT).show();
                return false;
            }
            this.iPriceIndex = position;
            ActRegisterExtendActivity.mapItemPackages.put(iPriceIndex, iCount + 1);
        }

        return true;
    }

    private void addEditTextView(final ActCommitItem item, boolean bHideGrid)
    {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.act_register_item_edittext, null);
        layoutMain.addView(view);
        TextView textTitle = (TextView) view.findViewById(R.id.textViewTitle);
        EditText editText = (EditText) view.findViewById(R.id.editText);
        if (bHideGrid)
        {
            view.findViewById(R.id.gridLine).setVisibility(View.GONE);
        }
        else
        {
            view.findViewById(R.id.gridLine).setVisibility(View.VISIBLE);
        }
        if (item.getRequired() == 1)
        {
            view.findViewById(R.id.imageViewMust).setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(item.getName()))
        {
            textTitle.setText(item.getName());
        }
        if (!TextUtils.isEmpty(item.getHint()))
        {
            editText.setHint(item.getHint());
        }
        if (item.getType() == ActCommitItem.INPUT_NUMBERS)
        {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        addDataAndViewTag(item, editText);

        editText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                item.setValue(s.toString());
            }
        });
        if (iCurViewIndex == 0 && item.getValue() != null && item.getValue() instanceof String && !TextUtils.isEmpty((String) item.getValue()))
        {
            editText.setText((String) item.getValue());
            editText.setSelection(editText.length());
        }
        else
        {
            editText.setText("");
        }
    }

    private void addRadioButtonView(final ActCommitItem item, boolean bHideGrid)
    {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.act_register_item_radiogroup, null);
        layoutMain.addView(view);
        TextView textTitle = (TextView) view.findViewById(R.id.textViewTitle);
        final LinearLayout radioGroup = (LinearLayout) view.findViewById(R.id.radioGroupAct);
        if (bHideGrid)
        {
            view.findViewById(R.id.gridLine).setVisibility(View.GONE);
        }
        else
        {
            view.findViewById(R.id.gridLine).setVisibility(View.VISIBLE);
        }
        if (item.getRequired() == 1)
        {
            view.findViewById(R.id.imageViewMust).setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(item.getName()))
        {
            textTitle.setText(item.getName());
        }
        int id = -1;
        try
        {
            ArrayList<Integer> listDefs = null;
            if (iCurViewIndex == 0 && item.getValue() != null && item.getValue() instanceof ArrayList)
            {
                listDefs = (ArrayList<Integer>) item.getValue();
            }
            else if (item.getOptions() != null)
            {
                listDefs = item.getDefault_option();
            }
            if (listDefs != null && listDefs.size() > 0)
            {
                id = listDefs.get(0);
            }
        }
        catch (Exception e)
        {

        }

        addDataAndViewTag(item, radioGroup);

        for (int i = 0; i < item.getOptions().size(); i++)
        {
            final ActCommitItemOption sonItemOp = item.getOptions().get(i);
            final TextView textView = new TextView(mContext);
            LinearLayout.LayoutParams params = new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
            params.leftMargin = mContext.getResources().getDimensionPixelOffset(R.dimen.space_5x);
            textView.setLayoutParams(params);
            textView.setTextColor(mContext.getResources().getColorStateList(R.color.text_color_gray_selector));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimensionPixelSize(R.dimen.text));
            textView.setSingleLine();
            textView.setText(sonItemOp.getName());
            textView.setId(sonItemOp.getId());//该项对应的数据的id，用于选择后的数据设置44
            textView.setPadding(getResources().getDimensionPixelOffset(R.dimen.space_2x), 0, 0, 0);
            radioGroup.addView(textView);
            if (id == sonItemOp.getId())
            {
                textView.setSelected(true);
            }
            else
            {
                textView.setSelected(false);
            }
            textView.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (item != null)
                    {
                        ArrayList<Integer> listData = new ArrayList<Integer>();
                        listData.add(v.getId());
                        item.setValue(listData);
                    }
                    for (int i = 0; i < radioGroup.getChildCount(); i++)
                    {
                        radioGroup.getChildAt(i).setSelected(false);
                    }
                    v.setSelected(true);
                }
            });
        }
    }

    private void addPopSelectView(final ActCommitItem item, boolean bHideGrid)
    {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.act_register_item_textview, null);
        layoutMain.addView(view);
        TextView textTitle = (TextView) view.findViewById(R.id.textViewTitle);
        final TextView textViewContent = (TextView) view.findViewById(R.id.textViewAct);
        if (item.getRequired() == 1)
        {
            view.findViewById(R.id.imageViewMust).setVisibility(View.VISIBLE);
        }
        if (bHideGrid)
        {
            view.findViewById(R.id.gridLine).setVisibility(View.GONE);
        }
        else
        {
            view.findViewById(R.id.gridLine).setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(item.getName()))
        {
            textTitle.setText(item.getName());
        }

        if (!TextUtils.isEmpty(item.getHint()))
        {
            textViewContent.setHint(item.getHint());
        }

        CommunityUtil.setTextDrawable(getContext(), textViewContent, R.drawable.actionbar_right, "", CommunityUtil.DRAWABLE_RIGHT);

        addDataAndViewTag(item, textViewContent);

        textViewContent.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showPopSelecDialog(item, textViewContent);
            }
        });

        String content = item.getPopSelectValue();
        if (iCurViewIndex == 0 && !TextUtils.isEmpty(content))
        {
            textViewContent.setText(content);
        }
        else
        {
            textViewContent.setText("");
        }
    }

    private void showPopSelecDialog(final ActCommitItem item, final TextView textView)
    {
        PopSelectorDialog selectorDialog = new PopSelectorDialog(getContext(), item);
        selectorDialog.show();
        selectorDialog.setOnLastSelectListener(new PopSelectorDialog.OnLastSelectListener()
        {
            @Override
            public void onLastSelectedItem(ActCommitItemOption... commitItemOption)
            {
                String text = "";
                if (commitItemOption != null && commitItemOption.length > 0)
                {
                    for (ActCommitItemOption option : commitItemOption)
                    {
                        text += option.getName();
                    }
                }
                textView.setText(text);
            }

            @Override
            public void onLastSelectedIndex(int... ids)
            {
                item.setPopSelectValue(ids);
            }
        });
    }

    private void addSelectLocationView(final ActCommitItem item, boolean bHideGrid)
    {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.act_register_item_textview, null);
        layoutMain.addView(view);
        TextView textTitle = (TextView) view.findViewById(R.id.textViewTitle);
        final TextView textLocation = (TextView) view.findViewById(R.id.textViewAct);
        if (item.getRequired() == 1)
        {
            view.findViewById(R.id.imageViewMust).setVisibility(View.VISIBLE);
        }
        if (bHideGrid)
        {
            view.findViewById(R.id.gridLine).setVisibility(View.GONE);
        }
        else
        {
            view.findViewById(R.id.gridLine).setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(item.getName()))
        {
            textTitle.setText(item.getName());
        }

        if (!TextUtils.isEmpty(item.getHint()))
        {
            textLocation.setHint(item.getHint());
        }
        setLocationData(textLocation, item);
        addDataAndViewTag(item, textLocation);
//        textLocation.setOnClickListener(new OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                //去往位置选择的界面
//                iTextViewClickPos = (int) textLocation.getTag();
//                AllOnlineApp.getInstantce().setSelectLocationInfo(null);
//                Intent intent = new Intent(mContext, TransitSearchPoiActivity.class);
//                intent.putExtra(TransitSearchPoiActivity.SEARCH_TYPE, TransitSearchPoiActivity.SELECT_LOC);
//                startActivityForResult(intent, ActRegisterExtendActivity.ACT_REQUEST_CODE);
//            }
//        });
    }

    private void setLocationData(TextView textView, ActCommitItem item)
    {
        String value = item.getLocationValue();
        if (iCurViewIndex == 0 && !TextUtils.isEmpty(value))
        {
            textView.setText(value);
        }
        else
        {
            textView.setText("");
        }
    }

    private void addUploadImageView(final ActCommitItem item, boolean bHideGrid)
    {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.act_register_item_gridview, null);
        layoutMain.addView(view);
        TextView textTitle = (TextView) view.findViewById(R.id.textViewTitle);
        final GridView gridView = (GridView) view.findViewById(R.id.gridViewAct);
        if (item.getRequired() == 1)
        {
            view.findViewById(R.id.imageViewMust).setVisibility(View.VISIBLE);
        }
        if (bHideGrid)
        {
            view.findViewById(R.id.gridLine).setVisibility(View.GONE);
        }
        else
        {
            view.findViewById(R.id.gridLine).setVisibility(View.VISIBLE);
        }
        if (!TextUtils.isEmpty(item.getName()))
        {
            textTitle.setText(item.getName());
        }

        ArrayList<ImageInfo> listImages = null;
        if(iCurViewIndex == 0)
        {
            listImages = item.getImagesListValue();
        }
        final ImageAddActAdapter imageAdapter = new ImageAddActAdapter((Activity) mContext, listImages);
        gridView.setAdapter(imageAdapter);
        imageAdapter.setAdapterIndex(iViewCount);

        gridView.setHorizontalSpacing(getResources().getDimensionPixelOffset(R.dimen.space));
        gridView.setVerticalSpacing(getResources().getDimensionPixelOffset(R.dimen.space));
        gridView.setNumColumns(4);
        gridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @SuppressLint("NewApi")
            @Override
            public void onGlobalLayout()
            {
                iGridViewItemWidth = gridView.getWidth() / 4 - getResources().getDimensionPixelSize(R.dimen.space) * 3;
                imageAdapter.setItemSize(iGridViewItemWidth);
                gridView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                ArrayList<ImageInfo> list = imageAdapter.getAllListData();
                if (list != null && position >= 0 && position < list.size())
                {
                    if (list.get(position).isAddButton)
                    {
                        toPhotoPick(imageAdapter);
                    }
                    else if (!imageAdapter.isBlankImageInfo(list.get(position)))
                    {
                        showPreviewImageView(imageAdapter, position);
                    }
                }
            }
        });
        addDataAndViewTag(item, gridView);
    }

    public String getUserName()
    {
        return userName;
    }

    private void setTitle(int position)
    {
        if (iCurViewIndex == 0)
        {
            userName = getContext().getString(R.string.act_item_mine);
        }
        else
        {
            userName = getContext().getString(R.string.act_item_name, position + 1);
        }
        textmainTitle.setText(userName);
    }

    private void addDataAndViewTag(ActCommitItem item, View view)
    {
        if (item == null || view == null)
        {
            return;
        }

        view.setTag(iViewCount);

        item.setView(view);

        iViewCount++;
    }

    private void toPhotoPick(ImageAddActAdapter imageAdapter)
    {
        CameraSdkParameterInfo mCameraSdkParameterInfo = new CameraSdkParameterInfo();
        mCameraSdkParameterInfo.setSingle_mode(false);
        mCameraSdkParameterInfo.setShow_camera(true);
        mCameraSdkParameterInfo.setMax_image(ImageAddActAdapter.MAX_IMAGE - imageAdapter.getImageList().size());
        mCameraSdkParameterInfo.setCroper_image(true);
        mCameraSdkParameterInfo.setFilter_image(false);

        Intent intent = new Intent(mContext, PhotoPickActivity.class);
        intent.putExtra(CameraSdkParameterInfo.EXTRA_PARAMETER, mCameraSdkParameterInfo);
        intent.putExtra(CameraSdkParameterInfo.EXTRA_INDEX, imageAdapter.getAdapterIndex());
        startActivityForResult(intent, CameraSdkParameterInfo.TAKE_PICTURE_FROM_GALLERY);
    }

    private void showPreviewImageView(ImageAddActAdapter imageAdapter, int position)
    {
        Intent intent = new Intent(mContext, PreviewActivity.class);
        Bundle bundle = new Bundle();
        CameraSdkParameterInfo info = new CameraSdkParameterInfo();
        info.setPosition(position);
        ArrayList<String> image_list = new ArrayList<String>();
        ArrayList<ImageInfo> listImages = imageAdapter.getImageList();
        for (int i = 0; i < listImages.size(); i++)
        {
            if (listImages.get(i) != null)
            {
                if (listImages.get(i).isNet())
                {
                    image_list.add(listImages.get(i).getDomain() + listImages.get(i).getImg_path());
                }
                else
                {
                    image_list.add(listImages.get(i).getSource_image());
                }
            }
        }
        if (image_list.size() > 0)
        {
            info.setImage_list(image_list);
            bundle.putSerializable(CameraSdkParameterInfo.EXTRA_PARAMETER, info);
            bundle.putInt(CameraSdkParameterInfo.EXTRA_INDEX, imageAdapter.getAdapterIndex());
            intent.putExtras(bundle);
            intent.putExtra(PreviewActivity.INTENT_TOP_TYPE, PreviewActivity.TOP_DELETE);
            startActivityForResult(intent, CameraSdkParameterInfo.TAKE_PICTURE_PREVIEW);
            ((Activity) mContext).overridePendingTransition(R.anim.zoom_enter, 0);
        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.imageViewClose:
                askDelete();
                break;
        }
    }

    private void askDelete()
    {
        final AskDialog askDialog = new AskDialog(getContext());
        askDialog.setShowMsg(getContext().getString(R.string.act_item_delete_ask, textmainTitle.getText().toString()));
        askDialog.show();
        askDialog.setOnYesClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                askDialog.dismiss();
                if (onViewDeleteListener != null)
                {
                    onViewDeleteListener.onViewDeleted(iCurViewIndex);
                }
            }
        });
    }

    public void setOnViewDeleteListener(OnViewOrDataChangeListener listener)
    {
        this.onViewDeleteListener = listener;
    }

    public interface OnViewOrDataChangeListener
    {
        public void onViewDeleted(int position);

        public void onPackageChange();
    }

    private void startActivityForResult(Intent intent, int code)
    {
        ((Activity) mContext).startActivityForResult(intent, code);
        isToOtherActivity = true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == ActRegisterExtendActivity.ACT_REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
            //去往地图选位置后范湖到此处
            setSelectLocationResultData();
        }
        else if (requestCode == CameraSdkParameterInfo.TAKE_PICTURE_FROM_GALLERY)
        {
            //图片选择后返回此处
            if (data != null)
            {
                parseResult(data.getExtras(), true);
            }
        }
        else if (requestCode == CameraSdkParameterInfo.TAKE_PICTURE_PREVIEW)
        {
            //图片预览界面返回此处
            if (data != null)
            {
                parseResult(data.getExtras(), false);
            }
        }
        isToOtherActivity = false;
    }

    private ActCommitItem getCallbackCommItemTextView()
    {
        if (commActDetailInfo.getCommit_items() != null && iTextViewClickPos >= 0 && iTextViewClickPos < commActDetailInfo.getCommit_items().size())
        {
            ActCommitItem actCommitItem = commActDetailInfo.getCommit_items().get(iTextViewClickPos);
            if (actCommitItem != null && actCommitItem.getView() != null && actCommitItem.getView() instanceof TextView)
            {
                return actCommitItem;
            }
        }
        return null;
    }

    public void setSelectLocationResultData()
    {
        //去往地图选位置后范湖到此处
        final LocationInfo selectedLocInfo = AllOnlineApp.getInstantce().getSelectLocationInfo();
        if (selectedLocInfo != null)
        {
            String locatinName = selectedLocInfo.getAddress();
            if (TextUtils.isEmpty(locatinName))
            {
                locatinName = selectedLocInfo.getName();
            }
            final ActCommitItem actCommitItem = getCallbackCommItemTextView();
            if (actCommitItem == null || !(actCommitItem.getView() instanceof TextView))
            {
                return;
            }
            final TextView textLocation = (TextView) actCommitItem.getView();
            if (textLocation != null)
            {
                textLocation.setText(locatinName);
            }
            actCommitItem.setLocationValue(selectedLocInfo.getLongitude(), selectedLocInfo.getLatitude(), locatinName);
            if (locatinName.equals(mContext.getString(R.string.my_location)))
            {
                LatLng latLng = new LatLng(selectedLocInfo.getLatitude(), selectedLocInfo.getLongitude());
                CommunityUtil.getAddressByLatLng(mContext, latLng, new CommunityUtil.OnAddressCallBack()
                {
                    @Override
                    public void onAddressBack(PoiItem poiItem)
                    {
                        String locName = mContext.getString(R.string.unknown);
                        if (poiItem != null)
                        {
                            locName = poiItem.getTitle();
                        }
                        actCommitItem.setLocationValue(selectedLocInfo.getLongitude(), selectedLocInfo.getLatitude(), locName);
                        textLocation.setText(locName);
                        AllOnlineApp.getInstantce().setSelectLocationInfo(null);
                    }
                });
            }
            else
            {
                AllOnlineApp.getInstantce().setSelectLocationInfo(null);
            }
        }
    }

    public ImageAddActAdapter getGridViewAdapter(int iIndex)
    {
        if (commActDetailInfo.getCommit_items() != null && iIndex >= 0 && iIndex < commActDetailInfo.getCommit_items().size())
        {
            ActCommitItem actCommitItem = commActDetailInfo.getCommit_items().get(iIndex);
            if (actCommitItem != null && actCommitItem.getView() != null && actCommitItem.getView() instanceof GridView)
            {
                GridView gridView = (GridView) actCommitItem.getView();
                if (gridView != null)
                {
                    return (ImageAddActAdapter) gridView.getAdapter();
                }
            }
        }
        return null;
    }

    private void parseResult(Bundle bundle, boolean isAdd)
    {
        if (bundle != null)
        {
            final ArrayList<ImageInfo> pic_list = new ArrayList<ImageInfo>();
            CameraSdkParameterInfo mCameraSdkParameterInfo = (CameraSdkParameterInfo) bundle.getSerializable(CameraSdkParameterInfo.EXTRA_PARAMETER);
            //可能多个上传图片的
            int iAdapterIndex = bundle.getInt(CameraSdkParameterInfo.EXTRA_INDEX);
            ArrayList<String> list = mCameraSdkParameterInfo.getImage_list();
            ImageAddActAdapter imageAdapter = getGridViewAdapter(iAdapterIndex);
            if (imageAdapter == null)
            {
                return;
            }
            if (list != null)
            {
                for (int i = 0; i < list.size(); i++)
                {
                    ImageInfo img = null;
                    if (isAdd)
                    {
                        //添加图片返回
                        img = new ImageInfo();
                        img.setSource_image(list.get(i));
                        img.setNet(false);
                    }
                    else
                    {
                        for (int j = 0; j < imageAdapter.getImageList().size(); j++)
                        {
                            ImageInfo imageInfo = imageAdapter.getItem(j);
                            if (imageInfo != null)
                            {
                                if (imageInfo.isNet())
                                {
                                    String netPath = imageInfo.getDomain() + imageInfo.getImg_path();
                                    if (netPath != null && netPath.equals(list.get(i)))
                                    {
                                        img = imageInfo;
                                    }
                                }
                                else if (imageInfo.getSource_image() != null && imageInfo.getSource_image().equals(list.get(i)))
                                {
                                    img = imageInfo;
                                }
                            }
                        }
                    }
                    if (img != null)
                    {
                        pic_list.add(img);
                    }
                }
                if (isAdd)
                {
                    compressPics(pic_list, iAdapterIndex);
                }
            }
            if (!isAdd)
            {
                imageAdapter.setList(pic_list);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void compressPics(final ArrayList<ImageInfo> pic_list, final int iAdapterIndex)
    {
        new CompressImageTask((Activity) mContext, new CompressImageTask.OnCompressListener()
        {
            @Override
            public void onCompressed(ArrayList<ImageInfo> result)
            {
                ImageAddActAdapter imageAdapter = getGridViewAdapter(iAdapterIndex);
                if (imageAdapter != null && result != null)
                {
                    imageAdapter.addList(result);
                }
            }
        }).execute(pic_list);
    }
}
