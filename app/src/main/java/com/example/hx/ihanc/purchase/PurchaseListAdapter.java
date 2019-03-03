package com.example.hx.ihanc.purchase;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.hx.ihanc.MysaleListRecyclerViewAdapter;
import com.example.hx.ihanc.R;
import com.example.hx.ihanc.SaleListItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PurchaseListAdapter extends RecyclerView.Adapter<PurchaseListAdapter.ViewHolder> {
    public View view;
    public final int TYPE_FOOT=0x12345678;
    public final int TYPE_NORMAL=0x22345678;
    public boolean isLast=false;
    private final List<SaleListItem> mValues;
    private OnPurchaseListFragmentListener mListener=null;

    public PurchaseListAdapter(List<SaleListItem> Values){
        this.mValues=Values;
    }
    @Override
    public PurchaseListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType==TYPE_NORMAL) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_salelist, parent, false);
            this.view = view;
            return new PurchaseListAdapter.ViewHolder(view,viewType);
        }else{
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycle_foot, parent, false);
            this.view = view;
            return new PurchaseListAdapter.ViewHolder(view,viewType);
        }
    }

    @Override
    public void onBindViewHolder(final PurchaseListAdapter.ViewHolder holder, int position) {
        if(position<mValues.size()) {
            NumberFormat format = NumberFormat.getCurrencyInstance(Locale.CHINA);
            holder.mItem = mValues.get(position);
            holder.mTimeView.setText(mValues.get(position).time);
            holder.mNameView.setText(mValues.get(position).name);
            holder.mSumView.setText(format.format(Integer.parseInt(mValues.get(position).sum)));
            if (!mValues.get(position).paid) {
                holder.mTimeView.setTextColor(view.getResources().getColor(R.color.colorAccent));
                holder.mNameView.setTextColor(view.getResources().getColor(R.color.colorAccent));
                holder.mSumView.setTextColor(view.getResources().getColor(R.color.colorAccent));
            }else{
                holder.mTimeView.setTextColor(view.getResources().getColor(R.color.black));
                holder.mNameView.setTextColor(view.getResources().getColor(R.color.black));
                holder.mSumView.setTextColor(view.getResources().getColor(R.color.black));
            }
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        mListener.OnPurchaseListFragmentListener(holder.mItem);
                    }
                }
            });
        }else{
            if(isLast)  holder.mNameView.setText("没有了...");
            else holder.mNameView.setText("努力加载中...");
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
        public final TextView mTimeView;
        public final TextView mNameView;
        public final TextView mSumView;
        public SaleListItem mItem;

        public ViewHolder(View view,int viewType) {
            super(view);
            mView = view;
            if(viewType==TYPE_NORMAL) {
                mTimeView = (TextView) view.findViewById(R.id.item_time);
                mNameView = (TextView) view.findViewById(R.id.item_name);
                mSumView = (TextView) view.findViewById(R.id.item_sum);
            }else{
                mTimeView = null;
                mNameView = (TextView) view.findViewById(R.id.footer);
                mSumView = null;
            }

        }

        @Override
        public String toString() {
            return super.toString() + " '" +mNameView==null?"":mNameView.getText() + "'";
        }
    }

    public interface OnPurchaseListFragmentListener{
        void OnPurchaseListFragmentListener(SaleListItem item);
    }

    public void setOnPurchaseListener(OnPurchaseListFragmentListener listener){
        this.mListener=listener;
    }

}
