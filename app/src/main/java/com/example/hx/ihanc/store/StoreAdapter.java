package com.example.hx.ihanc.store;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.hx.ihanc.R;
import com.example.hx.ihanc.Unit;
import com.example.hx.ihanc.ViewHolder;

import java.util.ArrayList;
import java.util.List;

public class StoreAdapter extends ArrayAdapter<store> {
    private  Context mContext;
    private  int resource;
    private ArrayList<store> stores;
    public StoreAdapter(Context context, int resource, @NonNull ArrayList<store> objects){
        super(context, resource, objects);
        this.mContext=context;
        this.resource=resource;
        this.stores=objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView==null){
            viewHolder=new ViewHolder();
            convertView= LayoutInflater.from(mContext).inflate(resource,null);
            viewHolder.tvTitle=convertView.findViewById(R.id.unit);
            convertView.setTag(viewHolder);
            viewHolder.tvTitle.setText(stores.get(position).getStore_name());
        }else{
            viewHolder=(ViewHolder) convertView.getTag();
        }
        viewHolder.tvTitle.setText(stores.get(position).getStore_name());
        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView== null){
            holder= new ViewHolder();
            convertView= LayoutInflater.from(mContext).inflate(resource,null);
            holder.tvTitle= convertView.findViewById(R.id.unit);
            convertView.setTag(holder);
        }else {
            holder= (ViewHolder) convertView.getTag();
        }
        holder.tvTitle.setText(stores.get(position).getStore_name());
        return convertView;
    }
}
