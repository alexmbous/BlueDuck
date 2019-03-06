package com.omni.ble.library.utils;

import android.util.Log;

import com.omni.ble.library.model.CommandType;
import com.omni.lib.utils.CRCUtil;
import com.omni.lib.utils.PrintUtil;

import java.util.Random;

public class BaseCommand {

    private static final String TAG="BaseCommand";


    public static final byte TRANSMISSION_TYPE_UPDATE=0;
    public static final byte TRANSMISSION_TYPE_SYSTEM_CONFIG=1;

    static byte[] addBytes(byte[] a,byte[] b){
        byte[] ans=new byte[a.length+b.length];
        System.arraycopy(a,0,ans,0,a.length);
        System.arraycopy(b,0,ans,a.length,b.length);
        return ans;
    }

    static byte[] addBytes(byte[] a,byte b){
        byte[] ans=new byte[a.length+1];
        System.arraycopy(a,0,ans,0,a.length);
        ans[a.length]=b;
        return ans;
    }

    /**
     *
     * @param a
     * @param b  int类型的指令，将转换成4字节的byte
     * @return
     */
    static byte[] addBytes(byte[] a,int b){
        byte[] bBytes=new byte[]{(byte)((b>>24)&0xFF),(byte)((b>>16)&0xFF),(byte)((b>>8)&0xff),(byte)(b&0xFF)};
        return addBytes(a,bBytes);
    }
    static byte[] addBytes(byte[] a,long b){
        return addBytes(a,(int)b);
    }

    private static byte[] getCommand(byte ckey,byte commandType,byte[] data){
        byte[] head=new byte[]{(byte)0xA3,(byte)0xA4};
        byte len = (byte)data.length; // 数据的长度
        byte rand = (byte) (new Random().nextInt(255) & 0xff);
        byte[] command=addBytes(head,new byte[]{len,rand,ckey,commandType});
        return addBytes(command,data);

    }


    static byte[] getXorCRCCommand(byte[] command){
//        Log.i(TAG, "getXorCRCCommand: 原始"+PrintUtil.getHexString(command));
        byte[] xorCommand=encode(command);
//        Log.i(TAG, "getXorCRCCommand: 加0x32异或后："+PrintUtil.getHexString(xorCommand));
        byte[] crcOrder= CRCByte(xorCommand);
//        Log.i(TAG, "getXorCRCCommand: 加CRC后："+PrintUtil.getHexString(crcOrder));
        return  crcOrder;
    }


    private static byte[] encode(byte[] command){
        byte[] xorComm = new byte[command.length];
        xorComm[0]=command[0];
        xorComm[1]=command[1];
        xorComm[2]=command[2];
        xorComm[3] =(byte) (command[3] +0x32);
        // 异或随机数后面的到CRC之前的数字
        for(int i=4;i<command.length;i++){
            xorComm[i]= (byte) (command[i] ^ command[3]);
        }
        return xorComm;
    }


    private static byte[] CRCByte(byte[] ori){
        byte[] ret = new byte[ori.length+1];
        int crc8 = com.omni.lib.utils.CRCUtil.calcCRC8(ori);
        for(int i=0;i<ori.length;i++) ret[i]=ori[i];
        ret[ori.length]= (byte) (crc8&0xFF);
        return ret;
    }

    static byte[] CRCByte2(byte[] ori){
        byte[] ret = new byte[ori.length+1];
        int crc = com.omni.lib.utils.CRCUtil.calcCRC8(ori);
        ret[0]= (byte) (crc &0xFF);
        for(int i=0;i<ori.length;i++) ret[i+1]=ori[i];
        return ret;
    }

    /**
     * 请求 车位锁 固件信息
     * @param ckey
     * @return
     */
    public static byte[] getCRCFirmwareInfo(byte ckey){

        byte[] head=new byte[]{(byte)0xA3,(byte)0xA4};
        byte len = 0; // 数据的长度
        byte rand = (byte) (new Random().nextInt(255) & 0xff);
        byte[] command=addBytes(head,new byte[]{len,rand,ckey, CommandType.DEVICE_INFO});
        Log.i(TAG, "getCRCFirmwareInfo: "+ PrintUtil.toHexString(command));
        return getXorCRCCommand(command);
    }

