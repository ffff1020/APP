package com.example.hx.ihanc;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

public class GoodsAdapter extends ArrayAdapter<Goods> {
    private Context mContext;
    private int resource;
    private List<Goods> mList;
    public GoodsAdapter(@NonNull Context context, int resource, @NonNull List<Goods>objects) {
        super(context, resource, objects);
        this.mContext=context;
        this.resource=resource;
        this.mList=objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView==null){
            viewHolder=new ViewHolder();
            convertView= LayoutInflater.from(mContext).inflate(resource,null);
            viewHolder.tvTitle=convertView.findViewById(R.id.title);
            convertView.setTag(viewHolder);
            return convertView;
        }else{
            viewHolder=(ViewHolder) convertView.getTag();
        }
        viewHolder.tvTitle.setText(mList.get(position).getGoods_name());
        return convertView;
    }

}
