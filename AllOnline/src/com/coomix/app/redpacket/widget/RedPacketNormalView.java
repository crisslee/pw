package com.coomix.app.redpacket.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;
import com.coomix.app.all.R;

/**
 * Created by think on 2017/2/12.
 */

public class RedPacketNormalView extends RedPacketBaseView {
    public RedPacketNormalView(Context context, String iRedPacketId) {
        this(context, null, 0, iRedPacketId);
    }

    public RedPacketNormalView(Context context, AttributeSet attrs, String iRedPacketId) {
        this(context, attrs, 0, iRedPacketId);
    }

    public RedPacketNormalView(Context context, AttributeSet attrs, int defStyle, String iRedPacketId) {
        super(context, attrs, defStyle, iRedPacketId);
    }

    @Override
    protected void initViews() {
        mainView = mInflater.inflate(R.layout.redpacket_chat_view, this);
        imageFlag = (ImageView) mainView.findViewById(R.id.imageViewRedpacket);
        textTitle = (TextView) mainView.findViewById(R.id.redpacketTitle);
        textHint = (TextView) mainView.findViewById(R.id.redpacketHint);
        textExplain = (TextView) mainView.findViewById(R.id.redpacketExplain);
    }
}
