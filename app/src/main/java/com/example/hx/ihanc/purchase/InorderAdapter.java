package com.example.hx.ihanc.purchase;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import com.example.hx.ihanc.R;
import com.example.hx.ihanc.Unit;
import com.example.hx.ihanc.ViewHolder;

import java.util.List;

public class InorderAdapter extends ArrayAdapter<String> implements SpinnerAdapter {
    private int resource;
    private Context mContext;
    private List<String> data;
    public InorderAdapter(Context context, int resource, @NonNull List<String> objects){
        super(context,resource,objects);
        this.resource=resource;
        mContext=context;
        data=objects;
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
            viewHolder.tvTitle.setTextColor(Color.RED);
        }else{
            viewHolder=(ViewHolder) convertView.getTag();
        }
        viewHolder.tvTitle.setText(data.get(position));
        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView==null){
            viewHolder=new ViewHolder();
            convertView= LayoutInflater.from(mContext).inflate(resource,null);
            viewHolder.tvTitle=convertView.findViewById(R.id.unit);
            convertView.setTag(viewHolder);
            viewHolder.tvTitle.setTextColor(Color.RED);
        }else{
            viewHolder=(ViewHolder) convertView.getTag();
        }
        viewHolder.tvTitle.setText(data.get(position));
        //  Log.d("unitAdapter",displayUnitList.get(position).getUnit_name());
        return convertView;
    }
}
