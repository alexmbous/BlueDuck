package com.blueduck.ride.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.blueduck.ride.R;

/**
 * 无银行卡提示添加银行卡对话框
 * No bank card prompt to add bank card dialog box
 */
public class BindCardDialog extends Dialog implements View.OnClickListener{


    public interface AddCardBtn{
        public void addClick(View view);
    }

    private AddCardBtn addCardBtn;

    public BindCardDialog(Context context, AddCardBtn addCardBtn, int themeResId) {
        super(context, R.style.myDialog);
        this.addCardBtn = addCardBtn;
        show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bind_card_dialog);
        setCanceledOnTouchOutside(true);
        findViewById(R.id.close_layout).setOnClickListener(this);
        findViewById(R.id.payment_add_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.close_layout:
                dismiss();
                break;
            case R.id.payment_add_btn:
                addCardBtn.addClick(view);
                break;
        }
    }
}
