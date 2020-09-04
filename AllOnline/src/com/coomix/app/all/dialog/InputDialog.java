package com.coomix.app.all.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.coomix.app.all.R;

/**
 * Created by think on 2018/12/5.
 */

public class InputDialog extends Dialog {

    private TextView textTitle;
    private EditText editText;
    private ResultCallback resultCallback;

    public InputDialog(@NonNull Context context) {
        this(context, R.style.inputDialog);
    }

    public InputDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        initViews(context);
    }

    private void initViews(Context context){
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_input, null);
        setContentView(view);

        textTitle = (TextView) view.findViewById(R.id.textViewTitle);
        TextView textConfirm = (TextView) view.findViewById(R.id.textViewConfirm);
        editText = (EditText) view.findViewById(R.id.editTextInput);
        textConfirm.setOnClickListener(view1 -> {
            dismiss();
            if(resultCallback != null){
                resultCallback.callback(editText.getText() != null ? editText.getText().toString() : "");
            }
        });

        findViewById(R.id.textViewCancel).setOnClickListener(view1 -> dismiss());
    }

    public void setStyle(int resTitle, int inputType, int iMaxLength, ResultCallback resultCallback){
        this.setStyle(getContext().getString(resTitle), inputType, iMaxLength, resultCallback);
    }

    public void setStyle(String title, int inputType, int iMaxLength, ResultCallback resultCallback){
        this.resultCallback = resultCallback;
        textTitle.setText(title);
        if(inputType >= 0) {
            editText.setInputType(inputType);
        }
        if(iMaxLength > 0){
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(iMaxLength)});
        }
    }

    public interface ResultCallback{
        public void callback(String data);
    }
}
