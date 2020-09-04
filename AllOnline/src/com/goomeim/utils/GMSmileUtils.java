/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.goomeim.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Spannable;
import android.text.Spannable.Factory;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.view.View;

import com.coomix.app.all.R;
import com.goomeim.controller.GMImManager;
import com.goomeim.controller.MessageListItemClickListener;
import com.goomeim.domain.GMEmojicon;
import com.goomeim.model.GMDefaultEmojiconDatas;

import net.goome.im.chat.GMMessage;
import net.goome.im.chat.GMTextMessageBody;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GMSmileUtils
{
    public static final String DELETE_KEY = "em_delete_delete_expression";

    private static final Factory spannableFactory = Spannable.Factory.getInstance();

    private static final Map<Pattern, Object> emoticons = new HashMap<Pattern, Object>();

    public static final int AUTO_LINK_EMAIL = 0;
    public static final int AUTO_LINK_TEL = 1;
    public static final int AUTO_LINK_WEB = 2;
    public static final int AUTO_LINK_HYPER = 3; //消息携带用户信息

    static
    {
        GMEmojicon[] emojicons = GMDefaultEmojiconDatas.getData();
        for (int i = 0; i < emojicons.length; i++)
        {
            addPattern(emojicons[i].getEmojiText(), emojicons[i].getIcon());
        }
        GMImManager.GMEmojiconInfoProvider emojiconInfoProvider = GMImManager.getInstance().getEmojiconInfoProvider();
        if (emojiconInfoProvider != null && emojiconInfoProvider.getTextEmojiconMapping() != null)
        {
            for (Entry<String, Object> entry : emojiconInfoProvider.getTextEmojiconMapping().entrySet())
            {
                addPattern(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * add text and icon to the map
     *
     * @param emojiText--
     *            text of emoji
     * @param icon
     *            -- resource id or local path
     */
    public static void addPattern(String emojiText, Object icon)
    {
        emoticons.put(Pattern.compile(Pattern.quote(emojiText)), icon);
    }

    /**
     * replace existing spannable with smiles and add url click
     *
     * @param context
     * @param spannable
     * @return
     */
    public static boolean addSmilesAndClick(final Context context, final GMMessage message, Spannable spannable, final MessageListItemClickListener itemClickListener)
    {
//        URLSpan urls[] = spannable.getSpans(0, spannable.length(), URLSpan.class);
//        if (urls != null && urls.length > 0)
//        {
//            for (int i = 0; i < urls.length; i++)
//            {
//                final URLSpan urlSpan = urls[i];
//                spannable.setSpan(new ClickableSpan()
//                {
//                    @Override
//                    public void onClick(View widget)
//                    {
//                        String url = urlSpan.getURL();
//                        if (url != null && itemClickListener != null)
//                        {
//                            itemClickListener.onTextAutoLinkClick(message, url);
//                        }
//                    }
//
//                    @Override
//                    public void updateDrawState(TextPaint ds)
//                    {
//                        super.updateDrawState(ds);
//                    }
//                }, spannable.getSpanStart(urlSpan), spannable.getSpanEnd(urlSpan), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
//            }
//        }

        if(message != null)
        {
            //只对聊天消息内容做标识。输入的时候message==null不处理
            //网址
            final String text = ((GMTextMessageBody) message.getMsgBody()).getText();
            Pattern pattern = Pattern.compile("((http[s]{0,1}|ftp)://[a-zA-Z0-9\\.\\-]+\\.([a-zA-Z]{2,4})(:\\d+)?(/[a-zA-Z0-9\\.\\-~!@#$%^&*+?:_/=<>]*)?)|(www.[a-zA-Z0-9\\.\\-]+\\.([a-zA-Z]{2,4})(:\\d+)?(/[a-zA-Z0-9\\.\\-~!@#$%^&*+?:_/=<>]*)?)");
            Matcher myMatcher = pattern.matcher(text);
            while (myMatcher.find())
            {
                final String webAddress = myMatcher.group();
                int iStart = text.indexOf(webAddress);
                if (iStart < 0)
                {
                    continue;
                }
                spannable.setSpan(new ClickableSpan()
                {
                    @Override
                    public void onClick(View widget)
                    {
                        if (itemClickListener != null)
                        {
                            itemClickListener.onTextAutoLinkClick(message, webAddress, AUTO_LINK_WEB);
                        }
                    }

                    @Override
                    public void updateDrawState(TextPaint ds)
                    {
                        super.updateDrawState(ds);
                        //设置文字的高亮颜色
                        ds.setColor(context.getResources().getColor(R.color.main_text_blue));
                        ds.setUnderlineText(true);
                        ds.clearShadowLayer();
                    }
                }, iStart, iStart + webAddress.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            //邮箱
            pattern = Pattern.compile("[A-Z0-9a-z\\._%+-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,4}");
            myMatcher = pattern.matcher(text);
            while (myMatcher.find())
            {
                final String emailAddress = myMatcher.group();
                int iStart = text.indexOf(emailAddress);
                if (iStart < 0)
                {
                    continue;
                }
                spannable.setSpan(new ClickableSpan()
                {
                    @Override
                    public void onClick(View widget)
                    {
                        if (itemClickListener != null)
                        {
                            itemClickListener.onTextAutoLinkClick(message, emailAddress, AUTO_LINK_EMAIL);
                        }
                    }

                    @Override
                    public void updateDrawState(TextPaint ds)
                    {
                        super.updateDrawState(ds);
                        //设置文字的高亮颜色
                        ds.setColor(context.getResources().getColor(R.color.main_text_blue));
                        ds.setUnderlineText(false);
                        ds.clearShadowLayer();
                    }
                }, iStart, iStart + emailAddress.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            //电话号码
            pattern = Pattern.compile("\\d{3}-\\d{8}|\\d{3}-\\d{7}|\\d{4}-\\d{8}|\\d{4}-\\d{7}|1+[34578]+\\d{9}|\\d{8}|\\d{7}|1+[34578]+[0-9]-\\d{4}-\\d{4}|1+[34578]+[0-9] \\d{4} \\d{4}");
            myMatcher = pattern.matcher(text);
            while (myMatcher.find())
            {
                final String telNum = myMatcher.group();
                int iStart = text.indexOf(telNum);
                if (iStart < 0)
                {
                    continue;
                }
                spannable.setSpan(new ClickableSpan()
                {
                    @Override
                    public void onClick(View widget)
                    {
                        if (itemClickListener != null)
                        {
                            itemClickListener.onTextAutoLinkClick(message, telNum, AUTO_LINK_TEL);
                        }
                    }

                    @Override
                    public void updateDrawState(TextPaint ds)
                    {
                        super.updateDrawState(ds);
                        //设置文字的高亮颜色
                        ds.setColor(context.getResources().getColor(R.color.main_text_blue));
                        ds.setUnderlineText(false);
                        ds.clearShadowLayer();
                    }
                }, iStart, iStart + telNum.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            //带有超链接的信息
            if (GMCommonUtils.isHyperLinkMessage(message))
            {
                ArrayList<GMHyperLinkValue> listLinkValue = GMCommonUtils.getMessageHyperLinkValueList(message);
                for (final GMHyperLinkValue linkValue : listLinkValue)
                {
                    if (linkValue != null)
                    {
                        final String nickName = linkValue.getNickName();
                        int iStart = text.indexOf(nickName);
                        if (iStart < 0)

                        {
                            continue;
                        }
                        spannable.setSpan(new ClickableSpan()
                        {
                            @Override
                            public void onClick(View widget)
                            {
                                if (itemClickListener != null)
                                {
                                    itemClickListener.onTextAutoLinkClick(message, nickName, AUTO_LINK_HYPER);
                                }
                            }

                            @Override
                            public void updateDrawState(TextPaint ds)
                            {
                                super.updateDrawState(ds);
                                //设置文字的高亮颜色
                                ds.setColor(context.getResources().getColor(R.color.main_text_blue));
                                ds.setUnderlineText(false);
                                ds.clearShadowLayer();
                            }
                        }, iStart, iStart + nickName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }

        boolean hasChanges = false;
        for (Entry<Pattern, Object> entry : emoticons.entrySet())
        {
            Matcher matcher = entry.getKey().matcher(spannable);
            while (matcher.find())
            {
                boolean set = true;
                for (ImageSpan span : spannable.getSpans(matcher.start(), matcher.end(), ImageSpan.class))
                {
                    if (spannable.getSpanStart(span) >= matcher.start() && spannable.getSpanEnd(span) <= matcher.end())
                    {
                        spannable.removeSpan(span);
                    }
                    else
                    {
                        set = false;
                        break;
                    }
                }
                if (set)
                {
                    hasChanges = true;
                    Object value = entry.getValue();
                    if (value instanceof String && !((String) value).startsWith("http"))
                    {
                        File file = new File((String) value);
                        if (!file.exists() || file.isDirectory())
                        {
                            return false;
                        }
                        spannable.setSpan(new MyImageSpan(context, Uri.fromFile(file)), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    else if(value instanceof Integer)
                    {
                        spannable.setSpan(new MyImageSpan(context, (Integer) value), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }

        return hasChanges;
    }

    public static Spannable getSmiledText(Context context, CharSequence text, MessageListItemClickListener itemClickListener)
    {
        Spannable spannable = spannableFactory.newSpannable(text);
        addSmilesAndClick(context, null, spannable, itemClickListener);
        return spannable;
    }

    public static Spannable getSmiledText(Context context, GMMessage message, CharSequence text, MessageListItemClickListener itemClickListener)
    {
        Spannable spannable = spannableFactory.newSpannable(text);
        addSmilesAndClick(context, message, spannable, itemClickListener);
        return spannable;
    }

    public static boolean containsKey(String key)
    {
        boolean b = false;
        for (Entry<Pattern, Object> entry : emoticons.entrySet())
        {
            Matcher matcher = entry.getKey().matcher(key);
            if (matcher.find())
            {
                b = true;
                break;
            }
        }

        return b;
    }

    public static int getSmilesSize()
    {
        return emoticons.size();
    }

    public static class MyImageSpan extends ImageSpan
    {
        public MyImageSpan(Context context, Uri uri)
        {
            super(context, uri);
        }

        public MyImageSpan(Context context, int id)
        {
            super(context, id);
        }

        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm)
        {
            Drawable d = getDrawable();
            Rect rect = d.getBounds();
            if (fm != null)
            {
                Paint.FontMetricsInt fmPaint = paint.getFontMetricsInt();
                int fontHeight = fmPaint.bottom - fmPaint.top;
                int drHeight = rect.bottom - rect.top;

                int top = drHeight / 2 - fontHeight / 4;
                int bottom = drHeight / 2 + fontHeight / 4;

                fm.ascent = -bottom;
                fm.top = -bottom;
                fm.bottom = top;
                fm.descent = top;
            }
            return rect.right;
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint)
        {
            Drawable b = getDrawable();
            canvas.save();
            int transY = ((bottom - top) - b.getBounds().bottom) / 2 + top;
            canvas.translate(x, transY);
            b.draw(canvas);
            canvas.restore();
        }
    }
}
