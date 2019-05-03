package com.blueduck.ride.qrcode;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blueduck.ride.R;
import com.blueduck.ride.base.BaseActivity;
import com.blueduck.ride.login.activity.EnableLocationActivity;
import com.blueduck.ride.main.activity.UnLockingActivity;
import com.blueduck.ride.qrcode.zbar.Result;
import com.blueduck.ride.qrcode.zbar.ZBarScannerView;
import com.blueduck.ride.utils.BroadCastValues;
import com.blueduck.ride.utils.CommonUtils;
import com.blueduck.ride.utils.LogUtils;


/**
 * Scan code interface
 * 扫码界面
 */
public class ScannerActivity extends BaseActivity implements ZBarScannerView.ResultHandler {

    private static final String TAG = "ScannerActivity";

    private ZBarScannerView zBarScannerView;
    private MediaPlayer mediaPlayer;//扫描成功播放音效类 / Scan successfully played sound effects
    private Vibrator vibrator;//扫描成功震动类 / Scan successful vibration class

    private ImageView topImg;
    private TextView title,topDescription;
    private LinearLayout inputLayout;

    private boolean isIssue = false;//是否是报故障扫码 / Is it a fault scan code
    private boolean isLicense = false;//是否扫描驾照 / Scan your driver's license
    private String curLat,curLng,outArea,rideUser;
    private String pricePerMinute;

    @Override
    protected int setLayoutViewId() {
        return R.layout.scanner_activity;
    }

