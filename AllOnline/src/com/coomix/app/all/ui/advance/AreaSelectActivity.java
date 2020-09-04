package com.coomix.app.all.ui.advance;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.model.bean.AreaFence;
import com.coomix.app.all.model.bean.City;
import com.coomix.app.all.model.bean.DeviceInfo;
import com.coomix.app.all.model.bean.Province;
import com.coomix.app.all.model.response.RespAreaFence;
import com.coomix.app.all.model.response.RespCityList;
import com.coomix.app.all.model.response.RespProvinceList;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.all.widget.SegmentControl;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;

public class AreaSelectActivity extends BaseActivity {
    private SegmentControl mSegment;
    private ListView mListView;
    private ListAdapter mAdapter;
    private ArrayList<Province> mProvinces;

    private AreaType mAreaType;
    private int mSelectProvince = -1;
    private int mSelectCity;
    private AreaType mShowType;
    private boolean isFromItem;
    private AreaFence mAreaFence;
    private DeviceInfo mDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_area_select);

        mAreaType = AreaType.CITY;
        mShowType = AreaType.PROVINCE;

        mDevice = (DeviceInfo) getIntent().getSerializableExtra(Constant.KEY_DEVICE);
        isFromItem = getIntent().getBooleanExtra(DeviceSettingActivity.IS_FROM_ITEM, true);
        mAreaFence = (AreaFence) getIntent().getSerializableExtra(DeviceSettingActivity.AREA_FENCE);

        initView();

        getProvincesList();
    }

    void initView() {
        MyActionbar actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, R.string.bottom_action_item_title_mine, 0, 0);
        // 标题
        if (mAreaFence != null && !TextUtils.isEmpty(mAreaFence.id) && mAreaFence.validate_flag == 1) {
            if (!TextUtils.isEmpty(mAreaFence.getShape_param().city)) {
                actionbar.setTitle("当前区域(" + mAreaFence.getShape_param().city + ")");
            } else if (!TextUtils.isEmpty(mAreaFence.getShape_param().province)) {

                actionbar.setTitle("当前区域(" + mAreaFence.getShape_param().province + ")");
            } else {
                actionbar.setTitle("区域选择");
            }
        } else {
            actionbar.setTitle("区域选择");
        }

        // 返回按钮
        actionbar.setLeftImageClickListener(view -> onBackPressed());

        mSegment = (SegmentControl) findViewById(R.id.sc_area);
        mSegment.setOnSegmentControlClickListener(new SegmentControl.OnSegmentControlClickListener() {
            @Override
            public void onSegmentControlClick(int index) {
                if (index == 0) {
                    mAreaType = AreaType.CITY;
                    mShowType = AreaType.PROVINCE;
                    mAdapter.notifyDataSetChanged();
                } else if (index == 1) {
                    mAreaType = AreaType.PROVINCE;
                    mShowType = AreaType.PROVINCE;
                    mAdapter.notifyDataSetChanged();
                }
            }
        });

        mListView = (ListView) findViewById(R.id.lv_list);
        mAdapter = new ListAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                if (mAreaType == AreaType.PROVINCE) {
                    if (mShowType == AreaType.PROVINCE) {
                        mSelectProvince = position;
                        Province province = mProvinces.get(position);
                        if(province != null) {
                            setAreaFence(province.id);
                        }
                    }
                    // 已选择
                } else if (mAreaType == AreaType.CITY) {
                    if (mShowType == AreaType.PROVINCE) {
                        mSelectProvince = position;
                        mShowType = AreaType.CITY;
                        Province province = mProvinces.get(position);
                        if (province.cities == null) {
                            // 获取城市列表
                            getCityList(province.id);
                        } else {
                            mAdapter.notifyDataSetChanged();
                        }
                    } else if (mShowType == AreaType.CITY) {
                        mSelectCity = position;
                        Province province = mProvinces.get(mSelectProvince);
                        if (province.cities != null) {
                            City city = province.cities.get(position);
                            if (city != null) {
                                setAreaFence(city.id);
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        // super.onBackPressed();

        Intent intent = new Intent();
        intent.putExtra(DeviceSettingActivity.IS_FROM_ITEM, isFromItem);
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void getProvincesList() {
        showProgressDialog(getString(R.string.please_wait));
        Disposable disposable = DataEngine.getAllMainApi()
                .getProvincesList(GlobalParam.getInstance().getCommonParas(),
                        AllOnlineApp.sAccount,
                    GlobalParam.getInstance().getAccessToken())
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespProvinceList>() {
                    @Override
                    public void onNext(RespProvinceList respProvinceList) {
                        hideLoading();
                        mProvinces = respProvinceList.getData();
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        showToast(e.getErrCodeMessage());
                        hideLoading();
                    }
                });
        subscribeRx(disposable);
    }

    private void getCityList(String provinceId) {
        showProgressDialog(getString(R.string.please_wait));
        Disposable disposable = DataEngine.getAllMainApi()
                .getCityList(GlobalParam.getInstance().getCommonParas(),
                        AllOnlineApp.sAccount,
                    GlobalParam.getInstance().getAccessToken(), provinceId)
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespCityList>() {
                    @Override
                    public void onNext(RespCityList respCityList) {
                        hideLoading();

                        if (mSelectProvince >= 0 && mSelectProvince < mProvinces.size()) {
                            Province province = mProvinces.get(mSelectProvince);
                            province.cities = respCityList.getData();
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        showToast(e.getErrCodeMessage());
                        hideLoading();
                    }
                });
        subscribeRx(disposable);
    }

    private void setAreaFence(String citycode) {
        showProgressDialog(getString(R.string.please_wait));
        Disposable disposable = DataEngine.getAllMainApi()
                .setAreaFence(GlobalParam.getInstance().getCommonParas(),
                        AllOnlineApp.sAccount,
                    GlobalParam.getInstance().getAccessToken(), mDevice.getImei(), citycode, 1)
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespAreaFence>() {
                    @Override
                    public void onNext(RespAreaFence respSetArea) {
                        hideLoading();
                        AreaFence areaFence = respSetArea.getData();
                        areaFence.validate_flag = 1;
                        if (mProvinces != null && mSelectProvince >= 0 && mSelectProvince < mProvinces.size()) {
                            Province province = mProvinces.get(mSelectProvince);
                            if (province != null) {
                                areaFence.getShape_param().province = province.name;
                                if (province.cities != null && mSelectCity >= 0
                                        && mSelectCity < province.cities.size()) {
                                    City city = province.cities.get(mSelectCity);
                                    if (city != null) {
                                        areaFence.getShape_param().city = city.name;
                                    }
                                }
                            }
                        }

                        Intent intent = new Intent();
                        intent.putExtra(DeviceSettingActivity.AREA_FENCE, areaFence);
                        intent.putExtra(DeviceSettingActivity.IS_FROM_ITEM, isFromItem);
                        setResult(RESULT_OK, intent);
                        finish();
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        showToast(e.getErrCodeMessage());
                        hideLoading();
                    }
                });
        subscribeRx(disposable);
    }

    // list adapter
    public class ListAdapter extends BaseAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            if (mShowType == AreaType.PROVINCE) {
                if (mProvinces != null) {
                    return mProvinces.size();
                }
            } else if (mShowType == AreaType.CITY) {
                if (mSelectProvince >= 0 && mSelectProvince < mProvinces.size()) {
                    Province province = mProvinces.get(mSelectProvince);
                    if (province != null && province.cities != null) {
                        return province.cities.size();
                    }
                }
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mShowType == AreaType.PROVINCE) {
                if (mProvinces != null && position >= 0 && position < mProvinces.size()) {
                    return mProvinces.get(position);
                }
            } else if (mShowType == AreaType.CITY) {
                if (mSelectProvince >= 0 && mSelectProvince < mProvinces.size()) {
                    Province province = mProvinces.get(mSelectProvince);
                    if (province != null && province.cities != null) {
                        return province.cities.get(position);
                    }
                }
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_area, null);
                viewHolder = new ViewHolder();
                viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
                viewHolder.ivNext = (ImageView) convertView.findViewById(R.id.iv_next);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if (mShowType == AreaType.PROVINCE) {
                if (position >= 0 && position < mProvinces.size()) {
                    Province province = mProvinces.get(position);
                    if (province != null) {
                        viewHolder.tvName.setText(province.name);
                    }
                }
            } else if (mShowType == AreaType.CITY) {
                if (mSelectProvince >= 0 && mSelectProvince < mProvinces.size()) {
                    Province province = mProvinces.get(mSelectProvince);
                    if (province != null && province.cities != null) {
                        City city = province.cities.get(position);
                        if (city != null) {
                            viewHolder.tvName.setText(city.name);
                        }
                    }
                }
            }

            if (mAreaType == AreaType.PROVINCE) {
                viewHolder.ivNext.setVisibility(View.INVISIBLE);
            } else if (mAreaType == AreaType.CITY) {
                if (mShowType == AreaType.PROVINCE) {
                    viewHolder.ivNext.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.ivNext.setVisibility(View.INVISIBLE);
                }
            }

            return convertView;
        }
    }

    /* 存放控件 */
    public class ViewHolder {
        public TextView tvName;
        public ImageView ivNext;
    }

    public enum AreaType {
        PROVINCE, CITY
    }

}
