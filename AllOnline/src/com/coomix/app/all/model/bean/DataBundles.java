package com.coomix.app.all.model.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2019/3/18.
 */
public class DataBundles implements Serializable {
    private static final long serialVersionUID = 6639777036525247159L;
    private ArrayList<DataItem> bundles;

    public ArrayList<DataItem> getBundles() {
        return bundles;
    }

    public void setBundles(ArrayList<DataItem> bundles) {
        this.bundles = bundles;
    }
}
