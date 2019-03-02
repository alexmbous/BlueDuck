package com.blueduck.ride.report.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.blueduck.ride.R;
import com.blueduck.ride.base.BaseActivity;
import com.blueduck.ride.qrcode.ScannerActivity;
import com.blueduck.ride.report.service.ReportService;
import com.blueduck.ride.utils.BroadCastValues;
import com.blueduck.ride.utils.CommonSharedValues;
import com.blueduck.ride.utils.CommonUtils;
import com.blueduck.ride.utils.ReportDialog;
import com.blueduck.ride.utils.RequestCallBack;

import java.util.List;

public class ReportActivity extends BaseActivity implements RequestCallBack {

    private static final String TAG = "ReportActivity";

    private ReportService reportService;
    private ImageView backImg;
    private EditText numberEt,contentEt,issueTypeEt;
    private ReportBroad reportBroad;
    private String number,content,issue;
    private double lat,lng;

    private List<String> issueList;

    @Override
    protected int setLayoutViewId() {
        return R.layout.report_activity;
    }

    @Override
    protected void init() {
        lat = getIntent().getDoubleExtra("lat",0);
        lng = getIntent().getDoubleExtra("lng",0);
        reportService = new ReportService(this,this,TAG);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadCastValues.SCAN_SUCCESS);
        reportBroad = new ReportBroad();
        registerReceiver(reportBroad,intentFilter);
    }

    @Override
    protected void initView() {
        baseTitleLayout.setVisibility(View.VISIBLE);
        baseTitleText.setText(getString(R.string.report_title));
        backImg = (ImageView) findViewById(R.id.title_left_image);
        backImg.setImageResource(R.drawable.menu);
        numberEt = (EditText) findViewById(R.id.report_scooter_number);
        findViewById(R.id.report_scan_image).setOnClickListener(this);
        issueTypeEt = (EditText) findViewById(R.id.issue_type_text);
        issueTypeEt.setOnClickListener(this);
        contentEt = (EditText) findViewById(R.id.report_content_edit);
        findViewById(R.id.report_duck_btn).setOnClickListener(this);
        number = sp.getString(CommonSharedValues.SP_FEEDBACK_NUMBER,"");
        if (!TextUtils.isEmpty(number)) setNumber();
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.report_scan_image:
                getCameraPermission();
                break;
            case R.id.issue_type_text:
                showReportDialog();
                break;
            case R.id.report_duck_btn:
                reportDuckBottom();
                break;
        }
    }

    private void reportDuckBottom(){
        number = numberEt.getText().toString().trim();
        content = contentEt.getText().toString().trim();
        if (TextUtils.isEmpty(number)){
            Toast.makeText(this,getString(R.string.issue_edit_hint),Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(issue)){
            Toast.makeText(this,getString(R.string.please_select_the_issue),Toast.LENGTH_SHORT).show();
        }else{
            submitReport();
        }
    }

    private void showReportDialog(){
        if (issueList != null && issueList.size() > 0) {
            new ReportDialog(this, new ReportDialog.ConfirmBtn() {
                @Override
                public void confirm(int type,String issueStr) {
                    issue = (type + 1)+"";
                    issueTypeEt.setText(issueStr);
                }
            }, issueList, 0);
        }else{
            getReportList();
        }
    }

    private void getCameraPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String []{Manifest.permission.CAMERA},1);
        }else{
            startScan();
        }
    }

    private void startScan(){
        Intent data = new Intent(this, ScannerActivity.class);
        data.putExtra("isIssue",true);
        startActivity(data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startScan();
            }else{
                CommonUtils.hintDialog(this, getString(R.string.not_camera_permission));
            }
        }
    }

    private void getReportList(){
        reportService.getReport(sp.getString(CommonSharedValues.SP_KEY_TOKEN,""),1);
    }

    private void submitReport(){
        reportService.submitReport(sp.getString(CommonSharedValues.SP_KEY_TOKEN,""),number,issue,lat,lng,content,2);
    }

    private void handlerReportList(List<String> list){
        if (list != null && list.size() > 0){
            //Remove the "All" field from the returned fault list, starting with "All"
            list.remove(0);//移除返回的故障列表中的“All”字段，第一个就是“all”
            issueList = list;
            showReportDialog();
        }
    }

    private void handlerReport(){
        sendBroadcast(new Intent(BroadCastValues.REPORT_SUCCESS_BROAD));
        finish();
    }

    @Override
    public void onSuccess(Object o, int flag) {
        if (flag == 1){
            List<String> list = (List<String>) o;
            handlerReportList(list);
        }else if (flag == 2){
            handlerReport();
        }
    }

    @Override
    public void onFail(Throwable t, int flag) {

    }

    private class ReportBroad extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if (BroadCastValues.SCAN_SUCCESS.equals(intent.getAction())){
                number = intent.getStringExtra("number");
                setNumber();
            }
        }
    }

    private void setNumber(){
        numberEt.setText(number);
        numberEt.setSelection(number.length());
        numberEt.requestFocus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(reportBroad);
    }
}
