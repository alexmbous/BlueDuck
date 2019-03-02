package com.blueduck.ride.base;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blueduck.ride.R;
import com.blueduck.ride.utils.BroadCastValues;
import com.blueduck.ride.utils.CommonSharedValues;
import com.blueduck.ride.utils.CommonUtils;

public abstract class BaseActivity extends Activity implements View.OnClickListener {

    protected View baseTitleLayout,baseDividerView;//公共标题栏布局 Public title bar layout
    protected TextView baseTitleText;//公共标题栏文本 Public title bar text

    protected SharedPreferences sp,shared;
    private boolean isRunning = false;
    private PushBroad pushBroad = null;
    protected MyApplication myApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonUtils.add(this);
        init();
        setContentView(R.layout.base_activity);
        setMainContentView(setLayoutViewId());
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    /**
     * 设置内容布局文件
     * Set content layout file
     * @param layoutId
     * @return
     */
    private void setMainContentView(int layoutId){
        myApplication = (MyApplication) getApplication();
        sp = getSharedPreferences(CommonSharedValues.SP_NAME, MODE_PRIVATE);
        shared = getSharedPreferences(CommonSharedValues.SAVE_LOGIN, MODE_PRIVATE);
        LinearLayout layout = (LinearLayout) findViewById(R.id.base_content_view);
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(layoutId,null);
        layout.addView(view,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        baseTitleLayout = (View) findViewById(R.id.base_title_view);
        baseTitleText = (TextView) findViewById(R.id.common_title_text);
        findViewById(R.id.title_left_layout).setOnClickListener(this);
        baseDividerView = (View) findViewById(R.id.divider_view);
        initView();
        initBroad();
    };

    protected void init(){};
    protected abstract int setLayoutViewId();
    protected void initView(){}

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.title_left_layout:
                finish();
                break;
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    @Override
    protected void onStart() {
        super.onStart();
        isRunning = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isRunning = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(pushBroad);
        CommonUtils.remove(this);
    }

    private void initBroad(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadCastValues.GOOGLE_PUSH_BROADCAST);
        pushBroad = new PushBroad();
        registerReceiver(pushBroad,intentFilter);
    }
    /**
     * google消息推送广播(异地登录)
     * Google message push broadcast (offsite login)
     */
    private class PushBroad extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (BroadCastValues.GOOGLE_PUSH_BROADCAST.equals(intent.getAction())){
                String token = sp.getString(CommonSharedValues.SP_KEY_TOKEN, "");
                if (!TextUtils.isEmpty(token)) {
                    CommonUtils.remoteLoginDialog(BaseActivity.this);
                }else{
                    CommonUtils.getNotificationManager(BaseActivity.this).cancel(CommonSharedValues.PUSH_SERVICE_NOTIFICATION);
                }
            }
        }
    }
}
