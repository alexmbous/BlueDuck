package com.blueduck.ride.utils;

public class BroadCastValues {

    /**
     * 开锁成功
     * Unlocked successfully
     * **/
    public static final String UNLOCKING_SUCCESS = "unlocking_success";
    /**
     * Map initialization to get nearby bicycle broadcasts
     * 地图初始化获取附近单车广播
     * **/
    public static final String MAP_INIT_GET_BIKE = "map_init_get_bike";
    /**
     * Map mobile end to send latitude and longitude broadcast
     * 地图移动结束发送经纬度广播
     * **/
    public static final String MAP_MOVE_END = "map_move_end";
    /**
     * Add save bank card successfully broadcast
     * 添加保存银行卡成功广播
     * **/
    public static final String SAVE_CARD_SUCCESS = "save_card_success";
    /**
     * Balance prepaid successfully broadcast
     * 余额充值支付成功广播
     * **/
    public static final String RECHARGE_PAY_SUCCESS = "recharge_pay_success";
    /**
     * User feedback scans the bicycle number successfully
     * 用户反馈扫描单车编号成功
     * **/
    public static final String SCAN_SUCCESS = "scan_success";
    /**
     * Modify user information broadcast
     * 修改用户信息广播
     * **/
    public static final String UPDATE_USER_INFO = "update_user_info";
    /**
     * Google message push broadcast (offsite login)
     * google消息推送广播(异地登录)
     * **/
    public static final String GOOGLE_PUSH_BROADCAST = "google_push_broadcast";
    /**
     * Log in to the remote user and click to confirm the broadcast.
     * 异地登录用户点击确认广播
     * **/
    public static final String REMOTE_LOGIN_BROADCAST = "remote_login_broadcast";
    /**
     * Google message push broadcast
     * (refresh the bicycle use information, for the case of unlocking the un-jumping fee interface)
     * google消息推送广播(刷新单车使用信息，针对关锁了未跳转结费界面的情况)
     * **/
    public static final String REFRESH_BIKE_USE_INFO = "refresh_bike_use_info";
    /**
     * Logout broadcast
     * 退出登录广播
     * **/
    public static final String LOG_OUT = "log_out";
    /**
     * End interface broadcast
     * 结束界面广播
     */
    public static final String FINISH_BROAD = "finish_broad";
    /**
     * End of ride evaluation or no evaluation
     * 骑行结束评价或者不评价
     */
    public static final String RATE_OR_DONT_RATE_SUCCESS = "rate_or_dont_rate_success";
    /**
     * Failure reported successfully broadcast
     * 报告故障成功广播
     */
    public static final String REPORT_SUCCESS_BROAD = "report_success_broad";

}
