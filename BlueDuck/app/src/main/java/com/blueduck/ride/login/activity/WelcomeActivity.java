package com.blueduck.ride.login.activity;

import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.view.WindowManager;

import com.blueduck.ride.main.activity.MainActivity;
import com.blueduck.ride.R;
import com.blueduck.ride.base.BaseActivity;
import com.blueduck.ride.utils.CommonSharedValues;
import com.blueduck.ride.utils.CommonUtils;

public class WelcomeActivity extends BaseActivity {

    private static final String TAG = "WelcomeActivity";

    private Handler handler = new Handler();

    @Override
    protected int setLayoutViewId() {
        return R.layout.welcome_activity;
    }

    @Override
    protected void init() {
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);
    }

    @Override
    protected void initView() {
        if (!CommonUtils.isNetwork(this)) {//没有网络则弹框结束界面 If there is no network, the frame ends.
            CommonUtils.networkDialog(this, true);
            return;
        }
        String token = sp.getString(CommonSharedValues.SP_KEY_TOKEN, "");
        String firstLogin = shared.getString(CommonSharedValues.IS_FIRST_LOGIN, "");
        if (TextUtils.isEmpty(firstLogin)){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(WelcomeActivity.this,PageActivity.class));
                    finish();
                }
            }, 3000);
        }else{
            if (TextUtils.isEmpty(token)) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
                        finish();
                    }
                }, 3000);
            } else {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                        finish();
                    }
                }, 3000);
            }
        }
    }
}
