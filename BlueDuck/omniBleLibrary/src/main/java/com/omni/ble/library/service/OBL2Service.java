package com.omni.ble.library.service;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.omni.ble.library.utils.OGBL1Command;
import com.omni.ble.library.utils.OGBL2Command;
import com.omni.lib.utils.CRCUtil;
import com.omni.lib.utils.PrintUtil;

import java.util.UUID;

/**
 * 单车纯蓝牙锁(新板) 操作的service
 * Created by lenovo on 2018/4/19.
 */

public class OBL2Service extends BaseService {
    private static final String TAG="OBL2Service";


    public static final String ACTION_OGBL2_OPEN="com.omni.ble.library.ACTION_OGBL2_OPEN";
    public static final String ACTION_OGBL2_CLOSE="com.omni.ble.library.ACTION_OGBL2_CLOSE";
    public static final String ACTION_OBL2_INFO="com.omni.ble.library.ACTION_OBL2_INFO";

    @Override
    public UUID getServiceUUID() {
        return UUID.fromString("6E400001-E6AC-A7E7-B1B3-E699BAE8D000");
    }

    @Override
    public UUID getWriteUUID() {
        return UUID.fromString("6E400002-E6AC-A7E7-B1B3-E699BAE8D000");
    }

    @Override
    public UUID getNotifyUUID() {
        return UUID.fromString("6E400003-E6AC-A7E7-B1B3-E699BAE8D000");
    }

    @Override
    public void onCharacteristicChangedCallback(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {


        String deviceAddress = gatt.getDevice().getAddress();
        final byte[] values=characteristic.getValue();

        Log.i(TAG, "onCharacteristicChangedCallback: "+ PrintUtil.toHexString(values));

        if(values.length==2 && values[0]==32&& values[1]==0) return ;
        byte[] command = new byte[values.length-2];
        for(int i=0;i<command.length;i++) command[i] = values[i];
        int calcCRC = CRCUtil.calcCRC16(command);
        int valueCRC =((values[values.length-2]&0xFF)<<8) | (values[values.length-1]&0xFF);



        if(calcCRC== valueCRC){
            Log.i(TAG, "onCharacteristicChanged: CRC success");
            byte head = (byte) (values[1]-0x32);
            command[1]=head;
            for(int i=2;i<values.length-2;i++){
                command[i] = (byte) (values[i] ^ head);
            }
            onHandNotifyCommand(deviceAddress,command);
        }else{
            Log.i(TAG, "onCharacteristicChanged: CRC fail");
        }
    }

    @Override
    public void onHandNotifyCommand(String mac, byte[] command) {
        Log.i(TAG, "onHandNotifyCommand data[]= "+ PrintUtil.toHexString(command));
        switch (command[3]){
            case OGBL1Command.ORDER_GET_KEY:
                // get key
                handGetKeyCommand(mac,command);
                break;
            case OGBL1Command.ORDER_UN_LOCK:
                handOpenCommand(command);
                break;
            case OGBL1Command.ORDER_LOCK_CLOSE:
                // lock
                handLockCloseCommand(command);
                break;
            case OGBL1Command.ORDER_INFO:
                handLockInfoCommand(command);
                break;
        }

    }


    /**
     * 开关锁状态，0-开锁状态，1-关锁状态
     */
    public static final String EXTRA_LOCK_STATUS="status";
    public static final String EXTRA_LOCK_POWER="power";
    /**
     * 是否有旧数据，1没有，0有旧数据
     */
    public static final String EXTRA_LOCK_OLD="old";
    public static final String EXTRA_LOCK_INCO_TIMESTAMP="info_timestamp";

    public void handLockInfoCommand(byte[] command){
        //0-开锁，1-关锁
        int lockStatus= command[5];
        int power = command[6];
        // 1没有，0有
        int old = command[7];
        long timestamp =  ((command[8]&0xFF)<<24) | ((command[9]&0xFF)<<16) |((command[10]&0xFF)<<8) | (command[11] &0xFF);


        Intent intent = new Intent(ACTION_OBL2_INFO);
        intent.putExtra(EXTRA_LOCK_STATUS,lockStatus);
        intent.putExtra(EXTRA_LOCK_POWER,power);
        intent.putExtra(EXTRA_LOCK_OLD,old);
        intent.putExtra(EXTRA_LOCK_INCO_TIMESTAMP,timestamp);
        sendLocalBroadcast(intent);
    }

    public void handGetKeyCommand(String mac,byte[] command){
       mBLECommunicationKey= command[5];
        Intent intent = new Intent(ACTION_BLE_OPT_GET_KEY_WITH_MAC);
        intent.putExtra("mac",mac);
        intent.putExtra("ckey",mBLECommunicationKey);
        sendLocalBroadcast(intent);

    }

    public void handOpenCommand(byte[] command){

        // 回复 0x21 指令
        sendResOpenCommand();

        int status= command[5];
        long timestamp = ((command[6]&0xFF)<<24) | ((command[7]&0xFF)<<16) |((command[8]&0xFF)<<8) | (command[9] &0xFF);

        Intent intent = new Intent(ACTION_OGBL2_OPEN);
        intent.putExtra("status",status);
        intent.putExtra("timestamp",timestamp);

        sendLocalBroadcast(intent);



    }

    public void handLockCloseCommand(byte[] command){
        int status = command[5];
        long openTimestamp = ((command[6]&0xFF)<<24) | ((command[7]&0xFF)<<16) |((command[8]&0xFF)<<8) | (command[9] &0xFF);
        long openTime = ((command[10]&0xFF)<<24) | ((command[11]&0xFF)<<16) |((command[12]&0xFF)<<8) | (command[13] &0xFF);

        Intent intent = new Intent(ACTION_OGBL2_CLOSE);
        intent.putExtra("status",status);
        intent.putExtra("openTimestamp",openTimestamp);
        intent.putExtra("openTime",openTime);

        sendLocalBroadcast(intent);

        // 关锁回应
        byte[] closeResp = OGBL2Command.getCRCLockResCommand(mBLECommunicationKey);
        writeToDevice(closeResp);


    }

    public byte[] sendGetKeyCommand( String deviceKey){
        byte[] crcOrder= OGBL2Command.getCRCKeyCommand( deviceKey);
        Log.i(TAG, "sendGetKeyCommand: 发送的指令"+PrintUtil.toHexString(crcOrder));
        return writeToDevice(crcOrder);

    }

    public byte[] sendOpenCommand(int uid,long timestamp){
        byte[] crcOrder= OGBL2Command.getCRCOpenCommand(uid,mBLECommunicationKey,timestamp);
        return writeToDevice(crcOrder);
    }

    public  byte[] sendShutDown(){
        byte[] command = OGBL2Command.getCRCShutDown(mBLECommunicationKey);
        return writeToDevice(command);
    }

    public byte[] sendResOpenCommand(){
        byte[] crcOrder= OGBL2Command.getCRCOpenResCommand( mBLECommunicationKey);
        return writeToDevice(crcOrder);
    }

    public byte[] sendGetLockInfoCommand( ){
        byte[] crcOrder= OGBL2Command.getCRCInfoCommand( mBLECommunicationKey);
        return writeToDevice(crcOrder);

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new  LocalBinder();

    public class LocalBinder extends Binder {
        public OBL2Service getService(){
            return OBL2Service.this;
        }
    }
}
