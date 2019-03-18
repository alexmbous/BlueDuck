package com.blueduck.ride.login.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.blueduck.ride.R;
import com.blueduck.ride.base.BaseActivity;
import com.blueduck.ride.login.service.LoginService;
import com.blueduck.ride.utils.BroadCastValues;
import com.blueduck.ride.utils.CommonUtils;
import com.blueduck.ride.utils.DateDialog;
import com.blueduck.ride.utils.GlideCircleTransform;
import com.blueduck.ride.utils.LogUtils;
import com.blueduck.ride.utils.RequestCallBack;
import com.blueduck.ride.utils.SelectPopupWindow;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PersonalInformationActivity extends BaseActivity implements SelectPopupWindow.SelectPopupWindowCall,RequestCallBack {

    private static final String TAG = "PersonalInformationActi";
    private ImageView imageHead;
    private EditText nameEt,emailEt,phoneEt,passwordEt,dateOfBirthEt;
    private Button createAccountBtn;
    private ImageButton showHidePasswordImageButton;
    private String imagePath,name,email,phone,password,birthday;
    private double lat,lng;
    private LoginService loginService;
    private FinishBroad finishBroad;
    private boolean isShowPasswordChecked = false;

    private Calendar ca;
    private int mYear,mMonth,mDay,maxDay;

    @Override
    protected int setLayoutViewId() {
        return R.layout.personal_information_activity;
    }

    @Override
    protected void init() {
        loginService = new LoginService(this,this,TAG);
        email = getIntent().getStringExtra("account");
        lat = getIntent().getDoubleExtra("lat",0);
        lng = getIntent().getDoubleExtra("lng",0);
        ca = Calendar.getInstance();
        mYear = ca.get(Calendar.YEAR);
        mMonth = ca.get(Calendar.MONTH);
        mDay = ca.get(Calendar.DAY_OF_MONTH);
        maxDay = ca.getActualMaximum(Calendar.DAY_OF_MONTH);
        initBroadCast();
    }

    private void initBroadCast(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadCastValues.FINISH_BROAD);
        finishBroad = new FinishBroad();
        registerReceiver(finishBroad,intentFilter);
    }

    @Override
    protected void initView() {
        baseTitleLayout.setVisibility(View.VISIBLE);
        baseTitleText.setText(getString(R.string.personal_information_title));
        imageHead = (ImageView) findViewById(R.id.image_head);
        imageHead.setOnClickListener(this);
        nameEt = (EditText) findViewById(R.id.name_edit);
        emailEt = (EditText) findViewById(R.id.email_edit);
        phoneEt = (EditText) findViewById(R.id.phone_edit);
        passwordEt = (EditText) findViewById(R.id.password_edit);
        dateOfBirthEt = (EditText) findViewById(R.id.date_of_birth_edit);
        dateOfBirthEt.setOnClickListener(this);
        createAccountBtn = (Button) findViewById(R.id.create_account_btn);
        showHidePasswordImageButton = (ImageButton) findViewById(R.id.show_hide_password_image_button);
        createAccountBtn.setOnClickListener(this);
        createAccountBtn.setEnabled(false);
        MyTextWatcher myTextWatcher = new MyTextWatcher();
        nameEt.addTextChangedListener(myTextWatcher);
        emailEt.addTextChangedListener(myTextWatcher);
        phoneEt.addTextChangedListener(myTextWatcher);
        passwordEt.addTextChangedListener(myTextWatcher);
        dateOfBirthEt.addTextChangedListener(myTextWatcher);
        emailEt.setText(email);
        showHidePasswordImageButton.setOnClickListener(this);
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
            checkData();
        }
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.image_head:
                checkPermission();
                break;
            case R.id.date_of_birth_edit:
                selectBirthday();
                break;
            case R.id.create_account_btn:
                createAccountBottom();
                break;
            case R.id.show_hide_password_image_button:
                showHidePassword(passwordEt, isShowPasswordChecked);
                isShowPasswordChecked = !isShowPasswordChecked;
                break;
        }
    }

    private void showHidePassword(EditText editText, boolean isShown) {
        System.out.println("isShown: " + isShown);
        if (!isShown) {
            // show password
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        } else {
            // hide password
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
        // select end of edittext
        editText.setSelection(editText.getText().length());
    }

    private void selectBirthday(){
        new DateDialog(this, new DateDialog.ConfirmBtn() {
            @Override
            public void confirm(int year, int month, int day) {
                dateOfBirthEt.setText((month < 10 ? ("0"+month) : month)+"/"+(day < 10 ? ("0"+day) : day)+"/"+year);
            }
        },mYear,mMonth,mDay,maxDay,DateDialog.Choose_birthday,0);
    }

    private void checkData(){
        name = nameEt.getText().toString();
        email = emailEt.getText().toString().trim();
        phone = phoneEt.getText().toString().trim();
        password = passwordEt.getText().toString().trim();
        birthday = dateOfBirthEt.getText().toString().trim();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(password) || TextUtils.isEmpty(birthday)){
            createAccountBtn.setEnabled(false);
            createAccountBtn.setBackground(ContextCompat.getDrawable(PersonalInformationActivity.this,R.drawable.skip_gray_btn_bg));
        }else{
            createAccountBtn.setEnabled(true);
            createAccountBtn.setBackground(ContextCompat.getDrawable(PersonalInformationActivity.this,R.drawable.skip_btn_bg));
        }
    }

    private void createAccountBottom(){
        checkData();
        if (!CommonUtils.isNumber(phone)){
            Toast.makeText(this,getString(R.string.phone_error),Toast.LENGTH_SHORT).show();
        }else if (password.length() < 8){
            Toast.makeText(this,getString(R.string.password_length_hint),Toast.LENGTH_SHORT).show();
        }else{
            getEmailCode();
        }
    }

    private void getEmailCode(){
        loginService.getEmailCode(email,"1",1);
    }

    private void handlerVerificationCode(int invalidMinute){
        LogUtils.i(TAG, "获取验证码成功ok");
        Intent intent = new Intent(this, VerificationActivity.class);
        intent.putExtra("skipType", 1);
        intent.putExtra("smsAndEmailType", "1");
        intent.putExtra("account", email);
        intent.putExtra("lat",lat);
        intent.putExtra("lng",lng);
        intent.putExtra("accountType",2);
        intent.putExtra("invalidMinute",invalidMinute);
        intent.putExtra("imagePath",imagePath);
        intent.putExtra("name",name);
        intent.putExtra("phone",phone);
        intent.putExtra("password",password);
        intent.putExtra("birthday",birthday);
        startActivity(intent);
    }

    @Override
    public void onSuccess(Object o, int flag) {
        if (flag == 1){
            int invalidMinute = (Integer) o;
            handlerVerificationCode(invalidMinute);
        }
    }

    @Override
    public void onFail(Throwable t, int flag) {

    }

    //同时请求相机，相册多项权限 At the same time request camera, album multiple permissions
    private File inputPhotoFile;//拍照回来的照片地址 Photo address returned
    private File outputPhotoFile;//裁剪后取出的照片地址 Photo address taken after cropping
    private static final int CAMERA = 100;//相机回调 Camera callback
    private static final int PHOTO_ALBUM = 101;//相册回调 Album callback
    private static final int TAILOR = 102;//裁剪回调 Crop callback
    private String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    private List<String> permissionList = new ArrayList<>();
    private void checkPermission(){
        permissionList.clear();
        for (String permission : permissions){
            if (ContextCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED){
                permissionList.add(permission);
            }
        }
        if (permissionList.isEmpty()){
            showSelectWindow();
        }else{
            ActivityCompat.requestPermissions(this,permissionList.toArray(new String[permissionList.size()]),1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1){//相机，相册 Camera, photo album
            for (int i = 0; i < grantResults.length; i ++){
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    if (i == grantResults.length - 1) {
                        showSelectWindow();
                    }
                }else{
                    Toast.makeText(this,getString(R.string.operation_failed),Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Display selection dialog
     * 显示选择对话框
     */
    private void showSelectWindow(){
        SelectPopupWindow selectPopupWindow = new SelectPopupWindow(this,this);
        selectPopupWindow.showAtLocation(imageHead, Gravity.BOTTOM, 0, 0);
    }

    @Override
    public void windowCall(int index) {
        if (index == 0){//打开相机 turn on camera
            String status = Environment.getExternalStorageState();//开启照相 Open photography
            if (status.equals(Environment.MEDIA_MOUNTED)) {
                inputPhotoFile = CommonUtils.startCameraPicCut(this,CAMERA);
            } else {
                Toast.makeText(this, "no SD card", Toast.LENGTH_LONG).show();
            }
        }else{//打开相册 Open album
            CommonUtils.startImageCapture(this,PHOTO_ALBUM);//开启手机图库 Open phone gallery
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA && resultCode == RESULT_OK) {//从相机拍照的图片 Picture taken from the camera
            outputPhotoFile = CommonUtils.startPhotoZoom(this,CommonUtils.getImageContentUri(this,inputPhotoFile), 150,TAILOR);//调用剪裁 Call clipping
        }else if (requestCode == TAILOR && resultCode == RESULT_OK && data != null) {//剪裁照片回调 Crop photo callback
            Bitmap bitmapImg = BitmapFactory.decodeFile(outputPhotoFile.getAbsolutePath());//拿到剪切数据 Get cut data
            imagePath = CommonUtils.saveImageToGallery(this, bitmapImg);
            showHead();
        }else if (requestCode == PHOTO_ALBUM && resultCode == RESULT_OK && data != null) {//从手机相册选择照片回调 Select a photo callback from your phone's photo album
            outputPhotoFile = CommonUtils.startPhotoZoom(this,data.getData(), 150,TAILOR);//调用剪裁 Call clipping
        }
    }

    /**
     * Show round picture
     * 显示圆形图片
     */
    private void showHead(){
        Glide.with(getApplicationContext())
                .load(imagePath)
                .transform(new GlideCircleTransform(getApplicationContext()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageHead);
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
