package com.example.hx.ihanc;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.hx.ihanc.creditFragment.OnListFragmentInteractionListener;
import com.example.hx.ihanc.dummy.DummyContent.DummyItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MycreditRecyclerViewAdapter extends RecyclerView.Adapter<MycreditRecyclerViewAdapter.ViewHolder> {

    private final List<Credit> mValues;
    private final OnListFragmentInteractionListener mListener;
    public View view;
    public final int TYPE_FOOT=0x12345678;
    public final int TYPE_NORMAL=0x22345678;
    public boolean isLast=false;

    public MycreditRecyclerViewAdapter(List<Credit> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType==TYPE_NORMAL) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_credit, parent, false);
            this.view = view;
            return new ViewHolder(view,viewType);
        }else{
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycle_foot, parent, false);
            this.view = view;
            return new ViewHolder(view,viewType);
        }

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if(position<mValues.size()) {
            holder.mItem = mValues.get(position);
            holder.mNameView.setText(mValues.get(position).getName());
            holder.mSumView.setText(mValues.get(position).getCreditSum());
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        mListener.onListFragmentInteraction(holder.mItem);
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
        public final TextView mNameView;
        public final TextView mSumView;
        public  Credit mItem;
        public ViewHolder(View view,int viewType) {
            super(view);
            mView = view;
            if(viewType==TYPE_NORMAL) {
                mNameView = (TextView) view.findViewById(R.id.item_member_name);
                mSumView = (TextView) view.findViewById(R.id.item_sum);
            }else{
                mNameView = (TextView) view.findViewById(R.id.footer);
                mSumView=null;
            }
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}
