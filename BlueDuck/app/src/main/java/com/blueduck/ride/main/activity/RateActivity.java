package com.blueduck.ride.main.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.blueduck.ride.R;
import com.blueduck.ride.base.BaseActivity;
import com.blueduck.ride.main.service.MainService;
import com.blueduck.ride.report.service.ReportService;
import com.blueduck.ride.utils.BroadCastValues;
import com.blueduck.ride.utils.CommonSharedValues;
import com.blueduck.ride.utils.ReportDialog;
import com.blueduck.ride.utils.RequestCallBack;

import java.util.List;

public class RateActivity extends BaseActivity implements RequestCallBack {

    private static final String TAG = "RateActivity";

    private ReportService reportService;
    private MainService mainService;
    private TextView scooterNumber;
    private RatingBar ratingBar;
    private EditText issueEt,commentEt;

    private float rating;
    private String rideId,content,issue;
    private List<String> issueList;

    @Override
    protected int setLayoutViewId() {
        return R.layout.rate_activity;
    }

    @Override
    protected void init() {
        reportService = new ReportService(this,this,TAG);
        mainService = new MainService(this,this,TAG);
        rating = getIntent().getFloatExtra("rating",0);
        rideId = getIntent().getStringExtra("rideId");
    }

    @Override
    protected void initView() {
        baseTitleLayout.setVisibility(View.VISIBLE);
        baseTitleText.setText(getString(R.string.rate));
        findViewById(R.id.title_left_layout).setVisibility(View.INVISIBLE);
        scooterNumber = (TextView) findViewById(R.id.rate_scooter_number);
        ratingBar = (RatingBar) findViewById(R.id.rating_bar);
        issueEt = (EditText) findViewById(R.id.issue_edit);
        issueEt.setOnClickListener(this);
        commentEt = (EditText) findViewById(R.id.comment_edit);
        findViewById(R.id.rate_ride_btn).setOnClickListener(this);
        findViewById(R.id.dont_rate_text).setOnClickListener(this);
        setData();
    }

    private void setData(){
        scooterNumber.setText(sp.getString(CommonSharedValues.SP_KEY_NUMBER,""));
        ratingBar.setRating(rating);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.issue_edit:
                showReportDialog();
                break;
            case R.id.rate_ride_btn:
                rateRide();
                break;
            case R.id.dont_rate_text:
                handlerRate();
                break;
        }
    }

    private void showReportDialog(){
        if (issueList != null && issueList.size() > 0) {
            new ReportDialog(this, new ReportDialog.ConfirmBtn() {
                @Override
                public void confirm(int type,String issueStr) {
                    issue = (type + 1)+"";
                    issueEt.setText(issueStr);
                }
            }, issueList, 0);
        }else{
            getReportList();
        }
    }

    private void rateRide(){
        content = commentEt.getText().toString().trim();
        rating = ratingBar.getRating();
        String rat = String.valueOf(rating).substring(0, 1);
        rideEndRate(rat);
    }

    /**
     * Get the type of fault
     * 获得故障类型
     */
    private void getReportList(){
        reportService.getReport(sp.getString(CommonSharedValues.SP_KEY_TOKEN,""),1);
    }

    /**
     * End of cycling evaluation
     * 骑行结束评价
     */
    private void rideEndRate(String rating){
        mainService.rideEndRate(sp.getString(CommonSharedValues.SP_KEY_TOKEN,""),
                rideId,rating,content,scooterNumber.getText().toString(),issue,2);
    }

    private void handlerReportList(List<String> list){
        if (list != null && list.size() > 0){
            //Remove the "All" field from the returned fault list, starting with "All"
            list.remove(0);//移除返回的故障列表中的“All”字段，第一个就是“all”
            issueList = list;
            showReportDialog();
        }
    }

    private void handlerRate(){
        Intent intent = new Intent(BroadCastValues.RATE_OR_DONT_RATE_SUCCESS);
        sendBroadcast(intent);
        finish();
    }

    @Override
    public void onSuccess(Object o, int flag) {
        if (flag == 1){
            List<String> list = (List<String>) o;
            handlerReportList(list);
        }else if (flag == 2){
            handlerRate();
        }
    }

    @Override
    public void onFail(Throwable t, int flag) {

    }
}
