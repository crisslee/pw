package com.coomix.app.all.ui.update;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2016/4/19.
 */
public class GoomeUpdateInfo implements Parcelable {

    public boolean update;
    public String verCode;
    public String verName;
    public String desc;
    public String url;
    public String newMd5;
    public String targetSize;

    // patch update info
    public boolean patchUpdate;
    public String  patchCode;
    public String  patchDesc;
    public String  patchNewMd5;
    public String  patchTargetSize;
    public String  patchUrl;

    public String getNewMd5() {
		return newMd5;
	}

	public void setNewMd5(String newMd5) {
		this.newMd5 = newMd5;
	}

	public String getTargetSize() {
		return targetSize;
	}

	public void setTargetSize(String targetSize) {
		this.targetSize = targetSize;
	}

    public GoomeUpdateInfo() {

    }

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public String getVerCode() {
        return verCode;
    }

    public void setVerCode(String verCode) {
        this.verCode = verCode;
    }

    public String getVerName() {
        return verName;
    }

    public void setVerName(String verName) {
        this.verName = verName;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isPatchUpdate()
    {
        return patchUpdate;
    }

    public void setPatchUpdate(boolean patchUpdate)
    {
        this.patchUpdate = patchUpdate;
    }

    public String getPatchCode()
    {
        return patchCode;
    }

    public void setPatchCode(String patchCode)
    {
        this.patchCode = patchCode;
    }

    public String getPatchDesc()
    {
        return patchDesc;
    }

    public void setPatchDesc(String patchDesc)
    {
        this.patchDesc = patchDesc;
    }

    public String getPatchNewMd5()
    {
        return patchNewMd5;
    }

    public void setPatchNewMd5(String patchNewMd5)
    {
        this.patchNewMd5 = patchNewMd5;
    }

    public String getPatchTargetSize()
    {
        return patchTargetSize;
    }

    public void setPatchTargetSize(String patchTargetSize)
    {
        this.patchTargetSize = patchTargetSize;
    }

    public String getPatchUrl()
    {
        return patchUrl;
    }

    public void setPatchUrl(String patchUrl)
    {
        this.patchUrl = patchUrl;
    }

    protected GoomeUpdateInfo(Parcel in) {
        update = in.readByte() == 1;
        verCode = in.readString();
        verName = in.readString();
        desc = in.readString();
        url = in.readString();
        newMd5 = in.readString();
        targetSize = in.readString();

        // patch info
        patchUpdate = in.readByte() == 1;
        patchCode = in.readString();
        patchDesc = in.readString();
        patchNewMd5 = in.readString();
        patchTargetSize = in.readString();
        patchUrl = in.readString();
    }

    public static final Creator<GoomeUpdateInfo> CREATOR = new Creator<GoomeUpdateInfo>() {
        @Override
        public GoomeUpdateInfo createFromParcel(Parcel in) {
            return new GoomeUpdateInfo(in);
        }

        @Override
        public GoomeUpdateInfo[] newArray(int size) {
            return new GoomeUpdateInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (update ? 1 : 0));
        dest.writeString(verCode);
        dest.writeString(verName);
        dest.writeString(desc);
        dest.writeString(url);
        dest.writeString(newMd5);
        dest.writeString(targetSize);

        // patch info
        dest.writeByte((byte) (patchUpdate ? 1 : 0));
        dest.writeString(patchCode);
        dest.writeString(patchDesc);
        dest.writeString(patchNewMd5);
        dest.writeString(patchTargetSize);
        dest.writeString(patchUrl);
    }

    @Override
    public String toString() {
        return "GoomeUpdateInfo{" +
                "update=" + update +
                ", verCode='" + verCode + '\'' +
                ", verName='" + verName + '\'' +
                ", desc='" + desc + '\'' +
                ", url='" + url + '\'' +
                ", newMd5='" + newMd5 + '\'' +
                ", targetSize='" + targetSize + '\'' +
                ", patchUpdate=" + patchUpdate +
                ", patchCode='" + patchCode + '\'' +
                ", patchDesc='" + patchDesc + '\'' +
                ", patchNewMd5='" + patchNewMd5 + '\'' +
                ", patchTargetSize='" + patchTargetSize + '\'' +
                ", patchUrl='" + patchUrl + '\'' +
                '}';
    }
}
