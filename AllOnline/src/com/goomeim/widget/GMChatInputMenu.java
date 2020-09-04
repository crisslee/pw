package com.goomeim.widget;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.coomix.app.all.R;
import com.goomeim.domain.GMEmojicon;
import com.goomeim.domain.GMEmojiconGroupEntity;
import com.goomeim.model.GMDefaultEmojiconDatas;
import com.goomeim.utils.GMSmileUtils;
import com.goomeim.widget.emojicon.GMEmojiconMenu;
import com.goomeim.widget.emojicon.GMEmojiconMenuBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * input menu
 * <p>
 * including below component: EaseChatPrimaryMenu: main menu bar, text input,
 * send button EaseChatExtendMenu: grid menu with image, file, location, etc
 * EaseEmojiconMenu: emoji icons
 */
public class GMChatInputMenu extends LinearLayout
{
    FrameLayout primaryMenuContainer, emojiconMenuContainer;
    protected GMChatPrimaryMenu chatPrimaryMenu;
    protected GMEmojiconMenuBase emojiconMenu;
    protected GMChatExtendMenu chatExtendMenu;
    protected FrameLayout chatExtendMenuContainer;
    protected LayoutInflater layoutInflater;

    private Handler handler = new Handler();
    private ChatInputMenuListener listener;
    private Context context;
    private boolean inited;

    public GMChatInputMenu(Context context, AttributeSet attrs, int defStyle)
    {
        this(context, attrs);
    }

