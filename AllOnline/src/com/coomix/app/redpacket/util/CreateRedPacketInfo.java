package com.coomix.app.redpacket.util;

import java.io.Serializable;

/**
 * Created by think on 2017/2/13.
 */
public class CreateRedPacketInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private int display_type; //1群红包，2社区红包，3私信红包
    private int packet_type; //红包类型普通，随机等，参见RedPacketConstant.java中定义的value
    private long amount; //红包金额
    private int pay_platform; //支付平台 1 微信，  2支付宝， 3 零钱
    private int pay_manner; // 各个支付平台对应的不同编号  谷米零钱App支付 5
    private int packet_num; //人数
    private String hello_words; //祝福语
    private int alloc_range; //红包范围
    private RedPacketExtendInfo extend_item;

    public int getDisplay_type() {
        return display_type;
    }

    public void setDisplay_type(int display_type) {
        this.display_type = display_type;
    }

    public int getPacket_type() {
        return packet_type;
    }

    public void setPacket_type(int packet_type) {
        this.packet_type = packet_type;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public int getPay_platform() {
        return pay_platform;
    }

    public void setPay_platform(int pay_platform) {
        this.pay_platform = pay_platform;
    }

    public int getPay_manner() {
        return pay_manner;
    }

    public void setPay_manner(int pay_manner) {
        this.pay_manner = pay_manner;
    }

    public int getPacket_num() {
        return packet_num;
    }

    public void setPacket_num(int packet_num) {
        this.packet_num = packet_num;
    }

    public String getHello_words() {
        return hello_words;
    }

    public void setHello_words(String hello_words) {
        this.hello_words = hello_words;
    }

    public RedPacketExtendInfo getExtend_item() {
        return extend_item;
    }

    public void setExtend_item(RedPacketExtendInfo extend_item) {
        this.extend_item = extend_item;
    }

    public int getAlloc_range() {
        return alloc_range;
    }

    public void setAlloc_range(int alloc_range) {
        this.alloc_range = alloc_range;
    }
}
