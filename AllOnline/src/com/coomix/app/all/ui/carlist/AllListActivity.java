package com.coomix.app.all.ui.carlist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.bumptech.GlideApp;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.manager.DeviceManager;
import com.coomix.app.all.manager.SettingDataManager;
import com.coomix.app.all.manager.ThemeManager;
import com.coomix.app.all.model.bean.DeviceInfo;
import com.coomix.app.all.model.bean.FileBean;
import com.coomix.app.all.model.bean.SubAccount;
import com.coomix.app.all.model.response.RespAccountGroupInfo;
import com.coomix.app.all.model.response.RespBatchAddress;
import com.coomix.app.all.model.response.RespDeviceList;
import com.coomix.app.all.share.PopupWindowUtil;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.ui.detail.DeviceDetailInfoActivity;
import com.coomix.app.all.util.ViewUtil;
import com.coomix.app.all.widget.ClearEditView;
import com.coomix.app.all.widget.observableList.ObservableListView;
import com.coomix.app.framework.util.CommonUtil;
import com.zhy.tree.bean.Node;
import com.zhy.tree.bean.TreeListViewAdapter.OnTreeNodeClickListener;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.List;

public class AllListActivity extends BaseActivity implements ExpandableListView.OnChildClickListener {
    View layoutCarTreeview, layoutCarTreeviewFooter;
    PopupWindow popupWinCarTreeView;
    private TextView textAccount;

    private TextView textOnline, textOffline, textUnuse;

    // 车辆列表
    private ObservableListView mDeviceListView;
    private DeviceListAdapter mDeviceListAdapter;

    private static final int MSG_MONITOR = 1;

    private ClearEditView editText;
    private TextView textSearchCancel;
    private TextView textToMap;
    private ListView listViewHistory;
    private HistoryAdapter historyAdapter;

    private String expireDes;
    private boolean isSubCountChanged = false;

    /**
     * 子账号界面
     */
    private List<FileBean> listSubcountsData = new ArrayList<FileBean>();
    private ListView listViewCounts;
    private SimpleTreeAdapter subAccountTreeAdapter;

    private int parentId = 0;
    private int checkedPositon = ListView.INVALID_POSITION;
    private boolean isSearching = false;
    private ArrayList<String> listHistory = null;

