package com.coomix.app.redpacket.util;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by ssl on 2017/2/13.
 */
public class RedPacketConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<RedPacketConfigItem> confs;

    public ArrayList<RedPacketConfigItem> getConfs() {
        return confs;
    }

    public void setConfs(ArrayList<RedPacketConfigItem> confs) {
        this.confs = confs;
    }

    public RedPacketConfigItem getRedPacketConfigItemByType(int packet_type) {
        if (confs != null) {
            for (RedPacketConfigItem configItem : confs) {
                if (configItem != null && configItem.getPacket_type() == packet_type) {
                    return configItem;
                }
            }
        }
        return new RedPacketConfigItem();
    }
}
