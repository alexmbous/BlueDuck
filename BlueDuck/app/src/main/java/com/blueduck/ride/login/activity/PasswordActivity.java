package com.blueduck.ride.login.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.blueduck.ride.R;
import com.blueduck.ride.base.BaseActivity;
import com.blueduck.ride.login.bean.LoginBean;
import com.blueduck.ride.login.service.LoginService;
import com.blueduck.ride.main.activity.MainActivity;
import com.blueduck.ride.utils.BroadCastValues;
import com.blueduck.ride.utils.CommonSharedValues;
import com.blueduck.ride.utils.CommonUtils;
import com.blueduck.ride.utils.LogUtils;
import com.blueduck.ride.utils.RequestCallBack;

public class PasswordActivity extends BaseActivity implements RequestCallBack {

    private static final String TAG = "PasswordActivity";
    private EditText passwordEt;
    private Button loginBtn;
    private LoginService loginService;
    private String account,password;
    private double lat,lng;
    private ForgetBroad forgetBroad;

    @Override
    protected int setLayoutViewId() {
        return R.layout.password_activity;
    }

    @Override
    protected void init() {
        loginService = new LoginService(this,this,TAG);
        account = getIntent().getStringExtra("account");
        lat = getIntent().getDoubleExtra("lat",0);
        lng = getIntent().getDoubleExtra("lng",0);
        initBroad();
    }

    private void initBroad(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadCastValues.FINISH_BROAD);
        forgetBroad = new ForgetBroad();
        registerReceiver(forgetBroad,intentFilter);
    }

    @Override
    protected void initView() {
        passwordEt = (EditText) findViewById(R.id.password_edit);
        passwordEt.addTextChangedListener(new MyTextWatcher());
        findViewById(R.id.forget_password_text).setOnClickListener(this);
        findViewById(R.id.password_back_layout).setOnClickListener(this);
        loginBtn = (Button) findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(this);
        loginBtn.setEnabled(false);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.password_back_layout:
                finish();
                break;
            case R.id.forget_password_text:
                getEmailCode();
                break;
            case R.id.login_btn:
                loginBottom();
                break;
        }
    }

    private void loginBottom(){
        password = passwordEt.getText().toString().trim();
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this,getString(R.string.not_null),Toast.LENGTH_SHORT).show();
        }else{
            login();
        }
    }

    /**
     * 登录
     * login
     */
    private void login(){
        loginService.login(account,password,lat,lng,
                shared.getString(CommonSharedValues.GOOGLE_PUSH_TOKEN,""),
                CommonUtils.getUniqueId(this),1);
    }

    /**
     * 获得邮箱验证码
     * Get the mailbox verification code
     */
    private void getEmailCode(){
        loginService.getEmailCode(account,"2",2);
    }

    private void handlerLogin(LoginBean loginBean){
        CommonUtils.saveLoginInfo(sp,loginBean,account,"",2);
        sendBroadcast(new Intent(BroadCastValues.FINISH_BROAD));
        startActivity(new Intent(this,MainActivity.class));
    }

    private void handlerVerificationCode(int invalidMinute){
        LogUtils.i(TAG, "获取验证码成功ok");
        Intent intent = new Intent(this, VerificationActivity.class);
        intent.putExtra("skipType", 3);
        intent.putExtra("smsAndEmailType", "2");
        intent.putExtra("account", account);
        intent.putExtra("accountType",2);
        intent.putExtra("invalidMinute",invalidMinute);
        startActivity(intent);
    }

    @Override
    public void onSuccess(Object o, int flag) {
        if (flag == 1){
            LoginBean loginBean = (LoginBean) o;
            handlerLogin(loginBean);
        }else if (flag == 2){
            int invalidMinute = (Integer) o;
            handlerVerificationCode(invalidMinute);
        }
    }

    @Override
    public void onFail(Throwable t, int flag) {

    }

    private class MyTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() > 0){
                loginBtn.setEnabled(true);
                loginBtn.setBackground(ContextCompat.getDrawable(PasswordActivity.this,R.drawable.skip_btn_bg));
            }else{
                loginBtn.setEnabled(false);
                loginBtn.setBackground(ContextCompat.getDrawable(PasswordActivity.this,R.drawable.skip_gray_btn_bg));
            }
        }
    }

    private class ForgetBroad extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (BroadCastValues.FINISH_BROAD.equals(intent.getAction())){
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(forgetBroad);
    }
}