    public GMChatInputMenu(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs);
    }

    public GMChatInputMenu(Context context)
    {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs)
    {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        layoutInflater.inflate(R.layout.gm_widget_chat_input_menu, this);
        primaryMenuContainer = (FrameLayout) findViewById(R.id.primary_menu_container);
        emojiconMenuContainer = (FrameLayout) findViewById(R.id.emojicon_menu_container);
        chatExtendMenuContainer = (FrameLayout) findViewById(R.id.extend_menu_container);

        // extend menu
        chatExtendMenu = (GMChatExtendMenu) findViewById(R.id.extend_menu);

    }

    /**
     * init rootView
     * <p>
     * This method should be called after registerExtendMenuItem(),
     * setCustomEmojiconMenu() and setCustomPrimaryMenu().
     *
     * @param emojiconGroupList --will use default if null
     */
    public void init(List<GMEmojiconGroupEntity> emojiconGroupList)
    {
        if (inited)
        {
            return;
        }
        // primary menu, use default if no customized one
        if (chatPrimaryMenu == null)
        {
            chatPrimaryMenu = (GMChatPrimaryMenu) layoutInflater.inflate(R.layout.gm_layout_chat_primary_menu, null);
        }
        primaryMenuContainer.addView(chatPrimaryMenu);

        // emojicon menu, use default if no customized one
        if (emojiconMenu == null)
        {
            emojiconMenu = (GMEmojiconMenu) layoutInflater.inflate(R.layout.gm_layout_emojicon_menu, null);
            if (emojiconGroupList == null)
            {
                emojiconGroupList = new ArrayList<GMEmojiconGroupEntity>();
                emojiconGroupList.add(new GMEmojiconGroupEntity(R.drawable.emoticon_tencent0, Arrays.asList(GMDefaultEmojiconDatas.getData())));
            }
            ((GMEmojiconMenu) emojiconMenu).init(emojiconGroupList);
        }
        emojiconMenuContainer.addView(emojiconMenu);

        processChatMenu();
        chatExtendMenu.init();

        inited = true;
    }

    public void init()
    {
        init(null);
    }

    /**
     * set custom emojicon menu
     *
     * @param customEmojiconMenu
     */
    public void setCustomEmojiconMenu(GMEmojiconMenuBase customEmojiconMenu)
    {
        this.emojiconMenu = customEmojiconMenu;
    }

    /**
     * set custom primary menu
     *
     * @param customPrimaryMenu
     */
    public void setCustomPrimaryMenu(GMChatPrimaryMenu customPrimaryMenu)
    {
        this.chatPrimaryMenu = customPrimaryMenu;
    }

    public GMChatPrimaryMenuBase getPrimaryMenu()
    {
        return chatPrimaryMenu;
    }

    public GMChatExtendMenu getExtendMenu()
    {
        return chatExtendMenu;
    }

    public GMEmojiconMenuBase getEmojiconMenu()
    {
        return emojiconMenu;
    }

    /**
     * register menu item
     *
     * @param name        item name
     * @param drawableRes background of item
     * @param itemId      id
     * @param listener    on click event of item
     */
    public void registerExtendMenuItem(String name, int drawableRes, int itemId, GMChatExtendMenu.GMChatExtendMenuItemClickListener listener)
    {
        chatExtendMenu.registerMenuItem(name, drawableRes, itemId, listener);
    }

    /**
     * register menu item
     *
     * @param nameRes     resource id of item name
     * @param drawableRes background of item
     * @param itemId      id
     * @param listener    on click event of item
     */
    public void registerExtendMenuItem(int nameRes, int drawableRes, int itemId, GMChatExtendMenu.GMChatExtendMenuItemClickListener listener)
    {
        chatExtendMenu.registerMenuItem(nameRes, drawableRes, itemId, listener);
    }

    protected void processChatMenu()
    {
        // send message button
        chatPrimaryMenu.setChatPrimaryMenuListener(new GMChatPrimaryMenuBase.EaseChatPrimaryMenuListener()
        {

            @Override
            public void onSendBtnClicked(String content)
            {
                if (listener != null)
                {
                    listener.onSendMessage(content);
                }
            }

            @Override
            public void onToggleVoiceBtnClicked()
            {
                hideExtendMenuContainer();
            }

            @Override
            public void onToggleExtendClicked()
            {
                toggleMore();
                if(listener != null)
                {
                    listener.onKeyboardShow();
                }
            }

            @Override
            public void onToggleEmojiconClicked()
            {
                toggleEmojicon();
                if(listener != null)
                {
                    listener.onKeyboardShow();
                }
            }

            @Override
            public void onEditTextClicked()
            {
                hideExtendMenuContainer();
                if(listener != null)
                {
                    listener.onKeyboardShow();
                }
            }

            @Override
            public boolean onPressToSpeakBtnTouch(View v, MotionEvent event)
            {
                if (listener != null)
                {
                    return listener.onPressToSpeakBtnTouch(v, event);
                }
                return false;
            }

            @Override
            public void onVoieKeyboardClicked()
            {
                hideExtendMenuContainer();
                showKeyboard();
            }
        });

        // emojicon menu
        emojiconMenu.setEmojiconMenuListener(new GMEmojiconMenuBase.EaseEmojiconMenuListener()
        {
            @Override
            public void onExpressionClicked(GMEmojicon emojicon)
            {
                if (emojicon.getType() != GMEmojicon.Type.BIG_EXPRESSION)
                {
                    if (emojicon.getEmojiText() != null)
                    {
                        chatPrimaryMenu.onEmojiconInputEvent(GMSmileUtils.getSmiledText(context, emojicon.getEmojiText(), null));
                    }
                }
                else
                {
                    if (listener != null)
                    {
                        listener.onBigExpressionClicked(emojicon);
                    }
                }
            }

            @Override
            public void onDeleteImageClicked()
            {
                chatPrimaryMenu.onEmojiconDeleteEvent();
            }
        });

    }

    /**
     * insert text
     *
     * @param text
     */
    public void insertText(String text)
    {
        getPrimaryMenu().onTextInsert(text);
    }

    /**
     * show or hide extend menu
     */
    protected void toggleMore()
    {
        if (chatExtendMenuContainer.getVisibility() == View.GONE)
        {
            hideKeyboard();
            handler.postDelayed(new Runnable()
            {
                public void run()
                {
                    chatExtendMenuContainer.setVisibility(View.VISIBLE);
                    chatExtendMenu.setVisibility(View.VISIBLE);
                    emojiconMenu.setVisibility(View.GONE);
                    chatPrimaryMenu.setButtonMoreBG(R.drawable.ease_chatting_setmode_keyboard_btn);
                }
            }, 200);
        }
        else
        {
            if (emojiconMenu.getVisibility() == View.VISIBLE)
            {
                emojiconMenu.setVisibility(View.GONE);
                chatExtendMenu.setVisibility(View.VISIBLE);
                chatPrimaryMenu.setButtonMoreBG(R.drawable.ease_chatting_setmode_keyboard_btn);
            }
            else
            {
                chatExtendMenuContainer.setVisibility(View.GONE);
                showKeyboard();
                chatPrimaryMenu.setButtonMoreBG(R.drawable.ease_type_select_btn);
            }
        }
    }

    /**
     * show or hide emojicon
     */
    protected void toggleEmojicon()
    {
        if (chatExtendMenuContainer.getVisibility() == View.GONE)
        {
            hideKeyboard();
            handler.postDelayed(new Runnable()
            {
                public void run()
                {
                    chatExtendMenuContainer.setVisibility(View.VISIBLE);
                    chatExtendMenu.setVisibility(View.GONE);
                    emojiconMenu.setVisibility(View.VISIBLE);
                    chatPrimaryMenu.setButtonMoreBG(R.drawable.ease_type_select_btn);
                }
            }, 150);
        }
        else
        {
            chatPrimaryMenu.setButtonMoreBG(R.drawable.ease_type_select_btn);
            if (emojiconMenu.getVisibility() == View.VISIBLE)
            {
                chatExtendMenuContainer.setVisibility(View.GONE);
                emojiconMenu.setVisibility(View.GONE);
                handler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        showKeyboard();
                    }
                }, 50);
            }
            else
            {
                chatExtendMenu.setVisibility(View.GONE);
                emojiconMenu.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * hide keyboard
     */
    public void hideKeyboard()
    {
        chatPrimaryMenu.hideKeyboard();
    }

    public void showKeyboard()
    {
        chatPrimaryMenu.showKeyboard();
    }

    public void showAtEndKeyboard()
    {
        chatExtendMenuContainer.setVisibility(View.GONE);
        emojiconMenu.setVisibility(View.GONE);
        chatPrimaryMenu.setModeKeyboard();
        showKeyboard();
    }

    /**
     * hide extend menu
     */
    public void hideExtendMenuContainer()
    {
        chatExtendMenu.setVisibility(View.GONE);
        emojiconMenu.setVisibility(View.GONE);
        chatExtendMenuContainer.setVisibility(View.GONE);
        chatPrimaryMenu.onExtendMenuContainerHide();
        chatPrimaryMenu.setButtonMoreBG(R.drawable.ease_type_select_btn);
    }

    /**
     * when back key pressed
     *
     * @return false--extend menu is on, will hide it first true --extend menu
     * is off
     */
    public boolean onBackPressed()
    {
        if (chatExtendMenuContainer.getVisibility() == View.VISIBLE)
        {
            hideExtendMenuContainer();
            return false;
        }
        else
        {
            return true;
        }

    }

    public void setChatInputMenuListener(ChatInputMenuListener listener)
    {
        this.listener = listener;
    }

    public interface ChatInputMenuListener
    {
        /**
         * when send message button pressed
         *
         * @param content message content
         */
        void onSendMessage(String content);

        /**
         * when big icon pressed
         *
         * @param emojicon
         */
        void onBigExpressionClicked(GMEmojicon emojicon);

        /**
         * when speak button is touched
         *
         * @param v
         * @param event
         * @return
         */
        boolean onPressToSpeakBtnTouch(View v, MotionEvent event);

        void onKeyboardShow();
        void onInputing();
    }

}
