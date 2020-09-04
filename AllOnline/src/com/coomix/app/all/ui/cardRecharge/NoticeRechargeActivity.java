package com.coomix.app.all.ui.cardRecharge;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.bumptech.GlideApp;
import com.coomix.app.all.R;
import com.coomix.app.all.manager.ThemeManager;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.ui.platformRecharge.PlatRechargeActivity;
import com.coomix.app.all.widget.MyActionbar;

/**
 * File Description: 公告类型为2时的充值入口
 *
 * @author felixqiu
 * @since 2019-10-21.
 */
public class NoticeRechargeActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout llRecharge, llCardRecharge;
    private ImageView ivRecharge, ivCardRecharge;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_recharge);
        initViews();
    }

    private void initViews() {
        MyActionbar bar = findViewById(R.id.actionbar);
        bar.initActionbar(true, R.string.btn_renew, 0, 0);

        llRecharge = findViewById(R.id.layoutRecharge);
        llCardRecharge = findViewById(R.id.layoutCardRecharge);
        llRecharge.setOnClickListener(this);
        llCardRecharge.setOnClickListener(this);
        ivRecharge = findViewById(R.id.imageViewRecharge);
        ivCardRecharge = findViewById(R.id.imageViewCardRecharge);

        GlideApp.with(this)
            .load(ThemeManager.getInstance().getThemeAll().getThemeColor().getCharge())
            .placeholder(R.drawable.mine_platform_recharge)
            .error(R.drawable.mine_platform_recharge)
            .into(ivRecharge);
        GlideApp.with(this)
            .load(ThemeManager.getInstance().getThemeAll().getThemeColor().getCard_charge())
            .placeholder(R.drawable.mine_card_recharge)
            .error(R.drawable.mine_card_recharge)
            .into(ivCardRecharge);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layoutRecharge:
                startActivity(PlatRechargeActivity.class);
                break;
            case R.id.layoutCardRecharge:
                startActivity(CardRechargeActivity.class);
                break;
            default:
                break;
        }
    }

    private void startActivity(Class<?> cls) {
        startActivity(new Intent(this, cls));
    }
}
