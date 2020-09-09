package com.coomix.app.all.ui.advance;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.coomix.app.all.R;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.ui.main.MainActivityParent;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;

import java.util.Arrays;
import java.util.UUID;

public class CommandListBleActivity extends BaseActivity {
    private BluetoothClient mClient;
    private String MAC;
    private String imei;
    private int commsyn;
    private int hornsyn;
    private UUID targetuuid=null;
    private UUID notifyuuid=null;
    private UUID writeuuid=null;
    private TextView getBleParams;
    private TextView getVersion;
    private TextView getStates;
    private TextView hornOn;
    private TextView hornClose;
    private TextView commuLoar;
    private TextView commuGprs;
    private TextView Timer;
    private TextView Noble;
    private EditText inputTimer;
    private Button submitTimer;
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command_list_bluetooth);
        if (MainActivityParent.instance != null) {
            mClient = new BluetoothClient(MainActivityParent.instance);
            MAC = MainActivityParent.instance.MAC;
            imei = MainActivityParent.instance.imei;
            targetuuid = MainActivityParent.instance.targetuuid;
            notifyuuid = MainActivityParent.instance.notifyuuid;
            writeuuid = MainActivityParent.instance.writeuuid;
        }
        commsyn=1;
        hornsyn=1;
        getBleParams = (TextView) findViewById(R.id.ble_find_params);
        getVersion = (TextView) findViewById(R.id.ble_find_version);
        getStates = (TextView) findViewById(R.id.ble_find_states);
        hornOn = (TextView) findViewById(R.id.ble_horn_on);
        hornClose = (TextView) findViewById(R.id.ble_horn_off);
        commuLoar = (TextView) findViewById(R.id.ble_commu_loar);
        commuGprs = (TextView) findViewById(R.id.ble_commu_gprs);
        Timer = (TextView) findViewById(R.id.ble_callback_timer);
        Noble = (TextView) findViewById(R.id.noble);
        inputTimer = (EditText) findViewById(R.id.callback_timer_input);
        submitTimer = (Button) findViewById(R.id.submit_callback_timer);
        if (!(MAC != null && imei != null && targetuuid != null && notifyuuid != null && writeuuid != null)) {
            getBleParams.setVisibility(View.INVISIBLE);
            getVersion.setVisibility(View.INVISIBLE);
            getStates.setVisibility(View.INVISIBLE);
            hornOn.setVisibility(View.INVISIBLE);
            hornClose.setVisibility(View.INVISIBLE);
            commuLoar.setVisibility(View.INVISIBLE);
            commuGprs.setVisibility(View.INVISIBLE);
            Timer.setVisibility(View.INVISIBLE);
            Noble.setVisibility(View.VISIBLE);
            inputTimer.setVisibility(View.INVISIBLE);
            submitTimer.setVisibility(View.INVISIBLE);

        }
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    @Override
    protected void onResume() {
        super.onResume();
//        mClient.notify(MAC, targetuuid, notifyuuid, new BleNotifyResponse() {
//            @Override
//            public void onNotify(UUID service, UUID character, byte[] value) {
//                String notfiyMessage = bytesToHex(value);
//                System.out.println("V/miio-bluetooth: ddebug notify:" + notfiyMessage);
                //checkprotocol(notfiyMessage);
//                                            6767086820012121320601000B00200868200121;
//                                            mClient.writeNoRsp(device.getAddress(), serviceUUID, characterUUID, bytes, new BleWriteResponse() {
//                                                @Override
//                                                public void onResponse(int code) {
//                                                    if (code == REQUEST_SUCCESS) {
//
//                                                    }
//                                                }
//                                            });
//            }
//            @Override
//            public void onResponse(int code) {
//            }
//        });

        getBleParams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askforParams();
            }
        });
        getVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askforVersion();
            }
        });
        getStates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askforStates();
            }
        });
        hornOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHorn();
            }
        });
        hornClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeHorn();
            }
        });
        commuLoar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLoar();
            }
        });
        commuGprs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGprs();
            }
        });
        submitTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTimer(inputTimer.getText().toString());
            }
        });
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static byte[] hexStringToByte(String hexString){
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private void sendCommand (String returndata) {
        System.out.println("V/miio-bluetooth: ddebug command write: " + returndata);
        byte[] byteList = hexStringToByte(returndata);
        int current = 0;
        while (current < byteList.length) {
            byte[] data = Arrays.copyOfRange(byteList, current, Math.min(byteList.length, current + 20));;
            mClient.write(MAC, targetuuid, writeuuid, data, new BleWriteResponse() {
                @Override
                public void onResponse(int code) {
                    System.out.println("V/miio-bluetooth: ddebug command response: " + code);
                }
            });
            current += 20;
        }
    }

    public void askforParams(){
        String Order = "PARAM#";
        try {
            byte[] utfOrder = Order.getBytes("UTF-8");
            Order = bytesToHex(utfOrder);
        }catch (Exception e){
            return;
        }
        String length =Integer.toHexString(Order.length()/2+7).toUpperCase();
        if(length.length()==1){
            length = "000"+length;
        }else if(length.length()==2){
            length = "00"+length;
        }
        String returndata="6767"+imei+"80"+length+"0001"+"0111223344"+Order;
        sendCommand(returndata);
    }

    public void askforVersion(){
        String Order = "VERSION#";
        try {
            byte[] utfOrder = Order.getBytes("UTF-8");
            Order = bytesToHex(utfOrder);
        }catch (Exception e){
            return;
        }
        String length =Integer.toHexString(Order.length()/2+7).toUpperCase();
        if(length.length()==1){
            length = "000"+length;
        }else if(length.length()==2){
            length = "00"+length;
        }
        String returndata="6767"+imei+"80"+length+"0001"+"0111223344"+Order;
        sendCommand(returndata);
    }

    public void askforStates(){
        String Order = "STATUS#";
        try {
            byte[] utfOrder = Order.getBytes("UTF-8");
            Order = bytesToHex(utfOrder);
        }catch (Exception e){
            return;
        }
        String length =Integer.toHexString(Order.length()/2+7).toUpperCase();
        if(length.length()==1){
            length = "000"+length;
        }else if(length.length()==2){
            length = "00"+length;
        }
        String returndata="6767"+imei+"80"+length+"0001"+"0111223344"+Order;
        sendCommand(returndata);
    }

    public void openHorn(){
        String Order = "HORN,0#";
        try {
            byte[] utfOrder = Order.getBytes("UTF-8");
            Order = bytesToHex(utfOrder);
        }catch (Exception e){
            return;
        }
        String length =Integer.toHexString(Order.length()/2+7).toUpperCase();
        if(length.length()==1){
            length = "000"+length;
        }else if(length.length()==2){
            length = "00"+length;
        }
        String syn = Integer.toHexString(hornsyn).toUpperCase();
        switch (syn.length()){
            case 1:syn = "000"+syn;
            case 2:syn = "00"+syn;
            case 3:syn = "0"+syn;
            case 4:syn = syn;
        }
        if(hornsyn==65535){
            hornsyn = 1;
        }
        String returndata="6767"+imei+"80"+length+syn+"0111223344"+Order;
        sendCommand(returndata);
    }

    public void closeHorn(){
        String Order = "HORN,1#";
        try {
            byte[] utfOrder = Order.getBytes("UTF-8");
            Order = bytesToHex(utfOrder);
        }catch (Exception e){
            return;
        }
        String length =Integer.toHexString(Order.length()/2+7).toUpperCase();
        if(length.length()==1){
            length = "000"+length;
        }else if(length.length()==2){
            length = "00"+length;
        }
        String syn = Integer.toHexString(hornsyn).toUpperCase();
        switch (syn.length()){
            case 1:syn = "000"+syn;
            case 2:syn = "00"+syn;
            case 3:syn = "0"+syn;
            case 4:syn = syn;
        }
        if(hornsyn==65535){
            hornsyn = 1;
        }
        String returndata="6767"+imei+"80"+length+"0001"+"0111223344"+Order;
        sendCommand(returndata);
    }

    public void setLoar(){
        String Order = "COMMUMODEN,0#";
        try {
            byte[] utfOrder = Order.getBytes("UTF-8");
            Order = bytesToHex(utfOrder);
        }catch (Exception e){
            return;
        }
        String length =Integer.toHexString(Order.length()/2+7).toUpperCase();
        if(length.length()==1){
            length = "000"+length;
        }else if(length.length()==2){
            length = "00"+length;
        }
        String syn = Integer.toHexString(commsyn).toUpperCase();
        switch (syn.length()){
            case 1:syn = "000"+syn;
            case 2:syn = "00"+syn;
            case 3:syn = "0"+syn;
            case 4:syn = syn;
        }
        if(commsyn==65535){
            commsyn = 1;
        }
        String returndata="6767"+imei+"80"+length+syn+"0111223344"+Order;
        sendCommand(returndata);
    }

    public void setGprs(){
        String Order = "COMMUMODEN,1#";
        try {
            byte[] utfOrder = Order.getBytes("UTF-8");
            Order = bytesToHex(utfOrder);
        }catch (Exception e){
            return;
        }
        String length =Integer.toHexString(Order.length()/2+7).toUpperCase();
        if(length.length()==1){
            length = "000"+length;
        }else if(length.length()==2){
            length = "00"+length;
        }
        String syn = Integer.toHexString(commsyn).toUpperCase();
        switch (syn.length()){
            case 1:syn = "000"+syn;
            case 2:syn = "00"+syn;
            case 3:syn = "0"+syn;
            case 4:syn = syn;
        }
        if(commsyn==65535){
            commsyn = 1;
        }
        String returndata="6767"+imei+"80"+length+syn+"0111223344"+Order;
        sendCommand(returndata);
    }

    public void setTimer(String timer){
        String Order = timer;
        if(Order==""){
            Toast.makeText(this,"请输入回传时间",Toast.LENGTH_LONG);
            return;
        }
        try {
            byte[] utfOrder = Order.getBytes("UTF-8");
            Order = bytesToHex(utfOrder);
        }catch (Exception e){
            return;
        }
        String length =Integer.toHexString(Order.length()/2+7).toUpperCase();
        if(length.length()==1){
            length = "000"+length;
        }else if(length.length()==2){
            length = "00"+length;
        }
        String returndata="6767"+imei+"80"+length+"0001"+"0111223344"+Order;
        sendCommand(returndata);
    }
}
