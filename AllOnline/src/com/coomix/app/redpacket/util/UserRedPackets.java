package com.coomix.app.redpacket.util;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by ssl on 2017/2/13.
 */
public class UserRedPackets implements Serializable {
    private static final long serialVersionUID = 1L;
    private ArrayList<RedPacketInfo> redpackets;

    public ArrayList<RedPacketInfo> getRedpackets() {
        return redpackets;
    }

    public void setRedpackets(ArrayList<RedPacketInfo> redpackets) {
        this.redpackets = redpackets;
    }
}
