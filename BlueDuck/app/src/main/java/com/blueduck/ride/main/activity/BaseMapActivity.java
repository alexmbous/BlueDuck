package com.blueduck.ride.main.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.blueduck.ride.R;
import com.blueduck.ride.main.bean.BoundaryArea;
import com.blueduck.ride.main.bean.LocationCall;
import com.blueduck.ride.main.bean.MyItem;
import com.blueduck.ride.main.bean.StopArea;
import com.blueduck.ride.main.bean.StopAreaBean;
import com.blueduck.ride.utils.BroadCastValues;
import com.blueduck.ride.utils.CommonSharedValues;
import com.blueduck.ride.utils.CommonUtils;
import com.blueduck.ride.utils.LocationClient;
import com.blueduck.ride.utils.LogUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.omni.ble.library.activity.BaseScooterServiceActivity;

import java.util.ArrayList;
import java.util.List;

import static com.google.maps.android.PolyUtil.containsLocation;

public class BaseMapActivity extends BaseScooterServiceActivity implements DrawerLayout.DrawerListener,
        OnMapReadyCallback,LocationClient.LocationCallBack,GoogleMap.OnInfoWindowClickListener,LocationSource,
        GoogleMap.OnMarkerClickListener,GoogleMap.OnCameraIdleListener {

    private static final String TAG = "BaseMapActivity";

    protected SharedPreferences sp;//保存本地数据的全局变量 Save global variables for local data
    protected SharedPreferences shared;//保存本地数据的全局变量 Save global variables for local data
    protected DrawerLayout drawerLayout;//侧滑菜单控件(google官方提供) Sliding menu control (google official)
    protected boolean isOpenMenu;//是否打开侧滑菜单 Whether to open the sliding menu

    private MapView mapView;
    protected GoogleMap mMap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//将状态栏设置为透明(android4.4以上才有效)

        sp = getSharedPreferences(CommonSharedValues.SP_NAME, MODE_PRIVATE);
        shared = getSharedPreferences(CommonSharedValues.SAVE_LOGIN, MODE_PRIVATE);
        drawerLayout = (DrawerLayout) findViewById(R.id.sliding_menu_view);//侧滑菜单控件 Sliding menu control
        drawerLayout.addDrawerListener(this);
        mapView = (MapView) findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        initStatusBarView();
        //Clear the latitude and longitude of the last saved update bike path during initialization (to prevent the App from uploading or
        // the last latitude and longitude in the ride, resulting in the crossover without crossing the boundary)
        //初始化时清除上次保存更新单车路径的经纬度(以防骑行中划掉App上传的还是上次的经纬度，导致该越界的没越界)
        saveUpdateLatLng(curLat,curLng,true);
    }

    /**
     * 初始化侧滑菜单与主界面的状态栏(显示电量那栏)
     * Initializes the sideslip menu and the status bar of the main interface (the bar displaying power)
     */
    private void initStatusBarView() {
        View mainStatus = (View) findViewById(R.id.main_status_bar_view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //如果当前版本号大于android4.4
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, CommonUtils.getStatusBarHeight(this));
            mainStatus.setLayoutParams(params);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if(null != mLocationClient){
            mLocationClient.stopLocation();
            mLocationClient = null;
        }
    }

    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 6;
    protected LocationClient mLocationClient;
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mLocationClient = new LocationClient(this,this,3000);
        getLocationPermission();
        LogUtils.i(TAG,"onMapReady:地图初始化成功");
        mMap.setOnCameraIdleListener(this);//地图移动监听 Map mobile monitoring
        mMap.setOnMarkerClickListener(this);//地图标记点击监听 Map marker click listener
        LatLng sydney = new LatLng(29.425950, -98.486147);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, DEFAULT_ZOOM));
        mMap.setLocationSource(this);
        mMap.setOnInfoWindowClickListener(this);//弹出信息窗口监听 Pop-up message window monitoring
    }

    /**
     * Animation moves to the center of the map
     * 动画移动到地图中心
     */
    protected void setCenter(){
        if(mMap!=null) {
            LatLng sydney = new LatLng(curLat, curLng);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, DEFAULT_ZOOM));
        }
    }

    /**保存更新单车路径经纬度到本地
     * Save updated bike path latitude and longitude to local
     * **/
    protected void saveUpdateLatLng(double lat,double lng,boolean isClear){
        SharedPreferences.Editor editor = shared.edit();
        if (isClear){
            editor.putString(CommonSharedValues.UPDATE_LAT, "");
            editor.putString(CommonSharedValues.UPDATE_LNG, "");
        }else {
            editor.putString(CommonSharedValues.UPDATE_LAT, lat + "");
            editor.putString(CommonSharedValues.UPDATE_LNG, lng + "");
        }
        editor.apply();
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            updateLocationUI();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateLocationUI();
                }
        }
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);//启动定位 Start positioning
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setMyLocationButtonEnabled(false);//将谷歌定位按钮隐藏 Hide the Google navigation button
        initLocation();
    }

    private void initLocation(){
        if (mMap == null){
            return;
        }
        mLocationClient.startLocation();//启动定位 Start position
    }

    protected double curLat;
    protected double curLng;
    protected String curAddress;
    private boolean isFirstLocation = true;
    @Override
    public void locationCall(LocationCall locationCall) {
        if (locationCall != null){
            float accuracy = locationCall.getAccuracy();
            //Positioning value is obtained when the accuracy is less than or equal to 10.
            if (accuracy <= 10){//精确度小于等于10的情况下才取定位值
                curLat = locationCall.getLat();
                curLng = locationCall.getLon();
                curAddress = locationCall.getMyAddress();
                //Save updated bike path latitude and longitude to local
                saveUpdateLatLng(curLat,curLng,false);//保存更新单车路径经纬度到本地
            }
            LogUtils.i(TAG, "onLocationChanged: 定位精确度="+accuracy);
            if(isFirstLocation){
                isFirstLocation = false;
                curLat = locationCall.getLat();
                curLng = locationCall.getLon();
                curAddress = locationCall.getMyAddress();
                setCenter();
                LogUtils.i(TAG, "onLocationChanged: 移动到中心位置");
                Intent intent = new Intent();
                intent.setAction(BroadCastValues.MAP_INIT_GET_BIKE);
                sendBroadcast(intent);
            }
            LogUtils.i(TAG, "onLocationChanged: lat=" +curLat);
            LogUtils.i(TAG, "onLocationChanged: lng=" +curLng);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(CommonSharedValues.SP_FEEDBACK_LAT,curLat+"");
            editor.putString(CommonSharedValues.SP_FEEDBACK_LNG,curLng+"");
            editor.apply();

            Location location = new Location("LocationProvider");
            location.setLatitude(locationCall.getLat());
            location.setLongitude(locationCall.getLon());
            location.setAccuracy(locationCall.getAccuracy());
            if (mListener != null) {
                mListener.onLocationChanged(location);
            }
        }else{
            LogUtils.i(TAG, "locationCall: ===  定位异常  Location Error");
        }
    }

    protected List<StopAreaBean> redList = new ArrayList<>();//红包单车集合 Red envelope bicycle collection
    protected List<StopArea> areaList = new ArrayList<>();//区域总实体 Regional general entity
    protected List<MyItem> markerList = new ArrayList<>();//保存滑板车集合 Save the scooter collection
    /**
     * 保存已存在的滑板车编号 Save the existing scooter number
     * 目的为防止每次地图移动都重绘单车图标，已有就不再重绘
     * The object is to prevent the bike icon from being redrawn every time the map moves
     */
    protected List<String> numbers = new ArrayList<>();

    /**
     * 判断滑板车是否存在
     * Determine whether the scooter exists
     * @param number
     * @return
     */
    protected boolean isExistScooter(String number){
        boolean exist = false;
        for (int i = 0; i < numbers.size(); i ++){
            if (number.equals(numbers.get(i))){
                exist = true;
                break;
            }
        }
        return exist;
    }


    /**添加红包与普通单车区域集合
     * Add red envelopes and common cycling area collections
     * **/
    protected void addAreaList(List<StopAreaBean> list,boolean isRed){
        if (areaList.size() > 0 && isRed){
            List<StopAreaBean> allList = areaList.get(0).getList();
            for (int i = 0; i < allList.size(); i ++){
                String allDetails = allList.get(i).getDetail();
                for (int j = 0; j < list.size(); j ++){
                    String redDetails = list.get(j).getDetail();
                    if (allDetails.equals(redDetails)){
                        allList.get(i).setRed(true);
                    }
                }
            }
        }else {
            StopArea stopArea = new StopArea();
            stopArea.setList(list);
            stopArea.setRedBike(isRed);
            areaList.add(stopArea);
        }
    }

    /**
     * Save the existing red envelope bicycle and ordinary bicycle parking area.
     * The purpose is to prevent the area from being redrawed every time the map moves.
     * 保存已存在的红包单车与普通单车停车区域目的为防止每次地图移动都重绘区域，已有就不再重绘
     */
    protected List<StopAreaBean> existAreaList = new ArrayList<>();
    protected String boundaryAreaDetail = "";

    /**
     * Determine if the area exists
     * 判断区域是否存在
     * a string parsed into a latitude and longitude set
     * @param detail 经纬度集合解析成的字符串
     * @return
     */
    private boolean isExistArea(String detail){
        boolean exist = false;
        for (int i = 0; i < existAreaList.size(); i ++){
            StopAreaBean stopAreaBean = existAreaList.get(i);
            String dt = stopAreaBean.getDetail();
            if (detail.equals(dt)){
                exist = true;
                break;
            }
        }
        return exist;
    }

    /**
     * Call this method to draw a red envelope bicycle and a normal bicycle parking area on the map.
     * 调用此方法实现在地图上画红包单车与普通单车停车区域**/
    protected void setMapStopArea(List<StopArea> areaList,BoundaryArea boundaryArea){
        if (mMap == null)return;
        if (areaList.size() > 0){
            List<StopAreaBean> list = areaList.get(0).getList();
            for (int i = 0; i < list.size(); i ++){
                StopAreaBean bean = list.get(i);
                //If the area does not exist, draw it. The purpose is to prevent the area from being redrawn every time the map moves.
                if (!isExistArea(bean.getDetail())) {//如果区域不存在就画，目的为防止每次地图移动都重绘区域，已有就不再重绘(客户需求)
                    List<LatLng> latLngs = bean.getLatLngs();
                    String type = bean.getType();
                    if (bean.isRed()) {//红包停车区域 Red envelope parking area
                        drawLine(latLngs, 2);
                    /*mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.red_area_img))
                            .position(new LatLng(Double.parseDouble(bean.getLat()), Double.parseDouble(bean.getLng())))
                            .title(getString(R.string.map_lucky_window_info)));*/
                    } else {
                        if (type.equals("1")) {//推荐停车区域 Recommended parking area
                            drawLine(latLngs, 1);
                        /*mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.area_img))
                                .position(new LatLng(Double.parseDouble(bean.getLat()), Double.parseDouble(bean.getLng())))
                                .title(getString(R.string.map_recommended_window_info)));*/
                        } else if (type.equals("2")) {//禁止停车区域 No parking area
                            drawLine(latLngs, 4);
                        /*mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.forbid_area_img))
                                .position(new LatLng(Double.parseDouble(bean.getLat()), Double.parseDouble(bean.getLng())))
                                .title(getString(R.string.map_forbid_window_info)));*/
                        } else if (type.equals("3")) {//强制停车区域 Forced parking area
                            drawLine(latLngs, 5);
                        /*mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.constraint_area_img))
                                .position(new LatLng(Double.parseDouble(bean.getLat()), Double.parseDouble(bean.getLng())))
                                .title(getString(R.string.map_forced_window_info)));*/
                        }
                    }
                }
            }
            existAreaList = list;
            setBoundaryArea(boundaryArea);
        }
    }

    private synchronized void drawLine(List<LatLng> latLngs,int areaType){
        PolygonOptions rectOptions = new PolygonOptions();
        for (int i = 0; i < latLngs.size(); i ++){
            rectOptions.add(new LatLng(latLngs.get(i).latitude,latLngs.get(i).longitude));
        }
        rectOptions.strokeWidth(3);
        if (areaType == 1){//普通单车停车区域 Ordinary bicycle parking area
            rectOptions.strokeColor(ContextCompat.getColor(this, R.color.main_colors));
            rectOptions.fillColor(ContextCompat.getColor(this, R.color.stop_area_color));
        }else if (areaType == 2){//红包单车停车区域 Red envelope bicycle parking area
            rectOptions.strokeColor(ContextCompat.getColor(this, R.color.red));
            rectOptions.fillColor(ContextCompat.getColor(this, R.color.red_stop_area_color));
        }else if (areaType == 3){//单车越界区域 Bicycle crossing area
            rectOptions.strokeWidth(5);
            rectOptions.strokeColor(ContextCompat.getColor(this, R.color.main_colors));
        }else if (areaType == 4){//单车禁停区域 Bicycle prohibited area
            rectOptions.strokeColor(ContextCompat.getColor(this, R.color.red));
            rectOptions.fillColor(ContextCompat.getColor(this, R.color.red_stop_area_color));
        }else if (areaType == 5){//单车强制停车区域 Bicycle forced parking area
            rectOptions.strokeColor(ContextCompat.getColor(this, R.color.main_colors));
            rectOptions.fillColor(ContextCompat.getColor(this, R.color.stop_area_color));
        }
        if (mMap != null) {
            mMap.addPolygon(rectOptions);
        }
    }

    /**
     * Call this method to draw a riding track on the map (only in riding)
     * 调用此方法在地图上画骑行轨道(仅骑行中)
     * @param orbit
     */
    protected boolean isDrawStartImg = false;
    protected Polyline polyline = null;
    protected void drawRidingLine(List<LatLng> list){
        if (mMap == null)return;
        if (list != null && list.size() > 0){
            if (!isDrawStartImg) {
                mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.scooter_start))
                        .position(list.get(0)));
                isDrawStartImg = true;
            }
            PolylineOptions options = new PolylineOptions();
            options.addAll(list).color(ContextCompat.getColor(this,R.color.main_colors)).width(8f);
            polyline = mMap.addPolyline(options);
        }
    }

    /**
     * Call this method to draw a border region on the map
     * 调用此方法实现在地图上画边界区域**/
    protected void setBoundaryArea(BoundaryArea boundaryArea){
        if (boundaryArea == null) return;
        String boundaryAreaStr = boundaryArea.getArea_detail();
        if (!TextUtils.isEmpty(boundaryAreaStr)) {
            if (!TextUtils.isEmpty(boundaryAreaDetail) && boundaryAreaStr.equals(boundaryAreaDetail))return;
            List<LatLng> list = PolyUtil.decode(boundaryAreaStr);
            List<LatLng> latLngs = new ArrayList<>();
            for (LatLng latLng : list){
                latLngs.add(latLng);
            }
            drawLine(latLngs,3);
        }
        boundaryAreaDetail = boundaryAreaStr;
    }


    /**
     * Verify cross-border logic
     * 验证越界逻辑
     * @param forcedBorderDistance 强制停车区允许误差值 Forced parking zone allowable error value
     * @param cityBorderDistance 城市边界允许误差值 Urban boundary allowable error value
     * @return
     */
    private String areaFlag = "";//强制停车区域越界标记 Forced parking area out of bounds mark
    protected String outAreaStatus(String forcedBorderDistance,String cityBorderDistance){
        areaFlag = "";
        for (int i = 0; i < existAreaList.size(); i ++){
            StopAreaBean bean = existAreaList.get(i);
            String type = bean.getType();
            String detail = bean.getDetail();
            if ("2".equals(type) && isArea(detail,"")){//禁停区域 Banned area
                return "2";
            }else if ("3".equals(type)){//强制停车区域 Forced parking area
                if (isArea(detail,forcedBorderDistance)){
                    return "0";
                }else{
                    areaFlag = "1";
                }
            }
        }
        if (!TextUtils.isEmpty(areaFlag)){
            return areaFlag;
        }
        if (!TextUtils.isEmpty(boundaryAreaDetail)){//是否有城市边界区域 Is there a city boundary area?
            if (isArea(boundaryAreaDetail,cityBorderDistance)){
                return "0";
            }else{
                return "1";
            }
        }
        //None of the above areas directly return "0"
        return "0";//以上区域都不存在直接返回“0”
    }

    /**
     * Verify that the current latitude and longitude is within the specified area
     * 验证当前经纬度是否在指定区域内
     * @param areaDetail
     * @return
     */
    private boolean isArea(String areaDetail,String scopeValue){
        List<LatLng> list = PolyUtil.decode(areaDetail);
        if (!TextUtils.isEmpty(scopeValue)){
            double sv = Double.parseDouble(scopeValue);
            for (LatLng latLng : list){
                //Calculate the distance between two points
                double distance = SphericalUtil.computeDistanceBetween(new LatLng(curLat, curLng), latLng);//计算两点之间的距离
                if (distance <= sv){
                    return true;
                }
            }
        }
        return containsLocation(new LatLng(curLat, curLng), list, true);
    }


    /***********************************   侧滑菜单回调  Skid menu callback ***********************************/

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(@NonNull View drawerView) {
        isOpenMenu = true;
        LogUtils.i(TAG,"onDrawerOpened: 侧滑菜单打开了");
    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) {
        isOpenMenu = false;
        LogUtils.i(TAG,"onDrawerClosed: 侧滑菜单关闭了");
    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    private LocationSource.OnLocationChangedListener mListener;
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
        }
        mLocationClient = null;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onCameraIdle() {
        CameraPosition position = mMap.getCameraPosition();
        LogUtils.i(TAG,"-------------onCameraChangeFinish:  地图移动结束");
        Intent intent = new Intent();
        //Map mobile end to send latitude and longitude broadcast
        intent.setAction(BroadCastValues.MAP_MOVE_END);//地图移动结束发送经纬度广播
        intent.putExtra("Latitude",position.target.latitude);
        intent.putExtra("Longitude",position.target.longitude);
        sendBroadcast(intent);
        LogUtils.i(TAG,"-------------onCameraChangeFinish:  发了广播");
    }
}
