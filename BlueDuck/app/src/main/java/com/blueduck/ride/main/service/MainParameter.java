package com.blueduck.ride.main.service;

import android.text.TextUtils;

import com.blueduck.ride.utils.CommonSharedValues;

import java.util.HashMap;
import java.util.Map;

public class MainParameter {

    /**
     * Get user information
     * 获得用户信息
     * @param token
     * @return
     */
    public static Map<String,String> getUserInfoParameter(String token){
        Map<String,String> map = new HashMap<>();
        map.put("requestType", "20008");
        map.put("token", token);
        return map;
    }

    /**
     * Get bicycle use information
     * 获得单车使用信息
     * @param token
     * @param closeTimestamp
     * @param versionCode
     * @return
     */
    public static Map<String,String> getBikeUseInfoParameter(String token,String closeTimestamp,String versionCode){
        Map<String,String> map = new HashMap<>();
        map.put("requestType", "30006");
        map.put("token", token);
        map.put("versionCode", versionCode);//app当前版本号 App current version number
        map.put("moreRide","1");
        if (!TextUtils.isEmpty(closeTimestamp)){
            map.put("data",closeTimestamp);
        }
        return map;
    }

    /**
     * Get a nearby bicycle
     * 获得附近单车
     * @param curLat
     * @param curLng
     * @param targetLat
     * @param targetLng
     * @return
     */
    public static Map<String,String> getBikeParameter(String token, double curLat, double curLng, double targetLat, double targetLng){
        Map<String, String> map = new HashMap<>();
        map.put("industryType", CommonSharedValues.industryType);
        map.put("requestType", "30001");
        map.put("token", token); //TODO: added by Garrett
        map.put("cur_lat",curLat+"");
        map.put("cur_lng",curLng+"");
        map.put("lat", targetLat + "");
        map.put("lng", targetLng + "");
        return map;
    }

    /**
     * Get a bicycle parking area
     * 获得单车停车区域
     * @param token
     * @param curLat
     * @param curLng
     * @param targetLat
     * @param targetLng
     * @param ids
     * @return
     */
    public static Map<String,String> getStopAreaParameter(String token, double curLat, double curLng, double targetLat, double targetLng, final String ids){
        Map<String, String> map = new HashMap<>();
        map.put("requestType", "30014");
        map.put("token", token);
        map.put("cur_lat",curLat+"");
        map.put("cur_lng",curLng+"");
        map.put("lat", targetLat + "");
        map.put("lng", targetLng + "");
        if (!TextUtils.isEmpty(ids)) {
            map.put("ids", ids);
        }
        return map;
    }

    /**
     * Bicycle unlock
     * 单车解锁
     * @param token
     * @param number
     * @return
     */
    public static Map<String,String> debLockingParameter(String token,String number){
        Map<String,String> map = new HashMap<>();
        map.put("requestType","30004");
        map.put("token",token);
        map.put("bikeNumber",number);
        return map;
    }

    /**
     * Upload old data or notify the server to lock
     * 上传旧数据或通知服务器关锁
     * @param token
     * @param number
     * @param power
     * @param uid
     * @param timestamp
     * @param runTime
     * @return
     */
    public static Map<String,String> uploadOldDataParameter(String token,String number,String power,int uid,long timestamp,int runTime){
        Map<String,String> map = new HashMap<>();
        map.put("requestType","30018");
        map.put("token",token);
        map.put("number",number);
        map.put("uid",uid+"");
        map.put("date",timestamp+"");
        map.put("runTime",runTime+"");
        if (!TextUtils.isEmpty(power)){
            map.put("power",power);
        }
        return map;
    }

    /**
     * Scooter network lock
     * 滑板车网络关锁
     * @param token
     * @param date
     * @return
     */
    public static Map<String,String> scooterLockingParameter(String token,String date){
        Map<String, String> map = new HashMap<>();
        map.put("requestType", "30030");
        map.put("token", token);
        map.put("date", date);
        return map;
    }

    /**
     * Upload riding path and riding distance
     * 上传骑行路径与骑行距离
     * @param token
     * @param rideId
     * @param outArea
     * @param orbit
     * @param distance
     * @param lat
     * @param lng
     * @return
     */
    public static Map<String,String> updateRideInfoParameter(String token,String rideId,String outArea,String orbit,double distance,double lat,double lng){
        Map<String, String> map = new HashMap<>();
        map.put("requestType", "30031");
        map.put("token", token);
        map.put("rideId",rideId);
        map.put("outArea",outArea);
        map.put("orbit",orbit);
        map.put("distance",distance+"");
        map.put("lat",lat+"");
        map.put("lng",lng+"");
        return map;
    }

    /**
     * End of cycling evaluation
     * 骑行结束评价
     * @param rideId
     * @param star
     * @param content
     * @return
     */
    public static Map<String,String> rideEndRateParameter(String token,String rideId,String star,String content,String number,String issueType){
        Map<String, String> map = new HashMap<>();
        map.put("requestType", "30032");
        map.put("token", token);
        map.put("rideId", rideId);
        if (!TextUtils.isEmpty(star)) {
            map.put("star", star);
        }
        if (!TextUtils.isEmpty(content)) {
            map.put("content", content);
        }
        if (!TextUtils.isEmpty(issueType)){
            map.put("type", issueType);
            map.put("number", number);
        }
        return map;
    }

    /**
     * Bicycle unlock
     * 单车开锁
     * @param token
     * @param number
     * @param lat
     * @param lng
     * @param inputNumber
     * @param rideUser
     * @return
     */
    public static Map<String,String> unLockingParameter(String token,String number,String lat,String lng,String inputNumber,String rideUser){
        Map<String,String> map = new HashMap<>();
        map.put("requestType","30004");
        map.put("token",token);
        map.put("bikeNumber",number);
        map.put("startLat",lat);
        map.put("startLng",lng);
        if (!TextUtils.isEmpty(inputNumber)) {
            map.put("inputNumber", inputNumber);
        }
        map.put("moreRide","1");
        if (!TextUtils.isEmpty(rideUser)){
            map.put("rideUser",rideUser);
        }
        return map;
    }

    /**
     * Unlocking failed to end unlocking
     * 开锁失败结束开锁
     * @param token
     * @param date
     * @return
     */
    public static Map<String,String> unLockFailParameter(String token,String date){
        Map<String,String> map = new HashMap<>();
        map.put("requestType","30024");
        map.put("token",token);
        map.put("date",date);
        return map;
    }

    /**
     * Notify the server that Bluetooth unlocked successfully
     * 通知服务器蓝牙开锁成功
     * @param token
     * @param timestamp
     * @param number
     * @param power
     * @return
     */
    public static Map<String,String> bleUnlockSuccessParameter(String token,long timestamp,String number,String power){
        Map<String,String> map = new HashMap<>();
        map.put("requestType","30017");
        map.put("token",token);
        map.put("date",timestamp+"");
        map.put("number",number);
        if (!TextUtils.isEmpty(power)){
            map.put("power",power);
        }
        return map;
    }
}
