package com.coomix.app.all.ui.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.OfflineCityItem;
import com.coomix.app.all.service.OfflineMapService;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.widget.ClearEditView;
import com.coomix.app.all.widget.ExpandableListAdapter;
import com.coomix.app.all.widget.ExpandableListAdapter.DownloadClickListener;
import com.coomix.app.all.widget.ListViewUpdateAdapter;
import com.coomix.app.all.widget.ListViewUpdateAdapter.UpdateListener;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.framework.app.BaseService.ServiceCallback;
import com.coomix.app.framework.app.Result;
import com.coomix.app.framework.util.PreferenceUtil;
import java.util.ArrayList;

/**
 * 百度离线地图显示页面
 * 
 * @author goome
 *
 */
public class OfflineBMapActivity extends BaseActivity implements UpdateListener, ServiceCallback {
    private ExpandableListAdapter mExpListAdapter;
    private ExpandableListView mExpListView;

    private ListView mListViewUpdate; // 下载管理
    private ListViewUpdateAdapter mListViewUpdateAdapter;
    private Button updateAllBtn;
    private Handler myHandler = new Handler();

    private ListView mListViewSearch;
    private SearchCityAdapter mSearchAdapter;
    private ClearEditView mClearEditViewSearch;

    private RelativeLayout mRelativeLayoutCityList;

    private ArrayList<OfflineCityItem> mArrayListOfflineItem;
    private SparseArray<OfflineCityItem> mSparseArrayOfflineItem;
    private ArrayList<MKOLUpdateElement> mArrayListUpdates;

