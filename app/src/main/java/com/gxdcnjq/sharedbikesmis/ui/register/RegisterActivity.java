package com.gxdcnjq.sharedbikesmis.ui.register;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.gxdcnjq.sharedbikesmis.R;
import com.gxdcnjq.sharedbikesmis.constant.ApiConstants;
import com.gxdcnjq.sharedbikesmis.entity.LoginResponse;
import com.gxdcnjq.sharedbikesmis.entity.Response;
import com.gxdcnjq.sharedbikesmis.entity.UserForLogin;
import com.gxdcnjq.sharedbikesmis.entity.UserForReg;
import com.gxdcnjq.sharedbikesmis.entity.WanNengResponse;
import com.gxdcnjq.sharedbikesmis.ui.main2.Main2Activity;
import com.gxdcnjq.sharedbikesmis.utils.OKHttpUtil;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText reg_et_username;
    private EditText reg_et_password;
    private Button btn_confirm_reg;
    private TextView btn_to_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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

        reg_et_username = findViewById(R.id.reg_et_username);
        reg_et_password = findViewById(R.id.reg_et_password);
        btn_confirm_reg = findViewById(R.id.btn_confirm_reg);
        btn_to_login = findViewById(R.id.btn_to_login);

        btn_confirm_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO：判断用户名和密码
                String username = reg_et_username.getText().toString();
                String password = reg_et_password.getText().toString();
                String phone = reg_et_password.getText().toString();
                UserForReg userForReg = new UserForReg(username,password,phone);
                Gson gson = new Gson();
                String json = gson.toJson(userForReg);
                Log.d("Pan",json);
                String res = OKHttpUtil.postSyncRequestJson(ApiConstants.BASE_URL_HTTP, new HashMap<>(), json, "user", "register");//服务器传回的json字符串
                if (res != null) {
                    // 使用 Gson 解析 JSON
                    WanNengResponse response = gson.fromJson(res, WanNengResponse.class);
                    if (response.getCode().equals("200")) {
                        Toast.makeText(getApplicationContext(), "注册成功", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), response.getMsg(), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "服务器连接超时", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_to_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
}