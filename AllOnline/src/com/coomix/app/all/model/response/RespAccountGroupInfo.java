package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.SubAccount;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * File Description:
 *
 * @author ssl
 * @since 2018/8/10.
 */
public class RespAccountGroupInfo extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private AccountAndGroup data;

    public AccountAndGroup getData() {
        return data;
    }

    public void setData(AccountAndGroup data) {
        this.data = data;
    }

    public class AccountAndGroup implements Serializable{
        private int customer_id;
        private int owndevnum;
        private int totaldevnum;
        private boolean isBeyondLimit;
        private ArrayList<GroupInfo> group;
        private ArrayList<SubAccount> children;

        public int getCustomer_id() {
            return customer_id;
        }

        public void setCustomer_id(int customer_id) {
            this.customer_id = customer_id;
        }

        public int getOwndevnum() {
            return owndevnum;
        }

        public void setOwndevnum(int owndevnum) {
            this.owndevnum = owndevnum;
        }

        public int getTotaldevnum() {
            return totaldevnum;
        }

        public void setTotaldevnum(int totaldevnum) {
            this.totaldevnum = totaldevnum;
        }

        public boolean getIsBeyondLimit() {
            return isBeyondLimit;
        }

        public void setIsBeyondLimit(boolean isBeyondLimit) {
            this.isBeyondLimit = isBeyondLimit;
        }

        public ArrayList<GroupInfo> getGroup() {
            return group;
        }

        public void setGroup(ArrayList<GroupInfo> group) {
            this.group = group;
        }

        public ArrayList<SubAccount> getChildren() {
            if(children == null){
                children = new ArrayList<SubAccount>();
            }
            return children;
        }

        public void setChildren(ArrayList<SubAccount> children) {
            this.children = children;
        }
    }

    public class GroupInfo implements Serializable{
        public int group_id;
        public String group_name;
    }
}
