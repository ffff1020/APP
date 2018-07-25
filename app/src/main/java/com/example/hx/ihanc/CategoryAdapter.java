package com.example.hx.ihanc;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

public class CategoryAdapter extends ArrayAdapter<category> {
    private Context mContext;
    private List<category> categoryList;
    private int resource;
    public CategoryAdapter(@NonNull Context context, int resource, @NonNull List<category> categoryList) {
        super(context, resource, categoryList);
        this.mContext=context;
        this.categoryList=categoryList;
        this.resource=resource;
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
        viewHolder.tvTitle.setText(categoryList.get(position).getCategory_name());
        return convertView;
    }
}
