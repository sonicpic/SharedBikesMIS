package com.gxdcnjq.sharedbikesmis.ui.repair;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.gxdcnjq.sharedbikesmis.MapApplication;
import com.gxdcnjq.sharedbikesmis.R;
import com.gxdcnjq.sharedbikesmis.constant.ApiConstants;
import com.gxdcnjq.sharedbikesmis.entity.Response;
import com.gxdcnjq.sharedbikesmis.entity.WanNengResponse;
import com.gxdcnjq.sharedbikesmis.utils.OKHttpUtil;
import com.gxdcnjq.sharedbikesmis.utils.TimeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RepairActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PERMISSION_CAMERA = 2;
    private static final int REQUEST_PICK_IMAGE = 3;

    private EditText editMac;
    private ImageView imagePreview;
    private EditText editDamageCondition;

    private Bitmap imageBitmap;
    private MapApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repair);

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

        editMac = findViewById(R.id.edit_mac);
        imagePreview = findViewById(R.id.image_preview);
        editDamageCondition = findViewById(R.id.edit_damage_condition);

//        Button btnSelectImage = findViewById(R.id.btn_select_image);
        Button btnSubmit = findViewById(R.id.btn_submit);

        app = (MapApplication) getApplication();
        if(app.getCurrentBikeDevice()!=null){
            editMac.setText(app.getCurrentBikeDevice().getDevice().getAddress());
        }


        imagePreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageSelectionDialog();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm();
            }
        });
    }

    private void showImageSelectionDialog() {
        String[] options = {"拍照", "从相册选择"};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("选择图片来源");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                checkCameraPermission();
            } else {
                pickImageFromGallery();
            }
        });
        builder.show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
        } else {
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "选择图片"), REQUEST_PICK_IMAGE);
    }

    private void submitForm() {
        String macAddress = editMac.getText().toString();
        String damageCondition = editDamageCondition.getText().toString();
        if(macAddress.equals("") || damageCondition.equals("")){
            Toast.makeText(this, "提交内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 图片文件
        if(imageBitmap != null){
            File file = bitmapToFile(imageBitmap, TimeUtils.getCurrentTimestampAsString());
            Map<String,Object> formData = new HashMap<>();
            formData.put("bikeMac",macAddress);
            formData.put("situation",damageCondition);
            formData.put("file",file);

            String res = OKHttpUtil.postSyncRequestFormDataFileAvailable(ApiConstants.BASE_URL_HTTP,new HashMap<>(),formData,"repairs","withPhoto");
            if(res == null){
                Toast.makeText(this, "服务器响应超时", Toast.LENGTH_SHORT).show();
                return;
            }
            Gson gson = new Gson();
            try {
                WanNengResponse response = gson.fromJson(res, WanNengResponse.class);
                if (!response.getCode().equals("200")) {
                    Toast.makeText(this, "报修失败", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "数据解析错误", Toast.LENGTH_SHORT).show();
            }
        }else{
            Map<String,Object> formData = new HashMap<>();
            formData.put("bikeMac",macAddress);
            formData.put("situation",damageCondition);

            String res = OKHttpUtil.postSyncRequestFormDataFileAvailable(ApiConstants.BASE_URL_HTTP,new HashMap<>(),formData,"repairs","withoutPhoto");
            if(res == null){
                Toast.makeText(this, "服务器响应超时", Toast.LENGTH_SHORT).show();
                return;
            }
            Gson gson = new Gson();
            try {
                WanNengResponse response = gson.fromJson(res, WanNengResponse.class);
                if (!response.getCode().equals("200")) {
                    Toast.makeText(this, "报修失败", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "数据解析错误", Toast.LENGTH_SHORT).show();
            }
        }
        Toast.makeText(this, "报修表单已提交", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imagePreview.setImageBitmap(imageBitmap);
        } else if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    imagePreview.setImageBitmap(imageBitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "没有相机权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public File bitmapToFile(Bitmap bitmap, String fileName) {
        // 创建临时文件
        File file = new File(getCacheDir(), fileName);

        try {
            // 将 Bitmap 写入文件
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
