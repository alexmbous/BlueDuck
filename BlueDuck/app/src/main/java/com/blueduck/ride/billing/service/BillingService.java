package com.blueduck.ride.billing.service;

import android.content.Context;

import com.blueduck.ride.R;
import com.blueduck.ride.base.BaseService;
import com.blueduck.ride.billing.bean.CardBean;
import com.blueduck.ride.utils.CommonUtils;
import com.blueduck.ride.utils.LogUtils;
import com.blueduck.ride.utils.RequestCallBack;
import com.blueduck.ride.utils.RequestDialog;
import com.blueduck.ride.utils.RetrofitHttp;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BillingService extends BaseService {

    public BillingService(Context context, RequestCallBack callBack, String TAG) {
        super(context, callBack, TAG);
    }

    /**
     * Get card list
     * 获得卡列表
     * @param flag
     */
    public void getCardList(String token,final int flag){
        RequestDialog.show(mContext);
        RetrofitHttp.getRetrofit(0).create(BillingApi.class)
                .getCardList(BillingParameter.cardListParameter(token))
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        RequestDialog.dismiss(mContext);
                        JSONObject jsonObject = response.body();
                        if (jsonObject != null){
                            LogUtils.i(TAG,"获得卡列表 : "+jsonObject.toString());
                            Gson gson = new Gson();
                            try {
                                int code = jsonObject.getInt("code");
                                if (code == 200) {
                                    JSONObject json = (JSONObject) jsonObject.get("data");
                                    CardBean cardBean = gson.fromJson(json.toString(), CardBean.class);
                                    callBack.onSuccess(cardBean, flag);
                                }else if (code == 202){//无数据 no data
                                    callBack.onFail(null,flag);
                                }else{
                                    CommonUtils.onFailure(mContext, code, TAG);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }else{
                            CommonUtils.onFailure(mContext, 500, TAG);
                        }
                    }

                    @Override
                    public void onFailure(Call<JSONObject> call, Throwable t) {
                        RequestDialog.dismiss(mContext);
                        CommonUtils.serviceError(mContext,t);
                    }
                });
    }

    /**
     * 设置默认或者删除银行卡
     * Set default or delete bank card
     * @param token
     * @param cardId
     * @param type
     * @param flag
     */
    public void setDefaultAndDeleteCard(String token, String cardId, int type, final int flag){
        RequestDialog.show(mContext);
        RetrofitHttp.getRetrofit(0).create(BillingApi.class)
                .setDefaultAndDeleteCard(BillingParameter.setDefaultAndDeleteCard(token,cardId,type))
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        RequestDialog.dismiss(mContext);
                        JSONObject retJson = response.body();
                        if (retJson != null){
                            LogUtils.i(TAG, "设置默认或者删除银行卡 : " + retJson.toString());
                            try {
                                int code = retJson.getInt("code");
                                if (code == 200){
                                    String result = retJson.getString("data");
                                    if ("1".equals(result)){
                                        callBack.onSuccess(result,flag);
                                    }else{
                                        CommonUtils.hintDialog(mContext,mContext.getString(R.string.operation_failed));
                                    }
                                }else{
                                    CommonUtils.onFailure(mContext,code,TAG);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }else{
                            CommonUtils.onFailure(mContext, 500, TAG);
                        }
                    }

                    @Override
                    public void onFailure(Call<JSONObject> call, Throwable t) {
                        RequestDialog.dismiss(mContext);
                        CommonUtils.serviceError(mContext,t);
                    }
                });
    }
}
