package com.gxdcnjq.sharedbikesmis.ui.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gxdcnjq.sharedbikesmis.MapApplication;
import com.gxdcnjq.sharedbikesmis.R;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity {
    private static final String TAG = "BluetoothActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION_COARSE_LOCATION = 2;

    private MapApplication app;

    private BluetoothAdapter mBluetoothAdapter;
    private ListView mListView;
    private FloatingActionButton mScanButton;


//    private ArrayAdapter<String> mArrayAdapter;
    private DeviceListAdapter adapter;
    private List<BluetoothDevice> mDevices = new ArrayList<>();
    private List<BluetoothDevice> pairedDevices;
    private UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        //设置全屏和全面屏适配
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        getWindow().setAttributes(lp);

        app = (MapApplication) getApplication();

        // 检查是否支持蓝牙
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        app.setmBluetoothAdapter(mBluetoothAdapter);
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 检查蓝牙是否已经开启，如果没有开启则请求开启
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // 检查是否有定位权限，如果没有则请求权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_PERMISSION_COARSE_LOCATION);
        }


        // 获取ListView控件
        mListView = findViewById(R.id.list_view_devices);
//        mArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
//        mListView.setAdapter(mArrayAdapter);

        List<String> deviceList = new ArrayList<>(); // 假设您有一个设备列表数据
        adapter = new DeviceListAdapter(this, deviceList);
        mListView.setAdapter(adapter);


        // 获取ScanButton控件
        mScanButton = findViewById(R.id.button_scan);



        // 设置ScanButton的点击事件，用于扫描周围的蓝牙设备
        mScanButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
//                mArrayAdapter.clear();
                adapter.clear();
                mDevices.clear();

                pairedDevices = new ArrayList<>(mBluetoothAdapter.getBondedDevices());
                for (BluetoothDevice device : pairedDevices) {
                    mDevices.add(device);
//                    mArrayAdapter.add("[已配对]" + device.getName() + "\n" + device.getAddress());
                    adapter.add("[已配对]" + device.getName() + "\n" + device.getAddress());
                }

                try {
                    // 注册广播接收器
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                    registerReceiver(mReceiver, filter);
                } catch (Exception e) {
                    Log.e("Pan", e.toString());
                }

                // 开始扫描蓝牙设备
                boolean success = mBluetoothAdapter.startDiscovery();
                Toast.makeText(BluetoothActivity.this, "正在扫描蓝牙设备", Toast.LENGTH_SHORT).show();
//                Toast.makeText(BluetoothActivity.this, String.valueOf(success), Toast.LENGTH_SHORT).show();
            }
        });


        // 设置ListView的点击事件，用于进行蓝牙设备的配对和连接
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice device = mDevices.get(position);

                // 尝试进行配对
                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    try {
                        device.createBond();
                    } catch (Exception e) {
                        Log.e(TAG, "配对失败", e);
                        Toast.makeText(BluetoothActivity.this, "配对失败", Toast.LENGTH_SHORT).show();
                    }
                }
                // 尝试进行连接
                try {
                    Log.d("Pan","尝试连接");
//                    bleManager.connect(device);

                    mSocket = device.createRfcommSocketToServiceRecord(mUUID);
                    mSocket.connect();
                    Toast.makeText(BluetoothActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                    app.setmSocket(mSocket);    //连接成功后，将mSocket传到全局Application中
                    app.setDevice(device);      //连接成功后，将device传到全局Application中

                    // 连接成功后，关闭蓝牙设备的扫描和连接
                    try {
                        mBluetoothAdapter.cancelDiscovery();
                        unregisterReceiver(mReceiver);
                    } catch (Exception e) {
                        Log.e("Pan", e.toString());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "连接失败", e);
                    Toast.makeText(BluetoothActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                    app.setmSocket(null);
                    app.setDevice(null);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 关闭蓝牙设备的扫描
        try {
            mBluetoothAdapter.cancelDiscovery();
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
            Log.e("Pan", e.toString());
        }
        //在Application中关闭连接,此处不关闭
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 处理开启蓝牙的请求结果
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "蓝牙已开启", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "蓝牙未开启", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // 处理获取定位权限的请求结果
        if (requestCode == REQUEST_PERMISSION_COARSE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "已获取定位权限", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "未获取定位权限", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    // 广播接收器，用于接收蓝牙设备扫描的结果
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("Pan", action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 扫描到一个蓝牙设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!mDevices.contains(device)) {
                    mDevices.add(device);
//                    mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    adapter.add(device.getName() + "\n" + device.getAddress());
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    // 扫描完成
                    Toast.makeText(context, "扫描完成", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    public class DeviceListAdapter extends ArrayAdapter<String> {

        public DeviceListAdapter(Context context, List<String> devices) {
            super(context, 0, devices);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_device, parent, false);
            }

            TextView textViewDeviceName = convertView.findViewById(R.id.text_view_device_name);
            TextView textViewMacAddress = convertView.findViewById(R.id.text_view_mac_address);

            String str = getItem(position);
            String[] lines = str.split("\n");
            String deviceName = lines[0];
            String macAddress = lines[1];
            textViewDeviceName.setText(deviceName);
            textViewMacAddress.setText(macAddress);


            return convertView;
        }
    }


}