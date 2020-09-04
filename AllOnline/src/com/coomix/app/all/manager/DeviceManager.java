package com.coomix.app.all.manager;

import android.text.TextUtils;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.model.bean.DeviceInfo;
import com.coomix.app.all.model.bean.SubAccount;
import com.coomix.app.all.model.response.DevType;
import com.coomix.app.all.model.response.RespAccountGroupInfo;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by ssl on 2017/11/27.
 */
public class DeviceManager {
    private static DeviceManager instance;
    private final int MAX_ENTRIES = 1000;
    /******账号信息*******/
    private ArrayList<SubAccount> listSubaccounts = new ArrayList<SubAccount>();
    private HashMap<String, ArrayList<DeviceInfo>> hmDeviceList = new HashMap<String, ArrayList<DeviceInfo>>();
    /****缓存解析出的地址**/
    private HashMap<String, String> hmAddressCache = new LinkedHashMap<String, String>();
    private SubAccount mCurrentSubaccount;
    private boolean isDeviceOneMore = true;
    private ArrayList<DeviceInfo> listValidDevices = new ArrayList<DeviceInfo>();
    private HashMap<Integer, String> hmGroupInfos = new HashMap<>();
    private static final byte[] LOCK = new byte[0];
    /** type -> {@link DevType} */
    private HashMap<String, DevType> devTypeMap = new HashMap<>();

    private DeviceManager() {
        initCurrentSubaccount();
    }

    public static synchronized DeviceManager getInstance() {
        if (instance == null) {
            instance = new DeviceManager();
        }
        return instance;
    }

    public void initCurrentSubaccount() {
        mCurrentSubaccount = new SubAccount();
        mCurrentSubaccount.id = "0";
        mCurrentSubaccount.pid = "0";
        mCurrentSubaccount.name = AllOnlineApp.sAccount;
        mCurrentSubaccount.showname = AllOnlineApp.sToken != null ? AllOnlineApp.sToken.name : "";
    }

    public void setDeviceListByAccount(String accountId, ArrayList<DeviceInfo> listDevices) {
        if (hmDeviceList == null) {
            hmDeviceList = new HashMap<String, ArrayList<DeviceInfo>>();
        }
        hmDeviceList.put(accountId, listDevices);

        setValidDeviceList(listDevices);
    }

    public void setCurrentAccountDeviceList(ArrayList<DeviceInfo> listDevices) {
        setDeviceListByAccount(getCurrentSubaccount().id, listDevices);
    }

    public ArrayList<DeviceInfo> getDeviceListByAccount(String accountId) {
        if (hmDeviceList == null || hmDeviceList.get(accountId) == null) {
            return new ArrayList<DeviceInfo>();
        }
        return hmDeviceList.get(accountId);
    }

    public boolean isContainsDeviceByAccount(String accountId) {
        if (hmDeviceList == null) {
            return false;
        }
        return hmDeviceList.containsKey(accountId);
    }

    public void setSubaccountsList(ArrayList<SubAccount> subaccounts, String pid) {
        if (listSubaccounts == null) {
            listSubaccounts = new ArrayList<SubAccount>();
        } else {
            listSubaccounts.clear();
        }
        if (subaccounts != null) {
            if (TextUtils.isEmpty(pid)) {
                pid = "0";
            }
            for (SubAccount subAccount : subaccounts) {
                if (subAccount != null) {
                    subAccount.pid = pid;
                }
            }
            listSubaccounts.addAll(subaccounts);
        }
    }

    public void addSubaccountsList(ArrayList<SubAccount> subaccounts, String pid) {
        if (listSubaccounts == null) {
            listSubaccounts = new ArrayList<SubAccount>();
        }
        if (subaccounts != null) {
            if (TextUtils.isEmpty(pid)) {
                pid = "0";
            }
            for(int i = 0; i < listSubaccounts.size();){
                SubAccount subAccount = listSubaccounts.get(i);
                if(subAccount != null && subAccount.pid != null && subAccount.pid.equals(pid)){
                    //删除已经存在的数据
                    listSubaccounts.remove(subAccount);
                    continue;
                }
                i++;
            }
            for (SubAccount subAccount : subaccounts) {
                if (subAccount != null) {
                    subAccount.pid = pid;
                    listSubaccounts.add(subAccount);
                }
            }
        }
    }

    public ArrayList<SubAccount> getSubaccountsList() {
        if (listSubaccounts == null) {
            listSubaccounts = new ArrayList<SubAccount>();
        }
        return listSubaccounts;
    }

    public void setCurrentSubaccount(SubAccount subaccount) {
        this.mCurrentSubaccount = subaccount;
    }

    public SubAccount getCurrentSubaccount() {
        if (mCurrentSubaccount == null || mCurrentSubaccount.name == null) {
            initCurrentSubaccount();
        }
        return mCurrentSubaccount;
    }

    public boolean isOneMoreDevice() {
        return isDeviceOneMore;
    }

    public void setOneMoreDevice(boolean bOneMore) {
        this.isDeviceOneMore = bOneMore;
    }

    public int getDeviceListSize() {
        return getCurrentAccountDeviceList().size();
    }

    public ArrayList<DeviceInfo> getCurrentAccountDeviceList() {
        return getDeviceListByAccount(getCurrentSubaccount().id);
    }

    public void clearData() {
        if (hmDeviceList != null) {
            hmDeviceList.clear();
        }
        if (listSubaccounts != null) {
            listSubaccounts.clear();
        }
        if (hmGroupInfos != null) {
            hmGroupInfos.clear();
        }
        initCurrentSubaccount();
    }

