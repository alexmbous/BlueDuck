package com.blueduck.ride.history.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.blueduck.ride.R;
import com.blueduck.ride.history.Utils;
import com.blueduck.ride.history.bean.HistoryBean;
import com.blueduck.ride.utils.CurrencyUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class HistoryAdapter extends BaseAdapter {

    private Context mContext;
    private List<HistoryBean> list;
    private LayoutInflater inflater;

    public void setList(List<HistoryBean> list) {
        this.list = list;
    }

    public HistoryAdapter(Context context,List<HistoryBean> list){
        this.mContext = context;
        this.list = list;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public HistoryBean getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null){
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.history_adapter,null);
            viewHolder.date = (TextView) convertView.findViewById(R.id.history_date);
            viewHolder.time = (TextView) convertView.findViewById(R.id.history_time);
            viewHolder.amount = (TextView) convertView.findViewById(R.id.history_amount);
            viewHolder.number = (TextView) convertView.findViewById(R.id.history_number);
            viewHolder.map = (ImageView) convertView.findViewById(R.id.history_map_view);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final HistoryBean historyBean = getItem(position);
        viewHolder.date.setText(Utils.dateFormat(historyBean.getDate()));
        viewHolder.time.setText(Utils.hourMinuteFormat(historyBean.getStartStamp()));
        if (historyBean.getAmount() != null) {
            double amount = Double.parseDouble(historyBean.getAmount());
            viewHolder.amount.setText(mContext.getString(R.string.dollar) + CurrencyUtil.convertToTwoDecimalPlaces(amount));
        }else{
            viewHolder.amount.setText(mContext.getString(R.string.dollar) + 0.0);
        }
        viewHolder.number.setText(mContext.getString(R.string.scooter_number_text)+
                mContext.getString(R.string.blank)+historyBean.getNumber());
        viewHolder.map.post(new Runnable() {
            @Override
            public void run() {
                loadMapView(historyBean,viewHolder.map);
            }
        });
        return convertView;
    }

    private void loadMapView(HistoryBean historyBean,ImageView mapView){
        float screenScale = 2;
        if(mContext.getResources().getDisplayMetrics().density > 2) screenScale = 4;
        float screenHeight = pxToDp(mapView.getHeight());
        float screenWidth =  pxToDp(mapView.getWidth());
        Uri google = Utils.getGoogleStaticMapURL(
                (int)screenWidth,
                (int)screenHeight,
                (int)screenScale,
                historyBean.getOrbit(),
                Double.parseDouble(historyBean.getStartLat()),
                Double.parseDouble(historyBean.getStartLng()),
                Double.parseDouble(historyBean.getEndLat()),
                Double.parseDouble(historyBean.getEndLng())
        );
        Glide.with(mContext.getApplicationContext())
                .load(google)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mapView);
    }

    public int pxToDp(int px) {
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }

    private class ViewHolder{
        TextView date;
        TextView time;
        TextView amount;
        TextView number;
        ImageView map;
    }
}
