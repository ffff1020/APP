package com.example.hx.ihanc.store;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.hx.ihanc.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class StockDetailListAdapter extends RecyclerView.Adapter<StockDetailListAdapter.ViewHolder> {
    private final ArrayList<StockDetail> data;
    public final int TYPE_FOOT=0x12345678;
    public final int TYPE_NORMAL=0x22345678;
    public boolean isLast=false;
    private NumberFormat f=NumberFormat.getCurrencyInstance(Locale.CHINA);
    public StockDetailListAdapter(ArrayList<StockDetail> data){
        this.data=data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==TYPE_NORMAL) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.stock_detail_item, parent, false);
            StockDetailListAdapter.ViewHolder holder=new StockDetailListAdapter.ViewHolder(view,viewType);
            return holder;
        }else{
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycle_foot, parent, false);
            return new StockDetailListAdapter.ViewHolder(view,viewType);
        }
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(position<data.size()){
            StockDetail item=data.get(position);
            holder.mTimeView.setText(item.time);
            if(item.type.equals("purchase"))
                 holder.mRemarkView.setText("进货");
            else if(item.type.equals("sale"))
                holder.mRemarkView.setText("销售");
            else holder.mRemarkView.setText(item.remark);
            holder.mSumView.setText(f.format(item.sum));
            holder.mStockView.setText(item.stock+"");
            holder.mDSumView.setText(f.format(item.Dsum));
            holder.mDstockView.setText(item.Dstock+"");
            holder.mUserView.setText(item.user);
        }else{
            if(isLast)  holder.mBankNameView.setText("没有了...");
            else holder.mBankNameView.setText("努力加载中...");
        }
    }
    @Override
    public int getItemCount() {
        return data.size()>0?data.size()+1:0;
    }
    @Override
    public int getItemViewType(int position) {
        if (position>=data.size()){
            return TYPE_FOOT;
        }
        return TYPE_NORMAL;
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        public final View mView;
        public final TextView mBankNameView;
        public final TextView mTimeView;
        public final TextView mUserView;
        public final TextView mDstockView;
        public final TextView mDSumView;
        public final TextView mStockView;
        public final TextView mSumView;
        public final TextView mRemarkView;
        public ViewHolder(View view,int viewType){
            super(view);
            mView=view;
            if(viewType==TYPE_NORMAL) {
                mTimeView=(TextView) view.findViewById(R.id.time);
                mUserView=(TextView) view.findViewById(R.id.user);
                mDstockView=(TextView) view.findViewById(R.id.Dstock);
                mDSumView=(TextView) view.findViewById(R.id.Dsum);
                mStockView=(TextView) view.findViewById(R.id.stock);
                mSumView=(TextView) view.findViewById(R.id.sum);
                mRemarkView=(TextView) view.findViewById(R.id.remark);
                mBankNameView=null;
            }else{
                mBankNameView = (TextView) view.findViewById(R.id.footer);
                mTimeView=null;
                mUserView=null;
                mDstockView=null;
                mDSumView=null;
                mStockView=null;
                mSumView=null;
                mRemarkView=null;
            }
        }
    }
}
