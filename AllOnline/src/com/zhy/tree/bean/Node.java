package com.zhy.tree.bean;

import com.coomix.app.all.R;

import java.util.ArrayList;
import java.util.List;

/**
 * http://blog.csdn.net/lmj623565791/article/details/40212367
 * 
 * @author zhy
 *
 */
public class Node {

    private int id;
    /**
     * 根节点pId为0
     */
    private int pId = 0;

    private String name;

    private String showname;

    public boolean isHaschild() {
        return haschild;
    }

    public void setHaschild(boolean haschild) {
        this.haschild = haschild;
    }

    private boolean haschild;

    /**
     * 当前的级别
     */
    private int level;

    /**
     * 是否展开
     */
    private boolean isExpand = false;

    public boolean checked = false;

    private int icon = R.drawable.ic_arrow_right;

    /**
     * 下一级的子Node
     */
    private List<Node> children = new ArrayList<Node>();

    /**
     * 父Node
     */
    private Node parent;

    public Node() {
    }

    public Node(int id, int pId, String name, String showname, boolean bHaschild) {
        super();
        this.id = id;
        this.pId = pId;
        this.name = name;
        this.showname = showname;
        this.haschild = bHaschild;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getpId() {
        return pId;
    }

    public void setpId(int pId) {
        this.pId = pId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShowName() {
        return showname;
    }

    public void setShowName(String showname) {
        this.showname = showname;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isExpand() {
        return isExpand;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isChecked() {
        return checked;
    }

    public List<Node> getChildren() {
        return children;
    }

    public int getChildrenSize() {
        if(children == null) {
            return 0;
        } else {
            return children.size();
        }
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    /**
     * 是否为跟节点
     * 
     * @return
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * 判断父节点是否展开
     * 
     * @return
     */
    public boolean isParentExpand() {
        if (parent == null)
            return false;
        return parent.isExpand();
    }

    /**
     * 是否是叶子界点
     * 
     * @return
     */
    public boolean isLeaf() {
        return (children == null || children.size() == 0);
    }

    /**
     * 获取level
     */
    public int getLevel() {
        return parent == null ? 0 : parent.getLevel() + 1;
    }

    /**
     * 设置展开
     * 
     * @param isExpand
     */
    public void setExpand(boolean isExpand) {
        this.isExpand = isExpand;
        if (!isExpand) {

            for (Node node : children) {
                node.setExpand(isExpand);
            }
        }
    }

}
