package com.example.hx.ihanc;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class SaleDetailAdapter extends ArrayAdapter {
    private Context mContext;
    private List<SaleDetail> saleDetailList;
    private int resource;
    private MyClickListener mListener;
    private boolean modify=false;

    public SaleDetailAdapter(Context context, int resource, @NonNull List<SaleDetail> saleDetailList, MyClickListener listener){
        super(context, resource, saleDetailList);
        this.mContext=context;
        this.saleDetailList=saleDetailList;
        this.resource=resource;
        this.mListener=listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView==null){
            viewHolder=new ViewHolder();
            convertView= LayoutInflater.from(mContext).inflate(resource,null);
            viewHolder.tvName=convertView.findViewById(R.id.saleItemName);
            viewHolder.tvNumber=convertView.findViewById(R.id.saleItemNumber);
            viewHolder.tvPrice=convertView.findViewById(R.id.saleItemPrice);
            viewHolder.tvSum=convertView.findViewById(R.id.saleItemSum);
            viewHolder.deleteButton=convertView.findViewById(R.id.saleItemDeleteBtn);
            convertView.setTag(viewHolder);
            //return convertView;
        }else{
            viewHolder=(ViewHolder) convertView.getTag();
        }
        SaleDetail mSaleDetail=(SaleDetail) saleDetailList.get(position);
        viewHolder.tvName.setText((position+1)+"."+mSaleDetail.getGoods_name());
        viewHolder.tvNumber.setText(mSaleDetail.getNumber());
        viewHolder.tvPrice.setText(mSaleDetail.getPrice());
        viewHolder.tvSum.setText("￥"+mSaleDetail.getSum());
        if(mListener==null) {
            //viewHolder.deleteButton.setweig(0);
            //viewHolder.deleteButton.setVisibility(View.INVISIBLE);
        }
        else{
           // viewHolder.deleteButton.setVisibility(View.VISIBLE);
            viewHolder.deleteButton.setOnClickListener(mListener);
            viewHolder.deleteButton.setTag(position);
        }
        if(saleDetailList.get(position).paid)
            viewHolder.deleteButton.setVisibility(View.GONE);
        else
            viewHolder.deleteButton.setVisibility(View.VISIBLE);
        return convertView;
    }
    public class ViewHolder{
        TextView tvName;
        TextView tvNumber;
        TextView tvPrice;
        TextView tvSum;
        TextView deleteButton;
    }

    public static abstract class MyClickListener implements View.OnClickListener {
           @Override
       public void onClick(View v) {
             myOnClick((Integer) v.getTag(), v);
             myModifyClick((Integer) v.getTag(), v);
             Log.d("modify","adapter");
           }
           public abstract void myOnClick(int position, View v);
           public abstract void myModifyClick(int position,View v);
    }
}
