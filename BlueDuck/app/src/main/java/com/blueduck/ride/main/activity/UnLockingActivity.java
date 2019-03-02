package com.blueduck.ride.main.activity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.blueduck.ride.R;
import com.blueduck.ride.main.bean.BoundaryArea;
import com.blueduck.ride.main.bean.LockBean;
import com.blueduck.ride.main.service.MainService;
import com.blueduck.ride.utils.BroadCastValues;
import com.blueduck.ride.utils.CommonSharedValues;
import com.blueduck.ride.utils.CommonUtils;
import com.blueduck.ride.utils.LogUtils;
import com.blueduck.ride.utils.RequestCallBack;
import com.google.gson.Gson;
import com.omni.ble.library.activity.BaseScooterServiceActivity;
import com.omni.ble.library.service.ScooterService;

import org.json.JSONException;
import org.json.JSONObject;

public class UnLockingActivity extends BaseScooterServiceActivity implements RequestCallBack,View.OnClickListener {

    private static final String TAG = "UnLockingActivity";

    private MainService mainService;
    private TextView titleText;
    private ImageView unLockingImg;

    private SharedPreferences sp;
    private String number,curLat,curLng,outArea,inputNumber,rideUser;
    private Animation animation;//旋转动画
    private ScooterService scooterService = null;//滑板车蓝牙服务 Scooter Bluetooth service
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager bluetoothManager;

