package com.omni.ble.library.utils;

import android.util.Log;

import com.omni.lib.utils.CRCUtil;
import com.omni.lib.utils.PrintUtil;

import java.util.Random;

/**
 * 蓝牙指令
 * Created by lenovo on 2018/3/26.
 */

public class OGBL2Command {
    private static final String TAG=OGBL2Command.class.getSimpleName();

    public  static  final  byte ORDER_GET_KEY=0x11;
    public  static  final  byte ORDER_UN_LOCK=0x21;
    public  static  final  byte ORDER_LOCK_CLOSE=0x22;
    public  static  final  byte ORDER_INFO=0x31;

    public static byte[] getCRCKeyCommand( String deviceKey){
        byte[] command = getKeyCommand(  deviceKey);
        Log.i(TAG, "getCRCKeyCommand: 原指令="+ PrintUtil.toHexString(command));
        return getXorCRCCommand(command);
    }

    public static byte[] getCRCOpenCommand(int uid,byte bleKey,long timestamp){
        byte[] command = getOpenCommand( uid, bleKey,timestamp);
        return getXorCRCCommand(command);
    }

    public static byte[] getCRCInfoCommand( byte bleKey){
        byte[] command = getCommand(  bleKey,ORDER_INFO,(byte)0x00);
        return getXorCRCCommand(command);
    }

    public static byte[] getCRCShutDown(byte bleKey){
        byte[] command = getCommand(  bleKey,(byte)0x90,(byte)0x00);
        return getXorCRCCommand(command);
    }

    /**
     * 给锁的回复信息， 收到了锁发送到app的上锁指令
     * @param bleKey
     * @return
     */
    private static byte[]   getLockCommand(byte bleKey){
        return getCommand(bleKey,ORDER_LOCK_CLOSE,(byte)0x00);
    }
    public static byte[] getCRCLockResCommand(byte bleKey){
        byte[] command = getLockCommand(   bleKey );
        return getXorCRCCommand(command);
    }

    public static byte[] getCRCOpenResCommand(byte bleKey){
        byte[] command = getCommand(bleKey,ORDER_UN_LOCK,(byte)0x00);
        return getXorCRCCommand(command);
    }

    /**
     * 获取 通信KEY指令
     * @param deviceKey 设备KEY
     * @return
     */
    private static byte[]   getKeyCommand( String deviceKey){
        byte randKey = (byte) (new Random().nextInt(255) & 0xff);

        byte[] command=new byte[13];
        command[0]= (byte) 0xFE;
        command[1]=(byte) (randKey);
        command[2]=0;
        command[3]=ORDER_GET_KEY;
        command[4]=8;
        for(int i=0;i<deviceKey.length();i++){
            command[5+i]=(byte)  deviceKey.charAt(i);  //
        }
        return  command;
    }




    public static byte[]   getOpenCommand(int uid,byte bleKey,long timestamp){
        byte randKey = (byte) (new Random().nextInt(255) & 0xff);
        byte uidB1=(byte) (( uid>>24)&0xFF);
        byte uidB2=(byte) (( uid>>16)&0xFF);
        byte uidB3=(byte) ( ( uid>>8)&0xFF);
        byte uidB4=(byte) ( uid &0xFF);

        byte time1=(byte) (( timestamp>>24)&0xFF);
        byte time2=(byte) (( timestamp>>16)&0xFF);
        byte time3=(byte) ( ( timestamp>>8)&0xFF);
        byte time4=(byte) ( timestamp &0xFF);
        byte[] command=new byte[13];// 不包含CRC校验的长度
        command[0]= (byte) 0xFE;
        command[1]=(byte) (randKey); //随机数 x1

        command[2]= bleKey;  // key
        command[3]= ORDER_UN_LOCK;  // 命令代码
        command[4]= 0x08;  // 长度
        command[5]= uidB1; // 用户id
        command[6]= uidB2;
        command[7]= uidB3;
        command[8]= uidB4;
        command[9]= time1; // timestamp
        command[10]= time2;
        command[11]= time3;
        command[12]= time4;
        return command ;
    }





    private static byte[]   getCommand(byte bleKey,byte order,byte len){
        byte randKey = (byte) (new Random().nextInt(255) & 0xff);
        byte[] command=new byte[5];// 不包含CRC校验的长度
        command[0]= (byte) 0xFE;
        command[1]=(byte) (randKey); //随机数 x1
        command[2]= bleKey;  // key
        command[3]= order;  // 命令代码
        command[4]= len;  // 长度
        return command;
    }




    private static byte[] getXorCRCCommand(byte[] command){
//        Log.i(TAG, "getXorCRCCommand: 原始"+PrintUtil.getHexString(command));
        byte[] xorCommand=encode(command);
//        Log.i(TAG, "getXorCRCCommand: 加0x32异或后："+PrintUtil.getHexString(xorCommand));
        byte[] crcOrder= CRC16Byte(xorCommand);
//        Log.i(TAG, "getXorCRCCommand: 加CRC后："+PrintUtil.getHexString(crcOrder));
        return  crcOrder;
    }

    private static byte[] encode(byte[] command){
        byte[] xorComm = new byte[command.length];
        xorComm[0]=command[0];
        xorComm[1] =(byte) (command[1] +0x32);
        for(int i=2;i<command.length;i++){
            xorComm[i]= (byte) (command[i] ^ command[1]);
        }
        return xorComm;
    }

    private static byte[] CRC16Byte(byte[] ori){
        byte[] ret = new byte[ori.length+2];
        int crc = CRCUtil.calcCRC16(ori);
        for(int i=0;i<ori.length;i++) ret[i]=ori[i];
        ret[ori.length]= (byte) ((crc>>8)&0xFF);
        ret[ori.length+1]=(byte)( crc &0xFF);
        return ret;
    }
}
