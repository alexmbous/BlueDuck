package com.blueduck.ride.qrcode;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.blueduck.ride.R;
import com.blueduck.ride.base.BaseActivity;
import com.blueduck.ride.main.activity.UnLockingActivity;
import com.blueduck.ride.utils.BroadCastValues;
import com.blueduck.ride.utils.CommonUtils;
import com.jungly.gridpasswordview.GridPasswordView;

public class EnterCodeActivity extends BaseActivity implements GridPasswordView.OnPasswordChangedListener{

    private static final String TAG = "EnterCodeActivity";

    private ImageView backImg;
    private GridPasswordView gridPasswordView;
    private EditText numberEt;
    private boolean isIssue,isLight = false;//是否是报故障扫码 / Is it a fault scan code
    private String curLat,curLng,outArea,rideUser;

    private CameraManager cameraManager;
    private Camera camera = null;
    private Camera.Parameters parameters;

    @Override
    protected int setLayoutViewId() {
        return R.layout.enter_code_activity;
    }

    @Override
    protected void init() {
        isIssue = getIntent().getBooleanExtra("isIssue",false);
        curLat = getIntent().getStringExtra("curLat");
        curLng = getIntent().getStringExtra("curLng");
        outArea = getIntent().getStringExtra("outArea");
        rideUser = getIntent().getStringExtra("rideUser");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//android6.0调用的手电筒接 / Flashlight connection called android6.0
            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        }
    }

    @Override
    protected void initView() {
        baseTitleLayout.setVisibility(View.VISIBLE);
        baseTitleText.setText(getString(R.string.enter_code_in_scooter));
        backImg = (ImageView) findViewById(R.id.title_left_image);
        backImg.setImageResource(R.drawable.close_black);
        gridPasswordView = (GridPasswordView) findViewById(R.id.password_view);
        gridPasswordView.setPasswordVisibility(true);
        gridPasswordView.setOnPasswordChangedListener(this);
        numberEt = (EditText) findViewById(R.id.enter_code_number_edit);
        findViewById(R.id.scan_code_layout).setOnClickListener(this);
        findViewById(R.id.ride_bottom).setOnClickListener(this);
    }

    /**
     * Jump to the unlock interface
     * 跳转到开锁界面
     * @param number
     */
    private void skipUnlocking(String number){
        CommonUtils.hideSoftinput(this);
        if (isIssue){
            Intent intent = new Intent();
            intent.putExtra("number", number);
            intent.setAction(BroadCastValues.SCAN_SUCCESS);
            sendBroadcast(intent);
            finish();
        }else {
            Intent intent = new Intent(this, UnLockingActivity.class);
            intent.putExtra("number", number);
            intent.putExtra("curLat", curLat);
            intent.putExtra("curLng", curLng);
            intent.putExtra("outArea",outArea);
            intent.putExtra("inputNumber","1");
            intent.putExtra("rideUser",rideUser);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.scan_code_layout:
                skipScan();
                break;
            case R.id.ride_bottom:
                /*String psw = gridPasswordView.getPassWord();
                if (psw.length() == 4){
                    skipUnlocking(psw);
                }*/
                String number = numberEt.getText().toString();
                if (TextUtils.isEmpty(number)){
                    Toast.makeText(this,getString(R.string.not_null),Toast.LENGTH_SHORT).show();
                }else{
                    skipUnlocking(number);
                }
                break;
        }
    }

    /**
     * Jump scan interface
     * 跳转扫描界面
     */
    private void skipScan(){
        Intent intent = new Intent(this,ScannerActivity.class);
        intent.putExtra("isIssue",isIssue);
        intent.putExtra("curLat",curLat);
        intent.putExtra("curLng",curLng);
        intent.putExtra("outArea",outArea);
        intent.putExtra("rideUser",rideUser);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        isLight = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//android6.0调用的手电筒接 / Flashlight connection called android6.0
            try {
                cameraManager.setTorchMode("0",isLight);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (camera != null) {
            camera.release();
            camera = null;
        }
        super.onPause();
    }

    /**
     * Flashlight click event
     * 手电筒点击事件**/
    private void lightClick(){
        if(isLight){
            isLight = false;
        }else{
            isLight = true;
        }
        openLight();
    }
    /**
     * Turn the flashlight on or off
     * 打开或关闭手电筒**/
    public void openLight(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//android6.0调用的手电筒接 / Flashlight connection called android6.0
            try {
                cameraManager.setTorchMode("0",isLight);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            //低于6.0系统的手电筒 / Flashlight below 6.0 system
            if (isLight){
                camera = Camera.open();
                parameters = camera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);//开启 / Open
                camera.setParameters(parameters);
                camera.startPreview();
            }else{
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);//关闭 / shut down
                camera.setParameters(parameters);
                camera.stopPreview();
                camera.release();
            }
        }
    }

    @Override
    public void onTextChanged(String psw) {

    }

    @Override
    public void onInputFinish(String psw) {

    }
}