    private HandlerEx mHandler = null;
    private DeviceManager deviceManager = null;
    private ArrayList<DeviceInfo> listSearched = new ArrayList<DeviceInfo>();
    private final int MAX_DEVS = 10;
    private ArrayList<DeviceInfo> listNows;
    // 搜索后子項目地点击
    private DeviceInfo searchedClickDevice = null;

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dev_list);

        deviceManager = DeviceManager.getInstance();
        mHandler = new HandlerEx();

        initViews();

        initTreeView();

        updateListData(true);

        SubAccount curSubAccount = deviceManager.getCurrentSubaccount();
        if (curSubAccount != null) {
            parentId = Integer.parseInt(curSubAccount.id);
            checkedPositon = Integer.parseInt(curSubAccount.id);
            setCurrentAccountName(curSubAccount.showname);
        }

        // 初始化过期说明
        //initExpireDes();
        expandAllGroups();

        startPolling();

        getAccountList();
    }

    private void initViews() {
        textToMap = (TextView) findViewById(R.id.textViewToMap);
        textToMap.setOnClickListener(view -> finish());

        //汽车列表
        mDeviceListView = (ObservableListView) findViewById(R.id.deviceList);
        editText = (ClearEditView) findViewById(R.id.editTextSearch);
        textSearchCancel = (TextView) findViewById(R.id.textViewSearchCancel);
        textSearchCancel.setVisibility(View.GONE);

        listViewHistory = (ListView) findViewById(R.id.listViewHistory);
        listViewHistory.setVisibility(View.GONE);

        textAccount = (TextView) findViewById(R.id.tv_current_account);

        mDeviceListView.setOnChildClickListener(this);

        textOnline = (TextView) findViewById(R.id.textViewOnline);
        textOffline = (TextView) findViewById(R.id.textViewOffline);
        textUnuse = (TextView) findViewById(R.id.textViewUnuse);
        textOnline.setOnClickListener(view -> {
            textOnline.setSelected(!textOnline.isSelected());
            textOffline.setSelected(false);
            textUnuse.setSelected(false);
            setTextBG(textOnline);
            setTextBG(textOffline);
            setTextBG(textUnuse);
            if (isSearching) {
                updateSearchedListData();
            } else {
                updateListData(false);
            }
            mDeviceListView.setSelection(0);
        });
        textOffline.setOnClickListener(view -> {
            textOnline.setSelected(false);
            textOffline.setSelected(!textOffline.isSelected());
            textUnuse.setSelected(false);
            setTextBG(textOnline);
            setTextBG(textOffline);
            setTextBG(textUnuse);
            if (isSearching) {
                updateSearchedListData();
            } else {
                updateListData(false);
            }
            mDeviceListView.setSelection(0);
        });
        textUnuse.setOnClickListener(view -> {
            textOnline.setSelected(false);
            textOffline.setSelected(false);
            textUnuse.setSelected(!textUnuse.isSelected());
            setTextBG(textOnline);
            setTextBG(textOffline);
            setTextBG(textUnuse);
            if (isSearching) {
                updateSearchedListData();
            } else {
                updateListData(false);
            }
            mDeviceListView.setSelection(0);
        });

        //click title
        textAccount.setOnClickListener(v -> showTreeView());

        textSearchCancel.setOnClickListener(view -> cancelSearch());

        editText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    searchClickedAction();
                    return true;
                }
                return false;
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @SuppressLint("DefaultLocale")
            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && !TextUtils.isEmpty(s.toString())) {
                    listViewHistory.setVisibility(View.GONE);
                }
                if (s != null) {
                    searchInLocal(s.toString());
                }
            }
        });
        editText.setOnClickListener(view -> {
            textToMap.setVisibility(View.GONE);
            textSearchCancel.setVisibility(View.VISIBLE);
            if (editText.getText() != null && !TextUtils.isEmpty(editText.getText().toString())) {
                editText.setSelection(editText.getText().toString().length());
            }
            showInputHistory();
        });

        //设置顶部主题颜色
        findViewById(R.id.layoutTop).setBackgroundColor(ThemeManager.getInstance().getThemeAll().getThemeColor().getColorWhole());
        textToMap.setTextColor(ThemeManager.getInstance().getThemeAll().getThemeColor().getColorNavibarText());
        textSearchCancel.setTextColor(ThemeManager.getInstance().getThemeAll().getThemeColor().getColorNavibarText());
        //搜索图标
        String url = ThemeManager.getInstance().getThemeAll().getThemeColor().getMagnifyGlass();
        if (!TextUtils.isEmpty(url)) {
            GlideApp.with(AllOnlineApp.mApp).load(url).into(new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                    if (resource == null) {
                        return;
                    }
                    editText.setCompoundDrawables(resource, null, getResources().getDrawable(R.drawable.search_delete), null);
                }
            });
        }
        //搜索框颜色
        editText.setBackgroundColor(ThemeManager.getInstance().getThemeAll().getThemeColor().getColorSearchBarBg());

        mDeviceListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                if(i == AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
                    getAddress(mDeviceListView.getFirstVisiblePosition() - 1, mDeviceListView.getLastVisiblePosition());
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });
    }

    private void setTextBG(TextView text) {
        if (text == null) {
            return;
        }
        if (text.isSelected()) {
            ViewUtil.setBg(text, ThemeManager.getInstance().getBGColorDrawable(this));
        } else {
            text.setBackgroundColor(getResources().getColor(R.color.white));
        }
    }

    private void cancelSearch() {
        hideKeyboard();
        textSearchCancel.setVisibility(View.GONE);
        textToMap.setVisibility(View.VISIBLE);
        listViewHistory.setVisibility(View.GONE);

        editText.setText("");

        //取消搜索则直接刷新列表恢复到之前的设备状态。
        isSearching = false;
        updateListData(false);
        startPolling();
    }

    private class HandlerEx extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_MONITOR:
                    getDeviceList(false);
                    startPolling();
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    private void startPolling() {
        stopPolling();
        if (mHandler != null) {
            mHandler.sendEmptyMessageDelayed(MSG_MONITOR, 15000);
        }
    }

    private void stopPolling() {
        if (mHandler != null && mHandler.hasMessages(MSG_MONITOR)) {
            mHandler.removeMessages(MSG_MONITOR);
        }
    }

    private void updateListData(boolean bFirst) {
        if (isSearching) {
            return;
        }
        ArrayList<DeviceInfo> listOnline = deviceManager.getOnlineDeviceList();
        ArrayList<DeviceInfo> listOffline = deviceManager.getOffLineDeviceList();
        ArrayList<DeviceInfo> listUnuse = deviceManager.getUnuseDeviceList();

        setSearchAndAccountVisual();

        textOnline.setText(getString(R.string.filter_online, listOnline.size()));
        textOffline.setText(getString(R.string.filter_offline, listOffline.size()));
        textUnuse.setText(getString(R.string.filter_invalid, listUnuse.size()));

        ArrayList<DeviceInfo> listDevices = new ArrayList<DeviceInfo>();
        if (textOnline.isSelected()) {
            listDevices = listOnline;
        } else if (textOffline.isSelected()) {
            listDevices = listOffline;
        } else if (textUnuse.isSelected()) {
            listDevices = listUnuse;
        } else {
            listDevices = deviceManager.getCurrentAccountDeviceList();
            if (bFirst && listDevices.size() <= 0) {
                //第一次进入,如果没有数据则新请求，需要弹窗提示等待
                getDeviceList(true);
            }
        }
        listNows = listDevices;

        if (mDeviceListAdapter == null) {
            mDeviceListAdapter = new DeviceListAdapter(this);
            mDeviceListView.setAdapter(mDeviceListAdapter);
        }
        mDeviceListAdapter.setData(listDevices);

        if (mDeviceListAdapter.getGroupCount() == 1) {
            mDeviceListView.expandGroup(0);
        }

        if(bFirst) {
            getAddress(0, 7);
        }
    }

    private void updateSearchedListData() {
        ArrayList<DeviceInfo> listOnline = deviceManager.getOnlineDeviceList(listSearched);
        ArrayList<DeviceInfo> listOffline = deviceManager.getOffLineDeviceList(listSearched);
        ArrayList<DeviceInfo> listUnuse = deviceManager.getUnuseDeviceList(listSearched);

        textOnline.setText(getString(R.string.filter_online, listOnline.size()));
        textOffline.setText(getString(R.string.filter_offline, listOffline.size()));
        textUnuse.setText(getString(R.string.filter_invalid, listUnuse.size()));

        ArrayList<DeviceInfo> listEnd = new ArrayList<DeviceInfo>();
        if (textOnline.isSelected()) {
            listEnd = listOnline;
        } else if (textOffline.isSelected()) {
            listEnd = listOffline;
        } else if (textUnuse.isSelected()) {
            listEnd = listUnuse;
        } else {
            listEnd = listSearched;
        }

        if (mDeviceListAdapter == null) {
            mDeviceListAdapter = new DeviceListAdapter(this);
            mDeviceListView.setAdapter(mDeviceListAdapter);
        }
        mDeviceListAdapter.setData(listEnd);

        if (mDeviceListAdapter.getGroupCount() == 1) {
            mDeviceListView.expandGroup(0);
        }
    }

    private void getDeviceList(final boolean bShowWait) {
        if (bShowWait) {
            showLoading(getString(R.string.please_wait));
        }
        Disposable d = DataEngine.getAllMainApi()
                .getDeviceList(GlobalParam.getInstance().getCommonParas(), AllOnlineApp.sAccount,
                        AllOnlineApp.sTarget, AllOnlineApp.sToken.access_token)
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespDeviceList>() {
                    @Override
                    public void onNext(RespDeviceList respDeviceList) {
                        if (bShowWait) {
                            hideLoading();
                        }
                        deviceManager.setCurrentAccountDeviceList(respDeviceList.getData());

                        updateListData(false);

                        if (isSearching) {
                            //跳转到地图，所以不需要批量请求地址数据
                            isSearching = false;
                            backToMapWithDevice(searchedClickDevice);
                        }if(bShowWait){
                            getAddress(0, 7);
                        }
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        if (bShowWait) {
                            hideLoading();
                            showToast(e.getErrMessage());
                        }
                    }
                });
        subscribeRx(d);
    }

    private void setSearchAndAccountVisual(){
        if(!AllOnlineApp.bMainHasChild){
            //无子账号,不显示账户
            textAccount.setVisibility(View.GONE);
            //无子账户的时候，当前账户就是唯一账户，所以判断当前账户下的设备数量
            if(deviceManager.getCurrentAccountDeviceList().size() < MAX_DEVS){
                //无子账且设备少于MAX_DEVS台，不显示搜索。
                editText.setVisibility(View.GONE);
            }else{
                editText.setVisibility(View.VISIBLE);
            }
        }else{
            textAccount.setVisibility(View.VISIBLE);
            editText.setVisibility(View.VISIBLE);
        }
    }

    private void getAddressFromServer(String data){
        if(TextUtils.isEmpty(data)){
            return;
        }
        Disposable d = DataEngine.getAllMainApi()
                .getBatchAddress(GlobalParam.getInstance().getCommonParas(), AllOnlineApp.sAccount, AllOnlineApp.sToken.access_token, data)
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespBatchAddress>() {
                    @Override
                    public void onNext(RespBatchAddress respBatchAddress) {
                        if (respBatchAddress == null) {
                            return;
                        }
                        ArrayList<RespBatchAddress.BatchAddress> listAddress = respBatchAddress.getData();
                        if (listAddress != null && listAddress.size() > 0) {
                            for (RespBatchAddress.BatchAddress batchAddress : listAddress) {
                                if (batchAddress != null) {
                                    deviceManager.setCachedAddress(batchAddress.getLat(), batchAddress.getLng(), batchAddress.getAddress());
                                }
                            }
                            if (mDeviceListAdapter != null) {
                                mDeviceListAdapter.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {

                    }
                });
        subscribeRx(d);
    }

    /*****列表滑动后再获取当前屏幕范围内的设备地址**/
    private void getAddress(int iStart, int iEnd){
        List<DeviceInfo> list = mDeviceListAdapter.getData();
        if(list != null && list.size() > 0){
            if(iStart < 0){
                iStart = 0;
            }
            if(iEnd < 0 || iEnd > list.size()){
                iEnd = list.size();
            }
            StringBuilder stringBuilder = new StringBuilder();
            for(int i = iStart; i < iEnd; i++){
                DeviceInfo device = list.get(i);
                if(device != null && device.getState() != DeviceInfo.STATE_DISABLE && device.getState() != DeviceInfo.STATE_EXPIRE){
                    if (TextUtils.isEmpty(deviceManager.getCachedAddress(device.getLat(), device.getLng()))) {
                        //有效的设备中，当前经纬度没有地址信息的
                        stringBuilder.append(device.getImei());
                        stringBuilder.append("|");
                        stringBuilder.append(device.getLng());
                        stringBuilder.append("|");
                        stringBuilder.append(device.getLat());
                        stringBuilder.append(",");
                    }
                }
            }
            if(stringBuilder.length() > 0) {
                getAddressFromServer(stringBuilder.toString());
            }
        }
    }

    private void getAccountList() {
        showLoading(getString(R.string.please_wait));
        Disposable d = DataEngine.getAllMainApi()
            .getAccountList(GlobalParam.getInstance().getCommonParas(), AllOnlineApp.sAccount, AllOnlineApp.sTarget,
                AllOnlineApp.sToken.access_token)
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespAccountGroupInfo>() {
                @Override
                public void onNext(RespAccountGroupInfo respAccountGroupInfo) {
                    hideLoading();
                    if (respAccountGroupInfo.getData() != null) {
                        AllOnlineApp.customerId = respAccountGroupInfo.getData().getCustomer_id();
                        deviceManager.addGroupInMap(respAccountGroupInfo.getData().getGroup());
                        deviceManager.addSubaccountsList(respAccountGroupInfo.getData().getChildren(),
                            String.valueOf(parentId));
                        updateSubcountDataAndView(true);
                        updateListData(false);
                    }
                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    hideLoading();
                    showToast(e.getErrMessage());
                }
            });
        subscribeRx(d);
    }

    private void expandAllGroups() {
        // 展开列表项
        int groupCount = mDeviceListAdapter.getGroupCount();
        for (int i = 0; i < groupCount; i++) {
            mDeviceListView.expandGroup(i);
        }
    }

    private void showInputHistory() {
        listViewHistory.setVisibility(View.VISIBLE);
        listHistory = SettingDataManager.getInstance(this).getSearchHistory();
        historyAdapter = new HistoryAdapter();
        listViewHistory.setAdapter(historyAdapter);
        listViewHistory.setOnItemClickListener((adapterView, view, i, l) -> {
            if (i >= 0 && i < listHistory.size()) {
                editText.setText(listHistory.get(i));
                searchClickedAction();
            }
        });
    }

    private void initTreeView() {
        if (popupWinCarTreeView == null) {
            //汽车子账号树列表
            layoutCarTreeview = LayoutInflater.from(this).inflate(R.layout.view_car_tree_list, null);
            listViewCounts = (ListView) layoutCarTreeview.findViewById(R.id.lvCarlistTree);
            layoutCarTreeviewFooter = layoutCarTreeview.findViewById(R.id.footer);
            layoutCarTreeviewFooter.setOnClickListener(v -> hideTreeView());

            popupWinCarTreeView = new PopupWindow(layoutCarTreeview, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            PopupWindowUtil.fixPopupWindow(popupWinCarTreeView);
            popupWinCarTreeView.setFocusable(true);
            popupWinCarTreeView.setOutsideTouchable(true);
            popupWinCarTreeView.setOnDismissListener(() -> CommonUtil.setTextDrawable(this, textAccount, R.drawable.down_icon, CommonUtil.DRAWABLE_RIGHT));
        }
    }

    private void showTreeView() {
        initTreeView();

        updateSubcountDataAndView(true);

        CommonUtil.setTextDrawable(this, textAccount, R.drawable.up_icon, CommonUtil.DRAWABLE_RIGHT);

        int offset = (int) (this.getResources().getDisplayMetrics().density * 2);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            int[] a = new int[2];
            textAccount.getLocationOnScreen(a);
            offset= offset + a[1] + textAccount.getHeight();
            if (Build.VERSION.SDK_INT == 25) {
                //【note!】Gets the screen height without the virtual key
                WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
                int screenHeight = wm.getDefaultDisplay().getHeight();
                popupWinCarTreeView.setHeight(screenHeight - offset);
            }
            popupWinCarTreeView.showAtLocation(textAccount, Gravity.NO_GRAVITY, 0, offset);
        } else {
            popupWinCarTreeView.showAsDropDown(textAccount, 0, offset);
        }
    }

    private void hideTreeView() {
        if (popupWinCarTreeView != null) {
            popupWinCarTreeView.dismiss();
        }
    }

    private void searchInLocal(String content) {
        if(listNows == null || listNows.size() <= 0){
            return;
        }
        if(!TextUtils.isEmpty(content)){
            isSearching = true;
            stopPolling();
            ArrayList<DeviceInfo> findDevices = new ArrayList<DeviceInfo>();
            for (DeviceInfo device : listNows) {
                if (device == null) {
                    continue;
                }
                if (device.getImei().toLowerCase().contains(content.toLowerCase())
                        || device.getName().toLowerCase().contains(content.toLowerCase())) {
                    findDevices.add(device);
                }
            }

            // 直接显示在原来的列表里面
            mDeviceListAdapter.setData(findDevices);
            mDeviceListAdapter.notifyDataSetChanged();
            if (findDevices.size() > 0) {
                // 展开列表项
                int groupCount = mDeviceListAdapter.getGroupCount();
                for (int i = 0; i < groupCount; i++) {
                    mDeviceListView.expandGroup(i);
                }
            }
        }
        else {
            //输入为空，不再搜索
            isSearching = false;
            updateListData(false);
            startPolling();
            int groupCount = mDeviceListAdapter.getGroupCount();
            for (int i = 0; i < groupCount; i++) {
                mDeviceListView.expandGroup(i);
            }
        }
    }

    private void searchClickedAction() {
        // 隐藏键盘
        hideKeyboard();
        listViewHistory.setVisibility(View.GONE);

        String searchContext = editText.getText().toString().trim();
        if (!TextUtils.isEmpty(searchContext)) {
            if (listHistory == null) {
                listHistory = new ArrayList<String>();
            }
            if (listHistory.contains(searchContext)) {
                listHistory.remove(searchContext);
            }
            while (listHistory.size() >= 10) {
                listHistory.remove(listHistory.size() - 1);
            }
            listHistory.add(0, searchContext);
            if (historyAdapter != null) {
                historyAdapter.notifyDataSetChanged();
            }
            SettingDataManager.getInstance(AllListActivity.this).saveSearchHistory(listHistory);

            isSearching = true;
            stopPolling();
            searchOnServer(searchContext);
        }
    }

    private void searchOnServer(String content) {
        if (TextUtils.isEmpty(content)) {
            return;
        }
        showLoading(getString(R.string.please_wait));
        Disposable d = DataEngine.getAllMainApi()
                .searchDeviceByKey(GlobalParam.getInstance().getCommonParas(), AllOnlineApp.sAccount,
                        AllOnlineApp.sTarget, AllOnlineApp.sToken.access_token, content, 1)
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespDeviceList>() {
                    @Override
                    public void onNext(RespDeviceList response) {
                        hideLoading();
                        if (response.getData() != null) {
                            textOnline.setSelected(false);
                            textOffline.setSelected(false);
                            textUnuse.setSelected(false);
                            listSearched = response.getData();
                            if(listSearched == null || listSearched.size() < 0){
                                showToast(getString(R.string.no_result));
                            }
                            updateSearchedListData();
                        }
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        hideLoading();
                    }
                });
        subscribeRx(d);
    }

    @Override
    public void onBackPressed() {
        if (textSearchCancel.getVisibility() == View.VISIBLE) {
            cancelSearch();
            return;
        }
        super.onBackPressed();
    }

    private void hideKeyboard() {
        if (editText == null) {
            return;
        }
        try {
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm == null) {
                return;
            }
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        } catch (Exception e) {
        }
    }

    private void setCurrentAccountName(String title) {
        textAccount.setText(title);
    }

    private void updateSubcountDataAndView(boolean bDataChange) {
        if (bDataChange) {
            // 初始化
            listSubcountsData.clear();
            ArrayList<SubAccount> listAccounts = deviceManager.getSubaccountsList();
            listSubcountsData.add(new FileBean(0, 0, AllOnlineApp.sAccount, AllOnlineApp.sToken.name, listAccounts.size() > 0));
            if (!listAccounts.isEmpty()) {
                for (int i = 0; i < listAccounts.size(); i++) {
                    String id = listAccounts.get(i).id;
                    String pid = listAccounts.get(i).pid;
                    String name = listAccounts.get(i).name;
                    String showname = listAccounts.get(i).showname;
                    int idInt = Integer.valueOf(id);
                    int pidInt = Integer.valueOf(pid);
                    listSubcountsData.add(new FileBean(idInt, pidInt, name, showname, listAccounts.get(i).haschild));
                }
            }
        }
        if (subAccountTreeAdapter == null) {
            subAccountTreeAdapter = new SimpleTreeAdapter<FileBean>(listViewCounts, this, listSubcountsData, 10);
            subAccountTreeAdapter.setNodeChecked(checkedPositon);
            subAccountTreeAdapter.setOnTreeNodeClickListener(new OnTreeNodeClickListener() {
                @Override
                public void onClick(Node node, int position, boolean bShow) {
                    isSubCountChanged = (parentId != node.getId());
                    checkedPositon = node.getId();
                    parentId = node.getId();

                    if (isSubCountChanged) {
                        SubAccount tempSubaccount = new SubAccount();
                        tempSubaccount.id = String.valueOf(parentId);
                        tempSubaccount.pid = String.valueOf(node.getpId());
                        tempSubaccount.name = node.getName();
                        tempSubaccount.showname = node.getShowName();
                        tempSubaccount.haschild = node.isHaschild();
                        deviceManager.setCurrentSubaccount(tempSubaccount);
                        if(!TextUtils.isEmpty(tempSubaccount.name)) {
                            AllOnlineApp.sTarget = tempSubaccount.name;
                        }

                        setCurrentAccountName(tempSubaccount.showname);
                        subAccountTreeAdapter.setNodeChecked(checkedPositon);
                    } else {
                        hideTreeView();
                        return;
                    }

                    if (deviceManager.isContainsDeviceByAccount(String.valueOf(parentId))) {
                        //如果存在缓存数据则直接先用缓存数据更新设备列表.
                        hideTreeView();
                        updateListData(false);
                    } else {
                        getDeviceList(true);
                    }

                    if (node.isHaschild()) {
                        //如果当前账户还有子账户，则请求子账户信息.继续显示账户窗口
                        getAccountList();
                    } else {
                        hideTreeView();
                    }
                }
            });
            listViewCounts.setAdapter(subAccountTreeAdapter);
            int selcetedPosition = subAccountTreeAdapter.getItemSelectedPosition(checkedPositon);
            if (selcetedPosition > 0) {
                listViewCounts.setSelection(selcetedPosition);
            }
        } else {
            if (bDataChange) {
                try {
                    subAccountTreeAdapter.setData(listSubcountsData, 10, parentId);
                    subAccountTreeAdapter.setNodeChecked(checkedPositon);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private boolean bFirstResume = true;

    @Override
    public void onResume() {
        super.onResume();
        if (!isSearching) {
            startPolling();
        }
        if (!bFirstResume) {
            updateListData(false);
        }
        bFirstResume = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        hideKeyboard();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopPolling();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        if (mDeviceListAdapter != null) {
            DeviceInfo deviceInfo = mDeviceListAdapter.getChild(groupPosition, childPosition);
            if (deviceInfo != null) {
                //未启用、过期的也跳转道信息
                if (deviceInfo.getState() == DeviceInfo.STATE_DISABLE || deviceInfo.getState() == DeviceInfo.STATE_EXPIRE) {
                    jumpToDetail(deviceInfo);
                    return true;
                }
                boolean latBflag = deviceInfo.getLat() > 90 || deviceInfo.getLat() < -90;
                boolean lngBFlag = deviceInfo.getLng() > 180 || deviceInfo.getLng() < -180;
                if (latBflag || lngBFlag) {
                    //该车经纬度无效
                    showToast(getString(R.string.car_state_latlng));
                    jumpToDetail(deviceInfo);
                    return true;
                }

                if (isSearching) {
                    if(!TextUtils.isEmpty(deviceInfo.getCustomer_name())) {
                        parentId = deviceInfo.getCustomer_id();

                        //本地搜索中没有customer_name，只有网络搜索后才有customer_name。所以本地搜索不需要改变账户信息以及设备信息
                        AllOnlineApp.sTarget = deviceInfo.getCustomer_name();

                        SubAccount subAccount = deviceManager.getCurrentSubaccount();
                        //subAccount.id = Integer.toString(deviceInfo.getCustomer_id() == AllOnlineApp.customerId ? 0 : deviceInfo.getCustomer_id());
                        subAccount.id = String.valueOf(deviceInfo.getCustomer_id());
                        subAccount.showname = deviceInfo.getCustomer_showname();
                        subAccount.name = deviceInfo.getCustomer_name();
                        deviceManager.setCurrentSubaccount(subAccount);
                        setCurrentAccountName(subAccount.showname);

                        if (!deviceManager.isContainsDeviceByAccount(String.valueOf(deviceInfo.getCustomer_id()))) {
                            //如果本地没有设备列表，需要先去请求到该账户的设备列表，然后再跳转
                            searchedClickDevice = deviceInfo;
                            getDeviceList(true);
                            return true;
                        }
                    }
                }
                backToMapWithDevice(deviceInfo);
            }
        }

        return true;
    }

    private void backToMapWithDevice(DeviceInfo deviceInfo) {
        if (deviceInfo == null) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(Constant.KEY_DEVICE, deviceInfo);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void jumpToDetail(DeviceInfo deviceInfo) {
        Intent intentDetail = new Intent(this, DeviceDetailInfoActivity.class);
        intentDetail.putExtra(DeviceDetailInfoActivity.DEV_IMEI, deviceInfo.getImei());
        intentDetail.putExtra(DeviceDetailInfoActivity.DEV_ANGLE, deviceInfo.getInstallAngle());
        startActivity(intentDetail);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public ObservableListView getmDeviceListView() {
        return mDeviceListView;
    }

    class HistoryAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return listHistory == null ? 0 : listHistory.size();
        }

        @Override
        public String getItem(int i) {
            if (i < 0 || i > getCount() - 1) {
                return null;
            }
            return listHistory.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder = null;
            if (view == null) {
                viewHolder = new ViewHolder();
                view = LayoutInflater.from(AllListActivity.this).inflate(R.layout.search_history_item, null);
                viewHolder.text = (TextView) view.findViewById(R.id.textViewName);
                viewHolder.image = (ImageView) view.findViewById(R.id.imageViewDel);
                viewHolder.line = view.findViewById(R.id.lineView);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            String name = getItem(i);
            if (!TextUtils.isEmpty(name)) {
                viewHolder.text.setText(name);
                viewHolder.text.setVisibility(View.VISIBLE);
                viewHolder.image.setVisibility(View.VISIBLE);
            } else {
                viewHolder.text.setText("");
                viewHolder.text.setVisibility(View.INVISIBLE);
                viewHolder.image.setVisibility(View.INVISIBLE);
            }
            if (listHistory.size() - 1 == i) {
                viewHolder.line.setVisibility(View.GONE);
            } else {
                viewHolder.line.setVisibility(View.VISIBLE);
            }

            viewHolder.image.setOnClickListener(view1 -> {
                if (i >= 0 && i < listHistory.size()) {
                    listHistory.remove(i);
                    notifyDataSetChanged();
                    SettingDataManager.getInstance(AllListActivity.this).saveSearchHistory(listHistory);
                }
            });

            return view;
        }
    }

    class ViewHolder {
        TextView text;
        ImageView image;
        View line;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                hideKeyboard();
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
}
