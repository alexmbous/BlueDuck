package com.blueduck.ride.personal.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.blueduck.ride.R;
import com.blueduck.ride.base.BaseActivity;
import com.blueduck.ride.personal.service.PersonalService;
import com.blueduck.ride.utils.BroadCastValues;
import com.blueduck.ride.utils.CommonSharedValues;
import com.blueduck.ride.utils.RequestCallBack;

public class ResetPasswordActivity extends BaseActivity implements RequestCallBack {

    private static final String TAG = "ResetPasswordActivity";

    private EditText newPassEt,confirmPassEt;
    private Button saveBtn;
    private String newPas,confirmPas;
    private PersonalService personalService;

    @Override
    protected int setLayoutViewId() {
        return R.layout.reset_password_activity;
    }

    @Override
    protected void init() {
        personalService = new PersonalService(this,this,TAG);
    }

    @Override
    protected void initView() {
        baseTitleLayout.setVisibility(View.VISIBLE);
        baseTitleText.setText(getString(R.string.reset_password));
        newPassEt = (EditText) findViewById(R.id.new_password_edit);
        newPassEt.addTextChangedListener(new MyTextWatcher());
        confirmPassEt = (EditText) findViewById(R.id.confirm_new_password_edit);
        confirmPassEt.addTextChangedListener(new MyTextWatcher());
        saveBtn = (Button) findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);
        saveBtn.setEnabled(false);
    }

    private class MyTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
        @Override
        public void afterTextChanged(Editable s) {
            checkData();
        }
    }

    private void checkData(){
        newPas = newPassEt.getText().toString().trim();
        confirmPas = confirmPassEt.getText().toString().trim();
        if (TextUtils.isEmpty(newPas) || TextUtils.isEmpty(confirmPas)){
            saveBtn.setEnabled(false);
            saveBtn.setBackground(ContextCompat.getDrawable(ResetPasswordActivity.this,R.drawable.skip_gray_btn_bg));
        }else{
            saveBtn.setEnabled(true);
            saveBtn.setBackground(ContextCompat.getDrawable(ResetPasswordActivity.this,R.drawable.skip_btn_bg));
        }
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.save_btn:
                saveBottom();
                break;
        }
    }

    private void saveBottom(){
        checkData();
        if (newPas.length() < 8 || confirmPas.length() < 8){
            Toast.makeText(this,getString(R.string.password_length_hint),Toast.LENGTH_SHORT).show();
        }else if (!newPas.equals(confirmPas)){
            Toast.makeText(this,getString(R.string.unlike),Toast.LENGTH_SHORT).show();
        }else{
            submitPassword(confirmPas);
        }
    }

    /**
     * 提交密码
     * Submit the password
     * @param password
     */
    private void submitPassword(String password){
        personalService.setPassword(sp.getString(CommonSharedValues.SP_KEY_TOKEN,""),password,1);
    }

    private void handlerSubmitSuccess(){
        SharedPreferences.Editor editor= sp.edit();
        editor.putString(CommonSharedValues.SP_KEY_PASSWORD,confirmPas);
        editor.commit();
        sendBroadcast(new Intent(MyAccountActivity.CHANGE_SUCCESS));
        sendBroadcast(new Intent(BroadCastValues.FINISH_BROAD));
        finish();
    }

    @Override
    public void onSuccess(Object o, int flag) {
        if (flag == 1){
            handlerSubmitSuccess();
        }
    }

    @Override
    public void onFail(Throwable t, int flag) {

    }
}
