package com.coomix.app.all.ui.devTime;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.BindView;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.DevMode;
import com.coomix.app.all.widget.MyActionbar;
import java.net.URLEncoder;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2018/12/27.
 */
public class TrackFragment extends DevTimeFragment {
    @BindView(R.id.topbar)
    MyActionbar topbar;
    @BindView(R.id.time)
    EditText time;
    @BindView(R.id.maxIntervalHint)
    TextView maxIntervalHint;

    private int maxInterval = 200;

    public TrackFragment() {
    }

    public static TrackFragment newInstance(DevMode mode) {
        TrackFragment f = new TrackFragment();
        f.mode = mode;
        f.currMode = DevMode.MODE_TRACK;
        return f;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_track;
    }

    @Override
    protected void initView() {
        topbar.setTitle(R.string.title_mode_track);
        topbar.setLeftImageResource(R.drawable.btn_back);
        topbar.setLeftImageClickListener(v -> setMode(R.string.title_mode_track));
        topbar.setLeftText(R.string.senior);
        topbar.setLeftTextCLickListener(v -> setMode(R.string.title_mode_track));
        time.addTextChangedListener(w);
    }

    @Override
    protected void processContent() {
        if (isTrack()) {
            time.setText(mode.content);
            time.setSelection(mode.content.length());
        }
        maxIntervalHint.setText(getString(R.string.max_interval_hint, maxInterval, 1));
    }

    private boolean isTrack() {
        return mode != null && mode.mode == DevMode.MODE_TRACK;
    }

    @Override
    protected String makeContent() {
        String span = time.getText().toString().trim();
        return URLEncoder.encode(span);
    }

    @Override
    protected boolean checkTime() {
        String span = time.getText().toString().trim();
        if (span.length() == 0) {
            showInvalidDlg();
            return false;
        }
        return true;
    }

    private TextWatcher w = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String str = time.getText().toString().trim();
            if (str.length() > 1 && str.charAt(0) == '0') {
                time.setText(str.substring(1));
                time.setSelection(str.substring(1).length());
                return;
            }
            int t;
            if (TextUtils.isEmpty(str)) {
                t = 0;
            } else {
                t = Integer.parseInt(str);
            }
            if (t > maxInterval) {
                time.setText(String.valueOf(maxInterval));
                time.setSelection(time.getText().toString().length());
            }
        }
    };
}