    @Override
    protected void init() {
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);
        mediaPlayer = MediaPlayer.create(this,R.raw.beep);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        isIssue = getIntent().getBooleanExtra("isIssue",false);
        isLicense = getIntent().getBooleanExtra("isLicense",false);
        curLat = getIntent().getStringExtra("curLat");
        curLng = getIntent().getStringExtra("curLng");
        outArea = getIntent().getStringExtra("outArea");
        rideUser = getIntent().getStringExtra("rideUser");
        pricePerMinute = getIntent().getStringExtra("pricePerMinute");
    }

    @Override
    protected void initView() {
        View statusBar = (View) findViewById(R.id.status_bar_view);
        LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,CommonUtils.getStatusBarHeight(this));
        statusBar.setLayoutParams(ll);
        title = (TextView) findViewById(R.id.scan_code_title_text);
        topImg = (ImageView) findViewById(R.id.scan_code_top_image);
        topDescription = (TextView) findViewById(R.id.scan_code_top_description);
        findViewById(R.id.scan_code_left_layout).setOnClickListener(this);
        inputLayout = (LinearLayout) findViewById(R.id.scan_code_input_layout);
        inputLayout.setOnClickListener(this);
        findViewById(R.id.scan_code_flashlight_layout).setOnClickListener(this);
        if (isLicense){
            title.setText(getString(R.string.scan_driver_license));
            topImg.setVisibility(View.GONE);
            topDescription.setVisibility(View.VISIBLE);
            inputLayout.setVisibility(View.GONE);
        }
        if(!isLicense && !isIssue && (pricePerMinute != null)) { // if ride Scan QR
            topDescription.setText("$1 to unlock, then " + pricePerMinute + "/minute");
            topDescription.setVisibility(View.VISIBLE);
        }
        ViewGroup container = findViewById(R.id.container);
        //ViewFinderView is a view customized according to requirements, will be overlaid on the camera preview screen,
        // usually including scan code frame, scan line, shadow mask around the scan code frame, etc.
        //ViewFinderView是根据需求自定义的视图，会被覆盖在相机预览画面之上，通常包含扫码框、扫描线、扫码框周围的阴影遮罩等
        zBarScannerView = new ZBarScannerView(this, new ViewFinderView(this,isLicense), this);
        container.addView(zBarScannerView);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.scan_code_left_layout:
                finish();
                break;
            case R.id.scan_code_flashlight_layout://手电筒 / flashlight
                lightClick();
                break;
            case R.id.scan_code_input_layout://手动输入 / Manual input
                skipEnterCode();
                break;
        }
    }

    /**
     * Jump input coding interface
     * 跳转输入编码界面
     */
    private void skipEnterCode(){
        Intent intent = new Intent(this,EnterCodeActivity.class);
        intent.putExtra("isIssue",isIssue);
        intent.putExtra("curLat",curLat);
        intent.putExtra("curLng",curLng);
        intent.putExtra("outArea",outArea);
        intent.putExtra("rideUser",rideUser);
        startActivity(intent);
        finish();
    }

    /**
     * flashlight
     * 手电筒
     */
    private void lightClick(){
        if(zBarScannerView.isFlashOn()){
            zBarScannerView.setFlash(false);//关闭手电筒 Turn off the flashlight
        }else{
            zBarScannerView.setFlash(true);//开启手电筒 Turn on the flashlight
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        zBarScannerView.startCamera();//打开系统相机，并进行基本的初始化 Turn on the system camera and perform basic initialization
    }

    @Override
    public void onPause() {
        super.onPause();
        zBarScannerView.stopCamera();//释放相机资源等各种资源 Free resources such as camera resources
        zBarScannerView.setFlash(false);//关闭手电筒 Turn off the flashlight
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void handleResult(Result rawResult) {
        LogUtils.i(TAG,"扫描结果回调 ===: "+"Contents = " + rawResult.getContents() + ", Format = " + rawResult.getBarcodeFormat().getName());
        String resultString = rawResult.getContents();
        String resultType = rawResult.getBarcodeFormat().getName();
        System.out.println("resultType: " + resultType);
        if (!TextUtils.isEmpty(resultString) && !TextUtils.isEmpty(resultType)){
            //Type: QR code: "QRCODE" Barcode: "EAN13"
            if ("QRCODE".equals(resultType) || "EAN13".equals(resultType)){//类型：二维码："QRCODE"  条形码："EAN13"
                scanSuccessMusic();
                if (resultString.contains("=")) {
                    int index = resultString.lastIndexOf("=");
                    resultString = resultString.substring(index + 1, resultString.length());
                }
                if (isIssue) {
                    Intent intent = new Intent();
                    intent.putExtra("number", resultString);
                    intent.setAction(BroadCastValues.SCAN_SUCCESS);
                    sendBroadcast(intent);
                    finish();
                }else if (isLicense){
                    Intent intent = new Intent(this,EnableLocationActivity.class);
                    intent.putExtra("number",resultString);
                    startActivity(intent);
                    finish();
                }else{
                    Intent intent = new Intent(this, UnLockingActivity.class);
                    intent.putExtra("number", resultString);
                    intent.putExtra("curLat", curLat);
                    intent.putExtra("curLng", curLng);
                    intent.putExtra("outArea",outArea);
                    intent.putExtra("rideUser",rideUser);
                    startActivity(intent);
                    finish();
                }
            }else{
                //Frame bug! (Because sometimes nothing is scanned, it returns a number starting with 0, the type is not "QRCODE or EAN13",
                //The correct scan result type is "QRCODE or EAN13" so it can only be masked again.
                //框架bug!(因为有时什么都没扫就返回一段以0开头的编号,类型并不是“QRCODE 或者 EAN13”，
                // 正确的扫描结果类型为“QRCODE 或者 EAN13”所以只能屏蔽这次结果再次识别)
                zBarScannerView.getOneMoreFrame();//再获取一帧图像数据进行识别 Then acquire one frame of image data for identification
            }
        }else{
            Toast.makeText(this,getString(R.string.operation_failed_please_try_again),Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Play scan effect
     * 播放扫描效果
     */
    private void scanSuccessMusic(){
        mediaPlayer.start();//播放扫描音乐 Play scan music
        vibrator.vibrate(200);
    }
}