    /**
     * 获取固件打印信息
     * @param ckey 蓝牙通信KEY
     * @return
     */
    public static byte[] getCRCFirmwarLogInfo(byte ckey,byte deviceType){
        byte[] command = getCommand(ckey,CommandType.LOG_DATA,new byte[]{deviceType});
        return getXorCRCCommand(command);
    }

    /**
     * 获取 固件信息中的第几包数据
     * @param ckey
     * @param nPack
     * @param deviceType
     * @return
     */
    public static byte[] getCRCFirmwareInfoDetail(byte ckey,int nPack,byte deviceType){
        byte[] data=new byte[]{(byte)((nPack>>8)&0x00FF),(byte)(nPack&0x00FF),deviceType};
        byte[] command=getCommand(ckey,CommandType.GET_FIRMWARE_DATA,data);
        Log.i(TAG, "getCRCFirmwareInfoDetail: data[]="+PrintUtil.toHexString(command));
        return getXorCRCCommand(command);
    }

    /**
     * 启动 数据传输指令（升级数据传输），发送成功后，设备会问APP要每一包升级数据的
     * @param cKey
     * @param nPack
     * @param crc
     * @param deviceType
     * @param updateKey
     * @return
     */
    public static byte[] getCRCUpdateFirmwareCommand(byte cKey,int nPack,int crc,byte deviceType,String updateKey){

        byte[] updateKeyBytes = updateKey.getBytes();
        byte[] data=new byte[]{(byte)0,(byte)((nPack>>8)&0x00FF),(byte)(nPack&0x00FF),(byte)((crc>>8)&0x00FF),(byte)(crc&0x00FF),deviceType};
        data = addBytes(data,updateKeyBytes);
        byte[] command=getCommand(cKey,CommandType.DEVICE_DATA,data);
        Log.i(TAG, "getCRCUpdateFirmwareCommand: 升级指令");
        Log.i(TAG, "getCRCUpdateFirmwareCommand: byte[]="+PrintUtil.toHexString(command));
        return getXorCRCCommand(command);
    }

    public static byte[] getCRCSystemConfigCommand(byte cKey,int nPack,int crc,byte deviceType,String updateKey){
        byte[] command=getCRCUpdateTransmissionCommand(TRANSMISSION_TYPE_SYSTEM_CONFIG,cKey,nPack,crc,deviceType,updateKey);
        return command;
    }




    public static byte[] getCRCUpdateTransmissionCommand(byte transmissionType,byte cKey,int nPack,int crc,byte deviceType,String updateKey){
        byte[] updateKeyBytes = updateKey.getBytes();
        byte[] data=new byte[]{ transmissionType,(byte)((nPack>>8)&0x00FF),(byte)(nPack&0x00FF),(byte)((crc>>8)&0x00FF),(byte)(crc&0x00FF),deviceType};
        data = addBytes(data,updateKeyBytes);
        byte[] command=getCommand(cKey,CommandType.DEVICE_DATA,data);
        if(transmissionType==(byte)1){
            Log.i(TAG, "getCRCUpdateTransmissionCommand: 数据传输指令");
            Log.i(TAG, "getCRCUpdateTransmissionCommand: byte[]="+PrintUtil.toHexString(command));
        }
        return getXorCRCCommand(command);
    }


    public static byte[] getCRCTransmissionData( int nPack,byte[] data){
        byte[] pack=new byte[]{(byte)((nPack>>8)&0xFF) ,(byte)(nPack&0xFF)};
        byte[] ct=addBytes(pack,data);
        int crc16= CRCUtil.calcCRC16(ct);

        byte[] crc16Byte=new byte[]{(byte)((crc16>>8)&0xFF) ,(byte)(crc16&0xFF)};
        return addBytes(crc16Byte,ct);
    }
}
