package com.coomix.app.all.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;
import java.util.ArrayList;

/**
 * @author shishenglong
 * @since 2016年10月15日 下午7:08:44
 */
public class PopListDialog extends Dialog implements View.OnClickListener {
    private TextView textCancel = null;
    private TextView textTitle = null;
    private ListView listView = null;
    private ArrayList<String> listData = null;

    public PopListDialog(Context context, ArrayList<String> listData) {
        this(context, R.style.add_dialog, listData);
    }

    public PopListDialog(Context context, int theme, ArrayList<String> listData) {
        super(context, theme);
        this.listData = listData;
        initViews();
    }

    private void initViews() {
        View mainView = LayoutInflater.from(getContext()).inflate(R.layout.popwindow_list, null);
        setContentView(mainView);

        textTitle = (TextView) mainView.findViewById(R.id.pop_title);
        textCancel = (TextView) mainView.findViewById(R.id.pop_cancel);
        textCancel.setOnClickListener(this);
        listView = (ListView) mainView.findViewById(R.id.pop_listview);

        setCanceledOnTouchOutside(true);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.windowAnimations = R.style.AnimBottom;
        params.width = AllOnlineApp.screenWidth - getContext().getResources().getDimensionPixelOffset(R.dimen.space_4x);
        params.height = getContext().getResources().getDimensionPixelOffset(R.dimen.pop_height);
        getWindow().setAttributes(params);

        if (listData != null && listData.size() > 0) {
            listView.setAdapter(new ArrayAdapter<String>(getContext(), R.layout.pop_item_list, listData));
        }
    }

    public void setTextTitle(String text) {
        textTitle.setText(text);
    }

    public void setData(ArrayList<String> listData) {
        this.listData = listData;
        listView.setAdapter(new ArrayAdapter<String>(getContext(), R.layout.pop_item_list, listData));
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        listView.setOnItemClickListener(listener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pop_cancel:
                dismiss();
                break;
        }
    }
}