    //取小数点后面4位，多余的直接丢掉
    private String formatDecimal(int scale, double data) {
        BigDecimal bd = new BigDecimal(String.valueOf(data));
        bd = bd.setScale(scale, BigDecimal.ROUND_FLOOR);
        String bds = bd.toString();
        return bds;
    }

    private String latlngString(double lat, double lng) {
        return formatDecimal(4, lat) + formatDecimal(4, lng);
    }

    public String getCachedAddress(double lat, double lng) {
        synchronized (LOCK) {
            String latlngStr = latlngString(lat, lng);
            if (hmAddressCache != null && hmAddressCache.containsKey(latlngStr)) {
                return hmAddressCache.get(latlngStr);
            }
        }
        return null;
    }

    //设置经纬度和地址的缓存
    public void setCachedAddress(double lat, double lng, String address) {
        synchronized (LOCK) {
            if (hmAddressCache == null) {
                hmAddressCache = new LinkedHashMap<String, String>();
            }
            int size = hmAddressCache.size();
            if (size >= MAX_ENTRIES) {
                int moreCount = size - MAX_ENTRIES;
                Iterator it = hmAddressCache.keySet().iterator();
                while (it.hasNext() && moreCount >= 0) {
                    it.next();
                    it.remove();
                    moreCount--;
                }
            }
            hmAddressCache.put(latlngString(lat, lng), address);
        }
    }

    /****过滤出有效的设备列表*/
    public void setValidDeviceList(ArrayList<DeviceInfo> listDevices) {
//        if (AllOnlineApp.isBeyondLimit) {
//            return;
//        }
        if (listValidDevices == null) {
            listValidDevices = new ArrayList<DeviceInfo>();
        } else {
            listValidDevices.clear();
        }

        if (listDevices == null || listDevices.size() <= 0) {
            return;
        }

        for (DeviceInfo device : listDevices) {
            if (device != null && !TextUtils.isEmpty(device.getImei()) && !listValidDevices.contains(device) &&
                    device.getState() != DeviceInfo.STATE_DISABLE && device.getState() != DeviceInfo.STATE_EXPIRE) {
                listValidDevices.add(device);
            }
        }
    }

    public ArrayList<DeviceInfo> getValidDeviceList() {
        return listValidDevices;
    }

    public int getValidDevIndex(DeviceInfo deviceInfo) {
        if (deviceInfo == null) {
            return -1;
        }
        for (int i = 0; i < listValidDevices.size(); i++) {
            DeviceInfo dev = listValidDevices.get(i);
            if (deviceInfo.getImei().equals(dev.getImei())) {
                return i;
            }
        }
        return 0;
    }

    /******过滤出离线的设备列表****/
    public ArrayList<DeviceInfo> getOffLineDeviceList() {
        return getOffLineDeviceList(getCurrentAccountDeviceList());
    }

    /******获取未上线的设备列表****/
    public ArrayList<DeviceInfo> getUnuseDeviceList() {
        return getUnuseDeviceList(getCurrentAccountDeviceList());
    }

    /******过滤出在线的设备列表****/
    public ArrayList<DeviceInfo> getOnlineDeviceList() {
        return getOnlineDeviceList(getCurrentAccountDeviceList());
    }

    /******过滤出离线的设备列表****/
    public ArrayList<DeviceInfo> getOffLineDeviceList(ArrayList<DeviceInfo> list) {
        ArrayList<DeviceInfo> filtered = new ArrayList<DeviceInfo>();
        try {
            for (DeviceInfo device : list) {
                if (device != null && (device.getState() == DeviceInfo.STATE_EXPIRE
                    || device.getState() == DeviceInfo.STATE_OFFLINE)) {
                    filtered.add(device);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filtered;
    }

    /******获取未上线的设备列表****/
    public ArrayList<DeviceInfo> getUnuseDeviceList(ArrayList<DeviceInfo> list) {
        ArrayList<DeviceInfo> filtered = new ArrayList<DeviceInfo>();
        try {
            for (DeviceInfo device : list) {
                if (device != null && (device.getState() == DeviceInfo.STATE_DISABLE)) {
                    filtered.add(device);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filtered;
    }


    /******过滤出在线的设备列表****/
    public ArrayList<DeviceInfo> getOnlineDeviceList(ArrayList<DeviceInfo> list) {
        ArrayList<DeviceInfo> filtered = new ArrayList<DeviceInfo>();
        try {
            for (DeviceInfo device : list) {
                if (device != null && (device.getState() == DeviceInfo.STATE_RUNNING
                    || device.getState() == DeviceInfo.STATE_STOP)) {
                    filtered.add(device);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filtered;
    }

    public void addGroupInMap(ArrayList<RespAccountGroupInfo.GroupInfo> listGroups) {
        if (listGroups == null) {
            return;
        }
        if (hmGroupInfos == null) {
            hmGroupInfos = new HashMap<Integer, String>();
        }
        for (RespAccountGroupInfo.GroupInfo groupInfo : listGroups) {
            hmGroupInfos.put(groupInfo.group_id, groupInfo.group_name);
        }
    }

    public String getGroupNameById(int groupId) {
        if (hmGroupInfos == null) {
            return null;
        }
        return hmGroupInfos.get(groupId);
    }

    public void addDevTypes(List<DevType> list) {
        if (list == null || list.size() <= 0) {
            return;
        }
        for (DevType dt : list) {
            devTypeMap.put(dt.getGtype(), dt);
        }
    }

    public boolean isGoomeType(String type) {
        DevType dt = devTypeMap.get(type);
        return dt != null && dt.is_goometype();
    }
}
