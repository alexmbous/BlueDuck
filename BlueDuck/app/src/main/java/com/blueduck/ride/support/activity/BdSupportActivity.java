package com.blueduck.ride.support.activity;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.blueduck.ride.R;
import com.blueduck.ride.base.BaseActivity;

public class BdSupportActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "BdSupportActivity";
    private LinearLayout callSupportLayout;
    private ImageView backImg;

    @Override
    protected int setLayoutViewId() {
        return R.layout.support_activity;
    }

    @Override
    protected void initView() {
        baseTitleLayout.setVisibility(View.VISIBLE);
        baseTitleText.setText(getString(R.string.support_title));
        backImg = (ImageView) findViewById(R.id.title_left_image);
        backImg.setImageResource(R.drawable.menu);
        callSupportLayout = (LinearLayout) findViewById(R.id.call_support_layout);
        callSupportLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.call_support_layout:
                openDialer();
                break;
        }
    }

    public void openDialer() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        // Send phone number to intent as data
        intent.setData(Uri.parse("tel:" + "+18332583382"));
        // Start the dialer app activity with number
        startActivity(intent);
    }
}
