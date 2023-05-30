package com.gxdcnjq.sharedbikesmis.ui.login;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gxdcnjq.sharedbikesmis.ui.main.MainActivity;
import com.gxdcnjq.sharedbikesmis.R;
import com.gxdcnjq.sharedbikesmis.entity.User;
import com.gxdcnjq.sharedbikesmis.ui.register.RegisterActivity;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {


    private Button btn_login;
    private ImageView welcomeImageView;
    private EditText et_username;
    private EditText et_password;
    private TextView btn_reg;
    private TextView btn_forget;
    private int clickCount = 0;
    private int currentImageIndex = 1; // 当前显示的图片索引，初始为1
    private String baseUrl = "http://127.0.0.1:8080";
    private User user_success = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        welcomeImageView = findViewById(R.id.joy);
        btn_login = findViewById(R.id.btn_login);
        btn_reg = findViewById(R.id.btn_reg);
        btn_forget = findViewById(R.id.btn_nouser);
        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);

        btn_login.setOnClickListener(this);
        btn_reg.setOnClickListener(this);
        btn_forget.setOnClickListener(this);



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

        welcomeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickCount++;
                Log.d("Pan","click");
                if (clickCount >= 5 && clickCount <10) {
                    Toast.makeText(LoginActivity.this, "别点啦！！！", Toast.LENGTH_SHORT).show();
                }else if(clickCount >=10 && clickCount <26){
//                    changeImage();
                }else if(clickCount >=26){
//                    Intent intent = new Intent(LoginActivity.this,EggsActivity.class);
//                    startActivity(intent);
                }
            }
        });
    }



    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();//登录按钮
        if (id == R.id.btn_login) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("user_json", "null");
            startActivity(intent);
            finish();
//                //TODO：判断用户名和密码
//                String username = et_username.getText().toString();
//                String password = et_password.getText().toString();
//                User user = new User(username, password);
//                Gson gson = new Gson();
//                String json = gson.toJson(user);
//                String args[] = new String[]{"user", "login"};
//                String res = null;//服务器传回的json字符串
//                res = OKHttpUtil.postSyncRequest(baseUrl, json, args);
//                if(res==null){
//                    Toast.makeText(this, "服务器响应超时", Toast.LENGTH_SHORT).show();
//                }else{
//                    Log.d("同步:", res);
//                    user_success = gson.fromJson(res, User.class);
//                    if (true) {
//                        //密码错误
//                        Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show();
//                    } else if (true) {
//                        //用户名不存在
//                        Toast.makeText(this, "用户名不存在", Toast.LENGTH_SHORT).show();
//                    } else {//登录成功，等待环信登录
//                        //Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
//                        Intent intent1 = new Intent(this, MainActivity.class);
//                        intent1.putExtra("user_json", res);
//                        startActivity(intent1);
//                        finish();
//                    }
//                }
            //注册按钮
        } else if (id == R.id.btn_reg) {
            Intent intent2 = new Intent(this, RegisterActivity.class);
            startActivity(intent2);
            //游客模式
        } else if (id == R.id.btn_nouser) {
            Intent intent1 = new Intent(this, MainActivity.class);
            intent1.putExtra("user_json", "null");
            startActivity(intent1);
            finish();
            //彩蛋

            clickCount++;
            Log.d("Pan", "click");
            if (clickCount >= 5) {
                // 切换图片
//                    changeImage();
            }
        } else if (id == R.id.joy) {
            clickCount++;
            Log.d("Pan", "click");
            if (clickCount >= 5) {
                // 切换图片
//                    changeImage();
            }
        }
    }

    //彩蛋
//    private void changeImage() {
//        // 根据当前索引设置下一张图片资源
//        switch (currentImageIndex) {
//            case 1:
//                welcomeImageView.setImageResource(R.mipmap.egg_hu);
//                currentImageIndex = 2;
//                break;
//            case 2:
//                welcomeImageView.setImageResource(R.mipmap.egg_li);
//                currentImageIndex = 3;
//                break;
//            case 3:
//                welcomeImageView.setImageResource(R.mipmap.egg_pan);
//                currentImageIndex = 4;
//                break;
//            case 4:
//                welcomeImageView.setImageResource(R.mipmap.egg_zhang);
//                currentImageIndex = 1;
//                break;
//        }
//    }

}