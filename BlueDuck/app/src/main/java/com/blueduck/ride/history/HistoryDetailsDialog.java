package com.blueduck.ride.history;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.blueduck.ride.R;
import com.blueduck.ride.history.bean.HistoryBean;
import com.blueduck.ride.utils.CurrencyUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;

/**
 * 骑行历史记录详情对话框
 * Cycling history details dialog box
 */
public class HistoryDetailsDialog extends Dialog implements View.OnClickListener{

    private TextView titleText,date,distance,scooter,startAddress,endAddress,rideTime,cost;
    private ImageView mapView;
    private Context mContext;
    private HistoryBean historyBean;

    public interface ContactCallBack{
        void contactCall(String number);
    }
    private ContactCallBack callBack;

    public HistoryDetailsDialog(Context context, ContactCallBack callBack, HistoryBean historyBean,int themeResId) {
        super(context, R.style.myDialog);
        this.mContext = context;
        this.callBack = callBack;
        this.historyBean = historyBean;
        show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_details_dialog);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.TOP;
        window.setAttributes(lp);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        initView();
        initData();
    }

    private void initView(){
        findViewById(R.id.history_details_close).setOnClickListener(this);
        titleText = (TextView) findViewById(R.id.history_details_title);
        mapView = (ImageView) findViewById(R.id.history_details_map_view);
        date = (TextView) findViewById(R.id.history_details_date_text);
        distance = (TextView) findViewById(R.id.history_details_distance_text);
        scooter = (TextView) findViewById(R.id.history_details_scooter_text);
        startAddress = (TextView) findViewById(R.id.history_start_address);
        endAddress = (TextView) findViewById(R.id.history_end_address);
        rideTime = (TextView) findViewById(R.id.history_details_ride_time_text);
        cost = (TextView) findViewById(R.id.history_details_cost_text);
        findViewById(R.id.history_details_contact_support_btn).setOnClickListener(this);
    }

    private void initData(){
        titleText.setText(Utils.dateFormat(historyBean.getDate()));
        mapView.post(new Runnable() {
            @Override
            public void run() {
                loadMapView(historyBean,mapView);
            }
        });
        date.setText(Utils.dateFormat(historyBean.getDate()));
        distance.setText(String.format(mContext.getString(R.string.mile), Utils
                .formatValue(convertMetersToMiles(Double.parseDouble(historyBean.getDistance())).toString(),2)));
        scooter.setText(historyBean.getNumber());
        LatLng startLocation = new LatLng(Double.parseDouble(historyBean.getStartLat()), Double.parseDouble(historyBean.getStartLng()));
        LatLng endLocation = new LatLng(Double.parseDouble(historyBean.getEndLat()), Double.parseDouble(historyBean.getEndLng()));
        startAddress.setText(Utils.getAddressFromLatLng(mContext,startLocation));
        endAddress.setText(Utils.getAddressFromLatLng(mContext,endLocation));
        long startTime = Long.parseLong(historyBean.getStartStamp()) * 1000;
        long endTime = Long.parseLong(historyBean.getEndStamp()) * 1000;
        long sumTime = endTime - startTime;
        rideTime.setText(Utils.hourMinuteFormat(sumTime));
        if (historyBean.getAmount() != null) {
            double amount = Double.parseDouble(historyBean.getAmount());
            cost.setText(mContext.getString(R.string.dollar) + CurrencyUtil.convertToTwoDecimalPlaces(amount));
        }else{
            cost.setText(mContext.getString(R.string.dollar) + 0.0);
        }
    }

    private Double convertMetersToMiles(Double meters) {
        return meters*0.000621371192;
    }

    private void loadMapView(HistoryBean historyBean,ImageView mapView){
        float screenScale = 2;
        if(mContext.getResources().getDisplayMetrics().density > 2) screenScale = 4;
        float screenHeight = pxToDp(mapView.getHeight());
        float screenWidth =  pxToDp(mapView.getWidth());
        Uri google = Utils.getGoogleStaticMapURL(
                (int)screenWidth,
                (int)screenHeight,
                (int)screenScale,
                historyBean.getOrbit(),
                Double.parseDouble(historyBean.getStartLat()),
                Double.parseDouble(historyBean.getStartLng()),
                Double.parseDouble(historyBean.getEndLat()),
                Double.parseDouble(historyBean.getEndLng())
        );
        Glide.with(mContext.getApplicationContext())
                .load(google)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mapView);
    }

    public int pxToDp(int px) {
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.history_details_close:
                dismiss();
                break;
            case R.id.history_details_contact_support_btn:
                callBack.contactCall(historyBean.getNumber());
                dismiss();
                break;
        }
    }
}
