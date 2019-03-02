package com.blueduck.ride.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.blueduck.ride.R;

/**
 * 版本更新提示对话框
 * Version update prompt dialog box
 */
public class VersionHintDialog extends Dialog implements View.OnClickListener{

    private String content;
    public interface VersionUpdateCallBack{
        void updateCall();
    }
    private VersionUpdateCallBack callBack;

    public VersionHintDialog(Context context, String content, VersionUpdateCallBack callBack, int themeResId) {
        super(context, R.style.myDialog);
        this.content = content;
        this.callBack = callBack;
        show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.version_hint_dialog);
        setCanceledOnTouchOutside(true);
        initView();
    }

    private void initView(){
        findViewById(R.id.update_now_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.update_now_btn:
                callBack.updateCall();
                dismiss();
                break;
        }
    }
}
