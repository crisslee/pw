package com.coomix.app.all.ui.activity;

import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import com.coomix.app.all.model.bean.ActDisplay;

/**
 * Created by ly on 2017/6/23.
 */
public class SwitchTextWatcher implements TextWatcher {
    AppCompatEditText editText;
    ActDisplay md;

    public SwitchTextWatcher(AppCompatEditText editText, ActDisplay md) {
        this.editText = editText;
        this.md = md;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        md.setInput(s == null ? "" : s.toString());
    }
}