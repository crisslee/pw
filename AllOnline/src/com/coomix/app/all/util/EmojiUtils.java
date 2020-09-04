package com.coomix.app.all.util;

import com.coomix.app.all.R;
import java.io.Serializable;
import java.util.LinkedHashMap;

public class EmojiUtils implements Serializable {

    private static final long serialVersionUID = 4226940873923053650L;
    /**
     * key-表情文字;value-表情图片资源
     */
    public static LinkedHashMap<String, Integer> emojiMap;

    static {
        emojiMap = new LinkedHashMap<String, Integer>();
        emojiMap.put("[微笑]", R.drawable.emoticon_tencent0);
        emojiMap.put("[撇嘴]", R.drawable.emoticon_tencent1);
        emojiMap.put("[色]", R.drawable.emoticon_tencent2);
        emojiMap.put("[发呆]", R.drawable.emoticon_tencent3);
        emojiMap.put("[得意]", R.drawable.emoticon_tencent4);
        emojiMap.put("[流泪]", R.drawable.emoticon_tencent5);
        emojiMap.put("[害羞]", R.drawable.emoticon_tencent6);
        emojiMap.put("[闭嘴]", R.drawable.emoticon_tencent7);
        emojiMap.put("[睡]", R.drawable.emoticon_tencent8);
        emojiMap.put("[大哭]", R.drawable.emoticon_tencent9);
        emojiMap.put("[尴尬]", R.drawable.emoticon_tencent10);
        emojiMap.put("[发怒]", R.drawable.emoticon_tencent11);
        emojiMap.put("[调皮]", R.drawable.emoticon_tencent12);
        emojiMap.put("[呲牙]", R.drawable.emoticon_tencent13);
        emojiMap.put("[惊讶]", R.drawable.emoticon_tencent14);
        emojiMap.put("[难过]", R.drawable.emoticon_tencent15);
        emojiMap.put("[酷]", R.drawable.emoticon_tencent16);
        emojiMap.put("[冷汗]", R.drawable.emoticon_tencent17);
        emojiMap.put("[抓狂]", R.drawable.emoticon_tencent18);
        emojiMap.put("[吐]", R.drawable.emoticon_tencent19);
        emojiMap.put("[偷笑]", R.drawable.emoticon_tencent20);
        emojiMap.put("[可爱]", R.drawable.emoticon_tencent21);
        emojiMap.put("[白眼]", R.drawable.emoticon_tencent22);
        emojiMap.put("[傲慢]", R.drawable.emoticon_tencent23);
        emojiMap.put("[饥饿]", R.drawable.emoticon_tencent24);
        emojiMap.put("[困]", R.drawable.emoticon_tencent25);
        emojiMap.put("[惊恐]", R.drawable.emoticon_tencent26);
        emojiMap.put("[流汗]", R.drawable.emoticon_tencent27);
        emojiMap.put("[憨笑]", R.drawable.emoticon_tencent28);
        emojiMap.put("[大兵]", R.drawable.emoticon_tencent29);
        emojiMap.put("[奋斗]", R.drawable.emoticon_tencent30);
        emojiMap.put("[咒骂]", R.drawable.emoticon_tencent31);
        emojiMap.put("[疑问]", R.drawable.emoticon_tencent32);
        emojiMap.put("[嘘]", R.drawable.emoticon_tencent33);
        emojiMap.put("[晕]", R.drawable.emoticon_tencent34);
        emojiMap.put("[折磨]", R.drawable.emoticon_tencent35);
        emojiMap.put("[衰]", R.drawable.emoticon_tencent36);
        emojiMap.put("[骷髅]", R.drawable.emoticon_tencent37);
        emojiMap.put("[敲打]", R.drawable.emoticon_tencent38);
        emojiMap.put("[再见]", R.drawable.emoticon_tencent39);
        emojiMap.put("[擦汗]", R.drawable.emoticon_tencent40);
        emojiMap.put("[抠鼻]", R.drawable.emoticon_tencent41);
        emojiMap.put("[鼓掌]", R.drawable.emoticon_tencent42);
        emojiMap.put("[糗大了]", R.drawable.emoticon_tencent43);
        emojiMap.put("[坏笑]", R.drawable.emoticon_tencent44);
        emojiMap.put("[左哼哼]", R.drawable.emoticon_tencent45);
        emojiMap.put("[右哼哼]", R.drawable.emoticon_tencent46);
        emojiMap.put("[哈欠]", R.drawable.emoticon_tencent47);
        emojiMap.put("[鄙视]", R.drawable.emoticon_tencent48);
        emojiMap.put("[委屈]", R.drawable.emoticon_tencent49);
        emojiMap.put("[快哭了]", R.drawable.emoticon_tencent50);
        emojiMap.put("[阴险]", R.drawable.emoticon_tencent51);
        emojiMap.put("[亲亲]", R.drawable.emoticon_tencent52);
        emojiMap.put("[吓]", R.drawable.emoticon_tencent53);
        emojiMap.put("[可怜]", R.drawable.emoticon_tencent54);
        emojiMap.put("[菜刀]", R.drawable.emoticon_tencent55);
        emojiMap.put("[西瓜]", R.drawable.emoticon_tencent56);
        emojiMap.put("[啤酒]", R.drawable.emoticon_tencent57);
        emojiMap.put("[篮球]", R.drawable.emoticon_tencent58);
        emojiMap.put("[乒乓]", R.drawable.emoticon_tencent59);
        emojiMap.put("[咖啡]", R.drawable.emoticon_tencent60);
        emojiMap.put("[饭]", R.drawable.emoticon_tencent61);
        emojiMap.put("[猪头]", R.drawable.emoticon_tencent62);
        emojiMap.put("[玫瑰]", R.drawable.emoticon_tencent63);
        emojiMap.put("[凋谢]", R.drawable.emoticon_tencent64);
        emojiMap.put("[示爱]", R.drawable.emoticon_tencent65);
        emojiMap.put("[爱心]", R.drawable.emoticon_tencent66);
        emojiMap.put("[心碎]", R.drawable.emoticon_tencent67);
        emojiMap.put("[蛋糕]", R.drawable.emoticon_tencent68);
        emojiMap.put("[闪电]", R.drawable.emoticon_tencent69);
        emojiMap.put("[炸弹]", R.drawable.emoticon_tencent70);
        emojiMap.put("[刀]", R.drawable.emoticon_tencent71);
        emojiMap.put("[足球]", R.drawable.emoticon_tencent72);
        emojiMap.put("[瓢虫]", R.drawable.emoticon_tencent73);
        emojiMap.put("[便便]", R.drawable.emoticon_tencent74);
        emojiMap.put("[月亮]", R.drawable.emoticon_tencent75);
        emojiMap.put("[太阳]", R.drawable.emoticon_tencent76);
        emojiMap.put("[礼物]", R.drawable.emoticon_tencent77);
        emojiMap.put("[拥抱]", R.drawable.emoticon_tencent78);
        emojiMap.put("[强]", R.drawable.emoticon_tencent79);
        emojiMap.put("[弱]", R.drawable.emoticon_tencent80);
        emojiMap.put("[握手]", R.drawable.emoticon_tencent81);
        emojiMap.put("[胜利]", R.drawable.emoticon_tencent82);
        emojiMap.put("[抱拳]", R.drawable.emoticon_tencent83);
        emojiMap.put("[勾引]", R.drawable.emoticon_tencent84);
        emojiMap.put("[拳头]", R.drawable.emoticon_tencent85);
        emojiMap.put("[差劲]", R.drawable.emoticon_tencent86);
        emojiMap.put("[爱你]", R.drawable.emoticon_tencent87);
        emojiMap.put("[NO]", R.drawable.emoticon_tencent88);
        emojiMap.put("[OK]", R.drawable.emoticon_tencent89);
    }

    public static Integer getImgByName(String imgName) {
        Integer integer = emojiMap.get(imgName);
        return integer == null ? -1 : integer;
    }
}
