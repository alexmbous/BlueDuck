package com.blueduck.ride.utils;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.blueduck.ride.R;


/**
 * Customize PopupWindow (select popup)
 * 自定义PopupWindow(选择弹出框)
 */

public class SelectPopupWindow extends PopupWindow{

    private static final String TAG = "MyPopupWindow";

    public interface SelectPopupWindowCall{
        void windowCall(int index);
    }

    public SelectPopupWindow(final Activity activity, final SelectPopupWindowCall windowCall){
        View view = LayoutInflater.from(activity).inflate(R.layout.select_popup_window,null);
        view.findViewById(R.id.popup_button_layout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_UP:
                        dismiss();
                        return true;
                }
                return false;
            }
        });
        view.findViewById(R.id.camera_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                windowCall.windowCall(0);
                dismiss();
            }
        });
        view.findViewById(R.id.from_library_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                windowCall.windowCall(1);
                dismiss();
            }
        });
        view.findViewById(R.id.window_cancel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(activity,R.color.transparent)));
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundAlpha(activity,0.5f);
        setContentView(view);
        setAnimationStyle(R.style.SelectPopupAnimation);
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                setBackgroundAlpha(activity,1f);
            }
        });
    }

    /**
     * Set window transparent background
     * 设置窗口透明背景
     * @param activity
     * @param bgAlpha
     */
    private void setBackgroundAlpha(Activity activity, float bgAlpha) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgAlpha;//0.0-1.0
        if (bgAlpha == 1) {
            //If you do not remove the Flag, there will be a black screen bug on the video on the page with the video.
            //不移除该Flag的话,在有视频的页面上的视频会出现黑屏的bug
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        } else {
            //This line of code is mainly to solve the bug that the translucent effect on Huawei mobile phone is invalid.
            //此行代码主要是解决在华为手机上半透明效果无效的bug
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
        activity.getWindow().setAttributes(lp);
    }

}
