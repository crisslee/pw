package com.coomix.app.all.ui.im;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.GlideApp;
import com.coomix.app.all.R;
import com.coomix.app.all.dialog.ProgressDialogEx;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.widget.SideBar;
import com.coomix.app.all.util.CharacterParser;
import com.goomeim.GMAppConstant;
import com.goomeim.utils.GMUserUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import net.goome.im.chat.GMChatRoom;
import net.goome.im.chat.GMChatroomMemberInfo;
import net.goome.im.chat.GMClient;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2017/8/19.
 */
public class GmSelectAtUserActivity extends BaseActivity implements View.OnClickListener
{

    private StickyListHeadersListView stickyListView = null;
    private SideBar sideBar = null;
    private ProgressDialogEx progressDialogEx;
    private long roomOwnerId;
    private final int REFRESH_USER_LIST = 0;
    private List<GMChatroomMemberInfo> memberList = null;
    private GmSelectAtUserActivity.UserSelectAdapter userSelectAdapter;
    private CharacterParser characterParser;
    public static final String SELECT_AT_USER_ID = "user_id";
    public static final String SELECT_AT_USER_NAME = "userName";
    private String[] letter;
    private final int TIME_OUT = 60000;

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case REFRESH_USER_LIST:
                    refreshUi(letter);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_em_select_at_user);

        initViews();

        initData();
    }

    private void initViews()
    {
        stickyListView = (StickyListHeadersListView) findViewById(R.id.listViewUsers);
        sideBar = (SideBar) findViewById(R.id.sidrbarUser);

        findViewById(R.id.actionbar_left_text).setOnClickListener(this);

        stickyListView.setOnItemClickListener(onItemClickListener);

        // 设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener()
        {

            @Override
            public void onTouchingLetterChanged(String s)
            {
                // 该字母首次出现的位置
                stickyListView.setSelection(userSelectAdapter.getPositionForSection(s.charAt(0)));
            }
        });
    }

    /**
     * 设置对应屏幕的字体大小
     */
    private int getTextSize()
    {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        float ratioWidth = (float) screenWidth / 540;
        float ratioHeight = (float) screenHeight / 960;

        float RATIO = Math.min(ratioWidth, ratioHeight);

        return Math.round(23 * RATIO);
    }

    private void initData()
    {
        if (getIntent() == null || !getIntent().hasExtra(GMAppConstant.EXTRA_USER_ID))
        {
            finish();
            return;
        }
        characterParser = new CharacterParser();
        long roomId = getIntent().getLongExtra(GMAppConstant.EXTRA_USER_ID, -1);
        if (roomId != -1)
        {
            showWaitInfo();
            getGroupMemberUserInfo(roomId);
        }
    }

    //    private synchronized void notifyUI(final List<String> uids)
    //    {
    //        EmUserUtil.getInstance().getAllDbUserSync(uids, new EmUserUtil.EmUserUtilCallBackHxUsersList()
    //        {
    //            @Override
    //            public void callbackUsers(List<HxDBUser> users)
    //            {
    //                Message msg = mHandler.obtainMessage();
    //                msg.what = REFRESH_USER_LIST;
    //                memberList = getSortUserList(users);
    //                letter = initSidebarData();
    //                mHandler.sendMessage(msg);
    //            }
    //        }, null);
    //    }

    private void getGroupMemberUserInfo(final long roomId)
    {
        if (roomId == -1)
        {
            return;
        }
        GMUserUtil.getInstance().getSonWorkHandler().post(new Runnable()
        {
            public void run()
            {
                List<GMChatroomMemberInfo> allMembers = new ArrayList<GMChatroomMemberInfo>();
                try
                {
                    GMChatRoom room = GMClient.getInstance().chatroomManager().getChatroomInfoFromDB(roomId);
                    roomOwnerId = room.getOwner();
                    List<GMChatroomMemberInfo> list = GMClient.getInstance().chatroomManager()
                        .getChatroomMemberListFromDB(roomId);
                    if (list != null)
                    {
                        for (GMChatroomMemberInfo m : list)
                        {
                            if (m.getUid() == roomOwnerId)
                            {
                                allMembers.add(m);
                                list.remove(m);
                                break;
                            }
                        }
                        allMembers.addAll(list);
                    }

                    Message msg = mHandler.obtainMessage();
                    msg.what = REFRESH_USER_LIST;
                    memberList = getSortUserList(allMembers);
                    letter = initSidebarData();
                    mHandler.sendMessage(msg);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    private ArrayList singleElement(ArrayList al)
    {
        ArrayList newAl = new ArrayList();

        for (Iterator it = al.iterator(); it.hasNext(); )
        {
            Object obj = it.next();
            if (!newAl.contains(obj))
            {
                newAl.add(obj);
            }
        }
        return newAl;
    }

    private String[] initSidebarData()
    {
        if (memberList == null)
        {
            return null;
        }
        ArrayList<String> letterList = new ArrayList<String>();
        ArrayList<String> spLetterList = new ArrayList<String>();
        String pinying = "";
        int size = memberList.size();
        for (int i = 0; i < size; )
        {
            GMChatroomMemberInfo user = memberList.get(i);
            if (user != null && user.getNickname() != null)
            {
                pinying = characterParser.hanziToPinyin(user.getNickname());
                if (TextUtils.isEmpty(pinying) || pinying.equals("#"))
                {
                    //把特殊字符#放到list的最后面
                    spLetterList.add(pinying);
                    memberList.remove(i);
                    memberList.add(user);
                    size--;
                }
                else
                {
                    letterList.add(pinying);
                    i++;
                }
            }
        }
        letterList.addAll(spLetterList);

        letterList = singleElement(letterList);
        if (letterList == null || letterList.size() <= 0)
        {
            return null;
        }

        return (String[]) letterList.toArray(new String[letterList.size()]);
    }

    private void refreshUi(String[] letter)
    {
        dismissWait();

        if (letter == null)
        {
            Toast.makeText(this, R.string.Failed_to_get_group_chat_information, Toast.LENGTH_SHORT).show();
            return;
        }

        sideBar.setSideBarText(letter, getTextSize());

        if (userSelectAdapter == null)
        {
            userSelectAdapter = new GmSelectAtUserActivity.UserSelectAdapter();
            stickyListView.setAdapter(userSelectAdapter);
        }
        else
        {
            userSelectAdapter.notifyDataSetChanged();
        }
    }

    private List<GMChatroomMemberInfo> getSortUserList(List<GMChatroomMemberInfo> list)
    {
        if (list == null)
        {
            return null;
        }
        Collections.sort(list, new Comparator<GMChatroomMemberInfo>()
        {
            @Override
            public int compare(GMChatroomMemberInfo lhs, GMChatroomMemberInfo rhs)
            {
                return characterParser.hanziToPinyin_All(lhs.getNickname()).compareToIgnoreCase(
                    characterParser.hanziToPinyin_All(rhs.getNickname()));
            }
        });

        return list;
    }

    private void showWaitInfo()
    {
        progressDialogEx = new ProgressDialogEx(this);
        progressDialogEx.setAutoDismiss(true);
        progressDialogEx.setDuration(TIME_OUT);

        try
        {
            progressDialogEx.show(getString(R.string.loading));
        }
        catch (Exception e)
        {

        }
        mHandler.postDelayed(showMsgRunnable, TIME_OUT + 1000);
    }

    private void dismissWait()
    {
        if (progressDialogEx != null && progressDialogEx.isShowing())
        {
            progressDialogEx.dismiss();
        }
        if (mHandler != null)
        {
            mHandler.removeCallbacks(showMsgRunnable);
        }
    }

    private Runnable showMsgRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            Toast.makeText(GmSelectAtUserActivity.this, R.string.failed_to_load_data, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.actionbar_left_text:
                finish();
                break;
        }
    }

    class UserSelectAdapter extends BaseAdapter implements StickyListHeadersAdapter, SectionIndexer
    {
        @Override
        public Object[] getSections()
        {
            return new Object[0];
        }

        @Override
        public int getPositionForSection(int sectionIndex)
        {
            for (int i = 0; i < getCount(); i++)
            {
                GMChatroomMemberInfo user = getItem(i);
                if (user != null && characterParser.hanziToPinyin(user.getNickname()).charAt(0) == sectionIndex)
                {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public int getSectionForPosition(int position)
        {
            GMChatroomMemberInfo user = getItem(position);
            if (user != null)
            {
                return characterParser.hanziToPinyin(user.getNickname()).charAt(0);
            }
            return -1;
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent)
        {
            GmSelectAtUserActivity.UserSelectAdapter.HeaderViewHolder headerViewHolder = null;
            if (convertView == null)
            {
                headerViewHolder = new GmSelectAtUserActivity.UserSelectAdapter.HeaderViewHolder();
                convertView = LayoutInflater.from(GmSelectAtUserActivity.this).inflate(
                    R.layout.user_select_list_header_item, null);
                headerViewHolder.headerTextView = (TextView) convertView.findViewById(R.id.textViewHeader);
                convertView.setTag(R.layout.user_select_list_header_item, headerViewHolder);
            }
            else
            {
                headerViewHolder = (GmSelectAtUserActivity.UserSelectAdapter.HeaderViewHolder) convertView.getTag(
                    R.layout.user_select_list_header_item);
            }

            GMChatroomMemberInfo user = getItem(position);
            if (user != null)
            {
                headerViewHolder.headerTextView.setText(characterParser.hanziToPinyin(user.getNickname()));
            }

            return convertView;
        }

        @Override
        public long getHeaderId(int position)
        {
            GMChatroomMemberInfo user = getItem(position);
            if (user != null)
            {
                return characterParser.hanziToPinyin(user.getNickname()).charAt(0);
            }
            return position;
        }

        @Override
        public int getCount()
        {
            return memberList == null ? 0 : memberList.size();
        }

        @Override
        public GMChatroomMemberInfo getItem(int position)
        {
            if (position < 0 || position > memberList.size() - 1)
            {
                return null;
            }
            return memberList.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            GmSelectAtUserActivity.UserSelectAdapter.ViewHolder viewHolder = null;
            if (convertView == null)
            {
                viewHolder = new GmSelectAtUserActivity.UserSelectAdapter.ViewHolder();
                convertView = LayoutInflater.from(GmSelectAtUserActivity.this).inflate(R.layout.item_em_groupmembers,
                    null);
                viewHolder.userIcon = (ImageView) convertView.findViewById(R.id.item_em_groupmember_icon);
                viewHolder.userName = (TextView) convertView.findViewById(R.id.item_em_groupmember_name);
                viewHolder.manager = (ImageView) convertView.findViewById(R.id.item_em_groupmember_manager_icon);
                convertView.setTag(R.layout.item_em_groupmembers, viewHolder);
            }
            else
            {
                viewHolder = (GmSelectAtUserActivity.UserSelectAdapter.ViewHolder) convertView.getTag(
                    R.layout.item_em_groupmembers);
            }

            GMChatroomMemberInfo user = getItem(position);
            if (user != null)
            {
                if (user.getUid() != -1 && user.getUid() == roomOwnerId)
                {
                    viewHolder.manager.setVisibility(View.VISIBLE);
                }
                else
                {
                    viewHolder.manager.setVisibility(View.GONE);
                }
                int imgSize = getResources().getDimensionPixelSize(R.dimen.space_7x);
                GlideApp.with(GmSelectAtUserActivity.this).load(user.getAvatar())
                    .override(imgSize, imgSize)
                    .placeholder(R.drawable.login_icon_large)
                    .error(R.drawable.login_icon_large)
                    .into(viewHolder.userIcon);
                viewHolder.userName.setText(user.getNickname());
            }

            return convertView;
        }

        private class HeaderViewHolder
        {
            TextView headerTextView;
        }

        private class ViewHolder
        {
            ImageView userIcon;
            ImageView manager;
            TextView userName;
        }
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            if (position >= 0 && position < memberList.size())
            {
                GMChatroomMemberInfo user = memberList.get(position);
                if (user != null)
                {
                    Intent intent = new Intent();
                    intent.putExtra(SELECT_AT_USER_ID, user.getUid());
                    intent.putExtra(SELECT_AT_USER_NAME, user.getNickname());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }
    };

    @Override
    public void finish()
    {
        super.finish();
        try
        {
            overridePendingTransition(R.anim.push_bottom_out, 0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy()
    {
        if (mHandler != null)
        {
            mHandler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }
}
