package com.blueduck.ride.login.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;

import com.blueduck.ride.R;
import com.blueduck.ride.base.BaseActivity;
import com.blueduck.ride.utils.BroadCastValues;
import com.blueduck.ride.utils.CommonSharedValues;
import com.blueduck.ride.utils.RequestDialog;
import com.blueduck.ride.utils.RetrofitHttp;

public class TermsOfUseActivity extends BaseActivity {

    private static final String TAG = "TermsOfUseActivity";
    private WebView webView;
    private ImageView agreeImg;
    private Button agreeBtn;

    private boolean isAgree = false;
    private String url;
    private FinishBroad finishBroad;

    @Override
    protected int setLayoutViewId() {
        return R.layout.terms_of_use_activity;
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
        baseTitleLayout.setVisibility(View.VISIBLE);
        baseTitleText.setText(getString(R.string.terms_of_use));
        webView = (WebView) findViewById(R.id.web_view);
        agreeImg = (ImageView) findViewById(R.id.agree_image);
        agreeImg.setOnClickListener(this);
        agreeBtn = (Button) findViewById(R.id.agree_btn);
        agreeBtn.setOnClickListener(this);
        agreeBtn.setEnabled(false);
        setData();
    }

    private void setData(){
        url = RetrofitHttp.BASE_URL + "other?requestType=50001&industryId=" + CommonSharedValues.industryType + "&type=" + 1;
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                RequestDialog.show(TermsOfUseActivity.this);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                String title = view.getTitle();
                if (!TextUtils.isEmpty(title)){
                    baseTitleText.setText(title);
                }
                RequestDialog.dismiss(TermsOfUseActivity.this);
            }

        });
        webView.loadUrl(url);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.agree_image:
                checkAgreeImg();
                break;
            case R.id.agree_btn:
                agreeBtn();
                break;
        }
    }

    private void agreeBtn(){
        startActivity(new Intent(this,ScanDriverLicenseActivity.class));
    }

    private void checkAgreeImg(){
        if (!isAgree){
            isAgree = true;
            agreeImg.setImageResource(R.drawable.check);
            agreeBtn.setEnabled(true);
            agreeBtn.setBackground(ContextCompat.getDrawable(TermsOfUseActivity.this,R.drawable.skip_btn_bg));
        }else{
            isAgree = false;
            agreeImg.setImageResource(R.drawable.uncheck);
            agreeBtn.setEnabled(false);
            agreeBtn.setBackground(ContextCompat.getDrawable(TermsOfUseActivity.this,R.drawable.skip_gray_btn_bg));
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
