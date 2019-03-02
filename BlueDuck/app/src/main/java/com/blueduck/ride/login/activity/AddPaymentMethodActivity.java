package com.blueduck.ride.login.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;

import com.blueduck.ride.R;
import com.blueduck.ride.base.BaseActivity;
import com.blueduck.ride.utils.BroadCastValues;
import com.blueduck.ride.utils.LogUtils;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

public class AddPaymentMethodActivity extends BaseActivity {

    private static final String TAG = "AddPaymentMethodActivit";
    private FinishBroad finishBroad;

    @Override
    protected int setLayoutViewId() {
        return R.layout.add_payment_method_activity;
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
        baseTitleText.setText(getString(R.string.add_payment_method));
        findViewById(R.id.enter_card_layout).setOnClickListener(this);
        findViewById(R.id.scan_card_layout).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.enter_card_layout:
                skipEnter("","","","");
                break;
            case R.id.scan_card_layout:
                scanCard();
                break;
        }
    }

    private void skipEnter(String number,String date,String cvv,String name){
        Intent intent = new Intent(this,EnterCardActivity.class);
        intent.putExtra("tag",1);
        intent.putExtra("number",number);
        intent.putExtra("date",date);
        intent.putExtra("cvv",cvv);
        intent.putExtra("name",name);
        startActivity(intent);
    }

    private void scanCard(){
        Intent scanIntent = new Intent(this, CardIOActivity.class);
        // customize these values to suit your needs.
        // 有可能扫描出有效日期
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, false); // default: false
        // CVV
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_RESTRICT_POSTAL_CODE_TO_NUMERIC_ONLY, false); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CARDHOLDER_NAME, false); // default: false
        // hides the manual entry button
        // if set, developers should provide their own manual entry mechanism in the app
        scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, true); // default: false
        // matches the theme of your application
        scanIntent.putExtra(CardIOActivity.EXTRA_KEEP_APPLICATION_THEME, false); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_HIDE_CARDIO_LOGO, true); // default: false
        // MY_SCAN_REQUEST_CODE is arbitrary and is only used within this activity.
        startActivityForResult(scanIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
            CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
            String number = "";
            for (int i = 0; i < scanResult.cardNumber.length(); i ++){
                if (i > 3){
                    if (i % 4 == 0){
                        number = number + scanResult.cardNumber.substring(i - 4, i) + getString(R.string.blank);
                    }else if (i == scanResult.cardNumber.length() - 1){
                        number = number + scanResult.cardNumber.substring(scanResult.cardNumber.length() - 4,scanResult.cardNumber.length());
                    }
                }
            }
            String year = scanResult.expiryYear+"";
            if (year.length() == 2){
                year = year.substring(0,2);
            }else if (year.length() == 4){
                year = year.substring(2,year.length());
            }
            skipEnter(number,scanResult.expiryMonth+""+year,
                    scanResult.cvv,scanResult.cardholderName);
        } else {
            LogUtils.i(TAG, "onActivityResult: Scan was canceled.");
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
