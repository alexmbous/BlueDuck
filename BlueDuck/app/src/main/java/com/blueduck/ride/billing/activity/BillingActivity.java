package com.blueduck.ride.billing.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.blueduck.ride.R;
import com.blueduck.ride.base.BaseActivity;
import com.blueduck.ride.billing.adapter.CardAdapter;
import com.blueduck.ride.billing.bean.CardBean;
import com.blueduck.ride.billing.bean.CardListBean;
import com.blueduck.ride.billing.service.BillingService;
import com.blueduck.ride.login.activity.EnterCardActivity;
import com.blueduck.ride.utils.BroadCastValues;
import com.blueduck.ride.utils.CardSetDialog;
import com.blueduck.ride.utils.CommonSharedValues;
import com.blueduck.ride.utils.CommonUtils;
import com.blueduck.ride.utils.LogUtils;
import com.blueduck.ride.utils.RequestCallBack;

import java.util.ArrayList;
import java.util.List;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

public class BillingActivity extends BaseActivity implements RequestCallBack,CardSetDialog.CardSetCallBack {

    private static final String TAG = "BillingActivity";
    public static final String CARD_ITEM_CLICK = "card_item_click";
    private ImageView titleLeftImg;
    private TextView titleText;
    private LinearLayout billingBg;
    private RelativeLayout noPaymentMethodLayout;
    private ScrollView havePaymentMethodLayout;
    private RecyclerView recyclerView;

    private BillingService billingService;
    private CardBean card;
    private CardAdapter cardAdapter;
    private ItemClickBroad itemClickBroad;
    private int cardPosition;

    @Override
    protected int setLayoutViewId() {
        return R.layout.billing_activity;
    }

    @Override
    protected void init() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//将状态栏设置为透明(android4.4以上才有效)
        billingService = new BillingService(this,this,TAG);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CARD_ITEM_CLICK);
        intentFilter.addAction(BroadCastValues.SAVE_CARD_SUCCESS);
        itemClickBroad = new ItemClickBroad();
        registerReceiver(itemClickBroad,intentFilter);
    }

    @Override
    protected void initView() {
        View statusBar = (View) findViewById(R.id.status_bar_view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //如果当前版本号大于android4.4
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, CommonUtils.getStatusBarHeight(this));
            statusBar.setLayoutParams(params);
        }
        findViewById(R.id.billing_left_layout).setOnClickListener(this);
        titleLeftImg = (ImageView) findViewById(R.id.billing_left_image);
        titleText = (TextView) findViewById(R.id.billing_title_text);
        billingBg = (LinearLayout) findViewById(R.id.billing_bg_layout);
        noPaymentMethodLayout = (RelativeLayout) findViewById(R.id.no_payment_method_layout);
        havePaymentMethodLayout = (ScrollView) findViewById(R.id.have_payment_method_layout);
        findViewById(R.id.billing_enter_card_layout).setOnClickListener(this);
        //findViewById(R.id.billing_scan_card_layout).setOnClickListener(this);
        findViewById(R.id.card_list_enter_card_layout).setOnClickListener(this);
        //findViewById(R.id.card_list_scan_card_layout).setOnClickListener(this);
        recyclerView = (RecyclerView) findViewById(R.id.card_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        getCardList();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.billing_left_layout:
                finish();
                break;
            case R.id.billing_enter_card_layout:
                skipEnter("","","","");
                break;
//            case R.id.billing_scan_card_layout:
//                scanCard();
//                break;
            case R.id.card_list_enter_card_layout:
                skipEnter("","","","");
                break;
//            case R.id.card_list_scan_card_layout:
//                scanCard();
//                break;
        }
    }

    /**
     * set payment card data
     */
    private void setAdapter(){
        if (card.getPaymentList() == null)return;
        if (card.getPaymentList().size() > 0) {
            handlerHaveData();
            List<CardListBean> listBean = new ArrayList<>();
            for (int i = 0; i < card.getPaymentList().size(); i++) {
                if (card.getDefaultPaymentId().equals(card.getPaymentList().get(i).getId())) {
                    listBean.add(0, card.getPaymentList().get(i));
                } else {
                    listBean.add(card.getPaymentList().get(i));
                }
            }
            card.setPaymentList(listBean);
            if (cardAdapter == null) {
                cardAdapter = new CardAdapter(this, card);
                recyclerView.setAdapter(cardAdapter);
            } else {
                cardAdapter.setCardBean(card);
                cardAdapter.notifyDataSetChanged();
            }
        }else{
            handlerNoData();
        }
    }

    private void getCardList(){
        billingService.getCardList(sp.getString(CommonSharedValues.SP_KEY_TOKEN,""),1);
    }

    private void setDefaultOrDeleteCard(int type){
        String cardId = card.getPaymentList().get(cardPosition).getId();
        billingService.setDefaultAndDeleteCard(sp.getString(CommonSharedValues.SP_KEY_TOKEN,""),cardId,type,2);
    }

    private void handlerNoData(){
        titleLeftImg.setImageResource(R.drawable.menu_white);
        titleText.setTextColor(ContextCompat.getColor(this,R.color.white));
        billingBg.setBackgroundColor(ContextCompat.getColor(this,R.color.main_colors));
        noPaymentMethodLayout.setVisibility(View.VISIBLE);
        havePaymentMethodLayout.setVisibility(View.GONE);
    }

    private void handlerHaveData(){
        titleLeftImg.setImageResource(R.drawable.menu);
        titleText.setTextColor(ContextCompat.getColor(this,R.color.black));
        billingBg.setBackgroundColor(ContextCompat.getColor(this,R.color.white));
        noPaymentMethodLayout.setVisibility(View.GONE);
        havePaymentMethodLayout.setVisibility(View.VISIBLE);
    }

    private void handlerCardList(){
        if (card != null){
            setAdapter();
        }
    }

    @Override
    public void onSuccess(Object o, int flag) {
        if (flag == 1){
            card = (CardBean) o;
            handlerCardList();
        }else if (flag == 2){
            getCardList();
        }
    }

    @Override
    public void onFail(Throwable t, int flag) {
        if (flag == 1){
            handlerNoData();
        }
    }

    @Override
    public void cardSetIndex(int index) {
        if (index == 0){//set default card
            setDefaultOrDeleteCard(index);
        }else if (index == 1){//delete card
            setDefaultOrDeleteCard(index);
        }
    }

    private void showCardSetDialog(){
        boolean isDefault = card.getDefaultPaymentId().equals(card.getPaymentList().get(cardPosition).getId());
        new CardSetDialog(this,this,isDefault,0);
    }

    private class ItemClickBroad extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if (CARD_ITEM_CLICK.equals(intent.getAction())){
                cardPosition = intent.getIntExtra("position",-1);
                showCardSetDialog();
            }else if (BroadCastValues.SAVE_CARD_SUCCESS.equals(intent.getAction())){//添加卡成功
                getCardList();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(itemClickBroad);
    }

    private void skipEnter(String number,String date,String cvv,String name){
        Intent intent = new Intent(this,EnterCardActivity.class);
        intent.putExtra("tag",3);
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
            if (scanResult.isExpiryValid()) {
                skipEnter(number, scanResult.expiryMonth + "" + year,
                        scanResult.cvv, scanResult.cardholderName);
            }else{
                skipEnter(number, "", scanResult.cvv, scanResult.cardholderName);
            }
        } else {
            LogUtils.i(TAG, "onActivityResult: Scan was canceled.");
        }
    }
}
