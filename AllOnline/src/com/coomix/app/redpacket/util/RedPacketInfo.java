package com.coomix.app.redpacket.util;

import com.coomix.app.all.model.bean.Readpos;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by ssl on 2017/2/13.
 */
public class RedPacketInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /*********红包初始状态*******/
    public static final int RP_STATUS_INITIAL = 0;
    /*********红包预支付状态*******/
    public static final int RP_STATUS_PREPAY = 1;
    /*********红包支付成功状态----已经激活 可以抢*******/
    public static final int RP_STATUS_IN_PROGRESS = 2;
    /*********红包已发送*******/
    public static final int RP_STATUS_SENDED = 3;
    /*********红包已抢完状态*******/
    public static final int RP_STATUS_LOOT_ALL = 4;
    /*********红包过期状态*******/
    public static final int RP_STATUS_EXPIRED = 5;
    /*********红包异常1*******/
    public static final int RP_STATUS_EXCEPTION1 = 6;
    /*********红包异常2*******/
    public static final int RP_STATUS_EXCEPTION2 = 7;

    public static final int RANGE_OUT_OF_RANGE = 0;
    public static final int RANGE_IN_RANGE = 1;

    //分页信息
    private Readpos readpos;

    //红包信息
    private String redpacket_id;
    private String uid; //谁发的红包
    private String name;
    private String img;
    private long amount;  //金额
    private int packet_num; //红包个数
    private int status;
    private int packet_type; //普通，随机
    private int display_type; //社区 群聊等
    private String hello_words; //祝福语

    //分配信息
    private int alloc_num; //已经分配的个数
    private long alloc_amount; //已经分配的金额
    //领取范围
    private int alloc_range;
    //是否在领取范围
    private int in_range = RANGE_IN_RANGE;
    private int alive_times; //几秒被抢光
    private int allocated; //自己是否抢到红包（0 未抢到，1已经抢到）
    private ArrayList<AllocInfo> alloc_infos; //所有领取者的信息
    private AllocInfo my_alloc_info; //自己领取的红包信息
    private RedPacketExtendInfo extend_item;
    private String hxuser;
    private String hxpwd;
    private int vtype; //用户加V标识

    private String toChatId; //私聊，群聊，聊天室等ID

    private ArrayList<RedPacketSlideShow> slideshows; // 抽奖活动轮播图

    public String getRedpacket_id() {
        return redpacket_id;
    }

    public void setRedpacket_id(String redpacket_id) {
        this.redpacket_id = redpacket_id;
    }

    public String getUid() {
        if (uid == null) {
            return "";
        }
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        if (name == null) {
            name = "";
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public int getPacket_num() {
        return packet_num;
    }

    public void setPacket_num(int packet_num) {
        this.packet_num = packet_num;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getHello_words() {
        return hello_words;
    }

    public void setHello_words(String hello_words) {
        this.hello_words = hello_words;
    }

    public int getAlloc_num() {
        return alloc_num;
    }

    public void setAlloc_num(int alloc_num) {
        this.alloc_num = alloc_num;
    }

    public int getAlive_times() {
        return alive_times;
    }

    public void setAlive_times(int alive_times) {
        this.alive_times = alive_times;
    }

    public Readpos getReadpos() {
        return readpos;
    }

    public void setReadpos(Readpos readpos) {
        this.readpos = readpos;
    }

    public int getAllocated() {
        return allocated;
    }

    public void setAllocated(int allocated) {
        this.allocated = allocated;
    }

    public AllocInfo getMy_alloc_info() {
        return my_alloc_info;
    }

    public void setMy_alloc_info(AllocInfo my_alloc_info) {
        this.my_alloc_info = my_alloc_info;
    }

    public ArrayList<AllocInfo> getAlloc_infos() {
        if (alloc_infos == null) {
            alloc_infos = new ArrayList<AllocInfo>();
        }
        return alloc_infos;
    }

    public void setAlloc_infos(ArrayList<AllocInfo> alloc_infos) {
        this.alloc_infos = alloc_infos;
    }

    public int getPacket_type() {
        return packet_type;
    }

    public void setPacket_type(int packet_type) {
        this.packet_type = packet_type;
    }

    public int getDisplay_type() {
        return display_type;
    }

    public void setDisplay_type(int display_type) {
        this.display_type = display_type;
    }

    public RedPacketExtendInfo getExtend_item() {
        return extend_item;
    }

    public void setExtend_item(RedPacketExtendInfo extend_item) {
        this.extend_item = extend_item;
    }

    public long getAlloc_amount() {
        return alloc_amount;
    }

    public void setAlloc_amount(long alloc_amount) {
        this.alloc_amount = alloc_amount;
    }

    public int getAlloc_range() {
        return alloc_range;
    }

    public void setAlloc_range(int alloc_range) {
        this.alloc_range = alloc_range;
    }

    public int getIn_range() {
        return in_range;
    }

    public void setIn_range(int in_range) {
        this.in_range = in_range;
    }

    public boolean isCommunityRedpacket() {
        return display_type == RedPacketConstant.RP_DISPLAY_COMMUNITY_TOPIC
            || display_type == RedPacketConstant.RP_DISPLAY_COMMUNITY_REPLY;
    }

    public ArrayList<RedPacketSlideShow> getSlideshows() {
        return slideshows;
    }

    public void setSlideshows(ArrayList<RedPacketSlideShow> slideshows) {
        this.slideshows = slideshows;
    }

    public String getHxuser() {
        return hxuser;
    }

    public void setHxuser(String hxuser) {
        this.hxuser = hxuser;
    }

    public String getHxpwd() {
        return hxpwd;
    }

    public void setHxpwd(String hxpwd) {
        this.hxpwd = hxpwd;
    }

    public int getVtype() {
        return vtype;
    }

    public void setVtype(int vtype) {
        this.vtype = vtype;
    }

    public String getToChatId() {
        if (toChatId == null) {
            toChatId = "";
        }
        return toChatId;
    }

    public void setToChatId(String toChatId) {
        this.toChatId = toChatId;
    }
}
