package com.blueduck.ride.main.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blueduck.ride.R;
import com.blueduck.ride.base.MyApplication;
import com.blueduck.ride.billing.activity.BillingActivity;
import com.blueduck.ride.history.activity.HistoryActivity;
import com.blueduck.ride.login.activity.EnterCardActivity;
import com.blueduck.ride.login.activity.PageActivity;
import com.blueduck.ride.main.adapter.MenuListAdapter;
import com.blueduck.ride.main.bean.ApkUpdateBean;
import com.blueduck.ride.main.bean.AreaResult;
import com.blueduck.ride.main.bean.BoundaryArea;
import com.blueduck.ride.main.bean.EndBike;
import com.blueduck.ride.main.bean.FinishBikeState;
import com.blueduck.ride.main.bean.LocationCall;
import com.blueduck.ride.main.bean.MenuListBean;
import com.blueduck.ride.main.bean.MyItem;
import com.blueduck.ride.main.bean.NearbyBike;
import com.blueduck.ride.main.bean.ReturnCloseBean;
import com.blueduck.ride.main.bean.RunBikeState;
import com.blueduck.ride.main.bean.StopAreaBean;
import com.blueduck.ride.main.bean.UserInfoBean;
import com.blueduck.ride.main.service.MainService;
import com.blueduck.ride.personal.activity.MyAccountActivity;
import com.blueduck.ride.qrcode.ScannerActivity;
import com.blueduck.ride.report.activity.ReportActivity;
import com.blueduck.ride.utils.BindCardDialog;
import com.blueduck.ride.utils.BorderDialog;
import com.blueduck.ride.utils.BroadCastValues;
import com.blueduck.ride.utils.CommonSharedValues;
import com.blueduck.ride.utils.CommonUtils;
import com.blueduck.ride.utils.CurrencyUtil;
import com.blueduck.ride.utils.DownloadService;
import com.blueduck.ride.utils.EndRideDialog;
import com.blueduck.ride.utils.GlideCircleTransform;
import com.blueduck.ride.utils.LogUtils;
import com.blueduck.ride.utils.ReportSuccessDialog;
import com.blueduck.ride.utils.RequestCallBack;
import com.blueduck.ride.utils.RequestDialog;
import com.blueduck.ride.utils.VersionHintDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.omni.ble.library.service.ScooterService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseMapActivity implements View.OnClickListener,RequestCallBack,
        AdapterView.OnItemClickListener,RatingBar.OnRatingBarChangeListener,BindCardDialog.AddCardBtn {

    private static final String TAG = "MainActivity";

    private MyApplication myApplication;//全局的类，随App的状态
    private MainService mainService;
    private MainBroadcast mainBroadcast;
    //private BluetoothAdapter mBluetoothAdapter;
    //private BluetoothManager bluetoothManager;
    private ScooterService scooterService = null;//滑板车服务类 Scooter service class

    private ImageView menuHead;
    private TextView userName;
    private ListView menuList;
    private LinearLayout scanLayout,rateRideLayout;

    private LinearLayout scooterInfoLayout;
    private TextView infoNumber,infoBattery,infoPrice;

    private LinearLayout useLayout;
    private TextView useNumber,useBattery;

    private LinearLayout useEndLayout;
    private TextView useEndNumber;
    private RatingBar ratingBar;

    private int menuClickType = -1;//侧滑菜单点击类型 Sliding menu click type
    private static final int MENU_TYPE_IS_MY_ACCOUNT = 0;
    private static final int MENU_TYPE_IS_BILLING = 1;
    private static final int MENU_TYPE_IS_RIDE_HISTORY = 2;
    private static final int MENU_TYPE_IS_HOW_TO_RIDE = 3;
    private static final int MENU_TYPE_IS_SUPPORT = 4;

    private boolean isLogOut = false;//是否退出登录 Whether to log out
    private String forcedBorderDistance;//强制停车区允许误差值 Forced parking zone allowable error value
    private String cityBorderDistance;//城市边界允许误差值 Urban boundary allowable error value
    private int isBindCard = -1;
    private BoundaryArea boundaryArea = null;//边界区域实体 Boundary area entity
    private int bikeState = 0;
    private String closeTimestamp;
    private boolean isWindowFocus = false;
    private boolean isRiding = false;//是否骑行中 Whether riding
    private boolean isGetKey = false;//是否获得key Whether to get the key
    private String rideId;//骑行ID Riding ID
    private String hostId;//自己的骑行Id Your own riding Id
    private String outArea = "0";
    private String lockStatus;
    private String bikeType;//车类型 1：单车 2：滑板车 Car type 1: bicycle 2: scooter
    private String scooterLockingDate;//滑板车手动关锁传的时间戳 Time stamp for the manual lock off of the scooter
    //Whether the draw parking area has been called in the ride (to prevent repeated calls)
    private boolean callUnlocking;//骑行中是否已经调用画停车区域(防止重复调用)
    private boolean isScanUnlock = false;//是否扫码开锁 Whether to scan the code to open the lock

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonUtils.add(this);
        myApplication = (MyApplication) getApplication();
        mainService = new MainService(this,this,TAG);
        initView();
        initMenu();
        initScooterInfoLayout();
        initUseLayout();
        initUseEndLayout();
        initBroadCast();//初始化广播 Initialize broadcast
        registerLocalReceiver();//初始化广播(滑板车) Initialize the broadcast (scooter)
        //initBluetooth();//初始化蓝牙 Initialize Bluetooth
    }

//    private void initBluetooth() {
//        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//        mBluetoothAdapter = bluetoothManager.getAdapter();
//    }

