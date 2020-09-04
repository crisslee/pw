package com.coomix.app.all.log;

import android.content.Context;
import android.os.Build;

import com.coomix.app.all.Constant;
import com.coomix.app.framework.util.OSUtil;

import java.io.Serializable;

/**
 * LogUpload接口上传的信息
 */
public class LogUploadInfo implements Serializable{

    private static final long serialVersionUID = -5503840882890216476L;

    private int errorCode; // 错误码
    private String production;// string 否 无 硬件类型，如Oppo 4，Huawei P8
    private String devname;// string 否 无 设备名（用户自己填的）
    private String os;// string 否 无 取值为ios/android，不区分大小写
    private int osver;// string 否 无 操作系统的版本信息
    private String osextra;// string 否 无 操作系统相关的额外信息
    private int appid;// number 是 无 App的编号，1001为爱车安，84为酷米客公交
    private String appver;// string 否 无 APP版本号，如果是开发环境则带上(dev)
    private String access_by;// string 否 无 网络类型（wifi/移动/联通）
    private String n;// string 是 无 手机唯一指纹
    private String md5;// string 是 无 文件的md5值
    private String size;// string 是 无 文件的大小
    private String extra;// string 否 无 其他自定义信息
    private String[] local_path; // 日志本地保存路径

    public LogUploadInfo(Context context) {
        setProduction(Build.MODEL);
        setDevname(Build.DISPLAY);
        setOs("android");
        setOsver(Build.VERSION.SDK_INT);
        setOsextra(Build.VERSION.RELEASE);
        setAppid(Constant.COOMIX_APP_ID);
        setAppver(OSUtil.getAppVersionNameExtend(context));
        setN(OSUtil.getUdid(context));
        setLocal_path(GoomeLog.getInstance().getLogPaths());
        setExtra("AppLog");
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getProduction() {
        return production;
    }

    public void setProduction(String production) {
        this.production = production;
    }

    public String getDevname() {
        return devname;
    }

    public void setDevname(String devname) {
        this.devname = devname;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public int getOsver() {
        return osver;
    }

    public void setOsver(int osver) {
        this.osver = osver;
    }

    public String getOsextra() {
        return osextra;
    }

    public void setOsextra(String osextra) {
        this.osextra = osextra;
    }

    public int getAppid() {
        return appid;
    }

    public void setAppid(int appid) {
        this.appid = appid;
    }

    public String getAppver() {
        return appver;
    }

    public void setAppver(String appver) {
        this.appver = appver;
    }

    public String getAccess_by() {
        return access_by;
    }

    public void setAccess_by(String access_by) {
        this.access_by = access_by;
    }

    public String getN() {
        return n;
    }

    public void setN(String n) {
        this.n = n;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String[] getLocal_path() {
        return local_path;
    }

    public void setLocal_path(String[] local_path) {
        this.local_path = local_path;
    }

}
