package com.coomix.app.all.weizhang.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cheshouye.api.client.WeizhangClient;
import com.cheshouye.api.client.WeizhangIntentService;
import com.cheshouye.api.client.json.CarInfo;
import com.cheshouye.api.client.json.CityInfoJson;
import com.cheshouye.api.client.json.InputConfigJson;
import com.cheshouye.api.client.json.ProvinceInfoJson;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.DeviceInfo;
import com.coomix.app.all.model.bean.WeiZhang;
import com.coomix.app.all.util.StringUtil;
import com.coomix.app.all.widget.ClearEditView;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.all.dialog.ProgressDialogEx;
import com.coomix.app.all.dialog.ProgressDialogEx.OnCancelListener2;
import com.coomix.app.all.ui.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 车牌号，车架号，发动机号 是对应的
 * 
 * @author goome
 * 
 */
public class WeiZhangMainActivity extends BaseActivity {

    private final String defaultStr = "";

    private String defaultChepai = ""; // 粤
    private String temp = "";
    private String ABC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private String numStr = "0123456789";

    private TextView short_name;
    private TextView query_city;
    private View btn_cpsz;
    private Button btn_query;

    private ProgressDialogEx dialog;

    private ClearEditView chepai_number;
    private ClearEditView chejia_number;
    private ClearEditView engine_number;
    private DeviceInfo device;
    private String tempChePai;

    private String chePai = "";

    private ArrayList<WeiZhang> mArrayListWeiZhang;

