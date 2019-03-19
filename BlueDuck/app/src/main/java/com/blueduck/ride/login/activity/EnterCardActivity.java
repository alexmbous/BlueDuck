package com.blueduck.ride.login.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.blueduck.ride.R;
import com.blueduck.ride.base.BaseActivity;
import com.blueduck.ride.login.service.LoginService;
import com.blueduck.ride.utils.BroadCastValues;
import com.blueduck.ride.utils.CommonSharedValues;
import com.blueduck.ride.utils.CommonUtils;
import com.blueduck.ride.utils.DateDialog;
import com.blueduck.ride.utils.LogUtils;
import com.blueduck.ride.utils.RequestCallBack;
import com.blueduck.ride.utils.RequestDialog;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.exception.AuthenticationException;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import java.util.Calendar;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

public class EnterCardActivity extends BaseActivity implements RequestCallBack,TextWatcher {

    private static final String TAG = "EnterCardActivity";

    private LinearLayout titleRightLayout;
    private ImageView titleRightImg;
    private EditText nameEt,numberEt,dateEt,cvvEt;
    private Button addCardBtn;

    private String name,number,date,cvv;
    private LoginService loginService;
    private FinishBroad finishBroad;

    private Calendar ca;
    private int mYear,mMonth,mDay,maxDay;
    private int tag = 0;//1：注册跳转，2：主界面跳转，3：卡列表跳转

    @Override
    protected int setLayoutViewId() {
        return R.layout.enter_card_activity;
    }

    @Override
    protected void init() {
        loginService = new LoginService(this,this,TAG);
        tag = getIntent().getIntExtra("tag",0);
        name = getIntent().getStringExtra("name");
        number = getIntent().getStringExtra("number");
        date = getIntent().getStringExtra("date");
        cvv = getIntent().getStringExtra("cvv");
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
        baseTitleText.setText(getString(R.string.enter_card));
        titleRightLayout = (LinearLayout) findViewById(R.id.title_right_layout);
        titleRightLayout.setVisibility(View.VISIBLE);
        titleRightLayout.setOnClickListener(this);
        titleRightImg = (ImageView) findViewById(R.id.title_right_image);
        titleRightImg.setImageResource(R.drawable.camera);
        nameEt = (EditText) findViewById(R.id.card_name_edit);
        numberEt = (EditText) findViewById(R.id.card_number_edit);
        dateEt = (EditText) findViewById(R.id.date_edit);
        dateEt.setOnClickListener(this);
        cvvEt = (EditText) findViewById(R.id.cvv_edit);
        addCardBtn = (Button) findViewById(R.id.add_card_btn);
        addCardBtn.setOnClickListener(this);
        addCardBtn.setEnabled(false);
        numberEt.addTextChangedListener(this);
        MyTextWatcher textWatcher = new MyTextWatcher();
        nameEt.addTextChangedListener(textWatcher);
        dateEt.addTextChangedListener(textWatcher);
        cvvEt.addTextChangedListener(textWatcher);

        nameEt.setText(name);
        numberEt.setText(number);
        dateEt.setText(date);
        cvvEt.setText(cvv);
    }

    private class MyTextWatcher implements TextWatcher{

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

    private void cardDateSelection(){
        new DateDialog(this, new DateDialog.ConfirmBtn() {
            @Override
            public void confirm(int year, int month, int day) {
                String ys = String.valueOf(year);
                dateEt.setText((month < 10 ? ("0"+month) : month)+"/"+ys.substring(2,ys.length()));
            }
        },mYear,mMonth,mDay,maxDay,DateDialog.Card_date_selection,0);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.title_right_layout:
                scanCard();
                break;
            case R.id.date_edit:
                cardDateSelection();
                break;
            case R.id.add_card_btn:
                addCard();
                break;
        }
    }

    private void checkData(){
        number = numberEt.getText().toString().trim();
        name = nameEt.getText().toString().trim();
        date = dateEt.getText().toString().trim();
        cvv = cvvEt.getText().toString().trim();
        if (TextUtils.isEmpty(number) || TextUtils.isEmpty(name) ||
                TextUtils.isEmpty(date) || TextUtils.isEmpty(cvv)){
            addCardBtn.setEnabled(false);
            addCardBtn.setBackground(ContextCompat.getDrawable(EnterCardActivity.this,R.drawable.skip_gray_btn_bg));
        }else{
            addCardBtn.setEnabled(true);
            addCardBtn.setBackground(ContextCompat.getDrawable(EnterCardActivity.this,R.drawable.skip_btn_bg));
        }
    }

