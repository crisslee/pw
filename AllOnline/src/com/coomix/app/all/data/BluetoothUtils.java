package com.coomix.app.all.data;

import android.content.Context;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.beacon.Beacon;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;

public class BluetoothUtils {
    private static BluetoothClient bluetoothClient = null;

    /*
        新增藍牙client
     */
    public static BluetoothClient initBlueToothClient(Context context){
        if(bluetoothClient==null){
            bluetoothClient = new BluetoothClient(context);
        }
        return bluetoothClient;
    }

    /*
        藍牙掃描
     */
    public static void searchBleDevice(){
        SearchRequest request = new SearchRequest.Builder().searchBluetoothLeDevice(3000,3).searchBluetoothClassicDevice(5000).searchBluetoothLeDevice(2000).build();
        bluetoothClient.search(request, new SearchResponse() {
            @Override
            public void onSearchStarted() {

            }

            @Override
            public void onDeviceFounded(SearchResult device) {
                Beacon beacon = new Beacon(device.scanRecord);

            }

            @Override
            public void onSearchStopped() {

            }

            @Override
            public void onSearchCanceled() {

            }
        });
    }




}
