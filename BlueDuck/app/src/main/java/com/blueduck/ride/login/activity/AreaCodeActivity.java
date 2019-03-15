package com.blueduck.ride.login.activity;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.blueduck.ride.R;
import com.blueduck.ride.base.BaseActivity;
import com.blueduck.ride.login.adapter.RegionsAdapter;
import com.blueduck.ride.login.bean.RegionsBean;
import com.blueduck.ride.login.service.LoginService;
import com.blueduck.ride.utils.RequestCallBack;

import java.util.List;

public class AreaCodeActivity extends BaseActivity implements RequestCallBack,AdapterView.OnItemClickListener{

    private static final String TAG = "AreaCodeActivity";

    private LoginService loginService;
    private ListView regionsList;
    private RegionsAdapter regionsAdapter;


    @Override
    protected int setLayoutViewId() {
        return R.layout.activity_area_code;
    }

    @Override
    protected void init() {
        loginService = new LoginService(this,this,TAG);
    }

    @Override
    protected void initView() {
        baseTitleLayout.setVisibility(View.VISIBLE);
        baseTitleText.setText(getString(R.string.countries_and_regions));
        regionsList = (ListView) findViewById(R.id.regions_list);
        regionsList.setOnItemClickListener(this);
        requestRegions();
    }

    private void requestRegions(){
        loginService.getRegions(1);
    }

    private void setAdapter(List<RegionsBean> regionsBeanList){
        if (regionsAdapter == null){
            regionsAdapter = new RegionsAdapter(this,regionsBeanList, shared);
            regionsList.setAdapter(regionsAdapter);
        }
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Intent intent = new Intent();
        RegionsBean regionsBean = (RegionsBean) adapterView.getItemAtPosition(position);
        intent.putExtra("regionsBean",regionsBean);
        setResult(RESULT_OK,intent);
        finish();
    }

    private void handlerRegions(List<RegionsBean> list){
        if (list != null && list.size() > 0) {
            setAdapter(list);
        }
    }

    @Override
    public void onSuccess(Object o, int flag) {
        if (flag == 1){
            List<RegionsBean> list = (List<RegionsBean>) o;
            handlerRegions(list);
        }
    }

    @Override
    public void onFail(Throwable t, int flag) {

    }
}
