package com.coomix.app.all.ui.im;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.GlideApp;
import com.coomix.app.all.R;

import net.goome.im.chat.GMChatroomMemberInfo;
import net.goome.im.chat.GMConstant.ChatroomMode;

import java.util.ArrayList;
import java.util.List;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2017/8/9.
 */
public class GmChatroomMembersAdapter extends BaseAdapter
{
    private Context mContext;
    private List<GMChatroomMemberInfo> users = new ArrayList<GMChatroomMemberInfo>();
    private long ownerId;
    private ChatroomMode mode;

    public GmChatroomMembersAdapter(Context context, List<GMChatroomMemberInfo> users, long owner, ChatroomMode mode)
    {
        this.mContext = context;
        this.users = users;
        this.ownerId = owner;
        this.mode = mode;
    }

    public void refreshUserInfo(List<GMChatroomMemberInfo> users)
    {
        this.users = users;
        notifyDataSetChanged();
    }

    @Override
    public int getCount()
    {
        return users != null ? users.size() : 0;
    }

    @Override
    public GMChatroomMemberInfo getItem(int position)
    {
        if (position >= 0 && users != null && position < users.size() && users.get(position) != null)
        {
            return users.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        GmChatroomMembersAdapter.viewHolder holder;
        if (convertView != null)
        {
            holder = (GmChatroomMembersAdapter.viewHolder) convertView.getTag();
        }
        else
        {
            holder = new GmChatroomMembersAdapter.viewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_em_groupmembers, null);
            holder.icon = (ImageView) convertView.findViewById(R.id.item_em_groupmember_icon);
            holder.managerIcon = (ImageView) convertView.findViewById(R.id.item_em_groupmember_manager_icon);
            holder.name = (TextView) convertView.findViewById(R.id.item_em_groupmember_name);
            convertView.setTag(holder);
        }
        final GMChatroomMemberInfo user = getItem(position);
        if (user == null)
        {
            return convertView;
        }
        if (user.getNickname() != null)
        {
            holder.name.setText(user.getNickname());
        }
        else
        {
            holder.name.setText(String.valueOf(user.getUid()));
        }
        int px = mContext.getResources().getDimensionPixelOffset(R.dimen.space_9x);
        GlideApp.with(mContext).load(user.getAvatar())
            .override(px, px)
            .placeholder(R.drawable.login_icon_large)
            .error(R.drawable.login_icon_large)
            .into(holder.icon);

        if (user.getUid() != -1 && user.getUid() == ownerId)
        {
            holder.managerIcon.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.managerIcon.setVisibility(View.GONE);
        }
        return convertView;
    }

    static class viewHolder
    {
        private ImageView icon;
        private ImageView managerIcon;
        private TextView name;
    }
}
