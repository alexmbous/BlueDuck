package com.blueduck.ride.personal.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blueduck.ride.R;
import com.blueduck.ride.base.BaseActivity;
import com.blueduck.ride.login.activity.VerificationActivity;
import com.blueduck.ride.login.service.LoginService;
import com.blueduck.ride.main.bean.UserInfoBean;
import com.blueduck.ride.main.service.MainService;
import com.blueduck.ride.utils.BroadCastValues;
import com.blueduck.ride.utils.CommonSharedValues;
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

public class MyAccountActivity extends BaseActivity implements RequestCallBack,SelectPopupWindow.SelectPopupWindowCall {

    private static final String TAG = "MyAccountActivity";
    public static final String CHANGE_SUCCESS = "change_success";
    private ImageView headImg;
    private EditText firstNameEt,lastNameEt,passwordEt,phoneEt;
    private TextView emailEt,birthEt;

    private Calendar ca;
    private int mYear,mMonth,mDay,maxDay;
    private String firstName,lastName,email,phone,birthday,password,imagePath;
    private LoginService loginService;
    private MainService mainService;

    private ChangeBroad changeBroad;

    @Override
    protected int setLayoutViewId() {
        return R.layout.my_account_activity;
    }

    @Override
    protected void init() {
        mainService = new MainService(this,this,TAG);
        loginService = new LoginService(this,this,TAG);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//将状态栏设置为透明(android4.4以上才有效)
        ca = Calendar.getInstance();
        mYear = ca.get(Calendar.YEAR);
        mMonth = ca.get(Calendar.MONTH);
        mDay = ca.get(Calendar.DAY_OF_MONTH);
        maxDay = ca.getActualMaximum(Calendar.DAY_OF_MONTH);
        initBroad();
    }

