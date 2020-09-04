package com.goomeim.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.GlideApp;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;
import com.coomix.app.all.util.DisplayUtils;
import com.goomeim.domain.GMEmojicon;
import com.goomeim.utils.GMSmileUtils;
import java.util.List;

public class EmojiconGridAdapter extends ArrayAdapter<GMEmojicon> {

    private GMEmojicon.Type emojiconType;
    private int itemWidth = 0;

    /**
     * @param itemWidth item宽度，emojiconType != Type.BIG_EXPRESSION生效
     */
    public EmojiconGridAdapter(Context context, int textViewResourceId, List<GMEmojicon> objects,
        GMEmojicon.Type emojiconType, int itemWidth) {
        super(context, textViewResourceId, objects);
        this.emojiconType = emojiconType;
        this.itemWidth = itemWidth;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            if (emojiconType == GMEmojicon.Type.BIG_EXPRESSION) {
                convertView = View.inflate(getContext(), R.layout.ease_row_big_expression, null);
            } else {
                convertView = View.inflate(getContext(), R.layout.ease_row_expression, null);
            }
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.iv_expression);
        TextView textView = (TextView) convertView.findViewById(R.id.tv_name);
        GMEmojicon emojicon = getItem(position);
        if (textView != null && emojicon.getName() != null) {
            textView.setText(emojicon.getName());
        }
        if (emojiconType != GMEmojicon.Type.BIG_EXPRESSION) {
            if (itemWidth <= 0) {
                // 表情边距
                int spacing = DisplayUtils.dp2px(getContext(), 8);
                int gvWidth = AllOnlineApp.screenWidth - spacing * 2;
                // GridView中item的宽度
                itemWidth = (gvWidth - spacing * 8) / 7;
            }
            if (itemWidth > 0) {
                LayoutParams params = imageView.getLayoutParams();
                params.width = itemWidth;
                params.height = itemWidth;
                imageView.setPadding(itemWidth / 8, itemWidth / 8, itemWidth / 8, itemWidth / 8);
                imageView.setLayoutParams(params);
            }
        }
        if (GMSmileUtils.DELETE_KEY.equals(emojicon.getEmojiText())) {
            imageView.setImageResource(R.drawable.emoticon_delete);
        } else {
            if (emojicon.getIcon() != 0) {
                imageView.setImageResource(emojicon.getIcon());
            } else if (emojicon.getIconPath() != null) {
                GlideApp.with(getContext()).load(emojicon.getIconPath()).placeholder(R.drawable.ease_default_expression)
                    .into(imageView);
            }
        }

        return convertView;
    }
}
