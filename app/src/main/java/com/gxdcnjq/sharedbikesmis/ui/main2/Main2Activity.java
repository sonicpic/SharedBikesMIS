package com.gxdcnjq.sharedbikesmis.ui.main2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.gxdcnjq.sharedbikesmis.MapApplication;
import com.gxdcnjq.sharedbikesmis.R;
import com.gxdcnjq.sharedbikesmis.constant.ApiConstants;
import com.gxdcnjq.sharedbikesmis.constant.MacConstants;
import com.gxdcnjq.sharedbikesmis.databinding.ActivityMain2Binding;
import com.gxdcnjq.sharedbikesmis.entity.Bike;
import com.gxdcnjq.sharedbikesmis.entity.BikesData;
import com.gxdcnjq.sharedbikesmis.entity.Response;
import com.gxdcnjq.sharedbikesmis.ui.bluetooth.BluetoothActivity;
import com.gxdcnjq.sharedbikesmis.ui.mine.PersonalCenterActivity;
import com.gxdcnjq.sharedbikesmis.ui.repair.RepairActivity;
import com.gxdcnjq.sharedbikesmis.utils.OKHttpUtil;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class Main2Activity extends AppCompatActivity implements AMapLocationListener, LocationSource, AMap.OnMarkerClickListener {

    private ActivityMain2Binding binding;
    //请求权限码
    private static final int REQUEST_PERMISSIONS = 9527;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;
    private TextView tv_latitude;
    private TextView tv_longitude;
    private TextView tv_location;
    private TextView tv_device_name;
    private TextView tv_device_mac;
    private MapView mapView;
    //地图控制器
    private AMap aMap = null;
    //位置更改监听
    private LocationSource.OnLocationChangedListener mListener;
    //定位样式
    private MyLocationStyle myLocationStyle = new MyLocationStyle();

    private FloatingActionButton fabMenu;
    private FloatingActionButton fabRide;
    private FloatingActionButton fabRepair;
    private FloatingActionButton fabMine;

    private MaterialButton btnRide;

    private boolean isMenuOpen = false;

    private double currentLatitude;
    private double currentLongitude;
    boolean unlock = false;

    private MapApplication app;
    private Polygon fencePolygon;

    // 创建一个 Handler 对象
    Handler handler = new Handler();
    // 定义一个 Runnable 对象
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            uploadLocation(app.getCurrentBikeDevice().getDevice().getAddress(), currentLatitude, currentLongitude);
            handler.postDelayed(this, 2000); // 2000 毫秒表示2秒
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        setContentView(R.layout.activity_main2);

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

        //绑定控件
        app = (MapApplication) getApplication();
        tv_latitude = binding.tvLatitude;
        tv_longitude = binding.tvLongitude;
        tv_location = binding.tvLocation;
        fabMenu = binding.fabMenu;
        fabRide = binding.fabRide;
        fabRepair = binding.fabRepair;
        fabMine = binding.fabMine;
        tv_device_name = binding.tvDeviceName;
        tv_device_mac = binding.tvDeviceMac;
        btnRide = binding.ride;

        //初始化地图
        initMap(savedInstanceState);

        //初始化定位
        initLocation();

        //检查Android版本
        checkingAndroidVersion();

        // 设置浮动按钮的点击事件监听器
        fabMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(app.getCurrentBikeDevice()!=null && unlock){
                    Toast.makeText(Main2Activity.this, "骑行中，请稍后操作", Toast.LENGTH_SHORT).show();
                }else{
                    if (isMenuOpen) {
                        // 如果菜单已展开，则收起菜单
                        closeMenu();
                    } else {
                        // 如果菜单未展开，则展开菜单
                        openMenu();
                    }
                }
            }
        });

        // 设置扫描设备按钮的点击事件监听器
        fabRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处理骑行按钮的点击事件
                // TODO: 添加您的处理逻辑
                Intent intent = new Intent(Main2Activity.this, BluetoothActivity.class);
                startActivity(intent);
                closeMenu();
            }
        });

        // 设置报修按钮的点击事件监听器
        fabRepair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处理报修按钮的点击事件
                // TODO: 添加您的处理逻辑
                Intent intent = new Intent(Main2Activity.this, RepairActivity.class);
                startActivity(intent);
                closeMenu();
            }
        });

        // 设置我的按钮
        fabMine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 添加您的处理逻辑
                Intent intent = new Intent(Main2Activity.this, PersonalCenterActivity.class);
                startActivity(intent);
            }
        });

        // 设置骑行/结束骑行按钮的点击事件监听器
        btnRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(app.getCurrentBikeDevice() == null){
                    Toast.makeText(Main2Activity.this, "共享单车未连接", Toast.LENGTH_SHORT).show();
                }else{
                    if (unlock) {
                        if(isPointInFence(new LatLng(currentLatitude, currentLongitude))){
                            Toast.makeText(Main2Activity.this, "禁停区内禁止关锁", Toast.LENGTH_SHORT).show();
                        }else{
                            lock(app.getCurrentBikeDevice().getDevice().getAddress(), currentLatitude, currentLongitude);
                        }
                    } else {
                        unlock(app.getCurrentBikeDevice().getDevice().getAddress(), currentLatitude, currentLongitude);
                    }
                }

            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //销毁定位客户端，同时销毁本地定位服务。
        mLocationClient.onDestroy();
        mapView.onDestroy();
        binding = null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();
        //初始化定位
        initLocation();
        refreshBike();
        refreshCurrentBike();
        //在Fragment执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        //在Fragment执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //在Fragment执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 检查Android版本
     */
    private void checkingAndroidVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Android6.0及以上先获取权限再定位
            requestPermission();

        } else {
            //Android6.0以下直接定位
            mLocationClient.startLocation();
        }
    }

    /**
     * 动态请求权限
     */
    @AfterPermissionGranted(REQUEST_PERMISSIONS)
    private void requestPermission() {
        String[] permissions = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        if (EasyPermissions.hasPermissions(this, permissions)) {
            //true 有权限 开始定位
            Toast.makeText(this, "已获得权限，可以定位啦！", Toast.LENGTH_SHORT).show();

            //启动定位
            mLocationClient.startLocation();
        } else {
            //false 无权限
            Toast.makeText(this, "需要权限", Toast.LENGTH_SHORT).show();
            EasyPermissions.requestPermissions(this, "需要权限", REQUEST_PERMISSIONS, permissions);
        }
    }

    /**
     * 请求权限结果
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //设置权限请求结果
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * 初始化定位
     */
    private void initLocation() {
        //初始化定位
        try {
            mLocationClient = new AMapLocationClient(this.getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mLocationClient != null) {
            //设置定位回调监听
            mLocationClient.setLocationListener(this);
            //初始化AMapLocationClientOption对象
            mLocationOption = new AMapLocationClientOption();
            //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //获取最近3s内精度最高的一次定位结果：
            //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
            mLocationOption.setOnceLocationLatest(true);
            //设置是否返回地址信息（默认返回地址信息）
            mLocationOption.setNeedAddress(true);
            //设置定位请求超时时间，单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
            mLocationOption.setHttpTimeOut(20000);
            //关闭缓存机制，高精度定位会产生缓存。
            mLocationOption.setLocationCacheEnable(false);
            //给定位客户端对象设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
        }
    }


    /**
     * 接收异步返回的定位结果
     *
     * @param aMapLocation
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                //地址
                String address = aMapLocation.getAddress();
                currentLatitude = aMapLocation.getLatitude();
                currentLongitude = aMapLocation.getLongitude();
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("纬度：" + aMapLocation.getLatitude() + "\n");
                stringBuffer.append("经度：" + aMapLocation.getLongitude() + "\n");
                stringBuffer.append("地址：" + address + "\n");

//                tv_latitude.setText("纬度 "+aMapLocation.getLatitude());
//                tv_longitude.setText("经度 "+aMapLocation.getLongitude());
                tv_location.setText(address);


                //停止定位，但本地定位服务并不会被销毁
                mLocationClient.stopLocation();

                //显示地图定位结果
                if (mListener != null) {
                    //显示系统图标
                    mListener.onLocationChanged(aMapLocation);
                }
            } else {
                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
    }

    /**
     * 初始化地图
     *
     * @param savedInstanceState
     */
    private void initMap(Bundle savedInstanceState) {
        mapView = binding.mapView;
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mapView.onCreate(savedInstanceState);
        //初始化地图控制器对象
        aMap = mapView.getMap();
        setFence();

        // 给地图设置标记点点击事件监听器
        aMap.setOnMarkerClickListener(this);

//        // 添加标记点
//        aMap.addMarker(new MarkerOptions()
//                        .position(new LatLng(39.33, 115.91))
//                        .title("小黄车")
//                        .snippet("这是一个小黄车")
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
//                .setClickable(true);

        MyLocationStyle myLocationStyle = new MyLocationStyle();
        // 自定义定位蓝点图标
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon5);
        int newWidth = 75; // 调整后的图标宽度
        int newHeight = 75; // 调整后的图标高度
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, false);
        BitmapDescriptor customIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap);
        myLocationStyle.myLocationIcon(customIcon);
        // 自定义精度范围的圆形边框颜色  都为0则透明
        myLocationStyle.strokeColor(Color.argb(100, 33, 150, 243));
        // 自定义精度范围的圆形边框宽度  0 无宽度
        myLocationStyle.strokeWidth(5);
        // 设置圆形的填充颜色  都为0则透明
        myLocationStyle.radiusFillColor(Color.argb(100, 33, 150, 243));
        // 设置定位蓝点的Style
        aMap.setMyLocationStyle(myLocationStyle);



        //设置最小缩放等级为16 ，缩放级别范围为[3, 20]
        aMap.setMinZoomLevel(15);

        // 设置定位监听
        aMap.setLocationSource(this);
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationEnabled(true);


    }

    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mLocationClient != null) {
            mLocationClient.startLocation();//启动定位
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }


    /**
     * 处理标记点的点击事件
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        // 获取标记点的标题和信息
        String title = marker.getTitle();
        String snippet = marker.getSnippet();

        // 显示标题和信息
        Toast.makeText(this, "标题：" + title + "\n信息：" + snippet, Toast.LENGTH_SHORT).show();

        // 返回 true 表示消费了该事件，不再触发其他默认的处理
        // 返回 false 则继续触发其他默认的处理
        return false;
    }

    private void openMenu() {
        // 展开菜单
        fabRide.setVisibility(View.VISIBLE);
        fabRepair.setVisibility(View.VISIBLE);
//        fabMine.setVisibility(View.VISIBLE);
        // 创建展开动画
        ObjectAnimator rideAnimatorY = ObjectAnimator.ofFloat(fabRide, "translationY", 0f, -100f);
        ObjectAnimator rideAnimatorX = ObjectAnimator.ofFloat(fabRide, "translationX", 0f, -100f);
        ObjectAnimator repairAnimatorY = ObjectAnimator.ofFloat(fabRepair, "translationY", 0f, -100f);
        ObjectAnimator repairAnimatorX = ObjectAnimator.ofFloat(fabRepair, "translationX", 0f, 100f);
        ObjectAnimator menuAnimatorY = ObjectAnimator.ofFloat(fabMenu, "translationY", 0f, 100f);
        ObjectAnimator menuAnimatorX = ObjectAnimator.ofFloat(fabMenu, "translationX", 0f, 0f);
//        ObjectAnimator mineAnimatorY = ObjectAnimator.ofFloat(fabMine, "translationY", 0f, -300f);
//        ObjectAnimator mineAnimatorX = ObjectAnimator.ofFloat(fabMine, "translationX", 0f, 0f);


        // 设置动画持续时间和插值器
        rideAnimatorY.setDuration(300);
        rideAnimatorX.setDuration(300);
        repairAnimatorY.setDuration(300);
        repairAnimatorX.setDuration(300);
//        mineAnimatorY.setDuration(300);
//        mineAnimatorX.setDuration(300);
        menuAnimatorY.setDuration(300);
        menuAnimatorX.setDuration(300);
        rideAnimatorY.setInterpolator(new DecelerateInterpolator());
        rideAnimatorX.setInterpolator(new DecelerateInterpolator());
        repairAnimatorY.setInterpolator(new DecelerateInterpolator());
        repairAnimatorX.setInterpolator(new DecelerateInterpolator());
//        mineAnimatorY.setInterpolator(new DecelerateInterpolator());
//        mineAnimatorX.setInterpolator(new DecelerateInterpolator());
        menuAnimatorY.setInterpolator(new DecelerateInterpolator());
        menuAnimatorX.setInterpolator(new DecelerateInterpolator());

        // 创建动画集合
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(rideAnimatorY, rideAnimatorX, repairAnimatorY, repairAnimatorX,menuAnimatorY,menuAnimatorX);
        animatorSet.start();

        // 旋转 Menu 按钮
        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(fabMenu, "rotation", 0f, 45f);
        rotationAnimator.setDuration(300);
        rotationAnimator.start();

        isMenuOpen = true;
    }

    private void closeMenu() {
        // 收起菜单
        // 创建收起动画
        ObjectAnimator rideAnimatorY = ObjectAnimator.ofFloat(fabRide, "translationY", -100f, 0f);
        ObjectAnimator rideAnimatorX = ObjectAnimator.ofFloat(fabRide, "translationX", -100f, 0f);
        ObjectAnimator repairAnimatorY = ObjectAnimator.ofFloat(fabRepair, "translationY", -100f, 0f);
        ObjectAnimator repairAnimatorX = ObjectAnimator.ofFloat(fabRepair, "translationX", 100f, 0f);
        ObjectAnimator menuAnimatorY = ObjectAnimator.ofFloat(fabMenu, "translationY", 100f, 0f);
        ObjectAnimator menuAnimatorX = ObjectAnimator.ofFloat(fabMenu, "translationX", 0f, 0f);
//        ObjectAnimator mineAnimatorY = ObjectAnimator.ofFloat(fabMine, "translationY", -300f, 0f);
//        ObjectAnimator mineAnimatorX = ObjectAnimator.ofFloat(fabMine, "translationX", 0f, 0f);

        // 设置动画持续时间和插值器
        rideAnimatorY.setDuration(300);
        rideAnimatorX.setDuration(300);
        repairAnimatorY.setDuration(300);
        repairAnimatorX.setDuration(300);
//        mineAnimatorY.setDuration(300);
//        mineAnimatorX.setDuration(300);
        menuAnimatorY.setDuration(300);
        menuAnimatorX.setDuration(300);
        rideAnimatorY.setInterpolator(new DecelerateInterpolator());
        rideAnimatorX.setInterpolator(new DecelerateInterpolator());
        repairAnimatorY.setInterpolator(new DecelerateInterpolator());
        repairAnimatorX.setInterpolator(new DecelerateInterpolator());
//        mineAnimatorY.setInterpolator(new DecelerateInterpolator());
//        mineAnimatorX.setInterpolator(new DecelerateInterpolator());
        menuAnimatorY.setInterpolator(new DecelerateInterpolator());
        menuAnimatorX.setInterpolator(new DecelerateInterpolator());

        // 创建动画集合
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(rideAnimatorY, rideAnimatorX, repairAnimatorY, repairAnimatorX,menuAnimatorY,menuAnimatorX);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // 在动画结束后隐藏按钮
                fabRide.setVisibility(View.GONE);
                fabRepair.setVisibility(View.GONE);
//                fabMine.setVisibility(View.GONE);
            }
        });
        animatorSet.start();

        // 逆时针旋转 Menu 按钮
        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(fabMenu, "rotation", 45f, 0f);
        rotationAnimator.setDuration(300);
        rotationAnimator.start();

        fabMenu.bringToFront();
        fabMenu.invalidate();
        isMenuOpen = false;
    }

    /**
     * 刷新当前可用单车
     */
    public void refreshBike() {
        aMap.clear();
        String res = OKHttpUtil.getSyncRequest(ApiConstants.BASE_URL_HTTP, "bikes", "findAll");
        if (res != null) {
            // 使用 Gson 解析 JSON
            Gson gson = new Gson();

            BikesData bikesData = gson.fromJson(res, BikesData.class);
            if (bikesData.getCode().equals("200")) {
                // 访问解析后的数据
                String code = bikesData.getCode();
                String msg = bikesData.getMsg();
                List<Bike> bikes = bikesData.getData();
                try {
                    //添加标记点
                    for (Bike bike : bikes) {
                        String macAddress = bike.getBikeMac();
                        String alias = "BJTU共享单车";
                        if(MacConstants.getNameByMacAddress(macAddress)!=null){
                            alias = MacConstants.getNameByMacAddress(macAddress);
                        }
                        String location = bike.getLocation();
                        // 去除方括号并按逗号分隔字符串
                        String[] parts = location.replaceAll("[\\[\\]]", "").split(",");
                        // 解析经度和纬度
                        double latitude = Double.parseDouble(parts[0].trim());
                        double longitude = Double.parseDouble(parts[1].trim());
                        Log.d("Pan",String.valueOf(latitude));
                        Log.d("Pan",String.valueOf(longitude));
                        // 添加标记点
                        // 创建自定义图标
                        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bike_icon);
                        int newWidth = 90; // 调整后的图标宽度
                        int newHeight = 90; // 调整后的图标高度
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, false);
                        BitmapDescriptor customIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap);
                        aMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(latitude, longitude))
                                        .title(alias)
                                        .snippet(macAddress)
                                        .icon(customIcon))
                                .setClickable(true);
                        setFence();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "数据解析错误", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, bikesData.getMsg(), Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(this, "服务器连接超时", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 刷新当前连接的共享单车
     */
    public void refreshCurrentBike() {
        if (app.getCurrentBikeDevice() != null) {
            String alias = app.getCurrentBikeDevice().getAliasName();
            tv_device_name.setText(alias);
            tv_device_mac.setText(app.getCurrentBikeDevice().getDevice().getAddress());

//            //获取这辆车的骑行状态
//            String res = OKHttpUtil.getSyncRequest(ApiConstants.BASE_URL_HTTP, "bikes", alias);
//            if (res != null) {
//                // 使用 Gson 解析 JSON
//                Gson gson = new Gson();
//                try {
//                    BikesData bikesData = gson.fromJson(res, BikesData.class);
//                    if (bikesData.getCode().equals("200")) {
//                        // 访问解析后的数据
//                        String code = bikesData.getCode();
//                        String msg = bikesData.getMsg();
//                        List<Bike> bikes = bikesData.getData();
//
//                        int status = bikes.get(0).getStatus();
//                        Log.d("Pan", String.valueOf(status));
//                    } else {
//                        Toast.makeText(this, bikesData.getMsg(), Toast.LENGTH_SHORT).show();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    Toast.makeText(this, "数据解析错误", Toast.LENGTH_SHORT).show();
//                }
//            } else {
//                Toast.makeText(this, "服务器连接超时", Toast.LENGTH_SHORT).show();
//            }
        }else{
            tv_device_name.setText("请使用蓝牙扫描附近的单车");
            tv_device_mac.setText("");
        }
    }

    /**
     * 开锁
     */
    public void unlock(String bikeMac, double latitude, double longitude) {
        String locationStr = "[" + latitude + "," + longitude + "]";
        Map<String, String> queryParams = new HashMap<>();
        Map<String, String> formDataParams = new HashMap<>();
        formDataParams.put("bikeMac", bikeMac);
        formDataParams.put("position", locationStr);
        Log.d("Pan",locationStr);
        String res = OKHttpUtil.postSyncRequestFormData(ApiConstants.BASE_URL_HTTP, queryParams, formDataParams, "bikes", "unlock");
        if (res != null) {
            Log.d("Pan", res);
            // 使用 Gson 解析 JSON
            Gson gson = new Gson();
            try {
                Response response = gson.fromJson(res, Response.class);
                if (response.getCode().equals("200")) {
                    Toast.makeText(this, "开锁成功", Toast.LENGTH_SHORT).show();
                    //TODO:禁止其他按钮操作
                    //开始定时上报位置
                    handler.postDelayed(runnable, 2000);

                    unlock = true;

                    //更改UI
                    animateUnlock();
                } else {
                    Toast.makeText(this, response.getMsg(), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "数据解析错误", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "服务器连接超时", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 上报当前位置
     */
    public void uploadLocation(String bikeMac, double latitude, double longitude) {
        String locationStr = "[" + latitude + "," + longitude + "]";
        Map<String, String> queryParams = new HashMap<>();
        Map<String, String> formDataParams = new HashMap<>();
        formDataParams.put("bikeMac", bikeMac);
        formDataParams.put("position", locationStr);
        String res = OKHttpUtil.postSyncRequestFormData(ApiConstants.BASE_URL_HTTP, queryParams, formDataParams, "tracks", "update");
        if (res != null) {
            Log.d("Pan", res);
            // 使用 Gson 解析 JSON
            Gson gson = new Gson();
            try {
                Response response = gson.fromJson(res, Response.class);
                if (response.getCode().equals("200")) {
//                    Toast.makeText(this, "上传成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, response.getMsg(), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "数据解析错误", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "服务器连接超时", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 关锁
     */
    public void lock(String bikeMac, double latitude, double longitude) {
        String locationStr = "[" + latitude + "," + longitude + "]";
        Map<String, String> queryParams = new HashMap<>();
        Map<String, String> formDataParams = new HashMap<>();
        formDataParams.put("bikeMac", bikeMac);
        formDataParams.put("position", locationStr);
        formDataParams.put("isNoParkingPlace", "false");
        Log.d("Pan",locationStr);
        String res = OKHttpUtil.postSyncRequestFormData(ApiConstants.BASE_URL_HTTP, queryParams, formDataParams, "bikes", "lock");
        if (res != null) {
            Log.d("Pan", res);
            // 使用 Gson 解析 JSON
            Gson gson = new Gson();
            try {
                Response response = gson.fromJson(res, Response.class);
                if (response.getCode().equals("200")) {
                    Toast.makeText(this, "关锁成功", Toast.LENGTH_SHORT).show();
                    //TODO:允许其他按钮操作
                    unlock = false;
                    //停止定时上报位置
                    handler.removeCallbacks(runnable);
                    //设置动画
                    animateLock();
                    //清除连接状态
                    app.setCurrentBikeDevice(null);
                    refreshCurrentBike();
                    //刷新可用车辆
                    refreshBike();
                } else {
                    Toast.makeText(this, response.getMsg(), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "数据解析错误", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "服务器连接超时", Toast.LENGTH_SHORT).show();
        }
    }

    private void animateButton() {
        int startColor = btnRide.getBackgroundTintList().getDefaultColor();
        int endColor = Color.RED;

        int startTextColor = btnRide.getTextColors().getDefaultColor();
        int endTextColor = Color.WHITE;

        int startStrokeColor = Color.TRANSPARENT;
        int endStrokeColor = Color.GREEN;

        ValueAnimator colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
        colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int animatedValue = (int) animator.getAnimatedValue();
                btnRide.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
            }
        });

        ValueAnimator textColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), startTextColor, endTextColor);
        textColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int animatedValue = (int) animator.getAnimatedValue();
                btnRide.setTextColor(animatedValue);
            }
        });

        GradientDrawable drawable = (GradientDrawable) btnRide.getBackground();
        ValueAnimator strokeColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), startStrokeColor, endStrokeColor);
        strokeColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int animatedValue = (int) animator.getAnimatedValue();
                drawable.setStroke(2, animatedValue);
            }
        });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(colorAnimator, textColorAnimator, strokeColorAnimator);
        animatorSet.setDuration(500);
        animatorSet.start();
    }

    private void animateUnlock(){
        //设置顶部卡片
        btnRide.setText("关锁");
        //设置顶部文字颜色变化动画
        final int startColor3 = getResources().getColor(R.color.white);
        final int endColor3 = getResources().getColor(R.color.colorAccent);
        ObjectAnimator tvInfoColorAnimation = ObjectAnimator.ofArgb(btnRide, "textColor", startColor3, endColor3);
        tvInfoColorAnimation.setDuration(1000); // 设置动画时长，单位为毫秒
        tvInfoColorAnimation.start(); // 启动动画

        //按钮背景颜色
        final int startColor = getResources().getColor(R.color.colorPrimary);
        final int endColor = getResources().getColor(R.color.white);
        ValueAnimator colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
        colorAnimator.setDuration(1000);
        colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int animatedValue = (int) animator.getAnimatedValue();
                btnRide.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
            }
        });
        colorAnimator.start();

        //按钮边框颜色
        final int startColor2 = getResources().getColor(R.color.colorPrimary);
        final int endColor2 = getResources().getColor(R.color.colorAccent);
        ValueAnimator colorAnimator2 = ValueAnimator.ofObject(new ArgbEvaluator(), startColor2, endColor2);
        colorAnimator2.setDuration(1000);
        colorAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int animatedValue = (int) animator.getAnimatedValue();
                btnRide.setStrokeColor(ColorStateList.valueOf(animatedValue));
            }
        });
        colorAnimator2.start();
    }

    private void animateLock(){
        //设置顶部卡片
        btnRide.setText("开锁");
        //设置顶部文字颜色变化动画
        final int startColor3 = getResources().getColor(R.color.colorAccent);
        final int endColor3 = getResources().getColor(R.color.white);
        ObjectAnimator tvInfoColorAnimation = ObjectAnimator.ofArgb(btnRide, "textColor", startColor3, endColor3);
        tvInfoColorAnimation.setDuration(1000); // 设置动画时长，单位为毫秒
        tvInfoColorAnimation.start(); // 启动动画

        //按钮背景颜色
        final int startColor = getResources().getColor(R.color.white);
        final int endColor = getResources().getColor(R.color.colorPrimary);
        ValueAnimator colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
        colorAnimator.setDuration(1000);
        colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int animatedValue = (int) animator.getAnimatedValue();
                btnRide.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
            }
        });
        colorAnimator.start();

        //按钮边框颜色
        final int startColor2 = getResources().getColor(R.color.colorAccent);
        final int endColor2 = getResources().getColor(R.color.colorPrimary);
        ValueAnimator colorAnimator2 = ValueAnimator.ofObject(new ArgbEvaluator(), startColor2, endColor2);
        colorAnimator2.setDuration(1000);
        colorAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int animatedValue = (int) animator.getAnimatedValue();
                btnRide.setStrokeColor(ColorStateList.valueOf(animatedValue));
            }
        });
        colorAnimator2.start();
    }

    /**
     * 电子围栏
     */
    private void setFence() {
        // 构造电子围栏区域的坐标点列表
        List<LatLng> points = new ArrayList<>();
        points.add(new LatLng(39.951776, 116.344154));
        points.add(new LatLng(39.952121, 116.346149));
        points.add(new LatLng(39.95141, 116.346332));
        points.add(new LatLng(39.951529, 116.347249));
        points.add(new LatLng(39.950896, 116.34742));
        points.add(new LatLng(39.950456, 116.344642));

        // 创建多边形选项对象，并设置填充颜色和透明度
        PolygonOptions options = new PolygonOptions()
                .addAll(points)
                .fillColor(Color.parseColor("#50FF0000")) // 设置填充颜色，#80表示透明度50%
                .strokeWidth(10) // 设置边框宽度
                .strokeColor(Color.parseColor("#FF0000")); // 设置边框颜色

        // 添加多边形到地图上
        fencePolygon = aMap.addPolygon(options);
    }

    private boolean isPointInFence(LatLng point) {
        // 电子围栏的边界点列表
        List<LatLng> fencePoints = new ArrayList<>();
        fencePoints.add(new LatLng(39.951776, 116.344154));
        fencePoints.add(new LatLng(39.952121, 116.346149));
        fencePoints.add(new LatLng(39.95141, 116.346332));
        fencePoints.add(new LatLng(39.951529, 116.347249));
        fencePoints.add(new LatLng(39.950896, 116.34742));
        fencePoints.add(new LatLng(39.950456, 116.344642));

        // 判断点是否在多边形内
        boolean isInside = isPointInPolygon(point, fencePoints);

        return isInside;
    }

    private boolean isPointInPolygon(LatLng point, List<LatLng> polygon) {
        int intersectCount = 0;
        int size = polygon.size();

        for (int i = 0; i < size; i++) {
            LatLng p1 = polygon.get(i);
            LatLng p2 = polygon.get((i + 1) % size);

            if (rayCrossesSegment(point, p1, p2)) {
                intersectCount++;
            }
        }

        return intersectCount % 2 == 1;
    }

    private boolean rayCrossesSegment(LatLng point, LatLng p1, LatLng p2) {
        double pointLng = point.longitude;
        double pointLat = point.latitude;
        double p1Lng = p1.longitude;
        double p1Lat = p1.latitude;
        double p2Lng = p2.longitude;
        double p2Lat = p2.latitude;

        if ((p1Lat < pointLat && p2Lat >= pointLat) || (p2Lat < pointLat && p1Lat >= pointLat)) {
            return p1Lng + (pointLat - p1Lat) / (p2Lat - p1Lat) * (p2Lng - p1Lng) < pointLng;
        }

        return false;
    }


}