    private void initBroad(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CHANGE_SUCCESS);
        changeBroad = new ChangeBroad();
        registerReceiver(changeBroad,intentFilter);
    }

    @Override
    protected void initView() {
        View statusBar = (View) findViewById(R.id.status_bar_view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //如果当前版本号大于android4.4
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, CommonUtils.getStatusBarHeight(this));
            statusBar.setLayoutParams(params);
        }
        findViewById(R.id.my_account_left_layout).setOnClickListener(this);
        findViewById(R.id.my_account_right_layout).setOnClickListener(this);
        headImg = (ImageView) findViewById(R.id.head_image);
        headImg.setOnClickListener(this);
        firstNameEt = (EditText) findViewById(R.id.first_name_edit);
        lastNameEt = (EditText) findViewById(R.id.last_name_edit);
        emailEt = (TextView) findViewById(R.id.my_account_email_edit);
        phoneEt = (EditText) findViewById(R.id.my_account_phone_edit);
        birthEt = (TextView) findViewById(R.id.my_account_date_of_birth_edit);
        birthEt.setOnClickListener(this);
        passwordEt = (EditText) findViewById(R.id.my_account_password_edit);
        findViewById(R.id.change_password_text).setOnClickListener(this);
        findViewById(R.id.log_out_text).setOnClickListener(this);
        getUserInfo();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.my_account_left_layout:
                finish();
                break;
            case R.id.my_account_right_layout:
                saveBtn();
                break;
            case R.id.head_image:
                checkPermission();
                break;
            case R.id.my_account_date_of_birth_edit:
                selectBirthday();
                break;
            case R.id.change_password_text:
                changePassword();
                break;
            case R.id.log_out_text:
                showLogOutDialog();
                break;
        }
    }

    private void changePassword(){
        email = emailEt.getText().toString().trim();
        getEmailCode();
    }

    /**
     * 退出登录弹出框
     * Exit the login pop-up box
     */
    private void showLogOutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.hint));
        builder.setMessage(getString(R.string.confirm_exit_login));
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendBroadcast(new Intent(BroadCastValues.LOG_OUT));
                CommonUtils.logOut(MyAccountActivity.this);
                finish();
            }
        });
        builder.create().show();
    }

    private void saveBtn(){
        firstName = firstNameEt.getText().toString().trim();
        lastName = lastNameEt.getText().toString().trim();
        email = emailEt.getText().toString().trim();
        phone = phoneEt.getText().toString().trim();
        birthday = birthEt.getText().toString().trim();
        password = passwordEt.getText().toString().trim();
        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone)
                || TextUtils.isEmpty(birthday) || TextUtils.isEmpty(password)){
            Toast.makeText(this,getString(R.string.not_null),Toast.LENGTH_SHORT).show();
        }else{
            if (!TextUtils.isEmpty(imagePath)){
                loginService.amazonS3Upload(imagePath,5);
            }else{
                uploadUserInfo();
            }
        }
    }

    private void selectBirthday(){
        new DateDialog(this, new DateDialog.ConfirmBtn() {
            @Override
            public void confirm(int year, int month, int day) {
                birthEt.setText((month < 10 ? ("0"+month) : month)+"/"+(day < 10 ? ("0"+day) : day)+"/"+year);
            }
        },mYear,mMonth,mDay,maxDay,DateDialog.Choose_birthday,0);
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
        selectPopupWindow.showAtLocation(headImg, Gravity.BOTTOM, 0, 0);
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
            showHead(imagePath);
        }else if (requestCode == PHOTO_ALBUM && resultCode == RESULT_OK && data != null) {//从手机相册选择照片回调 Select a photo callback from your phone's photo album
            outputPhotoFile = CommonUtils.startPhotoZoom(this,data.getData(), 150,TAILOR);//调用剪裁 Call clipping
        }
    }

    /**
     * Show round picture
     * 显示圆形图片
     */
    private void showHead(String imagePath){
        Glide.with(getApplicationContext())
                .load(imagePath)
                .transform(new GlideCircleTransform(getApplicationContext()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(headImg);
    }

    /**
     * Get user information
     * 获得用户信息
     */
    private void getUserInfo(){
        mainService.getUserInfo(sp.getString(CommonSharedValues.SP_KEY_TOKEN,""),true,1);
    }

    /**
     * Update user avatar
     * 更新用户头像
     */
    private void updatePhoto(String url){
        loginService.updatePhoto(sp.getString(CommonSharedValues.SP_KEY_TOKEN,""),url,2);
    }

    /**
     * 上传用户信息
     * Upload user information
     */
    private void uploadUserInfo(){
        loginService.uploadUserInfo(sp.getString(CommonSharedValues.SP_KEY_TOKEN,""),firstName,lastName,email,phone,password,birthday,"2",3);
    }

    /**
     * 获得邮箱验证码
     * Get the mailbox verification code
     */
    private void getEmailCode(){
        loginService.getEmailCode(email,"2",4);
    }

    private void handlerUploadUserInfo(){
        sendBroadcast(new Intent(BroadCastValues.UPDATE_USER_INFO));
        finish();
    }

    private void handlerUserInfo(UserInfoBean bean){
        CommonUtils.saveUserInfo(sp,bean);
        String imagePath = bean.getUserInfo().getUserVo().getHeadUrl();
        if (!TextUtils.isEmpty(imagePath)) {
            showHead(imagePath);
        }
        String firstN = bean.getUserInfo().getFirstname();
        String lastN = bean.getUserInfo().getLastname();
        if (!TextUtils.isEmpty(firstN)) {
            if (firstN.contains(" ")) {
                String[] n = firstN.split(" ");
                firstNameEt.setText(n[0]);
                lastNameEt.setText(n[1]);
            } else {
                if (firstN.equals(lastN)){
                    firstNameEt.setText(firstN);
                }else{
                    firstNameEt.setText(firstN);
                    lastNameEt.setText(lastN);
                }
            }
        }
        emailEt.setText(bean.getUserInfo().getEmail());
        phoneEt.setText(bean.getUserInfo().getUserVo().getPhone());
        birthEt.setText(bean.getUserInfo().getBirthday());
        String password = sp.getString(CommonSharedValues.SP_KEY_PASSWORD, "");
        if (!TextUtils.isEmpty(password)){
            passwordEt.setText(password);
        }else{
            passwordEt.setText("12345678");
        }
    }

    private void handlerVerificationCode(int invalidMinute){
        LogUtils.i(TAG, "获取验证码成功ok");
        Intent intent = new Intent(this, VerificationActivity.class);
        intent.putExtra("skipType", 2);
        intent.putExtra("smsAndEmailType", "2");
        intent.putExtra("account", email);
        intent.putExtra("accountType",2);
        intent.putExtra("invalidMinute",invalidMinute);
        startActivity(intent);
    }

    @Override
    public void onSuccess(Object o, int flag) {
        if (flag == 1) {
            UserInfoBean bean = (UserInfoBean) o;
            handlerUserInfo(bean);
        }else if (flag == 2){
            uploadUserInfo();
        }else if (flag == 3){
            handlerUploadUserInfo();
        }else if (flag == 4){
            int invalidMinute = (Integer) o;
            handlerVerificationCode(invalidMinute);
        }else if (flag == 5){
            String url = (String) o;
            updatePhoto(url);
        }
    }

    @Override
    public void onFail(Throwable t, int flag) {

    }

    private class ChangeBroad extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if (CHANGE_SUCCESS.equals(intent.getAction())){
                passwordEt.setText(sp.getString(CommonSharedValues.SP_KEY_PASSWORD,""));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(changeBroad);
    }
}
