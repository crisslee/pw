package com.coomix.app.redpacket.util;

import com.coomix.app.all.model.bean.Readpos;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by ssl
 *
 * @since 2017/3/31.
 */
public class Winners implements Serializable {
    private static final long serialVersionUID = 1L;

    private Readpos readpos;

    private ArrayList<WinnerInfo> winners;

    public ArrayList<WinnerInfo> getWinners() {
        return winners;
    }

    public void setWinners(ArrayList<WinnerInfo> winners) {
        this.winners = winners;
    }

    public Readpos getReadpos() {
        return readpos;
    }

    public void setReadpos(Readpos readpos) {
        this.readpos = readpos;
    }
}
