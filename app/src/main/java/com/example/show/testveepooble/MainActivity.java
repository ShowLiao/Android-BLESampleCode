package com.example.show.testveepooble;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.inuker.bluetooth.library.Code;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.inuker.bluetooth.library.utils.BluetoothUtils;
import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;
import com.veepoo.protocol.VPOperateManager;
import com.veepoo.protocol.listener.base.IABleConnectStatusListener;
import com.veepoo.protocol.listener.base.IABluetoothStateListener;
import com.veepoo.protocol.listener.base.IBleWriteResponse;
import com.veepoo.protocol.listener.base.IConnectResponse;
import com.veepoo.protocol.listener.base.INotifyResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import com.veepoo.protocol.listener.data.IBPDetectDataListener;

import com.veepoo.protocol.listener.data.ISportDataListener;
import com.veepoo.protocol.model.datas.BpData;

import com.veepoo.protocol.model.datas.SportData;
import com.veepoo.protocol.model.enums.EBPDetectModel;


public class MainActivity extends AppCompatActivity {

    VPOperateManager mVpoperateManager;         //for veepoo protocol

    private final static String TAG = MainActivity.class.getSimpleName();
    private final static String YOUR_APPLICATION = "timaimee";

    List<SearchResult> mListData = new ArrayList<>();
    List<String> mListAddress = new ArrayList<>();

    TextView infoText;
    String deviceMac = "";

    WriteResponse writeResponse = new WriteResponse();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initLog();
        Logger.t(TAG).i("onSearchStarted");
        mVpoperateManager = mVpoperateManager.getMangerInstance(getApplicationContext());

        infoText = (TextView)findViewById(R.id.InfoView);
        registerBluetoothStateListener();
    }

    private void initLog() {
        Logger.init(YOUR_APPLICATION)
                .methodCount(0)
                .methodOffset(0)
                .hideThreadInfo()
                .logLevel(LogLevel.FULL)
                .logAdapter(new CustomLogAdapter());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean scanDevice() {

        if (!mListAddress.isEmpty()) {
            mListAddress.clear();
        }
        if (!mListData.isEmpty()) {
            mListData.clear();
            Log.e("is empty", "ScanDevice");
        }

        if (!BluetoothUtils.isBluetoothEnabled()) {
            Toast.makeText(getApplicationContext(), "蓝牙没有开启", Toast.LENGTH_SHORT).show();
            return true;
        }
        mVpoperateManager.startScanDevice(mSearchResponse);
        return false;
    }

    /**
     * 蓝牙打开or关闭状态
     */
    private void registerBluetoothStateListener() {
        mVpoperateManager.registerBluetoothStateListener(mBluetoothStateListener);
    }


    /**
     * 监听系统蓝牙的打开和关闭的回调状态
     */
    private final IABleConnectStatusListener mBleConnectStatusListener = new IABleConnectStatusListener() {

        @Override
        public void onConnectStatusChanged(String mac, int status) {
            if (status == Constants.STATUS_CONNECTED) {
                Logger.t(TAG).i("STATUS_CONNECTED");
            } else if (status == Constants.STATUS_DISCONNECTED) {
                Logger.t(TAG).i("STATUS_DISCONNECTED");
            }
        }
    };

    /**
     * 监听蓝牙与设备间的回调状态
     */
    private final IABluetoothStateListener mBluetoothStateListener = new IABluetoothStateListener() {
        @Override
        public void onBluetoothStateChanged(boolean openOrClosed) {
            Logger.t(TAG).i("open=" + openOrClosed);
        }
    };

    private final SearchResponse mSearchResponse = new SearchResponse() {
        @Override
        public void onSearchStarted() {
            Logger.t(TAG).i("onSearchStarted");
        }

        @Override
        public void onDeviceFounded(final SearchResult device) {
            Logger.t(TAG).i(String.format("**device for %s-%s-%d", device.getName(), device.getAddress(), device.rssi));
            deviceMac = device.getAddress();   //"BL-P1-FE:BA:1A:D1:CC:93"; ->有可能會有多個Device 有需要可以建List讓User選
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!mListAddress.contains(device.getAddress())) {
                        mListData.add(device);
                        mListAddress.add(device.getAddress());
                    }
                    Collections.sort(mListData, new DeviceCompare());

                }
            });
        }

        @Override
        public void onSearchStopped() {
            Logger.t(TAG).i("onSearchStopped");
        }

        @Override
        public void onSearchCanceled() {
            Logger.t(TAG).i("onSearchCanceled");
        }
    };

    private void connectDevice(final String mac) {

        mVpoperateManager.registerConnectStatusListener(mac, mBleConnectStatusListener);

        mVpoperateManager.connectDevice(mac, new IConnectResponse() {

            @Override
            public void connectState(int code, BleGattProfile profile, boolean isoadModel) {
                if (code == Code.REQUEST_SUCCESS) {
                    //蓝牙与设备的连接状态
                    Logger.t(TAG).i("连接成功");

                } else {
                    Logger.t(TAG).i("连接失败");
                }
            }
        }, new INotifyResponse() {
            @Override
            public void notifyState(int state) {
                if (state == Code.REQUEST_SUCCESS) {
                    //蓝牙与设备的连接状态
                    Logger.t(TAG).i("监听成功-可进行其他操作");
                    infoText.setText("监听成功-可进行其他操作");
                    //startActivity(new Intent(mContext, OperaterActivity.class));
                } else {
                    Logger.t(TAG).i("监听失败，重新连接");
                    infoText.setText("监听失败-請重試");
                }

            }
        });
    }

    class WriteResponse implements IBleWriteResponse {

        @Override
        public void onResponse(int code) {
            Logger.t(TAG).i("write cmd status:" + code);

        }
    }

    public void onScanClick(View view) {
        Log.e("onScan Click", "ScanDevice");
        scanDevice();
    }

    public void onConnectDeviceClick(View view) {
        Log.e("onConnectDeviceClick", "conn Device");
        connectDevice(deviceMac);
    }

    public void onSetTimeClick(View view) {
        Log.e("onSetTimeClick", "set time ");

    }

    public void onWalkClick(View view) {
        VPOperateManager.getMangerInstance(getApplicationContext()).readSportStep(writeResponse, new ISportDataListener() {
            @Override
            public void onSportDataChange(SportData sportData) {
                String message = "当前计步:\n" + sportData.getStep();
                Logger.t(TAG).i(message);
                infoText.setText(message);
            }
        });
    }

    public void onDetectClick(View view) {
        infoText.setText("Monitoring please wait....");
        VPOperateManager.getMangerInstance(getApplicationContext()).startDetectBP(writeResponse, new IBPDetectDataListener() {
            @Override
            public void onDataChange(BpData bpData) {
                String message = "BpData date statues:\n" + bpData.toString();
                Logger.t(TAG).i(message);
                infoText.setText(message);
            }
        }, EBPDetectModel.DETECT_MODEL_PUBLIC);

    }

    public void onDetectStopClick(View view) {
        VPOperateManager.getMangerInstance(getApplicationContext()).stopDetectBP(writeResponse, EBPDetectModel.DETECT_MODEL_PUBLIC);
        infoText.setText("detect stop!!");
    }

}
