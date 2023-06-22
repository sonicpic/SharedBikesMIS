package com.gxdcnjq.sharedbikesmis.ui.mine;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.gxdcnjq.sharedbikesmis.R;
import com.gxdcnjq.sharedbikesmis.constant.ApiConstants;
import com.gxdcnjq.sharedbikesmis.entity.Bike;
import com.gxdcnjq.sharedbikesmis.entity.BikesData;
import com.gxdcnjq.sharedbikesmis.entity.LoginResponse;
import com.gxdcnjq.sharedbikesmis.entity.UserResponse;
import com.gxdcnjq.sharedbikesmis.utils.OKHttpUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonalCenterActivity extends AppCompatActivity {

    private CircleImageView avatarImageView;
    private TextView usernameTextView;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private RideRecordFragment rideRecordFragment;
    private RepairRecordFragment repairRecordFragment;
    private String username="";
    private String avatar;
    private static final int PICK_IMAGE_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_center);

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

        // 初始化视图
        avatarImageView = findViewById(R.id.avatarImageView);
        usernameTextView = findViewById(R.id.usernameTextView);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        // 根据token获取用户信息
        String res = OKHttpUtil.getSyncRequest(ApiConstants.BASE_URL_HTTP,"user/");
        if (res != null) {
            // 使用 Gson 解析 JSON
            Gson gson = new Gson();
            try {
                LoginResponse loginResponse = gson.fromJson(res, LoginResponse.class);
                if (loginResponse.getCode().equals("200")) {
                    // 访问解析后的数据
                    UserResponse userResponse = loginResponse.getData();
                    username = userResponse.getUsername();
                    avatar = userResponse.getAvatar();
                } else {
                    Toast.makeText(this, "获取用户信息出错", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "用户数据解析错误", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "服务器连接超时", Toast.LENGTH_SHORT).show();
        }
        usernameTextView.setText(username);
        // 获取用户头像
        if(avatar!=null){
            Glide.with(this)
                    .load(avatar)
                    .into(avatarImageView);
        }

        // 初始化ViewPager
        rideRecordFragment = new RideRecordFragment();
        repairRecordFragment = new RepairRecordFragment();
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(rideRecordFragment, "骑行记录");
        adapter.addFragment(repairRecordFragment, "报修记录");
        viewPager.setAdapter(adapter);

        // 关联ViewPager和TabLayout
        tabLayout.setupWithViewPager(viewPager);

        // 其他代码...
        avatarImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri imageUri = data.getData();
                // 执行HTTP上传操作，将图像URI传递给上传方法
                // 将图像URI转换为文件路径
                String imagePath = getImagePathFromUri(imageUri);
                File file = new File(imagePath);
                Map<String, Object> formData = new HashMap<>();
                formData.put("file",file);
                String res = OKHttpUtil.postSyncRequestFormDataFileAvailable(ApiConstants.BASE_URL_HTTP,new HashMap<>(),formData,"photos","upload");
                Log.d("Pan",res);
            }
        }
    }

    public String getImagePathFromUri(Uri uri) {
        String imagePath = null;
        if (uri != null) {
            if (DocumentsContract.isDocumentUri(this, uri)) {
                // 如果是Document类型的Uri，则通过Document ID进行处理
                String documentId = DocumentsContract.getDocumentId(uri);
                if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                    String id = documentId.split(":")[1];
                    String selection = MediaStore.Images.Media._ID + "=" + id;
                    imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
                } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                    Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
                    imagePath = getImagePath(contentUri, null);
                }
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                // 如果是Content类型的Uri，则使用普通方式处理
                imagePath = getImagePath(uri, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                // 如果是File类型的Uri，直接获取路径
                imagePath = uri.getPath();
            }
        }
        return imagePath;
    }

    @SuppressLint("Range")
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }


}
