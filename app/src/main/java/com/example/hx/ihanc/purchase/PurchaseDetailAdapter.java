package com.example.hx.ihanc.purchase;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hx.ihanc.R;


import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class PurchaseDetailAdapter extends RecyclerView.Adapter<PurchaseDetailAdapter.ViewHolder> {
    private ArrayList<PurchaseDetail> data;
    public final int TYPE_NORMAL=0x22345678;
    public final int TYPE_TTL=0x32345678;
    public boolean isLast=false;
    public boolean showAdd=true;
    private PurchaseDetailListener mListener;
    public double ttl_number=0;
    public int ttl_sum=0;

    private NumberFormat f=NumberFormat.getCurrencyInstance(Locale.CHINA);
    public PurchaseDetailAdapter(ArrayList<PurchaseDetail> data){
        this.data=data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=null;
        switch (viewType){
            case TYPE_NORMAL:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.purchase_detail_adapter_normal, parent, false);
                break;
            case TYPE_TTL:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.purchase_detail_adpter_ttl, parent, false);
                break;
        }
        return new PurchaseDetailAdapter.ViewHolder(view,viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
         if(position<data.size()&&data.size()>0){
             PurchaseDetail detail=data.get(position);
            holder.goods_name.setText(position+1+"."+detail.goods.getGoods_name());
            holder.number.setText(detail.number+detail.unit_name);
            holder.price.setText(f.format(detail.price));
            holder.sum.setText(f.format(detail.sum));
            holder.store.setText(detail.store_name);
            holder.update_button.setVisibility(detail.paid?View.GONE:View.VISIBLE);
            holder.update_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.purchaseAdd(position);
                }
            });
         }else{
             holder.ttl_info.setVisibility(data.size()==0||!showAdd?View.GONE:View.VISIBLE);
             holder.addButton.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     mListener.purchaseAdd(-1);
                 }
             });
             holder.ttl_sum.setText(f.format(ttl_sum));
             holder.ttl_number.setText(ttl_number+"");
         }
    }

    @Override
    public int getItemCount() {
        return data.size()+1;
    }
    @Override
    public int getItemViewType(int position) {
        if (position==data.size()||data.size()==0){
            return TYPE_TTL;
        }
        return TYPE_NORMAL;
    }

    public class ViewHolder  extends RecyclerView.ViewHolder{
        private View view;
        private final TextView ttl_number;
        private final TextView ttl_sum;
        private final Button addButton;
        private final LinearLayout ttl_info;
        private final TextView goods_name;
        private final TextView number;
        private final TextView price;
        private final TextView sum;
        private final Button update_button;
        private final TextView store;
        public ViewHolder(View view,int viewType){
            super(view);
            this.view=view;
            if (viewType==TYPE_TTL){
                ttl_number=(TextView) view.findViewById(R.id.ttlNumber);
                ttl_sum=(TextView)view.findViewById(R.id.ttlSum);
                addButton=(Button)view.findViewById(R.id.addButton);
                if (!showAdd)addButton.setVisibility(View.GONE);
                ttl_info=view.findViewById(R.id.ttlInfo);
                goods_name=null;
                number=null;
                price=null;
                sum=null;
                update_button=null;
                store=null;
            }else{
                ttl_number=null;
                ttl_sum=null;
                addButton=null;
                ttl_info=null;
                goods_name=(TextView) view.findViewById(R.id.goods_name);
                number=(TextView) view.findViewById(R.id.number);
                price=(TextView) view.findViewById(R.id.price);
                sum=(TextView) view.findViewById(R.id.sum);
                update_button=(Button)view.findViewById(R.id.update_button);
                store=(TextView) view.findViewById(R.id.store);
            }
        }
    }

    public interface PurchaseDetailListener{
        void purchaseAdd(int position);
    }

    public void setPurchaseDetailListener(PurchaseDetailListener mListener) {
        this.mListener = mListener;
    }


}
