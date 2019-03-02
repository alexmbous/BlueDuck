package com.blueduck.ride.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;

import com.blueduck.ride.main.bean.LocationCall;

import java.io.IOException;
import java.util.List;

/**
 * 自动切换网络和gps定位工具类
 * Automatic switching network and GPS positioning tool classes
 */
public class LocationClient {

    private static final String TAG = "LocationClient";

    private static final int CIRCULATE_HANDLER = 0;

    private Context mContext;
    private LocationManager locationManager;
    private MyLocationListener listener;
    private Location currentLocation;
    private LocationCallBack locationCallBack;//定位结果回调 Locate the result callback
    //The minimum interval of the positioning interval passed in is recommended to be more than 3000ms,
    // because the positioning success is around 2s. If it is 0, the positioning will be performed once
    private long interval = 0;//传入的定位间隔 最小间隔建议3000ms以上，因为定位成功在2s左右，如果为0就执行1次定位

    public interface LocationCallBack{
        void locationCall(LocationCall locationCall);
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case CIRCULATE_HANDLER://根据传入的时间戳循环定位 Loop based on the timestamp passed in
                    if (interval > 0){
                        location();
                        startLocationHandler();
                    }else{
                        location();
                    }
                    break;
            }
        }
    };

    public LocationClient(Context context, LocationCallBack locationCallBack, long interval) {
        this.mContext = context;
        this.locationCallBack = locationCallBack;
        this.interval = interval;
        init();
    }

    /**
     * 初始化定位
     * initialization location
     */
    private void init() {
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
    }

    /**
     * 根据时间间隔发动定位
     * Launch positioning at intervals
     */
    private void startLocationHandler(){
        stopLocationHandler();
        handler.sendEmptyMessageDelayed(CIRCULATE_HANDLER,interval);
    }

    /**
     * 移除时间间隔定位
     * Remove interval location
     */
    private void stopLocationHandler(){
        handler.removeMessages(CIRCULATE_HANDLER);
        handler.removeMessages(CIRCULATE_HANDLER);
        handler.removeMessages(CIRCULATE_HANDLER);
    }

    public void startLocation() {
        handler.sendEmptyMessage(CIRCULATE_HANDLER);//初始化时启动定时 Startup timing at initialization
    }

    public void stopLocation(){
        locationManager.removeUpdates(listener);
        stopLocationHandler();
    }

    /**
     * 更新定位
     * Update the location
     */
    private void location(){
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (listener != null){
            locationManager.removeUpdates(listener);
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 0, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, listener);
    }

    /**
     * network与GPS定位回调
     * 同时请求网络和gps定位更新，然后就会同时上报网络和gps的Location 信息。
     * 在没有gps信号的时候，会自动获取网络定位的位置信息，如果有gps信号，
     * 则优先获取gps提供的位置信息.isBetterLocation 根据 时间、准确性、定位方式等判断是否更新当前位置信息
     * 其中isBetterLocation是用来判断哪个location更好的。这个方法来自android官网的，通过location获取的时间，精度等信息进行判断。
     */
    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if (isBetterLocation(location,currentLocation)){
                currentLocation = location;
                getLocationData();
            }else{
                getLocationData();
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }

    /**
     * 获得定位数据
     * Get location data
     */
    private void getLocationData(){
        if (currentLocation != null){
            LocationCall locationCall = null;
            double lat = currentLocation.getLatitude();//纬度
            double lon = currentLocation.getLongitude();//经度
            float accuracy = currentLocation.getAccuracy();//位置的准确性
            String provider = currentLocation.getProvider();//位置提供者
            double altitude = currentLocation.getAltitude();//高度信息
            float bearing = currentLocation.getBearing();//方向角
            float speed = currentLocation.getSpeed();//速度 米/秒
            String myAddress = "";//地址
            Geocoder gc = new Geocoder(mContext);
            List<Address> addresses = null;
            try {
                addresses = gc.getFromLocation(currentLocation.getLatitude(),currentLocation.getLongitude(),1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addresses != null && addresses.size() > 0){
                Address address = addresses.get(0);
                if (!TextUtils.isEmpty(address.getAdminArea())){
                    myAddress += address.getAdminArea();
                }
                if (!TextUtils.isEmpty(address.getSubAdminArea())){
                    myAddress += address.getSubAdminArea();
                }
                if (!TextUtils.isEmpty(address.getFeatureName())){
                    myAddress += address.getFeatureName();
                }
            }
            locationCall = new LocationCall();
            locationCall.setLat(lat);
            locationCall.setLon(lon);
            locationCall.setAccuracy(accuracy);
            locationCall.setProvider(provider);
            locationCall.setAltitude(altitude);
            locationCall.setBearing(bearing);
            locationCall.setSpeed(speed);
            locationCall.setMyAddress(myAddress);
            locationCallBack.locationCall(locationCall);
        }
    }

    private static final int TWO_MINUTES = 1000 * 60 * 2;
    /**
     * Determines whether one Location reading is better than the current
     * Location fix
     *
     * @param location
     *            The new Location that you want to evaluate
     * @param currentBestLocation
     *            The current Location fix, to which you want to compare the new
     *            one
     */
    public boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use
        // the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be
            // worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
                .getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and
        // accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate
                && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

}
