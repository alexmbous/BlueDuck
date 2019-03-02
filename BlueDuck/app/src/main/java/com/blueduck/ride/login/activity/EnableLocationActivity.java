package com.blueduck.ride.login.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.blueduck.ride.R;
import com.blueduck.ride.base.BaseActivity;
import com.blueduck.ride.main.activity.MainActivity;
import com.blueduck.ride.utils.BroadCastValues;
import com.blueduck.ride.utils.CommonUtils;

public class EnableLocationActivity extends BaseActivity {

    private static final String TAG = "EnableLocationActivity";

    private String number;//扫描驾照传过来的编号

    @Override
    protected int setLayoutViewId() {
        return R.layout.enable_location_activity;
    }

    @Override
    protected void initView() {
        number = getIntent().getStringExtra("number");
        findViewById(R.id.enable_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.enable_btn:
                getLocationPermission();
                break;
        }
    }

    /**
     * Detect targeting permissions
     * 检测定位权限
     **/
    private void getLocationPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            skipMain();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    skipMain();
                }else{
                    CommonUtils.hintDialog(this, getString(R.string.not_location_permission));
                }
                break;
        }
    }

    private void skipMain(){
        startActivity(new Intent(this,MainActivity.class));
        sendBroadcast(new Intent(BroadCastValues.FINISH_BROAD));
        finish();
    }
}
