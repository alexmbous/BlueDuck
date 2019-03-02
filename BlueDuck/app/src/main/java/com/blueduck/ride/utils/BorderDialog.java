package com.blueduck.ride.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.blueduck.ride.R;

/**
 * 滑板车越界提示对话框
 * Prompt dialog box for scooter crossing the boundary
 */
public class BorderDialog extends Dialog{

    private TextView content;

    public BorderDialog(Context context, int themeResId) {
        super(context, R.style.myDialog);
        show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.border_dialog);
        setCanceledOnTouchOutside(true);
        initView();
    }

    private void initView(){
        content = (TextView) findViewById(R.id.parking_dialog_content);
    }

    public void setContent(String str){
        content.setText(str);
    }

}
