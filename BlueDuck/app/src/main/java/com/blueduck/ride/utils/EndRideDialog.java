package com.blueduck.ride.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.blueduck.ride.R;

/**
 * 骑行结束对话框
 * End of ride dialog box
 */
public class EndRideDialog extends Dialog implements View.OnClickListener{

    public interface EndRideCallBack{
        void endRideCall();
    }
    private EndRideCallBack callBack;

    public EndRideDialog(Context context, EndRideCallBack callBack, int themeResId) {
        super(context, R.style.myDialog);
        this.callBack = callBack;
        show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.end_ride_dialog);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.TOP;
        window.setAttributes(lp);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        initView();
    }

    private void initView(){
        findViewById(R.id.end_ride_close).setOnClickListener(this);
        findViewById(R.id.end_ride_safely_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.end_ride_close:
                dismiss();
                break;
            case R.id.end_ride_safely_btn:
                callBack.endRideCall();
                dismiss();
                break;
        }
    }
}
