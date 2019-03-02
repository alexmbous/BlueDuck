package com.blueduck.ride.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.blueduck.ride.R;


/**
 * Time-consuming request dialog
 * 耗时请求对话框
 */
public class RequestDialog extends Dialog {

    private static RequestDialog requestDialog;
    private static TextView dialog_message;
    private static String myMessage;

    public RequestDialog(Context context, int themeResId) {
        super(context, R.style.requestDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_dialog_layout);
        initView();
        setCanceledOnTouchOutside(false);
    }
    private void initView(){
        dialog_message = (TextView) findViewById(R.id.request_dialog_message);
        if (!TextUtils.isEmpty(myMessage)){
            dialog_message.setText(myMessage);
        }
    }

    public static void show(Context context){
        if (requestDialog == null) {
            requestDialog = new RequestDialog(context, 0);
        }
        requestDialog.show();
    }

    public static void show(Context context, String message){
        myMessage = message;
        if (requestDialog == null) {
            requestDialog = new RequestDialog(context, 0);
        }
        requestDialog.show();
    }

    public static void dismiss(Context context){
        if (requestDialog != null){
            requestDialog.dismiss();
            myMessage = null;
            requestDialog = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (requestDialog != null){
            requestDialog.dismiss();
            myMessage = null;
            requestDialog = null;
        }
    }
}
