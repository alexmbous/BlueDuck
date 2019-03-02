package com.blueduck.ride.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.blueduck.ride.R;

import java.util.ArrayList;
import java.util.List;


/**
 * 故障选择对话框
 * Failure selection dialog box
 */
public class ReportDialog extends Dialog implements View.OnClickListener{

    private Context mContext;
    private CycleWheelView issueList;
    private List<String> issues = new ArrayList<>();

    public interface ConfirmBtn{
        public void confirm(int type,String issueStr);
    }
    private ConfirmBtn confirmBtn;

    public ReportDialog(Context context, ConfirmBtn confirmBtn, List<String> issues,int themeResId) {
        super(context, R.style.myDialog);
        this.confirmBtn = confirmBtn;
        this.mContext = context;
        this.issues = issues;
        show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_dialog);
        setCanceledOnTouchOutside(true);
        initView();
    }

    private void initView(){
        issueList = (CycleWheelView) findViewById(R.id.issue_list);
        findViewById(R.id.confirm_btn).setOnClickListener(this);
        setCycleWheelViewParameter(issueList,issues,0);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.confirm_btn:
                confirmBtn.confirm(issueList.getSelection(),issueList.getSelectLabel());
                dismiss();
                break;
        }
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
