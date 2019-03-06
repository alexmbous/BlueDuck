package com.omni.ble.library.utils;





import android.util.Log;

import com.omni.ble.library.model.CommandType;
import com.omni.lib.utils.PrintUtil;

import java.util.Random;

/**
 * <br />
 * created by CxiaoX at 2017/4/22 18:02.
 */

public class CommandUtil extends  BaseCommand{

    private static  final  String TAG="CommandUtil";

    private static byte[] getCommand(byte ckey,byte commandType,byte[] data){
        byte[] head=new byte[]{(byte)0xA3,(byte)0xA4};
        byte len = (byte)data.length; // 数据的长度
        byte rand = (byte) (new Random().nextInt(255) & 0xff);
        byte[] command=addBytes(head,new byte[]{len,rand,ckey,commandType});
        return addBytes(command,data);

    }

    public static byte[] getCRCCarportDown(byte ckey, int uid,long timestamp ){
        byte[] data=new byte[]{0x01};
        data=addBytes(data,uid);
        data=addBytes(data,timestamp);
        data=addBytes(data,new byte[]{0x0});
        byte[] command =getCommand(ckey, CommandType.CONTROL_DOWN,data);
        return  getXorCRCCommand(command);
    }
    public static byte[] getCRCCarportDown(byte ckey, byte mode,int uid,long timestamp ){
        byte[] data=new byte[]{mode};
        data=addBytes(data,uid);
        data=addBytes(data,timestamp);
        data=addBytes(data,new byte[]{0x0});
        byte[] command =getCommand(ckey,CommandType.CONTROL_DOWN,data);
        return  getXorCRCCommand(command);
    }

    public static byte[] getCRCDeviceInfo(byte ckey  ){
        byte[] data=new byte[]{0x01};
        byte[] command =getCommand(ckey,CommandType.CARPORT_DEVICE_INFO,data);
        return  getXorCRCCommand(command);
    }


    public static byte[] getCRCPairRemote(byte ckey, byte[] mac ){
        byte[] data=new byte[]{0x01};
        data=addBytes(data,mac);
        byte[] command =getCommand(ckey,CommandType.PAIR_REMOTE,data);

        Log.i(TAG, "getCRCPairRemote: 车位锁配对遥控器 byte[]="+ PrintUtil.toHexString(command));

        return  getXorCRCCommand(command);
    }



    public static byte[] getCRCCarportDownResponse(byte ckey){
        byte[] data=new byte[]{0x02};
        byte[] command =getCommand(ckey,CommandType.CONTROL_DOWN,data);
        return  getXorCRCCommand(command);
    }

    public static byte[] getCRCCarportUp(byte ckey ){
        byte[] data=new byte[]{0x01};
        byte[] command =getCommand(ckey,CommandType.CONTROL_UP,data);
        return  getXorCRCCommand(command);
    }


    public static byte[] getCRCCarportUpResponse(byte ckey){
        byte[] data=new byte[]{0x02};
        byte[] command =getCommand(ckey,CommandType.CONTROL_UP,data);
        return  getXorCRCCommand(command);
    }

    private static String getCommForHex(byte[] values){
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(int i=0;i<values.length;i++){
            sb.append( String.format("%02X,",values[i]));
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append("]");
        return sb.toString();
    }

    public static byte[] getCRCCommunicationKey(String deviceKey ){
        byte[] data = new byte[8] ;
        for(int i=0;i<deviceKey.length();i++){
            data[i]= (byte) deviceKey.charAt(i);
        }
        byte[] command =getCommand((byte)0,CommandType.COMMUNICATION_KEY,data);
        return  getXorCRCCommand(command);
    }


    public static byte[] getCRCModifyDeviceKey(byte cKey,String deviceKey ){

        byte[] data = new byte[9] ;
        data[0]=1;
        for(int i=0;i<deviceKey.length();i++){
            data[i+1]= (byte) deviceKey.charAt(i);
        }
        byte[] command =getCommand(cKey,CommandType.MODIFY_DEVICE_KEY,data);
        return  getXorCRCCommand(command);
    }

    public static byte[] getCRCClearDeviceKey(byte cKey ){

        byte[] data = new byte[]{0x02} ;

        byte[] command =getCommand(cKey,CommandType.MODIFY_DEVICE_KEY,data);
        return  getXorCRCCommand(command);
    }

    public static byte[] getCRCGetOldData(byte cKey ){

        byte[] data = new byte[]{0x01} ;

        byte[] command =getCommand(cKey,CommandType.OLD_DATA,data);
        return  getXorCRCCommand(command);
    }


    /**
     *
     * @param cKey
     * @param opt 1-关机，2-开机
     * @return
     */
    public static byte[] getCRCShutDown(byte cKey,byte opt ){

        byte[] data = new byte[]{opt} ;

        byte[] command =getCommand(cKey,CommandType.SCOOTER_POWER_CONTROL,data);
        return  getXorCRCCommand(command);
    }

    public static byte[] getCRCShutDown(byte cKey,byte orderType,byte opt ){

        byte[] data = new byte[]{opt} ;

        byte[] command =getCommand(cKey,orderType,data);
        return  getXorCRCCommand(command);
    }

    public static byte[] getCRCClearOldData(byte cKey ){

        byte[] data = new byte[]{0x01} ;

        byte[] command =getCommand(cKey,CommandType.CLEAR_DATA,data);
        return  getXorCRCCommand(command);
    }
    public static byte[] getCRCGetPairInfo(byte cKey ){

        byte[] data = new byte[]{0x01} ;

        byte[] command =getCommand(cKey,CommandType.DEVICE_MAC_HAND_PAIR,data);
        return  getXorCRCCommand(command);
    }

    /**
     * 获取车锁锁 本地MAC 地址
     * @param ckey
     * @return
     */
    public static byte[] getCRCGetLocalMac(byte ckey ){
        byte[] data=new byte[]{0x01};
        byte[] command =getCommand(ckey,CommandType.DEVICE_LOCAL_MAC,data);
        return  getXorCRCCommand(command);
    }

    /**
     * 获取已经配对的MAC地址
     * @param ckey
     * @return
     */
    public static byte[] getCRCHadMacPair (byte ckey ){
        byte[] data=new byte[]{0x01};
        byte[] command =getCommand(ckey,CommandType.DEVICE_MAC_HAND_PAIR,data);
        return  getXorCRCCommand(command);
    }

    public static byte[] getCRCModelCommand(byte ckey,byte model){
        byte[] data=new byte[]{0x01,model};
        byte[] command =getCommand(ckey,CommandType.DEVICE_MODEL,data);
        return  getXorCRCCommand(command);
    }
    public static byte[] getCRCModelResponse(byte ckey){
        byte[] data=new byte[]{0x02};
        byte[] command =getCommand(ckey,CommandType.DEVICE_MODEL,data);
        return  getXorCRCCommand(command);
    }

}
