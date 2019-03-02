package com.blueduck.ride.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import com.blueduck.ride.R;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * 日期选择对话框
 * Date selection dialog box
 */
public class DateDialog extends Dialog implements View.OnClickListener,CycleWheelView.WheelItemSelectedListener{

    public static final int Choose_birthday = 1;
    public static final int Card_date_selection = 2;
    private Context mContext;
    private CycleWheelView left,center,right;
    private List<String> yearList = new ArrayList<>();
    private List<String> monthList = new ArrayList<>();
    private List<String> dayList = new ArrayList<>();
    private int mYear,mMonth,mDay,maxDay,flag,customYear;
    private Calendar ca;

    public interface ConfirmBtn{
        public void confirm(int year, int month, int day);
    }
    private ConfirmBtn confirmBtn;

    public DateDialog(Context context, ConfirmBtn confirmBtn, int year, int month, int day, int maxDay, int flag,int themeResId) {
        super(context, R.style.myDialog);
        this.confirmBtn = confirmBtn;
        this.mContext = context;
        this.mYear = year;
        this.mMonth = month;
        this.mDay = day;
        this.maxDay = maxDay;
        this.flag = flag;
        show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.date_dialog);
        setCanceledOnTouchOutside(true);
        initView();
    }

    private void initView(){
        left = (CycleWheelView) findViewById(R.id.left_list);
        center = (CycleWheelView) findViewById(R.id.center_list);
        center.setOnWheelItemSelectedListener(this);
        right = (CycleWheelView) findViewById(R.id.right_list);
        findViewById(R.id.confirm_btn).setOnClickListener(this);
        setData();
    }

    private void setData(){
        if (flag == Choose_birthday){
            right.setVisibility(View.VISIBLE);
            customYear = mYear;
        }else if (flag == Card_date_selection){
            right.setVisibility(View.GONE);
            customYear = 2119;
        }
        for (int i = 1900; i < customYear + 1; i ++){
            yearList.add(setTimeFormat(i)+"");
        }
        for (int i = 1; i < 13; i ++){
            monthList.add(setTimeFormat(i)+"");
        }
        for (int i = 1; i < maxDay + 1; i ++){
            dayList.add(setTimeFormat(i)+"");
        }
        setCycleWheelViewParameter(left,yearList,mYear-1900);
        setCycleWheelViewParameter(center,monthList,mMonth);
        setCycleWheelViewParameter(right,dayList,mDay-1);
    }

    private String setTimeFormat(int time) {
        String timeStr = "";
        if (time < 10) {
            timeStr = "0" + time;
        } else {
            timeStr = time + "";
        }
        return timeStr;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.confirm_btn:
                confirmBtn.confirm(1900+left.getSelection(),center.getSelection()+1,right.getSelection()+1);
                dismiss();
                break;
        }
    }

    @Override
    public void onItemSelected(int position, String label) {
        selectLinkageData();//选择年份或月份时联动天的数据 Select the year or month when the linkage day data
    }

    /**选择年份或月份时联动天的数据
     * Select the year or month when the linkage day data
     * **/
    private void selectLinkageData(){
        ca = Calendar.getInstance();
        int year =  (1900 + left.getSelection());
        int month = center.getSelection()+1;
        String date = year+"-"+(month < 10 ? "0" + month : month + "");
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM");
        Date de = null;
        try {
            de = sd.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ca.setTime(de);
        mYear = year;
        mMonth = month;
        setDayOfData();
    }

    /**设置时间选择器天数那一栏数据
     * Set the time selector column number of data
     * **/
    private void setDayOfData(){
        dayList.clear();
        int maxDay = ca.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i < maxDay+1; i ++){
            dayList.add(setTimeFormat(i)+"");
        }
        setCycleWheelViewParameter(right,dayList,mDay-1);
    }

    /**
     * 设置垂直循环滚动View参数
     * Set the vertical loop scroll View parameter
     * @param cyclewheelview
     * @param lists
     * @param initSelectIndex
     */
    private void setCycleWheelViewParameter(CycleWheelView cyclewheelview, List<String> lists, int initSelectIndex){
        cyclewheelview.setLabels(lists);//设置数据源 Set the data source
        cyclewheelview.setCycleEnable(true);//设置是否循环滚动 Set whether to cycle through
        cyclewheelview.setSelection(initSelectIndex);//初始化选中数据下标 Initialize the selected data subscript
        cyclewheelview.setLabelColor(Color.parseColor("#C0C0C0"));//未选中字体颜色 Font color not selected
        cyclewheelview.setLabelSelectColor(Color.parseColor("#000000"));//已选中字体颜色 Font color selected
        cyclewheelview.setSolid(Color.parseColor("#00000000"),Color.parseColor("#00000000"));//未选中Item的颜色/已选中Item的颜色 Item color is not selected / Item color is selected
        cyclewheelview.setDivider(ContextCompat.getColor(mContext,R.color.description_color),2);//设置中间两根分割线的颜色与高度 Set the color and height of the two split lines in the middle
        try {
            //Set the number of scales that the scroll wheel can display. It must be an odd number and greater than or equal to 3, for example: 3, 5, 7
            cyclewheelview.setWheelSize(3);//设置滚轮可显示的刻度数量，必须为奇数，且大于等于3，例如：3,5,7
        } catch (CycleWheelView.CycleWheelViewException e) {
            e.printStackTrace();
        }
    }
}
