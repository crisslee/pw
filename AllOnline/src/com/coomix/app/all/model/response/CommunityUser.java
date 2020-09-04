package com.coomix.app.all.model.response;

import com.coomix.app.all.Constant;
import com.coomix.app.all.util.StringUtil;
import com.coomix.app.all.util.CommunityUtil;
import java.io.Serializable;
import java.util.ArrayList;

public class CommunityUser implements Serializable {

    private static final long serialVersionUID = 113235689L;
    /**
     * 汽车在线用户名
     */
    private String account;
    private String uid = "0";
    private String name;
    private String img;
    private int listen = -1;// 是否关注用户， //0表示未关注；1表示已关注 ；2表示已拉黑该用户；3表示用户自己
    private String tel;
    private int sex;
    private String label; // 签名档
    private int grade;
    private int point;
    private String local_code;// location对应的编码（如860101,
    // 前两位表示国家，中间两位表示省份，后两位表示城市）
    private String location;

    private String sid;// 所属社区
    private String citycode;// 所属社区城市编码

    private String ticket;// 登录签名
    private String sign;// 请求校验值

    private String wxid = "";  //微信的openid

    private boolean isOperationSpecialist = false;

    private String isLogin;
    private long score;
    private long create_time;
    private long modify_time;
    /**
     * 　社区接口请求的城市citycode
     */
    private String querycity = Constant.COMMUNITY_CITYCODE;
    /** 环信用户信息 */
    //    private EmUser emUser;
    private String display_uid; //用于展示个用户的uid
    private int vtype; //加V的判断--第0位表示是否官方，第一位表示是否官方认证
    private int complain_flag;  // 用户是否被投诉过，0:未被投诉，1:被投诉过

    private ArrayList<Picture> background;

    private String hxAccount;//与社区账号绑定的环信账号

    private String hxPwd;//环信密码

    public int getVtype()
    {
        return vtype;
    }

    public void setVtype(int vtype)
    {
        this.vtype = vtype;
    }


    public CommunityUser() {
        super();
    }

    public CommunityUser(String ticket) {
        super();
        this.ticket = ticket;
    }

    public CommunityUser(String name, String img, int marker) {
        super();
        this.name = name;
        this.img = img;
        this.listen = marker;
    }

    public CommunityUser(String uid, String name, String img) {
        super();
        this.uid = uid;
        this.name = name;
        this.img = img;
    }

    public CommunityUser(String uid, String name, String img, String label) {
        super();
        this.uid = uid;
        this.name = name;
        this.img = img;
        this.label = label;
    }

    public CommunityUser(String uid, String name, String img, int marker, String label) {
        super();
        this.uid = uid;
        this.name = name;
        this.img = img;
        this.listen = marker;
        this.label = label;
    }

    public String getWxid() {
        return wxid;
    }

    public void setWxid(String wxid) {
        this.wxid = wxid;
    }


    public boolean isOperationSpecialist() {
        return isOperationSpecialist;
    }

    public void setOperationSpecialist(int operationSpecialist) {
        isOperationSpecialist = (operationSpecialist != 0);
    }
    public void setOperationSpecialist(boolean operationSpecialist) {
        isOperationSpecialist = operationSpecialist;
    }

    public boolean isWechatBinded() {
        if(isOperationSpecialist()) { //运营账号不需要绑定wx
            return true;
        } else {
            return !(wxid == null || "".equals(wxid));
        }
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public int getMarker() {
        return listen;
    }

    public void setMarker(int marker) {
        this.listen = marker;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getListen() {
        return listen;
    }

    public void setListen(int listen) {
        this.listen = listen;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getCitycode() {
        return citycode;
    }

    public void setCitycode(String citycode) {
        this.citycode = citycode;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getIsLogin() {
        return isLogin;
    }

    public void setIsLogin(String isLogin) {
        this.isLogin = isLogin;
    }

    @Override
    public String toString() {
        return "User [uid="
            + uid
            + ", name="
            + name
            + ", img="
            + img
            + ", listen="
            + listen
            + ", tel="
            + tel
            + ", sex="
            + sex
            + ", label="
            + label
            + ", grade="
            + grade
            + ", point="
            + point
            + ", sid="
            + sid
            + ", citycode="
            + citycode
            + ", ticket="
            + ticket
            + ", sign="
            + sign
            + ", isLogin="
            + isLogin
            + "]";
    }

    public void setScore(long score) {
        this.score = score;
    }

    public long getScore() {
        return score;
    }

    public void setCreate_time(long create_time) {
        this.create_time = create_time;
    }

    public long getCreate_time() {
        return create_time;
    }

    public void setModify_time(long modify_time) {
        this.modify_time = modify_time;
    }

    public long getModify_time() {
        return modify_time;
    }

    public void setLocal_code(String local_code) {
        this.local_code = local_code;
    }

    public String getLocal_code() {
        return local_code;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void setBackground(ArrayList<Picture> background) {
        this.background = background;
    }

    public ArrayList<Picture> getBackground() {
        return background;
    }

    public void setQuerycity(String querycity) {
        this.querycity = querycity;
    }

    public String getQuerycity() {
        if (querycity == null || querycity.isEmpty()) {
            return Constant.COMMUNITY_CITYCODE;
        }
        return querycity;
    }

    public boolean isLogin() {
        if (StringUtil.isTrimEmpty(ticket)) {
            return false;
        }
        return true;
    }

    public boolean isBindPhone() {
        if(CommunityUtil.isEmptyString(getTel())) {
            return false;
        }
        return true;
    }

    public String getHxAccount() {
        return hxAccount;
    }

    public void setHxAccount(String hxAccount) {
        this.hxAccount = hxAccount;
    }

    public String getHxPwd() {
        return hxPwd;
    }

    public void setHxPwd(String hxPwd) {
        this.hxPwd = hxPwd;
    }
}
