package com.goomeim.widget.emojicon;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.coomix.app.all.R;
import com.goomeim.domain.GMEmojicon;
import com.goomeim.domain.GMEmojiconGroupEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Emojicon menu
 */
public class GMEmojiconMenu extends GMEmojiconMenuBase
{

    private int emojiconColumns;
    private int bigEmojiconColumns;
    private final int defaultBigColumns = 4;
    private final int defaultColumns = 7;
    private GMEmojiconScrollTabBar tabBar;
    private GMEmojiconIndicatorView indicatorView;
    private GMEmojiconPagerView pagerView;

    private List<GMEmojiconGroupEntity> emojiconGroupList = new ArrayList<GMEmojiconGroupEntity>();

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public GMEmojiconMenu(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public GMEmojiconMenu(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs);
    }

    public GMEmojiconMenu(Context context)
    {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs)
    {
        LayoutInflater.from(context).inflate(R.layout.gm_widget_emojicon, this);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EaseEmojiconMenu);
        emojiconColumns = ta.getInt(R.styleable.EaseEmojiconMenu_emojiconColumns, defaultColumns);
        bigEmojiconColumns = ta.getInt(R.styleable.EaseEmojiconMenu_bigEmojiconRows, defaultBigColumns);
        ta.recycle();

        pagerView = (GMEmojiconPagerView) findViewById(R.id.pager_view);
        indicatorView = (GMEmojiconIndicatorView) findViewById(R.id.indicator_view);
        tabBar = (GMEmojiconScrollTabBar) findViewById(R.id.tab_bar);

    }

    public void init(List<GMEmojiconGroupEntity> groupEntities)
    {
        if (groupEntities == null || groupEntities.size() == 0)
        {
            return;
        }
        for (GMEmojiconGroupEntity groupEntity : groupEntities)
        {
            emojiconGroupList.add(groupEntity);
            tabBar.addTab(groupEntity.getIcon());
        }

        pagerView.setPagerViewListener(new EmojiconPagerViewListener());
        pagerView.init(emojiconGroupList, emojiconColumns, bigEmojiconColumns);

        tabBar.setTabBarItemClickListener(new GMEmojiconScrollTabBar.GMScrollTabBarItemClickListener()
        {
            @Override
            public void onItemClick(int position)
            {
                pagerView.setGroupPostion(position);
            }
        });

    }

    /**
     * add emojicon group
     *
     * @param groupEntity
     */
    public void addEmojiconGroup(GMEmojiconGroupEntity groupEntity)
    {
        emojiconGroupList.add(groupEntity);
        pagerView.addEmojiconGroup(groupEntity, true);
        tabBar.addTab(groupEntity.getIcon());
    }

    /**
     * add emojicon group list
     *
     * @param groupEntitieList
     */
    public void addEmojiconGroup(List<GMEmojiconGroupEntity> groupEntitieList)
    {
        for (int i = 0; i < groupEntitieList.size(); i++)
        {
            GMEmojiconGroupEntity groupEntity = groupEntitieList.get(i);
            emojiconGroupList.add(groupEntity);
            pagerView.addEmojiconGroup(groupEntity, i == groupEntitieList.size() - 1 ? true : false);
            tabBar.addTab(groupEntity.getIcon());
        }

    }

    /**
     * remove emojicon group
     *
     * @param position
     */
    public void removeEmojiconGroup(int position)
    {
        emojiconGroupList.remove(position);
        pagerView.removeEmojiconGroup(position);
        tabBar.removeTab(position);
    }

    public void setTabBarVisibility(boolean isVisible)
    {
        if (!isVisible)
        {
            tabBar.setVisibility(View.GONE);
        }
        else
        {
            tabBar.setVisibility(View.VISIBLE);
        }
    }

    private class EmojiconPagerViewListener implements GMEmojiconPagerView.GMEmojiconPagerViewListener
    {

        @Override
        public void onPagerViewInited(int groupMaxPageSize, int firstGroupPageSize)
        {
            indicatorView.init(groupMaxPageSize);
            indicatorView.updateIndicator(firstGroupPageSize);
            tabBar.selectedTo(0);
        }

        @Override
        public void onGroupPositionChanged(int groupPosition, int pagerSizeOfGroup)
        {
            indicatorView.updateIndicator(pagerSizeOfGroup);
            tabBar.selectedTo(groupPosition);
        }

        @Override
        public void onGroupInnerPagePostionChanged(int oldPosition, int newPosition)
        {
            indicatorView.selectTo(oldPosition, newPosition);
        }

        @Override
        public void onGroupPagePostionChangedTo(int position)
        {
            indicatorView.selectTo(position);
        }

        @Override
        public void onGroupMaxPageSizeChanged(int maxCount)
        {
            indicatorView.updateIndicator(maxCount);
        }

        @Override
        public void onDeleteImageClicked()
        {
            if (listener != null)
            {
                listener.onDeleteImageClicked();
            }
        }

        @Override
        public void onExpressionClicked(GMEmojicon emojicon)
        {
            if (listener != null)
            {
                listener.onExpressionClicked(emojicon);
            }
        }

    }

}
