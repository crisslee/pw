package com.coomix.app.all.model.bean;

import java.io.Serializable;

public class SubAccount implements Serializable {

    private static final long serialVersionUID = 2208477659079082732L;
    public String id;
    public String pid;
    public String name; //子账号名称
    public String showname; //子账号显示名称
    public boolean haschild;
    public int owndevnum;
    public int totaldevnum;

    public void copySubAccount(SubAccount subAccount) {
        if (subAccount != null) {
            this.id = subAccount.id;
            this.pid = subAccount.pid;
            this.name = subAccount.name;
            this.showname = subAccount.showname;
            this.haschild = subAccount.haschild;
            this.owndevnum = subAccount.owndevnum;
            this.totaldevnum = subAccount.totaldevnum;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof SubAccount) {
            SubAccount temp = (SubAccount) obj;
            if (id.equals(temp.id) && pid.equals(temp.pid) && name.equals(temp.name) && showname.equals(temp.showname)
                    && haschild == temp.haschild && owndevnum == temp.owndevnum && totaldevnum == temp.totaldevnum) {
                return true;
            } else {
                return false;
            }
        }
        return super.equals(obj);
    }
}