    private void addCard(){
        checkData();
        RequestDialog.show(this);
        stripeVerification();
    }

    private int getMonthOrYear(String date,int type) {
        String[] tem = date.split("/");
        int d = 0;
        if (type == 1){
            d = Integer.parseInt(tem[0]);
        }else{
            d = 2000 + Integer.parseInt(tem[1]);
        }
        return d;
    }

    /**
     *  验证stripe支付获取tokenID
     *  Verify stripe payment to get tokenID
     **/
    private void stripeVerification() {
        Card card = new Card(number, getMonthOrYear(date,1), getMonthOrYear(date,2), cvv,name,
                null,null,null,
                null,null, null,null);
        if (!card.validateCard()) {
            RequestDialog.dismiss(this);
            CommonUtils.hintDialog(this, getString(R.string.illegal_credit_card));
            return;
        }
        try {
            Stripe stripe = new Stripe(this, CommonSharedValues.STRIPE_LIVE);
            stripe.createToken(card, new TokenCallback() {
                @Override
                public void onError(Exception error) {
                    RequestDialog.dismiss(EnterCardActivity.this);
                    LogUtils.i(TAG,"onError: 获取 stripe token 失败");
                    if (TextUtils.isEmpty(error.getMessage())){
                        CommonUtils.hintDialog(EnterCardActivity.this, getString(R.string.bind_card_error));
                    }else {
                        CommonUtils.hintDialog(EnterCardActivity.this, error.getMessage());
                    }
                }

                @Override
                public void onSuccess(Token token) {
                    RequestDialog.dismiss(EnterCardActivity.this);
                    LogUtils.i(TAG,"onSuccess: 获取 stripe token 成功");
                    LogUtils.i(TAG,"onSuccess: token id=" + token.getId());
                    saveCard(token.getId());
                }
            });
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
    }

    private void saveCard(String tokenId){
        loginService.saveCard(sp.getString(CommonSharedValues.SP_KEY_TOKEN,""),tokenId,1);
    }

    private void handlerSaveCard(){
        if (tag == 1) {
            startActivity(new Intent(this, TermsOfUseActivity.class));
        }else{
            sendBroadcast(new Intent(BroadCastValues.SAVE_CARD_SUCCESS));
            finish();
        }
    }

    @Override
    public void onSuccess(Object o, int flag) {
        if (flag == 1){
            handlerSaveCard();
        }
    }

    @Override
    public void onFail(Throwable t, int flag) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        checkData();
        String str = s.toString();
        if (str.length() > 0) {
            numberEt.removeTextChangedListener(this);
            if (str.length() % 5 == 0){
                StringBuilder sb = new StringBuilder(str);
                sb.insert(str.length() - 1,getString(R.string.blank));
                numberEt.setText(sb.toString());
            }
            if (str.endsWith(getString(R.string.blank))) {
                numberEt.setText(str.substring(0, str.length() - 1));
            }
            numberEt.addTextChangedListener(this);
        }
        numberEt.setSelection(numberEt.getText().length());
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
            numberEt.removeTextChangedListener(this);
            for (int i = 0; i < scanResult.cardNumber.length(); i ++){
                if (i > 3){
                    String str = numberEt.getText().toString();
                    if (i % 4 == 0){
                        numberEt.setText(str + scanResult.cardNumber.substring(i - 4, i) + getString(R.string.blank));
                    }else if (i == scanResult.cardNumber.length() - 1){
                        numberEt.setText(str + scanResult.cardNumber.substring(scanResult.cardNumber.length() - 4,scanResult.cardNumber.length()));
                        numberEt.setSelection(numberEt.getText().length());
                        numberEt.addTextChangedListener(this);
                    }
                }
            }
            if(scanResult.isExpiryValid()){
                int month=scanResult.expiryMonth;
                int year=scanResult.expiryYear;
                dateEt.setText(month+""+year);
            }
            if (scanResult.cvv != null) {
                cvvEt.setText(scanResult.cvv);
            }
            if (scanResult.cardholderName != null) {
                nameEt.setText(scanResult.cardholderName);
            }
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
