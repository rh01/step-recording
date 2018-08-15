package com.example.test.ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import cc.nctu1210.api.koala6x.KoalaDevice;
import cc.nctu1210.api.koala6x.KoalaServiceManager;

public class MainActivity extends AppCompatActivity {

    /**
     * Layout Component
     */
    private Button btScan;
    private Button btDisconnect;
    private ListView listView;

    /**
     * Device List
     */
    private ArrayAdapter<String> Adapter;
    private ArrayList<String> koala = new ArrayList<String>();

    /**
     * 宣告蓝牙服务，adapter与扫描器
     */
    private KoalaServiceManager mServiceManager;
    private BluetoothAdapter mBluetoothAdapter;
    public static final int REQUEST_ENABLE_BT = 1;
    private BluetoothLeScanner mBLEScanner;

    /**
     * 宣告存储的阵列
     */
    private static ArrayList<KoalaDevice> mDevices = new ArrayList<KoalaDevice>();
    public ArrayList<String> connectDevice = new ArrayList<String>();

    /** 宣告flag*/
    private boolean startScan = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btScan = (Button) findViewById(R.id.bt_scan);
        btDisconnect = (Button) findViewById(R.id.bt_disconnect);
        listView = (ListView) findViewById(R.id.list_device);

        /** 注册按钮的点击事件*/
        btScan.setOnClickListener(scanListener);
        btDisconnect.setOnClickListener(disconnectListener);

        /** 创建Adapter，并注册ListView的点击事件的物件以及Adapter*/
        Adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, koala);
//        listView.setOnItemClickListener(listClickListener);
        listView.setAdapter(Adapter);

        // BLE
        // 获取Adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // 询问是否开启蓝牙，
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            // 获得扫描器
            mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode,resultCode,data);
    }


    private Button.OnClickListener scanListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Toast.makeText(MainActivity.this, "Click Scan Button", Toast.LENGTH_LONG).show();
            if (!startScan) {
                startScan = true;
                btScan.setText(R.string.stop_scan);
                // 资料库清空
                mDevices.clear();
                koala.clear();
                scanBLEDevice(startScan); // 开始扫描 BLE DEVICE
            } else {
                startScan = false;
                scanBLEDevice(startScan); // 开始扫描 BLE DEVICE
                btScan.setText(R.string.scan);
            }
        }
    };

    private void scanBLEDevice(boolean scanFlag) {
        if (scanFlag) {
            mBLEScanner.startScan(mScanCallback);
        }else{
            mBLEScanner.stopScan(mScanCallback);
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            final ScanResult scanResult = result;
            final BluetoothDevice device = scanResult.getDevice();
            new Thread(){
                @Override
                public void run(){
                    if (device != null) {
                        final KoalaDevice p = new KoalaDevice(device, scanResult.getRssi(), scanResult.getScanRecord().getBytes());
                        int position = findKoalaDevice(device.getAddress());
                        if (position == -1) {
                            mDevices.add(p);
                            koala.add(p.getDevice().getAddress());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                }
            }.start();;
        }
    };

    private int findKoalaDevice(String macAddr) {
        if (mDevices.size() == 0) {
            return -1;
        }
        for (int i=0; i<mDevices.size(); i++) {
            KoalaDevice tmpDevice = mDevices.get(i);
            if (macAddr.matches(tmpDevice.getDevice().getAddress())) {
                return i;
            }
        }
        return -1;
    }

    private Button.OnClickListener disconnectListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(MainActivity.this, "Click Disconnect Button", Toast.LENGTH_LONG).show();
        }
    };

    private ListView.OnItemClickListener listClickListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            // TODO:
        }
    };





}
