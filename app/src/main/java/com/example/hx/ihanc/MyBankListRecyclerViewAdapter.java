package com.example.hx.ihanc;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.hx.ihanc.BankListFragment.OnListFragmentInteractionListener;


import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link SaleListItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyBankListRecyclerViewAdapter extends RecyclerView.Adapter<MyBankListRecyclerViewAdapter.ViewHolder> {

    private final List<BankListItem> mValues;
    private final OnListFragmentInteractionListener mListener;
    public final int TYPE_FOOT=0x12345678;
    public final int TYPE_NORMAL=0x22345678;
    public boolean isLast=false;

    public MyBankListRecyclerViewAdapter(List<BankListItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType==TYPE_NORMAL) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_banklist, parent, false);
            return new ViewHolder(view,viewType);
        }else{
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycle_foot, parent, false);
            return new ViewHolder(view,viewType);
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if(position<mValues.size()){
            holder.mTimeView.setText(mValues.get(position).time);
            holder.mBankNameView.setText(mValues.get(position).bankName);
            holder.mSummaryView.setText(mValues.get(position).summary);
            holder.mSumView.setText(mValues.get(position).sum);
        }else{
            if(isLast)  holder.mBankNameView.setText("没有了...");
            else holder.mBankNameView.setText("努力加载中...");
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                   // mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
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
        public final TextView mBankNameView;
        public final TextView mSummaryView;
        public final TextView mSumView;

        public ViewHolder(View view,int viewType) {
            super(view);
            mView = view;
            if(viewType==TYPE_NORMAL) {
                mTimeView = (TextView) view.findViewById(R.id.item_time);
                mBankNameView = (TextView) view.findViewById(R.id.item_bankName);
                mSummaryView = (TextView) view.findViewById(R.id.item_summary);
                mSumView = (TextView) view.findViewById(R.id.item_sum);
            }else{
                mTimeView = null;
                mBankNameView = (TextView) view.findViewById(R.id.footer);;
                mSummaryView = null;
                mSumView = null;
            }
        }


    }
}
