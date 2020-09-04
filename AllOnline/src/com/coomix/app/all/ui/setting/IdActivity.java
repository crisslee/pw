package com.coomix.app.all.ui.setting;

import android.content.Context;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.TextView;
import android.widget.Toast;
import com.coomix.app.all.R;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.framework.util.OSUtil;

/**
 * 手机唯一码
 *
 * @author 刘生健
 * @since 2016-3-15 上午10:37:36
 */
public class IdActivity extends BaseActivity {
    private TextView idTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id);
        MyActionbar actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, R.string.ud_id, 0, 0);
        idTv = ((TextView) findViewById(R.id.id_tv));
        idTv.setText(OSUtil.getUdid(this));
        idTv.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(idTv.getText());
                Toast.makeText(IdActivity.this, R.string.copy_to_clipboard, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }
}
