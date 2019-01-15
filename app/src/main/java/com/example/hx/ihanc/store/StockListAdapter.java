package com.example.hx.ihanc.store;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.hx.ihanc.R;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Locale;


public class StockListAdapter extends RecyclerView.Adapter<StockListAdapter.ViewHolder> {
    private final ArrayList<stock> mValues;
    public final int TYPE_FOOT=0x12345678;
    public final int TYPE_NORMAL=0x22345678;
    public boolean isLast=false;
    private OnStockItemClickListener itemClickListener;
    public StockListAdapter(ArrayList<stock> data){
        mValues=data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType==TYPE_NORMAL) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.stock_item, parent, false);
            ViewHolder holder=new ViewHolder(view,viewType);
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemClickListener.onItemClick(view);
                }
            });
            return holder;
        }else{
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycle_foot, parent, false);
            return new ViewHolder(view,viewType);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(position<mValues.size()){
            stock item=mValues.get(position);
            if(item.inorder.equals("0"))
                holder.mOrderView.setVisibility(View.GONE);
            else{
                holder.mOrderView.setText(item.inorder);
                holder.mOrderView.setVisibility(View.VISIBLE);
            }
            holder.mGoodsView.setText(item.goods_name);
            NumberFormat f=NumberFormat.getCurrencyInstance(Locale.CHINA);
            holder.mSumView.setText("库存金额："+f.format(item.sum));
            holder.mNumberView.setText("库存数量："+item.number+item.unit_name);
            holder.mView.setTag(position);
        }else{
            if(isLast)  holder.mBankNameView.setText("没有了...");
            else holder.mBankNameView.setText("努力加载中...");
        }
    }


    @Override
    public int getItemCount() {
        return mValues.size()>0?mValues.size()+1:0;
    }

    @Override
    public int getItemViewType(int position) {
        if (position>=mValues.size()){
            return TYPE_FOOT;
        }
        return TYPE_NORMAL;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mOrderView;
        public final TextView mBankNameView;
        public final TextView mNumberView;
        public final TextView mSumView;
        public final TextView mGoodsView;

        public ViewHolder(View view,int viewType) {
            super(view);
            mView = view;
            if(viewType==TYPE_NORMAL) {
                mOrderView = (TextView) view.findViewById(R.id.inorder);
                mGoodsView = (TextView) view.findViewById(R.id.goods);
                mNumberView = (TextView) view.findViewById(R.id.number);
                mSumView = (TextView) view.findViewById(R.id.sum);
                mBankNameView=null;
            }else{
                mOrderView = null;
                mBankNameView = (TextView) view.findViewById(R.id.footer);;
                mNumberView = null;
                mSumView = null;
                mGoodsView=null;
            }
        }
    }

    public  interface OnStockItemClickListener {
        void onItemClick(View view);
    }

    public void setItemClickListener(OnStockItemClickListener listener){
        this.itemClickListener=listener;
    }
}
