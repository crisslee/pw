package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.StatisticsKey;
import com.coomix.app.all.model.bean.StatisticsSummary;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2018/11/6.
 */
public class RespStatistics extends RespBase implements Serializable {
    private static final long serialVersionUID = 2215204658051868229L;
    private StatisticsSummary summary;
    private StatisticsKey keys;
    private ArrayList<ArrayList<String>> data;

    public StatisticsSummary getSummary() {
        return summary;
    }

    public void setSummary(StatisticsSummary summary) {
        this.summary = summary;
    }

    public StatisticsKey getKeys() {
        return keys;
    }

    public void setKeys(StatisticsKey keys) {
        this.keys = keys;
    }

    public ArrayList<ArrayList<String>> getData() {
        return data;
    }

    public void setData(ArrayList<ArrayList<String>> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RespStatistics{" +
            "summary=" + summary +
            ", keys=" + keys +
            ", data=" + data +
            '}';
    }
}
