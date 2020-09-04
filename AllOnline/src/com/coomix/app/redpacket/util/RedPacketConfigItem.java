package com.coomix.app.redpacket.util;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by ssl
 *
 * @since 2017/2/13.
 */
public class RedPacketConfigItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long DEF_MAX_SINGLE = 20000;
    private final long DEF_MIN_AMOUNT = 1;
    private final long DEF_MAX_AMOUNT = 2000000;
    private final int DEF_MAX_NUM = 100;
    private final int[] DEF_ALLOC_RANGES = { 2000, 5000, 10000, 20000, 0 };

    private int packet_type; //红包类型，普通红包-随机-裂变等
    private long max_single = 0; //单位为分，默认20000
    private long min_amount = 0; //单位为分，默认是1
    private long max_amount = 0; //单位为分，私信红包默认为20000，群红包默认为2000000
    private int max_num = 0; //群红包默认为100
    private String hello_words; //祝福语
    private int[] alloc_ranges;
    private ArrayList<RedPacketCommit> commit_items; //4.1.0版本不处理

    public long getMax_single() {
        if (max_single <= 0) {
            max_single = DEF_MAX_SINGLE;
        }
        return max_single;
    }

    public void setMax_single(long max_single) {
        this.max_single = max_single;
    }

    public long getMin_amount() {
        if (min_amount <= 0) {
            return DEF_MIN_AMOUNT;
        }
        return min_amount;
    }

    public void setMin_amount(long min_amount) {
        this.min_amount = min_amount;
    }

    public long getMax_amount() {
        if (max_amount <= 0) {
            max_amount = DEF_MAX_AMOUNT;
        }
        return max_amount;
    }

    public void setMax_amount(long max_amount) {
        this.max_amount = max_amount;
    }

    public long getMax_num() {
        if (max_num <= 0) {
            max_num = DEF_MAX_NUM;
        }
        return max_num;
    }

    public void setMax_num(int max_num) {
        this.max_num = max_num;
    }

    public String getHello_words() {
        return hello_words;
    }

    public void setHello_words(String hello_words) {
        this.hello_words = hello_words;
    }

    public ArrayList<RedPacketCommit> getCommit_items() {
        return commit_items;
    }

    public void setCommit_items(ArrayList<RedPacketCommit> commit_items) {
        this.commit_items = commit_items;
    }

    public int getPacket_type() {
        return packet_type;
    }

    public void setPacket_type(int packet_type) {
        this.packet_type = packet_type;
    }

    public int[] getAlloc_ranges() {
        if (alloc_ranges == null) {
            alloc_ranges = DEF_ALLOC_RANGES;
        }
        return alloc_ranges;
    }

    public void setAlloc_ranges(int[] alloc_ranges) {
        this.alloc_ranges = alloc_ranges;
    }
}
