package com.coomix.app.all.model.response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RespLockList extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private LockList data;

    public LockList getData(){
        return data;
    }

    public void setData(LockList list){
        this.data = list;
    }


    public List<LockInfo> getLockInfoList(){
        if(data == null || data.getImeis().isEmpty()){
            return new ArrayList<>();
        }

        List<LockInfo> lockInfos = new ArrayList<>();
        for(int i = 0; i < data.getImeis().size(); i++){
            LockInfo info = new LockInfo();
            info.imei = data.getImeis().get(i);
            info.eid = data.getEids().get(i);
            info.lname = data.getLnames().get(i);
            info.devName = data.getDevnames().get(i);
            lockInfos.add(info);
        }

        return lockInfos;

    }

    private class LockList implements Serializable{
        private static final long serialVersionUID = 1L;
        List<String> imeis;
        List<String> eids;
        List<String> lnames;
        List<String> devnames;
        public List<String> getImeis(){
            if(imeis == null){
                imeis = new ArrayList<>();
            }
            return imeis;
        }
        public void setImeis(List<String> imeis){
            this.imeis = imeis;
        }


        public List<String> getEids(){
            if(eids == null){
                eids = new ArrayList<>();
            }
            return eids;
        }
        public void setEids(List<String> eids){
            this.eids = eids;
        }


        public List<String> getLnames(){
            if(lnames == null){
                lnames = new ArrayList<>();
            }
            return lnames;
        }
        public void setLnames(List<String> lnames){
            this.lnames = lnames;
        }

        public List<String> getDevnames(){
            if(devnames == null){
                devnames = new ArrayList<>();
            }
            return devnames;
        }
        public void setDevnames(List<String> devnames){
            this.devnames = devnames;
        }

    }
}
