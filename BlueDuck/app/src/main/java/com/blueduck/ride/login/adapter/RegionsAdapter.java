package com.blueduck.ride.login.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.blueduck.ride.R;
import com.blueduck.ride.login.bean.RegionsBean;

import java.util.List;

public class RegionsAdapter extends BaseAdapter {
    private Context myContext;
    private List<RegionsBean> list;
    private LayoutInflater inflater;
    private SharedPreferences shared;

    public RegionsAdapter(Context context, List<RegionsBean> list, SharedPreferences shared){
        this.myContext = context;
        this.list = list;
        this.shared = shared;
        inflater = LayoutInflater.from(myContext);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public RegionsBean getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null){
            viewHolder = new ViewHolder();
            view = inflater.inflate(R.layout.regions_adapter,null);
            viewHolder.name = (TextView) view.findViewById(R.id.regions_name);
            viewHolder.value = (TextView) view.findViewById(R.id.regions_value);
            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) view.getTag();
        }
        RegionsBean regionsBean = getItem(position);
        viewHolder.name.setText(regionsBean.getEnglish_name());
        viewHolder.value.setText("+"+regionsBean.getPhone_code());
        return view;
    }
    private class ViewHolder{
        TextView name;
        TextView value;
    }
}
