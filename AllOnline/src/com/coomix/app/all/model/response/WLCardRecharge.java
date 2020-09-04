package com.coomix.app.all.model.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class WLCardRecharge implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private int userId; //设备id
    private String imei; //imei号
    private String userName; //设备名
    private String msisdn; //物联卡号
    private String c_out_time;//1272124800, //物联卡到期时间
    private String platform_out_time;//1272124800, //平台到期时间
    private String user_out_time;//1272124800, //用户到期时间
    private int combo_id;// 1,
    private String batch;//"201806", //批次
    @SerializedName("package")
    private String package_content;//"30M", //套餐
    private int price;//1000, //充值价格 单位分
    private String productType;
    private String custMobile;

    /***卡状态,0:未知,1:测试期,2:静默期,3:正常使用,4:停机,5:销户,6:预销户,7:单向停机,8:休眠,9:过户,99:号码不存在 
     只有3,4能够充值，停机3个月不给充值**/
    private int card_status;

    private long custId; //1000000, //所属用户id
    private String custName;//谷米.爱车安", //所属用户名

    //ui选择状态判断
    private boolean selected;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getC_out_time() {
        return c_out_time;
    }

    public void setC_out_time(String c_out_time) {
        this.c_out_time = c_out_time;
    }

    public String getPlatform_out_time() {
        return platform_out_time;
    }

    public void setPlatform_out_time(String platform_out_time) {
        this.platform_out_time = platform_out_time;
    }

    public String getUser_out_time() {
        return user_out_time;
    }

    public void setUser_out_time(String user_out_time) {
        this.user_out_time = user_out_time;
    }

    public int getCombo_id() {
        return combo_id;
    }

    public void setCombo_id(int combo_id) {
        this.combo_id = combo_id;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getPackage_content() {
        return package_content;
    }

    public void setPackage_content(String package_content) {
        this.package_content = package_content;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getCard_status() {
        return card_status;
    }

    public void setCard_status(int card_status) {
        this.card_status = card_status;
    }

    public long getCustId() {
        return custId;
    }

    public void setCustId(long custId) {
        this.custId = custId;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getCustMobile() {
        return custMobile;
    }

    public void setCustMobile(String custMobile) {
        this.custMobile = custMobile;
    }

    public boolean isCanRecharge() {
        if(combo_id != 0 && (card_status == 3 || card_status == 4)){
            //只有谷米物联卡并且状态是3,4的才可以充值
            return true;
        }
        return false;
    }
}
