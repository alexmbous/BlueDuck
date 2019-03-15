package com.blueduck.ride.login.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.blueduck.ride.R;
import com.blueduck.ride.base.BaseActivity;
import com.blueduck.ride.login.bean.LoginBean;
import com.blueduck.ride.login.service.LoginService;
import com.blueduck.ride.personal.activity.ResetPasswordActivity;
import com.blueduck.ride.utils.BroadCastValues;
import com.blueduck.ride.utils.CommonSharedValues;
import com.blueduck.ride.utils.CommonUtils;
import com.blueduck.ride.utils.LogUtils;
import com.blueduck.ride.utils.RequestCallBack;
import com.jungly.gridpasswordview.GridPasswordView;


public class VerificationActivity extends BaseActivity implements GridPasswordView.OnPasswordChangedListener,RequestCallBack {

    private static final String TAG = "VerificationActivity";
    private static final int VERIFICATION_CODE_TIME = 0;
    private GridPasswordView gridPasswordView;
    private TextView time;

    private int skipType,accountType,invalidMinute;
    private String smsAndEmailType,account,regions,imagePath,name,phone,password,birthday;
    private double lat,lng;
    private LoginService loginService;
    private FinishBroad finishBroad;

    private int timeCount = 60;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case VERIFICATION_CODE_TIME:
                    timeCount --;
                    if (timeCount > 0) {
                        startTime(timeCount);
                        handler.sendEmptyMessageDelayed(VERIFICATION_CODE_TIME,1000);
                    }else{
                        stopTime();
                    }
                    break;
            }
        }
    };

    @Override
    protected int setLayoutViewId() {
        return R.layout.verification_activity;
    }

    @Override
    protected void init() {
        loginService = new LoginService(this,this,TAG);
        skipType = getIntent().getIntExtra("skipType",0);
        smsAndEmailType = getIntent().getStringExtra("smsAndEmailType");
        account = getIntent().getStringExtra("account");
        lat = getIntent().getDoubleExtra("lat",0);
        lng = getIntent().getDoubleExtra("lng",0);
        accountType = getIntent().getIntExtra("accountType",0);
        invalidMinute = getIntent().getIntExtra("invalidMinute",0);

        imagePath = getIntent().getStringExtra("imagePath");
        name = getIntent().getStringExtra("name");
        phone = getIntent().getStringExtra("phone");
        password = getIntent().getStringExtra("password");
        birthday = getIntent().getStringExtra("birthday");

        timeCount = 60 * invalidMinute;
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
        baseTitleText.setText(getString(R.string.verification));
        gridPasswordView = (GridPasswordView) findViewById(R.id.password_view);
        gridPasswordView.setPasswordVisibility(true);
        gridPasswordView.setOnPasswordChangedListener(this);
        time = (TextView) findViewById(R.id.resend_code_time);
        time.setOnClickListener(this);
        time.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String timeStr = time.getText().toString();
        if (!"Resend".equals(timeStr)) {
            handler.sendEmptyMessageDelayed(VERIFICATION_CODE_TIME, 1000);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeMessages(VERIFICATION_CODE_TIME);
    }

    /**
     * Start countdown
     * 启动倒计时
     * @param timeCount
     */
    private void startTime(int timeCount){
        String showTime = String.format("%s:%s", setTimeFormat(timeCount / 60), setTimeFormat(timeCount % 60));
        time.setText(showTime);
        time.setEnabled(false);
    }

    private String setTimeFormat(long time){
        String timeStr = "";
        if (time < 10){
            timeStr = "0" + time;
        }else{
            timeStr = time+"";
        }
        return timeStr;
    }

    /**
     * Stop countdown
     * 停止倒计时
     */
    private void stopTime(){
        timeCount = 60 * invalidMinute;
        time.setEnabled(true);
        time.setText(getString(R.string.resend));
        handler.removeMessages(VERIFICATION_CODE_TIME);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.resend_code_time:
                if (accountType == 2){//邮箱 email
                    getEmailCode();
                }
                break;
        }
    }

    /**
     * 获得邮箱验证码
     * Get the mailbox verification code
     */
    private void getEmailCode(){
        loginService.getEmailCode(account,smsAndEmailType,1);
    }

    /**
     * 注册
     * register
     * @param code
     */
    private void registerLogin(String code){
        loginService.registerLogin(account,code,lat,lng,
                shared.getString(CommonSharedValues.GOOGLE_PUSH_TOKEN,""),
                CommonUtils.getUniqueId(this),2);
    }

    /**
     * 修改密码
     * change password
     * @param code
     */
    private void changePassword(String code){
        loginService.changePassword(account,code,3);
    }

    /**
     * Update user avatar
     * 更新用户头像
     */
    private void updatePhoto(String url){
        loginService.updatePhoto(sp.getString(CommonSharedValues.SP_KEY_TOKEN,""),url,5);
    }
    /**
     * 上传用户信息
     * Upload user information
     */
    private void uploadUserInfo(){
        loginService.uploadUserInfo(sp.getString(CommonSharedValues.SP_KEY_TOKEN,""),name,name,account,phone,password,birthday,"1",6);
    }

    private void handlerResendCode(){
        LogUtils.i(TAG, "获取验证码成功ok");
        handler.sendEmptyMessage(VERIFICATION_CODE_TIME);
    }

    private void handlerRegister(LoginBean loginBean){
        CommonUtils.saveLoginInfo(sp,loginBean,account,regions,accountType);
        if (!TextUtils.isEmpty(imagePath)){
            //loginService.amazonS3Upload(imagePath,4);
            //TODO: handle avatar
        }else{
            uploadUserInfo();
        }
    }

    private void handlerChangePassword(String token){
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(CommonSharedValues.SP_KEY_TOKEN,token);
        editor.apply();
        Intent intent = new Intent(this,ResetPasswordActivity.class);
        intent.putExtra("skipType",skipType);
        startActivity(intent);
    }

    private void handlerUploadUserInfo(){
        startActivity(new Intent(this,AddPaymentMethodActivity.class));
    }

    @Override
    public void onSuccess(Object o, int flag) {
        if (flag == 1){
            invalidMinute = (Integer) o;
            timeCount = 60 * invalidMinute;
            handlerResendCode();
        }else if (flag == 2){
            LoginBean loginBean = (LoginBean) o;
            handlerRegister(loginBean);
        }else if (flag == 3){
            String token = (String) o;
            handlerChangePassword(token);
        }else if (flag == 4){
            String url = (String) o;
            updatePhoto(url);
        }else if (flag == 5){
            uploadUserInfo();
        }else if (flag == 6){
            handlerUploadUserInfo();
        }
    }

    @Override
    public void onFail(Throwable t, int flag) {

    }

    @Override
    public void onTextChanged(String psw) {
        if (psw.length() == 6){
            String code = gridPasswordView.getPassWord();
            if (skipType == 1){//注册 register
                registerLogin(code);
            }else if (skipType == 2 || skipType == 3){//修改密码 change password / 忘记密码 forget password
                changePassword(code);
            }
        }
    }

    @Override
    public void onInputFinish(String psw) {

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
