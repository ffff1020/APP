package com.example.hx.ihanc;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import java.util.List;

public class BankAdapter extends ArrayAdapter implements SpinnerAdapter {
    private List<bank> bankList;
    private int resource;
    private Context mContext;
    public BankAdapter(Context context, int resource, @NonNull List<bank> objects){
        super(context, resource, objects);
        this.mContext=context;
        this.resource=resource;
        this.bankList=objects;
    }
    @Override
    public int getCount() {
        return bankList.size();
    }

    @Nullable
    @Override
    public bank getItem(int position) {
        return bankList.get(position);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView== null){
            holder= new ViewHolder();
            convertView= LayoutInflater.from(mContext).inflate(resource,null);
            holder.tvTitle= convertView.findViewById(R.id.unit);
            convertView.setTag(holder);
        }else {
            holder= (ViewHolder) convertView.getTag();
        }
        holder.tvTitle.setText(bankList.get(position).getName());
        return convertView;
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
        }else{
            viewHolder=(ViewHolder) convertView.getTag();
        }
        viewHolder.tvTitle.setText(bankList.get(position).getName());
        Log.d("bankAdapter",bankList.get(position).getName());
        return convertView;
    }
}
