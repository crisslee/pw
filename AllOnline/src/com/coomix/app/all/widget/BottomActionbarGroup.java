package com.coomix.app.all.widget;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coomix.app.all.R;
import com.coomix.app.all.util.EmUtils;

import net.goome.im.chat.GMChatManager;
import net.goome.im.chat.GMClient;
import net.goome.im.chat.GMConversation;

import java.util.ArrayList;
import java.util.List;

public class BottomActionbarGroup extends RelativeLayout implements OnClickListener {

    public interface OnActionbarItemSelectListener {
        void onActionbarItemSelected(int index);
    }

    private List<BottomActionItemView> mTabList;

    private int mSelectedIndex;

    private OnActionbarItemSelectListener mListener;

    public BottomActionbarGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public BottomActionbarGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomActionbarGroup(Context context) {
        this(context, null);
    }

    @Override
    public void onClick(View v) {
        if (mListener == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.tab_map:
                mSelectedIndex = 0;
                break;
            case R.id.tab_alarm:
                mSelectedIndex = 1;
                break;
            case R.id.tab_mine:
                mSelectedIndex = 2;
                break;
        }
        mListener.onActionbarItemSelected(mSelectedIndex);
        changeSelection();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        changeSelection();
    }

    // private

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.bottom_action_bar_layout, this);
        unreadLabel = (TextView) findViewById(R.id.unread_msg_number);
        unReadIMLabel = (TextView) findViewById(R.id.unread_im_msg_number);
        mTabList = new ArrayList<BottomActionItemView>();
        BottomActionItemView tabMap = (BottomActionItemView) findViewById(R.id.tab_map);
        BottomActionItemView tabAlarm = (BottomActionItemView) findViewById(R.id.tab_alarm);
        BottomActionItemView tabMine = (BottomActionItemView) findViewById(R.id.tab_mine);
        tabMap.setOnClickListener(this);
        tabAlarm.setOnClickListener(this);
        tabMine.setOnClickListener(this);
        mTabList.add(tabMap);
        mTabList.add(tabAlarm);
        mTabList.add(tabMine);
        mSelectedIndex = 0;
    }

    public void changeSelection() {
        for (int i = 0; i < mTabList.size(); i++) {
            BottomActionItemView v = mTabList.get(i);
            if (i == mSelectedIndex) {
                v.setSelected(true);
            } else {
                v.setSelected(false);
            }
        }
    }


    // public
    public void setOnActionbarItemSelectListener(OnActionbarItemSelectListener l) {
        mListener = l;
    }

    public void setCurrentSelectedItem(int index) {
        mSelectedIndex = index;
    }

    public BottomActionItemView getItem(int position) {
        return mTabList.get(position);
    }

    private TextView unreadLabel;
    private TextView unReadIMLabel;
    /**
     * update unread message count
     */
    public void updateUnreadIMLabel() {
        new Handler().postDelayed(new Runnable(){
            public void run() {
                int unreadMsgCount = getUnreadMsgCountTotal();
                String unreadMsgCountStr = String.valueOf(unreadMsgCount);
                if (unreadMsgCount > 99)
                {
                    unreadMsgCountStr = "99+";
                }
                if (unreadMsgCount > 0) {
                    unReadIMLabel.setText(unreadMsgCountStr);
                    unReadIMLabel.setVisibility(View.VISIBLE);
                } else {
                    unReadIMLabel.setVisibility(View.INVISIBLE);
                }
            }
        }, 800);
    }

    /**
     * update unread message count
     */
    public void updateUnreadAlarmLabel(final int count) {
        if (count > 0)
        {
            unreadLabel.setVisibility(View.VISIBLE);
            if(count <= 99)
            {
                unreadLabel.setText(String.valueOf(count));
            }
            else
            {
                unreadLabel.setText("99+");
            }
        }
        else
        {
            unreadLabel.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * get unread message count
     *
     * @return 未设置免打扰的未读消息数目
     */
    public static int getUnreadMsgCountTotal() {
        GMChatManager mgr = GMClient.getInstance().chatManager();
        if (mgr == null) {
            return 0;
        }
        List<GMConversation> list = mgr.getAllConversations();
        int[] unreadMsgCount = EmUtils.loadGMIMGroupUnreadMsgsCount(list);

        return unreadMsgCount[0];
    }

}