    private OfflineMapService mBoundService;
    private boolean mIsFormHome;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((OfflineMapService.LocalBinder) service).getService();
            serviceReady();
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
        }
    };

    private void serviceReady() {
        mBoundService.registerServiceCallback(this);
        updateViews();
    }

    @Override
    public void callback(int messageId, Result result) {
        if (messageId == OfflineMapService.UPDATE_CITY_LIST) {
            updateViews();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);

        PreferenceUtil.init(this);

        findViewById();
        initView();
        setListener();

        // 判断SD卡是否存在
        Boolean isSDPresent = android.os.Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);
        if (isSDPresent) {
            Intent data = new Intent(this, OfflineMapService.class);
            startService(data);
            bindService(new Intent(this, OfflineMapService.class), mConnection, Context.BIND_AUTO_CREATE);
        } else {
            Toast.makeText(this, R.string.sdcard_not_exist, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void findViewById() {
        mExpListView = (ExpandableListView) findViewById(R.id.lvExp);
        mListViewUpdate = (ListView) findViewById(R.id.lv_update);
        mListViewSearch = (ListView) findViewById(R.id.lv_search);
        mClearEditViewSearch = (ClearEditView) findViewById(R.id.cte_search);
        mRelativeLayoutCityList = (RelativeLayout) findViewById(R.id.rl_city_list);
        updateAllBtn = (Button) findViewById(R.id.update_all_btn);
    }

    private void initView() {
        mIsFormHome = getIntent().getBooleanExtra("FROM", false);
        mArrayListOfflineItem = new ArrayList<OfflineCityItem>();
        mArrayListUpdates = new ArrayList<MKOLUpdateElement>();
        mSparseArrayOfflineItem = new SparseArray<OfflineCityItem>();

        mExpListView.setGroupIndicator(null);
        mExpListAdapter = new ExpandableListAdapter(this, mArrayListOfflineItem);
        mExpListView.setAdapter(mExpListAdapter);

        mListViewUpdateAdapter = new ListViewUpdateAdapter(this, mArrayListUpdates);
        mListViewUpdate.setAdapter(mListViewUpdateAdapter);

        mSearchAdapter = new SearchCityAdapter(this);
        mListViewSearch.setAdapter(mSearchAdapter);

        MyActionbar actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, "", 0, 0);
        actionbar.getRadioGroup().setVisibility(View.VISIBLE);
        // 列表切换事件
        actionbar.getRadioGroup().setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                checkListDisplay(checkedId);
            }
        });
        actionbar.setCheckRight();
        if (mIsFormHome) {
            myHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    actionbar.setCheckLeft();
                    if (AllOnlineApp.sHasUpdate) {
                        updateAllBtn.setVisibility(View.VISIBLE);
                    }
                }
            }, 400);
        }
    }

    private void setListener() {
        // 全部更新
        updateAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mArrayListUpdates != null && mArrayListUpdates.size() > 0) {
                    for (MKOLUpdateElement mk : mArrayListUpdates) {
                        if (mk.update) {
                            // System.out.println("下载更新数据城市id-> 更新吗 ++ "+
                            // mk.cityName );
                            update(mk.cityID);
                            mListViewUpdateAdapter.notifyDataSetChanged();
                        } else {
                            // System.out.println("下载更新数据城市id-> 直接使用的 ++ "+
                            // mk.cityName );
                        }
                    }

                    updateAllBtn.setEnabled(false);

                }

            }
        });

        mListViewUpdateAdapter.setUpdateListener(this);
        mExpListAdapter.setDownloadClickListener(new DownloadClickListener() {

            @Override
            public void download(int cityId) {
                OfflineCityItem item = mSparseArrayOfflineItem.get(cityId);
                // Log.e("FDEBUG", "download "+item.toString());
                if (item != null
                        && (item.cityType == OfflineCityItem.CITY || item.cityType == OfflineCityItem.COUNTRY)) {
                    if (mBoundService != null) {
                        mBoundService.startDownload(item);
                    }
                }
            }
        });

        mListViewUpdate.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((ListViewUpdateAdapter) (mListViewUpdate.getAdapter())).toggle(position);
            }
        });

        // 输入框的监听事件
        mClearEditViewSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && mSparseArrayOfflineItem != null && mSparseArrayOfflineItem.size() > 0) {
                    ArrayList<OfflineCityItem> finded = new ArrayList<OfflineCityItem>();

                    for (int i = 0; i < mSparseArrayOfflineItem.size(); i++) {
                        OfflineCityItem value = mSparseArrayOfflineItem.valueAt(i);
                        if (value != null) {
                            if (value.cityName.contains(s.toString().toLowerCase())) {
                                finded.add(value);
                            }
                        }
                    }
                    mSearchAdapter.setData(finded);
                    if (finded.size() < 1) {
                        mExpListView.setVisibility(View.VISIBLE);
                        mListViewSearch.setVisibility(View.GONE);
                    } else {
                        mExpListView.setVisibility(View.GONE);
                        mListViewSearch.setVisibility(View.VISIBLE);
                    }
                } else {
                    mExpListView.setVisibility(View.VISIBLE);
                    mListViewSearch.setVisibility(View.GONE);
                    mSearchAdapter.setData(null);
                }
            }
        });

        mListViewSearch.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (id != -1) {
                    int cityId = (int) id;
                    OfflineCityItem item = mSparseArrayOfflineItem.get(cityId);
                    if (item != null) {
                        if (mBoundService != null) {
                            mBoundService.startDownload(item);
                        }
                    }
                }
            }
        });
    }

    private void checkListDisplay(int checkedId) {

        if (checkedId == R.id.titlebar_rbtn_left) {
            mRelativeLayoutCityList.setVisibility(View.GONE);
            mListViewUpdate.setVisibility(View.VISIBLE); // 下载管理

            if (mArrayListUpdates != null && mArrayListUpdates.size() > 0) {
                for (MKOLUpdateElement mk : mArrayListUpdates) {
                    if (mk.update) {
                        updateAllBtn.setVisibility(View.VISIBLE);
                        break;
                    }
                }
            }

        } else {
            updateAllBtn.setVisibility(View.GONE);
            mRelativeLayoutCityList.setVisibility(View.VISIBLE);
            mListViewUpdate.setVisibility(View.GONE);

            updateAllBtn.setVisibility(View.GONE);

        }
    }

    private void updateViews() {
        mArrayListOfflineItem = mBoundService.getCityArrayList();
        mSparseArrayOfflineItem = mBoundService.getCitySparseArray();
        mArrayListUpdates = mBoundService.getUpdateCityData();
        mListViewUpdateAdapter.setData(mArrayListUpdates);
        mExpListAdapter.setData(mArrayListOfflineItem);

        if (mListViewSearch.getVisibility() == View.VISIBLE) {
            mSearchAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        if (mBoundService != null) {
            mBoundService.unregisterServiceCallback();
            unbindService(mConnection);
        }
        super.onDestroy();
    }

    @Override
    public void start(int cityId) {
        if (mBoundService != null) {
            mBoundService.startDownload(cityId);
        }
    }

    @Override
    public void pause(int cityId) {
        if (mBoundService != null) {
            mBoundService.pauseDownload(cityId);
        }
    }

    @Override
    public void delete(int cityId) {
        mBoundService.deleteCity(cityId);
    }

    @Override
    public void update(int cityId) {
        if (mBoundService != null) {
            mBoundService.updateCity(cityId);
        }
    }
}