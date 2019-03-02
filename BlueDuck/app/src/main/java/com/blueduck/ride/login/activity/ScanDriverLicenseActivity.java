package com.blueduck.ride.login.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.blueduck.ride.R;
import com.blueduck.ride.base.BaseActivity;
import com.blueduck.ride.qrcode.ScannerActivity;
import com.blueduck.ride.utils.BroadCastValues;
import com.blueduck.ride.utils.CommonUtils;

public class ScanDriverLicenseActivity extends BaseActivity {

    private static final String TAG = "ScanDriverLicenseActivi";

    private FinishBroad finishBroad;

    @Override
    protected int setLayoutViewId() {
        return R.layout.scan_driver_license_activity;
    }

    @Override
    protected void init() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadCastValues.FINISH_BROAD);
        finishBroad = new FinishBroad();
        registerReceiver(finishBroad,intentFilter);
    }

    @Override
    protected void initView() {
        findViewById(R.id.do_this_later_text).setOnClickListener(this);
        findViewById(R.id.scan_now_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.do_this_later_text:
                laterSkip();
                break;
            case R.id.scan_now_btn:
                getCameraPermission();
                break;
        }
    }

    private void laterSkip(){
        startActivity(new Intent(this,EnableLocationActivity.class));
    }

    private void scanDriverLicense(){
        Intent intent = new Intent(this,ScannerActivity.class);
        intent.putExtra("isLicense",true);
        startActivity(intent);
    }

    /**
     * Detect camera permissions
     * 检测相机权限
     **/
    private void getCameraPermission(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        } else {
            scanDriverLicense();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    scanDriverLicense();
                }else{
                    CommonUtils.hintDialog(this, getString(R.string.not_camera_permission));
                }
                break;
        }
    }

    /**
     * 结束界面广播
     * End interface broadcast
     */
    private class FinishBroad extends BroadcastReceiver {

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
        unregisterReceiver(finishBroad);
    }
}
