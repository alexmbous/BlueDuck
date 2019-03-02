package com.blueduck.ride.login.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blueduck.ride.R;
import com.blueduck.ride.base.BaseActivity;
import com.blueduck.ride.login.adapter.MyPageAdapter;
import com.blueduck.ride.utils.CommonSharedValues;
import com.blueduck.ride.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

public class PageActivity extends BaseActivity implements ViewPager.OnPageChangeListener{

    private static final String TAG = "PageActivity";

    private ViewPager viewPager;
    private TextView pageTopOne,pageTopTwo;
    private LinearLayout bottomDot;
    private RelativeLayout backBtn;
    private Button nextBtn;

    private List<View> views = new ArrayList<>();//导航页适配器数据源 Navigation page adapter data source
    private List<View> dots = new ArrayList<>();//导航圆点数组 Navigation dot array
    private int[] logoArray;
    private String [] pageTopOneArray,pageTopTwoArray;
    private int currentPage = 0;

    @Override
    protected int setLayoutViewId() {
        return R.layout.page_activity;
    }

    @Override
    protected void init() {
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);
    }

    @Override
    protected void initView() {
        pageTopOneArray = getResources().getStringArray(R.array.page_top_one_array);
        pageTopTwoArray = getResources().getStringArray(R.array.page_top_two_array);
        TypedArray ar = getResources().obtainTypedArray(R.array.logo_page_array);
        logoArray = new int[ar.length()];
        for (int i = 0; i < ar.length(); i++) {
            logoArray[i] = ar.getResourceId(i, 0);
        }
        ar.recycle();
        viewPager = (ViewPager) findViewById(R.id.page_view);
        findViewById(R.id.close_btn).setOnClickListener(this);
        pageTopOne = (TextView) findViewById(R.id.page_top_description_one);
        pageTopTwo = (TextView) findViewById(R.id.page_top_description_two);
        bottomDot = (LinearLayout) findViewById(R.id.page_bottom_dot);
        backBtn = (RelativeLayout) findViewById(R.id.page_back_btn);
        backBtn.setOnClickListener(this);
        backBtn.setVisibility(View.GONE);
        nextBtn = (Button) findViewById(R.id.page_next_btn);
        nextBtn.setOnClickListener(this);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(CommonUtils.dip2px(this, 250f),
                CommonUtils.dip2px(this, 250f));
        LinearLayout.LayoutParams dopParams = new LinearLayout.LayoutParams(CommonUtils.dip2px(this, 10f), CommonUtils.dip2px(this, 10f));
        dopParams.setMargins(CommonUtils.dip2px(this, 5f), 0, CommonUtils.dip2px(this, 5f), 0);
        for (int i = 0; i < logoArray.length; i++) {
            dots.add(new ImageView(this));
            bottomDot.addView(dots.get(i), dopParams);
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setGravity(Gravity.CENTER);
            ImageView logoImg = new ImageView(this);
            logoImg.setLayoutParams(logoParams);
            logoImg.setImageResource(logoArray[i]);
            linearLayout.addView(logoImg);
            views.add(linearLayout);
        }
        MyPageAdapter pageAdapter = new MyPageAdapter(views);
        viewPager.addOnPageChangeListener(this);
        viewPager.setAdapter(pageAdapter);
        initDescription(0);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.close_btn:
                endPage();
                break;
            case R.id.page_back_btn:
                previous();
                break;
            case R.id.page_next_btn:
                next();
                break;
        }
    }

    private void previous(){
        if (currentPage > 0){
            viewPager.setCurrentItem(currentPage - 1);
        }
    }

    private void next(){
        if (currentPage < logoArray.length - 1) {
            viewPager.setCurrentItem(currentPage + 1);
        }else if (currentPage == logoArray.length - 1){
            endPage();
        }
    }

    private void endPage(){
        if (isFirstLogin()) {
            SharedPreferences.Editor editor = shared.edit();
            editor.putString(CommonSharedValues.IS_FIRST_LOGIN, "1");
            editor.commit();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            finish();
        }
    }

    private boolean isFirstLogin(){
        String firstLogin = shared.getString(CommonSharedValues.IS_FIRST_LOGIN, "");
        if (TextUtils.isEmpty(firstLogin)){
            return true;
        }
        return false;
    }

    private void setDotBackground(int index){
        for (int i = 0; i < dots.size(); i ++){
            if (i == index){
                dots.get(i).setBackground(ContextCompat.getDrawable(this,R.drawable.solid_bg));
            }else{
                dots.get(i).setBackground(ContextCompat.getDrawable(this,R.drawable.hollow_bg));
            }
        }
    }

    private void initDescription(int position){
        setDotBackground(position);
        pageTopOne.setText(pageTopOneArray[position]);
        pageTopTwo.setText(pageTopTwoArray[position]);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        currentPage = position;
        initDescription(position);
        if (currentPage == 0){
            backBtn.setVisibility(View.GONE);
        }else{
            backBtn.setVisibility(View.VISIBLE);
        }
        if (currentPage == logoArray.length - 1){
            nextBtn.setText(getString(R.string.fly_now));
            nextBtn.setBackground(ContextCompat.getDrawable(this,R.drawable.skip_btn_green_bg));
        }else{
            nextBtn.setText(getString(R.string.next));
            nextBtn.setBackground(ContextCompat.getDrawable(this,R.drawable.skip_btn_bg));
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

}
