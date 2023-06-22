package com.gxdcnjq.sharedbikesmis.ui.mine;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;
import androidx.fragment.app.Fragment;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.ArcOptions;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.gxdcnjq.sharedbikesmis.R;
import com.gxdcnjq.sharedbikesmis.constant.ApiConstants;
import com.gxdcnjq.sharedbikesmis.entity.Location;
import com.gxdcnjq.sharedbikesmis.entity.RideRecord;
import com.gxdcnjq.sharedbikesmis.entity.TrackRecord;
import com.gxdcnjq.sharedbikesmis.entity.TrackRecordResponse;
import com.gxdcnjq.sharedbikesmis.utils.OKHttpUtil;
import com.gxdcnjq.sharedbikesmis.utils.TimeUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RideRecordFragment extends Fragment {

    private ListView rideRecordListView;
    private List<TrackRecord> trackRecordList;
    private List<RideRecord> rideRecordList;
    private ArrayAdapter<String> rideRecordAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ride_record, container, false);



        rideRecordListView = view.findViewById(R.id.rideRecordListView);
        trackRecordList = new ArrayList<>();


        trackRecordList = getData();
        rideRecordList = process(trackRecordList);


        CustomAdapter adapter = new CustomAdapter(getContext(), rideRecordList);
        rideRecordListView.setAdapter(adapter);


        rideRecordListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RideRecord rideRecord = rideRecordList.get(position);
                // 在点击某个列表项后的点击事件中调用以下代码显示对话框
                MapDialog mapDialog = new MapDialog(getContext(), rideRecord.getLocationList());
                mapDialog.show();
            }
        });


        return view;
    }

    public class CustomAdapter extends ArrayAdapter<RideRecord> {

        public CustomAdapter(Context context, List<RideRecord> data) {
            super(context, R.layout.ride_list_item, data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.ride_list_item, parent, false);
            }

            ImageView imageView = convertView.findViewById(R.id.ride_item_image);
            TextView ride_id_text = convertView.findViewById(R.id.ride_id_text);
            TextView ride_start_text = convertView.findViewById(R.id.ride_start_text);
            TextView ride_end_text = convertView.findViewById(R.id.ride_end_text);

            // 设置每个列表项的数据
            RideRecord itemData = getItem(position);
            List<Location> locationList = itemData.getLocationList();
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            String formattedDistance = decimalFormat.format(itemData.getDistance()) + " km";
            ride_id_text.setText("编号 "+String.valueOf(position+1) + "  距离 "+formattedDistance);
            ride_start_text.setText("开始 | "+TimeUtils.formatDateTime(itemData.getStartTime()));
            ride_end_text.setText("结束 | "+TimeUtils.formatDateTime(itemData.getEndTime()));
            // 可以根据需要设置图片等其他数据

            return convertView;
        }
    }

    public List<TrackRecord> getData(){
        List<TrackRecord> trackRecord = new ArrayList<TrackRecord>();

        String res = OKHttpUtil.getSyncRequest(ApiConstants.BASE_URL_HTTP,"tracks/");
        if (res != null) {
            Log.d("Pan", res);
            // 使用 Gson 解析 JSON
            Gson gson = new Gson();
            try {
                TrackRecordResponse response = gson.fromJson(res, TrackRecordResponse.class);
                if (response.getCode().equals("200")) {
                    trackRecord = (List<TrackRecord>) response.getData();
                } else {
                    Toast.makeText(getActivity(), response.getMsg(), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
//                Toast.makeText(getActivity(), "数据解析错误", Toast.LENGTH_SHORT).show();
                Log.e("Pan","track数据get解析错误");
            }
        } else {
            Toast.makeText(getActivity(), "服务器连接超时", Toast.LENGTH_SHORT).show();
        }

        return trackRecord;
    }

    public List<RideRecord> process(List<TrackRecord> trackRecordList){
        Map<String, RideRecord> rideMap = new HashMap<>();

        // 遍历 trackList
        for (TrackRecord track : trackRecordList) {
            String recordId = track.getRecordId();
            String createTime = track.getCreateTime();

            // 如果 rideMap 中不存在当前 recordId 的 ride，则创建新的 ride
            if (!rideMap.containsKey(recordId)) {
                RideRecord rideRecord = new RideRecord(recordId, createTime, createTime);
                rideMap.put(recordId, rideRecord);
            } else {
                // 如果 rideMap 中已存在当前 recordId 的 ride，则更新开始时间和结束时间
                RideRecord rideRecord = rideMap.get(recordId);
                if (createTime.compareTo(rideRecord.getStartTime()) < 0) {
                    rideRecord.setStartTime(createTime);
                } else if (createTime.compareTo(rideRecord.getEndTime()) > 0) {
                    rideRecord.setEndTime(createTime);
                }
            }

            // 将当前 track 的 location 添加到对应 ride 的 locations 列表中
            RideRecord rideRecord = rideMap.get(recordId);
            String locationStr = track.getLocation();
            // 去除方括号并按逗号分隔字符串
            String[] parts = locationStr.replaceAll("[\\[\\]]", "").split(",");
            // 解析经度和纬度
            double latitude = Double.parseDouble(parts[0].trim());
            double longitude = Double.parseDouble(parts[1].trim());
            Location location = new Location(latitude,longitude);
            rideRecord.getLocationList().add(location);

            // 计算距离并累加
            if (rideRecord.getLocationList().size() >= 2) {
                Location lastLocation = rideRecord.getLocationList().get(rideRecord.getLocationList().size() - 2);
                double distance = calculateDistance(lastLocation.getLatitude(), lastLocation.getLongitude(), latitude, longitude);
                rideRecord.setDistance(rideRecord.getDistance() + distance);
            }
        }

        // 将 rideMap 中的 rides 转换为 List<Ride>
        List<RideRecord> rideList = new ArrayList<>(rideMap.values());

        // 按照开始时间进行排序
        rideList.sort(Comparator.comparing(RideRecord::getStartTime));
        return rideList;
    }


    public class MapDialog extends Dialog {

        private List<Location> locationList;

        public MapDialog(@NonNull Context context, List<Location> locationList) {
            super(context);
            this.locationList = locationList;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.dialog_map);

            // 初始化地图控件
            MapView mapView = findViewById(R.id.map_view2);
            mapView.onCreate(savedInstanceState);

            // 获取地图对象
            AMap aMap = mapView.getMap();

            // 在地图上绘制轨迹线
            drawPolylineOnMap(aMap);
        }

        private void drawPolylineOnMap(AMap aMap) {
            // 创建轨迹线的经纬度点列表
            List<LatLng> latLngList = new ArrayList<>();
            for (Location location : locationList) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                LatLng latLng = new LatLng(latitude, longitude);
                latLngList.add(latLng);
            }

            calBorder(aMap);



            // 绘制轨迹线
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(latLngList)
                    .color(Color.argb(200,0, 150, 136))
                    .width(7f);
            aMap.addPolyline(polylineOptions);

        }

        private void calBorder(AMap aMap){
            double minLatitude = Double.MAX_VALUE;
            double maxLatitude = Double.MIN_VALUE;
            double minLongitude = Double.MAX_VALUE;
            double maxLongitude = Double.MIN_VALUE;

            // 遍历所有坐标点，更新边界值
            for (Location location : locationList) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                if (latitude < minLatitude) {
                    minLatitude = latitude;
                }
                if (latitude > maxLatitude) {
                    maxLatitude = latitude;
                }
                if (longitude < minLongitude) {
                    minLongitude = longitude;
                }
                if (longitude > maxLongitude) {
                    maxLongitude = longitude;
                }
            }

            // 创建边界框
            LatLngBounds bounds = new LatLngBounds.Builder()
                    .include(new LatLng(minLatitude, minLongitude))
                    .include(new LatLng(maxLatitude, maxLongitude))
                    .build();

            int padding = 100; // 可以根据需求调整
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            aMap.moveCamera(cameraUpdate);
        }
    }


    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 地球半径（单位：千米）

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = R * c; // 距离（单位：千米）
        return distance;
    }


}
