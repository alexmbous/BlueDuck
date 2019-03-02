package com.blueduck.ride.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.blueduck.ride.R;

/**
 * Credit card settings popup dialog
 * 信用卡设置弹出对话框
 */
public class CardSetDialog extends Dialog{

    public interface CardSetCallBack{
        public void cardSetIndex(int index);
    }
    private CardSetCallBack cardSetCallBack;
    private TextView defaultText;//default
    private TextView deleteText;//delete
    private View line;
    private boolean isDefault;//是否默认卡 Whether the default card

    public CardSetDialog(Context context, CardSetCallBack itemCallBack, boolean isDefault, int themeResId) {
        super(context, R.style.myDialog);
        this.cardSetCallBack = itemCallBack;
        this.isDefault = isDefault;
        show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_set_dialog);
        setCanceledOnTouchOutside(true);
        initViews();
        initListeners();
    }

    private void initViews(){
        defaultText = (TextView) findViewById(R.id.card_set_text_one);
        deleteText = (TextView) findViewById(R.id.card_set_text_two);
        line = findViewById(R.id.card_set_line);
        if (isDefault){
            defaultText.setVisibility(View.GONE);
            line.setVisibility(View.GONE);
        }
    }

    private void initListeners(){
        defaultText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardSetCallBack.cardSetIndex(0);
                dismiss();
            }
        });
        deleteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardSetCallBack.cardSetIndex(1);
                dismiss();
            }
        });
    }
}
