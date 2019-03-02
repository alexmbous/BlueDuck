package com.blueduck.ride.main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.blueduck.ride.R;
import com.blueduck.ride.main.bean.MenuListBean;

import java.util.List;

/**
 * 侧滑菜单适配器
 * Sideslip menu adapter
 */
public class MenuListAdapter extends BaseAdapter{

    private Context myContext;
    private List<MenuListBean> list;
    private LayoutInflater inflater;

    public MenuListAdapter(Context context, List<MenuListBean> list){
        this.myContext = context;
        this.list = list;
        inflater = LayoutInflater.from(myContext);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public MenuListBean getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View contentView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (contentView == null){
            viewHolder = new ViewHolder();
            contentView = inflater.inflate(R.layout.menu_list_item,null);
            viewHolder.image = (ImageView) contentView.findViewById(R.id.menu_item_image);
            viewHolder.title = (TextView) contentView.findViewById(R.id.meu_item_title);
            contentView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) contentView.getTag();
        }
        MenuListBean menuListBean = getItem(position);
        viewHolder.image.setImageResource(menuListBean.getImageId());
        viewHolder.title.setText(menuListBean.getTitle());
        return contentView;
    }

    private class ViewHolder{
        ImageView image;
        TextView title;
    }
}
