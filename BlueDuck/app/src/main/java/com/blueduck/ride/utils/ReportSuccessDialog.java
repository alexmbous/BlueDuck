package com.blueduck.ride.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.blueduck.ride.R;

/**
 * 报故障成功对话框
 * Failure success dialog box
 */
public class ReportSuccessDialog extends Dialog{

    public ReportSuccessDialog(Context context, int themeResId) {
        super(context, R.style.requestDialog);
        show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_success_dialog);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.TOP;
        window.setAttributes(lp);
        setCanceledOnTouchOutside(true);
    }

}