//    private boolean isOpenBlue(){
//        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()){//未开启蓝牙 Bluetooth is not turned on
//            return false;
//        }else{//已开启蓝牙 Bluetooth turned on
//            return true;
//        }
//    }

    private void initBroadCast(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadCastValues.LOG_OUT);
        intentFilter.addAction(BroadCastValues.UNLOCKING_SUCCESS);
        intentFilter.addAction(BroadCastValues.MAP_INIT_GET_BIKE);
        intentFilter.addAction(BroadCastValues.MAP_MOVE_END);
        intentFilter.addAction(BroadCastValues.UPDATE_USER_INFO);
        intentFilter.addAction(BroadCastValues.GOOGLE_PUSH_BROADCAST);
        intentFilter.addAction(BroadCastValues.REMOTE_LOGIN_BROADCAST);
        intentFilter.addAction(BroadCastValues.REFRESH_BIKE_USE_INFO);
        intentFilter.addAction(BroadCastValues.RATE_OR_DONT_RATE_SUCCESS);
        intentFilter.addAction(BroadCastValues.REPORT_SUCCESS_BROAD);
        mainBroadcast = new MainBroadcast();
        registerReceiver(mainBroadcast,intentFilter);
    }

    private void initScooterInfoLayout(){
        scooterInfoLayout = (LinearLayout) findViewById(R.id.main_scooter_info_layout);
        infoNumber = (TextView) findViewById(R.id.scooter_info_number);
        infoBattery = (TextView) findViewById(R.id.scooter_info_battery);
        infoPrice = (TextView) findViewById(R.id.scooter_info_price);
        findViewById(R.id.scooter_info_scan_layout).setOnClickListener(this);
        findViewById(R.id.info_report_scooter).setOnClickListener(this);
    }

    private void initUseLayout(){
        useLayout = (LinearLayout) findViewById(R.id.main_use_layout);
        useNumber = (TextView) findViewById(R.id.use_scooter_number);
        useBattery = (TextView) findViewById(R.id.use_scooter_battery);
        findViewById(R.id.use_end_btn).setOnClickListener(this);
    }

    private void initUseEndLayout(){
        useEndLayout = (LinearLayout) findViewById(R.id.main_use_end_layout);
        useEndNumber = (TextView) findViewById(R.id.use_end_scooter_number);
        ratingBar = (RatingBar) findViewById(R.id.rating_bar);
        ratingBar.setOnRatingBarChangeListener(this);
        findViewById(R.id.dont_rate_text).setOnClickListener(this);
    }

    private void initView(){
        findViewById(R.id.main_menu_layout).setOnClickListener(this);
        findViewById(R.id.main_how_to_ride_layout).setOnClickListener(this);
        findViewById(R.id.location_layout).setOnClickListener(this);
        rateRideLayout = (LinearLayout) findViewById(R.id.main_rate_ride_layout);
        scanLayout = (LinearLayout) findViewById(R.id.scan_layout);
        scanLayout.setOnClickListener(this);
    }

    private void initMenu(){
        findViewById(R.id.menu_head_layout).setOnClickListener(this);
        menuHead = (ImageView) findViewById(R.id.menu_head);
        userName = (TextView) findViewById(R.id.menu_user_name);
        menuList = (ListView) findViewById(R.id.menu_list);
        menuList.setOnItemClickListener(this);
        List<MenuListBean> menuLists = new ArrayList<>();
        String[] title = getResources().getStringArray(R.array.menu_list_title_array);
        TypedArray ta = getResources().obtainTypedArray(R.array.menu_list_img_array);
        for (int i = 0; i < ta.length(); i++) {
            MenuListBean menuListBean = new MenuListBean();
            menuListBean.setImageId(ta.getResourceId(i, 0));
            menuListBean.setTitle(title[i]);
            menuLists.add(menuListBean);
        }
        ta.recycle();
        menuList.setAdapter(new MenuListAdapter(this, menuLists));
        String headUrl = sp.getString(CommonSharedValues.SP_KEY_IMAGE_URL, "");
        showHeadImg(headUrl);
        getUserInfo();
    }

    /**
     * Display user avatar
     * 显示用户头像
     * @param headUrl
     */
    private void showHeadImg(String headUrl){
        headUrl = "https://s3.us-east-2.amazonaws.com/blueduck-static/resources/avatar1.jpg";
        if (!TextUtils.isEmpty(headUrl)) {
            menuHead.setVisibility(View.VISIBLE);
            Glide.with(getApplicationContext())
                    .load(headUrl)
                    .transform(new GlideCircleTransform(getApplicationContext()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(menuHead);
            saveSharedValue(CommonSharedValues.SP_KEY_IMAGE_URL,headUrl);
        }else{
            menuHead.setVisibility(View.GONE);
        }
    }

    /**
     * Open or close the skid
     * 打开或关闭侧滑
     */
    private void openMenu() {
        if (isOpenMenu) {
            drawerLayout.closeDrawers();
        } else {
            drawerLayout.openDrawer(Gravity.LEFT);
        }
    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) {
        super.onDrawerClosed(drawerView);
        skipActivity();
    }

    private void skipActivity(){
        if (!isOpenMenu){
            if (menuClickType == MENU_TYPE_IS_MY_ACCOUNT){
                startActivity(new Intent(this,MyAccountActivity.class));
            }else if (menuClickType == MENU_TYPE_IS_BILLING){
                startActivity(new Intent(this,BillingActivity.class));
            }else if (menuClickType == MENU_TYPE_IS_RIDE_HISTORY){
                getLocationPermission(3);
            }else if (menuClickType == MENU_TYPE_IS_HOW_TO_RIDE){
                skipPage();
            }else if (menuClickType == MENU_TYPE_IS_SUPPORT){
                startReport();
            }
            menuClickType = -1;
        }else{
            return;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        menuClickType = position;
        openMenu();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.main_menu_layout://菜单 menu
                openMenu();
                break;
            case R.id.main_how_to_ride_layout://如何骑行 How to ride
                skipPage();
                break;
            case R.id.location_layout://定位 location
                getLocationPermission(1);
                break;
            case R.id.scan_layout://扫描 scan
                getCameraPermission();
                break;
            case R.id.menu_head_layout://菜单头布局 Menu header layout
                menuClickType = MENU_TYPE_IS_MY_ACCOUNT;
                openMenu();
                break;
            case R.id.scooter_info_scan_layout://滑板车信息内的扫描按钮 The scan button in the scooter information
                infoScan();
                break;
            case R.id.info_report_scooter://滑板车信息内的报故障按钮 The trouble button in the scooter information
                infoReport();
                break;
            case R.id.use_end_btn://使用结束按钮 Use the end button
                endRideDialog();
                break;
            case R.id.dont_rate_text://骑行结束不评分 No marks will be given at the end of the ride
                dontRate();
                break;
        }
    }

    private void infoScan(){
        normalSate(0);
        restoreMarker();
        getCameraPermission();
    }

    private void infoReport(){
        normalSate(0);
        restoreMarker();
        getLocationPermission(2);
    }

    private boolean isDontRate = false;
    private void dontRate(){
        isDontRate = true;
        scooterInfoLayout.setVisibility(View.GONE);
        useEndLayout.setVisibility(View.GONE);
        useLayout.setVisibility(View.GONE);
        scanLayout.setVisibility(View.GONE);
        rateRideLayout.setVisibility(View.VISIBLE);
        handler.sendEmptyMessageDelayed(HANDLER_RATE_RIDE,5000);
    }

    private void startHistory(){
        Intent intent = new Intent(this,HistoryActivity.class);
        intent.putExtra("lat",curLat);
        intent.putExtra("lng",curLng);
        startActivity(intent);
    }

    private void startReport(){
        saveSharedValue(CommonSharedValues.SP_FEEDBACK_NUMBER,"");
        getLocationPermission(2);
    }

    private void skipPage(){
        startActivity(new Intent(this,PageActivity.class));
    }

    private void endRideDialog(){
        new EndRideDialog(this, new EndRideDialog.EndRideCallBack() {
            @Override
            public void endRideCall() {
                if (scooterService != null && isGetKey){
                    if ("0".equals(hostId)) {
                        sendScooterCloseCommand();//发送滑板车蓝牙关锁指令 Send scooter Bluetooth lock command
                    }
                }
                scooterLocking();
            }
        },0);
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        if (useEndLayout.getVisibility() == View.VISIBLE){
            Intent intent = new Intent(this,RateActivity.class);
            intent.putExtra("rating",rating);
            intent.putExtra("rideId",rideId);
            startActivity(intent);
        }
    }

    private void restoreMarker(){
        if (clickMarker != null) {
            clickMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.scooter));
        }
    }

    private Marker clickMarker;
    @Override
    public boolean onMarkerClick(Marker marker) {
        if (isBindCard == 1){//有绑卡 Tie card
            restoreMarker();
            clickMarker = marker;
            clickMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.scooter_press));
            MyItem myItem = (MyItem) marker.getTag();
            saveSharedValue(CommonSharedValues.SP_FEEDBACK_NUMBER,myItem.getNumber());
            if (myItem.getReadpack() == 0){//普通滑板车 Ordinary scooter
                infoNumber.setText(myItem.getNumber());
                infoPrice.setText("$" + CurrencyUtil.convertToTwoDecimalPlaces(Double.valueOf(myItem.getPrice())));
                infoBattery.setText(myItem.getPowerPercent()+"%");
            }
            scooterInfoLayout.setVisibility(View.VISIBLE);
            useEndLayout.setVisibility(View.GONE);
            useLayout.setVisibility(View.GONE);
            scanLayout.setVisibility(View.GONE);
            rateRideLayout.setVisibility(View.GONE);
        } else if (isBindCard == 0) {//未绑卡 Unlinked card
            showAddCardDialog();
        } else {//未请求到数据，重新获取 No data requested. Retrieve
            getBikeUseInfo();
        }
        return super.onMarkerClick(marker);
    }

    private BindCardDialog bindCardDialog = null;
    private void showAddCardDialog(){
        if (bindCardDialog == null) {
            bindCardDialog = new BindCardDialog(MainActivity.this, this, 0);
        } else if (!bindCardDialog.isShowing()) {
            bindCardDialog.show();
        }
    }

    @Override
    public void addClick(View view) {
        Intent intent = new Intent(this,EnterCardActivity.class);
        intent.putExtra("tag",2);
        startActivity(intent);
        if (bindCardDialog != null){
            if (bindCardDialog.isShowing()){
                bindCardDialog.dismiss();
            }
            bindCardDialog = null;
        }
    }

    /**
     * Click to scan
     * 点击扫描
     */
    private void clickScan(){
        if (isBindCard == 1) {//有绑卡 Tie card
            skipScan("");
        } else if (isBindCard == 0) {//未绑卡 Unlinked card
            showAddCardDialog();
        } else {//未请求到数据，重新获取 No data requested. Retrieve
            getBikeUseInfo();
        }
    }

    /**
     * Jump scan
     * 跳转扫描
     */
    private void skipScan(String rideUser){
        handler.sendEmptyMessage(HANDLER_DISCONNECT);
        isScanUnlock = true;
        callUnlocking = false;
        Intent intent = new Intent(this, ScannerActivity.class);
        intent.putExtra("curLat", curLat + "");
        intent.putExtra("curLng", curLng + "");
        intent.putExtra("outArea", outArea);
        intent.putExtra("rideUser",rideUser);
        MyItem myItem = (MyItem) clickMarker.getTag();
        if (myItem != null) {
            intent.putExtra("pricePerMinute", "$" + CurrencyUtil.convertToTwoDecimalPlaces(Double.valueOf(myItem.getPrice())));
        }
        startActivity(intent);
    }

    /**
     * Jump Report
     * 跳转报故障
     */
    private void skipIssue(){
        Intent intent = new Intent(this,ReportActivity.class);
        intent.putExtra("lat",curLat);
        intent.putExtra("lng",curLng);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        isScanUnlock = false;
        isWindowFocus = true;
        isMapMoveUpdate = true;//地图移动可以刷新数据 Map move can refresh data
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);//打开侧滑手势 Open the side-slip gesture
        getBikeUseInfo();
    }

    private boolean firstInit = true;
    @Override
    protected void onResume() {
        super.onResume();
        if (!CommonUtils.isOpenLocService(this)){
            CommonUtils.openGPSLocationHint(this);
        }
        if (firstInit) {
            getLocationPermission(1);
            firstInit = false;
        }
        isMoveMapCenter = true;
        if (!isRiding && mLocationClient != null) {//如果单车没有骑行中onResume就启动定位
            //If the bicycle does not ride onResume, it will start positioning.
            mLocationClient.startLocation();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        isWindowFocus = false;
        isMoveMapCenter = false;
        handler.removeMessages(HANDLER_MAP_MOVE_UPDATE);
        handler.removeMessages(HANDLER_LOCATION_MAP_CENTER);
        handler.removeMessages(HANDLER_MAP_RED_AREA);
        handler.removeMessages(HANDLER_IN_THE_LOCK);
        handler.removeMessages(HANDLER_SUE_TIME);
        if (!isRiding && mLocationClient != null) {//如果单车没有骑行中onStop就停止定位
            //Stop positioning if the bicycle does not ride onStop
            mLocationClient.stopLocation();
        }
    }

    /**
     * Detect targeting permissions
     * 检测定位权限
     **/
    private void getLocationPermission(int requestCode){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
        } else {
            if (requestCode == 1) {//定位 Positioning
                setCenter();
            }else if (requestCode == 2){//报故障 Report failure
                skipIssue();
            }else{//骑行列表 Ride History
                startHistory();
            }
        }
    }

    /**
     * Detect camera permissions
     * 检测相机权限
     **/
    private void getCameraPermission(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 4);
        } else {
            clickScan();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case 1://定位 Positioning
            case 2://报故障 Report failure
            case 3://骑行列表 Ride History
                locationPermissionsCallBack(requestCode,grantResults);
                break;
            case 4://相机 camera
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    clickScan();
                }else{
                    CommonUtils.hintDialog(this, getString(R.string.not_camera_permission));
                }
                break;
            case 5://更新版本 update version
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(MainActivity.this, DownloadService.class);
                    intent.putExtra("apkUrl", apkUrl);
                    startService(intent);
