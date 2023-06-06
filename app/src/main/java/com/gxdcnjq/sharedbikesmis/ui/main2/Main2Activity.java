package com.gxdcnjq.sharedbikesmis.ui.main2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gxdcnjq.sharedbikesmis.MapApplication;
import com.gxdcnjq.sharedbikesmis.R;
import com.gxdcnjq.sharedbikesmis.databinding.ActivityMain2Binding;
import com.gxdcnjq.sharedbikesmis.ui.bluetooth.BluetoothActivity;

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
    private TextView tv_info;
    private TextView tv_device;
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

    private boolean isMenuOpen = false;

    private MapApplication app;

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
        tv_info = binding.tvContent;
        fabMenu = binding.fabMenu;
        fabRide = binding.fabRide;
        fabRepair = binding.fabRepair;
        tv_device = binding.tvDevice;

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
                if (isMenuOpen) {
                    // 如果菜单已展开，则收起菜单
                    closeMenu();
                } else {
                    // 如果菜单未展开，则展开菜单
                    openMenu();
                }
            }
        });

        // 设置展开按钮的点击事件监听器
        fabRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处理骑行按钮的点击事件
                // TODO: 添加您的处理逻辑
                Toast.makeText(Main2Activity.this, "点击了骑行按钮", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Main2Activity.this, BluetoothActivity.class);
                startActivity(intent);
            }
        });

        // 设置报修按钮的点击事件监听器
        fabRepair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处理报修按钮的点击事件
                // TODO: 添加您的处理逻辑
                Toast.makeText(Main2Activity.this, "点击了报修按钮", Toast.LENGTH_SHORT).show();
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
        //在Fragment执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mapView.onResume();
        if(app.getDevice()!=null){
            tv_device.setText("共享单车已连接"+"\n"+app.getDevice().getName()+"\n"+app.getDevice().getAddress());
//            Toast.makeText(app, app.getDevice().getName(), Toast.LENGTH_SHORT).show();
        }
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
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            //Android6.0及以上先获取权限再定位
            requestPermission();

        }else {
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
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("纬度：" + aMapLocation.getLatitude() + "\n");
                stringBuffer.append("经度：" + aMapLocation.getLongitude()+ "\n");
                stringBuffer.append("地址：" + address + "\n");

                tv_info.setText(stringBuffer.toString());

                //停止定位，但本地定位服务并不会被销毁
                mLocationClient.stopLocation();

                //显示地图定位结果
                if(mListener != null){
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
     * @param savedInstanceState
     */
    private void initMap(Bundle savedInstanceState) {
        mapView = binding.mapView;
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mapView.onCreate(savedInstanceState);
        //初始化地图控制器对象
        aMap = mapView.getMap();

        // 给地图设置标记点点击事件监听器
        aMap.setOnMarkerClickListener(this);

        // 添加标记点
        aMap.addMarker(new MarkerOptions()
                        .position(new LatLng(39.95, 116.34))
                        .title("小黄车")
                        .snippet("这是一个小黄车")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
                .setClickable(true);

//        // 自定义定位蓝点图标
//        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_pedal_bike_24));
//        // 自定义精度范围的圆形边框颜色  都为0则透明
//        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
//        // 自定义精度范围的圆形边框宽度  0 无宽度
//        myLocationStyle.strokeWidth(0);
//        // 设置圆形的填充颜色  都为0则透明
//        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));
//        //设置定位蓝点的Style
//        aMap.setMyLocationStyle(myLocationStyle);

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
        // 创建展开动画
        ObjectAnimator rideAnimator = ObjectAnimator.ofFloat(fabRide, "translationY", 0f, -200f);
        ObjectAnimator repairAnimator = ObjectAnimator.ofFloat(fabRepair, "translationY", 0f, -400f);

        // 设置动画持续时间和插值器
        rideAnimator.setDuration(300);
        repairAnimator.setDuration(300);
        rideAnimator.setInterpolator(new DecelerateInterpolator());
        repairAnimator.setInterpolator(new DecelerateInterpolator());

        // 创建动画集合
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(rideAnimator, repairAnimator);
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
        ObjectAnimator rideAnimator = ObjectAnimator.ofFloat(fabRide, "translationY", -200f, 0f);
        ObjectAnimator repairAnimator = ObjectAnimator.ofFloat(fabRepair, "translationY", -400f, 0f);

        // 设置动画持续时间和插值器
        rideAnimator.setDuration(300);
        repairAnimator.setDuration(300);
        rideAnimator.setInterpolator(new AccelerateInterpolator());
        repairAnimator.setInterpolator(new AccelerateInterpolator());

        // 创建动画集合
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(rideAnimator, repairAnimator);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // 在动画结束后隐藏按钮
                fabRide.setVisibility(View.GONE);
                fabRepair.setVisibility(View.GONE);
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

}