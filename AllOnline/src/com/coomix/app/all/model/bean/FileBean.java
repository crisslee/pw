package com.coomix.app.all.model.bean;

import com.zhy.tree.bean.TreeNodeHaschild;
import com.zhy.tree.bean.TreeNodeId;
import com.zhy.tree.bean.TreeNodeLabel;
import com.zhy.tree.bean.TreeNodePid;
import com.zhy.tree.bean.TreeNodeShowLabel;

public class FileBean {
    @TreeNodeId
    private int _id;
    @TreeNodePid
    private int parentId;
    @TreeNodeLabel
    private String name;
    @TreeNodeShowLabel
    private String showname;
    @TreeNodeHaschild
    private boolean haschild;
    private long length;
    private String desc;

    public FileBean(int _id, int parentId, String name, String showname, boolean hasChild) {
        super();
        this._id = _id;
        this.parentId = parentId;
        this.name = name;
        this.showname = showname;
        this.haschild = hasChild;
    }
}