//                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="+getPackageName())));
                } else {
                    CommonUtils.hintDialog(this, getString(R.string.refuse_hint));
                }
                break;
        }
    }

    /**
     * Request targeting permission is successful
     * 请求定位权限成功
     * @param requestCode
     * @param grantResults
     */
    private void locationPermissionsCallBack(int requestCode,int[] grantResults){
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if (requestCode == 1) {//定位 Positioning
                setCenter();
            }else if (requestCode == 2){//报故障 Report failure
                skipIssue();
            }else{//骑行列表 Ride History
                startHistory();
            }
        }else{
            CommonUtils.hintDialog(this, getString(R.string.not_location_permission));
        }
    }

    /**
     * Get user information
     * 获得用户信息
     */
    private void getUserInfo(){
        mainService.getUserInfo(sp.getString(CommonSharedValues.SP_KEY_TOKEN,""),false,1);
    }

    /**
     * Get bicycle use information
     * 获得单车使用信息
     */
    private void getBikeUseInfo(){
        mainService.getBikeUseInfo(sp.getString(CommonSharedValues.SP_KEY_TOKEN,""), closeTimestamp,2);
    }

    /**
     * Get a nearby bicycle
     * 获得附近单车
     * @param curLat
     * @param curLng
     * @param targetLat
     * @param targetLng
     */
    private void getBike(double curLat, double curLng, double targetLat, double targetLng){
        String token = sp.getString(CommonSharedValues.SP_KEY_TOKEN,""); //TODO: fixed by Garrett
        mainService.getBike(token, curLat,curLng,targetLat,targetLng,3);
    }

    /**
     * Get a bicycle parking area
     * 获得单车停车区域
     * @param curLat
     * @param curLng
     * @param targetLat
     * @param targetLng
     * @param ids
     */
    private void getStopArea(double curLat, double curLng, double targetLat, double targetLng, final String ids){
        mainService.getStopArea(sp.getString(CommonSharedValues.SP_KEY_TOKEN,""),curLat,curLng,targetLat,targetLng,ids,4);
    }

    /**
     * Bicycle unlocking
     * 单车解锁中
     * @param number
     */
    private void debLocking(String number){
        mainService.debLocking(sp.getString(CommonSharedValues.SP_KEY_TOKEN,""),number,5);
    }

    /**
     * Notify the server to lock
     * 通知服务器关锁
     * @param uid
     * @param runTime
     * @param timestamp
     */
    private void setUnLockClose(int uid,int runTime, long timestamp){
        if (isUnLockClose)return;
        mainService.setUnLockClose(sp.getString(CommonSharedValues.SP_KEY_TOKEN,""),
                sp.getString(CommonSharedValues.SP_KEY_NUMBER,""),uid,
                sp.getString(CommonSharedValues.SP_KEY_UID, ""),runTime,timestamp,
                sp.getString(CommonSharedValues.SP_LOCK_POWER, ""),6);
    }

    /**
     * Scooter network lock
     * 滑板车网络关锁
     */
    private void scooterLocking(){
        mainService.scooterLocking(sp.getString(CommonSharedValues.SP_KEY_TOKEN,""),scooterLockingDate,7);
    }

    /**
     * Upload user ride information
     * 上传用户骑行信息
     */
    private void uploadRideInfo(){
        if (ridePath.size() > 0) {
            mainService.updateRideInfo(sp.getString(CommonSharedValues.SP_KEY_TOKEN,""),
                    rideId,outArea,PolyUtil.encode(ridePath), totalDistance,curLat,curLng,8);
        }
    }

    @Override
    public void onSuccess(Object o, int flag) {
        if (flag == 1){//获得用户信息 Get user information
            UserInfoBean bean = (UserInfoBean) o;
            handlerUserInfo(bean);
        }else if (flag == 2){//获得单车使用信息 Get bicycle use information
            JSONObject jsonObject = (JSONObject) o;
            handlerBikeUseInfo(jsonObject);
        }else if (flag == 3){//获得附近单车 Get a nearby bicycle
            JSONArray jsonArray = (JSONArray) o;
            handlerBike(jsonArray);
        }else if (flag == 4){//获得单车停车区域 Get a bicycle parking area
            AreaResult areaResult = (AreaResult) o;
            handlerAreaResult(areaResult);
        }else if (flag == 5){//单车解锁中 Bicycle unlocking
            JSONObject jsonObject = (JSONObject) o;
            handlerDbLocking(jsonObject);
        }else if (flag == 6){//通知服务器关锁或上传旧数据 Notify the server to lock or upload old data
            ReturnCloseBean bean = (ReturnCloseBean) o;
            handlerUnlockClose(bean);
        }else if (flag == 7){//滑板车网络关锁成功请求单车使用信息结费 The scooter network locks successfully requests the bicycle to use the information fee
            handler.sendEmptyMessageDelayed(HANDLER_NETWORK_LOCK,3000);
            getBikeUseInfo();
        }else if (flag == 8){//上传用户骑行信息 Upload user ride information
            //Instead of doing result processing for the time being,
            // just request it because there is no requirement for the result data to perform certain operations
            // TODO: 2019/1/3 暂时不做结果处理，请求了就行了，因为暂时没有需求要结果数据来执行某些操作
        }
    }

    @Override
    public void onFail(Throwable t, int flag) {

    }

    /**
     * The upload server is locked successfully (the end of the ride)
     * 上传服务器关锁成功(骑行结束)
     **/
    private void updateRidingEnd(Gson gson, JSONObject json) {
        dismissBorderDialog();
        isRiding = false;
        callUnlocking = false;
        isGetKey = false;
        EndBike endBike = gson.fromJson(json.toString(), EndBike.class);
        useEndNumber.setText(endBike.getTradeVo().getBikeUseVo().getNumber());
        LogUtils.i(TAG,"updateRidingEnd ：上传服务器关锁成功(骑行结束)");
        isUnLockClose = false;
        rideId = endBike.getTradeVo().getBikeUseVo().getId();
        uploadRideInfo();//上传骑行信息 Uploading ride information
        scooterInfoLayout.setVisibility(View.GONE);
        useEndLayout.setVisibility(View.VISIBLE);
        useLayout.setVisibility(View.GONE);
        scanLayout.setVisibility(View.GONE);
        rateRideLayout.setVisibility(View.GONE);
        setCenter();
        handler.sendEmptyMessageDelayed(HANDLER_DISCONNECT,2000);
        clearNetworkLock();
        clearMap();
    }

    /**
     * Handling notification server to lock or upload old data
     * 处理通知服务器关锁或上传旧数据
     * @param bean
     */
    private void handlerUnlockClose(ReturnCloseBean bean){
        JSONObject retJson = bean.getJsonObject();
        int uid = bean.getUid();
        try {
            int code = retJson.getInt("code");
            if ("2".equals(bikeType)) {//滑板车 scooter
                if (scooterService != null){
                    if (uid > 0) {//上传旧数据成功 Uploading old data successfully
                        sendClearOldData();//滑板车清除旧数据 Scooter clears old data
                        LogUtils.i(TAG, "handlerUnlockClose : 主界面 滑板车清除旧数据");
                    }else{//通知关锁成功 Notice that the lock was successful
                        sendCloseResponseCommand();//滑板车关锁成功回复锁 The scooter is locked and the lock is successfully restored.
                        LogUtils.i(TAG, "handlerUnlockClose : 主界面 滑板车关锁成功回复锁");
                    }
                }
            }
            if (code == 200){
                Gson gson = new Gson();
                Object object = retJson.get("data");
                if (object != null && !"".equals(object)) {//单人或多人骑行全部关锁结束骑行 Single or multi-person riding all locked and ending cycling
                    JSONObject json = (JSONObject) object;
                    int type = retJson.getInt("type");
                    LogUtils.i(TAG,"---->type=" + type);
                    if (type == 2) {//关锁成功，跳转结束界面 The lock is successful, the jump end interface
                        updateRidingEnd(gson, json);
                        LogUtils.i(TAG,"handlerUnlockClose : ===============关锁成功，跳转结束界面");
                    }
                }//否则就是多人骑行其中自己关锁 Otherwise, it’s a multiplayer ride where you lock yourself.
            }else if (code == 202){
                // TODO: 2018/12/12 是上传旧数据造成的，服务器记录不见了才会返回202,暂不做处理
            }else if (code == 30012){//骑行关锁后越界了 Crossed out after riding the lock
                lockStatus = "0";
                outArea = "1";
                scanLayout.setVisibility(View.VISIBLE);
                showBorderDialog(getString(R.string.border_description_text));
            }else if (code == 30015){//骑行关锁后停在禁停区域 Parked in the forbidden area after the lock is locked
                lockStatus = "0";
                outArea = "2";
                scanLayout.setVisibility(View.VISIBLE);
                showBorderDialog(getString(R.string.forbid_description_text));
            }else{
                CommonUtils.onFailure(this, code, TAG);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handling bicycle unlocking
     * 处理单车开锁中
     * @param retJson
     */
    private void handlerDbLocking(JSONObject retJson){
        try {
            int code = retJson.getInt("code");
            if (code == 30010){
                handler.sendEmptyMessageDelayed(HANDLER_IN_THE_LOCK,2000);
            }else{
                handler.removeMessages(HANDLER_IN_THE_LOCK);
                RequestDialog.dismiss(this);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handling cycling area results
     * 处理单车区域结果
     * @param areaResult
     */
    private void handlerAreaResult(AreaResult areaResult){
        String ids = areaResult.getIds();
        List<StopAreaBean> list = areaResult.getList();
        if (!TextUtils.isEmpty(ids)) {
            redList = list;
            handler.sendEmptyMessage(HANDLER_MAP_RED_AREA);
        } else {
            addAreaList(list, false);
            if (redList.size() > 0) {
                addAreaList(redList, true);
            }
            setMapStopArea(areaList,boundaryArea);
        }
        getBike(curLat, curLng, areaResult.getTargetLat(), areaResult.getTargetLng());
    }

    /**
     * Handling nearby bicycles
     * 处理附近单车
     * @param jsonArray
     */
    private void handlerBike(JSONArray jsonArray){
        Gson gson = new Gson();
        try{
            markerList.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = (JSONObject) jsonArray.get(i);
                NearbyBike bike = gson.fromJson(json.toString(), NearbyBike.class);
                if (!isExistScooter(bike.getNumber())) {
                    MyItem item = new MyItem(bike.getgLat(), bike.getgLng());
                    item.setNumber(bike.getNumber());
                    item.setPrice(bike.getPrice() + "");
                    item.setBid(Integer.valueOf(bike.getBid()));
                    item.setRedpackRuleVo(bike.getRedpackRuleVo());
                    item.setReadpack(bike.getReadpack());
                    item.setTypeCount(bike.getTypeCount());
                    item.setBikeType(bike.getBikeType());
                    item.setAreaId(bike.getAreaId());
                    item.setPowerPercent(bike.getPowerPercent());
                    if (item.getReadpack() == 0) {//普通单车 Ordinary bicycle
                        markerList.add(item);
                    } else {//红包单车 Red envelope bicycle
                        // TODO: 2018/12/11 暂时不处理，需要时再处理
                    }
                    numbers.add(item.getNumber());
                }
            }
            if (!isRiding){
                showScooter();
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    /**
     * Empty and destroy all bicycle icons on the map
     * 清空并销毁地图上所有单车图标
     */
    private void clearAllMarkers(){
        if (scooterMarkers.size() > 0){
            for (Marker marker : scooterMarkers){
                marker.remove();
            }
            scooterMarkers.clear();
        }
        clickMarker = null;
        LogUtils.i(TAG,"-=-=-=-=-=-=-=-=-=-=清空并销毁地图上所有单车图标。。。。。");
    }

    /**
     * 添加地图标记
     * Add map markers
     */
    private List<Marker> scooterMarkers = new ArrayList<>();
    private void showScooter(){
        for (MyItem myItem : markerList){
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.scooter))
                    .position(myItem.getPosition()));
            marker.setTag(myItem);
            scooterMarkers.add(marker);
        }
    }

    /**
     * End of the ride, clear the map
     * 骑行结束，清空地图
     */
    private void clearMap() {
        numbers.clear();
        markerList.clear();
        existAreaList.clear();
        boundaryAreaDetail = "";
        clearAllMarkers();
        ridePath.clear();
        totalDistance = 0;
        if (redList.size() > 0) {
            redList.clear();
        }
        if (mMap != null) {
            mMap.clear();//清除地图 clear map
            isDrawStartImg = false;//清空Map时骑行中画骑行路径时改变画起始点标记的状态,允许重画，不然将看不到骑行中的起始图标
            LogUtils.i(TAG,"clearMap: 调用了清空Map方法");
        }
    }

    /**
     * Handling bicycle use information
     * 处理单车使用信息
     * @param jsonObject
     */
    private void handlerBikeUseInfo(JSONObject jsonObject){
        try {
            Gson gson = new Gson();
            int code = jsonObject.getInt("code");
            if (code == 200){
                if (jsonObject.has("forcedBorderDistance")){
                    forcedBorderDistance = jsonObject.getString("forcedBorderDistance");
                }
                if (jsonObject.has("cityBorderDistance")){
                    cityBorderDistance = jsonObject.getString("cityBorderDistance");
                }
                if (jsonObject.has("type")) {
                    int type = jsonObject.getInt("type");
                    if (type == 1) {//预约中 In the appointment
//                        reserve(gson, jsonObject.getJSONArray("data"));
                    } else if (type == 2) {//骑行中 Riding
                        riding(gson, jsonObject.getJSONArray("data"));
                    } else if (type == 3) {//骑行完成 Cycling completed
                        ridingEnd(gson, jsonObject.getJSONObject("data"));
                    }
                } else {//正常状态 normal status
                    normalSate(code);
                }
            }else if (code == 202) {//没有数据,正常状态 No data, normal state
                normalSate(code);
            } else {
                CommonUtils.onFailure(this, code, TAG);
            }
            if (jsonObject.has("topVersion")) {//app发现有新版本 App found a new version
                JSONObject version = jsonObject.getJSONObject("topVersion");
                newVersion(gson, version);
            }
            if (jsonObject.has("bindCard")) {//有绑定银行卡 Have a binding bank card
                isBindCard = jsonObject.getInt("bindCard");
            } else {//没有绑定银行卡 No bank card binding
                isBindCard = -1;
            }
            if (jsonObject.has("cityVo")){
                JSONObject areaJson = jsonObject.getJSONObject("cityVo");
                boundaryArea = gson.fromJson(areaJson.toString(),BoundaryArea.class);
                setBoundaryArea(boundaryArea);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cycling complete jump interface
     * 骑行完成跳转界面
     **/
    private void ridingEnd(Gson gson, JSONObject json) {
        dismissBorderDialog();
        isRiding = false;
        callUnlocking = false;
        isGetKey = false;
        bikeState = 3;
        FinishBikeState finishBikeState = gson.fromJson(json.toString(), FinishBikeState.class);
        useEndNumber.setText(finishBikeState.getTradeVo().getBikeUseVo().getNumber());
        LogUtils.i(TAG,"骑行完成跳转界面");
        rideId = finishBikeState.getTradeVo().getBikeUseVo().getId();
        isUnLockClose = false;
        uploadRideInfo();//上传骑行信息 Uploading ride information
        scooterInfoLayout.setVisibility(View.GONE);
        useEndLayout.setVisibility(View.VISIBLE);
        useLayout.setVisibility(View.GONE);
        scanLayout.setVisibility(View.GONE);
        rateRideLayout.setVisibility(View.GONE);
        setCenter();
        handler.sendEmptyMessage(HANDLER_DISCONNECT);
        clearNetworkLock();
        clearMap();
    }

    /**
     * 正常状态
     * normal
     **/
    private void normalSate(int code) {
        if (!isDontRate && useEndLayout.getVisibility() == View.GONE) {
            bikeState = 0;
            LogUtils.i(TAG, "正常状态 code=" + code + "-----data null");//数据不存在 Data does not exist
            restoreMarker();
            scanLayout.setVisibility(View.VISIBLE);
            scooterInfoLayout.setVisibility(View.GONE);
            useLayout.setVisibility(View.GONE);
            useEndLayout.setVisibility(View.GONE);
            rateRideLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Riding
     * 骑行中
     * @param gson
     * @param jsonArray
     */
    private int currentShowUse = 0;//显示当前的骑行数据 Show current ride data
    private void riding(Gson gson, JSONArray jsonArray){
        LogUtils.i(TAG,"骑行中 : "+jsonArray.toString());
        if (jsonArray.length() == 0) return;
        List<RunBikeState> bikes = gson.fromJson(jsonArray.toString(),new TypeToken<List<RunBikeState>>(){}.getType());
        if (bikes.size() == 1 && ("0".equals(bikes.get(0).getRideStatus()) || "-2".equals(bikes.get(0).getRideStatus()))){
            normalSate(0);
            LogUtils.i(TAG, "正在开锁中，给个进度条提示用户");
            saveSharedValue(CommonSharedValues.SP_KEY_NUMBER,bikes.get(0).getBikeVo().getNumber());
            RequestDialog.show(this);
            debLocking(bikes.get(0).getBikeVo().getNumber());
            return;
        }
        isRiding = true;
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);//关闭侧滑手势 Turn off the sliding gesture
        bikeState = 2;
        //By default, one of your own riding data is displayed (hostId = 0 is your own riding data)
        RunBikeState runBikeState = null;//默认显示自己的一条骑行数据（hostId = 0 就是自己的骑行数据）
        for (int i = 0; i < bikes.size(); i ++){
            if ("0".equals(bikes.get(i).getHostId())){
                currentShowUse = i;
                runBikeState = bikes.get(currentShowUse);
            }
        }
        saveSharedValue(CommonSharedValues.SP_KEY_NUMBER,runBikeState.getBikeVo().getNumber());
        startUseTime();
        useNumber.setText(runBikeState.getBikeVo().getNumber());
        useBattery.setText(runBikeState.getBikeVo().getPower() + "%");
        bikeType = runBikeState.getBikeVo().getBikeType();
        scooterLockingDate = runBikeState.getDate();
        rideId = runBikeState.getId();
        hostId = runBikeState.getHostId();
        outArea = runBikeState.getOut_area();
        lockStatus = runBikeState.getStatus();
        if ("0".equals(outArea)){//骑行没有越界 Riding does not cross the border
            scanLayout.setVisibility(View.GONE);
        }else if ("1".equals(outArea)){//骑行越界了 Riding across the border
            if ("0".equals(lockStatus)){//锁关了 Locked off
                showBorderDialog(getString(R.string.border_description_text));
                scanLayout.setVisibility(View.VISIBLE);
            }else{
                scanLayout.setVisibility(View.GONE);
            }
        }else if ("2".equals(outArea)){//禁停区 Forbidden zone
            if ("0".equals(lockStatus)){//锁关了 Locked off
                showBorderDialog(getString(R.string.forbid_description_text));
                scanLayout.setVisibility(View.VISIBLE);
            }else{
                scanLayout.setVisibility(View.GONE);
            }
        }
        scanLayout.setVisibility(View.GONE);
        scooterInfoLayout.setVisibility(View.GONE);
        useEndLayout.setVisibility(View.GONE);
        useLayout.setVisibility(View.VISIBLE);
        rateRideLayout.setVisibility(View.GONE);
        clearAllMarkers();
        if (curLat != 0 || curLng != 0) {
            setCenter();
            if (!callUnlocking) {
                LogUtils.i(TAG,"初始化骑行中调用了画停车区域");
                String redAreaId = sp.getString(CommonSharedValues.SP_RED_BIKE_AREA_ID, "");
                getRedArea(curLat,curLng,curLat, curLng,redAreaId);
                callUnlocking = true;
            }
            LogUtils.i(TAG,"每次初始化界面调用了骑行状态");
        }
        if (scooterService != null){
//            if (isOpenBlue()) {//蓝牙已开启 Bluetooth is on
//                String mac = sp.getString(CommonSharedValues.SP_MAC_ADDRESS, "");
//                if (!TextUtils.isEmpty(mac)) {
//                    if (!isConnectedDevice(mac.toUpperCase())) {//如果蓝牙未连接 If Bluetooth is not connected
//                        //Start scanning Bluetooth devices, otherwise you can't connect directly
//                        startScanBLEDevice(mac.toUpperCase(), 20000);//开始扫描蓝牙设备，不然直接连连不上
//                    }else{
//                        LogUtils.i(TAG, "areRiding: 滑板车蓝牙已经连接，可以通讯");
//                    }
//                }
            //}else{
                LogUtils.i(TAG, "areRiding: 蓝牙开关未开启");
            //}
        }
    }

    /**
     * 显示越界对话框
     * @param content
     */
    private BorderDialog borderDialog = null;
    private void showBorderDialog(String content){
        if (borderDialog == null) {
            borderDialog = new BorderDialog(this, 0);
        } else if (!borderDialog.isShowing()) {
            borderDialog.show();
        }
        borderDialog.setContent(content);
    }

    /**
     * 关闭越界对话框
     */
    private void dismissBorderDialog(){
        if (borderDialog != null){
            if (borderDialog.isShowing()){
                borderDialog.dismiss();
            }
            borderDialog = null;
        }
    }

    /**
     * 弹出app有新版本更新对话框
     * A new version update dialog box appears for the app
     */
    private String apkUrl = null;
    private void newVersion(Gson gson, JSONObject version) {
        if (!myApplication.isUpdateVersions) return;
        ApkUpdateBean apkUpdateBean = gson.fromJson(version.toString(), ApkUpdateBean.class);
        apkUrl = apkUpdateBean.getUrl();
        final String content = apkUpdateBean.getContent().replace("\\n", "\n");
        versionsUpdateDialog(content);
        myApplication.isUpdateVersions = false;
    }

    /**
     * 版本更新对话框
     * Version update dialog box
     * @param content
     */
    private void versionsUpdateDialog(String content){
        new VersionHintDialog(this, content, new VersionHintDialog.VersionUpdateCallBack() {
            @Override
            public void updateCall() {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 5);
                } else {
                    Intent intent = new Intent(MainActivity.this, DownloadService.class);
                    intent.putExtra("apkUrl", apkUrl);
                    startService(intent);
//                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="+getPackageName())));
                }
            }
        },0);
    }

    private void handlerUserInfo(UserInfoBean bean){
        String firstNa = bean.getUserInfo().getFirstname();
        String lastNa = bean.getUserInfo().getLastname();
        String spFirstNa = sp.getString(CommonSharedValues.SP_KEY_INFO_FIRSTNAME, "");
        String spLastNa = sp.getString(CommonSharedValues.SP_KEY_INFO_LASTNAME, "");
        String phone = bean.getUserInfo().getUserVo().getPhone();
        String email = bean.getUserInfo().getEmail();
        String headUrl = bean.getUserInfo().getUserVo().getHeadUrl();
        showHeadImg(headUrl);
        if (!TextUtils.isEmpty(firstNa)) {
            setUserName(firstNa,lastNa);
            CommonUtils.saveUserInfo(sp,bean);
        } else if (!TextUtils.isEmpty(spFirstNa)){
            setUserName(spFirstNa,spLastNa);
        } else if (!TextUtils.isEmpty(phone)){
            userName.setText(phone);
        } else {
            userName.setText(email);
        }
    }

    private void setUserName(String firstN,String lastN){
        if (firstN.equals(lastN)){
            userName.setText(firstN);
        }else{
            userName.setText(firstN + getString(R.string.blank) + lastN);
        }
    }

    /**
     * Save constant value
     * 保存常量值
     * @param key
     * @param value
     */
    private void saveSharedValue(String key,String value){
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Get a red envelope bicycle or regular bicycle parking area
     * 获得红包单车或普通单车停车区域
     */
    private void getRedArea(double curLat, double curLng, double targetLat, double targetLng,String redAreaId){
        areaList.clear();
        if (!TextUtils.isEmpty(redAreaId)){
            getStopArea(curLat, curLng,targetLat,targetLng, redAreaId);
        }else{
            if (redList.size() > 0) {
                redList.clear();
            }
            getStopArea(curLat, curLng,targetLat,targetLng, "");
        }
    }

    private void startUseTime() {//启动骑行计时(秒) Start riding time (seconds)
        handler.removeMessages(HANDLER_SUE_TIME);
        handler.removeMessages(HANDLER_SUE_TIME);
        handler.removeMessages(HANDLER_SUE_TIME);
        handler.sendEmptyMessage(HANDLER_SUE_TIME);
    }

    private void clearNetworkLock(){//清除定时网络关锁
        handler.removeMessages(HANDLER_NETWORK_LOCK);
        handler.removeMessages(HANDLER_NETWORK_LOCK);
        handler.removeMessages(HANDLER_NETWORK_LOCK);
        RequestDialog.dismiss(this);
    }

    private boolean isMapMoveUpdate = false;//是否地图移动刷新数据 Whether the map moves to refresh the data
    private boolean isMoveMapCenter = false;//是否移动到地图中心 Whether to move to the map center
    private boolean isUnLockClose = false;//是否通知服务器关锁 Whether to notify the server to lock
    private static final int HANDLER_MAP_MOVE_UPDATE = 101;
    private static final int HANDLER_LOCATION_MAP_CENTER = 102;
    private static final int HANDLER_MAP_RED_AREA = 103;
    private static final int HANDLER_IN_THE_LOCK = 104;
    private static final int HANDLER_DISCONNECT = 105;
    private static final int HANDLER_SUE_TIME = 106;
    private static final int HANDLER_RATE_RIDE = 107;
    private static final int HANDLER_NETWORK_LOCK = 108;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case HANDLER_SUE_TIME://获取滑板车电量信息 Get scooter battery information
                    if ("2".equals(bikeType)) {//滑板车 scooter
                        if (scooterService != null && isGetKey) {
                            if ("0".equals(hostId)) {
                                sendGetScooterInfo();//获取滑板车电量信息 Get scooter battery information
                            }
                        }
                    }
                    handler.sendEmptyMessageDelayed(HANDLER_SUE_TIME, 1000);
                    break;
                case HANDLER_MAP_MOVE_UPDATE://地图移动5秒钟后可以刷新地图数据 Map data can be refreshed after 5 seconds of map movement
                    isMapMoveUpdate = true;
                    break;
                case HANDLER_LOCATION_MAP_CENTER://将地图移动到中心 Move the map to the center
                    setCenter();
                    isMoveMapCenter = true;
                    break;
                case HANDLER_MAP_RED_AREA://处理地图红包区域 Processing map red envelope area
                    //Get a normal bicycle parking area to load the red envelope bicycle parking area
                    getStopArea(curLat, curLng,curLat, curLng, "");//获得普通单车停车区域方便加载红包单车停车区域
                    break;
                case HANDLER_IN_THE_LOCK://处理正在开锁中 Processing is unlocking
                    debLocking(sp.getString(CommonSharedValues.SP_KEY_NUMBER, ""));
                    break;
                case HANDLER_DISCONNECT:// 断开蓝牙连接 Disconnect Bluetooth
                    if ("2".equals(bikeType)) {//滑板车 scooter
                        if (scooterService != null){
                            sendDisconnectScooter();//断开锁连接 Disconnect lock connection
                            LogUtils.i(TAG,"--------------------  滑板车 断开蓝牙"+"---"+bikeType);
                        }
                    }
                    break;
                case HANDLER_RATE_RIDE:
                    isDontRate = false;
                    normalSate(0);
                    break;
                case HANDLER_NETWORK_LOCK://处理滑板车网络关锁，每隔3s请求一次，一直等拿到结费订单
                    scooterLocking();
                    break;
            }
        }
    };

    private class MainBroadcast extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if (BroadCastValues.LOG_OUT.equals(intent.getAction()) ||
                    BroadCastValues.REMOTE_LOGIN_BROADCAST.equals(intent.getAction())) {//退出登录 sign out
                isLogOut = true;
            } else if (BroadCastValues.UNLOCKING_SUCCESS.equals(intent.getAction())){//开锁成功 Unlocked successfully
                isUnLockClose = false;
                useBattery.setText("0%");
                ratingBar.setRating(0);
                String bikeNumber = intent.getStringExtra("number");
                saveSharedValue(CommonSharedValues.SP_KEY_NUMBER,bikeNumber);
                String runTime = intent.getStringExtra("runTime");
                closeTimestamp = intent.getStringExtra("timestamp");
                boundaryArea = (BoundaryArea)intent.getSerializableExtra("boundaryArea");
                if (boundaryArea != null){//画停车区域 Painting parking area
                    setBoundaryArea(boundaryArea);
                }
                LogUtils.i(TAG,"=====bikeNumber=====" + bikeNumber + "--runTime--" + runTime + "--closeTimestamp--" + closeTimestamp);
                if (!callUnlocking) {//防止调用两次 Prevent calls twice
                    if (TextUtils.isEmpty(closeTimestamp)) {
                        String redAreaId = sp.getString(CommonSharedValues.SP_RED_BIKE_AREA_ID, "");
                        getRedArea(curLat,curLng,curLat,curLng,redAreaId);
                        callUnlocking = true;
                        useLayout.setVisibility(View.VISIBLE);
                        scanLayout.setVisibility(View.GONE);
                    }else{
                        if (!TextUtils.isEmpty(runTime)){
                            setUnLockClose(0,Integer.parseInt(runTime), Long.parseLong(closeTimestamp));
                            isUnLockClose = true;
                            LogUtils.i(TAG,"onReceive: 开锁界面结束，调用了主界面通知服务器关锁");
                        }
                        callUnlocking = false;
                    }
                }
            } else if (BroadCastValues.MAP_INIT_GET_BIKE.equals(intent.getAction())) {//地图第一次初始化广播 The map initializes the broadcast for the first time
                getRedArea(curLat, curLng,curLat,curLng,"");//获得普通单车停车区域 Get a regular bicycle parking area
                if (bikeState == 1) {//单车预约中 Bicycle reservation
                    /*LatLng latLng = new LatLng(Double.parseDouble(sp.getString(CommonSharedValues.SP_KEY_LATITUDE, "")),
                            Double.parseDouble(sp.getString(CommonSharedValues.SP_KEY_LONGITUDE, "")));
                    getRoute(latLng);*/
                } else if (bikeState == 2) {//单车骑行中 Cycling
                    if (!callUnlocking) {
                        String redAreaId = sp.getString(CommonSharedValues.SP_RED_BIKE_AREA_ID, "");
                        getRedArea(curLat, curLng,curLat,curLng, redAreaId);
                        LogUtils.i(TAG,"-------------地图初始化调用了骑行状态");
                        callUnlocking = true;
                    }
                }
            } else if (BroadCastValues.MAP_MOVE_END.equals(intent.getAction())) {//地图移动结束发送广播监听 Map mobile end sends broadcast monitor
                double targetLatitude = intent.getDoubleExtra("Latitude", 0);
                double targetLongitude = intent.getDoubleExtra("Longitude", 0);
                LogUtils.i(TAG,"目的地经纬度=" + targetLatitude + "---" + targetLongitude + "当前经纬度=" + curLat + ">>" + curLng);
                if (scooterInfoLayout.getVisibility() == View.GONE
                        && useLayout.getVisibility() == View.GONE
                        && useEndLayout.getVisibility() == View.GONE
                        && rateRideLayout.getVisibility() == View.GONE) {
                    if (isMapMoveUpdate) {
                        areaList.clear();
                        //When the map moves, the bicycle parking area is obtained, and there is no restriction. If there is a red envelope area,
                        // the red envelope area is displayed, and there is no ordinary area.
                        getStopArea(curLat, curLng,targetLatitude, targetLongitude, "");//地图移动时获得单车停车区域，不做限制，有红包区域就显示红包区域，没有就普通区域
                        isMapMoveUpdate = false;
                        handler.sendEmptyMessageDelayed(HANDLER_MAP_MOVE_UPDATE, 5000);
                        LogUtils.i(TAG,"onReceive: 地图移动刷新数据了");
                    }else{
                        LogUtils.i(TAG,"onReceive: 地图移动了，但没到刷新数据时间！！！！");
                    }
                    handler.removeMessages(HANDLER_LOCATION_MAP_CENTER);
                    handler.sendEmptyMessageDelayed(HANDLER_LOCATION_MAP_CENTER, 20000);
                    isMoveMapCenter = false;
                }
            } else if (BroadCastValues.UPDATE_USER_INFO.equals(intent.getAction())) {//更新用户资料 Update user profile
                getUserInfo();
            } else if (BroadCastValues.GOOGLE_PUSH_BROADCAST.equals(intent.getAction())) {//google消息推送广播(异地登录) Remote login
                if (isWindowFocus) {
                    CommonUtils.remoteLoginDialog(MainActivity.this);
                }
            } else if (BroadCastValues.REFRESH_BIKE_USE_INFO.equals(intent.getAction())) {//google消息推送广播(刷新单车使用信息，针对关锁了未跳转结费界面的情况)
                if (isWindowFocus || isRiding){
                    //Google message push broadcast (refresh the bicycle use information, for the case of unlocking the un-jumping fee interface)
                    getBikeUseInfo();
                }
            } else if (BroadCastValues.RATE_OR_DONT_RATE_SUCCESS.equals(intent.getAction())){//骑行结束评价或者不评价 End of ride evaluation or no evaluation
                dontRate();
            } else if (BroadCastValues.REPORT_SUCCESS_BROAD.equals(intent.getAction())){//报告故障成功广播 Failure reported successfully broadcast
                showReportSuccessDialog();
            }
        }
    }
    private ReportSuccessDialog reportSuccessDialog;
    private void showReportSuccessDialog(){
        if (reportSuccessDialog == null){
            reportSuccessDialog = new ReportSuccessDialog(this,0);
        }else if (!reportSuccessDialog.isShowing()){
            reportSuccessDialog.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtils.remove(this);
        unregisterReceiver(mainBroadcast);//取消广播注册 Cancel broadcast registration
        unRegisterReceiver();//取消广播注册(滑板车) Cancel broadcast registration (scooter)
        if (!isLogOut) {
            //Kill the current program process completely (or use: System.exit(0);)
            android.os.Process.killProcess(android.os.Process.myPid());//彻底杀死当前程序进程（或者使用：System.exit(0);）
        }
    }

    private List<LatLng> ridePath = new ArrayList<>();//骑行中保存的骑行路径经纬度 Riding latitude and longitude of the riding path saved in the ride
    private double totalDistance = 0;//骑行中保存的骑行总距离 Total distance of riding saved during riding

    /**
     * Get accurate latitude and longitude
     * 获得精确经纬度
     * @return
     */
    private LatLng getPreciseLatLng(){
        String updateLat = shared.getString(CommonSharedValues.UPDATE_LAT,"");
        String updateLng = shared.getString(CommonSharedValues.UPDATE_LNG,"");
        if (TextUtils.isEmpty(updateLat) || TextUtils.isEmpty(updateLng)){
            return new LatLng(curLat,curLng);
        }else{
            return new LatLng(Double.parseDouble(updateLat),Double.parseDouble(updateLng));
        }
    }

    @Override
    public void locationCall(LocationCall locationCall) {
        super.locationCall(locationCall);
        //This logic function verifies outArea (over-the-counter flag status) after each positioning is completed,
        // and requests 30031 to upload the riding information when a change occurs.
        //此逻辑功能为每次定位结束后，验证outArea(越界标记状态)，当发生改变时请求30031上传骑行信息
        if (isRiding){//骑行中 Riding
            if (ridePath.size() > 0){
                LatLng fromLatLng = ridePath.get(ridePath.size() - 1);
                LatLng toLatLng = getPreciseLatLng();
                if (fromLatLng.latitude != toLatLng.latitude || fromLatLng.longitude != toLatLng.longitude){
                    ridePath.add(toLatLng);
                    double distance = SphericalUtil.computeDistanceBetween(fromLatLng, toLatLng);//计算两点之间的距离 Calculate the distance between two points
                    totalDistance = CommonUtils.add(totalDistance,distance);//计算骑行累加总距离 Calculate the cumulative total distance of the ride
                    if (polyline != null) {
                        polyline.remove();
                    }
                    drawRidingLine(ridePath);
                    LogUtils.i(TAG, "onLocationChanged: ------ 添加了也计算了---距离："+totalDistance);
                }
            }else{
                LogUtils.i(TAG, "onLocationChanged: ------ 添加了");
                ridePath.add(getPreciseLatLng());
            }
            String areaStatus = outAreaStatus(forcedBorderDistance,cityBorderDistance);
            if (!outArea.equals(areaStatus)){//越界标记状态发生改变 Cross-border mark status changes
                outArea = areaStatus;
                uploadRideInfo();//上传骑行信息 Uploading ride information
            }
        }
        if (scooterInfoLayout.getVisibility() == View.GONE
                && useLayout.getVisibility() == View.GONE
                && useEndLayout.getVisibility() == View.GONE
                && rateRideLayout.getVisibility() == View.GONE) {
            if (isMoveMapCenter) {
                handler.sendEmptyMessageDelayed(HANDLER_LOCATION_MAP_CENTER, 20000);
                isMoveMapCenter = false;
            }
        }else{
            isMoveMapCenter = true;
            handler.removeMessages(HANDLER_LOCATION_MAP_CENTER);
        }
    }

    //Click the back button to record the current time, compare more than two seconds to exit the desktop
    private long exitTime = 0;//点击返回键记录的当前时间，比较大于两秒就退出桌面
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//系统返回键监听，退出程序 System return key listener, exit the program
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(this,getString(R.string.return_desktop),Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            }else{
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /***--------------------滑板车连接蓝牙部分 Scooter connected to the Bluetooth part ------------------------**/

//    @Override
//    protected void onServiceConnectedCallBack(ScooterService mBLEService) {
//        LogUtils.i(TAG, "onServiceConnectedCallBack: ---主界面----初始化绑定服务回调");
//        scooterService = mBLEService;
//    }
//
//    @Override
//    protected void onScanBLEDeviceCallBack(BluetoothDevice scanBLEDevice) {
//        if (isWindowFocus && isRiding) {
//            stopScanBLEDevice();//停止扫描 Stop scanning
//            connectScooter(scanBLEDevice.getAddress());//连接滑板车 Connecting scooter
//            LogUtils.i(TAG, "onScanBLEDeviceCallBack: -----主界面-----扫描蓝牙设备成功连接蓝牙--蓝牙mac地址为：" + scanBLEDevice.getAddress() + "---蓝牙名称为：" + scanBLEDevice.getName());
//        }
//    }
//
//    @Override
//    protected void onScanBLEDeviceNotCallBack(String deviceAddress) {
//        if (isWindowFocus && isRiding) {
//            if (!isConnectedDevice(deviceAddress)) {//如果蓝牙未连接 If Bluetooth is not connected
//                //Start scanning Bluetooth devices, otherwise you can't connect directly
//                startScanBLEDevice(deviceAddress, 20000);//开始扫描蓝牙设备，不然直接连连不上
//                LogUtils.i(TAG, "onScanBLEDeviceNotCallBack: ---主界面---扫描蓝牙失败！！！接着扫描");
//            }else{
//                LogUtils.i(TAG, "onScanBLEDeviceNotCallBack: ---主界面---滑板车蓝牙已经连接，可以通讯");
//            }
//        }
//    }
//
//    @Override
//    protected void onBLEWriteNotify() {
//        if (isWindowFocus && isRiding) {
//            sendGetKeyCommand(CommonSharedValues.BLE_SCOOTER_KEY);
//            LogUtils.i(TAG, "onBLEWriteNotify: -----主界面-----注册了通知回调，去获取key");
//        }
//    }
//
//    @Override
//    protected void onBLEGetKeyError() {
//        //Disconnect the lock, disconnect the Bluetooth callback method will reconnect
//        sendDisconnectScooter();//断开锁连接，断开蓝牙回调方法会重连
//        LogUtils.i(TAG, "onBLEGetKeyError: ---主界面--获取Key失败，先断开蓝牙再扫描连接");
//    }
//
//    @Override
//    protected void onBLEGetKey(String mac, byte communicationKey) {
//        isGetKey = true;
//        if (isWindowFocus && isRiding) {
//            sendGetDeviceInfo();//获取滑板车锁状态信息 Get scooter lock status information
//            LogUtils.i(TAG, "onBLEGetKey: ----主界面----获取Key成功,去获取锁状态");
//        }
//    }
//
//    @Override
//    protected void onBLEScooterInfo(int power, int speedMode, int speed, int mileage, int prescientMileage) {
//        if (isWindowFocus && isRiding) {
//            LogUtils.i(TAG, "onBLEScooterInfo: ---主界面---获取到滑板车信息 电量= " + power + " 模式= " + speedMode + " 速度= " + speed);
//            useBattery.setText(power + "%");
//            saveSharedValue(CommonSharedValues.SP_LOCK_POWER,power+"");
//        }
//    }
//
//    @Override
//    protected void onBLEDeviceInfo(int voltage, int status, String version) {
//        if (isWindowFocus && isRiding) {
//            LogUtils.i(TAG, "onBLEDeviceInfo: ---主界面---获取到锁状态信息");
//            if ((status & 0x40) != 0) {//有旧数据 Old data
//                sendGetOldData();
//                LogUtils.i(TAG, "onBLEDeviceInfo:  ---主界面--有旧数据，去获得旧数据");
//            } else {//无旧数据 No old data
//                if ((status & 0x01) != 0) {//锁是开的 The lock is open
//                    LogUtils.i(TAG, "onBLEDeviceInfo:  ---主界面---无旧数据，锁是开着的状态，可以进行通信了");
//                } else if ((status & 0x02) != 0) {//锁是关的 Lock is off
//                    LogUtils.i(TAG, "onBLEDeviceInfo:  ----主界面---无旧数据，锁是关着的状态");
//                }
//            }
//        }
//    }
//
//    @Override
//    protected void onBLEScooterOldData(long timestamp, long openTime, long userId) {
//        if (isWindowFocus && isRiding) {
//            LogUtils.i(TAG, "onBLEScooterOldData: ----主界面---- 获取到旧数据 向服务器上传旧数据");
//            setUnLockClose(Integer.parseInt(userId+""), Integer.parseInt(openTime+""), timestamp);
//        }
//    }
//
//    @Override
//    protected void onBLEScooterCloseCallBack(int status, long timestamp, long time) {
//        if (!isScanUnlock) {
//            LogUtils.i(TAG, "onBLEBicnmanOpenCallBack: ---主界面---收到蓝牙关锁通知 status = " + status);
//            if (status == 1) {//关锁成功 Locked successfully
//                setUnLockClose(0, Integer.parseInt(time + ""), timestamp);
//                isUnLockClose = true;
//                LogUtils.i(TAG, "onBLEBicnmanOpenCallBack: ---主界面---关锁成功,通知服务器关锁成功");
//            } else if (status == 2) {//关锁失败 Lock failure
//                Toast.makeText(this, getString(R.string.operation_failed), Toast.LENGTH_SHORT).show();
//                LogUtils.i(TAG, "onBLEBicnmanOpenCallBack: ---主界面---关锁失败！！");
//            }
//        }
//    }

//    @Override
//    protected void onBLEDisconnected() {
//        if ("2".equals(bikeType)) {//滑板车 scooter
//            isGetKey = false;
//            if (isWindowFocus && isRiding) {
//                if (isOpenBlue()) {
//                    //Start scanning Bluetooth devices, otherwise you can't connect directly
//                    startScanBLEDevice(sp.getString(CommonSharedValues.SP_MAC_ADDRESS, "").toUpperCase(), 20000);//开始扫描蓝牙设备，不然直接连连不上
//                    LogUtils.i(TAG, "onBLEDisconnected: --主界面--蓝牙断开连接,重新扫描");
//                } else {
//                    LogUtils.i(TAG, "onBLEDisconnected: --主界面--蓝牙断开连接,系统蓝牙开关被用户关闭！！！");
//                }
//            }
//        }
//    }

//    @Override
//    protected void onSystemBLEOpen() {
//        if ("2".equals(bikeType)) {//滑板车 scooter
//            if (isWindowFocus && isRiding) {
//                if (isOpenBlue()) {
//                    //Start scanning Bluetooth devices, otherwise you can't connect directly
//                    startScanBLEDevice(sp.getString(CommonSharedValues.SP_MAC_ADDRESS, "").toUpperCase(), 20000);//开始扫描蓝牙设备，不然直接连连不上
//                    LogUtils.i(TAG, "onSystemBLEClose: ---主界面---接收到系统蓝牙 开关 开启 ，重新扫描连接蓝牙");
//                }
//            }
//        }
//    }

    @Override
    protected void onBLECommandError(int status) {
        isGetKey = false;
        if (isWindowFocus && isRiding) {
            LogUtils.i(TAG, "onBLECommandError: ---主界面---发送某个指令失败，先断开蓝牙再扫描连接" +
                    "状态原因是 1：CRC认证错误 2：未获取通信KEY 3：通信KEY错误---status = " + status);
            //Disconnect the lock, disconnect the Bluetooth callback method will reconnect
            sendDisconnectScooter();//断开锁连接，断开蓝牙回调方法会重连
        }
    }
}