    private int count = 0;
    private boolean isPause = false;
    private boolean isOpen = false;
    private boolean isRepetition = false;//是否新请求开锁 Whether the new request is unlocked
    private boolean isUse = false;//锁是否在使用 Is the lock in use?
    private boolean isTCP = false;//是TCP开的锁 Is a TCP open lock
    private boolean isBLE = false;//是蓝牙开的锁 Is a Bluetooth open lock
    private boolean isUploadUnlockSuccess = false;//是否已经向服务器上传开锁成功请求 Have you uploaded an unlock request to the server?
    private String unLockMac;//开锁Mac地址 Unlock Mac address
    private String unLockDateTime;//开锁时间戳 Unlock timestamp
    private String tcpConnected;
    private BoundaryArea boundaryArea = null;//边界区域实体 Boundary area entity
    private String lockPower;//服务器返回锁电量 Server returns lock power
    private int bikeBleConnectType;//车蓝牙连接类型 1：单车 2：滑板车 Car Bluetooth connection type 1: Bicycle 2: Scooter
    private AlertDialog bleDialog = null;
    private AlertDialog errorDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonUtils.add(this);
        init();
        setContentView(R.layout.unlocking_activity);
        initView();
    }

    private void init() {
        mainService = new MainService(this,this,TAG);
        sp = getSharedPreferences(CommonSharedValues.SP_NAME, MODE_PRIVATE);
        number = getIntent().getStringExtra("number");
        curLat = getIntent().getStringExtra("curLat");
        curLng = getIntent().getStringExtra("curLng");
        outArea = getIntent().getStringExtra("outArea");
        inputNumber = getIntent().getStringExtra("inputNumber");
        rideUser = getIntent().getStringExtra("rideUser");
    }

    private void initView() {
        findViewById(R.id.title_left_layout).setOnClickListener(this);
        titleText = (TextView) findViewById(R.id.common_title_text);
        titleText.setText(getString(R.string.unlocking_title_text));
        unLockingImg = (ImageView) findViewById(R.id.unlocking_image);
        registerLocalReceiver();//初始化广播(滑板车) Initialize the broadcast (scooter)
        initBluetooth();//初始化蓝牙 Initialize Bluetooth
        initAnimation();
    }

    /**
     * 初始化蓝牙 Initialize Bluetooth
     */
    private void initBluetooth(){
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        unlock();
    }

    private boolean isOpenBlue(){
        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()){//未开启蓝牙 Bluetooth is not turned on
            return false;
        }else{//已开启蓝牙 Bluetooth turned on
            return true;
        }
    }

    /**
     * 初始化动画类
     */
    private void initAnimation(){
        animation = AnimationUtils.loadAnimation(this,R.anim.refresh);
        LinearInterpolator lil = new LinearInterpolator();//匀速运动
        animation.setInterpolator(lil);
        unLockingImg.startAnimation(animation);
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    debLocking();
                    break;
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//按下系统返回键键监听 Press the system back button to listen
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if(isOpen){
                CommonUtils.hintDialog(this,getString(R.string.do_not_return));
            }else{
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.title_left_layout://返回事件 Return event
                if(isOpen){
                    CommonUtils.hintDialog(this,getString(R.string.do_not_return));
                }else {
                    finish();
                }
                break;
        }
    }

    /**
     * Whether to open the Bluetooth prompt dialog
     * 是否打开蓝牙提示对话框
     * @param tcpConnected 0：未联网 1：联网 0: Not connected to the network 1: Networking
     */
    private void isOpenBlueDialog(final String tcpConnected){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(getString(R.string.hint));
        builder.setMessage(getString(R.string.bluetooth_to_sweep_yards));
        builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                Intent enableBTIntent =new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBTIntent, 1);//请求系统开启蓝牙 Request system to turn on Bluetooth
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if ("0".equals(tcpConnected)) {
                    //When the lock is not connected to the server and Bluetooth is not turned on
                    unLockFail();//当锁未连接服务器并且蓝牙未开启
                }
                dialog.dismiss();
            }
        });
        bleDialog = builder.create();
        bleDialog.show();
    }

    /**
     * Turn off the Bluetooth prompt dialog
     * 关闭蓝牙提示对话框
     */
    private void dismissBlueDialog(){
        if (bleDialog != null){
            if (bleDialog.isShowing()){
                bleDialog.dismiss();
            }
            bleDialog = null;
        }
    }

    /**保存数据到本地
     * Save data to local
     * **/
    private void saveSpValue(String key,String value){
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key,value);
        editor.apply();
    }
    /**保存数据到本地(退出登录不清空)
     * Save data to local (logout does not clear)
     * **/
    private void saveExitValue(String key,String value){
        SharedPreferences shared = getSharedPreferences(CommonSharedValues.SAVE_LOGIN,MODE_PRIVATE);
        SharedPreferences.Editor sharedEt = shared.edit();
        sharedEt.putString(key,value);
        sharedEt.apply();
    }

    private void showDialog(String message){
        dismissBlueDialog();
        handler.removeMessages(0);
        if (isPause)return;
        isPause = true;
        unLockingImg.clearAnimation();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.hint));
        builder.setMessage(message);
        builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        errorDialog = builder.create();
        errorDialog.show();
    }

    /**
     * Close the exception prompt dialog
     * 关闭异常提示对话框
     */
    private void dismissErrorDialog(){
        if (errorDialog != null){
            if (errorDialog.isShowing()){
                errorDialog.dismiss();
            }
            errorDialog = null;
        }
    }

    /**
     * Bicycle unlocking (Post)
     * 单车开锁(Post)
     */
    private void unlock(){
        mainService.unLocking(sp.getString(CommonSharedValues.SP_KEY_TOKEN,""),
                number,curLat,curLng,inputNumber,rideUser,1);
    }

    /**
     * Bike unlock (Get)
     * 单车解锁(Get)
     */
    private void debLocking(){
        mainService.debLocking(sp.getString(CommonSharedValues.SP_KEY_TOKEN,""),number,2);
    }

    /**
     * End unlocking (unlocked failure)
     * 结束开锁(开锁失败)
     */
    private void unLockFail(){
        mainService.unLockFail(sp.getString(CommonSharedValues.SP_KEY_TOKEN,""),unLockDateTime,3);
    }

    /**
     * Upload old data
     * 上传旧数据
     */
    private void uploadOldData(int uid,long timestamp,int runTime){
        mainService.uploadOldData(sp.getString(CommonSharedValues.SP_KEY_TOKEN,""),
                number, sp.getString(CommonSharedValues.SP_LOCK_POWER,""),uid,timestamp,runTime,4);
    }

    /**
     * Notify the server to unlock successfully
     * 通知服务器开锁成功
     * @param timestamp
     */
    private void bleUnLockSuccess(long timestamp){
        mainService.bleUnlockSuccess(sp.getString(CommonSharedValues.SP_KEY_TOKEN,""),
                timestamp,number, sp.getString(CommonSharedValues.SP_LOCK_POWER,""),5);
    }

    @Override
    public void onSuccess(Object o, int flag) {
        if (flag == 1){//单车开锁(Post) Bicycle unlocking (Post)
            Gson gson = new Gson();
            JSONObject retJson = (JSONObject) o;
            handlerUnlocking(retJson,gson);
        }else if (flag == 2){//单车解锁(Get) Bike unlock (Get)
            JSONObject retJson = (JSONObject) o;
            handlerDebLocking(retJson);
        }else if (flag == 3){//结束开锁(开锁失败) End unlocking (unlocked failure)
            showDialog(getString(R.string.unlock_failed));
        }else if (flag == 4){//上传旧数据 Upload old data
            JSONObject retJson = (JSONObject) o;
            handlerUploadOldData(retJson);
        }else if (flag == 5){//通知服务器开锁成功 Notify the server to unlock successfully
            String result = (String) o;
            handlerBleUnlockSuccess(result);
        }
    }

    @Override
    public void onFail(Throwable t, int flag) {

    }

    /**
     * Handling the notification server to unlock successfully
     * 处理通知服务器开锁成功
     * @param result
     */
    private void handlerBleUnlockSuccess(String result){
        if ("1".equals(result)){
            if (!isTCP && !isBLE) {
                if (bikeBleConnectType == 2) {
                    sendOpenResponseCommand();//开锁成功回复锁(只限于滑板车) Unlock the lock and return the lock (only for scooters)
                }
                //Save the bicycle number of the current ride and submit the fault.
                saveSpValue(CommonSharedValues.SP_FEEDBACK_NUMBER, number);//保存当前骑行的单车编号，提交故障用到
                isUploadUnlockSuccess = true;
                isBLE = true;
                LogUtils.i(TAG,"handlerBleUnlockSuccess: 通知服务器开锁成功");
                sendBroadCastMain("", "");
            }
        }else{
            LogUtils.i(TAG,"handlerBleUnlockSuccess: 通知服务器开锁成功失败！！");
        }
    }

    /**
     * Process upload old data results
     * 处理上传旧数据结果
     * @param retJson
     */
    private void handlerUploadOldData(JSONObject retJson){
        try {
            int code = retJson.getInt("code");
            if (code == 200 || code == 202){
                LogUtils.i(TAG,"handlerUploadOldData: 上传旧数据成功");
                if (bikeBleConnectType == 2) {
                    sendClearOldData();//滑板车清除旧数据 Scooter clears old data
                    unlock();
                }
            }else{
                CommonUtils.onFailure(this,code,TAG);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handling bicycle unlock results
     * 处理单车解锁结果
     */
    private void handlerDebLocking(JSONObject retJson){
        try {
            int code = retJson.getInt("code");
            if (code == 202){//开锁超时 Unlock timeout
                isOpen = false;
                showDialog(getResources().getString(R.string.unlock_failed));
            }else if (code == 30005) {//开锁成功 Unlocked successfully
                if (!isTCP && !isBLE) {
                    //Save the bicycle number of the current ride and submit the fault.
                    saveSpValue(CommonSharedValues.SP_FEEDBACK_NUMBER, number);//保存当前骑行的单车编号，提交故障用到
                    isTCP = true;
                    sendBroadCastMain("","");
                }
            }else if (code == 30014){//锁已经关了(这种情况是刚开锁就立马关锁了) The lock has been closed (this situation is just unlocked immediately after unlocking)
                if (!isTCP && !isBLE) {
                    //Save the bicycle number of the current ride and submit the fault.
                    saveSpValue(CommonSharedValues.SP_FEEDBACK_NUMBER, number);//保存当前骑行的单车编号，提交故障用到
                    isTCP = true;
                    sendBroadCastMain("",unLockDateTime);
                }
            }else if (code == 30010){//正在解锁 Unlocking
                count += 1;
                if (count > 120){
                    showDialog(getResources().getString(R.string.unlock_time_out));
                }else{
                    if (!isPause)handler.sendEmptyMessageDelayed(0, 1000);
                }
            }else{
                CommonUtils.onFailure(this,code,TAG);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handling bicycle unlocking results
     * 处理单车开锁结果
     * @param retJson
     * @param gson
     */
    private void handlerUnlocking(JSONObject retJson,Gson gson){
        try {
            int code = retJson.getInt("code");
            if (code == 200){
                isOpen = true;
                isUse = false;
                JSONObject jsonObject = (JSONObject) retJson.get("data");
                LockBean lockBean = gson.fromJson(jsonObject.toString(), LockBean.class);
                lockPower = lockBean.getPower();
                unLockMac = lockBean.getMac();
                unLockDateTime = lockBean.getDate();
                bikeBleConnectType = Integer.parseInt(lockBean.getBikeType());
                tcpConnected = lockBean.getTcpConnected();
                if ("0".equals(tcpConnected)){
                    saveExitValue(CommonSharedValues.BLE_UNLOCK_FLAG,"1");//纯蓝牙开锁标记 Pure Bluetooth unlock tag
                }
                if (lockBean.getRedpackRule() != null) {
                    //Save the red envelope area ID to the local, prevent the application from being crossed out and log out (only for riding)
                    //保存红包区域ID到本地，防止划掉App和退出登录（只针对骑行中）
                    saveSpValue(CommonSharedValues.SP_RED_BIKE_AREA_ID, lockBean.getRedpackRule().getArea_ids());
                }
                if (lockBean.getCityVo() != null) {
                    boundaryArea = lockBean.getCityVo();
                }
                if ("1".equals(lockBean.getData())) {
                    debLocking();
                    if (TextUtils.isEmpty(rideUser) || isMe(unLockMac)) {
                        //Save the Bluetooth address to the local, prevent the app from being crossed out and log out
                        // (only for the locks that are on the bike and using Bluetooth)
                        //保存蓝牙地址到本地，防止划掉App和退出登录（只针对骑行中并使用蓝牙开的锁）
                        saveSpValue(CommonSharedValues.SP_MAC_ADDRESS, unLockMac);
                        if (!isOpenBlue()) {
                            isOpenBlueDialog(tcpConnected);
                        } else {
                            bleUnlock();
                        }
                    }
                } else if ("0".equals(lockBean.getData())) {
                    showDialog(getString(R.string.bike_numbers_wrong));
                }
            }else if (code == 201) {//缺少参数 Missing parameters
                showDialog(getString(R.string.in_the_location));
            }else if (code == 202) {//单车编号不存在 Bicycle number does not exist
                showDialog(getString(R.string.bike_number_not_exist));
            }else if (code == 20004){//暂停使用单车 Suspension of bicycle use
                showDialog(getString(R.string.suspension_of_cycling));
            }else if (code == 30001){//单车正在使用中 Bicycle is in use
                isUse = true;
                String mac = "";
                String date = "";
                if (retJson.has("mac"))mac = retJson.getString("mac");
                if (retJson.has("date"))date = retJson.getString("date");
                if (retJson.has("power"))lockPower = retJson.getString("power");
                if (retJson.has("bikeType"))bikeBleConnectType = retJson.getInt("bikeType");
                if (!TextUtils.isEmpty(mac) && !TextUtils.isEmpty(date)) {
                    unLockMac = mac;
                    unLockDateTime = date;
                    if (!isOpenBlue()) {
                        isOpenBlueDialog("0");
                    } else {
                        bleUnlock();
                    }
                    LogUtils.i(TAG,"handlerUnlocking: 30001 蓝牙开锁，单车正在使用中，调用连接蓝牙设备方法");
                }else{
                    showDialog(getString(R.string.bicycle_is_in_use));
                }
            }else if (code == 30002){//单车已损坏 Bicycle is damaged
                showDialog(getString(R.string.bicycle_has_been_damaged));
            }else if (code == 30003){//单车已报废 Bicycle has been scrapped
                showDialog(getString(R.string.bicycle_has_been_scrapped));
            }else if (code == 30004){//单车已被预约 Bicycle has been reserved
                showDialog(getString(R.string.bicycle_has_been_reservation));
            }else if (code == 30005){//单车解锁成功 Bicycle unlocked successfully
                Intent intent = new Intent();
                intent.setAction(BroadCastValues.UNLOCKING_SUCCESS);
                intent.putExtra("number",number);
                sendBroadcast(intent);
                finish();
            }else if (code == 30006){//已有预约的单车 Already reserved bicycle
                showDialog(getString(R.string.has_a_bike_of_appointment));
            }else if (code == 30007){//未通过认证 Not certified
                showDialog(getString(R.string.not_through_the_certification));
            }else if (code == 30008){//余额不足 Insufficient balance
                showDialog(getString(R.string.not_sufficient_funds));
            }else if (code == 30009){//有未支付的订单 Have unpaid orders
                showDialog(getString(R.string.unpaid_orders));
            }else if (code == 30011) {//您有在使用其他单车 You are using other bicycles
                showDialog(getString(R.string.you_have_use_other_bike));
            }else if (code == 30013) {//未绑定银行卡 Unbound bank card
                showDialog(getString(R.string.unbound_bank_card));
            }else if (code == 30017) {//多人骑行已达最大数量 The maximum number of people riding has reached the maximum
                showDialog(getString(R.string.unlock_maximum));
            }else if (code == 30018) {//单车掉线未连接 Bicycle dropped is not connected
                showDialog(getString(R.string.bicycle_has_been_offline));
            }else if (code == 30019) {//单车未激活 Bicycle is not activated
                showDialog(getString(R.string.bike_not_activated));
            }else{
                CommonUtils.onFailure(this,code,TAG);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Bluetooth unlock
     * 蓝牙开锁
     */
    private void bleUnlock(){
        if (bikeBleConnectType == 2){//单车 bicycle
            LogUtils.i(TAG, "onResponse: 执行滑板车蓝牙");
            if (scooterService != null){
                if (!isConnectedDevice(unLockMac.toUpperCase())) {//如果蓝牙未连接 If Bluetooth is not connected
                    //Start scanning Bluetooth devices, otherwise you can't connect directly
                    startScanBLEDevice(unLockMac.toUpperCase(), 20000);//开始扫描蓝牙设备，不然直接连连不上
                    LogUtils.i(TAG, "onResponse: 蓝牙未连接,先扫描蓝牙，不然直接连接蓝牙连不上！");
                }else{
                    String uid = sp.getString(CommonSharedValues.SP_KEY_UID,"");
                    sendScooterOpenCommand(Integer.parseInt(uid),Long.parseLong(unLockDateTime));//发送开锁指令 Send unlock command
                    LogUtils.i(TAG, "onResponse: 蓝牙已连接,直接发开锁指令");
                }
            }
        }
    }

    /**
     * Judging whether the second unlock is not your own
     * 判断第二次开锁是不是自己
     * @param mac
     * @return
     */
    private boolean isMe(String mac){
        String saveMac = sp.getString(CommonSharedValues.SP_MAC_ADDRESS,"");
        if (!TextUtils.isEmpty(saveMac)){
            if (saveMac.equals(mac)){
                return true;
            }
        }
        return false;
    }

    private void sendBroadCastMain(String runTime,String timestamp){
        LogUtils.i(TAG,"sendBroadCastMain: 跳转界面成功");
        dismissBlueDialog();
        dismissErrorDialog();
        unLockingImg.clearAnimation();
        Intent intent = new Intent();
        intent.setAction(BroadCastValues.UNLOCKING_SUCCESS);
        intent.putExtra("number", number);
        intent.putExtra("boundaryArea", boundaryArea);
        intent.putExtra("runTime",runTime);
        intent.putExtra("timestamp",timestamp);
        sendBroadcast(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissBlueDialog();
        dismissErrorDialog();
        CommonUtils.remove(this);
        unLockingImg.clearAnimation();
        isPause = true;
        handler.removeMessages(0);
        unRegisterReceiver();//取消广播注册(滑板车) Cancel broadcast registration (scooter)
    }

    /***--------------------滑板车连接蓝牙部分 Scooter connected to the Bluetooth part ------------------------**/

    @Override
    protected void onServiceConnectedCallBack(ScooterService mBLEService) {
        scooterService = mBLEService;
        LogUtils.i(TAG, "onServiceConnectedCallBack: -------初始化绑定服务回调");
    }

    @Override
    protected void onScanBLEDeviceCallBack(BluetoothDevice scanBLEDevice) {
        isOpen = true;
        stopScanBLEDevice();//停止扫描 Stop scanning
        connectScooter(scanBLEDevice.getAddress());//连接滑板车 Connecting scooter
        LogUtils.i(TAG, "onScanBLEDeviceCallBack: ----------扫描蓝牙设备成功连接蓝牙--蓝牙mac地址为："+scanBLEDevice.getAddress()+"---蓝牙名称为："+scanBLEDevice.getName());
    }

    @Override
    protected void onScanBLEDeviceNotCallBack(String deviceAddress) {
        if (!isTCP && !isBLE) {
            //Start scanning Bluetooth devices, otherwise you can't connect directly
            startScanBLEDevice(deviceAddress, 20000);//开始扫描蓝牙设备，不然直接连连不上
            LogUtils.i(TAG, "onScanBLEDeviceNotCallBack: ------扫描蓝牙失败！！！接着扫描");
        }
    }

    @Override
    protected void onBLEWriteNotify() {
        isOpen = true;
        sendGetKeyCommand(CommonSharedValues.BLE_SCOOTER_KEY);
        LogUtils.i(TAG, "onBLEWriteNotify: ----------注册了通知回调，去获取key");
    }

    @Override
    protected void onBLEGetKeyError() {
        //Disconnect the lock, disconnect the Bluetooth callback method will reconnect
        sendDisconnectScooter();//断开锁连接，断开蓝牙回调方法会重连
        LogUtils.i(TAG, "onBLEGetKeyError: -----获取Key失败，先断开蓝牙再扫描连接");
    }

    @Override
    protected void onBLEGetKey(String mac, byte communicationKey) {
        sendGetDeviceInfo();//获取滑板车锁状态信息 Get scooter lock status information
        sendGetScooterInfo();//获取滑板车电量信息 Get scooter battery information
        LogUtils.i(TAG, "onBLEGetKey: --------获取Key成功,去获取锁状态和滑板车电量");
    }

    @Override
    protected void onBLEScooterInfo(int power, int speedMode, int speed, int mileage, int prescientMileage) {
        LogUtils.i(TAG, "onBLEScooterInfo: -----获取到滑板车电量信息 "+power);
        saveSpValue(CommonSharedValues.SP_LOCK_POWER, power+"");
    }

    @Override
    protected void onBLEDeviceInfo(int voltage, int status, String version) {
        LogUtils.i(TAG, "onBLEDeviceInfo: ------获取到锁状态信息");
        if((status&0x40)!=0){//有旧数据 Old data
            sendGetOldData();
            LogUtils.i(TAG, "onBLEDeviceInfo:  -----有旧数据，去获得旧数据");
        }else{//无旧数据 No old data
            if((status&0x01)!=0){//锁是开的 The lock is open
                if (isUse){
                    isOpen = false;
                    showDialog(getString(R.string.bicycle_is_in_use));
                }else{
                    //In this case, TCP is faster than Bluetooth, and it has not jumped to the billing interface.
                    //这种情况是TCP比蓝牙快，而又一直没跳转计费界面，
                    //In order to avoid being stuck in the unlocking interface, uploading the unlocking
                    // directly to the server and successfully jumping to the charging interface
                    // 为避免一直卡在开锁界面，所以直接向服务器上传开锁成功跳转计费界面
                    if ("0".equals(tcpConnected)) {//车子未联网
                        sendScooterCloseCommand();//发送滑板车蓝牙关锁指令 Send scooter Bluetooth lock command
                    }else{
                        if (!isUploadUnlockSuccess) {
                            isUploadUnlockSuccess = true;
                            bleUnLockSuccess(Long.parseLong(unLockDateTime));
                        }
                    }
                }
                LogUtils.i(TAG, "onBLEDeviceInfo:  ------无旧数据，开锁状态，已被使用中");
            }else if ((status&0x02)!=0){//锁是关的 Lock is off
                if (isUse){
                    isOpen = false;
                    showDialog(getString(R.string.bicycle_is_in_use));
                }else {
                    //Multiplayer riding only saves the first Bluetooth unlocked mac address
                    if (TextUtils.isEmpty(rideUser) || isMe(unLockMac)) {//多人骑行只保存第一次蓝牙开锁的mac地址
                        //Save the Bluetooth address to the local, prevent the app from being crossed out and log out
                        // (only for the locks that are on the bike and using Bluetooth)
                        //保存蓝牙地址到本地，防止划掉App和退出登录（只针对骑行中并使用蓝牙开的锁）
                        saveSpValue(CommonSharedValues.SP_MAC_ADDRESS, unLockMac);
                    }
                    String uid = sp.getString(CommonSharedValues.SP_KEY_UID, "");
                    sendScooterOpenCommand(Integer.parseInt(uid), Long.parseLong(unLockDateTime));//发送开锁指令 Send unlock command
                    LogUtils.i(TAG, "onBLEDeviceInfo:  -------无旧数据，关锁状态，发送开锁指令");
                }
            }
        }
    }

    @Override
    protected void onBLEScooterOldData(long timestamp, long openTime, long userId) {
        LogUtils.i(TAG, "onBLEScooterOldData: --------获取到旧数据--"+timestamp+"------dateTime--"+unLockDateTime);
        // 上传旧数据 Upload old data
        uploadOldData(Integer.parseInt(userId+""), timestamp, Integer.parseInt(openTime+""));
    }

    /*@Override
    protected void onBLEScooterOldData(int status) {
        Log.i(TAG, "onBLEScooterOldData: ------- 0 成功 1 失败 清除旧数据成功"+status);
        unlock();
    }*/

    @Override
    protected void onBLEScooterCloseCallBack(int status, long timestamp, long time) {
        LogUtils.i(TAG, "onBLEBicnmanOpenCallBack: ------开锁界面收到蓝牙关锁通知 status = "+status);
        if (status == 1){//关锁成功 Locked successfully
            String uid = sp.getString(CommonSharedValues.SP_KEY_UID,"");
            sendScooterOpenCommand(Integer.parseInt(uid), Long.parseLong(unLockDateTime));//发送开锁指令 Send unlock command
            LogUtils.i(TAG, "onBLEBicnmanOpenCallBack: ------开锁界面关锁成功,重新开锁");
        }else if (status == 2){//关锁失败 Lock failure
            sendScooterCloseCommand();//发送滑板车蓝牙关锁指令 Send scooter Bluetooth lock command
            LogUtils.i(TAG, "onBLEBicnmanOpenCallBack: ------开锁界面关锁失败！！重新发送关锁指令");
        }
    }

    @Override
    protected void onBLEScooterOpenCallBack(int status, long timestamp) {
        if (status == 1){//开锁成功 Unlocked successfully
            if (!isUploadUnlockSuccess) {
                isUploadUnlockSuccess = true;
                bleUnLockSuccess(timestamp);
            }
            LogUtils.i(TAG, "onBLEBicnmanOpenCallBack: ------开锁成功,通知服务器开锁成功");
        }else if (status == 2){//开锁失败 Unlocking failed
            isOpen = false;
            showDialog(getString(R.string.unlock_failed));
            LogUtils.i(TAG, "onBLEBicnmanOpenCallBack: ------开锁失败！！");
        }
    }


    @Override
    protected void onBLEDisconnected() {
        if (bikeBleConnectType == 2 && isOpenBlue()) {
            if (!isTCP && !isBLE) {
                //Start scanning Bluetooth devices, otherwise you can't connect directly
                startScanBLEDevice(unLockMac.toUpperCase(), 20000);//开始扫描蓝牙设备，不然直接连连不上
                LogUtils.i(TAG, "onBLEDisconnected: ----蓝牙断开连接");
            }
        }
    }

    @Override
    protected void onBLECommandError(int status) {
        LogUtils.i(TAG, "onBLECommandError: ------发送某个指令失败，先断开蓝牙再扫描连接" +
                "状态原因是 1：CRC认证错误 2：未获取通信KEY 3：通信KEY错误---status = "+status);
        //Disconnect the lock, disconnect the Bluetooth callback method will reconnect
        sendDisconnectScooter();//断开锁连接，断开蓝牙回调方法会重连
    }

    @Override
    protected void onSystemBLEOpen() {
        LogUtils.i(TAG,"onSystemBLEOpen: ------检测到系统 蓝牙开启");
        dismissBlueDialog();
        if (TextUtils.isEmpty(rideUser) || isMe(unLockMac)) {
            bleUnlock();
        }
    }
}
