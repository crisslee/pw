package com.coomix.app.all.ui.audioRecord;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.coomix.app.all.R;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.all.model.bean.AudioPackMd;
import com.coomix.app.all.ui.base.BaseActivity;

import java.util.ArrayList;

public class AudioBuyTrafficActivity extends BaseActivity {
    private static String TAG = "AudioBuyTrafficActivity";
    private RecyclerView recyclerviewAudioPack, recyclerviewAudioAnnualPack;
    AudioPackAdapter audioPackAdapter;
    AudioAnnualPackAdapter audioAnnualPackAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auido_buy_traffic);
        initView();
        test();
    }

    private void test() {
        ArrayList<AudioPackMd> list = new ArrayList<>();
        list.add(new AudioPackMd("20M", "30/year", false));
        list.add(new AudioPackMd("40M", "60/year", false));

        list.add(new AudioPackMd("60M", "90/year", false));
        list.add(new AudioPackMd("80M", "120/year", false));

        audioPackAdapter.setData(list);
        audioAnnualPackAdapter.setData(list);
    }

    private void initView() {
        MyActionbar actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, R.string.buy_traffic, 0, 0);

        recyclerviewAudioPack = (RecyclerView) findViewById(R.id.recyclerviewAudioPack);
        recyclerviewAudioPack.setLayoutManager(new GridLayoutManager(this, 3));
        audioPackAdapter =new AudioPackAdapter();
        recyclerviewAudioPack.setAdapter(audioPackAdapter);

        recyclerviewAudioAnnualPack = (RecyclerView) findViewById(R.id.recyclerviewAudioAnnualPack);
        recyclerviewAudioAnnualPack.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL, false));
        audioAnnualPackAdapter = new AudioAnnualPackAdapter();
        recyclerviewAudioAnnualPack.setAdapter(audioAnnualPackAdapter);
    }

}
