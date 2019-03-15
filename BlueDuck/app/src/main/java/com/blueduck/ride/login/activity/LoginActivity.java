package com.blueduck.ride.login.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
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
import com.blueduck.ride.login.service.LoginService;
import com.blueduck.ride.main.bean.LocationCall;
import com.blueduck.ride.utils.BroadCastValues;
import com.blueduck.ride.utils.CommonSharedValues;
import com.blueduck.ride.utils.CommonUtils;
import com.blueduck.ride.utils.LocationClient;
import com.blueduck.ride.utils.RequestCallBack;

public class LoginActivity extends BaseActivity implements RequestCallBack,LocationClient.LocationCallBack {

    private static final String TAG = "LoginActivity";
    private EditText emailEt;
    private Button startBtn;
    private LoginService loginService;

    private String account;
    private FinishBroad finishBroad;

    @Override
    protected int setLayoutViewId() {
        return R.layout.login_activity;
    }

    @Override
    protected void init() {
        loginService = new LoginService(this,this,TAG);
        getLocationPermission();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadCastValues.FINISH_BROAD);
        finishBroad = new FinishBroad();
        registerReceiver(finishBroad,intentFilter);
    }

    @Override
    protected void initView() {
        emailEt = (EditText) findViewById(R.id.login_email_edit);
        emailEt.addTextChangedListener(new MyTextWatcher());
        startBtn = (Button) findViewById(R.id.start_riding_btn);
        startBtn.setOnClickListener(this);
        startBtn.setEnabled(false);

        String account = shared.getString(CommonSharedValues.PHONE_LOGIN_EMAIL, "");
        if (!TextUtils.isEmpty(account)) {
            emailEt.setText(account);
            emailEt.requestFocus();
            emailEt.setSelection(account.length());
        }
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.start_riding_btn:
                startRidingBottom();
                break;
        }
    }

    private void startRidingBottom(){
        account = emailEt.getText().toString().trim();
        if (TextUtils.isEmpty(account)){
            Toast.makeText(this,getString(R.string.not_null),Toast.LENGTH_SHORT).show();
        }else if (!account.contains("@")){
            Toast.makeText(this,getString(R.string.email_error),Toast.LENGTH_SHORT).show();
        }else{
            verifyAccount();
        }
    }

    private void verifyAccount(){
        loginService.verifyAccount(account,1);
    }

    private void handlerLogin(int code){
        if (code == 202){//账号不存在 Account does not exist
            Intent intent = new Intent(this,PersonalInformationActivity.class);
            intent.putExtra("account",account);
            intent.putExtra("lat",lat);
            intent.putExtra("lng",lng);
            startActivity(intent);
        }else if (code == 203){//账号已存在 The account already exists.
            Intent intent = new Intent(this,PasswordActivity.class);
            intent.putExtra("account",account);
            intent.putExtra("lat",lat);
            intent.putExtra("lng",lng);
            startActivity(intent);
        }else{
            CommonUtils.onFailure(this, code, TAG);
        }
    }

    @Override
    public void onSuccess(Object o, int flag) {
        if (flag == 1){
            int code = (Integer) o;
            handlerLogin(code);
        }
    }

    @Override
    public void onFail(Throwable t, int flag) {

    }

    private class MyTextWatcher implements TextWatcher{
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() > 0){
                startBtn.setEnabled(true);
                startBtn.setBackground(ContextCompat.getDrawable(LoginActivity.this,R.drawable.skip_btn_bg));
            }else{
                startBtn.setEnabled(false);
                startBtn.setBackground(ContextCompat.getDrawable(LoginActivity.this,R.drawable.skip_gray_btn_bg));
            }
        }
    }

    private double lat = 0;
    private double lng = 0;
    private LocationClient locationClient;
    @Override
    public void locationCall(LocationCall locationCall) {
        if (locationCall != null){
            lat = locationCall.getLat();
            lng = locationCall.getLon();
        }
    }

    private void startLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationClient = new LocationClient(this,this,0);
        locationClient.startLocation();//启动定位 Start position
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocation();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(finishBroad);
        if (locationClient != null){
            locationClient.stopLocation();
            locationClient = null;
        }
    }

    /**
     * 结束界面广播
     */
    private class FinishBroad extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (BroadCastValues.FINISH_BROAD.equals(intent.getAction())){
                finish();
            }
        }
    }
}
