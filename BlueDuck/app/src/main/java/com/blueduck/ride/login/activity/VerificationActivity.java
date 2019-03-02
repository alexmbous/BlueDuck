package com.blueduck.ride.login.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.blueduck.ride.R;
import com.blueduck.ride.base.BaseActivity;
import com.blueduck.ride.login.bean.LoginBean;
import com.blueduck.ride.login.service.LoginService;
import com.blueduck.ride.main.activity.MainActivity;
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

    private int skipType,accountType;
    private String smsAndEmailType,account,regions;
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
        String timeStr = "";
        if (timeCount < 10){
            timeStr = "0"+timeCount;
        }else{
            timeStr = timeCount+"";
        }
        String showTime = String.format("%s:%s", "00", timeStr);
        time.setText(showTime);
        time.setEnabled(false);
    }

    /**
     * Stop countdown
     * 停止倒计时
     */
    private void stopTime(){
        timeCount = 60;
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

    private void getEmailCode(){
        loginService.getEmailCode(account,smsAndEmailType,1);
    }

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

    private void handlerResendCode(){
        LogUtils.i(TAG, "获取验证码成功ok");
        handler.sendEmptyMessage(VERIFICATION_CODE_TIME);
    }

    private void handlerRegister(LoginBean loginBean){
        CommonUtils.saveLoginInfo(sp,loginBean,account,regions,accountType);
        if("1".equals(loginBean.getIsRegister())){
            LogUtils.i(TAG, "handlerResult: 是注册");
            Intent intent = new Intent(this,PersonalInformationActivity.class);
            intent.putExtra("account",account);
            startActivity(intent);
        }else{
            LogUtils.i(TAG, "handlerResult: 是登录");
            startActivity(new Intent(this,MainActivity.class));
            sendBroadcast(new Intent(BroadCastValues.FINISH_BROAD));
        }
    }

    private void handlerChangePassword(String token){
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(CommonSharedValues.SP_KEY_TOKEN,token);
        editor.apply();
        startActivity(new Intent(this,ResetPasswordActivity.class));
    }

    @Override
    public void onSuccess(Object o, int flag) {
        if (flag == 1){
            handlerResendCode();
        }else if (flag == 2){
            LoginBean loginBean = (LoginBean) o;
            handlerRegister(loginBean);
        }else if (flag == 3){
            String token = (String) o;
            handlerChangePassword(token);
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
            }else if (skipType == 2){//修改密码 change password
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
