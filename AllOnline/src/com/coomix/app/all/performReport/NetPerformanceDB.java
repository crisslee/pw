package com.coomix.app.all.performReport;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * 网络性能上报用于为数据库存储信息的类
 * Created by Administrator on 2016/11/8.
 */

@DatabaseTable(tableName = "NET_PERFORMANCE_DB")
public class NetPerformanceDB implements Serializable
{
    private static final long serialVersionUID = 1L;

    @DatabaseField(generatedId=true)
    private int id; //数据库ID
    @DatabaseField
    private String nameid; //由接口名和方法名组成
    @DatabaseField
    private int    errorcode;//错误码。0：没出错，非0：约定好的特定错误对应特定code
    @DatabaseField
    private int    timecost;//接口访问耗时，网络请求开始到结束的耗时
    @DatabaseField
    private String detail;//详细信息，网络请求的完整URL
    @DatabaseField
    private String starttime; //接口调用的开始时间
    @DatabaseField
    private String upsize;//上传时候数据总量
    @DatabaseField
    private String downsize;//非上传时候从服务器返回的数据量，因为压缩的缘故可能获取的时候数据量是-1，-1置为0
    @DatabaseField
    private String httpheader;//网络请求时候的http header
    @DatabaseField
    private int networktype;

    public String getDetail()
    {
        return detail;
    }

    public void setDetail(String detail)
    {
        this.detail = detail;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getNameid()
    {
        return nameid;
    }

    public void setNameid(String nameid)
    {
        this.nameid = nameid;
    }

    public int getErrorcode()
    {
        return errorcode;
    }

    public void setErrorcode(int errorcode)
    {
        this.errorcode = errorcode;
    }

    public int getTimecost()
    {
        return timecost;
    }

    public void setTimecost(int timecost)
    {
        this.timecost = timecost;
    }

    public String getUpsize()
    {
        return upsize;
    }

    public void setUpsize(String size)
    {
        this.upsize = size;
    }

    public String getDownsize()
    {
        return downsize;
    }

    public void setDownsize(String size)
    {
        this.downsize = size;
    }

    public String getStarttime()
    {
        return starttime;
    }

    public void setStarttime(String time)
    {
        this.starttime = time;
    }

    public String getHttpheader()
    {
        return httpheader;
    }

    public void setHttpheader(String httpheader)
    {
        this.httpheader = httpheader;
    }

    public int getNetworktype()
    {
        return networktype;
    }

    public void setNetworktype(int networktype)
    {
        this.networktype = networktype;
    }
}
