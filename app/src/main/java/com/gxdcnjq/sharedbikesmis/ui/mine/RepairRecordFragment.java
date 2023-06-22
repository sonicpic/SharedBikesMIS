package com.gxdcnjq.sharedbikesmis.ui.mine;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.gxdcnjq.sharedbikesmis.R;
import com.gxdcnjq.sharedbikesmis.constant.ApiConstants;
import com.gxdcnjq.sharedbikesmis.entity.RepairRecord;
import com.gxdcnjq.sharedbikesmis.entity.RepairRecordResponse;
import com.gxdcnjq.sharedbikesmis.utils.OKHttpUtil;
import com.gxdcnjq.sharedbikesmis.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class RepairRecordFragment extends Fragment {

    private ListView repairRecordListView;
    private List<RepairRecord> repairRecordList;
    private ArrayAdapter<String> repairRecordAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_repair_record, container, false);

        repairRecordListView = view.findViewById(R.id.repairRecordListView);
        repairRecordList = new ArrayList<>();

        // 添加示例的报修记录数据
        repairRecordList.add(new RepairRecord("2023-06-19: Flat tire"));


        repairRecordList = getData();

        RepairRecordFragment.CustomAdapter adapter = new RepairRecordFragment.CustomAdapter(getContext(), repairRecordList);
        repairRecordListView.setAdapter(adapter);

//        repairRecordAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, repairRecordList);
//        repairRecordListView.setAdapter(repairRecordAdapter);

        return view;
    }

    public class CustomAdapter extends ArrayAdapter<RepairRecord> {

        public CustomAdapter(Context context, List<RepairRecord> data) {
            super(context, R.layout.repair_list_item, data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.repair_list_item, parent, false);
            }

            ImageView imageView = convertView.findViewById(R.id.repair_item_image);
            TextView repair_time_text = convertView.findViewById(R.id.repair_time_text);
            TextView repair_state_text = convertView.findViewById(R.id.repair_state_text);
            TextView repair_situation_text = convertView.findViewById(R.id.repair_situation_text);

            // 设置每个列表项的数据
            RepairRecord itemData = getItem(position);
            repair_time_text.setText(TimeUtils.formatDateTime2(itemData.getCreateTime()));
            repair_state_text.setText(itemData.getState());
            repair_situation_text.setText(itemData.getSituation());
            // 可以根据需要设置图片等其他数据
            if(getItem(position).getPicture()!=null){
                Glide.with(RepairRecordFragment.this.getView())
                        .load(getItem(position).getPicture())
                        .into(imageView);
            }

            return convertView;
        }
    }

    public List<RepairRecord> getData(){
        List<RepairRecord> repairRecords = new ArrayList<RepairRecord>();

        String res = OKHttpUtil.getSyncRequest(ApiConstants.BASE_URL_HTTP,"repairs/");
        if (res != null) {
            Log.d("Pan", res);
            // 使用 Gson 解析 JSON
            Gson gson = new Gson();
            try {
                RepairRecordResponse response = gson.fromJson(res, RepairRecordResponse.class);
                if (response.getCode().equals("200")) {
                    repairRecords = (List<RepairRecord>) response.getData();
                    Log.d("Pan", repairRecords.get(0).getSituation());
                } else {
                    Toast.makeText(getActivity(), response.getMsg(), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
//                Toast.makeText(getActivity(), "数据解析错误", Toast.LENGTH_SHORT).show();
                Log.e("Pan","维修数据get解析错误");
            }
        } else {
            Toast.makeText(getActivity(), "服务器连接超时", Toast.LENGTH_SHORT).show();
        }

        return repairRecords;
    }
}
