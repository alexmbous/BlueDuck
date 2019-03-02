package com.blueduck.ride.billing.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blueduck.ride.R;
import com.blueduck.ride.billing.activity.BillingActivity;
import com.blueduck.ride.billing.bean.CardBean;
import com.blueduck.ride.billing.bean.CardListBean;


public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder>{

    private Context mContext;
    private CardBean cardBean;

    public void setCardBean(CardBean cardBean) {
        this.cardBean = cardBean;
    }

    public CardAdapter(Context context, CardBean cardBean){
        this.mContext = context;
        this.cardBean = cardBean;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.card_item_layout,parent,false);
        final ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                Intent intent = new Intent();
                intent.setAction(BillingActivity.CARD_ITEM_CLICK);
                intent.putExtra("position", position);
                mContext.sendBroadcast(intent);
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardListBean cardListBean = cardBean.getPaymentList().get(position);
        holder.number.setText("**** **** **** "+cardListBean.getLast4());
        if (cardBean.getDefaultPaymentId().equals(cardListBean.getId())){
            holder.defaultText.setVisibility(View.VISIBLE);
        }else{
            holder.defaultText.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return cardBean.getPaymentList().size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        RelativeLayout item;
        TextView number;
        TextView defaultText;

        public ViewHolder(View itemView) {
            super(itemView);
            item = (RelativeLayout) itemView.findViewById(R.id.card_info_item_layout);
            number = (TextView) itemView.findViewById(R.id.card_number);
            defaultText = (TextView) itemView.findViewById(R.id.default_description_text);
        }
    }
}
