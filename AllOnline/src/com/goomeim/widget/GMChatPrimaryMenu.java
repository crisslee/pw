package com.goomeim.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coomix.app.all.R;
import com.coomix.app.framework.util.CommonUtil;
import com.goomeim.model.GMAtMessageHelper;

/**
 * primary menu
 */
public class GMChatPrimaryMenu extends GMChatPrimaryMenuBase implements OnClickListener
{
    private EditText editText;
    private View buttonSetModeKeyboard;
    private RelativeLayout edittext_layout;
    private View buttonSetModeVoice;
    private View buttonSend;
    private View buttonPressToSpeak;
    private TextView btnPressToSpeakText;
    private View faceRlayout;
    private ImageView faceNormal;
    private ImageView faceChecked;
    private Button buttonMore;
    private Context context;
    private GMVoiceRecorderView voiceRecorderView;
    private OnAtTriggerListener onAtTriggerListener = null;
    private boolean bDeleteing = false;
    private final int MAX_BYTES = (int) (3.5f * 1024); //3.5kb
    private boolean isBeyondLimit = false;

    public GMChatPrimaryMenu(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public GMChatPrimaryMenu(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public GMChatPrimaryMenu(Context context)
    {
        super(context);
        init(context, null);
    }

    private void init(final Context context, AttributeSet attrs)
    {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.ease_widget_chat_primary_menu, this);
        editText = (EditText) findViewById(R.id.et_sendmessage);
        buttonSetModeKeyboard = findViewById(R.id.btn_set_mode_keyboard);
        edittext_layout = (RelativeLayout) findViewById(R.id.edittext_layout);
        buttonSetModeVoice = findViewById(R.id.btn_set_mode_voice);
        buttonSend = findViewById(R.id.btn_send);
        buttonPressToSpeak = findViewById(R.id.btn_press_to_speak);
        btnPressToSpeakText = (TextView) findViewById(R.id.btn_press_to_speak_text);
        faceRlayout = findViewById(R.id.rl_face);
        faceNormal = (ImageView) findViewById(R.id.iv_face_normal);
        faceChecked = (ImageView) findViewById(R.id.iv_face_checked);
        buttonMore = (Button) findViewById(R.id.btn_more);

        buttonSetModeVoice.setVisibility(GONE);
        // edittext_layout.setBackgroundResource(R.drawable.ease_input_bar_bg_normal);

        // buttonPressToSpeak.setOnFocusChangeListener(new
        // OnFocusChangeListener() {
        // @Override
        // public void onFocusChange(View v, boolean hasFocus) {
        // if(btnPressToSpeakText == null) {
        // return;
        // }
        // if(hasFocus) {
        // btnPressToSpeakText.setText(R.string.button_pushtotalk_pressed);
        // } else {
        // btnPressToSpeakText.setText(R.string.button_pushtotalk);
        // }
        // }
        // });
        buttonSend.setOnClickListener(this);
        buttonSetModeKeyboard.setOnClickListener(this);
        buttonSetModeVoice.setOnClickListener(this);
        buttonMore.setOnClickListener(this);
        faceRlayout.setOnClickListener(this);
        editText.setOnClickListener(this);
        editText.requestFocus();

        editText.setOnKeyListener(editKeyListener);

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus)
                {
                    onClick(v);
                    // edittext_layout.setBackgroundResource(R.drawable.ease_input_bar_bg_active);
                }
                else
                {
                    // edittext_layout.setBackgroundResource(R.drawable.ease_input_bar_bg_normal);
                }

            }
        });
        editText.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                setButtonMoreBG(R.drawable.ease_type_select_btn);
                showNormalFaceImage();
                return false;
            }
        });
        // listen the text change
        editText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (!TextUtils.isEmpty(s))
                {
                    buttonMore.setVisibility(View.GONE);
                    buttonSend.setVisibility(View.VISIBLE);
                }
                else
                {
                    buttonMore.setVisibility(View.VISIBLE);
                    buttonSend.setVisibility(View.GONE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                if (bDeleteing)
                {
                    bDeleteing = false;
                }
                else
                {
                    judgeAndPopAtList(s.toString());
                }
                judgeTextLengthLimit();
            }
        });

        buttonPressToSpeak.setOnTouchListener(new View.OnTouchListener()
        {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (btnPressToSpeakText != null)
                {
                    switch (event.getAction())
                    {
                        case MotionEvent.ACTION_DOWN:
                            btnPressToSpeakText.setText(R.string.button_pushtotalk_pressed);
                            break;
                        case MotionEvent.ACTION_UP:
                            btnPressToSpeakText.setText(R.string.button_pushtotalk);
                            break;
                        default:
                            break;
                    }
                }
                boolean result = false;
                if (listener != null)
                {
                    result = listener.onPressToSpeakBtnTouch(v, event);
                    if (!result && btnPressToSpeakText != null)
                    {
                        btnPressToSpeakText.setText(R.string.button_pushtotalk);
                    }
                }
                return result;
            }
        });
    }

    private void judgeTextLengthLimit()
    {
        Editable editable = editText.getText();
        if (editable != null)
        {
            String text = editable.toString();
            if (!TextUtils.isEmpty(text))
            {
                try
                {
                    int num = text.getBytes("utf-8").length;
                    if (num > MAX_BYTES)
                    {
                        isBeyondLimit = true;
                    }
                    else
                    {
                        isBeyondLimit = false;
                    }
                }
                catch (Exception e)
                {

                }
            }
        }
    }

    public boolean isBeyondLimit()
    {
        return isBeyondLimit;
    }

    public void judgeAndPopAtList(String text)
    {
        if (TextUtils.isEmpty(text))
        {
            return;
        }
        if (editText.getSelectionEnd() > 0)
        {
            text = text.substring(0, editText.getSelectionEnd());
        }
        if (TextUtils.isEmpty(text) || !text.endsWith("@"))
        {
            return;
        }

        if (text.length() > 1)
        {
            Character character = text.charAt(text.length() - 2);
            if (character != null)
            {
                //如果@的前一个字符是数字，字母，下滑线，则不触发@的选择弹窗
                if (CommonUtil.isLetterDigitDecline(character.toString()))
                {
                    return;
                }
            }
        }

        if (onAtTriggerListener != null)
        {
            onAtTriggerListener.onAtTrigger();
        }
    }

    /**
     * set recorder rootView when speak icon is touched
     *
     * @param voiceRecorderView
     */
    public void setPressToSpeakRecorderView(GMVoiceRecorderView voiceRecorderView)
    {
        this.voiceRecorderView = voiceRecorderView;
    }

    /**
     * append emoji icon to editText
     *
     * @param emojiContent
     */
    public void onEmojiconInputEvent(CharSequence emojiContent)
    {
        onTextInsert(emojiContent);
        //editText.append(emojiContent);
    }

    /**
     * delete emojicon
     */
    public void onEmojiconDeleteEvent()
    {
        if (!TextUtils.isEmpty(editText.getText()))
        {
            KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
            editText.dispatchKeyEvent(event);
        }
    }

    /**
     * on clicke event
     *
     * @param view
     */
    @Override
    public void onClick(View view)
    {
        int id = view.getId();
        if (id == R.id.btn_send)
        {
            if (isBeyondLimit())
            {
                Toast.makeText(getContext(), R.string.chat_message_limit, Toast.LENGTH_SHORT).show();
                return;
            }
            if (listener != null)
            {
                String s = editText.getText().toString();
                editText.setText("");
                listener.onSendBtnClicked(s);
            }
        }
        else if (id == R.id.btn_set_mode_voice)
        {
            setModeVoice();
            showNormalFaceImage();
            if (listener != null)
            {
                listener.onToggleVoiceBtnClicked();
            }
        }
        else if (id == R.id.btn_set_mode_keyboard)
        {
            setModeKeyboard();
            showNormalFaceImage();
            if (listener != null)
            {
                listener.onVoieKeyboardClicked();
            }
        }
        else if (id == R.id.btn_more)
        {
            buttonSetModeVoice.setVisibility(View.GONE);
            buttonSetModeKeyboard.setVisibility(View.GONE);
            edittext_layout.setVisibility(View.VISIBLE);
            faceRlayout.setVisibility(View.VISIBLE);
            buttonPressToSpeak.setVisibility(View.GONE);
            showNormalFaceImage();
            if (listener != null)
            {
                listener.onToggleExtendClicked();
            }
        }
        else if (id == R.id.et_sendmessage)
        {
            // edittext_layout.setBackgroundResource(R.drawable.ease_input_bar_bg_active);
            faceNormal.setVisibility(View.VISIBLE);
            faceChecked.setVisibility(View.INVISIBLE);
            if (listener != null)
            {
                listener.onEditTextClicked();
            }
        }
        else if (id == R.id.rl_face)
        {
            toggleFaceImage();
            if (listener != null)
            {
                listener.onToggleEmojiconClicked();
            }
        }
        else
        {
        }
    }

    /**
     * show voice icon when speak bar is touched
     */
    protected void setModeVoice()
    {
        hideKeyboard();
        edittext_layout.setVisibility(View.GONE);
        faceRlayout.setVisibility(View.GONE);
        buttonSetModeVoice.setVisibility(View.GONE);
        buttonSetModeKeyboard.setVisibility(View.VISIBLE);
        buttonSend.setVisibility(View.GONE);
        buttonMore.setVisibility(View.VISIBLE);
        buttonPressToSpeak.setVisibility(View.VISIBLE);
        faceNormal.setVisibility(View.VISIBLE);
        faceChecked.setVisibility(View.INVISIBLE);

    }

    /**
     * show keyboard
     */
    public void setModeKeyboard()
    {
        edittext_layout.setVisibility(View.VISIBLE);
        faceRlayout.setVisibility(View.VISIBLE);
        buttonSetModeKeyboard.setVisibility(View.GONE);
        buttonSetModeVoice.setVisibility(View.GONE);
        // mEditTextContent.setVisibility(View.VISIBLE);
        editText.requestFocus();
        // buttonSend.setVisibility(View.VISIBLE);
        buttonPressToSpeak.setVisibility(View.GONE);
        if (TextUtils.isEmpty(editText.getText()))
        {
            buttonMore.setVisibility(View.VISIBLE);
            buttonSend.setVisibility(View.GONE);
        }
        else
        {
            buttonMore.setVisibility(View.GONE);
            buttonSend.setVisibility(View.VISIBLE);
        }
    }

    protected void toggleFaceImage()
    {
        if (faceNormal.getVisibility() == View.VISIBLE)
        {
            showSelectedFaceImage();
        }
        else
        {
            showNormalFaceImage();
        }
    }

    private void showNormalFaceImage()
    {
        faceNormal.setVisibility(View.VISIBLE);
        faceChecked.setVisibility(View.INVISIBLE);
    }

    private void showSelectedFaceImage()
    {
        faceNormal.setVisibility(View.INVISIBLE);
        faceChecked.setVisibility(View.VISIBLE);
    }

    @Override
    public void onExtendMenuContainerHide()
    {
        showNormalFaceImage();
    }

    @Override
    public void onTextInsert(CharSequence text)
    {
        int start = editText.getSelectionStart();
        Editable editable = editText.getEditableText();
        editable.insert(start, text);
        setModeKeyboard();
    }

    @Override
    public EditText getEditText()
    {
        return editText;
    }

    private View.OnKeyListener editKeyListener = new View.OnKeyListener()
    {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event)
        {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN)
            {
                try
                {
                    return deleteAtName();
                }
                catch (Exception e)
                {

                }
            }
            return false;
        }
    };

    private boolean deleteAtName()
    {
        if (editText == null || editText.getText() == null)
        {
            return false;
        }

        String allContent = editText.getText().toString();
        String atContent = "";
        if (!TextUtils.isEmpty(allContent))
        {
            bDeleteing = true;
        }
        if (allContent.contains("@") && allContent.contains(" "))
        {
            int selection = editText.getSelectionEnd();
            String beforeAt = "";
            String afterAt = "";
            if (selection > 0)
            {
                atContent = allContent.substring(0, selection);
                afterAt = allContent.substring(selection);
            }

            if (!TextUtils.isEmpty(atContent) && atContent.contains("@") && atContent.endsWith(" "))
            {
                int lastAtIndex = atContent.lastIndexOf("@");
                if (lastAtIndex > 0)
                {
                    beforeAt = atContent.substring(0, lastAtIndex);
                }
                String name = atContent.substring(lastAtIndex + 1, atContent.length() - 1);
                String atName = "@" + name + " ";

                if (allContent.contains(atName) && GMAtMessageHelper.get().isAtUserName(name))
                {
                    allContent = beforeAt + afterAt;
                    editText.setText(allContent);
                    editText.setSelection(selection - atName.length());
                    GMAtMessageHelper.get().removeAtUserInfo(name);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void setOnAtTriggerListener(OnAtTriggerListener listener)
    {
        this.onAtTriggerListener = listener;
    }

    public void setButtonMoreBG(int resId)
    {
        buttonMore.setBackgroundResource(resId);
    }
}
