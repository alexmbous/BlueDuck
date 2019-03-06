package com.blueduck.ride.utils;


/**
 * Public record saved value
 * 公共记录保存的值
 */
public class CommonSharedValues {

    public static final String industryType = "1";//保存的公共产业ID /Saved public industry ID
    public static final String BLE_SCOOTER_KEY = "GAtmiLB5";//scooter ble key
//    public static final String BLE_SCOOTER_KEY = "yOTmK50z";//scooter ble key

    /**通知ID Notification ID **/
    public static final int DOWNLOAD_SERVICE_NOTIFICATION = 1;//版本更新service通知 / Version update service notification
    public static final int PUSH_SERVICE_NOTIFICATION = 3;//google推送service通知 / Google push service notification

    /**亚马逊上传常量值 Amazon upload constant value **/
    public static final String AMAZONS3_ACCESS_KEY = "AKIAIEVPFHBUAI5DBBJA";//s3 access_key_id
    public static final String AMAZONS3_SECRET_KEY = "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";//s3 secret_key
    public static final String AMAZONS3_BUCKET_NAME = "blueduck-app-image";//s3 bucket_name
    public static final String AMAZONS3_IMAGE_PATH_PREFIX = "https://s3.us-east-2.amazonaws.com/blueduck-app-image/";//s3 image_prefix

    /** stripe **/
    public static final String STRIPE_TEST = "pk_test_hf1BQggZKCTgWqwT4aspB2Ne";
    public static final String STRIPE_LIVE = "pk_live_EmISdCUGB57ZladSMxhvi6E4";

    /** Map key **/
    public static final String MAP_KEY = "AIzaSyAt3zBFpuRrd3HdNc_7Bnsqc17Y7QMNeQA";

    public static final String SP_NAME = "save_name";
    public static final String SP_KEY_UID = "uId";
    public static final String SP_KEY_GENDER = "sp_key_gender";
    public static final String SP_KEY_PHONE = "phone";
    public static final String SP_KEY_PASSWORD = "password";//用户密码
    public static final String SP_KEY_IMAGE_URL = "imageUrl";
    public static final String SP_KEY_INFO_FIRSTNAME = "info_firstName";
    public static final String SP_KEY_INFO_LASTNAME = "info_lastName";
    public static final String SP_KEY_INFO_EMAIL = "info_email";
    public static final String SP_KEY_EMAIL_AUTH = "email_auth";
    public static final String SP_KEY_NICKNAME = "nickName";
    public static final String SP_KEY_TOKEN = "token";
    public static final String SP_KEY_AUTHSTATUS = "authStatus";
    public static final String SP_KEY_INDUSTRYID = "industryId";
    public static final String SP_KEY_NUMBER = "number";
    public static final String SP_KEY_LONGITUDE = "longitude";
    public static final String SP_KEY_LATITUDE = "latitude";
    public static final String SP_INVITATION_CODE = "sp_invitation_code";
    public static final String SP_PHONE_CODE = "sp_phone_code";
    public static final String SP_MAC_ADDRESS = "sp_mac_address";
    public static final String SP_RED_BIKE_AREA_ID = "sp_red_bike_area_id";
    public static final String SP_FEEDBACK_LAT = "sp_feedback_lat";
    public static final String SP_FEEDBACK_LNG = "sp_feedback_lng";
    public static final String SP_FEEDBACK_NUMBER = "sp_feedback_number";
    public static final String SP_LOCK_POWER = "sp_lock_power";
    public static final String SP_LOGIN_ACCOUNT_TYPE = "sp_login_account_type";
    public static final String SP_RESERVATION_SUM_TIME = "sp_reservation_sum_time";

    /**保存登录界面数据的名 Save the name of the login interface data **/
    public static final String SAVE_LOGIN = "save_login";
    public static final String PHONE_LOGIN_PHONE_CODE = "phone_login_phone_code";
    public static final String EXIT_LOGIN_ACCOUNT_TYPE = "exit_login_account_type";
    public static final String PHONE_LOGIN_PHONE_NUMBER = "phone_login_phone_number";
    public static final String PHONE_LOGIN_EMAIL = "phone_login_email";
    public static final String IS_FIRST_LOGIN = "is_first_login";
    public static final String EXIT_MAC_ADDRESS = "exit_mac_address";
    public static final String EXIT_AREA_ID = "exit_area_id";
    public static final String GOOGLE_PUSH_TOKEN = "google_push_token";
    public static final String UPDATE_LAT = "update_lat";
    public static final String UPDATE_LNG = "update_lng";
    public static final String BLE_UNLOCK_FLAG = "ble_unlock_flag";

}