    private String tempProvince;
    private String tempCName;
    private Handler myHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 100) {
                int cityId = msg.getData().getInt("cityId");
                if (cityId != -1) {
                    setQueryItem(cityId);
                }
            }
        };
    };

    // 行驶证图示
    private View popXSZ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.csy_activity_main);
        // getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.csy_titlebar);
        MyActionbar actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, "车辆违章查询", 0, 0);

        // ********************************************************
        Intent weizhangIntent = new Intent(this, WeizhangIntentService.class);
        weizhangIntent.putExtra("appId", 548);// 您的appId
        weizhangIntent.putExtra("appKey", "b5f6a538cd31d1c7a4ab9e71d1408c19");// 您的appKey
        startService(weizhangIntent);
        // ********************************************************

        // 选择省份缩写
        query_city = (TextView) findViewById(R.id.cx_city);
        chepai_number = (ClearEditView) findViewById(R.id.chepai_number);
        chejia_number = (ClearEditView) findViewById(R.id.chejia_number);
        engine_number = (ClearEditView) findViewById(R.id.engine_number);
        short_name = (TextView) findViewById(R.id.chepai_sz);

        mArrayListWeiZhang = new ArrayList<WeiZhang>();

        try {

            mArrayListWeiZhang.clear();
            mArrayListWeiZhang.addAll(AllOnlineApp.mDb.getWeiZhangListory());

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        // ----------------------------------------------

        btn_cpsz = (View) findViewById(R.id.btn_cpsz);
        btn_query = (Button) findViewById(R.id.btn_query);

        if (getIntent() != null) {
            device = (DeviceInfo) getIntent().getSerializableExtra("weizhang_device");
        }

        // 根据默认查询地城市id, 初始化查询项目
        // setQueryItem(defaultCityId, defaultCityName);
        // short_name.setText(defaultChepai);

        try {

            //tempChePai = device.number;
//            if (device.state != null) {
//                tempProvince = device.getAddress();
//            }
            tempProvince = device.getAddress();

            if (!StringUtil.isTrimEmpty(tempChePai)) {
                defaultChepai = tempChePai.trim().substring(0, 1);
                // chepai_number.setText(tempChePai);
                String secondStr = tempChePai.trim().substring(1, 2);

                String[] dateArr = getDate();

                for (int i = 0; i < dateArr.length; i++) {
                    String dateStr = dateArr[i];
                    if (defaultChepai.trim().equals(dateStr)) {
                        temp = dateStr;

                        if (ABC.contains(secondStr)) {
                            String lastFive = tempChePai.substring(2, 7);
                            // if(numStr.contains(lastFive)){
                            chePai = secondStr + lastFive;
                            // }
                        }

                        break;
                    } else {
                        temp = defaultStr;
                        continue;
                    }
                }
            } else {
                temp = defaultStr;
            }

            String cheJ = "";
            String eng = "";

            // 点击开始查询之后 再一次进入页面自动匹配显示
            if (mArrayListWeiZhang != null && mArrayListWeiZhang.size() > 0) {
                for (WeiZhang wei : mArrayListWeiZhang) { // 浙D2750P
                    if (!StringUtil.isTrimEmpty(chePai) && wei.chepai_number.contains(chePai)) {
                        cheJ = wei.chejia_number;
                        eng = wei.engine_number;
                        break;
                    }
                }

            }

            chejia_number.setText(cheJ);
            engine_number.setText(eng);

            chepai_number.setText(chePai);
            short_name.setText(temp);// 所在省份的简称

            // tempProvince
            if (!StringUtil.isTrimEmpty(tempProvince)) {
                int firstIndex = tempProvince.indexOf(".");
                if (firstIndex != -1) {
                    tempCName = tempProvince.substring((firstIndex + 1), (firstIndex + 3));// 所在的城市
                }
                tempProvince = tempProvince.substring(0, 2);// 所在的省份
            }

            popXSZ = (View) findViewById(R.id.popXSZ);
            popXSZ.setOnTouchListener(new popOnTouchListener());
            hideShowXSZ();

            dialog = ProgressDialogEx.show(this, "", getString(R.string.loading_set_fence_my), true, Constant.DIALOG_AUTO_DISMISS, new OnCancelListener2() {

                @Override
                public void onCancel(DialogInterface dialog) {
                }

                @Override
                public void onAutoCancel(DialogInterface dialog) {

                }
            });

            myHandler.postDelayed(myRunnable, 1000);

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        setListener();

        btn_cpsz.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (myHandler != null) {
                    myHandler.removeCallbacks(myRunnable);
                }

                Intent intent = new Intent();
                intent.setClass(WeiZhangMainActivity.this, ShortNameList.class);
                intent.putExtra("select_short_name", short_name.getText());
                startActivityForResult(intent, 0);
            }
        });

        query_city.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (myHandler != null) {
                    myHandler.removeCallbacks(myRunnable);
                }
                Intent intent = new Intent();
                intent.setClass(WeiZhangMainActivity.this, ProvinceList.class);
                startActivityForResult(intent, 1);
            }
        });

        btn_query.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // 获取违章信息
                CarInfo car = new CarInfo();
                String quertCityStr = null;
                String quertCityIdStr = null;

                final String shortNameStr = short_name.getText().toString().trim();
                final String chepaiNumberStr = chepai_number.getText().toString().trim();
                if (query_city.getText() != null && !query_city.getText().equals("")) {
                    quertCityStr = query_city.getText().toString().trim();

                }

                if (query_city.getTag() != null && !query_city.getTag().equals("")) {
                    quertCityIdStr = query_city.getTag().toString().trim();
                    car.setCity_id(Integer.parseInt(quertCityIdStr));
                }
                final String chejiaNumberStr = chejia_number.getText().toString().trim();
                final String engineNumberStr = engine_number.getText().toString().trim();

                Intent intent = new Intent();

                car.setChejia_no(chejiaNumberStr);
                car.setChepai_no(shortNameStr + chepaiNumberStr);
                car.setEngine_no(engineNumberStr);

                Bundle bundle = new Bundle();
                bundle.putSerializable("carInfo", car);
                intent.putExtras(bundle);

                boolean result = checkQueryItem(car);
                if (result) {
                    WeiZhang wModel = new WeiZhang(shortNameStr, (shortNameStr + chepaiNumberStr), chejiaNumberStr, engineNumberStr);

                    long mResult = AllOnlineApp.mDb.insertWeiZhang(wModel);

                    intent.setClass(WeiZhangMainActivity.this, WeizhangResult.class);
                    startActivity(intent);

                }
            }
        });

    }

    /**
     * 监听
     */
    private void setListener() {
        chepai_number.addTextChangedListener(mTextWChePai);
    }

    // 车牌号输入监听
    private TextWatcher mTextWChePai = new TextWatcher() {

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
            String cheJ = "";
            String eng = "";
            String sName = "";

            if (mArrayListWeiZhang != null && mArrayListWeiZhang.size() > 0 && s.toString().length() == 6) {
                for (WeiZhang wei : mArrayListWeiZhang) {
                    if (wei.chepai_number.contains(s.toString().trim())) {
                        // 输出对应的车牌前缀，发动机号，车架号
                        sName = wei.short_name;
                        cheJ = wei.chejia_number;
                        eng = wei.engine_number;
                        break;
                    }
                }
                short_name.setText(sName);
                chejia_number.setText(cheJ);
                engine_number.setText(eng);

            }

        }
    };

    private String[] getDate() {
        return new String[] { "京", "津", "沪", "川", "鄂", "甘", "赣", "桂", "贵", "黑", "吉", "翼", "晋", "辽", "鲁", "蒙", "闽", "宁", "青", "琼", "陕", "苏", "皖", "湘", "新", "渝", "豫", "粤", "云", "藏", "浙", "" };
    }

    private int getCityIdByPNameCName(String proName, String cName) {
        int provinceId = -1;

        try {

            List<ProvinceInfoJson> provinceList = WeizhangClient.getAllProvince();

            for (ProvinceInfoJson provinceInfoJson : provinceList) {
                String provinceName = provinceInfoJson.getProvinceName();
                if (provinceName.equals(proName)) {
                    provinceId = provinceInfoJson.getProvinceId();
                    break;
                }
            }

            if (provinceId != -1) {
                List<CityInfoJson> cityList = WeizhangClient.getCitys(provinceId);

                for (CityInfoJson cityInfoJson : cityList) {
                    String cityName = cityInfoJson.getCity_name();
                    if (cityName.equals(cName)) {
                        provinceId = cityInfoJson.getCity_id();
                        break;
                    }
                }
            }

        } catch (Exception e) {
            // TODO: handle exception
        }

        return provinceId;
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    private Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
            if (!StringUtil.isTrimEmpty(tempProvince) && !StringUtil.isTrimEmpty(tempCName)) {
                int cityId = getCityIdByPNameCName(tempProvince, tempCName);

                if (cityId != -1) {
                    setQueryItem(cityId);
                } else {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            } else {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null)
            return;

        switch (requestCode) {
        case 0:
            Bundle bundle = data.getExtras();
            String ShortName = bundle.getString("short_name");
            short_name.setText(ShortName);
            break;
        case 1:
            Bundle bundle1 = data.getExtras();
            // String cityName = bundle1.getString("city_name");
            String cityId = bundle1.getString("city_id");
            setQueryItem(Integer.parseInt(cityId));

            break;
        }
    }

    // 根据城市的配置设置查询项目
    private void setQueryItem(int cityId) {

        InputConfigJson cityConfig = WeizhangClient.getInputConfig(cityId);

        // 没有初始化完成的时候;
        if (cityConfig != null) {
            CityInfoJson city = WeizhangClient.getCity(cityId);

            query_city.setText(city.getCity_name());
            query_city.setTag(cityId);

            int len_chejia = cityConfig.getClassno();
            int len_engine = cityConfig.getEngineno();

            View row_chejia = (View) findViewById(R.id.row_chejia);
            View row_engine = (View) findViewById(R.id.row_engine);

            // 车架号
            if (len_chejia == 0) {
                row_chejia.setVisibility(View.GONE);
            } else {
                row_chejia.setVisibility(View.VISIBLE);
                setMaxlength(chejia_number, len_chejia);
                if (len_chejia == -1) {
                    chejia_number.setHint("请输入完整车架号");
                } else if (len_chejia > 0) {
                    chejia_number.setHint("请输入车架号后" + len_chejia + "位");
                }
            }

            // 发动机号
            if (len_engine == 0) {
                row_engine.setVisibility(View.GONE);
            } else {
                row_engine.setVisibility(View.VISIBLE);
                setMaxlength(engine_number, len_engine);
                if (len_engine == -1) {
                    engine_number.setHint("请输入完整车发动机号");
                } else if (len_engine > 0) {
                    engine_number.setHint("请输入发动机后" + len_engine + "位");
                }
            }
        }

        if (dialog != null) {
            dialog.dismiss();
        }
    }

    // 提交表单检测
    private boolean checkQueryItem(CarInfo car) {
        if (car.getCity_id() == 0) {
            Toast.makeText(WeiZhangMainActivity.this, "请选择查询地", 0).show();
            return false;
        }

        if (car.getChepai_no().length() != 7) {
            Toast.makeText(WeiZhangMainActivity.this, "您输入的车牌号有误", 0).show();
            return false;
        }

        if (car.getCity_id() > 0) {
            InputConfigJson inputConfig = WeizhangClient.getInputConfig(car.getCity_id());
            int engineno = inputConfig.getEngineno();
            int registno = inputConfig.getRegistno();
            int classno = inputConfig.getClassno();

            // 车架号
            if (classno > 0) {
                if (car.getChejia_no().equals("")) {
                    Toast.makeText(WeiZhangMainActivity.this, "输入车架号不为空", 0).show();
                    return false;
                }

                if (car.getChejia_no().length() != classno) {
                    Toast.makeText(WeiZhangMainActivity.this, "输入车架号后" + classno + "位", 0).show();
                    return false;
                }
            } else if (classno < 0) {
                if (car.getChejia_no().length() == 0) {
                    Toast.makeText(WeiZhangMainActivity.this, "输入全部车架号", 0).show();
                    return false;
                }
            }

            // 发动机
            if (engineno > 0) {
                if (car.getEngine_no().equals("")) {
                    Toast.makeText(WeiZhangMainActivity.this, "输入发动机号不为空", 0).show();
                    return false;
                }

                if (car.getEngine_no().length() != engineno) {
                    Toast.makeText(WeiZhangMainActivity.this, "输入发动机号后" + engineno + "位", 0).show();
                    return false;
                }
            } else if (engineno < 0) {
                if (car.getEngine_no().length() == 0) {
                    Toast.makeText(WeiZhangMainActivity.this, "输入全部发动机号", 0).show();
                    return false;
                }
            }

            // 注册证书编号
            if (registno > 0) {
                if (car.getRegister_no().equals("")) {
                    Toast.makeText(WeiZhangMainActivity.this, "输入证书编号不为空", 0).show();
                    return false;
                }

                if (car.getRegister_no().length() != registno) {
                    Toast.makeText(WeiZhangMainActivity.this, "输入证书编号后" + registno + "位", 0).show();
                    return false;
                }
            } else if (registno < 0) {
                if (car.getRegister_no().length() == 0) {
                    Toast.makeText(WeiZhangMainActivity.this, "输入全部证书编号", 0).show();
                    return false;
                }
            }

            return true;
        }
        return false;

    }

    // 设置/取消最大长度限制
    private void setMaxlength(EditText et, int maxLength) {
        if (maxLength > 0) {
            et.setFilters(new InputFilter[] { new InputFilter.LengthFilter(maxLength) });
        } else { // 不限制
            et.setFilters(new InputFilter[] {});
        }
    }

    // 显示隐藏行驶证图示
    private void hideShowXSZ() {
        View btn_help1 = (View) findViewById(R.id.ico_chejia);
        View btn_help2 = (View) findViewById(R.id.ico_engine);
        Button btn_closeXSZ = (Button) findViewById(R.id.btn_closeXSZ);

        btn_help1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                popXSZ.setVisibility(View.VISIBLE);
            }
        });
        btn_help2.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                popXSZ.setVisibility(View.VISIBLE);
            }
        });
        btn_closeXSZ.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                popXSZ.setVisibility(View.GONE);
            }
        });
    }

    // 避免穿透导致表单元素取得焦点
    private class popOnTouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View arg0, MotionEvent arg1) {
            popXSZ.setVisibility(View.GONE);
            return true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myHandler != null) {
            myHandler.removeCallbacksAndMessages(null);
        }
    }
}
