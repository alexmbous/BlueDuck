package com.blueduck.ride.history.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blueduck.ride.R;
import com.blueduck.ride.base.BaseActivity;
import com.blueduck.ride.history.HistoryDetailsDialog;
import com.blueduck.ride.history.adapter.HistoryAdapter;
import com.blueduck.ride.history.bean.HistoryBean;
import com.blueduck.ride.history.service.HistoryService;
import com.blueduck.ride.refresh.PullToRefreshBase;
import com.blueduck.ride.refresh.PullToRefreshListView;
import com.blueduck.ride.report.activity.ReportActivity;
import com.blueduck.ride.utils.CommonSharedValues;
import com.blueduck.ride.utils.CommonUtils;
import com.blueduck.ride.utils.RequestCallBack;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends BaseActivity implements RequestCallBack,PullToRefreshBase.OnRefreshListener2,
        AdapterView.OnItemClickListener,HistoryDetailsDialog.ContactCallBack {

    private static final String TAG = "HistoryActivity";
    private ImageView titleLeftImg;
    private TextView titleText;
    private LinearLayout historyBg,haveHistoryLayout;
    private RelativeLayout noHistoryLayout;

    private PullToRefreshListView listView;
    private List<HistoryBean> list = new ArrayList<>();
    private HistoryAdapter historyAdapter;
    private HistoryService historyService;
    private double lat,lng;

    private int pageNo = 1;//默认页数 The default number of pages
    private boolean isPullUpRefresh = false;//是否上拉刷新 Whether to pull up refresh

    @Override
    protected int setLayoutViewId() {
        return R.layout.history_activity;
    }

    @Override
    protected void init() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//将状态栏设置为透明(android4.4以上才有效)
        historyService = new HistoryService(this,this,TAG);
        lat = getIntent().getDoubleExtra("lat",0);
        lng = getIntent().getDoubleExtra("lng",0);
    }

    @Override
    protected void initView() {
        View statusBar = (View) findViewById(R.id.status_bar_view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //如果当前版本号大于android4.4
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, CommonUtils.getStatusBarHeight(this));
            statusBar.setLayoutParams(params);
        }
        findViewById(R.id.history_left_layout).setOnClickListener(this);
        titleLeftImg = (ImageView) findViewById(R.id.history_left_image);
        titleText = (TextView) findViewById(R.id.history_title_text);
        historyBg = (LinearLayout) findViewById(R.id.history_bg_layout);
        noHistoryLayout = (RelativeLayout) findViewById(R.id.no_history_layout);
        haveHistoryLayout = (LinearLayout) findViewById(R.id.have_history_layout);
        findViewById(R.id.first_ride_btn).setOnClickListener(this);
        listView = (PullToRefreshListView) findViewById(R.id.history_list);
        listView.setOnRefreshListener(this);
        listView.setOnItemClickListener(this);
        getHistory();
    }

    private void handlerNoData(){
        titleLeftImg.setImageResource(R.drawable.menu_white);
        titleText.setTextColor(ContextCompat.getColor(this,R.color.white));
        historyBg.setBackgroundColor(ContextCompat.getColor(this,R.color.main_colors));
        noHistoryLayout.setVisibility(View.VISIBLE);
        haveHistoryLayout.setVisibility(View.GONE);
    }

    private void handlerHaveData(){
        titleLeftImg.setImageResource(R.drawable.menu);
        titleText.setTextColor(ContextCompat.getColor(this,R.color.black));
        historyBg.setBackgroundColor(ContextCompat.getColor(this,R.color.white));
        noHistoryLayout.setVisibility(View.GONE);
        haveHistoryLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.history_left_layout:
                finish();
                break;
            case R.id.first_ride_btn:
                finish();
                break;
        }
    }

    private void getHistory(){
        historyService.getHistory(sp.getString(CommonSharedValues.SP_KEY_TOKEN,""),pageNo,1);
    }

    private void handlerHistory(JSONObject retJson){
        try {
            int code = retJson.getInt("code");
            if (code == 200){
                Gson gson = new Gson();
                JSONArray jsonArray = retJson.getJSONArray("data");
                if (pageNo == 1) list.clear();
                List<HistoryBean> lists = gson.fromJson(jsonArray.toString(),new TypeToken<List<HistoryBean>>(){}.getType());
                if (jsonArray.length() != 0){
                    list.addAll(lists);
                    setAdapter();
                }
                if (list.size() > 0){
                    handlerHaveData();
                }else{
                    handlerNoData();
                }
                if (isPullUpRefresh && jsonArray.length() == 0){
                    pageNo --;
                    listView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);//只支持下拉 Only drop-down is supported
                }
            }else{
                listView.onRefreshComplete();
                if (isPullUpRefresh) pageNo--;
                CommonUtils.onFailure(this, code, TAG);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setAdapter(){
        if (historyAdapter == null){
            historyAdapter = new HistoryAdapter(this,list);
            listView.setAdapter(historyAdapter);
        }else{
            historyAdapter.setList(list);
            historyAdapter.notifyDataSetChanged();
        }
        listView.onRefreshComplete();
        if (list.size() % 10 == 0){
            listView.setMode(PullToRefreshBase.Mode.BOTH);//同时支持上拉下拉 It also supports pull-ups and pull-downs
        }else{
            listView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);//只支持下拉 Only drop-down is supported
        }
    }

    @Override
    public void onSuccess(Object o, int flag) {
        if (flag == 1){
            JSONObject retJson = (JSONObject) o;
            handlerHistory(retJson);
        }
    }

    @Override
    public void onFail(Throwable t, int flag) {

    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {
        isPullUpRefresh = false;
        pageNo = 1;
        getHistory();
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {
        isPullUpRefresh = true;
        pageNo ++;
        getHistory();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        HistoryBean historyBean = (HistoryBean) parent.getItemAtPosition(position);
        new HistoryDetailsDialog(this,this,historyBean,0);
    }

    @Override
    public void contactCall(String number) {
        saveSharedValue(CommonSharedValues.SP_FEEDBACK_NUMBER,number);
        Intent intent = new Intent(this,ReportActivity.class);
        intent.putExtra("lat",lat);
        intent.putExtra("lng",lng);
        startActivity(intent);
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
}
