package com.coomix.app.all.ui.alarm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.PushSetting;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.widget.MyActionbar;
import com.umeng.analytics.MobclickAgent;
import java.util.HashMap;

/**
 * 报警消息类型
 *
 * @author goome
 */
public class AlarmTypeListActivity extends BaseActivity {

    public static final String ALARM_OPTION = "ALARM_OPTION";
    private ListView listView;
    private AlarmTypeAdapter alarmTypeAdapter;
    private PushSetting pushSetting = null;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("ev_function", "报警消息类型");
        MobclickAgent.onEvent(AlarmTypeListActivity.this, "ev_function", map);

        setContentView(R.layout.activity_alarms_type_list);

        if (getIntent() != null && getIntent().hasExtra(ALARM_OPTION)) {
            pushSetting = (PushSetting) getIntent().getSerializableExtra(ALARM_OPTION);
        }
        if (pushSetting == null || pushSetting.getAlarm_type().size() <= 0) {
            finish();
            return;
        }

        alarmTypeAdapter = new AlarmTypeAdapter(this);
        initViews();
    }

    protected void initViews() {
        MyActionbar actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, R.string.set_notice_type, 0, 0);
        actionbar.setLeftImageClickListener(v -> {
            onBackPressed();
        });

        listView = (ListView) findViewById(R.id.list);
        listView.setEmptyView(findViewById(R.id.empty));
        listView.setAdapter(alarmTypeAdapter);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(ALARM_OPTION, pushSetting);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class AlarmTypeAdapter extends BaseAdapter {
        private LayoutInflater layoutInflater;

        public AlarmTypeAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return pushSetting.getAlarm_type().size();
        }

        @Override
        public PushSetting.AlarmType getItem(int position) {
            if (position < 0 || position > pushSetting.getAlarm_type().size() - 1) {
                return null;
            }
            return pushSetting.getAlarm_type().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.alarm_type_item, null, false);
                convertView.setTag(holder);
                holder.alarmType = (TextView) convertView.findViewById(R.id.alarm_type);
                holder.itemCbx = (CheckBox) convertView.findViewById(R.id.itemCbx);
                holder.alarmLayout = (RelativeLayout) convertView.findViewById(R.id.alarm_type_layout);
            }
            holder = (ViewHolder) convertView.getTag();

            final PushSetting.AlarmType alarmType = getItem(position);
            if (alarmType == null) {
                return convertView;
            }
            holder.alarmType.setText(alarmType.name);

            holder.itemCbx.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        alarmType.push = true;
                    } else {
                        alarmType.push = false;
                    }
                }
            });

            holder.alarmLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alarmType.push = !alarmType.push;
                    notifyDataSetChanged();
                }
            });

            if (alarmType.push) {
                holder.itemCbx.setChecked(true);
            } else {
                holder.itemCbx.setChecked(false);
            }

            return convertView;
        }

        private void setCheckStatus(int iAlarmTypeId, boolean isChecked) {
            for (PushSetting.AlarmType type : pushSetting.getAlarm_type()) {
                if (iAlarmTypeId == type.id) {
                    type.push = isChecked;
                    break;
                }
            }
        }

        class ViewHolder {
            public RelativeLayout alarmLayout;
            public TextView alarmType;
            public CheckBox itemCbx;
        }
    }
}
